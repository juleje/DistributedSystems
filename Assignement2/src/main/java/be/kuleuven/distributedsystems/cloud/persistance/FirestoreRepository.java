package be.kuleuven.distributedsystems.cloud.persistance;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.WriteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Component
public class FirestoreRepository {

    private Firestore db;
    public FirestoreRepository(){
        FirestoreOptions firestoreOptions =
                FirestoreOptions.getDefaultInstance().toBuilder()
                        .setEmulatorHost("localhost:8084")
                        .setCredentials(new FirestoreOptions.EmulatorCredentials())
                        .setProjectId("demo-distributed-systems-kul")
                        .build();

        db = firestoreOptions.getService();
    }

    public void test() throws IOException, ExecutionException, InterruptedException {
        DocumentReference test = db.collection("test").document("rx8lGI5eZOuZGp1GzRvA");
        System.out.println(test.get().get());
    }

    public void testPost() {
        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("id","testId");
        bookingData.put("time", LocalDateTime.now());
        bookingData.put("customer", "testCustomer");//todo get from data

        Map<String, Object> ticketData = new HashMap<>();
        ticketData.put("trainCompany","trainCompanyTest");//todo get from data
        ticketData.put("trainId","trainIdTest");//todo get from data
        ticketData.put("seatId","seatIdTest");//todo get from data
        ticketData.put("ticketId","ticketIdTest");//todo get from data
        ticketData.put("customer","customerTest");//todo get from data
        ticketData.put("bookingReference","bookingReferenceTest");//todo get from data

        bookingData.put("tickets",ticketData);

        ApiFuture<WriteResult> future = db.collection("Bookings")
                .document("testId")//todo get from data
                .set(bookingData);

        WriteResult result = null;
        try {
            result = future.get();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("make docu fout"+ e.getMessage());
        }
        assert result != null;
        System.out.println(result.getUpdateTime());
    }
}
