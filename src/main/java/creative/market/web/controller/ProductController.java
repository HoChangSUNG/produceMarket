package creative.market.web.controller;

import creative.market.argumentresolver.Login;
import creative.market.service.ProductService;
import creative.market.service.dto.RegisterProductDTO;
import creative.market.service.dto.UploadFileDTO;
import creative.market.util.FileStoreUtils;
import creative.market.util.FileSubPath;
import creative.market.web.dto.CreateProductFormReq;
import creative.market.web.dto.LoginUserDTO;
import creative.market.web.dto.ResultRes;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
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

        //수정합시다...  loginUserDTO.getId() <- null... 현민이께 완성 되야 가능
        RegisterProductDTO registerProductDTO = createRegisterProductDTO(productReq,loginUserDTO,sigImage,ordinalImages);

        productService.register(registerProductDTO);
        return new ResultRes(new MessageRes("상품 등록 성공"));
    }

    private RegisterProductDTO createRegisterProductDTO(CreateProductFormReq productReq,LoginUserDTO loginUserDTO, UploadFileDTO sigImage, List<UploadFileDTO> ordinalImages) {
        return new RegisterProductDTO(productReq.getKindGradeId(), productReq.getName(), productReq.getPrice(),
                productReq.getInfo(), loginUserDTO.getId(), sigImage, ordinalImages);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    static class MessageRes {
        private String message;
    }


}
