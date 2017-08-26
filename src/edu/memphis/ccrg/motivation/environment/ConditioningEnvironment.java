package edu.memphis.ccrg.motivation.environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.memphis.ccrg.lida.environment.EnvironmentImpl;
import edu.memphis.ccrg.lida.framework.tasks.FrameworkTaskImpl;
import edu.memphis.ccrg.lida.framework.tasks.TaskManager;
import edu.memphis.ccrg.motivation.logging.ExperimentPhase;
import edu.memphis.ccrg.motivation.logging.MotivationMeasure;

/**
 * The conditioning environment runs through a list of stage sequences. 
 * Each stage sequence involves a sequence of stages, and can be repeated some number of times. 
 * Individual stages are shown for a parametric number of ticks.
 * @author Ryan J. McCall
 */
public class ConditioningEnvironment extends EnvironmentImpl {

	private static final Logger logger = Logger.getLogger(ConditioningEnvironment.class.getCanonicalName());	
	/*
	 * A list of stage sequences. 
	 */
	private List<List<String>> stageSequences = new ArrayList<List<String>>();
	private List<String> stageSequenceNames = new ArrayList<String>();
	/* 
	 * Index of current stage sequence being run.
	 */
	private int currentSequenceIndex;
	/*
	 * The current sequence being run. 
	 */
	private List<String> currentSequence;
	
	/*
	 * For each stage sequence, specifies how many times the sequence is repeated before continuing to the next one.  
	 */
	private List<Integer> sequenceRepetitions = new ArrayList<Integer>();	
	
	/*
	 * Number of times the currentSequence will be shown. 
	 */
	private int currentSequenceRepetitions;
	/* 
	 * Counts how many times a sequence has been repeated
	 */
	private int sequenceRepetitionCounter;
	
	/*
	 * Index of current stage in the sequence being run. 
	 */
	private int currentStageIndex;
	/*
	 * The current stage being shown. 
	 */
	private String currentStage;
	
	private boolean isExperimentRunning;
	/*
	 * The maximum length of time ticks that each stage is shown. 
	 */
	private int stageLength;
	private static final int DEFAULT_STAGE_LENGTH = 1000;
	/*
	 * Timer counting the number of cycles the experiment has been run.
	 */
	private int experimentTimer;
	
	// Environment state
	private double availableFoodAmount;
	private double foodDeliveryAmount;
	private static final double DEFAULT_FOOD_DELIVERY_AMOUNT = 1;
	
	// Agent state
	private static final double DEFAULT_EATING_SENSATION_CHANGE = 0.01;
	private static final double DEFAULT_EATING_PLEASURE_CHANGE = 0.01;
	private static final double DEFAULT_SICKNESS_CHANGE = 0.01;
	private static final double DEFAULT_HUNGER_CHANGE = 0.01;
	private double eatingSensation;
	private double eatingSensationChange;
	private double eatingPleasureLevel;
	private double eatingPleasureChange;
	private double sicknessLevel;
	private double sicknessChange; 
	private double hungerLevel;	
	private double hungerChange;
	
	// Environmental parameters		
	private static final double DEFAULT_EATING_SENSATION_AMOUNT = 0.5;
	private static final double DEFAULT_EATING_PLEASURE_AMOUNT = 0.5;
	private static final double DEFAULT_SHOT_SICKNESS_AMOUNT = 1.0;
	private static final double DEFAULT_MAX_CONSUMPTION_AMOUNT = 0.5;
	private double eatingSensationAmount;	
	private double eatingPleasureAmount;	
	private double shotSicknessAmount;
	private double maxConsumptionAmount;
	
	//Measures	
	private static final String CUE_STAGE = "light-cue";
	private static final String CONSUME_ACTION = "algorithm.consumeFood";	
	private static final int FOOD_CUP_RECORDINGS_PER_CUE = 5;
	private int consumptionRecordingFrequency;
	private boolean hasConsumptionOccurredThisInterval;
	private double foodCupBehaviorsThisCue;
	private double proportionFoodCupBehaviorTotal;	//Sum of proportions for each sequence repetition
	private boolean isFoodDeliveredThisSequence;
	private double foodConsumedTotal;
	private ExperimentPhase currentExperimentPhase;
	
