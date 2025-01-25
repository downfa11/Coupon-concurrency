package ns.example.kafka_querydsl.utils;

import ns.example.kafka_querydsl.exception.CouponIssueException;
import ns.example.kafka_querydsl.exception.ErrorCode;

public enum RedisScriptCode {
    SUCCESS(1),
    DUPLICATED_COUPON_ISSUE(2),
    INVALID_COUPON_ISSUE_QUANTITY(3);

    RedisScriptCode(int code){

    }

    public static RedisScriptCode find(String code){
        int codeValue = Integer.parseInt(code);

        if(codeValue==1) return SUCCESS;
        if(codeValue==2) return DUPLICATED_COUPON_ISSUE;
        if(codeValue==3) return INVALID_COUPON_ISSUE_QUANTITY;

        throw new IllegalArgumentException("Invalid redis script code.");
    }

    public static void checkRequestResult(RedisScriptCode code){
        if(code==INVALID_COUPON_ISSUE_QUANTITY)
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY, "가능한 수량을 초과했습니다.");
        if(code==DUPLICATED_COUPON_ISSUE)
            throw new CouponIssueException(ErrorCode.DUPLICATED_COUPON_ISSUE, "이미 발급 요청이 처리되었습니다.");
    }
}
