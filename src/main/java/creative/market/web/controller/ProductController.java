package creative.market.web.controller;

import creative.market.argumentresolver.Login;
import creative.market.service.ProductService;
import creative.market.service.dto.RegisterProductDTO;
import creative.market.service.dto.UploadFileDTO;
import creative.market.util.FileStoreUtils;
import creative.market.util.FileSubPath;
import creative.market.web.dto.CreateProductFormReq;
import creative.market.service.dto.LoginUserDTO;
import creative.market.web.dto.MessageRes;
import creative.market.web.dto.ResultRes;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product")
public class ProductController {

    @Value("${images}")
    private String rootPath;
    private final ProductService productService;


    @PostMapping
    //    @LoginCheck(type = {UserType.SELLER})
    public ResultRes createProduct(@Login LoginUserDTO loginUserDTO, CreateProductFormReq productReq) throws IOException {

        // 사진 저장
        UploadFileDTO sigImage = FileStoreUtils.storeFile(productReq.getSigImg(), rootPath, FileSubPath.PRODUCT_PATH);
        List<UploadFileDTO> ordinalImages = FileStoreUtils.storeFiles(productReq.getImg(), rootPath, FileSubPath.PRODUCT_PATH);

        RegisterProductDTO regProductDTO = new RegisterProductDTO(productReq.getKindGradeId(), productReq.getName(), productReq.getPrice(),
                productReq.getInfo(), loginUserDTO.getId(), sigImage, ordinalImages);

        productService.register(regProductDTO);
        return new ResultRes(new MessageRes("상품 등록 성공"));
    }
}
