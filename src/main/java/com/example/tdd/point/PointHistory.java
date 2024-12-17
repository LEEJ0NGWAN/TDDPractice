package com.example.tdd.point;

public record PointHistory(
                long id,
                long userId,
                long amount,
                TransactionType type,
                long updateMillis) {
}
