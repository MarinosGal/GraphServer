package com.graphserver.graph;

import java.util.Arrays;
import java.util.List;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * The basic class for the creation of a Graph
 * @author Marinos Galiatsatos
 */
public class GraphEngine {
	
    private DirectedSparseGraph<String, Number>  graph;

    public GraphEngine() {
    	
    }

	public void createGraph(List<String> nodes, String[][] edges){
		graph = new DirectedSparseGraph<>();
		addVertexes(nodes);
		addEdges2(edges);
	}

	private void addVertexes(List<String> vertexes){
		vertexes.forEach(v -> graph.addVertex(v));
	}

	private void addEdges2(String[][] edges){
		Arrays.asList(edges).forEach(e -> graph.addEdge(Math.random() , e[0], e[1], EdgeType.DIRECTED));
	}

	public DirectedSparseGraph<String, Number> getGraph(){
		return graph;
	}

}
