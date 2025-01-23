package kr.hhplus.be.server.interfaces.controller.point;

import kr.hhplus.be.server.domain.service.point.PointConcurrencyService;
import kr.hhplus.be.server.interfaces.controller.point.dto.PointRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/point")
public class PointConcurrencyController {

    private final PointConcurrencyService pointConcurrencyService;

    @PostMapping("/pessimistic/credit")
    public ResponseEntity<String> creditPointWithPessimisticLock(@RequestBody PointRequest request) {
        pointConcurrencyService.creditPoint(request.getUserId(), request.getAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body("Credit Point with Pessimistic Lock successful");
    }

    @PostMapping("/distributed/credit")
    public ResponseEntity<String> creditPointWithDistributedLock(@RequestBody PointRequest request) {
        pointConcurrencyService.creditPointWithRedissonLock(request.getUserId(), request.getAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body("Credit Point with Distributed Lock successful");
    }

    @PostMapping("/pessimistic/spend")
    public ResponseEntity<String> spendPointWithPessimisticLock(@RequestBody PointRequest request) {
        pointConcurrencyService.spendPoint(request.getUserId(), request.getAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body("Spend Point with Pessimistic Lock successful");
    }

    @PostMapping("/distributed/spend")
    public ResponseEntity<String> spendPointWithDistributedLock(@RequestBody PointRequest request) {
        pointConcurrencyService.spendPointWithRedissonLock(request.getUserId(), request.getAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body("Spend Point with Distributed Lock successful");
    }

}
