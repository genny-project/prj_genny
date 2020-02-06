package life.genny.test;

import java.io.FileNotFoundException;
import java.io.IOException;
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
import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.jbpm.services.task.utils.TaskFluent;
import org.jbpm.services.task.wih.NonManagedLocalHTWorkItemHandler;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.json.JSONObject;
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

import io.vertx.core.json.JsonObject;

import life.genny.eventbus.EventBusInterface;
import life.genny.eventbus.EventBusMock;
import life.genny.eventbus.VertxCache;
import life.genny.models.GennyToken;
import life.genny.qwanda.Answer;
import life.genny.qwanda.Ask;
import life.genny.qwanda.Context;
import life.genny.qwanda.ContextList;
import life.genny.qwanda.ContextType;
import life.genny.qwanda.Question;
import life.genny.qwanda.VisualControlType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.message.QDataAnswerMessage;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwandautils.GennyCacheInterface;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaMessage;
import life.genny.rules.QRules;
import life.genny.utils.BaseEntityUtils;
import life.genny.utils.GennyJbpmBaseTest;
import life.genny.utils.GennyKieSession;
import life.genny.utils.QuestionUtils;
import life.genny.utils.RulesUtils;
import life.genny.utils.SessionFacts;
import life.genny.utils.VertxUtils;

import life.genny.bootxport.bootx.GoogleImportService;

public class JasperTest {

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
    
	protected static Boolean USE_STANDALONE= true;   // sets whether to use standalone or remote service
    
    protected static  GennyToken userToken;
	protected static  GennyToken newUserToken;
	protected static  GennyToken serviceToken;

	public JasperTest() {

	}
	
	@Test
	public void TestTest() throws ClientProtocolException, IOException {
		GennyToken userToken = null;
        GennyToken serviceToken = null;
        QRules qRules = null;

        if (false) {
            userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
            serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
            qRules = new QRules(eventBusMock, userToken.getToken());
            qRules.set("realm", userToken.getRealm());
            qRules.setServiceToken(serviceToken.getToken());
            VertxUtils.cachedEnabled = true; // don't send to local Service Cache
        } else {
            VertxUtils.cachedEnabled = false; // don't send to local Service Cache
            qRules = GennyJbpmBaseTest.setupLocalService();
            userToken = new GennyToken("userToken", qRules.getToken());
            serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
        }
		
		/* Get the ask we are looking for */		
		QwandaMessage msg = QuestionUtils.getQuestions("PER_USER1", "PER_USER1", "QUE_STT_S1_A_Q1_GRP", serviceToken.getToken());
		Ask askParent = msg.asks.getItems()[0];
		
		/* get the specific child question from that ask */
		Ask ask = askParent.getChildAsks()[0];
		
		/* construct the Context */
		Context context = new Context(ContextType.UNITY, "UNT_S1_DATA");
		
		ContextList contextList = ask.getContextList();
		List<Context> ctxList = contextList.getContextList();
		ctxList.add(context);
	
		contextList.setContextList(ctxList);
		ask.setContextList(contextList);
		
		JsonObject askObj = new JsonObject(JsonUtils.toJson(ask));
		
//		/* submit that context */
//		QuestionUtils.addContextToAsk(ask, context, serviceToken);
//		
//		
//		String questionCode = "QUE_STT_S1_A_Q1_GRP";
//		
//		/* Get the ask we are looking for */		
//		QwandaMessage msg2 = QuestionUtils.getQuestions("PER_USER1", "PER_USER1", questionCode, serviceToken.getToken());
//		Ask ask2 = msg.asks.getItems()[0];
//		System.out.println("Ask qCode: " + ask2.getQuestionCode());
//		
//		/* get the specific child question from that ask */
//		Ask childAsk2 = ask.getChildAsks()[0];
//		System.out.println("chldAsk qCode: " + childAsk2.getQuestionCode());
//		
//		System.out.println("ask2: " + ask2.getContextList());
//		System.out.println("ChildAsk2: " + childAsk2.getContextList());
		
		
//		VertxUtils.writeCachedJson(serviceToken.getRealm(), ask.getQuestionCode(), askObj.toString(), serviceToken.getToken());
//
//
//
//
//		JsonObject askJson = VertxUtils.readCachedJson(serviceToken.getRealm(), ask.getQuestionCode(), serviceToken.getToken());
//		
//		Ask newAsk = JsonUtils.fromJson(askJson.getString("value"), life.genny.qwanda.Ask.class);
//		
//		System.out.println("CACHED ASK: " + newAsk);
//		System.out.println("CACHED ASK CONTEXTS: " + newAsk.getContextList());
        
        BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);		
                
        
        JsonObject ctxJson = VertxUtils.readCachedJson(serviceToken.getRealm(), "CTX_UNITY_CONTEXT" , serviceToken.getToken());
		System.out.println("CTX JSON: " + ctxJson);
//		JsonObject contextJson = new JsonObject(ctxJson.getString("value"));
//		Context ctx = JsonUtils.fromJson(ctxJson.getString("value"), life.genny.qwanda.Context.class);
//		System.out.println("CTX CODE: " + ctx.getContextCode());
//		BaseEntity unityBe = BaseEntityUtils.getBaseEntityByCode(ctx.getContextCode());
//		System.out.println("CTX Entity: " + ctx.getEntity());
//		ctx.setEntity(unityBe);
        
        
//        String ctxJson = JsonUtils.toJson(context);
//        System.out.println(ctxJson);
//        
//        JsonObject obj = new JsonObject();//.remove("realm");	3
//        obj.put("weight", 1.0);
//        obj.put("contextCode", "UNT_S1_DATA");
//        obj.put("visualControlType", "VCL_DEFAULT");
//        obj.put("created", "2020-02-02T23:45:19.405");
//        obj.put("name", "UNITY");
//        obj.put("realm", "genny");
        
