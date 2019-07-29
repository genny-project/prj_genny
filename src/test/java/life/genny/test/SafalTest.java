package life.genny.test;

import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;
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
import life.genny.qwanda.Answer;
import life.genny.qwanda.VisualControlType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataAnswerMessage;
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
	
	@Test
	public void eventProcessTest() {
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		
		QRules rules = getQRules(userToken);
		
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
		
		QEventMessage msg = new QEventMessage("EVT_MSG", "AUTH_INIT");
		msg.getData().setValue("NEW_SESSION");
		System.out.println("Hello");
		GennyKieSession gks = GennyKieSession.builder(serviceToken,true)
				.addJbpm("userSession.bpmn")
				.addJbpm("userValidation.bpmn")
				.addJbpm("userLifecycle.bpmn")
				.addJbpm("auth_init.bpmn")
				.addDrl("DEFAULT_EVENT.drl")
				.addToken(serviceToken)
				.addToken(userToken)
				.build();
		System.out.println("Hello");	
		
		gks.start();
		gks.injectSignal("newSession",msg);
		gks.advanceSeconds(5, true);
		
		gks.advanceSeconds(5, true);
	     
	}
	
	//@Test
	public void timeCHeck() {
		
		
		
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		System.out.println(userToken.getExpiryDateTime());
		
		LocalDateTime expTime = userToken.getExpiryDateTime();

		ZonedDateTime ldtZoned = expTime.atZone(ZoneId.systemDefault());
		ZonedDateTime utcZoned = ldtZoned.withZoneSameInstant(ZoneId.of("UTC"));
		System.out.println(utcZoned.toOffsetDateTime());
		
		LocalDateTime expTime1 = LocalDateTime.now();

		ZonedDateTime ldtZoned1 = expTime1.atZone(ZoneId.systemDefault());
		ZonedDateTime utcZoned1 = ldtZoned1.withZoneSameInstant(ZoneId.of("UTC"));
		System.out.println(utcZoned1.toOffsetDateTime());
		
	}
	
	
	//@Test
	public void userSessionTestToRunnningService() {
		
		//VertxUtils.cachedEnabled = true; // don't try and use any local services
		
		QRules rules =GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
		
		Answer ans = new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_NAME", "Safal Shrestha");
		QDataAnswerMessage ansMsg = new QDataAnswerMessage(ans);
		ansMsg.setToken(userToken.getToken());
		
		VertxUtils.writeMsg("data", JsonUtils.toJson(ansMsg));
		
	}
	
	//@Test
	public void SendData() {
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken);
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
		

		Answer ans = new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_NAME", "Safal Shrestha");
		QDataAnswerMessage ansMsg = new QDataAnswerMessage(ans);
		ansMsg.setToken(userToken.getToken());

		QEventMessage msg = new QEventMessage("EVT_MSG", "AUTH_INIT");

		GennyKieSession gks = GennyKieSession.builder(serviceToken)
				.addJbpm("userSession.bpmn")
				.addJbpm("userValidation.bpmn")
				.addJbpm("userLifecycle.bpmn")
				.addJbpm("bucketPage.bpmn")
				.addJbpm("showDashboard.bpmn")
				.addJbpm("auth_init.bpmn")
				.addJbpm("detailpage.bpmn")
				.addDrl("Answer.drl")
				
				.addFact("rules", rules)
				.addToken(userToken) 
				.addToken(serviceToken)
				.build();
				
		
		gks.start();
		gks.injectSignal("newSession",msg);
		gks.advanceSeconds(5, true);
		
		gks.injectSignal("data", ansMsg);
		gks.advanceSeconds(5, true);
	     
		
		
	}
	
	//@Test
	public void simpleTest() {
		QRules rules = GennyJbpmBaseTest.setupLocalService();
		rules.sendAllAttributes();
		
	}

