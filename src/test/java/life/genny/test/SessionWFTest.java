package life.genny.test;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.drools.core.marshalling.impl.ProtobufMessages.KnowledgeBase;
import org.drools.core.time.impl.PseudoClockScheduler;
import org.jbpm.test.JbpmJUnitBaseTestCase.Strategy;
import org.junit.Test;
import org.kie.api.command.Command;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.command.CommandFactory;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import life.genny.models.GennyToken;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwandautils.GennySettings;
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
		QRules qRules = getQRules(userToken); // defaults to user anyway
		String keycloackState = userToken.getCode();
		
		GennyKieSession gks = GennyKieSession.builder()
				.addJbpm("example_start.bpmn")
				.addFact("qRules",qRules)
				.addFact("eb", eventBusMock)
				.addToken(new GennyToken("serviceUser", qRules.getServiceToken()))
				.addToken(userToken)
				.build();
	    
		gks.start();
		
	     for (int i = 0; i<10; i++) {
		    	
		    	sleepMS(1000);
		    	gks.advanceSeconds(1);
		    	System.out.println("Clock :::: " + (i+1) + "sec");
		    	if(i==1) {
		    		gks.injectSignal("login",msg);
		    	}else if(i==3) {
		    		gks.injectSignal("login",msg1);
		    	}else if(i==6){
		    		gks.injectSignal("safal", "null");
		    	}else if(i==8){
		    		gks.injectSignal("anish", "null");
		    	}
		    	
		    }
	     
	    
	    gks.close();

	}

}