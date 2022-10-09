package creative.market.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class WholesaleAndRetailUtils {

    @Value("${api.id}")
    private static String id;
    @Value("${api.key}")
    private static String key;
    private static int WHOLE_SALES_CODE = 2; // 도매 시세 코드
    private static int RETAIL_CODE = 1; // 소매 시세 코드

    //연간 도소매 시세 조회(당일 년부터 5년치)
    public static String getYearData(int itemCategoryCode, int itemCode, int kindCode, int gradeRank) {
        String year = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy")); // 올해 년도
        String url = "https://www.kamis.or.kr/service/price/xml.do?action=yearlySalesList&p_yyyy=" + year + "&p_itemcategorycode=" + itemCategoryCode + "&p_itemcode=" + itemCode +
                "&p_kindcode=" + String.format("%02d", kindCode) + "&p_graderank=" + gradeRank + "&p_convert_kg_yn=Y&p_cert_key=" + key + "&p_cert_id=" + id + "&p_returntype=json";
        return getApiData(url);
    }

    // 월별 도소매 시세 정보 조회
    public static String getMonthlyWholeSalesAndRetailData(int endYear, int itemCategoryCode, int itemCode, int kindCode, int gradeRank) {
        String url = "https://www.kamis.or.kr/service/price/xml.do?action=monthlySalesList&p_yyyy=" + endYear + "&p_period=3&p_itemcategorycode=" + itemCategoryCode +
                "&p_itemcode=" + itemCode + "&p_kindcode=" + String.format("%02d", kindCode) + "&p_graderank=" + gradeRank + "&p_convert_kg_yn=Y&p_cert_key=" + key + "&p_cert_id=" + id +
                "&p_returntype=json";
        return getApiData(url);
    }


    //일별 도매 시세 조회
    public static String getDayWholeSalesData(LocalDateTime startDate, LocalDateTime endDate, int itemCategoryCode, int itemCode, int kindCode, int gradeId) {
        return getApiData(getDayDataUrl(WHOLE_SALES_CODE, startDate, endDate, itemCategoryCode, itemCode, kindCode, gradeId));
    }

    //일별 소매 시세 조회
    public static String getDayRetailData(LocalDateTime startDate, LocalDateTime endDate, int itemCategoryCode, int itemCode, int kindCode, int gradeId) {
        return getApiData(getDayDataUrl(RETAIL_CODE, startDate, endDate, itemCategoryCode, itemCode, kindCode, gradeId));
    }

    private static String getApiData(String url) {
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

    private static String getDayDataUrl(int type, LocalDateTime startDate, LocalDateTime endDate, int itemCategoryCode, int itemCode, int kindCode, int gradeId) {
        String convertStartDay = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // 시작 날짜
        String convertEndDay = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));  // 종료 날짜
        return "https://www.kamis.or.kr/service/price/xml.do?action=periodProductList&p_productclscode=" + String.format("%02d", type) + "&p_startday=" + convertStartDay +
                "&p_endday=" + convertEndDay + "&p_itemcategorycode=" + itemCategoryCode + "&p_itemcode=" + itemCode + "&p_kindcode=" + String.format("%02d", kindCode) +
                "&p_productrankcode=" + String.format("%02d", gradeId) + "&p_convert_kg_yn=Y&p_cert_key=" + key + "&p_cert_id=" + id + "&p_returntype=json";

    }

    //도매가 비교 함수 만들기

    // 소매가 비교 함수 만들기
}
