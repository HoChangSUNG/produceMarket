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

    public List<BuyerOrderPerPeriodDTO> findBuyerOrderPerPeriod(LocalDateTime startDate, LocalDateTime endDate, Long userId, int offset, int limit) {
        return queryFactory
                .select(new QBuyerOrderPerPeriodDTO(order.id, product.id, order.createdDate, product.name, orderProduct.count, orderProduct.price, productImage.path, product.status.stringValue()))
                .from(orderProduct)
                .join(orderProduct.order, order)
                .join(order.user, user)
                .join(orderProduct.product, product)
                .join(product.productImages, productImage)
                .where(dateBetween(startDate, endDate), userEq(userId), productImage.type.eq(ProductImageType.SIGNATURE))
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    public Long findBuyerOrderPerPeriodTotalCount(LocalDateTime startDate, LocalDateTime endDate, Long userId) {
        return queryFactory
                .select(orderProduct.count())
                .from(orderProduct)
                .join(orderProduct.order, order)
                .join(order.user, user)
                .join(orderProduct.product, product)
                .join(product.productImages, productImage)
                .where(dateBetween(startDate, endDate), userEq(userId), productImage.type.eq(ProductImageType.SIGNATURE))
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
}
