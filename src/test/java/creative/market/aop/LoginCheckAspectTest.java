package creative.market.aop;

import creative.market.exception.LoginAuthenticationException;
import creative.market.util.SessionUtils;
import creative.market.service.dto.LoginUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Import(LoginCheckTest.class)
@Slf4j
class LoginCheckAspectTest {

    @Autowired
    LoginCheckTest loginCheckTest;
    @Autowired
    HttpServletRequest request;
    @Autowired ApplicationContext ac;

    @Test
    @DisplayName("SELLER 로그인 인증 성공(SELLER로 로그인한 경우)")
    void sellerLoginCheck() throws Exception {
        //given
        createSession(UserType.SELLER);

        //then
        assertThat(loginCheckTest.sellerLoginCheck()).isEqualTo("ok");
    }

    @Test
    @DisplayName("SELLER 로그인 인증 실패(BUYER로 로그인한 경우)")
    void sellerLoginCheckFail1() throws Exception {
        //given
        loginCheckTest.logout();
        createSession(UserType.BUYER);

        //then
        assertThatThrownBy(() -> loginCheckTest.sellerLoginCheck())
                .isInstanceOf(LoginAuthenticationException.class)
                .hasMessage("로그인이 필요합니다.");
    }

    @Test
    @DisplayName("SELLER 로그인 인증 실패(로그인을 하지 않은 경우)")
    void sellerLoginCheckFail2() throws Exception {
        //then
        assertThatThrownBy(() -> loginCheckTest.sellerLoginCheck())
                .isInstanceOf(LoginAuthenticationException.class)
                .hasMessage("로그인이 필요합니다.");
    }

    @Test
    @DisplayName("SELLER 또는 ADMIN 로그인 인증 성공(SELLER로 로그인한 경우)")
    void sellerAndAdminLoginCheck1() throws Exception{
        //given
        createSession(UserType.SELLER);

        //then
        assertThat(loginCheckTest.sellerOrAdminLoginCheck()).isEqualTo("ok");

    }

    @Test
    @DisplayName("SELLER 또는 ADMIN 로그인 인증 성공(ADMIN로 로그인한 경우)")
    void sellerAndAdminLoginCheck2() throws Exception{
        //given
        createSession(UserType.ADMIN);

        //then
        assertThat(loginCheckTest.sellerOrAdminLoginCheck()).isEqualTo("ok");

    }

    @Test
    @DisplayName("SELLER 또는 ADMIN 로그인 인증 실패(로그인 안한 경우)")
    void sellerAndAdminLoginFail1() throws Exception{

        //then
        assertThatThrownBy(() -> loginCheckTest.sellerOrAdminLoginCheck())
                .isInstanceOf(LoginAuthenticationException.class)
                .hasMessage("로그인이 필요합니다.");
    }

    @Test
    @DisplayName("SELLER 또는 ADMIN 로그인 인증 실패(BUYER로 로그인한 경우)")
    void sellerAndAdminLoginFail2() throws Exception{
        //given
        createSession(UserType.BUYER);
        //then
        assertThatThrownBy(() -> loginCheckTest.sellerOrAdminLoginCheck())
                .isInstanceOf(LoginAuthenticationException.class)
                .hasMessage("로그인이 필요합니다.");
    }

    @Test
    @DisplayName("로그아웃 체크")
    void logout() throws Exception{
        //given
        createSession(UserType.BUYER);

        //when
        loginCheckTest.logout();

        //then
        assertThatThrownBy(() -> loginCheckTest.sellerLoginCheck())
                .isInstanceOf(LoginAuthenticationException.class)
                .hasMessage("로그인이 필요합니다.");
    }

    private void createSession(UserType userType) {
        LoginUserDTO loginUser = new LoginUserDTO(1L, "김현민");
        SessionUtils.createSession(request, userType.name(), loginUser);
    }

//    @Component
//    @Slf4j
//    static class LoginCheckTest {
//
//        @Autowired HttpServletRequest request;
//
//        @LoginCheck(type = UserType.SELLER)
//        public String sellerLoginCheck() {
//            return "ok";
//        }
//
//        @LoginCheck(type = {UserType.SELLER,UserType.ADMIN})
//        public String sellerOrAdminLoginCheck() {
//            return "ok";
//        }
//
//        public void logout(){
//            SessionUtils.expire(request);
//        }
//
//    }
}
