package creative.market.util;

import creative.market.aop.UserType;
import creative.market.service.dto.LoginUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import static org.assertj.core.api.Assertions.*;

@Slf4j
class SessionUtilTest {

    MockHttpServletRequest request;

    @BeforeEach
    public void createSession() {
        request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
    }

    @Test
    @DisplayName("세션 생성 테스트")
    void sessionCreateTest() throws Exception {
        //given
        LoginUserDTO loginUser = new LoginUserDTO(1L, "성호창");

        //when
        SessionUtils.createSession(request, UserType.BUYER.name(), loginUser);

        //then
        LoginUserDTO result = (LoginUserDTO) request.getSession().getAttribute(UserType.BUYER.name());
        assertThat(result.getId()).isEqualTo(loginUser.getId());
        assertThat(result.getName()).isEqualTo(loginUser.getName());

    }

    @Test
    @DisplayName("세션 조회시 값이 있는 경우")
    void sessionGetTest() throws Exception {
        //given
        LoginUserDTO loginUser = new LoginUserDTO(1L, "성호창");

        //when
        SessionUtils.createSession(request, UserType.BUYER.name(), loginUser);
        LoginUserDTO result = (LoginUserDTO) SessionUtils.getSession(request, UserType.BUYER.name());

        //then
        assertThat(result.getId()).isEqualTo(loginUser.getId());
        assertThat(result.getName()).isEqualTo(loginUser.getName());

    }

    @Test
    @DisplayName("세션 조회시 값이 없는 경우1")
    void sessionGetFailTest1() throws Exception {
        //given
        request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);

        //when
        Object result = SessionUtils.getSession(request, UserType.BUYER.name());

        //then
        assertThat(result).isNull();

    }

    @Test
    @DisplayName("세션 조회시 값이 없는 경우2")
    void sessionGetFailTest2() throws Exception {
        //given
        LoginUserDTO loginUser = new LoginUserDTO(1L, "성호창");

        //when
        SessionUtils.createSession(request,UserType.BUYER.name(), loginUser);
        LoginUserDTO result = (LoginUserDTO) SessionUtils.getSession(request, UserType.SELLER.name());


        //then
        assertThat(result).isNull();

    }

    @Test
    @DisplayName("세션 만료")
    void sessionInvalidate() throws Exception {
        //given
        LoginUserDTO loginUser = new LoginUserDTO(1L, "성호창");

        //when
        SessionUtils.createSession(request, UserType.BUYER.name(), loginUser);
        LoginUserDTO beforeExpireResult = (LoginUserDTO) SessionUtils.getSession(request, UserType.BUYER.name());

        //then
        // 세션 제거 전
        assertThat(beforeExpireResult.getId()).isEqualTo(loginUser.getId());
        assertThat(beforeExpireResult.getName()).isEqualTo(loginUser.getName());

        // 세션 제거 후
        SessionUtils.expire(request);
        assertThat(SessionUtils.getSession(request,UserType.BUYER.name())).isNull();
    }

}