package creative.market.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PagingResultRes<T> {

    private T result;
    private int curPageNum;
    private int lastPageNum;

}
