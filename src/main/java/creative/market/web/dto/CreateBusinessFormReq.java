package creative.market.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBusinessFormReq {

    @NotBlank
    private String businessNumber;

    @NotBlank
    private String businessName;

    private MultipartFile img;
}
