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
import life.genny.utils.QuestionUtils;

public class AuthInitTest extends GennyJbpmBaseTest {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());
	
	private static final String WFE_SEND_FORMS = "rulesCurrent/shared/_BPMN_WORKFLOWS/send_forms.bpmn";
	private static final String WFE_AUTH_INIT =  "rulesCurrent/shared/_BPMN_WORKFLOWS/auth_init.bpmn";

	public AuthInitTest() {
		super(false);
	}

	@Test
	public void testAuthInit() {

		KieSession kieSession = createKSession(WFE_AUTH_INIT,WFE_SEND_FORMS);
		

		QEventMessage msg = new QEventMessage("EVT_MSG", "AUTH_INIT");

		List<Command<?>> cmds = new ArrayList<Command<?>>();

		GennyToken token = getToken(realm, "user1", "Barry Allan", "hero");
		QRules qRules = getQRules(token); // defaults to user anyway
		System.out.println(qRules.getToken());
		cmds.add(CommandFactory.newInsert(qRules, "qRules"));
		cmds.add(CommandFactory.newInsert(msg, "msg"));
		cmds.add(CommandFactory.newInsert(new String("GADA"), "name"));

		// Set up Cache

		setUpCache(GennySettings.mainrealm, token);

		cmds.add(CommandFactory.newInsert(eventBusMock, "eb"));

		long startTime = System.nanoTime();
		ExecutionResults results = kieSession.execute(CommandFactory.newBatchExecution(cmds));
		long endTime = System.nanoTime();
		double difference = (endTime - startTime) / 1e6; // get ms


		results.getValue("msg"); // returns the inserted fact Msg
		QRules rules  = (QRules) results.getValue("qRules"); // returns the inserted fact QRules
		System.out.println(results.getValue("msg"));
		System.out.println(rules);
		System.out.println(results.getValue("name"));
		
		String apiUrl = GennySettings.qwandaServiceUrl+":8280/service/forms";
		System.out.println("Fetching setup info from "+apiUrl);
		String userToken = projectParms.getString("userToken");
		System.out.println("userToken (ensure user has test role) = "+userToken);
		try {
			
			/* create a test baseentity */
			BaseEntity testBe = new BaseEntity("GRP_FORM_TEST_BE", "Forms test");
			
			/* get the theme */
			BaseEntity expandable = rules.baseEntity.getBaseEntityByCode("THM_EXPANDABLE");

			/* create an ask  */
			Ask testBeAsk = QuestionUtils.createQuestionForBaseEntity(testBe, true,rules.getToken());
			rules.createVirtualContext(testBeAsk, expandable, ContextType.THEME);

			
			String jsonFormCodes = QwandaUtils.apiGet(apiUrl, userToken);
			if (!"You need to be a test.".equals(jsonFormCodes)) {
				Type type = new TypeToken<List<String>>() {
	            }.getType();
				List<String> formCodes = JsonUtils.fromJson(jsonFormCodes, type);
				System.out.println("Form Codes="+formCodes);
				
				List<Ask> askList = new ArrayList<>();
				String userCode = rules.getUser().getCode();
			//	for (String formCode : formCodes) {
				
				rules.sendForm("QUE_ADDRESS_GRP",userCode , userCode);
				//	rules.sendForm(formCode,userCode , userCode);
					/* create child Asks for the parents question to test different formats of question groups */
					BaseEntity grpBe = new BaseEntity("QUE_ADDRESS_GRP", "QUE_ADDRESS_GRP");
					Ask ask = QuestionUtils.createQuestionForBaseEntity(grpBe, false,rules.getToken());
					/* collect all child asks and set to the parent ask */				
					askList.add(ask);
			//		}
			

				Ask[] childAskArr = askList.stream().toArray(Ask[]::new);;
				testBeAsk.setChildAsks(childAskArr);
				
				Ask[] askArr = { testBeAsk };
				
				QDataAskMessage totalAskMsg = new QDataAskMessage(askArr);
				rules.publishCmd(totalAskMsg);

				BaseEntity headerFrameBe = rules.baseEntity.getBaseEntityByCode("FRM_HEADER");

				/* link the form-testing related question and link it to header */
				headerFrameBe = rules.createVirtualLink(headerFrameBe, testBeAsk, "LNK_ASK", "SOUTH");
				
				QDataBaseEntityMessage testMsg = new QDataBaseEntityMessage(headerFrameBe);
				rules.publishCmd(testMsg);

				
			} else {
				System.out.println("Ensure that the user you are using has a 'test' role ...");
			}
			
		} catch (Exception e) {
			System.out.println(e);
		}

		kieSession.dispose();
		System.out.println("BPMN completed in " + difference + " ms");

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