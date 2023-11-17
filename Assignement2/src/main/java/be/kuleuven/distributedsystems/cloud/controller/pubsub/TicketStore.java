package be.kuleuven.distributedsystems.cloud.controller.pubsub;

import be.kuleuven.distributedsystems.cloud.controller.WEBClient;
import be.kuleuven.distributedsystems.cloud.entities.Quote;
import be.kuleuven.distributedsystems.cloud.entities.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/subscription")
public class TicketStore {
    @Autowired
    private WEBClient webClient;

    @PostMapping("confirmQuotes")
    public void processSubscriberMessage(@RequestBody LinkedHashMap body) throws IOException, ExecutionException, InterruptedException {
       LinkedHashMap<String, String> wrapped = (LinkedHashMap) body.get("message");
        String bytesString =  wrapped.get("data");
        // Decode Base64 string to byte array
        byte[] decodedBytes = Base64.getDecoder().decode(bytesString);

        // Convert byte array to UTF-8 string
        String utf8String = new String(decodedBytes, StandardCharsets.UTF_8);

        // Convert string to list of quotes
        ObjectMapper mapper = new ObjectMapper();

        BookingDTO bookingDTO = mapper.readValue(utf8String, new TypeReference<BookingDTO>(){});
        webClient.confirmQuotes(bookingDTO.getQuotes(),bookingDTO.getUser());

    }

}
