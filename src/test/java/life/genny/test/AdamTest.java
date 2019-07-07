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

public class AdamTest {

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

	public AdamTest() {

	}

	@Test
	public void  initRemoteInitProjectTest() {
	System.out.println("Run the Remote Project Initialisation");
	QRules rules = setupLocalService();
	GennyToken userToken = new GennyToken("userToken", rules.getToken());
	GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

	System.out.println("session     =" + userToken.getSessionCode());
	System.out.println("userToken   =" + userToken.getToken());
	System.out.println("serviceToken=" + serviceToken.getToken());

	QEventMessage msg = new QEventMessage("EVT_MSG", "INIT_STARTUP");

	GennyKieSession gks = null;
	try {
		gks = GennyKieSession.builder(serviceToken,false)
				.addJbpm("init_project.bpmn")
				.addDrl("GenerateSearches")
				.addDrl("GenerateThemes")
				.addDrl("GenerateFrames")
				.addFact("qRules", rules)
				.addFact("msg", msg)

				.build();

		gks.start();

		gks.advanceSeconds(20, false);
		
		
		// test cache has data
		Frame3 bucket = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_BUCKET", Frame3.class, serviceToken.getToken());
		QDataBaseEntityMessage bucketMsg = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_BUCKET-MSG", QDataBaseEntityMessage.class, serviceToken.getToken());
		
		/* send message */
		rules.publishCmd(bucketMsg); // Send QDataBaseEntityMessage

	} catch (Exception e)
	{
		System.out.println(e.getLocalizedMessage());
	} finally {
		gks.close();
	}
}
	
	
   // @Test
	public void sendAuthInit()
	{


		QRules rules = setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		QEventMessage msg = new QEventMessage("EVT_MSG", "AUTH_INIT");

		GennyKieSession gks = null;
		try {
			gks = GennyKieSession.builder(serviceToken)
					.addDrl(DRL_SEND_USER_DATA_DIR)   // send the initial User data using the rules
					.addJbpm("auth_init.bpmn")
					.addJbpm("send_llama.bpmn")
					.addFact("qRules", rules)
					.addFact("msg", msg)
					.addToken(serviceToken)
					.addToken(userToken)
					.build();

			gks.start();

			gks.advanceSeconds(10, false);
		} finally {
			gks.close();
		}
	}
	
	
	//@Test
	public void simpleTest() {
		GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
		GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
		QRules qRules = new QRules(eventBusMock, userToken.getToken(), userToken.getAdecodedTokenMap());
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
	
	


	// Only run if no background service running, used to test GenerateRules
	
	//@Test
	public void initLocalRulesTest() {
		System.out.println("Run the Project Initialisation");
		VertxUtils.cachedEnabled = true; // don't try and use any local services
		GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "userToken");
		GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "serviceToken");
		QRules qRules = new QRules(eventBusMock, userToken.getToken(), userToken.getAdecodedTokenMap());
		qRules.set("realm", userToken.getRealm());
		qRules.setServiceToken(serviceToken.getToken());

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage msg = new QEventMessage("EVT_MSG", "INIT_STARTUP");

