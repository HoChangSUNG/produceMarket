package creative.market.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductFormReq {

    @NotNull
    private Long kindGradeId;
    @NotEmpty
    private String name;
    @NotNull
    private Integer price;
    @NotEmpty
    private String info;
    @NotNull
    private List<MultipartFile> img;
    @NotNull
    private MultipartFile sigImg;
}
