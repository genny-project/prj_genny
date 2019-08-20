package life.genny.test;

import org.kie.api.runtime.process.ProcessContext;
import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.reflect.TypeToken;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vertx.core.json.JsonObject;
import life.genny.eventbus.EventBusInterface;
import life.genny.eventbus.EventBusMock;
import life.genny.eventbus.VertxCache;
import life.genny.models.Frame3;
import life.genny.models.FramePosition;
import life.genny.models.GennyToken;
import life.genny.models.Theme;
import life.genny.models.ThemeAttributeType;
import life.genny.models.ThemeDouble;
import life.genny.qwanda.Answer;
import life.genny.qwanda.Context;
import life.genny.qwanda.ContextType;
import life.genny.qwanda.VisualControlType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QDataAnswerMessage;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwandautils.GennyCacheInterface;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.rules.QRules;
import life.genny.utils.BaseEntityUtils;
import life.genny.utils.FrameUtils2;
import life.genny.utils.VertxUtils;

public class RahulTest extends GennyJbpmBaseTest {

	//private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());
	private static final String WFP_USER_SESSION = "userSession.bpmn";
	private static final String WFP_USER_LIFECYCLE = "userLifecycle.bpmn";
	private static final String WFP_TEST_WORKFLOW = "testWorkflow.bpmn";
	private static final String WFP_APPLICATION_LIFECYCLE = "applicationLifecycle.bpmn";
	private static final String WFP_APPLICATION_CREATED = "applicationCreatedWorkflow.bpmn";
	private static final String WFP_APPLICATION_APPLIED = "applied.bpmn";
	private static final String WFP_AUTH_INIT = "auth_init.bpmn";
	private static final String WFP_USER_VALIDATION = "userValidation.bpmn";
	
	public RahulTest() {
		super(false);
	}
	
	//@Test
	public void quickTest() {
		GennyToken userToken = null;
		GennyToken serviceToken = null;
		QRules qRules = null;

		if (true) {
			userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
			serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
			qRules = new QRules(eventBusMock, userToken.getToken());
			qRules.set("realm", userToken.getRealm());
			qRules.setServiceToken(serviceToken.getToken());
			VertxUtils.cachedEnabled = true; // don't send to local Service Cache
		} else {
			qRules = GennyJbpmBaseTest.setupLocalService();
			userToken = new GennyToken("userToken", qRules.getToken());
			serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
		}
		
		BaseEntity intern = new BaseEntity("PRI_INTERN");
		BaseEntity internship = new BaseEntity("PRI_INTERNSHIP");

	    HashMap<String, BaseEntity> hashBeg = new HashMap<String, BaseEntity>();
	    
	    hashBeg.put("intern", intern);
	    hashBeg.put("internship", internship);

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage authInitMsg = new QEventMessage("EVT_MSG", "AUTH_INIT");
		QEventMessage msg1 = new QEventMessage("EVT_MSG", "INIT_1");
		QEventMessage msgLogout = new QEventMessage("EVT_MSG", "LOGOUT");

		// NOW SET UP Some baseentitys
		BaseEntity project = new BaseEntity("PRJ_" + serviceToken.getRealm().toUpperCase(),
				StringUtils.capitaliseAllWords(serviceToken.getRealm()));
		project.setRealm(serviceToken.getRealm());
		VertxUtils.writeCachedJson(serviceToken.getRealm(), "PRJ_" + serviceToken.getRealm().toUpperCase(),
				JsonUtils.toJson(project), serviceToken.getToken());
		
		// Log out to begin
		VertxUtils.writeCachedJson(userToken.getRealm(),userToken.getSessionCode(),null,userToken.getToken());

		GennyKieSession gks = null;

		try {
			gks = GennyKieSession.builder(serviceToken, true)
					.addJbpm("applicationLifecycle.bpmn")
					.addToken(userToken)
					.build();
			gks.start();
			
			gks.advanceSeconds(5, true);

			gks.injectSignal("newApplication", hashBeg);
			
			gks.advanceSeconds(5, true);
			
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			gks.close();
		}
		
	}
	
