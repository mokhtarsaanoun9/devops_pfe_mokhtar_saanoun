package OneWayDev.tn.OneWayDev.Service;

import OneWayDev.tn.OneWayDev.Repository.*;
import OneWayDev.tn.OneWayDev.dto.request.MissionCreationDTO;
import OneWayDev.tn.OneWayDev.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MissionServiceTest {

    @Mock
    private MissionRepository missionRepo;

    @Mock
    private UserRepository utilisateurRepo;

    @Mock
    private AffectationRepository affectationRepo;

    @Mock
    private FileService fileService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private MissionService missionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createMission_shouldCreateMissionCorrectly() {
        // Given
        MissionCreationDTO dto = new MissionCreationDTO("Mission Test", LocalDate.now());

        List<User> techs = createUsers(RoleType.TECHNICIEN, 3);
        List<User> redacs = createUsers(RoleType.REDACTEUR, 3);
        List<User> verifs = createUsers(RoleType.VERIFICATEUR, 2);

        when(utilisateurRepo.findByRolesRoleType(RoleType.TECHNICIEN)).thenReturn(techs);
        when(utilisateurRepo.findByRolesRoleType(RoleType.REDACTEUR)).thenReturn(redacs);
        when(utilisateurRepo.findByRolesRoleType(RoleType.VERIFICATEUR)).thenReturn(verifs);

        Mission missionToReturn = new Mission();
        missionToReturn.setId(1L);
        when(missionRepo.save(any(Mission.class))).thenReturn(missionToReturn);

        // When
        Mission result = missionService.createMission(dto);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(emailService, times(5)).sendMissionMail(any(User.class), any(Mission.class));
        verify(missionRepo).save(any(Mission.class));
    }

    private List<User> createUsers(RoleType roleType, int count) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            User user = new User();
            user.setId((long) i + 1);
            Role role = new Role();
            role.setRoleType(roleType);
            user.setRoles(List.of(role));
            user.setAffectations(new ArrayList<>());
            users.add(user);
        }
        return users;
    }
}
