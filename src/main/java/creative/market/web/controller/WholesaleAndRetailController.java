package creative.market.web.controller;

import creative.market.domain.category.KindGrade;
import creative.market.repository.KindGradeRepository;
import creative.market.util.WholesaleAndRetailUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("api/wholesale-and-retail")
@RequiredArgsConstructor
@Slf4j
public class WholesaleAndRetailController {

    private final KindGradeRepository kindGradeRepository;

    @GetMapping("/yearly")
    public String yearlyWholesaleAndRetail(@RequestParam Long kindGradeId) {
        WholesaleAndRetailApiParam params = getMonthAndYearApiParams(kindGradeId);
        log.info("[최근 5년 연도별 도소매 api] itemCategoryCode ={} itemCode={} kindCode={} gradeRank={}", params.itemCategoryCode, params.itemCode, params.kindCode, params.gradeRank);

        return WholesaleAndRetailUtils.getYearData(params.itemCategoryCode, params.itemCode, params.kindCode, params.gradeRank);
    }


    @GetMapping("/monthly")
    public String monthlyWholesaleAndRetail(@RequestParam int year, @RequestParam Long kindGradeId) {
        WholesaleAndRetailApiParam params = getMonthAndYearApiParams(kindGradeId);
        log.info("[월별 도소매 api]itemCategoryCode ={} itemCode={} kindCode={} gradeRank={}", params.itemCategoryCode, params.itemCode, params.kindCode, params.gradeRank);

        return WholesaleAndRetailUtils.getMonthlyWholeSalesAndRetailData(year, params.itemCategoryCode, params.itemCode, params.kindCode, params.gradeRank);
    }

    @GetMapping("/day/wholesale")
    public String dayWholesale(@RequestParam LocalDateTime start, @RequestParam LocalDateTime end, @RequestParam Long kindGradeId) {
        DayWholesaleAndRetailApiParam params = getDayApiParams(kindGradeId);
        log.info("[일별 도매 api]itemCategoryCode ={} itemCode={} kindCode={} gradeRank={}", params.itemCategoryCode, params.itemCode, params.kindCode, params.gradeId);

        return WholesaleAndRetailUtils.getDayWholeSalesData(start, end, params.itemCategoryCode, params.itemCode, params.kindCode, params.gradeId);
    }

    @GetMapping("/day/retail")
    public String dayRetail(@RequestParam LocalDateTime start, @RequestParam LocalDateTime end, @RequestParam Long kindGradeId) {
        DayWholesaleAndRetailApiParam params = getDayApiParams(kindGradeId);
        log.info("[일별 소매 api]itemCategoryCode ={} itemCode={} kindCode={} gradeRank={}", params.itemCategoryCode, params.itemCode, params.kindCode, params.gradeId);

        return WholesaleAndRetailUtils.getDayRetailData(start, end, params.itemCategoryCode, params.itemCode, params.kindCode, params.gradeId);

    }

    private WholesaleAndRetailApiParam getMonthAndYearApiParams(Long kindGradeId) {
        return new WholesaleAndRetailApiParam(getKindGradeById(kindGradeId));
    }

    private DayWholesaleAndRetailApiParam getDayApiParams(Long kindGradeId) {
        return new DayWholesaleAndRetailApiParam(getKindGradeById(kindGradeId));
    }

    private KindGrade getKindGradeById(Long kindGradeId) {
        return kindGradeRepository.findById(kindGradeId)
                .orElseThrow(() -> new NoSuchElementException("다시 시도해주세요"));
    }

    @Getter
    @Setter
    static class WholesaleAndRetailApiParam {

        int gradeRank;
        int kindCode;
        int itemCode;
        int itemCategoryCode;

        public WholesaleAndRetailApiParam(KindGrade kindGrade) {
            gradeRank = kindGrade.getGrade().getGradeRank();
            kindCode = kindGrade.getKind().getCode();
            itemCode = kindGrade.getKind().getItem().getItemCode();
            itemCategoryCode = kindGrade.getKind().getItem().getItemCategory().getItemCategoryCode();
        }
    }

    @Getter
    @Setter
    static class DayWholesaleAndRetailApiParam {

        int gradeId;
        int kindCode;
        int itemCode;
        int itemCategoryCode;

        public DayWholesaleAndRetailApiParam(KindGrade kindGrade) {
            gradeId = kindGrade.getGrade().getGradeId();
            kindCode = kindGrade.getKind().getCode();
            itemCode = kindGrade.getKind().getItem().getItemCode();
            itemCategoryCode = kindGrade.getKind().getItem().getItemCategory().getItemCategoryCode();
        }
    }

}
