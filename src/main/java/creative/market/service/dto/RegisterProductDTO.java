package creative.market.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class RegisterProductDTO {

    private Long kindGradeId;
    private String name;
    private int price;
    private String info;
    private Long sellerId;

    private UploadFileDTO sigImg;
    private List<UploadFileDTO> ordinalImg;


}
