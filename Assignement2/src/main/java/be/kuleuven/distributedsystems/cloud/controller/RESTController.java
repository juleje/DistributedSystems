package be.kuleuven.distributedsystems.cloud.controller;

import be.kuleuven.distributedsystems.cloud.entities.Seat;
import be.kuleuven.distributedsystems.cloud.entities.Train;
import be.kuleuven.distributedsystems.cloud.persistance.FirestoreRepository;
import com.google.type.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;

@RestController
@RequestMapping("/api")
public class RESTController {
    @Autowired
    private WEBClient webClient;
    @Autowired
    private FirestoreRepository firestore;

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

    //http://localhost:8080/api/getAvailableSeats?trainCompany=reliabletrains.com&trainId=c3c7dec3-4901-48ce-970d-dd9418ed9bcf&time=2024-02-05T13:52:00
    ///api/getAvailableSeats?trainCompany=${trainCompany}&trainId=${trainId}&time=${time}`
    @GetMapping("/getAvailableSeats")
    public Collection<Collection<Seat>> getAvailableSeats(@RequestParam String trainCompany, @RequestParam String trainId, @RequestParam String time) {
        return webClient.getAvailableSeats(trainCompany, trainId, time);
    }

}
