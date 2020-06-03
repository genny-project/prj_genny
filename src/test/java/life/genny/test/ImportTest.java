package life.genny.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
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

import org.junit.BeforeClass;
import org.junit.Test;
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

import com.google.gson.reflect.TypeToken;

import io.vavr.Tuple;
import io.vavr.Tuple2;
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
import life.genny.qwanda.datatype.DataType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.SearchEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.message.MessageData;
import life.genny.qwanda.message.QBulkMessage;
import life.genny.qwanda.message.QDataAnswerMessage;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.message.QEventBtnClickMessage;
import life.genny.qwanda.message.QEventMessage;
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

public class ImportTest {

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

	@Test
	public void priStatusTest()
	{
		System.out.println("PriStatus test");
		GennyToken userToken = null;
		GennyToken serviceToken = null;
		QRules qRules = null;

		if (false) {
			userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
			serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
			qRules = new QRules(eventBusMock, userToken.getToken());
			qRules.set("realm", userToken.getRealm());
			qRules.setServiceToken(serviceToken.getToken());
			VertxUtils.cachedEnabled = true; // don't send to local Service Cache
			GennyKieSession.loadAttributesJsonFromResources(userToken);

		} else {
			// VertxUtils.cachedEnabled = false;
			VertxUtils.cachedEnabled = false;
			qRules = GennyJbpmBaseTest.setupLocalService();
			userToken = new GennyToken("userToken", qRules.getToken());
			serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
			eventBusMock = new EventBusMock();
			vertxCache = new JunitCache(); // MockCache
			VertxUtils.init(eventBusMock, vertxCache);
		}

		BaseEntityUtils beUtils = new BaseEntityUtils(userToken);
		beUtils.setServiceToken(serviceToken);

		long starttime = System.currentTimeMillis();
		long looptime = 0;
		long searchtime = 0;
		SearchEntity searchBE = new SearchEntity("SBE_TEST", "All Journals")
				.addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "JNL_%")
				/* .addFilter("PRI_SYNC", SearchEntity.StringFilter.LIKE, "FALSE") */
/*				.addColumn("PRI_NAME", "Name").addColumn("LNK_INTERNSHIP", "Internship")
				.addColumn("LNK_INTERN", "Intern").addColumn("LNK_HOST_COMPANY_REP", "Host Company Rep")
				.addColumn("LNK_HOST_COMPANY", "Host Company").addColumn("LNK_INTERN_SUPERVISOR", "Supervisor")
				.addColumn("PRI_JOURNAL_DATE", "Date").addColumn("PRI_JOURNAL_HOURS", "Hours")
				.addColumn("PRI_JOURNAL_TASKS", "Tasks").addColumn("PRI_JOURNAL_LEARNING_OUTCOMES", "Learning Outcomes")
				.addColumn("PRI_FEEDBACK", "Feedback").addColumn("PRI_STATUS", "Status").addColumn("PRI_SYNC", "Synced")
				.addColumn("PRI_LAST_UPDATED", "Last Updated").addColumn("PRI_INTERN_LAST_UPDATE", "Intern last update")
				.addColumn("PRI_SUPERVISOR_LAST_UPDATE", "Last Supervisor Update")
				.addColumn("PRI_LAST_CHANGED_BY", "Last Changed By") */
				.setPageStart(0).setPageSize(20000);

		searchBE.setRealm(serviceToken.getRealm());


		String jsonSearchBE = JsonUtils.toJson(searchBE);
		/* System.out.println(jsonSearchBE); */
		String resultJson;
		BaseEntity result = null;
		int fixed = 0;
		try {
			resultJson = QwandaUtils.apiPostEntity(GennySettings.qwandaServiceUrl + "/qwanda/baseentitys/search",
					jsonSearchBE, serviceToken.getToken());
			searchtime = System.currentTimeMillis();
			QDataBaseEntityMessage resultMsg = JsonUtils.fromJson(resultJson, QDataBaseEntityMessage.class);
			/*
			 * System.out.println(drools.getRule().getName()
			 * +" Got to here in RETURN JOURNALS "+resultJson);
			 */
			BaseEntity[] bes = resultMsg.getItems();
			System.out.println("Returned " + bes.length + " items");
			System.out.println("The count return " + resultMsg.getTotal());
			/* Now only send the ones that are not synced */
			List<BaseEntity> unsyncedItemList = new ArrayList<BaseEntity>();
			int index=0;
			for (BaseEntity be : bes) {
				Optional<String> status = be.getValue("PRI_STATUS");
				if (!status.isPresent()) {
					
					beUtils.saveAnswer(new Answer(be.getCode(),be.getCode(),"PRI_STATUS","UNAPPROVED"));
					fixed++;
				}
				if ((index % 10)==0) {
					System.out.println("Checking "+index+" of "+resultMsg.getItems().length);
				}
				index++;

			}
			looptime = System.currentTimeMillis();
			resultMsg.setItems(unsyncedItemList.toArray(new BaseEntity[0]));

		} catch (Exception e1) {
			e1.printStackTrace();
		}

