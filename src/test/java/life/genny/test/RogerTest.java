package life.genny.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.Logger;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.admin.ProcessInstanceAdminService;
import org.jbpm.services.api.model.DeploymentUnit;
import org.jbpm.services.api.query.QueryService;
import org.jbpm.services.api.utils.KieServiceConfigurator;
import org.jbpm.services.task.utils.TaskFluent;
import org.jbpm.services.task.wih.NonManagedLocalHTWorkItemHandler;
import org.jbpm.test.services.TestIdentityProvider;
import org.jbpm.test.services.TestUserGroupCallbackImpl;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.keycloak.OAuth2Constants;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.util.JsonSerialization;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.api.task.model.User;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.internal.task.api.model.InternalComment;
import org.mindrot.jbcrypt.BCrypt;

import com.google.gson.reflect.TypeToken;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
//import life.genny.bootxport.bootx.GoogleImportService;
//import life.genny.bootxport.bootx.XlsxImport;
//import life.genny.bootxport.bootx.XlsxImportOnline;
import life.genny.eventbus.EventBusInterface;
import life.genny.eventbus.EventBusMock;
import life.genny.eventbus.VertxCache;
import life.genny.jbpm.customworkitemhandlers.ShowFrame;
import life.genny.model.OutputParamTreeSet;
import life.genny.models.BaseEntityImport;
import life.genny.models.Frame3;
import life.genny.models.FramePosition;
import life.genny.models.GennyToken;
import life.genny.models.TableData;
import life.genny.models.Theme;
import life.genny.models.ThemeAttributeType;
import life.genny.models.ThemePosition;
import life.genny.qwanda.Answer;
import life.genny.qwanda.Answers;
import life.genny.qwanda.Ask;
import life.genny.qwanda.Context;
import life.genny.qwanda.ContextType;
import life.genny.qwanda.VisualControlType;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.datatype.DataType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.SearchEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.message.MessageData;
import life.genny.qwanda.message.QBulkMessage;
import life.genny.qwanda.message.QCmdViewTableMessage;
import life.genny.qwanda.message.QDataAnswerMessage;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.message.QEventBtnClickMessage;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwanda.message.QScheduleMessage;
import life.genny.qwanda.message.QSearchBeResult;
import life.genny.qwanda.validation.Validation;
import life.genny.qwanda.validation.ValidationList;
import life.genny.qwandautils.GennyCacheInterface;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.KeycloakUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.rules.QRules;
import life.genny.rules.RulesLoader;
import life.genny.services.BaseEntityService2;
import life.genny.utils.BaseEntityUtils;
import life.genny.utils.FrameUtils2;
import life.genny.utils.GennyJbpmBaseTest;
import life.genny.utils.GennyKieSession;
import life.genny.utils.ImportUtils;
import life.genny.utils.JunitCache;
import life.genny.utils.OutputParam;
import life.genny.utils.RulesUtils;
import life.genny.utils.SearchCallable;
import life.genny.utils.SessionFacts;
import life.genny.utils.TableFrameCallable;
import life.genny.utils.TableUtils;
import life.genny.utils.VertxUtils;

public class RogerTest {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	protected static Boolean USE_STANDALONE = true; // sets whether to use standalone or remote service

	protected static String realm = GennySettings.mainrealm;
	protected static Set<String> realms;

	protected static EventBusInterface eventBusMock;
	protected static GennyCacheInterface vertxCache;

	private static final String DRL_SEND_USER_DATA_DIR = "SendUserData";

	protected EntityManagerFactory emf;
	protected DefinitionService bpmn2Service;
	protected RuntimeDataService runtimeDataService;
	protected ProcessService processService;
	protected UserTaskService userTaskService;
	protected QueryService queryService;
	protected ProcessInstanceAdminService processAdminService;

	protected TestIdentityProvider identityProvider;
	protected TestUserGroupCallbackImpl userGroupCallback;

	protected KieServiceConfigurator serviceConfigurator;

	protected DeploymentUnit deploymentUnit;

	protected static GennyToken userToken;
	protected static GennyToken newUserToken;
	protected static GennyToken serviceToken;
	
	
//	@Test
//	public void rulesloadertest() {
//		//RulesLoader.init();
//		log.info("Loading Rules");
//		Boolean noChangeInRules = RulesLoader.loadRules(GennySettings.rulesDir);
//
//		System.out.println(noChangeInRules);
//		
//		if ((!noChangeInRules) || (!"TRUE".equalsIgnoreCase(System.getenv("DISABLE_INIT_RULES_STARTUP")))) {
//			log.info("Rulesservice triggering rules");
//			(new RulesLoader()).triggerStartupRules(GennySettings.rulesDir);
//		} else {
//			log.warn("DISABLE_INIT_RULES_STARTUP IS TRUE -> No Init Rules triggered.");
//		}
//	}
 	 
