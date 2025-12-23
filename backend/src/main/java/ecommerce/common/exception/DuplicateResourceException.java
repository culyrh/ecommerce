package ecommerce.common.exception;

public class DuplicateResourceException extends BusinessException {
    public DuplicateResourceException(String message) {
        super(ErrorCode.DUPLICATE_RESOURCE, message);
    }

    public DuplicateResourceException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DuplicateResourceException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}