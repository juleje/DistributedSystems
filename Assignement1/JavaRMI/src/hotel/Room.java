package hotel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Room {

	private Integer roomNumber;
	private List<BookingDetail> bookings;

	public Room(Integer roomNumber) {
		this.roomNumber = roomNumber;
		bookings = new ArrayList<BookingDetail>();
	}

	public Integer getRoomNumber() {
		return roomNumber;
	}

	public void setRoomNumber(Integer roomNumber) {
		this.roomNumber = roomNumber;
	}

	public List<BookingDetail> getBookings() {
		return bookings;
	}

	public void setBookings(List<BookingDetail> bookings) {
		this.bookings = bookings;
	}

	public boolean isAvailable(LocalDate date) {
		boolean available = true;
		for (BookingDetail detail:bookings) {
			if(detail.isDuring(date)){
				available = false;
			}
		}
		return available;
	}

	public void addBooking(BookingDetail bookingDetail) {

	}
}