package com.jpruim.vpnreport;

import java.util.*;
import javax.mail.*;
import javax.mail.Session;
import javax.mail.internet.*;


public class EmailSender {
    static void SendEmail(String to, String from, String server, String content, String title){
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", server);
        Session session = Session.getDefaultInstance(properties);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(title);
            message.setContent(content, "text/html");
            Transport.send(message);
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
}
