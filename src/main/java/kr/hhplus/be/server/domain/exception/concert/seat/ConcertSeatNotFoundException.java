package kr.hhplus.be.server.domain.exception.concert.seat;

import kr.hhplus.be.server.common.exception.ErrorCode;

public class ConcertSeatNotFoundException extends IllegalArgumentException{
    public ConcertSeatNotFoundException() {
        super(ErrorCode.SEAT_NOT_FOUND.getMessage());
    }
}
