package kr.hhplus.be.server.domain.concert.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {
    private Integer id;
    private LocalDate concertDate;
    private String createdAt;
}
