package com.example.tdd.point;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.tdd.database.PointHistoryTable;
import com.example.tdd.database.UserPointTable;

@DisplayName("포인트 사용/충전 동시성 제어 테스트")
public class ConcurrentPointUpdateTest {

    private PointHistoryTable pointHistoryTable;
    private UserPointTable userPointTable;
    private PointProperties pointProperties;
    private PointService pointService;

    @BeforeEach
    public void beforeEach() {

        pointHistoryTable = new PointHistoryTable();
        userPointTable = new UserPointTable();
        pointProperties = new PointProperties();

        pointService = new PointService(
                userPointTable, pointHistoryTable, pointProperties);
    }

    @AfterEach
    public void afterEach() {
        pointHistoryTable = null;
        userPointTable = null;
        pointService = null;
    }

    @Test
    @DisplayName("다중 스레드 환경에서 addPoint와 usePoint 연속 실행해도 종합 결과값은 같아야 한다")
    public void multi_threaded_point_update_test() throws InterruptedException {

        // given
        final Random random = new Random();
        final long userId = 1;
        final long currentAmount = 500000;

        userPointTable.insertOrUpdate(userId, currentAmount);

        final int transactionCounts = 10;
        final long[] amounts = new long[transactionCounts];
        final TransactionType[] types = new TransactionType[transactionCounts];

        final ExecutorService executorService = Executors.newFixedThreadPool(transactionCounts);
        final CountDownLatch latch = new CountDownLatch(transactionCounts);

        final long updatedAmount = currentAmount + IntStream
                .range(0, transactionCounts)
                .mapToLong(index -> {

                    amounts[index] = random.nextLong(10000);
                    types[index] = random.nextBoolean() ? TransactionType.CHARGE : TransactionType.USE;

                    return TransactionType.CHARGE.equals(types[index]) ? amounts[index] : -amounts[index];
                })
                .sum();

        // when
        IntStream
                .range(0, transactionCounts)
                .forEach(i -> executorService.execute(() -> {
                    try {
                        if (TransactionType.CHARGE.equals(types[i]))
                            pointService.addPoint(userId, amounts[i]);
                        else
                            pointService.usePoint(userId, amounts[i]);
                    } finally {
                        latch.countDown();
                    }
                }));

        latch.await();
        executorService.shutdown();

        final UserPoint userPoint = pointService.getPoint(userId);

        // then
        assertEquals(updatedAmount, userPoint.point());
    }

    @Test
    @DisplayName("다중 스레드 환경에서 addPoint와 usePoint를 동시 실행해도 결과는 같아야 한다")
    public void concurrent_update_test() throws InterruptedException {

        // given
        final long userId = 1;
        final long currentAmount = 500;
        final int threadCount = 10;
        final long amount = 100;

        userPointTable.insertOrUpdate(userId, currentAmount);

        ExecutorService executorService = Executors.newFixedThreadPool(2 * threadCount);
        CountDownLatch latch = new CountDownLatch(2 * threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    pointService.addPoint(userId, amount);
                } finally {
                    latch.countDown();
                }
            });

            executorService.execute(() -> {
                try {
                    pointService.usePoint(userId, amount);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        final UserPoint userPoint = pointService.getPoint(userId);

        // then
        assertEquals(currentAmount, userPoint.point());
    }
}
