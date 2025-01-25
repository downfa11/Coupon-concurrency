package ns.example.kafka_querydsl.utils;

import lombok.AllArgsConstructor;
import ns.example.kafka_querydsl.domain.Coupon;
import ns.example.kafka_querydsl.service.CouponService;
import ns.example.kafka_querydsl.dto.CouponUsedEvent;
import ns.example.kafka_querydsl.domain.User;
import ns.example.kafka_querydsl.repository.CouponRepository;
import ns.example.kafka_querydsl.repository.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class CouponConsumer {
    private final CouponService couponService;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, CouponUsedEvent> kafkaTemplate;


    // topic 구독하고 메시지를 처리, 동시성 처리를 할 쓰레드 3개로 지정
    @KafkaListener(topics = "coupon-used-topic", groupId = "coupon-consumer-group", concurrency = "3")
    public void handleCouponUsedEvent(CouponUsedEvent event) {
        try {
            boolean usedCoupon = couponService.useCoupon(event.getUserId(), event.getCouponId());
            if (usedCoupon) {
                System.out.println("Coupon used by user: " + event.getUserId());
            } else {
                System.out.println("Failed to use coupon: " + event);
            }
        } catch (Exception e) {
            System.err.println("Error processing coupon: " + event);
            System.err.println("reason : " + e);
        }
    }

    public boolean useCouponToKafka(Long userId, Long couponId) {
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Coupon> couponOptional = couponRepository.findById(couponId);

        if (userOptional.isPresent() && couponOptional.isPresent()) {
            publishCouponUsedEvent(userOptional.get(), couponOptional.get());
            return true;
        }
        return false;
    }

    private void publishCouponUsedEvent(User user, Coupon coupon) {
        CouponUsedEvent event = new CouponUsedEvent(user.getId(), coupon.getId(), coupon.getDiscountAmount());
        kafkaTemplate.send("coupon-used-topic", event);
    }

}

