package be.kuleuven.distributedsystems.cloud;

import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannel;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.SubscriptionName;
import com.google.pubsub.v1.TopicName;
import io.grpc.ManagedChannelBuilder;
import org.apache.http.client.CredentialsProvider;
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
import java.util.Objects;

@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@SpringBootApplication
public class Application {

    public static String projectId = "demo-distributed-systems-kul";
    public static String topicId = "confirmQuotes";
    public static String subscriptionId = "confirmQuotes";
    public String pushEndpoint = "http://localhost:8083/subscription";

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException {
        System.setProperty("server.port", System.getenv().getOrDefault("PORT", "8080"));
        ApplicationContext context = SpringApplication.run(Application.class, args);

        // TODO: (level 2) load this data into Firestore
        String data = new String(new ClassPathResource("data.json").getInputStream().readAllBytes());
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
