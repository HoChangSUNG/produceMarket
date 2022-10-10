package creative.market.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UploadFileDTO {

    private String uploadFileName; // 업로드 시 파일 이름
    private String storeFileName; // 저장했을 때 파일 이름

}
