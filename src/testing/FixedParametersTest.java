package testing;

import analysis.MotivationProcessLogFile;
import edu.memphis.ccrg.lida.framework.Agent;
import edu.memphis.ccrg.lida.framework.FrameworkModule;
import edu.memphis.ccrg.lida.framework.ModuleName;
import edu.memphis.ccrg.lida.motivation.proceduralmemory.MotivationProceduralMemory;
import edu.memphis.ccrg.motivation.testing.ParameterRangeTest;

public class FixedParametersTest extends ParameterRangeTest {

	public static void main(String[] args) {
		new FixedParametersTest().run();
	}

	@Override
	public void setupTestParameters() {
		isLesioned = false;
		parameterStart = 0.0;
		parameterStep = 1.0;
		parameterEnd = 0.0;
		trials = 30;
	}

	@Override
	public void configureAgent(Agent agent, Object param) {		
		FrameworkModule m = agent.getSubmodule(ModuleName.ProceduralMemory);
		MotivationProceduralMemory pm = (MotivationProceduralMemory) m;
		pm.setLesioned(isLesioned);
	}
	
	@Override
	public void runPostTest(){
		String parameterName = "Fixed Parameters";
		MotivationProcessLogFile.processParameterRangeLog(parameterName, isLesioned+"", trials);
	}
}
