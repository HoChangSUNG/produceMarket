package creative.market.service.dto;

import creative.market.domain.category.KindGrade;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KindGradeMenuDTO {

    private Long id;
    private String name;
    public KindGradeMenuDTO(KindGrade kindGrade) {
        this.id = kindGrade.getId();
        this.name = kindGrade.getGrade().getGradeName();
    }
}
