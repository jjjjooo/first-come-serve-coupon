package com.example.coupon.service;

import com.example.coupon.repository.CouponRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class ApplyServiceTest {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private ApplyService applyService;


    @Test
    public void 유저는_동일_아이디로_한번만_응모_가능하다(){
        applyService.apply(1L);

        long count = couponRepository.count();

        assertThat(count).isEqualTo(1);
    }

    /**
     *  경합 상태
     *  메소드 진입 시점에 읽고 있는 스레드 / 쓰고 있는 스레드 서로 다름
     */

    @Test
    public void 쿠폰_100개가_정상_발급_되었다() throws InterruptedException {
        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        for(int i = 0; i < threadCount; i++){
            long userId = i;
            executorService.submit(() -> {
                try {
                    applyService.apply(userId);
                } finally {
                    countDownLatch.countDown();
                }

            });
        }
        countDownLatch.await();
        long count = couponRepository.count();

        assertThat(count).isEqualTo(100);
    }
}