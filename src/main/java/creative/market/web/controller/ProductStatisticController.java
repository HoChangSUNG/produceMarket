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

}
