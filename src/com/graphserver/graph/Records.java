package com.graphserver.graph;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.graphserver.data.FileUtil.*;
import static com.graphserver.module.monetdb.dao.MonetDbDao.*;

/**
 * A class for all the records
 * @author Marinos Galiatsatos
 */
public class Records {
	
	public static final String BITMAPFILE = "data/bitmaps.csv";
	
	private List<String> 		headers;
	private String 				newRecordsFile;
	private String 				del; 
	private boolean 			hasHeader; 
	private boolean 			hasRecordIds;

	public Records() {

	}

	public Records(String  newRecordsFile,
				   String  del,
				   boolean hasHeader,
				   boolean hasRecordIds) {

		this.newRecordsFile = newRecordsFile;
		this.del 			= del;
		this.hasHeader 		= hasHeader;
		this.hasRecordIds 	= hasRecordIds;
		init();
	}

	private void init() {
		try {
			if (fileExists( BITMAPFILE )) {
				deleteFile( BITMAPFILE );
			}

			createBitmapFile(newRecordsFile, del, hasHeader, hasRecordIds);
			headers = getHeaders(BITMAPFILE, del);
			writeRecordsHeadersToMetaDataFile(headers);

			Map<String, String> columnProps = createRecordsColumnProps(headers);
			dbInit();
			dropTable(RECORDS);

			String[][] bitMapData = readCSV(BITMAPFILE, del, hasHeader);
			createTable(RECORDS, headers.get(0), columnProps);

			insertValuesToTable(RECORDS, headers.toArray(new String[0]), bitMapData);
			dbClose();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Read records headers from meta data file
	 * @throws IOException if file cannot be opened
	 */
	public void sameRecords() throws IOException {
		this.headers = readRecordsHeadersFromMetaDataFile();
	}

	/**
	 * Get query values for rec id
	 * @param vCols the v columns
	 * @param bCols the b columns
	 * @param recId the rec id
	 * @return a double array with the values
	 */
	public Double[] getQueryValuesForRecId(String[] vCols, String[] bCols, String recId) {
		return getValuesForId(vCols, bCols, recId);
	}

	/**
	 * Get all ids for a user query
	 * @param bCols the b columns
	 * @return an array with the ids
	 */
	public String[] getAllIdsForUserQuery(String[] bCols) {
		return getAllIdsForGivenEdges(bCols);
	}

	/**
	 * G~et all query values For a Rec Id
	 * @param recId the chosen rec id
	 * @return a map with the values per id
	 */
	public Map<String, Double> getAllQueryValuesForRecId(String recId) {
		return getAllValuesForId(getRecordsFileHeadersWithoutRecAndBitmaps(headers), recId);
	}
}
