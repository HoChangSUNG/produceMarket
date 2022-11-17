package creative.market.repository.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import creative.market.domain.order.OrderStatus;
import creative.market.domain.product.ProductImageType;
import creative.market.repository.dto.*;
import lombok.RequiredArgsConstructor;
import org.qlrm.mapper.JpaResultMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static creative.market.domain.category.QGradeCriteria.gradeCriteria;
import static creative.market.domain.category.QItem.item;
import static creative.market.domain.category.QItemCategory.itemCategory;
import static creative.market.domain.category.QKind.kind;
import static creative.market.domain.category.QKindGrade.kindGrade;
import static creative.market.domain.order.QOrder.order;
import static creative.market.domain.order.QOrderProduct.orderProduct;
import static creative.market.domain.product.QProduct.product;
import static creative.market.domain.product.QProductImage.*;
import static creative.market.domain.user.QUser.user;

@Repository
@RequiredArgsConstructor
public class OrderProductQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;
    private final JpaResultMapper jpaResultMapper;

    public List<SellerAndTotalPricePerCategoryDTO> findCategoryTopRankSellerNameAndPrice(CategoryParamDTO categoryParam, LocalDateTime startDate, LocalDateTime endDate, int rankCount) {// 카테고리별 판매 상위 판매자 및 판매 가격 조회
        return queryFactory.select(new QSellerAndTotalPricePerCategoryDTO(user.name, getTotalPrice().coalesce(0L)))
                .from(orderProduct)
                .join(orderProduct.order, order)
                .join(orderProduct.product, product)
                .join(product.user, user)
                .join(product.kindGrade, kindGrade)
                .join(kindGrade.kind, kind)
                .join(kind.item, item)
                .join(item.itemCategory, itemCategory)
                .join(item.gradeCriteria, gradeCriteria)
                .where(itemCategoryEq(categoryParam.getItemCategoryCode()),
                        itemCodeEq(categoryParam.getItemCode()),
                        kindEq(categoryParam.getKindId()),
                        kindGradeEq(categoryParam.getKindGradeId()),
                        dateBetween(startDate, endDate),
                        orderStatus())
                .orderBy(getTotalPrice().desc())
                .groupBy(user)
                .offset(0)
                .limit(rankCount)
                .fetch();
    }

    public SellerAndTotalPricePerCategoryDTO findCategorySellerNameAndPrice(CategoryParamDTO categoryParam, LocalDateTime startDate, LocalDateTime endDate, Long userId) {//선택된 카테고리 판매 내역에서 판매자의 이름 및 판매 가격 조회
        return queryFactory.select(new QSellerAndTotalPricePerCategoryDTO(user.name, getTotalPrice().coalesce(0L)))
                .from(orderProduct)
                .join(orderProduct.order, order)
                .join(orderProduct.product, product)
                .join(product.user, user)
                .join(product.kindGrade, kindGrade)
                .join(kindGrade.kind, kind)
                .join(kind.item, item)
                .join(item.itemCategory, itemCategory)
                .join(item.gradeCriteria, gradeCriteria)
                .where(itemCategoryEq(categoryParam.getItemCategoryCode()),
                        itemCodeEq(categoryParam.getItemCode()),
                        kindEq(categoryParam.getKindId()),
                        kindGradeEq(categoryParam.getKindGradeId()),
                        dateBetween(startDate, endDate),
                        userEq(userId),
                        orderStatus()
                )
                .fetchOne();
    }

    //
    public List<BuyerTotalPricePerPeriodDTO> findBuyerTotalPricePerPeriod(YearMonth startDate, YearMonth endDate, Long userId) {// 구매자의 기간별 결제 금액
        String sql = "select cast(ifNull(sum(op.price * op.count),0) AS SIGNED ) as totalPrice, month_year_tb.ym as date" +
                " from (select opp.price as price, opp.count as count, o.created_date as created_date from order_product opp join orders o on opp.order_id = o.order_id where opp.status = 'ORDER' and o.user_id =:userId) op" +
                " right outer join year_month_data" +
                " as month_year_tb on date_format(op.created_date,'%Y-%m') = month_year_tb.ym" +
                " where month_year_tb.ym>=:startDate and  month_year_tb.ym<=:endDate" +
                " group by month_year_tb.ym" +
                " order by month_year_tb.ym";

        Query query = em.createNativeQuery(sql)
                .setParameter("userId", userId)
                .setParameter("startDate", startDate.toString())
                .setParameter("endDate", endDate.toString());

        return jpaResultMapper.list(query, BuyerTotalPricePerPeriodDTO.class);
    }

    public List<SellerPricePerPeriodDTO> findAllSellerTotalPricePerPeriodAndCategory(YearMonth startDate, YearMonth endDate, CategoryParamDTO categoryParamDTO) {//기간별 판매액 비교(카테고리별 전체 판매자 기간별 총 금액)
/*

        String sql = "select cast(ifNull(sum(op.price * op.count),0) as SIGNED) as totalPrice, month_year_tb.ym as date" +
                " from (select opp.price as price, opp.count as count, o.created_date as created_date" +
                "    from order_product opp " +
                "    join orders o on opp.order_id = o.order_id " +
                "    join product p on opp.product_id = p.product_id" +
                "    join kind_grade kg on p.kind_grade_id = kg.kind_grade_id" +
                "    join kind k on kg.kind_id = k.kind_id" +
                "    join item i on k.item_code = i.item_code" +
                "    join item_category ic on i.item_category_code = ic.item_category_code" +
                "    where opp.status = 'ORDER' 카테고리_dynamicQuery ) op" +
                " right outer join  year_month_data" +
                " as month_year_tb on date_format(op.created_date,'%Y-%m') = month_year_tb.ym" +
                " where month_year_tb.ym>=:startDate and  month_year_tb.ym<=:endDate" +
                " group by month_year_tb.ym" +
                " order by month_year_tb.ym";
*/
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

/*        String sql = "select cast(ifNull(sum(op.price * op.count),0) as SIGNED) as totalPrice, month_year_tb.ym as date" +
                "from (select opp.price as price, opp.count as count, o.created_date as created_date " +
                "    from order_product opp " +
                "    join orders o on opp.order_id = o.order_id " +
                "    join product p on opp.product_id = p.product_id" +
                "    join user u on p.user_id = u.user_id" +
                "    join kind_grade kg on p.kind_grade_id = kg.kind_grade_id" +
                "    join kind k on kg.kind_id = k.kind_id" +
                "    join item i on k.item_code = i.item_code" +
                "    join item_category ic on i.item_category_code = ic.item_category_code" +
                "    where opp.status = 'ORDER' and u.user_id =:userId 카테고리_dynamicQuery ) op" +
                " right outer join  year_month_data" +
                " as month_year_tb on date_format(op.created_date,'%Y-%m') = month_year_tb.ym" +
                " where month_year_tb.ym>='2022-01' and  month_year_tb.ym<='2022-11'" +
                " group by month_year_tb.ym" +
                " order by month_year_tb.ym";
*/

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

    public List<SellerPercentileDTO> findSellerTotalPricePercentileByPeriodAndCategory(YearMonth startDate, YearMonth endDate, CategoryParamDTO categoryParamDTO, Long userId) {// 기간별 해당 판매자 판매액 백분위 그래프
/*

        select (1 - op.percent_ranking)*100 as percentile, ym as date
        from(
                select ym_seller.user_id,ifNull(sum(count*price),0) total_price, ym, PERCENT_RANK() OVER (partition by ym ORDER BY ifNull(sum(count*price),0) desc) AS percent_ranking
                from(
                        select p.user_id as user_id, op.price as price, op.count as count, date_format(o.created_date,'%Y-%m')as date
                        from order_product op
                        join orders o on op.order_id = o.order_id
                        join product p on op.product_id = p.product_id
                        join user u on p.user_id = u.user_id
                        join kind_grade kg on p.kind_grade_id = kg.kind_grade_id
                        join kind k on kg.kind_id = k.kind_id
                        join item i on k.item_code = i.item_code
                        join item_category ic on i.item_category_code = ic.item_category_code
                        where op.status = 'ORDER'
                ) op
                right join
                (
                      select ym, user_id from year_month_data
                      cross join user
                      where user.dtype='Seller' and '2022-07'<=ym and  ym <='2022-11'
                ) ym_seller
                on op.user_id=ym_seller.user_id and op.date = ym_seller.ym
                group by ym_seller.user_id, ym
                ) op
        where op.user_id = 2
        order by date;
*/

        StringBuilder sb = new StringBuilder();
        String sql = sb
                .append("select (1 - op.percent_ranking)*100 as percentile, ym as date ")
                .append(" from(")
                .append("   select ym_seller.user_id, ifNull(sum(count*price),0) as total_price, ym, PERCENT_RANK() OVER (partition by ym ORDER BY ifNull(sum(count*price),0) desc) AS percent_ranking")
                .append("   from(")
                .append("       select p.user_id as user_id, op.price as price, op.count as count, date_format(o.created_date,'%Y-%m')as date")
                .append("       from order_product op ")
                .append("           join orders o on op.order_id = o.order_id ")
                .append("           join product p on op.product_id = p.product_id")
                .append("           join user u on p.user_id = u.user_id")
                .append("           join kind_grade kg on p.kind_grade_id = kg.kind_grade_id")
                .append("           join kind k on kg.kind_id = k.kind_id")
                .append("           join item i on k.item_code = i.item_code")
                .append("           join item_category ic on i.item_category_code = ic.item_category_code")
                .append("       where op.status = 'ORDER'").append(categoryDynamic(categoryParamDTO))
                .append("   ) op")
                .append("   right join ")
                .append("   (")
                .append("       select ym, user_id from year_month_data")
                .append("       cross join user")
                .append("       where user.dtype='Seller' and :startDate <= ym and  ym <= :endDate")
                .append("   ) ym_seller")
                .append("   on op.user_id=ym_seller.user_id and op.date = ym_seller.ym")
                .append("   group by ym_seller.user_id, ym")
                .append("   ) op")
                .append(" where op.user_id =:userId")
                .append(" order by date")
                .toString();

        Query query = em.createNativeQuery(sql)
                .setParameter("startDate", startDate.toString())
                .setParameter("endDate", endDate.toString())
                .setParameter("userId", userId);
        return jpaResultMapper.list(query, SellerPercentileDTO.class);

    }

    public List<SellerOrderCountPerPeriodDTO> findAllSellerTotalCountPerPeriodAndCategory(YearMonth startDate, YearMonth endDate, CategoryParamDTO categoryParamDTO) {//기간별 판매횟수 비교(카테고리별 전체 판매자 기간별 총 판매횟수)

        /*
        String sql = "select count(op.order_product_id) as totalCount, month_year_tb.ym as date" +
                "from (select opp.order_product_id as order_product_id, o.created_date as created_date" +
                "   from order_product opp" +
                "   join orders o on opp.order_id = o.order_id " +
                "   join product p on opp.product_id = p.product_id" +
                "   join kind_grade kg on p.kind_grade_id = kg.kind_grade_id" +
                "   join kind k on kg.kind_id = k.kind_id" +
                "   join item i on k.item_code = i.item_code" +
                "   join item_category ic on i.item_category_code = ic.item_category_code" +
                "   where opp.status = 'ORDER'" +
                ") op" +
                "right outer join  year_month_data as month_year_tb " +
                "on date_format(op.created_date,'%Y-%m') = month_year_tb.ym" +
                "where '2022-05'<= month_year_tb.ym and  month_year_tb.ym<='2022-11'" +
                "group by month_year_tb.ym\n" +
                "order by month_year_tb.ym;";
        */

        StringBuilder sb = new StringBuilder();
        String sql = sb
                .append(" select count(op.order_product_id) as totalCount, month_year_tb.ym as date")
                .append(" from (select opp.order_product_id as order_product_id, o.created_date as created_date")
                .append("   from order_product opp")
                .append("   join orders o on opp.order_id = o.order_id ")
                .append("   join product p on opp.product_id = p.product_id")
                .append("   join kind_grade kg on p.kind_grade_id = kg.kind_grade_id")
                .append("   join kind k on kg.kind_id = k.kind_id")
                .append("   join item i on k.item_code = i.item_code")
                .append("   join item_category ic on i.item_category_code = ic.item_category_code")
                .append("   where opp.status = 'ORDER' ").append(categoryDynamic(categoryParamDTO))
                .append(" ) op")
                .append(" right outer join  year_month_data as month_year_tb")
                .append(" on date_format(op.created_date,'%Y-%m') = month_year_tb.ym")
                .append(" where :startDate <= month_year_tb.ym and  month_year_tb.ym <= :endDate")
                .append(" group by month_year_tb.ym")
                .append(" order by month_year_tb.ym")
                .toString();

        Query query = em.createNativeQuery(sql)
                .setParameter("startDate", startDate.toString())
                .setParameter("endDate", endDate.toString());

        return jpaResultMapper.list(query, SellerOrderCountPerPeriodDTO.class);
    }

    public List<SellerOrderCountPerPeriodDTO> findSellerTotalOrderCountPerPeriodAndCategory(YearMonth startDate, YearMonth endDate, CategoryParamDTO categoryParamDTO, Long userId) {//기간별 판매횟수 비교 그래프((카테고리별 특정 판매자 기간별 총 판매횟수)
/*

        String sql = "select count(op.order_product_id) as totalCount, month_year_tb.ym as date" +
                "from (select opp.order_product_id as order_product_id, o.created_date as created_date" +
                "   from order_product opp" +
                "   join orders o on opp.order_id = o.order_id " +
                "   join product p on opp.product_id = p.product_id" +
                "   join user u on p.user_id = u.user_id" +
                "   join kind_grade kg on p.kind_grade_id = kg.kind_grade_id" +
                "   join kind k on kg.kind_id = k.kind_id" +
                "   join item i on k.item_code = i.item_code" +
                "   join item_category ic on i.item_category_code = ic.item_category_code" +
                "   where opp.status = 'ORDER' and u.user_id =4" +
                ") op" +
                "right outer join  year_month_data as month_year_tb " +
                "on date_format(op.created_date,'%Y-%m') = month_year_tb.ym" +
                "where '2022-05'<= month_year_tb.ym and  month_year_tb.ym<='2022-11'" +
                "group by month_year_tb.ym" +
                "order by month_year_tb.ym;";
*/

        StringBuilder sb = new StringBuilder();
        String sql = sb
                .append("select count(op.order_product_id) as totalCount, month_year_tb.ym as date")
                .append(" from (select opp.order_product_id as order_product_id, o.created_date as created_date")
                .append("   from order_product opp")
                .append("   join orders o on opp.order_id = o.order_id ")
                .append("   join product p on opp.product_id = p.product_id")
                .append("   join user u on p.user_id = u.user_id")
                .append("   join kind_grade kg on p.kind_grade_id = kg.kind_grade_id")
                .append("   join kind k on kg.kind_id = k.kind_id")
                .append("   join item i on k.item_code = i.item_code")
                .append("   join item_category ic on i.item_category_code = ic.item_category_code")
                .append("   where opp.status = 'ORDER' and u.user_id = :userId").append(categoryDynamic(categoryParamDTO))
                .append(" ) op")
                .append(" right outer join  year_month_data as month_year_tb ")
                .append(" on date_format(op.created_date,'%Y-%m') = month_year_tb.ym")
                .append(" where :startDate <= month_year_tb.ym and month_year_tb.ym <= :endDate")
                .append(" group by month_year_tb.ym")
                .append(" order by month_year_tb.ym")
                .toString();

        Query query = em.createNativeQuery(sql)
                .setParameter("startDate", startDate.toString())
                .setParameter("endDate", endDate.toString())
                .setParameter("userId", userId);

        return jpaResultMapper.list(query, SellerOrderCountPerPeriodDTO.class);
    }


    public List<SellerCountByPeriodDTO> findSellerCountOrderProductExistByPeriod(YearMonth startDate, YearMonth endDate, CategoryParamDTO categoryParamDTO) { // 기간별,카테고리별 판매 기록이 있는 판매자 개수 count
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
                .append(" select ym as date, ifnull(count,0) count")
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

        Query query = em.createNativeQuery(sql)
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

    public List<BuyerOrderPerPeriodDTO> findBuyerOrderPerPeriod(LocalDateTime startDate, LocalDateTime endDate, Long userId, int offset, int pageSize) {
        return queryFactory
                .select(new QBuyerOrderPerPeriodDTO(order.id, product.id, orderProduct.id, order.createdDate, product.name, orderProduct.count, orderProduct.price, productImage.path, orderProduct.status.stringValue()))
                .from(orderProduct)
                .join(orderProduct.order, order)
                .join(order.user, user)
                .join(orderProduct.product, product)
                .join(product.productImages, productImage)
                .where(dateBetween(startDate, endDate), userEq(userId), productImageType())
                .orderBy(order.createdDate.asc())
                .offset(offset)
                .limit(pageSize)
                .fetch();
    }

    public Long findBuyerOrderPerPeriodTotalCount(LocalDateTime startDate, LocalDateTime endDate, Long userId) {
        return queryFactory
                .select(order.countDistinct())
                .from(orderProduct)
                .join(orderProduct.order, order)
                .join(order.user, user)
                .join(orderProduct.product, product)
                .join(product.productImages, productImage)
                .where(dateBetween(startDate, endDate), userEq(userId), productImageType())
                .fetchOne();
    }

    public List<SaleHistoryRes> findSaleHistoryPerPeriod(LocalDateTime startDate, LocalDateTime endDate, Long userId, int offset, int limit) {
        return queryFactory
                .select(new QSaleHistoryRes(product.id, orderProduct.count, orderProduct.price, product.name, order.createdDate, order.address.jibun, order.address.road,order.address.detailAddress, order.address.zipcode, user.phoneNumber, productImage.path))
                .from(orderProduct)
                .join(orderProduct.product, product)
                .join(orderProduct.order, order)
                .join(order.user, user)
                .join(product.productImages, productImage)
                .where(dateBetween(startDate, endDate), orderStatus(), product.user.id.eq(userId), productImageType())
                .orderBy(order.createdDate.asc())
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    public Long findSaleHistoryPerPeriodCount(LocalDateTime startDate, LocalDateTime endDate, Long userId) {
        return queryFactory
                .select(orderProduct.count())
                .from(orderProduct)
                .join(orderProduct.product, product)
                .join(orderProduct.order, order)
                .where(dateBetween(startDate, endDate), orderStatus(), product.user.id.eq(userId))
                .fetchOne();
    }

    private NumberExpression<Long> getTotalPrice() {// orderProduct.count * orderProduct.price
        return orderProduct.price.multiply(orderProduct.count).sum().longValue();
    }

    private BooleanExpression kindGradeEq(Long kindGradeId) {
        return kindGradeId != null ? kindGrade.id.eq(kindGradeId) : null;
    }

    private BooleanExpression kindEq(Long kindId) {
        return kindId != null ? kind.id.eq(kindId) : null;
    }

    private BooleanExpression itemCodeEq(Integer itemCode) {
        return itemCode != null ? item.itemCode.eq(itemCode) : null;
    }

    private BooleanExpression itemCategoryEq(Integer itemCategoryCode) {
        return itemCategoryCode != null ? itemCategory.itemCategoryCode.eq(itemCategoryCode) : null;
    }

    private BooleanExpression dateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return startDate != null && endDate != null ? order.createdDate.between(startDate, endDate) : null;
    }

    private BooleanExpression userEq(Long userId) {
        return userId != null ? user.id.eq(userId) : null;
    }

    private BooleanExpression orderStatus() {
        return orderProduct.status.eq(OrderStatus.ORDER);
    }

    private BooleanExpression productImageType() {
        return productImage.type.eq(ProductImageType.SIGNATURE);
    }
}
