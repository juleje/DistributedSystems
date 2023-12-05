package be.kuleuven.distributedsystems.cloud.persistance;

import be.kuleuven.distributedsystems.cloud.entities.Booking;
import be.kuleuven.distributedsystems.cloud.entities.Seat;
import be.kuleuven.distributedsystems.cloud.entities.Ticket;
import be.kuleuven.distributedsystems.cloud.entities.Train;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static be.kuleuven.distributedsystems.cloud.auth.SecurityFilter.getUser;

@Component
public class FirestoreRepository {

    private Firestore db;
    private DateTimeFormatter formatter;

    public FirestoreRepository(){
        FirestoreOptions firestoreOptions =
                FirestoreOptions.getDefaultInstance().toBuilder()
                        .setEmulatorHost("localhost:8084")
                        .setCredentials(new FirestoreOptions.EmulatorCredentials())
                        .setProjectId("demo-distributed-systems-kul")
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

    public void addJsonData(Train train, List<Seat> seats) throws ExecutionException, InterruptedException {
        Map<String, Object> trainMap = new HashMap<>();
        String trainId = train.getTrainId().toString();
        trainMap.put("id", trainId);
        trainMap.put("company", train.getTrainCompany());
        trainMap.put("name", train.getName());
        trainMap.put("image", train.getImage());
        trainMap.put("location", train.getLocation());
        List<Seat> seatsList = new ArrayList<>();
       // trainMap.put("seats", seatsList);
        ApiFuture<WriteResult> future1 = db.collection("trains").document(trainId).set(trainMap);
        boolean done = future1.isDone();
        while (!done){
            done = future1.isDone();
        }

        for (Seat seat : seats) {
            Map<String, Object> seatData = new HashMap<>();
            seatData.put("trainCompany",seat.getTrainCompany());
            seatData.put("trainId",seat.getTrainId().toString());
            seatData.put("seatId",seat.getSeatId().toString());
            seatData.put("time",seat.getTime().format(formatter));
            seatData.put("type",seat.getType());
            seatData.put("name",seat.getName());
            seatData.put("price",seat.getPrice());
            db.collection("trains").document(trainId).collection("seats").add(seatData);
        }

    }
    public boolean trainCollectionCheck() throws ExecutionException, InterruptedException {
        Boolean returnBoolean = false;
        ApiFuture<QuerySnapshot> query = db.collection("trains").get();
        QuerySnapshot querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
        for (QueryDocumentSnapshot document : documents) {
            if (document.getData().isEmpty()){
                returnBoolean = true;
            }
        }
        return returnBoolean;
    }
}
