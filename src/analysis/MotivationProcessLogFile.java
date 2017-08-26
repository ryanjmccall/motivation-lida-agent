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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import testing.LearningRateRangeTest;

import edu.memphis.ccrg.cla.utils.TestUtils;
import edu.memphis.ccrg.lida.framework.tasks.TaskManager;
import edu.memphis.ccrg.motivation.logging.ExperimentPhase;
import edu.memphis.ccrg.motivation.logging.MotivationMeasure;
import edu.memphis.ccrg.motivation.testing.ParameterRangeTestFlags;

public class MotivationProcessLogFile {

	private static final Logger logger = Logger.getLogger(MotivationProcessLogFile.class.getCanonicalName());
	private static final String defaultLogFilePath = "logs/motivationLog.log";
	
	public static void main(String[] a){
		processParameterRangeLog("Upscale", "true", 30);
	}
	
	public static void processParameterRangeLog(String parameterName, String isLesioned, int trials) {
//		System.out.println("User directory: " + System.getProperty("user.dir"));
		File logFile = new File(defaultLogFilePath);
		List<double[][]> allRangeResults = new ArrayList<double[][]>();
		List<Double> parameterValues = new ArrayList<Double>();
		if (logFile.exists()) {	
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(logFile));
				for(String inputLine = reader.readLine(); inputLine != null; inputLine = reader.readLine()) {
					String[] splits = inputLine.split(LearningRateRangeTest.delimiter);
					if (ParameterRangeTestFlags.PARAMETER_START.toString().equals(splits[0].trim())) {
						double paramVal = Double.parseDouble(splits[1]);
						parameterValues.add(paramVal);
						double[][] paramResult = readParameterResults(reader, trials);
						allRangeResults.add(paramResult);
					}
				}
				reader.close();
			} catch (FileNotFoundException e) {		
				logger.log(Level.WARNING, "Could not find file with path: {1}",
						new Object[]{TaskManager.getCurrentTick(),logFile});
			} catch (IOException e) {
				logger.log(Level.WARNING, "IOException reading file with path: {1}",
						new Object[]{TaskManager.getCurrentTick(),logFile});
			}				
		} else {
			logger.log(Level.WARNING, "Cannot find log file at path: {0}", defaultLogFilePath);
		}
		// Write statistics to a file
		String outputPath = "data/" + parameterName + "/";
		File outputDirectory = new File(outputPath);
		if (!outputDirectory.exists()) {
			outputDirectory.mkdirs();
		}
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath + isLesioned + ".txt"));
			int i = 0;
			writer.write("Lesioned: " + isLesioned + "\nTrials: " + trials + "\n\n");
			writer.write(parameterName + "\t" + 
						ExperimentPhase.PhaseOne + "-" + MotivationMeasure.Phase_CueStageFoodCupBehavior + "\tStd Dev\t" +
						ExperimentPhase.PhaseOne + "-" + MotivationMeasure.Phase_FoodConsumed + "\tStd Dev\t" +
						ExperimentPhase.PhaseTwo + "-" + MotivationMeasure.Phase_CueStageFoodCupBehavior + "\tStd Dev\t" +
						ExperimentPhase.PhaseTwo + "-" + MotivationMeasure.Phase_FoodConsumed + "\tStd Dev\t" +
						ExperimentPhase.PhaseThree + "-" + MotivationMeasure.Phase_CueStageFoodCupBehavior + "\tStd Dev\t" +
						ExperimentPhase.PhaseThree + "-" + MotivationMeasure.Phase_FoodConsumed + "\tStd Dev" +	"\n");
			for(double[][] results: allRangeResults){
				StringBuilder sb = new StringBuilder();
				double paramValue = round(parameterValues.get(i)); 
				sb.append(paramValue);
				for(int m = 0; m < measuresPerTrial; m++){
					double[] pair = results[m];
					sb.append("\t" + round(pair[0]) + "\t" + round(pair[1])); //Mean and Std Dev pair
				}
				writer.write(sb.toString() + "\n");
				i++;
			}
			writer.close();
		} catch (IOException e) {
			logger.log(Level.WARNING, "IOException writing to path: {1}",
					new Object[]{TaskManager.getCurrentTick(),outputPath});
		}
	}

	private static double[][] readParameterResults(BufferedReader reader, int trials) {
		double[][] results = new double[measuresPerTrial][2];
		double[] m1s = new double[trials];
		double[] m2s = new double[trials];
		double[] m3s = new double[trials];
		double[] m4s = new double[trials];
		double[] m5s = new double[trials];
		double[] m6s = new double[trials];
		int currentTrial = 0; 
		try{
			for(String inputLine = reader.readLine(); inputLine != null; inputLine = reader.readLine()) {
				String[] splits = inputLine.split(LearningRateRangeTest.delimiter);
				if(ParameterRangeTestFlags.PARAMETER_END.toString().equals(splits[0].trim())){
					results[0][0] = getAverage(m1s);
					results[0][1] = getStdDev(results[0][0], m1s);
					results[1][0] = getAverage(m2s);
					results[1][1] = getStdDev(results[1][0], m2s);
					results[2][0] = getAverage(m3s);
					results[2][1] = getStdDev(results[2][0], m3s);
					results[3][0] = getAverage(m4s);
					results[3][1] = getStdDev(results[3][0], m4s);
					results[4][0] = getAverage(m5s);
					results[4][1] = getStdDev(results[4][0], m5s);
					results[5][0] = getAverage(m6s);
					results[5][1] = getStdDev(results[5][0], m6s);
					break;
				} else if (ParameterRangeTestFlags.TRIAL_START.toString().equals(splits[0].trim())) {
					double[] trialResults = parseTrial(reader);
					m1s[currentTrial] = trialResults[0];
					m2s[currentTrial] = trialResults[1];
					m3s[currentTrial] = trialResults[2];
					m4s[currentTrial] = trialResults[3];
					m5s[currentTrial] = trialResults[4];
					m6s[currentTrial] = trialResults[5];
					currentTrial++;
				}
			}
		}catch (IOException e) {
			logger.log(Level.WARNING, "IOException parsing parameter.",
						TaskManager.getCurrentTick());
		}		
		return results;
	}
	private static final int measuresPerTrial = 6; //2 measures for each of 3 experiment phases
	private static double[] parseTrial(BufferedReader reader) {
		double[] results = new double[measuresPerTrial];
		try{
			for(String inputLine = reader.readLine(); inputLine != null; inputLine = reader.readLine()) {
				String[] splits = inputLine.split(LearningRateRangeTest.delimiter);
				String phase = splits[0].trim();
				if(ParameterRangeTestFlags.TRIAL_END.toString().equals(phase)){
					break;
				}
				if(splits.length > 1){
					String measure = splits[1].trim();
					if(ExperimentPhase.PhaseOne.toString().equals(phase)){
						if(MotivationMeasure.Phase_CueStageFoodCupBehavior.toString().equals(measure)){
							results[0] = Double.parseDouble(splits[2]);
						}else if(MotivationMeasure.Phase_FoodConsumed.toString().equals(measure)){
							results[1] = Double.parseDouble(splits[2]);
						}
					}else if(ExperimentPhase.PhaseTwo.toString().equals(phase)){
						if(MotivationMeasure.Phase_CueStageFoodCupBehavior.toString().equals(measure)){
							results[2] = Double.parseDouble(splits[2]);
						}else if(MotivationMeasure.Phase_FoodConsumed.toString().equals(measure)){
							results[3] = Double.parseDouble(splits[2]);
						}
					}else if(ExperimentPhase.PhaseThree.toString().equals(phase)){
						if(MotivationMeasure.Phase_CueStageFoodCupBehavior.toString().equals(measure)){
							results[4] = Double.parseDouble(splits[2]);
						}else if(MotivationMeasure.Phase_FoodConsumed.toString().equals(measure)){
							results[5] = Double.parseDouble(splits[2]);
						}
					}		
				}
						
			}
		}catch (IOException e) {
			logger.log(Level.WARNING, "IOException parsing parameter.",
						TaskManager.getCurrentTick());
		}	
		return results;
	}

	public static void writeMeasures(Object[] measures, String inputPath, String outputDirPath) {
		logger.log(Level.INFO, "Writing measures...");
		File logFile = new File(inputPath);
		if (logFile.exists()) {
			File outputDir = new File(outputDirPath);
			if (!outputDir.exists()) {
				outputDir.mkdirs();
			} else {
				MotivationDataUtils.clearDataDirectory(outputDir);
			}
			for (Object m: measures) {
				String measureName = m.toString();
				logger.log(Level.INFO, "Writing measure: {0}", measureName);
				Collection<?> measureValues = MotivationDataUtils.parseMeasure(logFile, measureName);
				String outputPath = outputDirPath + measureName + ".txt";
				MotivationDataUtils.writeResults(outputPath, measureValues);
			}
		} else {
			logger.log(Level.WARNING, "Cannot find log file at path: {0}", inputPath);
		}
		logger.log(Level.INFO, "Finished Writing Measures");
	}

	public static double getAverage(double[] values) {
		double sum = 0.0;
		for (double d: values) {
			sum += d;
		}
		return values.length == 0? 0.0: sum/values.length;
	}	
	public static double getStdDev(double average, double[] values) {
		if(values.length == 0){
			return 0.0;
		}
		double squaredErrorSum = 0.0;
		for (double d: values) {
			squaredErrorSum += Math.pow(average - d, 2.0);
		}
		return Math.sqrt(squaredErrorSum / values.length);
	}
	
	/**
	 * Returns a rounded version of the specified double to 3 significant
	 * figures after the decimal.
	 * 
	 * @param d
	 * @return
	 */
	public static double round(double d) {
		return TestUtils.round(d, 3);
	}

	/**
	 * Returns a rounded version of the specified double. Intended for I/O
	 * purpose.
	 * 
	 * @param d
	 *            a double to be rounded
	 * @param places
	 *            number of decimal places to keep
	 * @return rounded double
	 */
	public static double round(double d, int places) {
		double factor = Math.pow(10, places);
		return Math.floor(d * factor) / factor;
	}
}
