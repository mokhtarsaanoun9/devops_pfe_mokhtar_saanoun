package OneWayDev.tn.OneWayDev.Service;


import OneWayDev.tn.OneWayDev.entity.MailToken;
import OneWayDev.tn.OneWayDev.entity.Mission;
import OneWayDev.tn.OneWayDev.entity.User;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final MailTokenService mailTokenService;
    private final JavaMailSender mailSender;
    @Async
    public void sendEmail(String to, String email) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setText(email, true);
            helper.setTo(to);
            helper.setSubject("Confirm your email");
            helper.setFrom("hello@esprit.com");
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new IllegalStateException("failed to send email");
        }
    }
    public void sendAffectationEmail(User user, Mission mission) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Nouvelle mission: " + mission.getTitre());
        message.setText("Vous avez été affecté à une mission du " + mission.getDateDebut() );
        mailSender.send(message);
    }


    public void sendMissionMail(User user, Mission mission) {
        String confirmToken = mailTokenService.createMissionToken(user, mission, "MISSION_CONFIRM", 15).getToken();
        String rejectToken = mailTokenService.createMissionToken(user, mission, "MISSION_REJECT", 15).getToken();

        String confirmLink = "http://localhost:1919/api/missions/confirm?token=" + confirmToken;
        String rejectLink = "http://localhost:1919/api/missions/reject?token=" + rejectToken;

        String subject = "Nouvelle mission assignée";
        String htmlContent = generateMissionMail(user.getFirstName(), mission.getDateDebut(), confirmLink, rejectLink);

        // send email logic
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
            mimeMessage.setFrom(new InternetAddress("noreply@localhost.com"));
            mimeMessage.setSubject(subject);
            mimeMessage.setContent(htmlContent, "text/html; charset=utf-8");
        };

        mailSender.send(messagePreparator);
    }


    public static String generateMissionMail(String name, LocalDate date, String confirmLink, String rejectLink) {
        return """
        <p>Bonjour %s,</p>
        <p>Vous avez été assigné à une mission prévue pour le %s.</p>
        <p>Veuillez confirmer votre participation :</p>

        <a href="%s" style="background-color: #4CAF50; color: white; padding: 10px 20px;
        text-decoration: none; border-radius: 5px;">✅ Accepter</a>

        <a href="%s" style="background-color: #F44336; color: white; padding: 10px 20px;
        text-decoration: none; border-radius: 5px; margin-left: 10px;">❌ Refuser</a>
        """.formatted(name, date, confirmLink, rejectLink);
    }
}
