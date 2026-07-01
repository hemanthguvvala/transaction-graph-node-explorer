package com.assignment.graph.dto;

public record LevelAggregate(
        int level,
        int nodeCount,
        int transactionCount,
        double totalAmount
) {}
