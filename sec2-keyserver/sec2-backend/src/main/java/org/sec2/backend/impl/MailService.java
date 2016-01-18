package org.sec2.backend.impl;

import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * The {@code MailService} is used to send email via SMTP. 
 * 
 * @author Utimaco Safeware
 *
 */
public class MailService {
    
    private static final Logger LOGGER = Logger.getLogger(MailService.class.getName());
    private Session session = null;

    private Properties configuration;
    
    private static final String PROPERTIES_SMTP_HOST     = "mail.smtp.host";
    private static final String PROPERTIES_SMTP_PORT     = "mail.smtp.port";
    private static final String PROPERTIES_SMTP_USERNAME = "mail.smtp.username";
    private static final String PROPERTIES_SMTP_PASSWORD = "mail.smtp.password";
    private static final String PROPERTIES_MAIL_FROM     = "mail.from";
    private static final String PROPERTIES_MAIL_STARTTLS = "mail.smtp.starttls.enable";
    private static final String PROPERTIES_MAIL_AUTH     = "mail.smtp.auth";
    /**
     * 
     * @param configuration
     */
    public MailService(Properties configuration) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(
                MailService.class.getName(),
                "MailService",
                configuration
            );
        }
        this.configuration = configuration;
        Properties mailProperties = System.getProperties();
        mailProperties.put(
            PROPERTIES_SMTP_HOST, 
            configuration.getProperty(PROPERTIES_SMTP_HOST)
        );
        mailProperties.put(
            PROPERTIES_SMTP_PORT, 
            configuration.getProperty(PROPERTIES_SMTP_PORT)
        );
        mailProperties.put(
            PROPERTIES_MAIL_STARTTLS, 
            configuration.getProperty(PROPERTIES_MAIL_STARTTLS)
        );
        mailProperties.put(
            PROPERTIES_MAIL_AUTH, 
            configuration.getProperty(PROPERTIES_MAIL_AUTH)
        );
        
        this.session = Session.getDefaultInstance(mailProperties, null);
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.exiting(
                MailService.class.getName(),
                "MailService"
            );
        }
    }

    /**
     * 
     * @param message
     */
    private void send(MimeMessage message) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(
                MailService.class.getName(),
                "send",
                message
            );
        }
        Transport transport = null;
        try {
            transport = this.session.getTransport("smtp");

            transport.connect(
                configuration.getProperty(PROPERTIES_SMTP_USERNAME), 
                configuration.getProperty(PROPERTIES_SMTP_PASSWORD)
            );
            transport.sendMessage(message, message.getAllRecipients());
        }
        catch (Exception e) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("Error at sending mail: %s", e.getMessage()));
                e.printStackTrace();
            }
        } 
        finally {
            try {
                transport.close();
            }
            catch (MessagingException e) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine(String.format("Error at closing smtp connection: %s", e.getMessage()));
                    e.printStackTrace();
                }
            }
        }
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.exiting(
                MailService.class.getName(),
                "send"
            );
        }
    }

    /**
     * 
     * @param to
     * @param subject
     * @param body
     * @param attachments Currently not used.
     * 
     */
    public void send(
        String to, 
        String subject, 
        String body,
        HashMap<String, byte[]> attachments
    ) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(
                MailService.class.getName(),
                "send",
                new Object[]{ to, subject, body, attachments }
            );
        }
        try {
            MimeBodyPart textBodyPart = new MimeBodyPart();
    
            textBodyPart.setText(body);
    
            MimeMultipart mimeMultipart = new MimeMultipart();
            mimeMultipart.addBodyPart(textBodyPart);
    
            /* Currently just ignore attachments...
            if ((attachments != null) && (attachments.size() > 0)) {
                Iterator attachmentsIterator = attachments.keySet().iterator();
    
                while (attachmentsIterator.hasNext()) {
                    String filename = (String) attachmentsIterator.next();
    
                    byte[] attachmentsByte = (byte[]) attachments.get(filename);
    
                    DataSource dataSource = new ByteArrayDataSource(
                            attachmentsByte, "application/pdf");
                    MimeBodyPart attachmentBodyPart = new MimeBodyPart();
                    attachmentBodyPart.setDataHandler(new DataHandler(dataSource));
                    attachmentBodyPart.setFileName(filename);
    
                    mimeMultipart.addBodyPart(attachmentBodyPart);
                }
            }*/
    
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(configuration.getProperty(PROPERTIES_MAIL_FROM)));
    
            InternetAddress to_address = new InternetAddress(to);
    
            message.addRecipient(Message.RecipientType.TO, to_address);
    
            message.setSubject(subject);
            message.setContent(mimeMultipart);
    
            send(message);
        }
        catch (MessagingException e) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("Error at sending email: %s", e.getMessage()));
                e.printStackTrace();
            }
        }

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.exiting(
                MailService.class.getName(),
                "send"
            );
        }
    }
}
