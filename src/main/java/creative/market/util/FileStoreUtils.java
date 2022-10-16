package creative.market.util;

import creative.market.service.dto.UploadFileDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileStoreUtils {

    public static String getFullPath(String rootPath, String subPath) {
        return rootPath + subPath;
    }

    // 파일 리스트 저장
    public static List<UploadFileDTO> storeFiles(List<MultipartFile> multipartFiles, String rootPath, String subPath) throws IOException {
        List<UploadFileDTO> storeResult = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            if (!multipartFiles.isEmpty()) {
                storeResult.add(storeFile(multipartFile, rootPath, subPath));
            }
        }
        return storeResult;
    }

    // 파일 저장
    public static UploadFileDTO storeFile(MultipartFile multipartFile, String rootPath, String subPath) throws IOException {
        if (multipartFile.isEmpty()) {
            return null;
        }
        String originalFilename = getOriginalFileName(multipartFile);// image.png
        // 서버에 저장하는 파일명
        String storeFileName = createStoreFileName(originalFilename);

        //파일 저장
        multipartFile.transferTo(new File(getFullPath(rootPath, subPath + storeFileName)));
        return new UploadFileDTO(originalFilename, storeFileName);
    }

    public static String getOriginalFileName(MultipartFile multipartFile) {
        if (multipartFile.isEmpty() || multipartFile == null){
            return null;
        }
        return multipartFile.getOriginalFilename();
    }

    private static String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename); // 확장자 추출
        return UUID.randomUUID().toString() + "." + ext; // wfese-wfe-223.png
    }

    private static String extractExt(String originalFilename) { // 확장자 추출
        int position = originalFilename.lastIndexOf(".");
        return originalFilename.substring(position + 1);
    }


}
