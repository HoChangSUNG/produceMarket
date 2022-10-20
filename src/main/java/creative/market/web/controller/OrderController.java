package creative.market.web.controller;

import creative.market.aop.LoginCheck;
import creative.market.aop.UserType;
import creative.market.argumentresolver.Login;
import creative.market.domain.Address;
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
    public ResultRes orderCancel(@PathVariable Long orderProductId,@Login LoginUserDTO loginUserDTO) {

        orderService.orderCancel(orderProductId,loginUserDTO.getId());

        return new ResultRes(new MessageRes("주문 취소 성공"));
    }
    private Address createAddress(String jibun, String road, int zipcode, String detailAddress) {
        return Address.builder()
                .jibun(jibun)
                .road(road)
                .zipcode(zipcode)
                .detailAddress(detailAddress).build();
    }
}
