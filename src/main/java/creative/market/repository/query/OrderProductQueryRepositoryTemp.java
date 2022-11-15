package creative.market.repository.query;

import creative.market.repository.dto.CategoryParamDTO;
import creative.market.repository.dto.SellerPricePerPeriodDTO;
import lombok.RequiredArgsConstructor;
import org.qlrm.mapper.JpaResultMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.time.YearMonth;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderProductQueryRepositoryTemp {
    private final EntityManager em;
    private final JpaResultMapper jpaResultMapper;

    public List<SellerPricePerPeriodDTO> findAllSellerTotalPricePerPeriodAndCategory(YearMonth startDate, YearMonth endDate, CategoryParamDTO categoryParamDTO) {//기간별 판매액 비교(카테고리별 전체 판매자 기간별 총 금액)
//        String sql = "select cast(ifNull(sum(op.price * op.count),0) as SIGNED) as totalPrice, month_year_tb.ym as date" +
//                " from (select opp.price as price, opp.count as count, o.created_date as created_date" +
//                "    from order_product opp " +
//                "    join orders o on opp.order_id = o.order_id " +
//                "    join product p on opp.product_id = p.product_id" +
//                "    join kind_grade kg on p.kind_grade_id = kg.kind_grade_id" +
//                "    join kind k on kg.kind_id = k.kind_id" +
//                "    join item i on k.item_code = i.item_code" +
//                "    join item_category ic on i.item_category_code = ic.item_category_code" +
//                "    where opp.status = 'ORDER' 카테고리_dynamicQuery ) op" +
//                " right outer join  year_month_data" +
//                " as month_year_tb on date_format(op.created_date,'%Y-%m') = month_year_tb.ym" +
//                " where month_year_tb.ym>=:startDate and  month_year_tb.ym<=:endDate" +
//                " group by month_year_tb.ym" +
//                " order by month_year_tb.ym";

        StringBuilder sb = new StringBuilder();
        String sql = sb.append("select cast(ifNull(sum(op.price * op.count),0) as SIGNED) as totalPrice, month_year_tb.ym as date")
                .append(" from (select opp.price as price, opp.count as count, o.created_date as created_date")
                .append("    from order_product opp")
                .append("    join orders o on opp.order_id = o.order_id ")
                .append("    join product p on opp.product_id = p.product_id")
                .append("    join kind_grade kg on p.kind_grade_id = kg.kind_grade_id")
                .append("    join kind k on kg.kind_id = k.kind_id")
                .append("    join item i on k.item_code = i.item_code")
                .append("    join item_category ic on i.item_category_code = ic.item_category_code")
                .append("    where opp.status = 'ORDER' ").append(categoryDynamic(categoryParamDTO)).append(") op")
                .append(" right outer join  year_month_data")
                .append(" as month_year_tb on date_format(op.created_date,'%Y-%m') = month_year_tb.ym")
                .append(" where month_year_tb.ym>=:startDate and  month_year_tb.ym<=:endDate")
                .append(" group by month_year_tb.ym")
                .append(" order by month_year_tb.ym")
                .toString();

        Query query = em.createNativeQuery(sql)
                .setParameter("startDate", startDate.toString())
                .setParameter("endDate", endDate.toString());

        return jpaResultMapper.list(query, SellerPricePerPeriodDTO.class);
    }

    public List<SellerPricePerPeriodDTO> findSellerTotalPricePerPeriodAndCategory(YearMonth startDate, YearMonth endDate, CategoryParamDTO categoryParamDTO, Long userId) {//기간별 판매액 비교(카테고리별 특정 판매자 기간별 총 금액)
//        String sql = "select cast(ifNull(sum(op.price * op.count),0) as SIGNED) as totalPrice, month_year_tb.ym as date" +
//                "from (select opp.price as price, opp.count as count, o.created_date as created_date " +
//                "    from order_product opp " +
//                "    join orders o on opp.order_id = o.order_id " +
//                "    join product p on opp.product_id = p.product_id" +
//                "    join user u on p.user_id = u.user_id" +
//                "    join kind_grade kg on p.kind_grade_id = kg.kind_grade_id" +
//                "    join kind k on kg.kind_id = k.kind_id" +
//                "    join item i on k.item_code = i.item_code" +
//                "    join item_category ic on i.item_category_code = ic.item_category_code" +
//                "    where opp.status = 'ORDER' and u.user_id =:userId 카테고리_dynamicQuery ) op" +
//                " right outer join  year_month_data" +
//                " as month_year_tb on date_format(op.created_date,'%Y-%m') = month_year_tb.ym" +
//                " where month_year_tb.ym>='2022-01' and  month_year_tb.ym<='2022-11'" +
//                " group by month_year_tb.ym" +
//                " order by month_year_tb.ym";

        StringBuilder sb = new StringBuilder();
        String sql = sb.append("select cast(ifNull(sum(op.price * op.count),0) as SIGNED) as totalPrice, month_year_tb.ym as date")
                .append(" from (select opp.price as price, opp.count as count, o.created_date as created_date")
                .append("    from order_product opp")
                .append("    join orders o on opp.order_id = o.order_id ")
                .append("    join product p on opp.product_id = p.product_id")
                .append("    join user u on p.user_id = u.user_id")
                .append("    join kind_grade kg on p.kind_grade_id = kg.kind_grade_id")
                .append("    join kind k on kg.kind_id = k.kind_id")
                .append("    join item i on k.item_code = i.item_code")
                .append("    join item_category ic on i.item_category_code = ic.item_category_code")
                .append("    where opp.status = 'ORDER' and u.user_id =:userId " ).append(categoryDynamic(categoryParamDTO)).append(") op")
                .append(" right outer join  year_month_data")
                .append(" as month_year_tb on date_format(op.created_date,'%Y-%m') = month_year_tb.ym")
                .append(" where month_year_tb.ym>=:startDate and  month_year_tb.ym<=:endDate")
                .append(" group by month_year_tb.ym")
                .append(" order by month_year_tb.ym")
                .toString();

        Query query = em.createNativeQuery(sql)
                .setParameter("startDate", startDate.toString())
                .setParameter("endDate", endDate.toString())
                .setParameter("userId", userId);

        return jpaResultMapper.list(query, SellerPricePerPeriodDTO.class);
    }

    private String categoryDynamic(CategoryParamDTO categoryParamDTO) {
        StringBuilder stringBuilder = new StringBuilder();

        Integer itemCategoryCode = categoryParamDTO.getItemCategoryCode();
        Integer itemCode = categoryParamDTO.getItemCode();
        Long kindId = categoryParamDTO.getKindId();
        Long kindGradeId = categoryParamDTO.getKindGradeId();

        if (itemCategoryCode != null) { // 부류가 null 이 아닌경우 where 절에 추가
            addQuery("ic.item_category_code", String.valueOf(itemCategoryCode), stringBuilder);
        }
        if (itemCode != null) { // 품목이 null 이 아닌경우 where 절에 추가
            addQuery("i.item_code", String.valueOf(itemCode), stringBuilder);
        }
        if (kindId != null) { // 품종이 null 이 아닌경우 where 절에 추가
            addQuery("k.kind_id", kindId.toString(), stringBuilder);
        }
        if (kindGradeId != null) { // 픔정등급이 null 이 아닌경우 where 절에 추가
            addQuery("kg.kind_grade_id", kindGradeId.toString(), stringBuilder);
        }

        return stringBuilder.toString();
    }

    private StringBuilder addQuery(String columnName, String value, StringBuilder stringBuilder) {
        return StringUtils.hasText(value) ? stringBuilder.append(" and ").append(columnName).append(" = ").append(value) : stringBuilder;

    }
}
