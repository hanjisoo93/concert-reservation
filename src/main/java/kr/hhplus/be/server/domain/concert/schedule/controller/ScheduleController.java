package kr.hhplus.be.server.domain.concert.schedule.controller;

import kr.hhplus.be.server.domain.concert.schedule.dto.ScheduleResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    @GetMapping("/{concertDate}")
    public ResponseEntity<List<ScheduleResponse>> getConcertSchedules(@PathVariable String concertDate) {
        List<ScheduleResponse> schedules = new ArrayList<>();
        schedules.add(new ScheduleResponse(1, LocalDate.parse(concertDate), "2024-01-01T10:00:00"));
        schedules.add(new ScheduleResponse(2, LocalDate.parse(concertDate), "2024-01-01T12:00:00"));

        return ResponseEntity.ok(schedules);
    }
}
