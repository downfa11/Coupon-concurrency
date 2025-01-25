package ns.example.kafka_querydsl.service;

import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ns.example.kafka_querydsl.dto.OrderEvent;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InventoryService {


    private static final int EMPTY =0;

    private final RedissonClient redissonClient;
    private final String prefix = "stock";

    private final CouponQueueService queueService;

    @Autowired
    public InventoryService(RedissonClient redissonClient,
                            @Qualifier("kafkaCouponQueueService") CouponQueueService queueService) {
        this.redissonClient = redissonClient;
        this.queueService = queueService;
    }

    @KafkaListener(topics = "order_created",groupId = "group_order")
    public void handleOrderCreatedKafka(OrderEvent order){
        boolean isStock = checkStock(order);
        queueService.enqueue((isStock ? "order_successed" : "order_canceled"),order);
    }

    public void handleOrderCreated(OrderEvent order) {
        boolean isStock = checkStock(order);
        if (!isStock) {
            log.info("Order canceled due to insufficient stock: {}", order);
        }
    }

    private boolean checkStock(OrderEvent order){
        String stockKey = keyResolver(order.getKind(), String.valueOf(order.getId()));
        return decreaseWithLock(stockKey, order.getCount());
    }

    public String keyResolver(String domain, String keyId){
        return String.format(prefix+":"+domain+":%s", keyId);
    }

    public boolean decreaseWithLock(final String key, final Long count){
        final String lockName = key + ":lock";
        final RLock lock = redissonClient.getLock(lockName);

        try {
            if(!lock.tryLock(1, 3, TimeUnit.SECONDS))
                return false;

            final Long stock = currentStock(key);
            if(stock <= EMPTY){
                log.info("남은 수량이 없습니다. ({}개)", stock);
                return false;
            }

            log.info("남은 수량 : {}개", stock);
            setStock(key, stock - count);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        } finally {
            if(lock != null && lock.isLocked() || lock.isHeldByCurrentThread()) {
                lock.unlock();
            }

            return true;
        }
    }

    public boolean decrease(final String key, final Long count){
        final Long stock = currentStock(key);

        if(stock <= EMPTY){
            log.info("남은 수량이 없습니다. ({}개)", stock);
            return false;
        }

        log.info("남은 수량 : {}개", stock);
        setStock(key, stock - count);
        return true;
    }

    public void setStock(String key, Long amount){
        redissonClient.getBucket(key).set(amount);
    }

    public Long currentStock(String key){
        return (Long)redissonClient.getBucket(key).get();
    }
}
