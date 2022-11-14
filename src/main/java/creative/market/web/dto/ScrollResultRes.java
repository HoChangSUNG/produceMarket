package creative.market.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScrollResultRes<T> {

    private T result;
    private Long totalNum;
}
