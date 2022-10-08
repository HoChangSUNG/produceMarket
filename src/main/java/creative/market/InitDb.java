package creative.market;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class InitDb {

    private final InitGradeCriteria initGradeCriteria;
    private final KindGradeTestService kindGradeTestService;

    @PostConstruct
    public void init() {
        initGradeCriteria.saveGradeCriteria(); // 상품 등급 기준 이미지 경로를 엔티티 저장
        kindGradeTestService.saveAll(); // 품종 등급까지 더미 데이터
    }
}