	public GennyToken createGennyToken(final String uuid, final String realm, String username, String name, String role)
	{
		return createGennyToken(uuid,realm, username, name, role,24*60*60);
	}



	public  GennyToken createGennyToken(final String realm, String username, String name, String role)
	{
		return createGennyToken(realm, username, name, role,24*60*60);
	}

	public  GennyToken createGennyToken(final String realm, String username, String name, String role, long expirysecs)
	{
		String normalisedUsername = null;
		if (!username.startsWith("PER_")) {
			normalisedUsername = "PER_"+username.toUpperCase();
		} else {
			normalisedUsername = username.toUpperCase();
		}
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime expiryTime = now.plusSeconds(expirysecs);
		GennyToken gennyToken = new GennyToken(normalisedUsername,realm,username,name,role,expiryTime);
		return gennyToken;
	}			
	public  GennyToken createGennyToken(String uuid,final String realm, String username, String name, String role, long expirysecs)
	{
		String normalisedUsername = null;
		if (!username.startsWith("PER_")) {
			normalisedUsername = "PER_"+username.toUpperCase();
		} else {
			normalisedUsername = username.toUpperCase();
		}
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime expiryTime = now.plusSeconds(expirysecs);
		GennyToken gennyToken = new GennyToken(uuid,normalisedUsername,realm,username,name,role,expiryTime);
		return gennyToken;
	}		
	
   @Test // wrong
	public void sessionTest_createUser_returnSuccess() {
	   
  	  
	    //RulesLoader.taskServiceMap.put(key, value)
//		GennyToken tokenUser = GennyJbpmBaseTest.createGennyToken("ABCDEFGH", "internmatch", "adam.crow@gada.io",
//				"Adam Crow", "admin");
//		GennyToken tokenSupervisor = GennyJbpmBaseTest.createGennyToken("BCDEFGSHS", "internmatch",
//				"kanika.gulati@gada.io", "Kanika Gulati", "supervisor");
		
		GennyToken tokenUser = createGennyToken("8966bf0c-25a2-4e28-9345-d17e3a59a2ea","internmatch", "kanika.test@outcome.life", "Kanika Gulati", "admin");
		GennyToken tokenSupervisor = createGennyToken("91751d1f-6dfe-4510-ac98-6cd93f40c241","internmatch", "adamccrow63+supervisor@gmail.com", "Chris Pyke", "supervisor");
		
		
		System.out.println(tokenUser.getToken());
		System.out.println(tokenSupervisor.getToken());

		System.out.println("showFrame Test");

//		VertxUtils.cacheInterface = new JunitCache();
//		VertxUtils.cachedEnabled = true;
		
		//VertxUtils.cacheInterface = new JunitCache();
		GennySettings.forceCacheApi = true;
		VertxUtils.cachedEnabled = true; // don't send to local Service Cache
		VertxUtils.eb = new EventBusMock();
		
		 RulesLoader ruleLoader = new RulesLoader();
		   ruleLoader.realms.add("internmatch");
//		   RulesLoader.emf = Persistence.createEntityManagerFactory( "org.jbpm.services.task" );
	 	   ruleLoader.triggerStartupRules(GennySettings.rulesDir);
		
		GennyKieSession gks = null;
		tokenUser.setUserCode("PER_SERVICE");
		
		//RulesLoader.taskServiceMap.put(tokenUser.getSessionCode(), value);
		try {
			gks = GennyKieSession.builder(tokenUser, true)
					.addDrl("SignalProcessing")
					.addDrl("DataProcessing")
					.addDrl("EventProcessing")
					.addJbpm("Lifecycles")
					.addDrl("AuthInit")
					.addJbpm("AuthInit")
					.addDrl("InitialiseProject")
					.addJbpm("InitialiseProject")
					.build();

			gks.createTestUsersGroups();

			GennyToken newUser1A = gks.createToken("PER_USER1");
			gks.start();

			 
			
			//gks.injectSignal("initProject"); // This should initialise everything
			gks.injectSignal("initProject");
			gks.injectEvent("authInitMsg", newUser1A); // log in as new user
			
			gks.injectSignal("initProjectFrames");
			
			gks.advanceSeconds(5, false);
			gks.displayTasks(newUser1A);

			gks.showStatuses();
			// Now answer a question

//			gks.injectAnswer("PRI_FIRSTNAME", newUser1A);
//			gks.injectAnswer("PRI_LASTNAME", newUser1A);
//			gks.injectAnswer("PRI_DOB", newUser1A);
//			gks.injectAnswer("PRI_PREFERRED_NAME", newUser1A);
//			gks.injectAnswer("PRI_EMAIL", newUser1A);
//			gks.injectAnswer("PRI_MOBILE", newUser1A);
//			gks.injectAnswer("PRI_USER_PROFILE_PICTURE", newUser1A);
//			gks.injectAnswer("PRI_ADDRESS_FULL", newUser1A);

//			gks.injectEvent("QUE_SUBMIT", newUser1A);

			// Now add an Edu Provider

			gks.injectEvent("msgLogout", newUser1A);
			gks.advanceSeconds(5, false);

			gks.showStatuses("PER_USER1", "PER_USER2");
			// gks.injectEvent("msgLogout",newUser2B);
			// gks.injectEvent("msgLogout",newUser1A);
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			if (gks != null) {
				gks.close();
			}
		}
	}

