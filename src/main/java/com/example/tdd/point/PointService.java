package com.example.tdd.point;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.tdd.database.PointHistoryTable;
import com.example.tdd.database.UserPointTable;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public UserPoint getPoint(long userId) {

        return userPointTable.selectById(userId);
    }

    public List<PointHistory> getPointHistory(long userId) {

        return pointHistoryTable.selectAllByUserId(userId);
    }
}
