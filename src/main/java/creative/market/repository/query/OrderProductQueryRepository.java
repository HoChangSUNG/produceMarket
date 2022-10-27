package creative.market.repository.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import creative.market.repository.dto.CategoryParamDTO;
import creative.market.repository.dto.QSellerAndTotalPricePerCategoryDTO;
import creative.market.repository.dto.SellerAndTotalPricePerCategoryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static creative.market.domain.category.QGradeCriteria.gradeCriteria;
import static creative.market.domain.category.QItem.item;
import static creative.market.domain.category.QItemCategory.itemCategory;
import static creative.market.domain.category.QKind.kind;
import static creative.market.domain.category.QKindGrade.kindGrade;
import static creative.market.domain.order.QOrder.order;
import static creative.market.domain.order.QOrderProduct.orderProduct;
import static creative.market.domain.product.QProduct.product;
import static creative.market.domain.user.QUser.user;

@Repository
@RequiredArgsConstructor
public class OrderProductQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<SellerAndTotalPricePerCategoryDTO> findCategoryTopRankSellerNameAndPrice(CategoryParamDTO categoryParam, LocalDateTime startDate, LocalDateTime endDate, int rankCount) {// 카테고리별 판매 상위 판매자 및 판매 가격 조회
        return queryFactory.select(new QSellerAndTotalPricePerCategoryDTO(user.name, getTotalPrice().longValue()))
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
                        dateBetween(startDate, endDate))
                .orderBy(getTotalPrice().desc())
                .groupBy(user)
                .offset(0)
                .limit(rankCount)
                .fetch();
    }

    public SellerAndTotalPricePerCategoryDTO findCategorySellerNameAndPrice(CategoryParamDTO categoryParam, LocalDateTime startDate, LocalDateTime endDate, Long userId) {//선택된 카테고리 판매 내역에서 판매자의 이름 및 판매 가격 조회
        return queryFactory.select(new QSellerAndTotalPricePerCategoryDTO(user.name, getTotalPrice().longValue()))
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
                                userEq(userId))
                        .fetchOne();
    }

    private NumberExpression<Integer> getTotalPrice() {
        return orderProduct.price.multiply(orderProduct.count).sum();
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
}
