package creative.market.util;

import creative.market.service.dto.UploadFileDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class FileStoreUtilsTest {

    @Value("${images}")
    private String rootPath;

    @Test
    @DisplayName("파일 생성 확인")
    void uploadFileTest() throws Exception{
        String absolutePath = FileStoreUtils.getFullPath(rootPath, FileSubPath.GRADE_CRITERIA_PATH + "쌀.png");

        //given
        MockMultipartFile multipart = new MockMultipartFile("image", "original.png", "image/png", new FileInputStream(absolutePath));

        //when
        UploadFileDTO uploadFileDTO = FileStoreUtils.storeFile(multipart, rootPath, FileSubPath.PRODUCT_PATH);

        log.info("originalName ={}",uploadFileDTO.getUploadFileName());
        log.info("storeName={}",uploadFileDTO.getStoreFileName());

        //then
        String storeFilePath = FileStoreUtils.getFullPath(rootPath, FileSubPath.PRODUCT_PATH + uploadFileDTO.getStoreFileName());
        File file = new File(storeFilePath);

        assertThat(file.exists()).isTrue(); // 존재 여부
        assertThat(file.isFile()).isTrue(); // 디렉토리가 아닌 파일 존재
        file.delete();
    }
}