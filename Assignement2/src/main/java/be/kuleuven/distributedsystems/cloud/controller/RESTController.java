package be.kuleuven.distributedsystems.cloud.controller;

import be.kuleuven.distributedsystems.cloud.entities.Booking;
import be.kuleuven.distributedsystems.cloud.entities.Seat;
import be.kuleuven.distributedsystems.cloud.entities.Train;
import com.google.type.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api")
public class RESTController {
    @Autowired
    private WEBClient webClient;

    //http://localhost:8080/swagger-ui/index.html
    /*
    public RESTController(WEBClient webClient){
        this.webClient = webClient;
    }
     */

    @GetMapping("/getTrains")
    public Collection<Train> getTrains() { //ResponseEntity<?>
        return webClient.getTrains();
        // var model = EntityModel.of(result);
        // return ResponseEntity.ok(model);
    }

    //http://localhost:8080/api/getTrain?trainCompany=reliabletrains.com&trainId=c3c7dec3-4901-48ce-970d-dd9418ed9bcf
    @GetMapping("/getTrain")
    public Train getTrain(@RequestParam String trainCompany, @RequestParam String trainId) { //ResponseEntity<?>
        return webClient.getTrain(trainCompany, trainId);
    }

    //http://localhost:8080/api/getTrainTimes?trainCompany=reliabletrains.com&trainId=c3c7dec3-4901-48ce-970d-dd9418ed9bcf
    @GetMapping("/getTrainTimes")
    public Collection<String> getTrainTimes(@RequestParam String trainCompany, @RequestParam String trainId) { //ResponseEntity<?>
        return webClient.getTrainTimes(trainCompany, trainId);
    }

    //http://localhost:8080/api/getAvailableSeats?trainCompany=reliabletrains.com&trainId=c3c7dec3-4901-48ce-970d-dd9418ed9bcf&time=2024-02-05T13:52:00
    ///api/getAvailableSeats?trainCompany=${trainCompany}&trainId=${trainId}&time=${time}`
    @GetMapping("/getAvailableSeats")
    public Collection<Seat> getAvailableSeats(@RequestParam String trainCompany, @RequestParam String trainId, @RequestParam String time) { //ResponseEntity<?>
        var result = webClient.getAvailableSeats(trainCompany, trainId, time);
        System.out.println(result.size());
        return result;
    }

    //http://localhost:8080/api/getSeat?trainCompany=reliabletrains.com&trainId=c3c7dec3-4901-48ce-970d-dd9418ed9bcf&seatId=cac56bf4-28d1-4e46-b912-8165c919b6c8
    ///api/getAvailableSeats?trainCompany=${trainCompany}&trainId=${trainId}&seatId=${seatId}`
    @GetMapping("/getSeat")
    public Seat getSeat(@RequestParam String trainCompany, @RequestParam String trainId, @RequestParam String seatId) { //ResponseEntity<?>
        return webClient.getSeat(trainCompany, trainId, seatId);
    }

    @PostMapping("/confirmQuotes")
    public void confirmQuotes() {

    }

    @GetMapping("/getBookings")
    public Collection<Booking> getBookings() { //ResponseEntity<?>
        return webClient.getBookings();
    }


}
