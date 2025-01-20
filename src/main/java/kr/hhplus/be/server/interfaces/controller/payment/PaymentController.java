package kr.hhplus.be.server.interfaces.controller.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import kr.hhplus.be.server.application.facade.payment.PaymentFacade;
import kr.hhplus.be.server.domain.entity.payment.Payment;
import kr.hhplus.be.server.interfaces.controller.payment.dto.PaymentRequest;
import kr.hhplus.be.server.interfaces.controller.payment.dto.PaymentResponse;
import kr.hhplus.be.server.domain.service.payment.PaymentService;
import kr.hhplus.be.server.common.error.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentFacade paymentFacade;

    @Operation(summary = "결제 처리", description = "사용자 ID 를 통해 결제 처리를 합니다.", tags={ "Payments API" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결제가 성공적으로 처리되었습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    @PostMapping
    public ResponseEntity<Object> processPayment(@RequestBody @Valid PaymentRequest paymentRequest) {
        paymentFacade.payment(paymentRequest.getReservationId());
        return ResponseEntity.ok("결제가 성공적으로 처리되었습니다.");
    }
}
