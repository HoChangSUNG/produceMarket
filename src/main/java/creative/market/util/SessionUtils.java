package creative.market.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SessionUtils {

    public static void createSession(HttpServletRequest request, String attributeName, Object attribute) { // 세션 생성
        request.getSession().setAttribute(attributeName,attribute);
    }

    public static Object getSession(HttpServletRequest request, String attributeName) { // 세션 조회
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(attributeName) == null) {
            return null;
        }
        return session.getAttribute(attributeName);
    }

    public static void expire(HttpServletRequest request) { // 세션 제거
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
