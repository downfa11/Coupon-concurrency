package ns.example.kafka_querydsl.controller;

import lombok.RequiredArgsConstructor;
import ns.example.kafka_querydsl.service.CouponIssueService;
import ns.example.kafka_querydsl.service.CouponService;
import ns.example.kafka_querydsl.utils.CouponConsumer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CouponIssueController {
    private final CouponService couponService;
    private final CouponIssueService couponIssueService;
    private final CouponConsumer couponConsumer;

    @PostMapping("/use/{couponId}")
    public ResponseEntity<Void> useCoupon(@RequestParam Long userId, @PathVariable Long couponId) {
        boolean success = couponService.useCoupon(userId, couponId);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/use/kafka/{couponId}")
    public ResponseEntity<Void> useCouponToKafka(@RequestParam Long userId, @PathVariable Long couponId) {
        couponConsumer.useCouponToKafka(userId, couponId);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/v1/issue")
    public ResponseEntity<Void> asyncIssueV1(@RequestParam Long userId, @RequestParam Long couponId) {
        couponIssueService.issueRequestV1(couponId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/v2/issue")
    public ResponseEntity<Void> asyncIssueV2(@RequestParam Long userId, @RequestParam Long couponId) {
        couponIssueService.issueRequestV2(couponId, userId);
        return ResponseEntity.ok().build();
    }
}
