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

//	@Test
	public void simple()
	{
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken); // defaults to user anyway
		GennyToken serviceToken = new GennyToken("serviceToken", rules.getServiceToken());

	//	GennyToken serviceToken = new GennyToken("PER_USER1","internmatch","user1","User1","test");
		
		Theme THM_NOT_INHERITBALE = Theme.builder("THM_NOT_INHERITBALE")
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE,true).end()				
				.build();
		Frame3 logo = Frame3.builder("FRM_PROJECT_LOGO")
				.addTheme(THM_NOT_INHERITBALE).end()
				.build();

		Frame3 frameRoot = Frame3.builder("FRM_ROOT")
				.addFrame(logo, FramePosition.NORTH).end()
				.build();

		Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();
		QDataBaseEntityMessage msg = FrameUtils2.toMessage(frameRoot, serviceToken, askMsgs);
		
		String test = JsonUtils.toJson(msg);
		System.out.println(test);
		
	}
	
	
	//@Test
	public void testTree() {

		/* token stuff */
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken); // defaults to user anyway
		GennyToken serviceToken = new GennyToken("serviceToken", rules.getServiceToken());

		Theme THM_DUMMY = Theme.builder("THM_DUMMY").addAttribute().height(100).width(90).end()
				.build();

		Theme THM_LOGO = Theme.builder("THM_LOGO")
		.addAttribute()
			.fit("contain")
			.height(100)
			.width(100)
			.end()
		.build();

		Theme THM_SIDEBAR = Theme.builder("THM_SIDEBAR")
		.addAttribute()
			.backgroundColor("#065B9A")
			.minWidth(300)
			.width("100%")
			.end()
		.build();

		Theme THM_HEADER = Theme.builder("THM_HEADER")
		.addAttribute()
			.backgroundColor("#18639F")
			.height(80)
			.end()
		.build();

		Theme THM_CENTRE = Theme.builder("THM_CENTRE")
		.addAttribute()
			.backgroundColor("#F8F9FA")
			.end()
		.build();

		Theme THM_FOOTER = Theme.builder("THM_FOOTER")
		.addAttribute()
			.backgroundColor("#16649E")
			.bold(true)
			.size("md")
			.height(50)
			.end()
		.build();

		Theme THM_FORM_LABEL_DEFAULT = Theme.builder("THM_FORM_LABEL_DEFAULT")
		.build();

		Theme THM_FORM_DEFAULT_REPLICA = Theme.builder("THM_FORM_DEFAULT_REPLICA")
		.addAttribute()
			.backgroundColor("none").end()
		.addAttribute(ThemeAttributeType.PRI_HAS_LABEL,true)
			.end()
		.build();

		Frame3 frameDummy = Frame3.builder("FRM_DUMMY").addTheme(THM_DUMMY).end().build();

		Frame3 logo = Frame3.builder("FRM_PROJECT_LOGO")
					.addTheme(THM_SIDEBAR).end()
					.question("QUE_LOGO").end()
					.addFrame(frameDummy).end()
					.build();

		Frame3 project = Frame3.builder("FRM_PROJECT")
					.addThemeParent("THM_PROJECT", "size", "md").end()
					.addThemeParent("THM_PROJECT_WEIGHT", "bold", true).end()
					.addThemeParent("THM_PROJECT_COLOR", "color", "white").end()
					.question("QUE_NAME_TWO")
						.addTheme(THM_FORM_LABEL_DEFAULT)
						.vcl(VisualControlType.VCL_LABEL)
						.end()
					.end()
					.addFrame(frameDummy).end()
					.build();

		Frame3 sideBar = Frame3.builder("FRM_SIDEAR")
						.addTheme(THM_SIDEBAR).end()
						.addFrame(frameDummy).end()
						.addFrame(logo, FramePosition.NORTH).end()
						.build();
		Frame3 header = Frame3.builder("FRM_HEADER")
						.addTheme(THM_HEADER).end()
						.addFrame(frameDummy).end()
						.addFrame(project).end()
						.build();
		Frame3 centre = Frame3.builder("FRM_CENTRE")
						.addTheme(THM_CENTRE).end()
						.addFrame(frameDummy).end()
						.build();

		Frame3 poweredBy = Frame3.builder("FRM_POWERED_BBY")
						.addThemeParent("THM_WIDTH", "width", 200).end()
						.addThemeParent("THM_COLOR", "color", "white").end()
						.question("QUE_POWERED_BY_GRP")
							.addTheme(THM_FORM_LABEL_DEFAULT)
							.vcl(VisualControlType.VCL_LABEL)
							.end()
						.addTheme(THM_FORM_DEFAULT_REPLICA)
							.weight(3.0)
							.end()
						.end()
						.build();

		Frame3 footer = Frame3.builder("FRM_FOOTER")
						.addTheme(THM_FOOTER).end()
						.addFrame(frameDummy).end()
						.addFrame(poweredBy, FramePosition.EAST).end()
						.build();

	//	Frame3 bucket = generateBucket();

			Theme THM_BUCKET_LABEL = Theme.builder("THM_BUCKET_LABEL")
				.addAttribute()
					.textAlign("center")
					.margin(0)
					.end()
				.build();

				Theme THM_BUCKET = Theme.builder("THM_BUCKET")
				.addAttribute()
					.backgroundColor("#F8F9FA")
					.overflowX("auto")
					.overflowY("auto")
					.width("100%")
					.end()
				.build();

				Theme THM_NOT_INHERITBALE = Theme.builder("THM_NOT_INHERITBALE")
						.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_TITLE, true).end()
						.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_DESCRIPTION, true).end()
						.addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true).end()
						.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE,true).end()				
						.build();

				Theme THM_BUCKET_COLUMN_HEADER = Theme.builder("THM_BUCKET_COLUMN_HEADER")
				.addAttribute()
					.backgroundColor("#d1d1d1")
					.height(20)
					.end()
				.build();

				Theme THM_BUCKET_COLUMN = Theme.builder("THM_BUCKET_COLUMN")
				.addAttribute()
					.backgroundColor("#EAEAEA")
					.minWidth(300)
					.width("100%")
					.margin(20)
					.textAlign("center")
					.end()
				.build();


				Frame3 columnHeader = Frame3.builder("FRM_BUCKET_COLUMN_HEADER")
									.addTheme(THM_BUCKET_COLUMN_HEADER).end()
									.question("QUE_NAME_TWO")
										.addTheme(THM_BUCKET_LABEL)
										.vcl(VisualControlType.VCL_LABEL)
										.end()
									.end()
									.build();

				Frame3 bucketColumn1 = Frame3.builder("FRM_BUCKET_COLUMN_ONE")
								
									.addTheme(THM_BUCKET_COLUMN).end()
									// .question("QUE_NAME_TWO")
									// 	.addTheme(THM_BUCKET_LABEL)
									// 	.vcl(VisualControlType.VCL_LABEL)
									// 	.end()
									// .end()
									.addTheme(THM_NOT_INHERITBALE).end()
									.addFrame(columnHeader, FramePosition.NORTH).end()
									.build();
				Frame3 bucketColumn2 = Frame3.builder("FRM_BUCKET_COLUMN_TWO")
									.addTheme(THM_BUCKET_COLUMN).end()
									// .question("QUE_NAME_TWO")
									// 	.addTheme(THM_BUCKET_LABEL)
									// 	.vcl(VisualControlType.VCL_LABEL)
									// 	.end()
									// .end()
									.build();
				Frame3 bucketColumn3 = Frame3.builder("FRM_BUCKET_COLUMN_THREE")
									.addTheme(THM_BUCKET_COLUMN).end()
									// .question("QUE_NAME_TWO")
									// 	.addTheme(THM_BUCKET_LABEL)
									// 	.vcl(VisualControlType.VCL_LABEL)
									// 	.end()
									// .end()
									.build();
				Frame3 bucketColumn4 = Frame3.builder("FRM_BUCKET_COLUMN_FOUR")
									.addTheme(THM_BUCKET_COLUMN).end()
									// .question("QUE_NAME_TWO")
									// 	.addTheme(THM_BUCKET_LABEL)
									// 	.vcl(VisualControlType.VCL_LABEL)
									// 	.end()
									// .end()
									.build();
				Frame3 bucketColumn5 = Frame3.builder("FRM_BUCKET_COLUMN_FIVE")
									.addTheme(THM_BUCKET_COLUMN).end()
									// .question("QUE_NAME_TWO")
									// 	.addTheme(THM_BUCKET_LABEL)
									// 	.vcl(VisualControlType.VCL_LABEL)
									// 	.end()
									// .end()
									.build();
				Frame3 bucketColumn6 = Frame3.builder("FRM_BUCKET_COLUMN_SIX")
									.addTheme(THM_BUCKET_COLUMN).end()
									// .question("QUE_NAME_TWO")
									// 	.addTheme(THM_BUCKET_LABEL)
									// 	.vcl(VisualControlType.VCL_LABEL)
									// 	.end()
									// .end()
									.build();
				Frame3 bucketColumn7 = Frame3.builder("FRM_BUCKET_COLUMN_SEVEN")
									.addTheme(THM_BUCKET_COLUMN).end()
									// .question("QUE_NAME_TWO")
									// 	.addTheme(THM_BUCKET_LABEL)
									// 	.vcl(VisualControlType.VCL_LABEL)
									// 	.end()
									// .end()
									.build();

				Frame3 bucket = Frame3.builder("FRM_BUCKET")
								.addTheme(THM_BUCKET).end()
								.addFrame(bucketColumn1, FramePosition.WEST).end()
								.addFrame(bucketColumn2, FramePosition.WEST).end()
								.addFrame(bucketColumn3, FramePosition.WEST).end()
								.addFrame(bucketColumn4, FramePosition.WEST).end()
								.addFrame(bucketColumn5, FramePosition.WEST).end()
								.addFrame(bucketColumn6, FramePosition.WEST).end()
								.addFrame(bucketColumn7, FramePosition.WEST).end()
								.build();

		
		Frame3 frameRoot = Frame3.builder("FRM_ROOT")
						.addFrame(header, FramePosition.NORTH).end()
						.addFrame(sideBar, FramePosition.WEST).end()
						.addFrame(bucket, FramePosition.CENTRE).end()
						.addFrame(footer, FramePosition.SOUTH).end()
						.build();

		Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();
		QDataBaseEntityMessage msg = FrameUtils2.toMessage(frameRoot, serviceToken, askMsgs);
		
		String test = JsonUtils.toJson(msg);
		
		rules.publishCmd(msg);
		for (QDataAskMessage askMsg : askMsgs) {
			rules.publishCmd(askMsg, serviceToken.getUserCode(), userToken.getUserCode()); // Send associated
		}
		System.out.println("Sent");
	}

	public Frame3 generateBucket(){

		Theme THM_BUCKET_LABEL = Theme.builder("THM_BUCKET_LABEL")
		.addAttribute().textAlign("center").end()
		.addAttribute().margin(0).end()
		.build();

		Theme THM_BUCKET = Theme.builder("THM_BUCKET")
		.addAttribute().backgroundColor("#F8F9FA").end()
		.addAttribute().overflowX("auto").end()
		.addAttribute().overflowY("auto").end()
		.addAttribute().width("100%").end()
		.build();

		Theme THM_NOT_INHERITBALE = Theme.builder("THM_NOT_INHERITBALE")
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_TITLE, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_DESCRIPTION, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true).end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE,true).end()				
				.build();

		Theme THM_BUCKET_COLUMN_HEADER = Theme.builder("THM_BUCKET_COLUMN_HEADER")
		.addAttribute().backgroundColor("#d1d1d1").end()
		.addAttribute().height(20).end()
		.build();

		Theme THM_BUCKET_COLUMN = Theme.builder("THM_BUCKET_COLUMN")
		.addAttribute().backgroundColor("#EAEAEA").end()
		.addAttribute().minWidth(300).end()
		.addAttribute().width("100%").end()
		.addAttribute().margin(20).end()
		.addAttribute().textAlign("center").end()
		.build();


		Frame3 columnHeader = Frame3.builder("FRM_BUCKET_COLUMN_HEADER")
							.addTheme(THM_BUCKET_COLUMN_HEADER).end()
							.question("QUE_NAME_TWO")
								.addTheme(THM_BUCKET_LABEL)
								.vcl(VisualControlType.VCL_LABEL)
								.end()
							.end()
							.build();

		Frame3 bucketColumn1 = Frame3.builder("FRM_BUCKET_COLUMN_ONE")
						
							.addTheme(THM_BUCKET_COLUMN).end()
							// .question("QUE_NAME_TWO")
							// 	.addTheme(THM_BUCKET_LABEL)
							// 	.vcl(VisualControlType.VCL_LABEL)
							// 	.end()
							// .end()
							.addTheme(THM_NOT_INHERITBALE).end()
							.addFrame(columnHeader, FramePosition.NORTH).end()
							.build();
		Frame3 bucketColumn2 = Frame3.builder("FRM_BUCKET_COLUMN_TWO")
							.addTheme(THM_BUCKET_COLUMN).end()
							// .question("QUE_NAME_TWO")
							// 	.addTheme(THM_BUCKET_LABEL)
							// 	.vcl(VisualControlType.VCL_LABEL)
							// 	.end()
							// .end()
							.build();
		Frame3 bucketColumn3 = Frame3.builder("FRM_BUCKET_COLUMN_THREE")
							.addTheme(THM_BUCKET_COLUMN).end()
							// .question("QUE_NAME_TWO")
							// 	.addTheme(THM_BUCKET_LABEL)
							// 	.vcl(VisualControlType.VCL_LABEL)
							// 	.end()
							// .end()
							.build();
		Frame3 bucketColumn4 = Frame3.builder("FRM_BUCKET_COLUMN_FOUR")
							.addTheme(THM_BUCKET_COLUMN).end()
							// .question("QUE_NAME_TWO")
							// 	.addTheme(THM_BUCKET_LABEL)
							// 	.vcl(VisualControlType.VCL_LABEL)
							// 	.end()
							// .end()
							.build();
		Frame3 bucketColumn5 = Frame3.builder("FRM_BUCKET_COLUMN_FIVE")
							.addTheme(THM_BUCKET_COLUMN).end()
							// .question("QUE_NAME_TWO")
							// 	.addTheme(THM_BUCKET_LABEL)
							// 	.vcl(VisualControlType.VCL_LABEL)
							// 	.end()
							// .end()
							.build();
		Frame3 bucketColumn6 = Frame3.builder("FRM_BUCKET_COLUMN_SIX")
							.addTheme(THM_BUCKET_COLUMN).end()
							// .question("QUE_NAME_TWO")
							// 	.addTheme(THM_BUCKET_LABEL)
							// 	.vcl(VisualControlType.VCL_LABEL)
							// 	.end()
							// .end()
							.build();
		Frame3 bucketColumn7 = Frame3.builder("FRM_BUCKET_COLUMN_SEVEN")
							.addTheme(THM_BUCKET_COLUMN).end()
							// .question("QUE_NAME_TWO")
							// 	.addTheme(THM_BUCKET_LABEL)
							// 	.vcl(VisualControlType.VCL_LABEL)
							// 	.end()
							// .end()
							.build();

		Frame3 bucket = Frame3.builder("FRM_BUCKET")
						.addTheme(THM_BUCKET).end()
						.addFrame(bucketColumn1, FramePosition.WEST).end()
						.addFrame(bucketColumn2, FramePosition.WEST).end()
						.addFrame(bucketColumn3, FramePosition.WEST).end()
						.addFrame(bucketColumn4, FramePosition.WEST).end()
						.addFrame(bucketColumn5, FramePosition.WEST).end()
						.addFrame(bucketColumn6, FramePosition.WEST).end()
						.addFrame(bucketColumn7, FramePosition.WEST).end()
						.build();

		return bucket;
	}
	// @Test
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

		Frame3 frameTest = Frame3.builder("FRM_TEST").addTheme(THM_COLOR_GREY).end()
				.addFrame(frameEast, FramePosition.EAST).end().addFrame(frameWest, FramePosition.WEST).end()
				.addFrame(frameNorth, FramePosition.NORTH).end().addFrame(frameSouth, FramePosition.SOUTH).end()
				.addFrame(frameCentre, FramePosition.CENTRE).end().build();

		Frame3 frameRoot = Frame3.builder("FRM_ROOT").addFrame(frameTest).end().build();

		/* send message */
		Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();
		QDataBaseEntityMessage msg = FrameUtils2.toMessage(frameRoot, serviceToken, askMsgs);
		rules.publishCmd(msg);
		System.out.println("Sent");

	}

	public void testTheme() {
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken); // defaults to user anyway
		GennyToken serviceToken = new GennyToken("serviceToken", rules.getServiceToken());

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

		Theme THM_BACKGROUND_ORANGE = Theme.builder("THM_BACKGROUND_ORANGE").addAttribute().backgroundColor("orange").end()
				.build();

		
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
				.addAttribute(ThemeAttributeType.PRI_CONTENT_ERROR).borderColor("red").color("red").end()
				.build();

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
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
				.build();

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
public void addInternshipOne() {
	// getting the tokens
	GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
	QRules rules = getQRules(userToken);
	GennyToken serviceToken = new GennyToken("serviceToken", rules.getServiceToken());

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


	Frame3 frameSubmit = Frame3.builder("FRM_SUBMIT").addTheme(THM_SUBMIT).end().question("QUE_TABLE_NEXT_BTN")
			.addTheme(THM_BUTTONS).weight(2.0).end().end().build();

	Frame3 frameCancel = Frame3.builder("FRM_CANCEL").addTheme(THM_CANCEL).end().question("QUE_CANCEL_BUTTON")
			.addTheme(THM_BUTTONS).weight(2.0).end().end().build();

	Frame3 frameForm = Frame3.builder("FRM_FORM").addTheme(THM_COLOR_BLACK).end()
			.question("QUE_ADD_INTERNSHIP_AGENT_STEP_ONE_GRP").addTheme(THM_FORM_INPUT_DEFAULT)
			.vcl(VisualControlType.VCL_INPUT).weight(2.0).end().addTheme(THM_FORM_LABEL_DEFAULT)
			.vcl(VisualControlType.VCL_LABEL).end().addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER)
			.end().addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT)
			.weight(3.0).end().addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();

	Frame3 frameCentre = Frame3.builder("FRM_CENTRE").addFrame(frameCancel, FramePosition.SOUTH).end()
			.addFrame(frameForm, FramePosition.CENTRE).end().addFrame(frameSubmit, FramePosition.SOUTH).end().build();

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

