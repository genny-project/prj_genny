package life.genny.utils;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import java.util.stream.Stream;

import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.drools.core.ClockType;
import org.drools.core.time.impl.PseudoClockScheduler;
import org.jbpm.executor.ExecutorServiceFactory;
import org.jbpm.executor.impl.ExecutorImpl;
import org.jbpm.executor.impl.ExecutorServiceImpl;
import org.jbpm.kie.services.impl.query.SqlQueryDefinition;
import org.jbpm.kie.services.impl.query.mapper.ProcessInstanceQueryMapper;
import org.jbpm.kie.services.impl.query.persistence.QueryDefinitionEntity;
import org.jbpm.process.audit.AbstractAuditLogger;
import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.api.query.QueryAlreadyRegisteredException;
import org.jbpm.services.api.query.QueryService;
import org.jbpm.services.api.query.model.QueryParam;
import org.jbpm.services.api.utils.KieServiceConfigurator;
import org.jbpm.services.task.wih.util.PeopleAssignmentHelper;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.kie.api.KieBase;
import org.kie.api.command.Command;
import org.kie.api.executor.ExecutorService;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Group;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.api.task.model.User;
import org.kie.internal.command.CommandFactory;
import org.kie.internal.identity.IdentityProvider;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.query.QueryContext;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.internal.task.api.UserGroupCallback;

import com.google.gson.reflect.TypeToken;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import life.genny.jbpm.customworkitemhandlers.AskQuestionTaskWorkItemHandler;
import life.genny.jbpm.customworkitemhandlers.AwesomeHandler;
import life.genny.jbpm.customworkitemhandlers.CheckTasksWorkItemHandler;
import life.genny.jbpm.customworkitemhandlers.GetProcessesUsingVariable;
import life.genny.jbpm.customworkitemhandlers.JMSSendTaskWorkItemHandler;
import life.genny.jbpm.customworkitemhandlers.NotificationHubWorkItemHandler;
import life.genny.jbpm.customworkitemhandlers.NotificationWorkItemHandler;
import life.genny.jbpm.customworkitemhandlers.PrintWorkItemHandler;
import life.genny.jbpm.customworkitemhandlers.ProcessAnswersWorkItemHandler;
import life.genny.jbpm.customworkitemhandlers.ProcessTaskIdWorkItemHandler;
import life.genny.jbpm.customworkitemhandlers.RuleFlowGroupWorkItemHandler;
import life.genny.jbpm.customworkitemhandlers.SendSignalWorkItemHandler;
import life.genny.jbpm.customworkitemhandlers.ShowAllFormsHandler;
import life.genny.jbpm.customworkitemhandlers.ShowFrame;
import life.genny.jbpm.customworkitemhandlers.ShowFrameWIthContextList;
import life.genny.jbpm.customworkitemhandlers.ShowFrames;
import life.genny.jbpm.customworkitemhandlers.ThrowSignalProcessWorkItemHandler;
import life.genny.jbpm.customworkitemhandlers.ThrowSignalWorkItemHandler;
import life.genny.models.GennyToken;
import life.genny.qwanda.Answer;
import life.genny.qwanda.Ask;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.datatype.DataType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.message.QCmdMessage;
import life.genny.qwanda.message.QDataAnswerMessage;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataAttributeMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.message.QDataMessage;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwanda.message.QMessage;
import life.genny.qwanda.validation.Validation;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.rules.GennyUsersCallback;
import life.genny.rules.RulesLoader;
import life.genny.rules.listeners.GennyAgendaEventListener;
import life.genny.rules.listeners.JbpmInitListener;
import life.genny.rules.listeners.NodeStatusLog;
import life.genny.utils.RulesUtils;
import life.genny.utils.SessionFacts;
import life.genny.utils.VertxUtils;

public class GennyKieSession extends JbpmJUnitBaseTestCase implements AutoCloseable {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	private static final String DRL_RULESENGINE_HOOKS_DIR = "RulesEngineHooks";

	Map<String, ResourceType> resources = new HashMap<String, ResourceType>();

	private KieSession kieSession;
	ProcessInstance processInstance;
	private ExecutorService executorService;

	private List<String> jbpms;
	private List<String> drls;
	private List<String> dtables;

	private ClockType clockType = ClockType.PSEUDO_CLOCK;

	private List<Tuple2<String, WorkItemHandler>> workItemHandlers;

	private Map<String, GennyToken> tokens = new HashMap<String, GennyToken>();

	PseudoClockScheduler sessionClock;
	
	JPAWorkingMemoryDbLogger logger = null;
	
	GennyToken serviceToken = null;
	GennyToken userToken = null; // for compatibility

	List<Command<?>> cmds = new ArrayList<Command<?>>();
	
	Map<String,List<String>> groups = new HashMap<String,List<String>>();
	
