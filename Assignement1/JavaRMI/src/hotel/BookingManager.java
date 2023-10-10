package hotel;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

public class BookingManager implements iRemoteBookingManager {

	private Room[] rooms;

	public BookingManager() {
		this.rooms = initializeRooms();
	}

	public Set<Integer> getAllRooms() throws RemoteException {
		Set<Integer> allRooms = new HashSet<Integer>();
		Iterable<Room> roomIterator = Arrays.asList(rooms);
		for (Room room : roomIterator) {
			allRooms.add(room.getRoomNumber());
		}
		return allRooms;
	}

	public boolean isRoomAvailable(Integer roomNumber, LocalDate date) throws RemoteException{
		for (int i = 0; i<rooms.length; i++){
			if(rooms[i].getRoomNumber().equals(roomNumber)){
				if(rooms[i].isAvailable(date)){
					return true;
				}
			}
		}
		return false;
	}

	public void addBooking(BookingDetail bookingDetail) throws RemoteException{
		Room room = getRoom(bookingDetail.getRoomNumber());
		if(room.isAvailable(bookingDetail.getDate())){
			room.addBooking(bookingDetail);
		}
		DateTimeFormatter pattern = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		throw new RemoteException("Room with number " + bookingDetail.getRoomNumber() + " is not available on " + bookingDetail.getDate().format(pattern));
	}

	private Room getRoom(Integer roomnumber) throws RemoteException{
		Optional<Room> optionalRoom = Arrays.stream(rooms).filter(room -> Objects.equals(room.getRoomNumber(), roomnumber)).findFirst();
		if(optionalRoom.isPresent()){
			return  optionalRoom.get();
		}
		throw new RuntimeException("Room with number " + roomnumber + " is not present.");
	}

	public Set<Integer> getAvailableRooms(LocalDate date) throws RemoteException{
		Set<Integer> allRooms = new HashSet<Integer>();
		Iterable<Room> roomIterator = Arrays.asList(rooms);
		for (Room room : roomIterator) {
			if(room.isAvailable(date)){
				allRooms.add(room.getRoomNumber());
			}
		}
		return allRooms;
	}

	private static Room[] initializeRooms() {
		Room[] rooms = new Room[4];
		rooms[0] = new Room(101);
		rooms[1] = new Room(102);
		rooms[2] = new Room(201);
		rooms[3] = new Room(203);
		return rooms;
	}
}
