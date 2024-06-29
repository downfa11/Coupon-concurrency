package ns.example.kafka_querydsl.service;

import lombok.AllArgsConstructor;
import ns.example.kafka_querydsl.entity.Order;
import ns.example.kafka_querydsl.repository.OrderRepository;
import ns.example.kafka_querydsl.utils.OrderEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class OrderService {
    private OrderRepository orderRepository;
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void createOrder(String client, String vendor, String kind, Long count){
        Order order = Order.builder()
                .kind(kind)
                .count(count)
                .client(client)
                .vendor(vendor)
                .createdAt(LocalDateTime.now())
                .build();
        orderRepository.save(order);

        OrderEvent event = new OrderEvent(order.getId(), order.getKind(), order.getCount());
        kafkaTemplate.send("order_created",event);
    }
}
