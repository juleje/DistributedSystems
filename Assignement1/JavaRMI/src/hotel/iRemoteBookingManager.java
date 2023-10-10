package hotel;



import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.Set;

public interface iRemoteBookingManager extends Remote {
    boolean isRoomAvailable(Integer var1, LocalDate var2) throws RemoteException;

    void addBooking(BookingDetail var1) throws RemoteException;

    Set<Integer> getAvailableRooms(LocalDate var1) throws RemoteException;

    Set<Integer> getAllRooms() throws RemoteException ;
}