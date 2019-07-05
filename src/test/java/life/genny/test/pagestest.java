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
import life.genny.qwanda.VisualControlType;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.rules.QRules;
import life.genny.rules.listeners.JbpmInitListener;

public class pagestest extends GennyJbpmBaseTest {

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

	public pagestest() {
		super(false);
	}

	@Test
	public void internProfileViewPage() {

		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken); // defaults to user anyway
		GennyToken serviceToken = new GennyToken("serviceToken", rules.getServiceToken());

		Theme THM_DUMMY = Theme.builder("THM_DUMMY")

				.addAttribute()
				.height(100).end()
				.addAttribute()
				.width(90).end()
				.build();

		Theme THM_BACKGROUND_BLUE = Theme.builder("THM_BACKGROUND_BLUE")

				.addAttribute()
				.backgroundColor("blue")
				.end()
				.build();

		Theme THM_BACKGROUND_RED = Theme.builder("THM_BACKGROUND_RED")

				.addAttribute()
				.backgroundColor("red").end()
				.build();

		Theme THM_BACKGROUND_GREEN = Theme.builder("THM_BACKGROUND_GREEN")

				.addAttribute()
				.backgroundColor("yellow").end()
				.build();

				Theme THM_FORM_INPUT_DEFAULT = Theme.builder("THM_FORM_INPUT_DEFAULT").addAttribute().borderBottomWidth(1).borderColor("#ddd").borderStyle("solid").placeholderColor("#888").end()
				.addAttribute(ThemeAttributeType.PRI_CONTENT_HOVER).borderColor("#aaa").end()
				.addAttribute(ThemeAttributeType.PRI_CONTENT_ACTIVE).borderColor("green").end()
				.addAttribute(ThemeAttributeType.PRI_CONTENT_ERROR).borderColor("red").color("red").end()
				.build();

		Theme THM_FORM_LABEL_DEFAULT = Theme.builder("THM_FORM_LABEL_DEFAULT")
				.addAttribute()
					.bold(true)
					.size("md")
				.end()
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
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_DESCRIPTION,true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_LABEL,true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_REQUIRED,true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_ICON,true).end()

				.build();

		Theme THM_FORM_CONTAINER_DEFAULT = Theme.builder("THM_FORM_CONTAINER_DEFAULT")
				.addAttribute()
					.backgroundColor("white")
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
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_DESCRIPTION,true).end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE,false).end()
				.build();

		Frame3 frameDummy = Frame3.builder("FRM_DUMMY")

				.addTheme(THM_DUMMY).end()
				.build();

		Frame3 frmTree = Frame3.builder("FRM_TREE")

				.addTheme(THM_BACKGROUND_RED).end()
				.question("QUE_NAME_GRP")
				//.setTree("") Tree goes here
				.addTheme(THM_FORM_INPUT_DEFAULT)
				.vcl(VisualControlType.VCL_INPUT)
				.weight(2.0)
				.end()
				.addTheme(THM_FORM_LABEL_DEFAULT)
				.vcl(VisualControlType.VCL_LABEL)
				.end()
				.addTheme(THM_FORM_WRAPPER_DEFAULT)
				.vcl(VisualControlType.VCL_WRAPPER)
				.end()
				.addTheme(THM_FORM_ERROR_DEFAULT)
				.vcl(VisualControlType.VCL_ERROR)
				.end()
				.addTheme(THM_FORM_DEFAULT)
				.weight(3.0)
				.end()
				.addTheme(THM_FORM_CONTAINER_DEFAULT)
				.weight(2.0)
				.end()
			.end()
		.build();

		Frame3 footer = Frame3.builder("FRM_FOOTER")

				// .addFrame(frameDummy, FramePosition.CENTRE).end()
				.addTheme(THM_BACKGROUND_GREEN).end()
				.addFrame(frameDummy, FramePosition.CENTRE).end()
				.build();

		Frame3 frmContainer = Frame3.builder("FRM_CONTAINER")

				.addFrame(frmTree, FramePosition.WEST).end()
				.addFrame(footer, FramePosition.SOUTH).end()
				.addTheme(THM_BACKGROUND_BLUE).end()
				.question("QUE_USER_PROFILE_GRP").end()
				.build();

		Frame3 desktop = Frame3.builder("FRM_ROOT")

				.addFrame(frmContainer).end().build();

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


}
