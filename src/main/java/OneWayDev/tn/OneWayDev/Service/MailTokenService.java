package OneWayDev.tn.OneWayDev.Service;

import OneWayDev.tn.OneWayDev.entity.MailToken;
import OneWayDev.tn.OneWayDev.Repository.MailTokenRepository;
import OneWayDev.tn.OneWayDev.entity.Mission;
import OneWayDev.tn.OneWayDev.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MailTokenService {

    private final MailTokenRepository mailTokenRepository;

    public void saveConfirmationToken(MailToken token) {
        mailTokenRepository.save(token);
    }

    public Optional<MailToken> getToken(String token) {
        return mailTokenRepository.findByToken(token);
    }

    public int setConfirmedAt(String token) {
        return mailTokenRepository.updateConfirmedAt(token, LocalDateTime.now());
    }

    public MailToken createMissionToken(User user, Mission mission, String purpose, int minutesValid) {
        MailToken token = MailToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .mission(mission)
                .purpose(purpose)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(minutesValid))
                .build();
        mailTokenRepository.save(token);
        return token;
    }
}
