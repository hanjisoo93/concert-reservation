package kr.hhplus.be.server.domain.token.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TokenStatus {

    ACTIVE("활성"),
    EXPIRED("만료");

    private final String text;
}
