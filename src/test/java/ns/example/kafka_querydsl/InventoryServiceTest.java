package ns.example.kafka_querydsl;

import ns.example.kafka_querydsl.entity.Item;
import ns.example.kafka_querydsl.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest
class InventoryServiceTest {

    @Autowired
    private InventoryService inventoryService;

    private String stockKey;
    private Item item;

    private class BuyWorker implements Runnable {
        private String stockKey;
        private Long count;
        private CountDownLatch countDownLatch;

        public BuyWorker(String stockKey, int count, CountDownLatch countDownLatch) {
            this.stockKey = stockKey;
            this.count = (long)count;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            inventoryService.decreaseWithLock(this.stockKey, count);
            countDownLatch.countDown();
        }
    }

    private class BuyNoLockWorker implements Runnable {
        private String stockKey;
        private Long count;
        private CountDownLatch countDownLatch;

        public BuyNoLockWorker(String stockKey, int count, CountDownLatch countDownLatch) {
            this.stockKey = stockKey;
            this.count = (long)count;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            inventoryService.decrease(this.stockKey, count);
            countDownLatch.countDown();
        }
    }

    @BeforeEach
    void setup() {
        final Long amount = 100L;
        this.item = Item.builder()
                .name("아이템")
                .keyId("0")
                .amount(amount).build();

        this.stockKey = inventoryService.keyResolver(item.getName(), item.getKeyId());
        inventoryService.setStock(stockKey, amount);
    }

    @Test
    void BuyWithoutLock() throws InterruptedException {
        int people = 50, count = 2;
        CountDownLatch countDownLatch = new CountDownLatch(people);

        List<Thread> workers = Stream.generate(() -> new Thread(new BuyNoLockWorker(stockKey, count, countDownLatch)))
                .limit(people).collect(Collectors.toList());
        workers.forEach(Thread::start);
        countDownLatch.await();

        Long currentCount = inventoryService.currentStock(stockKey);
        assertNotEquals(0, currentCount);
    }

    @Test
    void BuyWithLock() throws InterruptedException {
        int people = 50, count = 2;
        CountDownLatch countDownLatch = new CountDownLatch(people);

        List<Thread> workers = Stream.generate(() -> new Thread(new BuyWorker(stockKey, count, countDownLatch)))
                .limit(people).collect(Collectors.toList());
        workers.forEach(Thread::start);
        countDownLatch.await();

        Long currentCount = inventoryService.currentStock(stockKey);
        assertEquals(0L, currentCount);
    }
}

