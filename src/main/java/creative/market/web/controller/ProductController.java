package creative.market.web.controller;

import creative.market.aop.LoginCheck;
import creative.market.aop.UserType;
import creative.market.argumentresolver.Login;
import creative.market.domain.product.Product;
import creative.market.exception.FileSaveException;
import creative.market.repository.ProductRepository;
import creative.market.repository.dto.CategoryParamDTO;
import creative.market.repository.dto.ProductSearchConditionReq;
import creative.market.repository.dto.SellerAndTotalPricePerCategoryDTO;
import creative.market.repository.order.OrderProductRepository;
import creative.market.repository.query.OrderProductQueryRepository;
import creative.market.repository.query.ProductQueryRepository;
import creative.market.service.ProductService;
import creative.market.service.dto.LoginUserDTO;
import creative.market.service.dto.RegisterProductDTO;
import creative.market.service.dto.UploadFileDTO;
import creative.market.service.query.ProductQueryService;
import creative.market.util.FileStoreUtils;
import creative.market.util.FileSubPath;
import creative.market.util.PagingUtils;
import creative.market.web.dto.*;
import creative.market.service.dto.UpdateProductFormReq;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.*;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@Slf4j
public class ProductController {

    @Value("${images}")
    private String rootPath;
    private final ProductRepository productRepository;
    private final ProductQueryRepository productQueryRepository;
    private final OrderProductQueryRepository orderProductQueryRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductService productService;
    private final ProductQueryService productQueryService;

    private final static int MAIN_PAGE_LIMIT = 4;

    @GetMapping
    public PagingResultRes getProductList(ProductSearchConditionReq searchCondition,
                                          @RequestParam(defaultValue = "10") Integer pageSize,
                                          @RequestParam(defaultValue = "1") Integer pageNum) {// ?????? ????????? ??????
        log.info("pageNum={},limit={}", pageNum, pageSize);

        Long total = productRepository.findProductByConditionTotalCount(searchCondition);
        int offset = PagingUtils.getOffset(pageNum, pageSize);
        int totalPageNum = PagingUtils.getTotalPageNum(total, pageSize);

        return new PagingResultRes(productQueryService.productShortInfoList(searchCondition, offset, pageSize), pageNum, totalPageNum);
    }

    @GetMapping("/{productId}")
    public ResultRes getProductDetail(@PathVariable Long productId) { // ?????? ?????? ??????
        return new ResultRes(productQueryService.productDetailInfo(productId));
    }

    @DeleteMapping("/{productId}")
    @LoginCheck(type = {UserType.SELLER})
    public ResultRes deleteProduct(@PathVariable Long productId, @Login LoginUserDTO loginUserDTO) {
        productService.deleteProduct(productId, loginUserDTO.getId());
        return new ResultRes(new MessageRes("?????? ?????? ??????"));
    }

    @PostMapping
    @LoginCheck(type = {UserType.SELLER})
    public ResultRes registerProduct(@Valid CreateProductReq productReq, @Login LoginUserDTO loginUserDTO) { // ?????? ??????

        fileEmptyCheck(productReq.getImg()); // ????????? ????????? ????????? ?????? ??????

        try {
            // ?????? ??????
            UploadFileDTO sigImage = FileStoreUtils.storeFile(productReq.getSigImg(), rootPath, FileSubPath.PRODUCT_PATH);
            List<UploadFileDTO> ordinalImages = FileStoreUtils.storeFiles(productReq.getImg(), rootPath, FileSubPath.PRODUCT_PATH);

            RegisterProductDTO registerProductDTO = createRegisterProductDTO(productReq, loginUserDTO, sigImage, ordinalImages);

            productService.register(registerProductDTO);
            return new ResultRes(new MessageRes("?????? ?????? ??????"));
        } catch (IOException e) {
            log.error("file save error={}", e);
            throw new FileSaveException("?????? ????????? ??????????????????. ?????? ??????????????????");
        }

    }

    @GetMapping("/update/{productId}")
    @LoginCheck(type = {UserType.SELLER})
    public ResultRes getUpdateForm(@PathVariable Long productId, @Login LoginUserDTO loginUserDTO) { // ?????? ?????? ??? ?????? ?????? ??????
        productService.sellerAccessCheck(productId, loginUserDTO.getId()); // ?????? ?????? ?????? ??????
        return new ResultRes(productQueryService.productUpdateForm(productId));
    }

    @PostMapping("/update/{productId}")
    @LoginCheck(type = {UserType.SELLER})
    public ResultRes updateProduct(@Valid UpdateProductFormReq updateFormReq, @Login LoginUserDTO loginUserDTO, @PathVariable Long productId) { // ?????? ??????

        fileEmptyCheck(updateFormReq.getImg()); // ????????? ????????? ????????? ?????? ??????

        productService.update(productId, updateFormReq, loginUserDTO.getId());
        return new ResultRes(new MessageRes("?????? ?????? ??????"));
    }

    @GetMapping("/statistics/{productId}")
    public ResultRes totalPricePercentByCategory(@PathVariable Long productId) {

        Product product = productRepository.findById(productId).orElseThrow(() -> new NoSuchElementException("????????? ???????????? ????????????."));
        CategoryParamDTO categoryParamDTO = new CategoryParamDTO(null, null, null, product.getKindGrade().getId());

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0); // ?????? ??????????????? ?????? ???

