package creative.market.service.dto;

import creative.market.domain.category.Kind;
import creative.market.domain.category.KindGrade;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KindGradeMenuDTO {

    private Long kindGradeId;
    private int gradeId;
    private String name;
    public KindGradeMenuDTO(KindGrade kindGrade) {
        this.kindGradeId = kindGrade.getId();
        this.gradeId = kindGrade.getGrade().getGradeId();
        this.name = kindGrade.getGrade().getGradeName();
    }
}
