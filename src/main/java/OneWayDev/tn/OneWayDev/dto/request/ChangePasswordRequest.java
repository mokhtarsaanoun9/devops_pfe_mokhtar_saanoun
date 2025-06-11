package OneWayDev.tn.OneWayDev.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {
    @Valid
    @NotBlank(message = "old password is required and cannot be blank.")
    private String oldPassword;
    @NotBlank(message = "new password is required and cannot be blank.")
    private String newPassword;
}
