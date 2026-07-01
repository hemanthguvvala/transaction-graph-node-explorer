package com.assignment.graph.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.assignment.graph.dto.ChildrenTransactionsResponse;
import com.assignment.graph.dto.NodeResponse;
import com.assignment.graph.service.GraphService;

@RestController
@RequestMapping("/api/graph/nodes")
public class GraphController {

	private final GraphService graphService;

	public GraphController(GraphService graphService) {
		this.graphService = graphService;
	}

	@GetMapping("/{id}")
	public NodeResponse getNode(@PathVariable String id,
			@RequestParam(defaultValue = "1") int maxDepth) {
		return graphService.getNode(id, maxDepth);
	}

	@GetMapping("/{id}/children-transactions")
	public ChildrenTransactionsResponse getChildrenTransactions(
			@PathVariable String id,
			@RequestParam(required = false) Double minAmount,
			@RequestParam(required = false) Double maxAmount,
			@RequestParam(required = false) String txnType) {
		return graphService.getChildrenTransactions(id, minAmount, maxAmount, txnType);
	}
}
