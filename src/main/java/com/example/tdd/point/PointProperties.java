package com.example.tdd.point;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties("point")
public class PointProperties {

    // initialized with default values for preventing logic error.
    private long maximumAmount = 1000000;
    private long maximumUpdateTryCount = 5;
}
