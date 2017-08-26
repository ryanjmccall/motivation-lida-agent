package edu.memphis.ccrg.motivation.sensorymemory;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.memphis.ccrg.lida.framework.tasks.FrameworkTaskImpl;
import edu.memphis.ccrg.lida.framework.tasks.TaskManager;
import edu.memphis.ccrg.lida.sensorymemory.SensoryMemoryImpl;

public class MotivationSensoryMemory extends SensoryMemoryImpl {

	private static final Logger logger = Logger.getLogger(MotivationSensoryMemory.class.getCanonicalName());
	private String visionModality = "vision";
	private String feelingModality = "feeling";
	private String visualContent;
	private double[] feelingContent=new double[4];
	private String foodContent;
	private Map<String,Object> environmentParams = new HashMap<String,Object>();
	
	@Override
	public void init() {		
		int tpr = getParam("sm.senseFrequency",1);		
		taskSpawner.addTask(new SensoryMemoryBackgroundTask(tpr));
	}
	
	private class SensoryMemoryBackgroundTask extends FrameworkTaskImpl{
		public SensoryMemoryBackgroundTask(int tpr) {
			super(tpr);
		}
		@Override
		protected void runThisFrameworkTask() {
			runSensors();			
		}
	}
	
	@Override
	public void runSensors() {
		environmentParams.put("modality", visionModality);		
		synchronized (this) {
			visualContent=(String)environment.getState(environmentParams);	
		}
		environmentParams.put("modality", feelingModality);
		synchronized (this) {
			feelingContent=(double[])environment.getState(environmentParams);
		}
		environmentParams.put("modality", "other");
		synchronized (this) {
			foodContent=(String)environment.getState(environmentParams);
		}
	}
	
	/**
	 * modality - can be "feeling," "regular," "other"
	 * params - for "feeling" can be "hunger-need-feeling" or "food-pleasure-feeling"
	 */
	@Override
	public Object getSensoryContent(String modality, Map<String, Object> params) {
		Object content = null;
		if("feeling".equalsIgnoreCase(modality)){
			if(params != null){
				if(params.containsKey("hunger-need")){
					content = feelingContent[0];
				}else if(params.containsKey("food-pleasure")){
					content = feelingContent[1];
				}else if(params.containsKey("sickness")){
					content = feelingContent[2];
				}else if(params.containsKey("eating")){
					content = feelingContent[3];
				}
			}
		}else if("regular".equalsIgnoreCase(modality)){
			content = visualContent;
		}else if("other".equalsIgnoreCase(modality)){
			content = foodContent;
		}else{
			logger.log(Level.WARNING,"Modality: {1} is not supported.",
					new Object[]{TaskManager.getCurrentTick(),modality});
		}
		return content;
	}
	
	@Override
	public void decayModule(long t) {
	}
}