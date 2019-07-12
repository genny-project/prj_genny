package life.genny.test;

import java.lang.invoke.MethodHandles;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import life.genny.models.Frame3;
import life.genny.models.FramePosition;
import life.genny.models.GennyToken;
import life.genny.qwanda.message.QEventMessage;
import life.genny.rules.QRules;

public class RahulTest extends GennyJbpmBaseTest {

	//private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());
	private static final String WFP_USER_SESSION = "userSession.bpmn";
	private static final String WFP_USER_LIFECYCLE = "userLifecycle.bpmn";
	private static final String WFP_TEST_WORKFLOW = "testWorkflow.bpmn";
	private static final String WFP_APPLICATION_LIFECYCLE = "applicationLifecycle.bpmn";
	private static final String WFP_APPLICATION_AVAILABLE = "available.bpmn";
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

		GennyKieSession gks = GennyKieSession.builder(serviceToken)
				.addJbpm(WFP_USER_LIFECYCLE)
				.addFact("user",userToken)
				.addToken(new GennyToken("serviceUser", qRules.getServiceToken()))
				.addToken(userToken)
				.build();

	    gks.start();
	    
		System.out.println("GOOD STUFF !!! user lifecycle about to start"); 

		gks.getKieSession().signalEvent("newUserSignal", userToken);
		
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

		gks.getKieSession().signalEvent("creatingUserSignal", userToken);

		
		System.out.println("GOOD STUFF !!! User logged in... wait 10 seconds"); 

		gks.advanceSeconds(5,true); 
	
//		gks.getKieSession().signalEvent("abortAllSessions", userToken);
		
		System.out.println("GOOD STUFF !!! User about to log out"); 
		

		gks.getKieSession().signalEvent("userLogout", userToken);
		gks.getKieSession().signalEvent("abortAllSessions", userToken);
		
		
		
		gks.advanceSeconds(2,true); 	
		


	    gks.close();

		}
	
	@Test
	public void quickTest() {
		
		System.out.println("Show UserSession");
		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
       
		System.out.println("session=" + userToken.getSessionCode());
		System.out.println("userToken=" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage msg = new QEventMessage("EVT_MSG", "AUTH_INIT");

		GennyKieSession gks = null;
		try {
			gks = GennyKieSession.builder(serviceToken)
					.addJbpm(WFP_USER_SESSION)
					.addJbpm(WFP_USER_VALIDATION)
					.addJbpm(WFP_USER_LIFECYCLE)
					.addToken(userToken)
					.build();
			
			gks.start();
			
			gks.injectMessage(msg);
			
			System.out.println("START: TESTING HERE !!!"); 

			gks.getKieSession().signalEvent("creatingUserSignal", userToken);

		} catch (Exception e)
		{
			System.out.println(e.getLocalizedMessage());
		} finally {
			gks.close();
		}
		
		
	}	
}