package be.kuleuven.distributedsystems.cloud.persistance;

import be.kuleuven.distributedsystems.cloud.entities.Booking;
import be.kuleuven.distributedsystems.cloud.entities.Ticket;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static be.kuleuven.distributedsystems.cloud.Application.projectIdPub;
import static be.kuleuven.distributedsystems.cloud.auth.SecurityFilter.getUser;

@Component
public class FirestoreRepository {

    private Firestore db;
    private DateTimeFormatter formatter;

    public FirestoreRepository(){
        /*
        //DEV env
        FirestoreOptions firestoreOptions =
                FirestoreOptions.getDefaultInstance().toBuilder()
                        .setEmulatorHost("localhost:8084")
                        .setCredentials(new FirestoreOptions.EmulatorCredentials())
                        .setProjectId("demo-distributed-systems-kul")
                        .build();

         */
        //PUB env
        FirestoreOptions firestoreOptions =
                FirestoreOptions.getDefaultInstance().toBuilder()
                        .setProjectId(projectIdPub)
                        .build();
        db = firestoreOptions.getService();
        formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    }

    private Booking mapQueryDocumentSnapshotToBooking(QueryDocumentSnapshot snapshot){
        Map<String, Object> data = snapshot.getData();
        UUID id = UUID.fromString((String) data.get("id"));
        LocalDateTime time = LocalDateTime.parse((String) data.get("time"), formatter);
        String customer = (String) data.get("customer");
        List<Ticket> tickets = (List<Ticket>) data.get("tickets");
        return new Booking(id,time,tickets,customer);
    }
    public Collection<Booking> getBookings() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> snapshots = db.collection("bookings").get().get().getDocuments();
        List<Booking> userBookings = new ArrayList<>();
        for (QueryDocumentSnapshot bookingSnapshot:snapshots) {
            Booking booking = mapQueryDocumentSnapshotToBooking(bookingSnapshot);
            if(booking.getCustomer().equals(getUser().getEmail())){
                userBookings.add(booking);
            }
        }
        return userBookings;
    }

    public void addBooking(Booking booking) throws ExecutionException, InterruptedException {

        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("id",booking.getId().toString());
        bookingData.put("time", booking.getTime().format(formatter));
        bookingData.put("customer", booking.getCustomer());

        List<Map<String,Object>> tickets = new ArrayList<>();
        for (Ticket ticket: booking.getTickets()){
            Map<String, Object> ticketData = new HashMap<>();
            ticketData.put("trainCompany",ticket.getTrainCompany());
            ticketData.put("trainId",ticket.getTrainId().toString());
            ticketData.put("seatId",ticket.getSeatId().toString());
            ticketData.put("ticketId",ticket.getTicketId().toString());
            ticketData.put("customer",ticket.getCustomer());
            ticketData.put("bookingReference",ticket.getBookingReference());
            tickets.add(ticketData);
        }
        bookingData.put("tickets",tickets);


        ApiFuture<WriteResult> future = db.collection("bookings")
                .document(booking.getId().toString())
                .set(bookingData);
        future.get();
    }

    public Collection<Booking> getAllBookings() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> snapshots = db.collection("bookings").get().get().getDocuments();
        List<Booking> allBookings = new ArrayList<>();
        for (QueryDocumentSnapshot bookingSnapshot:snapshots) {
            allBookings.add(mapQueryDocumentSnapshotToBooking(bookingSnapshot));
        }
        return allBookings;
    }

    public Collection<String> geBestCustomers() throws ExecutionException, InterruptedException {
        Collection<Booking> bookings = getAllBookings();
        HashMap<String,Integer> usersAndBookings = new HashMap<>();

        for (Booking booking:bookings) {
            usersAndBookings.merge(booking.getCustomer(), booking.getTickets().size(), Integer::sum);
        }

        String userWithMaxTickets = "";
        int max = 0;
        for (String user:usersAndBookings.keySet()) {
            int usersTicket = usersAndBookings.get(user);
            if(usersTicket>=max){
                max = usersTicket;
                userWithMaxTickets = user;
            }
        }


        List<String> maxUsers = new ArrayList<>();
        for (String user:usersAndBookings.keySet()) {
            int usersTicket = usersAndBookings.get(user);
            if(usersTicket==max){
                maxUsers.add(user);
            }
        }

        return maxUsers;
    }

}
