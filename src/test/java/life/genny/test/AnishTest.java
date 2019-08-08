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
import life.genny.models.ThemePosition;
import life.genny.qwanda.Answer;
import life.genny.qwanda.Context;
import life.genny.qwanda.ContextType;
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

public class AnishTest extends GennyJbpmBaseTest {

        private static final Logger log = LoggerFactory
                        .getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

        public AnishTest() {
                super(false);
        }
        
        
       // @Test
    	public void userSessionANishTest() {
    		
    		//VertxUtils.cachedEnabled = true; // don't try and use any local services
    		
    		QRules rules = GennyJbpmBaseTest.setupLocalService();
    		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
    		
    		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
    		QEventMessage newLoginMessage = new QEventMessage("EVT_MSG","AUTH_INIT");
    		newLoginMessage.getData().setValue("NEW_SESSION");
    		
    		QEventMessage logOutMessage = new QEventMessage("EVT_MSG","LOGOUT");
    		logOutMessage.getData().setValue("LOGOUT");
    		QEventMessage displayTableMessage = new QEventMessage("EVT_MSG","DISPLAY_DETAILS");
    		QEventMessage AuthINit = new QEventMessage("EVT_MSG","AUTH_INIT");
    		
    		
    		GennyKieSession gks = GennyKieSession.builder(serviceToken)
    				.addJbpm( "user_lifecycle2.bpmn")
    				.addJbpm( "userSession2.bpmn")
    				.addJbpm( "show_dashboard2.bpmn" )
    				.addJbpm( "user_validation.bpmn" )
    				.addJbpm( "auth_init.bpmn" )
    				.addJbpm( "bucket_page2.bpmn" )
    				.addJbpm( "detailpage.bpmn" )
    				.addToken(userToken)
    				.addFact("rules", rules)
    				.build();
    		
    		
    		gks.start();
    		gks.advanceSeconds(3, true);
    		gks.injectSignal("newSession",newLoginMessage);
    		gks.advanceSeconds(5, true);
    		
    		/*gks.injectSignal("event",AuthINit);
    		gks.advanceSeconds(5, true);
    		
    		gks.injectSignal("event",displayTableMessage);
    		gks.advanceSeconds(5, true);
    		
    		
    		gks.injectSignal("event",AuthINit);
    		gks.advanceSeconds(5, true);
    		*/


            }
            
        public Frame3 getDashboard(){
                QRules rules = GennyJbpmBaseTest.setupLocalService();
                GennyToken userToken = new GennyToken("userToken", rules.getToken());
                GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
                BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
                BaseEntity project = beUtils.getBaseEntityByCode( "PRJ_" + serviceToken.getRealm().toUpperCase());

                try {   

                        Theme THM_CONTENT = Theme.builder("THM_CONTENT")
                                        .addAttribute()
                                                .backgroundColor(project.getValue("PRI_COLOR_BACKGROUND", "#F6F6F6"))
                                                .color("black")
                                                .end()
                                        .build();     
                        
                        Theme THM_DASHBOARD = Theme.builder("THM_DASHBOARD")
                                        .addAttribute()
                                                .backgroundColor("green")
                                                .maxWidth(900)
                                                .width("100%")
                                                .end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                        .build();   

                        Theme THM_DASHBOARD_ITEM = Theme.builder("THM_DASHBOARD_ITEM")
                                        .addAttribute()
                                                .backgroundColor("white")
                                                .color("black")
                                                .borderStyle("solid")
                                                .borderColor("black")
                                                .borderBottomWidth(2)
                                        .end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                        .build();   
                                        
                                        
                        Theme THM_DASHBOARD_ITEM_WRAPPER = Theme.builder("THM_DASHBOARD_ITEM_WRAPPER")
                                        .addAttribute()
                                                .flexDirection("row")
                                                .justifyContent("space-between")
                                                .width("100%")
                                                .end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                        .build(); 
                        
                        Theme THM_DASHBOARD_ITEM_BEHAVIOUR = Theme.builder("THM_DASHBOARD_ITEM_BEHAVIOUR")
                                        .addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true).end()
                                        .build(); 

                        /* interns-content-items */
                        Frame3 FRM_COUNT_ALL_INTERNS  = Frame3.builder("FRM_COUNT_ALL_INTERNS")
                                        .addTheme(THM_DASHBOARD_ITEM).end()
                                        .question("QUE_COUNT_ALL_INTERNS")
                                                .addTheme(THM_DASHBOARD_ITEM_WRAPPER).vcl(VisualControlType.VCL_WRAPPER).end()
                                                .addTheme(THM_DASHBOARD_ITEM_BEHAVIOUR).end()
                                        .end()
                                        .build();
                        
                        Frame3 FRM_COUNT_AVAILABLE_INTERNS  = Frame3.builder("FRM_COUNT_AVAILABLE_INTERNS")
                                        .addTheme(THM_DASHBOARD_ITEM).end()
                                        .question("QUE_COUNT_AVAILABLE_INTERNS").end()
                                        .build();

                        Frame3 FRM_COUNT_APPLIED_SHORTLISTED_INTERVIEWED_INTERNS  = Frame3.builder("FRM_COUNT_APPLIED_SHORTLISTED_INTERVIEWED_INTERNS")
                                        .addTheme(THM_DASHBOARD_ITEM).end()
                                        .question("QUE_COUNT_APPLIED_SHORTLISTED_INTERVIEWED_INTERNS").end()
                                        .build();
                        
                        Frame3 FRM_COUNT_OFFERED_INTERNS  = Frame3.builder("FRM_COUNT_OFFERED_INTERNS")
                                        .addTheme(THM_DASHBOARD_ITEM).end()
                                        .question("QUE_COUNT_OFFERED_INTERNS").end()
                                        .build();

                        Frame3 FRM_COUNT_PLACED_INTERNS  = Frame3.builder("FRM_COUNT_PLACED_INTERNS")
                                        .addTheme(THM_DASHBOARD_ITEM).end()
                                        .question("QUE_COUNT_PLACED_INTERNS").end()
                                        .build();
                        
                        Frame3 FRM_COUNT_IN_PROGRESS_INTERNS  = Frame3.builder("FRM_COUNT_IN_PROGRESS_INTERNS")
                                        .addTheme(THM_DASHBOARD_ITEM).end()
                                        .question("QUE_COUNT_IN_PROGRESS_INTERNS").end()
                                        .build();
                        Frame3 FRM_COUNT_COMPLETED_INTERNS  = Frame3.builder("FRM_COUNT_COMPLETED_INTERNS")
                                        .addTheme(THM_DASHBOARD_ITEM).end()
                                        .question("QUE_COUNT_COMPLETED_INTERNS").end()
                                        .build();
                        
                        
                        /* interns-header */
                        Theme THM_TITLE_LABEL = Theme.builder("THM_TITLE_LABEL")
                                        .addAttribute()
                                                .textAlign("center")
                                                .bold(true)
                                                .size("lg")
                                                .end()
                                        .build();  
                        
                        Theme THM_TITLE_WRAPPER = Theme.builder("THM_TITLE_WRAPPER")
                                        .addAttribute()
                                                .padding(20)
                                                .end()
                                        .build();  
                        

                        Theme THM_TITLE_BEHAVIOUR = Theme.builder("THM_TITLE_BEHAVIOUR")
                                        .addAttribute()
                                                .backgroundColor(project.getValue("PRI_COLOR_SURFACE", "#FFFFFF"))
                                                .color(project.getValue("PRI_COLOR_SURFACE_ON", "#000000"))
                                                .end()
                                        .addAttribute(ThemeAttributeType.PRI_HAS_INPUT, false).end()
                                        .addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true).end()
                                        .build();         


