package com.assignment.graph.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import com.assignment.graph.model.GraphNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Repository
public class GraphRepository {
	
	private static final Logger log = LoggerFactory.getLogger(GraphRepository.class);

	private final Map<String, GraphNode> nodeById = new HashMap<>();
	private final  Map<String , List<GraphNode>> childrenByParentId = new HashMap<>();
	
	@Value("${graph.data.location}")
	private Resource dataResource;
	
	@PostConstruct
	public void load() throws IOException{
		ObjectMapper mapper = new ObjectMapper();
		GraphData data = mapper.readValue(dataResource.getInputStream(), GraphData.class);
		List<GraphNode> nodes = (data.nodes == null) ? List.of() : data.nodes;
		
		for(GraphNode node : nodes) {
			nodeById.put(node.getId(), node);
		}
		
		for(GraphNode node: nodes) {
			String parentId = node.getParentId();
			if(parentId != null && nodeById.containsKey(parentId)) {
				childrenByParentId.computeIfAbsent(parentId, key -> new ArrayList<>()).add(node);
			}
		}
		
		for(List<GraphNode> children : childrenByParentId.values()) {
			children.sort(Comparator.comparing(GraphNode::getId));
		}
		
		log.info("Loaded {} graph nodes; {} of them have children",
                nodeById.size(), childrenByParentId.size());
	}
	
	public Optional<GraphNode> findById(String id){
		return Optional.ofNullable(nodeById.get(id));
	}
	
	public boolean exists(String id) {
		return nodeById.containsKey(id);
	}
	
	public List<GraphNode> childrenOf(String id){
		return childrenByParentId.getOrDefault(id, Collections.emptyList());
	}
	
	public Collection<GraphNode> allNodes(){
		return nodeById.values();
	}
	
	private static class GraphData{
		public List<GraphNode> nodes;
	}
}
