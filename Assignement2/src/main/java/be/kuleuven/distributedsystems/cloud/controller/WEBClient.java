package be.kuleuven.distributedsystems.cloud.controller;

import be.kuleuven.distributedsystems.cloud.entities.*;
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
import java.util.stream.Collectors;

import static be.kuleuven.distributedsystems.cloud.auth.SecurityFilter.getUser;

@Component
public class WEBClient {

    @Resource(name="webClientBuilder")
    private Builder builder;
    private final WebClient webClientReliableTrains;
    private final WebClient webClientUnReliableTrains;
    private final String reliableTrainsKey = "JViZPgNadspVcHsMbDFrdGg0XXxyiE";

    private List<Booking> bookings = new ArrayList<>();

    @Autowired
    public WEBClient(Builder builder){
        this.builder = builder;

        Map<String, String> uriVars = new HashMap<>();
        uriVars.put("key",reliableTrainsKey);

        this.webClientReliableTrains =builder
                .baseUrl("https://reliabletrains.com")
//                .defaultUriVariables(uriVars)
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

    //check response
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
    public void confirmQuotes(List<Quote> quotes) {
        //todo make transaction
        UUID bookingReference = UUID.randomUUID();
        User user = getUser();

        List<Ticket> tickets = new ArrayList<>();
        for (Quote quote:quotes) {
            if(isReliableTrainCompany(quote.getTrainCompany())){
                tickets.add(webClientReliableTrains
                        .put()
                        .uri(uriBuilder -> uriBuilder
                                .pathSegment("trains/"+quote.getTrainId())
                                .pathSegment("seats/"+quote.getSeatId())
                                .pathSegment("ticket")
                                .queryParam("customer",user.getEmail())
                                .queryParam("bookingReference",bookingReference)
                                .queryParam("key",reliableTrainsKey)
                                .build())
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Ticket>() {})
                        .block());
            }else if(isUnReliableTrainCompany(quote.getTrainCompany())){
                tickets.add(webClientUnReliableTrains
                        .put()
                        .uri(uriBuilder -> uriBuilder
                                .pathSegment("trains/"+quote.getTrainId())
                                .pathSegment("/seats/"+quote.getSeatId())
                                .pathSegment("/ticket")
                                .queryParam("customer",user.getEmail())
                                .queryParam("bookingReference",bookingReference)
                                .queryParam("key",reliableTrainsKey)
                                .build())
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Ticket>() {})
                        .block());
            }
        }

        Booking booking = new Booking(bookingReference,LocalDateTime.now(),tickets,user.getEmail());
        //todo make persitence with Firstore
        bookings.add(booking);
    }

    //todo verwijder
    public Collection<Booking> getBookings() {
        List<Booking> userBookings = new ArrayList<>();
        for (Booking booking:bookings) {
            if(booking.getCustomer().equals(getUser().getEmail())){
                userBookings.add(booking);
            }
        }
        return userBookings;
    }
    //""""""""""""""""""""""""""""""""""""""""""""""""""
    //todo verplaats naar firestore repo
    public Collection<Booking> getAllBookings() {
        return bookings;
    }

    public Collection<String> geBestCustomers() {
        HashMap<String,Integer> usersAndBookings = new HashMap<>();
        for (Booking booking:bookings) {
            int value = 0;
            if(usersAndBookings.get(booking.getCustomer()) != null){
                value = usersAndBookings.get(booking.getCustomer())+1;
            }
            usersAndBookings.put(booking.getCustomer(),value);
        }
        Map<String, Integer> sortedUsers = sortByValue(usersAndBookings);

        List<String> bestUsers = new ArrayList<>();
        int initialValue = sortedUsers.values().stream().findFirst().get();
        for (String username:sortedUsers.keySet()) {
            if(sortedUsers.get(username) == initialValue){
                bestUsers.add(username);

            }
        }

        return bestUsers;
    }

    // function to sort hashmap by values
    public HashMap<String, Integer> sortByValue(HashMap<String, Integer> hm)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Integer> > list =
                new LinkedList<Map.Entry<String, Integer> >(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2)
            {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }
    //"""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""
    private static boolean isUnReliableTrainCompany(String trainCompany) {
        return Objects.equals(trainCompany, "unreliabletrains.com");
    }

    private static boolean isReliableTrainCompany(String trainCompany) {
        return Objects.equals(trainCompany, "reliabletrains.com");
    }

}
