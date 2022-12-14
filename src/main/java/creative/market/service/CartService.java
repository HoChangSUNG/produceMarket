package creative.market.service;

import creative.market.domain.Cart;
import creative.market.domain.product.Product;
import creative.market.domain.user.User;
import creative.market.exception.DuplicateException;
import creative.market.exception.LoginAuthenticationException;
import creative.market.repository.CartRepository;
import creative.market.repository.ProductRepository;
import creative.market.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long register(Long productId, int count, Long userId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("상품이 존재하지 않습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("회원이 존재하지 않습니다."));

        // 본인이 등록한 상품을 장바구니에 담으려는 경우 예외 발생
        checkMyProduct(product.getId(),userId);

        duplicateCart(userId, productId); // 해당 상품이 이미 장바구니에 존재하는지

        Cart cart = createCart(product, user, count);
        cartRepository.save(cart);

        return cart.getId();
    }

    @Transactional
    public void delete(Long cartId, Long userId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new NoSuchElementException("장바구니에 상품이 존재하지 않습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 유저입니다."));

        userAccessCheck(cart, user);

        cartRepository.delete(cart);
    }

    @Transactional
    public void deleteCartListByProductIds(List<Long> productIds, Long userId) { // 상품 아이디에 해당하는 cart 가 존재시 제거
        List<Cart> deleteCartList = cartRepository.findByUserId(userId).stream()
                .filter(cart -> productIds.contains(cart.getProduct().getId()))
                .collect(Collectors.toList());

        deleteCartList.forEach(cart -> delete(cart.getId(), userId));
    }

    private void userAccessCheck(Cart cart, User user) {
        if (!cart.getUser().getId().equals(user.getId())) {
            throw new LoginAuthenticationException("삭제 권한이 없습니다.");
        }
    }

    private void duplicateCart(Long userId, Long productId) {
        long count = cartRepository.findByUserIdFetchJoinProductAndKind(userId).stream()
                .filter(cart -> cart.getProduct().getId().equals(productId))
                .count();
        if (count > 0) {
            throw new DuplicateException("이미 장바구니에 해당 상품이 존재합니다.");
        }
    }

    private Cart createCart(Product product, User user, int count) {
        return Cart.builder()
                .count(count)
                .product(product)
                .user(user)
                .build();
    }

    private void checkMyProduct(Long productId, Long userId) {
        Product product = productRepository.findById(productId).orElseThrow(NoSuchElementException::new);
        if (product.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인이 등록한 상품은 구매할 수 없습니다.");
        }
    }
}
