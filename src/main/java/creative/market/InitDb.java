package creative.market;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class InitDb {

//    private final KindGradeTestService kindGradeTestService;
//    private final ItemService itemService;

    @PostConstruct
    public void init() {
//        kindGradeTestService.saveAll(); // 품종 등급까지 더미 데이터
//        itemService.addGradeCriteriaId(); // 등급기준번호 추가
    }
}
