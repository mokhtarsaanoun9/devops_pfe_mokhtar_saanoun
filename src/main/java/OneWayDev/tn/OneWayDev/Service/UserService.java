package OneWayDev.tn.OneWayDev.Service;

import OneWayDev.tn.OneWayDev.Repository.RoleRepository;
import OneWayDev.tn.OneWayDev.dto.request.*;
import OneWayDev.tn.OneWayDev.entity.MailToken;
import OneWayDev.tn.OneWayDev.entity.Role;
import OneWayDev.tn.OneWayDev.entity.User;
import OneWayDev.tn.OneWayDev.entity.RoleType;
import OneWayDev.tn.OneWayDev.Repository.UserRepository;
import OneWayDev.tn.OneWayDev.exception.EmailExistsExecption;
import OneWayDev.tn.OneWayDev.exception.NotFoundExecption;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.mail.MailSendException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder bCryptPasswordEncoder;
    private final FileService fileService;

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email " + email));
    }
    public List<User>findAllUser( ){
        List<User> users = userRepository.findByRolesRoleTypeNot(RoleType.ADMIN);
        return users;
    }
    public void toggleUserBlockStatus(Long userId, boolean shouldBeUnblocked) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));

        user.setNonLocked(shouldBeUnblocked); // true = unblocked, false = blocked
        userRepository.save(user);
    }


    public User findUserById(Long idUser){
       Optional<User>  user= userRepository.findById(idUser);
       if (!user.isPresent()){
           throw new NotFoundExecption("no user found");
       }
       return user.get();
    }


    public void deleteUser(Long idUser){
        User user= userRepository.findById(idUser).orElseThrow(()->new NotFoundExecption(" no user found"));
        userRepository.deleteById(idUser);
    }


    public User blockedUser(Long idUser, Boolean blocked){
        Optional<User>  findUser= userRepository.findById(idUser);
        if (!findUser.isPresent()){
            throw new NotFoundExecption("no user found");
        }

        User user= findUser.get();
        user.setNonLocked(blocked);
        return userRepository.save(user);
    }

    public int enableAppUser(String email) {
        return userRepository.enableAppUser(email);
    }




   public String uploadFile(MultipartFile file) {
    if (file.isEmpty()) {
        throw new IllegalArgumentException("Photo profile is required, should select a photo");
    }
    String filename = StringUtils.cleanPath(file.getOriginalFilename());
    try {
        if (filename.contains("..")) {
            throw new IllegalArgumentException("Cannot upload file with relative path outside current directory");
        }
        Path uploadDir = Paths.get("src/main/resources/upload");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path filePath = uploadDir.resolve(filename);
        if (Files.exists(filePath)) {
            // If file already exists, return the filename to save it in user's profile
            return filename;
        }

        Files.copy(file.getInputStream(), filePath);
        return filename;

    } catch (Exception e) {
        throw new RuntimeException("Failed to store file " + filename, e);
    }
}
    public User profileManage(ProfileRequest profileRequest, String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            user.setFirstName(profileRequest.getFirstName());
            user.setLastName(profileRequest.getLastName());
            user.setPhone(profileRequest.getMobileNumber());
            user.setEmail(profileRequest.getEmail());
            return userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update profile: " + e.getMessage());
        }
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundExecption("User not found"));

        if (bCryptPasswordEncoder.matches(oldPassword, user.getPassword())) {
            user.setPassword(bCryptPasswordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }

        return false;
    }
}
