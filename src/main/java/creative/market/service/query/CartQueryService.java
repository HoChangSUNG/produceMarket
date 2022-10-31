package creative.market.service.query;

import creative.market.repository.CartRepository;
import creative.market.repository.dto.CartInfoRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartQueryService {
    private final CartRepository cartRepository;

    public List<CartInfoRes> getCartList(Long userId) {
        return cartRepository.findByUserIdFetchJoinProductAndKind(userId).stream()
                .map(CartInfoRes::new)
                .collect(Collectors.toList());
    }
}
