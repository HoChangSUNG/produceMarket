package creative.market.service.dto;

import creative.market.domain.category.Kind;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class KindMenuDTO {

    private Long id;
    private String name;
    private List<KindGradeMenuDTO> category;
    public KindMenuDTO(Kind kind) {
        this.id = kind.getId();
        this.name = kind.getName();
        this.category = kind.getKindGrades().stream()
                .map(kindGrade -> new KindGradeMenuDTO(kindGrade))
                .collect(Collectors.toList());
    }
}
