package com.assignment.graph.dto;

public record NodeSummary(
		
		String id,
		String parentId,
		String name,
		String accountNumber,
		int level
		) {}
