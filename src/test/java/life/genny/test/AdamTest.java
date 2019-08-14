package life.genny.test;

import org.kie.api.runtime.process.ProcessContext;
import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.reflect.TypeToken;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vertx.core.json.JsonObject;
import life.genny.eventbus.EventBusInterface;
import life.genny.eventbus.EventBusMock;
import life.genny.eventbus.VertxCache;
import life.genny.models.Frame3;
import life.genny.models.FramePosition;
import life.genny.models.GennyToken;
import life.genny.models.Theme;
import life.genny.models.ThemeAttributeType;
import life.genny.models.ThemeDouble;
import life.genny.qwanda.Answer;
import life.genny.qwanda.Context;
import life.genny.qwanda.ContextType;
import life.genny.qwanda.VisualControlType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QDataAnswerMessage;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwandautils.GennyCacheInterface;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.rules.QRules;
import life.genny.utils.BaseEntityUtils;
import life.genny.utils.FrameUtils2;
import life.genny.utils.VertxUtils;

public class AdamTest {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	protected static String realm = GennySettings.mainrealm;
	protected static Set<String> realms;

	protected static EventBusInterface eventBusMock;
	protected static GennyCacheInterface vertxCache;

	private static final String DRL_SEND_USER_DATA_DIR = "SendUserData";

