package life.genny.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.drools.core.ClockType;
import org.drools.core.time.impl.PseudoClockScheduler;
import org.jbpm.kie.services.impl.query.SqlQueryDefinition;
import org.jbpm.kie.services.impl.query.mapper.ProcessInstanceQueryMapper;
import org.jbpm.kie.services.impl.query.persistence.QueryDefinitionEntity;
import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.api.query.QueryAlreadyRegisteredException;
import org.jbpm.services.api.query.QueryService;
import org.jbpm.services.api.query.model.QueryParam;
import org.jbpm.services.api.utils.KieServiceConfigurator;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.kie.api.command.Command;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.command.CommandFactory;
import org.kie.internal.identity.IdentityProvider;
import org.kie.internal.query.QueryContext;
import org.kie.internal.task.api.UserGroupCallback;

import com.google.gson.reflect.TypeToken;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vertx.core.json.JsonObject;
import life.genny.jbpm.customworkitemhandlers.AskQuestionWorkItemHandler;
import life.genny.jbpm.customworkitemhandlers.AwesomeHandler;
import life.genny.jbpm.customworkitemhandlers.NotificationWorkItemHandler;
import life.genny.jbpm.customworkitemhandlers.PrintWorkItemHandler;
import life.genny.jbpm.customworkitemhandlers.RuleFlowGroupWorkItemHandler;
import life.genny.jbpm.customworkitemhandlers.ShowAllFormsHandler;
import life.genny.jbpm.customworkitemhandlers.ShowFrame;
import life.genny.jbpm.customworkitemhandlers.ShowFrameWIthContextList;
import life.genny.jbpm.customworkitemhandlers.ThrowSignalProcessWorkItemHandler;
import life.genny.models.GennyToken;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.message.QCmdMessage;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataAttributeMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.message.QDataMessage;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwanda.message.QMessage;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.rules.QRules;
import life.genny.rules.listeners.GennyAgendaEventListener;
import life.genny.rules.listeners.JbpmInitListener;
import life.genny.utils.RulesUtils;
import life.genny.utils.VertxUtils;
import life.genny.utils.WorkflowQueryInterface;

public class GennyKieSession extends JbpmJUnitBaseTestCase implements AutoCloseable {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	private static final String DRL_RULESENGINE_HOOKS_DIR = "RulesEngineHooks";

	Map<String, ResourceType> resources = new HashMap<String, ResourceType>();

	private KieSession kieSession;
	ProcessInstance processInstance;

	private List<String> jbpms;
	private List<String> drls;
	private List<String> dtables;

	private ClockType clockType = ClockType.PSEUDO_CLOCK;

	private List<Tuple2<String, WorkItemHandler>> workItemHandlers;

	private Map<String, GennyToken> tokens = new HashMap<String, GennyToken>();

	PseudoClockScheduler sessionClock;
	
	JPAWorkingMemoryDbLogger logger = null;
	
	GennyToken serviceToken = null;

	List<Command<?>> cmds = new ArrayList<Command<?>>();

	/**
	 * static factory method for builder
	 */
	public static Builder builder(GennyToken serviceToken) {
		
		return new GennyKieSession.Builder(serviceToken,false);
	}

	/**
	 * static factory method for builder
	 */
	public static Builder builder(GennyToken serviceToken,boolean persistence) {
		return new GennyKieSession.Builder(serviceToken,persistence);
	}

