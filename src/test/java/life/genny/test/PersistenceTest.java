package life.genny.test;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import org.drools.core.time.impl.PseudoClockScheduler;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.ruleflow.core.RuleFlowProcessFactory;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.TimedRuleExecutionOption;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.command.CommandFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vavr.Tuple3;
import life.genny.models.GennyToken;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwandautils.GennySettings;
import life.genny.rules.QRules;
import life.genny.rules.RulesLoader;
import life.genny.utils.GennyJbpmBaseTest;

public class PersistenceTest extends GennyJbpmBaseTest {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());


	//@Test
	public void dumbTest() {

		KieSession kieSession = createKSession("rulesCurrent/shared/_BPMN_WORKFLOWS/auth_init.bpmn",
				"rulesCurrent/shared/_BPMN_WORKFLOWS/send_forms.bpmn");

		String bridgeUrl = GennySettings.bridgeServiceUrl;
		System.out.println("BridgeUrl=" + bridgeUrl);
//
		QEventMessage msg = new QEventMessage("EVT_MSG", "AUTH_INIT");

		List<Command<?>> cmds = new ArrayList<Command<?>>();

		GennyToken token = getToken(realm, "user1", "Barry Allan", "hero");
		QRules qRules = getQRules(token); // defaults to user anyway
		System.out.println(qRules.getToken());
		cmds.add(CommandFactory.newInsert(qRules, "qRules"));
		cmds.add(CommandFactory.newInsert(msg, "msg"));
		cmds.add(CommandFactory.newInsert("GADA", "name"));

		// Set up Cache

		setUpCache(GennySettings.mainrealm, token);

		cmds.add(CommandFactory.newInsert(eventBusMock, "eb"));

		long startTime = System.nanoTime();
		ExecutionResults results = kieSession.execute(CommandFactory.newBatchExecution(cmds));
		long endTime = System.nanoTime();
		double difference = (endTime - startTime) / 1e6; // get ms

//		cmds.add(getCommands().newInsert(msg));
//		QRules qRules = getQRules(realm, "user1","Barry Allan", "hero"); // defaults to user anyway
//		cmds.add(getCommands().newInsert(qRules));
//	//	cmds.add(CommandFactory.newInsert(qRules, "qrules2"));
//	//	cmds.add(CommandFactory.newInsert(msg, "msg"));
//		cmds.add(getCommands().newInsert("GADA"));
//		
//		EventBusInterface eventBusMock = new EventBusMock();
//		GennyCacheInterface vertxCache = new MockCache();
//		VertxUtils.init(eventBusMock, vertxCache);
//
//		cmds.add(getCommands().newInsert(eventBusMock, "eb"));
//		
//		// cmds.add( CommandFactory.newQuery( "Get Msg" "getMsg" );
//		
//	//	kieSession.getKieRuntime(ProcessInstance.class).getProcess().getMetaData()
//
//		ExecutionResults results = kieSession.execute(getCommands().newBatchExecution(cmds));
//		System.out.println("QRules pid about to be read");

//		ProcessInstance pid = kieSession.getProcessInstance(1L);
//		WorkflowProcessInstance wpi = (WorkflowProcessInstance)kieSession.getProcessInstance(pid.getId());
//		wpi.getVariable(name)
//		QRules rs = (QRules) wpi.getVariable("rules");
//		System.out.println("QRules pid "+rs);
		// results.getValue( "list1" ); // returns the ArrayList
		results.getValue("msg"); // returns the inserted fact Msg
		results.getValue("rules"); // returns the inserted fact QRules
		// results.getValue( "Get People" );// returns the query as a QueryResults
		// instance.
		System.out.println(results.getValue("msg"));
		System.out.println(results.getValue("qRules"));
//	        SignalEventCommand signalEventCommand = new SignalEventCommand();
//
//	        signalEventCommand.setProcessInstanceId(1001);
//
//	        signalEventCommand.setEventType("start");
//
//	        signalEventCommand.setEvent(msg);
//
//	        command.getCommands().add(signalEventCommand);

//		 ProcessInstance pid = kieSession.startProcess("process_1", params);
//		 System.out.println("Process Instance id="+pid.getId());

		// recreate the session from database using the sessionId

		// ksession = JPAKnowledgeService.loadStatefulKnowledgeSession( sessionId,
		// kbase, null, env );

		// check whether the process instance has completed successfully
//	      assertProcessInstanceCompleted(pid.getId(), kieSession);
		// check whether the given nodes were executed during the process execution
		// assertNodeTriggered(pid.getId(), "StartProcess", "Hello", "EndProcess");

		kieSession.dispose();
		System.out.println("BPMN completed in " + difference + " ms");

	}

