package creative.market.web.controller;

import creative.market.aop.LoginCheck;
import creative.market.aop.UserType;
import creative.market.argumentresolver.Login;
import creative.market.service.UserService;
import creative.market.service.dto.LoginRes;
import creative.market.service.dto.LoginUserDTO;
import creative.market.service.dto.UserInfoRes;
import creative.market.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResultRes register(@RequestBody @Valid RegisterReq registerReq) {

        userService.register(registerReq);

        return new ResultRes(new MessageRes("회원가입 성공"));
    }

    @PostMapping("/login")
    public ResultRes login(@RequestBody @Valid LoginReq loginReq, HttpServletRequest request) {
        LoginRes loginRes = userService.login(loginReq.getLoginId(), loginReq.getPassword(), request);

        return new ResultRes(loginRes);
    }

    @GetMapping("/logout")
    public ResultRes logout(HttpServletRequest request) {
        userService.logout(request);

        return new ResultRes(new MessageRes("로그아웃 성공"));
    }

    @GetMapping
    @LoginCheck(type = {UserType.SELLER,UserType.BUYER})
    public ResultRes getInfo(@Login LoginUserDTO loginUserDTO) {
        UserInfoRes userInfoRes = userService.getInfo(loginUserDTO);

        return new ResultRes(userInfoRes);
    }
}
