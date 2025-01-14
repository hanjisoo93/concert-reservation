package kr.hhplus.be.server.common.exception;

public class BaseException extends RuntimeException{
    private final String errorCode;

    public BaseException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
