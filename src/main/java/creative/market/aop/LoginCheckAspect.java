package creative.market.aop;

import creative.market.exception.LoginAuthenticationException;
import creative.market.util.SessionUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
@Slf4j
public class LoginCheckAspect {

    @Before("@annotation(loginCheck)")
    public void doBefore(JoinPoint joinPoint, LoginCheck loginCheck) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        UserType[] userTypes = loginCheck.type();
        log.info("인증 권한 확인중, 인증 가능 유저 타입={}, 요청 메서드={}", userTypes, joinPoint.getSignature());

        for (UserType userType : userTypes) {
            if (SessionUtils.getSession(request, userType.name()) != null) { // 인증 사용자가 요청
                log.info("인증 사용자 요청, 사용자 유저 타입={}",userType);
                return;
            }
        }
        log.warn("미인증 사용자 요청");
        throw new LoginAuthenticationException("로그인이 필요합니다."); // 미인증 사용자가 요청
    }
}
