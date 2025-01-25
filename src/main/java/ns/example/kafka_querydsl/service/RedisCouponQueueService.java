package ns.example.kafka_querydsl.service;

import static ns.example.kafka_querydsl.utils.CouponRedisUtils.getIssueRequestQueueKey;

import ns.example.kafka_querydsl.repository.RedisRepository;
import org.springframework.stereotype.Service;

@Service
public class RedisCouponQueueService implements CouponQueueService {

    private RedisRepository redisRepository;

    @Override
    public void enqueue(String key, Object object) {
        redisRepository.rPush(getIssueRequestQueueKey(), String.valueOf(object));
    }

    @Override
    public String dequeue(String key) {
        return redisRepository.rPop(key);
    }
}
