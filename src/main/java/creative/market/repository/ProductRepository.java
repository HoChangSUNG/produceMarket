package creative.market.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import creative.market.domain.order.OrderStatus;
import creative.market.domain.order.QOrderProduct;
import creative.market.domain.product.Product;
import creative.market.domain.product.ProductStatus;
import creative.market.repository.dto.ProductSearchConditionReq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static creative.market.domain.QReview.*;
import static creative.market.domain.category.QGrade.*;
import static creative.market.domain.category.QGradeCriteria.*;
import static creative.market.domain.category.QItem.*;
import static creative.market.domain.category.QItemCategory.*;
import static creative.market.domain.category.QKind.*;
import static creative.market.domain.category.QKindGrade.*;
import static creative.market.domain.order.QOrder.order;
import static creative.market.domain.order.QOrderProduct.*;
import static creative.market.domain.product.QProduct.*;
import static creative.market.domain.user.QUser.*;

@Repository
@RequiredArgsConstructor
public class ProductRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public Long save(Product product) {
        em.persist(product);
        return product.getId();
    }

    public List<Product> findAll() {
        return queryFactory.selectFrom(product)
                .where(productExistCheck())
                .fetch();
    }

    public List<Product> findProductListOrderByCreatedDateDesc(int limit) {
        return queryFactory.selectFrom(product)
                .orderBy(product.createdDate.desc(), product.name.asc())
                .limit(limit)
                .fetch();
    }

    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(
                queryFactory.selectFrom(product)
                        .where(productExistCheck(), product.id.eq(id))
                        .fetchOne());
    }

    public List<Product> findProductByCondition(ProductSearchConditionReq condition, int offset, int limit) { // 조건에 따라 상품 리스트 조회
        return queryFactory.select(product)
                .from(orderProduct)
                .rightJoin(orderProduct.product, product)
                .join(product.kindGrade, kindGrade)
                .join(kindGrade.kind, kind)
                .join(kind.item, item)
                .join(item.itemCategory, itemCategory)
                .join(item.gradeCriteria, gradeCriteria)
                .where(productNameContains(condition.getProductName()),
                        itemCategoryEq(condition.getItemCategoryCode()),
                        itemCodeEq(condition.getItemCode()),
                        kindEq(condition.getKindId()),
                        kindGradeEq(condition.getKindGradeId()),
                        productExistCheck())
                .orderBy(orderConditions(condition.getOrderBy()))
                .groupBy(product)
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    public Long findProductByConditionTotalCount(ProductSearchConditionReq condition) { // 조건에 따라 상품 리스트 조회 count
        return queryFactory.select(product.count())
                .from(product)
                .join(product.kindGrade, kindGrade)
                .join(kindGrade.kind, kind)
                .join(kind.item, item)
                .join(item.itemCategory, itemCategory)
                .join(item.gradeCriteria, gradeCriteria)
                .where(productNameContains(condition.getProductName()),
                        itemCategoryEq(condition.getItemCategoryCode()),
                        itemCodeEq(condition.getItemCode()),
                        kindEq(condition.getKindId()),
                        kindGradeEq(condition.getKindGradeId()),
                        productExistCheck())
                .fetchOne();
    }

    public Optional<Product> findByIdFetchJoinSellerAndKind(Long productId) {
        return Optional.ofNullable(
                queryFactory.selectFrom(product)
                        .join(product.kindGrade, kindGrade).fetchJoin()
                        .join(kindGrade.kind, kind).fetchJoin()
                        .join(product.user, user).fetchJoin()
                        .where(product.id.eq(productId),
                                productExistCheck())
                        .fetchOne());
    }

    public Optional<Product> findByIdAndSellerId(Long productId, Long userId) {
        return Optional.ofNullable(
                queryFactory.selectFrom(product)
                        .join(product.user, user)
                        .where(product.id.eq(productId), user.id.eq(userId))
                        .fetchOne()
        );
    }

    public Optional<Product> findByIdFetchJoinSeller(Long productId) {
        return Optional.ofNullable(
                queryFactory.selectFrom(product)
                        .join(product.user, user).fetchJoin()
                        .where(product.id.eq(productId))
                        .fetchOne()
        );
    }

    public Optional<Product> findByIdFetchJoinItemCategory(Long productId) {
        return Optional.ofNullable(
                queryFactory.selectFrom(product)
                        .join(product.kindGrade, kindGrade).fetchJoin()
                        .join(kindGrade.grade, grade)
                        .join(kindGrade.kind, kind).fetchJoin()
                        .join(kind.item, item).fetchJoin()
                        .join(item.itemCategory, itemCategory)
                        .where(product.id.eq(productId), productExistCheck())
                        .fetchOne()
        );
    }

    public Double findProductAvgPrice(Long kindGradeId) { // 상품 평균 가격
        return queryFactory.select(product.price.avg().coalesce(0D))
                .from(product)
                .join(product.kindGrade, kindGrade)
                .where(kindGrade.id.eq(kindGradeId))
                .fetchOne();
    }

    public List<Long> findProductIdByOrderCountDesc(int offset, int limit, LocalDateTime startDate, LocalDateTime endDate) {
        // 전체 카테고리에서 판매횟수 내림차순 정렬 + 상품 등록 날짜 내림차순 정렬 ->  조건에 맞는 productId 리스트 리턴
        return queryFactory.select(product.id)
                .from(orderProduct)
                .join(orderProduct.product, product)
                .join(orderProduct.order, order)
                .where(orderCreatedDateBetween(startDate, endDate),
                        productExistCheck(),
                        orderStatus())
                .groupBy(product)
                .orderBy(orderProduct.count().desc(), product.createdDate.desc())
                .limit(limit)
                .offset(offset)
                .fetch();
    }

    public List<Long> findProductIdByReviewCountDesc(int offset, int limit, LocalDateTime startDate, LocalDateTime endDate) {
        // 전체 카테고리에서 별점 평균 내림차순 정렬 + 리뷰 개수 내림차순 정렬 + 상품 등록 날짜 내림차순 정렬 같으면 조건에 맞는 productId 리스트 리턴
        return queryFactory.select(product.id)
                .from(review)
                .join(review.product, product)
                .where(
                        reviewCreatedDateBetween(startDate, endDate),
                        productExistCheck())
                .groupBy(product.id)
                .orderBy(review.rate.avg().desc(), review.count().desc(), product.createdDate.desc())
                .limit(limit)
                .offset(offset)
                .fetch();
    }

    public List<Product> findByUserId(Long userId, int offset, int limit) {
        return queryFactory
                .selectFrom(product)
                .join(product.kindGrade, kindGrade).fetchJoin()
                .join(kindGrade.kind, kind).fetchJoin()
                .join(kind.item, item).fetchJoin()
                .where(productExistCheck(), product.user.id.eq(userId))
                .offset(offset)
                .limit(limit)
                .orderBy(product.createdDate.desc())
                .fetch();
    }

    public Long findByUserIdCount(Long userId) {
        return queryFactory
                .select(product.count())
                .from(product)
                .where(productExistCheck(), product.user.id.eq(userId))
                .fetchOne();
    }

    private BooleanExpression orderCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return startDate != null && endDate != null ? order.createdDate.between(startDate, endDate) : null;
    }

    private BooleanExpression reviewCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return startDate != null && endDate != null ? review.createdDate.between(startDate, endDate) : null;
    }

    private OrderSpecifier[] orderConditions(String orderBy) {
        List<OrderSpecifier> orderByList = new ArrayList<>();

        if (!StringUtils.hasText(orderBy) || orderBy.equals("latest")) { // 최신순
            orderByList.add(product.createdDate.desc());
            orderByList.add(product.name.asc());
        } else if (orderBy.equals("price")) {//가격 낮은 순
            orderByList.add(product.price.asc());
            orderByList.add(product.createdDate.desc());
        } else if (orderBy.equals("order-count")) { //판매 횟수 순
            orderByList.add(orderProduct.count().desc());
            orderByList.add(product.price.asc());
            orderByList.add(product.createdDate.desc());
        } else if (orderBy.equals("total-order-price")) { // 판매 금액 순
            orderByList.add(orderProduct.count.multiply(orderProduct.price).sum().coalesce(0).desc());
            orderByList.add(product.price.asc());
            orderByList.add(product.createdDate.desc());
        } else {
            orderByList.add(product.id.asc());
        }

        return orderByList.toArray(OrderSpecifier[]::new);

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

    private BooleanExpression productNameContains(String productName) {
        return StringUtils.hasText(productName) ? product.name.contains(productName) : null;
    }

    private BooleanExpression productExistCheck() {
        return product.status.eq(ProductStatus.EXIST);
    }

    private BooleanExpression orderStatus() {
        return orderProduct.status.eq(OrderStatus.ORDER);
    }

}
