package be.kuleuven.distributedsystems.cloud.controller.sendgrid;

import be.kuleuven.distributedsystems.cloud.controller.WEBClient;
import be.kuleuven.distributedsystems.cloud.entities.*;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class EmailController {
    @Autowired
    private Environment env;

    @Autowired
    private WEBClient webClient;
    private final Email from = new Email("jules.verbessem@student.kuleuven.be");
    public void sendConfirmationMailSucceded(Booking booking){
        String subject = "Confirmation tickets booking";
        Email to = new Email(booking.getCustomer());

        String body = makeBody(booking.getTickets(), booking.getCustomer());
        body += "Has been booked succesfully!\n\nKind regards.\n DNetTickets";
        sendEmail(subject,to,body);
    }

    private String makeBody(List<Ticket> tickets, String user) {

        var formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        StringBuilder builder = new StringBuilder();
        builder.append("Dear ").append(user.split("@")[0]).append(",\n");
        builder.append("Your tickets: \n");

        for (Ticket q: tickets) {
            boolean tryAgain = true;
            Seat seat = null;
            Train train = null;
            while (tryAgain){
                try{
                    seat = webClient.getSeat(q.getTrainCompany(),q.getTrainId().toString(),q.getSeatId().toString());
                    train =webClient.getTrain(q.getTrainCompany(),q.getTrainId().toString());
                    tryAgain = false;
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
            builder.append("- ").append(seat.getName()).append(" (").append(seat.getType()).append(") ");
            builder.append("for train ").append(train.getName()).append(" on ").append(seat.getTime().format(formatter)).append("\n");
        }
        return builder.toString();
    }

    private void sendEmail(String subject, Email to, String body){
        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, to, content);

        String SENDGRID_API_KEY = env.getProperty("spring.sendgrid.api-key");
        SendGrid sg = new SendGrid(SENDGRID_API_KEY);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println("Email send:" +response.getStatusCode());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void sendConfirmationMailFailed(Booking booking) {
        String subject = "Booking tickets failed";
        Email to = new Email(booking.getCustomer());

        String body = makeBody(booking.getTickets(), booking.getCustomer());
        body += "Has not booked! Sorry fot this inconvenience.\n\nKind regards.\n DNetTickets";

        sendEmail(subject,to,body);
    }

}