   @Test
   public void tokenTest_newUsertoken_returnSuccess() {
		System.out.println("Process View Test");
		GennyToken userToken = null;
		GennyToken serviceToken = null;
		QRules qRules = null;
		//VertxUtils.cacheInterface = new JunitCache();
		GennySettings.forceCacheApi = true;

		if (true) {
			userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
			
			GennyToken testToken = new GennyToken(userToken.getToken());
			System.out.println(testToken.getUserCode());
			System.out.println(testToken.getUsername());
			System.out.println(testToken.getUuid());
			serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
			qRules = new QRules(eventBusMock, userToken.getToken());
			qRules.set("realm", userToken.getRealm());
			qRules.setServiceToken(serviceToken.getToken());
			VertxUtils.cachedEnabled = true; // don't send to local Service Cache
			GennyKieSession.loadAttributesJsonFromResources(userToken);

		} else {
			qRules = GennyJbpmBaseTest.setupLocalService();
			userToken = new GennyToken("userToken", qRules.getToken());
			serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
		}

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		// System.out.println("userToken2 =" + userToken2.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		BaseEntity intern = new BaseEntity("PRI_INTERN");
		BaseEntity internship = new BaseEntity("BE_INTERNSHIP");
		BaseEntity hostCompany = new BaseEntity("CPY_HOSTCOMPANY");

		/* HashMap<String, BaseEntity> hashBeg = new HashMap<String, BaseEntity>(); */
		HashMap<String, String> hashBeg = new HashMap<String, String>();

		hashBeg.put("begstatus", "DUDE");

		/*
		 * hashBeg.put("intern", intern); hashBeg.put("internship", internship);
		 * hashBeg.put("hostCompany", hostCompany);
		 */

		SessionFacts initFacts = new SessionFacts(serviceToken, null, new QEventMessage("EVT_MSG", "INIT_STARTUP"));
		QEventMessage authInitMsg = new QEventMessage("EVT_MSG", "AUTH_INIT");
		authInitMsg.setToken(userToken.getToken());
		QEventMessage msgLogout = new QEventMessage("EVT_MSG", "LOGOUT");
		msgLogout.setToken(userToken.getToken());

		// NOW SET UP Some baseentitys
		BaseEntity project = new BaseEntity("PRJ_" + serviceToken.getRealm().toUpperCase(),
				WordUtils.capitalizeFully(serviceToken.getRealm()));
		project.setRealm(serviceToken.getRealm());
		VertxUtils.writeCachedJson(serviceToken.getRealm(), "PRJ_" + serviceToken.getRealm().toUpperCase(),
				JsonUtils.toJson(project), serviceToken.getToken());
		VertxUtils.writeCachedJson(realm, ":" + "PRJ_" + serviceToken.getRealm().toUpperCase(),
				JsonUtils.toJson(project), serviceToken.getToken());

		GennyKieSession gks = null;

		try {

			gks = GennyKieSession.builder(serviceToken, true)

					// ADD THE JBPM WORKFLOWS HERE

					.addJbpm("pidTest.bpmn")

					// ADD THE DROOLS RULES HERE

					.addToken(userToken).build();

			gks.start();

			GennyToken newUser2A = gks.createToken("PER_USER2 sss");
			GennyToken newUser2B = gks.createToken("PER_USER2 sss");
			/* Start Process */

			SessionFacts sf = new SessionFacts(serviceToken, userToken, "APP_ONE");
			gks.injectSignal("START_MOVE", sf);
			SessionFacts sf2 = new SessionFacts(serviceToken, userToken, "APP_TWO");
			gks.injectSignal("START_MOVE", sf2);

			Optional<Long> pid = GennyKieSession.getProcessIdByWorkflowBeCode(realm, "APP_TWO");
			if (pid.isPresent()) {
				log.info("PID is " + pid);
			}

			Optional<Long> pid2 = GennyKieSession.getProcessIdByWorkflowBeCode(realm, "APP_ONE");
			if (pid2.isPresent()) {
				log.info("PID2 is " + pid2);
			}
			gks.advanceSeconds(5, false);
			gks.advanceSeconds(5, false);
			/* Query Process */

			/* Send a signal to it */

		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			if (gks != null) {
				gks.close();
			}
		}
	}

