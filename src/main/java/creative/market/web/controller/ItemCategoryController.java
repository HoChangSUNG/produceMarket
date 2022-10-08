package creative.market.web.controller;

import creative.market.service.ItemCategoryService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/itemCategory")
public class ItemCategoryController {

    private final ItemCategoryService itemCategoryService;

    @GetMapping
    public ItemCategoryMenuResult itemCategoryMenu() { // 부류,품목,품종,등급 선택 메뉴
        return new ItemCategoryMenuResult(itemCategoryService.findItemCategoryMenu());
    }

    @Data
    @AllArgsConstructor
    static class ItemCategoryMenuResult<T> {
        private T result;
    }
}
