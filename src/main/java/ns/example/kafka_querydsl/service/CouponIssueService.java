package ns.example.kafka_querydsl.service;

import static ns.example.kafka_querydsl.utils.CouponRedisUtils.getIssueRequestKey;
import static ns.example.kafka_querydsl.utils.CouponRedisUtils.getIssueRequestQueueKey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ns.example.kafka_querydsl.domain.Coupon;
import ns.example.kafka_querydsl.dto.CouponIssueRequest;
import ns.example.kafka_querydsl.dto.CouponRedisEntity;
import ns.example.kafka_querydsl.exception.CouponIssueException;
import ns.example.kafka_querydsl.exception.ErrorCode;
import ns.example.kafka_querydsl.repository.RedisRepository;
import ns.example.kafka_querydsl.utils.DistributedLock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CouponIssueService {

    private final RedisRepository redisRepository;

    private final ObjectMapper objectMapper;
    private final DistributedLock distributedLock;

    private final CouponService couponService;

    private final CouponQueueService couponQueueService;

    public CouponIssueService(
            RedisRepository redisRepository,
            ObjectMapper objectMapper,
            DistributedLock distributedLock,
            CouponService couponService,
            @Qualifier("redisCouponQueueService") CouponQueueService couponQueueService) {
        this.redisRepository = redisRepository;
        this.objectMapper = objectMapper;
        this.distributedLock = distributedLock;
        this.couponService = couponService;
        this.couponQueueService = couponQueueService;
    }

    // 쿠폰 조회
    @Cacheable(cacheNames = "couponCache")
    public CouponRedisEntity getCouponCache(Long couponId){
        Coupon coupon = couponService.findCoupon(couponId);
        return new CouponRedisEntity(coupon);
    }

    // 쿠폰 발급 요청 - Redisson 분산 락
    public void issueRequestV1(Long couponId, Long userId){
        CouponRedisEntity coupon = getCouponCache(couponId);
        coupon.checkValidDate();

        distributedLock.execute("lock_%s".formatted(couponId), 3000, 3000, () -> {
            checkCouponQuantity(coupon, userId);
            issueRequest(couponId, userId);
        });
    }

    // 쿠폰 발급 요청 - RedisScript EVAL을 통한 트랜잭션 처리
    public void issueRequestV2(Long couponId, Long userId){
        CouponRedisEntity coupon = getCouponCache(couponId);
        coupon.checkValidDate();
        issueRequest(couponId, userId, coupon.totalQuantity());
    }

    // Validation
    public void checkCouponQuantity(CouponRedisEntity coupon, Long userId){
        Long couponId = coupon.id();
        if(!availableTotalIssueQuantity(coupon.totalQuantity(), couponId))
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY, "가능한 수량을 초과했습니다. coupon %s, userId %s".formatted(couponId, userId));
        if(!availableUserIssueQuantity(couponId, userId))
            throw new CouponIssueException(ErrorCode.DUPLICATED_COUPON_ISSUE, "이미 발급 요청이 처리되었습니다. coupon %s, userId %s".formatted(couponId, userId));
    }

    // 쿠폰 발급
    private void issueRequest(Long couponId, Long userId){
        CouponIssueRequest issueRequest = new CouponIssueRequest(couponId, userId);
        try {
            redisRepository.sAdd(getIssueRequestKey(couponId), String.valueOf(userId));
            // 요청의 발급 수량 확인, 제어를 위한 IssueRequestKey(couponId)

            couponQueueService.enqueue(getIssueRequestQueueKey(), objectMapper.writeValueAsString(issueRequest));
            // 쿠폰 발급 큐에 적재

        }catch (JsonProcessingException e){
            throw new CouponIssueException(ErrorCode.FAIL_COUPON_ISSUE_REQUEST, "input: %s".formatted(issueRequest));
        }
    }

    private void issueRequest(Long couponId, Long userId, Integer totalQuantity)
    {
        if(totalQuantity==null)
            redisRepository.issueRequest(couponId, userId, Integer.MAX_VALUE);

        redisRepository.issueRequest(couponId,userId,totalQuantity);
    }

    // 해당 사용자가 쿠폰 수량 제한을 만족하는지 검사합니다.
    public boolean availableTotalIssueQuantity(Integer totalQuantity, Long couponId){
        if(totalQuantity==null) return true;

        String key = getIssueRequestKey(couponId);
        return totalQuantity > redisRepository.sCard(key);
    }

    // 중복 발급 여부를 검사합니다.
    public boolean availableUserIssueQuantity(Long couponId, Long userId){
        String key = getIssueRequestKey(couponId);
        return !redisRepository.sIsMember(key, String.valueOf(userId));
    }
}
