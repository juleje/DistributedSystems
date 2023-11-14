package be.kuleuven.distributedsystems.cloud.controller;

import be.kuleuven.distributedsystems.cloud.entities.*;
import be.kuleuven.distributedsystems.cloud.persistance.FirestoreRepository;
import com.google.type.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api")
public class RESTController {
    @Autowired
    private WEBClient webClient;
    @Autowired
    private FirestoreRepository firestore;

    @GetMapping("/getTrains")
    public Collection<Train> getTrains() {
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
    public void confirmQuotes(@RequestBody List<Quote> body) {
        //todo do this later with pub/sub
        webClient.confirmQuotes(body);

    }

    @GetMapping("/getBookings")
    public Collection<Booking> getBookings() {
        try {
            return firestore.getBookings();
        } catch (ExecutionException | InterruptedException e) {
            System.out.println("Problem with fetching bookings of users: "+e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @GetMapping("/getAllBookings")
    public Collection<Booking> getAllBookings() {
        try {
            return firestore.getAllBookings();
        }catch (ExecutionException | InterruptedException e) {
            System.out.println("Problem with fetching all bookings: "+e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @GetMapping("/getBestCustomers")
    public Collection<String> getBestCustomers(){
        try {
            return firestore.geBestCustomers();
        }catch (ExecutionException | InterruptedException e) {
            System.out.println("Problem with fetching all bookings: "+e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