	   protected Map<String,Object> messages;
	   protected Map<String,Answer> answers;
	   List<Status> statuses = new ArrayList<Status>();

	

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
			this.serviceToken = serviceToken;
			
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

	public  void injectAnswer(final String attributeCode, GennyToken gToken) {
		injectAnswer(attributeCode,gToken,gToken.getUserCode());
	}
	
	public  void injectAnswer(final String attributeCode, GennyToken gToken, final String targetCode) {
		Answer[] answerArray = new Answer[1];
		answerArray[0] = answers.get(attributeCode);
		answerArray[0].setSourceCode(gToken.getUserCode());
		answerArray[0].setTargetCode(targetCode);
		QDataAnswerMessage answerMsg = new QDataAnswerMessage(answerArray);
		answerMsg.setToken(gToken.getToken()); 
		
		injectEvent(answerMsg,gToken);
	}

	public <T extends QMessage> void injectMessage(T msg, GennyToken gToken) {
		msg.setToken(gToken.getToken());
		kieSession.insert(msg);
	}

	
	public void injectSignalToProcessInstance(String type, Object object, long processInstanceId) {
		kieSession.signalEvent(type, object, processInstanceId);
	}

	public void injectSignal(String messageKey, GennyToken gToken) {
		SessionFacts sig = (SessionFacts)messages.get(messageKey);
		sig.setUserToken(gToken);
		kieSession.signalEvent(messageKey, sig);
	}
	
	public void injectSignal(String messageKey) {
		SessionFacts sig = (SessionFacts)messages.get(messageKey);
		sig.setUserToken(serviceToken);
		kieSession.signalEvent(messageKey, sig);
	}
	
	public void injectSignal(String type, Object object) {
		kieSession.signalEvent(type, object);
	}
	
	public void injectEvent(String msgCode, GennyToken gToken) {
		QMessage msg = (QMessage)messages.get(msgCode);
		injectEvent(msg, gToken);
	}
	
	public void injectEvent(QMessage msg) {
		injectEvent(msg, this.userToken);
	}

