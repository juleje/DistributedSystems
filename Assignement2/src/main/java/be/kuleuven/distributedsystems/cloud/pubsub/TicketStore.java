package be.kuleuven.distributedsystems.cloud.pubsub;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.*;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static be.kuleuven.distributedsystems.cloud.Application.topicId;
@RestController
public class TicketStore {

    public TicketStore(){
        subscribe();
    }
    public String subscriptionId = "confirmQuotes";
    public String pushEndpoint = "http://localhost:8083/confirmQuotes";

    //todo vind een manier waar en wanneer te subscriben
    private void subscribe(){
        try{
            SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create();
            PushConfig pushConfig = PushConfig.newBuilder().setPushEndpoint(this.pushEndpoint).build();

            Subscription subscription =
                    subscriptionAdminClient.createSubscription(subscriptionId, topicId, pushConfig, 60);
            System.out.println("Created push subscription: " + subscription.getName());
        }catch(IOException e){
            System.out.println("Error with subscribing");
        }

    }
    @PostMapping("/confirmQuotes")
    public void subscription(@RequestBody String body){
        System.out.println("yeessss");
    }

}
