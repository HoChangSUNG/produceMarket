package creative.market.domain;

import creative.market.domain.product.ProductStatus;
import lombok.Getter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import java.time.LocalDateTime;


@MappedSuperclass
@Getter
public class CreatedDateAndStatus {

    private LocalDateTime createdDate;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @PrePersist
    private void prePersist() {
        createdDate = LocalDateTime.now();
        status = ProductStatus.EXIST;
    }

    public void changeCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public void changeStatus(ProductStatus status) {
        this.status = status;
    }

}
