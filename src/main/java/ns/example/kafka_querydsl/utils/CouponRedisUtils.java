package ns.example.kafka_querydsl.utils;

public class CouponRedisUtils {

    public static String getIssueRequestKey(Long coupondId){
        return "issue.request.coupondId=%s".formatted(coupondId);
    }

    public static String getIssueRequestQueueKey(){
        return "issue.request.queue";
    }
}
