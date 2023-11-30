package be.kuleuven.distributedsystems.cloud.persistance;

import be.kuleuven.distributedsystems.cloud.entities.Seat;
import be.kuleuven.distributedsystems.cloud.entities.Train;

import java.util.List;

public class TrainDTO {
    private String name;
    private String location;
    private String image;
    private List<Seat> seats;

    public TrainDTO() {
    }

    public TrainDTO(String name, String location, String image, List<Seat> seats) {
        this.name = name;
        this.location = location;
        this.image = image;
        this.seats = seats;
    }

    public TrainDTO(List<Seat> seats) {
        this.seats = seats;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getImage() {
        return image;
    }
}
