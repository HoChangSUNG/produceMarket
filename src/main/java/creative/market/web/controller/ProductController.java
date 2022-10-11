package creative.market.web.controller;

import creative.market.aop.LoginCheck;
import creative.market.aop.UserType;
import creative.market.argumentresolver.Login;
import creative.market.service.ProductService;
import creative.market.service.dto.LoginUserDTO;
import creative.market.service.dto.RegisterProductDTO;
import creative.market.service.dto.UploadFileDTO;
import creative.market.util.FileStoreUtils;
import creative.market.util.FileSubPath;
import creative.market.web.dto.CreateProductFormReq;
import creative.market.web.dto.MessageRes;
import creative.market.web.dto.ResultRes;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    @Value("${images}")
    private String rootPath;
    private final ProductService productService;


//    @PostMapping
//    @LoginCheck(type = {UserType.SELLER})
//    public ResultRes createProduct(@Valid CreateProductFormReq productReq, @Login LoginUserDTO loginUserDTO) throws IOException {
//
//        // 사진 저장
//        UploadFileDTO sigImage = FileStoreUtils.storeFile(productReq.getSigImg(), rootPath, FileSubPath.PRODUCT_PATH);
//        List<UploadFileDTO> ordinalImages = FileStoreUtils.storeFiles(productReq.getImg(), rootPath, FileSubPath.PRODUCT_PATH);
//
//        RegisterProductDTO registerProductDTO = createRegisterProductDTO(productReq,loginUserDTO,sigImage,ordinalImages);
//
//        productService.register(registerProductDTO);
//        return new ResultRes(new MessageRes("상품 등록 성공"));
//    }


    // 셀러로 변경 가능하게 되면 -> createProductTest() 지우고 createProduct() 쓰기
    //                       -> productService.registertest() 지우기
    @PostMapping
    public ResultRes createProductTest(@Valid CreateProductFormReq productReq, @Login LoginUserDTO loginUserDTO) throws IOException {

        // 사진 저장
        UploadFileDTO sigImage = FileStoreUtils.storeFile(productReq.getSigImg(), rootPath, FileSubPath.PRODUCT_PATH);
        List<UploadFileDTO> ordinalImages = FileStoreUtils.storeFiles(productReq.getImg(), rootPath, FileSubPath.PRODUCT_PATH);
        LoginUserDTO test = new LoginUserDTO(1L, "sDFSDF");
        RegisterProductDTO registerProductDTO = createRegisterProductDTO(productReq,test,sigImage,ordinalImages);

        productService.registerTest(registerProductDTO);
        return new ResultRes(new MessageRes("상품 등록 성공"));
    }

    private RegisterProductDTO createRegisterProductDTO(CreateProductFormReq productReq,LoginUserDTO loginUserDTO, UploadFileDTO sigImage, List<UploadFileDTO> ordinalImages) {
        return new RegisterProductDTO(productReq.getKindGradeId(), productReq.getName(), productReq.getPrice(),
                productReq.getInfo(), loginUserDTO.getId(), sigImage, ordinalImages);
    }
}
