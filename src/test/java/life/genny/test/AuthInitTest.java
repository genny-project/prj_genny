package life.genny.test;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.kie.api.command.Command;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieSession;
import org.kie.internal.command.CommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

import life.genny.models.GennyToken;
import life.genny.qwanda.Context.VisualControlType;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.rules.QRules;
import life.genny.rules.listeners.JbpmInitListener;

public class AuthInitTest extends GennyJbpmBaseTest {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	private static final String WFE_SEND_FORMS = "rulesCurrent/shared/_BPMN_WORKFLOWS/send_forms.bpmn";
	private static final String WFE_SHOW_FORM = "rulesCurrent/shared/_BPMN_WORKFLOWS/show_form.bpmn";
	private static final String WFE_AUTH_INIT = "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/auth_init.bpmn";
	private static final String WFE_SEND_LLAMA = "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/send_llama.bpmn";
	private static final String DRL_PROJECT = "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/SendUserData/project.drl";
	private static final String DRL_USER_COMPANY = "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/SendUserData/user_company.drl";
	private static final String DRL_USER = "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/SendUserData/user.drl";
	private static final String DRL_EVENT_LISTENER_SERVICE_SETUP = "rulesCurrent/shared/_BPMN_WORKFLOWS/Initialise_Project/eventListenerServiceSetup.drl";
	private static final String DRL_EVENT_LISTENER_USER_SETUP = "rulesCurrent/shared/_BPMN_WORKFLOWS/Initialise_Project/eventListenerUserSetup.drl";

	public AuthInitTest() {
		super(false);
	}

	//@Test
	public void displayGermanFlag() {

		/* token stuff */
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken); // defaults to user anyway
		GennyToken serviceToken = new GennyToken("serviceToken", rules.getServiceToken());

		/* create theme */
		Theme THM_COLOR_GREY = Theme.builder("THM_COLOR_GREY").addAttribute().backgroundColor("grey").end().build();
		Theme THM_DUMMY = Theme.builder("THM_DUMMY").addAttribute().height(100).end().addAttribute().width(100).end()
				.build();

		Theme THM_COLOR_RED = Theme.builder("THM_COLOR_RED").addAttribute().backgroundColor("red").end().build();
		Theme THM_COLOR_GREEN = Theme.builder("THM_COLOR_GREEN").addAttribute().backgroundColor("green").end().build();
		Theme THM_COLOR_YELLOW = Theme.builder("THM_COLOR_YELLOW").addAttribute().backgroundColor("yellow").end()
				.build();
		Theme THM_COLOR_BLUE = Theme.builder("THM_COLOR_BLUE").addAttribute().backgroundColor("blue").end().build();
		Theme THM_COLOR_BLACK = Theme.builder("THM_COLOR_BLACK").addAttribute().backgroundColor("black").end().build();

		/* create frames */

		Frame3 frameDummy = Frame3.builder("FRM_DUMMY").addTheme(THM_DUMMY).end().build();

		Frame3 frameEast = Frame3.builder("FRM_EAST").addTheme(THM_COLOR_RED).end()
				.addFrame(frameDummy, FramePosition.CENTRE).build();
		Frame3 frameWest = Frame3.builder("FRM_WEST").addTheme(THM_COLOR_GREEN).end()
				.addFrame(frameDummy, FramePosition.CENTRE).build();
		Frame3 frameNorth = Frame3.builder("FRM_NORTH").addTheme(THM_COLOR_YELLOW).end()
				.addFrame(frameDummy, FramePosition.CENTRE).build();
		Frame3 frameSouth = Frame3.builder("FRM_SOUTH").addTheme(THM_COLOR_BLUE).end()
				.addFrame(frameDummy, FramePosition.CENTRE).build();
		Frame3 frameCentre = Frame3.builder("FRM_CENTRE").addTheme(THM_COLOR_BLACK).end()
				.addFrame(frameDummy, FramePosition.CENTRE).build();

		Frame3 frameTest = Frame3.builder("FRM_TEST")
				.addTheme(THM_COLOR_GREY).end()
				.addFrame(frameEast, FramePosition.EAST).end()
				.addFrame(frameWest, FramePosition.WEST).end()
				.addFrame(frameNorth, FramePosition.NORTH).end()
				.addFrame(frameSouth, FramePosition.SOUTH).end()
				.addFrame(frameCentre, FramePosition.CENTRE).end()
				.build();

		Frame3 frameRoot = Frame3.builder("FRM_ROOT").addFrame(frameTest).end().build();

