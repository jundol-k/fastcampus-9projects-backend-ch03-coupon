package com.example.couponcore.service;

import com.example.couponcore.repository.redis.RedisRepository;
import com.example.couponcore.repository.redis.dto.CouponRedisEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
    public class AsyncCouponIssueServiceV2 {

    private final RedisRepository redisRepository;
    private final CouponCacheService couponCacheService;

    // set을 이용한 방법
    public void issue(long couponId, long userId) {
        CouponRedisEntity coupon = couponCacheService.getCouponLocalCache(couponId);
        coupon.checkIssuableCoupon();
        issueRequest(couponId, userId, coupon.totalQuantity());
    }

    private void issueRequest(long couponId, long userId, Integer totalIssueQuantity) {
        if (totalIssueQuantity == null) {
            redisRepository.issueRequest(couponId, userId, Integer.MAX_VALUE);
        }
        redisRepository.issueRequest(couponId, userId, totalIssueQuantity);
    }
}
