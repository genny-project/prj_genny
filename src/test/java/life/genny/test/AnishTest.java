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
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.rules.QRules;
import life.genny.rules.listeners.JbpmInitListener;
import life.genny.utils.FrameUtils2;

public class AnishTest extends GennyJbpmBaseTest {

        private static final Logger log = LoggerFactory
                        .getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());


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

          public Frame3 generateCards(String count) {
                GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
                GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User",
                                "service");
                QRules rules = new QRules(eventBusMock, userToken.getToken(), userToken.getAdecodedTokenMap());
                rules.set("realm", userToken.getRealm());
                rules.setServiceToken(serviceToken.getToken());

                /* create themes */

                Theme THM_FORM_LABEL_DEFAULT = Theme.builder("THM_FORM_LABEL_DEFAULT")
                                                .addAttribute()
                                                .backgroundColor("white")
                                                .end().build();

                Theme THM_CARD_DEFAULT = Theme.builder("THM_CARD_DEFAULT")
                                .addAttribute().backgroundColor("none").end()
                               // .addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true).end()
                                //.addAttribute(ThemeAttributeType.PRI_HAS_INPUT, false).end()
                                .build();


                Theme THM_CARD = Theme.builder("THM_CARD")
                                .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                .addAttribute()
                                      //  .width(300)
                                        .height(200)
                                        //.backgroundColor("white")
                                        //.backgroundColor("pink")
                                        .backgroundColor("#F8F9FA")
                                        .width("100%")
                                        .shadowColor("#000")
                                        .shadowOpacity(0.4)
                                        .shadowRadius(5)
                                                .shadowOffset()
                                                .width(0)
                                                .height(0)
                                       .end()
                                 .end()
                                .build();

                Theme THM_CARD_LEFT = Theme.builder("THM_CARD_LEFT")
                                .addAttribute()
                                .backgroundColor("#EBECF0")
                                .end().build();
                Theme THM_CARD_CENTRE = Theme.builder("THM_CARD_CENTRE")
                                .addAttribute()
                                .backgroundColor("#FFFFFF")
                                .end().build();
                Theme THM_CARD_RIGHT = Theme.builder("THM_CARD_RIGHT")
                                .addAttribute()
                                .backgroundColor("#E1E2E1")
                                .end().build();

                /* create frames for each question */
                Frame3 frameName = Frame3.builder("FRM_NAME")
                                .question("QUE_NAME_TWO")
                                        .addTheme(THM_CARD_DEFAULT)
                                        .vcl(VisualControlType.VCL_LABEL)
                                        .end()
                                .end().build();
                Frame3 frameEmail = Frame3.builder("FRM_EMAIL")
                                .question("QUE_EMAIL")
                                        .addTheme(THM_CARD_DEFAULT)
                                        .vcl(VisualControlType.VCL_LABEL)
                                        .end()
                                .end().build();
                Frame3 frameMobile = Frame3.builder("FRM_MOBILE")
                                .question("QUE_MOBILE")
                                        .addTheme(THM_CARD_DEFAULT)
                                        .vcl(VisualControlType.VCL_LABEL)
                                        .end()
                                .end().build();
                Frame3 frameDob = Frame3.builder("FRM_DOB")
                                .question("QUE_DOB")
                                        .addTheme(THM_CARD_DEFAULT)
                                        .vcl(VisualControlType.VCL_LABEL)
                                        .end()
                                .end().build();


                /* build left, center, card frame-card */

                Frame3 frameLeftCard = Frame3.builder("FRM_CARD_LEFT")
                                .addTheme(THM_CARD_LEFT).end()
                                .question("QUE_IMAGE")
                                .end().build();
                Frame3 frameCentreCard = Frame3.builder("FRM_CARD_CENTRE")
                                .addTheme(THM_CARD_CENTRE).end()
                                //.question("QUE_NAME_TWO")
                                .addFrame(frameName, FramePosition.NORTH).end()
                                .addFrame(frameEmail, FramePosition.NORTH).end()
                                .addFrame(frameMobile, FramePosition.NORTH).end()
                                .addFrame(frameDob, FramePosition.NORTH).end()

                                .build();
                Frame3 frameRightCard = Frame3.builder("FRM_CARD_RIGHT")
                                .addTheme(THM_CARD_RIGHT).end()
                                .question("QUE_NAME_TWO")
                                .end().build();

                Theme THM_TEST1 = Theme.builder("THM_TEST1").
                                addAttribute().backgroundColor("none").width("100%")
                                .height(70).end().build();

                /* create frames */
                Frame3 frameCard = Frame3.builder("FRM_CARD" +"_"+ count)
                                .addTheme(THM_CARD).end()
                                .question("QUE_NAME_TWO")
                                        .addTheme(THM_FORM_LABEL_DEFAULT)
                                        .vcl(VisualControlType.VCL_WRAPPER)
                                        .end()
                                .end()
                                .addFrame(frameLeftCard, FramePosition.WEST).end()
                                .addFrame(frameCentreCard, FramePosition.WEST).end()
                               // .addFrame(frameRightCard, FramePosition.WEST).end()
                                .build();

                return frameCard;

        }

        @Test
        public void testTree() {

                /* token stuff */
        	QRules rules = GennyJbpmBaseTest.setupLocalService();
        	GennyToken userToken = new GennyToken("userToken", rules.getToken());
        	GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

                // System.out.println("saving answer");


                // rules.baseEntity.saveAnswer(new Answer(serviceToken.getUserCode(), "PRJ_INTERNMATCH", "PRI_USER_PROFILE_PICTURE", "https://imgur.com/a/HLsLP7X"));

                // System.out.println("saved answer");

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

                Frame3 logo = Frame3.builder("FRM_PROJECT_LOGO").addTheme(THM_SIDEBAR).end().question("QUE_IMAGE")
                                .addTheme(THM_LOGO).vcl(VisualControlType.VCL_WRAPPER).end().end().addFrame(frameDummy)
                                .end().build();

                Frame3 project = Frame3.builder("FRM_PROJECT").addThemeParent("THM_PROJECT", "size", "md").end()
                                .addThemeParent("THM_PROJECT_WEIGHT", "bold", true).end()
                                .addThemeParent("THM_PROJECT_COLOR", "color", "white").end().question("QUE_NAME_TWO")
                                .addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end().end()
                                .addFrame(frameDummy).end().build();

                Frame3 sideBar = Frame3.builder("FRM_SIDEBAR").addTheme(THM_SIDEBAR).end().addFrame(frameDummy).end()
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

                Theme THM_MAIN = Theme.builder("THM_MAIN")
                        .addAttribute()
                        .backgroundColor("#F8F9FA").end()
                        //.backgroundColor("pink").end()
                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                        .build();

                Frame3 frameRoot = Frame3.builder("FRM_ROOT")
                                .addFrame(header, FramePosition.NORTH).end()
                                .addFrame(sideBar, FramePosition.WEST).end()
                                .addFrame(tabs, FramePosition.CENTRE).end()
                                .addFrame(footer, FramePosition.SOUTH).end()
                                .build();

                Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();
                QDataBaseEntityMessage msg = FrameUtils2.toMessage(frameRoot, serviceToken, askMsgs);
                rules.publishCmd(msg);
                for (QDataAskMessage askMsg : askMsgs) {
                        rules.publishCmd(askMsg, serviceToken.getUserCode(), userToken.getUserCode()); // Send
                                                                                                       // associated
                }
                // rules.sendTreeData();
                System.out.println("Sent");
        }

        public Frame3 generateBucket() {

                Theme THM_FORM_INPUT_DEFAULT = Theme.builder("THM_FORM_INPUT_DEFAULT")
                                .addAttribute()
                                .borderBottomWidth(5)
                                .color("black")
                                .borderColor("#ddd")
                                .borderStyle("solid")
                                .placeholderColor("#888")
                                .height(70)
                                .end().build();

                Theme THM_BUCKET_LABEL = Theme.builder("THM_BUCKET_LABEL").addAttribute().textAlign("center").bold(true)
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
                                        .flexDirection("column")
                                .end()
                                .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                .build();

                Theme THM_TEST1 = Theme.builder("THM_TEST1")
                                .addAttribute()
                                        .backgroundColor("none")
                                        .width("100%")
                                        .height(70)
                                        .end().build();

                Frame3 available = Frame3.builder("FRM_GRP_AVAILABLE").addTheme(THM_TEST1).end()
                                .question("QUE_GRP_NOT_APPLIED").addTheme(THM_FORM_INPUT_DEFAULT)
                                .vcl(VisualControlType.VCL_INPUT).weight(2.0).end().end().addTheme(THM_BUCKET_LABEL)
                                .end().build();
                Frame3 applied = Frame3.builder("FRM_GRP_APPLIED").addTheme(THM_TEST1).end().question("QUE_GRP_APPLIED")
                                .addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT).weight(2.0).end()
                                .end().addTheme(THM_BUCKET_LABEL).end().build();
                Frame3 shortlisted = Frame3.builder("FRM_GRP_SHORTLISTED").addTheme(THM_TEST1).end()
                                .question("QUE_GRP_SHORTLISTED").addTheme(THM_FORM_INPUT_DEFAULT)
                                .vcl(VisualControlType.VCL_INPUT).weight(2.0).end().end().addTheme(THM_BUCKET_LABEL)
                                .end().build();
                Frame3 interviews = Frame3.builder("FRM_GRP_INTERVIEWS").addTheme(THM_TEST1).end()
                                .question("QUE_GRP_INTERVIEWS").addTheme(THM_FORM_INPUT_DEFAULT)
                                .vcl(VisualControlType.VCL_INPUT).weight(2.0).end().end().addTheme(THM_BUCKET_LABEL)
                                .end().build();
                Frame3 offered = Frame3.builder("FRM_GRP_OFFERED").addTheme(THM_TEST1).end().question("QUE_GRP_OFFERED")
                                .addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT).weight(2.0).end()
                                .end().addTheme(THM_BUCKET_LABEL).end().build();
                Frame3 placed = Frame3.builder("FRM_GRP_PLACED").addTheme(THM_TEST1).end().question("QUE_GRP_PLACED")
                                .addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT).weight(2.0).end()
                                .end().addTheme(THM_BUCKET_LABEL).end().build();
                Frame3 inProgress = Frame3.builder("FRM_GRP_IN_PROGRESS").addTheme(THM_TEST1).end()
                                .question("QUE_GRP_IN_PROGRESS").addTheme(THM_FORM_INPUT_DEFAULT)
                                .vcl(VisualControlType.VCL_INPUT).weight(2.0).end().end().addTheme(THM_BUCKET_LABEL)
                                .end().build();

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


                Frame3 card = Frame3.builder("FRM_CARD_TEST")
                                .addTheme(THM_TEST1).end()
                                .addTheme(THM_GREEN).end()
                                .question("QUE_GRP_NOT_APPLIED").addTheme(THM_FORM_INPUT_DEFAULT)
                                .vcl(VisualControlType.VCL_INPUT).weight(2.0).end().end().addTheme(THM_BUCKET_LABEL)
                                .end().build();

                Frame3 bucketColumn1 = Frame3.builder("FRM_BUCKET_COLUMN_ONE")
                                .addTheme(THM_BUCKET_COLUMN).end()
                                .addFrame(available, FramePosition.NORTH).end()
                               // .addFrame(card, FramePosition.NORTH).end()
                                .addFrame(frameCard1, FramePosition.NORTH).end()
                                .addFrame(frameCard2, FramePosition.NORTH).end()
                                .addFrame(frameCard3, FramePosition.NORTH).end()
                                .addFrame(frameCard4, FramePosition.NORTH).end()
                                .addFrame(frameCard5, FramePosition.NORTH).end()
                                .addFrame(frameCard6, FramePosition.NORTH).end()
                                .addFrame(frameCard7, FramePosition.NORTH).end()
                                .addFrame(frameCard8, FramePosition.NORTH).end()
                                .addFrame(frameCard9, FramePosition.NORTH).end()

                                //.addFrame(frameCard2, FramePosition.NORTH).end()
                                .build();
                Frame3 bucketColumn2 = Frame3.builder("FRM_BUCKET_COLUMN_TWO").addTheme(THM_BUCKET_COLUMN).end()
                                .addFrame(applied, FramePosition.NORTH).end().build();
                Frame3 bucketColumn3 = Frame3.builder("FRM_BUCKET_COLUMN_THREE").addTheme(THM_BUCKET_COLUMN).end()
                                .addFrame(shortlisted, FramePosition.NORTH).end().build();
                Frame3 bucketColumn4 = Frame3.builder("FRM_BUCKET_COLUMN_FOUR").addTheme(THM_BUCKET_COLUMN).end()
                                .addFrame(interviews, FramePosition.NORTH).end()
                                .addFrame(frameCard1, FramePosition.NORTH).end()
                                .addFrame(frameCard2, FramePosition.NORTH).end()
                                .addFrame(frameCard3, FramePosition.NORTH).end()
                                .addFrame(frameCard4, FramePosition.NORTH).end()
                                .addFrame(frameCard5, FramePosition.NORTH).end()
                                .addFrame(frameCard6, FramePosition.NORTH).end()
                                .addFrame(frameCard7, FramePosition.NORTH).end()
                                .addFrame(frameCard8, FramePosition.NORTH).end()
                                .addFrame(frameCard9, FramePosition.NORTH).end()
                                .addFrame(frameCard10, FramePosition.NORTH).end()
                                .addFrame(frameCard11, FramePosition.NORTH).end()
                                .addFrame(frameCard12, FramePosition.NORTH).end()
                                .addFrame(frameCard13, FramePosition.NORTH).end()
                                .addFrame(frameCard14, FramePosition.NORTH).end()
                                .addFrame(frameCard15, FramePosition.NORTH).end()
                                .addFrame(frameCard16, FramePosition.NORTH).end()
                                .addFrame(frameCard17, FramePosition.NORTH).end()
                                .addFrame(frameCard18, FramePosition.NORTH).end()
                                .addFrame(frameCard19, FramePosition.NORTH).end()
                                .build();

                Frame3 bucketColumn5 = Frame3.builder("FRM_BUCKET_COLUMN_FIVE").addTheme(THM_BUCKET_COLUMN).end()
                                .addFrame(offered, FramePosition.NORTH).end().build();
                Frame3 bucketColumn6 = Frame3.builder("FRM_BUCKET_COLUMN_SIX").addTheme(THM_BUCKET_COLUMN).end()
                                .addFrame(placed, FramePosition.NORTH).end().build();
                Frame3 bucketColumn7 = Frame3.builder("FRM_BUCKET_COLUMN_SEVEN").addTheme(THM_BUCKET_COLUMN).end()
                                .addFrame(inProgress, FramePosition.NORTH).end().build();

                Frame3 bucket = Frame3.builder("FRM_BUCKET").addTheme(THM_BUCKET).end()
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

                Theme THM_BUCKET_LABEL = Theme.builder("THM_BUCKET_LABEL").addAttribute().textAlign("center").end()
                                .build();

                Theme THM_TAB_HEADER = Theme.builder("THM_TAB_HEADER").addAttribute().backgroundColor("#065B9A").end()
                                .addAttribute().flexDirection("row").end().addAttribute().marginRight(5).end().build();

                Theme THM_TAB_CONTENT = Theme.builder("THM_TAB_CONTENT").addAttribute().backgroundColor("#F8F9FA").end()
                                .build();

                Theme THM_TAB = Theme.builder("THM_TAB").addAttribute().backgroundColor("#3F505F").borderStyle("solid")
                                .borderColor("white").color("white").height(40).end().build();

                Frame3 tab1 = Frame3.builder("FRM_TAB_ONE").addTheme(THM_TAB).end().question("QUE_NAME_TWO")
                                .addTheme(THM_BUCKET_LABEL).end().end().build();
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