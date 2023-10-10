package hotel;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class HotelServer {

    public static void main(String[] args){
        //System.setSecurityManager(null); depricated

        iRemoteBookingManager manager = new BookingManager();
        try {
            iRemoteBookingManager stub = (iRemoteBookingManager) UnicastRemoteObject.exportObject(manager,5000);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind("BookingManager",stub);
        } catch (AlreadyBoundException e) {
            System.out.println("Can not bind RemoteBookingManager as a stub");
        }  catch (RemoteException e) {
            System.out.println("Can not start the Hotel server.");
            throw new RuntimeException(e);
        }
    }
}
