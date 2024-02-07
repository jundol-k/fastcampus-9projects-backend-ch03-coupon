package com.example.couponcore.service;

import com.example.couponcore.TestConfig;
import com.example.couponcore.exception.CouponIssueException;
import com.example.couponcore.exception.ErrorCode;
import com.example.couponcore.model.Coupon;
import com.example.couponcore.model.CouponIssue;
import com.example.couponcore.model.CouponType;
import com.example.couponcore.repository.mysql.CouponIssueJpaRepository;
import com.example.couponcore.repository.mysql.CouponIssueRepository;
import com.example.couponcore.repository.mysql.CouponJpaRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CouponIssueServiceTest extends TestConfig {

    @Autowired
    CouponIssueService sut;
    @Autowired
    CouponIssueJpaRepository couponIssueJpaRepository;
    @Autowired
    CouponJpaRepository couponJpaRepository;
    @Autowired
    CouponIssueRepository couponIssueRepository;

    @BeforeEach
    void clean() {
        couponJpaRepository.deleteAllInBatch();
        couponIssueJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("쿠폰 발급 내역이 존재하면 예외를 반환한다.")
    void saveCouponIssue_1() {
        CouponIssue couponIssue = CouponIssue.builder()
                .couponId(1L)
                .userId(1L)
                .build();
        
        couponIssueJpaRepository.save(couponIssue);

        CouponIssueException couponIssueException = assertThrows(CouponIssueException.class, () -> {
            sut.saveCouponIssue(1L, 1L);
        });
        Assertions.assertEquals(couponIssueException.getErrorCode(), ErrorCode.DUPLICATED_COUPON_ISSUE);
    }

    @Test
    @DisplayName("쿠폰 발급 내역이 존재하지 않는경우 쿠폰을 발급한다")
    void saveCouponIssue_2() {
        long couponId = 1L;
        long userId = 1L;

        CouponIssue couponIssue = sut.saveCouponIssue(couponId, userId);
        Assertions.assertTrue(couponIssueJpaRepository.findById(couponIssue.getId()).isPresent());
    }

    @Test
    @DisplayName("발급 수량, 기한, 중복 발급 문제가 없다면 쿠폰을 발급한다.")
    void issue_1() {
        long userId = 1L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();

        couponJpaRepository.save(coupon);

        sut.issue(coupon.getId(), userId);

        Coupon savedCoupon = couponJpaRepository.findById(coupon.getId()).get();
        Assertions.assertEquals(savedCoupon.getIssuedQuantity(), 1);

        CouponIssue savedCouponIssue = couponIssueRepository.findFirstCouponIssue(coupon.getId(), userId);
        Assertions.assertNotNull(savedCouponIssue);
    }

    @Test
    @DisplayName("발급 수량에 문제가 있다면 예외를 반환한다.")
    void issue_2() {
        long userId = 1L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(100)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();

        couponJpaRepository.save(coupon);

        CouponIssueException couponIssueException = assertThrows(CouponIssueException.class, () -> {
            sut.issue(coupon.getId(), userId);
        });
        Assertions.assertEquals(couponIssueException.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
    }

    @Test
    @DisplayName("발급 기한에 문제가 있다면 예외를 반환한다.")
    void issue_3() {
        long userId = 1L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().minusDays(1))
                .build();

        couponJpaRepository.save(coupon);

        CouponIssueException couponIssueException = assertThrows(CouponIssueException.class, () -> {
            sut.issue(coupon.getId(), userId);
        });
        Assertions.assertEquals(couponIssueException.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_DATE);
    }

    @Test
    @DisplayName("중복 발급 문제가 있다면 예외를 반환한다.")
    void issue_4() {
        long userId = 1L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();
        couponJpaRepository.save(coupon);

        CouponIssue couponIssue = CouponIssue.builder()
                .couponId(coupon.getId())
                .userId(userId)
                .build();
        couponIssueJpaRepository.save(couponIssue);

        CouponIssueException couponIssueException = assertThrows(CouponIssueException.class, () -> {
            sut.issue(coupon.getId(), userId);
        });
        Assertions.assertEquals(couponIssueException.getErrorCode(), ErrorCode.DUPLICATED_COUPON_ISSUE);
    }

    @Test
    @DisplayName("쿠폰이 존재하지 않는다면 예외를 반환한다.")
    void issue_5() {
        long userId = 1L;
        long couponId = 1L;

        CouponIssueException couponIssueException = assertThrows(CouponIssueException.class, () -> {
            sut.issue(couponId, userId);
        });
        Assertions.assertEquals(couponIssueException.getErrorCode(), ErrorCode.COUPON_NOT_EXIST);
    }
}