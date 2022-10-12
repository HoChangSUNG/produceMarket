package creative.market.service;


import creative.market.domain.category.Item;
import creative.market.domain.category.KindGrade;
import creative.market.repository.KindGradeRepository;
import creative.market.service.dto.CriteriaSrcAndRetailUnitDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KindGradeService {

    private final KindGradeRepository kindGradeRepository;

    public CriteriaSrcAndRetailUnitDTO findSrcAndRetailById(Long kindGradeId) { // 나중에 query서비스로 빼기
        KindGrade findKindGrade = kindGradeRepository.findById(kindGradeId)
                .orElseThrow(() -> new IllegalArgumentException("올바른 카테고리가 아닙니다"));
        // 소매 단위
        String retailUnit = findKindGrade.getKind().getRetailsaleUnit();

        // 등급 사진
        String criteriaSrc = null;
        Item item = findKindGrade.getKind().getItem();
        if (item.getGradeCriteria() != null) {
            criteriaSrc = item.getGradeCriteria().getPath();
        }

        return new CriteriaSrcAndRetailUnitDTO(criteriaSrc, retailUnit);
    }
}
