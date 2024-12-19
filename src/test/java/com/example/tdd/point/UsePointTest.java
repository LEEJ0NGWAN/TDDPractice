package com.example.tdd.point;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.tdd.database.PointHistoryTable;
import com.example.tdd.database.UserPointTable;

@DisplayName("유저 포인트 사용기능 테스트")
public class UsePointTest {

    private PointHistoryTable pointHistoryTable;
    private UserPointTable userPointTable;

    private PointProperties pointProperties;
    private PointService pointService;

    @BeforeEach
    public void beforeEach() {

        pointHistoryTable = new PointHistoryTable();
        userPointTable = new UserPointTable();
        pointProperties = new PointProperties();

        pointService = new PointService(userPointTable, pointHistoryTable, pointProperties);
    }

    @AfterEach
    public void afterEach() {

        pointHistoryTable = null;
        userPointTable = null;
        pointService = null;
    }

    @Test
    @DisplayName("포인트를 정상적으로 사용할 수 있다")
    public void testUsePointSuccess2() {

        // given
        final long userId = 3;
        final long useAmount = 200;
        final long currentAmount = 500;
        final long expectedAmount = currentAmount - useAmount;

        userPointTable.insertOrUpdate(userId, currentAmount);

        // when
        final UserPoint result = pointService.usePoint(userId, useAmount);

        // then
        assertEquals(expectedAmount, result.point());
        assertTrue(
                pointHistoryTable
                        .selectAllByUserId(userId)
                        .stream()
                        .anyMatch(
                                history -> history.amount() == useAmount
                                        && TransactionType.USE.equals(history.type())));
    }

    @Test
    @DisplayName("포인트를 정상적으로 사용할 수 있다")
    public void testUsePointSuccess() {

        // given
        final long userId = 1;
        final long useAmount = 200;
        final long currentAmount = 500;
        final long expectedAmount = currentAmount - useAmount;

        userPointTable.insertOrUpdate(userId, currentAmount);

        // when
        final UserPoint result = pointService.usePoint(userId, useAmount);

        // then
        assertEquals(expectedAmount, result.point());
        assertTrue(
                pointHistoryTable
                        .selectAllByUserId(userId)
                        .stream()
                        .anyMatch(
                                history -> history.amount() == useAmount
                                        && TransactionType.USE.equals(history.type())));
    }

    @Test
    @DisplayName("포인트가 부족하면 사용할 수 없다")
    public void testUsePointInsufficient() {

        // given
        final long userId = 2;
        final long currentAmount = 100;
        final long useAmount = 200;

        userPointTable.insertOrUpdate(userId, currentAmount);

        // when & then
        assertThrows(PointUpdateFailureException.class, () -> {
            pointService.usePoint(userId, useAmount);
        });
    }
}
