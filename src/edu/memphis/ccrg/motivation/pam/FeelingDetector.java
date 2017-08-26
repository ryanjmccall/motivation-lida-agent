package edu.memphis.ccrg.motivation.pam;

public class FeelingDetector extends ObjectPresenceDetector {
	
	@Override
	public double detect() {
		Object content = sensoryMemory.getSensoryContent(modality, parameters);
		return (content instanceof Double)? (Double)content: 0.0;
	}
}