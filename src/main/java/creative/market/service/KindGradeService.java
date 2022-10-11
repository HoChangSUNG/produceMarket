package creative.market.service;


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
        String retailUnit = findKindGrade.getKind().getRetailsaleUnit();
        String criteriaSrc = findKindGrade.getKind().getItem().getGradeCriteria().getPath();

        return new CriteriaSrcAndRetailUnitDTO(criteriaSrc, retailUnit);
    }
}
