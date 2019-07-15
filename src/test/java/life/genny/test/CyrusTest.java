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
import life.genny.qwanda.VisualControlType;
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



//@Test
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

	Theme THM_BUTTONS = Theme.builder("THM_BUTTONS")
			.addAttribute().backgroundColor("#ffffff").padding(10)
			.justifyContent("center").borderColor("#000000").margin(4).maxWidth(700).width("100%").shadowColor("#000")
			.shadowOpacity(0.8).shadowRadius(5)
			.end()
			.build();

			Theme THM_OF = Theme.builder("THM_OF")
				.addAttribute().overflowY("auto").end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
		.end()
	.build();
/*	Frame3 frameSubmit = Frame3.builder("FRM_SUBMIT")
			.addTheme(THM_SUBMIT).end()
			.question("QUE_TABLE_NEXT_BTN")
				.addTheme(THM_BUTTONS).weight(2.0)
				.end()
			.end()
			.build();

	Frame3 frameCancel = Frame3.builder("FRM_CANCEL")
			.addTheme(THM_CANCEL).end()
			.question("QUE_CANCEL_BUTTON")
				.addTheme(THM_BUTTONS)
				.weight(2.0)
				.end()
			.end()
			.build();
*/
	Frame3 frameForm = Frame3.builder("FRM_FORM")
				.addTheme(THM_COLOR_BLACK).end()
				.addTheme(THM_OF).end()
				.question("QUE_ADD_INTERNSHIP_AGENT_STEP_ONE_GRP")
					.addTheme(THM_FORM_INPUT_DEFAULT)
					.vcl(VisualControlType.VCL_INPUT).weight(2.0).end().
					addTheme(THM_FORM_LABEL_DEFAULT)
					.vcl(VisualControlType.VCL_LABEL).end()
					.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
					.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end()
					.addTheme(THM_FORM_DEFAULT).weight(3.0).end()
					.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end()
			.end()
			.build();

	Frame3 frameCentre = Frame3.builder("FRM_CENTRE")
					.addFrame(frameForm, FramePosition.NORTH).end()
					//.addFrame(frameSubmit, FramePosition.NORTH).end()
					//.addFrame(frameCancel, FramePosition.NORTH).end()
				.build();

/*	Frame3 frameButtons = Frame3.builder("FRM_WEST")
				.addFrame(frameSubmit, FramePosition.WEST).end()
				.addFrame(frameCancel, FramePosition.WEST).end()
			.build();
*/

	Frame3 frameMain = Frame3.builder("FRM_MAIN")
				.addTheme(THM_COLOR_GREY).end()
				.addFrame(frameCentre, FramePosition.CENTRE)
			.end()
			.build();

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

	Theme THM_BUTTONS = Theme.builder("THM_BUTTONS")
			.addAttribute().backgroundColor("#ffffff").padding(10)
			.justifyContent("center").borderColor("#000000").margin(4).maxWidth(700).width("100%").shadowColor("#000")
			.shadowOpacity(0.8).shadowRadius(5)
			.end()
			.build();

			Theme THM_OF = Theme.builder("THM_OF")
				.addAttribute().overflowY("auto").end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
		.end()
	.build();

	Frame3 frameForm = Frame3.builder("FRM_FORM")
				.addTheme(THM_COLOR_BLACK).end()
				.addTheme(THM_OF).end()
				.question("QUE_ADD_INTERNSHIP_AGENT_STEP_TWO_GRP")
					.addTheme(THM_FORM_INPUT_DEFAULT)
					.vcl(VisualControlType.VCL_INPUT).weight(2.0).end().
					addTheme(THM_FORM_LABEL_DEFAULT)
					.vcl(VisualControlType.VCL_LABEL).end()
					.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
					.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end()
					.addTheme(THM_FORM_DEFAULT).weight(3.0).end()
					.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end()
			.end()
			.build();

	Frame3 frameCentre = Frame3.builder("FRM_CENTRE")
					.addFrame(frameForm, FramePosition.NORTH).end()

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

