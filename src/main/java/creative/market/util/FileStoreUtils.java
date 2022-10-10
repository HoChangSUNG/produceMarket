package creative.market.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

public class FileStoreUtils {

    public static String getFullPath(String rootPath,String subPath) {
        return rootPath + subPath;
    }
}
