package creative.market.web.controller;

import creative.market.aop.LoginCheck;
import creative.market.aop.UserType;
import creative.market.argumentresolver.Login;
import creative.market.service.CartService;
import creative.market.service.dto.LoginUserDTO;
import creative.market.service.query.CartQueryService;
import creative.market.web.dto.CreateCartReq;
import creative.market.web.dto.MessageRes;
import creative.market.web.dto.ResultRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
@Slf4j
public class CartController {

    private final CartService cartService;
    private final CartQueryService cartQueryService;

    @PostMapping
    @LoginCheck(type = {UserType.BUYER, UserType.SELLER})
    public ResultRes registerCart(@RequestBody @Valid CreateCartReq createCartReq, @Login LoginUserDTO loginUserDTO) {

        Long cartId = cartService.register(createCartReq.getProductId(), createCartReq.getCount(), loginUserDTO.getId());
        log.info("장바구니 등록 성공 id={}", cartId);
        return new ResultRes(new MessageRes("장바구니 등록 성공"));
    }

    @GetMapping
    @LoginCheck(type = {UserType.BUYER, UserType.SELLER})
    public ResultRes getCartList(@Login LoginUserDTO loginUserDTO) {

        return new ResultRes(cartQueryService.getCartList(loginUserDTO.getId()));
    }

    @DeleteMapping("/{cartId}")
    @LoginCheck(type = {UserType.BUYER, UserType.SELLER})
    public ResultRes deleteCart(@PathVariable Long cartId, @Login LoginUserDTO loginUserDTO) {

        cartService.delete(cartId,loginUserDTO.getId());
        return new ResultRes(new MessageRes("장바구니 삭제 성공"));
    }
}
