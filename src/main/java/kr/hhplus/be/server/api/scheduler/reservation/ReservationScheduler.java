package kr.hhplus.be.server.api.scheduler.reservation;

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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ReservationScheduler {

    private final ReservationRepository reservationRepository;
    private final ConcertSeatRepository concertSeatRepository;
    private final TokenRepository tokenRepository;

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    @Transactional
    public void processExpiredReservations() {
        // 1. 만료된 예약 조회
        List<Reservation> expiredReservations  = reservationRepository.findAllByExpiredAtBeforeAndStatusWithLock(LocalDateTime.now(), ReservationStatus.PENDING);

        for(Reservation reservation : expiredReservations) {
            // 2. 예약 상태 실패 처리
            reservation.updateStatus(ReservationStatus.FAILED);

            // 3. 좌석 상태 복원
            ConcertSeat concertSeat = concertSeatRepository.findByIdForUpdate(reservation.getSeatId());
            if(concertSeat == null) {
                throw new IllegalArgumentException("좌석을 찾을 수 없습니다.");
            }
            concertSeat.updateStatus(ConcertSeatStatus.AVAILABLE);

            // 4. 토큰 만료 처리
            Optional<Token> optionalToken = tokenRepository.findFirstByUserIdAndStatusForUpdate(reservation.getUserId(), TokenStatus.ACTIVE);
            Token token = optionalToken.orElseThrow(() -> new IllegalStateException("유효한 토큰이 없습니다."));

            if(token != null) {
                token.expireToken();
            }

            // 5. 변경 사항 저장
            reservationRepository.save(reservation);
            concertSeatRepository.save(concertSeat);
            if(token != null) {
                tokenRepository.save(token);
            }
        }
    }
}
