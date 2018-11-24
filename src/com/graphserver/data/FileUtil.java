package com.graphserver.data;

import static com.graphserver.module.monetdb.dao.MonetDbDao.INT;
import static com.graphserver.module.monetdb.dao.MonetDbDao.LEFTPAR;
import static com.graphserver.module.monetdb.dao.MonetDbDao.RIGHTPAR;
import static com.graphserver.module.monetdb.dao.MonetDbDao.VARCHAR;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.graphserver.graph.Records;
import com.graphserver.swing.FileChooserEx;

/**
 * A helper class for file manipulation
 * @author Marinos Galiatsatos
 */
public class FileUtil {

	/**
	 * Reads a csv file
	 * @param filename the name of the file
	 * @param del the delimeter of the file
	 * @param hasHeader if file has header or not
	 * @return the array with all the file data
	 */
	public static String[][] readCSV(String filename, String del, boolean hasHeader) {
		try(Stream<String> stream = Files.lines(Paths.get( filename ))){

			List<String[]> readFile = stream.map(i->i.split(del))
							 				.collect(Collectors.toList());
			if (hasHeader) {
				readFile.remove(0);
			}

			return readFile.toArray(new String[0][]);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get the headers from a file
	 * @param filename the file name
	 * @param del the delimiter
	 * @return a list of headers
	 * @throws IOException if the file cannot be read
	 */
	public static List<String> getHeaders(String filename, String del) throws IOException {
        try{
			File file = new File(filename);
        	FileReader fr = new FileReader(file);
        	LineNumberReader ln = new LineNumberReader(fr);
			String s = null;
        	while (ln.getLineNumber() == 0){
            	s = ln.readLine();
            }
			ln.close();
			fr.close();

            if (s == null) {
        		return null;
			}

            String[] firstLine = s.split(del);

            return Stream.of(firstLine).collect(Collectors.toList());

        }catch(Exception e){
        	e.printStackTrace();
        	return null;
        }
	}

	/**
	 * Read file as String
	 * @param file the file name
	 * @return the whole file as String
	 */
	public static String readFile(File file) {

		String content = "";

		try {
			content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return content;
	}

	/**
	 * Creates a specific bitmap file that the solution needs
	 * @param fileName the name of the file to read
	 * @param del the delimiter of that file
	 * @param hasHeader if has headers or not
	 * @param hasRecordIds if has an id column or not
	 * @throws IOException if the file cannot be read/opened
	 */
	public static void createBitmapFile(String fileName, String del, boolean hasHeader, boolean hasRecordIds) throws IOException{
		String emptyString = "";
		int    batchLines = 1000;
		long   lines = Files.lines(Paths.get(fileName)).count();

		StringBuilder[] lineBatch  = new StringBuilder[]{new StringBuilder()};

		AtomicInteger   currentRow = new AtomicInteger(0);
		AtomicInteger   recordId   = new AtomicInteger(1);
		AtomicInteger   count      = new AtomicInteger(0);

		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
		    
		  	stream.sequential().forEach(row->{

	  			StringBuilder stb = new StringBuilder();
	  			StringBuilder stb2 = new StringBuilder();
	  			StringBuilder stb3 = new StringBuilder();
	  			StringBuilder stb4 = new StringBuilder();
	  			String[] split = row.split( del );
	  			AtomicInteger i = new AtomicInteger(0);	
	  			
		  		if (count.get() == 0 && hasHeader) {
		  			//StringBuilder rec = new StringBuilder(emptyString);
		  			Arrays.asList(split).forEach(element->{
		  				if (hasRecordIds && i.get() == 0) {
		  					stb.append(emptyString);
		  				} else {
			  				stb.append("B").append(element)
				   			   .append(i.get() < split.length - 1 ? del : emptyString);
			  				//if (!hasRecordIds && i.get() == 0){
			  				//	rec.append("r").append(del);
			  				//}
		  				}
		  				stb2.append(element).append(del);
		  				i.getAndIncrement();
		  			});

		  		} else {
		  			Arrays.asList(split).forEach(element->{
		  				stb.append(element.toLowerCase().equals("null") ? 0 : 1)
		   			       .append(i.get() < split.length - 1 ? del : emptyString);
		    			
		  				stb2.append(element).append(del);
		  				i.getAndIncrement();
		  			});		   		
		  		}
		  		if (count.get() == 0 && !hasRecordIds) {
		  			stb3.append("r").append(del);
		  		} else {
		  			stb3.append(recordId.getAndIncrement()).append( del );	
		  		}
		    	stb4.append(emptyString);
		  		count.getAndIncrement();
		    	
		    	boolean check = (currentRow.get() % batchLines == 0 || currentRow.get() == lines - 1) && currentRow.get() > 0;
		    	lineBatch[0].append(hasRecordIds ? stb4.toString() + stb2.toString() + stb.toString()+"\n"
		    									 : stb3.toString() + stb2.toString() + stb.toString()+"\n");
		    	if(check){
		    		try {
		    			append("data/bitmaps.csv", lineBatch[0].toString());
		    			lineBatch[0] = new StringBuilder();
		    		} catch (Exception e) {
		    			e.printStackTrace();
		    		}
		    	}
		    	currentRow.getAndIncrement();
		    });

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Writes records' headers to file
	 * @param values the list of the headers
	 */
	public static void writeRecordsHeadersToMetaDataFile(List<String> values){
		String metaDataFilePath	= "metadata/bitmapfilemetadata.txt";
		try{			
			if (fileExists( metaDataFilePath )) {
				deleteFile( metaDataFilePath );
			}

			Path file = Paths.get( metaDataFilePath );
			Files.write(file, values );

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Reads records' headers from meta data file
	 * @return a list of the header names
	 */
	public static List<String> readRecordsHeadersFromMetaDataFile() {
		String metaDataFilePath	= "metadata/bitmapfilemetadata.txt";
		try {
			FileInputStream fis = new FileInputStream(metaDataFilePath);

			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		 
			String line;
			List<String> values = new LinkedList<>();
			while ((line = br.readLine()) != null) {
				values.add(line);
			}			
			br.close();
			return values;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Reads headers from metadata file
	 * @param filename the file name to read the headers
	 * @return the headers as a list
	 */
	public static List<String> readHeadersFromMetadataFile(String filename){
		try{
			return Files.readAllLines(Paths.get(filename));
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Deletes a file
	 * @param filename the file name to delete
	 */
	public static void deleteFile(String filename){
		try {
		    Files.delete(Paths.get(filename));
		} catch (NoSuchFileException x) {
		    System.err.format("%s: no such" + " file or directory%n", Paths.get(filename));
		} catch (DirectoryNotEmptyException x) {
		    System.err.format("%s not empty%n", Paths.get(filename));
		} catch (IOException x) {
		    System.err.println("File permission problems");
		}
	}

	/**
	 * Check if a file exists
	 * @param filename the file name to check
	 * @return true if the file exists
	 */
	public static boolean fileExists(String filename){
		try{
			return Files.exists(Paths.get(filename));			
		}catch(Exception e){
			return false;
		}
	}

	/**
	 * Get Records File Headers Without Rec And Bitmaps
	 * @param headers the input headers
	 * @return the clean headers
	 */
	public static String[] getRecordsFileHeadersWithoutRecAndBitmaps(List<String> headers){
		try{
			int length = (headers.size() - 1) / 2;

			return IntStream.rangeClosed(1, length)
					        .mapToObj(headers::get)
					        .toArray(String[]::new);

		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Create records props
	 * @param headers the input headers
	 * @return a map of database type for every header
	 */
	public static Map<String, String> createRecordsColumnProps(List<String> headers){
		Map<String, String> columnProps = new LinkedHashMap<>();
		for (int i = 0; i < headers.size(); i++) {
			if (i == 0) {
				columnProps.put(headers.get(i), INT);

			} else if (i <= headers.size()/2) {
				columnProps.put(headers.get(i), VARCHAR + LEFTPAR + 50 + RIGHTPAR);

			} else {
				columnProps.put(headers.get(i), INT);
			}
		}

		return columnProps;
	}

	/**
	 * Counts the lines of file
	 * @param fileName the file name to count the lines
	 * @return the number of lines in the file
	 */
	public static int countLinesOfFile(String fileName){
		try{
			BufferedReader reader = new BufferedReader(new FileReader( fileName ));
			int lines = 0;
			while (reader.readLine() != null) {
				lines++;
			}
			reader.close();
			return lines;
		}catch(Exception e){
			e.printStackTrace();
			return -1;
		}
	}

	/*
	 * private methods
	 */

	/**
	 * Appends text to file
	 * @param path the path to bitmaps file
	 * @param text the text to append
	 * @throws Exception if the cannot be opened
	 */
	private static void append(String path, String text) throws Exception {
		File f = new File(path);
		long fileLength = f.length();
		RandomAccessFile raf = new RandomAccessFile(f, "rw");
		raf.seek(fileLength);
		raf.writeBytes(text);
		raf.close();
	}
}
