package ns.example.kafka_querydsl.domain;

import static ns.example.kafka_querydsl.exception.ErrorCode.INVALID_COUPON_ISSUE_DATE;
import static ns.example.kafka_querydsl.exception.ErrorCode.INVALID_COUPON_ISSUE_QUANTITY;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ns.example.kafka_querydsl.exception.CouponIssueException;

@Entity
@Builder
@Table(name = "coupons")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(value=EnumType.STRING)
    private CouponType couponType;

    private String title;
    private String type;
    private Integer totalQuantity;
    private Integer discountAmount;
    private Integer issuedQuantity;

    @ManyToOne
    private Vendor vendor;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public boolean availableIssueQuantity() {
        if (totalQuantity == null) {
            return true;
        }
        return totalQuantity > issuedQuantity;
    }

    public boolean availableIssueDate() {
        LocalDateTime now = LocalDateTime.now();
        return startDate.isBefore(now) && endDate.isAfter(now);
    }

    public boolean isIssueComplete() {
        LocalDateTime now = LocalDateTime.now();
        return endDate.isBefore(now) || !availableIssueQuantity();
    }

    public void issue() {
        if (!availableIssueQuantity())
            throw new CouponIssueException(INVALID_COUPON_ISSUE_QUANTITY, "발급 가능한 수량을 초과합니다. total : %s, issued: %s".formatted(totalQuantity, issuedQuantity));
        if (!availableIssueDate())
            throw new CouponIssueException(INVALID_COUPON_ISSUE_DATE, "발급 가능한 일자가 아닙니다. request : %s, startDate: %s, endDate: %s".formatted(LocalDateTime.now(), startDate, endDate));

        issuedQuantity++;
    }
}
