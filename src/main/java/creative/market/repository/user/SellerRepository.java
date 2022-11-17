package creative.market.repository.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import creative.market.domain.user.Seller;
import creative.market.repository.dto.CategoryParamDTO;
import creative.market.repository.dto.SellerCountByPeriodDTO;
import creative.market.repository.dto.SellerPercentileDTO;
import lombok.RequiredArgsConstructor;
import org.qlrm.mapper.JpaResultMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static creative.market.domain.user.QSeller.*;

@Repository
@RequiredArgsConstructor
public class SellerRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;
    private final JpaResultMapper jpaResultMapper;


    public Optional<Seller> findByLoginIdAndPassword(String loginId, String password) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(seller)
                        .where(seller.loginId.eq(loginId).and(seller.password.eq(password)))
                        .fetchOne());
    }

    public Optional<Seller> findById(Long id) {
        Seller seller = em.find(Seller.class, id);
        return Optional.ofNullable(seller);
    }

    public Long findAllSellerCountWithExistAndDeletedSeller() { // 삭제된 seller + 존재하는 seller 모두 count
        return queryFactory.select(seller.count().longValue())
                .from(seller)
                .fetchOne();
    }

    public List<SellerCountByPeriodDTO> findSellerCountExistOrderByPeriod(YearMonth startDate, YearMonth endDate, CategoryParamDTO categoryParamDTO) { // 기간별,카테고리별 판매 기록이 있는 판매자 개수 count
        /*
        select ym as date, ifnull(count,0) count
        from(
                select count(distinct(p.user_id)) count, date_format(o.created_date,'%Y-%m') date
                from order_product op
                join orders o on op.order_id = o.order_id
                join product p on op.product_id = p.product_id
                join user u on p.user_id = u.user_id
                join kind_grade kg on p.kind_grade_id = kg.kind_grade_id
                join kind k on kg.kind_id = k.kind_id
                join item i on k.item_code = i.item_code
                join item_category ic on i.item_category_code = ic.item_category_code
                where op.status = 'ORDER' and '2022-05'<=date_format(o.created_date,'%Y-%m') and  date_format(o.created_date,'%Y-%m')<='2022-11'
        group by date
        order by null
        ) op
        right join year_month_data ym_tb
        on op.date = ym_tb.ym
        where '2022-05'<= ym_tb.ym and  ym_tb.ym <='2022-11'
        order by ym_tb.ym;
        */

        StringBuilder sb = new StringBuilder();
        String sql = sb
                .append(" select ym, ifnull(count,0) count")
                .append(" from(")
                .append("   select count(distinct(p.user_id)) count, date_format(o.created_date,'%Y-%m') date")
                .append("   from order_product op ")
                .append("       join orders o on op.order_id = o.order_id ")
                .append("       join product p on op.product_id = p.product_id")
                .append("       join user u on p.user_id = u.user_id")
                .append("       join kind_grade kg on p.kind_grade_id = kg.kind_grade_id")
                .append("       join kind k on kg.kind_id = k.kind_id")
                .append("       join item i on k.item_code = i.item_code")
                .append("       join item_category ic on i.item_category_code = ic.item_category_code")
                .append("   where op.status = 'ORDER' and :startDate <= date_format(o.created_date,'%Y-%m') and date_format(o.created_date,'%Y-%m') <= :endDate ").append(categoryDynamic(categoryParamDTO))
                .append("   group by date")
                .append("   order by null")
                .append(" ) op")
                .append(" right join year_month_data ym_tb")
                .append(" on op.date = ym_tb.ym")
                .append(" where :startDate <= ym_tb.ym and ym_tb.ym <= :endDate")
                .append(" order by ym_tb.ym")
                .toString();

        Query query = em.createNativeQuery(sql, Long.class)
                .setParameter("startDate", startDate.toString())
                .setParameter("endDate", endDate.toString());

        return jpaResultMapper.list(query, SellerCountByPeriodDTO.class);

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
