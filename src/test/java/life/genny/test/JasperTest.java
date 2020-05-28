package life.genny.test;

import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.json.JSONArray;
import org.json.JSONException;
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

import life.genny.eventbus.EventBusInterface;
import life.genny.eventbus.EventBusMock;
import life.genny.eventbus.VertxCache;
import life.genny.models.GennyToken;
import life.genny.qwanda.Answer;
import life.genny.qwanda.Ask;
import life.genny.qwanda.ContextList;
import life.genny.qwanda.Link;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.SearchEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.message.QDataAnswerMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwandautils.GennyCacheInterface;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.rules.QRules;
import life.genny.utils.BaseEntityUtils;
import life.genny.utils.BucketUtils;
import life.genny.utils.GennyJbpmBaseTest;
import life.genny.utils.GennyKieSession;
import life.genny.utils.OutputParam;
import life.genny.utils.RulesUtils;
import life.genny.utils.SearchUtils;
import life.genny.utils.SessionFacts;
import life.genny.utils.VertxUtils;

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
	
	//@Test
    public void SBETest() {
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
        	VertxUtils.cachedEnabled = false;
            qRules = GennyJbpmBaseTest.setupLocalService();
            userToken = new GennyToken("userToken", qRules.getToken());
            serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
        }
        
        
        BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
        SearchUtils searchUtils = new SearchUtils(beUtils);
		BucketUtils bucketUtils = new BucketUtils(beUtils);
		
		
		Map<String, ContextList> contextListMap = new HashMap<String, ContextList>();
		

		Ask FRM_BUCKET_CONTENT_ASK = bucketUtils.getBucketContentAsk(contextListMap, userToken);
		
		
		contextListMap = bucketUtils.getCardContextListMap(contextListMap, userToken);

        
		List<BaseEntity> beList = new ArrayList<BaseEntity>();
        
        SearchEntity searchBe = new SearchEntity("SBE_AVAILABLE_APPLICATIONS", "Available")
                .addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "PER_%")
                .addFilter("PRI_IS_INTERN", true)
                .addFilter("PRI_DISABLED", false) 
                .addColumn("PRI_STATUS_COLOR", "Status")
                .addColumn("PRI_INTERN_IMAGE_URL", "Image")
                .addColumn("PRI_NAME", "Name")
                .addColumn("PRI_EMAIL", "Email")
                .addColumn("PRI_MOBILE", "Mobile")
                .addColumn("PRI_STUDENT_ID", "Student ID")
                .addColumn("PRI_STATUS", "Status")
                .addColumn("PRI_NAME", "Name")
                .addColumn("PRI_CURRENT_COURSE", "Current Course")
                .addColumn("PRI_PROGRESS", "Progress")
                .setPageStart(0).setPageSize(1000);
        
        /* fetch the search results */
		QDataBaseEntityMessage msg = searchUtils.fetchSearchResults(searchBe, serviceToken);

		/* get the application counts */
		System.out.println("Item count: " + msg.getItems().length);
		
		for (BaseEntity entity : msg.getItems()) {
			System.out.println(entity);
		}
		
    }
	
	
	//@Test
    public void BucketTest() {
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
        	VertxUtils.cachedEnabled = false;
            qRules = GennyJbpmBaseTest.setupLocalService();
            userToken = new GennyToken("userToken", qRules.getToken());
            serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
        }
        
        
        BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
        SearchUtils searchUtils = new SearchUtils(beUtils);
		BucketUtils bucketUtils = new BucketUtils(beUtils);
		
		
		Map<String, ContextList> contextListMap = new HashMap<String, ContextList>();
		

		Ask FRM_BUCKET_CONTENT_ASK = bucketUtils.getBucketContentAsk(contextListMap, userToken);
		
		
		contextListMap = bucketUtils.getCardContextListMap(contextListMap, userToken);

        
		List<BaseEntity> beList = new ArrayList<BaseEntity>();
        
        SearchEntity searchBe = new SearchEntity("SBE_INTERVIEWED_APPLICATIONS", "Interviewed")
                .addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC)
                .addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "APP_%")
                .addFilter("PRI_STATUS", SearchEntity.StringFilter.EQUAL, "INTERVIEWED")
                .addFilter("PRI_DISABLED", false)
              	.addColumn("PRI_STATUS_COLOR", "Status")
              	.addColumn("PRI_INTERN_IMAGE_URL", "Image")
              	.addColumn("PRI_ASSOC_INTERNSHIP", "Internship")
              	.addColumn("PRI_ASSOC_HC", "Host Company")
              /*.addColumn("PRI_HOST_COMPANY_NAME", "Host Company")*/
              	.addColumn("PRI_INTERN_NAME", "Name")
              	.addColumn("PRI_STATUS", "Status")
              	.addColumn("PRI_EDU_PROVIDER_NAME", "Edu Provider")
              	.addColumn("PRI_INTERN_EMAIL", "Email")
              	.addColumn("PRI_INTERN_MOBILE", "Mobile")
              	.addColumn("PRI_INTERN_STUDENT_ID", "Student ID")
              /*.addColumn("PRI_PROGRESS", "Progress")*/
                .setPageStart(0).setPageSize(1000);
        
        System.out.println("inside search loop  ::");
		String code = searchBe.getCode().split("SBE_")[1];
		System.out.println("code  ::" +code );

		/* get the attributes from searchObj */
		Map<String, String> columns = searchUtils.getTableColumns(searchBe);

		/* fetch the search results */
		QDataBaseEntityMessage msg = searchUtils.fetchSearchResults(searchBe, serviceToken);

		/* get the application counts */
		long totalResults = msg.getItems().length;
		System.out.println("items in bucket " + code + " is :: " + totalResults );

		/* also update the searchBe with the attribute */
		Answer totalAnswer = new Answer(beUtils.getGennyToken().getUserCode(), searchBe.getCode(),
				"PRI_TOTAL_RESULTS", totalResults + "");
		beUtils.addAnswer(totalAnswer);
		beUtils.updateBaseEntity(searchBe, totalAnswer);

		/* get the applications */
		List<BaseEntity> appList = Arrays.asList(msg.getItems());

		/* add the application to the baseentity list */
		beList.addAll(appList);

		/* convert app to asks */
		List<Ask> appAsksList = searchUtils.generateQuestions(beUtils.getGennyToken(), beUtils, appList,
				columns, beUtils.getGennyToken().getUserCode());
		
		/* get the templat ask for card */
		Ask templateAsk = bucketUtils.getCardTemplate();

		/* implement template ask to appAks list */
		List<Ask> askList = bucketUtils.implementCardTemplate(code, appAsksList, templateAsk, contextListMap);
		
        
    }
	
	
    //@Test
    public void WorkflowTest() {
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
        	VertxUtils.cachedEnabled = false;
            qRules = GennyJbpmBaseTest.setupLocalService();
            userToken = new GennyToken("userToken", qRules.getToken());
            serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
        }
        
        System.out.println("userToken: " + userToken.getToken());
        System.out.println("serviceToken: " + serviceToken.getToken());
      	
        
        JSONObject json = new JSONObject();
        try {
            json.put("targetEntity", "APP_TEST_APPLICATION1");
            json.put("targetStatus", "OFFERED");
        } catch (JSONException e) {
            e.printStackTrace();
        }


		OutputParam output = new OutputParam("SIGNAL", "START_MOVE_FORWARD", json.toString());
      
		SessionFacts sessionFacts = new SessionFacts(serviceToken, userToken, output);
		
      	GennyKieSession gks = null;
      
      	try {
      		gks = GennyKieSession
					.builder(serviceToken,true)				
					.addToken(userToken)
					.addJbpm("codeTest.bpmn")
					.addDrl("JASPER_TEST.drl")
					.build();
			
			gks.start();
			gks.startProcess("codeTest");
//          	gks.injectSignal("START_SET_STATUS", sessionFacts); 
          	
          	System.out.println("DEBUG");
          
      	} catch (Exception e) {
      		System.out.println("[*] Might not be able to find the workflow!");
      		e.printStackTrace();
      	} 	finally {
      		if (gks != null) {
      			gks.close();
      		}
      		System.out.println("Finishing...");
      	}
        
	}
	
	
	
	@Test
    public void OfferedLimitTest() {
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
        	VertxUtils.cachedEnabled = false;
            qRules = GennyJbpmBaseTest.setupLocalService();
            userToken = new GennyToken("userToken", qRules.getToken());
            serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
        }
        
        System.out.println("session     =" + userToken.getSessionCode());
        System.out.println("userToken   =" + userToken.getToken());
        System.out.println("serviceToken=" + serviceToken.getToken());
        
        /* Get an instance of BaseEntityUtils */
        BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
        
        /* Create all test BaseEntitys */
        beUtils.create("PER_TEST_INTERN1", "Test Intern 1");
        beUtils.create("PER_TEST_INTERN2", "Test Intern 2");
        beUtils.create("PER_TEST_INTERN3", "Test Intern 3");
        beUtils.create("PER_TEST_INTERN4", "Test Intern 4");
        beUtils.create("PER_TEST_INTERN5", "Test Intern 5");
        
        beUtils.create("CPY_TEST_HOSTCOMPANY", "Test Host Company");
        beUtils.create("PER_TEST_HOSTCOMPANY_REP", "Test Host Cpy Rep");
        
        beUtils.create("CPY_TEST_EDUPROVIDER", "Test Edu Provider");
        beUtils.create("PER_TEST_EDUPROVIDER_REP", "Test Edu Pro Rep");

        beUtils.create("BEG_TEST_INTERNSHIP", "Test Internship");
        
        beUtils.create("APP_TEST_APPLICATION1", "Test Application 1");
        beUtils.create("APP_TEST_APPLICATION2", "Test Application 2");
        beUtils.create("APP_TEST_APPLICATION3", "Test Application 3");
        beUtils.create("APP_TEST_APPLICATION4", "Test Application 4");
        beUtils.create("APP_TEST_APPLICATION5", "Test Application 5");
                
        System.out.println("Created BEs");
        
        
        /* Get the BaseEntitys just created */
        BaseEntity intern1 = beUtils.getBaseEntityByCode("PER_TEST_INTERN1");
        BaseEntity intern2 = beUtils.getBaseEntityByCode("PER_TEST_INTERN2");
        BaseEntity intern3 = beUtils.getBaseEntityByCode("PER_TEST_INTERN3");
        BaseEntity intern4 = beUtils.getBaseEntityByCode("PER_TEST_INTERN4");
        BaseEntity intern5 = beUtils.getBaseEntityByCode("PER_TEST_INTERN5");
        
        BaseEntity hostCompany = beUtils.getBaseEntityByCode("CPY_TEST_HOSTCOMPANY");
        BaseEntity hostCompanyRep = beUtils.getBaseEntityByCode("PER_TEST_HOSTCOMPANY_REP");
        
        BaseEntity eduProvider = beUtils.getBaseEntityByCode("CPY_TEST_EDUPROVIDER");
        BaseEntity eduProviderRep = beUtils.getBaseEntityByCode("PER_TEST_EDUPROVIDER_REP");
        
        BaseEntity internship = beUtils.getBaseEntityByCode("BEG_TEST_INTERNSHIP");
        
        BaseEntity application1 = beUtils.getBaseEntityByCode("APP_TEST_APPLICATION1");
        BaseEntity application2 = beUtils.getBaseEntityByCode("APP_TEST_APPLICATION2");
        BaseEntity application3 = beUtils.getBaseEntityByCode("APP_TEST_APPLICATION3");
        BaseEntity application4 = beUtils.getBaseEntityByCode("APP_TEST_APPLICATION4");
        BaseEntity application5 = beUtils.getBaseEntityByCode("APP_TEST_APPLICATION5");
        
        
        Answer answer;
        
        /* Create all necessary attributes for BaseEntitys */
        
        /* INTERNS */
        
        answer = new Answer(userToken.getUserCode(), intern1.getCode(), "PRI_FIRSTNAME", "Greg");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern1.getCode(), "PRI_LASTNAME", "Legg");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern1.getCode(), "PRI_EMAIL", "peg.leg.greg@gmail.com");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern1.getCode(), "PRI_MOBILE", "0478926738");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern1.getCode(), "PRI_IS_INTERN", "true");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern1.getCode(), "PRI_STATUS", "AVAILABLE");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern1.getCode(), "PRI_STATUS_COLOR", "5cb85c");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern1.getCode(), "PRI_DISABLED", "false");
        beUtils.saveAnswer(answer);
        
        answer = new Answer(userToken.getUserCode(), intern2.getCode(), "PRI_FIRSTNAME", "John");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern2.getCode(), "PRI_LASTNAME", "Smith");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern2.getCode(), "PRI_EMAIL", "jsmith@gmail.com");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern2.getCode(), "PRI_MOBILE", "0478937286");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern2.getCode(), "PRI_IS_INTERN", "true");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern2.getCode(), "PRI_STATUS", "AVAILABLE");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern2.getCode(), "PRI_STATUS_COLOR", "5cb85c");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern2.getCode(), "PRI_DISABLED", "false");
        beUtils.saveAnswer(answer);
        
        answer = new Answer(userToken.getUserCode(), intern3.getCode(), "PRI_FIRSTNAME", "Chris");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern3.getCode(), "PRI_LASTNAME", "Pyke");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern3.getCode(), "PRI_EMAIL", "cp@gmail.com");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern3.getCode(), "PRI_MOBILE", "0478027818");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern3.getCode(), "PRI_IS_INTERN", "true");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern3.getCode(), "PRI_STATUS", "AVAILABLE");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern3.getCode(), "PRI_STATUS_COLOR", "5cb85c");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern3.getCode(), "PRI_DISABLED", "false");
        beUtils.saveAnswer(answer);

        answer = new Answer(userToken.getUserCode(), intern4.getCode(), "PRI_FIRSTNAME", "Jasper");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern4.getCode(), "PRI_LASTNAME", "Robison");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern4.getCode(), "PRI_EMAIL", "jrob@gmail.com");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern4.getCode(), "PRI_MOBILE", "0473896738");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern4.getCode(), "PRI_IS_INTERN", "true");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern4.getCode(), "PRI_STATUS", "AVAILABLE");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern4.getCode(), "PRI_STATUS_COLOR", "5cb85c");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern4.getCode(), "PRI_DISABLED", "false");
        beUtils.saveAnswer(answer);
        
        answer = new Answer(userToken.getUserCode(), intern5.getCode(), "PRI_FIRSTNAME", "Super");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern5.getCode(), "PRI_LASTNAME", "Man");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern5.getCode(), "PRI_EMAIL", "superman@gmail.com");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern5.getCode(), "PRI_MOBILE", "0478029175");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern5.getCode(), "PRI_IS_INTERN", "true");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern5.getCode(), "PRI_STATUS", "AVAILABLE");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern5.getCode(), "PRI_STATUS_COLOR", "5cb85c");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), intern5.getCode(), "PRI_DISABLED", "false");
        beUtils.saveAnswer(answer);
       
       
        /* HOST CPY */
        
        answer = new Answer(userToken.getUserCode(), hostCompany.getCode(), "PRI_ABN", "11223493505");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), hostCompany.getCode(), "PRI_NAME", "Amazen");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), hostCompany.getCode(), "PRI_IS_HOST_CPY", "true");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), hostCompany.getCode(), "PRI_STATUS", "ACTIVE");
 		beUtils.saveAnswer(answer);
        
        
        /* HOST CPY REP */
        
        answer = new Answer(userToken.getUserCode(), hostCompanyRep.getCode(), "PRI_FIRSTNAME", "scott");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), hostCompanyRep.getCode(), "PRI_LASTNAME", "nofriends");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), hostCompanyRep.getCode(), "PRI_EMAIL", "somehostcpyrep@gmail.com");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), hostCompanyRep.getCode(), "PRI_IS_HOST_CPY_REP", "true");
        beUtils.saveAnswer(answer);
        
        beUtils.createLink(hostCompany.getCode(), hostCompanyRep.getCode(), "LNK_CPY", "STAFF", 1.0);
        
        
        /* EDU PRO */
        
        answer = new Answer(userToken.getUserCode(), eduProvider.getCode(), "PRI_ABN", "11223923505");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), eduProvider.getCode(), "PRI_NAME", "uranus university");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), eduProvider.getCode(), "PRI_IS_EDU_PROVIDER", "true");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), eduProvider.getCode(), "PRI_STATUS", "ACTIVE");
        beUtils.saveAnswer(answer);
        
        beUtils.createLink(eduProvider.getCode(), intern1.getCode(), "LNK_CPY", "STUDENT", 1.0);
        beUtils.createLink(eduProvider.getCode(), intern2.getCode(), "LNK_CPY", "STUDENT", 1.0);
        beUtils.createLink(eduProvider.getCode(), intern3.getCode(), "LNK_CPY", "STUDENT", 1.0);
        beUtils.createLink(eduProvider.getCode(), intern4.getCode(), "LNK_CPY", "STUDENT", 1.0);
      	beUtils.createLink(eduProvider.getCode(), intern5.getCode(), "LNK_CPY", "STUDENT", 1.0);

        
        
        /* EDU PRO REP */
        
        answer = new Answer(userToken.getUserCode(), eduProviderRep.getCode(), "PRI_FIRSTNAME", "john");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), eduProviderRep.getCode(), "PRI_LASTNAME", "doe");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), eduProviderRep.getCode(), "PRI_EMAIL", "johndoe@gmail.com");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), eduProviderRep.getCode(), "PRI_IS_EDU_PRO_REP", "true");
        beUtils.saveAnswer(answer);

        
        beUtils.createLink(eduProvider.getCode(), eduProviderRep.getCode(), "LNK_CPY", "STAFF", 1.0);
        
        /* INTERNSHIP */
        
        answer = new Answer(userToken.getUserCode(), internship.getCode(), "PRI_NAME", "King of Britain");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), internship.getCode(), "LNK_NO_OF_INTERNS", "[\"SEL_NO_OF_INTERNS_THREE\"]");
        beUtils.saveAnswer(answer);
        answer = new Answer(userToken.getUserCode(), internship.getCode(), "PRI_IS_INTERNSHIP", "true");
 		beUtils.saveAnswer(answer);
 		answer = new Answer(userToken.getUserCode(), internship.getCode(), "PRI_STATUS", "ACTIVE");
 		beUtils.saveAnswer(answer);
 		answer = new Answer(userToken.getUserCode(), internship.getCode(), "PRI_IS_FULL", "false");
 		beUtils.saveAnswer(answer);
 		answer = new Answer(userToken.getUserCode(), internship.getCode(), "PRI_CURRENT_NO_OF_INTERNS", "0");
 		beUtils.saveAnswer(answer);
    
        beUtils.createLink(hostCompany.getCode(), internship.getCode(), "LNK_CPY", "INTERNSHIP", 1.0);
        
                
        /* APPLICATION */
        
        
        Map<String, String> codes = new HashMap<String, String>();
        
        codes.put(intern1.getCode(), application1.getCode());
        codes.put(intern2.getCode(), application2.getCode());
        codes.put(intern3.getCode(), application3.getCode());
        codes.put(intern4.getCode(), application4.getCode());
        codes.put(intern5.getCode(), application5.getCode());
        

        for (String key : codes.keySet()) {
        	System.out.println("Key: " + key);
        	answer = new Answer(userToken.getUserCode(), codes.get(key), "PRI_STATUS","APPLIED");
     		beUtils.saveAnswer(answer);
     		answer = new Answer(userToken.getUserCode(), codes.get(key), "PRI_DISABLED","false");
     		beUtils.saveAnswer(answer);
     		
     		String firstName = beUtils.getBaseEntityValue(key, "PRI_FIRSTNAME").toString();
     		String lastName = beUtils.getBaseEntityValue(key, "PRI_LASTNAME").toString();
     		String name = firstName + " " + lastName;
     		answer = new Answer(userToken.getUserCode(), codes.get(key),"PRI_INTERN_NAME", name);
     		beUtils.saveAnswer(answer);
     		
     		String email = beUtils.getBaseEntityValue(key, "PRI_EMAIL").toString();
     		answer = new Answer(userToken.getUserCode(), codes.get(key),"PRI_INTERN_EMAIL", email);
     		beUtils.saveAnswer(answer);
     		
     		String mobile = beUtils.getBaseEntityValue(key, "PRI_MOBILE").toString();
     		answer = new Answer(userToken.getUserCode(), codes.get(key),"PRI_INTERN_MOBILE", email);
     		beUtils.saveAnswer(answer);
     		
     		answer = new Answer(userToken.getUserCode(), codes.get(key), "PRI_CODE", codes.get(key));
     		beUtils.saveAnswer(answer);
     		
     		String internshipName = beUtils.getBaseEntityValue(internship.getCode(), "PRI_NAME").toString();
     		answer = new Answer(userToken.getUserCode(), codes.get(key), "PRI_ASSOC_INTERNSHIP", internshipName);
     		beUtils.saveAnswer(answer);
     		
     		String companyName = beUtils.getBaseEntityValue(hostCompany.getCode(), "PRI_NAME").toString();
     		answer = new Answer(userToken.getUserCode(), codes.get(key),"PRI_ASSOC_HC", companyName);
     		beUtils.saveAnswer(answer);
     		
        }
 		
 		
 		answer = new Answer(userToken.getUserCode(), "APP_61C57976-B45E-450A-B66F-443CE1689251", "PRI_DISABLED","true");
 		beUtils.saveAnswer(answer);
 		
 	
 		
 		beUtils.createLink(internship.getCode(), application1.getCode(), "LNK_BEG", "APPLICATION", 1.0);
 		beUtils.createLink(intern1.getCode(), application1.getCode(), "LNK_PER", "APPLICATION", 1.0);

        beUtils.createLink(internship.getCode(), application2.getCode(), "LNK_BEG", "APPLICATION", 1.0);
        beUtils.createLink(intern2.getCode(), application2.getCode(), "LNK_PER", "APPLICATION", 1.0);
        
        beUtils.createLink(internship.getCode(), application3.getCode(), "LNK_BEG", "APPLICATION", 1.0);
        beUtils.createLink(intern3.getCode(), application3.getCode(), "LNK_PER", "APPLICATION", 1.0);
        
        beUtils.createLink(internship.getCode(), application4.getCode(), "LNK_BEG", "APPLICATION", 1.0);
        beUtils.createLink(intern4.getCode(), application4.getCode(), "LNK_PER", "APPLICATION", 1.0);
 		
        beUtils.createLink(internship.getCode(), application5.getCode(), "LNK_BEG", "APPLICATION", 1.0);
        beUtils.createLink(intern5.getCode(), application5.getCode(), "LNK_PER", "APPLICATION", 1.0);
        
 		
    
 		BaseEntity internshipBe = beUtils.getParent(application1.getCode(), "LNK_BEG");
        System.out.println("internshipBe: " + internshipBe);
        
 		List<BaseEntity> childApplications = beUtils.getLinkedBaseEntities("BEG_TEST_INTERNSHIP", "LNK_BEG");
		System.out.println("App List: " + childApplications);
        
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