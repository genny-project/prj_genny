package life.genny.test;

import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.reflect.TypeToken;

import life.genny.eventbus.EventBusInterface;
import life.genny.eventbus.EventBusMock;
import life.genny.eventbus.VertxCache;
import life.genny.models.Frame3;
import life.genny.models.FramePosition;
import life.genny.models.GennyToken;
import life.genny.models.Theme;
import life.genny.models.ThemeAttributeType;
import life.genny.qwanda.VisualControlType;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwandautils.GennyCacheInterface;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.rules.QRules;
import life.genny.utils.FrameUtils2;
import life.genny.utils.VertxUtils;

public class SafalTest extends GennyJbpmBaseTest {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	protected static String realm = GennySettings.mainrealm;
	protected static Set<String> realms;

	protected static EventBusInterface eventBusMock;
	protected static GennyCacheInterface vertxCache;
	
	private static final String DRL_SEND_USER_DATA_DIR = "SendUserData";

	private static final String WFE_SEND_FORMS = "send_forms.bpmn";
	private static final String WFE_SHOW_FORM = "show_form.bpmn";
	private static final String WFE_AUTH_INIT = "auth_init.bpmn";
	private static final String WFE_SEND_LLAMA = "send_llama.bpmn";

	public SafalTest() {
		super(false);
	}

	//@Test
	public void quickTest() {

		GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
		GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
		QRules qRules = new QRules(eventBusMock, userToken.getToken(), userToken.getAdecodedTokenMap());
		qRules.set("realm", userToken.getRealm());
		qRules.setServiceToken(serviceToken.getToken());

		System.out.println("session=" + userToken.getSessionCode());
		System.out.println("userToken=" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage msg = new QEventMessage("EVT_MSG", "AUTH_INIT");

		GennyKieSession gks = null;
		try {
			gks = GennyKieSession.builder(serviceToken,true).addJbpm("adam_test_1.bpmn").addFact("qRules", qRules).addFact("msg", msg)
					.addToken(serviceToken).addToken(userToken).build();

			gks.start();

			gks.advanceSeconds(10, false);
			gks.broadcastSignal("inputSignal", "Hello");
			// gks.getKieSession().getQueryResults(query, arguments)
			gks.advanceSeconds(1, false);
		} finally {
			gks.close();
		}

	}
	
	
	//@Test
		public void timerIntervalTest() {
			VertxUtils.cachedEnabled = true; // don't try and use any local services
			GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "userToken");
			GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "serviceToken");
			QRules qRules = new QRules(eventBusMock, userToken.getToken(), userToken.getAdecodedTokenMap());
			
			
			GennyKieSession gks = GennyKieSession.builder(serviceToken)
					.addJbpm( "example_timer_start.bpmn")
					.build();
			
			//.addJbpm( WFE_TIMER_EXAMPLE_1)
		     gks.startProcess("TimerTest");
		     
		    /* for (int i = 0; i<20; i++) {
			    	System.out.println("Clock :::: " + (i+1) + "sec");
			    	sleepMS(1000);
			    	
			    	gks.advanceSeconds(1,false);
			    }*/
		    
		    gks.close();
		}
		
		@Test(timeout = 300000)	
		public void testTimerProcess() {
		

			VertxUtils.cachedEnabled = true; // don't try and use any local services
			GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "userToken");
			GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "serviceToken");
			QRules qRules = new QRules(eventBusMock, userToken.getToken(), userToken.getAdecodedTokenMap());
			
			GennyKieSession gks = GennyKieSession.builder(serviceToken)
					.addJbpm("example_timer_start.bpmn")
					.addJbpm("timer_example_workflow_1.bpmn","timer_example_workflow_2.bpmn","timer_example_workflow_3.bpmn")
					.addJbpm("timer_example_workflow_4.bpmn")
					.addFact("qRules",qRules)
					.addFact("eb", eventBusMock)
					.addToken(new GennyToken("serviceUser", qRules.getServiceToken()))
					.addToken(userToken)
					.build();
			
		     //gks.startProcess("com.sample.bpmn.exampleMsgStart");
		      gks.start();  
			        	
			gks.advanceSeconds(4,true);
			    	    			
			//gks.injectFact(event);
			gks.getKieSession().signalEvent("incomingSignal", "testobject");	
		    gks.close();
		}


//	@Test(timeout = 300000)	
	public void sessionWorkFLow() {
		
		//Creating two Qevent message for a simulation of two people login
		
				QEventMessage msg = new QEventMessage("EVT_MSG", "LOGIN");
				msg.data.setValue("safal");
				
				QEventMessage msg1 = new QEventMessage("EVT_MSG", "LOGIN");
				msg1.data.setValue("anish");

				GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
				GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
				QRules qRules = getQRules(userToken); // defaults to user anyway
				String keycloackState = userToken.getCode();
				
				GennyKieSession gks = GennyKieSession.builder(serviceToken)
						.addJbpm("example_start.bpmn")
						.addFact("qRules",qRules)
						.addFact("eb", eventBusMock)
						.addToken(new GennyToken("serviceUser", qRules.getServiceToken()))
						.addToken(userToken)
						.build();
			    
				gks.start();
				
				gks.advanceSeconds(2, true);
				
				gks.injectSignal("login",msg);
				
				gks.advanceSeconds(2, true);
				
				gks.injectSignal("login",msg1);
			
				gks.advanceSeconds(2, true);
				
				gks.injectSignal("safal", "null");

				gks.advanceSeconds(2, true);
				
				gks.injectSignal("anish", "null");

			    gks.close();

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

	}

}