	/**
	 * stageLength (int) - length of each stage in ticks
	 * consumptionAmount (double) - amount of food consumed each time the agent "eats," max amount of possible 
	 * 								food consumption is 1.0
	 *  
	 * eatingSensationAmount (double) - 
	 * eatingSensationChange (double) the amount the agent's eating sensation decreases by each tick
	 * 
	 * eatingPleasureAmount (double) - 
	 * eatingPleasureChange (double) - the amount the agent's eating signal decreases by each tick
	 * 
	 * shotSicknessAmount (double) - 
	 * sicknessChange (double) - the amount the agent's sickness decreases by each tick
	 * 
	 * hungerChange (double) - the amount the agent's hunger signal increases by each tick
	 * 
	 * experimentalStages (String) - a list of stage sequence defs separated by ";" where each def
	 * specifies the number of repetitions for the sequence, then its stages each separated by a comma.
	 */
	@Override
	public void init(){
		maxConsumptionAmount = getParam("environment.consumptionAmount",DEFAULT_MAX_CONSUMPTION_AMOUNT);
		foodDeliveryAmount = getParam("environment.foodDeliveryAmount",DEFAULT_FOOD_DELIVERY_AMOUNT);
		eatingSensationAmount = getParam("environment.eatingSensationAmount",DEFAULT_EATING_SENSATION_AMOUNT);
		eatingSensationChange = getParam("environment.eatingSensationChange",DEFAULT_EATING_SENSATION_CHANGE);
		eatingPleasureAmount = getParam("environment.eatingPleasureAmount",DEFAULT_EATING_PLEASURE_AMOUNT);		
		eatingPleasureChange = getParam("environment.eatingPleasureChange",DEFAULT_EATING_PLEASURE_CHANGE);
		shotSicknessAmount = getParam("environment.shotSicknessAmount",DEFAULT_SHOT_SICKNESS_AMOUNT);
		sicknessChange = getParam("environment.sicknessChange",DEFAULT_SICKNESS_CHANGE);		
		hungerChange = getParam("environment.hungerChange",DEFAULT_HUNGER_CHANGE);
		stageLength = getParam("environment.stageLength",DEFAULT_STAGE_LENGTH);
		consumptionRecordingFrequency=stageLength/FOOD_CUP_RECORDINGS_PER_CUE;
		initStageSequences();
	}
	
	private void initStageSequences() {
		String stageSequencesString = getParam("environment.experimentalStageSequences","");
		String[] sequenceDefs = stageSequencesString.split(";");
		for(String def: sequenceDefs){			
			String[] splits = def.trim().split(",");
			if(splits.length > 1){
				try{
					Integer reps = Integer.parseInt(splits[0]);
					if(reps > 0){
						sequenceRepetitions.add(reps);	
						StringBuilder sequenceName = new StringBuilder();
						List<String> sequence = new ArrayList<String>();
						for(int i = 1; i < splits.length; i++){
							sequence.add(splits[i].trim());
							sequenceName.append(splits[i].trim()+", ");
						}
						stageSequences.add(sequence);
						stageSequenceNames.add(sequenceName.toString());
					}
				}catch(NumberFormatException e){
					logger.log(Level.WARNING, "Cannot parse repetitions String into an Integer {0}", splits[0]);
				}
			}else{
				logger.log(Level.WARNING, "Bad sequence def {0}", def);
			}
		}
		if(!stageSequences.isEmpty()){
			resetState();
			taskSpawner.addTask(new ConditioningEnvironmentBackgroundTask());
		}else{
			logger.log(Level.INFO,"No stage sequences available for running.",0L);
		}
	}
	
	@Override
	public void resetState() {
		isExperimentRunning = true;
		experimentTimer = 0;
		currentStageIndex = -1;
		currentSequenceIndex = 0;
		sequenceRepetitionCounter = 0;
		availableFoodAmount = 0;
		eatingPleasureLevel = 0.0;
		hungerLevel = 0.0;
		sicknessLevel = 0;
		eatingSensation = 0.0;
//		currentSequence = stageSequences.get(0);
//		currentSequenceRepetitions = sequenceRepetitions.get(0);
		setCurrentSequence(0);
		currentStage = currentSequence.get(0);
	}
	private void setCurrentSequence(int index){
		currentSequence = stageSequences.get(index);
		currentSequenceRepetitions = sequenceRepetitions.get(index);
		switch(index){
			case 0:
				currentExperimentPhase = ExperimentPhase.PhaseOne;
				break;
			case 1:
				currentExperimentPhase = ExperimentPhase.PhaseTwo;
				break;
			case 2:
				currentExperimentPhase = ExperimentPhase.PhaseThree;
				break;
			default:
				currentExperimentPhase = ExperimentPhase.Other;
				break;				
		}
	}
	
