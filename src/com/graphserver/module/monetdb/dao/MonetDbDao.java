package com.graphserver.module.monetdb.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.graphserver.data.FileUtil.*;

/**
 * Base DAO for connecting to MonetDB
 */
public class MonetDbDao {
	
	private static final String MONETDB_DRIVER_NAME = "nl.cwi.monetdb.jdbc.MonetDriver";
	private static final String CONNECTION_URL      = "jdbc:monetdb://localhost:50000/monetdb";
	private static final String DB_NAME             = "monetdb";
	private static final String DB_PASSWORD         = "monetdb";
	
	public static final String TOPOLOGY  			= "topology";
	public static final String RECORDS   			= "recordskeeper";
	public static final String NODESINFO 			= "nodesinfo";
	public static final String RECORDSINFO 			= "recordsinfo";	
	public static final String N1					= "n1";
	public static final String N2					= "n2";
	public static final String R					= "r";	
	public static final String SELECT				= " SELECT ";
	public static final String FROM					= " FROM ";
	public static final String WHERE				= " WHERE ";
	public static final String SEMICOL				= ";";
	public static final String SPACE				= " ";
	public static final String ID					= "id";
	public static final String AND					= " AND ";
	public static final String PRIMARYKEY			= " PRIMARY KEY ";
	public static final String LEFTPAR				= " ( ";
	public static final String RIGHTPAR				= " ) ";
	public static final String INSERTINTO			= " INSERT INTO ";
	public static final String CREATETABLE			= " CREATE TABLE ";
	public static final String COMMA				= " , ";
	public static final String VALUES				= " VALUES ";
	public static final String DROPTABLE			= " DROP TABLE ";
	public static final String INT					= " INT ";
	public static final String VARCHAR				= " VARCHAR ";
	public static final String EQUALSONE            = "=1 ";
	public static final String EQUALS				= "=";
		
	private static Connection        con;
	private static Statement         stmt;
	private static ResultSet         rs;
	
	public static void dbInit() throws ClassNotFoundException, SQLException{
		Class.forName(MONETDB_DRIVER_NAME);
		con = DriverManager.getConnection(CONNECTION_URL, DB_NAME, DB_PASSWORD);
		stmt = con.createStatement();
	}
	public static void createTable(String tableName, String id, Map<String, String> columnProps){
		try {
			StringBuilder s = new StringBuilder();
			s.append(CREATETABLE).append(tableName).append(LEFTPAR);
			int i = 0;
			for (Map.Entry<String, String> c : columnProps.entrySet()) {
				s.append(c.getKey())
				 .append(SPACE)
				 .append(c.getValue());
				if (i < columnProps.size() - 1) {
					s.append(COMMA);
				} else {
					s.append(COMMA).append(PRIMARYKEY).append(LEFTPAR).append(id).append(RIGHTPAR);
				}
				i++;
			}	
			s.append(RIGHTPAR).append(SEMICOL);
 			stmt.executeUpdate(s.toString());

		} catch (SQLException e) {
			System.err.println("ERROR: Can't create table!");
			e.printStackTrace();
		}
	}

	public static void createTable(String tableName, Map<String, String> columnProps){
		createTable(tableName, ID, columnProps);
	}
	public static void insertValuesToTable(String tableName, String[] columnNames, String[][] values){
		insertValuesToTable(tableName, columnNames, values, null);
	}
	public static void insertValuesToTable(String tableName, String[] columnNames, String[][] values, List<Integer> nominals){
		List<Integer> nominals2 = (nominals != null && !nominals.isEmpty()) ? nominals : new ArrayList<>();

		String headers = Stream.of(columnNames).collect(Collectors.joining(","));
		StringBuilder s = new StringBuilder();
		try {
			s.append(INSERTINTO)
		     .append(tableName)
			 .append(LEFTPAR)
			 .append(headers)
			 .append(RIGHTPAR)
			 .append(VALUES);

			s.append(
				Stream.of(values)
					  .map(row->LEFTPAR+ IntStream.range(0, row.length)
							  					  .mapToObj(i->nominals2.contains(i) ? "'"+row[i]+"'" : row[i])
							  					  .collect(Collectors.joining(","))+RIGHTPAR)
					  .collect(Collectors.joining(",")));

			s.append(SEMICOL);


			stmt.executeUpdate(s.toString());
		} catch (SQLException e) {
			System.err.println("query = "+s.toString());
			e.printStackTrace();
		}
	}

