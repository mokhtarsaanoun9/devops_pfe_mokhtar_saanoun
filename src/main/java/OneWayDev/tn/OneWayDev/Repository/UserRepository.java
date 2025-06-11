package OneWayDev.tn.OneWayDev.Repository;

import OneWayDev.tn.OneWayDev.entity.User;
import OneWayDev.tn.OneWayDev.entity.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User>findByEmail(String email);
    Optional<User>findByPhone(String mobileNumber);
    List<User> findByRolesRoleType(RoleType roleType);
    List<User> findByRolesRoleTypeNot(RoleType roleType);
    // Page<User> findByRolesRoleType(RoleType type, Pageable pageable);
    @Transactional
    @Modifying
    @Query("UPDATE User u " +
            "SET u.enabled = TRUE WHERE u.email = ?1")
    int enableAppUser(String email);
}
