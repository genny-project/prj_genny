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
import life.genny.utils.VertxUtils;

public class AdamTest extends GennyJbpmBaseTest {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	private static final String WFE_SEND_FORMS = "send_forms.bpmn";
	private static final String WFE_SHOW_FORM = "show_form.bpmn";
	private static final String WFE_AUTH_INIT = "auth_init.bpmn";
	private static final String WFE_SEND_LLAMA = "send_llama.bpmn";

	public AdamTest() {
		super(false);
	}

	@Test
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
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

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
		
		VertxUtils.putObject(serviceToken.getRealm(), "", "DESKTOP", msg,serviceToken.getToken());
		
		QDataBaseEntityMessage msg2 = VertxUtils.getObject(serviceToken.getRealm(), "", "DESKTOP", QDataBaseEntityMessage.class,serviceToken.getToken()); 

		/* send message */
		rules.publishCmd(msg2); // Send QDataBaseEntityMessage

		System.out.println("Sending Asks");
		for (QDataAskMessage askMsg : askMsgs) {
			rules.publishCmd(askMsg, serviceToken.getUserCode(), userToken.getUserCode()); // Send associated
																							// QDataAskMessage
		}

		System.out.println("Sent");
	}

	// @Test

	public void testAuthInit() {

		QEventMessage msg = new QEventMessage("EVT_MSG", "AUTH_INIT");

		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules qRules = getQRules(userToken); // defaults to user anyway

		GennyKieSession gks = GennyKieSession.builder().addJbpm("auth_init.bpmn").addJbpm("show_form.bpmn")
				.addJbpm("send_forms.bpmn").addJbpm("send_llama.bpmn").addFact("qRules", qRules).addFact("msg", msg)
				.addFact("eb", eventBusMock).addToken(new GennyToken("serviceUser", qRules.getServiceToken()))
				.addToken(userToken).build();

		gks.start();
		gks.advanceSeconds(20, false);
		gks.close();

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