                        Frame3 FRM_DASHBOARD_INTERNS_HEADER  = Frame3.builder("FRM_DASHBOARD_INTERNS_HEADER")
                                                .question("QUE_COUNT_ALL_INTERNSHIPS")
                                                        .addTheme(THM_TITLE_LABEL).vcl(VisualControlType.VCL_LABEL).end()
                                                        .addTheme(THM_TITLE_WRAPPER).vcl(VisualControlType.VCL_WRAPPER).end()
                                                        .addTheme(THM_TITLE_BEHAVIOUR).end()
                                                .end()
                                                .build();
                                                
                        /* interns-content */
 
                        Frame3 FRM_DASHBOARD_INTERNS_CONTENT  = Frame3.builder("FRM_DASHBOARD_INTERNS_CONTENT")
                                                .addFrame(FRM_COUNT_ALL_INTERNS, FramePosition.NORTH).end()
                                                .addFrame(FRM_COUNT_AVAILABLE_INTERNS, FramePosition.NORTH).end()
                                                .addFrame(FRM_COUNT_APPLIED_SHORTLISTED_INTERVIEWED_INTERNS, FramePosition.NORTH).end()
                                                .addFrame(FRM_COUNT_OFFERED_INTERNS, FramePosition.NORTH).end()
                                                .addFrame(FRM_COUNT_PLACED_INTERNS, FramePosition.NORTH).end()
                                                .addFrame(FRM_COUNT_IN_PROGRESS_INTERNS, FramePosition.NORTH).end()
                                                .addFrame(FRM_COUNT_COMPLETED_INTERNS, FramePosition.NORTH).end()
                                                .build();

                        Frame3 FRM_DASHBOARD_INTERNS  = Frame3.builder("FRM_DASHBOARD_INTERNS")
                                                .addTheme(THM_DASHBOARD).end()
                                                .addFrame(FRM_DASHBOARD_INTERNS_HEADER, FramePosition.NORTH).end()
                                                .addFrame(FRM_DASHBOARD_INTERNS_CONTENT, FramePosition.CENTRE).end()
                                                .build();
                        
                        Frame3 FRM_CONTENT  = Frame3.builder("FRM_CONTENT")
                                                .addTheme(THM_CONTENT).end()
                                                .addFrame(FRM_DASHBOARD_INTERNS, FramePosition.CENTRE).end()
                                                .build();
                        
                        // Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();
                        // QDataBaseEntityMessage msg = FrameUtils2.toMessage(FRM_CONTENT, serviceToken, askMsgs);
                        // //rules.publishCmd(msg);
                        // for (QDataAskMessage askMsg : askMsgs) {
                        //         rules.publishCmd(askMsg, serviceToken.getUserCode(), userToken.getUserCode());
                        // }
                        
                        // System.out.println("Sent");
                        
