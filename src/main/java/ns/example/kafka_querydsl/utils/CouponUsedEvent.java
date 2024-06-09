package ns.example.kafka_querydsl.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponUsedEvent {
        private Long userId;
        private Long couponId;
        private double couponValue;

}
