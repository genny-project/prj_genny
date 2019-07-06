package life.genny.test;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import life.genny.models.GennyToken;
import life.genny.qwanda.message.QEventMessage;
import life.genny.rules.QRules;
import life.genny.utils.VertxUtils;

public class TimerTest extends GennyJbpmBaseTest {

	 private static final Logger logger = LoggerFactory.getLogger(TimerTest.class);
	
	private static final String WFE_TIMER_INTERVAL = "timer5.bpmn";


	public TimerTest() {
		super(false);
	}

	
	@Test
	public void timerIntervalTest() {
		VertxUtils.cachedEnabled = true; // don't try and use any local services
		GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "userToken");
		GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "serviceToken");
		QRules qRules = new QRules(eventBusMock, userToken.getToken(), userToken.getAdecodedTokenMap());
		
		
		GennyKieSession gks = GennyKieSession.builder(serviceToken)
				.addJbpm( WFE_TIMER_INTERVAL)
				.build();
		
	
	     gks.startProcess("TimerTest");
	    
	    gks.advanceSeconds(20,false);

	    
	    gks.close();
	}
	


	
	@Test(timeout = 300000)	
	public void testTimerProcess() {
		
		QEventMessage msg = new QEventMessage("EVT_MSG", "AUTH_INIT1");

		VertxUtils.cachedEnabled = true; // don't try and use any local services
		GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "userToken");
		GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "serviceToken");
		QRules qRules = new QRules(eventBusMock, userToken.getToken(), userToken.getAdecodedTokenMap());
		
		GennyKieSession gks = GennyKieSession.builder(serviceToken)
				.addJbpm("example_timer_start.bpmn")
				.addJbpm("timer_example_workflow_1.bpmn","timer_example_workflow_2.bpmn","timer_example_workflow_3.bpmn")
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