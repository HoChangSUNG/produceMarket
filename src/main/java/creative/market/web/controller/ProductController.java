package creative.market.web.controller;

import creative.market.aop.LoginCheck;
import creative.market.aop.UserType;
import creative.market.argumentresolver.Login;
import creative.market.exception.FileSaveException;
import creative.market.repository.dto.ProductSearchConditionReq;
import creative.market.service.ProductService;
import creative.market.service.dto.LoginUserDTO;
import creative.market.service.dto.RegisterProductDTO;
import creative.market.service.dto.UploadFileDTO;
import creative.market.service.query.ProductQueryService;
import creative.market.util.FileStoreUtils;
import creative.market.util.FileSubPath;
import creative.market.web.dto.CreateProductFormReq;
import creative.market.web.dto.MessageRes;
import creative.market.web.dto.ResultRes;
import creative.market.service.dto.UpdateProductFormReq;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

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
    private final ProductQueryService productQueryService;

    @GetMapping
    public ResultRes getProductList(ProductSearchConditionReq searchCondition) {// 상품 리스트 조회

        return new ResultRes(productQueryService.productShortInfoList(searchCondition));
    }

    @GetMapping("/{productId}")
    public ResultRes GetProductDetail(@PathVariable Long productId) { // 상품 상세 조회
        return new ResultRes(productQueryService.productDetailInfo(productId));
    }

    @PostMapping
    @LoginCheck(type = {UserType.SELLER})
    public ResultRes createProduct(@Valid CreateProductFormReq productReq, @Login LoginUserDTO loginUserDTO) { // 상품 생성

        try {
            // 사진 저장
            UploadFileDTO sigImage = FileStoreUtils.storeFile(productReq.getSigImg(), rootPath, FileSubPath.PRODUCT_PATH);
            List<UploadFileDTO> ordinalImages = FileStoreUtils.storeFiles(productReq.getImg(), rootPath, FileSubPath.PRODUCT_PATH);

            RegisterProductDTO registerProductDTO = createRegisterProductDTO(productReq, loginUserDTO, sigImage, ordinalImages);

            productService.register(registerProductDTO);
            return new ResultRes(new MessageRes("상품 등록 성공"));
        } catch (IOException e) {
            throw new FileSaveException("파일 저장에 실패했습니다. 다시 시도해주세요");
        }

    }

    @GetMapping("/update/{productId}")
    @LoginCheck(type = {UserType.SELLER})
    public ResultRes getUpdateForm(@PathVariable Long productId, @Login LoginUserDTO loginUserDTO) { // 상품 수정 전 기존 정보 전달
        productService.sellerAccessCheck(productId, loginUserDTO.getId());
        return new ResultRes(productQueryService.productUpdateForm(productId));
    }

    @PostMapping("/update/{productId}")
    @LoginCheck(type = {UserType.SELLER})
    public ResultRes updateProduct(@Valid UpdateProductFormReq updateFormReq, @Login LoginUserDTO loginUserDTO, @PathVariable Long productId) { // 상품 수정
        productService.update(productId, updateFormReq, loginUserDTO.getId());
        return new ResultRes(new MessageRes("상품 수정 성공"));
    }

    private RegisterProductDTO createRegisterProductDTO(CreateProductFormReq productReq, LoginUserDTO loginUserDTO, UploadFileDTO sigImage, List<UploadFileDTO> ordinalImages) {
        return new RegisterProductDTO(productReq.getKindGradeId(), productReq.getName(), productReq.getPrice(),
                productReq.getInfo(), loginUserDTO.getId(), sigImage, ordinalImages);
    }
}
