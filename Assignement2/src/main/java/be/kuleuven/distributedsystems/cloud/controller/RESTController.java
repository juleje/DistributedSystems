package be.kuleuven.distributedsystems.cloud.controller;

import be.kuleuven.distributedsystems.cloud.entities.*;
import be.kuleuven.distributedsystems.cloud.persistance.FirestoreRepository;
import be.kuleuven.distributedsystems.cloud.pubsub.TicketStore;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.*;
import com.google.type.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
public class RESTController {
    @Autowired
    private WEBClient webClient;
    @Autowired
    private FirestoreRepository firestore;

    @Autowired
    private TicketStore ticketStore;

    @GetMapping("/getTrains")
    public Collection<Train> getTrains() {
        //firestore.testPost();
        return webClient.getTrains();
    }

    //http://localhost:8080/api/getTrain?trainCompany=reliabletrains.com&trainId=c3c7dec3-4901-48ce-970d-dd9418ed9bcf
    @GetMapping("/getTrain")
    public Train getTrain(@RequestParam String trainCompany, @RequestParam String trainId) {
        return webClient.getTrain(trainCompany, trainId);
    }

    //http://localhost:8080/api/getTrainTimes?trainCompany=reliabletrains.com&trainId=c3c7dec3-4901-48ce-970d-dd9418ed9bcf
    @GetMapping("/getTrainTimes")
    public Collection<LocalDateTime> getTrainTimes(@RequestParam String trainCompany, @RequestParam String trainId) {
        return webClient.getTrainTimes(trainCompany, trainId);
    }

    @GetMapping("/getAvailableSeats")
    public Map<String, Collection<Seat>>  getAvailableSeats(@RequestParam String trainCompany, @RequestParam String trainId, @RequestParam String time) {
        List<Seat> orderSeats = new ArrayList<>(webClient.getAvailableSeats(trainCompany, trainId, time));
        orderSeats.sort(Comparator.comparing(Seat::getName));
        List<Seat> firstClass = new ArrayList<>();
        List<Seat> secondClass = new ArrayList<>();

        for (Seat seat: orderSeats) {
            if(seat.getType().equals("1st class")){
                firstClass.add(seat);
            }else if(seat.getType().equals("2nd class")){
                secondClass.add(seat);
            }else {
                System.out.println("Seat with wrong class" + seat.getType());
            }
        }

        Map<String, Collection<Seat>> elements = new HashMap<>();
        elements.put("first class",firstClass);
        elements.put("2nd class",secondClass);
        return elements;


    }


    @GetMapping("/getSeat")
    public Seat getSeat(@RequestParam String trainCompany, @RequestParam String trainId, @RequestParam String seatId) { //ResponseEntity<?>
        return webClient.getSeat(trainCompany, trainId, seatId);
    }


    @PostMapping("/confirmQuotes")
    public void confirmQuotes(@RequestBody List<Quote> body) throws IOException, InterruptedException, ExecutionException {
        //todo do this later with pub/sub
        webClient.confirmQuotes(body);

        String projectId = "TrainTickets";
        String topicId = "Quotes to confirm";
        String subscriptionId = "subscriptionId";
        publishMessage(projectId, topicId, body);
        createPushSubscription(projectId, subscriptionId, topicId);
    }

    public static void createPushSubscription(String projectId, String subscriptionId, String topicId) throws IOException {
        try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create()) {
            TopicName topicName = TopicName.of(projectId, topicId);
            SubscriptionName subscriptionName = SubscriptionName.of(projectId, subscriptionId);
            PushConfig pushConfig = PushConfig.newBuilder()
                    .setPushEndpoint("???")
                    .build();
            // Create a push subscription with default acknowledgement deadline of 10 seconds.
            // Messages not successfully acknowledged within 10 seconds will get resent by the server.
            Subscription subscription =
                    subscriptionAdminClient.createSubscription(
                            subscriptionName, topicName, pushConfig, 10);
            System.out.println("Created push subscription: " + subscription.getName());
        }
    }

    public static void publishMessage(String projectId, String topicId, List<Quote> body) throws IOException, InterruptedException, ExecutionException {
        TopicName topicName = TopicName.of(projectId, topicId);
        Publisher publisher = null;

        try {
            // Create a publisher instance with default settings bound to the topic
            publisher = Publisher.newBuilder(topicName).build();

            String message = new Gson().toJson(body);
            ByteString data = ByteString.copyFromUtf8(message);
            PubsubMessage pubsubMessage =
                    PubsubMessage.newBuilder()
                            .setData(data)
                            .build();

            // Once published, returns a server-assigned message id (unique within the topic)
            ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
            String messageId = messageIdFuture.get();
            System.out.println("Published a message with custom attributes: " + messageId);

        } finally {
            if (publisher != null) {
                // When finished with the publisher, shutdown to free up resources.
                publisher.shutdown();
                publisher.awaitTermination(1, TimeUnit.MINUTES);
            }
        }
    }

    @GetMapping("/getBookings")
    public Collection<Booking> getBookings() {
        //todo do this later with firestore repository
        return webClient.getBookings();
    }

    @GetMapping("/getAllBookings")
    public Collection<Booking> getAllBookings() {
        //todo do this later with firestore repository
        return webClient.getAllBookings();
    }

    @GetMapping("/getBestCustomers")
    public Collection<String> getBestCustomers(){
        //todo do this later with firstore repository
        return webClient.geBestCustomers();
    }
}
