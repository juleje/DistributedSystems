package be.kuleuven.distributedsystems.cloud.persistance;

import be.kuleuven.distributedsystems.cloud.entities.Seat;
import be.kuleuven.distributedsystems.cloud.entities.Train;

import java.util.List;

public class TrainDTO {

    private Train train;
    private List<Seat> seats;


    public TrainDTO() {
    }
    public TrainDTO(Train train, List<Seat> seats) {
        this.train = train;
        this.seats = seats;
    }

    public Train getTrain() {
        return train;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

}
