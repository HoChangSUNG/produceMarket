package creative.market.web.controller;

import creative.market.aop.LoginCheck;
import creative.market.aop.UserType;
import creative.market.argumentresolver.Login;
import creative.market.domain.Address;
import creative.market.domain.user.Admin;
import creative.market.domain.user.Buyer;
import creative.market.domain.user.Seller;
import creative.market.domain.user.User;
import creative.market.service.UserService;
import creative.market.service.dto.LoginRes;
import creative.market.service.dto.LoginUserDTO;
import creative.market.service.dto.UserInfoRes;
import creative.market.util.SessionUtils;
import creative.market.web.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResultRes register(@RequestBody @Valid CreateAndChangeUserReq registerReq) {

        Buyer buyer = createBuyer(registerReq);

        userService.register(buyer);

        return new ResultRes(new MessageRes("회원가입 성공"));
    }

    @PostMapping("/login")
    public ResultRes login(@RequestBody @Valid LoginReq loginReq, HttpServletRequest request) {
        User user = userService.login(loginReq.getLoginId(), loginReq.getPassword());
        UserType userType = null;

        if(user instanceof Buyer) {
            userType = UserType.BUYER;
        } else if(user instanceof Seller) {
            userType = UserType.SELLER;
        } else {
            userType = UserType.ADMIN;
        }

        createSession(userType, request, user.getId(), user.getName());

        return new ResultRes(new LoginRes(user.getId(), user.getName(), userType.name()));
    }

    @GetMapping("/logout")
    public ResultRes logout(HttpServletRequest request) {
        SessionUtils.expire(request);

        return new ResultRes(new MessageRes("로그아웃 성공"));
    }

    @LoginCheck(type = {UserType.ADMIN,UserType.BUYER,UserType.SELLER})
    @GetMapping("/login-check")
    public ResultRes loginCheck() {

        return new ResultRes(new MessageRes("로그인 상태"));
    }

    @GetMapping
    @LoginCheck(type = {UserType.SELLER,UserType.BUYER})
    public ResultRes getInfo(@Login LoginUserDTO loginUserDTO) {

        UserInfoRes userInfoRes = null;

        if(loginUserDTO.getUserType().equals(UserType.BUYER)) {
            Buyer buyer = userService.getBuyer(loginUserDTO.getId());
            userInfoRes = createUserInfoRes(buyer.getName(), buyer.getLoginId(), buyer.getPassword(), buyer.getBirth(), buyer.getEmail(), buyer.getPhoneNumber(), buyer.getAddress().getJibun(), buyer.getAddress().getRoad(), buyer.getAddress().getZipcode(), buyer.getAddress().getDetailAddress());

        } else if(loginUserDTO.getUserType().equals(UserType.SELLER)) {
            Seller seller = userService.getSeller(loginUserDTO.getId());
            userInfoRes = createUserInfoRes(seller.getName(), seller.getLoginId(), seller.getPassword(), seller.getBirth(), seller.getEmail(), seller.getPhoneNumber(), seller.getAddress().getJibun(), seller.getAddress().getRoad(), seller.getAddress().getZipcode(), seller.getAddress().getDetailAddress());
        }

        return new ResultRes(userInfoRes);
    }

    @PatchMapping
    @LoginCheck(type = {UserType.SELLER,UserType.BUYER})
    public ResultRes changeInfo(@RequestBody @Valid CreateAndChangeUserReq changeReq, @Login LoginUserDTO loginUserDTO) {
        userService.update(changeReq, loginUserDTO.getId(), loginUserDTO.getUserType());

        return new ResultRes(new MessageRes("회원정보 수정 성공"));
    }

    @DeleteMapping
    @LoginCheck(type = {UserType.BUYER,UserType.SELLER})
    public ResultRes delete(@RequestBody PasswordReq password, @Login LoginUserDTO loginUserDTO,HttpServletRequest request) {

        userService.delete(loginUserDTO.getId(), password.getPassword(), loginUserDTO.getUserType());
        SessionUtils.expire(request);
        return new ResultRes(new MessageRes("회원탈퇴 성공"));
    }

    private Buyer createBuyer(CreateAndChangeUserReq req) {
        Buyer buyer = Buyer.builder()
                .name(req.getName())
                .address(Address.builder()
                        .jibun(req.getJibun())
                        .road(req.getRoad())
                        .zipcode(req.getZipcode())
                        .detailAddress(req.getDetailAddress()).build())
                .birth(req.getBirth())
                .loginId(req.getLoginId())
                .email(req.getEmail())
                .password(req.getPassword())
                .phoneNumber(req.getPhoneNumber())
                .build();
        return buyer;
    }

    private void createSession(UserType userType, HttpServletRequest request, Long id, String name) {
        LoginUserDTO loginUser = new LoginUserDTO(id, name,userType);
        SessionUtils.createSession(request, userType.name(), loginUser);
    }

    private UserInfoRes createUserInfoRes(String name, String loginId, String password, String birth, String email, String phoneNumber, String jibun, String road, int zipcode, String detailAddress) {
        return new UserInfoRes(name, loginId, password, birth, email, phoneNumber, jibun, road, zipcode, detailAddress);
    }
}
