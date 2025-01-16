package kr.hhplus.be.server.domain.exception.token;

import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.common.exception.ErrorCode;

public class TokenException extends BusinessException {
    public TokenException(ErrorCode errorCode){super(errorCode);}
}
