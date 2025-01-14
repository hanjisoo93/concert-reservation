package kr.hhplus.be.server.domain.entity.reservation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationStatus {

    PENDING("예약 중"),
    SUCCESS("성공"),
    FAILED("실패");

    private final String text;
}
