package life.genny.test;

import org.junit.Test;

import life.genny.models.GennyToken;
import life.genny.qwandautils.JsonUtils;
import life.genny.utils.BaseEntityUtils;
import life.genny.utils.CapabilityUtils;
import life.genny.utils.GennyJbpmBaseTest;

public class CapabilityUtilsTest {

	@Test
	public void capabilityUtilsJsonTest() {
		GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken("internmatch", "service", "Service User",
				"service");
		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);

		CapabilityUtils cu = new CapabilityUtils(beUtils);

		cu.addCapability("ADD_USER", "add user");

		String json = JsonUtils.toJson(cu);

		System.out.println("Json="+json);

	}
}
