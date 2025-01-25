package ns.example.kafka_querydsl.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import ns.example.kafka_querydsl.domain.Coupon;
import ns.example.kafka_querydsl.domain.User;
import ns.example.kafka_querydsl.domain.Vendor;
import ns.example.kafka_querydsl.service.CouponService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class CouponController {

    private final CouponService couponService;

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

}