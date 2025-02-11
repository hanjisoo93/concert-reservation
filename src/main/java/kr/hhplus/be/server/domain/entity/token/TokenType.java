package kr.hhplus.be.server.domain.entity.token;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TokenType {
    WAITING("token:waiting:", 30 * 60),   // 30분 TTL (초 단위)
    ACTIVE("token:active:", 30 * 60);       // 30분 TTL (초 단위)

    private final String keyPrefix;
    private final long ttlSeconds;
}