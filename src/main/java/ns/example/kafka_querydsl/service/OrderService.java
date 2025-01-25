package ns.example.kafka_querydsl.service;

import lombok.AllArgsConstructor;
import ns.example.kafka_querydsl.domain.Order;
import ns.example.kafka_querydsl.repository.OrderRepository;
import ns.example.kafka_querydsl.dto.OrderEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    private final CouponQueueService queueService;

    @Autowired
    public OrderService(OrderRepository orderRepository, @Qualifier("kafkaCouponQueueService") CouponQueueService queueService) {
        this.orderRepository = orderRepository;
        this.queueService = queueService;
    }

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
        queueService.enqueue("order_created",event);
    }
}
