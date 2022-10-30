package creative.market.domain;

import lombok.Getter;

import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
public abstract class CreatedDate {

    private LocalDateTime createdDate;

    @PrePersist
    private void prePersist() {
        createdDate = LocalDateTime.now();
    }

    public void changeCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
