package life.genny.test;

import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import life.genny.models.GennyToken;
import life.genny.qwanda.message.QEventMessage;
import life.genny.rules.QRules;
import life.genny.rules.listeners.JbpmInitListener;

public class SessionWFTest extends GennyJbpmBaseTest {

	 private static final Logger logger = LoggerFactory.getLogger(SessionWFTest.class);
	
	//private static final String WFE_TIMER_INTERVAL = "rulesCurrent/shared/_BPMN_WORKFLOWS/XXXtimer5.bpmn";
	//private static final String WFE_TIMER_EXAMPLE_START = "rulesCurrent/shared/_BPMN_WORKFLOWS/TimerExamples/example_timer_start.bpmn";


	public SessionWFTest() {
		super(false);
	}
	
	
	@Test(timeout = 300000)	
	public void sessionWorkFLow() {
		
		//Creating two Qevent message for a simulation of two people login
		
		QEventMessage msg = new QEventMessage("EVT_MSG", "LOGIN");
		msg.data.setValue("safal");
		
		QEventMessage msg1 = new QEventMessage("EVT_MSG", "LOGIN");
		msg1.data.setValue("anish");

		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
		QRules qRules = getQRules(userToken); // defaults to user anyway
		String keycloackState = userToken.getCode();
		
		GennyKieSession gks = GennyKieSession.builder(serviceToken)
				.addJbpm("example_start.bpmn")
				.addFact("qRules",qRules)
				.addFact("eb", eventBusMock)
				.addToken(new GennyToken("serviceUser", qRules.getServiceToken()))
				.addToken(userToken)
				.build();
	    
		gks.start();
		
		gks.advanceSeconds(2, true);
		
		gks.injectSignal("login",msg);
		
		gks.advanceSeconds(2, true);
		
		gks.injectSignal("login",msg1);
	
		gks.advanceSeconds(2, true);
		
		gks.injectSignal("safal", "null");

		gks.advanceSeconds(2, true);
		
		gks.injectSignal("anish", "null");

	    gks.close();

	}

}