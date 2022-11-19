package creative.market.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductReq {

    @NotNull
    private Long kindGradeId;

    @NotBlank
    private String name;

    @NotNull
    @Min(1)
    private Integer price;

    @NotBlank
    private String info;

    private List<MultipartFile> img;

    @NotNull
    private MultipartFile sigImg;
}
