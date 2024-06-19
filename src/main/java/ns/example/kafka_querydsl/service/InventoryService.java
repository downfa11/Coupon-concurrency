package ns.example.kafka_querydsl.service;

import lombok.AllArgsConstructor;
import ns.example.kafka_querydsl.entity.Order;
import ns.example.kafka_querydsl.utils.OrderEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class InventoryService {
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @KafkaListener(topics = "order_created",groupId = "group_order")
    public void handleOrderCreated(OrderEvent order){
        boolean isStock = checkStock(order);
        if(!isStock)
            kafkaTemplate.send("order_canceled",order);
    }

    private boolean checkStock(OrderEvent order){
        // 재고 확인
        return true;
    }
}