        /*
        String test = "{ \"weight\": 1.0,\"contextCode\": \"UNT_S1_DATA\",\"visualControlType\": \"VCL_DEFAULT\",\"created\": \"2020-02-03T00:08:47.258\", \"name\": \"UNITY\",\"realm\": \"genny\" }";
        
        Context ctx = JsonUtils.fromJson(test, life.genny.qwanda.Context.class);
        
        System.out.println("ctx: " + ctx);
        
        */
        
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

        HashMap<String, BaseEntity> hashBeg = new HashMap<String, BaseEntity>();

        System.out.println("session     =" + userToken.getSessionCode());
        System.out.println("userToken   =" + userToken.getToken());
        System.out.println("serviceToken=" + serviceToken.getToken());


        // NOW SET UP Some baseentitys
        BaseEntity project = new BaseEntity("PRJ_" + serviceToken.getRealm().toUpperCase(),
                StringUtils.capitaliseAllWords(serviceToken.getRealm()));
        project.setRealm(serviceToken.getRealm());
        VertxUtils.writeCachedJson(serviceToken.getRealm(), "PRJ_" + serviceToken.getRealm().toUpperCase(),
                JsonUtils.toJson(project), serviceToken.getToken());
        
        // Log out to begin
        VertxUtils.writeCachedJson(userToken.getRealm(),userToken.getSessionCode(),null,userToken.getToken());
        
        GennyKieSession gks = null;
        
        System.out.println("Debug..");

        try {
        	gks = GennyKieSession.builder(serviceToken,true)
					.addJbpm("sigTest.bpmn")
                    .addJbpm("sendSignaturesTest.bpmn")
                    .addJbpm("baseEntityValidation.bpmn")

                    .addToken(userToken)
                    .build();
            
            System.out.println("Debug statement 1 [*]");
            
            gks.start();
            gks.startProcess("sigTest");
            
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
            gks.close();
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
	
    //@BeforeClass
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
		
		QRules qRules = null;

		if (USE_STANDALONE) {
			serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
			VertxUtils.cachedEnabled = true; // don't send to local Service Cache
			GennyKieSession.loadAttributesJsonFromResources(serviceToken);

		} else {
			qRules = GennyJbpmBaseTest.setupLocalService();
			userToken = new GennyToken("userToken", qRules.getToken());
			serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
		}

		System.out.println("serviceToken=" + serviceToken.getToken());

		
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