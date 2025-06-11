package OneWayDev.tn.OneWayDev.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileAgencyRequest {
    @Valid

    @NotBlank(message = " name  is required and cannot be blank.")
    @Size(min=3,max = 25,message = " name length min is 3 and max is 25")
    private String name;
    @Pattern(regexp = "\\d{8}", message = "Invalid phone number format. It should be 8 numbers.")
    private String mobileNumber;
    private String email;
}
