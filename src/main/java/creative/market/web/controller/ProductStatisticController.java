package creative.market.web.controller;

import creative.market.domain.category.KindGrade;
import creative.market.repository.KindGradeRepository;
import creative.market.repository.dto.LatestRetailAndWholesaleDTO;
import creative.market.util.WholesaleAndRetailUtils;
import creative.market.web.dto.ResultRes;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/products-statistics")
public class ProductStatisticController {
    private final KindGradeRepository kindGradeRepository;
    private final WholesaleAndRetailUtils wholesaleAndRetailUtils;


    @GetMapping("/whole-and-retail/{kindGradeId}")
    public ResultRes getLatestWholesaleAndRetail(@PathVariable Long kindGradeId) {
        KindGrade findKindGrade = kindGradeRepository.findById(kindGradeId)
                .orElseThrow(() -> new NoSuchElementException("카테고리가 존재하지 않습니다."));

        LatestRetailAndWholesaleDTO priceResult = wholesaleAndRetailUtils.getLatestPriceInfo(findKindGrade);// 단위 변환 + 도소매 단위 다른 경우 처리 결과
        return new ResultRes(priceResult);

    }


}
