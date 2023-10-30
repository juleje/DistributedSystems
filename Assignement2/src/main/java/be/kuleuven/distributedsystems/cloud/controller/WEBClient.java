package be.kuleuven.distributedsystems.cloud.controller;

import be.kuleuven.distributedsystems.cloud.entities.Train;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
        }/*else if(Objects.equals(companyId, "onreliabletrains.com")){
            return null;
        }*/
        return null;
    }

}
