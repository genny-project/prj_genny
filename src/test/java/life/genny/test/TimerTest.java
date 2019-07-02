package life.genny.test;

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

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	//private static final String WFE_TIMER_INTERVAL = "rulesCurrent/shared/_BPMN_WORKFLOWS/XXXtimer5.bpmn";
	private static final String WFE_TIMER_EXAMPLE_START = "rulesCurrent/shared/_BPMN_WORKFLOWS/TimerExamples/example_timer_start.bpmn";
	private static final String WFE_TIMER_EXAMPLE_4 = "rulesCurrent/shared/_BPMN_WORKFLOWS/TimerExamples/timer_example_workflow_4.bpmn";
	private static final String WFE_TIMER_EXAMPLE_1 = "rulesCurrent/shared/_BPMN_WORKFLOWS/TimerExamples/timer_example_workflow_1.bpmn";
	private static final String WFE_TIMER_EXAMPLE_2 = "rulesCurrent/shared/_BPMN_WORKFLOWS/TimerExamples/timer_example_workflow_2.bpmn";
	private static final String WFE_TIMER_EXAMPLE_3 = "rulesCurrent/shared/_BPMN_WORKFLOWS/TimerExamples/timer_example_workflow_3.bpmn";
//	private static final String WFE_TIMER_INTERVAL = "rulesCurrent/shared/_BPMN_WORKFLOWS/XXXTimerStart2.bpmn";

	private static final String WFE_SEND_FORMS = "rulesCurrent/shared/_BPMN_WORKFLOWS/send_forms.bpmn";
	private static final String WFE_SHOW_FORM = "rulesCurrent/shared/_BPMN_WORKFLOWS/show_form.bpmn";
	private static final String WFE_AUTH_INIT = "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/auth_init.bpmn";
	private static final String WFE_SEND_LLAMA = "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/send_llama.bpmn";
	private static final String DRL_PROJECT = "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/SendUserData/project.drl";
	private static final String DRL_USER_COMPANY = "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/SendUserData/user_company.drl";
	private static final String DRL_USER = "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/SendUserData/user.drl";
	private static final String DRL_EVENT_LISTENER_SERVICE_SETUP = "rulesCurrent/shared/_BPMN_WORKFLOWS/Initialise_Project/eventListenerServiceSetup.drl";
	private static final String DRL_EVENT_LISTENER_USER_SETUP = "rulesCurrent/shared/_BPMN_WORKFLOWS/Initialise_Project/eventListenerUserSetup.drl";


	public TimerTest() {
		super(false);
	}

	@Test(timeout = 30000000)	
	public void testTimerProcess() {
		
		Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
		String[] jbpms = { WFE_TIMER_EXAMPLE_START,WFE_TIMER_EXAMPLE_1,WFE_TIMER_EXAMPLE_2,WFE_TIMER_EXAMPLE_3,WFE_TIMER_EXAMPLE_4 };
		String[] drls = { DRL_PROJECT, DRL_USER_COMPANY, DRL_USER, DRL_EVENT_LISTENER_SERVICE_SETUP,
				DRL_EVENT_LISTENER_USER_SETUP };
		for (String p : jbpms) {
			resources.put(p, ResourceType.BPMN2);
		}
		for (String p : drls) {
			resources.put(p, ResourceType.DRL);
		}
		createRuntimeManager(Strategy.SINGLETON, resources, null);
		KieSession kieSession = getRuntimeEngine().getKieSession();
		// Register handlers
		addWorkItemHandlers(kieSession);
		kieSession.addEventListener(new JbpmInitListener(userToken));
		
		try {
			kieSession.setGlobal("log", log);
		} catch (RuntimeException e) {
			log.error("kieSession.setGlobal(\"log\", log); has an error "+e.getLocalizedMessage());
		}
		
		QEventMessage msg = new QEventMessage("EVT_MSG", "AUTH_INIT1");

		List<Command<?>> cmds = new ArrayList<Command<?>>();

		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules qRules = getQRules(userToken); // defaults to user anyway
		System.out.println(qRules.getToken());
		cmds.add(CommandFactory.newInsert(qRules, "qRules"));
		cmds.add(CommandFactory.newInsert(msg, "msg"));
		cmds.add(CommandFactory.newInsert(userToken, "userToken"));
		cmds.add(CommandFactory.newInsert(new GennyToken("serviceUser", qRules.getServiceToken()), "serviceToken"));
		// Set up Cache

		setUpCache(GennySettings.mainrealm, userToken);

		cmds.add(CommandFactory.newInsert(eventBusMock, "eb"));

		long startTime = System.nanoTime();
		ExecutionResults results = null;
		try {
			results = kieSession.execute(CommandFactory.newBatchExecution(cmds));
			//kieSession.startProcess("com.sample.bpmn.exampleTimerStart");
			  PseudoClockScheduler sessionClock = kieSession.getSessionClock();
			    // Timer is set to 60 seconds, so advancing with 70.
			    //sessionClock.advanceTime(70, TimeUnit.SECONDS);
			//sleepMS(15000);
		} catch (Exception ee) {

		} finally {
			long endTime = System.nanoTime();
			double difference = (endTime - startTime) / 1e6; // get ms

			if (results != null) {
				results.getValue("msg"); // returns the inserted fact Msg
				QRules rules = (QRules) results.getValue("qRules"); // returns the inserted fact QRules
				System.out.println(rules.getAsString("value"));
				System.out.println(rules);
			} else {
				System.out.println("NO RESULTS");
			}

			System.out.println("BPMN completed in " + difference + " ms");

			kieSession.dispose();
		}
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