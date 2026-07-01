package com.assignment.graph.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.assignment.graph.dto.ChildTreeNode;
import com.assignment.graph.dto.ChildrenTransactionsResponse;
import com.assignment.graph.dto.LevelAggregate;
import com.assignment.graph.dto.NodeResponse;
import com.assignment.graph.dto.NodeSummary;
import com.assignment.graph.exception.CycleDetectedException;
import com.assignment.graph.exception.InvalidParameterException;
import com.assignment.graph.exception.NodeNotFoundException;
import com.assignment.graph.model.GraphNode;
import com.assignment.graph.model.NodeTransaction;
import com.assignment.graph.repository.GraphRepository;

@Service
public class GraphService {

    private static final int MAX_TRAVERSAL_DEPTH = 5;

    private final GraphRepository graphRepository;

    public GraphService(GraphRepository graphRepository) {
        this.graphRepository = graphRepository;
    }

    public NodeResponse getNode(String id, int maxDepth) {
        if (maxDepth < 0 || maxDepth > 10) {
            throw new InvalidParameterException("maxDepth must be between 0 and 10");
        }

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

        ChildTreeNode childrenTree = null;
        if (maxDepth > 1) {
            int effectiveDepth = Math.min(maxDepth, MAX_TRAVERSAL_DEPTH);
            childrenTree = buildChildrenTree(node, level, 0, effectiveDepth, new HashSet<>());
        }

        List<LevelAggregate> levelAggregates = computeLevelAggregates(node);

        return new NodeResponse(node.getId(), node.getParentId(), node.getName(), node.getAccountNumber(),
                level, isRoot, isLeaf, parentChain, children, node.getTransactions(), nextLevelTransactions,
                childrenTree, levelAggregates);
    }

    public ChildrenTransactionsResponse getChildrenTransactions(String id, Double minAmount, Double maxAmount,
            String txnType) {
        GraphNode node = graphRepository.findById(id).orElseThrow(() -> new NodeNotFoundException(id));
        int level = buildParentChain(node).size();

        List<NodeTransaction> filtered = new ArrayList<>();
        for (GraphNode child : graphRepository.childrenOf(id)) {
            for (NodeTransaction txn : child.getTransactions()) {
                if (matches(txn, minAmount, maxAmount, txnType)) {
                    filtered.add(txn);
                }
            }
        }
        return new ChildrenTransactionsResponse(node.getId(), node.getName(), level, filtered);
    }

    private boolean matches(NodeTransaction txn, Double minAmount, Double maxAmount, String txnType) {
        if (minAmount != null && txn.getAmount() < minAmount) {
            return false;
        }
        if (maxAmount != null && txn.getAmount() > maxAmount) {
            return false;
        }
        if (txnType != null && !txnType.equalsIgnoreCase(txn.getTxnType())) {
            return false;
        }
        return true;
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

    private ChildTreeNode buildChildrenTree(GraphNode node, int nodeLevel, int currentDepth, int maxDepth,
            Set<String> visited) {
        if (!visited.add(node.getId())) {
            throw new CycleDetectedException(node.getId());
        }
        List<ChildTreeNode> children = new ArrayList<>();
        if (currentDepth < maxDepth) {
            for (GraphNode child : graphRepository.childrenOf(node.getId())) {
                children.add(buildChildrenTree(child, nodeLevel + 1, currentDepth + 1, maxDepth, visited));
            }
        }
        visited.remove(node.getId());
        return new ChildTreeNode(node.getId(), node.getName(), nodeLevel, node.getTransactions(), children);
    }

   
    private List<LevelAggregate> computeLevelAggregates(GraphNode root) {
        Map<Integer, int[]> counts = new TreeMap<>();   
        Map<Integer, Double> amounts = new TreeMap<>(); 
        Set<String> visited = new HashSet<>();

        Deque<GraphNode> nodeStack = new ArrayDeque<>();
        Deque<Integer> depthStack = new ArrayDeque<>();
        nodeStack.push(root);
        depthStack.push(0);

        while (!nodeStack.isEmpty()) {
            GraphNode node = nodeStack.pop();
            int depth = depthStack.pop();
            if (!visited.add(node.getId())) {
                throw new CycleDetectedException(node.getId());
            }

            int[] c = counts.computeIfAbsent(depth, k -> new int[2]);
            c[0] += 1;
            c[1] += node.getTransactions().size();

            double sum = 0;
            for (NodeTransaction txn : node.getTransactions()) {
                sum += Math.abs(txn.getAmount());
            }
            amounts.merge(depth, sum, Double::sum);

            for (GraphNode child : graphRepository.childrenOf(node.getId())) {
                nodeStack.push(child);
                depthStack.push(depth + 1);
            }
        }

        List<LevelAggregate> result = new ArrayList<>();
        for (Map.Entry<Integer, int[]> entry : counts.entrySet()) {
            int lvl = entry.getKey();
            int[] c = entry.getValue();
            result.add(new LevelAggregate(lvl, c[0], c[1], amounts.getOrDefault(lvl, 0.0)));
        }
        return result;
    }

    private NodeSummary toSummary(GraphNode node, int level) {
        return new NodeSummary(node.getId(), node.getParentId(), node.getName(), node.getAccountNumber(), level);
    }
}
