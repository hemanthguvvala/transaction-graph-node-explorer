package com.assignment.graph.exception;

public class NodeNotFoundException extends RuntimeException {

	public NodeNotFoundException(String id) {
		super("Graph node " + id + " does not exist");
	}
}
