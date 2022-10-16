package creative.market.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductFormReq {
    @NotNull(message = "카테고리를 선택해주세요")
    private Long kindGradeId;
    @NotBlank(message = "상품 이름을 입력해주세요")
    private String name;
    @NotNull(message = "가격을 입력해주세요")
    private Integer price;

    @NotBlank(message = "내용을 입력해주세요")
    private String info;

    private List<MultipartFile> img;
    private MultipartFile sigImg;
}
