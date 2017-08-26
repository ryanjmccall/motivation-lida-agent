package edu.memphis.ccrg.motivation.testing;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import analysis.MotivationProcessLogFile;
import edu.memphis.ccrg.lida.framework.Agent;
import edu.memphis.ccrg.lida.framework.gui.FrameworkGuiFactory;
import edu.memphis.ccrg.lida.framework.initialization.AgentStarter;
import edu.memphis.ccrg.lida.framework.initialization.AgentXmlFactory;
import edu.memphis.ccrg.lida.framework.initialization.ConfigUtils;
import edu.memphis.ccrg.lida.framework.initialization.FactoriesDataXmlLoader;
import edu.memphis.ccrg.lida.framework.initialization.GlobalInitializer;
import edu.memphis.ccrg.lida.framework.tasks.TaskManager;

/**
 * For a range of values of a single parameter, creates a LIDA agent with the value and runs it.
 * For each unique value specified, a unique LIDA agent is created and run.
 * @author Ryan J McCall
 */
public abstract class ParameterRangeTest {

	private static final Logger logger = Logger.getLogger(ParameterRangeTest.class.getCanonicalName());
	protected static final GlobalInitializer globalInitializer = GlobalInitializer.getInstance();
	public static final String delimiter = "~";
	protected static final int hrsTimeConversion = 1000*60*60;

	/**
	 * Initial parameter value tested
	 */
	protected double parameterStart;
	/**
	 * Amount parameter value changes by each iteration
	 */
	protected double parameterStep;
	/**
	 * Limit on parameter values tested
	 */
	protected double parameterEnd;
	
	protected int roundingSignificantFigures = 3; 
	
	private static final int defaultTrials = 30;
	protected int trials = defaultTrials;
	protected boolean isLesioned;
	
	protected boolean isGui;

	/**
	 * Initializes the Parameter range to be tested.
	 */
	public abstract void setupTestParameters();

	/**
	 * Configures specified agent with specified parameter
	 * 
	 * @param agent
	 * @param param
	 */
	public abstract void configureAgent(Agent agent, Object param);
	
	/**
	 * Invoked after the test is finished. E.g., to process the log file.
	 */
	public abstract void runPostTest();

	public void run() {
		long start = System.currentTimeMillis();		
		setupLogging();
		setupTestParameters();
		for (double param = parameterStart; param <= parameterEnd; param += parameterStep) {
			logger.log(Level.INFO, "");
			logger.log(Level.INFO, ParameterRangeTestFlags.PARAMETER_START + delimiter + 
						MotivationProcessLogFile.round(param, roundingSignificantFigures));
			for(int i = 0; i < trials; i++){
				logger.log(Level.INFO, "");
				logger.log(Level.INFO, ParameterRangeTestFlags.TRIAL_START + delimiter + i);
				System.gc(); // Last loop's variables can be cleared.
				Agent agent = loadAgent(isGui);				
				configureAgent(agent, param);
				globalInitializer.clearAttributes();
				TaskManager taskManager = agent.getTaskManager();
				taskManager.setExitOnShutdown(false); //Don't call System.exit(0) 
				taskManager.resumeTasks(); //Starts running Agent
				try {
					synchronized (taskManager) {
						taskManager.wait(); //Wait for TaskManager reach shutdown tick.
					}
				} catch (InterruptedException e) {
					System.out.println("Waiting on TaskManager interrupted.");
				}
				logger.log(Level.INFO, ParameterRangeTestFlags.TRIAL_END + delimiter + i);
			}
			logger.log(Level.INFO, ParameterRangeTestFlags.PARAMETER_END + delimiter + param);
		}
		runPostTest();
		long finish = System.currentTimeMillis();
		double elapsed = finish-start;
		logger.log(Level.INFO,"Experiment runtime: " + round(elapsed/hrsTimeConversion,3) + " hrs");
	}	
	private void setupLogging(){
		Properties agentProperties = ConfigUtils.loadProperties(AgentStarter.DEFAULT_PROPERTIES_PATH);
		String loggingFile = agentProperties.getProperty("lida.logging.configuration");
		if (loggingFile != null) {
			ConfigUtils.configLoggers(loggingFile);
		}
	}

	/**
	 * Method modeled after from AgentStarter
	 * @see AgentStarter.start()
	 */
	protected Agent loadAgent(boolean isGui) {
		Properties agentProperties = ConfigUtils.loadProperties(AgentStarter.DEFAULT_PROPERTIES_PATH);
		FactoriesDataXmlLoader.loadFactoriesData(agentProperties);
		Agent agent = new AgentXmlFactory().getAgent(agentProperties);
		if (agent == null) {
			logger.log(Level.SEVERE, "Failed to create agent.");
		}else{
			logger.log(Level.INFO, "Agent created", 0);
		}
		if(isGui){
			FrameworkGuiFactory.start(agent, agentProperties); 
		}
		return agent;
	}
	
	private double round(double d, int places) {
		double factor = Math.pow(10, places);
		return Math.floor(d * factor) / factor;
	}
}