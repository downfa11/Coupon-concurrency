package ns.example.kafka_querydsl.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import ns.example.kafka_querydsl.domain.Coupon;
import ns.example.kafka_querydsl.domain.CouponType;
import ns.example.kafka_querydsl.exception.CouponIssueException;
import ns.example.kafka_querydsl.exception.ErrorCode;

public record CouponRedisEntity(
        Long id,
        CouponType couponType,
        Integer totalQuantity,

        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        LocalDateTime startDate,

        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        LocalDateTime endDate) {

    public CouponRedisEntity(Coupon coupon){
        this(coupon.getId(), coupon.getCouponType(), coupon.getTotalQuantity(), coupon.getStartDate(), coupon.getEndDate());
    }

    private boolean availableDates(){
        LocalDateTime now = LocalDateTime.now();
        return startDate.isBefore(now) && endDate.isAfter(now);
    }

    public void checkValidDate(){
        if(!availableDates()){
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_DATE, "발급 기간이 유효하지 않습니다.");
        }
    }
}
