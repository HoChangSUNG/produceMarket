package creative.market.service;

import creative.market.aop.UserType;
import creative.market.domain.Address;
import creative.market.domain.user.Admin;
import creative.market.domain.user.Buyer;
import creative.market.domain.user.Seller;
import creative.market.domain.user.User;
import creative.market.exception.DuplicateException;
import creative.market.exception.LoginAuthenticationException;
import creative.market.repository.user.AdminRepository;
import creative.market.repository.user.BuyerRepository;
import creative.market.repository.user.SellerRepository;
import creative.market.repository.user.UserRepository;
import creative.market.service.dto.LoginRes;
import creative.market.service.dto.LoginUserDTO;
import creative.market.util.SessionUtils;
import creative.market.web.dto.RegisterReq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final BuyerRepository buyerRepository;
    private final SellerRepository sellerRepository;
    private final AdminRepository adminRepository;

    @Transactional
    public Long register(RegisterReq registerReq) {

        Buyer buyer = createBuyer(registerReq);

        buyerRepository.findByLoginId(buyer.getLoginId())
                .orElseThrow(() -> new DuplicateException("회원가입 실패(아이디 중복)"));

        return buyerRepository.register(buyer);
    }

    public LoginRes login(String loginId, String password, HttpServletRequest request) {
        Optional<Buyer> buyer = buyerRepository.findByLoginIdAndPassword(loginId, password);
        Optional<Seller> seller = sellerRepository.findByLoginIdAndPassword(loginId, password);
        Optional<Admin> admin = adminRepository.findByLoginIdAndPassword(loginId, password);

        User user = null;
        UserType userType = null;

        if(buyer.isPresent()) {
            user = buyer.get();
            userType = UserType.BUYER;
        } else if(seller.isPresent()) {
            user = seller.get();
            userType = UserType.SELLER;
        } else if(admin.isPresent()) {
            user = admin.get();
            userType = UserType.ADMIN;
        }

        if (user == null)
            throw new LoginAuthenticationException("로그인 실패");

        createSession(userType, request, user.getId(), user.getName());
        return new LoginRes(user.getId(), user.getName(), userType.name());
    }

    public void logout(HttpServletRequest request) {
        SessionUtils.expire(request);
    }

    @Transactional
    public void delete(Long id) {
        userRepository.delete(id);
    }

    private void createSession(UserType userType, HttpServletRequest request, Long id, String name) {
        LoginUserDTO loginUser = new LoginUserDTO(id, name);
        SessionUtils.createSession(request, userType.name(), loginUser);
    }

    private Buyer createBuyer(RegisterReq registerReq) {
        Buyer buyer = Buyer.builder()
                .name(registerReq.getName())
                .address(Address.builder()
                        .jibun(registerReq.getJibun())
                        .road(registerReq.getRoad())
                        .zipcode(registerReq.getZipcode())
                        .detailAddress(registerReq.getDetailAddress()).build())
                .birth(registerReq.getBirth())
                .loginId(registerReq.getLoginId())
                .email(registerReq.getEmail())
                .password(registerReq.getPassword())
                .phoneNumber(registerReq.getPhoneNumber())
                .build();
        return buyer;
    }
}