        int rankCount = 5; // ?????? ??????????????? ????????? top rank ??????

        //????????? ???????????? ??? ?????????
        Long totalPriceSum = orderProductRepository.findCategoryOrderTotalPriceSum(categoryParamDTO, startDate, endDate);

        // ????????? ?????????????????? ?????? ????????? top 5 ??????
        List<SellerAndTotalPricePerCategoryDTO> topRankPricePercent = orderProductQueryRepository.findCategoryTopRankSellerNameAndPrice(categoryParamDTO, startDate, endDate, rankCount);
        List<PercentAndPriceRes> priceTopRankPercentRes = convertToPricePercentDTOS(topRankPricePercent, totalPriceSum, productId);

        //????????? ?????????????????? ?????? ????????? ????????? ??????
        SellerAndTotalPricePerCategoryDTO SellerPricePercent = orderProductQueryRepository.findCategorySellerNameAndPrice(categoryParamDTO, startDate, endDate, product.getUser().getId());
        PercentAndPriceRes percentAndPriceRes = convertToPricePercentDTO(SellerPricePercent, totalPriceSum, productId);

        return new ResultRes(new PricePercentPieGraphPerCategoryRes(priceTopRankPercentRes, percentAndPriceRes));
    }

    @GetMapping("/main-page/latest")
    public ResultRes mainPageLatestByAllCategory() { // ?????? ????????? ????????? ??????

        int offset = 0;
        return new ResultRes(productQueryRepository.findProductMainPageByLatestCreatedDate(offset, MAIN_PAGE_LIMIT));
    }

    @GetMapping("/main-page/order-count")
    public ResultRes mainPageOrderCntByAllCategory() { // ?????? ????????? ?????? ????????? ??????

        int offset = 0;
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = minusMonths(endDate, 1); // 1??????

        log.info("startDate={}, endDate={}", startDate, endDate);
        return new ResultRes(productQueryService.productMainPageByOrderCount(offset, MAIN_PAGE_LIMIT, startDate, endDate));
    }

    @GetMapping("/main-page/review-rate-avg")
    public ResultRes mainPageReviewRateAvgByAllCategory() { // ?????? ????????? ?????? ?????? ??? ??????

        int offset = 0;
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = minusMonths(endDate, 1); // 1??????

        log.info("startDate={}, endDate={}", startDate, endDate);
        return new ResultRes(productQueryService.productMainPageByReviewAvgRate(offset, MAIN_PAGE_LIMIT, startDate, endDate));
    }

    private LocalDateTime minusMonths(LocalDateTime standardLocalDateTime, int months) { // n?????? LocalDateTime
        return standardLocalDateTime.minusMonths(months)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }

    private List<PercentAndPriceRes> convertToPricePercentDTOS(List<SellerAndTotalPricePerCategoryDTO> params, Long totalPriceSum, Long productId) {
        Long totalPriceSumRemain = totalPriceSum;
        List<PercentAndPriceRes> result = new ArrayList<>();

        for (int i = 0; i < params.size(); i++) {
            SellerAndTotalPricePerCategoryDTO param = params.get(i);
            result.add(convertToPricePercentDTO(param, totalPriceSum, productId));
            totalPriceSumRemain -= param.getTotalPrice();
        }

        if (totalPriceSumRemain != 0) { // ?????? ??????????????? ?????? ???????????? ????????? ?????? ?????? ??? < ?????? ???????????? ?????? ??? -> ?????? ???????????? '??????'??? ??????????????? ?????? ??????
            PercentAndPriceRes etc = new PercentAndPriceRes("??????", totalPriceSumRemain, getPercent(totalPriceSumRemain, totalPriceSum));
            result.add(etc);
        }

        return result;
    }

    private PercentAndPriceRes convertToPricePercentDTO(SellerAndTotalPricePerCategoryDTO param, Long totalPriceSum, Long productId) {
        String sellerName = param.getSellerName();
        Long totalPrice = param.getTotalPrice();

        if (sellerName == null) {
            Product product = productRepository.findByIdFetchJoinSeller(productId).orElseThrow(() -> new NoSuchElementException("???????????? ?????? ???????????????."));
            sellerName = product.getUser().getName();
        }
        return new PercentAndPriceRes(sellerName, totalPrice, getPercent(totalPrice, totalPriceSum));
    }

    private float getPercent(long totalPrice, long totalPriceSum) {
        return totalPriceSum == 0 ? 0 : ((float) totalPrice / totalPriceSum) * 100;
    }

    private void fileEmptyCheck(List<MultipartFile> multipartFiles) {
        if (CollectionUtils.isEmpty(multipartFiles)) {
            throw new FileSaveException("????????? ????????? 1??? ?????? ??????????????? ?????????.");
        }
    }

    private RegisterProductDTO createRegisterProductDTO(CreateProductReq productReq, LoginUserDTO loginUserDTO, UploadFileDTO sigImage, List<UploadFileDTO> ordinalImages) {
        return new RegisterProductDTO(productReq.getKindGradeId(), productReq.getName(), productReq.getPrice(),
                productReq.getInfo(), loginUserDTO.getId(), sigImage, ordinalImages);
    }
}
