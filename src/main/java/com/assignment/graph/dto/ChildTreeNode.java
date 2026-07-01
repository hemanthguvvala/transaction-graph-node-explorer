package com.assignment.graph.dto;

import java.util.List;

import com.assignment.graph.model.NodeTransaction;

public record ChildTreeNode(
        String id,
        String name,
        int level,
        List<NodeTransaction> transactions,
        List<ChildTreeNode> children
) {}
