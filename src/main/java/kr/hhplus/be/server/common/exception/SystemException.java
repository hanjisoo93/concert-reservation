package kr.hhplus.be.server.common.exception;

public class SystemException extends BaseException{
    public SystemException(ErrorCode errorCode){
        super(errorCode);
    }
}
