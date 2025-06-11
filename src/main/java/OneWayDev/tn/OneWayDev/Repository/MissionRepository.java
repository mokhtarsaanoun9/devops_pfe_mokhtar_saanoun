package OneWayDev.tn.OneWayDev.Repository;

import OneWayDev.tn.OneWayDev.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MissionRepository extends JpaRepository<Mission,Long> {

}
