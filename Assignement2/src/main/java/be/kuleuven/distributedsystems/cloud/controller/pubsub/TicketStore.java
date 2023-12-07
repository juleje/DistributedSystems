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
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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
        String bookingReference = UUID.randomUUID().toString();

        String[] referenceAndUserFromCrash = needRecoverFromCrashCrash(quotes);
        if(referenceAndUserFromCrash!=null){
            bookingReference = referenceAndUserFromCrash[0];
            user = referenceAndUserFromCrash[1];
            quotes = quotesYetToBook(quotes);
        }


        List<Ticket> tickets = new ArrayList<>();
        boolean crashed = false;
        for (Quote quote:quotes) {
            try{
                tickets.add(bookTicket(quote,user,bookingReference));

            }catch (Exception ex) {
                System.out.println("There was a problem with booking a ticket: " + ex.getMessage());
                crashed = true;
                break;
            }
        }

        //409 WebClientResponseException
        Booking booking = new Booking(UUID.fromString(bookingReference), LocalDateTime.now(),tickets,user);
        if(crashed){
            rollback(tickets);
            System.out.println("fail the transaction");
            emailController.sendConfirmationMailFailed(booking);
        }else{
            try {
                firestore.addBooking(booking);
                emailController.sendConfirmationMailSucceded(booking);
            } catch (ExecutionException | InterruptedException e) {
                rollback(tickets);
                System.out.println("fail the transaction");
                emailController.sendConfirmationMailFailed(booking);
            }
        }
    }

    private List<Quote> quotesYetToBook(List<Quote> quotes) {
        List<Quote> quotesYetToBook = new ArrayList<>();
        for (Quote q:quotes) {
            boolean serviceUnReachable = true;
            while(serviceUnReachable){
                try{
                    getTicket(q.getTrainCompany(),q.getTrainId(),q.getSeatId());
                    System.out.println("Allready booked");
                    serviceUnReachable = false;
                }catch (WebClientResponseException ex){
                    if(ex.getStatusCode().equals(HttpStatusCode.valueOf(404))){
                        quotesYetToBook.add(q);
                        serviceUnReachable=false;
                    }
                }
            }

        }
        return quotesYetToBook;
    }

    private String[] needRecoverFromCrashCrash(List<Quote> quotes) {
        Ticket t = null;
        for (Quote q:quotes) {
            boolean serviceUnReachable = true;
            while(serviceUnReachable){
                try{
                    t = getTicket(q.getTrainCompany(),q.getTrainId(),q.getSeatId());
                    System.out.println("Allready booked");
                    serviceUnReachable = false;
                }catch (WebClientResponseException ex){
                    if(ex.getStatusCode().equals(HttpStatusCode.valueOf(404))){
                        serviceUnReachable=false;
                    }
                }
            }
        }
        if(t== null){
            return null;
        }
        return new String[]{t.getBookingReference(),t.getCustomer()};
    }

    private Ticket bookTicket(Quote quote, String user, String bookingReference) {
        if(isReliableTrainCompany(quote.getTrainCompany())){
            return webClientReliableTrains
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
                    .block();
        }else if(isUnReliableTrainCompany(quote.getTrainCompany())){
            return webClientUnReliableTrains
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
                    .block();
        }else if (isDNetTrainCompany(quote.getTrainCompany())) {
            //todo transaction
            String trainCompany = quote.getTrainCompany();
            UUID trainId = quote.getTrainId();
            UUID seatId = quote.getSeatId();
            UUID ticketId = UUID.randomUUID();
            String customer = user;
            String bookingReferenceString = bookingReference.toString();
            Ticket ticket = new Ticket(trainCompany, trainId, seatId, ticketId, customer, bookingReferenceString);
            firestore.addTicketToSeat(ticket);
            return ticket;
        }
        return null;
    }

    private void rollback(List<Ticket> tickets) {
        for (Ticket ticket:tickets) {
            if(ticket!=null){
                removeTicket(ticket.getTrainCompany(),ticket.getTrainId(),ticket.getSeatId());
            }
        }
    }

    private Ticket getTicket(String trainCompany, UUID trainId, UUID seatId){
        if(isReliableTrainCompany(trainCompany)){
            return webClientReliableTrains
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
        }else if(isUnReliableTrainCompany(trainCompany)){
            return webClientUnReliableTrains
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
        } else if (isDNetTrainCompany(trainCompany)) {
            firestore.getTicket(trainCompany,trainId,seatId);
        }
        return null;
    }

    private void removeTicket(String trainCompany, UUID trainId, UUID seatId) {
        boolean succed = false;

        while(!succed){
            try{
                if(isReliableTrainCompany(trainCompany)){
                    Ticket ticket = getTicket(trainCompany,trainId,seatId);
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
                } else if (isDNetTrainCompany(trainCompany)) {
                    //TODO getTicket
                    Ticket ticket = getTicket(trainCompany,trainId,seatId);
                    firestore.removeTicket(ticket.getTrainId().toString(),ticket.getSeatId().toString(),ticket.getTicketId().toString());
                    succed = true;
                } else if(isUnReliableTrainCompany(trainCompany)) {
                    Ticket ticket = getTicket(trainCompany,trainId,seatId);
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
            } catch (Exception ex){
                System.out.println("There was a problem with deleting an ticket, try again.");
            }
        }
    }
}