//	@Test
	public void linkTest() {

		VertxUtils.cachedEnabled = true; // don't try and use any local services
		GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "userToken");
		GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "serviceToken");
		QRules qRules = new QRules(eventBusMock, userToken.getToken());
		
		GennyKieSession gks = GennyKieSession.builder(serviceToken)
				.addJbpm( "link1.bpmn")
				.build();
	
	     gks.startProcess("link");
	     
	     
		
	}
	
	//@Test
	public void quickTest() {

		GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
		GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
		QRules qRules = new QRules(eventBusMock, userToken.getToken());
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
	public void AdHocTest() {
		
		VertxUtils.cachedEnabled = true; // don't try and use any local services
		GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "userToken");
		GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "serviceToken");
		QRules qRules = new QRules(eventBusMock, userToken.getToken());
		
		
		GennyKieSession gks = GennyKieSession.builder(serviceToken)
				.addJbpm( "adhoc.bpmn")
				.build();
	
	     gks.startProcess("adhoc");
	     
	     for (int i = 0; i<20; i++) {
		    	System.out.println("Clock :::: " + (i+1) + "sec");
		    
		    	gks.advanceSeconds(1,true);
		    	if(i == 3) {
		    		gks.getProcessInstance().signalEvent("NAME", null);
		    	}else if(i==5) {
		    		gks.getProcessInstance().signalEvent("AGE", null);
		    	}else if(i ==7 ) {
		    		gks.getProcessInstance().signalEvent("LOGOUT", null);
		    	}else if(i==9) {
		    		gks.getProcessInstance().signalEvent("NAME", null);
		    	}
		    }
	    
	    gks.close();
	}
	
	//@Test
	public void v7Test() {
	
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken);
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
		rules.sendAllAttributes();	
		
		/* Themes and frames*/
		
		Theme THM_BLACK_PARTITON = Theme.builder("THM_BLACK_PARTITON")
									.addAttribute().backgroundColor("black").end().build();
		
		Theme THM_RED_PARTITON = Theme.builder("THM_RED_PARTITON")
								.addAttribute().backgroundColor("red").end().build();
		
		Theme THM_YELLOW_PARTITON = Theme.builder("THM_YELLOW_PARTITON")
									.addAttribute().backgroundColor("yellow").end().build();
		
		Frame3 FRM_BLACK = Frame3.builder("FRM_BLACK")
							.addTheme(THM_BLACK_PARTITON)
							.end()
							.question("QUE_NAME_TWO").end()
							.build();
		
		Frame3 FRM_RED = Frame3.builder("FRM_RED")
				.addTheme(THM_RED_PARTITON)
				.end()
				.question("QUE_NAME_TWO").end()
				.build();
		
		Frame3 FRM_YELLOW = Frame3.builder("FRM_YELLOW")
				.addTheme(THM_YELLOW_PARTITON)
				.end()
				.question("QUE_NAME_TWO").end()
				.build();
		
		Frame3 FRM_CENTER = Frame3.builder("FRM_CENTER")
				.addFrame(FRM_BLACK, FramePosition.NORTH).end()
				.addFrame(FRM_RED,FramePosition.NORTH).end()
				.addFrame(FRM_YELLOW,FramePosition.NORTH).end().build();

		
		Frame3 frameMain = Frame3.builder("FRM_MAIN")
				.addFrame(FRM_CENTER, FramePosition.CENTRE)
			.end()
			.build();

		Frame3 frameRoot = Frame3.builder("FRM_ROOT").addFrame(frameMain).end().build();

		/* end */
		Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();
	
		QDataBaseEntityMessage msg = FrameUtils2.toMessage(frameRoot, serviceToken, askMsgs);
	
		/* send message */
		rules.publishCmd(msg); // Send QDataBaseEntityMessage
	
		System.out.println("Sending Asks");
		for (QDataAskMessage askMsg : askMsgs) {
			rules.publishCmd(askMsg, serviceToken.getUserCode(), userToken.getUserCode()); // Send associated
			// QDataAskMessage
		}
	
		System.out.println("Sent");
		
	}
	
//	@Test
	public void userPool() {
		System.out.println("Show UserSession");
		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		GennyKieSession gks = null;
		
		try {
			gks = GennyKieSession.builder(serviceToken, false)
					.addJbpm("sas.bpmn")
//					.addFact("rules", rules)
					.addToken(userToken)
					.build();
				
				gks.startProcess("laneandpool");
			
			
				gks.advanceSeconds(3, true);
	
			System.out.println("Sent");

		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			gks.close();
		}
	}
	
	//@Test
	public void userSessionTest2() {
		System.out.println("Show UserSession");
		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage msg = new QEventMessage("EVT_MSG", "AUTH_INIT");
		QEventMessage msg1 = new QEventMessage("EVT_MSG", "SOMEEVENT");
		

		GennyKieSession gks = null;
		
		try {
			gks = GennyKieSession.builder(serviceToken)
					.addJbpm("userSession.bpmn")
					.addJbpm("userValidation.bpmn")
					.addJbpm("userLifecycle.bpmn")
					.addJbpm("bucketPage.bpmn")
					.addJbpm("showDashboard.bpmn")
					.addJbpm("auth_init.bpmn")
					.addJbpm("detailpage.bpmn")

					
					.addFact("rules", rules)
					.addToken(userToken) 
					.addToken(serviceToken)
					.build();
					gks.start();
			
			
				gks.advanceSeconds(3, true);
				//gks.getKieSession().getProcessInstance(processInstanceId)
				
				
				gks.injectSignal("newSession",msg);
				gks.advanceSeconds(5, true);
				
				gks.injectSignal("userMessage", msg1);
				gks.advanceSeconds(5, true);
				gks.injectSignal("userMessage", msg);
				
//			for (int i=0;i<2;i++) {
//				gks.displayForm("FRM_DASHBOARD",userToken);
//				gks.advanceSeconds(2, true);
//				gks.displayForm("FRM_DASHBOARD2",userToken);
//				gks.advanceSeconds(2, true);
//			}
			//gks.sendLogout(userToken);
			System.out.println("Sent");

		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			gks.close();
		}
	}
	
	//@Test
		public void timerIntervalTest() {
			VertxUtils.cachedEnabled = true; // don't try and use any local services
			GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "userToken");
			GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "serviceToken");
			QRules qRules = new QRules(eventBusMock, userToken.getToken());
			
			
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
		
		//@Test(timeout = 300000)	
		public void testTimerProcess() {
		

			VertxUtils.cachedEnabled = true; // don't try and use any local services
			GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "userToken");
			GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "serviceToken");
			QRules qRules = new QRules(eventBusMock, userToken.getToken());
			
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

		// Set up realm\
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

};