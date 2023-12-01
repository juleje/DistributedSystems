package be.kuleuven.distributedsystems.cloud;

import be.kuleuven.distributedsystems.cloud.entities.Seat;
import be.kuleuven.distributedsystems.cloud.entities.Train;
import be.kuleuven.distributedsystems.cloud.persistance.FirestoreRepository;
import be.kuleuven.distributedsystems.cloud.persistance.LocalDateTimeTypeAdapter;
import be.kuleuven.distributedsystems.cloud.persistance.TrainDTO;
import be.kuleuven.distributedsystems.cloud.persistance.TrainsDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.WriteResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.HypermediaWebClientConfigurer;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;

@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@SpringBootApplication
public class Application {
    private static FirestoreRepository firestoreRepository;
    public static String projectId = "demo-distributed-systems-kul";
    public static String topicId = "confirmQuotes";
    public static String subscriptionId = "confirmQuotes";
    public String pushEndpoint = "http://localhost:8083/subscription";

    @Autowired
    public Application(FirestoreRepository firestoreRepository) {
        this.firestoreRepository = firestoreRepository;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

        System.setProperty("server.port", System.getenv().getOrDefault("PORT", "8080"));
        ApplicationContext context = SpringApplication.run(Application.class, args);

        // TODO: (level 2) load this data into Firestore
        String data = new String(new ClassPathResource("data.json").getInputStream().readAllBytes());
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .create();
        TrainsDTO trainsDTO = gson.fromJson(data, TrainsDTO.class);
        TrainDTO trainDTO = trainsDTO.getTrains().get(0);
        List<Seat> seats = trainDTO.getSeats();

        // make objects
        String trainCompany = "DataCompany";
        UUID trainId = UUID.randomUUID();
        for (Seat seat : seats) {
            seat.setTrainCompany(trainCompany);
            seat.setTrainId(trainId);
            seat.setSeatId(UUID.randomUUID());
        }
        Train train = new Train(trainCompany, trainId, trainDTO.getName(), trainDTO.getLocation(), trainDTO.getImage());

        // make hashmap
        firestoreRepository.addJsonData(train, seats);
    }

    @Bean
    public boolean isProduction() {
        return Objects.equals(System.getenv("GAE_ENV"), "standard");
    }

    @Bean
    public String projectId() {
        return "demo-distributed-systems-kul";
    }
    

    /*
     * You can use this builder to create a Spring WebClient instance which can be used to make REST-calls.
     */
    @Bean
    WebClient.Builder webClientBuilder(HypermediaWebClientConfigurer configurer) {
        return configurer.registerHypermediaTypes(WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()))
                .codecs(clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs().maxInMemorySize(100 * 1024 * 1024)));
    }

    @Bean
    HttpFirewall httpFirewall() {
        DefaultHttpFirewall firewall = new DefaultHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        return firewall;
    }
}
