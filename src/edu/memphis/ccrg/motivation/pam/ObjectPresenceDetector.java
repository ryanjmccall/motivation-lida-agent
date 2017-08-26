package edu.memphis.ccrg.motivation.pam;

import java.util.HashMap;
import java.util.Map;

import edu.memphis.ccrg.lida.pam.tasks.BasicDetectionAlgorithm;

public class ObjectPresenceDetector extends BasicDetectionAlgorithm {

	protected String modality;
	protected String contentName;
	protected Map<String,Object> parameters = new HashMap<String,Object>();
	
	@Override
	public void init(){
		super.init();
		modality=getParam("modality","");
		contentName = getParam("contentName","");
		parameters.put(contentName, contentName);
	}
	
	@Override
	public double detect() {
		Object content = sensoryMemory.getSensoryContent(modality, parameters);		
		return contentName.equals(content)? 1.0: 0.0;
	}
}