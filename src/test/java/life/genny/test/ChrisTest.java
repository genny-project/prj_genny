package life.genny.test;

import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.jbpm.services.task.utils.TaskFluent;
import org.jbpm.services.task.wih.NonManagedLocalHTWorkItemHandler;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.internal.task.api.model.InternalComment;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import life.genny.eventbus.EventBusInterface;
import life.genny.eventbus.EventBusMock;
import life.genny.eventbus.VertxCache;
import life.genny.models.GennyToken;
import life.genny.qwanda.Answer;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.message.QDataAnswerMessage;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwandautils.GennyCacheInterface;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.rules.QRules;
import life.genny.utils.RulesUtils;
import life.genny.utils.SessionFacts;
import life.genny.utils.VertxUtils;

public class ChrisTest {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	protected static String realm = GennySettings.mainrealm;
	protected static Set<String> realms;

	protected static EventBusInterface eventBusMock;
	protected static GennyCacheInterface vertxCache;
	
	/* Constant(s): */
    public static final String AMQ_BROKER_URL = "tcp://localhost:61616";
    public static final String QUEUE_NAME = "queue/KIE.SERVER.EXECUTOR";
 
    /* Instance variable(s): */
    protected ConnectionFactory mActiveMQConnectionFactory;
    protected JmsTemplate mJmsTemplate;

	public ChrisTest() {

	}
	
