package life.genny.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;

import org.drools.core.base.MapGlobalResolver;
import org.drools.core.command.runtime.process.SignalEventCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.impl.EnvironmentFactory;
import org.drools.core.time.impl.PseudoClockScheduler;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.ruleflow.core.RuleFlowProcessFactory;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.jbpm.test.JbpmJUnitTestCase;
import org.json.JSONObject;
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
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.internal.command.CommandFactory;
import org.kie.internal.persistence.jpa.JPAKnowledgeService;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vertx.core.json.JsonObject;
import life.genny.eventbus.EventBusInterface;
import life.genny.eventbus.EventBusMock;
import life.genny.eventbus.MockCache;
import life.genny.eventbus.VertxCache;
import life.genny.models.GennyToken;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.Person;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwandautils.GennyCacheInterface;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.KeycloakUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.qwandautils.SecurityUtils;
import life.genny.rules.QRules;
import life.genny.rules.RulesLoader;
import life.genny.utils.VertxUtils;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.kie.api.command.Command;
import org.kie.api.runtime.KieSession;

import static org.junit.Assert.*;


public class JavaServiceQuickstartTest extends JbpmJUnitBaseTestCase {

//	private static final Logger log = org.apache.logging.log4j.LogManager
//			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());
	
	private static final Logger log
    = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());
//	static ch.qos.logback.classic.Logger logger = 
//			  (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());


