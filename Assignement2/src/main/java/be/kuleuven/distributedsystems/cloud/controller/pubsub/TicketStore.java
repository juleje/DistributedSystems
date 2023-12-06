package be.kuleuven.distributedsystems.cloud.controller.pubsub;

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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static be.kuleuven.distributedsystems.cloud.controller.WEBClient.*;

@RestController
@RequestMapping("/subscription")
public class TicketStore {

    @Resource(name="webClientBuilder")
    private WebClient.Builder builder;
    private final WebClient webClientReliableTrains;
    private final WebClient webClientUnReliableTrains;
    private final String reliableTrainsKey = "JViZPgNadspVcHsMbDFrdGg0XXxyiE";
    @Autowired
    private FirestoreRepository firestore;

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
                                .queryParam("key",reliableTrainsKey)
                                .build())
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Ticket>() {})
                        .block());
            } else if (isDNetTrainCompany(quote.getTrainCompany())) {
                String trainCompany = quote.getTrainCompany();
                UUID trainId = quote.getTrainId();
                UUID seatId = quote.getSeatId();
                UUID ticketId = UUID.randomUUID();
                String customer = user;
                String bookingReferenceString = bookingReference.toString();
                Ticket ticket = new Ticket(trainCompany, trainId, seatId, ticketId, customer, bookingReferenceString);
                tickets.add(ticket);
                firestore.addTicketToSeat(ticket);
            } else if(isUnReliableTrainCompany(quote.getTrainCompany())){
                try{
                    tickets.add(webClientUnReliableTrains
                            .put()
                            .uri(uriBuilder -> uriBuilder
                                    .pathSegment("trains/"+quote.getTrainId())
                                    .pathSegment("/seats/"+quote.getSeatId())
                                    .pathSegment("/ticket")
                                    .queryParam("customer",user)
                                    .queryParam("bookingReference",bookingReference)
                                    .queryParam("key",reliableTrainsKey)
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
                removeTicket(ticket);
            }
        }else{
            Booking booking = new Booking(bookingReference, LocalDateTime.now(), tickets, user);
            try {
                firestore.addBooking(booking);
            } catch (ExecutionException | InterruptedException e) {
                System.out.println("fail the transaction");
            }
        }
    }

    private void removeTicket(Ticket ticket) {
        boolean succeeded = false;
        while(!succeeded){
            try{
                if(isReliableTrainCompany(ticket.getTrainCompany())){
                    Ticket ticketToRemove = webClientReliableTrains
                            .get()
                            .uri(uriBuilder -> uriBuilder
                                    .pathSegment("trains/"+ticket.getTrainId())
                                    .pathSegment("seats/"+ticket.getSeatId())
                                    .pathSegment("ticket")
                                    .queryParam("key",reliableTrainsKey)
                                    .build())
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<Ticket>() {})
                            .block();
                    System.out.println("ACID property: when booking a ticket the system has failed. Rollback ticket:" + ticketToRemove.getTicketId());
                    webClientReliableTrains
                            .delete()
                            .uri(uriBuilder -> uriBuilder
                                    .pathSegment("trains/"+ticketToRemove.getTrainId())
                                    .pathSegment("seats/"+ticketToRemove.getSeatId())
                                    .pathSegment("ticket"+ticketToRemove.getTicketId())
                                    .queryParam("key",reliableTrainsKey)
                                    .build())
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<Ticket>() {})
                            .block();
                    succeeded = true;
                } else if (isDNetTrainCompany(ticket.getTrainCompany())) {
                    //TODO
                    firestore.removeTicket(ticket);
                    succeeded = true;
                } else if(isUnReliableTrainCompany(ticket.getTrainCompany())) {
                    Ticket ticketToRemove = webClientUnReliableTrains
                            .get()
                            .uri(uriBuilder -> uriBuilder
                                    .pathSegment("trains/"+ticket.getTrainId())
                                    .pathSegment("seats/"+ticket.getSeatId())
                                    .pathSegment("ticket")
                                    .queryParam("key",reliableTrainsKey)
                                    .build())
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<Ticket>() {})
                            .block();
                    System.out.println("ACID property: when booking a ticket the system has failed. Rollback ticket:" + ticketToRemove.getTicketId());
                    webClientUnReliableTrains
                            .delete()
                            .uri(uriBuilder -> uriBuilder
                                    .pathSegment("trains/"+ticketToRemove.getTrainId())
                                    .pathSegment("seats/"+ticketToRemove.getSeatId())
                                    .pathSegment("ticket/"+ticketToRemove.getTicketId())
                                    .queryParam("key",reliableTrainsKey)
                                    .build())
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<Ticket>() {})
                            .block();
                    succeeded = true;
                }
            } catch (Exception ex){
                System.out.println("There was a problem with deleting an ticket, try again.");
            }
        }
    }
}
