package be.kuleuven.distributedsystems.cloud.pubsub;

import be.kuleuven.distributedsystems.cloud.Application;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.*;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static be.kuleuven.distributedsystems.cloud.Application.topicId;
@RestController
@RequestMapping("/subscription")
public class TicketStore {

    @PostMapping("confirmQuotes")
    public void processSubscriberMessage(@RequestBody byte[] body) throws IOException, ExecutionException, InterruptedException {
        System.out.println("yeessss");
    }

}
