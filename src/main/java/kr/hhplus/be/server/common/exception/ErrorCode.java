package kr.hhplus.be.server.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    /**
     * 비즈니스 예외
     */

    // 토큰
    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "유효한 토큰을 찾을 수 없습니다."),
    INVALID_TOKEN_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 상태입니다."),
    INVALID_TOKEN_EXPIRED_AT(HttpStatus.BAD_REQUEST, "유효하지 않은 만료 시간입니다."),

    // 콘서트 좌석
    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하는 좌석이 없습니다."),
    SEAT_ALREADY_RESERVED(HttpStatus.BAD_REQUEST, "해당 좌석은 이미 예약되었습니다."),

    // 예약
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약 정보를 찾을 수 없습니다."),
    RESERVATION_EXPIRED(HttpStatus.BAD_REQUEST, "예약 요청 가능한 시간이 만료되었습니다."),
    INVALID_RESERVATION_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 예약 상태입니다."),
    INVALID_RESERVATION_EXPIRED_AT(HttpStatus.BAD_REQUEST, "유효하지 않은 만료 시간입니다."),

    // 포인트
    POINT_NOT_FOUND(HttpStatus.NOT_FOUND, "포인트 정보가 존재하지 않습니다."),
    INSUFFICIENT_POINT(HttpStatus.BAD_REQUEST, "포인트가 부족합니다."),
    INVALID_POINT_USAGE(HttpStatus.BAD_REQUEST, "사용할 포인트는 1 이상이어야 합니다."),
    INVALID_POINT_CHARGE(HttpStatus.BAD_REQUEST, "충전할 포인트는 1 이상이어야 합니다."),

    // 결제
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "결제 금액은 0보다 커야 합니다."),


    /**
     * 시스템 예외
     */
    SYSTEM_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "시스템 오류가 발생했습니다."),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}
