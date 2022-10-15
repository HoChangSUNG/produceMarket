package creative.market.repository.user;

import creative.market.domain.Address;
import creative.market.domain.user.Buyer;
import creative.market.domain.user.Seller;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Optional;

@SpringBootTest
@Transactional
class BuyerRepositoryTest {

    @Autowired
    EntityManager em;
    @Autowired
    BuyerRepository buyerRepository;
    @Autowired
    SellerRepository sellerRepository;

    private Buyer getBuyer(String name,String loginId, String pw,Address address) {
        Buyer buyer = Buyer.builder().name(name).loginId(loginId).password(pw).address(address).build();
        em.persist(buyer);
        return buyer;
    }

    @Test
    @DisplayName("구매자 -> 판매자 전환")
    void updateTypeToSeller() {
        //given
        Address address = Address.builder().jibun("1").road("1").zipcode(12).detailAddress("234").build();
        Buyer buyer = getBuyer("김현민","loginId1","pw1",address);

        //when
        updateType(buyer);
        em.flush();
        em.clear();
        Optional<Seller> seller = sellerRepository.findById(buyer.getId());

        //then
        Assertions.assertThat(buyer.getName()).isEqualTo(seller.get().getName());
        Assertions.assertThat(buyer.getId()).isEqualTo(seller.get().getId());
    }

    private void updateType(Buyer buyer) {
        em.createNativeQuery("update User b set b.dtype = 'Seller' where b.user_id = :id")
                .setParameter("id", buyer.getId())
                .executeUpdate();
    }
}