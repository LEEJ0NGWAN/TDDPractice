package com.example.tdd.point;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.tdd.database.PointHistoryTable;
import com.example.tdd.database.UserPointTable;

@DisplayName("유저 포인트 조회기능 테스트")
public class GetPointTest {

    private UserPointTable userPointTable;
    private PointService pointService;

    @BeforeEach
    public void beforeEach() {

        // dummy
        final PointHistoryTable pointHistoryTable = null;
        final PointProperties pointProperties = null;

        userPointTable = new UserPointTable();
        pointService = new PointService(userPointTable, pointHistoryTable, pointProperties);
    }

    @AfterEach
    public void afterEach() {

        // destroy used componenets
        userPointTable = null;
        pointService = null;
    }

    @Test
    @DisplayName("DB에 저장된 유저 포인트는 정상 조회 되어야 한다")
    public void valid_user_get_point_test() {

        // given
        final long userId1 = 1;
        final long amount1 = 10000;
        userPointTable.insertOrUpdate(userId1, amount1);

        final long userId2 = 2239845723498571234L;
        final long amount2 = 20000;
        userPointTable.insertOrUpdate(userId2, amount2);

        // when
        final UserPoint userPoint1 = pointService.getPoint(userId1);
        final UserPoint userPoint2 = pointService.getPoint(userId2);

        // then
        assertEquals(userId1, userPoint1.id());
        assertEquals(amount1, userPoint1.point());

        assertEquals(userId2, userPoint2.id());
        assertEquals(amount2, userPoint2.point());
    }

    @Test
    @DisplayName("DB에 없는 유저의 포인트는 0으로 조회되며 에러가 나지 않아야 한다")
    public void invalid_user_get_point_test() {

        // given
        final long badUserId1 = -2345098845L;
        final long badUserId2 = 0;
        final long badUserId3 = 12092380524089345L;

        final long expectedAmount = 0;

        // when
        final UserPoint userPoint1 = pointService.getPoint(badUserId1);
        final UserPoint userPoint2 = pointService.getPoint(badUserId2);
        final UserPoint userPoint3 = pointService.getPoint(badUserId3);

        // then
        assertEquals(badUserId1, userPoint1.id());
        assertEquals(expectedAmount, userPoint1.point());

        assertEquals(badUserId2, userPoint2.id());
        assertEquals(expectedAmount, userPoint2.point());

        assertEquals(badUserId3, userPoint3.id());
        assertEquals(expectedAmount, userPoint3.point());
    }

    @Test
    @DisplayName("유효하지 않은 유저ID 및 포인트더라도 에러 없이 조회되어야 한다")
    public void invalid_point_test() {

        // given
        final long userId = 1;
        final long badAmount = -234059812080932523L;
        userPointTable.insertOrUpdate(userId, badAmount);

        final long badUserId = -65340827L;
        final long amount = 99999999999999999L;
        userPointTable.insertOrUpdate(badUserId, amount);

        // when
        final UserPoint userBadPoint = pointService.getPoint(userId);
        final UserPoint badUserPoint = pointService.getPoint(badUserId);

        // then
        assertEquals(userId, userBadPoint.id());
        assertEquals(badAmount, userBadPoint.point());

        assertEquals(badUserId, badUserPoint.id());
        assertEquals(amount, badUserPoint.point());
    }
}
