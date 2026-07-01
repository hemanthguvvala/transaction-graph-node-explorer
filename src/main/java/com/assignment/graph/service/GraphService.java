package com.assignment.graph.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.assignment.graph.dto.NodeResponse;
import com.assignment.graph.dto.NodeSummary;
import com.assignment.graph.exception.CycleDetectedException;
import com.assignment.graph.exception.NodeNotFoundException;
import com.assignment.graph.model.GraphNode;
import com.assignment.graph.model.NodeTransaction;
import com.assignment.graph.repository.GraphRepository;

@Service
public class GraphService {

	private final GraphRepository graphRepository;

	public GraphService(GraphRepository graphRepository) {
		this.graphRepository = graphRepository;
	}

	public NodeResponse getNode(String id) {
		GraphNode node = graphRepository.findById(id).orElseThrow(() -> new NodeNotFoundException(id));

		List<NodeSummary> parentChain = buildParentChain(node);
		int level = parentChain.size();

		boolean isRoot = node.getParentId() == null || !graphRepository.exists(node.getParentId());

		List<GraphNode> childNodes = graphRepository.childrenOf(id);
		boolean isLeaf = childNodes.isEmpty();

		List<NodeSummary> children = new ArrayList<>();

		for (GraphNode child : childNodes) {
			children.add(toSummary(child, level + 1));
		}

		List<NodeTransaction> nextLevelTransactions = new ArrayList<>();
		for (GraphNode child : childNodes) {
			nextLevelTransactions.addAll(child.getTransactions());
		}

		return new NodeResponse(node.getId(), node.getParentId(), node.getName(), node.getAccountNumber(), level,
				isRoot, isLeaf, parentChain, children, node.getTransactions(), nextLevelTransactions);

	}

	private List<NodeSummary> buildParentChain(GraphNode node) {
		LinkedList<GraphNode> ancestors = new LinkedList<>();
		Set<String> visited = new HashSet<>();
		visited.add(node.getId());

		GraphNode current = node;
		while (current.getParentId() != null) {
			String parentId = current.getParentId();
			GraphNode parent = graphRepository.findById(parentId).orElse(null);
			if (parent == null) {
				break;
			}
			if (visited.contains(parentId)) {
				throw new CycleDetectedException(parentId);
			}
			visited.add(parentId);
			ancestors.addFirst(parent);
			current = parent;
		}
		List<NodeSummary> chain = new ArrayList<>();
		int lvl = 0;
		for (GraphNode ancestor : ancestors) {
			chain.add(toSummary(ancestor, lvl++));
		}
		return chain;
	}

	private NodeSummary toSummary(GraphNode node, int level) {
		return new NodeSummary(node.getId(), node.getParentId(), node.getName(), node.getAccountNumber(), level);
	}
}
