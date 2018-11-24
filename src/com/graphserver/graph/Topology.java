package com.graphserver.graph;

import static com.graphserver.data.FileUtil.*;
import static com.graphserver.module.monetdb.dao.MonetDbDao.*;
import static com.graphserver.graph.GraphHelpers.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The topology class
 * @author Marinos Galiatsatos
 */
public class Topology {

	private static final String Q1 = "select n1, n2 from topology;";

	private String[][]	 newAllEdges;
	private List<String> uniqueNodes;
	private String		 fileName;
	private String		 del;
	private boolean		 hasHeader;

	public Topology(){

	}

	public Topology(String  fileName,
					String  del,
					boolean hasHeader) {

		this.fileName  = fileName;
		this.del	   = del;
		this.hasHeader = hasHeader;
		init();
	}
	
	private void init() {
		String[][] tv = readCSV(fileName, del, hasHeader);
		String[]   topologyHeaders;

		if (hasHeader) {
			try {
				List<String> topologyHeadersList = getHeaders(fileName, del);

				if (topologyHeadersList == null) {
					return;
				}

				topologyHeaders = topologyHeadersList.toArray(new String[0]);

			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("For some reason can't load headers from topology file! Backup headers created...");
				topologyHeaders = createTopologyHeaders();
			}

		} else {
			topologyHeaders = createTopologyHeaders();
		}

		Map<String, String> columnProps =
				Stream.of(topologyHeaders)
					  .collect(Collectors.toMap(th->th,
							  					th->INT,
							                    (a,b)->b,
							                    LinkedHashMap::new));
		try {
			dbInit();
			dropTable(TOPOLOGY);
			createTable(TOPOLOGY, ID, columnProps);
			if (tv !=  null && tv[0].length != topologyHeaders.length) {
				System.err.println("Topology headers do not match column length of topology values");
				return;
			}
			insertValuesToTable(TOPOLOGY, topologyHeaders, tv);
			dbClose();
			sameTopology();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * load same topology
	 */
	public void sameTopology(){
		try {
			dbInit();
			String[][] result = getQueryResult(Q1, new String[]{N1,N2});
			dbClose();
			uniqueNodes = createAndGetUniqueNodes( result );
			//allEdges    = createAndGetEdges( result );
			newAllEdges = result;
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}		
	}

	/**
	 * Get unique nodes
	 * @return a list of unique nodes
	 */
	public List<String> getUniqueNodes() {
		return uniqueNodes;
	}

	/**
	 * Get all the new edges
	 * @return an array with all the new edges
	 */
	public String[][] getAllNewEdges(){
		return newAllEdges;
	}
}
