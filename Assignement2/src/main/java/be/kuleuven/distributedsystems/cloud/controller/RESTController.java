package be.kuleuven.distributedsystems.cloud.controller;

import be.kuleuven.distributedsystems.cloud.entities.Train;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        var result = webClient.getTrains();
        System.out.println(result.size());
        return result;
        // var model = EntityModel.of(result);
        // return ResponseEntity.ok(model);
    }

    @GetMapping("/getTrain/{companyId}/{trainId}")
    public Train getTrain(@PathVariable String companyId, @PathVariable String trainId) { //ResponseEntity<?>
        var result = webClient.getTrain(companyId, trainId);
        System.out.println(result);
        return result;
        // var model = EntityModel.of(result);
        // return ResponseEntity.ok(model);
    }

}
