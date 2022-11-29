package creative.market;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import creative.market.domain.Address;
import creative.market.domain.Review;
import creative.market.domain.order.Order;
import creative.market.domain.product.Product;
import creative.market.domain.user.Seller;
import creative.market.repository.ProductRepository;
import creative.market.repository.order.OrderRepository;
import creative.market.repository.user.SellerRepository;
import creative.market.service.OrderService;
import creative.market.service.ProductService;
import creative.market.service.ReviewService;
import creative.market.service.dto.LoginUserDTO;
import creative.market.service.dto.OrderProductParamDTO;
import creative.market.service.dto.RegisterProductDTO;
import creative.market.service.dto.UploadFileDTO;
import creative.market.util.FileStoreUtils;
import creative.market.util.FileSubPath;
import creative.market.web.dto.CreateProductReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitDb {

    @Value("${images}")
    private String rootPath;

    private final ProductService productService;
    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final ReviewService reviewService;

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void init() throws IOException {
        //String[] productList = {"백미", "샤인머스켓", "방울토마토"};
        //initProducts(productList);

        //initOrders();
        //initReviews();
    }

    public void initReviews() {
        List<Product> products = productRepository.findAll();

        for (Product product : products) {
            Review review = Review.builder()
                    .rate(4.5f)
                    .content("맛있어요!")
                    .build();
            reviewService.save(review, product.getId(), 56L);
        }
    }

    public void initProducts(String[] productList) throws IOException {
        Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        List<Seller> sellers = sellerRepository.findAll();
        int i = 0;

        for (String s : productList) {
            String productData = getProductData(s, "2");

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> map = mapper.readValue(productData, new TypeReference<Map<String, Object>>() {});
            Map<String, Object> searchAdResult = (Map<String, Object>) map.get("shoppingResult");
            List<Map<String, String>> products = (List<Map<String, String>>) searchAdResult.get("products");

            for (Map<String, String> product : products) {
                String title = product.get("productName");
                String kg = getKg(title).replace(" ", "");
                if(kg.equals("")) {
                    continue;
                }
                String imageUrl = product.get("imageUrl");
                String price = product.get("price");
                int unitPrice = (int) (Integer.parseInt(price) / Float.parseFloat(kg));

                URL img = new URL(imageUrl);
                HttpURLConnection conn = (HttpURLConnection) img.openConnection();
                BufferedImage bufferedImage = ImageIO.read(conn.getInputStream());
                MultipartFile multipartFile = convertBufferedImageToMultipartFile(bufferedImage);

                List<MultipartFile> list = new ArrayList();
                list.add(multipartFile);

                UploadFileDTO sigImage = FileStoreUtils.storeFile(multipartFile, rootPath, FileSubPath.PRODUCT_PATH);
                List<UploadFileDTO> ordinalImages = FileStoreUtils.storeFiles(list, rootPath, FileSubPath.PRODUCT_PATH);

                Long kindGradeId = 0L;
                Long randomValue = Long.valueOf(random.nextInt(3));
                if(s.equals("백미")) {
                    kindGradeId = 404L;
                } else if(s.equals("샤인머스켓")) {
                    kindGradeId = 762L + randomValue;
                } else if(s.equals("방울토마토")) {
                    kindGradeId = 663L + randomValue;
                }

                CreateProductReq createProductReq = new CreateProductReq(kindGradeId, title, unitPrice, title, null, null);
                LoginUserDTO loginUserDTO = new LoginUserDTO(sellers.get(i++).getId(), null, null);
                productService.register(createRegisterProductDTO(createProductReq, loginUserDTO, sigImage, ordinalImages));

                i%=50;
            }
        }
    }

    public void initOrders() {
        Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        List<Product> products = productRepository.findAll(); // 175개

        Address address = Address.builder()
                .jibun("충남 천안시 서북구 백석동 909")
                .road("충남 천안시 서북구 백석2길 12")
                .zipcode(31158)
                .detailAddress("101동 203호")
                .build();

        List<OrderProductParamDTO> orderProducts = new ArrayList<>();

        LocalDateTime date = LocalDateTime.of(2022, 1, 2, 1, 1);

        for (Product product : products) {
            int cnt = random.nextInt(3) + 4;
            for (int i = 0; i < cnt; i++) {
                orderProducts.clear();
                orderProducts.add(new OrderProductParamDTO(random.nextInt(10) + 1, product.getId()));
                Long orderId = orderService.order(56L, orderProducts, address);

                Order findOrder = orderRepository.findById(orderId)
                        .orElseThrow(() -> new NoSuchElementException("주문이 존재하지 않습니다."));
                findOrder.changeCreatedDate(date);

                date = date.plusDays(1);
                if (date.isAfter(LocalDateTime.of(2022, 11, 28, 1, 1, 1))) {
                    date = LocalDateTime.of(2022, 1, 2, 1, 1);
                }
            }
        }

        List<Product> productList = new ArrayList<>();
        productList.add(products.get(10)); // user_id = 16
        productList.add(products.get(40)); // user_id = 46
        productList.add(products.get(70)); // user_id = 26

        for (Product product : productList) {
            date = LocalDateTime.of(2022, 1, 2, 1, 1);

            for(int i=0; i<12; i++) {
                int cnt = random.nextInt(10) + 10;

                for(int j=0; j<cnt; j++) {
                    orderProducts.clear();
                    orderProducts.add(new OrderProductParamDTO(random.nextInt(10) + 1, product.getId()));
                    Long orderId = orderService.order(56L, orderProducts, address);

                    Order findOrder = orderRepository.findById(orderId)
                            .orElseThrow(() -> new NoSuchElementException("주문이 존재하지 않습니다."));
                    findOrder.changeCreatedDate(date);
                }

                date = date.plusMonths(1);
            }
        }
    }

    private String getProductData(String productName, String pagingIndex) throws IOException {
        String encodeProduct = URLEncoder.encode(productName, "UTF-8");
        String str = "https://search.shopping.naver.com/api/search/all?sort=rel&pagingIndex=" + pagingIndex + "&pagingSize=40&viewType=list&productSet=total&deliveryFee=&deliveryTypeValue=&frm=NVSHATC&query=" + encodeProduct + "&origQuery=" + encodeProduct + "&iq=&eq=&xq=";
        URL url = new URL(str);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("GET");

        httpConn.setRequestProperty("authority", "search.shopping.naver.com");
        httpConn.setRequestProperty("accept", "application/json, text/plain, */*");
        httpConn.setRequestProperty("accept-language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
        httpConn.setRequestProperty("cookie", "NNB=ULPSGTUM34MWG; _ga=GA1.2.1972022868.1663045147; NDARK=Y; _gcl_au=1.1.1684337725.1663175435; ASID=70d9a7ca00000183690d940e00016c07; autocomplete=use; AD_SHP_BID=17; nx_ssl=2; page_uid=h3zfmwprvh8sscW8r6ZssssstKo-131217; spage_uid=h3zfmwprvh8sscW8r6ZssssstKo-131217; BMR=s=1669213986424&r=https%3A%2F%2Fm.blog.naver.com%2Fnuberus%2F221743756298&r2=https%3A%2F%2Fwww.google.com%2F; sus_val=0oC0g5hK4onnS2dHVFN2ZbMl");
        httpConn.setRequestProperty("logic", "PART");
        httpConn.setRequestProperty("referer", "https://search.shopping.naver.com/search/all?query=%EB%B0%B1%EB%AF%B8&frm=NVSHATC&prevQuery=%EB%B0%B1%EB%AF%B8");
        httpConn.setRequestProperty("sec-ch-ua", "\"Google Chrome\";v=\"107\", \"Chromium\";v=\"107\", \"Not=A?Brand\";v=\"24\"");
        httpConn.setRequestProperty("sec-ch-ua-mobile", "?0");
        httpConn.setRequestProperty("sec-ch-ua-platform", "\"Windows\"");
        httpConn.setRequestProperty("sec-fetch-dest", "empty");
        httpConn.setRequestProperty("sec-fetch-mode", "cors");
        httpConn.setRequestProperty("sec-fetch-site", "same-origin");
        httpConn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36");

        InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                ? httpConn.getInputStream()
                : httpConn.getErrorStream();

        Scanner s = new Scanner(responseStream).useDelimiter("\\A");

        return s.hasNext() ? s.next() : "";
    }

    private String getKg(String str) {
        int index = str.indexOf("kg");
        String kg = "";
        if (index >= 0) {
            kg = str.substring(index - 2, index);
            if(kg.charAt(0) == '.') {
                return str.substring(index - 3, index);
            }
            if(!isInteger(kg)) {
                kg = str.substring(index - 1, index);
            }
        }
        return kg;
    }

    private boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private MultipartFile convertBufferedImageToMultipartFile(BufferedImage image) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", out);
        } catch (IOException e) {
            log.error("IO Error", e);
            return null;
        }
        byte[] bytes = out.toByteArray();
        return new CustomMultipartFile(bytes, "image", "image.png", "png", bytes.length);
    }

    private RegisterProductDTO createRegisterProductDTO(CreateProductReq productReq, LoginUserDTO loginUserDTO, UploadFileDTO sigImage, List<UploadFileDTO> ordinalImages) {
        return new RegisterProductDTO(productReq.getKindGradeId(), productReq.getName(), productReq.getPrice(),
                productReq.getInfo(), loginUserDTO.getId(), sigImage, ordinalImages);
    }
}
