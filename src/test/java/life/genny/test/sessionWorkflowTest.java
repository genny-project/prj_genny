package life.genny.test;

import java.lang.invoke.MethodHandles;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import life.genny.models.GennyToken;
import life.genny.qwanda.message.QEventMessage;
import life.genny.rules.QRules;
import life.genny.utils.GennyJbpmBaseTest;
import life.genny.utils.GennyKieSession;

public class sessionWorkflowTest extends GennyJbpmBaseTest {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());
	private static final String WFP_CREATE_USER = "userSession.bpmn";

	public sessionWorkflowTest() {
		super(false);
	}

	@Test(timeout = 30000)
	public void createUserWorkflow() {

		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules qRules = getQRules(userToken); // defaults to user anyway

		GennyKieSession gks = GennyKieSession.builder(userToken)
				.addJbpm("userSession.bpmn")
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
}