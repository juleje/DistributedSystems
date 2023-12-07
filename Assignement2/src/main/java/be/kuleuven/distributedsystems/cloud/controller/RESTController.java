package be.kuleuven.distributedsystems.cloud.controller;

import be.kuleuven.distributedsystems.cloud.controller.pubsub.BookingDTO;
import be.kuleuven.distributedsystems.cloud.controller.pubsub.TicketStore;
import be.kuleuven.distributedsystems.cloud.entities.*;
import be.kuleuven.distributedsystems.cloud.persistance.FirestoreRepository;
import be.kuleuven.distributedsystems.cloud.controller.pubsub.MessagePublisher;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static be.kuleuven.distributedsystems.cloud.auth.SecurityFilter.getUser;
import static be.kuleuven.distributedsystems.cloud.auth.SecurityFilter.getUser;

@RestController
@RequestMapping("/api")
public class RESTController {
    @Autowired
    private WEBClient webClient;
    @Autowired
    private FirestoreRepository firestore;
    @Autowired
    private MessagePublisher messagePublisher;

    @GetMapping("/getTrains")
    public Collection<Train> getTrains() throws ExecutionException, InterruptedException {
        return webClient.getTrains();
    }

    //http://localhost:8080/api/getTrain?trainCompany=reliabletrains.com&trainId=c3c7dec3-4901-48ce-970d-dd9418ed9bcf
    @GetMapping("/getTrain")
    public ResponseEntity<?>  getTrain(@RequestParam String trainCompany, @RequestParam String trainId) {
        try{
           return ResponseEntity.ok().body(webClient.getTrain(trainCompany, trainId));
        }catch (Exception ex){
            return ResponseEntity.status(503).body("There was a problem with the Unreliable Train Company: "+ex.getMessage());
        }
    }

    //http://localhost:8080/api/getTrainTimes?trainCompany=reliabletrains.com&trainId=c3c7dec3-4901-48ce-970d-dd9418ed9bcf
    @GetMapping("/getTrainTimes")
    public ResponseEntity<?> getTrainTimes(@RequestParam String trainCompany, @RequestParam String trainId) {
        try{
            return ResponseEntity.ok().body(webClient.getTrainTimes(trainCompany, trainId));
        }catch (Exception ex){
            return ResponseEntity.status(503).body("There was a problem with the Unreliable Train Company: "+ex.getMessage());
        }
    }

    @GetMapping("/getAvailableSeats")
    public ResponseEntity<?>  getAvailableSeats(@RequestParam String trainCompany, @RequestParam String trainId, @RequestParam String time) {

        Collection<Seat> unorderedSeats;
        try{
            unorderedSeats = new ArrayList<>(webClient.getAvailableSeats(trainCompany, trainId, time));
        }catch (Exception ex){
            return ResponseEntity.status(503).body("There was a problem with the Unreliable Train Company: "+ex.getMessage());
        }

        List<Seat> orderSeats = new ArrayList<>(unorderedSeats);
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
        return ResponseEntity.ok().body(elements);
    }

    @GetMapping("/getSeat")
    public ResponseEntity<?> getSeat(@RequestParam String trainCompany, @RequestParam String trainId, @RequestParam String seatId) { //ResponseEntity<?>
        try{
            return ResponseEntity.ok().body(webClient.getSeat(trainCompany, trainId, seatId));
        }catch (Exception ex){
            return ResponseEntity.status(503).body("There was a problem with the Unreliable Train Company: "+ex.getMessage());
        }
    }

    @PostMapping("/confirmQuotes")
    public ResponseEntity<?> confirmQuotes(@RequestBody List<Quote> body) {
        try{
            //Config Pub/Sub
            Publisher publisher = messagePublisher.publisher();
            messagePublisher.topic();
            messagePublisher.subscribe();

            //Publish
            User user = getUser();
            BookingDTO dto = new BookingDTO(body,user.getEmail());
            String message = new Gson().toJson(dto);
            ByteString data = ByteString.copyFromUtf8(message);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
            publisher.publish(pubsubMessage);

            publisher.shutdown();
            return ResponseEntity.ok().body("Booking confirmed");
        }catch (Exception ex){
            ex.printStackTrace();
            return ResponseEntity.status(503).body("There was a problem with the Unreliable Train Company: " + ex.getMessage());
        }
    }

    @GetMapping("/getBookings")
    public ResponseEntity<?> getBookings() {
        try {
            return ResponseEntity.ok().body(firestore.getBookings());
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(503).body("There was a problem with Firestore: "+e.getMessage());
        }
    }

    @GetMapping("/getAllBookings")
    public ResponseEntity<?> getAllBookings() {
        try {
            return ResponseEntity.ok().body(firestore.getAllBookings());
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(503).body("There was a problem with Firestore: "+e.getMessage());
        }
    }

    @GetMapping("/getBestCustomers")
    public ResponseEntity<?> getBestCustomers(){
        try {
            return ResponseEntity.ok().body(firestore.geBestCustomers());
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(503).body("There was a problem with Firestore: "+e.getMessage());
        }
    }
}
