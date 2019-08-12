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

import life.genny.models.Frame3;
import life.genny.models.FramePosition;
import life.genny.models.GennyToken;
import life.genny.models.Theme;
import life.genny.models.ThemeAttributeType;
import life.genny.qwanda.Answer;
import life.genny.qwanda.VisualControlType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.rules.QRules;
import life.genny.rules.listeners.JbpmInitListener;
import life.genny.utils.BaseEntityUtils;
import life.genny.utils.FrameUtils2;
import life.genny.utils.VertxUtils;

public class CyrusTest extends GennyJbpmBaseTest {

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

	public CyrusTest() {
		super(false);
	}

 @Test
	public void addInternshipTwoAgent() {
		// getting the tokens
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken);
    GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
    BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
    BaseEntity project = beUtils.getBaseEntityByCode( "PRJ_" + serviceToken.getRealm().toUpperCase());

		// building the themes and the footers
		Theme THM_COLOR_GREY = Theme.builder("THM_COLOR_RED").addAttribute().backgroundColor("red").end().build();


		Theme THM_FORM_VCL_INPUT_GENNY= Theme.builder("THM_FORM_VCL_INPUT_GENNY").addAttribute().borderBottomWidth(1)
				.borderColor("#ddd").borderStyle("solid").placeholderColor("#888").end()
				.addAttribute(ThemeAttributeType.PRI_CONTENT_HOVER).borderColor("#aaa").end()
				.addAttribute(ThemeAttributeType.PRI_CONTENT_ACTIVE).borderColor("green").end()
				//.addAttribute(ThemeAttributeType.PRI_CONTENT_ERROR).borderColor("red").color("red").end()
				.build();

		Theme THM_FORM_VCL_LABEL_GENNY = Theme.builder("THM_FORM_VCL_LABEL_GENNY").addAttribute().color("black").end().build();

		Theme THM_FORM_VCL_WRAPPER_GENNY = Theme.builder("THM_FORM_VCL_WRAPPER_GENNY").addAttribute().marginBottom(10)
		 		.padding(10).end().addAttribute(ThemeAttributeType.PRI_CONTENT_ERROR).backgroundColor("#fc8e6").end().build();

		Theme THM_FORM_VCL_ERROR_GENNY= Theme.builder("THM_FORM_VCL_ERROR_GENNY").addAttribute().color("red").end().build();

	/*	Theme THM_FORM_DEFAULT_FORM = Theme.builder("THM_FORM_DEFAULT_FORM")
				.addAttribute().backgroundColor("none").end()
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_TITLE, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_DESCRIPTION, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_REQUIRED, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_ICON, true).end().build(); */

