package be.kuleuven.distributedsystems.cloud.controller;

import be.kuleuven.distributedsystems.cloud.entities.Train;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api")
public class RESTController {
    private WEBClient webClient;

    //http://localhost:8080/swagger-ui/index.html
    @Autowired
    public RESTController(WEBClient webClient){
        this.webClient = webClient;
    }

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

}