//@Test
public void addInternshipTwo() {
	// getting the tokens
	GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
	QRules rules = getQRules(userToken);
	GennyToken serviceToken = new GennyToken("serviceToken", rules.getServiceToken());

	// building the themes and the footers
	Theme THM_COLOR_GREY = Theme.builder("THM_COLOR_RED").addAttribute().backgroundColor("red").end().build();

	Theme THM_SUBMIT = Theme.builder("THM_SUBMIT").addAttribute().backgroundColor("#1183c8").end().addAttribute()
			.justifyContent("center").end().build();

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
			.backgroundColor("white").padding(10).margin(10).maxWidth(700).width("100%").borderColour("#000000").borderStyle("solid").borderWidth(2).shadowColor("#000").shadowOpacity(0.4)
			.shadowRadius(5).shadowOffset().width(0).height(0).end().end()
			.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_TITLE, true).end()
			.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_DESCRIPTION, true).end()
			.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

	Theme THM_BUTTONS = Theme.builder("THM_BUTTONS").
			addAttribute()
			.backgroundColor("#ffffff")
			.padding(10)
			.justifyContent("center")
			.borderColor("#000000")
			.margin(4)
			.maxWidth(700)
			.width("100%")
			.shadowColor("#000")
			.shadowOpacity(0.8).shadowRadius(5).end()
			.build();

	Theme THM_OF = Theme.builder("THM_OF").
	addAttribute()
		.overflowY("auto")
		.end()
	.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
		.end()
	.build();

	Frame3 frameSubmit = Frame3.builder("FRM_SUBMIT")
			.addTheme(THM_SUBMIT).end()
			.addTheme(THM_OF).end()
			.question("QUE_SUBMIT_BUTTON")
			.addTheme(THM_BUTTONS).weight(2.0).end()
			.end()
			.build();

	Frame3 frameForm = Frame3.builder("FRM_FORM")
			.addTheme(THM_COLOR_BLACK).end()
			.addTheme(THM_OF).end()
			.question("QUE_ADD_INTERNSHIP_AGENT_STEP_TWO_GRP")
				.addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT).end()
				.addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
				.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end()
				.addTheme(THM_FORM_DEFAULT).end()
				.addTheme(THM_FORM_CONTAINER_DEFAULT).end()
			.end()
			.build();

	Frame3 frameCentre = Frame3.builder("FRM_CENTRE")
			.addFrame(frameForm, FramePosition.CENTRE).end()
			.addFrame(frameSubmit, FramePosition.SOUTH).end()
			.build();

	Frame3 frameMain = Frame3.builder("FRM_MAIN")
			.addTheme(THM_COLOR_GREY).end()
			.addFrame(frameCentre, FramePosition.CENTRE).end()
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

