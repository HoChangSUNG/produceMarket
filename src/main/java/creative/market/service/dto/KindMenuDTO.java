package creative.market.service.dto;

import creative.market.domain.category.Kind;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class KindMenuDTO {

    private Long kindId;
    private String name;
    private String retailUnit;
    private List<KindGradeMenuDTO> grades;
    public KindMenuDTO(Kind kind) {
        this.kindId = kind.getId();
        this.name = kind.getName();
        this.retailUnit = kind.getRetailsaleUnit();
        this.grades = kind.getKindGrades().stream()
                .map(kindGrade -> new KindGradeMenuDTO(kindGrade))
                .collect(Collectors.toList());
    }
}
