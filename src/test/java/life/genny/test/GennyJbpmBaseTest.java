package life.genny.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.jbpm.test.JbpmJUnitBaseTestCase.Strategy;
import org.junit.BeforeClass;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
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
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.TimedRuleExecutionOption;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.internal.command.CommandFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vertx.core.json.JsonObject;
import life.genny.eventbus.EventBusInterface;
import life.genny.eventbus.EventBusMock;
import life.genny.eventbus.EventBusVertx;
import life.genny.eventbus.VertxCache;
import life.genny.jbpm.customworkitemhandlers.AwesomeHandler;
import life.genny.jbpm.customworkitemhandlers.NotificationWorkItemHandler;
import life.genny.jbpm.customworkitemhandlers.ShowAllFormsHandler;
import life.genny.models.GennyToken;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwandautils.GennyCacheInterface;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.KeycloakUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.rules.QRules;
import life.genny.rules.RulesLoader;
import life.genny.rules.listeners.JbpmInitListener;
import life.genny.utils.VertxUtils;


public class GennyJbpmBaseTest extends JbpmJUnitBaseTestCase {

	
	private static final Logger log
    = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	protected static EntityManagerFactory emf;
	protected static EntityManager em;

	protected static String realm = GennySettings.mainrealm;
	protected static Set<String> realms;
	
	protected static EventBusInterface eventBusMock;
	protected static GennyCacheInterface vertxCache;
	
	protected static JsonObject projectParms;
	
	protected static Optional<Boolean> isUsingRemote = Optional.empty();
	
	protected  GennyToken userToken;
	protected  GennyToken serviceToken;
	
	private static final String DRL_PROJECT = "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/SendUserData/project.drl";
	private static final String DRL_USER_COMPANY = "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/SendUserData/user_company.drl";
	private static final String DRL_USER = "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/SendUserData/user.drl";
	private static final String DRL_EVENT_LISTENER_SERVICE_SETUP = "rulesCurrent/shared/_BPMN_WORKFLOWS/Initialise_Project/eventListenerServiceSetup.drl";
	private static final String DRL_EVENT_LISTENER_USER_SETUP = "rulesCurrent/shared/_BPMN_WORKFLOWS/Initialise_Project/eventListenerUserSetup.drl";



	@BeforeClass
	public static void init() throws FileNotFoundException, SQLException {
				 
		System.out.println("BridgeUrl=" + GennySettings.bridgeServiceUrl);
		System.out.println("QwandaUrl=" + GennySettings.qwandaServiceUrl);

		// Work out if the local dev system is running by seeing if bridge is responding
		if (GennyJbpmBaseTest.checkRemoteService()) {
			System.out.println("Testing against remote system");
		}
				
		System.out.println("Setting up EntityManagerFactory");
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
		VertxUtils.cachedEnabled = false; // use any local services

	}

	static Environment env; // drools persistence

