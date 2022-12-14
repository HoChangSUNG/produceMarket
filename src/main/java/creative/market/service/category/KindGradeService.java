package creative.market.service.category;


import creative.market.domain.category.Item;
import creative.market.domain.category.KindGrade;
import creative.market.repository.category.KindGradeRepository;
import creative.market.service.dto.CriteriaSrcAndRetailUnitRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KindGradeService {

    private final KindGradeRepository kindGradeRepository;

    public CriteriaSrcAndRetailUnitRes findSrcAndRetailById(Long kindGradeId) {
        KindGrade findKindGrade = kindGradeRepository.findById(kindGradeId)
                .orElseThrow(() -> new NoSuchElementException("올바른 카테고리가 아닙니다"));
        // 소매 단위
        String retailUnit = findKindGrade.getKind().getRetailsaleUnit();

        // 등급 사진
        String criteriaSrc = null;
        Item item = findKindGrade.getKind().getItem();
        if (item.getGradeCriteria() != null) {// 등급 기준 사진이 없는 경우
            criteriaSrc = item.getGradeCriteria().getPath();
        }

        return new CriteriaSrcAndRetailUnitRes(criteriaSrc, retailUnit);
    }
}
