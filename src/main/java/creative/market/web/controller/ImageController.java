package creative.market.web.controller;

import creative.market.util.FileStoreUtils;
import creative.market.util.FileSubPath;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;

@RestController
@RequestMapping("/img")
public class ImageController { // 이미지 전송

    @Value("${images}")
    private String rootPath;

    @GetMapping("/product/{imageName}")
    public Resource downloadProductImg(@PathVariable String imageName) throws MalformedURLException { // 상품 사진 보내기
        return new UrlResource("file:"+ FileStoreUtils.getFullPath(rootPath, FileSubPath.PRODUCT_PATH +imageName));
    }

}