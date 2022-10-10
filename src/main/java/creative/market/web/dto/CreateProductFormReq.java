package creative.market.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductFormReq {

    private Long kindGradeId;
    private String name;
    private Integer price;
    private String info;
    private List<MultipartFile> img;
    private MultipartFile sigImg;
}
