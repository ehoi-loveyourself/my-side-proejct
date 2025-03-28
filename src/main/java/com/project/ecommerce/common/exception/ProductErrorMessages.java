package com.project.ecommerce.common.exception;

public class ProductErrorMessages {
    public static final String NOT_FOUND_PRODUCT = "해당 상품을 찾을 수 없습니다.";
    public static final String CANNOT_UPDATE_PRODUCT = "해당 상품을 수정할 수 없습니다.";
    public static final String INVALID_PRICE = "가격은 100원 이상이어야 합니다";
    public static final String NO_AUTHORIZATION = "권한이 없습니다.";
    public static final String STOCK_MUST_MORE_THAN_ZERO = "재고 수량은 0 이상이어야 합니다.";
    public static final String CANNOT_UPDATE_PRODUCT_WITH_SAME_STOCK = "기존 재고 수량과 같습니다.";
    public static final String REQUIRED_PRODUCT_ID = "상품을 선택하세요";
}