	public AdamTest() {

	}

	
	@Test
	public void answerRulesTest()
	{
		System.out.println("Test Answer Rules");
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

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage initMsg = new QEventMessage("EVT_MSG", "INIT_STARTUP");

		QEventMessage authInitMsg = new QEventMessage("EVT_MSG", "AUTH_INIT");
		QEventMessage msg1 = new QEventMessage("EVT_MSG", "INIT_1");
		QEventMessage msgLogout = new QEventMessage("EVT_MSG", "LOGOUT");

		List<Answer> answers = new ArrayList<Answer>();
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_FIRSTNAME", "Bruce"));
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_LASTNAME", "Wayne"));
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_ADDRESS_JSON", 
				"{\"street_number\":\"64\",\"street_name\":\"Fakenham Road\",\"suburb\":\"Ashburton\",\"state\":\"Victoria\",\"country\":\"AU\",\"postal_code\":\"3147\",\"full_address\":\"64 Fakenham Rd, Ashburton VIC 3147, Australia\",\"street_address\":\"64 Fakenham Road\"}"));
		
		
		QDataAnswerMessage answerMsg = new QDataAnswerMessage(answers.toArray(new Answer[0]));
		
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
			gks = GennyKieSession.builder(serviceToken,true)
					.addDrl("DataProcessing")
					.addDrl("EventProcessing")
					.addDrl("InitialiseProject")
					.addJbpm("InitialiseProject")
					.addJbpm("userValidation")
					.addJbpm("Lifecycles")
					.addDrl("AuthInit")
					.addJbpm("AuthInit")

					.addToken(userToken)
					.build();
			gks.start();
			gks.injectEvent(initMsg); // This should create a new process

			gks.injectEvent(authInitMsg); // This should create a new process
			gks.advanceSeconds(5, false);

			gks.injectEvent(answerMsg); // This should create a new process
			gks.advanceSeconds(5, false);
			gks.injectEvent(msgLogout);

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
	public void processAddress()
	{
		
		Answer address = new Answer("PER_USER1", "PER_USER1", "PRI_ADDRESS_JSON", 
				"{\"street_number\":\"64\",\"street_name\":\"Fakenham Road\",\"suburb\":\"Ashburton\",\"state\":\"Victoria\",\"country\":\"AU\",\"postal_code\":\"3147\",\"full_address\":\"64 Fakenham Rd, Ashburton VIC 3147, Australia\",\"street_address\":\"64 Fakenham Road\"}");
		
		JsonObject addressDataJson = new JsonObject(address.getValue());

			System.out.println("The Address Json is  :: " + addressDataJson);

			List<Answer> answers = new ArrayList<Answer>();
			answers.add(new Answer("PER_USER1", "PER_USER1", "PRI_ADDRESS_COUNTRY", addressDataJson.getString("country")));
			answers.add(new Answer("PER_USER1", "PER_USER1", "PRI_ADDRESS_POSTCODE", addressDataJson.getString("postal_code")));
			answers.add(new Answer("PER_USER1", "PER_USER1", "PRI_ADDRESS_SUBURB", addressDataJson.getString("suburb")));
			answers.add(new Answer("PER_USER1", "PER_USER1", "PRI_ADDRESS_STATE", addressDataJson.getString("state")));
			answers.add(new Answer("PER_USER1", "PER_USER1", "PRI_ADDRESS_ADDRESS1", addressDataJson.getString("street_address")));
			answers.add(new Answer("PER_USER1", "PER_USER1", "PRI_ADDRESS_FULL", addressDataJson.getString("full_address")));
	
			
				
	}
	
	//@Test
	public void virtualQuestionTest() {
		System.out.println("Send Virtual Question");
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
			qRules = GennyJbpmBaseTest.setupLocalService();
			userToken = new GennyToken("userToken", qRules.getToken());
			serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
		}

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());


		// NOW SET UP Some baseentitys
		BaseEntity user = VertxUtils.readFromDDT(serviceToken.getRealm(),userToken.getUserCode(), true,
				serviceToken.getToken());

		BaseEntity project = VertxUtils.readFromDDT(serviceToken.getRealm(),"PRJ_"+realm.toUpperCase(), true,
				serviceToken.getToken());

		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
		BaseEntity sortIconBe = beUtils.getBaseEntityByCode("ICN_SORT");
        
		Context context = new Context(ContextType.ICON, sortIconBe, VisualControlType.VCL_ICON, 1.0);

		
		Frame3 FRM_POWERED_BY = null;
		
		try {
			FRM_POWERED_BY = Frame3.builder("FRM_POWERED_BY")
					.addTheme("THM_WIDTH_200",serviceToken).end()
					.addTheme("THM_COLOR_WHITE",serviceToken).end()   
					.question("QUE_POWERED_BY_GRP")
						.sourceAlias("PRJ_"+realm.toUpperCase())
						.targetAlias("PRJ_"+realm.toUpperCase())
						.addTheme("THM_FORM_LABEL_DEFAULT",serviceToken)
						.vcl(VisualControlType.VCL_LABEL)
						.end()
					.addTheme("THM_FORM_DEFAULT_REPLICA",serviceToken)
						.weight(3.0)
						.end()
					.end()
					.build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		BaseEntity[] bea = new BaseEntity[1];
//		bea[0] = project;
//		QDataBaseEntityMessage prjtest = new QDataBaseEntityMessage();
		qRules.publishCmd(project, "PROJECT");
		
		Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();

		QDataBaseEntityMessage msg = FrameUtils2.toMessage(FRM_POWERED_BY, serviceToken, askMsgs);
   //     qRules.publishCmd(msg);

        List<Tuple2<String,String>> sourceTargetCodes = new ArrayList<Tuple2<String,String>>();
        sourceTargetCodes.add(Tuple.of(serviceToken.getUserCode(),userToken.getUserCode()));
        
        for (QDataAskMessage askMsg : askMsgs) {
                qRules.publishCmd(askMsg, sourceTargetCodes);
        }
        System.out.println("Sent");
		
		System.out.println(user);
	}

	
	//@Test
	public void userSessionTest() {
		System.out.println("Show UserSession");
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
					.addJbpm("userLifecycle.bpmn")
					.addJbpm("userSession.bpmn")
					.addJbpm("auth_init.bpmn")
					.addJbpm("showDashboard.bpmn")
					.addJbpm("userValidation.bpmn")
					.addDrl("SendUserData")
					.addToken(userToken)
					.build();
			gks.start();

			gks.injectEvent(authInitMsg); // This should create a new process
			gks.advanceSeconds(5, true);
			gks.injectEvent(authInitMsg); // check that auth init with same session is ok and that process Id is looked up!
			gks.advanceSeconds(5, true);
			gks.injectEvent(msgLogout);

			BaseEntity user = VertxUtils.getObject(serviceToken.getRealm(), "", userToken.getUserCode(),
					BaseEntity.class, serviceToken.getToken());
			System.out.println("final user created " + user);
			System.out.println("Sent");

		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			gks.close();
		}
	}

	// @Test
	public void userSessionTest2() {
		System.out.println("Show UserSession");
		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage authInitMsg = new QEventMessage("EVT_MSG", "AUTH_INIT");
		QEventMessage msg1 = new QEventMessage("EVT_MSG", "INIT_1");
		QEventMessage msgLogout = new QEventMessage("EVT_MSG", "LOGOUT");

		GennyKieSession gks = null;

		try {
			gks = GennyKieSession.builder(serviceToken, false).addJbpm("test_session_1.bpmn")
					.addJbpm("test_session_2.bpmn").addJbpm("dashboard.bpmn").addToken(userToken).build();
			gks.start();

//				gks.advanceSeconds(2, true);
//				gks.injectSignal("userMessage", msg1);
//				gks.advanceSeconds(2, true);

			gks.injectEvent(authInitMsg);
			gks.advanceSeconds(2, true);
//				gks.injectSignal("userMessage", msgLogout);

//			for (int i=0;i<2;i++) {
//				gks.displayForm("FRM_DASHBOARD",userToken);
//				gks.advanceSeconds(2, true);
//				gks.displayForm("FRM_DASHBOARD2",userToken);
//				gks.advanceSeconds(2, true);
//			}

			// gks.sendLogout(userToken);

			System.out.println("Sent");

		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			gks.close();
		}
	}

