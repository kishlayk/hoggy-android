package com.my.kiki.mailclient;

import android.content.Context;
import android.os.AsyncTask;

import com.my.kiki.utils.Utils;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMail extends AsyncTask<Void,Void,Void> {

    private Context context;
    private Session session;
    private String toEmail;
    private String userEmail;
    private String subject;
    private String message;

    public SendMail(Context context, String toEmail, String subject, String message) {
        this.context = context;
        this.toEmail = toEmail;
        this.subject = subject;
        this.message = message;
        this.userEmail = Utils.getInstance(context).getString(Utils.PREF_USER_EMAIL);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Properties props = new Properties();

        String gmailPort = "587";

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", gmailPort);
        props.put("mail.smtp.starttls.enable", "true");

           session = Session.getDefaultInstance(props,null);
        try {

            MimeMessage mm = new MimeMessage(session);

            mm.setFrom(new InternetAddress(Config.fromMailID));
            mm.setRecipient(Message.RecipientType.TO, new InternetAddress(Config.toMailID));
            mm.addRecipient(Message.RecipientType.TO,new InternetAddress(userEmail));
            String mailContentText = "Title: " + subject + "\n\n" + message;
            mm.setSubject(Config.supportSubject);
            mm.setText(mailContentText);

            String emailHost = "smtp.gmail.com";
            Transport transport = session.getTransport("smtp");

            transport.connect(emailHost, Config.fromMailID,Config.fromP);
            transport.sendMessage(mm, mm.getAllRecipients());
            transport.close();

        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