	private class ConditioningEnvironmentBackgroundTask extends FrameworkTaskImpl{
		private boolean isFirstTime=true;
		@Override
		protected void runThisFrameworkTask() {
			if(isFirstTime){
				logSequenceStart();
				isFirstTime=false;
			}
			if(isExperimentRunning){
				updateExperimentalStage();
				updateAgentState();
			}
		}
	}
	private void updateExperimentalStage(){
		if(experimentTimer++ % stageLength == 0){ // Advance timer, check if current stage has finished.
			if(currentStageIndex == currentSequence.size()-1){  // If at the end of a sequence
				currentStageIndex = 0;
				processSequenceEnd();
				if(++sequenceRepetitionCounter==currentSequenceRepetitions){ //If done repeating currentSequence
					processRepeatingSequenceEnd();
					sequenceRepetitionCounter=0;
					if(currentSequenceIndex==stageSequences.size()-1){
						//Have performed all sequences, end experiment
						isExperimentRunning=false;
						logger.log(Level.INFO,"\n*** End of Experiment ***",TaskManager.getCurrentTick());
						return;
					}else{ 
						//Advance sequence
						currentSequenceIndex++;
						setCurrentSequence(currentSequenceIndex);
					}
				}
				logSequenceStart();
			}else{ 
				// Advance current stage
				currentStageIndex++;
			}	
			updateCurrentStage();
		}
		if(experimentTimer % consumptionRecordingFrequency == 0){
			if(CUE_STAGE.equalsIgnoreCase(currentStage) && hasConsumptionOccurredThisInterval){
				foodCupBehaviorsThisCue++;
			}
			hasConsumptionOccurredThisInterval=false;
		}
	}
	
	private void processSequenceEnd() {
		logger.log(Level.INFO, "*Sequence Measures*",TaskManager.getCurrentTick());
		double proportionBehavior = foodCupBehaviorsThisCue/FOOD_CUP_RECORDINGS_PER_CUE;
		logger.log(Level.INFO, "{1}~ {2}~ {3}", 
					new Object[]{TaskManager.getCurrentTick(), currentExperimentPhase, MotivationMeasure.Sequence_CueStageFoodCupBehavior, proportionBehavior});
		proportionFoodCupBehaviorTotal += proportionBehavior;
		foodCupBehaviorsThisCue=0;
		//
		double consumed = isFoodDeliveredThisSequence? (foodDeliveryAmount-availableFoodAmount): 0.0;
		isFoodDeliveredThisSequence = false;
		logger.log(Level.INFO, "{1}~ {2}~ {3}",
					new Object[]{TaskManager.getCurrentTick(), currentExperimentPhase, MotivationMeasure.Sequence_FoodConsumed, consumed});
		foodConsumedTotal += consumed;
		availableFoodAmount=0;
	}
	private void processRepeatingSequenceEnd(){
		logger.log(Level.INFO, "\n**Phase Measures**", TaskManager.getCurrentTick());
		double ave = proportionFoodCupBehaviorTotal/currentSequenceRepetitions;
		logger.log(Level.INFO, "{1}~ {2}~ {3}",
					new Object[]{TaskManager.getCurrentTick(), currentExperimentPhase, MotivationMeasure.Phase_CueStageFoodCupBehavior, ave});
		proportionFoodCupBehaviorTotal=0;
		//
		double amount = foodConsumedTotal/currentSequenceRepetitions;
		logger.log(Level.INFO, "{1}~ {2}~ {3}",
				new Object[]{TaskManager.getCurrentTick(), currentExperimentPhase, MotivationMeasure.Phase_FoodConsumed, amount});
		foodConsumedTotal=0;
	}
	private void logSequenceStart(){
		logger.log(Level.INFO,"\nSequence {1}, Repetition ({2} of {3})",
				   new Object[]{TaskManager.getCurrentTick(),currentSequenceIndex+1,
								sequenceRepetitionCounter+1,currentSequenceRepetitions});
		logger.log(Level.INFO, "Sequence Stages {1}", 
					new Object[]{TaskManager.getCurrentTick(),stageSequenceNames.get(currentSequenceIndex)});
	}
	private synchronized void updateCurrentStage(){
		currentStage = currentSequence.get(currentStageIndex);
		logger.log(Level.INFO,"*Stage {1}", new Object[]{TaskManager.getCurrentTick(),currentStage});
		availableFoodAmount = 0;
		if("food-delivery".equalsIgnoreCase(currentStage)){
			availableFoodAmount = foodDeliveryAmount;
			isFoodDeliveredThisSequence = true;
		}else if("licl-shot".equals(currentStage)){
			sicknessLevel = shotSicknessAmount; 
		}
	}
	private synchronized void updateAgentState() {
		eatingPleasureLevel -= eatingPleasureChange;
		if(eatingPleasureLevel < 0){
			eatingPleasureLevel = 0;
		}
		hungerLevel += hungerChange; // increase hunger
		if(hungerLevel > 1.0){
			hungerLevel = 1.0;
		}
		sicknessLevel -= sicknessChange; //decrease sickness
		if(sicknessLevel < 0.0){
			sicknessLevel = 0.0;
		}
		eatingSensation -= eatingSensationChange;
		if(eatingSensation < 0.0){
			eatingSensation = 0.0;
		}
	}
	
