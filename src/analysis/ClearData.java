package analysis;

import java.io.File;
import java.util.List;

public class ClearData {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String dataPath = "data/learningRate";
		File f = new File(dataPath);
		clearDataDirectory(f);
		
		dataPath = "data/weightedTAS";
		f = new File(dataPath);
		clearDataDirectory(f);
	}
	
	/**
	 * Clears the contents in the specified directory that was originally created by
	 * {@link #writeMeasures(Object[], List, String, String)}.
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

}