//@Test
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

	Theme THM_BUTTONS = Theme.builder("THM_BUTTONS")
			.addAttribute().backgroundColor("#ffffff").padding(10)
			.justifyContent("center").borderColor("#000000").margin(4).maxWidth(700).width("100%").shadowColor("#000")
			.shadowOpacity(0.8).shadowRadius(5)
			.end()
			.build();

			Theme THM_OF = Theme.builder("THM_OF")
				.addAttribute().overflowY("auto").end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
		.end()
	.build();

	Frame3 frameForm = Frame3.builder("FRM_FORM")
				.addTheme(THM_COLOR_BLACK).end()
				.addTheme(THM_OF).end()
				.question("QUE_SELECT_USER_TYPE_GRP")
					.addTheme(THM_FORM_INPUT_DEFAULT)
					.vcl(VisualControlType.VCL_INPUT).weight(2.0).end().
					addTheme(THM_FORM_LABEL_DEFAULT)
					.vcl(VisualControlType.VCL_LABEL).end()
					.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
					.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end()
					.addTheme(THM_FORM_DEFAULT).weight(3.0).end()
					.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end()
			.end()
			.build();

	Frame3 frameCentre = Frame3.builder("FRM_CENTRE")
					.addFrame(frameForm, FramePosition.NORTH).end()

				.build();

	Frame3 frameMain = Frame3.builder("FRM_MAIN")
				.addTheme(THM_COLOR_GREY).end()
				.addFrame(frameCentre, FramePosition.CENTRE)
			.end()
			.build();

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

	Theme THM_BUTTONS = Theme.builder("THM_BUTTONS")
			.addAttribute().backgroundColor("#ffffff").padding(10)
			.justifyContent("center").borderColor("#000000").margin(4).maxWidth(700).width("100%").shadowColor("#000")
			.shadowOpacity(0.8).shadowRadius(5)
			.end()
			.build();

			Theme THM_OF = Theme.builder("THM_OF")
				.addAttribute().overflowY("auto").end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
		.end()
	.build();

	Frame3 frameForm = Frame3.builder("FRM_FORM")
				.addTheme(THM_COLOR_BLACK).end()
				.addTheme(THM_OF).end()
				.question("QUE_AGENT_PROFILE_GRP")
					.addTheme(THM_FORM_INPUT_DEFAULT)
					.vcl(VisualControlType.VCL_INPUT).weight(2.0).end().
					addTheme(THM_FORM_LABEL_DEFAULT)
					.vcl(VisualControlType.VCL_LABEL).end()
					.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
					.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end()
					.addTheme(THM_FORM_DEFAULT).weight(3.0).end()
					.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end()
			.end()
			.build();

	Frame3 frameCentre = Frame3.builder("FRM_CENTRE")
					.addFrame(frameForm, FramePosition.NORTH).end()

				.build();

	Frame3 frameMain = Frame3.builder("FRM_MAIN")
				.addTheme(THM_COLOR_GREY).end()
				.addFrame(frameCentre, FramePosition.CENTRE)
			.end()
			.build();

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

	Theme THM_BUTTONS = Theme.builder("THM_BUTTONS")
			.addAttribute().backgroundColor("#ffffff").padding(10)
			.justifyContent("center").borderColor("#000000").margin(4).maxWidth(700).width("100%").shadowColor("#000")
			.shadowOpacity(0.8).shadowRadius(5)
			.end()
			.build();

			Theme THM_OF = Theme.builder("THM_OF")
				.addAttribute().overflowY("auto").end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
		.end()
	.build();

	Frame3 frameForm = Frame3.builder("FRM_FORM")
				.addTheme(THM_COLOR_BLACK).end()
				.addTheme(THM_OF).end()
				.question("QUE_ADD_INTERNSHIP_TEMPLATE_AGENT_GRP")
					.addTheme(THM_FORM_INPUT_DEFAULT)
					.vcl(VisualControlType.VCL_INPUT).weight(2.0).end().
					addTheme(THM_FORM_LABEL_DEFAULT)
					.vcl(VisualControlType.VCL_LABEL).end()
					.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
					.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end()
					.addTheme(THM_FORM_DEFAULT).weight(3.0).end()
					.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end()
			.end()
			.build();

	Frame3 frameCentre = Frame3.builder("FRM_CENTRE")
					.addFrame(frameForm, FramePosition.NORTH).end()
				.build();

	Frame3 frameMain = Frame3.builder("FRM_MAIN")
				.addTheme(THM_COLOR_GREY).end()
				.addFrame(frameCentre, FramePosition.CENTRE)
			.end()
			.build();

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

	Theme THM_BUTTONS = Theme.builder("THM_BUTTONS")
			.addAttribute().backgroundColor("#ffffff").padding(10)
			.justifyContent("center").borderColor("#000000").margin(4).maxWidth(700).width("100%").shadowColor("#000")
			.shadowOpacity(0.8).shadowRadius(5)
			.end()
			.build();

			Theme THM_OF = Theme.builder("THM_OF")
				.addAttribute().overflowY("auto").end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
		.end()
	.build();

	Frame3 frameForm = Frame3.builder("FRM_FORM")
				.addTheme(THM_COLOR_BLACK).end()
				.addTheme(THM_OF).end()
				.question("QUE_ADD_AGENT_GRP")
					.addTheme(THM_FORM_INPUT_DEFAULT)
					.vcl(VisualControlType.VCL_INPUT).weight(2.0).end().
					addTheme(THM_FORM_LABEL_DEFAULT)
					.vcl(VisualControlType.VCL_LABEL).end()
					.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
					.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end()
					.addTheme(THM_FORM_DEFAULT).weight(3.0).end()
					.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end()
			.end()
			.build();

	Frame3 frameCentre = Frame3.builder("FRM_CENTRE")
					.addFrame(frameForm, FramePosition.NORTH).end()
				.build();

	Frame3 frameMain = Frame3.builder("FRM_MAIN")
				.addTheme(THM_COLOR_GREY).end()
				.addFrame(frameCentre, FramePosition.CENTRE)
			.end()
			.build();

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

	Theme THM_BUTTONS = Theme.builder("THM_BUTTONS")
			.addAttribute().backgroundColor("#ffffff").padding(10)
			.justifyContent("center").borderColor("#000000").margin(4).maxWidth(700).width("100%").shadowColor("#000")
			.shadowOpacity(0.8).shadowRadius(5)
			.end()
			.build();

			Theme THM_OF = Theme.builder("THM_OF")
				.addAttribute().overflowY("auto").end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
		.end()
	.build();

	Frame3 frameForm = Frame3.builder("FRM_FORM")
				.addTheme(THM_COLOR_BLACK).end()
				.addTheme(THM_OF).end()
				.question("QUE_ADD_EDU_PROVIDER_STAFF_GRP")
					.addTheme(THM_FORM_INPUT_DEFAULT)
					.vcl(VisualControlType.VCL_INPUT).weight(2.0).end().
					addTheme(THM_FORM_LABEL_DEFAULT)
					.vcl(VisualControlType.VCL_LABEL).end()
					.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
					.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end()
					.addTheme(THM_FORM_DEFAULT).weight(3.0).end()
					.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end()
			.end()
			.build();

	Frame3 frameCentre = Frame3.builder("FRM_CENTRE")
					.addFrame(frameForm, FramePosition.NORTH).end()
				.build();

	Frame3 frameMain = Frame3.builder("FRM_MAIN")
				.addTheme(THM_COLOR_GREY).end()
				.addFrame(frameCentre, FramePosition.CENTRE)
			.end()
			.build();

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

	Theme THM_BUTTONS = Theme.builder("THM_BUTTONS")
			.addAttribute().backgroundColor("#ffffff").padding(10)
			.justifyContent("center").borderColor("#000000").margin(4).maxWidth(700).width("100%").shadowColor("#000")
			.shadowOpacity(0.8).shadowRadius(5)
			.end()
			.build();

			Theme THM_OF = Theme.builder("THM_OF")
				.addAttribute().overflowY("auto").end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
		.end()
	.build();

	Frame3 frameForm = Frame3.builder("FRM_FORM")
				.addTheme(THM_COLOR_BLACK).end()
				.addTheme(THM_OF).end()
				.question("QUE_ADD_HOST_COMPANY_STAFF_GRP")
					.addTheme(THM_FORM_INPUT_DEFAULT)
					.vcl(VisualControlType.VCL_INPUT).weight(2.0).end().
					addTheme(THM_FORM_LABEL_DEFAULT)
					.vcl(VisualControlType.VCL_LABEL).end()
					.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
					.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end()
					.addTheme(THM_FORM_DEFAULT).weight(3.0).end()
					.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end()
			.end()
			.build();

	Frame3 frameCentre = Frame3.builder("FRM_CENTRE")
					.addFrame(frameForm, FramePosition.NORTH).end()
				.build();

	Frame3 frameMain = Frame3.builder("FRM_MAIN")
				.addTheme(THM_COLOR_GREY).end()
				.addFrame(frameCentre, FramePosition.CENTRE)
			.end()
			.build();

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

	Theme THM_BUTTONS = Theme.builder("THM_BUTTONS")
			.addAttribute().backgroundColor("#ffffff").padding(10)
			.justifyContent("center").borderColor("#000000").margin(4).maxWidth(700).width("100%").shadowColor("#000")
			.shadowOpacity(0.8).shadowRadius(5)
			.end()
			.build();

			Theme THM_OF = Theme.builder("THM_OF")
				.addAttribute().overflowY("auto").end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
		.end()
	.build();

	Frame3 frameForm = Frame3.builder("FRM_FORM")
				.addTheme(THM_COLOR_BLACK).end()
				.addTheme(THM_OF).end()
				.question("QUE_ADD_INTERN_GRP")
					.addTheme(THM_FORM_INPUT_DEFAULT)
					.vcl(VisualControlType.VCL_INPUT).weight(2.0).end().
					addTheme(THM_FORM_LABEL_DEFAULT)
					.vcl(VisualControlType.VCL_LABEL).end()
					.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
					.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end()
					.addTheme(THM_FORM_DEFAULT).weight(3.0).end()
					.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end()
			.end()
			.build();

	Frame3 frameCentre = Frame3.builder("FRM_CENTRE")
					.addFrame(frameForm, FramePosition.NORTH).end()
				.build();

	Frame3 frameMain = Frame3.builder("FRM_MAIN")
				.addTheme(THM_COLOR_GREY).end()
				.addFrame(frameCentre, FramePosition.CENTRE)
			.end()
			.build();

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

	Theme THM_BUTTONS = Theme.builder("THM_BUTTONS")
			.addAttribute().backgroundColor("#ffffff").padding(10)
			.justifyContent("center").borderColor("#000000").margin(4).maxWidth(700).width("100%").shadowColor("#000")
			.shadowOpacity(0.8).shadowRadius(5)
			.end()
			.build();

			Theme THM_OF = Theme.builder("THM_OF")
				.addAttribute().overflowY("auto").end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
		.end()
	.build();

	Frame3 frameForm = Frame3.builder("FRM_FORM")
				.addTheme(THM_COLOR_BLACK).end()
				.addTheme(THM_OF).end()
				.question("QUE_SELECT_COMPANY_TYPE_GRP")
					.addTheme(THM_FORM_INPUT_DEFAULT)
					.vcl(VisualControlType.VCL_INPUT).weight(2.0).end().
					addTheme(THM_FORM_LABEL_DEFAULT)
					.vcl(VisualControlType.VCL_LABEL).end()
					.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
					.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end()
					.addTheme(THM_FORM_DEFAULT).weight(3.0).end()
					.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end()
			.end()
			.build();

	Frame3 frameCentre = Frame3.builder("FRM_CENTRE")
					.addFrame(frameForm, FramePosition.NORTH).end()
				.build();

	Frame3 frameMain = Frame3.builder("FRM_MAIN")
				.addTheme(THM_COLOR_GREY).end()
				.addFrame(frameCentre, FramePosition.CENTRE)
			.end()
			.build();

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

	Theme THM_BUTTONS = Theme.builder("THM_BUTTONS")
			.addAttribute().backgroundColor("#ffffff").padding(10)
			.justifyContent("center").borderColor("#000000").margin(4).maxWidth(700).width("100%").shadowColor("#000")
			.shadowOpacity(0.8).shadowRadius(5)
			.end()
			.build();

			Theme THM_OF = Theme.builder("THM_OF")
				.addAttribute().overflowY("auto").end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
		.end()
	.build();

	Frame3 frameForm = Frame3.builder("FRM_FORM")
				.addTheme(THM_COLOR_BLACK).end()
				.addTheme(THM_OF).end()
				.question("QUE_ADD_AGENCY_GRP")
					.addTheme(THM_FORM_INPUT_DEFAULT)
					.vcl(VisualControlType.VCL_INPUT).weight(2.0).end().
					addTheme(THM_FORM_LABEL_DEFAULT)
					.vcl(VisualControlType.VCL_LABEL).end()
					.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
					.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end()
					.addTheme(THM_FORM_DEFAULT).weight(3.0).end()
					.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end()
			.end()
			.build();

	Frame3 frameCentre = Frame3.builder("FRM_CENTRE")
					.addFrame(frameForm, FramePosition.NORTH).end()
				.build();

	Frame3 frameMain = Frame3.builder("FRM_MAIN")
				.addTheme(THM_COLOR_GREY).end()
				.addFrame(frameCentre, FramePosition.CENTRE)
			.end()
			.build();

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

	Theme THM_BUTTONS = Theme.builder("THM_BUTTONS")
			.addAttribute().backgroundColor("#ffffff").padding(10)
			.justifyContent("center").borderColor("#000000").margin(4).maxWidth(700).width("100%").shadowColor("#000")
			.shadowOpacity(0.8).shadowRadius(5)
			.end()
			.build();

			Theme THM_OF = Theme.builder("THM_OF")
				.addAttribute().overflowY("auto").end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
		.end()
	.build();

	Frame3 frameForm = Frame3.builder("FRM_FORM")
				.addTheme(THM_COLOR_BLACK).end()
				.addTheme(THM_OF).end()
				.question("QUE_ADD_EDU_PROVIDER_GRP")
					.addTheme(THM_FORM_INPUT_DEFAULT)
					.vcl(VisualControlType.VCL_INPUT).weight(2.0).end().
					addTheme(THM_FORM_LABEL_DEFAULT)
					.vcl(VisualControlType.VCL_LABEL).end()
					.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
					.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end()
					.addTheme(THM_FORM_DEFAULT).weight(3.0).end()
					.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end()
			.end()
			.build();

	Frame3 frameCentre = Frame3.builder("FRM_CENTRE")
					.addFrame(frameForm, FramePosition.NORTH).end()
				.build();

	Frame3 frameMain = Frame3.builder("FRM_MAIN")
				.addTheme(THM_COLOR_GREY).end()
				.addFrame(frameCentre, FramePosition.CENTRE)
			.end()
			.build();

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

	Theme THM_BUTTONS = Theme.builder("THM_BUTTONS")
			.addAttribute().backgroundColor("#ffffff").padding(10)
			.justifyContent("center").borderColor("#000000").margin(4).maxWidth(700).width("100%").shadowColor("#000")
			.shadowOpacity(0.8).shadowRadius(5)
			.end()
			.build();

			Theme THM_OF = Theme.builder("THM_OF")
				.addAttribute().overflowY("auto").end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
		.end()
	.build();

	Frame3 frameForm = Frame3.builder("FRM_FORM")
				.addTheme(THM_COLOR_BLACK).end()
				.addTheme(THM_OF).end()
				.question("QUE_ADD_HOST_COMPANY_GRP")
					.addTheme(THM_FORM_INPUT_DEFAULT)
					.vcl(VisualControlType.VCL_INPUT).weight(2.0).end().
					addTheme(THM_FORM_LABEL_DEFAULT)
					.vcl(VisualControlType.VCL_LABEL).end()
					.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
					.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end()
					.addTheme(THM_FORM_DEFAULT).weight(3.0).end()
					.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end()
			.end()
			.build();

	Frame3 frameCentre = Frame3.builder("FRM_CENTRE")
					.addFrame(frameForm, FramePosition.NORTH).end()
				.build();

	Frame3 frameMain = Frame3.builder("FRM_MAIN")
				.addTheme(THM_COLOR_GREY).end()
				.addFrame(frameCentre, FramePosition.CENTRE)
			.end()
			.build();

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

	Theme THM_BUTTONS = Theme.builder("THM_BUTTONS")
			.addAttribute().backgroundColor("#ffffff").padding(10)
			.justifyContent("center").borderColor("#000000").margin(4).maxWidth(700).width("100%").shadowColor("#000")
			.shadowOpacity(0.8).shadowRadius(5)
			.end()
			.build();

			Theme THM_OF = Theme.builder("THM_OF")
				.addAttribute().overflowY("auto").end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
		.end()
	.build();

	Frame3 frameForm = Frame3.builder("FRM_FORM")
				.addTheme(THM_COLOR_BLACK).end()
				.addTheme(THM_OF).end()
				.question("QUE_EDU_PROVIDER_STAFF_PROFILE_GRP")
					.addTheme(THM_FORM_INPUT_DEFAULT)
					.vcl(VisualControlType.VCL_INPUT).weight(2.0).end().
					addTheme(THM_FORM_LABEL_DEFAULT)
					.vcl(VisualControlType.VCL_LABEL).end()
					.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
					.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end()
					.addTheme(THM_FORM_DEFAULT).weight(3.0).end()
					.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end()
			.end()
			.build();

	Frame3 frameCentre = Frame3.builder("FRM_CENTRE")
					.addFrame(frameForm, FramePosition.NORTH).end()

				.build();

	Frame3 frameMain = Frame3.builder("FRM_MAIN")
				.addTheme(THM_COLOR_GREY).end()
				.addFrame(frameCentre, FramePosition.CENTRE)
			.end()
			.build();

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

	Theme THM_BUTTONS = Theme.builder("THM_BUTTONS")
			.addAttribute().backgroundColor("#ffffff").padding(10)
			.justifyContent("center").borderColor("#000000").margin(4).maxWidth(700).width("100%").shadowColor("#000")
			.shadowOpacity(0.8).shadowRadius(5)
			.end()
			.build();

			Theme THM_OF = Theme.builder("THM_OF")
				.addAttribute().overflowY("auto").end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
		.end()
	.build();

	Frame3 frameForm = Frame3.builder("FRM_FORM")
				.addTheme(THM_COLOR_BLACK).end()
				.addTheme(THM_OF).end()
				.question("QUE_ADD_STAFF_GRP")
					.addTheme(THM_FORM_INPUT_DEFAULT)
					.vcl(VisualControlType.VCL_INPUT).weight(2.0).end().
					addTheme(THM_FORM_LABEL_DEFAULT)
					.vcl(VisualControlType.VCL_LABEL).end()
					.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
					.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end()
					.addTheme(THM_FORM_DEFAULT).weight(3.0).end()
					.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end()
			.end()
			.build();

	Frame3 frameCentre = Frame3.builder("FRM_CENTRE")
					.addFrame(frameForm, FramePosition.NORTH).end()

				.build();

	Frame3 frameMain = Frame3.builder("FRM_MAIN")
				.addTheme(THM_COLOR_GREY).end()
				.addFrame(frameCentre, FramePosition.CENTRE)
			.end()
			.build();

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

	Theme THM_BUTTONS = Theme.builder("THM_BUTTONS")
			.addAttribute().backgroundColor("#ffffff").padding(10)
			.justifyContent("center").borderColor("#000000").margin(4).maxWidth(700).width("100%").shadowColor("#000")
			.shadowOpacity(0.8).shadowRadius(5)
			.end()
			.build();

			Theme THM_OF = Theme.builder("THM_OF")
				.addAttribute().overflowY("auto").end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
		.end()
	.build();

	Frame3 frameForm = Frame3.builder("FRM_FORM")
				.addTheme(THM_COLOR_BLACK).end()
				.addTheme(THM_OF).end()
				.question("QUE_ADD_STUDENT_GRP")
					.addTheme(THM_FORM_INPUT_DEFAULT)
					.vcl(VisualControlType.VCL_INPUT).weight(2.0).end().
					addTheme(THM_FORM_LABEL_DEFAULT)
					.vcl(VisualControlType.VCL_LABEL).end()
					.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
					.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end()
					.addTheme(THM_FORM_DEFAULT).weight(3.0).end()
					.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end()
			.end()
			.build();

	Frame3 frameCentre = Frame3.builder("FRM_CENTRE")
					.addFrame(frameForm, FramePosition.NORTH).end()

				.build();

	Frame3 frameMain = Frame3.builder("FRM_MAIN")
				.addTheme(THM_COLOR_GREY).end()
				.addFrame(frameCentre, FramePosition.CENTRE)
			.end()
			.build();

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

	Theme THM_BUTTONS = Theme.builder("THM_BUTTONS")
			.addAttribute().backgroundColor("#ffffff").padding(10)
			.justifyContent("center").borderColor("#000000").margin(4).maxWidth(700).width("100%").shadowColor("#000")
			.shadowOpacity(0.8).shadowRadius(5)
			.end()
			.build();

			Theme THM_OF = Theme.builder("THM_OF")
				.addAttribute().overflowY("auto").end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
		.end()
	.build();

	Frame3 frameForm = Frame3.builder("FRM_FORM")
				.addTheme(THM_COLOR_BLACK).end()
				.addTheme(THM_OF).end()
				.question("QUE_INTERN_PROFILE_GRP")
					.addTheme(THM_FORM_INPUT_DEFAULT)
					.vcl(VisualControlType.VCL_INPUT).weight(2.0).end().
					addTheme(THM_FORM_LABEL_DEFAULT)
					.vcl(VisualControlType.VCL_LABEL).end()
					.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
					.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end()
					.addTheme(THM_FORM_DEFAULT).weight(3.0).end()
					.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end()
			.end()
			.build();

	Frame3 frameCentre = Frame3.builder("FRM_CENTRE")
					.addFrame(frameForm, FramePosition.NORTH).end()

				.build();

	Frame3 frameMain = Frame3.builder("FRM_MAIN")
				.addTheme(THM_COLOR_GREY).end()
				.addFrame(frameCentre, FramePosition.CENTRE)
			.end()
			.build();

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

	Theme THM_BUTTONS = Theme.builder("THM_BUTTONS")
			.addAttribute().backgroundColor("#ffffff").padding(10)
			.justifyContent("center").borderColor("#000000").margin(4).maxWidth(700).width("100%").shadowColor("#000")
			.shadowOpacity(0.8).shadowRadius(5)
			.end()
			.build();

			Theme THM_OF = Theme.builder("THM_OF")
				.addAttribute().overflowY("auto").end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
		.end()
	.build();

	Frame3 frameForm = Frame3.builder("FRM_FORM")
				.addTheme(THM_COLOR_BLACK).end()
				.addTheme(THM_OF).end()
				.question("QUE_HOST_COMPANY_STAFF_PROFILE_GRP")
					.addTheme(THM_FORM_INPUT_DEFAULT)
					.vcl(VisualControlType.VCL_INPUT).weight(2.0).end().
					addTheme(THM_FORM_LABEL_DEFAULT)
					.vcl(VisualControlType.VCL_LABEL).end()
					.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
					.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end()
					.addTheme(THM_FORM_DEFAULT).weight(3.0).end()
					.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end()
			.end()
			.build();

	Frame3 frameCentre = Frame3.builder("FRM_CENTRE")
					.addFrame(frameForm, FramePosition.NORTH).end()

				.build();

	Frame3 frameMain = Frame3.builder("FRM_MAIN")
				.addTheme(THM_COLOR_GREY).end()
				.addFrame(frameCentre, FramePosition.CENTRE)
			.end()
			.build();

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

	Theme THM_BUTTONS = Theme.builder("THM_BUTTONS")
			.addAttribute().backgroundColor("#ffffff").padding(10)
			.justifyContent("center").borderColor("#000000").margin(4).maxWidth(700).width("100%").shadowColor("#000")
			.shadowOpacity(0.8).shadowRadius(5)
			.end()
			.build();

			Theme THM_OF = Theme.builder("THM_OF")
				.addAttribute().overflowY("auto").end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
		.end()
	.build();

	Frame3 frameForm = Frame3.builder("FRM_FORM")
				.addTheme(THM_COLOR_BLACK).end()
				.addTheme(THM_OF).end()
				.question("QUE_ADD_INTERNSHIP_STEP_ONE_GRP")
					.addTheme(THM_FORM_INPUT_DEFAULT)
					.vcl(VisualControlType.VCL_INPUT).weight(2.0).end().
					addTheme(THM_FORM_LABEL_DEFAULT)
					.vcl(VisualControlType.VCL_LABEL).end()
					.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
					.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end()
					.addTheme(THM_FORM_DEFAULT).weight(3.0).end()
					.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end()
			.end()
			.build();

	Frame3 frameCentre = Frame3.builder("FRM_CENTRE")
					.addFrame(frameForm, FramePosition.NORTH).end()

				.build();

	Frame3 frameMain = Frame3.builder("FRM_MAIN")
				.addTheme(THM_COLOR_GREY).end()
				.addFrame(frameCentre, FramePosition.CENTRE)
			.end()
			.build();

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

	Theme THM_BUTTONS = Theme.builder("THM_BUTTONS")
			.addAttribute().backgroundColor("#ffffff").padding(10)
			.justifyContent("center").borderColor("#000000").margin(4).maxWidth(700).width("100%").shadowColor("#000")
			.shadowOpacity(0.8).shadowRadius(5)
			.end()
			.build();

			Theme THM_OF = Theme.builder("THM_OF")
				.addAttribute().overflowY("auto").end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
		.end()
	.build();

	Frame3 frameForm = Frame3.builder("FRM_FORM")
				.addTheme(THM_COLOR_BLACK).end()
				.addTheme(THM_OF).end()
				.question("QUE_ADD_INTERNSHIP_STEP_TWO_GRP")
					.addTheme(THM_FORM_INPUT_DEFAULT)
					.vcl(VisualControlType.VCL_INPUT).weight(2.0).end().
					addTheme(THM_FORM_LABEL_DEFAULT)
					.vcl(VisualControlType.VCL_LABEL).end()
					.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
					.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end()
					.addTheme(THM_FORM_DEFAULT).weight(3.0).end()
					.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end()
			.end()
			.build();

	Frame3 frameCentre = Frame3.builder("FRM_CENTRE")
					.addFrame(frameForm, FramePosition.NORTH).end()

				.build();

	Frame3 frameMain = Frame3.builder("FRM_MAIN")
				.addTheme(THM_COLOR_GREY).end()
				.addFrame(frameCentre, FramePosition.CENTRE)
			.end()
			.build();

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

	Theme THM_BUTTONS = Theme.builder("THM_BUTTONS")
			.addAttribute().backgroundColor("#ffffff").padding(10)
			.justifyContent("center").borderColor("#000000").margin(4).maxWidth(700).width("100%").shadowColor("#000")
			.shadowOpacity(0.8).shadowRadius(5)
			.end()
			.build();

			Theme THM_OF = Theme.builder("THM_OF")
				.addAttribute().overflowY("auto").end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
		.end()
	.build();

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
			//	System.out.println(formCode);
				
				
				//	String frameCode = "FRM_" +  formCode;
				
				Frame3 frameForm = Frame3.builder("FRM_" +  formCode)
				.addTheme(THM_COLOR_BLACK).end()
				.addTheme(THM_OF).end()
				.question(formCode)
				.addTheme(THM_FORM_INPUT_DEFAULT)
				.vcl(VisualControlType.VCL_INPUT).weight(2.0).end().
				addTheme(THM_FORM_LABEL_DEFAULT)
				.vcl(VisualControlType.VCL_LABEL).end()
				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
				.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end()
				.addTheme(THM_FORM_DEFAULT).weight(3.0).end()
				.addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end()
				.end()
				.build();
				
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

