package ns.example.kafka_querydsl.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaCouponQueueService implements CouponQueueService {

    private KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void enqueue(String topic, Object object) {
        kafkaTemplate.send(topic, object);

    }

    @Override
    public String dequeue(String topic) {

        // todo. Kafka에서 메시지를 소비해서 dequeue를 구현해야함
        return String.valueOf("");
    }
}
