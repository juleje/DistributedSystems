package be.kuleuven.distributedsystems.cloud.controller.pubsub;

import be.kuleuven.distributedsystems.cloud.controller.sendgrid.EmailController;
import be.kuleuven.distributedsystems.cloud.entities.Booking;
import be.kuleuven.distributedsystems.cloud.entities.Quote;
import be.kuleuven.distributedsystems.cloud.entities.Ticket;
import be.kuleuven.distributedsystems.cloud.persistance.FirestoreRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static be.kuleuven.distributedsystems.cloud.controller.WEBClient.isReliableTrainCompany;
import static be.kuleuven.distributedsystems.cloud.controller.WEBClient.isUnReliableTrainCompany;

@RestController
@RequestMapping("/subscription")
public class TicketStore {

    @Resource(name="webClientBuilder")
    private WebClient.Builder builder;
    private final WebClient webClientReliableTrains;
    private final WebClient webClientUnReliableTrains;
    private final String trainsKey = "JViZPgNadspVcHsMbDFrdGg0XXxyiE";
    @Autowired
    private FirestoreRepository firestore;

    @Autowired
    private EmailController emailController;

    @Autowired
    public TicketStore(WebClient.Builder builder){
        this.builder = builder;

        this.webClientReliableTrains =builder
                .baseUrl("https://reliabletrains.com")
                .build();
        this.webClientUnReliableTrains = builder
                .baseUrl("https://unreliabletrains.com")
                .build();
    }

    @PostMapping("confirmQuotes")
    public void processSubscriberMessage(@RequestBody LinkedHashMap body) throws Exception {
        System.out.println("Message deliverd");
        LinkedHashMap<String, String> wrapped = (LinkedHashMap) body.get("message");
        String bytesString =  wrapped.get("data");
        // Decode Base64 string to byte array
        byte[] decodedBytes = Base64.getDecoder().decode(bytesString);

        // Convert byte array to UTF-8 string
        String utf8String = new String(decodedBytes, StandardCharsets.UTF_8);

        // Convert string to list of quotes
        ObjectMapper mapper = new ObjectMapper();

        BookingDTO bookingDTO = mapper.readValue(utf8String, new TypeReference<BookingDTO>(){});
        confirmQuotes(bookingDTO.getQuotes(),bookingDTO.getUser());
    }

    //"/trains/c3c7dec3-4901-48ce-970d-dd9418ed9bcf/seats/3865d890-f659-4c55-bf84-3b3a79cb377a/ticket?customer={customer}&bookingReference={bookingReference}&key=JViZPgNadspVcHsMbDFrdGg0XXxyiE",
    public void confirmQuotes(List<Quote> quotes,String user) {
        UUID bookingReference = UUID.randomUUID();

        List<Ticket> tickets = new ArrayList<>();
        boolean crashed = false;
        for (Quote quote:quotes) {
            if(isReliableTrainCompany(quote.getTrainCompany())){
                tickets.add(webClientReliableTrains
                        .put()
                        .uri(uriBuilder -> uriBuilder
                                .pathSegment("trains/"+quote.getTrainId())
                                .pathSegment("seats/"+quote.getSeatId())
                                .pathSegment("ticket")
                                .queryParam("customer",user)
                                .queryParam("bookingReference",bookingReference)
                                .queryParam("key", trainsKey)
                                .build())
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Ticket>() {})
                        .block());
            }else if(isUnReliableTrainCompany(quote.getTrainCompany())){
                try{
                    tickets.add(webClientUnReliableTrains
                            .put()
                            .uri(uriBuilder -> uriBuilder
                                    .pathSegment("trains/"+quote.getTrainId())
                                    .pathSegment("/seats/"+quote.getSeatId())
                                    .pathSegment("/ticket")
                                    .queryParam("customer",user)
                                    .queryParam("bookingReference",bookingReference)
                                    .queryParam("key", trainsKey)
                                    .build())
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<Ticket>() {})
                            .block());
                }catch (Exception ex){
                    System.out.println("There was a problem with booking a ticket: "+ex.getMessage());
                    crashed = true;
                    break;
                }
            }
        }


        //409 WebClientResponseException

        if(crashed){
            for (Ticket ticket:tickets) {
                removeTicket(ticket.getTrainCompany(),ticket.getTrainId(),ticket.getSeatId());
            }
        }else{
            Booking booking = new Booking(bookingReference, LocalDateTime.now(),tickets,user);
            try {
                firestore.addBooking(booking);
                emailController.sendConfirmationMailSucceded(quotes,user);
            } catch (ExecutionException | InterruptedException e) {
                System.out.println("fail the transaction");
                emailController.sendConfirmationMailFailed(quotes,user);
            }
        }

    }

    private void removeTicket(String trainCompany, UUID trainId, UUID seatId) {

        boolean succed = false;

        while(!succed){
            try{
                if(isReliableTrainCompany(trainCompany)){
                    Ticket ticket = webClientReliableTrains
                            .get()
                            .uri(uriBuilder -> uriBuilder
                                    .pathSegment("trains/"+trainId)
                                    .pathSegment("seats/"+seatId)
                                    .pathSegment("ticket")
                                    .queryParam("key", trainsKey)
                                    .build())
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<Ticket>() {})
                            .block();
                    System.out.println("ACID propertie: when booking a ticket the system has failed. Rollback ticket:" + ticket.getTicketId());
                    webClientReliableTrains
                            .delete()
                            .uri(uriBuilder -> uriBuilder
                                    .pathSegment("trains/"+trainId)
                                    .pathSegment("seats/"+seatId)
                                    .pathSegment("ticket"+ticket.getTicketId())
                                    .queryParam("key", trainsKey)
                                    .build())
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<Ticket>() {})
                            .block();
                    succed = true;
                }else if(isUnReliableTrainCompany(trainCompany)){
                    Ticket ticket = webClientUnReliableTrains
                            .get()
                            .uri(uriBuilder -> uriBuilder
                                    .pathSegment("trains/"+trainId)
                                    .pathSegment("seats/"+seatId)
                                    .pathSegment("ticket")
                                    .queryParam("key", trainsKey)
                                    .build())
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<Ticket>() {})
                            .block();
                    System.out.println("ACID propertie: when booking a ticket the system has failed. Rollback ticket:" + ticket.getTicketId());
                    webClientUnReliableTrains
                            .delete()
                            .uri(uriBuilder -> uriBuilder
                                    .pathSegment("trains/"+trainId)
                                    .pathSegment("seats/"+seatId)
                                    .pathSegment("ticket/"+ticket.getTicketId())
                                    .queryParam("key", trainsKey)
                                    .build())
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<Ticket>() {})
                            .block();
                    succed = true;
                }
            }catch (Exception ex){
                System.out.println("There was a problem with deleting an ticket, try again.");
            }

        }

    }

}
