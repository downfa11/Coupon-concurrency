package ns.example.kafka_querydsl.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ns.example.kafka_querydsl.entity.Order;
import ns.example.kafka_querydsl.utils.OrderEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class ShippingService {
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @KafkaListener(topics = "payment_completed",groupId = "group_payment")
    public void handlePaymentCompleted(OrderEvent order){
        prepareShipping(order);
    }

    void prepareShipping(OrderEvent order){
        log.info(order.getId()+" 주문 배송이 준비중입니다.");
        kafkaTemplate.send("shipment_completed",order);
    }
}
