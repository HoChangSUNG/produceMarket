package creative.market.repository.dto;

import creative.market.domain.Cart;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CartInfoRes {

    private Long cartId;
    private Long productId;
    private int count;
    private String productName;
    private int price;
    private String signatureImgSrc;

    public CartInfoRes(Cart cart) {
        this.cartId = cart.getId();
        this.productId = cart.getProduct().getId();
        this.count = cart.getCount();
        this.productName = cart.getProduct().getName();
        this.price =  cart.getProduct().getPrice();
        this.signatureImgSrc = cart.getProduct().getSignatureProductImage().getPath();
    }
}
