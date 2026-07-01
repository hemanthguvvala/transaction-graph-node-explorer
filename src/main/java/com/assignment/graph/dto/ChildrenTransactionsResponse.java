package com.assignment.graph.dto;

import java.util.List;

import com.assignment.graph.model.NodeTransaction;

public record ChildrenTransactionsResponse(
        String id,
        String name,
        int level,
        List<NodeTransaction> childrenTransactions
) {}
