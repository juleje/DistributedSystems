package be.kuleuven.distributedsystems.cloud.controller;

import be.kuleuven.distributedsystems.cloud.entities.Train;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api")
public class RESTController {
    private WEBClient webClient;

    @Autowired
    public RESTController(WEBClient webClient){
        this.webClient = webClient;
    }

    @GetMapping("/getTrains")
    public Collection<Train> getTrains(){ //ResponseEntity<?>

        System.out.println("api called");

        var result = webClient.getTrains();

        //406 Not Acceptable from GET https://reliabletrains.com/trains?key=JViZPgNadspVcHsMbDFrdGg0XXxyiE
        // getrequest is done right
        // serialization is croqued because of '_links'


        System.out.println("data");
        System.out.println(result.size());
        return result;
        // var model = EntityModel.of(result);
        // return ResponseEntity.ok(model);
    }

}
