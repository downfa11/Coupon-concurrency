package ns.example.kafka_querydsl;

import ns.example.kafka_querydsl.entity.Coupon;
import ns.example.kafka_querydsl.entity.User;
import ns.example.kafka_querydsl.entity.Vendor;
import ns.example.kafka_querydsl.repository.CouponRepository;
import ns.example.kafka_querydsl.repository.UserRepository;
import ns.example.kafka_querydsl.repository.VendorRepository;
import ns.example.kafka_querydsl.service.CouponConsumer;
import ns.example.kafka_querydsl.service.CouponService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class CouponServiceTest {

    @MockBean
    private CouponRepository couponRepository;

    @MockBean
    private VendorRepository vendorRepository;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponConsumer couponConsumer;


    @Test
    void generateUser() {
        User user = new User();
        user.setId(1L);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = couponService.generateRandomUser();

        verify(userRepository, times(2)).save(any(User.class));
        assertNotNull(result);
        assertEquals("User" + user.getId(), result.getName());
    }

    @Test
    void generateCoupon() {
        String couponType = "문화상품권 1만원권";
        when(couponRepository.countByType(couponType)).thenReturn(50L);

        Vendor vendor = new Vendor();
        vendor.setId(1L);
        when(vendorRepository.findById(1L)).thenReturn(Optional.of(vendor));

        Coupon coupon = new Coupon();
        coupon.setType(couponType);
        when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);

        Coupon result = couponService.generateRandomCoupon();

        verify(couponRepository, times(1)).save(any(Coupon.class));
        assertNotNull(result);
        assertEquals(couponType, result.getType());
    }

    @Test
    void moreThanCouponCount() {
        User user = new User();
        user.setId(1L);


        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setCount(100);
        Vendor vendor = new Vendor();
        vendor.setId(1L);
        coupon.setVendor(vendor);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(couponRepository.findById(coupon.getId())).thenReturn(Optional.of(coupon));
        when(userRepository.save(any(User.class))).thenReturn(user);

        //todo. Expected:true, Actual:false
        for(int i=0;i<100;i++) {
            assertTrue(couponService.useCoupon(user.getId(), coupon.getId()));
        }


        assertFalse(couponService.useCoupon(user.getId(), coupon.getId()));

        verify(userRepository, times(100)).save(any(User.class));
        verify(couponRepository, times(100)).save(any(Coupon.class));

    }

    @Test
    void getUserCoupons() {
        User user = new User();
        user.setId(1L);
        Vendor vendor = new Vendor();
        vendor.setId(1L);

        Coupon coupon1 = new Coupon();
        coupon1.setVendor(vendor);
        coupon1.setType("비트코인");
        user.setCoupons(Arrays.asList(coupon1));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon1));

        List<Coupon> result = couponService.getUserCoupons(1L);

        verify(userRepository, times(1)).findById(1L);
        assertEquals(1, result.size());
    }

    @Test
    void useCoupon() {
        User user = new User();
        user.setId(1L);
        user.setCoupons(new ArrayList<>());

        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setCount(100);
        Vendor vendor = new Vendor();
        vendor.setId(1L);
        coupon.setVendor(vendor);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        when(userRepository.save(any(User.class))).thenReturn(user);

        boolean result = couponService.useCoupon(1L, 1L);

        assertTrue(result);
        verify(userRepository, times(1)).save(user);
        verify(couponRepository, times(1)).save(coupon);
    }

    @Test
    void useInvalidCoupon() {
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
        user.setCoupons(new ArrayList<>());

        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setCount(100);
        Vendor vendor = new Vendor();
        vendor.setId(1L);
        coupon.setVendor(vendor);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));

        boolean result = couponConsumer.useCouponToKafka(user.getId(), coupon.getId());

        assertTrue(result);

    }

    @Test
    void useInvalidCouponToKafka() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = couponConsumer.useCouponToKafka(1L, 1L);

        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
        verify(couponRepository, never()).save(any(Coupon.class));
    }
}