//@Test
	public void displayBucketPage() {
		System.out.println("Show Bucket Page");
		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage msg = new QEventMessage("EVT_MSG", "INIT_STARTUP");

		GennyKieSession gks = null;
		try {
			gks = GennyKieSession.builder(serviceToken, false).addJbpm("show_bucket_page.bpmn").addFact("msg", msg)
					.addToken(userToken).build();

			gks.start();
			gks.injectSignal("inputSignal", "Hello");
			gks.advanceSeconds(20, false);

			System.out.println("Sent");

		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			gks.close();
		}
	}

	// @Test
	public void displayTestPage1() {
		System.out.println("Show test page 1");
		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage msg = new QEventMessage("EVT_MSG", "INIT_STARTUP");

		GennyKieSession gks = null;
		try {
			gks = GennyKieSession.builder(serviceToken, false).addJbpm("test_page_1.bpmn").addFact("msg", msg)
					.addToken(userToken).build();

			gks.start();
			gks.injectSignal("inputSignal", "Hello");
			gks.advanceSeconds(20, false);

			System.out.println("Sent");

		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			gks.close();
		}
	}

//@Test
	public void sendAuthInit() {

		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		QEventMessage msg = new QEventMessage("EVT_MSG", "AUTH_INIT");

		GennyKieSession gks = null;
		try {
			gks = GennyKieSession.builder(serviceToken).addDrl(DRL_SEND_USER_DATA_DIR) // send the initial User data
																						// using the rules
					.addJbpm("adhoc.bpmn").addFact("qRules", rules).addFact("msg", msg).addToken(serviceToken)
					.addToken(userToken).build();

			gks.start();

			gks.advanceSeconds(5, true);
			gks.injectMessage(msg);
			gks.advanceSeconds(3, true);
		} finally {
			gks.close();
		}
	}

	// @Test
	public void simpleTest() {
		GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
		GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
		QRules qRules = new QRules(eventBusMock, userToken.getToken());
		qRules.set("realm", userToken.getRealm());
		qRules.setServiceToken(serviceToken.getToken());

		Theme THM_NOT_INHERITBALE = Theme.builder("THM_NOT_INHERITBALE")
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();
		Frame3 logo = Frame3.builder("FRM_PROJECT_LOGO").addTheme(THM_NOT_INHERITBALE).end().build();

		Frame3 frameRoot = Frame3.builder("FRM_ROOT").addFrame(logo, FramePosition.NORTH).end().build();

		Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();
		QDataBaseEntityMessage msg = FrameUtils2.toMessage(frameRoot, serviceToken, askMsgs);

		String test = JsonUtils.toJson(msg);
		System.out.println(test);
	}

	// @Test
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
			gks = GennyKieSession.builder(serviceToken, true).addJbpm("adam_test_1.bpmn").addFact("qRules", qRules)
					.addFact("msg", msg).addToken(serviceToken).addToken(userToken).build();

			gks.start();

			gks.advanceSeconds(10, false);
			gks.broadcastSignal("inputSignal", "Hello");
			// gks.getKieSession().getQueryResults(query, arguments)
			gks.advanceSeconds(1, false);
		} finally {
			gks.close();
		}

	}

	// Only run if no background service running, used to test GenerateRules

	// @Test
	public void initLocalRulesTest() {
		System.out.println("Run the Project Initialisation");
		VertxUtils.cachedEnabled = true; // don't try and use any local services
		GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "userToken");
		GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "serviceToken");
		QRules qRules = new QRules(eventBusMock, userToken.getToken());
		qRules.set("realm", userToken.getRealm());
		qRules.setServiceToken(serviceToken.getToken());

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage msg = new QEventMessage("EVT_MSG", "INIT_STARTUP");

		GennyKieSession gks = null;
		try {
			gks = GennyKieSession.builder(serviceToken, false).addJbpm("init_project.bpmn").addDrl("GenerateSearches")
					.addDrl("GenerateThemes").addDrl("GenerateFrames").addFact("qRules", qRules).addFact("msg", msg)

					.build();

			gks.start();

			gks.advanceSeconds(20, false);
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			gks.close();
		}

	}

	// @Test
	public void testTheme() {
		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		// Theme THM_DUMMY =
		// Theme.builder("THM_DUMMY").addAttribute().height(100).end().addAttribute().width(90).end()
		// .build();
		Theme THM_DUMMY = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_DUMMY", Theme.class,
				serviceToken.getToken());

