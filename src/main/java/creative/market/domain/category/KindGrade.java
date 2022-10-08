package creative.market.domain.category;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KindGrade {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kind_grade_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kind_id")
    private Kind kind;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id")
    private Grade grade;

    @Builder
    public KindGrade(Kind kind, Grade grade) {
        changeKind(kind);
        this.grade = grade;
    }

    //==연관관계 메서드==//
    public void changeKind(Kind kind) {
        if (kind != null) {
            this.kind = kind;
            kind.getKindGrades().add(this);
        }
    }
}
