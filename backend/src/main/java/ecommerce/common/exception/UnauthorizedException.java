package ecommerce.common.exception;

public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(ErrorCode forbidden, String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }

    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }
}