//		Theme THM_DISPLAY_VERTICAL = Theme.builder("THM_DISPLAY_VERTICAL")
//				.name("Display Visual Controls Vertically") /* Optional - defaults to the code */
//				.addAttribute(ThemeAttributeType.PRI_CONTENT).flexDirection("column").shadowOffset().height(5).width(5)
//				.end().maxWidth(600).padding(10).end().addAttribute() /* defaults to ThemeAttributeType.PRI_CONTENT */
//				.justifyContent("flex-start").end().build();
		Theme THM_DISPLAY_VERTICAL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_DISPLAY_VERTICAL",
				Theme.class, serviceToken.getToken());

//		Theme THM_DISPLAY_HORIZONTAL = Theme.builder("THM_DISPLAY_HORIZONTAL").addAttribute().flexDirection("row").end()
//				.build();
		Theme THM_DISPLAY_HORIZONTAL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_DISPLAY_HORIZONTAL",
				Theme.class, serviceToken.getToken());

//		Theme THM_BACKGROUND_WHITE = Theme.builder("THM_BACKGROUND_WHITE").addAttribute().backgroundColor("white").end()
//				.build();
		Theme THM_BACKGROUND_WHITE = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BACKGROUND_WHITE",
				Theme.class, serviceToken.getToken());

//		Theme THM_BACKGROUND_GREEN = Theme.builder("THM_BACKGROUND_GREEN").addAttribute().backgroundColor("green").end()
//				.build();
		Theme THM_BACKGROUND_GREEN = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BACKGROUND_GREEN",
				Theme.class, serviceToken.getToken());

//		Theme THM_BACKGROUND_YELLOW = Theme.builder("THM_BACKGROUND_YELLOW").addAttribute().backgroundColor("yellow")
//				.end().build();
		Theme THM_BACKGROUND_YELLOW = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BACKGROUND_YELLOW",
				Theme.class, serviceToken.getToken());

//		Theme THM_BACKGROUND_RED = Theme.builder("THM_BACKGROUND_RED").addAttribute().backgroundColor("red").end()
//				.build();
		Theme THM_BACKGROUND_RED = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BACKGROUND_RED", Theme.class,
				serviceToken.getToken());

//		Theme THM_BACKGROUND_GRAY = Theme.builder("THM_BACKGROUND_GRAY").addAttribute().backgroundColor("gray").end()
//				.build();
		Theme THM_BACKGROUND_GRAY = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BACKGROUND_GRAY",
				Theme.class, serviceToken.getToken());

		Theme THM_BACKGROUND_ORANGE = Theme.builder("THM_BACKGROUND_ORANGE").addAttribute().backgroundColor("orange")
				.end().build();

		Theme THM_BACKGROUND_BLACK = Theme.builder("THM_BACKGROUND_BLACK").addAttribute().backgroundColor("black").end()
				.build();

