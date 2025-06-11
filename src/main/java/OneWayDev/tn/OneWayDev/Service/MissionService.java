package OneWayDev.tn.OneWayDev.Service;

import OneWayDev.tn.OneWayDev.Repository.*;
import OneWayDev.tn.OneWayDev.dto.request.MissionCreationDTO;
import OneWayDev.tn.OneWayDev.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MissionService {
    private final MissionRepository missionRepo;
    private final UserRepository utilisateurRepo;
    private final AffectationRepository affectationRepo;
    private final FileService fileService;

    private final EmailService emailService;
    public Mission createMission(MissionCreationDTO dto) {
        Mission mission = new Mission();
        mission.setTitre(dto.getTitle());
        mission.setDateDebut(dto.getDateDebut());
        mission.setStatut(StatutMission.EN_COURS);

        List<User> techs = findAvailable(RoleType.TECHNICIEN, dto);
        List<User> redacs = findAvailable(RoleType.REDACTEUR, dto);
        List<User> verifs = findAvailable(RoleType.VERIFICATEUR, dto);

        List<User> membres = new ArrayList<>();
        membres.addAll(techs.subList(0, 2));
        membres.addAll(redacs.subList(0, 2));
        membres.add(verifs.get(0));

        List<Affectation> affectations = new ArrayList<>();
        for (User user : membres) {
            Affectation aff = new Affectation();
            aff.setMission(mission);
            aff.setUtilisateur(user);
            aff.setStatut(StatutAffectation.EN_ATTENTE);
            affectations.add(aff);

            emailService.sendMissionMail(user, mission);
        }

        mission.setAffectations(affectations);

        return missionRepo.save(mission);
    }

    private List<User> findAvailable(RoleType role, MissionCreationDTO dto) {
        return utilisateurRepo.findByRolesRoleType(role).stream()
                .filter(u -> u.getAffectations().stream()
                        .noneMatch(a -> datesChevauchent(dto, a.getMission())))
                .collect(Collectors.toList());
    }

    private boolean datesChevauchent(MissionCreationDTO dto, Mission m) {

        return dto.getDateDebut().isEqual(m.getDateDebut());
    }
    public List<Mission> getAll() {
        return missionRepo.findAll();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void updateMissionsStatut() {
        LocalDate today = LocalDate.now();
        List<Mission> missions = missionRepo.findAll();

        for (Mission m : missions) {
            if (m.getStatut() == StatutMission.EN_COURS && allMembersConfirmed(m)) {
                m.setStatut(StatutMission.CONFIRMEE);
            }
            if (m.getDateDebut().isBefore(today)) {
                if (m.getStatut() == StatutMission.CONFIRMEE) {
                    m.setStatut(StatutMission.TERMINEE);
                } else if (m.getStatut() == StatutMission.EN_COURS) {
                    m.setStatut(StatutMission.EXPIREE);
                }
            }
        }

        missionRepo.saveAll(missions);
    }

    public boolean allMembersConfirmed(Mission mission) {
        List<Affectation> affectations = mission.getAffectations();

        if (affectations == null || affectations.isEmpty()) {
            return false;
        }

        long confirmedTechniciens = affectations.stream()
                .filter(a -> a.getUtilisateur().getRoles().stream()
                        .anyMatch(r -> r.getRoleType() == RoleType.TECHNICIEN))
                .filter(a -> a.getStatut() == StatutAffectation.ACCEPTEE)
                .count();

        long confirmedRedacteurs = affectations.stream()
                .filter(a -> a.getUtilisateur().getRoles().stream()
                        .anyMatch(r -> r.getRoleType() == RoleType.REDACTEUR))
                .filter(a -> a.getStatut() == StatutAffectation.ACCEPTEE)
                .count();

        long confirmedVerificateurs = affectations.stream()
                .filter(a -> a.getUtilisateur().getRoles().stream()
                        .anyMatch(r -> r.getRoleType() == RoleType.VERIFICATEUR))
                .filter(a -> a.getStatut() == StatutAffectation.ACCEPTEE)
                .count();


        return confirmedTechniciens >= 2 && confirmedRedacteurs >= 2 && confirmedVerificateurs >= 1;
    }

    public void acceptMission(Long missionId, Long userId) {
        Affectation member = affectationRepo.findByMissionIdAndUtilisateurId(missionId, userId)
                .orElseThrow(() -> new RuntimeException("Membre non trouvé"));
        member.setStatut(StatutAffectation.ACCEPTEE);
        affectationRepo.save(member);
    }

    public void rejectMission(Long missionId, Long userId) {
        Affectation member = affectationRepo.findByMissionIdAndUtilisateurId(missionId, userId)
                .orElseThrow(() -> new RuntimeException("Membre non trouvé"));

        Mission mission = member.getMission();
        User refusant = member.getUtilisateur();
        RoleType roleRefuse = refusant.getRoles().get(0).getRoleType(); // si multi-rôle, adapter

        // Marquer comme refusé
        member.setStatut(StatutAffectation.REFUSEE);
        affectationRepo.save(member);

        // Trouver un autre utilisateur disponible avec le même rôle
        List<User> candidats = utilisateurRepo.findByRolesRoleType(roleRefuse).stream()
                .filter(u -> !u.getId().equals(userId)) // exclure celui qui a refusé
                .filter(u -> u.getAffectations().stream()
                        .noneMatch(a -> datesChevauchent(new MissionCreationDTO(
                                mission.getTitre(),
                                mission.getDateDebut()
                        ), a.getMission())))
                .collect(Collectors.toList());

        if (!candidats.isEmpty()) {
            User nouveau = candidats.get(0);

            Affectation nouvelleAffectation = new Affectation();
            nouvelleAffectation.setMission(mission);
            nouvelleAffectation.setUtilisateur(nouveau);
            nouvelleAffectation.setStatut(StatutAffectation.EN_ATTENTE);

            affectationRepo.save(nouvelleAffectation);

            // notifier le nouveau membre
            emailService.sendAffectationEmail(nouveau, mission);
        } else {
            // Aucun remplaçant disponible
            System.out.println("Aucun utilisateur disponible pour remplacer le refus.");
        }
    }

    @Transactional
    public void deleteById(Long id) {
        Mission mission = missionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mission introuvable"));

        missionRepo.delete(mission);
    }
}
