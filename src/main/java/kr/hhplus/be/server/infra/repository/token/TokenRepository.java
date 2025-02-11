package kr.hhplus.be.server.infra.repository.token;

import kr.hhplus.be.server.domain.entity.token.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

}
