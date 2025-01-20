package kr.hhplus.be.server.domain.exception.concert.seat;

import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.common.exception.ErrorCode;

public class ConcertSeatNotFoundException extends BusinessException {
    public ConcertSeatNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
