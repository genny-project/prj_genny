package life.genny.test;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieSession;
import org.kie.internal.command.CommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

import life.genny.jbpm.customworkitemhandlers.AwesomeHandler;
import life.genny.jbpm.customworkitemhandlers.NotificationWorkItemHandler;
import life.genny.models.GennyToken;
import life.genny.qwanda.Ask;
import life.genny.qwanda.ContextType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.rules.QRules;
import life.genny.rules.listeners.JbpmInitListener;
import life.genny.utils.QuestionUtils;

public class AuthInitTest extends GennyJbpmBaseTest {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());
	
	private static final String WFE_SEND_FORMS = "rulesCurrent/shared/_BPMN_WORKFLOWS/send_forms.bpmn";
	private static final String WFE_SHOW_FORM = "rulesCurrent/shared/_BPMN_WORKFLOWS/show_form.bpmn";
	private static final String WFE_AUTH_INIT =  "rulesCurrent/shared/_BPMN_WORKFLOWS/auth_init.bpmn";
	private static final String DRL_PROJECT =  "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/SendUserData/project.drl";
	private static final String DRL_USER_COMPANY =  "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/SendUserData/user_company.drl";
	private static final String DRL_USER =  "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/SendUserData/user.drl";
	private static final String DRL_EVENT_LISTENER_SERVICE_SETUP =  "rulesCurrent/shared/_BPMN_WORKFLOWS/Initialise_Project/eventListenerServiceSetup.drl";
	private static final String DRL_EVENT_LISTENER_USER_SETUP =  "rulesCurrent/shared/_BPMN_WORKFLOWS/Initialise_Project/eventListenerUserSetup.drl";
	
	public AuthInitTest() {
		super(false);
	}

	@Test

	public void testAuthInit() {

		KieSession kieSession = createKSession(WFE_AUTH_INIT,WFE_SEND_FORMS,WFE_SHOW_FORM);
//		KieSession kieSession = createKSession(WFE_AUTH_INIT,WFE_SEND_FORMS,WFE_SHOW_FORM,DRL_PROJECT,DRL_USER_COMPANY,DRL_USER,DRL_EVENT_LISTENER_SERVICE_SETUP,DRL_EVENT_LISTENER_USER_SETUP);
		

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
		
		kieSession.addEventListener(new JbpmInitListener(userToken));


		try {
		long startTime = System.nanoTime();
		ExecutionResults results = kieSession.execute(CommandFactory.newBatchExecution(cmds));
		long endTime = System.nanoTime();
		double difference = (endTime - startTime) / 1e6; // get ms


		results.getValue("msg"); // returns the inserted fact Msg
		QRules rules  = (QRules) results.getValue("qRules"); // returns the inserted fact QRules
		System.out.println(results.getValue("msg"));
		System.out.println(rules);
		

		System.out.println("BPMN completed in " + difference + " ms");
		} catch (Exception ee) {
			
		}
		finally {
			
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