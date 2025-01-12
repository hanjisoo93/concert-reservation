package kr.hhplus.be.server.domain.point.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointChangeType {

    DEPOSIT("충전"),
    WITHDRAWAL("사용");

    private final String text;
}
