package com.assignment.graph.dto;

import java.util.List;

import com.assignment.graph.model.NodeTransaction;

public record NodeResponse(
		
		String id,
		String parentId,
		String name,
		String accountNumber,
		int level,
		boolean isRoot,
		boolean isLeaf,
		List<NodeSummary> parentChain,
		List<NodeSummary> children,
		List<NodeTransaction> transactions,
		List<NodeTransaction> nextLevelTransactions
		) {

}
