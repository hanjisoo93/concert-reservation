package kr.hhplus.be.server.domain.token.repository;

import kr.hhplus.be.server.domain.token.entity.Token;
import kr.hhplus.be.server.domain.token.entity.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    Token findAllByUserIdAndStatus(Long userId, TokenStatus status);

}
