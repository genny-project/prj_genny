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

import life.genny.qwanda.datatype.DataType;
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
import life.genny.qwanda.validation.Validation;
import life.genny.qwanda.validation.ValidationList;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.rules.QRules;
import life.genny.rules.listeners.JbpmInitListener;
import life.genny.utils.BaseEntityUtils;
import life.genny.utils.FrameUtils2;
import life.genny.utils.GennyJbpmBaseTest;
import life.genny.utils.VertxUtils;
import life.genny.qwanda.datatype.DataType;

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

		Theme THM_FORM_VCL_WRAPPER_GENNY = Theme.builder("THM_FORM_VCL_WRAPPER_GENNY").addAttribute().margin(10)
		 		.end().addAttribute(ThemeAttributeType.PRI_CONTENT_ERROR).end().build();

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
					.backgroundColor("white").maxWidth(600).width("100%")
					.borderWidth(1)
					.borderColor("silver")
					.borderStyle("solid")
					.margin("auto")
					.shadowColor("silver")
					.shadowOpacity(1.0)
					.shadowRadius(5)
				.end()
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_TITLE, false).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_DESCRIPTION, false).end()
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
	//	.addAttribute().display("flex").end()
		.addAttribute().borderStyle("solid").end()
		.addAttribute().backgroundColor(project.getValue("PRI_COLOR_BACKGROUND", "#f6f6f6")).end()
		.addAttribute().color(project.getValue("PRI_COLOR_SURFACE_ON", "#ffffff")).end()
		.addAttribute().borderColor("black").end()
		.addAttribute().borderWidth(1).end()
		.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
	.end()
	.build();


	Theme THM_FORM_BUTTONS_TEST = Theme.builder("THM_FORM_BUTTONS_TEST")
		.addAttribute()
			.justifyContent("center")
			.borderWidth(1)
			.borderColor("black")
		.end()
		.build();

		Theme THM_FORM_BUTTONS_TEST1 = Theme.builder("THM_FORM_BUTTONS_TEST1")
		.addAttribute()
			.borderWidth(0)
			.padding(10)
		.end()
		.build();


		Theme THM_FORM_BUTTONS_BEHAVIOUR_TEST= Theme.builder("THM_FORM_BUTTONS_BEHAVIOUR_TEST")
		.addAttribute(ThemeAttributeType.PRI_HAS_LABEL, false).end()
		.build();

		Theme THM_PROJECT_COLOR_PRIMARY_VARIANT_LIGHT = Theme.builder("THM_PROJECT_COLOR_PRIMARY_VARIANT_LIGHT")
		.addAttribute()
			.backgroundColor(project.getValue("PRI_COLOR_PRIMARY_VARIANT_LIGHT", "#395268"))
			 .color(project.getValue("PRI_COLOR_PRIMARY_ON", "#FFFFFF"))
		.end()
		.build();  
	
	
	Validation validation = new Validation("VLD_ANYTHING", "Anything", ".*");
	List<Validation> validations = new ArrayList<>();
	validations.add(validation);
	
	ValidationList buttonValidationList = new ValidationList();
	buttonValidationList.setValidationList(validations);

	DataType buttonDataType = new DataType("DTT_BUTTON_EVENT", buttonValidationList, "buttonEvent", "");
	
	Frame3 FRM_FORM_TEST = Frame3.builder("FRM_FORM_TEST")
			.addTheme(THM_FORM_ATTRIBUTES_GENNY).end()
				.question("QUE_JOURNAL_W1D1_GRP")
					.addTheme(THM_FORM_BUTTONS_TEST).dataType(buttonDataType).vcl(VisualControlType.INPUT_WRAPPER).end()
					.addTheme(THM_PROJECT_COLOR_PRIMARY_VARIANT_LIGHT).dataType(buttonDataType).weight(2.0).end()
					.addTheme(THM_FORM_BUTTONS_TEST1).dataType(buttonDataType).vcl(VisualControlType.INPUT_FIELD).end()
					.addTheme(THM_FORM_BUTTONS_BEHAVIOUR_TEST).dataType(buttonDataType).end()
					.addTheme(THM_FORM_VCL_INPUT_GENNY).vcl(VisualControlType.VCL_INPUT).weight(2.0).end()
					.addTheme(THM_FORM_VCL_LABEL_GENNY).vcl(VisualControlType.VCL_LABEL).end()
					.addTheme(THM_FORM_VCL_WRAPPER_GENNY).vcl(VisualControlType.VCL_WRAPPER).end()
					.addTheme(THM_FORM_VCL_ERROR_GENNY).vcl(VisualControlType.VCL_ERROR).end()
					.addTheme(THM_FORM_BEHAVIOUR_GENNY).vcl(VisualControlType.GROUP_WRAPPER).weight(3.0).end()
					.addTheme(THM_FORM_GROUP_LABEL_GENNY).vcl(VisualControlType.GROUP_LABEL).weight(3.0).end()
					.addTheme(THM_BACKGROUND_NONE).weight(3.0).end()
					.addTheme(THM_FORM_GROUP_WRAPPER_GENNY).vcl(VisualControlType.GROUP_WRAPPER).weight(3.0).end()
				.end()
			.build();
	
	

		Frame3 frameCentre = Frame3.builder("FRM_CENTRE").addFrame(FRM_FORM_TEST, FramePosition.NORTH).end()
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

}