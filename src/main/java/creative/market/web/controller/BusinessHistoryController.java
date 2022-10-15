package creative.market.web.controller;

import creative.market.aop.LoginCheck;
import creative.market.aop.UserType;
import creative.market.argumentresolver.Login;
import creative.market.domain.business.BusinessHistory;
import creative.market.domain.business.BusinessImage;
import creative.market.domain.business.BusinessStatus;
import creative.market.service.BusinessHistoryService;
import creative.market.service.UserService;
import creative.market.service.dto.LoginUserDTO;
import creative.market.service.dto.UploadFileDTO;
import creative.market.service.query.BusinessHistoryQueryService;
import creative.market.util.FileStoreUtils;
import creative.market.util.FileSubPath;
import creative.market.web.dto.CreateBusinessFormReq;
import creative.market.web.dto.MessageRes;
import creative.market.web.dto.ResultRes;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/business")
public class BusinessHistoryController {

    @Value("${images}")
    private String rootPath;
    private final BusinessHistoryService businessHistoryService;
    private final BusinessHistoryQueryService businessHistoryQueryService;
    private final UserService userService;

    @PostMapping
    @LoginCheck(type = UserType.BUYER)
    public ResultRes createBusiness(@Valid CreateBusinessFormReq businessReq, @Login LoginUserDTO loginUserDTO) throws IOException {

        UploadFileDTO img = FileStoreUtils.storeFile(businessReq.getImg(), rootPath, FileSubPath.BUSINESS_PATH);

        BusinessHistory businessHistory = createBusinessHistory(businessReq, img, loginUserDTO);

        businessHistoryService.createBusiness(businessHistory);

        return new ResultRes(new MessageRes("사업자 등록 성공"));
    }

    @GetMapping
    @LoginCheck(type = UserType.ADMIN)
    public ResultRes getBusinessHistoryList() {

        return new ResultRes(businessHistoryQueryService.getBusinessHistoryList());
    }

    @PostMapping("/accept/{businessId}")
    @LoginCheck(type = UserType.ADMIN)
    public ResultRes acceptBusiness(@PathVariable Long businessId) {

        businessHistoryService.acceptBusiness(businessId);

        return new ResultRes(new MessageRes("사업자 승인 성공"));
    }

    @PostMapping("/reject/{businessId}")
    @LoginCheck(type = UserType.ADMIN)
    public ResultRes rejectBusiness(@PathVariable Long businessId) {

        businessHistoryService.rejectBusiness(businessId);

        return new ResultRes(new MessageRes("사업자 거절 성공"));
    }

    private BusinessHistory createBusinessHistory(CreateBusinessFormReq businessReq, UploadFileDTO img, LoginUserDTO loginUserDTO) {
        return BusinessHistory.builder()
                .businessName(businessReq.getBusinessName())
                .businessImage(createProductImage(img))
                .businessNumber(businessReq.getBusinessNumber())
                .status(BusinessStatus.WAIT)
                .user(userService.findById(loginUserDTO.getId()).orElse(null))
                .build();
    }

    private BusinessImage createProductImage(UploadFileDTO img) {
        return BusinessImage.builder().name(img.getUploadFileName())
                .path(FileSubPath.BUSINESS_PATH + img.getStoreFileName())
                .build();
    }
}