   @Test
   @Ignore
	public void userTaskTest()
	{
		System.out.println("Process View Test");
		GennyToken userToken = null;
		GennyToken serviceToken = null;
		QRules qRules = null;
		GennySettings.forceCacheApi = true;
		if (true) {
			userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
			serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
			qRules = new QRules(eventBusMock, userToken.getToken());
			qRules.set("realm", userToken.getRealm());
			qRules.setServiceToken(serviceToken.getToken());
			VertxUtils.cachedEnabled = true; // don't send to local Service Cache
			GennyKieSession.loadAttributesJsonFromResources(userToken);

		} else {
			qRules = GennyJbpmBaseTest.setupLocalService();
			userToken = new GennyToken("userToken", qRules.getToken());
			serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
		}

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		//System.out.println("userToken2   =" + userToken2.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());
		
       BaseEntity intern = new BaseEntity("PRI_INTERN");
       BaseEntity internship = new BaseEntity("BE_INTERNSHIP");        
       BaseEntity hostCompany = new BaseEntity("CPY_HOSTCOMPANY");

       /*HashMap<String, BaseEntity> hashBeg = new HashMap<String, BaseEntity>();*/
       HashMap<String, String> hashBeg = new HashMap<String, String>();
       
       hashBeg.put("begstatus", "DUDE");
       
       /*hashBeg.put("intern", intern);
       hashBeg.put("internship", internship);
       hashBeg.put("hostCompany", hostCompany);*/

		SessionFacts initFacts = new SessionFacts(serviceToken, null, new QEventMessage("EVT_MSG", "INIT_STARTUP"));
		QEventMessage authInitMsg = new QEventMessage("EVT_MSG", "AUTH_INIT"); authInitMsg.setToken(userToken.getToken());
		QEventMessage msgLogout = new QEventMessage("EVT_MSG", "LOGOUT");msgLogout.setToken(userToken.getToken());


		// NOW SET UP Some baseentitys
		BaseEntity project = new BaseEntity("PRJ_" + serviceToken.getRealm().toUpperCase(),
				org.codehaus.plexus.util.StringUtils.capitaliseAllWords(serviceToken.getRealm()));
		project.setRealm(serviceToken.getRealm());
		VertxUtils.writeCachedJson(serviceToken.getRealm(), "PRJ_" + serviceToken.getRealm().toUpperCase(),
				JsonUtils.toJson(project), serviceToken.getToken());
		VertxUtils.writeCachedJson(realm,  ":" + "PRJ_" + serviceToken.getRealm().toUpperCase(),JsonUtils.toJson(project), serviceToken.getToken());


		GennyKieSession gks = null;

		try {
			
			
			gks = GennyKieSession
					.builder(serviceToken,true)

//ADD THE JBPM WORKFLOWS HERE					

					.addJbpm("pidTest.bpmn")
					
//ADD THE DROOLS RULES HERE


					.addToken(userToken)
					.build();
			
			gks.start();
			
			GennyToken newUser2A = gks.createToken("PER_USER2 ss"); 
			GennyToken newUser2B = gks.createToken("PER_USER2 ss"); 
			/* Start Process */
			
			 gks.startProcess("pidTest");
			
			/*
			 * 
			//  
			gks.start();
				SessionFacts sf = new SessionFacts(serviceToken, userToken, "APP_ONE");
				gks.injectSignal("START_MOVE", sf);
				SessionFacts sf2 = new SessionFacts(serviceToken, userToken, "APP_TWO");
				gks.injectSignal("START_MOVE", sf2);
	
				Optional<Long> pid = GennyKieSession.getProcessIdByWorkflowBeCode(realm, "APP_TWO");
				if (pid.isPresent()) {
					log.info("PID is " + pid);
				}
	
				Optional<Long> pid2 = GennyKieSession.getProcessIdByWorkflowBeCode(realm, "APP_ONE");
				if (pid2.isPresent()) {
					log.info("PID2 is " + pid2);
				}
				gks.advanceSeconds(5, false);
				gks.advanceSeconds(5, false);
			 * 
			 */
			
			/* Query Process */
			
			/* Send a signal to it */
			
			
           
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		finally {
			if (gks!=null) {
				gks.close();
			}
		}
	}

	 

}
