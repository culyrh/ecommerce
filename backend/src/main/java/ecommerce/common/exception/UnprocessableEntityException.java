package ecommerce.common.exception;

public class UnprocessableEntityException extends BusinessException {
    public UnprocessableEntityException(ErrorCode externalApiError, String message) {
        super(ErrorCode.UNPROCESSABLE_ENTITY, message);
    }

    public UnprocessableEntityException(ErrorCode errorCode) {
        super(errorCode);
    }
}