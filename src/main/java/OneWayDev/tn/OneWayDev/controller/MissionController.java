package OneWayDev.tn.OneWayDev.controller;

import OneWayDev.tn.OneWayDev.Service.MailTokenService;
import OneWayDev.tn.OneWayDev.Service.MissionService;
import OneWayDev.tn.OneWayDev.dto.request.MissionCreationDTO;
import OneWayDev.tn.OneWayDev.entity.MailToken;
import OneWayDev.tn.OneWayDev.entity.Mission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/missions")
@CrossOrigin("*")
@RequiredArgsConstructor
public class MissionController {
    private final MissionService missionService;
    private final MailTokenService mailTokenService;


    @PostMapping
    public ResponseEntity<Mission> create(@RequestBody MissionCreationDTO dto) {
        return ResponseEntity.ok(missionService.createMission(dto));
    }

    @GetMapping
    public List<Mission> getAll() {
        return missionService.getAll();
    }




    @GetMapping("/confirm")
    public ResponseEntity<String> confirmParticipation(@RequestParam String token) {
        MailToken mailToken = mailTokenService.getToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token not found"));

        if (mailToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("❌ Le lien a expiré.");
        }

        if (!"MISSION_CONFIRM".equals(mailToken.getPurpose())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("⛔ Lien invalide.");
        }

        mailTokenService.setConfirmedAt(token);
        missionService.acceptMission(mailToken.getMission().getId(), mailToken.getUser().getId());

        return ResponseEntity.ok("✅ Vous avez accepté la mission.");
    }

    @GetMapping("/reject")
    public ResponseEntity<String> rejectParticipation(@RequestParam String token) {
        MailToken mailToken = mailTokenService.getToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token not found"));

        if (mailToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("❌ Le lien a expiré.");
        }

        if (!"MISSION_REJECT".equals(mailToken.getPurpose())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("⛔ Lien invalide.");
        }

        mailTokenService.setConfirmedAt(token);
        missionService.rejectMission(mailToken.getMission().getId(), mailToken.getUser().getId());

        return ResponseEntity.ok("❌ Vous avez refusé la mission.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMission(@PathVariable Long id) {
        missionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