			Theme THM_FORM_BEHAVIOUR_GENNY= Theme.builder("THM_FORM_BEHAVIOUR_GENNY")
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_LABEL, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_DESCRIPTION, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_REQUIRED, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_ICON, true).end()
			.build(); 

			Theme THM_BACKGROUND_NONE= Theme.builder("THM_BACKGROUND_NONE")
				.addAttribute()
					.backgroundColor("none")
				.end()
			.build(); 

			Theme THM_FORM_GROUP_LABEL_GENNY= Theme.builder("THM_FORM_GROUP_LABEL_GENNY")
				.addAttribute()
					.color("black")
				.end()
			.build(); 

				
			Theme THM_FORM_GROUP_WRAPPER_GENNY = Theme.builder("THM_FORM_GROUP_WRAPPER_GENNY")
				.addAttribute()
						.backgroundColor("white").padding(10).maxWidth(600)
						.borderWidth(2)
						.borderColor("black")
						.borderStyle("solid")
						.margin(5)
					.end()
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_TITLE, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_DESCRIPTION, true).end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

		/*Theme THM_BUTTONS = Theme.builder("THM_BUTTONS").addAttribute().backgroundColor("#ffffff").padding(10)
				.justifyContent("center").borderColor("#000000").margin(4).maxWidth(700).width("100%").shadowColor("#000")
				.shadowOpacity(0.8).shadowRadius(5).end().build();*/

		// Theme THM_BUTTON_DEFAULT = Theme.builder("THM_BUTTON_DEFAULT")
		// 	.addAttribute()
		// 		.justifyContent("centre")
		// 		.width("100%")
		// 		.maxWidth(300)
		// 		.padding(0)
		// 		.backgroundColor("green")
		// 		.borderStyle("solid")
		// 		.borderWidth(2)
		// 		.borderBottomWidth(2)
		// 		.borderColor("#ffffff")
		// 	.end()
		// 	.build();

		// Theme THM_BUTTON_DEFAULT_INPUT = Theme.builder("THM_BUTTON_DEFAULT_INPUT")
		// .addAttribute()
		// 	.size("xl")
		// 	.colour("#ffffff")
		// .end()
		// .build();

		// Theme THM_BUTTON_DEFAULT_WRAPPER = Theme.builder("THM_BUTTON_DEFAULT_WRAPPER")
		// .addAttribute()
		// 	.alignItems("center")
		// .end()
		// .build();

    Theme THM_FORM_ATTRIBUTES_GENNY = Theme.builder("THM_FORM_ATTRIBUTES_GENNY")
		.addAttribute().color("black").end()
		.addAttribute().overflowY("auto").end()
    .addAttribute().borderStyle("solid").end()
    .addAttribute().backgroundColor(project.getValue("PRI_COLOR_PRIMARY", "#233A4E")).end()
    .addAttribute().color(project.getValue("PRI_COLOR_SURFACE_ON", "white")).end()
    .addAttribute().borderColor("black").end()
		.addAttribute().borderWidth(1).end()
		.addAttribute().justifyContent("start").end()
		.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
	.end()
	.build();

		Frame3 frameForm = Frame3.builder("FRM_FORM")
				.addTheme(THM_FORM_ATTRIBUTES_GENNY).end()
					.question("QUE_INTERN_PROFILE_GRP")
						.addTheme(THM_FORM_VCL_INPUT_GENNY).vcl(VisualControlType.VCL_INPUT).weight(2.0).end()
						.addTheme(THM_FORM_VCL_LABEL_GENNY).vcl(VisualControlType.VCL_LABEL).end()
						.addTheme(THM_FORM_VCL_WRAPPER_GENNY).vcl(VisualControlType.VCL_WRAPPER).end()
						.addTheme(THM_FORM_VCL_ERROR_GENNY).vcl(VisualControlType.VCL_ERROR).end()
						.addTheme(THM_FORM_BEHAVIOUR_GENNY).weight(3.0).end()
						.addTheme(THM_FORM_GROUP_LABEL_GENNY).vcl(VisualControlType.GROUP_LABEL).weight(3.0).end()
						.addTheme(THM_BACKGROUND_NONE).weight(3.0).end()
						.addTheme(THM_FORM_GROUP_WRAPPER_GENNY).vcl(VisualControlType.GROUP_WRAPPER).weight(2.0).end()
					.end()
				.build();

		Frame3 frameCentre = Frame3.builder("FRM_CENTRE").addFrame(frameForm, FramePosition.NORTH).end()
				.build();

		Frame3 frameMain = Frame3.builder("FRM_MAIN")
				.addTheme(THM_COLOR_GREY).end()
				.addFrame(frameCentre, FramePosition.CENTRE)
				.end()
				.build();

		Frame3 frameRoot = Frame3.builder("FRM_ROOT").addFrame(frameMain).end().build();

		Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();
		System.out.println(askMsgs.size());

		QDataBaseEntityMessage msg = FrameUtils2.toMessage(frameRoot, serviceToken, askMsgs);

		/* send message */
		rules.publishCmd(msg); // Send QDataBaseEntityMessage

		System.out.println("Sending Asks");
		for (QDataAskMessage askMsg : askMsgs) {
			rules.publishCmd(askMsg, serviceToken.getUserCode(), userToken.getUserCode()); // Send associated
			// QDataAskMessage
		}

		System.out.println(askMsgs.size());
		System.out.println("Sent");
	}

	// @Test
	public void testForQuestionsList() {
		// getting the tokens
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken);
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
		String apiUrl = GennySettings.qwandaServiceUrl + "/service/forms";

		// building the themes and the footers
		Theme THM_COLOR_GREY = Theme.builder("THM_COLOR_RED").addAttribute().backgroundColor("red").end().build();

		Theme THM_COLOR_BLACK = Theme.builder("THM_COLOR_BLACK").addAttribute().backgroundColor("#ffffff").end().build();

		Theme THM_FORM_INPUT_DEFAULT = Theme.builder("THM_FORM_INPUT_DEFAULT").addAttribute().borderBottomWidth(1)
				.borderColor("#ddd").borderStyle("solid").placeholderColor("#888").end()
				.addAttribute(ThemeAttributeType.PRI_CONTENT_HOVER).borderColor("#aaa").end()
				.addAttribute(ThemeAttributeType.PRI_CONTENT_ACTIVE).borderColor("green").end()
				.addAttribute(ThemeAttributeType.PRI_CONTENT_ERROR).borderColor("red").color("red").end().build();

		Theme THM_FORM_LABEL_DEFAULT = Theme.builder("THM_FORM_LABEL_DEFAULT").addAttribute().end().build();

		Theme THM_FORM_WRAPPER_DEFAULT = Theme.builder("THM_FORM_WRAPPER_DEFAULT").addAttribute().marginBottom(10)
				.padding(10).end().addAttribute(ThemeAttributeType.PRI_CONTENT_ERROR).backgroundColor("#fc8e6").end().build();

		Theme THM_FORM_ERROR_DEFAULT = Theme.builder("THM_FORM_ERROR_DEFAULT").addAttribute().color("red").end().build();

		Theme THM_FORM_DEFAULT = Theme.builder("THM_FORM_DEFAULT").addAttribute().backgroundColor("none").end()
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_TITLE, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_DESCRIPTION, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_REQUIRED, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_ICON, true).end().build();

		Theme THM_FORM_CONTAINER_DEFAULT = Theme.builder("THM_FORM_CONTAINER_DEFAULT").addAttribute()
				.backgroundColor("white").padding(10).maxWidth(700).width("100%").shadowColor("#000").shadowOpacity(0.4)
				.shadowRadius(5).shadowOffset().width(0).height(0).end().end()
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_TITLE, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_DESCRIPTION, true).end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

		Theme THM_BUTTONS = Theme.builder("THM_BUTTONS").addAttribute().backgroundColor("#ffffff").padding(10)
				.justifyContent("center").borderColor("#000000").margin(4).maxWidth(700).width("100%").shadowColor("#000")
				.shadowOpacity(0.8).shadowRadius(5).end().build();

		Theme THM_OF = Theme.builder("THM_OF").addAttribute().overflowY("auto").end()
				.addAttribute().justifyContent("flex-start").end()
				.addAttribute().padding(10).end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

		rules.sendAllAttributes();

		System.out.println("Sent");
		try {
			String jsonFormCodes = QwandaUtils.apiGet(apiUrl, userToken.getToken());
			if (!"You need to be a test.".equals(jsonFormCodes)) {
				Type type = new TypeToken<List<String>>() {
				}.getType();
				List<String> formCodes = JsonUtils.fromJson(jsonFormCodes, type);
				System.out.println("Form Codes=" + formCodes);

				System.out.println("Before caching the formCodes");

				for (String formCode : formCodes) {
					// System.out.println(formCode);

					// String frameCode = "FRM_" + formCode;

					Frame3 frameForm = Frame3.builder("FRM_" + formCode).addTheme(THM_COLOR_BLACK).end().addTheme(THM_OF).end()
							.question(formCode).addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT).weight(2.0).end()
							.addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
							.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
							.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT)
							.weight(3.0).end().addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();

					Frame3 frameCentre = Frame3.builder("FRM_CENTRE").addFrame(frameForm, FramePosition.NORTH).end().build();

					Frame3 frameMain = Frame3.builder("FRM_MAIN").addTheme(THM_COLOR_GREY).end()
							.addFrame(frameCentre, FramePosition.CENTRE).end().build();

					Frame3 frameRoot = Frame3.builder("FRM_ROOT").addFrame(frameMain).end().build();

					Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();

					QDataBaseEntityMessage msg = FrameUtils2.toMessage(frameRoot, serviceToken, askMsgs);

					/* send message */
					rules.publishCmd(msg); // Send QDataBaseEntityMessage
					System.out.println("Sending Asks");
					for (QDataAskMessage askMsg : askMsgs) {
						rules.publishCmd(askMsg, serviceToken.getUserCode(), userToken.getUserCode()); // Send associated
						// QDataAskMessage
					}

					// we are caching the frame forms
					VertxUtils.putObject(serviceToken.getRealm(), "", frameForm.getCode(), frameForm, serviceToken.getToken());
					System.out.println(frameForm.getCode());

				}
				System.out.println("After caching the formCodes   ::  ");
				System.out.println("\n");

			}

		} catch (Exception e) {

		}

	}

	// @Test
	public void displayFormsAutomatically() {
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken);
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		rules.sendAllAttributes();

		// building the themes and the footers
		Theme THM_BLACK = Theme.builder("THM_BLACK").addAttribute().backgroundColor("yellow").end().build();

		Theme THM_WHITE = Theme.builder("THM_WHITE").addAttribute().backgroundColor("#ffffff").end().build();

		Frame3 FRM_WEST = Frame3.builder("FRM_WEST").addTheme(THM_BLACK).end().question("QUE_NAME_TWO").end().build();

		Frame3 FRM_MIDDLE = Frame3.builder("FRM_MIDDLE").addTheme(THM_WHITE).end().question("QUE_NAME_TWO").end().build();

		Frame3 FRM_EAST = Frame3.builder("FRM_EAST").addTheme(THM_BLACK).end().question("QUE_NAME_TWO").end().build();

		Frame3 frameCentre = Frame3.builder("FRM_CENTRE").addFrame(FRM_WEST, FramePosition.WEST).end()
				.addFrame(FRM_MIDDLE, FramePosition.WEST).end().addFrame(FRM_EAST, FramePosition.WEST).end().build();

		Frame3 frameMain = Frame3.builder("FRM_MAIN").addFrame(frameCentre, FramePosition.CENTRE).end().build();

		Frame3 frameRoot = Frame3.builder("FRM_ROOT").addFrame(frameMain).end().build();

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

	//@Test
	public void testForQuestionsList2() {

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

		String apiUrl = GennySettings.qwandaServiceUrl + "/service/forms";

		qRules.sendAllAttributes();

		// building the themes and the footers

		Theme THM_BLACK = Theme.builder("THM_BLACK").addAttribute().backgroundColor("yellow").end().build();

		Theme THM_COLOR_BLACK = Theme.builder("THM_COLOR_BLACK").addAttribute().backgroundColor("#ffffff").end().build();

		Theme THM_FORM_INPUT_DEFAULT = Theme.builder("THM_FORM_INPUT_DEFAULT").addAttribute().borderBottomWidth(1)
				.borderColor("#ddd").borderStyle("solid").placeholderColor("#888").end()
				.addAttribute(ThemeAttributeType.PRI_CONTENT_HOVER).borderColor("#aaa").end()
				.addAttribute(ThemeAttributeType.PRI_CONTENT_ACTIVE).borderColor("green").end()
				.addAttribute(ThemeAttributeType.PRI_CONTENT_ERROR).borderColor("red").color("red").end().build();

		Theme THM_FORM_LABEL_DEFAULT = Theme.builder("THM_FORM_LABEL_DEFAULT").addAttribute().end().build();

		Theme THM_FORM_WRAPPER_DEFAULT = Theme.builder("THM_FORM_WRAPPER_DEFAULT").addAttribute().marginBottom(10)
				.padding(10).end().addAttribute(ThemeAttributeType.PRI_CONTENT_ERROR).backgroundColor("#fc8e6").end().build();

		Theme THM_FORM_ERROR_DEFAULT = Theme.builder("THM_FORM_ERROR_DEFAULT").addAttribute().color("red").end().build();

		Theme THM_FORM_DEFAULT = Theme.builder("THM_FORM_DEFAULT").addAttribute().backgroundColor("none").end()
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_TITLE, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_DESCRIPTION, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_REQUIRED, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_ICON, true).end().build();

		Theme THM_FORM_CONTAINER_DEFAULT = Theme.builder("THM_FORM_CONTAINER_DEFAULT").addAttribute()
				.backgroundColor("white").padding(10).maxWidth(700).width("100%").shadowColor("#000").shadowOpacity(0.4)
				.shadowRadius(5).shadowOffset().width(0).height(0).end().end()
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_TITLE, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_DESCRIPTION, true).end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

				Theme THM_OF = Theme.builder("THM_OF").addAttribute().overflowY("auto").end()
				.addAttribute().justifyContent("flex-start").end()
				.addAttribute().padding(10).end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

				Theme THM_OF_ONE = Theme.builder("THM_OF_ONE").addAttribute().overflowY("auto").end()
				.addAttribute().padding(10).end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();


		System.out.println("Sent");
		try {

			String jsonFormCodes = QwandaUtils.apiGet(apiUrl, userToken.getToken());
				Type type = new TypeToken<List<String>>() {
				}.getType();
				List<String> formCodes = JsonUtils.fromJson(jsonFormCodes, type);

				System.out.println("Before caching the formCodes");

				System.out.println("Form Codes=" + formCodes);

				Frame3 FRM_WEST = Frame3.builder("FRM_WEST").addTheme(THM_BLACK).end().question("QUE_NAME_TWO").end().build();

				// Frame3 frameCentre = Frame3.builder("FRM_CENTRE")
				// .addFrame(FRM_WEST, FramePosition.WEST).end()
				// .build();
				Frame3.Builder frameFormTestBuilder = Frame3.builder("FRM_FORM_TEST")
				.addFrame(FRM_WEST, FramePosition.WEST).end();
			
			//	FrameUtils2.toMessage(frameForm, serviceToken);
			//int index =0;
				for (String formCode : formCodes) {
					//index++;
					Frame3 frameForm = Frame3.builder("FRM_" + formCode).addTheme(THM_COLOR_BLACK).end().addTheme(THM_OF_ONE).end()
							.question(formCode).addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT).weight(2.0).end()
							.addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
							.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
							.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT)
							.weight(3.0).end().addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();
				
							// we are caching the frame forms
						VertxUtils.putObject(serviceToken.getRealm(), "", frameForm.getCode(), frameForm, serviceToken.getToken());
						System.out.println(frameForm.getCode());

						frameFormTestBuilder.addFrame(frameForm, FramePosition.NORTH).end();

						// if(index >5){
						// 	break;
						// }
				}

				
			Frame3 formsFrame = frameFormTestBuilder.build();
			Frame3 frameMain = Frame3.builder("FRM_MAIN").addTheme(THM_OF).end()
													.addFrame(formsFrame, FramePosition.CENTRE).end().build();
	
			Frame3 frameRoot = Frame3.builder("FRM_ROOT").addFrame(frameMain).end().build();

			Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();
			QDataBaseEntityMessage msg = FrameUtils2.toMessage(frameRoot, serviceToken, askMsgs);
			qRules.publishCmd(msg);
			for (QDataAskMessage askMsg : askMsgs) {
				qRules.publishCmd(askMsg, serviceToken.getUserCode(), userToken.getUserCode());
			}

			// System.out.println("Sent");
			// System.out.println("\n");
			// System.out.println("After caching the formCodes   ::  ");
			// System.out.println("Form Codes=" + formCodes);

		} catch (Exception e) {

		}

	}

}