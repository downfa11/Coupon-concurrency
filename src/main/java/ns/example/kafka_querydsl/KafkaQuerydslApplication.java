package ns.example.kafka_querydsl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class KafkaQuerydslApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaQuerydslApplication.class, args);
	}

}