	/**
	 * forces use of the Builder
	 */
	private GennyKieSession(GennyToken serviceToken,boolean persistence) {
		super(persistence, persistence);
		try {
			if (!"PER_SERVICE".equals(serviceToken.getUserCode())) {
				System.out.println("ERROR: MUST PASS A SERVICE TOKEN");
				throw new Exception();
			}
			tokens.put("PER_SERVICE", serviceToken);
			cmds.add(CommandFactory.newInsert(serviceToken, serviceToken.getCode()));
			super.setUp();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ExecutionResults start() {
		System.out.println("Starting !");
		sessionClock = kieSession.getSessionClock();
		long startTime = System.nanoTime();
		ExecutionResults results = null;
		if (tokens.isEmpty()) {
			System.out.println("You must supply at least a service token!");
		} else {
			try {
				results = kieSession.execute(CommandFactory.newBatchExecution(cmds));
			} catch (Exception ee) {

			} finally {
				long endTime = System.nanoTime();
				double difference = (endTime - startTime) / 1e6; // get ms
				// System.out.println("BPMN completed in " + difference + " ms");
			}
		}
		return results;
	}

	public ProcessInstance startProcess(String processId) {
		processInstance = kieSession.startProcess(processId);
		sessionClock = kieSession.getSessionClock();

		return processInstance;
	}

	public void broadcastSignal(final String type, final Object event) {
		kieSession.signalEvent(type, event);
	}

	public void broadcastSignal(final String type, final Object event, long processInstanceId) {
		kieSession.signalEvent(type, event, processInstanceId);
	}

	public void updateFact(FactHandle handle, Object object) {
		kieSession.update(handle, object);
	}

	public long advanceSeconds(long amount) {
		return advanceSeconds(amount, false);
	}
	
	public void injectMessage(Object object) {
		kieSession.insert(object);
	}
	
	public void injectSignalToProcessInstance(String type, Object object, long processInstanceId) {
		kieSession.signalEvent(type, object, processInstanceId);
	}
	
	public void injectSignal(String type, Object object) {
		kieSession.signalEvent(type, object);
	}
	

	public void injectEvent(QMessage msg) {
		String userCode= "";
		if (msg.getToken()!=null) {
			GennyToken uToken = new GennyToken(msg.getToken());
			userCode = uToken.getUserCode();
		}
		if (msg instanceof QEventMessage) {
			QEventMessage msgEvent = (QEventMessage)msg;
		System.out.println("Injecting event "+msg.getMsg_type()+"  "+msgEvent.getData().getCode()+ " user->"+userCode);
		} else {
			QDataMessage msgData = (QDataMessage)msg;
		System.out.println("Injecting data "+msg.getMsg_type()+"  "+msgData.getData_type()+ " user->"+userCode);

		}
		QEventMessage eventMsg = null;
		QDataMessage dataMsg = null;
		String msg_code = "";
		String msg_type = "";
		GennyToken gToken = this.serviceToken;
		String bridgeSourceAddress = "";

		
		GennyToken userToken = null;
		if (msg.getToken() == null ) {
		for (String tokenKey : this.tokens.keySet()) {
			GennyToken gt = this.tokens.get(tokenKey);
			if (!gt.getCode().equals("PER_SERVICE")) {
				userToken = gt;
				break;
			}
		}
		} else {
			GennyToken uToken = new GennyToken(msg.getToken());
			userToken = uToken;
			
			kieSession.insert(userToken); 
		}
		
			if (msg instanceof QEventMessage)  {
				eventMsg = (QEventMessage)msg;
				eventMsg.setToken(userToken.getToken());
			}
			if (msg instanceof QDataMessage)  {
				dataMsg = (QDataMessage)msg;
				dataMsg.setToken(userToken.getToken());
			}

			if ((eventMsg != null) && (eventMsg.getData().getCode().equals("INIT_STARTUP"))) {
				kieSession.startProcess("init_project");
			} else if (userToken != null) {
				// This is a userToken so send the event through
				String session_state = userToken.getSessionCode();
				String processIdStr = null;
				gToken = userToken;
				bridgeSourceAddress = "bridge";
				Long processId  = null;
				// Check if an existing userSession is there

				Optional<Long> processIdBysessionId = getProcessIdBysessionId(session_state);
				boolean hasProcessIdBySessionId = processIdBysessionId.isPresent();
				if (hasProcessIdBySessionId) {
					processId = processIdBysessionId.get();
				
					System.out.println("incoming " + msg_type + " message from " + bridgeSourceAddress + ": "
							+ userToken.getRealm() + ":" + userToken.getSessionCode() + ":" + userToken.getUserCode()
							+ "   " + msg_code + " to pid " + processId);

					// So send the event through to the userSession
					if (eventMsg != null) {
						kieSession.signalEvent("event", eventMsg, processId);
					} else if (dataMsg != null) {
						kieSession.signalEvent("data", dataMsg, processId);
					}

				} else {
					// Must be the AUTH_INIT
					if ((eventMsg != null) && (eventMsg.getMsg_type().equals("EVT_MSG")) && (eventMsg.getData().getCode().equals("AUTH_INIT"))) {
						eventMsg.getData().setValue("NEW_SESSION");
						System.out.println("incominog  message from " + bridgeSourceAddress + ": " + userToken.getRealm() + ":"
								+ userToken.getSessionCode() + ":" + userToken.getUserCode() + "   " + msg_code
								+ " to NEW SESSION");
						try {
							kieSession.signalEvent("newSession", eventMsg);
						} catch (Exception e) {
							System.out.println("Runtime error: "+e.getLocalizedMessage());
						}
					} else {
						log.error("NO EXISTING SESSION AND NOT AUTH_INIT");
						;
					}
				}
			} 
	}
	
	public long advanceSeconds(long amount, boolean humanTime) {
		long absoluteTime = 0;
		for (int sec = 0; sec < (amount * 2); sec++) {
			absoluteTime = sessionClock.advanceTime(500, TimeUnit.MILLISECONDS); // half a sec should be ok
			if (humanTime) {
				sleepMS(500);
			}
			if ((sec % 2)==0) {
				System.out.print(((amount)-(sec/2))+"s..");
			}
		}
		System.out.println();
		return absoluteTime;
	}

	public long advanceMinutes(long amount) {
		return advanceMinutes(amount, false);
	}

	public long advanceMinutes(long amount, boolean humanTime) {
		return advanceSeconds(amount * 60, humanTime);
	}

	public long advanceHours(long amount) {
		return advanceHours(amount, false);
	}

	public long advanceHours(long amount, boolean humanTime) {
		return advanceMinutes(amount * 60, humanTime);
	}

	public long advanceDays(long amount) {
		return advanceDays(amount, false);
	}

	public long advanceDays(long amount, boolean humanTime) {
		return advanceHours(amount * 24, humanTime);
	}

	protected void sleepMS(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setup() {
		
		
		if (clockType.equals(ClockType.PSEUDO_CLOCK)) {
			System.setProperty("drools.clockType", "pseudo"/* clockType.name() */);
		} else if (clockType.equals(ClockType.REALTIME_CLOCK)) {
			System.setProperty("drools.clockType", "realtime");
		}
		// KieSessionConfiguration config =
		// KieServices.Factory.get().newKieSessionConfiguration();

		// config.setOption( ClockTypeOption.get("realtime") );

		/* Set up RulesEngine Hooks Setup */
		if (this.drls==null) {
			this.drls = new ArrayList<String>();
		}
		this.drls.add(DRL_RULESENGINE_HOOKS_DIR);

		if (jbpms != null) {
			for (String p : jbpms) {
				if (StringUtils.endsWith(p, ".bpmn")) {
					String fullJbpmPath = findFullPath(p);
					resources.put(fullJbpmPath, ResourceType.BPMN2);
					System.out.println("Loading " + fullJbpmPath);
				} else {
					// Is a directory
					List<String> fullJbpmPaths = findFullPaths(p, "bpmn",false);
					fullJbpmPaths.forEach(f -> {
						resources.put(f, ResourceType.BPMN2);
						System.out.println("Loading " + f.toString());
					});

				}
			}
		}

		if (drls != null) {
			for (String p : drls) {
				if (StringUtils.endsWith(p, ".drl")) {
					String fullDrlPath = findFullPath(p);
					resources.put(fullDrlPath, ResourceType.DRL);
					System.out.println("Loading " + fullDrlPath);
				} else {
					// Is a directory
					List<String> fullPaths = findFullPaths(p, "drl",false);
					fullPaths.forEach(f -> {
						resources.put(f, ResourceType.DRL);
						System.out.println("Loading " + f.toString());
					});
				}
			}
		}

		if (dtables != null) {
			for (String p : dtables) {
				if (StringUtils.endsWith(p, ".xls")) {
					String fullDtablePath = findFullPath(p);
					resources.put(fullDtablePath, ResourceType.DTABLE);
					System.out.println("Loading " + fullDtablePath);
				} else {
					// Is a directory
					List<String> fullPaths = findFullPaths(p, "xls",false);
					fullPaths.forEach(f -> {
						resources.put(f, ResourceType.DTABLE);
						System.out.println("Loading " + f.toString());
					});
				}

			}
		}

		System.out.println("Loaded in All Resources");

		String uniqueRuntimeStr = UUID.randomUUID().toString();

		createRuntimeManager(Strategy.SINGLETON, resources, uniqueRuntimeStr);
		kieSession = getRuntimeEngine().getKieSession();
		

		if (kieSession != null) {
			logger = new JPAWorkingMemoryDbLogger(kieSession);

			// Register handlers
			addWorkItemHandlers(getRuntimeEngine());
			
			kieSession.addEventListener(new GennyAgendaEventListener());

			if (tokens.containsKey("PER_SERVICE")) {
				kieSession.addEventListener(new JbpmInitListener(tokens.get("PER_SERVICE")));
				this.serviceToken = tokens.get("PER_SERVICE");
				
			}
			if (tokens.containsKey("PER_USER1")) {
				kieSession.addEventListener(new JbpmInitListener(tokens.get("PER_USER1")));
			}
			
			// Handle attributes ourselves if no background service
			if (VertxUtils.cachedEnabled) {
				loadAttributesJsonFromResources(this.serviceToken);
			}


		//	kieSession.setGlobal("log", log);

			// Add any tokens
//			for (String tokenKey : this.tokens.keySet()) {
//				GennyToken token = this.tokens.get(tokenKey);
//				cmds.add(CommandFactory.newInsert(token, tokenKey));
//			}

		} else {
			log.error("KieSession not initialised");
		}
		
		QueryDefinitionEntity qde = new QueryDefinitionEntity();
		configureServices();
		SqlQueryDefinition query = new SqlQueryDefinition("getAllProcessInstances", "jdbc/jbpm-ds");
		query.setExpression("select * from VariableInstanceLog");
		try {
			queryService.registerQuery(query);
		} catch (QueryAlreadyRegisteredException e) {
			log.warn(query.getName()+" is already registered");
		}
		System.out.println("Completed Setup");
	}

	private void addWorkItemHandlers(RuntimeEngine rteng) {
		kieSession.getWorkItemManager().registerWorkItemHandler("Awesome", new AwesomeHandler());
		kieSession.getWorkItemManager().registerWorkItemHandler("Notification", new NotificationWorkItemHandler());
		kieSession.getWorkItemManager().registerWorkItemHandler("ShowAllForms", new ShowAllFormsHandler());
		kieSession.getWorkItemManager().registerWorkItemHandler("ShowFrame", new ShowFrame(kieSession));
		kieSession.getWorkItemManager().registerWorkItemHandler("Print", new PrintWorkItemHandler(kieSession));
		kieSession.getWorkItemManager().registerWorkItemHandler("ShowFrameWithContextList", new ShowFrameWIthContextList(kieSession));
		kieSession.getWorkItemManager().registerWorkItemHandler("RuleFlowGroup", new RuleFlowGroupWorkItemHandler(kieSession,rteng));
		kieSession.getWorkItemManager().registerWorkItemHandler("ThrowSignalProcess",
				new ThrowSignalProcessWorkItemHandler(kieSession,rteng));
		kieSession.getWorkItemManager().registerWorkItemHandler("AskQuestion",
				new AskQuestionWorkItemHandler(GennyKieSession.class,kieSession,rteng));


		if (workItemHandlers != null) {
			for (Tuple2<String, WorkItemHandler> wih : workItemHandlers) {
				kieSession.getWorkItemManager().registerWorkItemHandler(wih._1, wih._2);
			}
		}

	}

	/**
	 * @return the kieSession
	 */
	public KieSession getKieSession() {
		return kieSession;
	}

	/**
	 * @return the processInstance
	 */
	public ProcessInstance getProcessInstance() {
		return processInstance;
	}

	/**
	 * @param processInstance the processInstance to set
	 */
	public void setProcessInstance(ProcessInstance processInstance) {
		this.processInstance = processInstance;
	}

	/**
	 * @param kieSession the kieSession to set
	 */
	public void setKieSession(KieSession kieSession) {
		this.kieSession = kieSession;
	}

	public void assertNodeTriggered(String nodeName) {
		assertNodeTriggered(this.getProcessInstance().getId(), nodeName);
	}

	public void assertProcessInstanceCompleted() {
		assertProcessInstanceCompleted(this.getProcessInstance().getId());
	}

	private String findFullPath(String filename) {
		String baseRulesDir = "./rules"; // default for project
		if (!"/rules".equals(GennySettings.rulesDir)) {
			baseRulesDir = GennySettings.rulesDir;
		}
		String testRulesDir = baseRulesDir;
		File base = new File(testRulesDir);
		File found = searchFile(new File(testRulesDir), filename);
		return found.getAbsoluteFile().getPath().substring(base.getAbsoluteFile().getPath().length() + 1);

	}
	
	public void displayForm(final String frameCode,GennyToken userToken)
	{
		QDataBaseEntityMessage FRM_MSG = VertxUtils.getObject(userToken.getRealm(), "", frameCode+"-MSG",
				QDataBaseEntityMessage.class, userToken.getToken());	
		FRM_MSG.setToken(userToken.getToken());
		String frmStr = JsonUtils.toJson(FRM_MSG);
		frmStr = frmStr.replaceAll(frameCode, "FRM_ROOT");
		QDataBaseEntityMessage FRM_MSG_ROOT = JsonUtils.fromJson(frmStr, QDataBaseEntityMessage.class);
		VertxUtils.writeMsg("webcmds", JsonUtils.toJson(FRM_MSG_ROOT));
   		Type setType = new TypeToken<Set<QDataAskMessage>>() {
		}.getType();

		String askMsgs2Str = VertxUtils.getObject(userToken.getRealm(), "", frameCode+"-ASKS", String.class,
				userToken.getToken());

		Set<QDataAskMessage> askMsgs2 = JsonUtils.fromJson(askMsgs2Str, setType);

		System.out.println("Sending Asks");
		for (QDataAskMessage askMsg : askMsgs2) {
			askMsg.setToken(userToken.getToken());
			String json = JsonUtils.toJson(askMsg);
	    	String jsonStr = json.replaceAll("PER_SERVICE", userToken.getUserCode()); // set the user

	    	VertxUtils.writeMsg("webcmds", jsonStr);    																							// QDataAskMessage
		}
	}
	
	public void sendLogout(GennyToken userToken)
	{
		QCmdMessage msg = new QCmdMessage("LOGOUT","LOGOUT");
		msg.setToken(userToken.getToken());
		String jsonStr = JsonUtils.toJson(msg);
		VertxUtils.writeMsg("webcmds", jsonStr);  
		
	}

	private List<String> findFullPaths(String dirname, String extension, boolean allowXXX) {
		String baseRulesDir = "./rules"; // default for project
		if (!"/rules".equals(GennySettings.rulesDir)) {
			baseRulesDir = GennySettings.rulesDir;
		}
		String testRulesDir = baseRulesDir;
		File base = new File(testRulesDir);
		File found = searchFile(new File(testRulesDir), dirname);
		String finalFile = found.getAbsoluteFile().getPath().substring(base.getAbsoluteFile().getPath().length() + 1);
		// System.out.println("Found finalDir = " + finalFile);

		Set<Path> resourcePaths = getAllFilesInDirectory(found.getAbsoluteFile().getPath(), extension);

		List<String> files = new ArrayList<String>();
		resourcePaths.forEach(f -> { 
			String filename = f.getFileName().toString();
			
			String fullPath = findFullPath(f.toString());
			if ((!fullPath.contains("XXX")) ||allowXXX) {
				files.add(findFullPath(f.toString()));
			}
		});
		return files;
	}

	private File searchFile(File file, String search) {
		if (file.isDirectory()) {
			if (file.getName().equals(search)) {
				return file;
			}

			File[] arr = file.listFiles();
			for (File f : arr) {
				File found = searchFile(f, search);
				if (found != null)
					return found;
			}
		} else {
			if (file.getName().equals(search)) {
				return file;
			}
		}
		return null;
	}

	private Set<Path> getAllFilesInDirectory(String directoryPathStr, String extension) {
		Set<Path> paths = new HashSet<>();

		Path directoryPath = Paths.get(directoryPathStr);

		try {
			paths = Files.walk(directoryPath).filter(s -> s.toString().endsWith("." + extension)).map(Path::getFileName)
					.sorted().collect(Collectors.toSet());
		} catch (IOException e) {
			log.warn("No files");
		}
		return paths;
	}

	public void close() {
		System.out.println("Completed");
		kieSession.dispose();
		try {
			super.tearDown();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * more fluent Builder
	 */
	public static class Builder {

		private GennyKieSession managedInstance = null;

		public Builder(GennyToken serviceToken) {
			managedInstance = new GennyKieSession(serviceToken,false);
		}

		public Builder(GennyToken serviceToken,boolean persistence) {
			managedInstance = new GennyKieSession(serviceToken,persistence);
		}

		/**
		 * fluent setter for jbpms in the list
		 * 
		 * @param none
		 * @return
		 */
		public Builder addJbpm(String... jbpms) {
			if (managedInstance.jbpms == null) {
				managedInstance.jbpms = new ArrayList<String>();
			}
			for (String jbpm : jbpms) {
				managedInstance.jbpms.add(jbpm);
			}
			return this;
		}

		/**
		 * fluent setter for drls in the list
		 * 
		 * @param none
		 * @return
		 */
		public Builder addDrl(String... drls) {
			if (managedInstance.drls == null) {
				managedInstance.drls = new ArrayList<String>();
			}
			for (String drl : drls) {
				managedInstance.drls.add(drl);
			}

			return this;
		}
		/**
		 * fluent setter for dtables in the list
		 * 
		 * @param none
		 * @return
		 */
		public Builder addDecisionTable(String... dtables) {
			if (managedInstance.dtables == null) {
				managedInstance.dtables = new ArrayList<String>();
			}
			for (String dtable : dtables) {
				managedInstance.dtables.add(dtable);
			}
			return this;
		}

		public Builder addWorkItemHandler(WorkItemHandler workItemHandler) {
			if (managedInstance.workItemHandlers == null) {
				managedInstance.workItemHandlers = new ArrayList<Tuple2<String, WorkItemHandler>>();
			}
			String randomStr = UUID.randomUUID().toString();
			managedInstance.workItemHandlers.add(Tuple.of(randomStr, workItemHandler));
			return this;
		}

		public Builder addWorkItemHandler(String title, WorkItemHandler workItemHandler) {
			if (managedInstance.workItemHandlers == null) {
				managedInstance.workItemHandlers = new ArrayList<Tuple2<String, WorkItemHandler>>();
			}
			managedInstance.workItemHandlers.add(Tuple.of(title, workItemHandler));
			return this;
		}

		public Builder clockType(ClockType clockType) {
			managedInstance.clockType = clockType;
			return this;
		}

		public Builder addToken(GennyToken token) {
			managedInstance.tokens.put(token.getCode(), token);
			managedInstance.cmds.add(CommandFactory.newInsert(token, token.getCode()));
			return this;
		}

		public Builder addFact(String key, Object object) {
			managedInstance.cmds.add(CommandFactory.newInsert(object, key));
			return this;
		}

		public GennyKieSession build() {
			managedInstance.setup();
			return managedInstance;
		}

	}

	private static QueryService queryService;
	private static KieServiceConfigurator serviceConfigurator;

	protected static void configureServices() {
		serviceConfigurator = ServiceLoader.load(KieServiceConfigurator.class).iterator().next();

		IdentityProvider identityProvider = new IdentityProvider() {

			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return "";
			}

			@Override
			public List<String> getRoles() {
				// TODO Auto-generated method stub
				return new ArrayList<String>();
			}

			@Override
			public boolean hasRole(String role) {
				// TODO Auto-generated method stub
				return true;
			}
		};

		UserGroupCallback userGroupCallback = new UserGroupCallback() {

			@Override
			public boolean existsUser(String userId) {
				// TODO Auto-generated method stub
				return true;
			}

			@Override
			public boolean existsGroup(String groupId) {
				// TODO Auto-generated method stub
				return true;
			}

			@Override
			public List<String> getGroupsForUser(String userId) {
				// TODO Auto-generated method stub
				return new ArrayList<String>();
			}
		};

		serviceConfigurator.configureServices("org.jbpm.persistence.jpa", identityProvider, userGroupCallback);
		queryService = serviceConfigurator.getQueryService();

	}
	
	public static Optional<Long> getProcessIdBysessionId(String sessionId) {
		// Do pagination here
		QueryContext ctx = new QueryContext(0, 100);
		Collection<ProcessInstanceDesc> instances = queryService.query("getAllProcessInstances",
				ProcessInstanceQueryMapper.get(), ctx, QueryParam.equalsTo("value", sessionId));

		return instances.stream().map(d -> d.getId()).findFirst();

	}
	
	public static void loadAttributesJsonFromResources(GennyToken gToken)
	{
		// This file can be created by using the script locally and placing in the src/test/resources
		//TOKEN=$(./gettoken-prod.sh )
		// curl -X GET --header 'Accept: application/json'  --header "Authorization: Bearer $TOKEN" 'http://alyson7.genny.life/qwanda/attributes'
		
		
		File file = new File("src/test/resources/attributes.json");
	    String jsonString;
		try {
			jsonString = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
			VertxUtils.writeCachedJson(gToken.getRealm(), "attributes", jsonString, gToken.getToken());
			
			QDataAttributeMessage attributesMsg = JsonUtils.fromJson(jsonString, QDataAttributeMessage.class);
			Attribute[] attributeArray = attributesMsg.getItems();

			for (Attribute attribute : attributeArray) {
				RulesUtils.attributeMap.put(attribute.getCode(), attribute);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public static Optional<Long> getProcessIdBySessionId(String sessionId) {
		// TODO Auto-generated method stub
		return GennyKieSession.getProcessIdBysessionId(sessionId);
	}

}