package life.genny.test;

import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.logging.log4j.Logger;
import org.drools.core.base.MapGlobalResolver;
import org.drools.core.impl.EnvironmentFactory;
import org.jbpm.test.JbpmJUnitTestCase;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.TimedRuleExecutionOption;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.internal.persistence.jpa.JPAKnowledgeService;
import org.kie.internal.runtime.StatefulKnowledgeSession;

import io.vavr.Tuple3;
import life.genny.qwanda.entity.Person;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwandautils.GennySettings;
import life.genny.rules.RulesLoader;

public class JavaServiceQuickstartTest extends JbpmJUnitTestCase {

	private static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	protected static EntityManagerFactory emf;
	protected static EntityManager em;

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

	}

	static Environment env; // drools persistence

	public JavaServiceQuickstartTest() {
		// configure this tests to not use persistence in this case

		// super(false);
	}

	@Test
	public void testProcess() {
		String baseRulesDir = "./rules"; // default for project
		if (!"/rules".equals(GennySettings.rulesDir)) {
			baseRulesDir = GennySettings.rulesDir;
		}
		String testRulesDir = baseRulesDir+"/rulesCurrent/shared/00_Startup";

//		if ("genny".equals(GennySettings.mainrealm)) {
//			return; // skip tests
//		}

		String realm = GennySettings.mainrealm;

		Set<String> realms = new HashSet<String>();
		realms.add(realm);

		List<Tuple3<String, String, String>> rules = RulesLoader.processFileRealms("genny", testRulesDir,
				realms);
		System.out.println("LOADED ALL RULES from "+testRulesDir);
		realms.stream().forEach(System.out::println);
		realms.remove("genny");

		System.out.println("LOADING " + realm + " RULES");
		Integer rulesCount = RulesLoader.setupKieRules(realm, rules);
		System.out.println("Rules Count for " + realm + " = " + rulesCount);

		StatefulKnowledgeSession kieSession = null;
	//	KieSession kieSession = null;
		KieSessionConfiguration ksconf = KieServices.Factory.get().newKieSessionConfiguration();
		ksconf.setOption(TimedRuleExecutionOption.YES);

		// create a new knowledge session that uses JPA to store the runtime state
		KieBase kieBase = RulesLoader.getKieBaseCache().get(GennySettings.mainrealm);
		if (false) {
			kieSession = JPAKnowledgeService.newStatefulKnowledgeSession(
					kieBase, ksconf, env); // This is stateful
		} else {
			kieSession = (StatefulKnowledgeSession) kieBase.newKieSession(ksconf, env);
		}

		int sessionId = kieSession.getId();
		System.out.println("Session id = " + sessionId);

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

		Map<String, Object> params = new HashMap<String, Object>();

		QEventMessage msg = new QEventMessage("EVT_MSG", "AUTH_INIT");
		msg.getData().setCode("INIT_STARTUP");
		params.put("msg", msg);

		 ProcessInstance pid = kieSession.startProcess("process_1", params);
		 System.out.println("Process Instance id="+pid.getId());
		// recreate the session from database using the sessionId

		// ksession = JPAKnowledgeService.loadStatefulKnowledgeSession( sessionId,
		// kbase, null, env );
		 
		 kieSession.dispose();

	}

}