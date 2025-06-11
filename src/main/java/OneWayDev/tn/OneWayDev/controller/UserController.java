package OneWayDev.tn.OneWayDev.controller;

import OneWayDev.tn.OneWayDev.dto.request.*;
import OneWayDev.tn.OneWayDev.entity.User;
import OneWayDev.tn.OneWayDev.Repository.UserRepository;
import OneWayDev.tn.OneWayDev.Service.UserService;
import OneWayDev.tn.OneWayDev.exception.EmailExistsExecption;
import OneWayDev.tn.OneWayDev.exception.NotFoundExecption;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("user")
@CrossOrigin("*")
@Validated
public class UserController {

    private UserService userService;
    private UserRepository userRepository;
    @GetMapping("/all")
    public List<User> getAll(){
        return userService.findAllUser();
    }
    @GetMapping("/email/{email}")
    public User getUserByEmail(@PathVariable(value = "email") String email){
        return userService.getUserByEmail(email);
    }
    @GetMapping("/findById/{idUser}")
    public User findUserById(@PathVariable(value = "idUser") Long idUser){
        return userService.findUserById(idUser);
    }



    @DeleteMapping("/delete/{idUser}")
    public ResponseEntity<Map<String, String>> removeCategorie(@PathVariable Long idUser) {
        Map<String, String> response = new HashMap<>();
        try {
            userService.deleteUser(idUser);
            response.put("message", "user dropped successfully");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (NotFoundExecption e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }
    @PutMapping("/{id}/block")
    public ResponseEntity<?> toggleUserBlockStatus(@PathVariable Long id, @RequestParam boolean isBlocked) {
        userService.toggleUserBlockStatus(id, isBlocked);
        return ResponseEntity.ok().build();
    }
  /*  @PutMapping("/block/{id}")
    public User blockUser(@PathVariable(value = "id") Long idUser){
        return userService.blockedUser(idUser, false);
    }
*/

    @PutMapping("/unblock/{id}")
    public User unblockUser(@PathVariable(value = "id") Long idUser){
        return userService.blockedUser(idUser, true);
    }

    @PostMapping("/upload-profile-picture")
    public ResponseEntity<Map<String, String>> uploadProfilePicture(@RequestParam("file") MultipartFile file, @RequestParam("email") String email) {
        String filePath = userService.uploadFile(file);
        User user = userService.getUserByEmail(email);
        user.setPhotoProfile(filePath);
        userRepository.save(user);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "Profile picture updated successfully");
        responseBody.put("filePath", filePath);

        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }


     @GetMapping("/image/{imageName}")
     public ResponseEntity<org.springframework.core.io.Resource> getImage(@PathVariable String imageName) {
        Path imagePath = Paths.get("src/main/resources/upload").resolve(imageName);
        try {
            org.springframework.core.io.Resource resource = new UrlResource(imagePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok().body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    @PutMapping("/profile-manage")
    public ResponseEntity<?> profileManage(@ModelAttribute @Valid ProfileRequest profileRequest, Principal principal){
        try {
            return new ResponseEntity<>(userService.profileManage(profileRequest,principal.getName()), HttpStatus.OK);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(new CustomErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

        @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody @Valid ChangePasswordRequest request, Principal principal) {
        String username = principal.getName();
        boolean isChanged = userService.changePassword(username, request.getOldPassword(), request.getNewPassword());

        if (isChanged) {
            return ResponseEntity.ok("Password changed successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to change password");
        }
    }

}
