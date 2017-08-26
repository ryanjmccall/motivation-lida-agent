package analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.memphis.ccrg.lida.framework.tasks.TaskManager;

public class MotivationDataUtils {

	private static final Logger logger = Logger.getLogger(MotivationDataUtils.class.getCanonicalName());

	/**
	 * For the specified directory, clears all files as well as all sub-directories and their files.
	 * @param file 
	 */
	public static void clearDataDirectory(File file) {
		if(file != null && file.exists()){
			for(File subFile: file.listFiles()){
				if(subFile.isDirectory()){
					for(File f: subFile.listFiles()){
						f.delete();
					}					
				}
				subFile.delete();
			}
		}
	}
	
	/**
	 * Reads file with specified path and returns a collection of all instances of specified measure's value.
	 * Line format assumed is: "measure-name:value"
	 * @param logFilePath
	 * @param measure
	 * @return
	 */
	public static Collection<String> parseMeasure(File logFile, String measure){
		Collection<String> results = new ArrayList<String>();
		if(measure==null){
			logger.log(Level.SEVERE,"Measure was null.");
			return results;
		}
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(logFile));
			for(String inputLine = reader.readLine(); inputLine != null; inputLine = reader.readLine()) {
				String[] splits = inputLine.split(":");
				if (measure.equals(splits[0]) && splits.length>1) {
					results.add(splits[1].trim());
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {		
			results = new ArrayList<String>();
			logger.log(Level.WARNING, "Could not find file with path: {1}",
					new Object[]{TaskManager.getCurrentTick(),logFile});
		} catch (IOException e) {
			results = new ArrayList<String>();
			logger.log(Level.WARNING, "IOException reading file with path: {1}",
					new Object[]{TaskManager.getCurrentTick(),logFile});
		}		
		return results;
	}
	
	/**
	 * Writes each element of the results on a separate line to specified outputPath.
	 * @param outputPath
	 * @param results
	 */
	public static void writeResults(String outputPath, Collection<?> results) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));
			for(Object o: results){
				writer.write(o + "\n");
			}
			writer.close();
		} catch (IOException e) {
			logger.log(Level.WARNING, "IOException writing to path: {1}",
					new Object[]{TaskManager.getCurrentTick(),outputPath});
		}		
	}
}