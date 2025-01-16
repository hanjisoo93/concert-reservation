package kr.hhplus.be.server.domain.exception.point;

import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.common.exception.ErrorCode;

public class PointHistoryException extends BusinessException {
    public PointHistoryException(ErrorCode errorCode) {
        super(errorCode);
    }
}
