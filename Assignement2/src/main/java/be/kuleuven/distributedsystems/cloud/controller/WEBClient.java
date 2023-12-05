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
    private FirestoreRepository firestoreRepository;

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

    public Collection<Train> getTrains() throws ExecutionException, InterruptedException {
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

        returnable.addAll(firestoreRepository.getTrains());

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



    public static boolean isUnReliableTrainCompany(String trainCompany) {
        return Objects.equals(trainCompany, "unreliabletrains.com");
    }

    public static boolean isReliableTrainCompany(String trainCompany) {
        return Objects.equals(trainCompany, "reliabletrains.com");
    }

    public static boolean isDNetTrainCompany(String trainCompany) {
        return Objects.equals(trainCompany, "DNet Train Company");
    }

}
