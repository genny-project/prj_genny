package life.genny.test;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import life.genny.models.GennyToken;
import life.genny.qwanda.message.QEventMessage;
import life.genny.rules.QRules;

public class TimerTest extends GennyJbpmBaseTest {

	 private static final Logger logger = LoggerFactory.getLogger(TimerTest.class);
	
	private static final String WFE_TIMER_INTERVAL = "timer5.bpmn";


	public TimerTest() {
		super(false);
	}

	
	@Test
	public void timerIntervalTest() {
		
		
		GennyKieSession gks = GennyKieSession.builder()
				.addJbpm( WFE_TIMER_INTERVAL)
				.build();
		
	
	     gks.startProcess("TimerTest");
	    
	    gks.advanceSeconds(20,false);

	    
	    gks.close();
	}
	


	
	@Test(timeout = 300000)	
	public void testTimerProcess() {
		
		QEventMessage msg = new QEventMessage("EVT_MSG", "AUTH_INIT1");

		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules qRules = getQRules(userToken); // defaults to user anyway
		
		GennyKieSession gks = GennyKieSession.builder()
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