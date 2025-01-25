package ns.example.kafka_querydsl.repository;

import jakarta.persistence.LockModeType;
import ns.example.kafka_querydsl.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Long countByType(String type);
}