//	@Test(timeout = 60000)
	public void testPersistentProcess() {
		System.out.println("Persistent Timer Test");
		KieSession kieSession = createKSession("rulesCurrent/shared/_BPMN_WORKFLOWS/TimerStart2.bpmn");
		long startTime = System.nanoTime();

		ProcessInstance pInstance = kieSession.startProcess("DelayTimerEventProcess");
		long pInstanceId = pInstance.getId();

		PseudoClockScheduler sessionClock = kieSession.getSessionClock();
		// Only advancing time by 10 seconds, so process should still be waiting.
		// sessionClock.advanceTime(20, TimeUnit.SECONDS);

		int sleep = 20000;
		log.debug("Sleeping {} seconds", sleep / 1000);
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<Command<?>> cmds = new ArrayList<Command<?>>();
//		
//		GennyToken token = getToken(realm, "user1","Barry Allan", "hero");
//		QRules qRules = getQRules(token); // defaults to user anyway
//	//	System.out.println(qRules.getToken());
//		cmds.add(CommandFactory.newInsert(qRules,"qRules"));
//		
//		EventBusInterface eventBusMock = new EventBusMock();
//		GennyCacheInterface vertxCache = new MockCache();
//		VertxUtils.init(eventBusMock, vertxCache);
//		
//		// Set up Cache
//
//		setUpCache(GennySettings.mainrealm,token);

		// ExecutionResults results =
		// kieSession.execute(CommandFactory.newBatchExecution(cmds));
		long endTime = System.nanoTime();
		double difference = (endTime - startTime) / 1e6; // get ms

		kieSession.dispose();
		System.out.println("Persistent BPMN completed in " + difference + " ms");

	}

	public PersistenceTest() {
		super(false);
	}

	// @Test
	public void basicTest() {
		RuleFlowProcessFactory factory =

				RuleFlowProcessFactory.createProcess("org.jbpm.HelloWorld");

		factory

				// Header

				.name("HelloWorldProcess")

				.version("1.0")

				.packageName("org.jbpm")

				// Nodes

				.startNode(1).name("Start").done()

				.actionNode(2).name("Action")

				.action("java", "System.out.println(\"Hello World\");").done()

				.endNode(3).name("End").done()

				// Connections

				.connection(1, 2)

				.connection(2, 3);

		RuleFlowProcess process = factory.validate().getProcess();

	}

	//   @Test
    public void test() {
        log.debug("jBPM unit test sample");

        final RuntimeManager runtimeManager = createRuntimeManager("rulescurrent/shared/_BPMN_WORKFLOWS/sample.bpmn");
        final RuntimeEngine runtimeEngine = getRuntimeEngine(null);
        final KieSession kieSession = runtimeEngine.getKieSession();

        final ProcessInstance processInstance = kieSession.startProcess("rulescurrent.shared.00_Startup.sample");

        assertProcessInstanceNotActive(processInstance.getId(), kieSession);
        assertNodeTriggered(processInstance.getId(), "Hello");

        runtimeManager.disposeRuntimeEngine(runtimeEngine);
        runtimeManager.close();
    }
   


}