//		Theme THM_BACKGROUND_BLUE = Theme.builder("THM_BACKGROUND_BLUE").addAttribute().backgroundColor("blue").end()
//				.build();
		Theme THM_BACKGROUND_BLUE = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BACKGROUND_BLUE",
				Theme.class, serviceToken.getToken());

		Theme THM_BACKGROUND_INTERNMATCH = Theme.builder("THM_BACKGROUND_INTERNMATCH").addAttribute()
				.backgroundColor("#233a4e").end().build();

//		Theme THM_WIDTH_300 = Theme.builder("THM_WIDTH_300").addAttribute().width(300).end().build();
		Theme THM_WIDTH_300 = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_WIDTH_300", Theme.class,
				serviceToken.getToken());

//		Theme THM_FORM_INPUT_DEFAULT = Theme.builder("THM_FORM_INPUT_DEFAULT").addAttribute().borderBottomWidth(1)
//				.borderColor("#ddd").borderStyle("solid").placeholderColor("#888").end()
//				.addAttribute(ThemeAttributeType.PRI_CONTENT_HOVER).borderColor("#aaa").end()
//				.addAttribute(ThemeAttributeType.PRI_CONTENT_ACTIVE).borderColor("green").end()
//				.addAttribute(ThemeAttributeType.PRI_CONTENT_ERROR).borderColor("red").color("red").end().build();

		Theme THM_FORM_INPUT_DEFAULT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_FORM_INPUT_DEFAULT",
				Theme.class, serviceToken.getToken());

//		Theme THM_FORM_LABEL_DEFAULT = Theme.builder("THM_FORM_LABEL_DEFAULT").addAttribute().bold(true).size("md")
//				.end().build();
		Theme THM_FORM_LABEL_DEFAULT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_FORM_LABEL_DEFAULT",
				Theme.class, serviceToken.getToken());

//		Theme THM_FORM_WRAPPER_DEFAULT = Theme.builder("THM_FORM_WRAPPER_DEFAULT").addAttribute().marginBottom(10)
//				.padding(10).end().addAttribute(ThemeAttributeType.PRI_CONTENT_ERROR).backgroundColor("#fc8e6").end()
//				.build();
		Theme THM_FORM_WRAPPER_DEFAULT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_FORM_WRAPPER_DEFAULT",
				Theme.class, serviceToken.getToken());

//		Theme THM_FORM_ERROR_DEFAULT = Theme.builder("THM_FORM_ERROR_DEFAULT").addAttribute().color("red").end()
//				.build();
		Theme THM_FORM_ERROR_DEFAULT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_FORM_ERROR_DEFAULT",
				Theme.class, serviceToken.getToken());

//		Theme THM_FORM_DEFAULT = Theme.builder("THM_FORM_DEFAULT").addAttribute().backgroundColor("none").end()
//				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_TITLE, true).end()
//				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_DESCRIPTION, true).end()
//				.addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true).end()
//				.addAttribute(ThemeAttributeType.PRI_HAS_REQUIRED, true).end()
//				.addAttribute(ThemeAttributeType.PRI_HAS_ICON, true).end()
//				.build();
		Theme THM_FORM_DEFAULT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_FORM_DEFAULT", Theme.class,
				serviceToken.getToken());

//		Theme THM_FORM_CONTAINER_DEFAULT = Theme.builder("THM_FORM_CONTAINER_DEFAULT").addAttribute()
//				.backgroundColor("none").padding(10).maxWidth(700).width("100%").shadowColor("#000").shadowOpacity(0.4)
//				.shadowRadius(0).shadowOffset().width(0).height(0).end().end()
//				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_TITLE, true).end()
//				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_DESCRIPTION, true).end()
//				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, true).end().build();
		Theme THM_FORM_CONTAINER_DEFAULT = VertxUtils.getObject(serviceToken.getRealm(), "",
				"THM_FORM_CONTAINER_DEFAULT", Theme.class, serviceToken.getToken());

