package org.sec2.backend.impl;

import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

/**
 * Convenient class that contains methods used for everything related to emails.
 * 
 * @author Utimaco Safeware
 *
 */
public class MailHelper {

    /**
     * The logger.
     */
    private static final Logger LOGGER = Logger.getLogger(KeyServer.class.getName());
    
    /**
     * Email Regex.
     */
    public final static String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    /**
     * TODO: documentation
     * 
     * @param mailService
     * @param emailAddress
     * @param challenge
     * @param timestamp
     */
    public static void sendChallengeToUser(MailService mailService, String emailAddress, byte[] challenge, long timestamp) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(
                MailHelper.class.getName(),
                "sendChallengeToUser", 
                new Object[]{ mailService, emailAddress, challenge, timestamp }
            );
        }
        String readableChallenge = DatatypeConverter.printHexBinary(challenge);
        mailService.send(
            "sec2.keyserver@googlemail.com", // TODO: change this to emailAddress in production mode
            "Please confirm your registration at Sec2",
            // TODO: Create some sort of magic URL that has all necessary information for the client  
            String.format("Dear Sir/Madam,\n"
                        + "Thanks for your registration at Sec2.\n"
                        + "To activate your account enter the following activation key in your Sec2 app.\n\n"
                        + "Your activation key: %s\n\n\n"
                        + "Thank you,\n Sec2 Administration Team", readableChallenge),
            null
        );
        
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(
                MailHelper.class.getName(),
                "sendChallengeToUser"
            );
        }
    }
    
    /**
     * Extracts the {@code EMAILADDRESS} field from the issuer's DN field in a 
     * X.509 certificate.
     * 
     * @param certificate The {@link X509Certificate} from where the email 
     *                    address is extracted. 
     * @return A valid email address if found, {@code null} otherwise.
     */
    public static String extractEmailAddress(X509Certificate certificate) {
        String issuerDN = certificate.getIssuerDN().getName();
        String emailAddress = extractEmailAddressFromString(issuerDN);
        return emailAddress;
    }
    

    /**
     * Extracts the {@code EMAILADDRESS=} field from a given string and 
     * returns everything behind it before the next COMMA appears if the 
     * string in between is a valid email address. The email check is 
     * performed with the regular expression in {@link #EMAIL_PATTERN}. 
     *    
     * @param haystack String to search in
     * @return A valid email address if found, {@code null} otherwise.
     */
    public static String extractEmailAddressFromString(String haystack) {
        // TODO: implement more reliable email extraction and testing
        haystack = haystack.toLowerCase();
        int index = haystack.indexOf("emailaddress=");
        if (index == -1)
            return null;
        haystack = haystack.substring(index + "emailaddress=".length());
        index = haystack.indexOf(",");
        if (index == -1)
            index = haystack.length();
        String possiblyMail = haystack.substring(0, index);
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(possiblyMail);
        if (matcher.matches()) {
            return possiblyMail;
        }
        return null;
    }

}