                        Theme THM_TREE_GROUP_BEHAVIOUR = Theme.builder("THM_TREE_GROUP_BEHAVIOUR")
                                .addAttribute(ThemeAttributeType.PRI_IS_EXPANDABLE, true).end()
                                .addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_LABEL, true).end()
                                .addAttribute(ThemeAttributeType.PRI_IS_QUESTION_GRP_LABEL_CLICKABLE, true).end()
                                .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                .addAttribute(ThemeAttributeType.PRI_HAS_CHILD_ASKS, false).end()
                                .build();
                        
                        return FRM_CONTENT;



                } catch (Exception e) {
                        //TODO: handle exception
                }
                return null;
        }
    	
        @Test
        public void testDesktop() {
                QRules rules = GennyJbpmBaseTest.setupLocalService();
                GennyToken userToken = new GennyToken("userToken", rules.getToken());
                GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
                BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
                BaseEntity project = beUtils.getBaseEntityByCode( "PRJ_" + serviceToken.getRealm().toUpperCase());

                try {
                        rules.sendAllAttributes();

                        /* frame-tabs */
                      //  Frame3 FRM_TABS = generateTabs();

                        /* frame-header */
                        Frame3 FRM_HEADER = generateHeader();

                        /* frame-footer */
                        Frame3 FRM_FOOTER = generateFooter();

                        /* frame-sidebar */
                        Frame3 FRM_SIDEBAR = generateSidebar();
                      
                        /* frame-dashboard */
                        Frame3 FRM_CONTENT = getDashboard();

                      /*   Frame3 FRM_MAIN = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_MAIN", Frame3.class,
                        serviceToken.getToken()); */
                        
                     //    Frame3 FRM_TABS = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_TABS", Frame3.class,
                      //  serviceToken.getToken()); 
                         
                
                        

                        /* frame-root */
                        Frame3 FRM_APP  = Frame3.builder("FRM_APP")
                                        .addTheme("THM_PROJECT", ThemePosition.FRAME, serviceToken).end()
                                        .addFrame(FRM_HEADER, FramePosition.NORTH).end()
                                        .addFrame(FRM_SIDEBAR, FramePosition.WEST).end()
                                        //.addFrame(FRM_TABS, FramePosition.CENTRE).end() 
                                        .addFrame(FRM_CONTENT, FramePosition.CENTRE).end() 
                                        .addFrame(FRM_FOOTER, FramePosition.SOUTH).end() 
                                        .build();
                        /* frame-root */
                        Frame3 FRM_ROOT = Frame3.builder("FRM_ROOT")
                                        .addFrame(FRM_APP, FramePosition.CENTRE).end()
                                        .build();

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

        //@Test
        public void testCachedDesktop() {
                QRules rules = GennyJbpmBaseTest.setupLocalService();
                GennyToken userToken = new GennyToken("userToken", rules.getToken());
                GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

                rules.sendAllAttributes();

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
        
        //@Test
        public void virtualAskAndContextTest(){
                QRules rules = GennyJbpmBaseTest.setupLocalService();
                GennyToken userToken = new GennyToken("userToken", rules.getToken());
                GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
                
                rules.sendAllAttributes();


                Frame3 FRM_HEADER = this.generateHeader();


                Theme THM_MAIN = Theme.builder("THM_MAIN")
                                .addAttribute()
                                        .backgroundColor("grey")
                                        .color("#18639F")
                                        //.height(80)
                                        .end()
                                .build();                                        

                Frame3 FRM_MAIN = Frame3.builder("FRM_MAIN")
                                .addTheme(THM_MAIN).end()
                                .question("QUE_NAME_TWO").end()
                                .build();

                try {
                        /* frame-root */
                        Frame3 FRM_ROOT = Frame3.builder("FRM_ROOT")
				                        .addFrame(FRM_HEADER, FramePosition.NORTH).end()
				                        .addFrame(FRM_MAIN, FramePosition.CENTRE).end()
				                        .build();

                        Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();
                        QDataBaseEntityMessage msg = FrameUtils2.toMessage(FRM_ROOT, serviceToken, askMsgs);
                        rules.publishCmd(msg);
                        for (QDataAskMessage askMsg : askMsgs) {
                                rules.publishCmd(askMsg, serviceToken.getUserCode(), userToken.getUserCode());
                        }
                        System.out.println("Sent");
                } catch (Exception e) {
                        System.out.println("Failed " + e.getLocalizedMessage());
                }
                
        }

        public Frame3 generateHeader() {
                QRules rules = GennyJbpmBaseTest.setupLocalService();
                GennyToken userToken = new GennyToken("userToken", rules.getToken());
                GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
                BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
                BaseEntity project = beUtils.getBaseEntityByCode( "PRJ_" + serviceToken.getRealm().toUpperCase());

                try {

                        Theme THM_DROPDOWN_BEHAVIOUR_GENNY = Theme.builder("THM_DROPDOWN_BEHAVIOUR_GENNY")
                                        .addAttribute(ThemeAttributeType.PRI_IS_DROPDOWN, true).end()
                                        .addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_LABEL, true).end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_QUESTION_GRP_LABEL_CLICKABLE, true).end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                        .build();      

                        Theme THM_BACKGROUND_NONE = Theme.builder("THM_BACKGROUND_NONE")
                                                .addAttribute().backgroundColor("none").end().build();
                        
                              
                        Theme THM_DROPDOWN_HEADER_WRAPPER_GENNY = Theme.builder("THM_DROPDOWN_HEADER_WRAPPER_GENNY")
                                        .addAttribute()
                                        .padding(5)
                                        .backgroundColor(project.getValue("PRI_COLOR_PRIMARY_VARIANT_LIGHT", "#395268"))
                                        .end()
                                        .build();                       
                        
                        Theme THM_DROPDOWN_GROUP_LABEL_GENNY = Theme.builder("THM_DROPDOWN_GROUP_LABEL_GENNY")
                                        .addAttribute()
                                                .marginBottom(0)
                                                .size("sm")
                                                .bold(false)
                                        .end()
                                        .build();                       
                        
                        Theme THM_DROPDOWN_CONTENT_WRAPPER_GENNY = Theme.builder("THM_DROPDOWN_CONTENT_WRAPPER_GENNY")
                                        .addAttribute()
                                                .backgroundColor(project.getValue("PRI_COLOR_SURFACE", "#FFFFFF"))
                                                .color("green")
                                                .width(200)
                                        .end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                        .build();     

                        Theme THM_BOX_SHADOW_SM = Theme.builder("THM_BOX_SHADOW_SM")
                                        .addAttribute()
                                                .shadowColor("#000")
                                                .shadowOpacity(0.4)
                                                .shadowRadius(10)
                                                .shadowOffset()
                                                        .width(0)
                                                        .height(0)
                                                .end()
                                        .end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                        .build();     
                       
                        Theme THM_DROPDOWN_VCL_GENNY = Theme.builder("THM_DROPDOWN_VCL_GENNY")
                                        .addAttribute()
                                                //.color(project.getValue("PRI_COLOR_SURFACE_ON", "#000000"))
                                                .color(project.getValue("PRI_COLOR_SURFACE_ON", "green"))
                                        .end()
                                        .build();  
                                        
                                        
                        
                        
                        Frame3 FRM_HEADER_OPTIONS = Frame3.builder("FRM_HEADER_OPTIONS")
                                        .question("QUE_OPTIONS_GRP")
                                                .addTheme(THM_BACKGROUND_NONE).vcl(VisualControlType.GROUP).weight(2.0).end()
                                                .addTheme(THM_DROPDOWN_BEHAVIOUR_GENNY).vcl(VisualControlType.GROUP).end()
                                                .addTheme(THM_DROPDOWN_HEADER_WRAPPER_GENNY).vcl(VisualControlType.GROUP_HEADER_WRAPPER).end()
                                                .addTheme(THM_DROPDOWN_GROUP_LABEL_GENNY).vcl(VisualControlType.GROUP_LABEL).end()
                                                .addTheme(THM_DROPDOWN_CONTENT_WRAPPER_GENNY).vcl(VisualControlType.GROUP_CONTENT_WRAPPER).end()
                                                .addTheme(THM_BOX_SHADOW_SM).vcl(VisualControlType.GROUP_CONTENT_WRAPPER).end()
                                                .addTheme(THM_DROPDOWN_VCL_GENNY).vcl(VisualControlType.VCL).end()
                                        .end()
                                        .build();
                        
                        Frame3 FRM_HEADER_ADD_ITEMS = Frame3.builder("FRM_HEADER_ADD_ITEMS")
                                        .question("QUE_ADD_ITEMS_GRP")
                                        	.addTheme(THM_BACKGROUND_NONE).vcl(VisualControlType.GROUP).weight(2.0).end()
	                                        .addTheme(THM_DROPDOWN_BEHAVIOUR_GENNY).vcl(VisualControlType.GROUP).end()
	                                        .addTheme(THM_DROPDOWN_HEADER_WRAPPER_GENNY).vcl(VisualControlType.GROUP_HEADER_WRAPPER).end()
	                                        .addTheme(THM_DROPDOWN_GROUP_LABEL_GENNY).vcl(VisualControlType.GROUP_LABEL).end()
	                                        .addTheme(THM_DROPDOWN_CONTENT_WRAPPER_GENNY).vcl(VisualControlType.GROUP_CONTENT_WRAPPER).end()
	                                        .addTheme(THM_BOX_SHADOW_SM).vcl(VisualControlType.GROUP_CONTENT_WRAPPER).end()
	                                        .addTheme(THM_DROPDOWN_VCL_GENNY).vcl(VisualControlType.VCL).end()
                                        .end()
                                        .build();
                       
                        Theme THM_PADDING = Theme.builder("THM_PADDING")
                                        .addAttribute()
                                                .padding(10)
                                                .flexGrow(0)
                                                .flexShrink(0)
                                                .width(0)
                                                .flexBasis("initial")
                                        .end()
                                        .build();     
                                        
                        Frame3 FRM_PADDING = Frame3.builder("FRM_PADDING")
                                        .addTheme(THM_PADDING).end()
                                        .build();


                        Theme THM_HEADER = Theme.builder("THM_HEADER")
                                        .addAttribute()
                                                .height(80)
                                                .paddingX(20)
                                                .end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                        .build();    
                        
            			BaseEntity sortIconBe = beUtils.getBaseEntityByCode("ICN_SORT");
                        
            			Context context = new Context(ContextType.ICON, sortIconBe, VisualControlType.VCL_ICON, 1.0);
                        
            			/* Test Context */
                        Frame3 FRM_HAMBURGER_MENU = Frame3.builder("FRM_HAMBURGER_MENU")
                        							.question("QUE_NAME_TWO")
                        							//.addContext(context).end()
                        							.end()
                        							.build();
                        
                        /* Test Virtual Ask */
                        /*
                        Frame3 FRM_HAMBURGER_MENU = Frame3.builder("FRM_HAMBURGER_MENU")
                        							.question("PRI-EVENT", "Nothing")
                        							.addContext(context).end()
                        							.end()
                        							.build();
                        							
                         */

                        Theme THM_FRAME_ALIGN_EAST = Theme.builder("THM_FRAME_ALIGN_EAST")
                                                .addAttribute()
                                                        .marginLeft("auto")
                                                        .flexGrow(0)
                                                        .flexBasis("initial")
                                                .end()
                                                .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                                .build();
                        Theme THM_FRAME_ALIGN_WEST = Theme.builder("THM_FRAME_ALIGN_WEST")
                                                .addAttribute()
                                                        .marginRight("auto")
                                                        .flexGrow(0)
                                                        .flexBasis("initial")
                                                .end()
                                                .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                                .build();

                        Frame3 FRM_HEADER = Frame3.builder("FRM_HEADER")
                                        .addTheme(THM_HEADER).end()
                                        .addTheme(THM_FRAME_ALIGN_WEST, ThemePosition.WEST).end()
                                        .addFrame(FRM_HAMBURGER_MENU, FramePosition.WEST).end()
                                        .addTheme(THM_FRAME_ALIGN_EAST, ThemePosition.EAST).end()
                                        .addFrame(FRM_HEADER_OPTIONS, FramePosition.EAST).end()
                                        .addFrame(FRM_PADDING, FramePosition.EAST).end()
                                        .addFrame(FRM_HEADER_ADD_ITEMS, FramePosition.EAST).end()
                                        .build();

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
                BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
                BaseEntity project = beUtils.getBaseEntityByCode( "PRJ_" + serviceToken.getRealm().toUpperCase());

                try {

                        Theme THM_SIDEBAR = Theme.builder("THM_SIDEBAR")
                                        .addAttribute()
                                                .backgroundColor(project.getValue("PRI_COLOR_PRIMARY", "#233A4E"))
                                        .end()
                                        .build();

                        Theme THM_SIDEBAR_WIDTH = Theme.builder("THM_SIDEBAR_WIDTH")
                                        .addAttribute()
                                                .minWidth(300)
                                                .width(100)
                                        .end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE,false).end()
                                        .build();


                        Theme THM_TREE_ITEM = Theme.builder("THM_TREE_ITEM")
                                        .addAttribute()
                                                .color("white")
                                                .height("auto")
                                                .flexGrow(0)
                                                .flexBasis("auto")
                                        .end()
                                        .build();

                        Frame3 FRM_LOGO = Frame3.builder("FRM_LOGO")
                                        .addTheme(THM_TREE_ITEM, ThemePosition.WRAPPER).end()
                                        .question("QUE_PROJECT_SIDEBAR_GRP")
                                                .addTheme("THM_LOGO", serviceToken)
                                                .vcl(VisualControlType.VCL_WRAPPER).end()
                                        .end()
                                        .build();
                        

                        Theme THM_TREE_GROUP_BEHAVIOUR = Theme.builder("THM_TREE_GROUP_BEHAVIOUR")
                                                        .addAttribute(ThemeAttributeType.PRI_IS_EXPANDABLE, true).end()
                                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                                        .addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_LABEL, true).end()
                                                        .addAttribute(ThemeAttributeType.PRI_IS_QUESTION_GRP_LABEL_CLICKABLE, true).end()
                                                        .build();
                        
                        Theme THM_TREE_GROUP_LABEL = Theme.builder("THM_TREE_GROUP_LABEL")
                                                        .addAttribute()
                                                                .bold(false)
                                                                .size("md")
                                                                .end()
                                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
                                                        .end()
                                                        .build();
                                                        
                        Theme THM_TREE_GROUP_WRAPPER = Theme.builder("THM_TREE_GROUP_WRAPPER")
                                                        .addAttribute()
                                                                .width("100%")
                                                                .paddingX(20)
                                                                .end()
                                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                                        .build();
                        
                        Theme THM_TREE_GROUP_CLICKABLE_WRAPPER = Theme.builder("THM_TREE_GROUP_CLICKABLE_WRAPPER")
                                                        .addAttribute()
                                                                .width("100%")
                                                                .justifyContent("space-between")
                                                        .end()
                                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                                        .build();

                        Theme THM_TREE_GROUP_CONTENT_WRAPPER = Theme.builder("THM_TREE_GROUP_CONTENT_WRAPPER")
                                                        .addAttribute()
                                                                .paddingLeft(10)
                                                                .end()
                                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                                        .build();


                        Frame3 FRM_TREE_FORM_VIEW = Frame3.builder("FRM_TREE_FORM_VIEW")
				                                .addTheme(THM_TREE_ITEM, ThemePosition.WRAPPER).end()
				                                .question("QUE_TREE_FORM_VIEW_GRP")
                                                                  .addTheme(THM_TREE_GROUP_BEHAVIOUR).end()
                                                                  .addTheme(THM_TREE_GROUP_CLICKABLE_WRAPPER)
                                                                        .vcl(VisualControlType.GROUP_CLICKABLE_WRAPPER)
                                                                  .end()
                                                                  .addTheme(THM_TREE_GROUP_CONTENT_WRAPPER)
                                                                        .vcl(VisualControlType.GROUP_CONTENT_WRAPPER)
                                                                  .end()
                                                                  .addTheme(THM_TREE_GROUP_WRAPPER)
                                                                        .vcl(VisualControlType.GROUP_WRAPPER)
                                                                  .end()
                                                                  .addTheme(THM_TREE_GROUP_LABEL)
                                                                        .vcl(VisualControlType.GROUP_LABEL)
                                                                  .end()
				                                .end()
				                                .build();
                        
                        Frame3 FRM_TREE_BUCKET_VIEW = Frame3.builder("FRM_TREE_BUCKET_VIEW")
                                                        .addTheme(THM_TREE_ITEM, ThemePosition.WRAPPER).end()
                                                        .question("QUE_TREE_BUCKET_VIEW").end()
                                                        .build();
                        
                        Frame3 FRM_TREE_DETAIL_VIEW = Frame3.builder("FRM_TREE_DETAIL_VIEW")
                                                        .addTheme(THM_TREE_ITEM, ThemePosition.WRAPPER).end()
                                                        .question("QUE_TREE_DETAIL_VIEW")
                                                                .addTheme(THM_TREE_GROUP_WRAPPER)
                                                                        .vcl(VisualControlType.VCL_WRAPPER)
                                                                .end()
                                                        .end()
                                                        .build();
                        
                        Frame3 FRM_TREE_TABLE_VIEW = Frame3.builder("FRM_TREE_TABLE_VIEW")
                                                        .addTheme(THM_TREE_ITEM, ThemePosition.WRAPPER).end()
                                                        .question("QUE_TREE_TABLE_VIEW").end()
                                                        .build();

                        Frame3 FRM_SIDEBAR = Frame3.builder("FRM_SIDEBAR")
                                                .addTheme(THM_SIDEBAR).end()
                                                .addTheme(THM_SIDEBAR_WIDTH).end()
                                                .addFrame(FRM_LOGO, FramePosition.NORTH).end()
                                                .addFrame(FRM_TREE_TABLE_VIEW, FramePosition.NORTH).end()
                                                .addFrame(FRM_TREE_BUCKET_VIEW, FramePosition.NORTH).end()
                                                .addFrame(FRM_TREE_DETAIL_VIEW, FramePosition.NORTH).end()
                                                .addFrame(FRM_TREE_FORM_VIEW, FramePosition.NORTH).end()
                                                .build();

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
                BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
                BaseEntity project = beUtils.getBaseEntityByCode( "PRJ_" + serviceToken.getRealm().toUpperCase());

                try {   
                        Theme THM_LABEL_BOLD = Theme.builder("THM_LABEL_BOLD")
                                                .addAttribute()
                                                        .bold(true)
                                                        .size("md")
                                                        .paddingX(10)
                                                .end()
                                                .addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true).end()
                                                .build();
                                                
                        Theme THM_FOOTER = Theme.builder("THM_FOOTER")
                                        .addAttribute()
                                                .backgroundColor(project.getValue("PRI_COLOR_PRIMARY", "#233A4E"))
                                                .height(50)
                                                .paddingX(20)
                                        .end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
					.build();

                        Frame3 FRM_POWERED_BY = Frame3.builder("FRM_POWERED_BY")
                                                .question("QUE_POWERED_BY_GRP")
                                                        .addTheme(THM_LABEL_BOLD)
                                                        .vcl(VisualControlType.VCL_LABEL)
                                                        .end()
                                                        
                                                        .addTheme("THM_DISPLAY_HORIZONTAL", serviceToken)
                                                        .vcl(VisualControlType.VCL_WRAPPER)
                                                        .end()
                                                
                                                .end()
                                                .build();

                        Theme THM_FRAME_ALIGN_EAST = Theme.builder("THM_FRAME_ALIGN_EAST")
                                                        .addAttribute()
                                                                .marginLeft("auto")
                                                                .flexGrow(0)
                                                                .flexBasis("initial")
                                                        .end()
                                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                                        .build();
                        Theme THM_FRAME_ALIGN_WEST = Theme.builder("THM_FRAME_ALIGN_WEST")
                                                        .addAttribute()
                                                                .marginRight("auto")
                                                                .flexGrow(0)
                                                                .flexBasis("initial")
                                                        .end()
                                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                                        .build();

                        Frame3 FRM_FOOTER = Frame3.builder("FRM_FOOTER")
                                        .addTheme(THM_FOOTER).end()
                                        .addTheme(THM_FRAME_ALIGN_EAST, ThemePosition.EAST).end()
                                        .addFrame(FRM_POWERED_BY, FramePosition.EAST).end()
                                        .build();

                        System.out.println("Generated Footer Frame");
                        return FRM_FOOTER;
                } catch (Exception e) {
                        System.out.println("Error :: " + e.getLocalizedMessage());
                }
                return null;
        }

        public Frame3 generateBucket() {

                Theme THM_FORM_INPUT_DEFAULT = Theme.builder("THM_FORM_INPUT_DEFAULT").addAttribute()
                                .borderBottomWidth(5).color("black").borderColor("#ddd").borderStyle("solid")
                                .placeholderColor("#888").height(70).end().build();

                Theme THM_BUCKET_LABEL = Theme.builder("THM_BUCKET_LABEL")
                                .addAttribute()
                                        .textAlign("center")
                                        .bold(true)
                                        .color("black")
                                .end()
                                .build();

                Theme THM_BUCKET = Theme.builder("THM_BUCKET")
                                .addAttribute()
                                        .backgroundColor("#F8F9FA")
                                        .overflowX("auto")
                                        .overflowY("auto")
                                        .width("100%")
                                .end()
                                .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                .build();

                Theme THM_BUCKET_COLUMN = Theme.builder("THM_BUCKET_COLUMN").addAttribute().backgroundColor("#EAEAEA")
                                .minWidth(300).width("100%").margin(20).textAlign("center").flexDirection("column")
                                .end().addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();
                Theme THM_BUCKET_COLUMN_PADDING = Theme.builder("THM_BUCKET_COLUMN_PADDING").addAttribute().padding(20)
                                .justifyContent("flex-start").end()
                                .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                Theme THM_TEST1 = Theme.builder("THM_TEST1")
                                .addAttribute()
                                        .backgroundColor("none")
                                        .width("100%")
                                        .height(70)
                                        .flexGrow(0)
                                        .flexBasis("initial")
                                .end()
                                .build();
                
                Theme THM_BUCKET_HEADER = Theme.builder("THM_BUCKET_HEADER")
                                .addAttribute()
                                        .backgroundColor("red")
                                .end()
                                .build();

                Frame3 available = Frame3.builder("FRM_GRP_AVAILABLE")
                                .addTheme(THM_TEST1).end()
                                //.addTheme(THM_BUCKET_HEADER).weight(1.0).end()
                                        .question("QUE_GRP_NOT_APPLIED")
                                                .addTheme(THM_BUCKET_LABEL).end()
                                        .end()
                                .end()
                                .build();



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

                Frame3 card = Frame3.builder("FRM_CARD_TEST").addTheme(THM_TEST1).end().addTheme(THM_GREEN).end()
                                .question("QUE_GRP_NOT_APPLIED").addTheme(THM_FORM_INPUT_DEFAULT)
                                .vcl(VisualControlType.VCL_INPUT).weight(2.0).end().end().addTheme(THM_BUCKET_LABEL)
                                .end().build();

                Frame3 bucketColumn1 = Frame3.builder("FRM_BUCKET_COLUMN_ONE")
                                .addTheme(THM_BUCKET_COLUMN, ThemePosition.WRAPPER).end()
                                .addTheme(THM_BUCKET_COLUMN_PADDING, ThemePosition.CENTRE).end()
                                .addFrame(available, FramePosition.NORTH).end()
                                .addFrame(frameCard1, FramePosition.CENTRE).end()
                                .addFrame(frameCard2, FramePosition.CENTRE).end()
                                .addFrame(frameCard3, FramePosition.CENTRE).end()
                                .addFrame(frameCard4, FramePosition.CENTRE).end()
                                .addFrame(frameCard5, FramePosition.CENTRE).end()
                                .addFrame(frameCard6, FramePosition.CENTRE).end()
                                .addFrame(frameCard7, FramePosition.CENTRE).end()
                                .addFrame(frameCard8, FramePosition.CENTRE).end()
                                .addFrame(frameCard9, FramePosition.CENTRE).end().build();
                Frame3 bucketColumn2 = Frame3.builder("FRM_BUCKET_COLUMN_TWO")
                                .addTheme(THM_BUCKET_COLUMN, ThemePosition.WRAPPER).end()
                                .addTheme(THM_BUCKET_COLUMN_PADDING, ThemePosition.CENTRE).end()
                                .addFrame(applied, FramePosition.NORTH).end()
                                .addFrame(frameCard1, FramePosition.CENTRE)
                                .end().addFrame(frameCard2, FramePosition.CENTRE).end().build();
                Frame3 bucketColumn3 = Frame3.builder("FRM_BUCKET_COLUMN_THREE")
                                .addTheme(THM_BUCKET_COLUMN, ThemePosition.WRAPPER).end()
                                .addTheme(THM_BUCKET_COLUMN_PADDING, ThemePosition.CENTRE).end()
                                .addFrame(shortlisted, FramePosition.NORTH).end().build();
                Frame3 bucketColumn4 = Frame3.builder("FRM_BUCKET_COLUMN_FOUR")
                                .addTheme(THM_BUCKET_COLUMN, ThemePosition.WRAPPER).end()
                                .addTheme(THM_BUCKET_COLUMN_PADDING, ThemePosition.CENTRE).end()
                                .addFrame(interviews, FramePosition.NORTH).end()
                                .addFrame(frameCard1, FramePosition.CENTRE).end()
                                .addFrame(frameCard2, FramePosition.CENTRE).end()
                                .addFrame(frameCard3, FramePosition.CENTRE).end()
                                .addFrame(frameCard4, FramePosition.CENTRE).end()
                                .addFrame(frameCard5, FramePosition.CENTRE).end()
                                .addFrame(frameCard6, FramePosition.CENTRE).end()
                                .addFrame(frameCard7, FramePosition.CENTRE).end()
                                .addFrame(frameCard8, FramePosition.CENTRE).end()
                                .addFrame(frameCard9, FramePosition.CENTRE).end()
                                .addFrame(frameCard10, FramePosition.CENTRE).end()
                                .addFrame(frameCard11, FramePosition.CENTRE).end()
                                .addFrame(frameCard12, FramePosition.CENTRE).end()
                                .addFrame(frameCard13, FramePosition.CENTRE).end()
                                .addFrame(frameCard14, FramePosition.CENTRE).end()
                                .addFrame(frameCard15, FramePosition.CENTRE).end()
                                .addFrame(frameCard16, FramePosition.CENTRE).end()
                                .addFrame(frameCard17, FramePosition.CENTRE).end()
                                .addFrame(frameCard18, FramePosition.CENTRE).end()
                                .addFrame(frameCard19, FramePosition.CENTRE).end().build();

                Frame3 bucketColumn5 = Frame3.builder("FRM_BUCKET_COLUMN_FIVE")
                                .addTheme(THM_BUCKET_COLUMN, ThemePosition.WRAPPER).end()
                                .addTheme(THM_BUCKET_COLUMN_PADDING, ThemePosition.CENTRE).end()
                                .addFrame(offered, FramePosition.NORTH).end().build();
                Frame3 bucketColumn6 = Frame3.builder("FRM_BUCKET_COLUMN_SIX")
                                .addTheme(THM_BUCKET_COLUMN, ThemePosition.WRAPPER).end()
                                .addTheme(THM_BUCKET_COLUMN_PADDING, ThemePosition.CENTRE).end()
                                .addFrame(placed, FramePosition.NORTH).end().build();
                Frame3 bucketColumn7 = Frame3.builder("FRM_BUCKET_COLUMN_SEVEN")
                                .addTheme(THM_BUCKET_COLUMN, ThemePosition.WRAPPER).end()
                                .addTheme(THM_BUCKET_COLUMN_PADDING, ThemePosition.CENTRE).end()
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

        public Frame3 generateCards(String count) {
                GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
                GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User",
                                "service");
                QRules rules = new QRules(eventBusMock, userToken.getToken());
                rules.set("realm", userToken.getRealm());
                rules.setServiceToken(serviceToken.getToken());

                /* create themes */

                Theme THM_FORM_LABEL_DEFAULT = Theme.builder("THM_FORM_LABEL_DEFAULT").addAttribute()
                                .backgroundColor("white").end().build();

                Theme THM_CARD_DEFAULT = Theme.builder("THM_CARD_DEFAULT").addAttribute().backgroundColor("none").end()
                                // .addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true).end()
                                // .addAttribute(ThemeAttributeType.PRI_HAS_INPUT, false).end()
                                .build();

                Theme THM_CARD = Theme.builder("THM_CARD").addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
                                .end().addAttribute().marginBottom(20).height(150).flexGrow(0).flexShrink(0)
                                .flexBasis("initial").backgroundColor("#F8F9FA").width("100%").shadowColor("#000")
                                .shadowOpacity(0.4).shadowRadius(5).end().addAttribute().shadowOffset().width(0)
                                .height(0).end().end().build();

                Theme THM_CARD_LEFT = Theme.builder("THM_CARD_LEFT").addAttribute().backgroundColor("#EBECF0").end()
                                .build();
                Theme THM_CARD_CENTRE = Theme.builder("THM_CARD_CENTRE").addAttribute().backgroundColor("#FFFFFF").end()
                                .build();
                Theme THM_CARD_RIGHT = Theme.builder("THM_CARD_RIGHT").addAttribute().backgroundColor("#E1E2E1").end()
                                .build();

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

                Frame3 frameLeftCard = Frame3.builder("FRM_CARD_LEFT").addTheme(THM_CARD_LEFT).end()
                                .question("QUE_IMAGE").end().build();
                Frame3 frameCentreCard = Frame3.builder("FRM_CARD_CENTRE").addTheme(THM_CARD_CENTRE).end()
                                // .question("QUE_NAME_TWO")
                                .addFrame(frameName, FramePosition.NORTH).end()
                                .addFrame(frameEmail, FramePosition.NORTH).end()
                                .addFrame(frameMobile, FramePosition.NORTH).end()
                                .addFrame(frameDob, FramePosition.NORTH).end()

                                .build();
                Frame3 frameRightCard = Frame3.builder("FRM_CARD_RIGHT").addTheme(THM_CARD_RIGHT).end()
                                .question("QUE_NAME_TWO").end().build();

                Theme THM_TEST1 = Theme.builder("THM_TEST1").addAttribute().backgroundColor("none").width("100%")
                                .height(70).flexGrow(0).flexBasis("initial").end().build();

                /* create frames */
                Frame3 frameCard = Frame3.builder("FRM_CARD" + "_" + count).addTheme(THM_CARD, ThemePosition.WRAPPER)
                                .end().question("QUE_NAME_TWO").addTheme(THM_FORM_LABEL_DEFAULT)
                                .vcl(VisualControlType.VCL_WRAPPER).end().end()
                                .addFrame(frameLeftCard, FramePosition.WEST).end()
                                .addFrame(frameCentreCard, FramePosition.WEST).end()
                                // .addFrame(frameRightCard, FramePosition.WEST).end()
                                .build();

                return frameCard;

        }

        public Frame3 generateTabs() {

                QRules rules = GennyJbpmBaseTest.setupLocalService();
                GennyToken userToken = new GennyToken("userToken", rules.getToken());
                GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
                BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
                BaseEntity project = beUtils.getBaseEntityByCode( "PRJ_" + serviceToken.getRealm().toUpperCase());


                Theme THM_TABS = Theme.builder("THM_TABS").addAttribute().backgroundColor("none").end().addAttribute()
                                .flexDirection("column").end().build();

                Theme THM_TAB_HEADER = Theme.builder("THM_TAB_HEADER")
                                .addAttribute()
                                        .backgroundColor(project.getValue("PRI_COLOR_PRIMARY", "#233A4E"))
                                        .flexDirection("row")
                                .end()
                                .build();

                Theme THM_TAB_CONTENT = Theme.builder("THM_TAB_CONTENT")
                                .addAttribute()
                                .backgroundColor(project.getValue("PRI_COLOR_BACKGROUND", "#F6F6F6")).end()
                                .build();

                Theme THM_TAB = Theme.builder("THM_TAB")
                                .addAttribute()
                                        .backgroundColor(project.getValue("PRI_COLOR_PRIMARY_VARIANT_LIGHT", "#395268"))
                                        .flexBasis("auto")
                                        .color("white")
                                        .height(40)
                                        .paddingX(10)
                                .end()
                                .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                .build();
                Theme THM_TAB_WIDTH = Theme.builder("THM_TAB_WIDTH")
                                .addAttribute()
                                        .width("auto")
                                .end()
                                .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                .build();

                Frame3 tab1 = Frame3.builder("FRM_TAB_ONE")
                                .addTheme(THM_TAB).end()
                                .addTheme(THM_TAB_WIDTH, ThemePosition.WRAPPER).end()
                                .question("QUE_TAB_BUCKET_VIEW")
                                .end()
                                .build();
                Frame3 tab2 = Frame3.builder("FRM_TAB_TWO")
                                .addTheme(THM_TAB).end()
                                .addTheme(THM_TAB_WIDTH, ThemePosition.WRAPPER).end()
                                .question("QUE_TAB_TABLE_VIEW")
                                .end()
                                .build
                                ();
                Frame3 tab3 = Frame3.builder("FRM_TAB_THREE")
                                .addTheme(THM_TAB).end()
                                .addTheme(THM_TAB_WIDTH, ThemePosition.WRAPPER).end()
                                .question("QUE_TAB_DETAIL_VIEW")
                                .end()
                                .build();

                Theme THM_FRAME_ALIGN_WEST = Theme.builder("THM_FRAME_ALIGN_WEST")
                                .addAttribute()
                                        .marginRight("auto")
                                        .flexGrow(0)
                                        .flexBasis("initial")
                                .end()
                                .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                .build();


                Frame3 tabHeader = Frame3.builder("FRM_TAB_HEADER")
                                .addTheme(THM_TAB_HEADER, ThemePosition.WRAPPER).end()
                                .addTheme(THM_FRAME_ALIGN_WEST, ThemePosition.WEST).end()
                                .addFrame(tab1, FramePosition.WEST).end()
                                .addFrame(tab2, FramePosition.WEST).end()
                                .addFrame(tab3, FramePosition.WEST).end()
                                .build();

                Frame3 bucket = generateBucket();
                // Frame3 detailView = generateDetailView();

                Frame3 tabContent = Frame3.builder("FRM_TAB_CONTENT").addTheme(THM_TAB_CONTENT).end().addFrame(bucket)
                                // .addFrame(detailView) /* comment out this to see the detail view */
                                .end().build();

                Frame3 tabs = Frame3.builder("FRM_TABS").addTheme(THM_TABS).end()
                                .addFrame(tabHeader, FramePosition.NORTH).end()
                                .addFrame(tabContent, FramePosition.CENTRE).end().build();

                System.out.println("Generated Tabs Frame");
                return tabs;
        }

        public Frame3 generateDetailView() {

                Theme THM_DETAIL_VIEW = Theme.builder("THM_DETAIL_VIEW").addAttribute().backgroundColor("#E2E1E0")
                                .flexDirection("column").end().build();

                Theme THM_CONTAINER = Theme.builder("THM_CONTAINER").addAttribute().backgroundColor("white")
                                .width("90%").height(300).end().build();

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

                Frame3 FRM_DETAIL_VIEW_CONTAINER = Frame3.builder("FRM_DETAIL_VIEW_CONTAINER").addTheme(THM_CONTAINER)
                                .end().addFrame(FRM_NAME, FramePosition.WEST).end()
                                .addFrame(FRM_NAME, FramePosition.WEST).end().addFrame(FRM_EMAIL, FramePosition.WEST)
                                .end().addFrame(FRM_DOB, FramePosition.WEST).end()
                                .addFrame(FRM_MOBILE, FramePosition.WEST).end().build();

                Frame3 FRM_DETAIL_VIEW = Frame3.builder("FRM_DETAIL_VIEW").addTheme(THM_DETAIL_VIEW).end()
                                .question("QUE_NAME_TWO").end().addFrame(FRM_DETAIL_VIEW_CONTAINER, FramePosition.NORTH)
                                .end().build();

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
        public void desktopWfTest() {
                GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
                GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User",
                                "service");
                QRules qRules = new QRules(eventBusMock, userToken.getToken());
                qRules.set("realm", userToken.getRealm());
                qRules.setServiceToken(serviceToken.getToken());

                System.out.println("session=" + userToken.getSessionCode());
                System.out.println("userToken=" + userToken.getToken());
                System.out.println("serviceToken=" + serviceToken.getToken());

                GennyKieSession gks = null;
                try {
                        Frame3 FRM_DESKTOP = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_DESKTOP",
                                        Frame3.class, serviceToken.getToken());

                        System.out.print("desktop-frame :: " + FRM_DESKTOP.getCode());

                        // Insert emoji for success here :)

                        gks = GennyKieSession.builder(serviceToken, false).addJbpm("desktop_test.bpmn")
                                        // .addFact("qRules", qRules)
                                        // .addFact("msg", msg)
                                        .addToken(serviceToken).addToken(userToken).build();

                        gks.start();
                        gks.advanceSeconds(5, true);
                        gks.injectSignal("runDesktop", FRM_DESKTOP);

                } finally {
                        gks.close();
                }

        }

        
}
