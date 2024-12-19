package com.example.tdd.point;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.tdd.database.PointHistoryTable;
import com.example.tdd.database.UserPointTable;

@DisplayName("포인트 충전/사용 내역 조회 테스트")
public class GetPointHistoryTest {

    private PointHistoryTable pointHistoryTable;
    private PointService pointService;

    @BeforeEach
    public void beforeEach() {

        // dummy
        final UserPointTable userPointTable = null;
        final PointProperties pointProperties = null;

        pointHistoryTable = new PointHistoryTable();
        pointService = new PointService(userPointTable, pointHistoryTable, pointProperties);
    }

    @AfterEach
    public void afterEach() {

        // destroy components
        pointHistoryTable = null;
        pointService = null;
    }

    @Test
    @DisplayName("유저의 충전/사용 내역이 올바른 순서로 정상 조회되어야 한다")
    public void valid_user_point_history_test() {

        // given
        final long userId = 1;
        final Random random = new Random();

        final int transactionSizes = random.nextInt(10, 20);
        final long[] amounts = new long[transactionSizes];
        final TransactionType[] types = new TransactionType[transactionSizes];

        IntStream
                .range(0, transactionSizes)
                .forEach(index -> {

                    amounts[index] = random.nextLong(100000);
                    types[index] = random.nextBoolean() ? TransactionType.CHARGE : TransactionType.USE;

                    pointHistoryTable.insert(userId, amounts[index], types[index], index);
                });

        // when
        final List<PointHistory> histories = pointService.getPointHistory(userId);

        // then
        assertEquals(transactionSizes, histories.size());
        IntStream
                .range(0, transactionSizes)
                .forEach(index -> {

                    final PointHistory history = histories.get(index);

                    assertEquals(userId, history.userId());
                    assertEquals(amounts[index], history.amount());
                    assertEquals(types[index], history.type());
                });
    }

    @Test
    @DisplayName("내역이 없는 유저의 충전/사용 내역 조회 시 빈 리스트를 반환한다")
    public void invalid_user_point_history_test() {

        // given
        final long userId = 99;

        // when
        final List<PointHistory> histories = pointService.getPointHistory(userId);

        // then
        assertNotNull(histories);
        assertTrue(histories.isEmpty());
    }
}
