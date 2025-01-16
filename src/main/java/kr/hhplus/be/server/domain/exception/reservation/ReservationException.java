package kr.hhplus.be.server.domain.exception.reservation;

import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.common.exception.ErrorCode;

public class ReservationException extends BusinessException {
    public ReservationException(ErrorCode errorCode){
        super(errorCode);
    }
}