	public void injectEvent(QMessage msg,GennyToken gToken) {
		msg.setToken(gToken.getToken());
		
		if (msg instanceof QEventMessage) {
			QEventMessage msgEvent = (QEventMessage)msg;
			System.out.println("Injecting event "+msg.getMsg_type()+"  "+msgEvent.getData().getCode()+ " user->"+gToken.getUserCode());
		} else {
			QDataMessage msgData = (QDataMessage)msg;
			System.out.println("Injecting data "+msg.getMsg_type()+"  "+msgData.getData_type()+ " user->"+gToken.getUserCode());

		}
		QEventMessage eventMsg = null;
		QDataMessage dataMsg = null;
		String msg_code = "";
		String msg_type = "";
		String bridgeSourceAddress = "";

		
			if (msg instanceof QEventMessage)  {
				eventMsg = (QEventMessage)msg;
			}
			if (msg instanceof QDataMessage)  {
				dataMsg = (QDataMessage)msg;
			}

			if ((eventMsg != null) && (eventMsg.getData().getCode().equals("INIT_STARTUP"))) {
				kieSession.startProcess("initProject");
			} else  {
				// This is a userToken so send the event through
				String session_state = gToken.getSessionCode();
				bridgeSourceAddress = "bridge";
				Long processId  = null;
				
				// Check if an existing userSession is there
				


				Optional<Long> processIdBysessionId = getProcessIdBysessionId(session_state);
				boolean hasProcessIdBySessionId = processIdBysessionId.isPresent();
				if (hasProcessIdBySessionId) {
					if (hasProcessIdBySessionId) {
						processId = processIdBysessionId.get();
					}
					System.out.println("incoming " + msg_type + " message from " + bridgeSourceAddress + ": "
							+ gToken.getRealm() + ":" + gToken.getSessionCode() + ":" + gToken.getUserCode()
							+ "   " + msg_code + " to pid " + processId);

					// So send the event through to the userSession
					if (eventMsg != null) {
						SessionFacts sessionFactsEvent = new SessionFacts(serviceToken, gToken , eventMsg);
					//	kieSession.signalEvent("EV_"+session_state, sessionFactsEvent);
					//	kieSession.signalEvent("EV_"+session_state, sessionFactsEvent, processId);
						// HACK
						if (eventMsg.getData().getCode().equals("QUE_SUBMIT")) {
							Answer dataAnswer = new Answer(gToken.getUserCode(),gToken.getUserCode(),"PRI_SUBMIT","QUE_SUBMIT");
							dataMsg = new QDataAnswerMessage(dataAnswer);
							SessionFacts sessionFactsData = new SessionFacts(serviceToken, gToken , dataMsg);							
							kieSession.signalEvent("data", sessionFactsData, processId);
						}
							else	if (eventMsg.getData().getCode().equals("QUE_CANCEL")) {
								Answer dataAnswer = new Answer(gToken.getUserCode(),gToken.getUserCode(),"PRI_SUBMIT","QUE_CANCEL");
								dataMsg = new QDataAnswerMessage(dataAnswer);
								SessionFacts sessionFactsData = new SessionFacts(serviceToken, gToken , dataMsg);							
								kieSession.signalEvent("data", sessionFactsData, processId);
						
						} else {
							kieSession.signalEvent("event", sessionFactsEvent, processId);
						}
					} else if (dataMsg != null) {
						SessionFacts sessionFactsData = new SessionFacts(serviceToken, gToken , dataMsg);
					//	kieSession.signalEvent("DT_"+session_state, sessionFactsData);
						kieSession.signalEvent("data", sessionFactsData, processId);
					}

				} else {
					// Must be the AUTH_INIT
					if ((eventMsg != null) && (eventMsg.getMsg_type().equals("EVT_MSG")) && (eventMsg.getData().getCode().equals("AUTH_INIT"))) {
						eventMsg.getData().setValue("NEW_SESSION");
						System.out.println("incoming  message from " + bridgeSourceAddress + ": " + gToken.getRealm() + ":"
								+ gToken.getSessionCode() + ":" + gToken.getUserCode() + "   " + msg_code
								+ " to NEW SESSION");
						try {
							SessionFacts sessionFactsEvent = new SessionFacts(serviceToken, gToken , eventMsg);
							kieSession.signalEvent("newSession", sessionFactsEvent);
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

		String uniqueRuntimeStr = this.serviceToken.getRealm();//UUID.randomUUID().toString();
		
		System.setProperty("org.kie.server.bypass.auth.user", "true");

		if (!GennySettings.useJMS) {
			System.out.println("NOT USING JMS");
			EntityManagerFactory emf = super.getEmf();


			RuntimeEnvironmentBuilder envBuilder = RuntimeEnvironmentBuilder.Factory
					.get()
					.newEmptyBuilder()
					// remember to register the executor service
					.addEnvironmentEntry( EnvironmentName.ENTITY_MANAGER_FACTORY, emf )
					.entityManagerFactory(emf)
					.persistence(sessionPersistence)
					
//					.userGroupCallback(new UserGroupCallback() {
//		    			public List<String> getGroupsForUser(String userId) {  // could actually send token rather than user
//		    				List<String> result = new ArrayList<String>();
//		    				// fetch user baseentity
//		    				String userCode = "PER_"+userId.toUpperCase();
//		    				
//		    				if ("acrow".equals(userId)) {
//		    					result.add("GADA");
//		    				} else if ("domenic".equals(userId)) {
//		    					result.add("GADA");
//		    				} else if ("gerard".equals(userId)) {
//		    					result.add("OUTCOME");
//		    				} else if ("chris".equals(userId)) {
//		    					result.add("OUTCOME");
//		    					result.add("GADA");
//		    				}
//		    				return result;
//		    			}
//		    			public boolean existsUser(String arg0) {
//		    				return true;
//		    			}
//		    			public boolean existsGroup(String arg0) {
//		    				return true;
//		    			}
//		    		});
					.userGroupCallback(new GennyUsersCallback());
					
					
	        for (Map.Entry<String, ResourceType> entry : resources.entrySet()) {
	            try {
					envBuilder.addAsset(ResourceFactory.newClassPathResource(entry.getKey()), entry.getValue());
				} catch (Exception e) {
					System.out.println("Error loading "+entry.getKey()+" :"+e.getLocalizedMessage());
				}
	        }
	        RuntimeEnvironment env = envBuilder.get();
	     
	        RulesLoader.runtimeManager =  createRuntimeManager(Strategy.SINGLETON, resources, env, uniqueRuntimeStr);
		       KieBase kieBase =  env.getKieBase();
		       RulesLoader.getKieBaseCache().put(serviceToken.getRealm(),kieBase);
			
		} else {
			System.out.println("USINGJMS");
			EntityManagerFactory emf = super.getEmf();
			String executorQueueName = "queue/KIE.SERVER.EXECUTOR";
			// build executor service
	        executorService = ExecutorServiceFactory.newExecutorService(emf);
	        executorService.setInterval(3);
	        executorService.setRetries(3);
	        executorService.setThreadPoolSize(1);
	        executorService.setTimeunit(TimeUnit.valueOf( "SECONDS"));

	        ((ExecutorImpl) ((ExecutorServiceImpl) executorService).getExecutor()).setQueueName(executorQueueName);

	        executorService.init();
			
			RuntimeEnvironmentBuilder envBuilder = RuntimeEnvironmentBuilder.Factory
					.get()
					.newEmptyBuilder()
					// remember to register the executor service
					.addEnvironmentEntry("ExecutorService", executorService) 
					.addEnvironmentEntry( EnvironmentName.ENTITY_MANAGER_FACTORY, emf )
					.entityManagerFactory(emf)
					.userGroupCallback(new GennyUsersCallback());
				
					
	        for (Map.Entry<String, ResourceType> entry : resources.entrySet()) {
	            envBuilder.addAsset(ResourceFactory.newClassPathResource(entry.getKey()), entry.getValue());
	        }
	        RuntimeEnvironment env = envBuilder.get();
	       KieBase kieBase =  env.getKieBase();
	       RulesLoader.getKieBaseCache().put(serviceToken.getRealm(),kieBase);

			RulesLoader.runtimeManager = createRuntimeManager(Strategy.SINGLETON, resources, env, uniqueRuntimeStr);

		}

		kieSession = getRuntimeEngine().getKieSession();
		
		

		if (kieSession != null) {
		//	logger = new JPAWorkingMemoryDbLogger(kieSession);
			AbstractAuditLogger logger = new NodeStatusLog(kieSession);

			// Register handlers
			addWorkItemHandlers(getRuntimeEngine());
			
			kieSession.addEventListener(new GennyAgendaEventListener());

			kieSession.addEventListener(new JbpmInitListener(serviceToken));

			for (GennyToken token : tokens.values()) {
				if (!token.getUserCode().equals("PER_SERVICE")) {
					kieSession.addEventListener(new JbpmInitListener(tokens.get(token.getUserCode())));
				}
			}
			
			// Handle attributes ourselves if no background service
			if (VertxUtils.cachedEnabled) {
				loadAttributesJsonFromResources(this.serviceToken);
			}

			kieSession.getEnvironment().set("Autoclaim", "true");  // for JBPM
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
		
		
		this.setupMessages();
		this.setupCache();
		this.setupAnswers();
		this.userToken = GennyJbpmBaseTest.createGennyToken(serviceToken.getRealm(), "user1", "Barry Allan", "user");

		// Hack
		RulesLoader.taskServiceMap.put(serviceToken.getRealm(), this.getTaskService());
		
		System.out.println("Completed Setup");
	}

	private void addWorkItemHandlers(RuntimeEngine rteng) {
		kieSession.getWorkItemManager().registerWorkItemHandler("SendSignal",
				new SendSignalWorkItemHandler(MethodHandles.lookup().lookupClass(),rteng));

		kieSession.getWorkItemManager().registerWorkItemHandler("GetProcessesUsingVariable", new GetProcessesUsingVariable());
		kieSession.getWorkItemManager().registerWorkItemHandler("Awesome", new AwesomeHandler());
		kieSession.getWorkItemManager().registerWorkItemHandler("Notification", new NotificationWorkItemHandler());
		kieSession.getWorkItemManager().registerWorkItemHandler("ShowAllForms", new ShowAllFormsHandler());
		kieSession.getWorkItemManager().registerWorkItemHandler("ShowFrame", new ShowFrame());
		rteng.getKieSession().getWorkItemManager().registerWorkItemHandler("ShowFrames", new ShowFrames());
		kieSession.getWorkItemManager().registerWorkItemHandler("Print", new PrintWorkItemHandler());
		kieSession.getWorkItemManager().registerWorkItemHandler("ShowFrameWithContextList", new ShowFrameWIthContextList());
		kieSession.getWorkItemManager().registerWorkItemHandler("RuleFlowGroup", new RuleFlowGroupWorkItemHandler(rteng));
		kieSession.getWorkItemManager().registerWorkItemHandler("ThrowSignalProcess",
				new ThrowSignalProcessWorkItemHandler(rteng));
		kieSession.getWorkItemManager().registerWorkItemHandler("ThrowSignal",
				new ThrowSignalWorkItemHandler(MethodHandles.lookup().lookupClass(),rteng));
		
		kieSession.getWorkItemManager().registerWorkItemHandler("SendSignal",
				new SendSignalWorkItemHandler(MethodHandles.lookup().lookupClass(),rteng));

		
		kieSession.getWorkItemManager().registerWorkItemHandler("JMSSendTask", new JMSSendTaskWorkItemHandler());

		kieSession.getWorkItemManager().registerWorkItemHandler("AskQuestionTask",
				new AskQuestionTaskWorkItemHandler(MethodHandles.lookup().lookupClass(),rteng,kieSession));
		kieSession.getWorkItemManager().registerWorkItemHandler("CheckTasks",
				new CheckTasksWorkItemHandler(MethodHandles.lookup().lookupClass(),rteng,kieSession));
		kieSession.getWorkItemManager().registerWorkItemHandler("ProcessAnswers",
				new ProcessAnswersWorkItemHandler(MethodHandles.lookup().lookupClass(),rteng, kieSession));

		kieSession.getWorkItemManager().registerWorkItemHandler("ProcessTaskId",
				new ProcessTaskIdWorkItemHandler(MethodHandles.lookup().lookupClass(),rteng,kieSession));

		kieSession.getWorkItemManager().registerWorkItemHandler("NotificationHub", new NotificationHubWorkItemHandler());
		
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
	
	public static void  displayForm(final String rootFrameCode, String targetFrameCode, GennyToken userToken)
	{

		if (userToken == null) {
			log.error("Must supply userToken!");

		} else {
			// log.info("userToken = " + userToken.getCode());

			if (rootFrameCode == null) {
				log.error("Must supply a root Frame Code!");
			} else {

				QDataBaseEntityMessage FRM_MSG = VertxUtils.getObject(userToken.getRealm(), "", rootFrameCode + "_MSG",
						QDataBaseEntityMessage.class, userToken.getToken());

				if (FRM_MSG != null) {

					if (targetFrameCode == null) {
						targetFrameCode = "FRM_ROOT";
					}

					QDataBaseEntityMessage TARGET_FRM_MSG = VertxUtils.getObject(userToken.getRealm(), "",
							targetFrameCode + "_MSG", QDataBaseEntityMessage.class, userToken.getToken());

					for (BaseEntity targetFrame : TARGET_FRM_MSG.getItems()) {
						if (targetFrame.getCode().equals(targetFrameCode)) {

							System.out.println("ShowFrame : Found Targeted Frame BaseEntity : " + targetFrame);

							/* Adding the links in the targeted BaseEntity */
							Attribute attribute = new Attribute("LNK_FRAME", "LNK_FRAME", new DataType(String.class));

							for (BaseEntity sourceFrame : FRM_MSG.getItems()) {
								if (sourceFrame.getCode().equals(rootFrameCode)) {

									System.out.println("ShowFrame : Found Source Frame BaseEntity : " + sourceFrame);
									EntityEntity entityEntity = new EntityEntity(targetFrame, sourceFrame, attribute,
											1.0, "CENTRE");
									Set<EntityEntity> entEntList = targetFrame.getLinks();
									entEntList.add(entityEntity);
									targetFrame.setLinks(entEntList);

									/* Adding Frame to Targeted Frame BaseEntity Message */
									FRM_MSG.add(targetFrame);
									FRM_MSG.setReplace(true);
									break;
								}
							}
							break;
						}
					}

					FRM_MSG.setToken(userToken.getToken());

					FRM_MSG.setReplace(true);

					VertxUtils.writeMsg("webcmds", JsonUtils.toJson(FRM_MSG));

					Type setType = new TypeToken<Set<QDataAskMessage>>() {
					}.getType();

					String askMsgs2Str = VertxUtils.getObject(userToken.getRealm(), "", rootFrameCode + "_ASKS",
							String.class, userToken.getToken());

					Set<QDataAskMessage> askMsgs2 = JsonUtils.fromJson(askMsgs2Str, setType);

					// System.out.println("Sending Asks");
					if ((askMsgs2 != null) && (!askMsgs2.isEmpty())) {
						for (QDataAskMessage askMsg : askMsgs2) { // TODO, not needed
							for (Ask aask : askMsg.getItems()) {
								for (Validation val : aask.getQuestion().getAttribute().getDataType()
										.getValidationList()) {
									if (val.getRegex() == null) {
										log.error("Regex for " + aask.getQuestion().getCode() + " == null");
									}

								}
							}
							askMsg.setToken(userToken.getToken());
							String json = JsonUtils.toJson(askMsg);
							String jsonStr = json.replaceAll("PER_SERVICE", userToken.getUserCode()); // set the user
							VertxUtils.writeMsg("webcmds", jsonStr); // QDataAskMessage
						}
					}
				} else {
					log.error(rootFrameCode + "_MSG" + " DOES NOT EXIST IN CACHE - cannot display frame");
				}

			}

		}	}
	
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
	       if (executorService != null) {
	            executorService.destroy();
	        }
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
		
		
	    String jsonString;
			jsonString = readLineByLineJava8( "src/test/resources/attributes.json" );
			VertxUtils.writeCachedJson(gToken.getRealm(), "attributes", jsonString, gToken.getToken());
			
			QDataAttributeMessage attributesMsg = JsonUtils.fromJson(jsonString, QDataAttributeMessage.class);
			Attribute[] attributeArray = attributesMsg.getItems();

			for (Attribute attribute : attributeArray) {
				RulesUtils.attributeMap.put(attribute.getCode(), attribute);
			}
	}

	
	public static Optional<Long> getProcessIdBySessionId(String sessionId) {
		return GennyKieSession.getProcessIdBysessionId(sessionId);
	}

	
	public static void sendData(GennyToken serviceToken, GennyToken userToken,String rootFrameCode, String targetFrameCode,   List<QDataBaseEntityMessage> msgs , Set<QDataAskMessage> askMsgs2)
	{

		if (userToken == null) {
			log.error("Must supply userToken!");

		} else {

			if (rootFrameCode == null) {
				log.error("Must supply a root Frame Code!");
			} else {
				log.info("Sending root Frame Code sent to display  = " + rootFrameCode);

				QDataBaseEntityMessage FRM_MSG = VertxUtils.getObject(userToken.getRealm(), "", rootFrameCode + "_MSG",
						QDataBaseEntityMessage.class, userToken.getToken());

				if (FRM_MSG != null) {

					if (targetFrameCode == null) {
						targetFrameCode = "FRM_ROOT";
					}

					for (QDataBaseEntityMessage msg : msgs) {
					for (BaseEntity targetFrame : msg.getItems()) {
						if (targetFrame.getCode().equals(targetFrameCode)) {

							log.info("ShowFrame : Found Targeted Frame BaseEntity : " + targetFrame);

							/* Adding the links in the targeted BaseEntity */
							Attribute attribute = new Attribute("LNK_FRAME", "LNK_FRAME", new DataType(String.class));

							for (BaseEntity sourceFrame : FRM_MSG.getItems()) {
								if (sourceFrame.getCode().equals(rootFrameCode)) {

									System.out.println("ShowFrame : Found Source Frame BaseEntity : " + sourceFrame);
									EntityEntity entityEntity = new EntityEntity(targetFrame, sourceFrame, attribute,
											1.0, "CENTRE");
									Set<EntityEntity> entEntList = targetFrame.getLinks();
									entEntList.add(entityEntity);
									targetFrame.setLinks(entEntList);

									/* Adding Frame to Targeted Frame BaseEntity Message */
									FRM_MSG.add(targetFrame);
									FRM_MSG.setReplace(true);
									break;
								}
							}
							break;
						}
					}
					}

					FRM_MSG.setToken(userToken.getToken());

					FRM_MSG.setReplace(true);

					VertxUtils.writeMsg("webcmds", JsonUtils.toJson(FRM_MSG));


					// System.out.println("Sending Asks");
					if ((askMsgs2 != null) && (!askMsgs2.isEmpty())) {
						for (QDataAskMessage askMsg : askMsgs2) { // TODO, not needed
							for (Ask aask : askMsg.getItems()) {
								for (Validation val : aask.getQuestion().getAttribute().getDataType()
										.getValidationList()) {
									if (val.getRegex() == null) {
										log.error("Regex for " + aask.getQuestion().getCode() + " == null");
									}

								}
							}
							askMsg.setToken(userToken.getToken());
							String json = JsonUtils.toJson(askMsg);
							String jsonStr = json.replaceAll("PER_SERVICE", userToken.getUserCode()); // set the user
							VertxUtils.writeMsg("webcmds", jsonStr); // QDataAskMessage
						}
					}
				} else {
					log.error(rootFrameCode + "_MSG" + " DOES NOT EXIST IN CACHE - cannot display frame");
				}

			}

		}
	}
	
	
	
	
    //Read file content into string with - Files.lines(Path path, Charset cs)
	 
 

	/**
	 * @return the taskService
	 */
	public TaskService getTaskService() {
		return  getRuntimeEngine().getTaskService();
	}

	public RuntimeEngine getGennyRuntimeEngine()
	{
		return getRuntimeEngine();
	}
	
	private static String readLineByLineJava8(String filePath)
    {
        StringBuilder contentBuilder = new StringBuilder();
 
        try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8))
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
 
        return contentBuilder.toString();
    }
	
	public void showStatuses(String... userCodes)
	{
			List<Status> statuses = new ArrayList<Status>();
	        statuses.add(Status.Ready);
	        statuses.add(Status.Completed);
	        statuses.add(Status.Created);
	        statuses.add(Status.Error);
	        statuses.add(Status.Exited);
	        statuses.add(Status.InProgress);
	        statuses.add(Status.Obsolete);
	        statuses.add(Status.Reserved);
	        statuses.add(Status.Suspended);
	        
	        List<String> groups = new ArrayList<String>();
	        
            for (String userCode2 : userCodes) {
            	if (userCode2.startsWith("GRP_")) {                 
                    String code = this.serviceToken.getRealm()+ "+" + userCode2;
                    groups.add(code);
                    System.out.println(userCode2+" "+showTaskNames(getTaskService().getTasksAssignedAsPotentialOwner(null, groups, "en-AU", 0,10)));           		
            	} else {
            		System.out.println(userCode2+"  "+showTaskNames(getTaskService().getTasksAssignedAsPotentialOwnerByStatus(this.serviceToken.getRealm()+"+"+userCode2.toUpperCase(), statuses, null)));
            	}
            }
        System.out.println();
	}
	
	 String showTaskNames(List<TaskSummary> tasks)
	 {
		 String ret = "";
		 if (tasks.isEmpty()) {
			return "(empty)"; 
		 } 
		 for (TaskSummary task : tasks) {
			 ret += "["+task.getName()+"("+task.getProcessId()+"):"+task.getStatusId()+"],";
		 }
		 return ret;
	 }
	 
		public void createTestUsersGroups() {
			
			PeopleAssignmentHelper peopleAssignmentHelper;
			peopleAssignmentHelper = new PeopleAssignmentHelper();
			List<OrganizationalEntity> organizationalEntities = new ArrayList<OrganizationalEntity>();
			String ids = "Software Developer,Project Manager";
			//peopleAssignmentHelper.processPeopleAssignments(ids, organizationalEntities, false);
			//PeopleAssignmentHelper.ACTOR_ID
			//PeopleAssignmentHelper.BUSINESSADMINISTRATOR_ID
			//PeopleAssignmentHelper.BUSINESSADMINISTRATOR_GROUP_ID
			//peopleAssignmentHelper.
			
			createTestGroup(this.serviceToken.getRealm(), "GRP_USERS", "Users",serviceToken);
			createTestGroup(this.serviceToken.getRealm(), "GRP_GADA", "GADA",serviceToken);
			createTestGroup(this.serviceToken.getRealm(), "GRP_CROWTECH", "Crowtech",serviceToken);
			createTestGroup(this.serviceToken.getRealm(), "GRP_OUTCOME", "Outcome.Life",serviceToken);
			createTestGroup(this.serviceToken.getRealm(), "Administrators", "Administrators",serviceToken);
			List<String> gada =new ArrayList<String>();
			gada.add("GRP_USERS");
			gada.add("GRP_GADA");
			groups.put("gada", gada);
			
			List<String> outcome =new ArrayList<String>();
			outcome.add("GRP_USERS");
			outcome.add("GRP_OUTCOME");
			groups.put("outcome", outcome);
			
			List<String>crowtech =new ArrayList<String>();
			crowtech.add("GRP_USERS");
			crowtech.add("GRP_CROWTECH");
			crowtech.add("GRP_GADA");
			crowtech.add("Administrators"); // TODO
			groups.put("crowtech", crowtech);
			
			List<String>both =new ArrayList<String>();
			both.add("GRP_USERS");
			both.add("GRP_GADA");
			both.add("GRP_OUTCOME");
			groups.put("gada-outcome", both);
		

			BaseEntity PER_USER1 = createTestUser(serviceToken.getRealm(), "PER_USER1", "Ginger", gada,serviceToken);
			BaseEntity acrow = createTestUser(serviceToken.getRealm(), "PER_ADAMCROW63_AT_GMAIL_COM", "Adam", crowtech,serviceToken);
			BaseEntity domenic = createTestUser(serviceToken.getRealm(), "PER_DOMENIC_AT_OUTCOME_LIFE", "Domenic", gada,serviceToken);
			BaseEntity gerard = createTestUser(serviceToken.getRealm(), "PER_GERARD_AT_OUTCOME_LIFE", "Gerard ", outcome,serviceToken);
			BaseEntity anish = createTestUser(serviceToken.getRealm(), "PER_ANISH_AT_GADA_IO", "Anish", new ArrayList<String>(),serviceToken);
			BaseEntity chris = createTestUser(serviceToken.getRealm(), "PER_CHRIS_AT_GADA_IO", "Chris", both,serviceToken);

		}

		private BaseEntity createTestGroup(String realm, String code, String name , GennyToken serviceToken) {
			BaseEntity group = createTestBaseEntity(realm, code, serviceToken);
			// save the new group
			VertxUtils.writeCachedJson(serviceToken.getRealm(), code,
					JsonUtils.toJson(group), serviceToken.getToken());
			Group JbpmGroup = (Group) TaskModelProvider.getFactory().newGroup(code);
			return group;
			
		}
		private BaseEntity createTestUser(String realm, String code, String name , List<String> groups, GennyToken serviceToken) {
			BaseEntity user = createTestBaseEntity(realm, code, serviceToken);
			// Link to each group
			Attribute linkAttribute = RulesUtils.getAttribute("LNK_CORE",serviceToken.getToken());

			for (String groupCode : groups) {
				 BaseEntity group = VertxUtils.getObject(serviceToken.getRealm(), "", groupCode,
							BaseEntity.class, serviceToken.getToken());
				 // now link the new user
				 try {
					group.addTarget(user, linkAttribute, 1.0);
					 VertxUtils.writeCachedJson(serviceToken.getRealm(), groupCode,
								JsonUtils.toJson(group), serviceToken.getToken());
				} catch (BadDataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			List<String> realmGroups = new ArrayList<String>();
			for (String str : groups) {
				realmGroups.add(realm+"+"+str);
			}
			String newGroupString = JsonUtils.toJson(realmGroups);
			Attribute groupsAttribute = RulesUtils.getAttribute("PRI_GROUPS",serviceToken.getToken());
			try {
				user.addAnswer(new Answer(user,user,groupsAttribute,newGroupString));
			} catch (BadDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// save the new user
			VertxUtils.writeCachedJson(serviceToken.getRealm(), code,
					JsonUtils.toJson(user), serviceToken.getToken());

			User JbpmUser = (User) TaskModelProvider.getFactory().newUser(code);
			

			return user;
			
		}
		
		private BaseEntity createTestBaseEntity(String realm, String code, GennyToken serviceToken)
		{
			code = code.toUpperCase();
			BaseEntity be = new BaseEntity(code,
					StringUtils.capitaliseAllWords(serviceToken.getRealm()));
			be.setRealm(serviceToken.getRealm());
			VertxUtils.writeCachedJson(serviceToken.getRealm(), code,
					JsonUtils.toJson(be), serviceToken.getToken());
		//	VertxUtils.writeCachedJson(realm,  ":" + code,JsonUtils.toJson(be), serviceToken.getToken());
			return be;

		}

		private void setupAnswers()
		{
			answers = new HashMap<String,Answer>();
			createAnswer("PRI_FIRSTNAME","Bruce");
			createAnswer("PRI_LASTNAME","Wayne");
			createAnswer("PRI_ADDRESS_JSON","{\"street_number\":\"64\",\"street_name\":\"Fakenham Road\",\"suburb\":\"Ashburton\",\"state\":\"Victoria\",\"country\":\"AU\",\"postal_code\":\"3147\",\"full_address\":\"64 Fakenham Rd, Ashburton VIC 3147, Australia\",\"latitude\":-37.863208,\"longitude\":145.092359,\"street_address\":\"64 Fakenham Road\"}");
			createAnswer("PRI_MOBILE","0432212321");
			createAnswer("PRI_DOB","2000-01-02");
			createAnswer("PRI_EMAIL","bruce@gmail.com");
			createAnswer("PRI_PREFERRED_NAME","Brucey");
			createAnswer("PRI_USER_PROFILE_PICTURE","https://image.com");
			createAnswer("PRI_ADDRESS_FULL","17 Hardware Lane, MELBOURNE, VIC, 3000");

		}
		
		public Answer createAnswer(final String attributeCode, final String value )
		{
			return createAnswer(attributeCode, value, false, 1.0);
		}

		
		public Answer createAnswer(final String attributeCode, final String value, final Boolean changeEvent, final Double weight )
		{
			Answer answer = new Answer("SOURCE","TARGET", attributeCode, value, changeEvent);
			answer.setWeight(weight);
			answers.put(attributeCode, answer);
			return answer;
		}

		 private void setupMessages()
		 {
			 	messages = new HashMap<String,Object>();
				createMessage("initProject",new SessionFacts(serviceToken, null, new QEventMessage("EVT_MSG", "INIT_STARTUP")));
				createMessage("authInitMsg",new QEventMessage("EVT_MSG", "AUTH_INIT"));
				createMessage("QUE_SUBMIT",new QEventMessage("EVT_MSG", "QUE_SUBMIT"));
				createMessage("QUE_CANCEL",new QEventMessage("EVT_MSG", "QUE_CANCEL"));
				createMessage("QUE_RESET",new QEventMessage("EVT_MSG", "QUE_RESET"));
				createMessage("msgLogout",new QEventMessage("EVT_MSG", "LOGOUT"));
		 }
		 
		 private void setupCache()
		 {

				// NOW SET UP Some baseentitys
				BaseEntity project = new BaseEntity("PRJ_" + serviceToken.getRealm().toUpperCase(),
						StringUtils.capitaliseAllWords(serviceToken.getRealm()));
				project.setRealm(serviceToken.getRealm());
				
				createCacheBE("PRJ_" + serviceToken.getRealm().toUpperCase(),project);

				VertxUtils.writeCachedJson(GennySettings.GENNY_REALM,
						"TOKEN" + serviceToken.getRealm().toUpperCase(),serviceToken.getToken());

		 }
		 
		 protected QEventMessage getQEventMessage(String key) {
			 return (QEventMessage) messages.get(key);
		 }
		 
		 protected SessionFacts getSessionFacts(String key) {
			 return (SessionFacts) messages.get(key);
		 }
		 
		 private <T extends BaseEntity> void createCacheBE(String code, T be)
		 {
			 VertxUtils.writeCachedJson(serviceToken.getRealm(), code,
						JsonUtils.toJson(be), serviceToken.getToken());
			 
		 }
		 
		 private Object createMessage(String key,Object payload)
		 {
			 messages.put(key, payload);
			 return payload;
		 }
		 
		 public GennyToken createToken(String code)
		 {
			 String name = StringUtils.capitaliseAllWords(code);
			 return GennyJbpmBaseTest.createGennyToken(this.serviceToken.getRealm(), code, name, "user");
		 }


}