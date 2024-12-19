package com.example.tdd.point;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PointUpdateFailureException extends RuntimeException {

    private final long amount;
    private final TransactionType type;
}
