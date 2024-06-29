package ns.example.kafka_querydsl;

import ns.example.kafka_querydsl.utils.OrderEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    @Autowired
    KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Transactional
    public void register(Long id, Long count, String kind) throws InterruptedException {
        kafkaTemplate.send("transaction-topic", createEvent(id,count,kind));
        Thread.sleep(1000);

        if (id == null) {
            throw new IllegalArgumentException();
        }
    }

    private OrderEvent createEvent(Long id, Long count, String kind) {
        return OrderEvent.builder()
                .id(id)
                .count(count)
                .kind(kind).build();
    }
}
