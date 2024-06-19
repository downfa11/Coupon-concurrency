package ns.example.kafka_querydsl.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ns.example.kafka_querydsl.entity.Order;
import ns.example.kafka_querydsl.repository.OrderRepository;
import ns.example.kafka_querydsl.utils.CouponUsedEvent;
import ns.example.kafka_querydsl.utils.OrderEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrderService {
    private OrderRepository orderRepository;
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void createOrder(String kind, Long count){
        Order order = new Order();
        order.setKind(kind);
        order.setCount(count);
        orderRepository.save(order);

        OrderEvent event = new OrderEvent(order.getId(), order.getKind(), order.getCount());
        kafkaTemplate.send("order_created",event);
    }
}
