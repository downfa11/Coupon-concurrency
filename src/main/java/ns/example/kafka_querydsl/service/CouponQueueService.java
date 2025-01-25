package ns.example.kafka_querydsl.service;

public interface CouponQueueService {
    void enqueue(String key, Object object);
    String dequeue(String key);
}