	public static String[][] getQueryResult(String query, String[] columnNames){
		try {
			rs = stmt.executeQuery(query);		
			List<String[]> result = new ArrayList<>();
			while (rs.next()) {
				String[] row = new String[columnNames.length];
				for (int j = 0; j < columnNames.length; j++) {
					row[j] = rs.getString(columnNames[j]);
				}
				result.add( row );
			}
			return result.toArray(new String[0][]);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean dropTable(String tableName){
		try{
			stmt.executeUpdate(DROPTABLE+tableName+SEMICOL);
			System.out.println("INFO: Table "+tableName+" dropped.");
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	
	public static void dbClose(){
		try {
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param vColumns v type columns
	 * @param bColumns b type columns
	 * @return the values given the id
	 */
	public static Double[] getValuesForId(String[] vColumns, String[] bColumns, String id){
		try{
			StringBuilder query = new StringBuilder();
			query.append(SELECT);
				 for (int i = 0; i < vColumns.length; i++) {
					 if (i < vColumns.length - 1) {
						 query.append(vColumns[i])
					 	      .append(COMMA);
					 } else {
						 query.append(vColumns[i]);
					 }
				 }
				 query.append(FROM)
				      .append(RECORDS)
				      .append(WHERE);
				 for (int j = 0; j < bColumns.length; j++) {
					 if (j < bColumns.length - 1) {
						 query.append(bColumns[j])
					 	  	  .append(EQUALSONE)
					 	      .append(AND);
					 } else {
						 query.append(bColumns[j])
						      .append(EQUALSONE);
						      
					 }
				 }			
				 query.append(AND)
				 	  .append(R)
				 	  .append(EQUALS)
				 	  .append(id)
				      .append(SEMICOL);

			rs = stmt.executeQuery(query.toString());

			Double[] values = new Double[vColumns.length];
			while(rs.next()){
				for (int i = 0; i < vColumns.length; i++) {
					values[i] = rs.getDouble(vColumns[i]);
				}
			}		
			return values;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	public static Map<String, Double> getAllValuesForId(String[] allvColumns, String id) {
		try{
			StringBuilder query = new StringBuilder();
			query.append(SELECT);
			 for (int i = 0; i < allvColumns.length; i++) {
				 if (i < allvColumns.length - 1) {
					 query.append(allvColumns[i])
				 	      .append(COMMA);
				 } else {
					 query.append(allvColumns[i]);
				 }
			 }
			 query.append(FROM)
			      .append(RECORDS)
			      .append(WHERE)
			 	  .append(R)
			 	  .append(EQUALS)
			 	  .append(id)
			      .append(SEMICOL);

			 rs = stmt.executeQuery(query.toString());
			 Map<String, Double> notNullValues = new LinkedHashMap<>();

			 while(rs.next()){
				 for (int i = 0; i < allvColumns.length; i++) {
					 String currentResult = rs.getString(allvColumns[i]);
					 if(    currentResult != null
						&& !currentResult.isEmpty()					 
						&& !currentResult.toLowerCase().equals("na"  )
						&& !currentResult.toLowerCase().equals("nan" )
						&& !currentResult.toLowerCase().equals("null") ) {

						notNullValues.put(allvColumns[i], Double.valueOf(currentResult) );
					 }
				 }
			 }				
			 return notNullValues;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	} 
	public static String[] getAllIdsForGivenEdges( String[] bColumns ){
		try{
			StringBuilder query = new StringBuilder();
			query.append(SELECT)
				 .append(R)				 
				 .append(FROM)
				 .append(RECORDS)
				 .append(WHERE);
				 for (int j = 0; j < bColumns.length; j++) {
					 if (j < bColumns.length - 1) {
						 query.append(bColumns[j])
					 	  	  .append(EQUALSONE)
					 	      .append(AND);
					 } else {
						 query.append(bColumns[j])
						      .append(EQUALSONE);						      
					 }
				 }			
		    query.append(SEMICOL);

			rs = stmt.executeQuery(query.toString());
			List<String> values = new ArrayList<>();
			if (rs == null) {
				return null;
			}
			while (rs.next()) {
				values.add(rs.getString( R ));
			}			
			return values.toArray(new String[0]);

		} catch (Exception e) {
			System.err.println("No such edge/s.");
			return null;
		}
	}

}