//		Frame3 FRM_DUMMY2 = Frame3.builder("FRM_DUMMY").addTheme(THM_DUMMY).end().build();
//		String td2 = JsonUtils.toJson(THM_DUMMY);
//		ThemeDouble td = new ThemeDouble(THM_DUMMY,1.0);
//		String js2 = JsonUtils.toJson(td);
//		String js = JsonUtils.toJson(FRM_DUMMY2);
//		VertxUtils.putObject(serviceToken.getRealm(), "", FRM_DUMMY2.getCode(), FRM_DUMMY2, serviceToken.getToken());

		Frame3 FRM_DUMMY = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_DUMMY", Frame3.class,
				serviceToken.getToken());

//		Frame3 centre = Frame3.builder("FRM_CENTRE").addFrame(FRM_DUMMY, FramePosition.CENTRE).end().build();
		Frame3 FRM_CENTRE = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_CENTRE", Frame3.class,
				serviceToken.getToken());

//		Frame3 profile = Frame3.builder("FRM_PROFILE").addTheme(THM_DISPLAY_HORIZONTAL).end()
//				.addTheme(THM_BACKGROUND_RED).end().addFrame(FRM_DUMMY, FramePosition.CENTRE).end().build();
		Frame3 FRM_PROFILE = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_PROFILE", Frame3.class,
				serviceToken.getToken());

//		Frame3 header = Frame3.builder("FRM_HEADER").addFrame(FRM_PROFILE, FramePosition.EAST).end().build();
		Frame3 FRM_HEADER = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_HEADER", Frame3.class,
				serviceToken.getToken());

//		Frame3 notes = Frame3.builder("FRM_NOTES").addTheme(THM_WIDTH_300).end()/*
//																				 * .addTheme(THM_DISPLAY_VERTICAL).end()
//																				 */
//				.addTheme(THM_BACKGROUND_RED).end().question("QUE_USER_COMPANY_GRP").end().build();
		Frame3 FRM_NOTES = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_NOTES", Frame3.class,
				serviceToken.getToken());

//		Frame3 FRM_SIDEBAR2 = Frame3.builder("FRM_SIDEBAR2")
//				/* .addTheme(THM_WIDTH_300).end() *//*
//													 * .addTheme(THM_DISPLAY_VERTICAL) .end()
//													 */.addTheme(THM_BACKGROUND_GRAY).end()
//				.question("QUE_USER_PROFILE_GRP").addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT)
//				.weight(2.0).end().addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
//				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
//				.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT)
//				.weight(3.0).end().addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();
		Frame3 FRM_SIDEBAR2 = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_SIDEBAR2", Frame3.class,
				serviceToken.getToken());

//		Frame3 sidebar3 = Frame3.builder("FRM_SIDEBAR3")
//				/* .addTheme(THM_WIDTH_300).end() *//*
//													 * .addTheme(THM_DISPLAY_VERTICAL) .end()
//													 */.addTheme(THM_BACKGROUND_YELLOW).end()
//				.question("QUE_USER_PROFILE_GRP").addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT)
//				.weight(2.0).end().addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
//				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
//				.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT)
//				.weight(3.0).end().addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();
		Frame3 FRM_SIDEBAR3 = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_SIDEBAR3", Frame3.class,
				serviceToken.getToken());

//		Frame3 sidebar = Frame3.builder("FRM_SIDEBAR")
//				// .addTheme(THM_WIDTH_300).end()
//
//				/*
//				 * .addTheme().addAttribute().width(400).end().end().addTheme(
//				 * THM_DISPLAY_VERTICAL).end()
//				 */
//				.addTheme(THM_BACKGROUND_GREEN).end().question("QUE_FIRSTNAME")
//				// .question("QUE_USER_PROFILE_GRP")
//
//				.addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT).weight(2.0).end()
//				.addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
//				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
//				.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT)
//				.weight(3.0).end().addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();

		Frame3 FRM_SIDEBAR = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_SIDEBAR", Frame3.class,
				serviceToken.getToken());

//		Frame3 footer = Frame3.builder("FRM_FOOTER").addFrame(FRM_DUMMY, FramePosition.CENTRE).end()
//				.addTheme(THM_BACKGROUND_BLUE).end().build();
		Frame3 FRM_FOOTER = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_FOOTER", Frame3.class,
				serviceToken.getToken());

