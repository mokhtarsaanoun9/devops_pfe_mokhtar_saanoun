package OneWayDev.tn.OneWayDev;

import OneWayDev.tn.OneWayDev.entity.*;
import OneWayDev.tn.OneWayDev.Repository.RoleRepository;
import OneWayDev.tn.OneWayDev.Repository.UserRepository;
import OneWayDev.tn.OneWayDev.config.RsakeysConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.stream.Stream;

@SpringBootApplication
@EnableConfigurationProperties(RsakeysConfig.class)
@RequiredArgsConstructor
public class OneWayDevApplication implements CommandLineRunner {
	private  final RoleRepository roleRepository;

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	public static void main(String[] args) {
		SpringApplication.run(OneWayDevApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {


		if (roleRepository.count() == 0) {
			Stream.of(RoleType.ADMIN,RoleType.REDACTEUR,RoleType.TECHNICIEN,RoleType.VERIFICATEUR)
					.forEach(roleType -> {
						Role role = new Role();
						role.setRoleType(roleType);
						roleRepository.save(role);
					});

			for (int i = 1; i <= 10; i++) {
				RoleType roleType;
				if (i % 3 == 1) roleType = RoleType.TECHNICIEN;
				else if (i % 3 == 2) roleType = RoleType.REDACTEUR;
				else roleType = RoleType.VERIFICATEUR;

				String email = roleType.name().toLowerCase() + i + "@1waydev.tn";
				String password = roleType.name().substring(0, 4).toLowerCase() + i + "#Pwd";

				createUserIfNotExists(email, roleType.name().charAt(0) + roleType.name().substring(1).toLowerCase(), "User" + i, roleType.name(), password);
			}

			User admin = new User();
			Role role = roleRepository.findByRoleType(RoleType.ADMIN).get();
			admin.setFirstName("admin");
			admin.setLastName("admin");
			admin.setEmail("admin@1waydev.tn");
			admin.setPhone("28598395");
			admin.setRoles(List.of(role));
			admin.setEnabled(true);
			admin.setNonLocked(true);
			admin.setPassword(passwordEncoder.encode("adminADMIN#1919"));
			userRepository.save(admin);

		}
	}

	private void createUserIfNotExists(String email, String firstName, String lastName, String roleName, String password) {
		if (userRepository.findByEmail(email).isEmpty()) {
			Role role = roleRepository.findByRoleType(RoleType.valueOf(roleName)).orElseThrow();
			User user = new User();
			user.setFirstName(firstName);
			user.setLastName(lastName);
			user.setEmail(email);
			user.setPhone("00000000");
			user.setRoles(List.of(role));
			user.setEnabled(true);
			user.setNonLocked(true);
			user.setPassword(passwordEncoder.encode(password));
			userRepository.save(user);
		}
	}
}
