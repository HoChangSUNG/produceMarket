package creative.market.domain;

import lombok.Getter;

import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
public class CreatedDate {

    private LocalDateTime createdDate;

    @PrePersist
    private void prePersist() {
        createdDate = LocalDateTime.now();
    }
}
