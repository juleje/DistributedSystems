package be.kuleuven.distributedsystems.cloud.controller.sendgrid;

import be.kuleuven.distributedsystems.cloud.entities.Quote;
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
import java.util.List;

@Component
public class EmailController {
    @Autowired
    private Environment env;
    private Email from = new Email("jules.verbessem@student.kuleuven.be");
    public void sendConfirmationMailSucceded(List<Quote> quotes, String user){
        String subject = "Confirmation tickets booking";
        Email to = new Email(user);

        StringBuilder listtickets = new StringBuilder();
        for (Quote q: quotes) {
            listtickets.append(q.getTrainCompany()+": seat "+q.getSeatId()+" for train "+q.getTrainId()+"\n");
        }
        String body = "You booked the tickets: \n"+listtickets.toString()+"Kind regards.\n DNetTickets";
        sendEmail(subject,to,body);
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

    public void sendConfirmationMailFailed(List<Quote> quotes, String user) {
        String subject = "Booking tickets failed";
        Email to = new Email(user);

        StringBuilder listtickets = new StringBuilder();
        for (Quote q: quotes) {
            listtickets.append(q.getTrainCompany()+": seat "+q.getSeatId()+" for train "+q.getTrainId()+"\n");
        }
        String body = "The booking failed for the tickets: \n"+listtickets.toString()+"Kind regards.\n DNetTickets";
        sendEmail(subject,to,body);
    }
}
