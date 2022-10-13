package creative.market.aop;

import creative.market.util.SessionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
@Service
@Slf4j
public class LoginCheckTest {
    private final HttpServletRequest request;

    @LoginCheck(type = UserType.SELLER)
    public String sellerLoginCheck() {
        log.info("판매자 권한 성공");
        return "ok";
    }

    @LoginCheck(type = {UserType.SELLER,UserType.ADMIN})
    public String sellerOrAdminLoginCheck() {
        log.info("판매자,관리자 권한 성공");

        return "ok";
    }

    public void logout(){
        SessionUtils.expire(request);
    }
}
