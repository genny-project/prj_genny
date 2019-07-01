package life.genny.test;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.test.JbpmJUnitBaseTestCase.Strategy;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.junit.Test;
import org.kie.api.command.Command;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.command.CommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

import io.vavr.Tuple2;
import io.vertx.core.json.JsonObject;
import life.genny.jbpm.customworkitemhandlers.AwesomeHandler;
import life.genny.jbpm.customworkitemhandlers.NotificationWorkItemHandler;
import life.genny.models.GennyToken;
import life.genny.qwanda.Ask;
import life.genny.qwanda.ContextType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.llama.Frame;
import life.genny.qwanda.llama.Frame.ThemeAttribute;
import life.genny.qwanda.llama.Llama;
import life.genny.qwanda.message.QBulkMessage;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.rules.QRules;
import life.genny.rules.listeners.JbpmInitListener;
import life.genny.utils.FrameUtils;
import life.genny.utils.QuestionUtils;
import life.genny.utils.Layout.LayoutPosition;

public class TimerTest extends GennyJbpmBaseTest {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());
	private static final String WFE_MESSAGE = "rulesCurrent/shared/_BPMN_WORKFLOWS/msg_example_workflow_1.bpmn";
	private static final String WFE_TIMER =  "rulesCurrent/shared/_BPMN_WORKFLOWS/timer_example_workflow_1.bpmn";
	private static final String WFE_SEND_FORMS = "rulesCurrent/shared/_BPMN_WORKFLOWS/send_forms.bpmn";
	private static final String WFE_SHOW_FORM = "rulesCurrent/shared/_BPMN_WORKFLOWS/show_form.bpmn";
	private static final String WFE_AUTH_INIT =  "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/auth_init.bpmn";
	private static final String WFE_SEND_LLAMA =  "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/send_llama.bpmn";
	private static final String DRL_PROJECT =  "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/SendUserData/project.drl";
	private static final String DRL_USER_COMPANY =  "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/SendUserData/user_company.drl";
	private static final String DRL_USER =  "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/SendUserData/user.drl";
	private static final String DRL_EVENT_LISTENER_SERVICE_SETUP =  "rulesCurrent/shared/_BPMN_WORKFLOWS/Initialise_Project/eventListenerServiceSetup.drl";
	private static final String DRL_EVENT_LISTENER_USER_SETUP =  "rulesCurrent/shared/_BPMN_WORKFLOWS/Initialise_Project/eventListenerUserSetup.drl";
	
	public TimerTest() {
		super(false);
	}



	@Test(timeout = 3000000)
	public void testTimerProcess() {
	
		Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
		String[] jbpms = {WFE_TIMER,WFE_SEND_FORMS,WFE_SHOW_FORM,WFE_SEND_LLAMA,WFE_MESSAGE};
	     String[] drls = {DRL_PROJECT,DRL_USER_COMPANY,DRL_USER,DRL_EVENT_LISTENER_SERVICE_SETUP,DRL_EVENT_LISTENER_USER_SETUP};
	    for (String p : jbpms) {
	     
	    	 resources.put(p, ResourceType.BPMN2);
	    }
	    for (String p : drls) {
	         
	    	 resources.put(p, ResourceType.DRL);
	    }
	    createRuntimeManager(Strategy.SINGLETON, resources, null);
		KieSession kieSession = getRuntimeEngine().getKieSession();
		//Register handlers
		addWorkItemHandlers(kieSession);
		kieSession.addEventListener(new JbpmInitListener(userToken));
		

		QEventMessage msg = new QEventMessage("EVT_MSG", "AUTH_INIT");

		List<Command<?>> cmds = new ArrayList<Command<?>>();

		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules qRules = getQRules(userToken); // defaults to user anyway
		System.out.println(qRules.getToken());
		
		
		
		cmds.add(CommandFactory.newInsert(msg, "msg"));
		cmds.add(CommandFactory.newInsert(userToken,"userToken"));
		cmds.add(CommandFactory.newInsert(new GennyToken("serviceUser",qRules.getServiceToken()),"serviceToken"));
		// Set up Cache

		setUpCache(GennySettings.mainrealm, userToken);

		cmds.add(CommandFactory.newInsert(eventBusMock, "eb"));
		
		long startTime = System.nanoTime();
		ExecutionResults results = null;
		try {
			//results = kieSession.execute(CommandFactory.newBatchExecution(cmds));
			
		
			kieSession.startProcess("com.sample.bpmn.msg_example_workflow_1");

			//kieSession.signalEvent("incomingSignal",null);
			} catch (Exception ee) {
				
		}
		finally {
		
/*
			try {
				Thread.sleep(1500000);
			} catch (InterruptedException e) {

			}
			*/

			kieSession.dispose();
			System.out.println("Stoped");
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