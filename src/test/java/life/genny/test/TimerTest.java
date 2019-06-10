package life.genny.test;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import org.kie.api.command.Command;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimerTest extends GennyJbpmBaseTest {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());


	public TimerTest() {
		super(false);
	}



	// @Test(timeout = 30000)
	public void testTimerProcess() {

		KieSession kieSession = createKSession("rulesCurrent/shared/_BPMN_WORKFLOWS/process_2.bpmn");
		// KieSession kieSession =
		// setupSession("/rulesCurrent/shared/00_Startup/process_2.bpmn",true);

		List<Command<?>> cmds = new ArrayList<Command<?>>();

		ProcessInstance pid = kieSession.startProcess("process_2", null);
		// ExecutionResults results = kieSession.execute(
		// CommandFactory.newBatchExecution( cmds ) );

		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {

		}

		kieSession.dispose();

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