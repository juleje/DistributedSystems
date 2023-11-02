package be.kuleuven.distributedsystems.cloud.persistance;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FirestoreRepository {

    private Firestore db;
    public FirestoreRepository(){


    }

    public void test() throws IOException {
        FirestoreOptions firestoreOptions =
                FirestoreOptions.getDefaultInstance().toBuilder()
                        .setProjectId("demo-distributed-systems-kul")
                        .setEmulatorHost("localhost:8084")
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .build();

        db = firestoreOptions.getService();
        DocumentReference test = db.collection("test").document("rx8lGI5eZOuZGp1GzRvA");
        System.out.println(test);
    }
}
