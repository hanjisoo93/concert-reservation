package kr.hhplus.be.server.domain.exception.concert.seat;

public class ConcertSeatNotFoundException extends RuntimeException{
    public ConcertSeatNotFoundException(String message) {
        super(message);
    }
}