//	protected final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

	protected static EntityManagerFactory emf;
	protected static EntityManager em;

	protected static String realm = GennySettings.mainrealm;
	protected static Set<String> realms;
	
	protected static EventBusInterface eventBusMock;
	protected static GennyCacheInterface vertxCache;


	@Test //(timeout = 30000)
	public void testAuthInit() {

		KieSession kieSession = createKSession("rulesCurrent/shared/_BPMN_WORKFLOWS/auth_init.bpmn");
//		KieSession kieSession = setupSession("/rulesCurrent/shared/00_Startup",true);

		String bridgeUrl = GennySettings.bridgeServiceUrl;
		System.out.println("BridgeUrl="+bridgeUrl);
//
		QEventMessage msg = new QEventMessage("EVT_MSG", "AUTH_INIT");

		List<Command<?>> cmds = new ArrayList<Command<?>>();
		
		GennyToken token = getToken(realm, "user1","Barry Allan", "hero");
		QRules qRules = getQRules(token); // defaults to user anyway
		System.out.println(qRules.getToken());
		cmds.add(CommandFactory.newInsert(qRules,"qRules"));
		cmds.add(CommandFactory.newInsert(msg, "msg"));
		cmds.add(CommandFactory.newInsert("GADA", "name"));
		
		
		// Set up Cache

		setUpCache(GennySettings.mainrealm,token);

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
		System.out.println("BPMN completed in "+difference+" ms");

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
		//    sessionClock.advanceTime(20, TimeUnit.SECONDS);
		    
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

		
	//	ExecutionResults results = kieSession.execute(CommandFactory.newBatchExecution(cmds));
		long endTime = System.nanoTime();
		double difference = (endTime - startTime) / 1e6; // get ms		

		kieSession.dispose();
		System.out.println("Persistent BPMN completed in "+difference+" ms");

	}

	@BeforeClass
	public static void init() throws FileNotFoundException, SQLException {
				 

				
		log.info("Setting up EntityManagerFactory");
//		try {
//			emf = Persistence.createEntityManagerFactory("h2-pu");
//			env = EnvironmentFactory.newEnvironment(); // KnowledgeBaseFactory.newEnvironment();
//			env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
//			// env.set(EnvironmentName.TRANSACTION_MANAGER,
//			// TransactionManagerServices.getTransactionManager());
//			env.set(EnvironmentName.GLOBALS, new MapGlobalResolver());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		if (emf == null) {
//			log.error("EMF is null");
//		} else {
//			System.out.println("Setting up EntityManager");
//			em = emf.createEntityManager();
//		}

		// Set up realm
		realms = new HashSet<String>();
		realms.add(realm);
		realms.stream().forEach(System.out::println);
		realms.remove("genny");

		// Enable the PseudoClock using the following system property.
		System.setProperty("drools.clockType", "pseudo");
		
		eventBusMock = new EventBusMock();
		vertxCache = new VertxCache();  // MockCache
		VertxUtils.init(eventBusMock, vertxCache);

	}

	static Environment env; // drools persistence

	public JavaServiceQuickstartTest() {
		// configure this tests to not use persistence in this case

		 super(true,true);
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

	private KieSession setupSession(final String rulesDir, final boolean debug) {
		String baseRulesDir = "./rules"; // default for project
		if (!"/rules".equals(GennySettings.rulesDir)) {
			baseRulesDir = GennySettings.rulesDir;
		}
		String testRulesDir = baseRulesDir + rulesDir;

		List<Tuple3<String, String, String>> rules = RulesLoader.processFileRealms("genny", testRulesDir, realms);
		Integer rulesCount = RulesLoader.setupKieRules(realm, rules);
		System.out.println("Rules Count for " + realm + ":" + testRulesDir + " = " + rulesCount);

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
				System.out.println("jBPM event 'afterNodeTriggered'. Process ID: " + process.getId()
						+ ", Process definition ID: " + process.getProcessId() + ", Process name: "
						+ process.getProcessName() + ", Process state: " + process.getState() + ", Parent process ID: "
						+ process.getParentProcessInstanceId() + ", Node instance ID: " + node.getId() + ", Node ID: "
						+ node.getNodeId() + ", Node name: " + node.getNodeName());

			}

			public void beforeNodeLeft(ProcessNodeLeftEvent event) {
				WorkflowProcessInstance process = (WorkflowProcessInstance) event.getProcessInstance();
				NodeInstance node = event.getNodeInstance();
				System.out.println("jBPM event 'beforeNodeLeft'. Process ID: " + process.getId()
						+ ", Process definition ID: " + process.getProcessId() + ", Process name: "
						+ process.getProcessName() + ", Process state: " + process.getState() + ", Parent process ID: "
						+ process.getParentProcessInstanceId() + ", Node instance ID: " + node.getId() + ", Node ID: "
						+ node.getNodeId() + ", Node name: " + node.getNodeName());

			}

			public void afterNodeLeft(ProcessNodeLeftEvent event) {
				WorkflowProcessInstance process = (WorkflowProcessInstance) event.getProcessInstance();
				NodeInstance node = event.getNodeInstance();
				System.out.println("jBPM event 'afterNodeLeft'. Process ID: " + process.getId()
						+ ", Process definition ID: " + process.getProcessId() + ", Process name: "
						+ process.getProcessName() + ", Process state: " + process.getState() + ", Parent process ID: "
						+ process.getParentProcessInstanceId() + ", Node instance ID: " + node.getId() + ", Node ID: "
						+ node.getNodeId() + ", Node name: " + node.getNodeName());

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

	private QRules getQRules(final GennyToken token) {

		List<Tuple2<String, Object>> globals = new ArrayList<Tuple2<String, Object>>();
		Map<String, String> keyValueMap = new HashMap<String, String>();

		globals = RulesLoader.getStandardGlobals();



		QRules qRules = new QRules(eventBusMock, token.getToken(), token.getAdecodedTokenMap());
		qRules.set("realm", realm);

		return qRules;

	}
	
	private BaseEntity getProject(final String realm) 
	{
		BaseEntity project=null;
		
        final String code = "PRJ_" + realm.toUpperCase();
        final String name = realm;
        final Date date = new Date(); // *1000 is to convert seconds to milliseconds
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"); // the format of
                                                                                    // your date
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+10")); // give a timezone reference for formating
        sdf.format(date);
//        final Attribute firstNameAtt = service.findAttributeByCode("PRI_FIRSTNAME");
//        final Attribute lastNameAtt = service.findAttributeByCode("PRI_LASTNAME");
//        final Attribute nameAtt = service.findAttributeByCode("PRI_NAME");
//        final Attribute emailAtt = service.findAttributeByCode("PRI_EMAIL");
//        final Attribute uuidAtt = service.findAttributeByCode("PRI_UUID");
//        final Attribute usernameAtt = service.findAttributeByCode("PRI_USERNAME");
//
       try {
           project = new BaseEntity(code, name);
//
//          project.addAttribute(firstNameAtt, 0.0, firstName);
//          project.addAttribute(lastNameAtt, 0.0, lastName);
//          project.addAttribute(nameAtt, 0.0, name);
//          project.addAttribute(emailAtt, 0.0, email);
//          project.addAttribute(uuidAtt, 0.0, id);
//          project.addAttribute(usernameAtt, 0.0, username);
       } catch (Exception e) {
    	   
       }
       return project;
	}
	
	private void setUpCache(final String realm, GennyToken token)
	{
		BaseEntity project = getProject(realm);
		String json = JsonUtils.toJson(project);
		VertxUtils.writeCachedJson(realm, project.getCode(), json, token.getToken());
	}
	
	private GennyToken getToken(final String realm, String username, String name, String role)
	{
		GennyToken gennyToken = null;
		// Test if local token available from background system
		String cacheToken;
		JsonObject cache = null;
		try {
			
			String kToken = getKeycloakToken(realm);
			if (kToken != null) {
				Map<String,Object> adecodedTokenMap = RulesLoader.getDecodedTokenMap(kToken);

				gennyToken =  new GennyToken(realm,(String)adecodedTokenMap.get("preferred_username"),name,role);
				gennyToken.setToken(kToken);
				gennyToken.setAdecodedTokenMap(adecodedTokenMap);
				
			} else {
			
			cacheToken = QwandaUtils.apiGet("http://alyson7.genny.life/read/"+realm+"/CACHE:SERVICE_TOKEN", "DUMMY");
			cacheToken = cacheToken.replaceAll("\\\\\"", "");
			cache = new JsonObject(cacheToken);
			if ("ok".equals(cache.getString("status"))) {
				String token = cache.getString("value");
				Map<String,Object> adecodedTokenMap = RulesLoader.getDecodedTokenMap(token);
				gennyToken =  new GennyToken(realm,(String)adecodedTokenMap.get("preferred_username"),name,role);
				gennyToken.setToken(token);
				gennyToken.setAdecodedTokenMap(adecodedTokenMap);
			} else {
				gennyToken =  new GennyToken(realm,username,name,role);
				VertxUtils.writeCachedJson(realm,"CACHE:SERVICE_TOKEN",gennyToken.getToken());
			}
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			gennyToken =  new GennyToken(realm,username,name,role);
			VertxUtils.writeCachedJson(realm,"CACHE:SERVICE_TOKEN",gennyToken.getToken());
		}
	

	
		
		return gennyToken;
	}
	
	private String getKeycloakToken(String realm)
	{
		String apiUrl = "http://alyson7.genny.life/api/events/init?url=http://"+realm+".genny.life";
		System.out.println("Fetching setup info from "+apiUrl);
		try {
			String keycloakJson = QwandaUtils.apiGet(apiUrl, "DUMMY");
			JsonObject json = new JsonObject(keycloakJson);
			String authServer = json.getString("auth-server-url");
			authServer = StringUtils.removeEnd(authServer, "/auth");
			JsonObject credentials = json.getJsonObject("credentials");
			String secret = credentials.getString("secret");
			String username = System.getenv("USERNAME");
			String password = System.getenv("PASSWORD");

			String token = KeycloakUtils.getAccessToken(authServer, realm, realm, secret, username, password);

			// check if user token already exists
			String userCode = "PER_"+QwandaUtils.getNormalisedUsername(username);
			JsonObject cacheJson = VertxUtils.readCachedJson(realm, "TOKEN:"+userCode,token);
			String status = cacheJson.getString("status");
				
			if ("ok".equals(status)) {
				String userToken = cacheJson.getString("value");
				return userToken;
			}
			
			System.out.println(keycloakJson);
			
			
			
			return token;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	//	KeycloakUtils.getAccessToken(keycloakUrl, realm, clientId, secret, username, password);
		return "DUMMY";
	}

}