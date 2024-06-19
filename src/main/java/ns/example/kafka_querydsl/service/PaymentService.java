package ns.example.kafka_querydsl.service;

import lombok.AllArgsConstructor;
import ns.example.kafka_querydsl.entity.Order;
import ns.example.kafka_querydsl.utils.OrderEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PaymentService {
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;


    @KafkaListener(topics = "order_created",groupId = "group_order")
    public void handleOrderCreated(OrderEvent order){
        boolean isPayment = processPayment(order);
        if(!isPayment)
            kafkaTemplate.send("payment_failed",order);
        else kafkaTemplate.send("payment_completed",order);
    }

    private boolean processPayment(OrderEvent order){
        return true;
    }
}
