package kr.hhplus.be.server.common.exception;

public class BusinessException extends BaseException{
    public BusinessException(ErrorCode errorCode){
        super(errorCode);
    }
}
