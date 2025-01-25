package ns.example.kafka_querydsl.service;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TransactionServiceTest {

    @Autowired
    TransactionService transactionService;

    @Test
    public void register() throws InterruptedException {
        Long id = 1L;
        Long count = 100L;
        String kind = "phone";

        transactionService.register(id,count,kind);
        Thread.sleep(5000);

    }
}
