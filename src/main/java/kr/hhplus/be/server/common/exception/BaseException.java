package kr.hhplus.be.server.common.exception;

import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException{
    private final ErrorCode errorCode;

    protected BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
