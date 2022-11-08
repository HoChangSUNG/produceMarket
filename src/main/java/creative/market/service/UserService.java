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
import creative.market.service.dto.UserInfoRes;
import creative.market.util.SessionUtils;
import creative.market.web.dto.CreateAndChangeUserReq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final BuyerRepository buyerRepository;
    private final SellerRepository sellerRepository;
    private final AdminRepository adminRepository;

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public Long register(Buyer buyer) {

        buyerRepository.findByLoginId(buyer.getLoginId())
                .ifPresent((b) -> {
                    throw new DuplicateException("회원가입 실패(아이디 중복)");
                });

        return buyerRepository.register(buyer);
    }

    public User login(String loginId, String password) {
        Optional<Buyer> buyer = buyerRepository.findByLoginIdAndPassword(loginId, password);
        Optional<Seller> seller = sellerRepository.findByLoginIdAndPassword(loginId, password);
        Optional<Admin> admin = adminRepository.findByLoginIdAndPassword(loginId, password);

        User user = null;

        if (buyer.isPresent()) {
            user = buyer.get();
        } else if (seller.isPresent()) {
            user = seller.get();
        } else if (admin.isPresent()) {
            user = admin.get();
        }

        if (user == null)
            throw new LoginAuthenticationException("로그인 실패");

        return user;
    }

    @Transactional
    public void update(CreateAndChangeUserReq req, Long userId, UserType type) {

        User user = null;

        if(type.equals(UserType.SELLER)) {
            user = sellerRepository.findById(userId).get();
        } else if(type.equals(UserType.BUYER)) {
            user = buyerRepository.findById(userId).get();
        } else {
            throw new LoginAuthenticationException("로그인이 필요합니다.");
        }

        Address address = createAddress(req.getJibun(), req.getRoad(), req.getZipcode(), req.getDetailAddress());

        user.updateUser(req.getName(), req.getLoginId(), req.getPassword(), req.getBirth(), req.getEmail(), req.getPhoneNumber(), address);
    }

    @Transactional
    public void delete(Long id) {
        userRepository.delete(id);
    }

    public Buyer getBuyer(Long id) {
        Buyer buyer = buyerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("회원이 존재하지 않습니다."));

        return buyer;
    }

    public Seller getSeller(Long id) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("회원이 존재하지 않습니다."));

        return seller;
    }

    private Address createAddress(String jibun, String road, int zipcode, String detailAddress) {
        return Address.builder()
                .jibun(jibun)
                .road(road)
                .zipcode(zipcode)
                .detailAddress(detailAddress)
                .build();
    }
}
