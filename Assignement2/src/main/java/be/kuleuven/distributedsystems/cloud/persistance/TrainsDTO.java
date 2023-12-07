package be.kuleuven.distributedsystems.cloud.persistance;

import be.kuleuven.distributedsystems.cloud.entities.Train;

import java.util.List;

public class TrainsDTO {
    private List<TrainDTO> trains;

    public TrainsDTO() {
    }
    public TrainsDTO(List<TrainDTO> trains) {
        this.trains = trains;
    }
    public List<TrainDTO> getTrains() {
        return trains;
    }

    public void setTrains(List<TrainDTO> trains) {
        this.trains = trains;
    }
}
