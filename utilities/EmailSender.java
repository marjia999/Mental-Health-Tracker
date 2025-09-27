package utilities;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailSender {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String FROM_EMAIL = "elesebrown8@gmail.com";
    private static final String FROM_NAME = "Mental Health Tracker";
    private static final String APP_NAME = "Mental Health Tracker";

    public static void sendVerificationEmail(String toEmail, String verificationCode) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.ssl.trust", SMTP_HOST);

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(FROM_EMAIL, "dths khdb zjna dqlp");
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Your " + APP_NAME + " Verification Code");

            String htmlContent = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "    <style>" +
                    "        body { font-family: Arial, sans-serif; line-height: 1.6; }" +
                    "        .container { max-width: 600px; margin: 20px auto; padding: 20px; }" +
                    "        .header { color: #2c3e50; text-align: center; }" +
                    "        .code { " +
                    "            font-size: 24px; " +
                    "            font-weight: bold; " +
                    "            color: #3498db;" +
                    "            text-align: center;" +
                    "            margin: 20px 0;" +
                    "            padding: 10px;" +
                    "            background: #f8f9fa;" +
                    "            border-radius: 5px;" +
                    "        }" +
                    "        .footer { margin-top: 30px; font-size: 12px; color: #7f8c8d; }" +
                    "    </style>" +
                    "</head>" +
                    "<body>" +
                    "    <div class=\"container\">" +
                    "        <div class=\"header\">" +
                    "            <h2>Verify Your Account</h2>" +
                    "        </div>" +
                    "        " +
                    "        <p>Hello,</p>" +
                    "        " +
                    "        <p>Thank you for registering with <strong>" + APP_NAME + "</strong>. " +
                    "        Please use the following verification code to complete your registration:</p>" +
                    "        " +
                    "        <div class=\"code\">" + verificationCode + "</div>" +
                    "        " +
                    "        <p>This code will expire in 15 minutes. If you didn't request this, please ignore this email.</p>" +
                    "        " +
                    "        <div class=\"footer\">" +
                    "            <p>Best regards,<br>The " + APP_NAME + " Team</p>" +
                    "            <p><small>This is an automated message - please do not reply directly</small></p>" +
                    "        </div>" +
                    "    </div>" +
                    "</body>" +
                    "</html>";

            message.setContent(htmlContent, "text/html");

            Transport.send(message);
            System.out.println("✓ Verification email sent to: " + toEmail);

        } catch (Exception e) {
            System.err.println("✗ Failed to send verification email:");
            e.printStackTrace();
        }
    }

}