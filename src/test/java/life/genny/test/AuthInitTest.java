package life.genny.test;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.test.JbpmJUnitBaseTestCase.Strategy;
import org.junit.Test;
import org.kie.api.command.Command;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.command.CommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

import io.vavr.Tuple2;
import io.vertx.core.json.JsonObject;
import life.genny.jbpm.customworkitemhandlers.AwesomeHandler;
import life.genny.jbpm.customworkitemhandlers.NotificationWorkItemHandler;
import life.genny.models.GennyToken;
import life.genny.qwanda.Ask;
import life.genny.qwanda.ContextType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.llama.Frame;
import life.genny.qwanda.llama.Frame.ThemeAttribute;
import life.genny.qwanda.llama.Llama;
import life.genny.qwanda.message.QBulkMessage;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.rules.QRules;
import life.genny.rules.listeners.JbpmInitListener;
import life.genny.utils.FrameUtils;
import life.genny.utils.QuestionUtils;
import life.genny.utils.Layout.LayoutPosition;

public class AuthInitTest extends GennyJbpmBaseTest {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());
	
	private static final String WFE_SEND_FORMS = "rulesCurrent/shared/_BPMN_WORKFLOWS/send_forms.bpmn";
	private static final String WFE_SHOW_FORM = "rulesCurrent/shared/_BPMN_WORKFLOWS/show_form.bpmn";
	private static final String WFE_AUTH_INIT =  "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/auth_init.bpmn";
	private static final String WFE_SEND_LLAMA =  "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/send_llama.bpmn";
	private static final String DRL_PROJECT =  "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/SendUserData/project.drl";
	private static final String DRL_USER_COMPANY =  "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/SendUserData/user_company.drl";
	private static final String DRL_USER =  "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/SendUserData/user.drl";
	private static final String DRL_EVENT_LISTENER_SERVICE_SETUP =  "rulesCurrent/shared/_BPMN_WORKFLOWS/Initialise_Project/eventListenerServiceSetup.drl";
	private static final String DRL_EVENT_LISTENER_USER_SETUP =  "rulesCurrent/shared/_BPMN_WORKFLOWS/Initialise_Project/eventListenerUserSetup.drl";
	
	public AuthInitTest() {
		super(false);
	}

	@Test
	public void testTheme()
	{
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken); // defaults to user anyway
		GennyToken serviceToken = new GennyToken("serviceToken",rules.getServiceToken());

		Theme THM_DISPLAY_VERTICAL = Theme.builder("THM_DISPLAY_VERTICAL")
			    .name("Display Visual Controls Vertically") /* Optional - defaults to the code */
			    .addAttribute(ThemeAttributeType.PRI_CONTENT)
			    	.flexDirection("column")
			        .shadowOffset()
			        	.height(5)
			        	.width(5)
			        	.maxWidth(600)
			        	.padding(10)
			        	.end()
			        .end()
			    .addAttribute()  /* defaults to ThemeAttributeType.PRI_CONTENT */
			        .justifyContent("flex-start")
			        .end()
			    .build();

		Theme THM_DISPLAY_HORIZONTAL = Theme.builder("THM_DISPLAY_HORIZONTAL")
			    .addAttribute()
			    	.flexDirection("row")
			        .end()
			    .build();

		Theme THM_BACKGROUND_WHITE = Theme.builder("THM_BACKGROUND_WHITE")
			    .addAttribute()
			    	.backgroundColor("white")
			        .end()
			    .build();

		Theme THM_BACKGROUND_RED = Theme.builder("THM_BACKGROUND_RED")
			    .addAttribute()
			    	.backgroundColor("red")
			        .end()
			    .build();

		Theme THM_BACKGROUND_GRAY = Theme.builder("THM_BACKGROUND_GRAY")
			    .addAttribute().backgroundColor("gray").end().build();

		Theme THM_BACKGROUND_BLACK = Theme.builder("THM_BACKGROUND_BLACK")
			    .addAttribute().backgroundColor("black").end().build();

		Theme THM_BACKGROUND_INTERNMATCH = Theme.builder("THM_BACKGROUND_INTERNMATCH")
			    .addAttribute().backgroundColor("#233a4e").end().build();

		Theme THM_WIDTH_300 = Theme.builder("THM_WIDTH_300")
			    .addAttribute()
			    	.width(300)
			        .end()
			    .build();

		Frame2 centre = Frame2.Builder.newInstance("FRM_CENTRE")
				.build();

		Frame2 profile = Frame2.Builder.newInstance("FRM_PROFILE")
						.addTheme(THM_DISPLAY_HORIZONTAL)
						.addTheme(THM_BACKGROUND_RED) 
						.build();
		
    	Frame2 header = Frame2.Builder.newInstance("FRM_HEADER")
    					.addFrame(profile,FramePosition.EAST)
    					.build();
		
		Frame2 sidebar = Frame2.Builder.newInstance("FRM_SIDEBAR")
						.addTheme(THM_WIDTH_300)
						.addTheme(THM_DISPLAY_VERTICAL)
						.addTheme(THM_BACKGROUND_RED) 
						.setQuestion("QUE_USER_PROFILE_GRP")
						.build();

		Frame2 notes = Frame2.Builder.newInstance("FRM_NOTES")
				.addTheme(THM_WIDTH_300)
				.addTheme(THM_DISPLAY_VERTICAL)
				.build();

		
		Frame2 footer = Frame2.Builder.newInstance("FRM_FOOTER")
						.build();


		
		Frame2 mainFrame = Frame2.Builder.newInstance("FRM_MAIN").addTheme("THM_COLOR_WHITE")
                
                .addTheme(THM_BACKGROUND_INTERNMATCH) 
                .addTheme(THM_BACKGROUND_WHITE) 
                .addTheme(THM_BACKGROUND_BLACK) 
                .addTheme(THM_BACKGROUND_GRAY) 
		    	.addFrame(header,FramePosition.NORTH)
		    	.addFrame(sidebar,FramePosition.EAST)
		    	.addFrame(footer,FramePosition.SOUTH)
		    	.addFrame(centre,FramePosition.CENTRE)
		    	.build();
		
		Frame2 desktop = Frame2.Builder.newInstance("FRM_ROOT") 
                
                .addFrame(mainFrame)                      
                .build(); 


		ArrayList<QDataAskMessage> askMsgs = new ArrayList<QDataAskMessage>();
		
		QDataBaseEntityMessage msg = FrameUtils2.toMessage(desktop,serviceToken,askMsgs);
		
        /* send message */
        rules.publishCmd(msg);  // Send QDataBaseEntityMessage
        
        for (QDataAskMessage askMsg : askMsgs) {
         	rules.publishCmd(askMsg,serviceToken.getUserCode(),userToken.getUserCode());  // Send associated QDataAskMessage
        }
        

       	System.out.println("Sent");
	}

	
	
	
	
	//@Test
	public void testDesktopPageDisplay()
	{

		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken); // defaults to user anyway
		GennyToken serviceToken = new GennyToken("serviceToken",rules.getServiceToken());

		setUpCache(GennySettings.mainrealm, userToken);
		
		Frame2 centre = Frame2.Builder.newInstance("FRM_CENTRE")
				.build();

		Frame2 profile = Frame2.Builder.newInstance("FRM_PROFILE")
						.addTheme("THM_DISPLAY_HORIZONTAL","flexDirection","row")
						.addTheme("THM_BACKGROUND_RED","backgroundColor","red") 
						.build();
		
    	Frame2 header = Frame2.Builder.newInstance("FRM_HEADER")
    					.addFrame(profile,FramePosition.EAST)
    					.build();
		
		Frame2 sidebar = Frame2.Builder.newInstance("FRM_SIDEBAR")
						.addTheme("THM_WIDTH_300","width",300)
						.addTheme("THM_DISPLAY_VERTICAL","flexDirection", "column")
						.addTheme("THM_DISPLAY_VERTICAL","justifyContent","flex-start")
						.addTheme("THM_BACKGROUND_RED","backgroundColor","red") 
						.setQuestion("QUE_USER_PROFILE_GRP")
						.build();

		Frame2 notes = Frame2.Builder.newInstance("FRM_NOTES")
				.addTheme("THM_WIDTH_300","width",300)
				.addTheme("THM_DISPLAY_VERTICAL","flexDirection", "column")
				.addTheme("THM_DISPLAY_VERTICAL","justifyContent","flex-start")
				.build();

		
		Frame2 footer = Frame2.Builder.newInstance("FRM_FOOTER")
						.build();


		
		Frame2 mainFrame = Frame2.Builder.newInstance("FRM_MAIN").addTheme("THM_COLOR_WHITE")
		    	.addFrame(header,FramePosition.NORTH)
		    	.addFrame(sidebar,FramePosition.EAST)
		    	.addFrame(footer,FramePosition.SOUTH)
		    	.addFrame(centre,FramePosition.CENTRE)
		    	.build();
		
		Frame2 desktop = Frame2.Builder.newInstance("FRM_ROOT") 
                .addTheme("THM_BACKGROUND_GRAY","backgroundColor","gray") 
                .addTheme("THM_BACKGROUND_INTERNMATCH","backgroundColor","#233a4e") 
                .addTheme("THM_COLOR_WHITE","backgroundColor","white") 
                .addTheme("THM_COLOR_BLACK",ThemeAttributeType.PRI_CONTENT,"backgroundColor","black") 
                
                .addFrame(mainFrame)                      
                .build(); 


		ArrayList<QDataAskMessage> askMsgs = new ArrayList<QDataAskMessage>();
		
		QDataBaseEntityMessage msg = FrameUtils2.toMessage(desktop,serviceToken,askMsgs);
		
        /* send message */
        rules.publishCmd(msg);  // Send QDataBaseEntityMessage
        
        for (QDataAskMessage askMsg : askMsgs) {
         	rules.publishCmd(askMsg,serviceToken.getUserCode(),userToken.getUserCode());  // Send associated QDataAskMessage
        }
        

       	System.out.println("Sent");
	}
	
	
	
	//@Test

	public void testAuthInit() {

	       Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
	     String[] jbpms = {WFE_AUTH_INIT,WFE_SEND_FORMS,WFE_SHOW_FORM,WFE_SEND_LLAMA};
	     String[] drls = {DRL_PROJECT,DRL_USER_COMPANY,DRL_USER,DRL_EVENT_LISTENER_SERVICE_SETUP,DRL_EVENT_LISTENER_USER_SETUP};
        for (String p : jbpms) {
            resources.put(p, ResourceType.BPMN2);
        }
        for (String p : drls) {
            resources.put(p, ResourceType.DRL);
        }
        createRuntimeManager(Strategy.SINGLETON, resources, null);
		KieSession kieSession = getRuntimeEngine().getKieSession();
		//Register handlers
		addWorkItemHandlers(kieSession);
		kieSession.addEventListener(new JbpmInitListener(userToken));
		

		QEventMessage msg = new QEventMessage("EVT_MSG", "AUTH_INIT");

		List<Command<?>> cmds = new ArrayList<Command<?>>();

		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules qRules = getQRules(userToken); // defaults to user anyway
		System.out.println(qRules.getToken());
		cmds.add(CommandFactory.newInsert(qRules, "qRules"));
		cmds.add(CommandFactory.newInsert(msg, "msg"));
		cmds.add(CommandFactory.newInsert(userToken,"userToken"));
		cmds.add(CommandFactory.newInsert(new GennyToken("serviceUser",qRules.getServiceToken()),"serviceToken"));
		// Set up Cache

		setUpCache(GennySettings.mainrealm, userToken);

		cmds.add(CommandFactory.newInsert(eventBusMock, "eb"));
		
		long startTime = System.nanoTime();
		ExecutionResults results = null;
	try {
		results = kieSession.execute(CommandFactory.newBatchExecution(cmds));
		} catch (Exception ee) {
			
		}
		finally {
			long endTime = System.nanoTime();
			double difference = (endTime - startTime) / 1e6; // get ms

			if (results != null) {
				results.getValue("msg"); // returns the inserted fact Msg
				QRules rules  = (QRules) results.getValue("qRules"); // returns the inserted fact QRules
				System.out.println(results.getValue("msg"));
				System.out.println(rules);
			} else {
				System.out.println("NO RESULTS");
			}

			System.out.println("BPMN completed in " + difference + " ms");

			kieSession.dispose();
		}
		

	}

//	@Test
	public void formsTest()
	{
		String apiUrl = GennySettings.qwandaServiceUrl+":8280/service/forms";
		System.out.println("Fetching setup info from "+apiUrl);
		String userToken = projectParms.getString("userToken");
		System.out.println("userToken (ensure user has test role) = "+userToken);
		try {
			String jsonFormCodes = QwandaUtils.apiGet(apiUrl, userToken);
			if (!"You need to be a test.".equals(jsonFormCodes)) {
				Type type = new TypeToken<List<String>>() {
	            }.getType();
				List<String> formCodes = JsonUtils.fromJson(jsonFormCodes, type);
				System.out.println("Form Codes="+formCodes);
				
				for (String formCode : formCodes) {
			//		rules.sendForm("QUE_ADD_HOST_COMPANY_GRP", rules.getUser().getCode(), rules.getUser().getCode());
				}
				
			} else {
				System.out.println("Ensure that the user you are using has a 'test' role ...");
			}
			
		} catch (Exception e) {
			
		}
	}

}