//		Frame3 FRM_MAINFRAME = Frame3.builder("FRM_MAIN").addTheme(THM_BACKGROUND_WHITE).end()
//				.addFrame(FRM_SIDEBAR, FramePosition.WEST).end().addFrame(FRM_SIDEBAR2, FramePosition.WEST).end()
//				.addFrame(FRM_SIDEBAR3, FramePosition.WEST).end()
//				// .addFrame(notes,FramePosition.EAST).end()
//				.addFrame(FRM_FOOTER, FramePosition.SOUTH).end().addFrame(FRM_CENTRE, FramePosition.CENTRE).end()
//				.addFrame(FRM_HEADER, FramePosition.NORTH).end().build();

		Frame3 FRM_MAINFRAME = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_MAINFRAME", Frame3.class,
				serviceToken.getToken());

//		Frame3 FRM_ROOT = Frame3.builder("FRM_ROOT")
//				.addFrame(FRM_MAINFRAME).end().build();

		Frame3 FRM_ROOT = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_ROOT", Frame3.class,
				serviceToken.getToken());

		Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();

		QDataBaseEntityMessage msg = FrameUtils2.toMessage(FRM_ROOT, serviceToken, askMsgs);

//		VertxUtils.putObject(serviceToken.getRealm(), "", "FRM_ROOT-MSG", msg, serviceToken.getToken());

		QDataBaseEntityMessage msg2 = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_ROOT-MSG",
				QDataBaseEntityMessage.class, serviceToken.getToken());

		msg2.setToken(userToken.getToken());
		/* send message */
		// rules.publishCmd(msg2); // Send QDataBaseEntityMessage
		VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg2));
//		String askMsgsStr = JsonUtils.toJson(askMsgs);
//		VertxUtils.putObject(serviceToken.getRealm(), "", "DESKTOP-ASKS", askMsgsStr, serviceToken.getToken());

		Type setType = new TypeToken<Set<QDataAskMessage>>() {
		}.getType();

		String askMsgs2Str = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_ROOT-ASKS", String.class,
				serviceToken.getToken());

		Set<QDataAskMessage> askMsgs2 = JsonUtils.fromJson(askMsgs2Str, setType);

		System.out.println("Sending Asks");
		for (QDataAskMessage askMsg : askMsgs) {
			rules.publishCmd(askMsg, serviceToken.getUserCode(), userToken.getUserCode()); // Send associated
																							// QDataAskMessage
		}

		System.out.println("Sent");
	}

	// @Test
	public void testCacheTheme() {

		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		System.out.println("Starting");

		QDataBaseEntityMessage msg2 = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_ROOT-MSG",
				QDataBaseEntityMessage.class, serviceToken.getToken());

		/* send message */
		rules.publishCmd(msg2); // Send QDataBaseEntityMessage

		Type setType = new TypeToken<Set<QDataAskMessage>>() {
		}.getType();

		String askMsgs2Str = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_ROOT-ASKS", String.class,
				serviceToken.getToken());

		Set<QDataAskMessage> askMsgs2 = JsonUtils.fromJson(askMsgs2Str, setType);

		System.out.println("Sending Asks");
		for (QDataAskMessage askMsg : askMsgs2) {
			rules.publishCmd(askMsg, serviceToken.getUserCode(), userToken.getUserCode()); // Send associated
																							// QDataAskMessage
		}

		System.out.println("Sent2");

	}

	// @Test
	public void testLogout() {

	}

	// @Test
	public void formsTest() {
		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		String apiUrl = GennySettings.qwandaServiceUrl + "/service/forms";
		System.out.println("Fetching setup info from " + apiUrl);
		System.out.println("userToken (ensure user has test role) = " + userToken);
		try {
			String jsonFormCodes = QwandaUtils.apiGet(apiUrl, userToken.getToken());
			if (!"You need to be a test.".equals(jsonFormCodes)) {
				Type type = new TypeToken<List<String>>() {
				}.getType();
				List<String> formCodes = JsonUtils.fromJson(jsonFormCodes, type);
				System.out.println("Form Codes=" + formCodes);

				for (String formCode : formCodes) {
					// rules.sendForm("QUE_ADD_HOST_COMPANY_GRP", rules.getUser().getCode(),
					// rules.getUser().getCode());
				}

			} else {
				System.out.println("Ensure that the user you are using has a 'test' role ...");
			}

		} catch (Exception e) {

		}
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

}