	@Test
	public void userTaskTest()
	{
		System.out.println("UserTask TestXXX");
		GennyToken userToken = null;
		GennyToken serviceToken = null;
		QRules qRules = null;

		if (true) {
			userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
			serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
			qRules = new QRules(eventBusMock, userToken.getToken());
			qRules.set("realm", userToken.getRealm());
			qRules.setServiceToken(serviceToken.getToken());
			VertxUtils.cachedEnabled = true; // don't send to local Service Cache
			GennyKieSession.loadAttributesJsonFromResources(userToken);

		} else {
			qRules = GennyJbpmBaseTest.setupLocalService();
			userToken = new GennyToken("userToken", qRules.getToken());
			serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
		}

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		//System.out.println("userToken2   =" + userToken2.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());
		
        BaseEntity intern = new BaseEntity("PRI_INTERN");
        BaseEntity internship = new BaseEntity("BE_INTERNSHIP");        
        BaseEntity hostCompany = new BaseEntity("CPY_HOSTCOMPANY");

        /*HashMap<String, BaseEntity> hashBeg = new HashMap<String, BaseEntity>();*/
        HashMap<String, String> hashBeg = new HashMap<String, String>();
        
        hashBeg.put("begstatus", "DUDE");
        
        /*hashBeg.put("intern", intern);
        hashBeg.put("internship", internship);
        hashBeg.put("hostCompany", hostCompany);*/

		SessionFacts initFacts = new SessionFacts(serviceToken, null, new QEventMessage("EVT_MSG", "INIT_STARTUP"));
		QEventMessage authInitMsg = new QEventMessage("EVT_MSG", "AUTH_INIT"); authInitMsg.setToken(userToken.getToken());
		QEventMessage msgLogout = new QEventMessage("EVT_MSG", "LOGOUT");msgLogout.setToken(userToken.getToken());


		// NOW SET UP Some baseentitys
		BaseEntity project = new BaseEntity("PRJ_" + serviceToken.getRealm().toUpperCase(),
				StringUtils.capitaliseAllWords(serviceToken.getRealm()));
		project.setRealm(serviceToken.getRealm());
		VertxUtils.writeCachedJson(serviceToken.getRealm(), "PRJ_" + serviceToken.getRealm().toUpperCase(),
				JsonUtils.toJson(project), serviceToken.getToken());
		VertxUtils.writeCachedJson(realm,  ":" + "PRJ_" + serviceToken.getRealm().toUpperCase(),JsonUtils.toJson(project), serviceToken.getToken());


		GennyKieSession gks = null;

		try {
			gks = GennyKieSession.builder(serviceToken,true)
				//	.addDrl("SignalProcessing")
				//	.addDrl("DataProcessing")
				//	.addJbpm("Lifecycles")
					.addJbpm("cardsA.bpmn")
					.addJbpm("cardsB.bpmn")
					.addJbpm("cardsC.bpmn")
//					.addJbpm("cardsC.bpmn")
//					.addJbpm("processLifecycle.bpmn")
//					.addJbpm("placementLifecycle.bpmn")
//					.addJbpm("internshipLifecycle.bpmn")
//					.addDrl("TaskRouting")
					.addJbpm("notificationHub2.bpmn")
					.addJbpm("baseEntityValidation.bpmn")
//					.addDrl("ADD_APPLICATION_ATTRIBUTES.drl")
//					.addJbpm("placementLifecycle.bpmn")
					.addDrl("MoveBucket")
					.addDrl("CommonEnter")
					.addDrl("SpecificEnter")
					.addDrl("SpecificReminder")
//					.addDrl("EventProcessing")
					.addDrl("Timer")
				//	.addJbpm("AuthInit")
				//	.addDrl("InitialiseProject")
				//	.addJbpm("InitialiseProject")

					.addToken(userToken)
					.build();
			
			gks.start();
//			gks.injectEvent();
			
//			gks.injectSignal("newCompany", hashBeg);
//			gks.injectSignal("newTask", "newTask");
//			gks.startProcess("processLifecycle");
			gks.startProcess("cardsA");
			
            gks.advanceSeconds(5, false);
//            gks.injectSignal("status", "FORWARD"); 		// Applied to Shortlist
            gks.advanceSeconds(5, false);
//            gks.injectSignal("status", "FORWARD"); 		// Shortlist to Interview
//            gks.advanceSeconds(5, false);
//            gks.injectSignal("status", "BACKWARD");		// Interview back to Shortlist 
//            gks.advanceSeconds(5, false);
//            gks.injectSignal("status", "FORWARD");		// Shortlist to Interview
//            gks.advanceSeconds(5, false);
//            gks.injectSignal("status", "FORWARD");		// Interview to Offered
//            gks.advanceSeconds(5, false);
//            gks.injectSignal("status", "FORWARD");		// Offered to Placed
//            gks.advanceSeconds(5, false);
//            gks.injectSignal("status", "FORWARD");		// Placed to Progress

			/*
			BaseEntity icn_sort = new BaseEntity("ICN_SORT","Icon Sort");
			try {
				
				icn_sort.addAttribute(RulesUtils.getAttribute("PRI_ICON_CODE", serviceToken.getToken()), 1.0, "sort");
				icn_sort.setRealm(realm);
				VertxUtils.writeCachedJson(realm,   "ICN_SORT",JsonUtils.toJson(icn_sort), serviceToken.getToken());

			} catch (BadDataException e1) {
				e1.printStackTrace();
			}

			//gks.injectSignal("initProject", initFacts); // This should initialise everything
			gks.advanceSeconds(5, false);
			
			gks.getKieSession().getWorkItemManager().registerWorkItemHandler("Human Task", new NonManagedLocalHTWorkItemHandler(gks.getKieSession(),gks.getTaskService()));
			
		       // One potential owner, should go straight to state Reserved
//	        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
//	        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [new User('Bobba Fet'), new User('Darth Vader') ], excludedOwners = [new User('Darth Vader')],businessAdministrators = [ new User('Administrator') ], }),";
//	        str += "name =  'This is my task name' })";
//	        Task task = TaskFactory.evalTask(new StringReader(str));
//	        gks.getTaskService().addTask(task, new HashMap<String, Object>());
			//gks.getTaskService().
	        List<TaskSummary> tasks = null;

	        User acrow = (User) TaskModelProvider.getFactory().newUser("acrow");
	        // Start a process
	        gks.startProcess("adam_user1");
	        gks.advanceSeconds(5, false);
	        Map<String,Object> params = new HashMap<String,Object>();
	        Task task = new TaskFluent().setName("Amazing GADA Stuff")
	                .addPotentialGroup("GADA")
	                .setAdminUser("acrow")
	             //   .addPotentialUser("acrow")
	                .setProcessId("direct")
	                .setDeploymentID("genny")
	                .getTask();

	        Task task2 = new TaskFluent().setName("Awesome GADA stuff")
	              //  .addPotentialGroup("GADA")
	                .setAdminUser("Administrator")
	                .addPotentialUser("domenic")
	                .setDeploymentID("genny")
	                .setProcessId("direct")
	                .getTask();

	        Task task3 = new TaskFluent().setName("Boring Outcome Stuff")
	                .addPotentialGroup("OUTCOME")
	                .setAdminUser("Administrator")
	                .addPotentialUser("gerard")
	                .setProcessId("direct")
	                .setDeploymentID("genny")
	                .getTask();


	        gks.getTaskService().addTask(task, params);
	        gks.getTaskService().addTask(task2, params);
	        gks.getTaskService().addTask(task3, params);
	        long taskId = task.getId();
	        long taskId2 = task2.getId();
	        long taskId3 = task3.getId();

              // Do Task Operations
            
              showStatuses(gks);

            
            // Add Comment
            InternalComment commentImpl = (InternalComment) TaskModelProvider.getFactory().newComment();
            
            commentImpl.setAddedAt(new Date());
            commentImpl.setAddedBy(acrow);
            gks.getTaskService().addComment(taskId2, commentImpl);
            
             Map<String, Object> content = gks.getTaskService().getTaskContent(taskId2 );
             System.out.println(content);
              // Start Task
              gks.getTaskService().start(taskId, "acrow");    
              showStatuses(gks);

              gks.getTaskService().suspend(taskId, "acrow");    
              showStatuses(gks);

              gks.getTaskService().resume(taskId, "acrow");    
              showStatuses(gks);
              
              gks.getTaskService().forward(taskId2, "domenic", "anish");

              // Claim Task
//              gks.getTaskService().claim(taskId, "acrow");
//              showStatuses(gks);
//          
              Map<String, Object> results = new HashMap<String, Object>();
              results.put("Result", "Done");
              gks.getTaskService().complete(taskId, "acrow", results);
              showStatuses(gks);

              results.put("Result", "some document data");

//              long processInstanceId =
//            		  processService.startProcess(deployUnit.getIdentifier(), "org.jbpm.writedocument");
//            		  List<Long> taskIds =
//            		  runtimeDataService.getTasksByProcessInstanceId(processInstanceId);
//            		  Long taskId4 = taskIds.get(0);
//            		  userTaskService.start(taskId, "john");
//            		  UserTaskInstanceDesc task4 = runtimeDataService.getTaskById(taskId4);
//            		  Map<String, Object> results = new HashMap<String, Object>();
//            		  results.put("Result", "some document data");
//            		  userTaskService.complete(taskId4, "john", results);
             
			gks.injectEvent(authInitMsg); // This should create a new process
			gks.advanceSeconds(5, false);

			BaseEntity user = VertxUtils.getObject(serviceToken.getRealm(), "", userToken.getUserCode(),
					BaseEntity.class, serviceToken.getToken());

			gks.injectEvent(msgLogout);
			
			*/
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		finally {
			if (gks!=null) {
				gks.close();
			}
		}
	}
	
	//@Test
    public void LifecycleTest() {
        GennyToken userToken = null;
        GennyToken serviceToken = null;
        QRules qRules = null;

        if (true) {
            userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
            serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
            qRules = new QRules(eventBusMock, userToken.getToken());
            qRules.set("realm", userToken.getRealm());
            qRules.setServiceToken(serviceToken.getToken());
            VertxUtils.cachedEnabled = true; // don't send to local Service Cache
        } else {
            qRules = GennyJbpmBaseTest.setupLocalService();
            userToken = new GennyToken("userToken", qRules.getToken());
            serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
        }
        
        BaseEntity intern = new BaseEntity("PRI_INTERN");
        BaseEntity internship = new BaseEntity("BE_INTERNSHIP");        
        BaseEntity hostCompany = new BaseEntity("CPY_HOSTCOMPANY");
//        BaseEntity xyz = new BaseEntity("BE_XYZ");

        HashMap<String, BaseEntity> hashBeg = new HashMap<String, BaseEntity>();
        
        hashBeg.put("intern", intern);
        hashBeg.put("internship", internship);
        hashBeg.put("hostCompany", hostCompany);
//        hashBeg.put("xyz", xyz);

        System.out.println("session     =" + userToken.getSessionCode());
        System.out.println("userToken   =" + userToken.getToken());
        System.out.println("serviceToken=" + serviceToken.getToken());

        QEventMessage authInitMsg = new QEventMessage("EVT_MSG", "AUTH_INIT");
        authInitMsg.setToken(userToken.getToken());
        QEventMessage msg1 = new QEventMessage("EVT_MSG", "INIT_1");
        QEventMessage msgLogout = new QEventMessage("EVT_MSG", "LOGOUT");
        QEventMessage menu = new QEventMessage("EVT_MSG", "MENU");

        // NOW SET UP Some baseentitys
        BaseEntity project = new BaseEntity("PRJ_" + serviceToken.getRealm().toUpperCase(),
                StringUtils.capitaliseAllWords(serviceToken.getRealm()));
        project.setRealm(serviceToken.getRealm());
        VertxUtils.writeCachedJson(serviceToken.getRealm(), "PRJ_" + serviceToken.getRealm().toUpperCase(),
                JsonUtils.toJson(project), serviceToken.getToken());
        
        // Log out to begin
        VertxUtils.writeCachedJson(userToken.getRealm(),userToken.getSessionCode(),null,userToken.getToken());

        GennyKieSession gks = null;

        try {
            gks = GennyKieSession
            		.builder(serviceToken, true)
                    .addJbpm("genericApplication.bpmn")
                    .addJbpm("baseEntityValidation.bpmn")
//                    .addJbpm("notificationHub.bpmn")
//                    .addJbpm("placementLifecycle.bpmn")
//                    .addDrl("GREEN.drl")
                    .addDrl("ADD_APPLICATION_ATTRIBUTES.drl")
                    .addToken(userToken)
                    .build();
            
            gks.start();

            gks.injectSignal("newApplication", hashBeg);
            
            gks.advanceSeconds(5, false);
            
            gks.injectSignal("controlSignal", "FORWARD");

            gks.advanceSeconds(5, false);
            
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        } finally {
            gks.close();
        }
        
    }

	//@Test
	public void newUserTest() {
		System.out.println("New User test");
		GennyToken userToken = null;
		GennyToken userToken2 = null;
		GennyToken serviceToken = null;
		QRules qRules = null;

		if (true) {
			userToken = GennyJbpmBaseTest.createGennyToken(realm, "user13", "Barry Allan", "user");
			userToken2 = GennyJbpmBaseTest.createGennyToken(realm, "user2", "Barry2 Allan2", "user");
			serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
			qRules = new QRules(eventBusMock, userToken.getToken());
			qRules.set("realm", userToken.getRealm());
			qRules.setServiceToken(serviceToken.getToken());
			VertxUtils.cachedEnabled = true; // don't send to local Service Cache
		} else {
			qRules = GennyJbpmBaseTest.setupLocalService();
			userToken = new GennyToken("userToken", qRules.getToken());
			serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
		}

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("userToken2   =" + userToken2.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage initMsg = new QEventMessage("EVT_MSG", "INIT_STARTUP");

		QEventMessage authInitMsg1 = new QEventMessage("EVT_MSG", "AUTH_INIT");
		authInitMsg1.setToken(userToken.getToken());
//		QEventMessage authInitMsg2 = new QEventMessage("EVT_MSG", "AUTH_INIT");
//		authInitMsg2.setToken(userToken2.getToken());
//		QEventMessage msg1 = new QEventMessage("EVT_MSG", "INIT_1");
		QEventMessage msgLogout1 = new QEventMessage("EVT_MSG", "LOGOUT");
		msgLogout1.setToken(userToken.getToken());
//		QEventMessage msgLogout2 = new QEventMessage("EVT_MSG", "LOGOUT");
//		msgLogout2.setToken(userToken2.getToken());

		List<Answer> answers = new ArrayList<Answer>();
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_FIRSTNAME", "Bruce"));
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_LASTNAME", "Wayne"));
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_ADDRESS_JSON",
				"{\"street_number\":\"64\",\"street_name\":\"Fakenham Road\",\"suburb\":\"Ashburton\",\"state\":\"Victoria\",\"country\":\"AU\",\"postal_code\":\"3147\",\"full_address\":\"64 Fakenham Rd, Ashburton VIC 3147, Australia\",\"street_address\":\"64 Fakenham Road\"}"));

		QDataAnswerMessage answerMsg = new QDataAnswerMessage(answers.toArray(new Answer[0]));
		answerMsg.setToken(userToken.getToken());

		// NOW SET UP Some baseentitys
		BaseEntity project = new BaseEntity("PRJ_" + serviceToken.getRealm().toUpperCase(),
				StringUtils.capitaliseAllWords(serviceToken.getRealm()));
		project.setRealm(serviceToken.getRealm());
		VertxUtils.writeCachedJson(serviceToken.getRealm(), "PRJ_" + serviceToken.getRealm().toUpperCase(),
				JsonUtils.toJson(project), serviceToken.getToken());

		GennyKieSession gks = null;

		System.setProperty("org.kie.executor.jms", "false");
		try {
			gks = GennyKieSession
					.builder(serviceToken, true)
					.addDrl("SignalProcessing")
					.addDrl("DataProcessing")
					.addDrl("EventProcessing")
					.addDrl("InitialiseProject")
					.addJbpm("InitialiseProject")
					.addJbpm("Lifecycles")
					.addDrl("AuthInit")
					.addJbpm("AuthInit")
//					.addJbpm("userSession.bpmn")
//					.addJbpm("userValidation.bpmn")
//					.addJbpm("userLifecycle.bpmn")
//					.addJbpm("userApplication.bpmn")
			//		.addJbpm("auth_init.bpmn")
					.addToken(userToken).build();
			
			
			gks.start();
//			gks.injectEvent(initMsg); // This should create a new process

			gks.injectEvent(authInitMsg1); // This should create a new process
			gks.advanceSeconds(5, false);

//			gks.injectEvent(authInitMsg1); // This should attach to existing process
//			gks.advanceSeconds(5, false);

//			gks.injectEvent(answerMsg); // This sends an answer to the first userSessio
//			gks.advanceSeconds(5, false);
			gks.injectEvent(msgLogout1);
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			if (gks != null) {
				gks.close();
			}
		}
	}

	
	//@Test
    public void someIntegrationTest() throws Exception {
        System.out.println("Test starting...");
        sendMessages();
        receiveMessages();
        System.out.println("Test done!");
    }
 
