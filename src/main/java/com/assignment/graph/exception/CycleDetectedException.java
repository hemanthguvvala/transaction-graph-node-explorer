package com.assignment.graph.exception;

public class CycleDetectedException extends RuntimeException {

	public CycleDetectedException(String nodeId) {
		super("Cycle detected in graph at node " + nodeId);
	}
}
