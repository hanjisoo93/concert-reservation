package kr.hhplus.be.server.api.facade.reservation;

import kr.hhplus.be.server.domain.concert.seat.entity.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.entity.ConcertSeatStatus;
import kr.hhplus.be.server.domain.concert.seat.repository.ConcertSeatRepository;
import kr.hhplus.be.server.domain.reservation.entity.Reservation;
import kr.hhplus.be.server.domain.reservation.entity.ReservationStatus;
import kr.hhplus.be.server.domain.reservation.repository.ReservationRepository;
import kr.hhplus.be.server.domain.token.entity.Token;
import kr.hhplus.be.server.domain.token.entity.TokenStatus;
import kr.hhplus.be.server.domain.token.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ReservationFacade {

    private final ReservationRepository reservationRepository;
    private final ConcertSeatRepository concertSeatRepository;
    private final TokenRepository tokenRepository;

    @Transactional
    public Reservation createReservation(Long seatId, Long userId) {
        // 1. 좌석 상태 확인
        ConcertSeat seat = concertSeatRepository.findByIdForUpdate(seatId);
        if(seat == null) {
            throw new IllegalArgumentException("좌석을 찾을 수 없습니다.");
        }

        if(ConcertSeatStatus.RESERVED.equals(seat.getStatus())){
            throw new IllegalStateException("이미 예약 중인 좌석입니다.");
        }

        // 2. 유효한 토큰 확인
        Optional<Token> optionalToken = tokenRepository.findFirstByUserIdAndStatusForUpdate(userId, TokenStatus.ACTIVE);
        Token token = optionalToken.orElseThrow(() -> new IllegalStateException("유효한 토큰이 없습니다."));

        // 3. 예약 생성
        // 3-1. 좌석 상태 수정
        seat.updateStatus(ConcertSeatStatus.RESERVED);
        concertSeatRepository.save(seat);

        // 3-2. 토큰 만료 시간 연장
        token.updateExpiredAt(LocalDateTime.now().plusMinutes(30));
        tokenRepository.save(token);

        // 3-3. 예약 등록
        Reservation reservation = Reservation.createReservation(userId, seatId);
        reservationRepository.save(reservation);

        return reservation;
    }
}
