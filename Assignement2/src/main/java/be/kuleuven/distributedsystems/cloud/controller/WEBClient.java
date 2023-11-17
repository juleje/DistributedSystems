package be.kuleuven.distributedsystems.cloud.controller;

import be.kuleuven.distributedsystems.cloud.entities.*;
import be.kuleuven.distributedsystems.cloud.persistance.FirestoreRepository;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.web.reactive.function.client.WebClient;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static be.kuleuven.distributedsystems.cloud.auth.SecurityFilter.getUser;

@Component
public class WEBClient {

    @Resource(name="webClientBuilder")
    private Builder builder;
    private final WebClient webClientReliableTrains;
    private final WebClient webClientUnReliableTrains;
    private final String reliableTrainsKey = "JViZPgNadspVcHsMbDFrdGg0XXxyiE";

    @Autowired
    private FirestoreRepository firestore;

    @Autowired
    public WEBClient(Builder builder){
        this.builder = builder;

        this.webClientReliableTrains =builder
                .baseUrl("https://reliabletrains.com")
                .build();
        this.webClientUnReliableTrains = builder
                .baseUrl("https://unreliabletrains.com")
                .build();
    }

    public Collection<Train> getTrains() {
        Collection<Train> returnable = new ArrayList<>();
        returnable.addAll(webClientReliableTrains
                .get()
                .uri(uriBuilder -> uriBuilder
                        .pathSegment("trains")
                        .queryParam("key",reliableTrainsKey)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<CollectionModel<Train>>() {})
                .block()
                .getContent());
        try{
            returnable.addAll(webClientUnReliableTrains
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .pathSegment("trains")
                            .queryParam("key",reliableTrainsKey)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<CollectionModel<Train>>() {})
                    .block()
                    .getContent());
        }catch (Exception ex){
            System.out.println("There went something wrong with the Unreliable Train Company: "+ex.getMessage());
        }

        return returnable;
    }

    public Train getTrain(String companyId, String trainId) {
        if(isReliableTrainCompany(companyId)){
            return webClientReliableTrains
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .pathSegment("trains/"+trainId)
                            .queryParam("key",reliableTrainsKey)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Train>() {})
                    .block();
        }else if(isUnReliableTrainCompany(companyId)){
            return webClientUnReliableTrains
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .pathSegment("trains/"+trainId)
                            .queryParam("key",reliableTrainsKey)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Train>() {})
                    .block();
        }
        return null;
    }


    public Collection<LocalDateTime> getTrainTimes(String companyId, String trainId) {
        if(isReliableTrainCompany(companyId)){
            Collection<LocalDateTime> times =  webClientReliableTrains
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .pathSegment("trains/"+trainId)
                            .pathSegment("times")
                            .queryParam("key",reliableTrainsKey)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<CollectionModel<LocalDateTime>>() {})
                    .block()
                    .getContent();
            List<LocalDateTime> returnable = new ArrayList<>(times);
            Collections.sort(returnable);
            return returnable;
        }else if(isUnReliableTrainCompany(companyId)){
            Collection<LocalDateTime> times = webClientUnReliableTrains
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .pathSegment("trains/"+trainId)
                            .pathSegment("times")
                            .queryParam("key",reliableTrainsKey)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<CollectionModel<LocalDateTime>>() {})
                    .block()
                    .getContent();
            List<LocalDateTime> returnable = new ArrayList<>(times);
            Collections.sort(returnable);
            return returnable;
        }
        return null;
    }

    public Collection<Seat> getAvailableSeats(String companyId, String trainId, String time) {
        if(isReliableTrainCompany(companyId)){
            return webClientReliableTrains
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .pathSegment("trains/"+trainId)
                            .pathSegment("seats")
                            .queryParam("time", time)
                            .queryParam("available", "true")
                            .queryParam("key",reliableTrainsKey)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<CollectionModel<Seat>>() {})
                    .block()
                    .getContent();

        }else if(isUnReliableTrainCompany(companyId)){
            return webClientUnReliableTrains
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .pathSegment("trains/"+trainId)
                            .pathSegment("seats")
                            .queryParam("time", time)
                            .queryParam("available", "true")
                            .queryParam("key",reliableTrainsKey)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<CollectionModel<Seat>>() {})
                    .block()
                    .getContent();
        }
        return null;
    }

    ///trains/c3c7dec3-4901-48ce-970d-dd9418ed9bcf/seats/3865d890-f659-4c55-bf84-3b3a79cb377a?key=JViZPgNadspVcHsMbDFrdGg0XXxyiE
    public Seat getSeat(String trainCompany, String trainId, String seatId) {
        if(isReliableTrainCompany(trainCompany)){

            return webClientReliableTrains
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .pathSegment("trains/"+trainId)
                            .pathSegment("seats/"+seatId)
                            .queryParam("key",reliableTrainsKey)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Seat>() {})
                    .block();

        }else if(isUnReliableTrainCompany(trainCompany)){
            return webClientUnReliableTrains
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .pathSegment("trains/"+trainId)
                            .pathSegment("seats/"+seatId)
                            .queryParam("key",reliableTrainsKey)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Seat>() {})
                    .block();
        }
        return null;
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
                removeTicket(ticket.getTrainCompany(),ticket.getTrainId(),ticket.getSeatId());
            }
        }else{
            Booking booking = new Booking(bookingReference,LocalDateTime.now(),tickets,user);
            try {
                firestore.addBooking(booking);
            } catch (ExecutionException | InterruptedException e) {
                System.out.println("fail the transaction");
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
                                    .queryParam("key",reliableTrainsKey)
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
                                    .queryParam("key",reliableTrainsKey)
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
                                    .queryParam("key",reliableTrainsKey)
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
                                    .queryParam("key",reliableTrainsKey)
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

    private static boolean isUnReliableTrainCompany(String trainCompany) {
        return Objects.equals(trainCompany, "unreliabletrains.com");
    }

    private static boolean isReliableTrainCompany(String trainCompany) {
        return Objects.equals(trainCompany, "reliabletrains.com");
    }

}
