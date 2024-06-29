package ns.example.kafka_querydsl;


import lombok.extern.slf4j.Slf4j;
import ns.example.kafka_querydsl.utils.OrderEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransactionConsumer {

    @KafkaListener(topics = "transaction-topic",groupId = "group_order")
    public void create(OrderEvent event) {
        log.info("received : " + event.toString());
    }
}