		long endtime = System.currentTimeMillis();
		System.out.println("search took " + (searchtime - starttime) + " ms");
		System.out.println("loop took " + (looptime - searchtime) + " ms");
		System.out.println("finish took " + (endtime - looptime) + " ms");
		System.out.println("total took " + (endtime - starttime) + " ms");	
		System.out.println("Fixed "+fixed+" journals");

	}
	


	//@Test
	public void importTest()
	{
		System.out.println("Import test");
		GennyToken userToken = null;
		GennyToken serviceToken = null;
		QRules qRules = null;

		if (false) {
			userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
			serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
			qRules = new QRules(eventBusMock, userToken.getToken());
			qRules.set("realm", userToken.getRealm());
			qRules.setServiceToken(serviceToken.getToken());
			VertxUtils.cachedEnabled = true; // don't send to local Service Cache
			GennyKieSession.loadAttributesJsonFromResources(userToken);

		} else {
			// VertxUtils.cachedEnabled = false;
			VertxUtils.cachedEnabled = false;
			qRules = GennyJbpmBaseTest.setupLocalService();
			userToken = new GennyToken("userToken", qRules.getToken());
			serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
			eventBusMock = new EventBusMock();
			vertxCache = new JunitCache(); // MockCache
			VertxUtils.init(eventBusMock, vertxCache);
		}

		BaseEntityUtils beUtils = new BaseEntityUtils(userToken);
		beUtils.setServiceToken(serviceToken);

		
		String searchBarValue = "import:" + "https://internmatch-interns.gada.io";
		Answer answer = new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_SEARCH_BAR", searchBarValue);

		/* Now import a google doc xls file and generate a List of BaseEntityImports */
		String gennyUrl = null;
		Integer start = 0;
		Integer finish = 10000;
		String[] split = answer.getValue().split(":");

		if (split.length == 2) {
			gennyUrl = split[1];
		} 

		gennyUrl = gennyUrl.trim();
		
		System.out.println("gennyUrl = "+gennyUrl);
	}
	


	String showTaskNames(List<TaskSummary> tasks) {
		String ret = "";
		if (tasks.isEmpty()) {
			return "(empty)";
		}
		for (TaskSummary task : tasks) {
			ret += "[" + task.getName() + "(" + task.getProcessId() + "):" + task.getStatusId() + "],";
		}
		return ret;
	}



	@BeforeClass
	public static void init() throws FileNotFoundException, SQLException {

		System.out.println("BridgeUrl=" + GennySettings.bridgeServiceUrl);
		System.out.println("QwandaUrl=" + GennySettings.qwandaServiceUrl);

		GennyToken tokenUser = GennyJbpmBaseTest.createGennyToken("ABCDEFGH", "internmatch", "adam.crow@gada.io",
				"Adam Crow", "intern");
		GennyToken tokenSupervisor = GennyJbpmBaseTest.createGennyToken("BCDEFGSHS", "internmatch",
				"kanika.gulati@gada.io", "Kanika Gulati", "supervisor");
		System.out.println(tokenUser.getToken());
		System.out.println(tokenSupervisor.getToken());

		// Set up realm
		realms = new HashSet<String>();
		realms.add(realm);
		realms.stream().forEach(System.out::println);
		realms.remove("genny");

		// Enable the PseudoClock using the following system property.
		System.setProperty("drools.clockType", "pseudo");

		eventBusMock = new EventBusMock();
		vertxCache = new VertxCache(); // MockCache
		VertxUtils.init(eventBusMock, vertxCache);

		QRules qRules = null;

		if (USE_STANDALONE) {
			serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
			VertxUtils.cachedEnabled = true; // don't send to local Service Cache
			GennyKieSession.loadAttributesJsonFromResources(serviceToken);

		} else {
			qRules = GennyJbpmBaseTest.setupLocalService();
			userToken = new GennyToken("userToken", qRules.getToken());
			serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
		}

		System.out.println("serviceToken=" + serviceToken.getToken());

	}

	private void update(Object obj) {
		log.info("Updated fact " + obj + " in rules engine");
	}

	private void retract(Object obj) {
		log.info("Retract fact " + obj + " in rules engine");
	}

	private void insert(Object obj) {
		// log.info("Insert fact "+obj+" in rules engine");
	}
}