    protected void sendMessages() {
        for (int i = 1; i <= 10; i++) {
            final int theMessageIndex = i;
            final String theMessageString = "Message: " + theMessageIndex;
            System.out.println("Sending message with text: " + theMessageString);
 
            mJmsTemplate.send(new MessageCreator() {
                public Message createMessage(Session inJmsSession) throws JMSException {
                    TextMessage theTextMessage = inJmsSession.createTextMessage(theMessageString);
                    theTextMessage.setIntProperty("messageNumber", theMessageIndex);
 
                    return theTextMessage;
                }
            });
        }
    }
 
    protected void receiveMessages() throws Exception {
        Message theReceivedMessage = mJmsTemplate.receive();
        while (theReceivedMessage != null) {
            if (theReceivedMessage instanceof TextMessage) {
                final TextMessage theTextMessage = (TextMessage)theReceivedMessage;
                System.out.println("Received a message with text: " + theTextMessage.getText());
            }
 
            theReceivedMessage = mJmsTemplate.receive();
        }
        System.out.println("All messages received!");
    }
	
	@BeforeClass
	public static void init() throws FileNotFoundException, SQLException {

		System.out.println("BridgeUrl=" + GennySettings.bridgeServiceUrl);
		System.out.println("QwandaUrl=" + GennySettings.qwandaServiceUrl);

		// Set up realm
		realms = new HashSet<String>();
		realms.add(realm);
		realms.stream().forEach(System.out::println);
		realms.remove("genny");

		// Enable the PseudoClock using the following system property.
		System.setProperty("drools.clockType", "pseudo");

		eventBusMock = new EventBusMock();
		vertxCache = new VertxCache(); // MockCache
		VertxUtils.init(eventBusMock, vertxCache);
		
	 

	}
	
	@Before
    public void setUp() {
        mActiveMQConnectionFactory = new ActiveMQConnectionFactory(AMQ_BROKER_URL);
        mJmsTemplate = new JmsTemplate(mActiveMQConnectionFactory);
        final Destination theTestDestination = new ActiveMQQueue(QUEUE_NAME);
        mJmsTemplate.setDefaultDestination(theTestDestination);
        mJmsTemplate.setReceiveTimeout(500L);
    }

}