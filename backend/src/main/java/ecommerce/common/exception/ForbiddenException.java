package ecommerce.common.exception;

public class ForbiddenException extends BusinessException {
    public ForbiddenException(ErrorCode accessDenied, String message) {
        super(ErrorCode.FORBIDDEN, message);
    }

    public ForbiddenException(ErrorCode errorCode) {
        super(errorCode);
    }
}