@Test
public void addAgentDetails() {
	// getting the tokens
	GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
	QRules rules = getQRules(userToken);
	GennyToken serviceToken = new GennyToken("serviceToken", rules.getServiceToken());

	// building the themes and the footers
	Theme THM_COLOR_GREY = Theme.builder("THM_COLOR_RED").addAttribute().backgroundColor("red").end().build();

	Theme THM_SUBMIT = Theme.builder("THM_SUBMIT").addAttribute().backgroundColor("#1183c8").end().addAttribute()
			.justifyContent("center").end().build();

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
			.backgroundColor("white").padding(10).margin(10).maxWidth(700).width("100%").borderColour("#000000").borderStyle("solid").borderWidth(2).shadowColor("#000").shadowOpacity(0.4)
			.shadowRadius(5).shadowOffset().width(0).height(0).end().end()
			.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_TITLE, true).end()
			.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_DESCRIPTION, true).end()
			.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

	Theme THM_BUTTONS = Theme.builder("THM_BUTTONS").
			addAttribute()
			.backgroundColor("#ffffff")
			.padding(10)
			.justifyContent("center")
			.borderColor("#000000")
			.margin(4)
			.maxWidth(700)
			.width("100%")
			.shadowColor("#000")

			.shadowOpacity(0.8).shadowRadius(5).end()
			.build();

	Theme THM_OF = Theme.builder("THM_OF").
	addAttribute()
		.overflowY("auto")
		.end()
	.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
		.end()
	.build();

	Frame3 frameSubmit = Frame3.builder("FRM_SUBMIT")
			.addTheme(THM_SUBMIT).end()
			.addTheme(THM_OF).end()
			.question("QUE_SUBMIT_BUTTON")
			.addTheme(THM_BUTTONS).weight(2.0).end()
			.end()
			.build();

	Frame3 frameForm = Frame3.builder("FRM_FORM")
			.addTheme(THM_COLOR_BLACK).end()
			.addTheme(THM_OF).end()
			.question("QUE_ADD_AGENT_GRP")
				.addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT).end()
				.addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
				.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end()
				.addTheme(THM_FORM_DEFAULT).end()
				.addTheme(THM_FORM_CONTAINER_DEFAULT).end()
			.end()
			.build();

	Frame3 frameCentre = Frame3.builder("FRM_CENTRE")
			.addFrame(frameForm, FramePosition.CENTRE).end()
			.addFrame(frameSubmit, FramePosition.SOUTH).end()
			.build();

	Frame3 frameMain = Frame3.builder("FRM_MAIN")
			.addTheme(THM_COLOR_GREY).end()
			.addFrame(frameCentre, FramePosition.CENTRE).end()
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


