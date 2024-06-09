package ns.example.kafka_querydsl;

import ns.example.kafka_querydsl.entity.Coupon;
import ns.example.kafka_querydsl.entity.User;
import ns.example.kafka_querydsl.entity.Vendor;
import ns.example.kafka_querydsl.repository.CouponRepository;
import ns.example.kafka_querydsl.repository.UserRepository;
import ns.example.kafka_querydsl.repository.VendorRepository;
import ns.example.kafka_querydsl.service.CouponConsumer;
import ns.example.kafka_querydsl.service.CouponService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private VendorRepository vendorRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CouponService couponService;
    private CouponConsumer couponConsumer;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void generateUser(){
        User user = new User();
        user.setId(1L);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = couponService.generateRandomUser();

        verify(userRepository,times(1)).save(any(User.class));
        // 메소드가 한 번 호출되었는지 Verity
        assertEquals("User" + user.getId(), result.getName());
    }

    @Test
    void generateCoupon() {
        String couponType = "문화상품권 1만원권";
        when(couponRepository.countByType(couponType)).thenReturn(50L);
        // 해당 타입의 쿠폰은 50개가 있다고 가정

        Vendor vendor = new Vendor();
        vendor.setId(1L);
        when(vendorRepository.findById(1L)).thenReturn(Optional.of(vendor));

        Coupon coupon = new Coupon();
        coupon.setType(couponType);
        when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);
        // 실제 DB에 저장하지 않고 미리 정의된 Coupon 객체를 반환
        // any(Coupon.class) : Coupon 클래스 타입의 객체를 매칭

        Coupon result = couponService.generateRandomCoupon();

        verify(couponRepository, times(1)).save(any(Coupon.class));
        assertNotNull(result);
        assertEquals(couponType, result.getType());
    }

    @Test
    void MoreThanCouponCount() {
        String couponType = "문화상품권 1만원권";
        when(couponRepository.countByType(couponType)).thenReturn(101L);

        Coupon result = couponService.generateRandomCoupon();

        verify(couponRepository, never()).save(any(Coupon.class));
        assertNull(result);
    }

    @Test
    void getUserCoupons() {
        User user = new User();
        user.setId(1L);
        Coupon coupon1 = new Coupon();
        Coupon coupon2 = new Coupon();
        user.setCoupons(Arrays.asList(coupon1, coupon2));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        List<Coupon> result = couponService.getUserCoupons(1L);

        verify(userRepository, times(1)).findById(1L);
        assertEquals(2, result.size());
    }

    @Test
    void useCoupon() {
        User user = new User();
        user.setId(1L);
        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setCount(1);
        Vendor vendor = new Vendor();
        coupon.setVendor(vendor);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));

        boolean result = couponService.useCoupon(1L, 1L);

        assertTrue(result);
        verify(userRepository, times(1)).save(user);
        verify(couponRepository, times(1)).delete(coupon);
    }

    @Test
    void UseInvalidCoupon() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = couponService.useCoupon(1L, 1L);

        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
        verify(couponRepository, never()).save(any(Coupon.class));
    }


    @Test
    void useCouponToKafka() {
        User user = new User();
        user.setId(1L);
        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setCount(1);
        Vendor vendor = new Vendor();
        coupon.setVendor(vendor);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));

        boolean result = couponConsumer.useCouponToKafka(1L, 1L);

        assertTrue(result);
        verify(userRepository, times(1)).save(user);
        verify(couponRepository, times(1)).delete(coupon);
    }

    @Test
    void UseInvalidCouponToKafka() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = couponConsumer.useCouponToKafka(1L, 1L);

        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
        verify(couponRepository, never()).save(any(Coupon.class));
    }
}
