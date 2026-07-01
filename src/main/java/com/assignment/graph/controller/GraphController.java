package com.assignment.graph.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
	public NodeResponse getnode(@PathVariable String id) {
		return graphService.getNode(id);
	}
}
