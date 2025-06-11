package OneWayDev.tn.OneWayDev.Repository;

import OneWayDev.tn.OneWayDev.entity.Affectation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AffectationRepository extends JpaRepository<Affectation,Long> {
    Optional<Affectation> findByMissionIdAndUtilisateurId(Long missionId, Long userId);
}
