package ns.example.kafka_querydsl.repository;

import static ns.example.kafka_querydsl.utils.CouponRedisUtils.getIssueRequestKey;
import static ns.example.kafka_querydsl.utils.CouponRedisUtils.getIssueRequestQueueKey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import ns.example.kafka_querydsl.dto.CouponIssueRequest;
import ns.example.kafka_querydsl.utils.RedisScriptCode;
import ns.example.kafka_querydsl.exception.CouponIssueException;
import ns.example.kafka_querydsl.exception.ErrorCode;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class RedisRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<String> issueScript = issueRequestScript();
    private final ObjectMapper objectMapper;

    public Boolean zAdd(String key, String value, double score){
        // Sorted Set에 없는 경우만 add
        return redisTemplate.opsForZSet().addIfAbsent(key, value, score);
    }

    public Long sAdd(String key, String value){
        return redisTemplate.opsForSet().add(key, value);
    }

    public Long sCard(String key){
        return redisTemplate.opsForSet().size(key);
    }

    public Boolean sIsMember(String key, String value){
        return redisTemplate.opsForSet().isMember(key, value);
    }

    public Long rPush(String key, String value){
        return redisTemplate.opsForList().rightPush(key, value);
    }

    public String rPop(String key) { return redisTemplate.opsForList().rightPop(key); }

    public void issueRequest(Long couponId, Long userId, Integer totalQuantity) {
        String requestKey = getIssueRequestKey(couponId);
        CouponIssueRequest couponIssueRequest = new CouponIssueRequest(couponId, userId);
        String queueKey = getIssueRequestQueueKey();
        try {
            String code = redisTemplate.execute(
                    issueScript,
                    List.of(requestKey, queueKey),
                    String.valueOf(userId),
                    String.valueOf(totalQuantity),
                    objectMapper.writeValueAsString(couponIssueRequest)
            );
            RedisScriptCode.checkRequestResult(RedisScriptCode.find(code));
        } catch (JsonProcessingException e) {
            throw new CouponIssueException(ErrorCode.FAIL_COUPON_ISSUE_REQUEST,
                    "input: %s".formatted(couponIssueRequest));
        }
    }

    private RedisScript<String> issueRequestScript(){
        String script = """
                if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then
                    return '2'
                end
                
                if tonumber(ARGV[2]) > redis.call('SCARD', KEYS[1]) then
                    redis.call('SADD', KEYS[1], ARGV[1])
                    redis.call('RPUSH', KEYS[2], ARGC[3])
                    return '1'
                end
                
                return '3'
                """;
        return RedisScript.of(script, String.class);
    }
}
