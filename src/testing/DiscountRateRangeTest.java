package testing;

import analysis.MotivationProcessLogFile;
import edu.memphis.ccrg.lida.framework.Agent;
import edu.memphis.ccrg.lida.framework.FrameworkModule;
import edu.memphis.ccrg.lida.framework.ModuleName;
import edu.memphis.ccrg.lida.motivation.pam.MotivationPerceptualAssociativeMemory;
import edu.memphis.ccrg.lida.motivation.proceduralmemory.MotivationProceduralMemory;
import edu.memphis.ccrg.motivation.testing.ParameterRangeTest;

public class DiscountRateRangeTest extends ParameterRangeTest {

	public static void main(String[] args) {
		new DiscountRateRangeTest().run();
	}

	@Override
	public void setupTestParameters() {
		isLesioned = true;
		parameterStart = 0.50;
		parameterStep = 0.02;
		parameterEnd = 0.99;
		trials = 30;
	}

	@Override
	public void configureAgent(Agent agent, Object param) {		
		FrameworkModule m = agent.getSubmodule(ModuleName.PerceptualAssociativeMemory);
		MotivationPerceptualAssociativeMemory pam = (MotivationPerceptualAssociativeMemory) m;
		pam.setDiscountRate((Double) param);
		//Lesioning
		m = agent.getSubmodule(ModuleName.ProceduralMemory);
		MotivationProceduralMemory pm = (MotivationProceduralMemory) m;
		pm.setLesioned(isLesioned);
	}
	
	@Override
	public void runPostTest(){
		String parameterName = "Discount Rate";
		MotivationProcessLogFile.processParameterRangeLog(parameterName, isLesioned+"", trials);
	}
}
