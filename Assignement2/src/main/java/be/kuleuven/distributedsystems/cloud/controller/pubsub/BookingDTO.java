package be.kuleuven.distributedsystems.cloud.controller.pubsub;

import be.kuleuven.distributedsystems.cloud.entities.Quote;
import be.kuleuven.distributedsystems.cloud.entities.User;

import java.util.List;

public class BookingDTO {
    private List<Quote> quotes;
    private String user;

    public BookingDTO( ){
    }

    public BookingDTO( List<Quote> quotes, String user){
        this.quotes = quotes;
        this.user = user;
    }

    public List<Quote> getQuotes() {
        return quotes;
    }

    public String getUser() {
        return user;
    }

    public void setQuotes(List<Quote> quotes) {
        this.quotes = quotes;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
