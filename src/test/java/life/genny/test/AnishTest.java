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

public class AnishTest extends GennyJbpmBaseTest {

        private static final Logger log = LoggerFactory
                        .getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

        private static final String WFE_SEND_FORMS = "rulesCurrent/shared/_BPMN_WORKFLOWS/send_forms.bpmn";
        private static final String WFE_SHOW_FORM = "rulesCurrent/shared/_BPMN_WORKFLOWS/show_form.bpmn";
        private static final String WFE_AUTH_INIT = "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/auth_init.bpmn";
        private static final String WFE_SEND_LLAMA = "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/send_llama.bpmn";
        private static final String DRL_PROJECT = "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/SendUserData/project.drl";
        private static final String DRL_USER_COMPANY = "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/SendUserData/user_company.drl";
        private static final String DRL_USER = "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/SendUserData/user.drl";
        private static final String DRL_EVENT_LISTENER_SERVICE_SETUP = "rulesCurrent/shared/_BPMN_WORKFLOWS/Initialise_Project/eventListenerServiceSetup.drl";
        private static final String DRL_EVENT_LISTENER_USER_SETUP = "rulesCurrent/shared/_BPMN_WORKFLOWS/Initialise_Project/eventListenerUserSetup.drl";

        public AnishTest() {
                super(false);
        }

       // @Test
        public void simpleTest() {
                GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
                GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User",
                                "service");
                QRules qRules = new QRules(eventBusMock, userToken.getToken(), userToken.getAdecodedTokenMap());
                qRules.set("realm", userToken.getRealm());
                qRules.setServiceToken(serviceToken.getToken());

