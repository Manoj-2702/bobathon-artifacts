package com.bank.payments;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the payment transfer API.
 * Exposes endpoints consumed by the bank's frontend application.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Initiates a fund transfer between two accounts.
     * Accepts a JSON body with fromAccount, toAccount, and amount.
     */
    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody TransferRequest request) {
        System.out.println("Incoming transfer request: from=" + request.fromAccount
                + " to=" + request.toAccount
                + " amount=" + request.amount);

        if (!paymentService.validateRequest(request)) {
            return ResponseEntity.badRequest().body("Invalid request");
        }

        String result = paymentService.transfer(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Health check endpoint for load balancer probes.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("UP");
    }
}
