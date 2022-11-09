package creative.market.util;

public class PagingUtils {

    public static int getOffset(int pageNum, int pageSize) {
        return pageNum < 1 || pageSize < 1 ? 0 : (pageNum - 1) * pageSize;
    }

    public static int getTotalPageNum(long listTotalSize, int pageSize) {
        return listTotalSize == 0 || pageSize<1 ? 1 : (int) Math.ceil((double) listTotalSize / (double) pageSize);
    }
}
