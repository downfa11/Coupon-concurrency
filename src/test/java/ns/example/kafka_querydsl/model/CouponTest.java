package ns.example.kafka_querydsl.model;

import static ns.example.kafka_querydsl.exception.ErrorCode.INVALID_COUPON_ISSUE_DATE;
import static ns.example.kafka_querydsl.exception.ErrorCode.INVALID_COUPON_ISSUE_QUANTITY;

import java.time.LocalDateTime;
import ns.example.kafka_querydsl.domain.Coupon;
import ns.example.kafka_querydsl.exception.CouponIssueException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CouponTest {

    @Test
    void 발급_수량이_남아있다면_True를_반환해야함() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .build();
        // when
        boolean result = coupon.availableIssueQuantity();
        // then
        Assertions.assertTrue(result);
    }

    @Test
    void 발급_수량이_소진되면_False를_반환해야함() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(100)
                .build();
        // when
        boolean result = coupon.availableIssueQuantity();
        // then
        Assertions.assertFalse(result);
    }

    @Test
    void 최대_발급_수량이_설정안되면_True를_반환해야함() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(null)
                .issuedQuantity(100)
                .build();
        // when
        boolean result = coupon.availableIssueQuantity();
        // then
        Assertions.assertTrue(result);
    }

    @Test
    void 발급_기간이_시작하지_않았다면_False를_반환해야함() {
        // given
        Coupon coupon = Coupon.builder()
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .build();
        // when
        boolean result = coupon.availableIssueDate();
        // then
        Assertions.assertFalse(result);
    }

    @Test
    void 발급_기간에_해당하면_True를_반환해야함() {
        // given
        Coupon coupon = Coupon.builder()
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .build();
        // when
        boolean result = coupon.availableIssueDate();
        // then
        Assertions.assertTrue(result);
    }

    @Test
    void 발급_기간이_종료되면_False를_반환해야함() {
        // given
        Coupon coupon = Coupon.builder()
                .startDate(LocalDateTime.now().minusDays(2))
                .endDate(LocalDateTime.now().minusDays(1))
                .build();
        // when
        boolean result = coupon.availableIssueDate();
        // then
        Assertions.assertFalse(result);
    }

    @Test
    void 발급_수량과_기간이_유효하면_발급_성공() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .build();
        // when
        coupon.issue();
        // then
        Assertions.assertEquals(coupon.getIssuedQuantity(), 100);
    }

    @Test
    void 발급_수량을_초과하면_예외_처리해야함() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(100)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .build();
        // when & then
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, coupon::issue);
        Assertions.assertEquals(exception.getErrorCode(), INVALID_COUPON_ISSUE_QUANTITY);
    }

    @Test
    void 행사_기간이_아니면_예외_처리해야함() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .build();
        // when & then
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, coupon::issue);
        Assertions.assertEquals(exception.getErrorCode(), INVALID_COUPON_ISSUE_DATE);
    }

    @Test
    void 행사_기간이_종료되면_True를_반환해야함() {
        // given
        Coupon coupon = Coupon.builder()
                .startDate(LocalDateTime.now().minusDays(2))
                .endDate(LocalDateTime.now().minusDays(1))
                .totalQuantity(100)
                .issuedQuantity(0)
                .build();
        // when
        boolean result = coupon.isIssueComplete();
        // then
        Assertions.assertTrue(result);
    }

    @Test
    void 발급_가능한_수량이_없으면_True를_반환해야함() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(100)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .build();
        // when
        boolean result = coupon.isIssueComplete();
        // then
        Assertions.assertTrue(result);
    }

    @Test
    void 발급_기간과_수량이_유효하면_False를_반환해야함() {
        // given
        Coupon coupon = Coupon.builder()
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .totalQuantity(100)
                .issuedQuantity(0)
                .build();
        // when
        boolean result = coupon.isIssueComplete();
        // then
        Assertions.assertFalse(result);
    }
}