	@Test
	public void newUserTest()
	{
		System.out.println("New User test");
		GennyToken userToken = null;
		GennyToken userToken2 = null;
		GennyToken serviceToken = null;
		QRules qRules = null;

		if (true) {
			userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
			userToken2 = GennyJbpmBaseTest.createGennyToken(realm, "user2", "Barry2 Allan2", "user");
			serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
			qRules = new QRules(eventBusMock, userToken.getToken());
			qRules.set("realm", userToken.getRealm());
			qRules.setServiceToken(serviceToken.getToken());
			VertxUtils.cachedEnabled = true; // don't send to local Service Cache
		} else {
			qRules = GennyJbpmBaseTest.setupLocalService();
			userToken = new GennyToken("userToken", qRules.getToken());
			serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
		}

//		System.out.println("session     =" + userToken.getSessionCode());
//		System.out.println("userToken   =" + userToken.getToken());
//		System.out.println("userToken2   =" + userToken2.getToken());
//		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage initMsg = new QEventMessage("EVT_MSG", "INIT_STARTUP");

		QEventMessage authInitMsg1 = new QEventMessage("EVT_MSG", "AUTH_INIT"); authInitMsg1.setToken(userToken.getToken());
		QEventMessage authInitMsg2 = new QEventMessage("EVT_MSG", "AUTH_INIT");authInitMsg2.setToken(userToken2.getToken());
		QEventMessage msg1 = new QEventMessage("EVT_MSG", "INIT_1");
		QEventMessage msgLogout1 = new QEventMessage("EVT_MSG", "LOGOUT");msgLogout1.setToken(userToken.getToken());
		QEventMessage msgLogout2 = new QEventMessage("EVT_MSG", "LOGOUT");msgLogout2.setToken(userToken2.getToken());

		List<Answer> answers = new ArrayList<Answer>();
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_FIRSTNAME", "Bruce"));
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_LASTNAME", "Wayne"));
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_ADDRESS_JSON", 
				"{\"street_number\":\"64\",\"street_name\":\"Fakenham Road\",\"suburb\":\"Ashburton\",\"state\":\"Victoria\",\"country\":\"AU\",\"postal_code\":\"3147\",\"full_address\":\"64 Fakenham Rd, Ashburton VIC 3147, Australia\",\"street_address\":\"64 Fakenham Road\"}"));
		
		
		QDataAnswerMessage answerMsg = new QDataAnswerMessage(answers.toArray(new Answer[0]));
		answerMsg.setToken(userToken.getToken());
		
		// NOW SET UP Some baseentitys
		BaseEntity project = new BaseEntity("PRJ_" + serviceToken.getRealm().toUpperCase(),
				StringUtils.capitaliseAllWords(serviceToken.getRealm()));
		project.setRealm(serviceToken.getRealm());
		VertxUtils.writeCachedJson(serviceToken.getRealm(), "PRJ_" + serviceToken.getRealm().toUpperCase(),
				JsonUtils.toJson(project), serviceToken.getToken());
		

		GennyKieSession gks = null;

		try {
			gks = GennyKieSession.builder(serviceToken,true)
					.addDrl("SignalProcessing")
					.addDrl("DataProcessing")
					.addDrl("EventProcessing")
					.addDrl("InitialiseProject")
					.addJbpm("InitialiseProject")
					.addJbpm("Lifecycles")
					.addDrl("AuthInit")
					.addJbpm("AuthInit")
					.addJbpm("userSession.bpmn")
					.addJbpm("userValidation.bpmn")
					.addJbpm("userLifecycle.bpmn")

					.addToken(userToken)
					.build();
			gks.start();
			gks.injectEvent(initMsg); // This should create a new process

			gks.injectEvent(authInitMsg1); // This should create a new process
			gks.advanceSeconds(5, false);
			gks.injectEvent(authInitMsg2); // This should create a new process
			gks.advanceSeconds(5, false);
			gks.injectEvent(authInitMsg1); // This should create a new process
			gks.advanceSeconds(5, false);

			gks.injectEvent(answerMsg); // This should create a new process
			gks.advanceSeconds(5, false);
			gks.injectEvent(msgLogout1);
			gks.injectEvent(msgLogout2);
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		finally {
			if (gks!=null) {
				gks.close();
			}
		}
	}
		
		//@Test
		public void userSessionTestToRunnningService() {
		
		//VertxUtils.cachedEnabled = true; // don't try and use any local services
		
		QRules rules =GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
		
		QDataAnswerMessage sas = new QDataAnswerMessage(new Answer("asd","asd","asdasd","adasdasd"));
		sas.setToken(userToken.getToken());
		VertxUtils.writeMsg("data", JsonUtils.toJson(sas));
	}
}