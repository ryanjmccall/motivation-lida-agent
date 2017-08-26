package edu.memphis.ccrg.motivation.pam;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.memphis.ccrg.lida.framework.initialization.AgentStarter;
import edu.memphis.ccrg.lida.framework.initialization.ConfigUtils;
import edu.memphis.ccrg.lida.framework.initialization.FactoriesDataXmlLoader;
import edu.memphis.ccrg.lida.framework.shared.Node;
import edu.memphis.ccrg.lida.framework.shared.NodeStructure;
import edu.memphis.ccrg.lida.framework.shared.NodeStructureImpl;
import edu.memphis.ccrg.lida.pam.PamNode;

public class UpdateIncentiveSalienceTest {

	private static final double epsilon=10e-9;
	
	@BeforeClass
	public static void setup() {
		Properties prop = ConfigUtils.loadProperties(AgentStarter.DEFAULT_PROPERTIES_PATH);
		FactoriesDataXmlLoader.loadFactoriesData(prop);
	}
	
	@Test
	public void copyTest(){		
		NodeStructure pam = new NodeStructureImpl("PamNodeImpl", "PamLinkImpl");
		PamNode n = (PamNode) pam.addDefaultNode("a", 0.1, -1.0);
		n.setBaseLevelActivation(0.2);
		n.setIncentiveSalience(0.6);
		n.setBaseLevelIncentiveSalience(0.7);
		
		NodeStructure csm = new NodeStructureImpl();
		Node n2 = csm.addDefaultNode(n);
		assertEquals(n.getActivation(),n2.getActivation(),epsilon);
		assertEquals(n.getIncentiveSalience(),n2.getIncentiveSalience(),epsilon);
	}

}