	public GennyJbpmBaseTest(final Boolean persistence) {
		 super(persistence,persistence);
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



	protected void setDebug(KieSession kieSession) {
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
		KieSession kieSession = getRuntimeEngine().getKieSession();
		//Register handlers
		addWorkItemHandlers(kieSession);
		kieSession.addEventListener(new JbpmInitListener(userToken));
		return kieSession;
	}
	


	public KieSession createKSession(Map<String, ResourceType> res) {
		createRuntimeManager(res);
		KieSession kieSession =  getRuntimeEngine().getKieSession();
		addWorkItemHandlers(kieSession);
		return kieSession;
	}

	public KieSession restoreKSession(String... process) {
		disposeRuntimeManager();
		createRuntimeManager(process);
		KieSession kieSession = getRuntimeEngine().getKieSession();
		addWorkItemHandlers(kieSession);
		return kieSession;
	}
	
	protected void addWorkItemHandlers(KieSession kieSession)
	{
		kieSession.getWorkItemManager().registerWorkItemHandler("Awesome", new AwesomeHandler());
		kieSession.getWorkItemManager().registerWorkItemHandler("Notification", new NotificationWorkItemHandler());
		kieSession.getWorkItemManager().registerWorkItemHandler("ShowAllForms", new ShowAllFormsHandler());

	}

	protected static KieServices getServices() {
		return KieServices.Factory.get();
	}

	protected static KieCommands getCommands() {
		return getServices().getCommands();
	}

	protected static QRules createQRules(final GennyToken userToken,final GennyToken serviceToken, EventBusInterface eventBusMock) {

		QRules qRules = new QRules(eventBusMock, userToken.getToken());
		qRules.set("realm", userToken.getRealm());
		qRules.setServiceToken(serviceToken.getToken());
		return qRules;

	}
	
	protected QRules getQRules(final GennyToken token) {

		List<Tuple2<String, Object>> globals = new ArrayList<Tuple2<String, Object>>();
		Map<String, String> keyValueMap = new HashMap<String, String>();

		globals = RulesLoader.getStandardGlobals();



		QRules qRules = new QRules(eventBusMock, token.getToken());
		qRules.set("realm", realm);
		qRules.setServiceToken(projectParms.getString("serviceToken"));
		return qRules;

	}
	
	protected BaseEntity getProject(final String realm) 
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
	
	protected void setUpCache(final String realm, GennyToken token)
	{
		BaseEntity project = getProject(realm);
		String json = JsonUtils.toJson(project);
		VertxUtils.writeCachedJson(realm, project.getCode(), json, token.getToken());
	}
	
	protected GennyToken getToken(final String realm, String username, String name, String role)
	{
		String normalisedUsername = "PER_"+username.toUpperCase();

		GennyToken gennyToken = null;
		// Test if local token available from background system
		String cacheToken;
		JsonObject cache = null;
		try {
			String kToken = getKeycloakToken(realm);
			if (kToken != null) {
				gennyToken =  new GennyToken(kToken);
				this.userToken = gennyToken;
				
			} else {
			
			cacheToken = QwandaUtils.apiGet("http://alyson7.genny.life:8089/read/"+realm+"/CACHE:SERVICE_TOKEN", "DUMMY");
			cacheToken = cacheToken.replaceAll("\\\\\"", "");
			cache = new JsonObject(cacheToken);
			if ("ok".equals(cache.getString("status"))) {
				String token = cache.getString("value");
				gennyToken =  new GennyToken(token);
				this.serviceToken = gennyToken;
			} else {
				gennyToken =  new GennyToken(normalisedUsername,realm,username,name,role);
				VertxUtils.writeCachedJson(realm,"CACHE:SERVICE_TOKEN",gennyToken.getToken());
			}
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			gennyToken =  new GennyToken(normalisedUsername,realm,username,name,role);
			VertxUtils.writeCachedJson(realm,"CACHE:SERVICE_TOKEN",gennyToken.getToken());
		}
	

	
		
		return gennyToken;
	}
	
	protected String getKeycloakToken(String realm)
	{
		String apiUrl = GennySettings.projectUrl+"/api/events/init?url=http://"+realm+".genny.life";
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
			
			//System.out.println(keycloakJson);
			
			
			
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

	static protected boolean checkRemoteService()
	{
		if (isUsingRemote.isPresent()) {
			return isUsingRemote.get();
		}
		String apiUrl = GennySettings.projectUrl+"/api/events/init?url=http://"+realm+".genny.life";
		System.out.println("Fetching setup info from "+apiUrl);
		try {
			String keycloakJson = QwandaUtils.apiGet(apiUrl, "DUMMY");
			projectParms = new JsonObject(keycloakJson);
			String authServer = projectParms.getString("auth-server-url");
			authServer = StringUtils.removeEnd(authServer, "/auth");
			JsonObject credentials = projectParms.getJsonObject("credentials");
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
				GennyToken userGennyToken = new GennyToken("userToken",userToken);
				System.out.println("User "+username+" is logged in! "+userGennyToken.getAdecodedTokenMap().get("session_state"));;
				projectParms.put("userToken", userToken);
			} else {
				System.out.println("User "+username+" is NOT LOGGED IN!");;
				projectParms.put("userToken", token);  // use non alyson token
				return false;
			}
			
			JsonObject serviceTokenJson = VertxUtils.readCachedJson(GennySettings.GENNY_REALM, "TOKEN" + realm.toUpperCase(),token);
			status = serviceTokenJson.getString("status");
				
			if ("ok".equals(status)) {
				String serviceToken = serviceTokenJson.getString("value");
				System.out.println("Service Account available!");;
				projectParms.put("serviceToken", serviceToken);
			} else {
				log.error("Service Token UNAVAILABLE!");;
				projectParms.put("serviceToken", token);  // use non alyson token
				return false;
			}

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			return false;
		}
		
	//	KeycloakUtils.getAccessToken(keycloakUrl, realm, clientId, secret, username, password);
		return true;
	}
	

	static 	public  QRules setupLocalService() {
		GennyJbpmBaseTest localService = new GennyJbpmBaseTest(false);
		try {
			localService.init();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		GennyToken userToken = localService.getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = localService.getQRules(userToken); // defaults to user anyway

		return rules;
	}

	public static GennyToken createGennyToken(final String realm, String username, String name, String role)
	{
		return createGennyToken(realm, username, name, role,24*60*60);
	}

	public static GennyToken createGennyToken(final String realm, String username, String name, String role, long expirysecs)
	{
		String normalisedUsername = null;
		if (!username.startsWith("PER_")) {
			normalisedUsername = "PER_"+username.toUpperCase();
		} else {
			normalisedUsername = username.toUpperCase();
		}
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime expiryTime = now.plusSeconds(expirysecs);
		GennyToken gennyToken = new GennyToken(normalisedUsername,realm,username,name,role,expiryTime);
		return gennyToken;
	}			
}