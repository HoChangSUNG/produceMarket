package creative.market.web.controller;

import creative.market.util.FileStoreUtils;
import creative.market.util.FileSubPath;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/gradeCriteria")
public class GradeCriteriaController {
    @Value("${images}")
    private String rootPath;

    @GetMapping("/{imageName}")
    public Resource downloadCriteriaImg(@PathVariable String imageName) throws MalformedURLException { // 상품 등급 기준표 사진 보내기
        return new UrlResource("file:"+ FileStoreUtils.getFullPath(rootPath, FileSubPath.GRADE_CRITERIA_PATH +imageName));
    }

}
