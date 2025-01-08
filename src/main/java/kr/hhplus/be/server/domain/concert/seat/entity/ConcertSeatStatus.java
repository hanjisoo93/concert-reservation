package kr.hhplus.be.server.domain.concert.seat.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConcertSeatStatus {

    AVAILABLE("예약 가능"),
    RESERVED("예약 중"),
    CONFIRMED("예약 완료");

    private final String text;
}
