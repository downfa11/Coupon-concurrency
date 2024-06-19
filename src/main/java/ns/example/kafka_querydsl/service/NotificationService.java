package ns.example.kafka_querydsl.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ns.example.kafka_querydsl.utils.OrderEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class NotificationService {

    @KafkaListener(topics = "payment_completed",groupId = "group_notification")
    public void notificationPayment(OrderEvent order){
        log.info("[notification] payment 완료 " + order.getId()+" 상품 : "+order.getKind()+" 수량 : "+order.getCount());
    }

    @KafkaListener(topics = "shipment_completed", groupId = "group_notification")
    public void notificationShippment(OrderEvent order){
        log.info("[notification] "+order.getId()+" 상품의 배송이 완료되었습니다.");
    }

    @KafkaListener(topics = "order_created", groupId = "group_notification")
    public void notificationOrderCreated(OrderEvent order){
        log.info("[notification] "+order.getId()+" 상품의 주문이 완료되었습니다.");
    }

    @KafkaListener(topics = "order_cancelled", groupId = "group_notification")
    public void notificationOrderCancelled(OrderEvent order){
        log.info("[notification] "+order.getId()+" 상품의 주문이 취소되었습니다.");
    }
}