                Theme THM_INHERITABLE = Theme.builder("THM_INHERITABLE")
                                .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, true).end().build();
                Frame3 logo = Frame3.builder("FRM_PROJECT_LOGO").addTheme(THM_INHERITABLE).end().build();

                Frame3 frameRoot = Frame3.builder("FRM_ROOT").addFrame(logo, FramePosition.NORTH).end().build();

                Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();
                QDataBaseEntityMessage msg = FrameUtils2.toMessage(frameRoot, serviceToken, askMsgs);

                String test = JsonUtils.toJson(msg);
                System.out.println(test);
        }

        @Test
        public void testTree() {

                /* token stuff */
                GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
                QRules rules = getQRules(userToken); // defaults to user anyway
                GennyToken serviceToken = new GennyToken("serviceToken", rules.getServiceToken());

                Theme THM_DUMMY = Theme.builder("THM_DUMMY").addAttribute().height(100).end().addAttribute().width(90)
                                .end().build();

                Theme THM_LOGO = Theme.builder("THM_LOGO").addAttribute().fit("contain").end().addAttribute()
                                .height(100).end().addAttribute().width(100).end().build();

                Theme THM_SIDEBAR = Theme.builder("THM_SIDEBAR").addAttribute().backgroundColor("#065B9A").end()
                                .addAttribute().minWidth(300).end().addAttribute().width(100).end().build();

                Theme THM_HEADER = Theme.builder("THM_HEADER").addAttribute().backgroundColor("#18639F").end()
                                .addAttribute().height(80).end().build();

                Theme THM_FOOTER = Theme.builder("THM_FOOTER").addAttribute().backgroundColor("#16649E").end()
                                .addAttribute().bold(true).size("md").end().addAttribute().height(50).end().build();

                Theme THM_FORM_LABEL_DEFAULT = Theme.builder("THM_FORM_LABEL_DEFAULT").addAttribute().end().build();

                Theme THM_FORM_DEFAULT_REPLICA = Theme.builder("THM_FORM_DEFAULT_REPLICA").addAttribute()
                                .backgroundColor("none").end().addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true)
                                .end().build();

                Frame3 frameDummy = Frame3.builder("FRM_DUMMY").addTheme(THM_DUMMY).end().build();

                Frame3 logo = Frame3.builder("FRM_PROJECT_LOGO").addTheme(THM_SIDEBAR).end().question("QUE_LOGO")
                                .addTheme(THM_LOGO).vcl(VisualControlType.VCL_WRAPPER).end().end().addFrame(frameDummy)
                                .end().build();

                Frame3 project = Frame3.builder("FRM_PROJECT").addThemeParent("THM_PROJECT", "size", "md").end()
                                .addThemeParent("THM_PROJECT_WEIGHT", "bold", true).end()
                                .addThemeParent("THM_PROJECT_COLOR", "color", "white").end().question("QUE_NAME_TWO")
                                .addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end().end()
                                .addFrame(frameDummy).end().build();

                Frame3 sideBar = Frame3.builder("FRM_SIDEAR").addTheme(THM_SIDEBAR).end().addFrame(frameDummy).end()
                                .addFrame(logo, FramePosition.NORTH).end().build();
                Frame3 header = Frame3.builder("FRM_HEADER").addTheme(THM_HEADER).end().addFrame(frameDummy).end()
                                .addFrame(project).end().build();

                Frame3 poweredBy = Frame3.builder("FRM_POWERED_BBY").addThemeParent("THM_WIDTH", "width", 200).end()
                                .addThemeParent("THM_COLOR", "color", "white").end().question("QUE_POWERED_BY_GRP")
                                .addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
                                .addTheme(THM_FORM_DEFAULT_REPLICA).weight(3.0).end().end().build();

                Frame3 footer = Frame3.builder("FRM_FOOTER").addTheme(THM_FOOTER).end().addFrame(frameDummy).end()
                                .addFrame(poweredBy, FramePosition.EAST).end().build();

                Frame3 tabs = generateTabs();

                Frame3 frameRoot = Frame3.builder("FRM_ROOT").addFrame(header, FramePosition.NORTH).end()
                                .addFrame(sideBar, FramePosition.WEST).end().addFrame(tabs, FramePosition.CENTRE).end()
                                .addFrame(footer, FramePosition.SOUTH).end().build();

                Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();
                QDataBaseEntityMessage msg = FrameUtils2.toMessage(frameRoot, serviceToken, askMsgs);
                rules.publishCmd(msg);
                for (QDataAskMessage askMsg : askMsgs) {
                        rules.publishCmd(askMsg, serviceToken.getUserCode(), userToken.getUserCode()); // Send
                                                                                                       // associated
                }
                System.out.println("Sent");
        }

        public Frame3 generateBucket() {

                Theme THM_FORM_INPUT_DEFAULT = Theme.builder("THM_FORM_INPUT_DEFAULT")
                        .addAttribute().borderBottomWidth(1).color("white")
                        .borderColor("#ddd").borderStyle("solid").placeholderColor("#888").end()
                        .addAttribute(ThemeAttributeType.PRI_CONTENT_HOVER).borderColor("#aaa").end()
                        .addAttribute(ThemeAttributeType.PRI_CONTENT_ACTIVE).borderColor("green").end()
                        .addAttribute(ThemeAttributeType.PRI_CONTENT_ERROR).borderColor("red").color("red").end().build();

                Theme THM_BUCKET_LABEL = Theme.builder("THM_BUCKET_LABEL")
                                        .addAttribute().textAlign("center")
                                        .bold(true)
                                        .end().build();

                Theme THM_BUCKET = Theme.builder("THM_BUCKET")
                                .addAttribute()
                                .backgroundColor("#F8F9FA")
                                .overflowX("auto")
                                .overflowY("auto")
                                .width("100%")
                                .end()
                                .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                .build();

                Theme THM_BUCKET_COLUMN = Theme.builder("THM_BUCKET_COLUMN")
                                .addAttribute()
                                .backgroundColor("#EAEAEA")
                                .minWidth(300)
                                .width("100%")
                                .margin(20)
                                .textAlign("center")
                                .flexDirection("column").end()
                                .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                .build();

                Theme THM_TEST1 = Theme.builder("THM_TEST1")
                                .addAttribute()
                                .backgroundColor("green")
                                .textAlign("center")
                                .width("100%")
                                .height(30)
                                .end().build();
                Theme THM_TEST2 = Theme.builder("THM_TEST2")
                                .addAttribute()
                                .backgroundColor("pink")
                                .width("100%")
                                .height(20)
                                .end().build();
                Theme THM_TEST3 = Theme.builder("THM_TEST3")
                                .addAttribute()
                                .backgroundColor("yellow")
                                .height(70)
                                .end().build();

                Frame3 test1 = Frame3.builder("FRM_TEST1")
                                .addTheme(THM_TEST1).end()
                                .question("QUE_USER_PROFILE_GRP")
                                	.addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT)
                                	.weight(2.0)
                                	.end()
                            	.end()
                                //.addTheme(THM_BUCKET_LABEL).vcl(VisualControlType.VCL_LABEL).end().end()
                                .build();
                Frame3 test2 = Frame3.builder("FRM_TEST2")
                                .addTheme(THM_TEST2).end()
                                .question("QUE_LASTNAME")
                                .addTheme(THM_BUCKET_LABEL).vcl(VisualControlType.VCL_LABEL).end().end()
                                .build();
                Frame3 test3 = Frame3.builder("FRM_TEST3")
                                .addTheme(THM_TEST3).end()
                                .question("QUE_EMAIL")
                                .addTheme(THM_BUCKET_LABEL).vcl(VisualControlType.VCL_LABEL).end().end()
                                .build();
                Frame3 bucketColumn1 = Frame3.builder("FRM_BUCKET_COLUMN_ONE")
                                .addTheme(THM_BUCKET_COLUMN).end()
                                .addFrame(test1, FramePosition.NORTH).end()
                                .addFrame(test2, FramePosition.NORTH).end()
                                .addFrame(test3, FramePosition.NORTH).end()
                                .build();
                Frame3 bucketColumn2 = Frame3.builder("FRM_BUCKET_COLUMN_TWO")
                                .addTheme(THM_BUCKET_COLUMN).end()
                                .build();
                Frame3 bucketColumn3 = Frame3.builder("FRM_BUCKET_COLUMN_THREE")
                                .addTheme(THM_BUCKET_COLUMN).end()
                                .build();
                Frame3 bucketColumn4 = Frame3.builder("FRM_BUCKET_COLUMN_FOUR")
                                .addTheme(THM_BUCKET_COLUMN).end()
                                .build();
                Frame3 bucketColumn5 = Frame3.builder("FRM_BUCKET_COLUMN_FIVE")
                                .addTheme(THM_BUCKET_COLUMN).end()
                                .build();
                Frame3 bucketColumn6 = Frame3.builder("FRM_BUCKET_COLUMN_SIX")
                                .addTheme(THM_BUCKET_COLUMN).end()
                                .build();
                Frame3 bucketColumn7 = Frame3.builder("FRM_BUCKET_COLUMN_SEVEN")
                                .addTheme(THM_BUCKET_COLUMN).end()
                                .build();

                Frame3 bucket = Frame3.builder("FRM_BUCKET")
                                .addTheme(THM_BUCKET).end()
                                .addFrame(bucketColumn1, FramePosition.WEST).end()
                                .addFrame(bucketColumn2, FramePosition.WEST).end()
                                .addFrame(bucketColumn3, FramePosition.WEST).end()
                                .addFrame(bucketColumn4, FramePosition.WEST).end()
                                .addFrame(bucketColumn5, FramePosition.WEST).end()
                                .addFrame(bucketColumn6, FramePosition.WEST).end()
                                .addFrame(bucketColumn7, FramePosition.WEST).end().build();

                return bucket;
        }

        public Frame3 generateTabs() {

                Theme THM_TABS = Theme.builder("THM_TABS").addAttribute().backgroundColor("red").end().addAttribute()
                                .flexDirection("column").end().build();

                Theme THM_BUCKET_LABEL = Theme.builder("THM_BUCKET_LABEL").addAttribute().textAlign("center").end().build();

                Theme THM_TAB_HEADER = Theme.builder("THM_TAB_HEADER")
                                // .addAttribute().backgroundColor("green").end()
                                .addAttribute().backgroundColor("#065B9A").end().addAttribute().flexDirection("row")
                                .end().addAttribute().marginRight(5).end().build();

                Theme THM_TAB_CONTENT = Theme.builder("THM_TAB_CONTENT")
                                // .addAttribute().backgroundColor("yellow").end()
                                .addAttribute().backgroundColor("#F8F9FA").end().build();

                Theme THM_TAB = Theme.builder("THM_TAB").addAttribute().backgroundColor("#3F505F").borderStyle("solid")
                                .borderColor("white").color("white").height(40).end().build();

                Frame3 tab1 = Frame3.builder("FRM_TAB_ONE").addTheme(THM_TAB).end().question("QUE_NAME_TWO").addTheme(THM_BUCKET_LABEL).end().end()
                                .build();
                Frame3 tab2 = Frame3.builder("FRM_TAB_TWO").addTheme(THM_TAB).end().question("QUE_NAME_TWO").end()
                                .build();
                Frame3 tab3 = Frame3.builder("FRM_TAB_THREE").addTheme(THM_TAB).end().question("QUE_NAME_TWO").end()
                                .build();

                Frame3 tabHeader = Frame3.builder("FRM_TAB_HEADER").addTheme(THM_TAB_HEADER).end()
                                .addFrame(tab1, FramePosition.WEST).end().addFrame(tab2, FramePosition.WEST).end()
                                .addFrame(tab3, FramePosition.WEST).end().question("QUE_NAME_TWO").end().build();

                Frame3 bucket = generateBucket();

                Frame3 tabContent = Frame3.builder("FRM_TAB_CONTENT").addTheme(THM_TAB_CONTENT).end().addFrame(bucket)
                                .end()
                                // .question("QUE_NAME_TWO").end()
                                .build();

                Frame3 tabs = Frame3.builder("FRM_TABS").addTheme(THM_TABS).end()
                                .addFrame(tabHeader, FramePosition.NORTH).end()
                                .addFrame(tabContent, FramePosition.CENTRE).end().build();
                return tabs;
        }

}