package com.graphserver.graph;

import com.graphserver.swing.GraphApp;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Basic functions of an array
 * @author Marinos Galiatsatos
 */
public class Functions {
	/**
	 * Calculates the sum of the given array
	 * @param input the input array
	 * @return the sum of the array
	 */
	private static double sum(Double[] input) {
		return
			Stream.of(input)
				  .filter(Objects::nonNull)
				  .mapToDouble(Double::new)
				  .sum();
	}

	/**
	 * Calculates the min of the given array
	 * @param input the input array
	 * @return the min of the given array
	 */
	private static double min(Double[] input){
		return
			Stream.of(input)
				  .filter(Objects::nonNull)
				  .mapToDouble(Double::new)
				  .min()
				  .orElse(0.0);
	}

	/**
	 * Calculates the max of the given array
	 * @param input the input array
	 * @return the max of the input array
	 */
	private static double max(Double[] input){
		return
			Stream.of(input)
					.filter(Objects::nonNull)
					.mapToDouble(Double::new)
					.max()
					.orElse(0.0);
	}

	/**
	 * Calculates average of an array
	 * @param input the input array
	 * @return the average of the given array
	 */
	private static double avg(Double[] input){
		return Stream.of(input)
				     .filter(Objects::nonNull)
				     .mapToDouble(Double::new)
				     .average()
				     .orElse(0.0);
	}

	/**
	 * Choose which function to use
	 * @param selectedFun the selected function
	 * @param values the values to operate the function on
	 * @return the result of the selected function
	 */
	public static String chooseFun(String selectedFun, Double[] values){
		
		switch (selectedFun){
			case(GraphApp.SUM):
				return ""+Functions.sum(values);

			case(GraphApp.MIN):
				return ""+Functions.min(values);

			case(GraphApp.MAX):
				return ""+Functions.max(values);

			case(GraphApp.AVG):
				return ""+Functions.avg(values);

		 	default:
				return null;
		}
	}

}
