package creative.market.service;

import creative.market.domain.Cart;
import creative.market.domain.order.OrderProduct;
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

import java.util.NoSuchElementException;

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

        duplicateCart(userId, productId); // 해당 상품이 이미 장바구니에 존재하는지

        Cart cart = createCart(product, user, count);
        cartRepository.save(cart);

        return cart.getId();
    }

    private void duplicateCart(Long userId, Long productId) {
        long count = cartRepository.findByUserIdWithProduct(userId).stream().filter(cart -> cart.getProduct().getId().equals(productId)).count();
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
}
