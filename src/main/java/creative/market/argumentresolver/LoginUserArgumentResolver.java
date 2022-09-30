package creative.market.argumentresolver;

import creative.market.aop.UserType;
import creative.market.util.SessionUtils;
import creative.market.web.LoginUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        log.info("LoginUserArgumentResolver supportParameter 실행");

        boolean hasLoginAnnotation = parameter.hasParameterAnnotation(Login.class);
        boolean hasUserType = LoginUserDTO.class.isAssignableFrom(parameter.getParameterType());

        return hasLoginAnnotation && hasUserType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        log.info("LoginUserArgumentResolver resolveArgument 실행");

        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        for (UserType userType : UserType.values()) { // 세션 값이 있는지 확인
            LoginUserDTO loginUserDTO = (LoginUserDTO)SessionUtils.getSession(request, userType.name());
            if (loginUserDTO != null) {
                return loginUserDTO;
            }
        }
        return null;
    }
}
