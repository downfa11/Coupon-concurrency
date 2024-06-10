package ns.example.kafka_querydsl.service;

import lombok.AllArgsConstructor;
import ns.example.kafka_querydsl.entity.User;
import ns.example.kafka_querydsl.entity.Coupon;
import ns.example.kafka_querydsl.entity.Vendor;
import ns.example.kafka_querydsl.repository.CouponRepository;
import ns.example.kafka_querydsl.repository.UserRepository;
import ns.example.kafka_querydsl.repository.VendorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
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
        List<String> couponTypes = Arrays.asList("문화상품권 1만원권", "해피머니상품권 5천원권", "비트코인");
        String couponType = couponTypes.get(new Random().nextInt(couponTypes.size()));

        Long couponCount = couponRepository.countByType(couponType);
        if (couponCount < MAX_COUPONS) {
            Vendor vendor = vendorRepository.findById(1L).orElseThrow();

            Coupon coupon = new Coupon();
            coupon.setType(couponType);
            coupon.setCount(5000);
            coupon.setValue(getCouponValue(couponType));
            coupon.setVendor(vendor);

            return couponRepository.save(coupon);
        }

        return null;
    }

    public List<Coupon> getUserCoupons(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return user.getCoupons();
    }

    @Transactional
    public boolean useCoupon(Long userId, Long couponId) {
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Coupon> couponOptional = couponRepository.findById(couponId);

        if (userOptional.isPresent() && couponOptional.isPresent()) {
            User user = userOptional.get();
            Coupon coupon = couponOptional.get();

            if (coupon.getCount() <= 0) {
                return false;
            }

                user.getCoupons().add(coupon);
                coupon.setCount(coupon.getCount() - 1);
            userRepository.save(user);
            couponRepository.save(coupon);

                if (coupon.getCount() == 0) {
                    couponRepository.delete(coupon);
                }

                    payToVendor(coupon);
                    return true;

        }

        return false;
    }

    public void payToVendor(Coupon coupon) {
        Vendor vendor = coupon.getVendor();
        int couponValue = coupon.getValue();
        int vendorPayment = (int) (couponValue * COUPON_RATE);
        vendor.setBalance(vendor.getBalance() + vendorPayment);
        vendorRepository.save(vendor);
    }

    private int getCouponValue(String couponType) {
        switch (couponType) {
            case "문화상품권 1만원권":
                return 10000;
            case "해피머니상품권 5천원권":
                return 5000;
            case "비트코인": // 97,919,000 KRW (2024-06-09 01:37 기준)
                return 97919000;
            default:
                return 0;
        }
    }

}


