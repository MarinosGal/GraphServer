package com.graphserver.graph;

import static com.graphserver.module.monetdb.dao.MonetDbDao.ID;
import static com.graphserver.module.monetdb.dao.MonetDbDao.N1;
import static com.graphserver.module.monetdb.dao.MonetDbDao.N2;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.uci.ics.jung.visualization.control.AbsoluteCrossoverScalingControl;
import org.apache.commons.collections.map.LinkedMap;

/**
 * Helper class for graphs
 * @author Marinos Galiatsatos
 */
public class GraphHelpers {
	/**
	 * Get unique nodes
	 * @param values the values to search for
	 * @return the list with the unique nodes
	 */
	public static List<String> createAndGetUniqueNodes(String[][] values) {
		Set<String> nodes = new LinkedHashSet<>();
		for(int j=0; j<values.length; j++) {
			nodes.add(values[j][0]);
			nodes.add(values[j][1]);
		}
		return Arrays.asList( nodes.toArray(new String[0]) );
	}

	/**
	 * Create and get edges
	 * @param values the values
	 * @return a map of edges
	 */
	public static Map<String, String> createAndGetEdges(String[][] values) {
		return
			Stream.of(values)
					.collect(Collectors.toMap(i->i[0],
										      i->i[1],
							                  (a,b)->b,
							      		  	  LinkedHashMap::new));
	}

	/**
	 * Predefined topology headers
	 * @return the array with the headers
	 */
	public static String[] createTopologyHeaders(){
		return new String[] {ID, N1, N2};
	}

}
