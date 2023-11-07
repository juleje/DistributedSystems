package be.kuleuven.distributedsystems.cloud.controller;

import be.kuleuven.distributedsystems.cloud.entities.Booking;
import be.kuleuven.distributedsystems.cloud.entities.Seat;
import be.kuleuven.distributedsystems.cloud.entities.Train;
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

@Component
public class WEBClient {

    @Resource(name="webClientBuilder")
    private Builder builder;
    private WebClient webClientReliableTrains;
    private WebClient webClientUnReliableTrains;
    private final String reliableTrainsKey = "JViZPgNadspVcHsMbDFrdGg0XXxyiE";

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
        if(Objects.equals(companyId, "reliabletrains.com")){
            return webClientReliableTrains
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .pathSegment("trains/"+trainId)
                            .queryParam("key",reliableTrainsKey)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Train>() {})
                    .block();
        }else if(Objects.equals(companyId, "unreliabletrains.com")){
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
        if(Objects.equals(companyId, "reliabletrains.com")){
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
        }else if(Objects.equals(companyId, "unreliabletrains.com")){
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
        if(Objects.equals(companyId, "reliabletrains.com")){

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

        }else if(Objects.equals(companyId, "unreliabletrains.com")){
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

}