// @Test

	public void testDesktopPageDisplay() {

		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken); // defaults to user anyway
		GennyToken serviceToken = new GennyToken("serviceToken", rules.getServiceToken());

		setUpCache(GennySettings.mainrealm, userToken);

		Frame3 centre = Frame3.builder("FRM_CENTRE").build();

		Frame3 profile = Frame3.builder("FRM_PROFILE").addThemeParent("THM_DISPLAY_HORIZONTAL", "flexDirection", "row")
				.end().addThemeParent("THM_BACKGROUND_RED", "backgroundColor", "red").end().build();

		Frame3 header = Frame3.builder("FRM_HEADER").addFrame(profile, FramePosition.EAST).build();

		Frame3 sidebar = Frame3.builder("FRM_SIDEBAR").addThemeParent("THM_WIDTH_300", "width", 300).end()
				.addThemeParent("THM_DISPLAY_VERTICAL", "flexDirection", "column").end()
				.addThemeParent("THM_DISPLAY_VERTICAL", "justifyContent", "flex-start").end()
				.addThemeParent("THM_BACKGROUND_RED", "backgroundColor", "red").end().question("QUE_USER_PROFILE_GRP")
				.end().build();

		Frame3 notes = Frame3.builder("FRM_NOTES").addThemeParent("THM_WIDTH_300", "width", 300).end()
				.addThemeParent("THM_DISPLAY_VERTICAL", "flexDirection", "column").end()
				.addThemeParent("THM_DISPLAY_VERTICAL", "justifyContent", "flex-start").end().build();

		Frame3 footer = Frame3.builder("FRM_FOOTER").build();

		Frame3 mainFrame = Frame3.builder("FRM_MAIN").addFrame(header, FramePosition.NORTH).end()
				.addFrame(sidebar, FramePosition.EAST).end().addFrame(footer, FramePosition.SOUTH).end()
				.addFrame(centre, FramePosition.CENTRE).end().build();

		Frame3 desktop = Frame3.builder("FRM_ROOT").addThemeParent("THM_BACKGROUND_GRAY", "backgroundColor", "gray")
				.end().addThemeParent("THM_BACKGROUND_INTERNMATCH", "backgroundColor", "#233a4e").end()
				.addThemeParent("THM_COLOR_WHITE", "backgroundColor", "white").end()
				.addThemeParent("THM_COLOR_BLACK", ThemeAttributeType.PRI_CONTENT, "backgroundColor", "black").end()

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
		String apiUrl = GennySettings.qwandaServiceUrl + "/service/forms";
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