package kr.hhplus.be.server.domain.token.repository;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.token.entity.Token;
import kr.hhplus.be.server.domain.token.entity.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    Token findAllByUserIdAndStatus(Long userId, TokenStatus status);

    // 가장 최근에 생성된 토큰 조회
    @Query("SELECT t FROM Token t WHERE t.userId = :userId ORDER BY t.createdAt DESC")
    Optional<Token> findFirstByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Token t WHERE t.userId = :userId and t.status = :status ORDER BY t.createdAt DESC")
    Optional<Token> findFirstByUserIdAndStatusForUpdate(@Param("userId") Long userId, @Param("status") TokenStatus status);

    int countByStatus(TokenStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Token> findTop100ByStatusOrderByCreatedAtAsc(TokenStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Token> findAllByExpiredAtBeforeAndStatus(LocalDateTime expiredAt, TokenStatus status);

    @Lock(LockModeType.NONE)
    @Query("""
    SELECT t FROM Token t 
    WHERE t.userId = :userId 
      AND t.status IN :statuses 
      AND t.expiredAt > :now
    ORDER BY t.createdAt DESC
    """)
    Optional<Token> findFirstByUserIdAndStatusAndNotExpired(
            @Param("userId") Long userId,
            @Param("statuses") List<TokenStatus> statuses,
            @Param("now") LocalDateTime now
    );

    Optional<Token> findByUuid(String tokenUuid);
}
