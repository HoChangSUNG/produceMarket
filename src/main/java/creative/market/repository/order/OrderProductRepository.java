package creative.market.repository.order;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import creative.market.domain.order.OrderProduct;
import creative.market.domain.order.OrderStatus;

import creative.market.repository.dto.CategoryParamDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static creative.market.domain.category.QGradeCriteria.gradeCriteria;
import static creative.market.domain.category.QItem.item;
import static creative.market.domain.category.QItemCategory.itemCategory;
import static creative.market.domain.category.QKind.kind;
import static creative.market.domain.category.QKindGrade.kindGrade;
import static creative.market.domain.order.QOrder.*;
import static creative.market.domain.order.QOrderProduct.*;
import static creative.market.domain.product.QProduct.product;
import static creative.market.domain.user.QUser.*;

@Repository
@RequiredArgsConstructor
public class OrderProductRepository {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    public Optional<OrderProduct> findById(Long orderProductId) {
        return Optional.ofNullable(
                em.find(OrderProduct.class, orderProductId)
        );
    }

    public Optional<OrderProduct> findByIdWithOrder(Long orderProductId) {
        return Optional.ofNullable(
                queryFactory.selectFrom(orderProduct)
                        .join(orderProduct.order).fetchJoin()
                        .where(orderProduct.id.eq(orderProductId))
                        .fetchOne()
        );
    }

    public List<OrderProduct> findByProductIdAndUserId(Long productId, Long userId) {
        return queryFactory
                .selectFrom(orderProduct)
                .join(orderProduct.order, order).fetchJoin()
                .where(orderProduct.product.id.eq(productId).and(order.user.id.eq(userId)))
                .fetch();
    }

    public Long findCategoryOrderTotalPriceSum(CategoryParamDTO categoryParam, LocalDateTime startDate, LocalDateTime endDate) { //기간별 카테고리 총 판매액
        return queryFactory.select(getTotalPriceSum().coalesce(0L))
                .from(orderProduct)
                .join(orderProduct.order, order)
                .join(orderProduct.product, product)
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
                        orderStatus()
                )
                .fetchOne();
    }

    public Long findOrderProductCountBySeller(Long userId) { // 특정 판매자가 판매한 총 판매 내역 개수
        return queryFactory.select(orderProduct.count())
                .from(orderProduct)
                .join(orderProduct.product, product)
                .join(product.user, user)
                .where(user.id.eq(userId))
                .fetchOne();
    }

    private NumberExpression<Integer> getTotalPrice() {
        return orderProduct.price.multiply(orderProduct.count);
    }

    private NumberExpression<Long> getTotalPriceSum() {
        return getTotalPrice().sum().longValue();
    }

    private NumberExpression<Long> getTotalPriceAvg() {
        return getTotalPrice().avg().longValue();
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

    private BooleanExpression orderStatus() {
        return orderProduct.status.eq(OrderStatus.ORDER);
    }
}