	@Override
	public Object getState(Map<String, ?> params) {
		Object state=null;
		if(params != null){
			String modality = (String)params.get("modality");
			if("feeling".equals(modality)){
				state=new double[]{hungerLevel,eatingPleasureLevel,sicknessLevel,eatingSensation};
			}else if("vision".equals(modality)){
				state=currentStage;
			}else if("other".equals(modality)){
				if(availableFoodAmount > 0.0){
					state="food";
				}else{
					state = "";
				}
			}
		}
		return state;
	}	
	/* 
	 * Code executes whenever the environment receives an action
	 */
	@Override
	public synchronized void processAction(Object o) {
		if(o instanceof String){
			if(CONSUME_ACTION.equalsIgnoreCase((String) o)){
				hasConsumptionOccurredThisInterval = true;
				eatingSensation = eatingSensationAmount; 
				if(availableFoodAmount > 0){
					double currentFoodConsumed = 0.0;		
					if(maxConsumptionAmount > availableFoodAmount){ // Less food available than can be eaten in one bite.
						currentFoodConsumed = availableFoodAmount;
						availableFoodAmount = 0;							
					}else{ 	 // Able to eat 'maxConsumptionAmount'
						currentFoodConsumed = maxConsumptionAmount;
						availableFoodAmount -= currentFoodConsumed;
					}
					hungerLevel -= currentFoodConsumed;
					if(hungerLevel < 0){
						hungerLevel = 0.0;
					}
					eatingPleasureLevel = eatingPleasureAmount;
					logger.log(Level.INFO,"(Consumes food {1})",
							new Object[]{TaskManager.getCurrentTick(),currentFoodConsumed});
				}else{
					logger.log(Level.INFO,"(Consumes, but no food.)",TaskManager.getCurrentTick());
				}
			}
		}else{
			logger.log(Level.WARNING,"Can only process actions that are Strings",TaskManager.getCurrentTick());
		}
	}
	
	@Override
	public Object getModuleContent(Object... params){
		Object o = null;
		if(params.length > 0){
			String code = (String) params[0];
			if("currentSequence".equals(code)){
				if(currentSequence!= null){
					StringBuilder sb = new StringBuilder();
					for(String s: currentSequence){
						sb.append(s + ", ");
					}
					o = sb.toString();
				}
			}else if("currentSequenceReps".equals(code)){
				o = sequenceRepetitionCounter;
			}else if("sequenceReps".equals(code)){
				o = currentSequenceRepetitions;
			}else if("currentStage".equals(code)){
				o = currentStage;
			}else if("foodAmount".equals(code)){
				o = round(availableFoodAmount,3);
			}else if("eatingSensation".equals(code)){
				o = round(eatingSensation,3);
			}else if("eatingPleasure".equals(code)){
				o = round(eatingPleasureLevel,3);
			}else if("hungerLevel".equals(code)){
				o = round(hungerLevel,3);
			}else if("sicknessLevel".equals(code)){
				o = round(sicknessLevel,3);
			}else if("stageElapsedTime".equals(code)){
				o = experimentTimer%stageLength;
			}else if("stageLength".equals(code)){
				o = stageLength;
			}
		}
		return o;
	}
	private double round(double d, int places) {
		double factor = Math.pow(10,places);
		return Math.floor(d*factor)/factor;
	}
}