//@Test
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
		gks = GennyKieSession.builder(serviceToken, false)
				.addJbpm("test_page_1.bpmn")
				.addFact("msg", msg)
				.addToken(userToken)
				.build();

		gks.start();
		gks.injectSignal("inputSignal", userToken);
	//	gks.getKieSession().signalEvent(type, event);
		gks.advanceSeconds(20, false);

		System.out.println("Sent");

	} catch (Exception e) {
		System.out.println(e.getLocalizedMessage());
	} finally {
		gks.close();
	}
}

//@Test
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
				.addDrl("FRM_FORM2.drl")
				.addDrl("FRM_FORM1.drl")
				.addDrl("GenerateThemes")
			//	.addJbpm("test_session_2.bpmn")
				.addToken(userToken)
				.build();

		
		// for (int i=0;i<2;i++) {	
		// 	gks.advanceSeconds(2, true);
		// 	gks.injectSignal("inputSignal", "Hello");
		// 	gks.advanceSeconds(2, true);
		// 	gks.injectSignal("inputSignal2", "Hello");
		// }
		
		//	for (int i=0;i<2;i++) {
				gks.displayForm("FRM_FORM",userToken);
				gks.advanceSeconds(2, true);
				// gks.displayForm("FRM_FORM",userToken);
				// gks.advanceSeconds(2, true);
			//}
		//gks.sendLogout(userToken);
		 	System.out.println("Sent");

		 } 
	catch (Exception e) {
		System.out.println(e.getLocalizedMessage());
	} finally {
		gks.close();
	}
}

@Test
public void testCachedDesktop() {
				QRules rules = GennyJbpmBaseTest.setupLocalService();
				GennyToken userToken = new GennyToken("userToken", rules.getToken());
				GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
				rules.sendAllAttributes();
		GennyKieSession gks = null;

		try {
			gks = GennyKieSession.builder(serviceToken, false)
					.addToken(userToken)
					.build();
					gks.start();


				gks.displayForm("FRM_FORM2",userToken);

			System.out.println("Sent");

		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			gks.close();
		}
	}
}