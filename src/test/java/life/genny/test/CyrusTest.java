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

	// @Test
	public void addInternshipOneAgent() {
		// getting the tokens
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken);
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		// building the themes and the footers
		Theme THM_COLOR_GREY = Theme.builder("THM_COLOR_RED").addAttribute().backgroundColor("red").end().build();

		Theme THM_SUBMIT = Theme.builder("THM_SUBMIT").addAttribute().backgroundColor("#1183c8").end().addAttribute()
				.justifyContent("center").end().build();

		Theme THM_CANCEL = Theme.builder("THM_CANCEL").addAttribute().backgroundColor("#C0C0C0").end().build();

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
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();
		/*
		 * Frame3 frameSubmit = Frame3.builder("FRM_SUBMIT") .addTheme(THM_SUBMIT).end()
		 * .question("QUE_TABLE_NEXT_BTN") .addTheme(THM_BUTTONS).weight(2.0) .end()
		 * .end() .build();
		 * 
		 * Frame3 frameCancel = Frame3.builder("FRM_CANCEL") .addTheme(THM_CANCEL).end()
		 * .question("QUE_CANCEL_BUTTON") .addTheme(THM_BUTTONS) .weight(2.0) .end()
		 * .end() .build();
		 */
		Frame3 frameForm = Frame3.builder("FRM_FORM").addTheme(THM_COLOR_BLACK).end().addTheme(THM_OF).end()
				.question("QUE_ADD_INTERNSHIP_AGENT_STEP_ONE_GRP").addTheme(THM_FORM_INPUT_DEFAULT)
				.vcl(VisualControlType.VCL_INPUT).weight(2.0).end().addTheme(THM_FORM_LABEL_DEFAULT)
				.vcl(VisualControlType.VCL_LABEL).end().addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER)
				.end().addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT)
				.weight(3.0).end().addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();

		Frame3 frameCentre = Frame3.builder("FRM_CENTRE").addFrame(frameForm, FramePosition.NORTH).end()
				// .addFrame(frameSubmit, FramePosition.NORTH).end()
				// .addFrame(frameCancel, FramePosition.NORTH).end()
				.build();

		/*
		 * Frame3 frameButtons = Frame3.builder("FRM_WEST") .addFrame(frameSubmit,
		 * FramePosition.WEST).end() .addFrame(frameCancel, FramePosition.WEST).end()
		 * .build();
		 */

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

		System.out.println("Sent");
	}

	// @Test
	public void addInternshipTwoAgent() {
		// getting the tokens
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken);
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

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
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

		Frame3 frameForm = Frame3.builder("FRM_FORM").addTheme(THM_COLOR_BLACK).end().addTheme(THM_OF).end()
				.question("QUE_ADD_INTERNSHIP_AGENT_STEP_TWO_GRP").addTheme(THM_FORM_INPUT_DEFAULT)
				.vcl(VisualControlType.VCL_INPUT).weight(2.0).end().addTheme(THM_FORM_LABEL_DEFAULT)
				.vcl(VisualControlType.VCL_LABEL).end().addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER)
				.end().addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT)
				.weight(3.0).end().addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();

		Frame3 frameCentre = Frame3.builder("FRM_CENTRE").addFrame(frameForm, FramePosition.NORTH).end()

				.build();

		Frame3 frameMain = Frame3.builder("FRM_MAIN").addTheme(THM_COLOR_GREY).end()
				.addFrame(frameCentre, FramePosition.CENTRE).end().build();

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
	public void selectUserType() {
		// getting the tokens
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken);
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

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
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

		Frame3 frameForm = Frame3.builder("FRM_FORM").addTheme(THM_COLOR_BLACK).end().addTheme(THM_OF).end()
				.question("QUE_SELECT_USER_TYPE_GRP").addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT)
				.weight(2.0).end().addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end().addTheme(THM_FORM_ERROR_DEFAULT)
				.vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT).weight(3.0).end()
				.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();

		Frame3 frameCentre = Frame3.builder("FRM_CENTRE").addFrame(frameForm, FramePosition.NORTH).end()

				.build();

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

		System.out.println("Sent");
	}

	// @Test
	public void agentProfileDetails() {
		// getting the tokens
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken);
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

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
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

		Frame3 frameForm = Frame3.builder("FRM_FORM").addTheme(THM_COLOR_BLACK).end().addTheme(THM_OF).end()
				.question("QUE_AGENT_PROFILE_GRP").addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT).weight(2.0)
				.end().addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end().addTheme(THM_FORM_ERROR_DEFAULT)
				.vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT).weight(3.0).end()
				.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();

		Frame3 frameCentre = Frame3.builder("FRM_CENTRE").addFrame(frameForm, FramePosition.NORTH).end()

				.build();

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

		System.out.println("Sent");
	}

	// @Test
	public void addInternshipTemplate() {
		// getting the tokens
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken);
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

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

		Theme THM_OF = Theme.builder("THM_OF").addAttribute().overflowY("auto").end().addAttribute()
				.justifyContent("flex-start").end().addAttribute().padding(10).end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

		Frame3 frameForm = Frame3.builder("FRM_FORM").addTheme(THM_COLOR_BLACK).end().addTheme(THM_OF).end()
				.question("QUE_ADD_INTERNSHIP_TEMPLATE_AGENT_GRP").addTheme(THM_FORM_INPUT_DEFAULT)
				.vcl(VisualControlType.VCL_INPUT).weight(2.0).end().addTheme(THM_FORM_LABEL_DEFAULT)
				.vcl(VisualControlType.VCL_LABEL).end().addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER)
				.end().addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT)
				.weight(3.0).end().addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();

		Frame3 frameCentre = Frame3.builder("FRM_CENTRE").addFrame(frameForm, FramePosition.NORTH).end().build();

		Frame3 frameMain = Frame3.builder("FRM_MAIN").addTheme(THM_COLOR_GREY).end()
				.addFrame(frameCentre, FramePosition.CENTRE).end().build();

		Frame3 frameRoot = Frame3.builder("FRM_ROOT").addFrame(frameMain).end().build();

		Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();

		QDataBaseEntityMessage msg = FrameUtils2.toMessage(frameRoot, serviceToken, askMsgs);

		rules.sendAllAttributes();
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
	public void agentDetais() {
		// getting the tokens
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken);
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

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
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

		Frame3 frameForm = Frame3.builder("FRM_FORM").addTheme(THM_COLOR_BLACK).end().addTheme(THM_OF).end()
				.question("QUE_ADD_AGENT_GRP").addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT).weight(2.0)
				.end().addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end().addTheme(THM_FORM_ERROR_DEFAULT)
				.vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT).weight(3.0).end()
				.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();

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

		System.out.println("Sent");
	}

	// @Test
	public void addEduProviderStaffProfile() {
		// getting the tokens
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken);
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

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
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

		Frame3 frameForm = Frame3.builder("FRM_FORM").addTheme(THM_COLOR_BLACK).end().addTheme(THM_OF).end()
				.question("QUE_ADD_EDU_PROVIDER_STAFF_GRP").addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT)
				.weight(2.0).end().addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end().addTheme(THM_FORM_ERROR_DEFAULT)
				.vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT).weight(3.0).end()
				.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();

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

		System.out.println("Sent");
	}

	// @Test
	public void addHostCompanyStaffDetails() {
		// getting the tokens
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken);
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

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
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

		Frame3 frameForm = Frame3.builder("FRM_FORM").addTheme(THM_COLOR_BLACK).end().addTheme(THM_OF).end()
				.question("QUE_ADD_HOST_COMPANY_STAFF_GRP").addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT)
				.weight(2.0).end().addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end().addTheme(THM_FORM_ERROR_DEFAULT)
				.vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT).weight(3.0).end()
				.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();

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

		System.out.println("Sent");
	}

	// @Test
	public void addinternProfile() {
		// getting the tokens
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken);
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

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
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

		Frame3 frameForm = Frame3.builder("FRM_FORM").addTheme(THM_COLOR_BLACK).end().addTheme(THM_OF).end()
				.question("QUE_ADD_INTERN_GRP").addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT).weight(2.0)
				.end().addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end().addTheme(THM_FORM_ERROR_DEFAULT)
				.vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT).weight(3.0).end()
				.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();

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

		System.out.println("Sent");
	}

	// @Test
	public void selectCompanyType() {
		// getting the tokens
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken);
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

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
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

		Frame3 frameForm = Frame3.builder("FRM_FORM").addTheme(THM_COLOR_BLACK).end().addTheme(THM_OF).end()
				.question("QUE_SELECT_COMPANY_TYPE_GRP").addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT)
				.weight(2.0).end().addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end().addTheme(THM_FORM_ERROR_DEFAULT)
				.vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT).weight(3.0).end()
				.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();

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

		System.out.println("Sent");
	}

	// @Test
	public void agencyDetails() {
		// getting the tokens
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken);
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

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
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

		Frame3 frameForm = Frame3.builder("FRM_FORM").addTheme(THM_COLOR_BLACK).end().addTheme(THM_OF).end()
				.question("QUE_ADD_AGENCY_GRP").addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT).weight(2.0)
				.end().addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end().addTheme(THM_FORM_ERROR_DEFAULT)
				.vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT).weight(3.0).end()
				.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();

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

		System.out.println("Sent");
	}

	// @Test
	public void educationProviderDetails() {
		// getting the tokens
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken);
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

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
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

		Frame3 frameForm = Frame3.builder("FRM_FORM").addTheme(THM_COLOR_BLACK).end().addTheme(THM_OF).end()
				.question("QUE_ADD_EDU_PROVIDER_GRP").addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT)
				.weight(2.0).end().addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end().addTheme(THM_FORM_ERROR_DEFAULT)
				.vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT).weight(3.0).end()
				.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();

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

		System.out.println("Sent");
	}

	// @Test
	public void hostCompanyDetails() {
		// getting the tokens
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken);
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

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
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

		Frame3 frameForm = Frame3.builder("FRM_FORM").addTheme(THM_COLOR_BLACK).end().addTheme(THM_OF).end()
				.question("QUE_ADD_HOST_COMPANY_GRP").addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT)
				.weight(2.0).end().addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end().addTheme(THM_FORM_ERROR_DEFAULT)
				.vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT).weight(3.0).end()
				.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();

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

		System.out.println("Sent");
	}

	// @Test
	public void eduProviderStaffProfileDetails() {
		// getting the tokens
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken);
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

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
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

		Frame3 frameForm = Frame3.builder("FRM_FORM").addTheme(THM_COLOR_BLACK).end().addTheme(THM_OF).end()
				.question("QUE_EDU_PROVIDER_STAFF_PROFILE_GRP").addTheme(THM_FORM_INPUT_DEFAULT)
				.vcl(VisualControlType.VCL_INPUT).weight(2.0).end().addTheme(THM_FORM_LABEL_DEFAULT)
				.vcl(VisualControlType.VCL_LABEL).end().addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER)
				.end().addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT)
				.weight(3.0).end().addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();

		Frame3 frameCentre = Frame3.builder("FRM_CENTRE").addFrame(frameForm, FramePosition.NORTH).end()

				.build();

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

		System.out.println("Sent");
	}

	// @Test
	public void addStaff() {
		// getting the tokens
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken);
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

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
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

		Frame3 frameForm = Frame3.builder("FRM_FORM").addTheme(THM_COLOR_BLACK).end().addTheme(THM_OF).end()
				.question("QUE_ADD_STAFF_GRP").addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT).weight(2.0)
				.end().addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end().addTheme(THM_FORM_ERROR_DEFAULT)
				.vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT).weight(3.0).end()
				.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();

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

		System.out.println("Sent");
	}

	// @Test
	public void eduProviderStaffAddStudent() {
		// getting the tokens
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken);
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

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
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

		Frame3 frameForm = Frame3.builder("FRM_FORM").addTheme(THM_COLOR_BLACK).end().addTheme(THM_OF).end()
				.question("QUE_ADD_STUDENT_GRP").addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT).weight(2.0)
				.end().addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end().addTheme(THM_FORM_ERROR_DEFAULT)
				.vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT).weight(3.0).end()
				.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();

		Frame3 frameCentre = Frame3.builder("FRM_CENTRE").addFrame(frameForm, FramePosition.NORTH).end()

				.build();

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

		System.out.println("Sent");
	}

	// @Test
	public void internProfile() {
		// getting the tokens
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken);
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

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
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

		Frame3 frameForm = Frame3.builder("FRM_FORM").addTheme(THM_COLOR_BLACK).end().addTheme(THM_OF).end()
				.question("QUE_INTERN_PROFILE_GRP").addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT)
				.weight(2.0).end().addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end().addTheme(THM_FORM_ERROR_DEFAULT)
				.vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT).weight(3.0).end()
				.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();

		Frame3 frameCentre = Frame3.builder("FRM_CENTRE").addFrame(frameForm, FramePosition.NORTH).end()

				.build();

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

		System.out.println("Sent");
	}

	// @Test
	public void hostCompanyStaffProfileDetails() {
		// getting the tokens
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken);
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

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
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

		Frame3 frameForm = Frame3.builder("FRM_FORM").addTheme(THM_COLOR_BLACK).end().addTheme(THM_OF).end()
				.question("QUE_HOST_COMPANY_STAFF_PROFILE_GRP").addTheme(THM_FORM_INPUT_DEFAULT)
				.vcl(VisualControlType.VCL_INPUT).weight(2.0).end().addTheme(THM_FORM_LABEL_DEFAULT)
				.vcl(VisualControlType.VCL_LABEL).end().addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER)
				.end().addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT)
				.weight(3.0).end().addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();

		Frame3 frameCentre = Frame3.builder("FRM_CENTRE").addFrame(frameForm, FramePosition.NORTH).end()

				.build();

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

		System.out.println("Sent");
	}

	// @Test
	public void addInternshipOneHostCompanyStaff() {
		// getting the tokens
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken);
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

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
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

		Frame3 frameForm = Frame3.builder("FRM_FORM").addTheme(THM_COLOR_BLACK).end().addTheme(THM_OF).end()
				.question("QUE_ADD_INTERNSHIP_STEP_ONE_GRP").addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT)
				.weight(2.0).end().addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end().addTheme(THM_FORM_ERROR_DEFAULT)
				.vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT).weight(3.0).end()
				.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();

		Frame3 frameCentre = Frame3.builder("FRM_CENTRE").addFrame(frameForm, FramePosition.NORTH).end()

				.build();

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

		System.out.println("Sent");
	}

	// @Test
	public void addInternshipTwoHostCompanyStaff() {
		// getting the tokens
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken);
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

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
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

		Frame3 frameForm = Frame3.builder("FRM_FORM").addTheme(THM_COLOR_BLACK).end().addTheme(THM_OF).end()
				.question("QUE_ADD_INTERNSHIP_STEP_TWO_GRP").addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT)
				.weight(2.0).end().addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end().addTheme(THM_FORM_ERROR_DEFAULT)
				.vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT).weight(3.0).end()
				.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();

		Frame3 frameCentre = Frame3.builder("FRM_CENTRE").addFrame(frameForm, FramePosition.NORTH).end()

				.build();

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

		System.out.println("Sent");
	}

	// @Test
	public void test() {
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
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

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

					/* we are caching the frame forms */
					VertxUtils.putObject(serviceToken.getRealm(), "", frameForm.getCode(), frameForm, serviceToken.getToken());
					System.out.println(frameForm.getCode());

				}
				System.out.println("After caching the formCodes   ::  ");
				System.out.println("\n");

				for (String formCode : formCodes) {
					/* we are getting the cached frame forms */
					Frame3 cachedFrameForm = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_" + formCode, Frame3.class,
							serviceToken.getToken());

					System.out.println(cachedFrameForm.getCode());

				}

			} else {
				System.out.println("Ensure that the user you are using has a 'test' role ...");
			}

		} catch (Exception e) {

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
			gks.injectSignal("inputSignal", userToken);
			// gks.getKieSession().signalEvent(type, event);
			gks.advanceSeconds(20, false);

			System.out.println("Sent");

		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			gks.close();
		}
	}

	// @Test
	public void userSessionTest() {
		System.out.println("Show UserSession");
		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage msg = new QEventMessage("EVT_MSG", "AUTH_INIT");

		GennyKieSession gks = null;
		try {
			gks = GennyKieSession.builder(serviceToken, false)
					// .addDrl("FRM_FORM2.drl")
					.addDrl("FRM_FORM.drl").addDrl("GenerateThemes")
					// .addJbpm("test_session_2.bpmn")
					.addToken(userToken).build();

			// for (int i=0;i<2;i++) {
			// gks.advanceSeconds(2, true);
			// gks.injectSignal("inputSignal", "Hello");
			// gks.advanceSeconds(2, true);
			// gks.injectSignal("inputSignal2", "Hello");
			// }

			for (int i = 0; i < 2; i++) {
				gks.displayForm("FRM_FORM", userToken);
				gks.advanceSeconds(2, true);
				gks.displayForm("FRM_FORM", userToken);
				gks.advanceSeconds(2, true);
			}
			// gks.sendLogout(userToken);
			System.out.println("Sent");

		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			gks.close();
		}
	}

	public Frame3 generateCards(String count) {
		GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
		GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
		QRules rules = new QRules(eventBusMock, userToken.getToken(), userToken.getAdecodedTokenMap());
		rules.set("realm", userToken.getRealm());
		rules.setServiceToken(serviceToken.getToken());

		/* create themes */

		Theme THM_FORM_LABEL_DEFAULT = Theme.builder("THM_FORM_LABEL_DEFAULT").addAttribute().backgroundColor("white").end()
				.build();

		Theme THM_CARD_DEFAULT = Theme.builder("THM_CARD_DEFAULT").addAttribute().backgroundColor("none").end()
				// .addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true).end()
				// .addAttribute(ThemeAttributeType.PRI_HAS_INPUT, false).end()
				.build();

		Theme THM_CARD = Theme.builder("THM_CARD").addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
				.addAttribute()
				// .width(300)
				.height(200)
				// .backgroundColor("white")
				// .backgroundColor("pink")
				.backgroundColor("#F8F9FA").width("100%").shadowColor("#000").shadowOpacity(0.4).shadowRadius(5).shadowOffset()
				.width(0).height(0).end().end().build();

		Theme THM_CARD_LEFT = Theme.builder("THM_CARD_LEFT").addAttribute().backgroundColor("#EBECF0").end().build();
		Theme THM_CARD_CENTRE = Theme.builder("THM_CARD_CENTRE").addAttribute().backgroundColor("#FFFFFF").end().build();
		Theme THM_CARD_RIGHT = Theme.builder("THM_CARD_RIGHT").addAttribute().backgroundColor("#E1E2E1").end().build();

		/* create frames for each question */
		Frame3 frameName = Frame3.builder("FRM_NAME").question("QUE_NAME_TWO").addTheme(THM_CARD_DEFAULT)
				.vcl(VisualControlType.VCL_LABEL).end().end().build();
		Frame3 frameEmail = Frame3.builder("FRM_EMAIL").question("QUE_EMAIL").addTheme(THM_CARD_DEFAULT)
				.vcl(VisualControlType.VCL_LABEL).end().end().build();
		Frame3 frameMobile = Frame3.builder("FRM_MOBILE").question("QUE_MOBILE").addTheme(THM_CARD_DEFAULT)
				.vcl(VisualControlType.VCL_LABEL).end().end().build();
		Frame3 frameDob = Frame3.builder("FRM_DOB").question("QUE_DOB").addTheme(THM_CARD_DEFAULT)
				.vcl(VisualControlType.VCL_LABEL).end().end().build();

		/* build left, center, card frame-card */

		Frame3 frameLeftCard = Frame3.builder("FRM_CARD_LEFT").addTheme(THM_CARD_LEFT).end().question("QUE_IMAGE").end()
				.build();
		Frame3 frameCentreCard = Frame3.builder("FRM_CARD_CENTRE").addTheme(THM_CARD_CENTRE).end()
				// .question("QUE_NAME_TWO")
				.addFrame(frameName, FramePosition.NORTH).end().addFrame(frameEmail, FramePosition.NORTH).end()
				.addFrame(frameMobile, FramePosition.NORTH).end().addFrame(frameDob, FramePosition.NORTH).end()

				.build();
		Frame3 frameRightCard = Frame3.builder("FRM_CARD_RIGHT").addTheme(THM_CARD_RIGHT).end().question("QUE_NAME_TWO")
				.end().build();

		Theme THM_TEST1 = Theme.builder("THM_TEST1").addAttribute().backgroundColor("none").width("100%").height(70).end()
				.build();

		/* create frames */
		Frame3 frameCard = Frame3.builder("FRM_CARD" + "_" + count).addTheme(THM_CARD).end().question("QUE_NAME_TWO")
				.addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end().end()
				.addFrame(frameLeftCard, FramePosition.WEST).end().addFrame(frameCentreCard, FramePosition.WEST).end()
				// .addFrame(frameRightCard, FramePosition.WEST).end()
				.build();

		return frameCard;

	}

	// @Test
	public void testDesktop() {
		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		try {
			rules.sendAllAttributes();
			/* frame-tabs */
			Frame3 FRM_TABS = generateTabs();

			/* frame-header */
			Frame3 FRM_HEADER = generateHeader();

			/* frame-footer */
			Frame3 FRM_FOOTER = generateFooter();

			/* frame-sidebar */
			Frame3 FRM_SIDEBAR = generateSidebar();

			Frame3 FRM_FORM = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_FORM", Frame3.class,
					serviceToken.getToken());

			System.out.println(FRM_FORM.getCode());

			/* frame-root */
			Frame3 FRM_ROOT = Frame3.builder("FRM_ROOT").addFrame(FRM_HEADER, FramePosition.NORTH).end()
					.addFrame(FRM_SIDEBAR, FramePosition.WEST).end().addFrame(FRM_FORM, FramePosition.CENTRE).end()
					.addFrame(FRM_FOOTER, FramePosition.SOUTH).end().build();

			Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();
			QDataBaseEntityMessage msg = FrameUtils2.toMessage(FRM_ROOT, serviceToken, askMsgs);
			rules.publishCmd(msg);
			for (QDataAskMessage askMsg : askMsgs) {
				rules.publishCmd(askMsg, serviceToken.getUserCode(), userToken.getUserCode());
			}
			System.out.println("Sent");
		} catch (Exception e) {
			System.out.println("Errorsss " + e.getLocalizedMessage());
		}
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

	public void testCachedDesktop() {
		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		GennyKieSession gks = null;

		try {
			gks = GennyKieSession.builder(serviceToken, false).addToken(userToken).build();
			gks.start();

			gks.displayForm("FRM_DESKTOP2", userToken);

			System.out.println("Sent");

		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			gks.close();
		}

		// try {
		// /* frame-tabs */
		// Frame3 FRM_TABS = VertxUtils.getObject(serviceToken.getRealm(), "",
		// "FRM_TABS", Frame3.class,
		// serviceToken.getToken());
		// System.out.print("desktop-tabs :: " + FRM_TABS.getCode());
		//
		// /* frame-header */
		// Frame3 FRM_HEADER = VertxUtils.getObject(serviceToken.getRealm(), "",
		// "FRM_HEADER",
		// Frame3.class, serviceToken.getToken());
		// System.out.print("desktop-header :: " + FRM_HEADER.getCode());
		//
		// /* frame-footer */
		// Frame3 FRM_FOOTER = VertxUtils.getObject(serviceToken.getRealm(), "",
		// "FRM_FOOTER",
		// Frame3.class, serviceToken.getToken());
		// System.out.print("desktop-footer :: " + FRM_FOOTER.getCode());
		//
		// /* frame-sidebar */
		// Frame3 FRM_SIDEBAR = VertxUtils.getObject(serviceToken.getRealm(), "",
		// "FRM_SIDEBAR",
		// Frame3.class, serviceToken.getToken());
		//
		// System.out.print("desktop-sidebar :: " + FRM_SIDEBAR.getCode());
		// /* frame-root */
		// Frame3 FRM_DESKTOP = VertxUtils.getObject(serviceToken.getRealm(), "",
		// "FRM_DESKTOP",
		// Frame3.class, serviceToken.getToken());
		//
		// System.out.print("desktop-frame :: " + FRM_DESKTOP.getCode());
		//
		// Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();
		// QDataBaseEntityMessage msg = FrameUtils2.toMessage(FRM_DESKTOP, serviceToken,
		// askMsgs);
		// rules.publishCmd(msg);
		// for (QDataAskMessage askMsg : askMsgs) {
		// rules.publishCmd(askMsg, serviceToken.getUserCode(),
		// userToken.getUserCode());
		// }
		// System.out.println("Sent");
		// } catch (Exception e) {
		// System.out.println("Error " + e.getLocalizedMessage());
		// }
	}

	public Frame3 generateHeader() {
		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
		try {

			Frame3 FRM_DUMMY = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_DUMMY", Frame3.class,
					serviceToken.getToken());

			Theme THM_TABLE_ACTIONS_VISUAL_CONTROL = Theme.builder("THM_TABLE_ACTIONS_VISUAL_CONTROL")
					.addAttribute(ThemeAttributeType.PRI_HAS_LABEL, false).end()
					.addAttribute(ThemeAttributeType.PRI_HAS_ICON, true).end()
					.addAttribute(ThemeAttributeType.PRI_HAS_INPUT, false).end()
					.addAttribute(ThemeAttributeType.PRI_IS_EXPANDABLE, true).end()
					.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_TITLE, true).end()
					.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

			Frame3 FRM_PROJECT = Frame3.builder("FRM_PROJECT").addTheme("THM_PROJECT", serviceToken).end()
					.question("QUE_NAME_TWO").addTheme("THM_FORM_LABEL_DEFAULT", serviceToken).vcl(VisualControlType.VCL_LABEL)
					.end().end().addFrame(FRM_DUMMY).end().build();

			Frame3 FRM_HEADER_OPTIONS = Frame3.builder("FRM_HEADER_OPTIONS").question("QUE_OPTIONS_GRP")
					.addTheme(THM_TABLE_ACTIONS_VISUAL_CONTROL).end().end().build();

			Frame3 FRM_HEADER = Frame3.builder("FRM_HEADER").addTheme("THM_HEADER", serviceToken).end().addFrame(FRM_DUMMY)
					.end().addFrame(FRM_PROJECT).end().addFrame(FRM_HEADER_OPTIONS, FramePosition.EAST).end().build();

			System.out.println("Generated Header Frame");
			return FRM_HEADER;

		} catch (Exception e) {
			System.out.println("Error " + e.getLocalizedMessage());
		}
		return null;
	}

	public Frame3 generateSidebar() {
		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		try {
			Frame3 FRM_DUMMY = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_DUMMY", Frame3.class,
					serviceToken.getToken());

			Frame3 FRM_LOGO = Frame3.builder("FRM_LOGO").addTheme("THM_SIDEBAR", serviceToken).end()
					.question("QUE_PROJECT_SIDEBAR_GRP").addTheme("THM_LOGO", serviceToken).vcl(VisualControlType.VCL_WRAPPER)
					.end().end().addFrame(FRM_DUMMY).end().build();

			Frame3 FRM_SIDEBAR = Frame3.builder("FRM_SIDEBAR").addTheme("THM_SIDEBAR", serviceToken).end()
					.addFrame(FRM_LOGO, FramePosition.NORTH).end().addFrame(FRM_DUMMY).end().build();

			System.out.println("Generated Sidebar Frame");
			return FRM_SIDEBAR;
		} catch (Exception e) {
			System.out.println("Error " + e.getLocalizedMessage());
		}
		return null;
	}

	public Frame3 generateFooter() {
		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		try {

			Frame3 FRM_DUMMY = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_DUMMY", Frame3.class,
					serviceToken.getToken());
			Frame3 FRM_POWERED_BY = Frame3.builder("FRM_POWERED_BY").addTheme("THM_WIDTH_200", serviceToken).end()
					.addTheme("THM_COLOR_WHITE", serviceToken).end().question("QUE_POWERED_BY_GRP")
					.addTheme("THM_FORM_LABEL_DEFAULT", serviceToken).vcl(VisualControlType.VCL_LABEL).end()
					.addTheme("THM_FORM_DEFAULT_REPLICA", serviceToken).weight(3.0).end().end().build();

			Frame3 FRM_FOOTER = Frame3.builder("FRM_FOOTER").addTheme("THM_FOOTER", serviceToken).end().addFrame(FRM_DUMMY)
					.end().addFrame(FRM_POWERED_BY, FramePosition.EAST).end().build();

			System.out.println("Generated Footer Frame");
			return FRM_FOOTER;
		} catch (Exception e) {
			System.out.println("Error :: " + e.getLocalizedMessage());
		}
		return null;
	}

	public Frame3 generateBucket() {

		Theme THM_FORM_INPUT_DEFAULT = Theme.builder("THM_FORM_INPUT_DEFAULT").addAttribute().borderBottomWidth(5)
				.color("black").borderColor("#ddd").borderStyle("solid").placeholderColor("#888").height(70).end().build();

		Theme THM_BUCKET_LABEL = Theme.builder("THM_BUCKET_LABEL").addAttribute().textAlign("center").bold(true).end()
				.build();

		Theme THM_BUCKET = Theme.builder("THM_BUCKET").addAttribute().backgroundColor("#F8F9FA").overflowX("auto")
				.overflowY("auto").width("100%").end().addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

		Theme THM_BUCKET_COLUMN = Theme.builder("THM_BUCKET_COLUMN").addAttribute().backgroundColor("#EAEAEA").minWidth(300)
				.width("100%").margin(20).textAlign("center").flexDirection("column").end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

		Theme THM_TEST1 = Theme.builder("THM_TEST1").addAttribute().backgroundColor("none").width("100%").height(70).end()
				.build();

		Frame3 available = Frame3.builder("FRM_GRP_AVAILABLE").addTheme(THM_TEST1).end().question("QUE_GRP_NOT_APPLIED")
				.addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT).weight(2.0).end().end()
				.addTheme(THM_BUCKET_LABEL).end().build();
		Frame3 applied = Frame3.builder("FRM_GRP_APPLIED").addTheme(THM_TEST1).end().question("QUE_GRP_APPLIED")
				.addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT).weight(2.0).end().end()
				.addTheme(THM_BUCKET_LABEL).end().build();
		Frame3 shortlisted = Frame3.builder("FRM_GRP_SHORTLISTED").addTheme(THM_TEST1).end().question("QUE_GRP_SHORTLISTED")
				.addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT).weight(2.0).end().end()
				.addTheme(THM_BUCKET_LABEL).end().build();
		Frame3 interviews = Frame3.builder("FRM_GRP_INTERVIEWS").addTheme(THM_TEST1).end().question("QUE_GRP_INTERVIEWS")
				.addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT).weight(2.0).end().end()
				.addTheme(THM_BUCKET_LABEL).end().build();
		Frame3 offered = Frame3.builder("FRM_GRP_OFFERED").addTheme(THM_TEST1).end().question("QUE_GRP_OFFERED")
				.addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT).weight(2.0).end().end()
				.addTheme(THM_BUCKET_LABEL).end().build();
		Frame3 placed = Frame3.builder("FRM_GRP_PLACED").addTheme(THM_TEST1).end().question("QUE_GRP_PLACED")
				.addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT).weight(2.0).end().end()
				.addTheme(THM_BUCKET_LABEL).end().build();
		Frame3 inProgress = Frame3.builder("FRM_GRP_IN_PROGRESS").addTheme(THM_TEST1).end().question("QUE_GRP_IN_PROGRESS")
				.addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT).weight(2.0).end().end()
				.addTheme(THM_BUCKET_LABEL).end().build();

		Theme THM_GREEN = Theme.builder("THM_GREEN").addAttribute().backgroundColor("green").end().build();

		Frame3 frameCard1 = generateCards("1");
		Frame3 frameCard2 = generateCards("2");
		Frame3 frameCard3 = generateCards("3");
		Frame3 frameCard4 = generateCards("4");
		Frame3 frameCard5 = generateCards("5");
		Frame3 frameCard6 = generateCards("6");
		Frame3 frameCard7 = generateCards("7");
		Frame3 frameCard8 = generateCards("8");
		Frame3 frameCard9 = generateCards("9");
		Frame3 frameCard10 = generateCards("1");
		Frame3 frameCard11 = generateCards("1");
		Frame3 frameCard12 = generateCards("2");
		Frame3 frameCard13 = generateCards("3");
		Frame3 frameCard14 = generateCards("4");
		Frame3 frameCard15 = generateCards("5");
		Frame3 frameCard16 = generateCards("6");
		Frame3 frameCard17 = generateCards("7");
		Frame3 frameCard18 = generateCards("8");
		Frame3 frameCard19 = generateCards("9");

		Frame3 card = Frame3.builder("FRM_CARD_TEST").addTheme(THM_TEST1).end().addTheme(THM_GREEN).end()
				.question("QUE_GRP_NOT_APPLIED").addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT).weight(2.0)
				.end().end().addTheme(THM_BUCKET_LABEL).end().build();

		Frame3 bucketColumn1 = Frame3.builder("FRM_BUCKET_COLUMN_ONE").addTheme(THM_BUCKET_COLUMN).end()
				.addFrame(available, FramePosition.NORTH).end()
				// .addFrame(card, FramePosition.NORTH).end()
				.addFrame(frameCard1, FramePosition.NORTH).end().addFrame(frameCard2, FramePosition.NORTH).end()
				.addFrame(frameCard3, FramePosition.NORTH).end().addFrame(frameCard4, FramePosition.NORTH).end()
				.addFrame(frameCard5, FramePosition.NORTH).end().addFrame(frameCard6, FramePosition.NORTH).end()
				.addFrame(frameCard7, FramePosition.NORTH).end().addFrame(frameCard8, FramePosition.NORTH).end()
				.addFrame(frameCard9, FramePosition.NORTH).end()

				// .addFrame(frameCard2, FramePosition.NORTH).end()
				.build();
		Frame3 bucketColumn2 = Frame3.builder("FRM_BUCKET_COLUMN_TWO").addTheme(THM_BUCKET_COLUMN).end()
				.addFrame(applied, FramePosition.NORTH).end().build();
		Frame3 bucketColumn3 = Frame3.builder("FRM_BUCKET_COLUMN_THREE").addTheme(THM_BUCKET_COLUMN).end()
				.addFrame(shortlisted, FramePosition.NORTH).end().build();
		Frame3 bucketColumn4 = Frame3.builder("FRM_BUCKET_COLUMN_FOUR").addTheme(THM_BUCKET_COLUMN).end()
				.addFrame(interviews, FramePosition.NORTH).end().addFrame(frameCard1, FramePosition.NORTH).end()
				.addFrame(frameCard2, FramePosition.NORTH).end().addFrame(frameCard3, FramePosition.NORTH).end()
				.addFrame(frameCard4, FramePosition.NORTH).end().addFrame(frameCard5, FramePosition.NORTH).end()
				.addFrame(frameCard6, FramePosition.NORTH).end().addFrame(frameCard7, FramePosition.NORTH).end()
				.addFrame(frameCard8, FramePosition.NORTH).end().addFrame(frameCard9, FramePosition.NORTH).end()
				.addFrame(frameCard10, FramePosition.NORTH).end().addFrame(frameCard11, FramePosition.NORTH).end()
				.addFrame(frameCard12, FramePosition.NORTH).end().addFrame(frameCard13, FramePosition.NORTH).end()
				.addFrame(frameCard14, FramePosition.NORTH).end().addFrame(frameCard15, FramePosition.NORTH).end()
				.addFrame(frameCard16, FramePosition.NORTH).end().addFrame(frameCard17, FramePosition.NORTH).end()
				.addFrame(frameCard18, FramePosition.NORTH).end().addFrame(frameCard19, FramePosition.NORTH).end().build();

		Frame3 bucketColumn5 = Frame3.builder("FRM_BUCKET_COLUMN_FIVE").addTheme(THM_BUCKET_COLUMN).end()
				.addFrame(offered, FramePosition.NORTH).end().build();
		Frame3 bucketColumn6 = Frame3.builder("FRM_BUCKET_COLUMN_SIX").addTheme(THM_BUCKET_COLUMN).end()
				.addFrame(placed, FramePosition.NORTH).end().build();
		Frame3 bucketColumn7 = Frame3.builder("FRM_BUCKET_COLUMN_SEVEN").addTheme(THM_BUCKET_COLUMN).end()
				.addFrame(inProgress, FramePosition.NORTH).end().build();

		Frame3 bucket = Frame3.builder("FRM_BUCKET").addTheme(THM_BUCKET).end().addFrame(bucketColumn1, FramePosition.WEST)
				.end().addFrame(bucketColumn2, FramePosition.WEST).end().addFrame(bucketColumn3, FramePosition.WEST).end()
				.addFrame(bucketColumn4, FramePosition.WEST).end().addFrame(bucketColumn5, FramePosition.WEST).end()
				.addFrame(bucketColumn6, FramePosition.WEST).end().addFrame(bucketColumn7, FramePosition.WEST).end().build();

		return bucket;
	}

	public Frame3 generateTabs() {

		Theme THM_TABS = Theme.builder("THM_TABS").addAttribute().backgroundColor("none").end().addAttribute()
				.flexDirection("column").end().build();

		Theme THM_BUCKET_LABEL = Theme.builder("THM_BUCKET_LABEL").addAttribute().textAlign("center").end().build();

		Theme THM_TAB_HEADER = Theme.builder("THM_TAB_HEADER").addAttribute().backgroundColor("#065B9A").end()
				.addAttribute().flexDirection("row").end().addAttribute().marginRight(5).end().build();

		Theme THM_TAB_CONTENT = Theme.builder("THM_TAB_CONTENT").addAttribute().backgroundColor("#F8F9FA").end().build();

		Theme THM_TAB = Theme.builder("THM_TAB").addAttribute().backgroundColor("#3F505F").borderStyle("solid")
				.borderColor("white").color("white").height(40).end().build();

		Frame3 tab1 = Frame3.builder("FRM_TAB_ONE").addTheme(THM_TAB).end().question("QUE_TAB_BUCKET_VIEW")
				.addTheme(THM_BUCKET_LABEL).end().end().build();
		Frame3 tab2 = Frame3.builder("FRM_TAB_TWO").addTheme(THM_TAB).end().question("QUE_TAB_TABLE_VIEW").end().build();
		Frame3 tab3 = Frame3.builder("FRM_TAB_THREE").addTheme(THM_TAB).end().question("QUE_TAB_DETAIL_VIEW").end().build();

		Frame3 tabHeader = Frame3.builder("FRM_TAB_HEADER").addTheme(THM_TAB_HEADER).end()
				.addFrame(tab1, FramePosition.WEST).end().addFrame(tab2, FramePosition.WEST).end()
				.addFrame(tab3, FramePosition.WEST).end().question("QUE_NAME_TWO").end().build();

		Frame3 bucket = generateBucket();
		// Frame3 detailView = generateDetailView();

		Frame3 tabContent = Frame3.builder("FRM_TAB_CONTENT").addTheme(THM_TAB_CONTENT).end().addFrame(bucket)
				// .addFrame(detailView) /* comment out this to see the detail view */
				.end().build();

		Frame3 tabs = Frame3.builder("FRM_TABS").addTheme(THM_TABS).end().addFrame(tabHeader, FramePosition.NORTH).end()
				.addFrame(tabContent, FramePosition.CENTRE).end().build();

		System.out.println("Generated Tabs Frame");
		return tabs;
	}

	public Frame3 generateDetailView() {

		Theme THM_DETAIL_VIEW = Theme.builder("THM_DETAIL_VIEW").addAttribute().backgroundColor("#E2E1E0")
				.flexDirection("column").end().build();

		Theme THM_CONTAINER = Theme.builder("THM_CONTAINER").addAttribute().backgroundColor("white").width("90%")
				.height(300).end().build();

		Theme THM_FORM_LABEL_DEFAULT = Theme.builder("THM_FORM_LABEL_DEFAULT")
				.addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_TITLE, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_DESCRIPTION, true).end().build();

		Frame3 FRM_EMAIL = Frame3.builder("FRM_EMAIL").question("QUE_EMAIL").addTheme(THM_FORM_LABEL_DEFAULT)
				.vcl(VisualControlType.VCL_LABEL).end().end().build();

		Frame3 FRM_NAME = Frame3.builder("FRM_NAME").question("QUE_NAME_TWO").addTheme(THM_FORM_LABEL_DEFAULT)
				.vcl(VisualControlType.VCL_LABEL).end().end().build();
		Frame3 FRM_DOB = Frame3.builder("FRM_DOB").question("QUE_DOB").end().build();
		Frame3 FRM_MOBILE = Frame3.builder("FRM_MOBILE").question("QUE_MOBILE").end().build();
		Frame3 FRM_IMAGE = Frame3.builder("FRM_IMAGE").question("QUE_IMAGE").end().build();

		Frame3 FRM_DETAIL_VIEW_CONTAINER = Frame3.builder("FRM_DETAIL_VIEW_CONTAINER").addTheme(THM_CONTAINER).end()
				.addFrame(FRM_NAME, FramePosition.WEST).end().addFrame(FRM_NAME, FramePosition.WEST).end()
				.addFrame(FRM_EMAIL, FramePosition.WEST).end().addFrame(FRM_DOB, FramePosition.WEST).end()
				.addFrame(FRM_MOBILE, FramePosition.WEST).end().build();

		Frame3 FRM_DETAIL_VIEW = Frame3.builder("FRM_DETAIL_VIEW").addTheme(THM_DETAIL_VIEW).end().question("QUE_NAME_TWO")
				.end().addFrame(FRM_DETAIL_VIEW_CONTAINER, FramePosition.NORTH).end().build();

		System.out.println("Generated DetailView Frame");
		return FRM_DETAIL_VIEW;

	}

	public Frame3 generateTableView() {

		Theme THM_TABLE_VIEW = Theme.builder("THM_TABLE_VIEW").addAttribute().backgroundColor("#E2E1E0")
				.flexDirection("column").end().build();

		Frame3 FRM_TABLE_VIEW = Frame3.builder("FRM_TABLE_VIEW").addTheme(THM_TABLE_VIEW).end()
				.question("QUE_TAB_TABLE_VIEW").end().build();

		System.out.println("Generated TableView Frame");
		return FRM_TABLE_VIEW;
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

	@Test
	public void testForQuestionsList2() {

		GennyToken userToken = null;
		GennyToken serviceToken = null;
		QRules qRules = null;

		if (false) {
			userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
			serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
			qRules = new QRules(eventBusMock, userToken.getToken(), userToken.getAdecodedTokenMap());
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
			if (!"You need to be a test.".equals(jsonFormCodes)) {
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

			System.out.println("Sent");
			System.out.println("\n");
			System.out.println("After caching the formCodes   ::  ");
			System.out.println("Form Codes=" + formCodes);
			}

		} catch (Exception e) {

		}

	}

}