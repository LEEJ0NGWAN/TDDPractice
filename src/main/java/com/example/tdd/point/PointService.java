package com.example.tdd.point;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.example.tdd.database.PointHistoryTable;
import com.example.tdd.database.UserPointTable;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {

    private static final ConcurrentHashMap<Long, AtomicLong> updatingUserPointMap = new ConcurrentHashMap<>();

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;
    private final PointProperties properties;

    public UserPoint getPoint(long userId) {

        return userPointTable.selectById(userId);
    }

    public List<PointHistory> getPointHistory(long userId) {

        return pointHistoryTable.selectAllByUserId(userId);
    }

    public UserPoint addPoint(long userId, long amount) {

        return updatePoint(userId, amount, TransactionType.CHARGE);
    }

    public UserPoint usePoint(long userId, long amount) {

        return updatePoint(userId, amount, TransactionType.USE);
    }

    private UserPoint updatePoint(long userId, long amount, TransactionType type) {

        int updateTryCount = 0;

        UserPoint result = null;

        while (updateTryCount++ < properties.getMaximumUpdateTryCount()) {

            final AtomicLong userPoint = updatingUserPointMap.computeIfAbsent(userId,
                    id -> new AtomicLong(userPointTable.selectById(id).point()));

            final long currentAmount = userPoint.get();
            final long updatedAmount = currentAmount
                    + (TransactionType.CHARGE.equals(type) ? amount : -amount);

            if (updatedAmount < 0 || properties.getMaximumAmount() < updatedAmount)
                break;

            // cas atomic
            final boolean updated = userPoint.compareAndSet(currentAmount, updatedAmount);

            if (updated) {

                pointHistoryTable.insert(userId, amount, type, System.currentTimeMillis());

                boolean dbUpdated = false;
                while (!dbUpdated) {

                    result = userPointTable.insertOrUpdate(userId, userPoint.get());

                    if (result.point() == userPoint.get())
                        dbUpdated = true;
                }
                break;
            }
        }

        // 만약 작업중인 쓰레드가 없으면 concurrent 메모리에서 유저 포인트 제거ㅋ
        updatingUserPointMap
                .computeIfPresent(userId,
                        (key, value) -> value.get() == userPointTable.selectById(userId).point() ? null : value);

        if (result == null)
            throw new PointUpdateFailureException(amount, type);

        return result;
    }
}
