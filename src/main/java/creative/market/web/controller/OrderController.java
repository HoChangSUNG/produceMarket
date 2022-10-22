package creative.market.web.controller;

import creative.market.aop.LoginCheck;
import creative.market.aop.UserType;
import creative.market.argumentresolver.Login;
import creative.market.domain.Address;
import creative.market.domain.user.Buyer;
import creative.market.domain.user.Seller;
import creative.market.repository.user.BuyerRepository;
import creative.market.repository.user.SellerRepository;
import creative.market.service.OrderService;
import creative.market.service.dto.LoginUserDTO;
import creative.market.web.dto.MessageRes;
import creative.market.web.dto.OrderReq;
import creative.market.web.dto.ResultRes;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;
    private final SellerRepository sellerRepository;
    private final BuyerRepository buyerRepository;

    @PostMapping
    @LoginCheck(type = {UserType.BUYER, UserType.SELLER})
    public ResultRes orderProducts(@RequestBody @Valid OrderReq orderReq, @Login LoginUserDTO loginUserDTO) { // 상품 주문

        if (CollectionUtils.isEmpty(orderReq.getOrderProducts())) {
            throw new NoSuchElementException("주문할 상품의 개수와 상품을 추가해주세요");
        }

        Address address = createAddress(orderReq.getJibun(), orderReq.getRoad(), orderReq.getZipcode(), orderReq.getDetailAddress());
        orderService.order(loginUserDTO.getId(), orderReq.getOrderProducts(), address);

        return new ResultRes(new MessageRes("상품 주문 성공"));
    }

    @DeleteMapping("/{orderProductId}")
    @LoginCheck(type = {UserType.BUYER, UserType.SELLER})
    public ResultRes orderCancel(@PathVariable Long orderProductId, @Login LoginUserDTO loginUserDTO) {

        orderService.orderCancel(orderProductId, loginUserDTO.getId());

        return new ResultRes(new MessageRes("주문 취소 성공"));
    }

    @GetMapping("/address")
    @LoginCheck(type = {UserType.BUYER, UserType.SELLER})
    public ResultRes getDefaultAddress(@Login LoginUserDTO loginUserDTO) {
        Address address;
        if (loginUserDTO.getUserType() == UserType.BUYER) {
            Buyer buyer = buyerRepository.findById(loginUserDTO.getId()).orElseThrow(() -> new NoSuchElementException("존재하지 않는 구매자입니다."));
            address = buyer.getAddress();
        } else {
            Seller seller = sellerRepository.findById(loginUserDTO.getId()).orElseThrow(() -> new NoSuchElementException("존재하지 않는 판매자입니다."));
            address = seller.getAddress();
        }
        return new ResultRes(address);
    }

    private Address createAddress(String jibun, String road, int zipcode, String detailAddress) {
        return Address.builder()
                .jibun(jibun)
                .road(road)
                .zipcode(zipcode)
                .detailAddress(detailAddress).build();
    }

}
