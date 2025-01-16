package kr.hhplus.be.server.domain.exception.point;

import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.common.exception.ErrorCode;

public class PointException extends BusinessException {
    public PointException(ErrorCode errorCode) {
        super(errorCode);
    }
}
