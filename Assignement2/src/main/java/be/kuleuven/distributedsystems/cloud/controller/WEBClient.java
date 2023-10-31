package be.kuleuven.distributedsystems.cloud.controller;

import be.kuleuven.distributedsystems.cloud.entities.Booking;
import be.kuleuven.distributedsystems.cloud.entities.Seat;
import be.kuleuven.distributedsystems.cloud.entities.Train;
import com.google.type.DateTime;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Component
public class WEBClient {

    @Resource(name="webClientBuilder")
    private Builder builder;
    private WebClient webClient;
    private final String reliableTrainsKey = "JViZPgNadspVcHsMbDFrdGg0XXxyiE";

    @Autowired
    public WEBClient(Builder builder){
        this.builder = builder;

        Map<String, String> uriVars = new HashMap<>();
        uriVars.put("key",reliableTrainsKey);

        this.webClient =builder
                .baseUrl("https://reliabletrains.com")
//                .defaultUriVariables(uriVars)
                .build();
    }

    public Collection<Train> getTrains() {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .pathSegment("trains")
                        .queryParam("key",reliableTrainsKey)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<CollectionModel<Train>>() {})
                .block()
                .getContent();
    }

    public Train getTrain(String companyId, String trainId) {
        if(Objects.equals(companyId, "reliabletrains.com")){
            return webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .pathSegment("trains/"+trainId)
                            .queryParam("key",reliableTrainsKey)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Train>() {})
                    .block();
        }/*else if(Objects.equals(companyId, "unreliabletrains.com")){
            return null;
        }*/
        return null;
    }

    public Collection<String> getTrainTimes(String companyId, String trainId) {
        if(Objects.equals(companyId, "reliabletrains.com")){
            return webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .pathSegment("trains/"+trainId)
                            .pathSegment("times")
                            .queryParam("key",reliableTrainsKey)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<CollectionModel<String>>() {})
                    .block()
                    .getContent();
        }/*else if(Objects.equals(companyId, "unreliabletrains.com")){
            return null;
        }*/
        return null;
    }

    public Collection<Seat> getAvailableSeats(String companyId, String trainId, String time) {
        if(Objects.equals(companyId, "reliabletrains.com")){
            return webClient
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
        }/*else if(Objects.equals(companyId, "unreliabletrains.com")){
            return null;
        }*/
        return null;
    }

    public Seat getSeat(String companyId, String trainId, String seatId) {
        if(Objects.equals(companyId, "reliabletrains.com")){
            return webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .pathSegment("trains/"+trainId)
                            .pathSegment("seats/"+seatId)
                            .queryParam("key",reliableTrainsKey)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Seat>() {})
                    .block();
        }/*else if(Objects.equals(companyId, "unreliabletrains.com")){
            return null;
        }*/
        return null;
    }

    public void confirmQuotes() {

    }

    public Collection<Booking> getBookings() {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .pathSegment("account")
                        //.queryParam("key",reliableTrainsKey)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<CollectionModel<Booking>>() {})
                .block()
                .getContent();
    }
}
