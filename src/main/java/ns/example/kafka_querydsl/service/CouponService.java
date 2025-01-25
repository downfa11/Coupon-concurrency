package ns.example.kafka_querydsl.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ns.example.kafka_querydsl.domain.Coupon;
import ns.example.kafka_querydsl.domain.CouponType;
import ns.example.kafka_querydsl.domain.User;
import ns.example.kafka_querydsl.domain.Vendor;
import ns.example.kafka_querydsl.repository.CouponRepository;
import ns.example.kafka_querydsl.repository.UserRepository;
import ns.example.kafka_querydsl.repository.VendorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
public class CouponService {
    private static final Random random = new Random();
    public static final int MAX_COUPONS = 100;
    public static final double COUPON_RATE = 0.1;

    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;

    public User generateRandomUser() {
        User user = new User();
        user = userRepository.save(user);
        user.setName("User" + user.getId());
        return userRepository.save(user);
    }


    public Vendor generateRandomVendor() {
        Vendor vendor = new Vendor();
        vendor = vendorRepository.save(vendor);
        vendor.setName("Vendor" + vendor.getId());
        vendor.setBalance(random.nextInt() * 1000);
        return vendorRepository.save(vendor);
    }

    @Transactional
    public Coupon generateRandomCoupon() {
        CouponType randomType = getRandomCouponType();

        Long couponCount = couponRepository.countByType(randomType.name());
        if (couponCount < MAX_COUPONS) {
            Vendor vendor = vendorRepository.findById(1L)
                    .orElseThrow(() -> new IllegalArgumentException("Not found Vendor"));

            Coupon coupon = Coupon.builder()
                    .couponType(randomType)
                    .title(randomType.getTitle())
                    .totalQuantity(5000)
                    .discountAmount(randomType.getValue())
                    .issuedQuantity(0)
                    .vendor(vendor)
                    .startDate(LocalDateTime.now())
                    .endDate(LocalDateTime.now().plusDays(1))
                    .build();

            return couponRepository.save(coupon);
        }
        return null;
    }

    private CouponType getRandomCouponType() {
        CouponType[] types = CouponType.values();
        return types[new Random().nextInt(types.length)];
    }

    public List<Coupon> getUserCoupons(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return user.getCoupons();
    }

    public Coupon findCoupon(Long couponId) {
        return couponRepository.findById(couponId).orElseThrow();
    }

    @Transactional
    public boolean useCoupon(Long userId, Long couponId) {
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Coupon> couponOptional = couponRepository.findById(couponId);

        if (userOptional.isPresent() && couponOptional.isPresent()) {
            User user = userOptional.get();
            Coupon coupon = couponOptional.get();

            if (coupon.getIssuedQuantity() <= 0) {
                return false;
            }

            user.getCoupons().add(coupon);
            coupon.setIssuedQuantity(coupon.getIssuedQuantity() - 1);
            userRepository.save(user);
            couponRepository.save(coupon);

            if (coupon.getIssuedQuantity() == 0) {
                couponRepository.delete(coupon);
            }

            payToVendor(coupon);
            return true;

        }

        return false;
    }

    public void payToVendor(Coupon coupon) {
        Vendor vendor = coupon.getVendor();
        int couponValue = coupon.getDiscountAmount();
        int vendorPayment = (int) (couponValue * COUPON_RATE);
        vendor.setBalance(vendor.getBalance() + vendorPayment);
        vendorRepository.save(vendor);
    }
}


