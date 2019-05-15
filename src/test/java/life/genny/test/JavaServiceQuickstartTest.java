package life.genny.test;

import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.logging.log4j.Logger;
import org.drools.core.base.MapGlobalResolver;
import org.drools.core.command.runtime.process.SignalEventCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.impl.EnvironmentFactory;
import org.drools.core.time.impl.PseudoClockScheduler;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.ruleflow.core.RuleFlowProcessFactory;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.jbpm.test.JbpmJUnitTestCase;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;

import org.kie.api.io.ResourceType;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.TimedRuleExecutionOption;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.internal.command.CommandFactory;
import org.kie.internal.persistence.jpa.JPAKnowledgeService;
import org.kie.internal.runtime.StatefulKnowledgeSession;

import io.vavr.Tuple3;
import life.genny.qwanda.entity.Person;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwandautils.GennySettings;
import life.genny.rules.RulesLoader;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.kie.api.command.Command;
import org.kie.api.runtime.KieSession;


import static org.junit.Assert.*;

import org.slf4j.LoggerFactory;

public class JavaServiceQuickstartTest extends JbpmJUnitBaseTestCase {

	private static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	protected final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());
    
	protected static EntityManagerFactory emf;
	protected static EntityManager em;
	
	protected static String realm = GennySettings.mainrealm;
	protected static Set<String> realms;
	
    private static final String CONDITIONAL_ID = "org.jbpm.test.functional.event.StartEvent-conditional";


	@BeforeClass
	public static void init() throws FileNotFoundException, SQLException {
		System.out.println("Setting up EntityManagerFactory");
		try {
			emf = Persistence.createEntityManagerFactory("h2-pu");
			env = EnvironmentFactory.newEnvironment(); // KnowledgeBaseFactory.newEnvironment();
			env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
	//		   env.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager());
			    env.set(EnvironmentName.GLOBALS, new MapGlobalResolver());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (emf == null) {
			log.error("EMF is null");
		} else {
			System.out.println("Setting up EntityManager");
			em = emf.createEntityManager();
		}
		
		//Set up realm
		realms = new HashSet<String>();
		realms.add(realm);
		realms.stream().forEach(System.out::println);
		realms.remove("genny");

		 //Enable the PseudoClock using the following system property.
	    System.setProperty("drools.clockType", "pseudo");
	}

	static Environment env; // drools persistence

	public JavaServiceQuickstartTest() {
		// configure this tests to not use persistence in this case

		// super(false);
	}

	@Test(timeout = 30000)
	public void testProcess() {

		  KieSession kieSession = createKSession("rulesCurrent/shared/00_Startup/auth_init.bpmn");
//		  KieSession kieSession = createKSession("rulesCurrent/shared/00_Startup/test.bpmn");
//		KieSession kieSession = setupSession("/rulesCurrent/shared/00_Startup",true);
		
		
//		Map<String, Object> params = new HashMap<String, Object>();
//
		QEventMessage msg = new QEventMessage("EVT_MSG", "AUTH_INIT");
//		params.put("msg", msg);
//		params.put("inout", "condition");
//		kieSession.insert(msg);
//		kieSession.insert(new String("condition"));
		
	       List<Command<?>> cmds = new ArrayList<Command<?>>();
	        cmds.add(getCommands().newInsert(msg));
	//        cmds.add(getCommands().newInsert("condition"));
	//        kieSession.execute(getCommands().newBatchExecution(cmds, null));
	        
//	        BatchExecutionCommand command = new BatchExecutionCommand();
//
//	        command.setLookup("ksession1");
//
//	        StartProcessCommand startProcessCommand = new StartProcessCommand();
//
//	        startProcessCommand.setProcessId("org.drools.task.processOne");
//
//	        command.getCommands().add(startProcessCommand);
//	        
	 //       cmds.add( CommandFactory.newSetGlobal( "list1", new ArrayList(), true ) );
	        cmds.add( CommandFactory.newInsert( msg, "msg" ) );
	    //    cmds.add( CommandFactory.newQuery( "Get Msg" "getMsg" );

	        ExecutionResults results = kieSession.execute( CommandFactory.newBatchExecution( cmds ) );
	 //       results.getValue( "list1" ); // returns the ArrayList
	        results.getValue( "msg" ); // returns the inserted fact Person
	  //      results.getValue( "Get People" );// returns the query as a QueryResults instance.
	        System.out.println(results.getValue("msg"));

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
	      //assertNodeTriggered(pid.getId(), "StartProcess", "Hello", "EndProcess");

	      


	      
	      
		 kieSession.dispose();

	}
	
	//@Test
	public void basicTest()
	{
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
	
	//@Test(timeout = 30000)
	public void testTimerProcess() {

		  KieSession kieSession = createKSession("rulesCurrent/shared/00_Startup/process_2.bpmn");
	//	  KieSession kieSession = setupSession("/rulesCurrent/shared/00_Startup/process_2.bpmn",true);
		
	       List<Command<?>> cmds = new ArrayList<Command<?>>();

	       ProcessInstance pid = kieSession.startProcess("process_2", null);
	   //     ExecutionResults results = kieSession.execute( CommandFactory.newBatchExecution( cmds ) );

	        try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {

			}

	      
	      
		 kieSession.dispose();

	}
	
	
	private KieSession setupSession(final String rulesDir, final boolean debug)
	{
		String baseRulesDir = "./rules"; // default for project
		if (!"/rules".equals(GennySettings.rulesDir)) {
			baseRulesDir = GennySettings.rulesDir;
		}
		String testRulesDir = baseRulesDir+rulesDir;

		List<Tuple3<String, String, String>> rules = RulesLoader.processFileRealms("genny", testRulesDir,
				realms);
		Integer rulesCount = RulesLoader.setupKieRules(realm, rules);
		System.out.println("Rules Count for " + realm +":"+testRulesDir+ " = " + rulesCount);

		StatefulKnowledgeSession kieSession = null;
		KieSessionConfiguration ksconf = KieServices.Factory.get().newKieSessionConfiguration();
		ksconf.setOption(TimedRuleExecutionOption.YES);

		// create a new knowledge session that uses JPA to store the runtime state
		KieBase kieBase = RulesLoader.getKieBaseCache().get(realm);
		kieSession = (StatefulKnowledgeSession) kieBase.newKieSession(ksconf, env);


		int sessionId = kieSession.getId();
		System.out.println("Session id = " + sessionId);

		if (debug) {
			setDebug(kieSession);
		}
		
		return kieSession;

	}
	
	
	@Test
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
	
	private void setDebug(KieSession kieSession) {
		kieSession.addEventListener(new DefaultProcessEventListener() {
			public void beforeProcessStarted(ProcessStartedEvent event) {
				WorkflowProcessInstance process = (WorkflowProcessInstance) event.getProcessInstance();
				System.out.println("jBPM event 'beforeProcessStarted'. Process ID: " + process.getId()
						+ ", Process definition ID: " + process.getProcessId() + ", Process name: "
						+ process.getProcessName() + ", Process state: " + process.getState() + ", Parent process ID: "
						+ process.getParentProcessInstanceId());
			}

			public void afterProcessStarted(ProcessStartedEvent event) {
				WorkflowProcessInstance process = (WorkflowProcessInstance) event.getProcessInstance();
				System.out.println("jBPM event 'afterProcessStarted'. Process ID: " + process.getId()
						+ ", Process definition ID: " + process.getProcessId() + ", Process name: "
						+ process.getProcessName() + ", Process state: " + process.getState() + ", Parent process ID: "
						+ process.getParentProcessInstanceId());
			}

			public void beforeProcessCompleted(ProcessCompletedEvent event) {
				WorkflowProcessInstance process = (WorkflowProcessInstance) event.getProcessInstance();
				System.out.println("jBPM event 'beforeProcessCompleted'. Process ID: " + process.getId()
						+ ", Process definition ID: " + process.getProcessId() + ", Process name: "
						+ process.getProcessName() + ", Process state: " + process.getState() + ", Parent process ID: "
						+ process.getParentProcessInstanceId());
			}

			public void afterProcessCompleted(ProcessCompletedEvent event) {
				WorkflowProcessInstance process = (WorkflowProcessInstance) event.getProcessInstance();
				System.out.println("jBPM event 'afterProcessCompleted'. Process ID: " + process.getId()
						+ ", Process definition ID: " + process.getProcessId() + ", Process name: "
						+ process.getProcessName() + ", Process state: " + process.getState() + ", Parent process ID: "
						+ process.getParentProcessInstanceId());
			}

			public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
				WorkflowProcessInstance process = (WorkflowProcessInstance) event.getProcessInstance();
				NodeInstance node = event.getNodeInstance();
				System.out.println("jBPM event 'beforeNodeTriggered'. Process ID: " + process.getId()
						+ ", Process definition ID: " + process.getProcessId() + ", Process name: "
						+ process.getProcessName() + ", Process state: " + process.getState() + ", Parent process ID: "
						+ process.getParentProcessInstanceId() + ", Node instance ID: " + node.getId() + ", Node ID: "
						+ node.getNodeId() + ", Node name: " + node.getNodeName());

			}

			public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
				WorkflowProcessInstance process = (WorkflowProcessInstance) event.getProcessInstance();
				NodeInstance node = event.getNodeInstance();
				System.out.println("jBPM event 'afterNodeTriggered'. Process ID: " + process.getId() + ", Process definition ID: "
						+ process.getProcessId() + ", Process name: " + process.getProcessName() + ", Process state: "
						+ process.getState() + ", Parent process ID: " + process.getParentProcessInstanceId()
						+ ", Node instance ID: " + node.getId() + ", Node ID: " + node.getNodeId() + ", Node name: "
						+ node.getNodeName());

			}

			public void beforeNodeLeft(ProcessNodeLeftEvent event) {
				WorkflowProcessInstance process = (WorkflowProcessInstance) event.getProcessInstance();
				NodeInstance node = event.getNodeInstance();
				System.out.println("jBPM event 'beforeNodeLeft'. Process ID: " + process.getId() + ", Process definition ID: "
						+ process.getProcessId() + ", Process name: " + process.getProcessName() + ", Process state: "
						+ process.getState() + ", Parent process ID: " + process.getParentProcessInstanceId()
						+ ", Node instance ID: " + node.getId() + ", Node ID: " + node.getNodeId() + ", Node name: "
						+ node.getNodeName());

			}

			public void afterNodeLeft(ProcessNodeLeftEvent event) {
				WorkflowProcessInstance process = (WorkflowProcessInstance) event.getProcessInstance();
				NodeInstance node = event.getNodeInstance();
				System.out.println("jBPM event 'afterNodeLeft'. Process ID: " + process.getId() + ", Process definition ID: "
						+ process.getProcessId() + ", Process name: " + process.getProcessName() + ", Process state: "
						+ process.getState() + ", Parent process ID: " + process.getParentProcessInstanceId()
						+ ", Node instance ID: " + node.getId() + ", Node ID: " + node.getNodeId() + ", Node name: "
						+ node.getNodeName());

			}

			public void beforeVariableChanged(ProcessVariableChangedEvent event) {
				WorkflowProcessInstance process = (WorkflowProcessInstance) event.getProcessInstance();
				System.out.println("jBPM event 'beforeVariableChanged'. Process ID: " + process.getId()
						+ ", Process definition ID: " + process.getProcessId() + ", Process name: "
						+ process.getProcessName() + ", Process state: " + process.getState() + ", Parent process ID: "
						+ process.getParentProcessInstanceId() + ", Variable ID: " + event.getVariableId()
						+ ", Variable instance ID: " + event.getVariableInstanceId() + ", Old value: "
						+ (event.getOldValue() == null ? "null" : event.getOldValue().toString()) + ", New value: "
						+ (event.getNewValue() == null ? "null" : event.getNewValue().toString()));
			}

			public void afterVariableChanged(ProcessVariableChangedEvent event) {
				WorkflowProcessInstance process = (WorkflowProcessInstance) event.getProcessInstance();
				System.out.println("jBPM event 'afterVariableChanged'. Process ID: " + process.getId()
						+ ", Process definition ID: " + process.getProcessId() + ", Process name: "
						+ process.getProcessName() + ", Process state: " + process.getState() + ", Parent process ID: "
						+ process.getParentProcessInstanceId() + ", Variable ID: " + event.getVariableId()
						+ ", Variable instance ID: " + event.getVariableInstanceId() + ", Old value: "
						+ (event.getOldValue() == null ? "null" : event.getOldValue().toString()) + ", New value: "
						+ (event.getNewValue() == null ? "null" : event.getNewValue().toString()));
			}

		});
	}
	
	   public KieSession createKSession(String... process) {
	        createRuntimeManager(process);
	        return getRuntimeEngine().getKieSession();
	    }

	    public KieSession createKSession(Map<String, ResourceType> res) {
	        createRuntimeManager(res);
	        return getRuntimeEngine().getKieSession();
	    }

	    public KieSession restoreKSession(String... process) {
	        disposeRuntimeManager();
	        createRuntimeManager(process);
	        return getRuntimeEngine().getKieSession();
	    }
	  protected static KieServices getServices() {
	        return KieServices.Factory.get();
	    }

	    protected static KieCommands getCommands() {
	        return getServices().getCommands();
	    }

}