		/* send message */
		Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();
		QDataBaseEntityMessage msg = FrameUtils2.toMessage(frameRoot, serviceToken, askMsgs);
		rules.publishCmd(msg);
		System.out.println("Sent");

	}

	@Test
	public void testTheme() {
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken); // defaults to user anyway
		GennyToken serviceToken = new GennyToken("serviceToken", rules.getServiceToken());
		
		Theme THM_DUMMY = Theme.builder("THM_DUMMY").addAttribute().height(100).end().addAttribute().width(100).end()
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

		
		Theme THM_BACKGROUND_RED = Theme.builder("THM_BACKGROUND_RED").addAttribute().backgroundColor("red").end()
				.build();

		Theme THM_BACKGROUND_GRAY = Theme.builder("THM_BACKGROUND_GRAY").addAttribute().backgroundColor("gray").end()
				.build();

		Theme THM_BACKGROUND_BLACK = Theme.builder("THM_BACKGROUND_BLACK").addAttribute().backgroundColor("black").end()
				.build();

		Theme THM_BACKGROUND_BLUE = Theme.builder("THM_BACKGROUND_BLUE").addAttribute().backgroundColor("blue").end()
				.build();

		
		Theme THM_BACKGROUND_INTERNMATCH = Theme.builder("THM_BACKGROUND_INTERNMATCH").addAttribute()
				.backgroundColor("#233a4e").end().build();

		Theme THM_WIDTH_300 = Theme.builder("THM_WIDTH_300").addAttribute().width(300).end().build();
		
		Theme THM_FORM_INPUT_DEFAULT = Theme.builder("THM_FORM_INPUT_DEFAULT").addAttribute().borderBottomWidth(1).borderColor("#ddd").borderStyle("solid").placeholderColor("#888").end()
				.addAttribute(ThemeAttributeType.PRI_CONTENT_HOVER).borderColor("#aaa").end()
				.addAttribute(ThemeAttributeType.PRI_CONTENT_ACTIVE).borderColor("green").end()
				.addAttribute(ThemeAttributeType.PRI_CONTENT_ERROR).borderColor("red").color("red").end()			
				.build();
		
		Theme THM_FORM_LABEL_DEFAULT = Theme.builder("THM_FORM_LABEL_DEFAULT")
				.addAttribute().end()		
				.build();
		
		Theme THM_FORM_WRAPPER_DEFAULT = Theme.builder("THM_FORM_WRAPPER_DEFAULT")
				.addAttribute().marginBottom(10).padding(10).end()
				.addAttribute(ThemeAttributeType.PRI_CONTENT_ERROR).backgroundColor("#fc8e6").end()
				.build();

		Theme THM_FORM_ERROR_DEFAULT = Theme.builder("THM_FORM_ERROR_DEFAULT")
				.addAttribute().color("red").end()
				.build();

		Theme THM_FORM_DEFAULT = Theme.builder("THM_FORM_DEFAULT")
				.addAttribute().backgroundColor("none").end()
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_TITLE,true).end()
				.addAttribute(ThemeAttributeType.PRI_HPRI_HAS_QUESTION_GRP_DESCRIPTION,true)
					.PRI_HAS_LABEL(true)
					.PRI_HAS_REQUIRED(true)
					.PRI_HAS_ICON(true)
					.end()
				.build();

		Theme THM_FORM_CONTAINER_DEFAULT = Theme.builder("THM_FORM_CONTAINER_DEFAULT")
				.addAttribute().backgroundColor("white")
					.padding(10)
					.maxWidth(700)
					.width("100%")
					.shadowColor("#000")
					.shadowOpacity(0.4)
					.shadowRadius(5)
					.shadowOffset()
						.width(0)
						.height(0)
						.end()
				.end()
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_TITLE,true).end()
				.addAttribute(ThemeAttributeType.PRI_HPRI_HAS_QUESTION_GRP_DESCRIPTION,true)
					.PRI_HAS_LABEL(true)
					.PRI_HAS_REQUIRED(true)
					.PRI_HAS_ICON(true)
					.end()
				.build();	
		
		Frame3 frameDummy = Frame3.builder("FRM_DUMMY").addTheme(THM_DUMMY).end().build();


		Frame3 centre = Frame3.builder("FRM_CENTRE").addFrame(frameDummy, FramePosition.CENTRE).end().build();

		Frame3 profile = Frame3.builder("FRM_PROFILE")
				.addTheme(THM_DISPLAY_HORIZONTAL).end()
				.addTheme(THM_BACKGROUND_RED).end()
				.addFrame(frameDummy, FramePosition.CENTRE).end()
				.build();

		Frame3 header = Frame3.builder("FRM_HEADER").addFrame(profile, FramePosition.EAST).end().build();

		Frame3 notes = Frame3.builder("FRM_NOTES")
				.addTheme(THM_WIDTH_300).end()
				.addTheme(THM_DISPLAY_VERTICAL).end()
				.addTheme(THM_BACKGROUND_RED).end()
				.question("QUE_USER_COMPANY_GRP").end()
				.build();
		Frame3 sidebar2 = Frame3.builder("FRM_SIDEBAR2")
				.addTheme(THM_WIDTH_300).end()
				.addTheme(THM_DISPLAY_VERTICAL).end()
				.addTheme(THM_BACKGROUND_GRAY).end()
				.question("QUE_USER_PROFILE_GRP").end()
				.build();

		
		Frame3 sidebar = Frame3.builder("FRM_SIDEBAR")
				.addTheme(THM_WIDTH_300).end()
				.addTheme(THM_DISPLAY_VERTICAL).end()
				.addTheme(THM_BACKGROUND_GREEN).end()
				.question("QUE_USER_PROFILE_GRP")
					.addTheme(THM_FORM_INPUT_DEFAULT)
						.vcl(VisualControlType.INPUT)
						.weight(2.0)
						.end()
					.addTheme(THM_FORM_LABEL_DEFAULT)
						.vcl(VisualControlType.LABEL)
						.end()
					.addTheme(THM_FORM_WRAPPER_DEFAULT)
						.vcl(VisualControlType.WRAPPER)
						.end()
					.addTheme(THM_FORM_ERROR_DEFAULT)
						.vcl(VisualControlType.ERROR)
						.end()
					.addTheme(THM_FORM_DEFAULT)
						.weight(3.0)
						.end()
					.addTheme(THM_FORM_CONTAINER_DEFAULT)
						.weight(2.0)
						.end()
					.end()											
				.build();

		Frame3 footer = Frame3.builder("FRM_FOOTER").addFrame(frameDummy, FramePosition.CENTRE).end().addTheme(THM_BACKGROUND_BLUE).end().build();

		Frame3 mainFrame = Frame3.builder("FRM_MAIN")		    	
				.addTheme("THM_COLOR_WHITE").end()               
                .addTheme(THM_BACKGROUND_INTERNMATCH).end() 
                .addTheme(THM_BACKGROUND_BLACK).end()  
                .addTheme(THM_BACKGROUND_GRAY).end()  
                .addTheme(THM_BACKGROUND_WHITE).end() 
                
		    	.addFrame(sidebar,FramePosition.WEST).end()
		    //	.addFrame(sidebar2,FramePosition.WEST).end() // not working as expected
		    	.addFrame(notes,FramePosition.EAST).end()
		    	.addFrame(footer,FramePosition.SOUTH).end()
		    	.addFrame(centre,FramePosition.CENTRE).end()
		    	.addFrame(header,FramePosition.NORTH).end()
		    	.build();
		
		Frame3 desktop = Frame3.builder("FRM_ROOT")

				.addFrame(mainFrame).end().build();

		Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();

		QDataBaseEntityMessage msg = FrameUtils2.toMessage(desktop, serviceToken, askMsgs);

		/* send message */
		rules.publishCmd(msg); // Send QDataBaseEntityMessage

		System.out.println("Sending Asks");
		for (QDataAskMessage askMsg : askMsgs) {
			rules.publishCmd(askMsg, serviceToken.getUserCode(), userToken.getUserCode()); // Send associated
																							// QDataAskMessage
		}

		System.out.println("Sent");
	}

	// @Test
	public void testDesktopPageDisplay() {

		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken); // defaults to user anyway
		GennyToken serviceToken = new GennyToken("serviceToken", rules.getServiceToken());

		setUpCache(GennySettings.mainrealm, userToken);

		Frame3 centre = Frame3.builder("FRM_CENTRE").build();

		Frame3 profile = Frame3.builder("FRM_PROFILE")
				.addTheme("THM_DISPLAY_HORIZONTAL", "flexDirection", "row").end()
				.addTheme("THM_BACKGROUND_RED", "backgroundColor", "red").end().build();

		Frame3 header = Frame3.builder("FRM_HEADER").addFrame(profile, FramePosition.EAST).build();

		Frame3 sidebar = Frame3.builder("FRM_SIDEBAR").addTheme("THM_WIDTH_300", "width", 300).end()
				.addTheme("THM_DISPLAY_VERTICAL", "flexDirection", "column").end()
				.addTheme("THM_DISPLAY_VERTICAL", "justifyContent", "flex-start").end()
				.addTheme("THM_BACKGROUND_RED", "backgroundColor", "red").end().question("QUE_USER_PROFILE_GRP").end().build();

		Frame3 notes = Frame3.builder("FRM_NOTES").addTheme("THM_WIDTH_300", "width", 300).end()
				.addTheme("THM_DISPLAY_VERTICAL", "flexDirection", "column").end()
				.addTheme("THM_DISPLAY_VERTICAL", "justifyContent", "flex-start").end().build();

		Frame3 footer = Frame3.builder("FRM_FOOTER").build();

		Frame3 mainFrame = Frame3.builder("FRM_MAIN").addTheme("THM_COLOR_WHITE").end()
				.addFrame(header, FramePosition.NORTH).end().addFrame(sidebar, FramePosition.EAST).end()
				.addFrame(footer, FramePosition.SOUTH).end().addFrame(centre, FramePosition.CENTRE).end().build();

		Frame3 desktop = Frame3.builder("FRM_ROOT")
				.addTheme("THM_BACKGROUND_GRAY", "backgroundColor", "gray").end()
				.addTheme("THM_BACKGROUND_INTERNMATCH", "backgroundColor", "#233a4e").end()
				.addTheme("THM_COLOR_WHITE", "backgroundColor", "white").end()
				.addTheme("THM_COLOR_BLACK", ThemeAttributeType.PRI_CONTENT, "backgroundColor", "black").end()

				.addFrame(mainFrame).end().build();

		Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();

		QDataBaseEntityMessage msg = FrameUtils2.toMessage(desktop, serviceToken, askMsgs);

		/* send message */
		rules.publishCmd(msg); // Send QDataBaseEntityMessage

		for (QDataAskMessage askMsg : askMsgs) {
			rules.publishCmd(askMsg, serviceToken.getUserCode(), userToken.getUserCode()); // Send associated
																							// QDataAskMessage
		}

		System.out.println("Sent");
	}

	// @Test

	public void testAuthInit() {

		Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
		String[] jbpms = { WFE_AUTH_INIT, WFE_SEND_FORMS, WFE_SHOW_FORM, WFE_SEND_LLAMA };
		String[] drls = { DRL_PROJECT, DRL_USER_COMPANY, DRL_USER, DRL_EVENT_LISTENER_SERVICE_SETUP,
				DRL_EVENT_LISTENER_USER_SETUP };
		for (String p : jbpms) {
			resources.put(p, ResourceType.BPMN2);
		}
		for (String p : drls) {
			resources.put(p, ResourceType.DRL);
		}
		createRuntimeManager(Strategy.SINGLETON, resources, null);
		KieSession kieSession = getRuntimeEngine().getKieSession();
		// Register handlers
		addWorkItemHandlers(kieSession);
		kieSession.addEventListener(new JbpmInitListener(userToken));

		QEventMessage msg = new QEventMessage("EVT_MSG", "AUTH_INIT");

		List<Command<?>> cmds = new ArrayList<Command<?>>();

		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules qRules = getQRules(userToken); // defaults to user anyway
		System.out.println(qRules.getToken());
		cmds.add(CommandFactory.newInsert(qRules, "qRules"));
		cmds.add(CommandFactory.newInsert(msg, "msg"));
		cmds.add(CommandFactory.newInsert(userToken, "userToken"));
		cmds.add(CommandFactory.newInsert(new GennyToken("serviceUser", qRules.getServiceToken()), "serviceToken"));
		// Set up Cache

		setUpCache(GennySettings.mainrealm, userToken);

		cmds.add(CommandFactory.newInsert(eventBusMock, "eb"));

		long startTime = System.nanoTime();
		ExecutionResults results = null;
		try {
			results = kieSession.execute(CommandFactory.newBatchExecution(cmds));
		} catch (Exception ee) {

		} finally {
			long endTime = System.nanoTime();
			double difference = (endTime - startTime) / 1e6; // get ms

			if (results != null) {
				results.getValue("msg"); // returns the inserted fact Msg
				QRules rules = (QRules) results.getValue("qRules"); // returns the inserted fact QRules
				System.out.println(results.getValue("msg"));
				System.out.println(rules);
			} else {
				System.out.println("NO RESULTS");
			}

			System.out.println("BPMN completed in " + difference + " ms");

			kieSession.dispose();
		}

	}

	// @Test
	public void formsTest() {
		String apiUrl = GennySettings.qwandaServiceUrl + ":8280/service/forms";
		System.out.println("Fetching setup info from " + apiUrl);
		String userToken = projectParms.getString("userToken");
		System.out.println("userToken (ensure user has test role) = " + userToken);
		try {
			String jsonFormCodes = QwandaUtils.apiGet(apiUrl, userToken);
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

}