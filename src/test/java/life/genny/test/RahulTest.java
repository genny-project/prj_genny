package life.genny.test;

import java.util.HashMap;
import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.junit.Test;
import org.kie.api.runtime.process.ProcessInstance;


import life.genny.models.GennyToken;
import life.genny.qwanda.Answer;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QDataAnswerMessage;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwandautils.JsonUtils;
import life.genny.rules.QRules;
import life.genny.utils.VertxUtils;

public class RahulTest extends GennyJbpmBaseTest {

	//private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());
	private static final String WFP_USER_SESSION = "userSession.bpmn";
	private static final String WFP_USER_LIFECYCLE = "userLifecycle.bpmn";
	private static final String WFP_TEST_WORKFLOW = "testWorkflow.bpmn";
	private static final String WFP_APPLICATION_LIFECYCLE = "applicationLifecycle.bpmn";
	private static final String WFP_APPLICATION_CREATED = "applicationCreatedWorkflow.bpmn";
	private static final String WFP_APPLICATION_APPLIED = "applied.bpmn";
	private static final String WFP_AUTH_INIT = "auth_init.bpmn";
	private static final String WFP_USER_VALIDATION = "userValidation.bpmn";
	
	public RahulTest() {
		super(false);
	}
	
	//@Test(timeout = 30000)
	public void userLifecycleWorkflow() {
		
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules qRules = getQRules(userToken); // defaults to user anyway
		GennyToken serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());


		GennyKieSession gks = GennyKieSession.builder(serviceToken)
				.addJbpm(WFP_USER_LIFECYCLE)
				.addFact("user", userToken)
				.addToken(new GennyToken("serviceUser", qRules.getServiceToken()))
				.addToken(userToken)
				.build();

	    gks.start();
	   		
		gks.getKieSession().signalEvent("userInSession", userToken); 
		
	    gks.advanceSeconds(5,true);
	    
		gks.getKieSession().signalEvent("userIsActive", userToken);

	    gks.advanceSeconds(5,true);
	    
		gks.getKieSession().signalEvent("deleteuser", userToken); 
		
		gks.close();

	}
	
	//@Test(timeout = 30000)
	public void userSessionWorkflow() {

		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules qRules = getQRules(userToken); // defaults to user anyway

		GennyKieSession gks = GennyKieSession.builder(serviceToken)
				.addJbpm(WFP_USER_SESSION)
				.addFact("user",userToken)
				.addToken(new GennyToken("serviceUser", qRules.getServiceToken()))
				.addToken(userToken)
				.build();     
		
		System.out.println("GOOD STUFF !!! user lifecycle about to start"); 

	    gks.start();

	    gks.advanceSeconds(5,true);
	    
		//System.out.println("GOOD STUFF !!! User about to log in"); 

		gks.getKieSession().signalEvent("newSession", userToken);

		
		System.out.println("GOOD STUFF !!! User logged in... wait 10 seconds"); 

		gks.advanceSeconds(5,true); 
	
//		gks.getKieSession().signalEvent("abortAllSessions", userToken);
		
		System.out.println("GOOD STUFF !!! User about to log out"); 
		
		gks.advanceSeconds(2,true); 	

	    gks.close();

		}
	
	@Test
	public void quickTest() {
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
	    
	    hashBeg.put("intern", intern);
	    hashBeg.put("internship", internship);
	    hashBeg.put("hostCompany", hostCompany);

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage authInitMsg = new QEventMessage("EVT_MSG", "AUTH_INIT");
		QEventMessage msg1 = new QEventMessage("EVT_MSG", "INIT_1");
		QEventMessage msgLogout = new QEventMessage("EVT_MSG", "LOGOUT");

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
			gks = GennyKieSession.builder(serviceToken, true)
//					.addDrl("ADD_COMPANY_ATTRIBUTES.drl")
					.addDrl("ADD_PERSON_ATTRIBUTES.drl")
//					.addJbpm("companyLifecycle.bpmn")
//					.addJbpm("applicationLifecycle.bpmn")
					.addJbpm("baseEntityValidation.bpmn")
					.addJbpm("personLifecycle.bpmn")
					.addToken(userToken)
					.build();
			gks.start();
			
			gks.advanceSeconds(5, true);
		
			gks.injectSignal("newPerson", hashBeg);

			gks.advanceSeconds(5, true);
			
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			gks.close();
		}
		
	}
	
	//@Test
	public void temptest() {
		
		userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
		serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
		
		System.out.println("userToken ::" + userToken);
		System.out.println("serviceToken ::" + serviceToken);
		
		GennyKieSession gks = GennyKieSession.builder(serviceToken, true)
				.addJbpm("applicationLifecycle.bpmn")
				.addToken(userToken)
				.build();
		
		BaseEntity intern = new BaseEntity("PRI_INTERN");
		BaseEntity internship = new BaseEntity("PRI_INTERNSHIP");

	    HashMap<String, BaseEntity> hashBeg = new HashMap<String, BaseEntity>();
	    
	    hashBeg.put("intern", intern);
	    hashBeg.put("internship", internship);
	    
		gks.start();
		
		gks.injectSignal("newApplication", hashBeg);

		gks.close();
		
	}
		
		//@Test
		public void userSessionTestToRunnningService() {
		
		//VertxUtils.cachedEnabled = true; // don't try and use any local services
		
		QRules rules =GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
		
		QDataAnswerMessage sas = new QDataAnswerMessage(new Answer("asd","asd","asdasd","adasdasd"));
		sas.setToken(userToken.getToken());
		VertxUtils.writeMsg("data", JsonUtils.toJson(sas));
	}
}