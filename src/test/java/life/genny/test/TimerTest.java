package life.genny.test;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


import org.drools.core.time.impl.PseudoClockScheduler;
import org.jbpm.test.JbpmJUnitBaseTestCase.Strategy;
import org.junit.Test;
import org.kie.api.command.Command;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.command.CommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import life.genny.models.GennyToken;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwandautils.GennySettings;
import life.genny.rules.QRules;
import life.genny.rules.listeners.JbpmInitListener;

public class TimerTest extends GennyJbpmBaseTest {

	 private static final Logger logger = LoggerFactory.getLogger(TimerTest.class);
	
	//private static final String WFE_TIMER_INTERVAL = "rulesCurrent/shared/_BPMN_WORKFLOWS/XXXtimer5.bpmn";
	private static final String WFE_TIMER_EXAMPLE_START = "rulesCurrent/shared/_BPMN_WORKFLOWS/TimerExamples/example_timer_start.bpmn";
	private static final String WFE_TIMER_EXAMPLE_4 = "rulesCurrent/shared/_BPMN_WORKFLOWS/TimerExamples/timer_example_workflow_4.bpmn";
	private static final String WFE_TIMER_EXAMPLE_1 = "rulesCurrent/shared/_BPMN_WORKFLOWS/TimerExamples/timer_example_workflow_1.bpmn";
	private static final String WFE_TIMER_EXAMPLE_2 = "rulesCurrent/shared/_BPMN_WORKFLOWS/TimerExamples/timer_example_workflow_2.bpmn";
	private static final String WFE_TIMER_EXAMPLE_3 = "rulesCurrent/shared/_BPMN_WORKFLOWS/TimerExamples/timer_example_workflow_3.bpmn";
	private static final String WFE_TIMER_DELAY = "rulesCurrent/shared/_BPMN_WORKFLOWS/XXXTimerStart2.bpmn";
	private static final String WFE_TIMER_INTERVAL = "timer5.bpmn";

	private static final String WFE_SEND_FORMS = "rulesCurrent/shared/_BPMN_WORKFLOWS/send_forms.bpmn";
	private static final String WFE_SHOW_FORM = "rulesCurrent/shared/_BPMN_WORKFLOWS/show_form.bpmn";
	private static final String WFE_AUTH_INIT = "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/auth_init.bpmn";
	private static final String WFE_SEND_LLAMA = "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/send_llama.bpmn";


	public TimerTest() {
		super(false);
	}

	
	@Test
	public void timerIntervalTest() {
		
		
		GennyKieSession gks = GennyKieSession.builder()
				.addJbpm( WFE_TIMER_INTERVAL)
				.build();
		
	
	     gks.startProcess("TimerTest");
	    
	    // Timer is set to 60 seconds, so advancing with 70.
	    gks.advanceSeconds(20,false);

	    // Test that the timer has triggered.
	   // gks.assertNodeTriggered("Goodbye Process");
	   // gks.assertProcessInstanceCompleted();
	    
	    gks.close();
	}
	


	
	@Test(timeout = 300000)	
	public void testTimerProcess() {
		
		QEventMessage msg = new QEventMessage("EVT_MSG", "AUTH_INIT1");

		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules qRules = getQRules(userToken); // defaults to user anyway
		
		GennyKieSession gks = GennyKieSession.builder()
				.addJbpm("example_timer_start.bpmn")
				.addJbpm("timer_example_workflow_1.bpmn")
				.addJbpm("timer_example_workflow_2.bpmn")
				.addJbpm("timer_example_workflow_3.bpmn")
				.addJbpm("timer_example_workflow_4.bpmn")
				.addFact("qRules",qRules)
				.addFact("msg",msg)
				.addFact("eb", eventBusMock)
				.addToken(new GennyToken("serviceUser", qRules.getServiceToken()))
				.addToken(userToken)
				.build();
		
	   //  gks.startProcess("TimerTest");
	     gks.start();
		    
	    gks.advanceSeconds(20,false);

	    
	    gks.close();
	

	}




	

//	@Test
	public void testTimerActivated() {
//	    ProcessInstance pInstance = kieSession.startProcess("sample-process");
//	    long pInstanceId = pInstance.getId();
//
//	    PseudoClockScheduler sessionClock = kieSession.getSessionClock();
//	    // Timer is set to 60 seconds, so advancing with 70.
//	    sessionClock.advanceTime(70, TimeUnit.SECONDS);
//
//	    // Test that the timer has triggered.
//	    assertNodeTriggered(pInstanceId, "Goodbye Process");
//	    assertProcessInstanceCompleted(pInstanceId);
	}
//  @Test
//  public void timerTest() {
//      PersistentTests.LOG.debug("********************* jBPM unit test Timer Test ************************");
//
//      final RuntimeManager runtimeManager = createRuntimeManager("rulescurrent/shared/_BPMN_WORKFLOWS/timer5.bpmn");
//      final RuntimeEngine runtimeEngine = getRuntimeEngine(null);
//      final KieSession kieSession = runtimeEngine.getKieSession();
//
//      final ProcessInstance processInstance = kieSession.startProcess("timer5");
//
//      assertProcessInstanceNotActive(processInstance.getId(), kieSession);
//      
//      int sleep = 20000;
//      LOG.debug("Sleeping {} seconds", sleep / 1000);
//      try {
//			Thread.sleep(sleep);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//  //    assertNodeTriggered(processInstance.getId(), "Hello");
//
//      runtimeManager.disposeRuntimeEngine(runtimeEngine);
//      runtimeManager.close();
//  }
// 
}