		GennyKieSession gks = null;
		try {
			gks = GennyKieSession.builder(serviceToken,false)
					.addJbpm("init_project.bpmn")
					.addDrl("GenerateSearches")
					.addDrl("GenerateThemes")
					.addDrl("GenerateFrames")
					.addFact("qRules", qRules)
					.addFact("msg", msg)

					.build();

			gks.start();

			gks.advanceSeconds(20, false);
		} catch (Exception e)
		{
			System.out.println(e.getLocalizedMessage());
		} finally {
			gks.close();
		}

	}

	private QRules setupLocalService() {
		GennyJbpmBaseTest localService = new GennyJbpmBaseTest(false);
		try {
			localService.init();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		GennyToken userToken = localService.getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = localService.getQRules(userToken); // defaults to user anyway

		return rules;
	}

	
	
	
//	@Test
	public void testTheme() {
		QRules rules = setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		Theme THM_DUMMY = Theme.builder("THM_DUMMY").addAttribute().height(100).end().addAttribute().width(90).end()
				.build();

		Theme THM_DISPLAY_VERTICAL = Theme.builder("THM_DISPLAY_VERTICAL")
				.name("Display Visual Controls Vertically") /* Optional - defaults to the code */
				.addAttribute(ThemeAttributeType.PRI_CONTENT).flexDirection("column").shadowOffset().height(5).width(5)
				.end().maxWidth(600).padding(10).end().addAttribute() /* defaults to ThemeAttributeType.PRI_CONTENT */
				.justifyContent("flex-start").end().build();

		Theme THM_DISPLAY_HORIZONTAL = Theme.builder("THM_DISPLAY_HORIZONTAL").addAttribute().flexDirection("row").end()
				.build();

		Theme THM_BACKGROUND_WHITE = Theme.builder("THM_BACKGROUND_WHITE").addAttribute().backgroundColor("white").end()
				.build();

		Theme THM_BACKGROUND_GREEN = Theme.builder("THM_BACKGROUND_GREEN").addAttribute().backgroundColor("green").end()
				.build();
		Theme THM_BACKGROUND_YELLOW = Theme.builder("THM_BACKGROUND_YELLOW").addAttribute().backgroundColor("yellow")
				.end().build();

		Theme THM_BACKGROUND_RED = Theme.builder("THM_BACKGROUND_RED").addAttribute().backgroundColor("red").end()
				.build();

		Theme THM_BACKGROUND_GRAY = Theme.builder("THM_BACKGROUND_GRAY").addAttribute().backgroundColor("gray").end()
				.build();

		Theme THM_BACKGROUND_ORANGE = Theme.builder("THM_BACKGROUND_ORANGE").addAttribute().backgroundColor("orange")
				.end().build();

		Theme THM_BACKGROUND_BLACK = Theme.builder("THM_BACKGROUND_BLACK").addAttribute().backgroundColor("black").end()
				.build();

		Theme THM_BACKGROUND_BLUE = Theme.builder("THM_BACKGROUND_BLUE").addAttribute().backgroundColor("blue").end()
				.build();

		Theme THM_BACKGROUND_INTERNMATCH = Theme.builder("THM_BACKGROUND_INTERNMATCH").addAttribute()
				.backgroundColor("#233a4e").end().build();

		Theme THM_WIDTH_300 = Theme.builder("THM_WIDTH_300").addAttribute().width(300).end().build();

		Theme THM_FORM_INPUT_DEFAULT = Theme.builder("THM_FORM_INPUT_DEFAULT").addAttribute().borderBottomWidth(1)
				.borderColor("#ddd").borderStyle("solid").placeholderColor("#888").end()
				.addAttribute(ThemeAttributeType.PRI_CONTENT_HOVER).borderColor("#aaa").end()
				.addAttribute(ThemeAttributeType.PRI_CONTENT_ACTIVE).borderColor("green").end()
				.addAttribute(ThemeAttributeType.PRI_CONTENT_ERROR).borderColor("red").color("red").end().build();

		Theme THM_FORM_LABEL_DEFAULT = Theme.builder("THM_FORM_LABEL_DEFAULT").addAttribute().bold(true).size("md")
				.end().build();

		Theme THM_FORM_WRAPPER_DEFAULT = Theme.builder("THM_FORM_WRAPPER_DEFAULT").addAttribute().marginBottom(10)
				.padding(10).end().addAttribute(ThemeAttributeType.PRI_CONTENT_ERROR).backgroundColor("#fc8e6").end()
				.build();

		Theme THM_FORM_ERROR_DEFAULT = Theme.builder("THM_FORM_ERROR_DEFAULT").addAttribute().color("red").end()
				.build();

		Theme THM_FORM_DEFAULT = Theme.builder("THM_FORM_DEFAULT").addAttribute().backgroundColor("none").end()
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_TITLE, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_DESCRIPTION, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_REQUIRED, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_ICON, true).end()

				.build();

		Theme THM_FORM_CONTAINER_DEFAULT = Theme.builder("THM_FORM_CONTAINER_DEFAULT").addAttribute()
				.backgroundColor("none").padding(10).maxWidth(700).width("100%").shadowColor("#000").shadowOpacity(0.4)
				.shadowRadius(0).shadowOffset().width(0).height(0).end().end()
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_TITLE, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_DESCRIPTION, true).end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, true).end().build();

		Frame3 frameDummy = Frame3.builder("FRM_DUMMY").addTheme(THM_DUMMY).end().build();

		Frame3 centre = Frame3.builder("FRM_CENTRE").addFrame(frameDummy, FramePosition.CENTRE).end().build();

		Frame3 profile = Frame3.builder("FRM_PROFILE").addTheme(THM_DISPLAY_HORIZONTAL).end()
				.addTheme(THM_BACKGROUND_RED).end().addFrame(frameDummy, FramePosition.CENTRE).end().build();

		Frame3 header = Frame3.builder("FRM_HEADER").addFrame(profile, FramePosition.EAST).end().build();

		Frame3 notes = Frame3.builder("FRM_NOTES").addTheme(THM_WIDTH_300).end()/*
																				 * .addTheme(THM_DISPLAY_VERTICAL).end()
																				 */
				.addTheme(THM_BACKGROUND_RED).end().question("QUE_USER_COMPANY_GRP").end().build();
		Frame3 sidebar2 = Frame3.builder("FRM_SIDEBAR2")
				/* .addTheme(THM_WIDTH_300).end() *//*
													 * .addTheme(THM_DISPLAY_VERTICAL) .end()
													 */.addTheme(THM_BACKGROUND_GRAY).end()
				.question("QUE_USER_PROFILE_GRP").addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT)
				.weight(2.0).end().addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
				.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT)
				.weight(3.0).end().addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();

		Frame3 sidebar3 = Frame3.builder("FRM_SIDEBAR3")
				/* .addTheme(THM_WIDTH_300).end() *//*
													 * .addTheme(THM_DISPLAY_VERTICAL) .end()
													 */.addTheme(THM_BACKGROUND_YELLOW).end()
				.question("QUE_USER_PROFILE_GRP").addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT)
				.weight(2.0).end().addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
				.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT)
				.weight(3.0).end().addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();

		Frame3 sidebar = Frame3.builder("FRM_SIDEBAR")
				// .addTheme(THM_WIDTH_300).end()

				/*
				 * .addTheme().addAttribute().width(400).end().end().addTheme(
				 * THM_DISPLAY_VERTICAL).end()
				 */
				.addTheme(THM_BACKGROUND_GREEN).end().question("QUE_FIRSTNAME")
				// .question("QUE_USER_PROFILE_GRP")

				.addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT).weight(2.0).end()
				.addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
				.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT)
				.weight(3.0).end().addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();

		Frame3 footer = Frame3.builder("FRM_FOOTER").addFrame(frameDummy, FramePosition.CENTRE).end()
				.addTheme(THM_BACKGROUND_BLUE).end().build();

		Frame3 mainFrame = Frame3.builder("FRM_MAIN").addTheme(THM_BACKGROUND_WHITE).end()
				.addFrame(sidebar, FramePosition.WEST).end().addFrame(sidebar2, FramePosition.WEST).end()
				.addFrame(sidebar3, FramePosition.WEST).end()
				// .addFrame(notes,FramePosition.EAST).end()
				.addFrame(footer, FramePosition.SOUTH).end().addFrame(centre, FramePosition.CENTRE).end()
				.addFrame(header, FramePosition.NORTH).end().build();

		Frame3 desktop = Frame3.builder("FRM_ROOT")

				.addFrame(mainFrame).end().build();

		Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();

		QDataBaseEntityMessage msg = FrameUtils2.toMessage(desktop, serviceToken, askMsgs);

		VertxUtils.putObject(serviceToken.getRealm(), "", "DESKTOP", msg, serviceToken.getToken());

		QDataBaseEntityMessage msg2 = VertxUtils.getObject(serviceToken.getRealm(), "", "DESKTOP",
				QDataBaseEntityMessage.class, serviceToken.getToken());

		/* send message */
		rules.publishCmd(msg2); // Send QDataBaseEntityMessage

		String askMsgsStr = JsonUtils.toJson(askMsgs);
		VertxUtils.putObject(serviceToken.getRealm(), "", "DESKTOP-ASKS", askMsgsStr, serviceToken.getToken());

		Type setType = new TypeToken<Set<QDataAskMessage>>() {
		}.getType();

		String askMsgs2Str = VertxUtils.getObject(serviceToken.getRealm(), "", "DESKTOP-ASKS", String.class,
				serviceToken.getToken());

		Set<QDataAskMessage> askMsgs2 = JsonUtils.fromJson(askMsgs2Str, setType);

		System.out.println("Sending Asks");
		for (QDataAskMessage askMsg : askMsgs2) {
			rules.publishCmd(askMsg, serviceToken.getUserCode(), userToken.getUserCode()); // Send associated
																							// QDataAskMessage
		}

		System.out.println("Sent");
	}

	// @Test
	public void testCacheTheme() {

		QRules rules = setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		System.out.println("Starting");

		QDataBaseEntityMessage msg2 = VertxUtils.getObject(serviceToken.getRealm(), "", "DESKTOP",
				QDataBaseEntityMessage.class, serviceToken.getToken());

		/* send message */
		rules.publishCmd(msg2); // Send QDataBaseEntityMessage

		Type setType = new TypeToken<Set<QDataAskMessage>>() {
		}.getType();

		String askMsgs2Str = VertxUtils.getObject(serviceToken.getRealm(), "", "DESKTOP-ASKS", String.class,
				serviceToken.getToken());

		Set<QDataAskMessage> askMsgs2 = JsonUtils.fromJson(askMsgs2Str, setType);

		System.out.println("Sending Asks");
		for (QDataAskMessage askMsg : askMsgs2) {
			rules.publishCmd(askMsg, serviceToken.getUserCode(), userToken.getUserCode()); // Send associated
																							// QDataAskMessage
		}

		System.out.println("Sent2");

	}

	@Test
	public void testLogout() {

	}

	// @Test
	public void formsTest() {
		QRules rules = setupLocalService();
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