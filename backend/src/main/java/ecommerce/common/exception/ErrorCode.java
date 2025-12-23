package ecommerce.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400 Bad Request
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "잘못된 요청입니다"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "입력값 검증 실패"),
    INVALID_QUERY_PARAM(HttpStatus.BAD_REQUEST, "INVALID_QUERY_PARAM", "쿼리 파라미터가 잘못되었습니다"),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "유효하지 않은 입력값입니다"),

    // 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "토큰이 만료되었습니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 토큰입니다"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다"),

    // 403 Forbidden
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "해당 리소스에 접근할 권한이 없습니다"),

    // 404 Not Found
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "리소스를 찾을 수 없습니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다"),
    SELLER_NOT_FOUND(HttpStatus.NOT_FOUND, "SELLER_NOT_FOUND", "판매자를 찾을 수 없습니다"),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다"),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "카테고리를 찾을 수 없습니다"),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "주문을 찾을 수 없습니다"),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_NOT_FOUND", "리뷰를 찾을 수 없습니다"),
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "COUPON_NOT_FOUND", "쿠폰을 찾을 수 없습니다"),
    VOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "VOTE_NOT_FOUND", "투표를 찾을 수 없습니다"),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION_NOT_FOUND", "알림을 찾을 수 없습니다"),

    // 409 Conflict
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "DUPLICATE_RESOURCE", "중복된 리소스입니다"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다"),
    DUPLICATE_BUSINESS_NUMBER(HttpStatus.CONFLICT, "DUPLICATE_BUSINESS_NUMBER", "이미 등록된 사업자 번호입니다"),
    DUPLICATE_VOTE(HttpStatus.CONFLICT, "DUPLICATE_VOTE", "이미 투표하셨습니다"),
    DUPLICATE_NOTIFICATION(HttpStatus.CONFLICT, "DUPLICATE_NOTIFICATION", "이미 알림 신청하셨습니다"),
    DUPLICATE_NOTIFICATION_REQUEST(HttpStatus.CONFLICT, "DUPLICATE_NOTIFICATION_REQUEST", "이미 재입고 알림을 신청하셨습니다"),
    STATE_CONFLICT(HttpStatus.CONFLICT, "STATE_CONFLICT", "리소스 상태 충돌"),
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK", "재고가 부족합니다"),

    // 422 Unprocessable Entity
    UNPROCESSABLE_ENTITY(HttpStatus.UNPROCESSABLE_ENTITY, "UNPROCESSABLE_ENTITY", "처리할 수 없는 요청"),
    INVALID_ORDER_STATUS(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_ORDER_STATUS", "주문 상태가 올바르지 않습니다"),

    // 429 Too Many Requests
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS", "요청 한도 초과"),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류"),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DATABASE_ERROR", "데이터베이스 오류"),
    EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "EXTERNAL_API_ERROR", "외부 API 호출 실패"),
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "UNKNOWN_ERROR", "알 수 없는 오류");

    private final HttpStatus status;
    private final String code;
    private final String message;

    public int getStatusValue() {
        return status.value();
    }
}