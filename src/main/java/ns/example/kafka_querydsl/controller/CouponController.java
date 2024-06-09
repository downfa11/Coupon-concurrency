package ns.example.kafka_querydsl.controller;

import lombok.AllArgsConstructor;
import ns.example.kafka_querydsl.entity.Coupon;
import ns.example.kafka_querydsl.entity.User;
import ns.example.kafka_querydsl.entity.Vendor;
import ns.example.kafka_querydsl.service.CouponConsumer;
import ns.example.kafka_querydsl.service.CouponService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final CouponConsumer couponConsumer;

    @GetMapping("/random-user")
    public User getRandomUser() {
        return couponService.generateRandomUser();
    }

    @GetMapping("/random-vendor")
    public Vendor getRandomVendor() {
        return couponService.generateRandomVendor();
    }

    @GetMapping("/coupons")
    public ResponseEntity<List<Coupon>> getUserCoupons(@RequestParam Long userId) {
        List<Coupon> userCoupons = couponService.getUserCoupons(userId);
        return ResponseEntity.ok(userCoupons);
    }

    @PostMapping("/coupons")
    public ResponseEntity<Coupon> generateRandomCoupon() {
        Coupon coupon = couponService.generateRandomCoupon();
        if (coupon != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(coupon);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

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


}