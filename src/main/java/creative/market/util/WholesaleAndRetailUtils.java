package creative.market.util;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class WholesaleAndRetailUtils {

    @Value("${api.id}")
    private static String id;
    @Value("${api.key}")
    private static String key;
    private static int WHOLE_SALES_CODE = 2; // 도매 시세 코드
    private static int RETAIL_CODE = 1; // 소매 시세 코드

    //연간 도소매 시세 조회(당일 년부터 5년치)
    public String getYearData(int itemCategoryCode, int itemCode, int kindCode, int gradeRank) {
        String year = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy")); // 올해 년도
        String url = "https://www.kamis.or.kr/service/price/xml.do?action=yearlySalesList&p_yyyy=" + year + "&p_itemcategorycode=" + itemCategoryCode + "&p_itemcode=" + itemCode +
                "&p_kindcode=" + String.format("%02d", kindCode) + "&p_graderank=" + gradeRank + "&p_convert_kg_yn=Y&p_cert_key=" + key + "&p_cert_id=" + id + "&p_returntype=json";
        return getApiData(url);
    }

    //월별 도소매 시세 조회
    public String getMonthlyWholeSalesAndRetailData(int endYear, int itemCategoryCode, int itemCode, int kindCode, int gradeRank) {
        String url = "https://www.kamis.or.kr/service/price/xml.do?action=monthlySalesList&p_yyyy=" + endYear + "&p_period=3&p_itemcategorycode=" + itemCategoryCode +
                "&p_itemcode=" + itemCode + "&p_kindcode=" + String.format("%02d", kindCode) + "&p_graderank=" + gradeRank + "&p_convert_kg_yn=Y&p_cert_key=" + key + "&p_cert_id=" + id +
                "&p_returntype=json";
        return getApiData(url);
    }

    //일별 도매 시세 조회
    public String getDayWholeSalesData(LocalDate startDate, LocalDate endDate, int itemCategoryCode, int itemCode, int kindCode, int gradeId) {
        return getApiData(getDayDataUrl(WHOLE_SALES_CODE, startDate, endDate, itemCategoryCode, itemCode, kindCode, gradeId));
    }

    //일별 소매 시세 조회
    public String getDayRetailData(LocalDate startDate, LocalDate endDate, int itemCategoryCode, int itemCode, int kindCode, int gradeId) {
        return getApiData(getDayDataUrl(RETAIL_CODE, startDate, endDate, itemCategoryCode, itemCode, kindCode, gradeId));
    }

    //최근 도매가 조회
    public LatestConvertPriceDTO latestWholeSalesPrice(int itemCategoryCode, int itemCode, int kindCode, int gradeId) {
        JSONObject wholeSaleObject = priceJsonObject(WHOLE_SALES_CODE, LocalDate.now().minusDays(20), LocalDate.now(), itemCategoryCode, itemCode, kindCode, gradeId);
        return convertToLatestPriceDTO(wholeSaleObject);
    }

    //최근 소매가 조회
    public LatestConvertPriceDTO latestRetailPrice(int itemCategoryCode, int itemCode, int kindCode, int gradeId) {
        JSONObject retailObject = priceJsonObject(RETAIL_CODE, LocalDate.now().minusDays(20), LocalDate.now(), itemCategoryCode, itemCode, kindCode, gradeId);
        return convertToLatestPriceDTO(retailObject);
    }

    //일간 도소매 조회 url
    private String getDayDataUrl(int type, LocalDate startDate, LocalDate endDate, int itemCategoryCode, int itemCode, int kindCode, int gradeId) {
        String convertStartDay = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // 시작 날짜
        String convertEndDay = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));  // 종료 날짜

        return "https://www.kamis.or.kr/service/price/xml.do?action=periodProductList&p_productclscode=" + String.format("%02d", type) + "&p_startday=" + convertStartDay +
                "&p_endday=" + convertEndDay + "&p_itemcategorycode=" + itemCategoryCode + "&p_itemcode=" + itemCode + "&p_kindcode=" + String.format("%02d", kindCode) +
                "&p_productrankcode=" + String.format("%02d", gradeId) + "&p_convert_kg_yn=Y&p_cert_key=" + key + "&p_cert_id=" + id + "&p_returntype=json";
    }

    //api 조회 결과
    private String getApiData(String url) {
        String result;
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader bf;
            bf = new BufferedReader(new InputStreamReader(new URL(url).openStream(), "UTF-8"));
            while ((result = bf.readLine()) != null) {
                sb.append(result);
            }
            bf.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    //최근 가격 저장된 JSON 객체를 DTO로 변환
    private LatestConvertPriceDTO convertToLatestPriceDTO(JSONObject priceJsonObject) {
        if (priceJsonObject == null) {
            return new LatestConvertPriceDTO();
        }

        String[] regday = priceJsonObject.get("regday").toString().split("/");
        int year = Integer.parseInt(priceJsonObject.get("yyyy").toString());
        int month = Integer.parseInt(regday[0]);
        int day = Integer.parseInt(regday[1]);
        int price = Integer.parseInt(priceJsonObject.get("price").toString().replaceAll(",", ""));

        return new LatestConvertPriceDTO(year, month, day, price);
    }

    //최근 가격 저장된 JSON 얻기
    private JSONObject priceJsonObject(int type, LocalDate startDate, LocalDate endDate, int itemCategoryCode, int itemCode, int kindCode, int gradeId) {
        JSONObject recentlyResult = null;
        try {
            String dayRetailDataUrl = getDayDataUrl(type, startDate, endDate, itemCategoryCode, itemCode, kindCode, gradeId);
            JSONParser jsonParser = new JSONParser();
            JSONObject result = (JSONObject) jsonParser.parse(getApiData(dayRetailDataUrl));
            JSONObject data = (JSONObject) result.get("data");
            JSONArray itemList = (JSONArray) data.get("item");

            for (int i = 0; i < itemList.size(); i++) {
                JSONObject curResult = (JSONObject) itemList.get(i);
                String region = curResult.get("countyname").toString();
                if (!region.equals("평균")) {
                    break;
                }
                recentlyResult = curResult;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return recentlyResult;

    }
}
