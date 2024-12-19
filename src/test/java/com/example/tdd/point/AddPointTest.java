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

@DisplayName("유저 포인트 충전기능 테스트")
public class AddPointTest {

    private static final long MAXIMUM_AMOUNT = 1000000L;

    private PointHistoryTable pointHistoryTable;
    private UserPointTable userPointTable;

    private PointProperties pointProperties;
    private PointService pointService;

    @BeforeEach
    public void beforeEach() {

        pointHistoryTable = new PointHistoryTable();
        userPointTable = new UserPointTable();
        pointProperties = new PointProperties();
        pointProperties.setMaximumAmount(MAXIMUM_AMOUNT);

        pointService = new PointService(userPointTable, pointHistoryTable, pointProperties);
    }

    @AfterEach
    public void afterEach() {

        pointHistoryTable = null;
        userPointTable = null;
        pointService = null;
    }

    @Test
    @DisplayName("포인트를 정상적으로 추가할 수 있다")
    public void testAddPointSuccess() {

        // given
        final long userId = 1;
        final long addAmount = 300;
        final long currentAmount = 500;
        final long expectedAmount = currentAmount + addAmount;

        userPointTable.insertOrUpdate(userId, currentAmount);

        // when
        final UserPoint result = pointService.addPoint(userId, addAmount);

        // then
        assertEquals(expectedAmount, result.point());
        assertTrue(
                pointHistoryTable
                        .selectAllByUserId(userId)
                        .stream()
                        .anyMatch(
                                history -> history.amount() == addAmount
                                        && TransactionType.CHARGE.equals(history.type())));
    }

    @Test
    @DisplayName("최대값을 초과하여 포인트를 추가할 수 없다")
    public void testAddPointExceedsMaximum() {

        // given
        final long userId = 1;
        final long currentAmount = MAXIMUM_AMOUNT - 100;
        final long addAmount = 600;

        userPointTable.insertOrUpdate(userId, currentAmount);

        // when & then
        assertThrows(PointUpdateFailureException.class, () -> {
            pointService.addPoint(userId, addAmount);
        });
    }
}
