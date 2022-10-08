package creative.market.domain;

import lombok.Getter;

import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
public class ChangeDate extends CreatedDate{

    private LocalDateTime changeDate;

    @PreUpdate
    private void preUpdate() {
        changeDate = LocalDateTime.now();
    }

}
