package creative.market.web.controller;

import creative.market.service.ItemCategoryService;
import creative.market.service.KindGradeService;
import creative.market.service.dto.CriteriaSrcAndRetailUnitRes;
import creative.market.web.dto.CategoryRes;
import creative.market.web.dto.ResultRes;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/item-category")
public class ItemCategoryController {

    private final ItemCategoryService itemCategoryService;
    private final KindGradeService kindGradeService;

    @GetMapping
    public CategoryRes itemCategoryMenu() { // 부류,품목,품종,등급 선택 메뉴
        return new CategoryRes(itemCategoryService.findItemCategoryMenu());
    }

    @GetMapping("/{kindGradeId}")
    public ResultRes itemCriteriaSrcAndUnit(@PathVariable Long kindGradeId) {// 등급 src, 단위 조회
        CriteriaSrcAndRetailUnitRes criteriaAndUnit = kindGradeService.findSrcAndRetailById(kindGradeId);
        return new ResultRes(criteriaAndUnit);
    }

}
