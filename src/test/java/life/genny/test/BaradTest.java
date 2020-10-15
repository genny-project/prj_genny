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
import life.genny.qwanda.message.QDataAnswerMessage;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.message.QEventBtnClickMessage;
import life.genny.qwanda.message.QEventMessage;
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

public class AdamTest {

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
	public void testMerge()
	{
		System.out.println("Test Merge");
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
        String sourceCode = "PER_74EC4B96-C1AE-423E-91DE-829112BDEB43";
        String targetCode = "PER_6B16911E-D277-4815-BDF1-06AC7FD3FE04";

        // fetch the BEs
        BaseEntity source = beUtils.getBaseEntityByCode(sourceCode);
        BaseEntity target = beUtils.getBaseEntityByCode(targetCode);

        // merge the BEs
        for (EntityAttribute ea : source.getBaseEntityAttributes()){
            Answer ans = new Answer(target, target, ea.getAttribute(), ea.getValueAsString());
            ans.setInferred(ea.getinferred());
            target.addAnswer(ans);
        } 
        beUtils.updateBaseEntity(target);

        // fix up all the links of the relates BEs to point to the target

        String encodedsql = encodeValue("update baseentity_attribute set valueString='[\"" + target.getCode()
        + "\"]' where valueString='[\"" + source.getCode() + "\"]'");
        String resultJson = QwandaUtils.apiGet(
        GennySettings.qwandaServiceUrl + "/service/executesql/" + encodedsql,
        serviceToken.getToken());

        // Search for ones that already have link
        
        // Delete source BEs

        // Delete other related BEs 

        encodedsql = encodeValue("delete from baseentity_attribute where baseentityCode='" + source.getCode()+"'");
        resultJson = QwandaUtils.apiGet(
        GennySettings.qwandaServiceUrl + "/service/executesql/" + encodedsql,
        serviceToken.getToken());

        encodedsql = encodeValue("delete from baseentity where code='" + source.getCode()+"'");
        resultJson = QwandaUtils.apiGet(
        GennySettings.qwandaServiceUrl + "/service/executesql/" + encodedsql,
        serviceToken.getToken());

	}
	
	@Test
	public void approveAllJournals() {
		String encodedsql = encodeValue("update baseentity_attribute set valueString='APPROVED' where attributeCode='PRI_STATUS' and baseentitycode like 'JNL_%' and valueString = 'UNAPPROVED'");
        String resultJson = QwandaUtils.apiGet(
        GennySettings.qwandaServiceUrl + "/service/executesql/" + encodedsql,
		serviceToken.getToken());
	}

	@Test
	public void testPhoneNumber() {
		processPhoneNumber("0434321232");
		processPhoneNumber("61434321232");
		processPhoneNumber("0398857158");
		processPhoneNumber("131333");
		processPhoneNumber("1300776554");
		processPhoneNumber("13007765543");
		processPhoneNumber("1800654321");
		processPhoneNumber("61377776666");
	}

	public void processPhoneNumber(String phonenumber) {

		if (phonenumber != null) {
			/* remove all non digits */
			phonenumber = phonenumber.replaceAll("[^\\d]", "");
			if (!phonenumber.startsWith("+")) {
				if (phonenumber.startsWith("0")) {
					phonenumber = "61" + phonenumber.substring(1); /* remove the 0 and assume Australian */
				}
			}
			phonenumber = StringUtils.deleteWhitespace(phonenumber);

			if (checkPhone(phonenumber)) {
				/* Now check if it is an Australian mobile */
				if ((phonenumber.startsWith("614")) || (phonenumber.startsWith("615"))) {
					System.out.println("Mobile detected = " + phonenumber);
				} else {
					System.out.println("Landline detected = " + phonenumber);

				}
			} else {
				String message = "Phone number is invalid ! (check number of digits) " + phonenumber;
				System.out.println(message);

			}

		} else
		{
			String message = "Phone number is empty!";
			System.out.println(message);
		}

	}

	@Test
	public void fixPhoneNumbers() {

		System.out.println("Fix Phone Numbers test");
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

		SearchEntity searchBE = new SearchEntity("FIND PHONE NUMBERS", "Update")
				.addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "PER_%").addColumn("PRI_PHONE", "Phone")
				.setPageStart(0).setPageSize(10000000);

		searchBE.setRealm(serviceToken.getRealm());

		System.out.println("About to search for intern");
		List<BaseEntity> bes = beUtils.getBaseEntitys(searchBE);
		for (BaseEntity per : bes) {
			BaseEntity be = beUtils.getBaseEntityByCode(per.getCode());
			Optional<String> phoneNumber = be.getValue("PRI_PHONE");
			Optional<String> mobile = be.getValue("PRI_MOBILE");
			Optional<String> landline = be.getValue("PRI_LANDLINE");

			if (phoneNumber.isPresent()) {

				if (!StringUtils.isBlank(phoneNumber.get())) {
					if (!checkPhone(phoneNumber.get())) {
						System.out.println("BAD phone number " + phoneNumber.get() + " for be " + be.getCode());
						String fixedNum = normalisePhone(phoneNumber.get());
						if (!checkPhone(fixedNum)) {
							System.out.println("STILL BAD phone number " + fixedNum + " for be " + be.getCode());
						} else {
							Answer ans = new Answer(per.getCode(), per.getCode(), "PRI_PHONE", fixedNum);
							beUtils.saveAnswer(ans);
						}
					} else {

					}
				}
			}

			if (mobile.isPresent()) {
				if (!StringUtils.isBlank(mobile.get())) {
					if (!checkPhone(mobile.get())) {
						System.out.println("BAD mobile number " + mobile.get() + " for be " + be.getCode());
						String fixedNum = normalisePhone(mobile.get());
						if (!checkPhone(fixedNum)) {
							System.out.println("STILL BAD mobile number " + fixedNum + " for be " + be.getCode());
						} else {
							Answer ans = new Answer(per.getCode(), per.getCode(), "PRI_MOBILE", fixedNum);
							beUtils.saveAnswer(ans);
						}

					} else if (!phoneNumber.isPresent()) {
						Answer ans = new Answer(per.getCode(), per.getCode(), "PRI_PHONE", mobile.get());
						beUtils.saveAnswer(ans);
					}
				}

			}

			if (landline.isPresent()) {
				if (!StringUtils.isBlank(landline.get())) {

					if (!checkPhone(landline.get())) {
						System.out.println("BAD landline number " + landline.get() + " for be " + be.getCode());
						String fixedNum = normalisePhone(landline.get());
						if (!checkPhone(fixedNum)) {
							System.out.println("STILL BAD landline number " + fixedNum + " for be " + be.getCode());
						} else {
							Answer ans = new Answer(per.getCode(), per.getCode(), "PRI_LANDLINE", fixedNum);
							beUtils.saveAnswer(ans);
						}

					} else if (!phoneNumber.isPresent()) {
						Answer ans = new Answer(per.getCode(), per.getCode(), "PRI_PHONE", landline.get());
						beUtils.saveAnswer(ans);
					}

				}
			}

		}

	}

	private Boolean checkPhone(String phonenum) {
		return checkregex(phonenum,
				"^(\\d{2}){0,1}((0{0,1}[2|3|7|8]{1}[ \\-]*(\\d{4}\\d{4}))|(\\d{2}){0,1}(1[ \\-]{0,1}(300|800|900|902)[ \\-]{0,1}((\\d{6})|(\\d{3}\\d{3})))|(13[ \\-]{0,1}([\\d \\-]{4})|((\\d{0,2})0{0,1}4{1}[\\d \\-]{8,10})))$");
	}

	private Boolean checkregex(String input, String regex) {

		// Create a Pattern object
		Pattern r = Pattern.compile(regex);

		// Now create matcher object.
		Matcher m = r.matcher(input);
		if (m.find()) {
			return true;
		}
		return false;
	}

	private String normalisePhone(String phonenumber) {
		if (phonenumber != null) {
			phonenumber = StringUtils.deleteWhitespace(phonenumber);
			/* remove all non digits */
			phonenumber = phonenumber.replaceAll("[^\\d]", "");
			if (!phonenumber.startsWith("+")) {
				if (phonenumber.startsWith("0")) {
					phonenumber = "61" + phonenumber.substring(1); /* remove the 0 and assume Australian */
				} else if (phonenumber.startsWith("610")) {
					phonenumber = "61" + phonenumber.substring(3); /* remove the 0 and assume Australian */
				}
			}

		}

		return phonenumber;
	}

	@Test
	public void testBucket() {

		System.out.println("Submit Button test");
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

		Answer answer = new Answer(userToken.getUserCode(), "PER_1C39E067-C9D4-44E5-9053-6B98159502F7", "PRI_IMAGE_URL",
				"http://127.0.0.1:9898/public/d33ef76a-76cb-432b-aa57-dcb4af86e760");

		// http://127.0.0.1:9898/public/d33ef76a-76cb-432b-aa57-dcb4af86e760
//http://127.0.0.1:9898/public/487fbf91-7030-4903-91e8-b1e0b39ab3c7
		BaseEntity person = beUtils.getBaseEntityByCode(answer.getTargetCode());

		Boolean isIntern = person.is("PRI_IS_INTERN");
		if (isIntern) {
			/* copy across the new details to an app */

			SearchEntity searchBE = new SearchEntity("FIND APPS", "Update")
					.addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
					.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "APP_%")
					.addFilter("PRI_INTERN_CODE", SearchEntity.StringFilter.LIKE, "%" + answer.getTargetCode() + "%")
					.setPageStart(0).setPageSize(100);

			searchBE.setRealm(serviceToken.getRealm());

			System.out.println("About to search for intern");
			List<BaseEntity> bes = beUtils.getBaseEntitys(searchBE);
			beUtils.saveAnswer(new Answer(userToken.getUserCode(), person.getCode(), "PRI_IMAGE_URL", answer.getValue(),
					false, true));
			for (BaseEntity app : bes) {
				Answer ans = new Answer(userToken.getUserCode(), app.getCode(), "PRI_IMAGE_URL", answer.getValue(),
						false, true);
				System.out.println("Updating image on app " + app.getCode());

				try {
					BaseEntity be = new BaseEntity(app.getCode(), app.getName());
					be.addAttribute(RulesUtils.getAttribute("PRI_IMAGE_URL", userToken.getToken()));
					be.setValue("PRI_IMAGE_URL", answer.getValue());
					QDataBaseEntityMessage msg = new QDataBaseEntityMessage(be);
					msg.setReplace(true);
					String[] recips = app.getPushCodes();

					Set<String> pushCodes = new HashSet<>(Arrays.asList(recips));
					String userCode = userToken.getUserCode();
					pushCodes.add(userCode);
					pushCodes.add("SUPERUSER");
					pushCodes.add("ADMIN");
					pushCodes.add("AGENT");
					msg.setRecipientCodeArray(pushCodes.toArray(new String[0]));
					msg.setToken(userToken.getToken());
					VertxUtils.writeMsg("project", msg);
					VertxUtils.writeMsgEnd(userToken, pushCodes);

				} catch (BadDataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			VertxUtils.writeMsgEnd(userToken);

			/* update(answersToSave); */
		}
	}

	@Test
	public void testEmailSearch() {
		System.out.println("Submit Button test");
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

		Answer answer = new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_EMAIL",
				"domenic@outcome.life");

		SearchEntity searchBE = new SearchEntity("TEst", "Email People")
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "PER_%")
				.addFilter("PRI_EMAIL", SearchEntity.StringFilter.EQUAL, answer.getValue())
				.addColumn("PRI_NAME", "Name").setPageStart(0).setPageSize(100);

		searchBE.setRealm(beUtils.getServiceToken().getRealm());

		Tuple2<String, List<String>> data = beUtils.getHql(searchBE);
		String hql = data._1;

		hql = Base64.getUrlEncoder().encodeToString(hql.getBytes());
		try {
			String resultJsonStr = QwandaUtils.apiGet(
					GennySettings.qwandaServiceUrl + "/qwanda/baseentitys/count24/" + hql,
					beUtils.getServiceToken().getToken(), 120);

			System.out.println("Search EMAIL result is " + resultJsonStr);
			if (resultJsonStr.equals("0")) {
				System.out.println("This email " + answer.getValue() + " is unique");
			} else {
				VertxUtils.sendFeedbackError(beUtils.getGennyToken(), answer, "Email Address is already taken");
				retract(answer);

			}
		} catch (Exception e) {

		}
	}

	@Test
	public void internAppImageFix() {
		System.out.println("Submit Button test");
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

		SearchEntity searchBE = new SearchEntity("SBE_INTERNSHIP_IMAGE_FIX", "Update")
				.addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "APP_%").addColumn("PRI_CODE", "Name")
				.addColumn("PRI_INTERN_CODE", "Intern").setPageStart(0).setPageSize(100000);

		searchBE.setRealm(serviceToken.getRealm());

		System.out.println("About to search for apps");
		List<BaseEntity> apps = beUtils.getBaseEntitys(searchBE);

		System.out.println("Number of apps = " + apps.size());

		for (BaseEntity app : apps) {

			BaseEntity is = beUtils.getBaseEntityByCode(app.getCode());
			// fetch host company from internship
			try {
				String LNK_INTERN = is.getValueAsString("PRI_INTERN_CODE");
				if (LNK_INTERN != null) {
					// LNK_INTERN = LNK_INTERN.substring(2,LNK_INTERN.length()-2);
					System.out.println("Intern :" + LNK_INTERN);
					BaseEntity intern = beUtils.getBaseEntityByCode(LNK_INTERN);
					String imageUrl = intern.getValue("PRI_IMAGE_URL", null);
					if (!StringUtils.isBlank(imageUrl)) {
						beUtils.saveAnswer(new Answer(is.getCode(), is.getCode(), "PRI_IMAGE_URL", imageUrl));
					} else {
						imageUrl = intern.getValue("PRI_USER_PROFILE_PICTURE", null);
						if (!StringUtils.isBlank(imageUrl)) {
							beUtils.saveAnswer(new Answer(is.getCode(), is.getCode(), "PRI_IMAGE_URL", imageUrl));
							beUtils.saveAnswer(
									new Answer(intern.getCode(), intern.getCode(), "PRI_IMAGE_URL", imageUrl));
						}
					}
				} else {
					System.out.println(app.getCode() + " has NO LNK_INTERN");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Test
	public void internshipImageFix() {
		System.out.println("Submit Button test");
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

		SearchEntity searchBE = new SearchEntity("SBE_INTERNSHIP_IMAGE_FIX", "Update")
				.addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "BEG_%").addColumn("PRI_CODE", "Name")
				.addColumn("LNK_HOST_COMPANY", "Host Company").setPageStart(0).setPageSize(100000);

		searchBE.setRealm(serviceToken.getRealm());

		System.out.println("About to search for internships");
		List<BaseEntity> internships = beUtils.getBaseEntitys(searchBE);

		System.out.println("Number of Internships = " + internships.size());

		for (BaseEntity internship : internships) {

			BaseEntity is = beUtils.getBaseEntityByCode(internship.getCode());
			// fetch host company from internship
			try {
				String LNK_HOST_COMPANY = is.getValueAsString("LNK_HOST_COMPANY");
				if (LNK_HOST_COMPANY != null) {
					LNK_HOST_COMPANY = LNK_HOST_COMPANY.substring(2, LNK_HOST_COMPANY.length() - 2);
					System.out.println("Host Company :" + LNK_HOST_COMPANY);
					BaseEntity hostCompany = beUtils.getBaseEntityByCode(LNK_HOST_COMPANY);
					String imageUrl = hostCompany.getValue("PRI_IMAGE_URL", null);
					if (StringUtils.isBlank(imageUrl)) {
						beUtils.saveAnswer(new Answer(is.getCode(), is.getCode(), "PRI_IMAGE_URL", imageUrl));
					} else {
						imageUrl = hostCompany.getValue("PRI_USER_PROFILE_PICTURE", null);
						if (imageUrl != null) {
							beUtils.saveAnswer(new Answer(is.getCode(), is.getCode(), "PRI_IMAGE_URL", imageUrl));
							beUtils.saveAnswer(new Answer(hostCompany.getCode(), hostCompany.getCode(), "PRI_IMAGE_URL",
									imageUrl));
						}
					}
				} else {
					System.out.println(internship.getCode() + " has NO LNK_HOST_COMPANY");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Test
	public void fixLNK_InternSupervisorTest() {
		System.out.println("Intern Supervisor fix Fix test");
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

		SearchEntity searchBE = new SearchEntity("SBE_TEST", "internships")
				.addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
				.addFilter("LNK_INTERN_SUPERVISOR", SearchEntity.StringFilter.LIKE, "PER_%")
				.addColumn("PRI_CODE", "Name").addColumn("LNK_INTERN_SUPERVISOR", "Supervisor");

		searchBE.setRealm(realm);
		searchBE.setPageStart(0);
		searchBE.setPageSize(100000);

		List<BaseEntity> apps = beUtils.getBaseEntitys(searchBE);

		System.out.println("Number of Internships = " + apps.size());

		for (BaseEntity app : apps) {
			String per = app.getValueAsString("LNK_INTERN_SUPERVISOR");
			System.out.println("Supervisor = " + per);
			beUtils.saveAnswer(
					new Answer(userToken.getUserCode(), app.getCode(), "LNK_INTERN_SUPERVISOR", "[\"" + per + "\"]"));
		}

		System.out.println("Finished");
	}

	@Test
	public void journalChangeTest() {
		System.out.println("journalChange test");
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

		BaseEntity be = beUtils.getBaseEntityByCode("JNL_488F4EC2-8731-4E30-9198-1821EF0914EB20200914");

		if (be != null) {
			String name = "APPROVED";
			Answer answer = new Answer(userToken.getUserCode(), be.getCode(), "PRI_STATUS", name);
			answer.setChangeEvent(true);
			beUtils.saveAnswer(answer);

		}

	}

	@Test
	public void attributeChangeTest() {
		System.out.println("attributeChange test");
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

		BaseEntity be = beUtils.getBaseEntityByCode("PER_AFCACF0F-2618-4C5C-A292-2026A974D602");

		if (be != null) {
			String name = "Aaron Windy Chathanattu";
			Answer answer = new Answer(userToken.getUserCode(), be.getCode(), "PRI_NAME", name);
			answer.setChangeEvent(true);
			beUtils.saveAnswer(answer);

		}

	}

	@Test
	public void pushTest() {
		System.out.println("push test");
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

		BaseEntity be = beUtils.getBaseEntityByCode("CPY_ITA");

		if (be != null) {
			String name = "Institute of Technology 2 Australia";

			try {
				be.setName(name);
				be.setValue("PRI_NAME", name);
				QDataBaseEntityMessage msg = new QDataBaseEntityMessage(be);
				msg.setToken(userToken.getToken());
				msg.setReplace(true);
				String[] rxList = new String[1];
				rxList[0] = "SUPERVISOR";
				msg.setRecipientCodeArray(rxList);
				VertxUtils.writeMsg("project", JsonUtils.toJson(msg));

			} catch (BadDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		be = beUtils.getBaseEntityByCode("PER_AFCACF0F-2618-4C5C-A292-2026A974D602");

		if (be != null) {
			String name = "Aaron Rainy Chathanattu";

			try {
				be.setName(name);
				be.setValue("PRI_NAME", name);
				QDataBaseEntityMessage msg = new QDataBaseEntityMessage(be);
				msg.setToken(userToken.getToken());
				msg.setReplace(true);
				String[] rxList = new String[1];
				rxList[0] = "project";
				msg.setRecipientCodeArray(rxList);
				VertxUtils.writeMsg("project", JsonUtils.toJson(msg));

			} catch (BadDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	@Test
	public void fixInternshipAddressTest() {
		System.out.println("Internship Address Fix test");
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

		SearchEntity searchBE = new SearchEntity("SBE_TEST", "internships")
				.addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "BEG_%").addFilter("PRI_IS_INTERNSHIP", true)
				.addColumn("PRI_CODE", "Name").addColumn("LNK_HOST_COMPANY", "Host Company");

		searchBE.setRealm(realm);
		searchBE.setPageStart(0);
		searchBE.setPageSize(100000);

		List<BaseEntity> internships = beUtils.getBaseEntitys(searchBE);

		System.out.println("Number of Internships = " + internships.size());

		for (BaseEntity internship : internships) {

			BaseEntity is = beUtils.getBaseEntityByCode(internship.getCode());
			if (is.getValue("PRI_ADDRESS_STATE").isPresent()) {
				continue;
			}
			// fetch host company from internship
			try {
				String LNK_HOST_COMPANY = is.getValueAsString("LNK_HOST_COMPANY");
				if (LNK_HOST_COMPANY != null) {
					LNK_HOST_COMPANY = LNK_HOST_COMPANY.substring(2, LNK_HOST_COMPANY.length() - 2);
					System.out.println("Host Company :" + LNK_HOST_COMPANY);
					BaseEntity hostCompany = beUtils.getBaseEntityByCode(LNK_HOST_COMPANY);
					saveAddressItems(beUtils, is, userToken, hostCompany);

				} else {
					System.out.println(internship.getCode() + " has NO LNK_HOST_COMPANY");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println("Finished");
	}

	private void saveAddressItems(BaseEntityUtils beUtils, final BaseEntity target, GennyToken userToken,
			BaseEntity hostCompany) {
		saveAddressItem(beUtils, target, "PRI_ADDRESS_STATE", userToken, hostCompany);
		saveAddressItem(beUtils, target, "PRI_ADDRESS_ADDRESS1", userToken, hostCompany);
		saveAddressItem(beUtils, target, "PRI_ADDRESS_CITY", userToken, hostCompany);
		saveAddressItem(beUtils, target, "PRI_ADDRESS_COUNTRY", userToken, hostCompany);
		saveAddressItem2(beUtils, target, "PRI_ADDRESS_LATITUDE", userToken, hostCompany);
		saveAddressItem2(beUtils, target, "PRI_ADDRESS_LONGITUDE", userToken, hostCompany);
		saveAddressItem(beUtils, target, "PRI_ADDRESS_POSTCODE", userToken, hostCompany);
		saveAddressItem(beUtils, target, "PRI_ADDRESS_SUBURB", userToken, hostCompany);
	}

	private void saveAddressItem(BaseEntityUtils beUtils, final BaseEntity target, final String attributeCode,
			GennyToken userToken, BaseEntity hostCompany) {
		Optional<String> optTargetValue = target.getValue(attributeCode);
		if (optTargetValue.isPresent()) {
			return;
		}
		Optional<String> optValue = hostCompany.getValue(attributeCode);
		if (optValue.isPresent()) {

			beUtils.saveAnswer(new Answer(userToken.getUserCode(), target.getCode(), attributeCode, optValue.get()));
		}

	}

	private void saveAddressItem2(BaseEntityUtils beUtils, final BaseEntity target, final String attributeCode,
			GennyToken userToken, BaseEntity hostCompany) {
		Optional<Double> optTargetValue = target.getValue(attributeCode);
		if (optTargetValue.isPresent()) {
			return;
		}
		Optional<Double> optValue = hostCompany.getValue(attributeCode);
		if (optValue.isPresent()) {

			beUtils.saveAnswer(new Answer(userToken.getUserCode(), target.getCode(), attributeCode, optValue.get()));
		}

	}

	@Test
	public void fixJournalCountsTest() {
		System.out.println("Journal Counts test");
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

		SearchEntity searchBE = new SearchEntity("SBE_TEST", "interns")
				.addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "PER_%").addFilter("PRI_IS_INTERN", true)
				.addColumn("PRI_CODE", "Name");

		searchBE.setRealm(realm);
		searchBE.setPageStart(0);
		searchBE.setPageSize(100000);

		List<BaseEntity> interns = beUtils.getBaseEntitys(searchBE);

		for (BaseEntity intern : interns) {

			searchBE = new SearchEntity("SBE_TEST", "internjournals")
					.addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
					.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "JNL_%")
					.addFilter("LNK_INTERN", SearchEntity.StringFilter.LIKE, "%" + intern.getCode() + "%")
					.addColumn("PRI_CODE", "Name");

			Tuple2<String, List<String>> results = beUtils.getHql(searchBE); // hql += " order by " + sortCode + " " +
																				// sortValue;
			String hql = results._1;
			String hql2 = Base64.getUrlEncoder().encodeToString(hql.getBytes());
			try {
				String resultJsonStr = QwandaUtils.apiGet(
						GennySettings.qwandaServiceUrl + "/qwanda/baseentitys/count24/" + hql2, serviceToken.getToken(),
						120);

				Integer count = Integer.decode(resultJsonStr);
				System.out.println("Count = " + count);

				Answer journalCount = new Answer(beUtils.getGennyToken().getUserCode(), intern.getCode(),
						"PRI_NUM_JOURNALS", count);
				beUtils.saveAnswer(journalCount);

			} catch (Exception e1) {
				e1.printStackTrace();
			}

		}

		System.out.println("Finished");
	}

	@Test
	public void internImagesFix() {
		System.out.println("Intern Images test");
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

		// Get Keycloak User
		String accessToken = null;
		try {
			String keycloakUrl = "https://keycloak.gada.io";
			accessToken = KeycloakUtils.getAccessToken(keycloakUrl, "master", "admin-cli", null, "admin",
					System.getenv("KEYCLOAK_PASSWORD"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// fetch all keycloak users

		String keycloakUrl = "https://keycloak.gada.io";
		List<LinkedHashMap> results = new ArrayList<LinkedHashMap>();
		final HttpClient client = new DefaultHttpClient();

		try {
			final HttpGet get = new HttpGet(
					"https://keycloak.gada.io/auth/admin/realms/" + realm + "/users?first=0&max=20000");
			get.addHeader("Authorization", "Bearer " + accessToken);
			try {
				final HttpResponse response = client.execute(get);
				if (response.getStatusLine().getStatusCode() != 200) {
					throw new IOException();
				}
				final HttpEntity entity = response.getEntity();
				final InputStream is = entity.getContent();
				try {
					results = JsonSerialization.readValue(is, (new ArrayList<UserRepresentation>()).getClass());
				} finally {
					is.close();
				}
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		} finally {
			client.getConnectionManager().shutdown();
		}

		Map<String, String> keycloakEmailUuidMap = new HashMap<String, String>();

		System.out.println("Number of keycloak users = " + results.size());
		int count = 0;
		for (LinkedHashMap userMap : results) {
			String username = (String) userMap.get("username");
			String email = (String) userMap.get("email");
			String code = QwandaUtils.getNormalisedUsername("PER_" + username);
			String appCode = QwandaUtils.getNormalisedUsername("APP_" + username);
			String id = (String) userMap.get("id");
			keycloakEmailUuidMap.put(email, id);

		}

		SearchEntity searchBE = new SearchEntity("SBE_TEST", "interns")
				.addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "PER_%").addFilter("PRI_IS_INTERN", true)
				.addColumn("PRI_CODE", "Name");

		searchBE.setRealm(realm);
		searchBE.setPageStart(0);
		searchBE.setPageSize(100000);

		List<BaseEntity> interns = beUtils.getBaseEntitys(searchBE);

		Map<String, BaseEntity> internMap = new HashMap<String, BaseEntity>();
		Map<String, String> appcodeMap = new HashMap<String, String>();
		Map<String, BaseEntity> appMap = new HashMap<String, BaseEntity>();
		Map<String, BaseEntity> appInternMap = new HashMap<String, BaseEntity>();
		Map<String, String> appKeycloakMap = new HashMap<String, String>();

		for (BaseEntity intern : interns) {
			internMap.put(intern.getCode(), intern);
		}

		searchBE = new SearchEntity("SBE_TEST", "apps").addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "APP\\_%\\_AT\\_%")
				.addColumn("PRI_CODE", "Name");

		searchBE.setRealm(realm);
		searchBE.setPageStart(0);
		searchBE.setPageSize(100000);

		List<BaseEntity> apps = new ArrayList<BaseEntity>();

		Tuple2<String, List<String>> data = beUtils.getHql(searchBE);
		String hql = data._1;

		hql = Base64.getUrlEncoder().encodeToString(hql.getBytes());
		try {
			String resultJsonStr = QwandaUtils.apiGet(
					GennySettings.qwandaServiceUrl + "/qwanda/baseentitys/search24/" + hql + "/"
							+ searchBE.getPageStart(0) + "/" + searchBE.getPageSize(GennySettings.defaultPageSize),
					serviceToken.getToken(), 120);

			JsonObject resultJson = null;

			try {
				resultJson = new JsonObject(resultJsonStr);
				io.vertx.core.json.JsonArray result = resultJson.getJsonArray("codes");
				for (int i = 0; i < result.size(); i++) {
					BaseEntity be = null;
					String code = result.getString(i);
					if ("APP_L_DOT_TEKULA_AT_CQUMAIL_DOT_COM".equals(code)) {
						System.out.println("Detected APP_L_DOT_TEKULA_AT_CQUMAIL_DOT_COM");
					}
					BaseEntity app = beUtils.getBaseEntityByCode(code);
					appMap.put(code, app);
					BaseEntity intern = null;
					// Need to find the email of the person , find their be, then find their id
					if (code.contains("_AT_")) {
						String email = getEmailFromOldCode(code);
						intern = getPersonFromEmail(beUtils, email);
						if (intern == null) {
							intern = getPersonFromAppCode(beUtils, code);
						}
						System.out.println("Intern is " + intern);
						appInternMap.put(code, intern);
						String uuid = keycloakEmailUuidMap.get(email);
						if (uuid == null) {
							uuid = intern.getValue("PRI_UUID", null);
						}
						appKeycloakMap.put(code, uuid);
						String appCode = "APP_" + uuid.toUpperCase();
						appcodeMap.put(code, appCode);

					}
				}
			} catch (Exception e1) {
				log.error("Bad Json -> [" + resultJsonStr);
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}

		for (String appCode : appMap.keySet()) {

			if ("APP_L_DOT_TEKULA_AT_CQUMAIL_DOT_COM".equals(appCode)) {
				System.out.println("Detected APP_L_DOT_TEKULA_AT_CQUMAIL_DOT_COM");
			} else {
				// continue;
			}
			// Find their app

			BaseEntity app = appMap.get(appCode);
			BaseEntity intern = appInternMap.get(appCode);

			if (app.getCode().contains("_AT_")) {
				// This is old
				// System.out.println("APP CODE=" + appCode);
			} else {
				// System.out.println("APP CODE=" + appCode);
				continue;
			}

			// Now change the app code in db

			if (appCode.contains("_AT_")) {
				System.out.println("Changing " + appCode + " TO " + appcodeMap.get(appCode));

				try {
                    //
					String encodedsql = encodeValue("update baseentity_attribute set valueString='"
							+ appcodeMap.get(appCode) + "' where valueString='" + appCode + "'");
					String resultJson = QwandaUtils.apiGet(
							GennySettings.qwandaServiceUrl + "/service/executesql/" + encodedsql,
							serviceToken.getToken());

					encodedsql = encodeValue("update baseentity_attribute set baseEntityCode='"
							+ appcodeMap.get(appCode) + "' where baseEntityCode='" + appCode + "'");
					resultJson = QwandaUtils.apiGet(
							GennySettings.qwandaServiceUrl + "/service/executesql/" + encodedsql,
							serviceToken.getToken());

					//
					encodedsql = encodeValue("update baseentity set code='" + appcodeMap.get(appCode) + "' where code='"
							+ appCode + "'");
					resultJson = QwandaUtils.apiGet(
							GennySettings.qwandaServiceUrl + "/service/executesql/" + encodedsql,
							serviceToken.getToken());

// change persons
					String perCode = "PER_" + appCode.substring(4);
					String newPerCode = "PER_" + appcodeMap.get(appCode).substring(4);
					encodedsql = encodeValue("update baseentity_attribute set valueString='" + newPerCode
							+ "' where valueString='" + perCode + "'");
					resultJson = QwandaUtils.apiGet(
							GennySettings.qwandaServiceUrl + "/service/executesql/" + encodedsql,
							serviceToken.getToken());

					encodedsql = encodeValue(
							"update baseentity set code='" + newPerCode + "' where code='" + perCode + "'");
					resultJson = QwandaUtils.apiGet(
							GennySettings.qwandaServiceUrl + "/service/executesql/" + encodedsql,
							serviceToken.getToken());

				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

		searchBE = new SearchEntity("SBE_TEST", "people").addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "PER\\_%\\_AT\\_%")
				.addColumn("PRI_CODE", "Name");

		searchBE.setRealm(realm);
		searchBE.setPageStart(0);
		searchBE.setPageSize(100000);

		List<BaseEntity> people = beUtils.getBaseEntitys(searchBE);

		for (BaseEntity person : people) {

			if ("PER_JANE_DOT_OKEEFE_AT_RACI_DOT_ORG_DOT_AU".equals(person.getCode())) {
				System.out.println("identified");
			}

			String perCode = person.getCode();
			String email = getEmailFromOldCode(perCode);

			String uuid = keycloakEmailUuidMap.get(email);
			if (uuid == null) {
				person = beUtils.getBaseEntityByCode(perCode);
				uuid = person.getValue("PRI_UUID", null);
			}
			String newPerCode = null;
			if (uuid == null) {
				System.out.println("Bad uuid");
				// So create the keycloak user!
				try {
					uuid = KeycloakUtils.createDummyUser(serviceToken.getToken(), serviceToken.getRealm());
					String userPassword = "password1";
					String firstname = person.getValueAsString("PRI_FIRSTNAME");
					String lastname = person.getValueAsString("PRI_LASTNAME");
					newPerCode = "PER_" + uuid.toUpperCase();
					String userId = KeycloakUtils.updateUser(uuid, serviceToken.getToken(), serviceToken.getRealm(),
							email, firstname, lastname, email, userPassword, "user", "users");
					userId = userId.toUpperCase();
					Answer keycloakId = new Answer(beUtils.getGennyToken().getUserCode(), person.getCode(), "PRI_UUID",
							userId);
					beUtils.saveAnswer(keycloakId);
				} catch (IOException e) {
				}
			} else {
				newPerCode = "PER_" + uuid.toUpperCase();
			}

			try {
				String encodedsql = encodeValue("update baseentity_attribute set valueString='" + newPerCode
						+ "' where valueString='" + perCode + "'");
				String resultJson = QwandaUtils.apiGet(
						GennySettings.qwandaServiceUrl + "/service/executesql/" + encodedsql, serviceToken.getToken());

				encodedsql = encodeValue("update baseentity_attribute set valueString='[\"" + newPerCode
						+ "\"]' where valueString='[\"" + perCode + "\"]'");
				resultJson = QwandaUtils.apiGet(GennySettings.qwandaServiceUrl + "/service/executesql/" + encodedsql,
						serviceToken.getToken());

				encodedsql = encodeValue("update baseentity_attribute set baseentityCode='" + newPerCode
						+ "' where baseentityCode='" + perCode + "'");
				resultJson = QwandaUtils.apiGet(GennySettings.qwandaServiceUrl + "/service/executesql/" + encodedsql,
						serviceToken.getToken());

				//
				encodedsql = encodeValue(
						"update baseentity set code='" + newPerCode + "' where code='" + perCode + "'");
				resultJson = QwandaUtils.apiGet(GennySettings.qwandaServiceUrl + "/service/executesql/" + encodedsql,
						serviceToken.getToken());

			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		searchBE = new SearchEntity("SBE_TEST", "people").addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
				.addFilter("LNK_INTERN_SUPERVISOR", SearchEntity.StringFilter.LIKE, "%PER\\_%\\_AT\\_%")
				.addColumn("PRI_CODE", "Name").addColumn("LNK_INTERN_SUPERVISOR", "Supervisor");

		searchBE.setRealm(realm);
		searchBE.setPageStart(0);
		searchBE.setPageSize(100000);

		people = beUtils.getBaseEntitys(searchBE);

		for (BaseEntity person : people) {
			String supervisor = person.getValue("LNK_INTERN_SUPERVISOR", null);
			supervisor = supervisor.substring(2, supervisor.length() - 2);
			BaseEntity p = beUtils.getBaseEntityByCode(supervisor);
			if (p == null) {
				String email = getEmailFromOldCode(supervisor);
				person = getPersonFromEmail(beUtils, email);
				String uuid = null;
				if (person == null) {
					try {
						uuid = keycloakEmailUuidMap.get(email);
						if (uuid == null) {
							uuid = KeycloakUtils.createDummyUser(serviceToken.getToken(), serviceToken.getRealm());
						}
						String userPassword = "password1";
						if (email.startsWith("gerard")) {
							String firstname = "Gerard";
							String lastname = "Holland";
							uuid = KeycloakUtils.updateUser(uuid, serviceToken.getToken(), serviceToken.getRealm(),
									email, firstname, lastname, email, userPassword, "user", "users");
							String userId = uuid.toUpperCase();
							person = beUtils.create("PER_" + userId, "Gerard Holland");
							Answer keycloakId = new Answer(beUtils.getGennyToken().getUserCode(), person.getCode(),
									"PRI_UUID", userId);
							person = beUtils.saveAnswer(keycloakId);
							person = beUtils.saveAnswer(new Answer(beUtils.getGennyToken().getUserCode(),
									person.getCode(), "PRI_EMAIL", email));
							person = beUtils.saveAnswer(new Answer(beUtils.getGennyToken().getUserCode(),
									person.getCode(), "PRI_FIRSTNAME", firstname));
							person = beUtils.saveAnswer(new Answer(beUtils.getGennyToken().getUserCode(),
									person.getCode(), "PRI_LANENAME", lastname));
							person = beUtils.saveAnswer(new Answer(beUtils.getGennyToken().getUserCode(),
									person.getCode(), "PRI_IS_SUPERVISOR", true));
						}
						if (email.startsWith("spc")) {
							String firstname = "Stephenie";
							String lastname = "Pulis-Cassar";
							uuid = KeycloakUtils.updateUser(uuid, serviceToken.getToken(), serviceToken.getRealm(),
									email, firstname, lastname, email, userPassword, "user", "users");
							String userId = uuid.toUpperCase();
							person = beUtils.create("PER_" + userId, "aStephenie Pulis-Cassar");
							Answer keycloakId = new Answer(beUtils.getGennyToken().getUserCode(), person.getCode(),
									"PRI_UUID", userId);
							person = beUtils.saveAnswer(keycloakId);
							person = beUtils.saveAnswer(new Answer(beUtils.getGennyToken().getUserCode(),
									person.getCode(), "PRI_EMAIL", email));
							person = beUtils.saveAnswer(new Answer(beUtils.getGennyToken().getUserCode(),
									person.getCode(), "PRI_FIRSTNAME", firstname));
							person = beUtils.saveAnswer(new Answer(beUtils.getGennyToken().getUserCode(),
									person.getCode(), "PRI_LANENAME", lastname));
							person = beUtils.saveAnswer(new Answer(beUtils.getGennyToken().getUserCode(),
									person.getCode(), "PRI_IS_SUPERVISOR", true));
						}

					} catch (IOException e) {
					}
				}
				uuid = keycloakEmailUuidMap.get(email);
				if (uuid == null) {
					uuid = person.getValue("PRI_UUID", null);
				}

				// get the uuid
				if (uuid != null) {
					String newPerCode = "[\"PER_" + uuid.toUpperCase() + "\"]";

					try {
						String encodedsql = encodeValue("update baseentity_attribute set valueString='" + newPerCode
								+ "' where valueString='[\"" + supervisor + "\"]'");
						String resultJson = QwandaUtils.apiGet(
								GennySettings.qwandaServiceUrl + "/service/executesql/" + encodedsql,
								serviceToken.getToken());
					} catch (Exception e) {
					}
				}
			}

		}

		searchBE = new SearchEntity("SBE_TEST", "people").addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
				.addFilter("LNK_HOST_COMPANY_REP", SearchEntity.StringFilter.LIKE, "%PER\\_%\\_AT\\_%")
				.addColumn("PRI_CODE", "Name");

		searchBE.setRealm(realm);
		searchBE.setPageStart(0);
		searchBE.setPageSize(100000);

		people.addAll(beUtils.getBaseEntitys(searchBE));

		for (BaseEntity person : people) {

			if ("PER_SPC_PLUS_HC_AT_OUTCOME_DOT_LIFE".equals(person.getCode())) {
				System.out.println("identified");
			}

			String perCode = person.getCode();
			String email = getEmailFromOldCode(perCode);

			String uuid = keycloakEmailUuidMap.get(email);
			if (uuid == null) {
				person = beUtils.getBaseEntityByCode(perCode);
				uuid = person.getValue("PRI_UUID", null);
			}
			String newPerCode = null;
			if (uuid == null) {
				System.out.println("Bad uuid");
				// So create the keycloak user!
				try {
					uuid = KeycloakUtils.createDummyUser(serviceToken.getToken(), serviceToken.getRealm());
					String userPassword = "password1";
					String firstname = person.getValueAsString("PRI_FIRSTNAME");
					String lastname = person.getValueAsString("PRI_LASTNAME");
					newPerCode = "PER_" + uuid.toUpperCase();
					String userId = KeycloakUtils.updateUser(uuid, serviceToken.getToken(), serviceToken.getRealm(),
							email, firstname, lastname, email, userPassword, "user", "users");
					userId = userId.toUpperCase();
					Answer keycloakId = new Answer(beUtils.getGennyToken().getUserCode(), person.getCode(), "PRI_UUID",
							userId);
					beUtils.saveAnswer(keycloakId);
				} catch (IOException e) {
				}
			} else {
				newPerCode = "PER_" + uuid.toUpperCase();
			}

			try {
				String encodedsql = encodeValue("update baseentity_attribute set valueString='" + newPerCode
						+ "' where valueString='" + perCode + "'");
				String resultJson = QwandaUtils.apiGet(
						GennySettings.qwandaServiceUrl + "/service/executesql/" + encodedsql, serviceToken.getToken());

				encodedsql = encodeValue("update baseentity_attribute set valueString='[\"" + newPerCode
						+ "\"]' where valueString='[\"" + perCode + "\"]'");
				resultJson = QwandaUtils.apiGet(GennySettings.qwandaServiceUrl + "/service/executesql/" + encodedsql,
						serviceToken.getToken());

				encodedsql = encodeValue("update baseentity_attribute set baseentityCode='" + newPerCode
						+ "' where baseentityCode='" + perCode + "'");
				resultJson = QwandaUtils.apiGet(GennySettings.qwandaServiceUrl + "/service/executesql/" + encodedsql,
						serviceToken.getToken());

				//
				encodedsql = encodeValue(
						"update baseentity set code='" + newPerCode + "' where code='" + perCode + "'");
				resultJson = QwandaUtils.apiGet(GennySettings.qwandaServiceUrl + "/service/executesql/" + encodedsql,
						serviceToken.getToken());

			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		searchBE = new SearchEntity("SBE_TEST", "people").addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
				.addFilter("PRI_INTERN_NAME", SearchEntity.StringFilter.LIKE, "Answer%").addColumn("PRI_CODE", "Code")
				.addColumn("PRI_EMAIL", "email").addColumn("PRI_NAME", "name")
				.addColumn("PRI_INTERN_NAME", "internname");

		searchBE.setRealm(realm);
		searchBE.setPageStart(0);
		searchBE.setPageSize(100000);

		people = beUtils.getBaseEntitys(searchBE);

		for (BaseEntity person : people) {
			String name = person.getValue("PRI_NAME", "Unknown");
			beUtils.saveAnswer(
					new Answer(beUtils.getGennyToken().getUserCode(), person.getCode(), "PRI_INTERN_NAME", name));
		}

		searchBE = new SearchEntity("SBE_TEST", "people").addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "DEV%").addColumn("PRI_CODE", "Code")
				.addColumn("LNK_USER", "user");

		searchBE.setRealm(realm);
		searchBE.setPageStart(0);
		searchBE.setPageSize(100000);

		List<BaseEntity> devices = beUtils.getBaseEntitys(searchBE);

		for (BaseEntity device : devices) {
			String userCode = device.getValue("LNK_USER", null);
			if (userCode != null) {
				if (userCode.contains("_AT_")) {
					String email = getEmailFromOldCode(userCode);
					BaseEntity person = getPersonFromEmail(beUtils, email);
					if (person == null) {
						String uuid = keycloakEmailUuidMap.get(email);
						if (uuid != null) {
							String code = "PER_" + uuid.toUpperCase();
							person = beUtils.getBaseEntityByCode(code);
						}
					}
					if (person != null) {
						beUtils.saveAnswer(new Answer(beUtils.getGennyToken().getUserCode(), device.getCode(),
								"LNK_USER", person.getCode()));
					}
				}
			}
		}

		System.out.println("Finished");
	}

	private String getEmailFromOldCode(String oldCode) {
		String ret = null;
		if (oldCode.contains("_AT_")) {

			if ("PER_JIUNWEI_DOT_LU_AT_CQUMAIL_DOT_COM".equals(oldCode)) {
				oldCode = "PER_JIUN_DASH_WEI_DOT_LU_AT_CQUMAIL_DOT_COM"; // addd dash
			}

			oldCode = oldCode.substring(4);
			// convert to email
			oldCode = oldCode.replaceAll("_PLUS_", "+");
			oldCode = oldCode.replaceAll("_DOT_", ".");
			oldCode = oldCode.replaceAll("_AT_", "@");
			oldCode = oldCode.replaceAll("_DASH_", "-");
			ret = oldCode.toLowerCase();
		}
		return ret;
	}

	private BaseEntity getPersonFromEmail(BaseEntityUtils beUtils, String email) {
		BaseEntity person = null;

		SearchEntity searchBE = new SearchEntity("SBE_TEST", "email")
				.addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "PER_%")
				.addFilter("PRI_EMAIL", SearchEntity.StringFilter.LIKE, email).addColumn("PRI_CODE", "Name")
				.addColumn("PRI_EMAIL", "Email");

		searchBE.setRealm(realm);
		searchBE.setPageStart(0);
		searchBE.setPageSize(100000);
		Tuple2<String, List<String>> emailhqlTuple = beUtils.getHql(searchBE);
		String emailhql = emailhqlTuple._1;

		emailhql = Base64.getUrlEncoder().encodeToString(emailhql.getBytes());
		try {
			String resultJsonStr = QwandaUtils.apiGet(
					GennySettings.qwandaServiceUrl + "/qwanda/baseentitys/search24/" + emailhql + "/"
							+ searchBE.getPageStart(0) + "/" + searchBE.getPageSize(GennySettings.defaultPageSize),
					serviceToken.getToken(), 120);

			JsonObject resultJson = null;
			resultJson = new JsonObject(resultJsonStr);
			io.vertx.core.json.JsonArray result2 = resultJson.getJsonArray("codes");
			String internCode = result2.getString(0);
			if (internCode.contains("_AT_")) {
				person = beUtils.getBaseEntityByCode(internCode);
			}

		} catch (Exception e) {

		}
		return person;
	}

	private BaseEntity getPersonFromAppCode(BaseEntityUtils beUtils, String appCode) {
		BaseEntity person = null;

		if (appCode.contains("_AT_")) {
			String perCode = "PER_" + appCode.substring(4);
			SearchEntity searchBE = new SearchEntity("SBE_TEST", "email")
					.addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
					.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, perCode).addColumn("PRI_CODE", "Name");

			searchBE.setRealm(realm);
			searchBE.setPageStart(0);
			searchBE.setPageSize(100000);
			Tuple2<String, List<String>> emailhqlTuple = beUtils.getHql(searchBE);
			String emailhql = emailhqlTuple._1;

			emailhql = Base64.getUrlEncoder().encodeToString(emailhql.getBytes());
			try {
				String resultJsonStr = QwandaUtils.apiGet(
						GennySettings.qwandaServiceUrl + "/qwanda/baseentitys/search24/" + emailhql + "/"
								+ searchBE.getPageStart(0) + "/" + searchBE.getPageSize(GennySettings.defaultPageSize),
						serviceToken.getToken(), 120);

				JsonObject resultJson = null;
				resultJson = new JsonObject(resultJsonStr);
				io.vertx.core.json.JsonArray result2 = resultJson.getJsonArray("codes");
				String internCode = result2.getString(0);
				if (internCode.contains("_AT_")) {
					person = beUtils.getBaseEntityByCode(internCode);
				}

			} catch (Exception e) {

			}
		}
		if (person == null) {
			person = beUtils.getBaseEntityByCode("PER_" + appCode.substring(4));
		}
		return person;
	}

	// @Test
	public void myInterviewTest() {
		String code = "P 2e69c4fd-cfa8-42be-a654-0e0891de157a";

		System.out.println("Search cache test");
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

		BaseEntity project = beUtils.getBaseEntityByCode("PRJ_" + userToken.getRealm().toUpperCase());
		String apiKey = project.getValueAsString("ENV_API_KEY_MY_INTERVIEW");
		String secretToken = project.getValueAsString("ENV_SECRET_MY_INTERVIEW");
		long unixTimestamp = Instant.now().getEpochSecond();
		String apiSecret = apiKey + secretToken + unixTimestamp;
		String hashed = BCrypt.hashpw(apiSecret, BCrypt.gensalt(10));
		String url = "https://api.myinterview.com/2.21.2/getVideo?apiKey=" + apiKey + "&hashTimestamp=" + unixTimestamp
				+ "&hash=" + hashed + "&video=2e69c4fd-cfa8-42be-a654-0e0891de157a";
		String url2 = "https://embed.myinterview.com/player.v3.html?apiKey=" + apiKey + "&hashTimestamp="
				+ unixTimestamp + "&hash=" + hashed + "&video=2e69c4fd-cfa8-42be-a654-0e0891de157a&autoplay=1&fs=0";
		System.out.println(url);
		System.out.println(url2);

	}

	@Test
	public void keycloakUserFix() {
		System.out.println("Search cache test");
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
		String accessToken = null;
		try {
			String keycloakUrl = "https://keycloak.gada.io";
			accessToken = KeycloakUtils.getAccessToken(keycloakUrl, "master", "admin-cli", null, "admin",
					System.getenv("KEYCLOAK_PASSWORD"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// fetch all keycloak users

		String keycloakUrl = "https://keycloak.gada.io";
		List<LinkedHashMap> results = new ArrayList<LinkedHashMap>();
		final HttpClient client = new DefaultHttpClient();

		try {
			final HttpGet get = new HttpGet(
					"https://keycloak.gada.io/auth/admin/realms/" + realm + "/users?first=0&max=20000");
			get.addHeader("Authorization", "Bearer " + accessToken);
			try {
				final HttpResponse response = client.execute(get);
				if (response.getStatusLine().getStatusCode() != 200) {
					throw new IOException();
				}
				final HttpEntity entity = response.getEntity();
				final InputStream is = entity.getContent();
				try {
					results = JsonSerialization.readValue(is, (new ArrayList<UserRepresentation>()).getClass());
				} finally {
					is.close();
				}
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		} finally {
			client.getConnectionManager().shutdown();
		}

		System.out.println("Number of keycloak users = " + results.size());

		SearchEntity searchBE = new SearchEntity("SBE_TEST", "Users")
				.addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "PER_%").addColumn("PRI_CODE", "Name");

		searchBE.setRealm(realm);
		searchBE.setPageStart(0);
		searchBE.setPageSize(100000);

		String jsonSearchBE = JsonUtils.toJson(searchBE);
		/* System.out.println(jsonSearchBE); */
		String resultJson;
		BaseEntity result = null;
		QDataBaseEntityMessage resultMsg = null;
		try {
			resultJson = QwandaUtils.apiPostEntity(GennySettings.qwandaServiceUrl + "/qwanda/baseentitys/search",
					jsonSearchBE, serviceToken.getToken());
			resultMsg = JsonUtils.fromJson(resultJson, QDataBaseEntityMessage.class);
			/*
			 * System.out.println(drools.getRule().getName()
			 * +" Got to here in RETURN JOURNALS "+resultJson);
			 */
			BaseEntity[] bes = resultMsg.getItems();
			System.out.println("Returned " + bes.length + " items");
			System.out.println("The count return " + resultMsg.getTotal());
			/* Now only send the ones that are not synced */

		} catch (Exception e1) {
			e1.printStackTrace();
		}

		System.out.println("Number of PERSON = " + resultMsg.getItems().length);
		Map<String, BaseEntity> persons = new HashMap<String, BaseEntity>();
		for (BaseEntity be : resultMsg.getItems()) {
			persons.put(be.getCode(), be);
		}

//		String username = "kanika.gulati+intern1@gada.io";
//		String email = "kanika.gulati+intern1@gada.io";
//		String code = QwandaUtils.getNormalisedUsername("PER_"+username);
//		BaseEntity user = persons.get(code);
//		String id = "3fdf680c-c2b9-4edb-93be-142fe85be7d4";
//		String newCode = "PER_"+id.toUpperCase();

//	    // So for every keycloak user
//	    // (1) fetch their baseentity
		int count = 0;
		for (LinkedHashMap userMap : results) {
			// userMap.get("username");
			// usernap.get("email");
			String username = (String) userMap.get("username");
			String email = (String) userMap.get("email");
			String code = QwandaUtils.getNormalisedUsername("PER_" + username);
			String appCode = QwandaUtils.getNormalisedUsername("APP_" + username);
			BaseEntity user = persons.get(code);
			if (user != null) {
				String id = (String) userMap.get("id");
				String newCode = "PER_" + id.toUpperCase();
				System.out.println("Fix " + user.getCode() + " to " + newCode);
				String newAppCode = "APP_" + id.toUpperCase();

				// fix
				// (1) Change all baseEntityCode in baseeentity_attribute
				try {
					String encodedsql = encodeValue("update baseentity_attribute set baseentityCode='" + newCode
							+ "' where baseentityCode='" + user.getCode() + "'");
					resultJson = QwandaUtils.apiGet(
							GennySettings.qwandaServiceUrl + "/service/executesql/" + encodedsql,
							serviceToken.getToken());
					encodedsql = encodeValue("update baseentity_attribute set valueString='[\"" + newCode
							+ "]\"' where valueString='\"[" + user.getCode() + "\"]'");
					resultJson = QwandaUtils.apiGet(
							GennySettings.qwandaServiceUrl + "/service/executesql/" + encodedsql,
							serviceToken.getToken());
					encodedsql = encodeValue("update baseentity_attribute set valueString='" + newCode
							+ "' where valueString='" + user.getCode() + "'");
					resultJson = QwandaUtils.apiGet(
							GennySettings.qwandaServiceUrl + "/service/executesql/" + encodedsql,
							serviceToken.getToken());

					encodedsql = encodeValue(
							"update baseentity set code='" + newCode + "' where code='" + user.getCode() + "'");
					resultJson = QwandaUtils.apiGet(
							GennySettings.qwandaServiceUrl + "/service/executesql/" + encodedsql,
							serviceToken.getToken());

					encodedsql = encodeValue(
							"update baseentity set code='" + newAppCode + "' where code='" + appCode + "'");
					resultJson = QwandaUtils.apiGet(
							GennySettings.qwandaServiceUrl + "/service/executesql/" + encodedsql,
							serviceToken.getToken());

				} catch (Exception e) {

				}

			}
			count++;
			// if (count > 4) {
			// break;
			// }
		}

	}

	// @Test
	public void searchCountTest() {
		System.out.println("Search count test");
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

		String searchCode = "SBE_INTERNS";
		SearchEntity searchBE = VertxUtils.getObject(realm, "", searchCode, SearchEntity.class,
				serviceToken.getToken());

		long starttime = System.currentTimeMillis();
		long endtime = 0;

		Tuple2<String, List<String>> results = beUtils.getHql(searchBE); // hql += " order by " + sortCode + " " +
																			// sortValue;
		String hql = results._1;
		String hql2 = Base64.getUrlEncoder().encodeToString(hql.getBytes());
		try {
			String resultJsonStr = QwandaUtils.apiGet(
					GennySettings.qwandaServiceUrl + "/qwanda/baseentitys/count24/" + hql2, serviceToken.getToken(),
					120);

			System.out.println("Count = " + resultJsonStr);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		endtime = System.currentTimeMillis();
		System.out.println("Total time taken = " + (endtime - starttime) + " ms");
	}

	@Test
	public void searchCacheTest() {
		System.out.println("Search cache test");
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

		TableUtils tableUtils = new TableUtils(beUtils);

		QDataBaseEntityMessage msg = null;
		// String searchCode = "SBE_EDU_PROVIDERS_ACTIVE";
		String searchCode = "SBE_INTERNS";
		SearchEntity searchBE = VertxUtils.getObject(realm, "", searchCode, SearchEntity.class,
				serviceToken.getToken());

		long starttime = System.currentTimeMillis();
		long endtime = 0;

		String beFilter1 = null;
		String beFilter2 = null;
		String attributeFilterValue1 = "";
		String attributeFilterCode1 = null;
		String attributeFilterValue2 = "";
		String attributeFilterCode2 = null;
		String sortCode = null;
		String sortValue = null;
		String sortType = null;

		Integer pageStart = searchBE.getValue("SCH_PAGE_START", 0);
		Integer pageSize = searchBE.getValue("SCH_PAGE_SIZE", GennySettings.defaultPageSize);

		List<String> attributeFilter = new ArrayList<String>();
		Tuple2<String, List<String>> results = beUtils.getHql(searchBE); // hql += " order by " + sortCode + " " +
																			// sortValue;
//hql = "select distinct ea.baseEntityCode from EntityAttribute ea , EntityAttribute eb , EntityAttribute ec  where ea.baseEntityCode=eb.baseEntityCode  and (ea.baseEntityCode like 'CPY_%'   or ea.baseEntityCode like 'null')   and eb.attributeCode = 'PRI_STATUS' and  eb.valueString = 'ACTIVE' and ea.baseEntityCode=ec.baseEntityCode  and ec.attributeCode = 'PRI_IS_EDU_PROVIDER' and  ec.valueBoolean = true order by PRI_NAME ASC";
//hql =  select distinct ea.baseEntityCode from EntityAttribute ea , EntityAttribute eb , EntityAttribute ec  where ea.baseEntityCode=eb.baseEntityCode  and (ea.baseEntityCode like 'CPY_%'   or ea.baseEntityCode like 'null')   and eb.attributeCode = 'PRI_STATUS' and  eb.valueString = 'ACTIVE' and ea.baseEntityCode=ec.baseEntityCode  and ec.attributeCode = 'PRI_IS_EDU_PROVIDER' and  ec.valueBoolean = true
		String hql = results._1;
//hql = "select distinct ea.baseEntityCode from EntityAttribute ea , EntityAttribute eb , EntityAttribute ed  where ea.baseEntityCode=eb.baseEntityCode  and (ea.baseEntityCode like 'CPY_%'  )   and eb.attributeCode = 'PRI_IS_EDU_PROVIDER' and  eb.valueBoolean = true and ea.baseEntityCode=ed.baseEntityCode and ed.attributeCode='PRI_NAME'  order by ed.valueString ASC";
//hql = "select distinct ea.baseEntityCode from EntityAttribute ea , EntityAttribute eb ,  EntityAttribute ed  where ea.baseEntityCode=eb.baseEntityCode  and (ea.baseEntityCode like 'PER_%'  )   and eb.attributeCode = 'PRI_IS_INTERN' and  eb.valueBoolean = true   and ea.baseEntityCode=ed.baseEntityCode and ed.attributeCode='PRI_NAME'  order by ed.valueString ASC";
//hql = "select distinct ea.baseEntityCode from life.genny.qwanda.attribute.EntityAttribute ea , life.genny.qwanda.attribute.EntityAttribute eb , life.genny.qwanda.attribute.EntityAttribute ec , life.genny.qwanda.attribute.EntityAttribute ed  where  ea.baseEntityCode=eb.baseEntityCode  and (ea.baseEntityCode like 'CPY_%'  )   and eb.attributeCode = 'PRI_STATUS' and  eb.valueString = 'ACTIVE' and ea.baseEntityCode=ec.baseEntityCode  and ec.attributeCode = 'PRI_IS_EDU_PROVIDER' and  ec.valueBoolean = true and ea.baseEntityCode=ed.baseEntityCode and ed.attributeCode='PRI_NAME'  order by ed.valueString ASC";
		String hql2 = Base64.getUrlEncoder().encodeToString(hql.getBytes());
		JsonObject resultJson;
		try {
			String resultJsonStr = QwandaUtils.apiGet(GennySettings.qwandaServiceUrl + "/qwanda/baseentitys/search24/"
					+ hql2 + "/" + pageStart + "/" + pageSize, serviceToken.getToken(), 120);

			resultJson = new JsonObject(resultJsonStr);
			Long total = resultJson.getLong("total");
			// check the cac
//			JsonArray result = resultJson.getJsonArray("codes");
//			List<String> resultCodes = new ArrayList<String>();
//			for (int i = 0; i < result.size(); i++) {
//				String code = result.getString(i);
//				resultCodes.add(code);
//			}
// 			String[] filterArray = attributeFilter.toArray(new String[0]);
//			List<BaseEntity> beList = resultCodes.stream().map(e -> {
//				BaseEntity be = beUtils.getBaseEntityByCode(e);
//				be = VertxUtils.privacyFilter(be, filterArray);
//				return be;
//			}).collect(Collectors.toList());
//			msg = new QDataBaseEntityMessage(beList.toArray(new BaseEntity[0]));
//			msg.setTotal(total);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		endtime = System.currentTimeMillis();
		System.out.println("Total time taken = " + (endtime - starttime) + " ms");
	}

	// @Test
	public void slackTest() {
		System.out.println("Journal Slack test");
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

		QEventMessage message = new QEventMessage("JOURNAL_ADD", "Journal Event");
		message.getData().setParentCode("PER_DOMINIKAASYDNEY_AT_GMAIL_DOT_COM");
		message.getData().setTargetCode(
				"JNL_751E30C4-BD8B-4817-8E91-6CB662B74CA020200527,JNL_751E30C4-BD8B-4817-8E91-6CB662B74CA020200526");

		String userWhoUpdatedJournals = message.getData().getParentCode();
		String listOfChangedJournals = message.getData().getTargetCode();
		String webhookURL = null;
		String studentName = "Unknown";
		String hostCompany = "Unknown";
		String educationProvider = "Unknown";
		LocalDateTime updateTime = null;

		List<String> changedJournals = new ArrayList<String>(); /*
																 * Stream.of(listOfChangedJournals.split(",",
																 * -1)).collect(Collectors.toList());
																 */
		String[] journalArray = listOfChangedJournals.split(",", -1);
		for (String journalCode : journalArray) {
			changedJournals.add(journalCode);
		}

		List<BaseEntity> journals = new ArrayList<BaseEntity>();
		for (String journalCode : changedJournals) {
			BaseEntity journal = beUtils.getBaseEntityByCode(journalCode);
			journals.add(journal);

		}

		/* studentName = user.getValue("PRI_LASTNAME",true); */
		/* updateTime = journal.getValue("PRI_INTERN_LAST_UPDATE",true); */

		Map<String, BaseEntity> internMap = new HashMap<String, BaseEntity>();
		Map<String, BaseEntity> supervisorMap = new HashMap<String, BaseEntity>();

		for (BaseEntity journal : journals) {

			String journalName = journal.getName();
			String status = journal.getValue("PRI_STATUS", "NO STATUS");

			Optional<String> optHostCompanySupervisorCode = journal.getValue("LNK_INTERN_SUPERVISOR");
			if (optHostCompanySupervisorCode.isPresent()) {
				String supervisorCode = optHostCompanySupervisorCode.get();
				supervisorCode = supervisorCode.substring(2);
				supervisorCode = supervisorCode.substring(0, (supervisorCode.length() - 2));
				BaseEntity supervisor = beUtils.getBaseEntityByCode(supervisorCode);
			}

			Optional<String> optInternCode = journal.getValue("LNK_INTERN");
			if (optInternCode.isPresent()) {
				String internCode = optInternCode.get();
				internCode = internCode.substring(2);
				internCode = internCode.substring(0, (internCode.length() - 2));
				BaseEntity intern = beUtils.getBaseEntityByCode(internCode);
				studentName = intern.getName();
				Optional<String> optEduCode = intern.getValue("LNK_EDU_PROVIDER");
				if (optEduCode.isPresent()) {
					String eduCode = optEduCode.get();
					eduCode = eduCode.substring(2);
					eduCode = eduCode.substring(0, (eduCode.length() - 2));
					BaseEntity edu = beUtils.getBaseEntityByCode(eduCode);
					educationProvider = edu.getName();
				}
				hostCompany = intern.getValue("PRI_ASSOC_HOST_COMPANY", "NOT SET");
			}

			BaseEntity agent = beUtils.getBaseEntityByCode("CPY_OUTCOME_LIFE");
			webhookURL = agent.getValueAsString("PRI_SLACK");

			/* Sending Slack Notification */

			updateTime = LocalDateTime.now();

			JsonObject msgpayload = new JsonObject("{\n" + "   \"blocks\":[\n" + "      {\n"
					+ "         \"type\":\"section\",\n" + "         \"text\":{\n"
					+ "            \"type\":\"mrkdwn\",\n" + "            \"text\":\"New Journal (" + status + ") -> "
					+ journalName + " :memo:\"\n" + "         }\n" + "      },\n" + "      {\n"
					+ "         \"type\":\"divider\"\n" + "      },\n" + "      {\n"
					+ "         \"type\":\"section\",\n" + "         \"fields\":[\n" + "            {\n"
					+ "               \"type\":\"mrkdwn\",\n" + "               \"text\":\"*Student:*\\n" + studentName
					+ "\"\n" + "            },\n" + "            {\n" + "               \"type\":\"mrkdwn\",\n"
					+ "               \"text\":\"*Time:*\\n" + updateTime + "\"\n" + "            },\n"
					+ "            {\n" + "               \"type\":\"mrkdwn\",\n"
					+ "               \"text\":\"*Host Company:*\\n" + hostCompany + "\"\n" + "            },\n"
					+ "            {\n" + "               \"type\":\"mrkdwn\",\n"
					+ "               \"text\":\"*Education Provider:*\\n" + educationProvider + "\"\n"
					+ "            }\n" + "         ]\n" + "      },\n" + "      {\n"
					+ "         \"type\":\"divider\"\n" + "      },\n" + "      {\n"
					+ "         \"type\":\"context\",\n" + "         \"elements\":[\n" + "            {\n"
					+ "               \"type\":\"mrkdwn\",\n"
					+ "               \"text\":\"*Last updated:* 9:15 AM May 22, 2020\"\n" + "            }\n"
					+ "         ]\n" + "      }\n" + "   ]\n" + "}");

			System.out.println("Payload is" + msgpayload.toString());

			try {
				QwandaUtils.apiPostEntity(webhookURL, msgpayload.toString(), serviceToken.getToken());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	// @Test
	public void journalTest() {
		System.out.println("Journal test");
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

		String roleAttribute = "LNK_INTERN_SUPERVISOR";
		String userCode = "PER_DAMIEN_AT_DESIGNCONSULTING_DOT_COM_DOT_AU";

		BaseEntity deviceBe = beUtils.getBaseEntityByCode("DEV_" + "565D971D-5F9F-4161-88E0-E34765B57F75");
		LocalDateTime veryearly = LocalDateTime.of(1970, 01, 01, 0, 0, 0);
		LocalDateTime lastUpdated = null;
		if (deviceBe != null) {
			lastUpdated = deviceBe.getValue("PRI_LAST_UPDATED", veryearly);
		} else {
			lastUpdated = veryearly;
		}

		long starttime = System.currentTimeMillis();
		long looptime = 0;
		long searchtime = 0;

		final DateTimeFormatter formatterMysql = DateTimeFormatter.ISO_DATE_TIME;
		String dtStr = formatterMysql.format(lastUpdated).replace("T", " ");
		String hql = "select ea from EntityAttribute ea, EntityAttribute eb where ea.baseEntityCode=eb.baseEntityCode ";
		hql += " and eb.attributeCode = '" + roleAttribute + "' and eb.valueString = '[\"" + userCode + "\"]'";
		hql += " and ea.baseEntityCode like 'JNL_%'  ";
		hql += " and ((ea.updated >= '" + dtStr + "') or (ea.updated is null and ea.created >= '" + dtStr + "'))";
		hql = Base64.getUrlEncoder().encodeToString(hql.getBytes());
		String resultJson;
		QDataBaseEntityMessage resultMsg = null;
		try {
			resultJson = QwandaUtils.apiGet(GennySettings.qwandaServiceUrl + "/qwanda/baseentitys/search22/" + hql,
					serviceToken.getToken(), 120);
			searchtime = System.currentTimeMillis();
			resultMsg = JsonUtils.fromJson(resultJson, QDataBaseEntityMessage.class);

		} catch (Exception e1) {
			e1.printStackTrace();
		}

		long endtime = System.currentTimeMillis();
		System.out.println("search took " + (searchtime - starttime) + " ms " + resultMsg);
		System.out.println("loop took " + (looptime - searchtime) + " ms");
		System.out.println("finish took " + (endtime - looptime) + " ms");
		System.out.println("total took " + (endtime - starttime) + " ms");
	}

	// @Test
	public void testTableTest() {
		System.out.println("Test Table test");
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

		QEventBtnClickMessage message = new QEventBtnClickMessage("QUE_TREE_ITEM_INTERNS_GRP");

		long starttime = System.currentTimeMillis();
		long looptime = 0;
		long searchtime = 0;
		Boolean cache = false;
		String code = message.getData().getCode();
		System.out.println("QUESTION CODE   ::   " + code);

		code = StringUtils.removeStart(code, "QUE_TREE_ITEM_");
		System.out.println("removing prefix CODE   ::   " + code);

		code = StringUtils.removeEnd(code, "_GRP");

		System.out.println("FINAL CODE   ::   " + code);

		String searchBeCode = "SBE_" + code;
		System.out.println("SBE CODE   ::   " + searchBeCode);

		String frameCode = "FRM_TABLE_" + code;
		System.out.println("FRAME CODE   ::   " + frameCode);

		long s1time = System.currentTimeMillis();
		/* get current search */
		TableUtils tableUtils = new TableUtils(beUtils);
		SearchEntity searchBE = tableUtils.getSessionSearch(searchBeCode);

		long s2time = System.currentTimeMillis();
		Answer pageAnswer = new Answer(beUtils.getGennyToken().getUserCode(), searchBE.getCode(), "SCH_PAGE_START",
				"0");
		Answer pageNumberAnswer = new Answer(beUtils.getGennyToken().getUserCode(), searchBE.getCode(), "PRI_INDEX",
				"1");

		searchBE = beUtils.updateBaseEntity(searchBE, pageAnswer, SearchEntity.class);
		searchBE = beUtils.updateBaseEntity(searchBE, pageNumberAnswer, SearchEntity.class);

		VertxUtils.putObject(beUtils.getGennyToken().getRealm(), "", searchBE.getCode(), searchBE,
				beUtils.getGennyToken().getToken());

		long s3time = System.currentTimeMillis();

		ExecutorService WORKER_THREAD_POOL = Executors.newFixedThreadPool(10);
		CompletionService<QBulkMessage> service = new ExecutorCompletionService<>(WORKER_THREAD_POOL);

		TableFrameCallable tfc = new TableFrameCallable(beUtils, cache);
		SearchCallable sc = new SearchCallable(tableUtils, searchBE, beUtils, cache);

		List<Callable<QBulkMessage>> callables = Arrays.asList(tfc, sc);

		QBulkMessage aggregatedMessages = new QBulkMessage();

		long startProcessingTime = System.currentTimeMillis();
		long totalProcessingTime;

		if (cache) {
			for (Callable<QBulkMessage> callable : callables) {
				service.submit(callable);
			}
			try {
				Future<QBulkMessage> future = service.take();
				QBulkMessage firstThreadResponse = future.get();
				aggregatedMessages.add(firstThreadResponse);
				totalProcessingTime = System.currentTimeMillis() - startProcessingTime;

//			assertTrue("First response should be from the fast thread", 
//			  "fast thread".equals(firstThreadResponse.getData_type()));
//			assertTrue(totalProcessingTime >= 100
//			  && totalProcessingTime < 1000);
				log.info("Thread finished after: " + totalProcessingTime + " milliseconds");

				future = service.take();
				QBulkMessage secondThreadResponse = future.get();
				aggregatedMessages.add(secondThreadResponse);
				log.info("2nd Thread finished after: " + totalProcessingTime + " milliseconds");
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			totalProcessingTime = System.currentTimeMillis() - startProcessingTime;

			WORKER_THREAD_POOL.shutdown();
			try {
				if (!WORKER_THREAD_POOL.awaitTermination(90, TimeUnit.SECONDS)) {
					WORKER_THREAD_POOL.shutdownNow();
				}
			} catch (InterruptedException ex) {
				WORKER_THREAD_POOL.shutdownNow();
				Thread.currentThread().interrupt();
			}
		} else {

			tfc.call();
			sc.call();

		}
		totalProcessingTime = System.currentTimeMillis() - startProcessingTime;
		log.info("All threads finished after: " + totalProcessingTime + " milliseconds");
		aggregatedMessages.setToken(userToken.getToken());
		QDataAskMessage[] asks = aggregatedMessages.getAsks();
		aggregatedMessages.setAsks(null);

		if (cache) {
			String json = JsonUtils.toJson(aggregatedMessages);
			VertxUtils.writeMsg("webcmds", json);
			for (QDataAskMessage askMsg : asks) {
				askMsg.setToken(userToken.getToken());
				VertxUtils.writeMsg("webcmds", JsonUtils.toJson(askMsg));
			}
		}
		/* show tab-view first */
		// ShowFrame.display(beUtils.getGennyToken(), "FRM_QUE_TAB_VIEW", "FRM_CONTENT",
		// "Test");

		/* show table-view inside tab-content */
		// ShowFrame.display(beUtils.getGennyToken(), "FRM_TABLE_VIEW",
		// "FRM_TAB_CONTENT", "Test");
		// long s4time = System.currentTimeMillis();
		// tableUtils.performSearch(userToken, serviceToken, searchBeCode, null);
		// long s5time = System.currentTimeMillis();
		/* Send to front end */
		/* output.setTypeOfResult("NONE"); */
		/* output.setResultCode("NONE"); */ /* dont display anything new */

		retract(message);
		/* update(output); */
		long endtime = System.currentTimeMillis();
		System.out.println("init setup took " + (s1time - starttime) + " ms");
		System.out.println("search session setup took " + (s2time - s1time) + " ms");
		System.out.println("update searchBE BE setup took " + (s3time - s2time) + " ms");
		// System.out.println("send frame table and tab display setup took " + (s4time -
		// s3time) + " ms");
		// System.out.println("searching took " + (s5time - s4time) + " ms");
		// System.out.println("finish took " + (endtime - s5time) + " ms");
		System.out.println("total took " + (endtime - starttime) + " ms");
	}

//	/* show tab-view first */
//		 ShowFrame.display(beUtils.getGennyToken(), "FRM_QUE_TAB_VIEW", "FRM_CONTENT", "Test");
//
//	/* show table-view inside tab-content */
//	ShowFrame.display(beUtils.getGennyToken(), "FRM_TABLE_VIEW", "FRM_TAB_CONTENT", "Test");
//long s4time = System.currentTimeMillis();
//	tableUtils.performSearch(userToken, serviceToken,searchBeCode, null);
//long s5time = System.currentTimeMillis();
//	/* Send to front end */
//	/*output.setTypeOfResult("NONE");*/
//	/*output.setResultCode("NONE");*/  /* dont display anything new */
//
//	retract($message);
//	/* update(output);*/
//long endtime = System.currentTimeMillis();
//System.out.println("init setup took "+(s1time-starttime)+" ms");
//System.out.println("search session setup took "+(s2time-s1time)+" ms");
//System.out.println("update searchBE BE setup took "+(s3time-s2time)+" ms");
//	System.out.println("send frame table and tab display setup took "+(s4time-s3time)+" ms");
//	System.out.println("searching took "+(s5time-s4time)+" ms");
//System.out.println("finish took "+(endtime-s5time)+" ms");
//System.out.println("total took "+(endtime-starttime)+" ms");
//	
	// @Test
	public void FixMissingSupervisorsTest() {
		System.out.println("Fix Missing Supervisors test");
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
			VertxUtils.cachedEnabled = false;
			qRules = GennyJbpmBaseTest.setupLocalService();
			userToken = new GennyToken("userToken", qRules.getToken());
			serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
		}

		BaseEntityUtils beUtils = new BaseEntityUtils(userToken);
		beUtils.setServiceToken(serviceToken);

		long starttime = System.currentTimeMillis();
		long looptime = 0;
		long searchtime = 0;
		SearchEntity searchBE = new SearchEntity("SBE_TEST", "Supervsior Journals")
				.addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "JNL_%")
				/* .addFilter("PRI_SYNC", SearchEntity.StringFilter.LIKE, "FALSE") */
				.addFilter("LNK_INTERN_SUPERVISOR", SearchEntity.StringFilter.LIKE,
						"%PER_HOLGER_AT_SPACETRUNKSTUDIO_DOT_COM_DOT_AU%")
				.addColumn("PRI_NAME", "Name").addColumn("LNK_INTERNSHIP", "Internship")
				.addColumn("LNK_INTERN", "Intern").addColumn("LNK_HOST_COMPANY_REP", "Host Company Rep")
				.addColumn("LNK_HOST_COMPANY", "Host Company").addColumn("LNK_INTERN_SUPERVISOR", "Supervisor")
				.addColumn("PRI_JOURNAL_DATE", "Date").addColumn("PRI_JOURNAL_HOURS", "Hours")
				.addColumn("PRI_JOURNAL_TASKS", "Tasks").addColumn("PRI_JOURNAL_LEARNING_OUTCOMES", "Learning Outcomes")
				.addColumn("PRI_FEEDBACK", "Feedback").addColumn("PRI_STATUS", "Status").addColumn("PRI_SYNC", "Synced")
				.addColumn("PRI_LAST_UPDATED", "Last Updated").addColumn("PRI_INTERN_LAST_UPDATE", "Intern last update")
				.addColumn("PRI_SUPERVISOR_LAST_UPDATE", "Last Supervisor Update")
				.addColumn("PRI_LAST_CHANGED_BY", "Last Changed By").setPageStart(0).setPageSize(2000);

		searchBE.setRealm(serviceToken.getRealm());

		BaseEntityService2 service = new BaseEntityService2(null);
		service.findBySearchBECount(searchBE);

		String jsonSearchBE = JsonUtils.toJson(searchBE);
		/* System.out.println(jsonSearchBE); */
		String resultJson;
		BaseEntity result = null;
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
			for (BaseEntity be : bes) {
				Optional<String> unsynced = be.getValue("PRI_SYNC");
				/*
				 * if (!((unsynced.isPresent()) && ("TRUE".equals(unsynced.get())))) { Answer
				 * ans = new Answer($user.getCode(),be.getCode(),"PRI_SYNC","TRUE");
				 * beUtils.saveAnswer(ans); be = beUtils.getBaseEntityByCode(be.getCode());
				 * unsyncedItemList.add(be); }
				 */
				be = beUtils.getBaseEntityByCode(be.getCode());
				unsyncedItemList.add(be);
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
//		BaseEntityUtils beUtils = new BaseEntityUtils(userToken);
//		beUtils.setServiceToken(serviceToken);
//		SearchEntity searchBE = new SearchEntity("SBE_SEARCH", "Fix Missing Supervisors")
//				.addSort("PRI_CODE", "Created", SearchEntity.Sort.ASC)
//				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "BEG_%") 
//				.addColumn("PRI_ASSOC_HOST_COMPANY_EMAIL", "Host Company Rep Email")
//				.addColumn("LNK_INTERN_SUPERVISOR", "Supervisor")
//				.addColumn("LNK_HOST_COMPANY_REP", "Host Company Rep")
//				.setPageStart(0)
//				.setPageSize(20000);
//		
//		searchBE.setRealm(serviceToken.getRealm());
//		
// 		String jsonSearchBE = JsonUtils.toJson(searchBE);
// 		/* System.out.println(jsonSearchBE); */
//		String resultJson;
//		BaseEntity result = null; 
//		try {
//			resultJson = QwandaUtils.apiPostEntity(GennySettings.qwandaServiceUrl + "/qwanda/baseentitys/search",
//					jsonSearchBE, serviceToken.getToken());
//				QDataBaseEntityMessage resultMsg = JsonUtils.fromJson(resultJson, QDataBaseEntityMessage.class);
//				BaseEntity[] bes = resultMsg.getItems();
//				System.out.println("Processing "+resultMsg.getItems().length+" Internships");
//				Map<String,BaseEntity> supervisors = new HashMap<String,BaseEntity>();
//				for (BaseEntity be : bes) {
//
//					BaseEntity hcr = null;
//					BaseEntity supervisor = null;
//					String lnkHCR = null;
//					
//					System.out.println("Processing Internship "+be.getCode());
//					Optional<String> optEmail = be.getValue("PRI_ASSOC_HOST_COMPANY_EMAIL");
//					Optional<String> optHCR = be.getValue("LNK_HOST_COMPANY_REP");
//					Optional<String> optSupervisor = be.getValue("LNK_INTERN_SUPERVISOR");
//					if (optHCR.isPresent()) {
//						String hcrCode = optHCR.get();
//						System.out.println("HCR found is"+hcrCode);
//						if ((hcrCode != null)&&(hcrCode.length() > 2)) {
//							hcrCode = hcrCode.substring(2,hcrCode.length()-2);
//							System.out.println("HCR = "+hcrCode);
//							hcr = beUtils.getBaseEntityByCode(hcrCode); 
//						} else {
//							System.out.println("BAD HCRCODE !!!");
//						}
//						
//					}
//					if (optEmail.isPresent()) {
//						
//						String email = optEmail.get();
//						System.out.println("Setting Host Company Rep"+email);
//						System.out.println("Email = "+email);
//						String code = "PER_"+QwandaUtils.getNormalisedUsername(email);
//						if (!optHCR.isPresent()) {
//							 lnkHCR = "[\""+code+"\"]";
//							beUtils.saveAnswer(new Answer(be.getCode(),be.getCode(),"LNK_HOST_COMPANY_REP",lnkHCR));
//						}
//					}
//					
//					if (!optSupervisor.isPresent()) {
//							System.out.println("Setting Supervisor "+lnkHCR);
//							if (lnkHCR != null) {
//								beUtils.saveAnswer(new Answer(be.getCode(),be.getCode(),"LNK_INTERN_SUPERVISOR",lnkHCR));
//							} else 
//							if (hcr != null) {
//								lnkHCR = "[\""+hcr.getCode()+"\"]";
//								beUtils.saveAnswer(new Answer(be.getCode(),be.getCode(),"LNK_INTERN_SUPERVISOR",lnkHCR));
//								
//								beUtils.saveAnswer(new Answer(hcr.getCode(),hcr.getCode(),"PRI_IS_SUPERVISOR",true));
//								beUtils.saveAnswer(new Answer(hcr.getCode(),hcr.getCode(),"PRI_DISABLED",false));
//							}
//					} else {
//						
//						// Fix supeervisor
//						String supervisorCode = optSupervisor.get();
//						System.out.println("Supervisor found is"+supervisorCode);
//						if ((supervisorCode != null)&&(supervisorCode.length() > 2)) {
//							supervisorCode = supervisorCode.substring(2,supervisorCode.length()-2);
//							System.out.println("SUPERVISOR = "+supervisorCode);
//							supervisor = beUtils.getBaseEntityByCode(supervisorCode); 
//							if (supervisor != null) {
//							beUtils.saveAnswer(new Answer(supervisor.getCode(),supervisor.getCode(),"PRI_IS_SUPERVISOR",true));
//							beUtils.saveAnswer(new Answer(supervisor.getCode(),supervisor.getCode(),"PRI_DISABLED",false));
//							} else {
//								System.out.println("NO SUPERVISOR EXISTS !!!");
//							}
//						} else {
//							System.out.println("BAD SUPERVISOR CODE !!!");
//						}
//
//					}
//
//				}
//				System.out.println("Finished Fixing Journals");
//		} catch (Exception e1) {
//			e1.printStackTrace();
//		}

//		SearchEntity searchBE = new SearchEntity("SBE_SEARCH", "Fix Missing Supervisors")
//				.addSort("PRI_CODE", "Created", SearchEntity.Sort.ASC)
//				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "BEG_%") 
//				.addColumn("PRI_ASSOC_HOST_COMPANY_EMAIL", "Host Company Rep Email")
//				.addColumn("LNK_INTERN_SUPERVISOR", "Supervisor")
//				.addColumn("LNK_HOST_COMPANY_REP", "Host Company Rep")
//				.setPageStart(0)
//				.setPageSize(20000);
//		
//		searchBE.setRealm(serviceToken.getRealm());
//		
// 		String jsonSearchBE = JsonUtils.toJson(searchBE);
// 		/* System.out.println(jsonSearchBE); */
//		String resultJson;
//		BaseEntity result = null; 
//		try {
//			resultJson = QwandaUtils.apiPostEntity(GennySettings.qwandaServiceUrl + "/qwanda/baseentitys/search",
//					jsonSearchBE, serviceToken.getToken());
//				QDataBaseEntityMessage resultMsg = JsonUtils.fromJson(resultJson, QDataBaseEntityMessage.class);
//				BaseEntity[] bes = resultMsg.getItems();
//				System.out.println("Processing "+resultMsg.getItems().length+" Internships");
//				Map<String,BaseEntity> supervisors = new HashMap<String,BaseEntity>();
//				for (BaseEntity be : bes) {
//if (be.getCode().equals("BEG_32650251AD874EEA9252740795D6F9D6")) {
//					BaseEntity hcr = null;
//					BaseEntity supervisor = null;
//					String lnkHCR = null;
//					
//					System.out.println("Processing Internship "+be.getCode());
//					Optional<String> optEmail = be.getValue("PRI_ASSOC_HOST_COMPANY_EMAIL");
//					Optional<String> optHCR = be.getValue("LNK_HOST_COMPANY_REP");
//					Optional<String> optSupervisor = be.getValue("LNK_INTERN_SUPERVISOR");
//					if (optHCR.isPresent()) {
//						String hcrCode = optHCR.get();
//						hcrCode = hcrCode.substring(2,hcrCode.length()-2);
//						System.out.println("HCR = "+hcrCode);
//						hcr = beUtils.getBaseEntityByCode(hcrCode); 
//						
//						
//					}
//					if (optEmail.isPresent()) {
//						String email = optEmail.get();
//						System.out.println("Email = "+email);
//						String code = "PER_"+QwandaUtils.getNormalisedUsername(email);
//						if (!optHCR.isPresent()) {
//							 lnkHCR = "[\""+code+"\"]";
//							beUtils.saveAnswer(new Answer(be.getCode(),be.getCode(),"LNK_HOST_COMPANY_REP",lnkHCR));
//						}
//					}
//					
//					if (!optSupervisor.isPresent()) {
//							System.out.println("Setting Supervisor "+lnkHCR);
//							if (lnkHCR != null) {
//								beUtils.saveAnswer(new Answer(be.getCode(),be.getCode(),"LNK_INTERN_SUPERVISOR",lnkHCR));
//							} else 
//							if (hcr != null) {
//								lnkHCR = "[\""+hcr.getCode()+"\"]";
//								beUtils.saveAnswer(new Answer(be.getCode(),be.getCode(),"LNK_INTERN_SUPERVISOR",lnkHCR));
//							}
//					}
//}
//				}
//				System.out.println("Finished Fixing Journals");
//		} catch (Exception e1) {
//			e1.printStackTrace();
//		}		
	}

	// @Test
	public void showFramesTest() {
		GennyToken tokenUser = GennyJbpmBaseTest.createGennyToken("ABCDEFGH", "internmatch", "adam.crow@gada.io",
				"Adam Crow", "intern");
		GennyToken tokenSupervisor = GennyJbpmBaseTest.createGennyToken("BCDEFGSHS", "internmatch",
				"kanika.gulati@gada.io", "Kanika Gulati", "supervisor");
		System.out.println(tokenUser.getToken());
		System.out.println(tokenSupervisor.getToken());

		System.out.println("showFrame Test");

		GennyKieSession gks = null;

		try {
			gks = GennyKieSession.builder(serviceToken, true).addDrl("SignalProcessing").addDrl("DataProcessing")
					.addDrl("EventProcessing").addJbpm("Lifecycles").addDrl("AuthInit").addJbpm("AuthInit")
					.addDrl("InitialiseProject").addJbpm("InitialiseProject").build();

			gks.createTestUsersGroups();

			GennyToken newUser1A = gks.createToken("PER_USER1");
			gks.start();

			gks.injectSignal("initProject"); // This should initialise everything
			gks.injectEvent("authInitMsg", newUser1A); // log in as new user
			gks.advanceSeconds(5, false);
			gks.displayTasks(newUser1A);

			gks.showStatuses();
			// Now answer a question

			gks.injectAnswer("PRI_FIRSTNAME", newUser1A);
			gks.injectAnswer("PRI_LASTNAME", newUser1A);
			gks.injectAnswer("PRI_DOB", newUser1A);
			gks.injectAnswer("PRI_PREFERRED_NAME", newUser1A);
			gks.injectAnswer("PRI_EMAIL", newUser1A);
			gks.injectAnswer("PRI_MOBILE", newUser1A);
			gks.injectAnswer("PRI_USER_PROFILE_PICTURE", newUser1A);
			gks.injectAnswer("PRI_ADDRESS_FULL", newUser1A);

			gks.injectEvent("QUE_SUBMIT", newUser1A);

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

	// @Test
	public void importUsers() {
		System.out.println("Import Users test");
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
			VertxUtils.cachedEnabled = false;
			qRules = GennyJbpmBaseTest.setupLocalService();
			userToken = new GennyToken("userToken", qRules.getToken());
			serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
		}

		BaseEntityUtils beUtils = new BaseEntityUtils(userToken);
		beUtils.setServiceToken(serviceToken);

		String searchBarValue = "googleid:users:" + System.getenv("GOOGLE_DOC_ID") + ":" + "Users";
		// String searchBarValue =
		// "googleid:hc:"+System.getenv("GOOGLE_DOC_ID_4")+":"+"Form Responses 1";
		Answer answer = new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_SEARCH_BAR", searchBarValue);

		/* Now import a google doc xls file and generate a List of BaseEntityImports */
		String googleDocId = null;
		String sheetId = "Users"; /* default */
		Integer start = 0;
		Integer finish = 1000;
		String[] split = answer.getValue().split(":");

		if (split.length == 3) {
			googleDocId = split[2];
		} else if (split.length == 4) {
			googleDocId = split[2];
			sheetId = split[3].trim();
		}
		if (split.length == 6) {
			googleDocId = split[2];
			sheetId = split[3].trim();
			start = Integer.parseInt(split[4]);
			finish = Integer.parseInt(split[5]);
		}

		googleDocId = googleDocId.trim();

		Map<String, String> fieldMapping = new HashMap<String, String>();
		/* fieldMapping.put("Company Trading Name".toLowerCase(), "PRI_NAME"); */
		fieldMapping.put("firstname", "PRI_FIRSTNAME");
		fieldMapping.put("lastname", "PRI_LASTNAME");
		fieldMapping.put("email", "PRI_EMAIL");
		fieldMapping.put("company", "PRI_ASSOC_COMPANY");
		fieldMapping.put("password", "PRI_PASSWORD");
		fieldMapping.put("roles", "PRI_ROLES");
		fieldMapping.put("mobile", "PRI_PHONE");

		fieldMapping.put("UNIQUE_KEY_FIELD", "email");
		fieldMapping.put("PREFIX", "PER_");

		List<BaseEntityImport> beImports = ImportUtils.importGoogleDoc(googleDocId, sheetId, fieldMapping);
		System.out.println("Importing Rule : " + beImports.size() + " items");
		/* now generate the baseentity and send through all the answers */
		List<Answer> answers = new ArrayList<Answer>();
		Integer count = 0;

		/* First create The Host Company reps */

		for (BaseEntityImport beImport : beImports) {
			if (beImport.getAttributeValuePairList().isEmpty()) {
				continue;
			}
			System.out.println("Import User :" + beImport.getCode() + ":" + beImport.getName());

			/* check if already there */
			BaseEntity be = beUtils.getBaseEntityByCode(beImport.getCode());
			if (be == null) {
				be = beUtils.create(beImport.getCode(), beImport.getName());
			}
			Map<String, String> kv = new HashMap<String, String>();

			for (Tuple2<String, String> attributeCodeValue : beImport.getAttributeValuePairList()) {
				if (attributeCodeValue != null) {
					String value = StringUtils.isBlank(attributeCodeValue._2) ? "" : attributeCodeValue._2;
					value = value.trim();
					if (value.contains("'")) {
						value = value.replace("'", "\\'");
					}
					kv.put(attributeCodeValue._1, value);
				}
			}

			Boolean intern = false;
			String[] roles = kv.get("PRI_ROLES").split(",");
			for (String role : roles) {
				Answer isSomething = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(),
						"PRI_IS_" + role.toUpperCase(), "TRUE");
				answers.add(isSomething);
				if ("INTERN".equals(role.toUpperCase())) {
					Answer isImported = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_STATUS",
							"AVAILABLE");
					answers.add(isImported);
					intern = true;
				}
			}

			String hostCompany = kv.get("PRI_ASSOC_COMPANY");
			BaseEntity hc = ImportUtils.fetchBaseEntityByName(beUtils, hostCompany, "CPY_");

			if (intern) {
				if (hc != null) {
					Answer lnkHC = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "LNK_HOST_COMPANY",
							"[\"" + hc.getCode() + "\"]");
					answers.add(lnkHC);

					Answer isHC = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_ASSOC_HC",
							kv.get("PRI_ASSOC_COMPANY"));
					answers.add(isHC);
				}

			} else {
				if (hc != null) {
					Answer lnkHC = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "LNK_COMPANY",
							"[\"" + hc.getCode() + "\"]");
					answers.add(lnkHC);

					Answer isHC = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_ASSOC_COMPANY",
							kv.get("PRI_ASSOC_COMPANY"));
					answers.add(isHC);
				}

			}

			Answer isImported = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_IMPORTED", "TRUE");
			answers.add(isImported);
			Answer isFirstname = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_FIRSTNAME",
					kv.get("PRI_FIRSTNAME"));
			answers.add(isFirstname);
			Answer isLastname = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_LASTNAME",
					kv.get("PRI_LASTNAME"));
			answers.add(isLastname);
			Answer isEmail = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_EMAIL",
					kv.get("PRI_EMAIL"));
			answers.add(isEmail);
			Answer isMobile = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_PHONE",
					kv.get("PRI_PHONE"));
			answers.add(isMobile);
			Answer isPhone = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_MOBILE",
					kv.get("PRI_PHONE"));
			answers.add(isPhone);

			QDataAnswerMessage msg = new QDataAnswerMessage(answers);
			System.out.println("created QDataAnswerMessage  for " + beImport.getCode() + ":" + be.getName());
			msg.setToken(beUtils.getGennyToken().getToken());
			/* creating new user in keycloak */
			/* ASSUME FIRSTNAME, LASTNAME, EMAIL ARE GOOD!!! TODO */
			try {
				String email = kv.get("PRI_EMAIL");
				String userPassword = kv.get("PRI_PASSWORD");
				String userId = "";
				/* System.out.println(" keycloak token is "+serviceToken.getToken()); */
				if (kv.get("PRI_ROLES").contains("DEV")) {
					userId = KeycloakUtils.createUser(serviceToken.getToken(), serviceToken.getRealm(), email,
							kv.get("PRI_FIRSTNAME"), kv.get("PRI_LASTNAME"), email, userPassword, "dev,admin,test,user",
							"dev,test,admin,user");
				} else

				if (kv.get("PRI_ROLES").contains("ADMIN")) {
					userId = KeycloakUtils.createUser(serviceToken.getToken(), serviceToken.getRealm(), email,
							kv.get("PRI_FIRSTNAME"), kv.get("PRI_LASTNAME"), email, userPassword, "admin,user",
							"admin,user");
				} else if (kv.get("PRI_ROLES").contains("AGENT")) {
					userId = KeycloakUtils.createUser(serviceToken.getToken(), serviceToken.getRealm(), email,
							kv.get("PRI_FIRSTNAME"), kv.get("PRI_LASTNAME"), email, userPassword, "user", "user");

				}
				Answer keycloakId = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_UUID", userId);
				answers.add(keycloakId);

			} catch (IOException e) {
				/* keycloak exception spits the answer */
			}

			System.out.println("inserting new user  message containing all their new attributes");
			if ((count >= start) && (count <= finish)) {
				insert(msg);
			} else {
				if (count > finish) {
					break;
				}
			}
			count++;
		}
		System.out.println("Aggregated  all Answers ");

		System.out.println("Finished insertion of QDataAnswerMessage containing imported");

	}

	// @Test
	public void sendVerify2Test() {
		System.out.println("Search test");
		String password = System.getenv("SERVICE_PASSWORD");
		String token = null;

		try {
			token = KeycloakUtils.getAccessToken("https://keycloak.gada.io", "internmatch", "internmatch",
					"dc7d0960-2e1d-4a78-9eef-77678066dbd3", "service", password);
		} catch (IOException e1) {
		}
//		sendVerifyMail(token, "gerard+intern31@outcome.life", "Gerard", "Holland");
//		sendVerifyMail(token, "kanika.gulati+intern@gada.io", "Kanika Intern Test", "two");
//		sendVerifyMail(token, "thomas.crow+intern@gada.io", "Thomas Intern Test", "three");
//		sendVerifyMail(token, "barad.ghimire+intern@gada.io", "Barad Intern Test", "four");
//		sendVerifyMail(token, "cto+intern@gada.io", "Adam Intern Test", "five");
//		sendVerifyMail(token, "christopher.pyke+intern@gada.io", "Chris Testing App", "Six");
//		sendVerifyMail(token, "gerard.holland", "outcome.life","Gerard", "Holland");
//		sendVerifyMail(token, "domenic.saporito", "outcome.life","Domenic", "Saporito");
		sendVerifyMail(token, "adamcrow63", "gmail.com", "Adam", "Crow");
//		sendVerifyMail(token, "christopher.pyke", "gada.io","Christopher", "Pyke");
//		sendVerifyMail(token, "stephenie.pulis-cassar", "outcomelife.com.au","Stephenie", "Pulis-Cassar");
//		sendVerifyMail(token, "joshua.tinner+intern31@outcome.life", "Stephenie", "Pulis-Cassar");
	}

	private void sendVerifyMail(String token, String emailusername) {
		String userId = KeycloakUtils.sendVerifyEmail(realm, emailusername, token);
		System.out.println("UserId=" + userId);
	}

	private void sendVerifyMail(String token, String emailusername, String firstname, String lastname) {
//		LocalDateTime now = LocalDateTime.now();
//		String mydatetime = new SimpleDateFormat("yyyyMMddHHmmss").format(now.toDate());
//		// System.out.println(username+" serviceToken=" + token);
//		String emailusername = username + "+" + mydatetime + "@" + domain;

		String password = UUID.randomUUID().toString().substring(0, 8);
		String userId;
		try {
			userId = KeycloakUtils.createUser(token, realm, emailusername, firstname, lastname, emailusername, password,
					"user", "user");
		} catch (IOException e) {
		}
		userId = KeycloakUtils.sendVerifyEmail(realm, emailusername, token);
		System.out.println("UserId=" + userId);
	}

	private void sendVerifyMail(String token, String username, String domain, String firstname, String lastname) {
		LocalDateTime now = LocalDateTime.now();
		String mydatetime = new SimpleDateFormat("yyyyMMddHHmmss").format(now);
		// System.out.println(username+" serviceToken=" + token);
		String emailusername = username + "+" + mydatetime + "@" + domain;

		String password = UUID.randomUUID().toString().substring(0, 8);
		String userId;
		try {
			userId = KeycloakUtils.createUser(token, realm, emailusername, firstname, lastname, emailusername, password,
					"user", "user");
		} catch (IOException e) {
		}
		userId = KeycloakUtils.sendVerifyEmail(realm, emailusername, token);
		System.out.println("UserId=" + userId);
	}

	// @Test
	public void importHostCompaniesTest() {
		this.importHostCompanies();
	}

	// @Test
	public void importHostCompaniesRepsTest() {
		this.importHostCompaniesReps();
	}

	// @Test
	public void importInternsTest() {
		this.importInterns();
	}

	// @Test
	public void importInternshipsTest() {
		this.importInternships();
	}

	// @Test
	public void verifyInternsTest() {
		verifyInterns();
	}

	// @Test
	public void importInternships() {
		System.out.println("Import Internships test");
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
			VertxUtils.cachedEnabled = false;
			qRules = GennyJbpmBaseTest.setupLocalService();
			userToken = new GennyToken("userToken", qRules.getToken());
			serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());

		}

		BaseEntityUtils beUtils = new BaseEntityUtils(userToken);
		beUtils.setServiceToken(serviceToken);

		String searchBarValue = "googleid:internships:" + System.getenv("GOOGLE_DOC_ID") + ":CQU T1";
		System.out.println("searchBarText=" + searchBarValue);
		Answer answer = new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_SEARCH_BAR", searchBarValue);

		/* Now import a google doc xls file and generate a List of BaseEntityImports */
		String googleDocId = null;
		String sheetId = "CQU T1"; /* default */
		Integer start = 0;
		Integer finish = 5;
		String[] split = answer.getValue().split(":");

		if (split.length == 3) {
			googleDocId = split[2];
		} else if (split.length == 4) {
			googleDocId = split[2];
			sheetId = split[3].trim();
		}
		if (split.length == 6) {
			googleDocId = split[2];
			sheetId = split[3].trim();
			start = Integer.parseInt(split[4]);
			finish = Integer.parseInt(split[5]);
		}

		googleDocId = googleDocId.trim();

		Map<String, String> fieldMapping = new HashMap<String, String>();
		fieldMapping.put("Education Provider", "PRI_ASSOC_EP");
		fieldMapping.put("Student ID", "PRI_STUDENT_ID");
		fieldMapping.put("Student First Name", "PRI_IMPORT_FIRSTNAME");
		fieldMapping.put("Last Name", "PRI_IMPORT_LASTNAME");
		fieldMapping.put("Student Email", "PRI_EMAIL");
		fieldMapping.put("Industry", "PRI_INDUSTRY");
		fieldMapping.put("Internship Title", "PRI_NAME");
		fieldMapping.put("Company Trading Name", "PRI_ASSOC_HOST_COMPANY");
		fieldMapping.put("ABN", "PRI_ABN");
		fieldMapping.put("Host Company Supervisor", "PRI_ASSOC_SUPERVISOR");
		fieldMapping.put("Host Company Rep", "PRI_ASSOC_HCR");
		fieldMapping.put("Host Company Rep Email", "PRI_ASSOC_HOST_COMPANY_EMAIL");

		/* fieldMapping.put("UNIQUE_KEY_FIELD", "Student Email".toLowerCase()); */
		fieldMapping.put("NAME_KEY_FIELD", "Internship Title".toLowerCase());
		fieldMapping.put("PREFIX", "BEG_");

		List<BaseEntityImport> beImports = ImportUtils.importGoogleDoc(googleDocId, sheetId, fieldMapping);
		System.out.println("Importing Rule : " + beImports.size() + " items");
		/* now generate the baseentity and send through all the answers */
		List<Answer> answers = new ArrayList<Answer>();
		Integer count = 0;
		for (BaseEntityImport beImport : beImports) {
			/* check if already there */

			BaseEntity be = beUtils.getBaseEntityByCode(beImport.getCode());
			if (be == null) {
				be = beUtils.create(beImport.getCode(), beImport.getName());
			}

			Map<String, String> kv = new HashMap<String, String>();
			String hostCompany = "";
			String supName = "";
			String eduName = "";
			String hcrEmail = "";

			for (Tuple2<String, String> attributeCodeValue : beImport.getAttributeValuePairList()) {
				String value = StringUtils.isBlank(attributeCodeValue._2) ? "" : attributeCodeValue._2;
				value = value.trim();
				kv.put(attributeCodeValue._1, value);
				Answer answer2 = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), attributeCodeValue._1,
						value);
				answers.add(answer2);
				ImportUtils.processAttribute(beUtils, be.getCode(), attributeCodeValue, "PRI_ASSOC_EP", "PRI_NAME",
						"CPY_", "LNK_EDU_PROVIDER", answers);
				BaseEntity hc = ImportUtils.processAttribute(beUtils, be.getCode(), attributeCodeValue, "PRI_ABN",
						"PRI_ABN", "CPY_", "LNK_HOST_COMPANY", answers);
				if (hc != null) {
					System.out.println("Found link to " + hc.getName() + " as LNK_HOST_COMPANY");
					Answer assocHC = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_ASSOC_HC",
							hc.getName());
					hostCompany = hc.getName();
					answers.add(assocHC);
					if (hc.getValue("PRI_ADDRESS_FULL_JSON").isPresent()) {
						Answer assocHCloc = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(),
								"PRI_ADDRESS_FULL_JSON", (String) (hc.getValue("PRI_ADDRESS_FULL_JSON").get()));
						answers.add(assocHCloc);
					}

				}
				BaseEntity sup = ImportUtils.processAttribute(beUtils, be.getCode(), attributeCodeValue,
						"PRI_ASSOC_SUPERVISOR", "PRI_NAME", "PER_", "LNK_INTERN_SUPERVISOR", answers);
				if (sup != null) {
					System.out.println("Found link to " + sup.getName() + " as LNK_INTERN_SUPERVISOR");
					Answer assocSUP = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(),
							"PRI_ASSOC_SUPERVISOR", sup.getName());
					supName = sup.getName();
					answers.add(assocSUP);
				}
				BaseEntity rep = ImportUtils.processAttribute(beUtils, be.getCode(), attributeCodeValue,
						"PRI_ASSOC_HCR", "PRI_NAME", "PER_", "LNK_HOST_COMPANY_REP", answers);
				if (rep != null) {
					System.out.println("Found link to " + rep.getName() + " as LNK_HOST_COMPANY_REP");
					Answer assocREP = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_ASSOC_HCR",
							rep.getName());
					if (rep.getValue("PRI_EMAIL").isPresent()) {
						hcrEmail = (String) (rep.getValue("PRI_EMAIL").get());
					}
					answers.add(assocREP);

				}

			}
			String industry = kv.get("PRI_INDUSTRY");
			if (industry != null) {
				Answer industryAns = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_LNK_INDUSTRY",
						"[\"SEL_INDUSTRY_" + industry.toUpperCase() + "\"]");
				answers.add(industryAns);
			}
			Answer duration = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(),
					"PRI_INTERNSHIP_DURATION_STRIPPED", "12");
			answers.add(duration);

			Answer internCount = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "LNK_NO_OF_INTERNS",
					"[\"SEL_NO_OF_INTERNS_ONE\"]");
			answers.add(internCount);
			Answer daysperweek = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_DAYS_PER_WEEK",
					"3");
			answers.add(daysperweek);
			Answer currentInterns = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(),
					"PRI_CURRENT_INTERNS", "1");
			answers.add(currentInterns);
			Answer isInternship = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_IS_INTERNSHIP",
					"true");
			answers.add(isInternship);
			Answer isImported = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_IMPORTED", "true");
			answers.add(isImported);
			Answer hasLoggedIn = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_HAS_LOGGED_IN",
					"false");
			answers.add(hasLoggedIn);
			Answer status = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_STATUS", "ACTIVE");
			answers.add(status);
			Answer ilastname = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_LASTNAME",
					kv.get("PRI_IMPORT_LASTNAME"));
			answers.add(ilastname);
			Answer ifirstname = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_FIRSTNAME",
					kv.get("PRI_IMPORT_FIRSTNAME"));
			answers.add(ifirstname);
			Answer iemail = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_EMAIL",
					kv.get("PRI_EMAIL"));
			answers.add(iemail);

			String internCode = "PER_" + QwandaUtils.getNormalisedUsername(kv.get("PRI_EMAIL"));
			System.out.println("Linking Internship " + be.getCode() + " to intern " + internCode);
			Answer intern = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "LNK_INTERN",
					"[\"" + internCode + "\"]");
			answers.add(intern);

			Answer isid = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_STUDENT_ID",
					kv.get("PRI_INTERN_STUDENT_ID"));
			answers.add(isid);
			Answer iname = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_INTERN_NAME",
					(ifirstname.getValue() + " " + ilastname.getValue()).trim());
			answers.add(iname);
			Answer accepted = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_INTERN_ACCEPTED",
					"true");
			answers.add(accepted);

			QDataAnswerMessage msg = new QDataAnswerMessage(answers);
			System.out.println("created QDataAnswerMessage  for " + beImport.getCode());
			msg.setToken(beUtils.getGennyToken().getToken());

			System.out.println("inserting new internship message containing all their new attributes");
			if ((count >= start) && (count <= finish)) {
				insert(msg);
			} else {
				if (count > finish) {
					break;
				}
			}
			count++;
		}
		System.out.println("Aggregated  all Answers ");

		System.out.println("Finished insertion of QDataAnswerMessage containing imported");

	}

	// @Test
	public void importInterns() {
		System.out.println("Import Interns test");
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
			VertxUtils.cachedEnabled = false;
			qRules = GennyJbpmBaseTest.setupLocalService();
			userToken = new GennyToken("userToken", qRules.getToken());
			serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());

		}

		BaseEntityUtils beUtils = new BaseEntityUtils(userToken);
		beUtils.setServiceToken(serviceToken);

		String searchBarValue = "googleid:interns:" + System.getenv("GOOGLE_DOC_ID") + ":CQU T1";
		System.out.println("searchBarText=" + searchBarValue);
		Answer answer = new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_SEARCH_BAR", searchBarValue);

		/* Now import a google doc xls file and generate a List of BaseEntityImports */
		String googleDocId = null;
		String sheetId = "Sheet1"; /* default */
		Integer start = 0;
		Integer finish = 5;
		String[] split = answer.getValue().split(":");

		if (split.length == 3) {
			googleDocId = split[2];
		} else if (split.length == 4) {
			googleDocId = split[2];
			sheetId = split[3].trim();
		}
		if (split.length == 6) {
			googleDocId = split[2];
			sheetId = split[3].trim();
			start = Integer.parseInt(split[4]);
			finish = Integer.parseInt(split[5]);
		}

		googleDocId = googleDocId.trim();

		Map<String, String> fieldMapping = new HashMap<String, String>();
		fieldMapping.put("Education Provider", "PRI_ASSOC_EP");
		fieldMapping.put("Student ID", "PRI_STUDENT_ID");
		fieldMapping.put("Student First Name", "PRI_IMPORT_FIRSTNAME");
		fieldMapping.put("Last Name", "PRI_IMPORT_LASTNAME");
		fieldMapping.put("Student Email", "PRI_EMAIL");
		fieldMapping.put("Industry", "PRI_INDUSTRY");
		fieldMapping.put("Company Trading Name", "PRI_ASSOC_HOST_COMPANY");
		fieldMapping.put("ABN", "PRI_ASSOC_HOST_COMPANY");
		fieldMapping.put("Host Company Supervisor", "PRI_ASSOC_SUPERVISOR");
		fieldMapping.put("Host Company Rep", "PRI_ASSOC_HCR");
		fieldMapping.put("Host Company Rep Email", "PRI_ASSOC_HOST_COMPANY_EMAIL");

		fieldMapping.put("UNIQUE_KEY_FIELD", "Student Email".toLowerCase());
		fieldMapping.put("NAME_KEY_FIELD", "Student Email".toLowerCase());
		fieldMapping.put("PREFIX", "PER_");

		List<BaseEntityImport> beImports = ImportUtils.importGoogleDoc(googleDocId, sheetId, fieldMapping);
		System.out.println("Importing Rule : " + beImports.size() + " items");
		/* now generate the baseentity and send through all the answers */
		List<Answer> answers = new ArrayList<Answer>();
		Integer count = 0;
		for (BaseEntityImport beImport : beImports) {
			/* check if already there */

			BaseEntity be = beUtils.getBaseEntityByCode(beImport.getCode());
			if (be == null) {
				be = beUtils.create(beImport.getCode(), beImport.getName());
			}
			System.out.println("Import Intern:" + beImport.getCode() + ":" + beImport.getName());

			Map<String, String> kv = new HashMap<String, String>();

			for (Tuple2<String, String> attributeCodeValue : beImport.getAttributeValuePairList()) {
				String value = StringUtils.isBlank(attributeCodeValue._2) ? "" : attributeCodeValue._2;
				value = value.trim();
				kv.put(attributeCodeValue._1, value);
				Answer answer2 = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), attributeCodeValue._1,
						value);
				answers.add(answer2);
				ImportUtils.processAttribute(beUtils, be.getCode(), attributeCodeValue, "PRI_ASSOC_EP", "PRI_NAME",
						"CPY_", "LNK_EDU_PROVIDER", answers);

			}
			Answer isIntern = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_IS_INTERN", "true");
			answers.add(isIntern);
			Answer isImported = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_IMPORTED", "true");
			answers.add(isImported);
			Answer hasLoggedIn = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_HAS_LOGGED_IN",
					"false");
			answers.add(hasLoggedIn);
			Answer status = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_STATUS", "AVAILABLE");
			answers.add(status);
			Answer ilastname = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_LASTNAME",
					kv.get("PRI_IMPORT_LASTNAME"));
			answers.add(ilastname);
			Answer ifirstname = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_FIRSTNAME",
					kv.get("PRI_IMPORT_FIRSTNAME"));
			answers.add(ifirstname);
			Answer iemail = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_EMAIL",
					kv.get("PRI_INTERN_EMAIL"));
			answers.add(iemail);
			Answer isid = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_STUDENT_ID",
					kv.get("PRI_INTERN_STUDENT_ID"));
			answers.add(isid);
			Answer iname = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_INTERN_NAME",
					(ifirstname + " " + ilastname).trim());
			answers.add(iname);
			Answer accepted = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_INTERN_ACCEPTED",
					"true");
			answers.add(accepted);

			QDataAnswerMessage msg = new QDataAnswerMessage(answers);
			System.out.println("created QDataAnswerMessage  for " + beImport.getCode());
			msg.setToken(beUtils.getGennyToken().getToken());
			/* creating new user in keycloak */
			/* ASSUME FIRSTNAME, LASTNAME, EMAIL ARE GOOD!!! TODO */
			try {
				String email = kv.get("PRI_EMAIL").trim();
				String firstname = kv.get("PRI_IMPORT_FIRSTNAME").trim();
				String lastname = kv.get("PRI_IMPORT_LASTNAME").trim();
				String userPassword = UUID.randomUUID().toString().substring(0, 10);
				/* System.out.println(" keycloak token is "+serviceToken.getToken()); */
				String userId = KeycloakUtils.createUser(serviceToken.getToken(), serviceToken.getRealm(), email,
						firstname, lastname, email, userPassword, "user", "user");
				Answer keycloakId = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_UUID", userId);
				answers.add(keycloakId);
			} catch (IOException e) {
				/* keycloak exception spits the answer */
			}

			System.out.println("inserting new intern message containing all their new attributes");
			if ((count >= start) && (count <= finish)) {
				insert(msg);
			} else {
				if (count > finish) {
					break;
				}
			}
			count++;
		}
		System.out.println("Aggregated  all Answers ");

		System.out.println("Finished insertion of QDataAnswerMessage containing imported");

	}

	// @Test
	public void importHostCompaniesReps() {
		System.out.println("Import Host Companies  Rep test");
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
			VertxUtils.cachedEnabled = false;
			qRules = GennyJbpmBaseTest.setupLocalService();
			userToken = new GennyToken("userToken", qRules.getToken());
			serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
		}

		BaseEntityUtils beUtils = new BaseEntityUtils(userToken);
		beUtils.setServiceToken(serviceToken);

		String searchBarValue = "googleid:hc:" + System.getenv("GOOGLE_DOC_ID") + ":" + "Host Company List";
		// String searchBarValue =
		// "googleid:hc:"+System.getenv("GOOGLE_DOC_ID_4")+":"+"Form Responses 1";
		Answer answer = new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_SEARCH_BAR", searchBarValue);

		/* Now import a google doc xls file and generate a List of BaseEntityImports */
		String googleDocId = null;
		String sheetId = "Sheet1"; /* default */
		Integer start = 0;
		Integer finish = 1000;
		String[] split = answer.getValue().split(":");

		if (split.length == 3) {
			googleDocId = split[2];
		} else if (split.length == 4) {
			googleDocId = split[2];
			sheetId = split[3].trim();
		}
		if (split.length == 6) {
			googleDocId = split[2];
			sheetId = split[3].trim();
			start = Integer.parseInt(split[4]);
			finish = Integer.parseInt(split[5]);
		}

		googleDocId = googleDocId.trim();

		Map<String, String> fieldMapping = new HashMap<String, String>();
		/* fieldMapping.put("Company Trading Name".toLowerCase(), "PRI_NAME"); */
		fieldMapping.put("ABN", "PRI_ABN");
		fieldMapping.put("HC Website", "PRI_WEBSITE");
		fieldMapping.put("Supervisor First Name", "PRI_FIRSTNAME");
		fieldMapping.put("Supervisor Surname", "PRI_LASTNAME");
		fieldMapping.put("Supervisor Position", "PRI_JOB_TITLE");
		fieldMapping.put("Supervisor Phone Number", "PRI_PHONE");
		fieldMapping.put("Supervisor Email", "PRI_EMAIL");
		fieldMapping.put("HC Address", "PRI_IMPORT_FULL_ADDRESS");

		fieldMapping.put("UNIQUE_KEY_FIELD", "Supervisor Email");
		fieldMapping.put("PREFIX", "PER_");

		List<BaseEntityImport> beImports = ImportUtils.importGoogleDoc(googleDocId, sheetId, fieldMapping);
		System.out.println("Importing Rule : " + beImports.size() + " items");
		/* now generate the baseentity and send through all the answers */
		List<Answer> answers = new ArrayList<Answer>();
		Integer count = 0;

		/* First create The Host Company reps */

		for (BaseEntityImport beImport : beImports) {
			if (beImport.getAttributeValuePairList().isEmpty()) {
				continue;
			}
			System.out.println("Import Host Company Rep :" + beImport.getCode() + ":" + beImport.getName());

			/* check if already there */
			BaseEntity be = beUtils.getBaseEntityByCode(beImport.getCode());
			if (be == null) {
				be = beUtils.create(beImport.getCode(), beImport.getName());
			}
			Map<String, String> kv = new HashMap<String, String>();

			for (Tuple2<String, String> attributeCodeValue : beImport.getAttributeValuePairList()) {
				if (attributeCodeValue != null) {
					String value = StringUtils.isBlank(attributeCodeValue._2) ? "" : attributeCodeValue._2;
					value = value.trim();
					if (value.contains("'")) {
						value = value.replace("'", "\\'");
					}

					kv.put(attributeCodeValue._1, value);
					Answer answer2 = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(),
							attributeCodeValue._1, value);
					answers.add(answer2);
					String beCode = be.getCode();
					BaseEntity hc = ImportUtils.linkToFind(beUtils, beCode, attributeCodeValue, "PRI_ABN", "PRI_ABN",
							"CPY_", "LNK_HOST_COMPANY_REP", answers);

					if (hc != null) {
						System.out.println("Found link to " + hc.getName() + " as LNK_HOST_COMPANY_REP");
						Answer assocHC = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_ASSOC_HC",
								hc.getName());
						answers.add(assocHC);

					}
				}
			}

			Answer isEntity = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_IS_HOST_CPY_REP",
					"true");
			answers.add(isEntity);
			Answer isImported = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_IMPORTED", "true");
			answers.add(isImported);
			String firstname = kv.get("PRI_FIRSTNAME");
			String lastname = kv.get("PRI_LASTNAME");
			Answer fullname = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_NAME",
					firstname.trim() + " " + lastname.trim());
			answers.add(fullname);
			QDataAnswerMessage msg = new QDataAnswerMessage(answers);
			System.out.println("created QDataAnswerMessage  for " + beImport.getCode() + ":" + be.getName());
			msg.setToken(beUtils.getGennyToken().getToken());
			/* creating new user in keycloak */
			/* ASSUME FIRSTNAME, LASTNAME, EMAIL ARE GOOD!!! TODO */
			try {
				String email = kv.get("PRI_EMAIL");
				String userPassword = UUID.randomUUID().toString().substring(0, 10);
				/* System.out.println(" keycloak token is "+serviceToken.getToken()); */
				String userId = KeycloakUtils.createUser(serviceToken.getToken(), serviceToken.getRealm(), email,
						firstname, lastname, email, userPassword, "user", "user");
				Answer keycloakId = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_UUID", userId);
				answers.add(keycloakId);
			} catch (IOException e) {
				/* keycloak exception spits the answer */
			}

			System.out.println("inserting new host company rep message containing all their new attributes");
			if ((count >= start) && (count <= finish)) {
				insert(msg);
			} else {
				if (count > finish) {
					break;
				}
			}
			count++;
		}
		System.out.println("Aggregated  all Answers ");

		System.out.println("Finished insertion of QDataAnswerMessage containing imported");

	}

	public void importHostCompanies() {
		System.out.println("Import Host Companies test");
		GennyToken userToken = null;
		GennyToken serviceToken = null;
		QRules qRules = null;

		if (false) {
			userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
			serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
			qRules = new QRules(eventBusMock, userToken.getToken());
			qRules.set("realm", userToken.getRealm());
			;
			qRules.setServiceToken(serviceToken.getToken());
			VertxUtils.cachedEnabled = true; // don't send to local Service Cache
			GennyKieSession.loadAttributesJsonFromResources(userToken);

		} else {
			VertxUtils.cachedEnabled = false;
			qRules = GennyJbpmBaseTest.setupLocalService();
			userToken = new GennyToken("userToken", qRules.getToken());
			serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
		}

		BaseEntityUtils beUtils = new BaseEntityUtils(userToken);
		beUtils.setServiceToken(serviceToken);

		String searchBarValue = "googleid:hc:" + System.getenv("GOOGLE_DOC_ID") + ":" + "Host Company List";
		// String searchBarValue =
		// "googleid:hc:"+System.getenv("GOOGLE_DOC_ID_4")+":"+"Form Responses 1";
		Answer answer = new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_SEARCH_BAR", searchBarValue);

		/* Now import a google doc xls file and generate a List of BaseEntityImports */
		String googleDocId = null;
		String sheetId = "Sheet1"; /* default */
		Integer start = 0;
		Integer finish = 1000;
		String[] split = answer.getValue().split(":");

		if (split.length == 3) {
			googleDocId = split[2];
		} else if (split.length == 4) {
			googleDocId = split[2];
			sheetId = split[3].trim();
		}
		if (split.length == 6) {
			googleDocId = split[2];
			sheetId = split[3].trim();
			start = Integer.parseInt(split[4]);
			finish = Integer.parseInt(split[5]);
		}

		googleDocId = googleDocId.trim();

		Map<String, String> fieldMapping = new HashMap<String, String>();
		fieldMapping.put("Company Trading Name", "PRI_NAME");
		fieldMapping.put("ABN", "PRI_ABN");
		fieldMapping.put("HC Website", "PRI_WEBSITE");
		fieldMapping.put("Supervisor Phone Number", "PRI_PHONE");
		fieldMapping.put("Supervisor Email", "PRI_EMAIL");
		fieldMapping.put("HC Address", "PRI_IMPORT_FULL_ADDRESS");

		fieldMapping.put("UNIQUE_KEY_FIELD", "ABN");
		fieldMapping.put("NAME_KEY_FIELD", "Company Trading Name");
		fieldMapping.put("PREFIX", "CPY_");

		List<BaseEntityImport> beImports = ImportUtils.importGoogleDoc(googleDocId, sheetId, fieldMapping);
		System.out.println("Importing Rule : " + beImports.size() + " items");
		/* now generate the baseentity and send through all the answers */
		List<Answer> answers = new ArrayList<Answer>();
		Integer count = 0;

		/* First create The Host Company reps */

		for (BaseEntityImport beImport : beImports) {
			if (beImport.getAttributeValuePairList().isEmpty()) {
				continue;
			}
			System.out.println("Import Host Company :" + beImport.getCode() + ":" + beImport.getName());
			/* check if already there */
			BaseEntity be = beUtils.getBaseEntityByCode(beImport.getCode());
			if (be == null) {
				be = beUtils.create(beImport.getCode(), beImport.getName());
			}
			Map<String, String> kv = new HashMap<String, String>();

			for (Tuple2<String, String> attributeCodeValue : beImport.getAttributeValuePairList()) {
				String value = StringUtils.isBlank(attributeCodeValue._2) ? "" : attributeCodeValue._2;
				if (value.contains("'")) {
					value = value.replace("'", "\\'");
				}
				value = value.trim();
				kv.put(attributeCodeValue._1, value);
				Answer answer2 = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), attributeCodeValue._1,
						value);
				answers.add(answer2);
			}
			Answer isEntity = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_IS_HOST_CPY",
					"true");
			answers.add(isEntity);
			Answer isImported = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_IMPORTED", "true");
			answers.add(isImported);
			Answer status = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_STATUS", "ACTIVE");
			answers.add(status);

			System.out.println("PRI_PHONE=" + kv.get("PRI_PHONE") + " EMAIL=" + kv.get("PRI_EMAIL"));
			Answer isLandline = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_LANDLINE",
					kv.get("PRI_PHONE"));
			answers.add(isLandline);
			Answer isEmail = new Answer(beUtils.getGennyToken().getUserCode(), be.getCode(), "PRI_EMAIL",
					kv.get("PRI_EMAIL"));
			answers.add(isEmail);

			QDataAnswerMessage msg = new QDataAnswerMessage(answers);
			System.out.println("created QDataAnswerMessage  for " + beImport.getCode() + ":" + be.getName());
			msg.setToken(beUtils.getGennyToken().getToken());

			System.out.println("inserting new host company message containing all their new attributes");
			if ((count >= start) && (count <= finish)) {
				insert(msg);
			} else {
				if (count > finish) {
					break;
				}
			}
			count++;
		}
		System.out.println("Aggregated  all Answers ");

		System.out.println("Finished insertion of QDataAnswerMessage containing imported");

	}

	public void verifyInterns() {
		System.out.println("Verify Interns");
		GennyToken userToken = null;
		GennyToken serviceToken = null;
		QRules qRules = null;

//		}
		String password = System.getenv("SERVICE_PASSWORD");
		String userPassword = System.getenv("USER_PASSWORD");
		String token = null;
		String userId = null;
		userPassword = UUID.randomUUID().toString().substring(0, 10);
		String googleDocId = System.getenv("GOOGLE_DOC_ID");
		googleDocId = googleDocId.trim();

		Map<String, String> fieldMapping = new HashMap<String, String>();
		fieldMapping.put("Education Provider".toLowerCase(), "PRI_ASSOC_EP");
		fieldMapping.put("Student ID".toLowerCase(), "PRI_STUDENT_ID");

		fieldMapping.put("Student First Name".toLowerCase(), "PRI_IMPORT_FIRSTNAME");
		fieldMapping.put("Last Name".toLowerCase(), "PRI_IMPORT_LASTNAME");
		fieldMapping.put("Student Email".toLowerCase(), "PRI_EMAIL");
		fieldMapping.put("Industry".toLowerCase(), "PRI_INDUSTRY");
		fieldMapping.put("Internship Title".toLowerCase(), "PRI_NAME");
		fieldMapping.put("Company Trading Name".toLowerCase(), "PRI_ASSOC_HOST_COMPANY");
		fieldMapping.put("Host Company Supervisor".toLowerCase(), "PRI_ASSOC_SUPERVISOR");
		fieldMapping.put("Host Company Rep".toLowerCase(), "PRI_ASSOC_HCR");
		fieldMapping.put("Host Company Rep Email".toLowerCase(), "PRI_ASSOC_HOST_COMPANY_EMAIL");

		fieldMapping.put("UNIQUE_KEY_FIELD", "Student Email".toLowerCase());
		fieldMapping.put("NAME_KEY_FIELD", "Internship Title".toLowerCase());
		fieldMapping.put("PREFIX", "BEG_");

		List<BaseEntityImport> beImports = ImportUtils.importGoogleDoc(googleDocId, "CQU T1", getFieldMappings());

		try {
			token = KeycloakUtils.getAccessToken("https://keycloak.gada.io", "internmatch", "internmatch",
					"dc7d0960-2e1d-4a78-9eef-77678066dbd3", "service", password);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// create all the users in keycloak
		for (BaseEntityImport beImport : beImports) {
			List<Tuple2<String, String>> pairs = beImport.getAttributeValuePairList();
			Map<String, String> kv = new HashMap<String, String>();
			for (Tuple2<String, String> p : pairs) {
				kv.put(p._1, p._2);
			}

			String email = kv.get("PRI_EMAIL").trim();
			String firstname = kv.get("PRI_IMPORT_FIRSTNAME").trim();
			String lastname = kv.get("PRI_IMPORT_LASTNAME").trim();

			System.out.println("firstname is " + firstname + ":" + lastname + "    " + email);

			try {
				userId = KeycloakUtils.createUser(token, realm, email, firstname, lastname, email, userPassword, "user",
						"user");
				userId = KeycloakUtils.sendVerifyEmail(realm, email, token);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				if (e.getMessage().contains("Email is already taken")) {
					System.out.println();
				}

			}
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Finished");
	}

//	@Test
	public void registerUser() {
		System.out.println("Search test");
		GennyToken userToken = null;
		GennyToken serviceToken = null;
		QRules qRules = null;

//		if (false) {
//			userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
//			serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
//			qRules = new QRules(eventBusMock, userToken.getToken());
//			qRules.set("realm", userToken.getRealm());
//			qRules.setServiceToken(serviceToken.getToken());
//			VertxUtils.cachedEnabled = true; // don't send to local Service Cache
//			GennyKieSession.loadAttributesJsonFromResources(userToken);
//
//		} else {
//			VertxUtils.cachedEnabled = false;
//			qRules = GennyJbpmBaseTest.setupLocalService();
//			userToken = new GennyToken("userToken", qRules.getToken());
//			serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
//
//		}
		String password = System.getenv("SERVICE_PASSWORD");
		String userPassword = System.getenv("USER_PASSWORD");
		String token = null;
		String userId = null;
		try {
			token = KeycloakUtils.getAccessToken("https://keycloak-interns.gada.io", "internmatch", "internmatch",
					"dc7d0960-2e1d-4a78-9eef-77678066dbd3", "service", password);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		LocalDateTime now = LocalDateTime.now();
		String mydatetime = new SimpleDateFormat("yyyyMMddHHmmss").format(now);

///		String username = "rahul.samaranayake+"+mydatetime+"@outcomelife.com.au";		
//		String firstname = "Rahul";
//		String lastname = "Samaranayake";
//
//		String username = "adamcrow63+"+mydatetime+"@gmail.com";		
//		String firstname = "Adam";
//		String lastname = "Crow";

		String username = "gerard.holland+" + mydatetime + "@outcome.life";
		String firstname = "Gerard";
		String lastname = "Holland";

//		String username = "domenic.saporito+"+mydatetime+"@outcome.life";		
//		String firstname = "Domenic";
//		String lastname = "Saporito";

		System.out.println(username + " serviceToken=" + token);

		password = UUID.randomUUID().toString().substring(0, 8);
//		List<LinkedHashMap> results = new ArrayList<LinkedHashMap>();
//	    final HttpClient client = new DefaultHttpClient();
//	    
//	    try {
//	    	String encodedUsername = encodeValue("adamcrow63@gmail.com");
//	      final HttpGet get =
//	          new HttpGet("https://keycloak.gada.io" + "/auth/admin/realms/" + realm + "/users?username=" + encodedUsername);
//	      get.addHeader("Authorization", "Bearer " + token);
//	      try {
//	        final HttpResponse response = client.execute(get);
//	        if (response.getStatusLine().getStatusCode() != 200) {
//	          throw new IOException();
//	        }
//	        final HttpEntity entity = response.getEntity();
//	        final InputStream is = entity.getContent();
//	        try {
//	          results =  JsonSerialization.readValue(is, (new ArrayList<UserRepresentation>()).getClass());
//	        } finally {
//	          is.close();
//	        }
//	      } catch (final IOException e) {
//	        throw new RuntimeException(e);
//	      }
//	    } finally {
//	      client.getConnectionManager().shutdown();
//	    }

		try {
			userId = KeycloakUtils.createUser(token, realm, username, firstname, lastname, username, userPassword,
					"user", "user");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		userId = KeycloakUtils.sendVerifyEmail(realm, username, token);

//		HttpClient httpClient = new DefaultHttpClient();
//
//		HttpPut putRequest = new HttpPut("https://keycloak.gada.io" + "/auth/admin/realms/" + "internmatch" + "/users/" + userId + "/send-verify-email");
//
//		log.info("https://keycloak.gada.io" + "/auth/admin/realms/" + "internmatch" + "/users/" + userId + "/send-verify-email");
//
//		putRequest.addHeader("Content-Type", "application/json");
//		putRequest.addHeader("Authorization", "Bearer " + token);
//
//		HttpResponse response = null;
//		try {
//			response = httpClient.execute(putRequest);
//		} catch (ClientProtocolException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		int statusCode = response.getStatusLine().getStatusCode();
//
		System.out.println("UserId=" + userId);

	}

	private static String encodeValue(String value) {
		try {
			return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex.getCause());
		}
	}

	@Test
	public void searchTest() {
		System.out.println("Search test");
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
			VertxUtils.cachedEnabled = false;
			qRules = GennyJbpmBaseTest.setupLocalService();
			userToken = new GennyToken("userToken", qRules.getToken());
			serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());

		}

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		BaseEntityUtils beUtils = new BaseEntityUtils(userToken);
		beUtils.setServiceToken(serviceToken);

//		SearchEntity searchBE = new SearchEntity("ADAMTEST", "Test Search")
//				.addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
//				.addFilter("LNK_INTERN", SearchEntity.StringFilter.LIKE, "%PER_INTERN3%")
//				.addColumn("PRI_NAME", "Name")
//				.addColumn("LNK_INTERNSHIP","Internship")
//				.addColumn("LNK_INTERN_SUPERVISOR", "Supervisor")
//				.addColumn("LNK_HOST_COMPANY_REP", "Host Company Rep")
//				.addColumn("LNK_HOST_COMPANY", "Host Company")
//				.setPageStart(0)
//				.setPageSize(20);

//		SearchEntity searchBE = new SearchEntity("ADAMTEST", "Intern Apps")
//				.addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
//				.addFilter("LNK_INTERN", SearchEntity.StringFilter.LIKE, "%PER_KANIKA_DOT_GULATI_AT_GADA_DOT_IO").addColumn("PRI_NAME", "Name")
//				.addColumn("LNK_INTERNSHIP", "Internship").addColumn("LNK_INTERN_SUPERVISOR", "Supervisor")
//				.addColumn("LNK_HOST_COMPANY_REP", "Host Company Rep").addColumn("LNK_HOST_COMPANY", "Host Company")
//				.setPageStart(0).setPageSize(100);

//		SearchEntity searchBE = new SearchEntity("ADAMTEST", "Intern Journals")
//				.addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
//				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "JNL_%") 
//				/* .addFilter("PRI_SYNC", SearchEntity.StringFilter.LIKE, "FALSE") */
//				.addFilter("LNK_INTERN", SearchEntity.StringFilter.LIKE, "%"+"PER_KANIKA_DOT_GULATI_PLUS_INTERN_AT_GADA_DOT_IO"+"%") 
//				.addColumn("PRI_NAME", "Name")
//				.addColumn("LNK_INTERNSHIP","Internship")
//				.addColumn("LNK_INTERN", "Intern")
//				.addColumn("LNK_HOST_COMPANY_REP", "Host Company Rep")
//				.addColumn("LNK_HOST_COMPANY", "Host Company")
//				.addColumn("LNK_INTERN_SUPERVISOR", "Supervisor")
//				.addColumn("PRI_JOURNAL_DATE","Date")
//				.addColumn("PRI_JOURNAL_HOURS","Hours")
//				.addColumn("PRI_JOURNAL_TASKS","Tasks")				
//				.addColumn("PRI_JOURNAL_LEARNING_OUTCOMES","Learning Outcomes")
//				.addColumn("PRI_FEEDBACK","Feedback")
//				.addColumn("PRI_STATUS","Status")				
//				.addColumn("PRI_SYNC","Synced")
//				.setPageStart(0)
//				.setPageSize(2000);	

		SearchEntity searchBE = new SearchEntity("SBE_SUPERVISOR_JOOURNALS", "Supervisor Journals")
				.addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "JNL_%")
				/* .addFilter("PRI_SYNC", SearchEntity.StringFilter.LIKE, "FALSE") */
				.addFilter("LNK_INTERN_SUPERVISOR", SearchEntity.StringFilter.LIKE, "%" + "PER_SUPERVISOR1" + "%")
				.addColumn("PRI_NAME", "Name").addColumn("LNK_INTERNSHIP", "Internship")
				.addColumn("LNK_INTERN", "Intern").addColumn("LNK_HOST_COMPANY_REP", "Host Company Rep")
				.addColumn("LNK_HOST_COMPANY", "Host Company").addColumn("LNK_INTERN_SUPERVISOR", "Supervisor")
				.addColumn("PRI_JOURNAL_DATE", "Date").addColumn("PRI_JOURNAL_HOURS", "Hours")
				.addColumn("PRI_JOURNAL_TASKS", "Tasks").addColumn("PRI_JOURNAL_LEARNING_OUTCOMES", "Learning Outcomes")
				.addColumn("PRI_FEEDBACK", "Feedback").addColumn("PRI_STATUS", "Status").addColumn("PRI_SYNC", "Synced")
				.setPageStart(0).setPageSize(2000);

		String jsonSearchBE = JsonUtils.toJson(searchBE);
		String resultJson;
		try {
			resultJson = QwandaUtils.apiPostEntity(GennySettings.qwandaServiceUrl + "/qwanda/baseentitys/search",
					jsonSearchBE, beUtils.getServiceToken().getToken());
			try {
				QDataBaseEntityMessage msg = JsonUtils.fromJson(resultJson, QDataBaseEntityMessage.class);
				BaseEntity[] bes = msg.getItems();
				System.out.println("Number of bes returned is " + bes.length);
				for (BaseEntity be : bes) {
					System.out.println("Be code = " + be.getCode());
				}
			} catch (Exception e) {
				log.info("The result of getSearchResults was null Exception ::  ");
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	// @Test
	public void importGoogleIdTest() {
		System.out.println("Import Google IDTest");

		GennyKieSession gks = null;

		try {
			gks = GennyKieSession.builder(serviceToken, true).addDrl("IS_INTERN.drl").build();

//			gks = GennyKieSession.builder(serviceToken, true).addDrl("SignalProcessing").addDrl("DataProcessing")
//					.addDrl("EventProcessing").addJbpm("Lifecycles").addJbpm("adam_user1.bpmn")
//					.addJbpm("adam_user2.bpmn").addJbpm("adam_user3.bpmn").addDrl("AuthInit").addJbpm("AuthInit")
//					.addDrl("InitialiseProject").addJbpm("InitialiseProject").build();
////
//			gks.createTestUsersGroups();
//
//			GennyToken newUser2A = gks.createToken("PER_USER2", "user,test,admin");
//			GennyToken newUser2B = gks.createToken("PER_USER2");
//			GennyToken newUser1A = gks.createToken("PER_USER1");
//			gks.start();
//
//			gks.injectSignal("initProject"); // This should initialise everything
//			gks.injectEvent("authInitMsg", newUser2A); // log in as new user
//			gks.advanceSeconds(5, false);
//			gks.showStatuses("PER_USER1", "PER_USER2");
//
//			// Now answer a question
//
//			gks.injectAnswer("PRI_FIRSTNAME", newUser2A);
//			gks.injectAnswer("PRI_LASTNAME", newUser2A);
//			gks.injectAnswer("PRI_DOB", newUser2A);
//			gks.injectAnswer("PRI_PREFERRED_NAME", newUser2A);
//			gks.injectAnswer("PRI_EMAIL", newUser2A);
//			gks.injectAnswer("PRI_MOBILE", newUser2A);
//			gks.injectAnswer("PRI_USER_PROFILE_PICTURE", newUser2A);
//			gks.injectAnswer("PRI_ADDRESS_FULL", newUser2A);
//
//			gks.injectEvent("QUE_SUBMIT", newUser2A);
//
//			// Now import a google doc/ xls file and generate a List of BaseEntityImports

			String googleDocId = System.getenv("GOOGLE_DOC_ID");
			googleDocId = googleDocId.trim();
			List<BaseEntityImport> beImports = ImportUtils.importGoogleDoc(googleDocId, "Sheet1", getFieldMappings());

			// now generate the baseentity and send through all the answers
//			BaseEntityUtils beUtils = new BaseEntityUtils(newUser2A);
//			beUtils.setServiceToken(serviceToken);
//			for (BaseEntityImport beImport : beImports) {
//				BaseEntity be = beUtils.create(beImport.getCode(), beImport.getName());
//				List<Answer> answers = new ArrayList<Answer>();
//				for (Tuple2<String, String> attributeCodeValue : beImport.getAttributeValuePairList()) {
//					Answer answer = new Answer(be.getCode(), be.getCode(), attributeCodeValue._1,
//							attributeCodeValue._2);
//					answers.add(answer);
//				}
//
//				QDataAnswerMessage msg = new QDataAnswerMessage(answers);
////				msg.setToken(newUser2A.getToken());
//				// now inject into a rulegroup
////				gks.injectEvent(msg, newUser2A);
//			}

			// System.out.println(beImports);
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			if (gks != null) {
				gks.close();
			}
		}
	}

	public AdamTest() {
		loadServiceConfigurator();
	}

	protected void loadServiceConfigurator() {
		this.serviceConfigurator = ServiceLoader.load(KieServiceConfigurator.class).iterator().next();
	}

	// @Test
	public void queryTest() {
		System.out.println("Process View Test");
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

			GennyToken newUser2A = gks.createToken("PER_USER2");
			GennyToken newUser2B = gks.createToken("PER_USER2");
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

	private void runRules(GennyToken serviceToken, GennyToken userToken) {
		KieSessionConfiguration ksconf = KieServices.Factory.get().newKieSessionConfiguration();
		// ksconf.setOption(TimedRuleExecutionOption.YES);

		KieSession newKieSession = null;

		OutputParam output = new OutputParam();
		Answers answersToSave = new Answers();

		KieBase kieBase = RulesLoader.getKieBaseCache().get(serviceToken.getRealm());
		newKieSession = (StatefulKnowledgeSession) kieBase.newKieSession(ksconf, RulesLoader.env);

//			newKieSession = (StatefulKnowledgeSession)this.runtimeEngine.getKieSession();

		FactHandle outputParamTreeSetHandle = newKieSession.insert(new OutputParamTreeSet());

	}

	public Map<String, String> getFieldMappings2() {
		Map<String, String> fieldMapping = new HashMap<String, String>();
		fieldMapping.put("Batch".toLowerCase(), "PRI_BATCH_NO");
		fieldMapping.put("State".toLowerCase(), "PRI_IMPORT_STATE");
		fieldMapping.put("Student ID".toLowerCase(), "PRI_STUDENT_ID");
		fieldMapping.put("Disp".toLowerCase(), "PRI_IMPORT_DISP");
		fieldMapping.put("First Name".toLowerCase(), "PRI_FIRSTNAME");
		fieldMapping.put("Last Name".toLowerCase(), "PRI_LASTNAME");
		fieldMapping.put("PHONE".toLowerCase(), "PRI_IMPORT_PHONE");
		fieldMapping.put("EMAIL".toLowerCase(), "PRI_EMAIL");
		fieldMapping.put("TARGET START DATE".toLowerCase(), "PRI_TARGET_START_DATE");
		fieldMapping.put("ADDRESS".toLowerCase(), "PRI_IMPORT_ADDRESS");
		fieldMapping.put("SUBURB".toLowerCase(), "PRI_IMPORT_SUBURB");
		fieldMapping.put("Postcode".toLowerCase(), "PRI_IMPORT_POSTCODE");

		return fieldMapping;

	}

	public Map<String, String> getFieldMappings() {

		Map<String, String> fieldMapping = new HashMap<String, String>();
		fieldMapping.put("Education Provider".toLowerCase(), "PRI_ASSOC_EDU_PROV");
		fieldMapping.put("Student ID".toLowerCase(), "PRI_STUDENT_ID");
		fieldMapping.put("Student First Name".toLowerCase(), "PRI_IMPORT_FIRSTNAME");
		fieldMapping.put("Last Name".toLowerCase(), "PRI_IMPORT_LASTNAME");
		fieldMapping.put("Student Email".toLowerCase(), "PRI_EMAIL");
		fieldMapping.put("Industry".toLowerCase(), "PRI_INDUSTRY");
		fieldMapping.put("Host Company".toLowerCase(), "PRI_ASSOC_HOST_COMPANY");
		fieldMapping.put("Host Company Rep".toLowerCase(), "PRI_ASSOC_HCR");
		fieldMapping.put("Host Company Email".toLowerCase(), "PRI_ASSOC_HOST_COMPANY_EMAIL");
		return fieldMapping;

	}

//		public Integer importGoogleDoc(final String id, Map<String,String> fieldMapping)
//		{
//
//			log.info("Importing "+id);
//			Integer count = 0;
//			   try {
//				   GoogleImportService gs = GoogleImportService.getInstance();
//				    XlsxImport xlsImport = new XlsxImportOnline(gs.getService());
//			//	    Realm realm = new Realm(xlsImport,id);
////				    realm.getDataUnits().stream()
////				        .forEach(data -> System.out.println(data.questions.size()));
//				    Set<String> keys = new HashSet<String>();
//				    for (String field : fieldMapping.keySet()) {
//				    	keys.add(field);
//				    }
//				      Map<String, Map<String,String>> mapData = xlsImport.mappingRawToHeaderAndValuesFmt(id, "Sheet1", keys);
//				      Integer rowIndex = 0;
//				      for (Map<String,String> row : mapData.values())
//				      {
//				    	  String rowStr = "Row:"+rowIndex+"->";
//				    	  for (String col : row.keySet()) {
//				    		  String val = row.get(col.trim());
//				    		  if (val!=null) {
//				    			  val = val.trim();
//				    		  }
//				    		  String attributeCode = fieldMapping.get(col);
//				    		  rowStr += attributeCode+"="+val + ",";
//				    	  }
//				    	  rowIndex++;
//				    	  System.out.println(rowStr);
//				      }
//
//				    } catch (Exception e1) {
//				      return 0;
//				    }
//
//
//			return count;
//		}
//

	// @Test
	public void generateCapabilitiesTest() {
		System.out.println("GenerateCapabilities Test");

		GennyKieSession gks = null;

		try {
			gks = GennyKieSession.builder(serviceToken, true).addDrl("AuthInit").addJbpm("AuthInit")
					.addDrl("InitialiseProject").addJbpm("InitialiseProject").addDrl("GenerateCapabilities").build();

			gks.start();

			gks.injectSignal("initProject"); // This should initialise everything

		} catch (Exception e) {

		} finally {
			if (gks != null) {
				gks.close();
			}
		}
	}

	// @Test
	public void addEduProvTest() {
		System.out.println("AddEduProv Test");

		GennyKieSession gks = null;

		try {
			gks = GennyKieSession.builder(serviceToken, true).addDrl("SignalProcessing").addDrl("DataProcessing")
					.addDrl("EventProcessing").addJbpm("Lifecycles").addJbpm("adam_user1.bpmn")
					.addJbpm("adam_user2.bpmn").addJbpm("adam_user3.bpmn").addDrl("AuthInit").addJbpm("AuthInit")
					.addDrl("InitialiseProject").addJbpm("InitialiseProject").build();

			gks.createTestUsersGroups();

			GennyToken newUser2A = gks.createToken("PER_USER2");
			GennyToken newUser2B = gks.createToken("PER_USER2");
			GennyToken newUser1A = gks.createToken("PER_USER1");
			gks.start();

			gks.injectSignal("initProject"); // This should initialise everything
			gks.injectEvent("authInitMsg", newUser2A); // log in as new user
			gks.advanceSeconds(5, false);
			gks.showStatuses("PER_USER1", "PER_USER2");

			// Now answer a question

			gks.injectAnswer("PRI_FIRSTNAME", newUser2A);
			gks.injectAnswer("PRI_LASTNAME", newUser2A);
			gks.injectAnswer("PRI_DOB", newUser2A);
			gks.injectAnswer("PRI_PREFERRED_NAME", newUser2A);
			gks.injectAnswer("PRI_EMAIL", newUser2A);
			gks.injectAnswer("PRI_MOBILE", newUser2A);
			gks.injectAnswer("PRI_USER_PROFILE_PICTURE", newUser2A);
			gks.injectAnswer("PRI_ADDRESS_FULL", newUser2A);

			gks.injectEvent("QUE_SUBMIT", newUser2A);

			// Now add an Edu Provider

			gks.injectEvent("msgLogout", newUser2A);
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

//	@Test
	public void askQuestionTest() {
		System.out.println("AskQuestion Test");

		GennyKieSession gks = null;

		try {
			gks = GennyKieSession.builder(serviceToken, true).addDrl("SignalProcessing").addDrl("DataProcessing")
					.addDrl("EventProcessing").addJbpm("Lifecycles").addJbpm("adam_user1.bpmn")
					.addJbpm("adam_user2.bpmn").addJbpm("adam_user3.bpmn").addDrl("AuthInit").addJbpm("AuthInit")
					.addDrl("InitialiseProject").addJbpm("InitialiseProject").build();

			gks.createTestUsersGroups();

			GennyToken newUser2A = gks.createToken("PER_USER2");
			GennyToken newUser2B = gks.createToken("PER_USER2");
			GennyToken newUser1A = gks.createToken("PER_USER1");
			gks.start();

			gks.injectSignal("initProject"); // This should initialise everything
			gks.injectEvent("authInitMsg", newUser2A); // log in as new user
			// gks.injectEvent("authInitMsg",newUser2B); // log in as same new user
			// gks.injectEvent("authInitMsg",newUser1A); // log in as same new user
			gks.advanceSeconds(5, false);
			gks.showStatuses("PER_USER1", "PER_USER2");

//				System.out.println("Invoking AskQuestionTask workItem");
			// Send an AskQuestion that should send an internal signal to the userSession
//				AskQuestionTaskWorkItemHandler askQ = new AskQuestionTaskWorkItemHandler(GennyKieSession.class,gks.getGennyRuntimeEngine(),gks.getKieSession());
//				WorkItemManager workItemManager = gks.getKieSession().getWorkItemManager();
//				WorkItemImpl workItem = new WorkItICEemImpl();
//		        workItem.setParameter("userToken",
//		                              userToken);
//		        workItem.setParameter("questionCode",
//		                              "QUE_USER_PROFILE_GRP");
//		        workItem.setParameter("callingWorkflow", "AdamTest");
//		        workItem.setParameter("Priority", "10");  // if l;eft out defaults to 0
//		        workItem.setDeploymentId(userToken.getRealm());
//		        workItem.setName("AskQuestion");
//		        workItem.setProcessInstanceId(1234L); // made up processId
//		        askQ.executeWorkItem(workItem, workItemManager);

//		        showStatuses(gks);

			// Now answer a question

			gks.injectAnswer("PRI_FIRSTNAME", newUser2A);
			gks.injectAnswer("PRI_LASTNAME", newUser2A);
			gks.injectAnswer("PRI_DOB", newUser2A);
			gks.injectAnswer("PRI_PREFERRED_NAME", newUser2A);
			gks.injectAnswer("PRI_EMAIL", newUser2A);
			gks.injectAnswer("PRI_MOBILE", newUser2A);
			gks.injectAnswer("PRI_USER_PROFILE_PICTURE", newUser2A);
			gks.injectAnswer("PRI_ADDRESS_FULL", newUser2A);

			gks.injectEvent("QUE_SUBMIT", newUser2A);

			gks.injectEvent("msgLogout", newUser2A);
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

	// @Test
	public void userTaskTest() {
		System.out.println("UserTask Test");
		GennyToken userToken = null;
		QRules qRules = null;

		if (true) {
			userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
			serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "PER_SERVICE", "Service User", "service");
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
		VertxUtils.writeCachedJson(GennySettings.GENNY_REALM, "TOKEN" + realm.toUpperCase(), serviceToken.getToken());
		JsonObject tokenObj = VertxUtils.readCachedJson(GennySettings.GENNY_REALM, "TOKEN" + realm.toUpperCase());
		String token = tokenObj.getString("value");

		GennyKieSession gks = null;

		try {
			gks = GennyKieSession.builder(serviceToken, true)
					// .addDrl("SignalProcessing")
					// .addDrl("DataProcessing")
					// .addDrl("EventProcessing")
					// .addJbpm("Lifecycles")
					.addJbpm("adam_user1.bpmn").addJbpm("adam_user2.bpmn")
					// .addDrl("AuthInit")
					// .addJbpm("AuthInit")
					// .addDrl("InitialiseProject")
					// .addJbpm("InitialiseProject")

					.addToken(userToken).build();

			gks.createTestUsersGroups();

			gks.start();

			BaseEntity icn_sort = new BaseEntity("ICN_SORT", "Icon Sort");
			try {

				icn_sort.addAttribute(RulesUtils.getAttribute("PRI_ICON_CODE", serviceToken.getToken()), 1.0, "sort");
				icn_sort.setRealm(realm);
				VertxUtils.writeCachedJson(realm, "ICN_SORT", JsonUtils.toJson(icn_sort), serviceToken.getToken());

			} catch (BadDataException e1) {
				e1.printStackTrace();
			}

			// gks.injectSignal("initProject", initFacts); // This should initialise
			// everything
			gks.advanceSeconds(5, false);

			gks.getKieSession().getWorkItemManager().registerWorkItemHandler("Human Task",
					new NonManagedLocalHTWorkItemHandler(gks.getKieSession(), gks.getTaskService()));
			List<TaskSummary> tasks = null;

			// Start a process
			gks.startProcess("adam_user2");

			Task tasky = gks.getTaskService().getTaskById(1L);
			System.out.println(tasky);
			System.out.println("Formname: " + tasky.getFormName());
			System.out.println("Description: " + tasky.getDescription());
			System.out.println("People Assignments: " + tasky.getPeopleAssignments().getPotentialOwners());
			gks.showStatuses();

			gks.advanceSeconds(5, false);
			Map<String, Object> params = new HashMap<String, Object>();
			Task task = new TaskFluent().setName("Amazing GADA Stuff")
					.setAdminUser(realm + "+PER_ADAMCROW63_AT_GMAIL_COM").setAdminGroup("Administrators")
					.addPotentialGroup(realm + "+GRP_GADA").setAdminUser(realm + "+PER_ADAMCROW63_AT_GMAIL_COM")
					// .addPotentialUser("acrow")
					.setProcessId("direct").setDeploymentID(realm).getTask();

			Task task2 = new TaskFluent().setName("Awesome GADA stuff")
					// .addPotentialGroup("GADA")
					.setAdminUser(realm + "+PER_ADAMCROW63_AT_GMAIL_COM").setAdminGroup("Administrators")
					.addPotentialUser(realm + "+PER_DOMENIC_AT_OUTCOME_LIFE").setDeploymentID(realm)
					.setProcessId("direct").getTask();

			Task task3 = new TaskFluent().setName("Boring Outcome Stuff").addPotentialGroup(realm + "+GRP_OUTCOME")
					.setAdminUser(realm + "+PER_ADAMCROW63_AT_GMAIL_COM").setAdminGroup("Administrators")
					.addPotentialUser(realm + "+PER_GERARD_AT_OUTCOME_LIFE").setProcessId("direct")
					.setDeploymentID(realm).getTask();

			gks.getTaskService().addTask(task, params);
			gks.getTaskService().addTask(task2, params);
			gks.getTaskService().addTask(task3, params);
			long taskId = task.getId();
			long taskId2 = task2.getId();
			long taskId3 = task3.getId();

			// Do Task Operations

			gks.showStatuses();

			// Add Comment
			User JbpmUser = (User) TaskModelProvider.getFactory().newUser(realm + "+PER_ADAMCROW63_AT_GMAIL_COM");
			InternalComment commentImpl = (InternalComment) TaskModelProvider.getFactory().newComment();
			commentImpl.setAddedAt(new Date());
			commentImpl.setAddedBy(JbpmUser);
			gks.getTaskService().addComment(taskId2, commentImpl);

			Map<String, Object> content = gks.getTaskService().getTaskContent(taskId2);
			System.out.println(content);
			// Start Task
			gks.getTaskService().start(taskId, realm + "+PER_ADAMCROW63_AT_GMAIL_COM");
			gks.showStatuses();

			gks.getTaskService().suspend(taskId, realm + "+PER_ADAMCROW63_AT_GMAIL_COM");
			gks.showStatuses();

			gks.getTaskService().resume(taskId, realm + "+PER_ADAMCROW63_AT_GMAIL_COM");
			gks.showStatuses();

			gks.getTaskService().forward(taskId2, realm + "+PER_DOMENIC_AT_OUTCOME_LIFE",
					realm + "+PER_ANISH_AT_GADA_IO");

			// Claim Task
//	              gks.getTaskService().claim(taskId, "acrow");
//	              showStatuses(gks);
//
			Map<String, Object> results = new HashMap<String, Object>();
			results.put("Result", "Done");
			gks.getTaskService().complete(taskId, realm + "+PER_ADAMCROW63_AT_GMAIL_COM", results);
			gks.showStatuses();

			results.put("Result", "some document data");

//	              long processInstanceId =
//	            		  processService.startProcess(deployUnit.getIdentifier(), "org.jbpm.writedocument");
//	            		  List<Long> taskIds =
//	            		  runtimeDataService.getTasksByProcessInstanceId(processInstanceId);
//	            		  Long taskId4 = taskIds.get(0);
//	            		  userTaskService.start(taskId, "john");
//	            		  UserTaskInstanceDesc task4 = runtimeDataService.getTaskById(taskId4);
//	            		  Map<String, Object> results = new HashMap<String, Object>();
//	            		  results.put("Result", "some document data");
//	            		  userTaskService.complete(taskId4, "john", results);

			gks.injectEvent(authInitMsg); // This should create a new process
			gks.advanceSeconds(5, false);

			BaseEntity user = VertxUtils.getObject(serviceToken.getRealm(), "", userToken.getUserCode(),
					BaseEntity.class, serviceToken.getToken());

			// Ok, let's close the PER_USER1 task and see if the workflow continues....
			System.out.println("CLosing adam_user2 task");
			// first claim
			List<TaskSummary> per_user1_tasks = gks.getTaskService()
					.getTasksAssignedAsPotentialOwner(realm + "+PER_USER1", null);
			TaskSummary taskSummary = per_user1_tasks.get(0);
			gks.getTaskService().claim(taskSummary.getId(), realm + "+PER_USER1");
			System.out.println("PER_USER1 CLAIMED "
					+ showTaskNames(gks.getTaskService().getTasksAssignedAsPotentialOwner(realm + "+PER_USER1", null)));
			gks.getTaskService().start(taskSummary.getId(), realm + "+PER_USER1");
			System.out.println("PER_USER1 STARTED "
					+ showTaskNames(gks.getTaskService().getTasksAssignedAsPotentialOwner(realm + "+PER_USER1", null)));
			gks.getTaskService().suspend(taskSummary.getId(), realm + "+PER_USER1");
			System.out.println("PER_USER1 SUSPENDED "
					+ showTaskNames(gks.getTaskService().getTasksAssignedAsPotentialOwner(realm + "+PER_USER1", null)));
			gks.getTaskService().resume(taskSummary.getId(), realm + "+PER_USER1");
			System.out.println("PER_USER1 RESUMED "
					+ showTaskNames(gks.getTaskService().getTasksAssignedAsPotentialOwner(realm + "+PER_USER1", null)));

			Map<String, Object> results2 = new HashMap<String, Object>();
			results2.put("Status", "good");
			gks.getTaskService().complete(taskSummary.getId(), realm + "+PER_USER1", results2);
			System.out.println("PER_USER1 COMPLETED  "
					+ showTaskNames(gks.getTaskService().getTasksAssignedAsPotentialOwner(realm + "+PER_USER1", null))
					+ ":results sent=" + results2);

			gks.injectEvent(msgLogout);
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			if (gks != null) {
				gks.close();
			}
		}
	}

//
//
//		private void showStatuses(GennyKieSession gks)
//		{
//				statuses = new ArrayList<Status>();
//		        statuses.add(Status.Ready);
//		        statuses.add(Status.Completed);
//		        statuses.add(Status.Created);
//		        statuses.add(Status.Error);
//		        statuses.add(Status.Exited);
//		        statuses.add(Status.InProgress);
//		        statuses.add(Status.Obsolete);
//		        statuses.add(Status.Reserved);
//		        statuses.add(Status.Suspended);
//
//	            List<String> groups = new ArrayList<String>();
//	            groups.add(realm+"+GRP_GADA");
//
//
//	        System.out.println("POTENTIAL PER_USER1  "+showTaskNames(gks.getTaskService().getTasksAssignedAsPotentialOwnerByStatus(realm+"+PER_USER1", statuses, null)));
//            System.out.println("POTENTIAL acrow      "+showTaskNames(gks.getTaskService().getTasksAssignedAsPotentialOwnerByStatus(realm+"+PER_ADAMCROW63_AT_GMAIL_COM", statuses, null)));
//            System.out.println("POTENTIAL dom        "+showTaskNames(gks.getTaskService().getTasksAssignedAsPotentialOwner(realm+"+PER_DOMENIC_AT_OUTCOME_LIFE", null)));
//            System.out.println("POTENTIAL gerard     "+showTaskNames(gks.getTaskService().getTasksAssignedAsPotentialOwner(realm+"+PER_GERARD_AT_OUTCOME_LIFE",  null)));
//            System.out.println("POTENTIAL chris      "+showTaskNames(gks.getTaskService().getTasksAssignedAsPotentialOwner(realm+"+PER_CHRIS_AT_GADA_IO", null)));
//            System.out.println("POTENTIAL anish      "+showTaskNames(gks.getTaskService().getTasksAssignedAsPotentialOwner(realm+"+PER_ANISH_AT_GADA_IO",  null)));
//            System.out.println("POTENTIAL chris+gada "+showTaskNames(gks.getTaskService().getTasksAssignedAsPotentialOwner(realm+"+PER_CHRIS_AT_GADA_IO", groups, "en-AU", 0,10)));
//            System.out.println("POTENTIAL gada       "+showTaskNames(gks.getTaskService().getTasksAssignedAsPotentialOwner(null, groups, "en-AU", 0,10)));
//
//            System.out.println("OWNED acrow          "+showTaskNames(gks.getTaskService().getTasksOwned(realm+"+PER_ADAMCROW63_AT_GMAIL_COM", null)));
//            System.out.println();
//		}

	@Test
	public void headerTest() {
		System.out.println("Header test");
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
			qRules = GennyJbpmBaseTest.setupLocalService();
			userToken = new GennyToken("userToken", qRules.getToken());
			serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());

		}

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		BaseEntityUtils beUtils = new BaseEntityUtils(userToken);
		beUtils.setServiceToken(serviceToken);

//		  ShowFrame.display(userToken, "FRM_TABLE_VIEW", "FRM_CONTENT", "Test");
//		  String searchCode = "SBE_SEARCH_TEST";
//
//		  Answer answer = new Answer(userToken.getUserCode(),userToken.getUserCode(),"PRI_SEARCH_TEXT","univ");
//
//
//   		  SearchEntity searchBE = new SearchEntity(searchCode,"Test Search")
//   		  	     .addSort("PRI_NAME","Created",SearchEntity.Sort.ASC)
//   		  	     .addFilter("PRI_NAME",SearchEntity.StringFilter.LIKE,"%"+answer.getValue()+"%")
//   		  	     .addColumn("PRI_NAME", "Name")
//   		      	 .addColumn("PRI_LANDLINE", "Phone")
//   		  	     .addColumn("PRI_EMAIL", "Email")
//   		  	     .addColumn("PRI_ADDRESS_CITY","City")
//   		  	     .addColumn("PRI_ADDRESS_STATE","State")
//   		  	     .setPageStart(0)
//   		  	     .setPageSize(20);
//
//   		VertxUtils.putObject(userToken.getRealm(), "", searchCode, searchBE,
//				userToken.getToken());
//
//   		TableUtils.performSearch(serviceToken , beUtils, searchCode, answer);
//

		BaseEntityUtils beUtils2 = new BaseEntityUtils(userToken);

		/* get current search */
//		SearchEntity searchBE2 = TableUtils.getSessionSearch("SBE_SEARCHBAR",userToken);
//
//
//		System.out.println("NEXT for "+searchBE2.getCode());
//
//		Integer pageIndex = searchBE2.getValue("SCH_PAGE_START",0);
//		Integer pageSize = searchBE2.getValue("SCH_PAGE_SIZE", GennySettings.defaultPageSize);
//		pageIndex = pageIndex + pageSize;
//
//		Integer pageNumber = 1;
//
//		if(pageIndex != 0){
//			pageNumber = pageIndex / pageSize;
//		}
//
//		Answer pageAnswer = new Answer(userToken.getUserCode(),searchBE2.getCode(), "SCH_PAGE_START", pageIndex+"");
//		Answer pageNumberAnswer = new Answer(userToken.getUserCode(),searchBE2.getCode(), "PRI_INDEX", pageNumber+"");
//
//		searchBE2 = beUtils2.updateBaseEntity(searchBE2, pageAnswer,SearchEntity.class);
//		searchBE2 = beUtils2.updateBaseEntity(searchBE2, pageNumberAnswer,SearchEntity.class);
//
//		VertxUtils.putObject(beUtils2.getGennyToken().getRealm(), "", searchBE2.getCode(), searchBE2,
//			beUtils2.getGennyToken().getToken());
//
//
//        ShowFrame.display(userToken, "FRM_TABLE_VIEW", "FRM_CONTENT", "Test");
//		TableUtils.performSearch(userToken , beUtils2, "SBE_SEARCHBAR", null);

		/* get current search */
		TableUtils tableUtils = new TableUtils(beUtils2);
		SearchEntity searchBE = tableUtils.getSessionSearch("SBE_SEARCHBAR");

		System.out.println("PREV for " + searchBE.getCode());

		Integer pageIndex = searchBE.getValue("SCH_PAGE_START", 0);
		Integer pageSize = searchBE.getValue("SCH_PAGE_SIZE", GennySettings.defaultPageSize); // TODO, don't let this be
																								// 0
		pageIndex = pageIndex - pageSize;

		if (pageIndex < 0) {
			pageIndex = 0;
		}

		Integer pageNumber = (pageIndex / pageSize) + 1;

		Answer pageAnswer = new Answer(beUtils.getGennyToken().getUserCode(), searchBE.getCode(), "SCH_PAGE_START",
				pageIndex + "");
		Answer pageNumberAnswer = new Answer(beUtils.getGennyToken().getUserCode(), searchBE.getCode(), "PRI_INDEX",
				pageNumber + "");

		searchBE = beUtils.updateBaseEntity(searchBE, pageAnswer, SearchEntity.class);
		searchBE = beUtils.updateBaseEntity(searchBE, pageNumberAnswer, SearchEntity.class);

		VertxUtils.putObject(beUtils.getGennyToken().getRealm(), "", searchBE.getCode(), searchBE,
				beUtils.getGennyToken().getToken());

		ShowFrame.display(userToken, "FRM_TABLE_VIEW", "FRM_CONTENT", "Test");
		// tableUtils.performSearch(userToken, "SBE_SEARCHBAR", null);

	}

	// @Test
	public void paginationTest() {
		System.out.println("Pagination Test");
		GennyToken userToken = null;
		GennyToken userToken2 = null;
		GennyToken serviceToken = null;
		QRules qRules = null;

		if (false) {
			userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
			userToken2 = GennyJbpmBaseTest.createGennyToken(realm, "user2", "Barry2 Allan2", "user");
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

		QEventMessage initMsg = new QEventMessage("EVT_MSG", "INIT_STARTUP");

		QEventMessage authInitMsg1 = new QEventMessage("EVT_MSG", "AUTH_INIT");
		authInitMsg1.setToken(userToken.getToken());
		// QEventMessage authInitMsg2 = new QEventMessage("EVT_MSG",
		// "AUTH_INIT");authInitMsg2.setToken(userToken2.getToken());
		QEventMessage msg1 = new QEventMessage("EVT_MSG", "INIT_1");

		/* table next btn event */

		MessageData data = new MessageData("QUE_TABLE_NEXT_BTN");
		data.setParentCode("QUE_TABLE_FOOTER_GRP");
		data.setCode("QUE_TABLE_FOOTER_GRP");

		QEventMessage nextEvtMsg = new QEventMessage("EVT_MSG", "BTN_CLICK");
		nextEvtMsg.setToken(userToken.getToken());
		nextEvtMsg.setData(data);

		QEventMessage msgLogout1 = new QEventMessage("EVT_MSG", "LOGOUT");
		msgLogout1.setToken(userToken.getToken());
		// QEventMessage msgLogout2 = new QEventMessage("EVT_MSG",
		// "LOGOUT");msgLogout2.setToken(userToken2.getToken());

		List<Answer> answers = new ArrayList<Answer>();
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_FIRSTNAME", "Bruce"));
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_LASTNAME", "Wayne"));
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_ADDRESS_JSON",
				// "{\"street_number\":\"64\",\"street_name\":\"Fakenham
				// Road\",\"suburb\":\"Ashburton\",\"state\":\"Victoria\",\"country\":\"AU\",\"postal_code\":\"3147\",\"full_address\":\"64
				// Fakenham Rd, Ashburton VIC 3147, Australia\",\"street_address\":\"64 Fakenham
				// Road\"}"));
				"{\"street_number\":\"64\",\"street_name\":\"Fakenham Road\",\"suburb\":\"Ashburton\",\"state\":\"Victoria\",\"country\":\"AU\",\"postal_code\":\"3147\",\"full_address\":\"64 Fakenham Rd, Ashburton VIC 3147, Australia\",\"latitude\":-37.863208,\"longitude\":145.092359,\"street_address\":\"64 Fakenham Road\"}"));

		QDataAnswerMessage answerMsg = new QDataAnswerMessage(answers.toArray(new Answer[0]));
		answerMsg.setToken(userToken.getToken());

		Answer searchBarAnswer = new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_SEARCH_TEXT2",
				"univ");
		QDataAnswerMessage searchMsg = new QDataAnswerMessage(searchBarAnswer);
		searchMsg.setToken(userToken.getToken());

		// NOW SET UP Some baseentitys
		BaseEntity project = new BaseEntity("PRJ_" + serviceToken.getRealm().toUpperCase(),
				WordUtils.capitalizeFully(serviceToken.getRealm()));
		project.setRealm(serviceToken.getRealm());
		VertxUtils.writeCachedJson(serviceToken.getRealm(), "PRJ_" + serviceToken.getRealm().toUpperCase(),
				JsonUtils.toJson(project), serviceToken.getToken());
		VertxUtils.writeCachedJson(realm, ":" + "PRJ_" + serviceToken.getRealm().toUpperCase(),
				JsonUtils.toJson(project), serviceToken.getToken());
		BaseEntity project2 = VertxUtils.getObject(serviceToken.getRealm(), "",
				"PRJ_" + serviceToken.getRealm().toUpperCase(), BaseEntity.class, serviceToken.getToken());

		GennyKieSession gks = null;

		try {
			gks = GennyKieSession.builder(serviceToken, true).addDrl("SignalProcessing").addDrl("DataProcessing")
					.addDrl("EventProcessing")
					// .addDrl("InitialiseProject")
					// .addDrl("XXXPRI_SEARCH_TEXT2.drl")
					.addDrl("QUE_TABLE_NEXT_BTN.drl")
					// .addDrl("XXXQUE_TABLE_NEXT_BTN.drl")
					// .addJbpm("InitialiseProject")
					.addJbpm("Lifecycles").addDrl("AuthInit").addJbpm("AuthInit")

					.addToken(userToken).build();
			gks.start();

			BaseEntity icn_sort = new BaseEntity("ICN_SORT", "Icon Sort");
			try {

				icn_sort.addAttribute(RulesUtils.getAttribute("PRI_ICON_CODE", serviceToken.getToken()), 1.0, "sort");
				icn_sort.setRealm(realm);
				VertxUtils.writeCachedJson(realm, "ICN_SORT", JsonUtils.toJson(icn_sort), serviceToken.getToken());

			} catch (BadDataException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			gks.injectEvent(initMsg); // This should create a new process

			gks.injectEvent(authInitMsg1); // This should create a new process
			gks.advanceSeconds(5, false);
			gks.injectEvent(authInitMsg1); // This should attach to existing process
			gks.advanceSeconds(5, false);

			gks.injectEvent(answerMsg); // This sends an answer to the first userSessio
			gks.advanceSeconds(5, false);

			BaseEntity user = VertxUtils.getObject(serviceToken.getRealm(), "", userToken.getUserCode(),
					BaseEntity.class, serviceToken.getToken());

			gks.injectEvent(searchMsg); // This sends a search bar request

			QEventMessage pageNextMsg = new QEventMessage("EVT_MSG", "QUE_TABLE_NEXT_BTN");
			pageNextMsg.setToken(userToken.getToken());
			QEventMessage pagePrevMsg = new QEventMessage("EVT_MSG", "QUE_TABLE_PREV_BTN");
			pagePrevMsg.setToken(userToken.getToken());
			gks.injectEvent(pageNextMsg); // This sends a page Next request
			// gks.injectEvent(pageNextMsg); // This sends a page Next request
			// gks.injectEvent(pagePrevMsg); // This sends a page Prev request

			gks.injectEvent(msgLogout1);
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			if (gks != null) {
				gks.close();
			}
		}
	}

//@Test
	public void testTableHeader() {
		System.out.println("Table test");
		GennyToken userToken = null;
		GennyToken userToken2 = null;
		GennyToken serviceToken = null;
		QRules qRules = null;

		if (false) {
			userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
			userToken2 = GennyJbpmBaseTest.createGennyToken(realm, "user2", "Barry2 Allan2", "user");
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
			GennyKieSession.loadAttributesJsonFromResources(userToken);

		}

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		BaseEntityUtils beUtils = new BaseEntityUtils(userToken);
		BaseEntity project = beUtils.getBaseEntityByCode("PRJ_" + serviceToken.getRealm().toUpperCase());

		GennyKieSession gks = null;

		try {
			gks = GennyKieSession.builder(serviceToken, true).addDrl("SignalProcessing").addDrl("DataProcessing")
					.addDrl("EventProcessing").addDrl("InitialiseProject").addDrl("XXXPRI_SEARCH_TEXT2.drl")
					.addJbpm("InitialiseProject").addJbpm("Lifecycles").addDrl("AuthInit").addJbpm("AuthInit")

					.addToken(userToken).build();
			gks.start();

			String searchBarString = "Adam";

			SearchEntity searchBE = new SearchEntity("SBE_SEARCH", "Search")
					.addSort("PRI_NAME", "Name", SearchEntity.Sort.ASC)
					.addFilter("PRI_NAME", SearchEntity.StringFilter.LIKE, "%" + searchBarString + "%")
					.addColumn("PRI_NAME", "Name").addColumn("PRI_LANDLINE", "Phone").addColumn("PRI_EMAIL", "Email")
					.addColumn("PRI_MOBILE", "Mobile").addColumn("PRI_ADDRESS_CITY", "City")
					.addColumn("PRI_ADDRESS_STATE", "State").setPageStart(0).setPageSize(10);

			searchBE.setRealm(serviceToken.getRealm());

			VertxUtils.putObject(serviceToken.getRealm(), "", searchBE.getCode(), searchBE, serviceToken.getToken());

			Answer answer = new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_SEARCH_TEXT",
					searchBarString);
			TableUtils tableUtils = new TableUtils(beUtils);
			// tableUtils.performSearch(serviceToken, "SBE_SEARCHBAR", answer);

			/* Send to front end */

			GennyKieSession.displayForm("FRM_TABLE_VIEW", "FRM_CONTENT", userToken);
			System.out.println("Sent");
		} catch (Exception e) {
			System.out.println("Error " + e.getLocalizedMessage());
		}
	}

	public void tableTest() {
		System.out.println("Table test");
		GennyToken userToken = null;
		GennyToken userToken2 = null;
		GennyToken serviceToken = null;
		QRules qRules = null;

		if (false) {
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
			GennyKieSession.loadAttributesJsonFromResources(userToken);
		}

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		/* Look up Search */
		SearchEntity searchBE = new SearchEntity("SBE_SEARCH", "Search")
				.addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC)
				.addFilter("PRI_NAME", SearchEntity.StringFilter.LIKE, "%univ%").addColumn("PRI_NAME", "Name")
				.addColumn("PRI_LANDLINE", "Phone").setPageStart(0).setPageSize(10);

		Frame3 headerFrame = null;
		try {

			Validation tableCellValidation = new Validation("VLD_ANYTHING", "Anything", ".*");

			List<Validation> tableCellValidations = new ArrayList<>();
			tableCellValidations.add(tableCellValidation);

			ValidationList tableCellValidationList = new ValidationList();
			tableCellValidationList.setValidationList(tableCellValidations);

			DataType tableCellDataType = new DataType("DTT_TABLE_CELL_GRP", tableCellValidationList, "Table Cell Group",
					"");

			headerFrame = Frame3.builder("FRM_TABLE_HEADER").addTheme("THM_TABLE_BORDER", serviceToken).end()
					.question("QUE_NAME_GRP").addTheme("THM_QUESTION_GRP_LABEL", serviceToken)
					.vcl(VisualControlType.GROUP).dataType(tableCellDataType).end()
					.addTheme("THM_DISPLAY_HORIZONTAL", serviceToken).weight(2.0).end()
					.addTheme("THM_TABLE_HEADER_CELL_WRAPPER", serviceToken).vcl(VisualControlType.VCL_WRAPPER).end()
					.addTheme("THM_TABLE_HEADER_CELL_GROUP_LABEL", serviceToken).vcl(VisualControlType.GROUP_LABEL)
					.end().addTheme("THM_DISPLAY_VERTICAL", serviceToken).dataType(tableCellDataType).weight(1.0).end()
					.end().build();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		headerFrame.setRealm(serviceToken.getRealm());
		FrameUtils2.toMessage(headerFrame, serviceToken);

		BaseEntityUtils beUtils = new BaseEntityUtils(userToken);

		/* frame-root */
		Frame3 FRM_ROOT = null;
		try {
			Frame3 FRM_HEADER = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_HEADER", Frame3.class,
					serviceToken.getToken());// generateHeader();
			Frame3 FRM_SIDEBAR = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_SIDEBAR", Frame3.class,
					serviceToken.getToken());// generateHeader();
			Frame3 FRM_CONTENT = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_CONTENT", Frame3.class,
					serviceToken.getToken());// generateHeader();
			Frame3 FRM_FOOTER = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_FOOTER", Frame3.class,
					serviceToken.getToken());// generateHeader();
			Frame3 FRM_APP = Frame3.builder("FRM_APP").addTheme("THM_PROJECT", ThemePosition.FRAME, serviceToken).end()
					.addFrame(FRM_HEADER, FramePosition.NORTH).end().addFrame(FRM_SIDEBAR, FramePosition.WEST).end()
					/* .addFrame(FRM_TABS, FramePosition.CENTRE).end() */
					.addFrame(FRM_CONTENT, FramePosition.CENTRE).end().addFrame(FRM_FOOTER, FramePosition.SOUTH).end()
					.build();
			FRM_ROOT = Frame3.builder("FRM_ROOT").addFrame(FRM_APP, FramePosition.CENTRE).end().build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();
		QDataBaseEntityMessage msg = FrameUtils2.toMessage(FRM_ROOT, serviceToken, askMsgs);
		msg.setToken(userToken.getToken());
		// qRules.publishCmd(msg);
		VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg));
//  for (QDataAskMessage askMsg : askMsgs) {
//          rules.publishCmd(askMsg, serviceToken.getUserCode(), userToken.getUserCode());
//  }

		// Test sending a page
		QDataBaseEntityMessage msg2 = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_QUE_DASHBOARD_VIEW_MSG",
				QDataBaseEntityMessage.class, serviceToken.getToken());

		msg2.setToken(userToken.getToken());
		/* send message */
		// rules.publishCmd(msg2); // Send QDataBaseEntityMessage
		VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg2));

		TableUtils tableUtils = new TableUtils(beUtils);

		QDataBaseEntityMessage msg4 = tableUtils.fetchSearchResults(searchBE);
		TableData tableData = tableUtils.generateTableAsks(searchBE);
		log.info(tableData);

		// "FRM_QUE_DASHBOARD_VIEW","FRM_CONTENT"
		Set<Ask> asksSet = new HashSet<Ask>();
		asksSet.add(tableData.getAsk());
		Ask[] askArray = asksSet.stream().toArray(Ask[]::new);
		QDataAskMessage askMsg = new QDataAskMessage(askArray);
		Set<QDataAskMessage> askSet = new HashSet<QDataAskMessage>();
		askSet.add(askMsg);
		List<QDataBaseEntityMessage> msgs = new ArrayList<QDataBaseEntityMessage>();
		msgs.add(msg2);

		GennyKieSession.sendData(serviceToken, userToken, "FRM_QUE_DASHBOARD_VIEW", "FRM_CONTENT", msgs, askSet);

		VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg)); // Send the results to the frontend to be put into the
																// redux store

		List<QDataBaseEntityMessage> msgs3 = tableData.getThemeMsgList();
		//
		GennyKieSession.sendData(serviceToken, userToken, "FRM_TABLE_VIEW", "FRM_CONTENT", msgs3, askSet);

	}

	// @Test
	public void newUserTest() {
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

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("userToken2   =" + userToken2.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage initMsg = new QEventMessage("EVT_MSG", "INIT_STARTUP");

		QEventMessage authInitMsg1 = new QEventMessage("EVT_MSG", "AUTH_INIT");
		authInitMsg1.setToken(userToken.getToken());
		QEventMessage authInitMsg2 = new QEventMessage("EVT_MSG", "AUTH_INIT");
		authInitMsg2.setToken(userToken2.getToken());

		MessageData data = new MessageData("QUE_TABLE_NEXT_BTN");
		data.setParentCode("QUE_TABLE_FOOTER_GRP");
		data.setRootCode("QUE_TABLE_FOOTER_GRP");

		QEventMessage nextEvtMsg = new QEventMessage("EVT_MSG", "BTN_CLICK");
		nextEvtMsg.setToken(userToken.getToken());
		nextEvtMsg.setData(data);

		QEventMessage msg1 = new QEventMessage("EVT_MSG", "INIT_1");
		QEventMessage msgLogout1 = new QEventMessage("EVT_MSG", "LOGOUT");
		msgLogout1.setToken(userToken.getToken());
		QEventMessage msgLogout2 = new QEventMessage("EVT_MSG", "LOGOUT");
		msgLogout2.setToken(userToken2.getToken());

		List<Answer> answers = new ArrayList<Answer>();
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_FIRSTNAME", "Bruce"));
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_LASTNAME", "Wayne"));
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_ADDRESS_JSON",
				"{\"street_number\":\"64\",\"street_name\":\"Fakenham Road\",\"suburb\":\"Ashburton\",\"state\":\"Victoria\",\"country\":\"AU\",\"postal_code\":\"3147\",\"full_address\":\"64 Fakenham Rd, Ashburton VIC 3147, Australia\",\"street_address\":\"64 Fakenham Road\"}"));

		QDataAnswerMessage answerMsg = new QDataAnswerMessage(answers.toArray(new Answer[0]));
		answerMsg.setToken(userToken.getToken());

		Answer searchBarAnswer = new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_SEARCH_TEXT2",
				"Phantom");
		QDataAnswerMessage searchMsg = new QDataAnswerMessage(searchBarAnswer);
		searchMsg.setToken(userToken.getToken());

		// NOW SET UP Some baseentitys
		BaseEntity project = new BaseEntity("PRJ_" + serviceToken.getRealm().toUpperCase(),
				WordUtils.capitalizeFully(serviceToken.getRealm()));
		project.setRealm(serviceToken.getRealm());
		VertxUtils.writeCachedJson(serviceToken.getRealm(), "PRJ_" + serviceToken.getRealm().toUpperCase(),
				JsonUtils.toJson(project), serviceToken.getToken());
		VertxUtils.writeCachedJson(realm, ":" + "PRJ_" + serviceToken.getRealm().toUpperCase(),
				JsonUtils.toJson(project), serviceToken.getToken());
		BaseEntity project2 = VertxUtils.getObject(serviceToken.getRealm(), "",
				"PRJ_" + serviceToken.getRealm().toUpperCase(), BaseEntity.class, serviceToken.getToken());

		GennyKieSession gks = null;

		try {
			gks = GennyKieSession.builder(serviceToken, true).addDrl("SignalProcessing").addDrl("DataProcessing")
					.addDrl("EventProcessing").addDrl("InitialiseProject").addDrl("XXXPRI_SEARCH_TEXT2.drl")
					.addJbpm("InitialiseProject").addJbpm("Lifecycles").addDrl("AuthInit").addJbpm("AuthInit")

					.addToken(userToken).build();
			gks.start();

			BaseEntity icn_sort = new BaseEntity("ICN_SORT", "Icon Sort");
			try {

				icn_sort.addAttribute(RulesUtils.getAttribute("PRI_ICON_CODE", serviceToken.getToken()), 1.0, "sort");
				icn_sort.setRealm(realm);
				VertxUtils.writeCachedJson(realm, "ICN_SORT", JsonUtils.toJson(icn_sort), serviceToken.getToken());

			} catch (BadDataException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			gks.injectEvent(initMsg); // This should create a new process

			gks.injectEvent(authInitMsg1); // This should create a new process
			gks.advanceSeconds(5, false);
//			gks.injectEvent(authInitMsg2); // This should create a new process
			gks.advanceSeconds(5, false);
			gks.injectEvent(authInitMsg1); // This should attach to existing process
			gks.advanceSeconds(5, false);

			gks.injectEvent(answerMsg); // This sends an answer to the first userSessio
			gks.advanceSeconds(5, false);

			BaseEntity user = VertxUtils.getObject(serviceToken.getRealm(), "", userToken.getUserCode(),
					BaseEntity.class, serviceToken.getToken());

			gks.injectEvent(searchMsg); // This sends a search bar request

			gks.injectEvent(msgLogout1);
//			gks.injectEvent(msgLogout2);
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			if (gks != null) {
				gks.close();
			}
		}
	}

	// @Test
	public void answerRulesTest() {
		System.out.println("Test Answer Rules");
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

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage initMsg = new QEventMessage("EVT_MSG", "INIT_STARTUP");

		QEventMessage authInitMsg = new QEventMessage("EVT_MSG", "AUTH_INIT");
		QEventMessage msg1 = new QEventMessage("EVT_MSG", "INIT_1");
		QEventMessage msgLogout = new QEventMessage("EVT_MSG", "LOGOUT");

		List<Answer> answers = new ArrayList<Answer>();
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_FIRSTNAME", "Bruce"));
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_LASTNAME", "Wayne"));
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_ADDRESS_JSON",
				"{\"street_number\":\"64\",\"street_name\":\"Fakenham Road\",\"suburb\":\"Ashburton\",\"state\":\"Victoria\",\"country\":\"AU\",\"postal_code\":\"3147\",\"full_address\":\"64 Fakenham Rd, Ashburton VIC 3147, Australia\",\"street_address\":\"64 Fakenham Road\"}"));

		QDataAnswerMessage answerMsg = new QDataAnswerMessage(answers.toArray(new Answer[0]));

		// NOW SET UP Some baseentitys
		BaseEntity project = new BaseEntity("PRJ_" + serviceToken.getRealm().toUpperCase(),
				WordUtils.capitalizeFully(serviceToken.getRealm()));
		project.setRealm(serviceToken.getRealm());
		VertxUtils.writeCachedJson(serviceToken.getRealm(), "PRJ_" + serviceToken.getRealm().toUpperCase(),
				JsonUtils.toJson(project), serviceToken.getToken());

		// Log out to begin
		VertxUtils.writeCachedJson(userToken.getRealm(), userToken.getSessionCode(), null, userToken.getToken());

		GennyKieSession gks = null;

		try {
			gks = GennyKieSession.builder(serviceToken, true).addDrl("DataProcessing").addDrl("EventProcessing")
					.addDrl("InitialiseProject").addJbpm("InitialiseProject").addJbpm("userValidation")
					.addJbpm("Lifecycles").addDrl("AuthInit").addJbpm("AuthInit")

					.addToken(userToken).build();
			gks.start();
			gks.injectEvent(initMsg); // This should create a new process

			gks.injectEvent(authInitMsg); // This should create a new process
			gks.advanceSeconds(5, false);

			gks.injectEvent(answerMsg); // This should create a new process
			gks.advanceSeconds(5, false);
			gks.injectEvent(msgLogout);

		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			if (gks != null) {
				gks.close();
			}
		}
	}

	// @Test
	public void processAddress() {

		Answer address = new Answer("PER_USER1", "PER_USER1", "PRI_ADDRESS_JSON",
				"{\"street_number\":\"64\",\"street_name\":\"Fakenham Road\",\"suburb\":\"Ashburton\",\"state\":\"Victoria\",\"country\":\"AU\",\"postal_code\":\"3147\",\"full_address\":\"64 Fakenham Rd, Ashburton VIC 3147, Australia\",\"street_address\":\"64 Fakenham Road\"}");

		JsonObject addressDataJson = new JsonObject(address.getValue());

		System.out.println("The Address Json is  :: " + addressDataJson);

		List<Answer> answers = new ArrayList<Answer>();
		answers.add(new Answer("PER_USER1", "PER_USER1", "PRI_ADDRESS_COUNTRY", addressDataJson.getString("country")));
		answers.add(
				new Answer("PER_USER1", "PER_USER1", "PRI_ADDRESS_POSTCODE", addressDataJson.getString("postal_code")));
		answers.add(new Answer("PER_USER1", "PER_USER1", "PRI_ADDRESS_SUBURB", addressDataJson.getString("suburb")));
		answers.add(new Answer("PER_USER1", "PER_USER1", "PRI_ADDRESS_STATE", addressDataJson.getString("state")));
		answers.add(new Answer("PER_USER1", "PER_USER1", "PRI_ADDRESS_ADDRESS1",
				addressDataJson.getString("street_address")));
		answers.add(
				new Answer("PER_USER1", "PER_USER1", "PRI_ADDRESS_FULL", addressDataJson.getString("full_address")));

	}

	// @Test
	public void virtualQuestionTest() {
		System.out.println("Send Virtual Question");
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
		} else {
			qRules = GennyJbpmBaseTest.setupLocalService();
			userToken = new GennyToken("userToken", qRules.getToken());
			serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
		}

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		// NOW SET UP Some baseentitys
		BaseEntity user = VertxUtils.readFromDDT(serviceToken.getRealm(), userToken.getUserCode(), true,
				serviceToken.getToken());

		BaseEntity project = VertxUtils.readFromDDT(serviceToken.getRealm(), "PRJ_" + realm.toUpperCase(), true,
				serviceToken.getToken());

		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
		BaseEntity sortIconBe = beUtils.getBaseEntityByCode("ICN_SORT");

		Context context = new Context(ContextType.ICON, sortIconBe, VisualControlType.VCL_ICON, 1.0);

		Frame3 FRM_POWERED_BY = null;

		try {
			FRM_POWERED_BY = Frame3.builder("FRM_POWERED_BY").addTheme("THM_WIDTH_200", serviceToken).end()
					.addTheme("THM_COLOR_WHITE", serviceToken).end().question("QUE_POWERED_BY_GRP")
					.sourceAlias("PRJ_" + realm.toUpperCase()).targetAlias("PRJ_" + realm.toUpperCase())
					.addTheme("THM_FORM_LABEL_DEFAULT", serviceToken).vcl(VisualControlType.VCL_LABEL).end()
					.addTheme("THM_FORM_DEFAULT_REPLICA", serviceToken).weight(3.0).end().end().build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		BaseEntity[] bea = new BaseEntity[1];
//		bea[0] = project;
//		QDataBaseEntityMessage prjtest = new QDataBaseEntityMessage();
		qRules.publishCmd(project, "PROJECT");

		Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();

		QDataBaseEntityMessage msg = FrameUtils2.toMessage(FRM_POWERED_BY, serviceToken, askMsgs);
		// qRules.publishCmd(msg);

		List<Tuple2<String, String>> sourceTargetCodes = new ArrayList<Tuple2<String, String>>();
		sourceTargetCodes.add(Tuple.of(serviceToken.getUserCode(), userToken.getUserCode()));

		for (QDataAskMessage askMsg : askMsgs) {
			qRules.publishCmd(askMsg, sourceTargetCodes);
		}
		System.out.println("Sent");

		System.out.println(user);
	}

	// @Test
	public void userSessionTest() {
		System.out.println("Show UserSession");
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

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage authInitMsg = new QEventMessage("EVT_MSG", "AUTH_INIT");
		QEventMessage msg1 = new QEventMessage("EVT_MSG", "INIT_1");
		QEventMessage msgLogout = new QEventMessage("EVT_MSG", "LOGOUT");

		// NOW SET UP Some baseentitys
		BaseEntity project = new BaseEntity("PRJ_" + serviceToken.getRealm().toUpperCase(),
				WordUtils.capitalizeFully(serviceToken.getRealm()));
		project.setRealm(serviceToken.getRealm());
		VertxUtils.writeCachedJson(serviceToken.getRealm(), "PRJ_" + serviceToken.getRealm().toUpperCase(),
				JsonUtils.toJson(project), serviceToken.getToken());

		// Log out to begin
		VertxUtils.writeCachedJson(userToken.getRealm(), userToken.getSessionCode(), null, userToken.getToken());

		GennyKieSession gks = null;

		try {
			gks = GennyKieSession.builder(serviceToken, true).addJbpm("userLifecycle.bpmn").addJbpm("userSession.bpmn")
					.addJbpm("auth_init.bpmn").addJbpm("showDashboard.bpmn").addJbpm("userValidation.bpmn")
					.addDrl("SendUserData").addToken(userToken).build();
			gks.start();

			gks.injectEvent(authInitMsg); // This should create a new process
			gks.advanceSeconds(5, true);
			gks.injectEvent(authInitMsg); // check that auth init with same session is ok and that process Id is looked
											// up!
			gks.advanceSeconds(5, true);
			gks.injectEvent(msgLogout);

			BaseEntity user = VertxUtils.getObject(serviceToken.getRealm(), "", userToken.getUserCode(),
					BaseEntity.class, serviceToken.getToken());
			System.out.println("final user created " + user);
			System.out.println("Sent");

		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			gks.close();
		}
	}

	// @Test
	public void userSessionTest2() {
		System.out.println("Show UserSession");
		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage authInitMsg = new QEventMessage("EVT_MSG", "AUTH_INIT");
		QEventMessage msg1 = new QEventMessage("EVT_MSG", "INIT_1");
		QEventMessage msgLogout = new QEventMessage("EVT_MSG", "LOGOUT");

		GennyKieSession gks = null;

		try {
			gks = GennyKieSession.builder(serviceToken, false).addJbpm("test_session_1.bpmn")
					.addJbpm("test_session_2.bpmn").addJbpm("dashboard.bpmn").addToken(userToken).build();
			gks.start();

//				gks.advanceSeconds(2, true);
//				gks.injectSignal("userMessage", msg1);
//				gks.advanceSeconds(2, true);

			gks.injectEvent(authInitMsg);
			gks.advanceSeconds(2, true);
//				gks.injectSignal("userMessage", msgLogout);

//			for (int i=0;i<2;i++) {
//				gks.displayForm("FRM_DASHBOARD",userToken);
//				gks.advanceSeconds(2, true);
//				gks.displayForm("FRM_DASHBOARD2",userToken);
//				gks.advanceSeconds(2, true);
//			}

			// gks.sendLogout(userToken);

			System.out.println("Sent");

		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			gks.close();
		}
	}

//@Test
	public void displayBucketPage() {
		System.out.println("Show Bucket Page");
		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage msg = new QEventMessage("EVT_MSG", "INIT_STARTUP");

		GennyKieSession gks = null;
		try {
			gks = GennyKieSession.builder(serviceToken, false).addJbpm("show_bucket_page.bpmn").addFact("msg", msg)
					.addToken(userToken).build();

			gks.start();
			gks.injectSignal("inputSignal", "Hello");
			gks.advanceSeconds(20, false);

			System.out.println("Sent");

		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			gks.close();
		}
	}

	// @Test
	public void displayTestPage1() {
		System.out.println("Show test page 1");
		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage msg = new QEventMessage("EVT_MSG", "INIT_STARTUP");

		GennyKieSession gks = null;
		try {
			gks = GennyKieSession.builder(serviceToken, false).addJbpm("test_page_1.bpmn").addFact("msg", msg)
					.addToken(userToken).build();

			gks.start();
			gks.injectSignal("inputSignal", "Hello");
			gks.advanceSeconds(20, false);

			System.out.println("Sent");

		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			gks.close();
		}
	}

//@Test
	public void sendAuthInit() {

		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		QEventMessage msg = new QEventMessage("EVT_MSG", "AUTH_INIT");

		GennyKieSession gks = null;
		try {
			gks = GennyKieSession.builder(serviceToken).addDrl(DRL_SEND_USER_DATA_DIR) // send the initial User data
																						// using the rules
					.addJbpm("adhoc.bpmn").addFact("qRules", rules).addFact("msg", msg).addToken(serviceToken)
					.addToken(userToken).build();

			gks.start();

			gks.advanceSeconds(5, true);
			gks.injectMessage(msg);
			gks.advanceSeconds(3, true);
		} finally {
			gks.close();
		}
	}

	// @Test
	public void simpleTest() {
		GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
		GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
		QRules qRules = new QRules(eventBusMock, userToken.getToken());
		qRules.set("realm", userToken.getRealm());
		qRules.setServiceToken(serviceToken.getToken());

		Theme THM_NOT_INHERITBALE = Theme.builder("THM_NOT_INHERITBALE")
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();
		Frame3 logo = Frame3.builder("FRM_PROJECT_LOGO").addTheme(THM_NOT_INHERITBALE).end().build();

		Frame3 frameRoot = Frame3.builder("FRM_ROOT").addFrame(logo, FramePosition.NORTH).end().build();

		Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();
		QDataBaseEntityMessage msg = FrameUtils2.toMessage(frameRoot, serviceToken, askMsgs);

		String test = JsonUtils.toJson(msg);
		System.out.println(test);
	}

	// @Test
	public void quickTest() {

		GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
		GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
		QRules qRules = new QRules(eventBusMock, userToken.getToken());
		qRules.set("realm", userToken.getRealm());
		qRules.setServiceToken(serviceToken.getToken());

		System.out.println("session=" + userToken.getSessionCode());
		System.out.println("userToken=" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage msg = new QEventMessage("EVT_MSG", "AUTH_INIT");

		GennyKieSession gks = null;
		try {
			gks = GennyKieSession.builder(serviceToken, true).addJbpm("adam_test_1.bpmn").addFact("qRules", qRules)
					.addFact("msg", msg).addToken(serviceToken).addToken(userToken).build();

			gks.start();

			gks.advanceSeconds(10, false);
			gks.broadcastSignal("inputSignal", "Hello");
			// gks.getKieSession().getQueryResults(query, arguments)
			gks.advanceSeconds(1, false);
		} finally {
			gks.close();
		}

	}

	// Only run if no background service running, used to test GenerateRules

	// @Test
	public void initLocalRulesTest() {
		System.out.println("Run the Project Initialisation");
		VertxUtils.cachedEnabled = true; // don't try and use any local services
		GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "userToken");
		GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "serviceToken");
		QRules qRules = new QRules(eventBusMock, userToken.getToken());
		qRules.set("realm", userToken.getRealm());
		qRules.setServiceToken(serviceToken.getToken());

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage msg = new QEventMessage("EVT_MSG", "INIT_STARTUP");

		GennyKieSession gks = null;
		try {
			gks = GennyKieSession.builder(serviceToken, false).addJbpm("init_project.bpmn").addDrl("GenerateSearches")
					.addDrl("GenerateThemes").addDrl("GenerateFrames").addFact("qRules", qRules).addFact("msg", msg)

					.build();

			gks.start();

			gks.advanceSeconds(20, false);
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			gks.close();
		}

	}

	// @Test
	public void testTheme() {
		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		// Theme THM_DUMMY =
		// Theme.builder("THM_DUMMY").addAttribute().height(100).end().addAttribute().width(90).end()
		// .build();
		Theme THM_DUMMY = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_DUMMY", Theme.class,
				serviceToken.getToken());

//		Theme THM_DISPLAY_VERTICAL = Theme.builder("THM_DISPLAY_VERTICAL")
//				.name("Display Visual Controls Vertically") /* Optional - defaults to the code */
//				.addAttribute(ThemeAttributeType.PRI_CONTENT).flexDirection("column").shadowOffset().height(5).width(5)
//				.end().maxWidth(600).padding(10).end().addAttribute() /* defaults to ThemeAttributeType.PRI_CONTENT */
//				.justifyContent("flex-start").end().build();
		Theme THM_DISPLAY_VERTICAL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_DISPLAY_VERTICAL",
				Theme.class, serviceToken.getToken());

//		Theme THM_DISPLAY_HORIZONTAL = Theme.builder("THM_DISPLAY_HORIZONTAL").addAttribute().flexDirection("row").end()
//				.build();
		Theme THM_DISPLAY_HORIZONTAL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_DISPLAY_HORIZONTAL",
				Theme.class, serviceToken.getToken());

//		Theme THM_BACKGROUND_WHITE = Theme.builder("THM_BACKGROUND_WHITE").addAttribute().backgroundColor("white").end()
//				.build();
		Theme THM_BACKGROUND_WHITE = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BACKGROUND_WHITE",
				Theme.class, serviceToken.getToken());

//		Theme THM_BACKGROUND_GREEN = Theme.builder("THM_BACKGROUND_GREEN").addAttribute().backgroundColor("green").end()
//				.build();
		Theme THM_BACKGROUND_GREEN = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BACKGROUND_GREEN",
				Theme.class, serviceToken.getToken());

//		Theme THM_BACKGROUND_YELLOW = Theme.builder("THM_BACKGROUND_YELLOW").addAttribute().backgroundColor("yellow")
//				.end().build();
		Theme THM_BACKGROUND_YELLOW = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BACKGROUND_YELLOW",
				Theme.class, serviceToken.getToken());

//		Theme THM_BACKGROUND_RED = Theme.builder("THM_BACKGROUND_RED").addAttribute().backgroundColor("red").end()
//				.build();
		Theme THM_BACKGROUND_RED = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BACKGROUND_RED", Theme.class,
				serviceToken.getToken());

//		Theme THM_BACKGROUND_GRAY = Theme.builder("THM_BACKGROUND_GRAY").addAttribute().backgroundColor("gray").end()
//				.build();
		Theme THM_BACKGROUND_GRAY = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BACKGROUND_GRAY",
				Theme.class, serviceToken.getToken());

		Theme THM_BACKGROUND_ORANGE = Theme.builder("THM_BACKGROUND_ORANGE").addAttribute().backgroundColor("orange")
				.end().build();

		Theme THM_BACKGROUND_BLACK = Theme.builder("THM_BACKGROUND_BLACK").addAttribute().backgroundColor("black").end()
				.build();

//		Theme THM_BACKGROUND_BLUE = Theme.builder("THM_BACKGROUND_BLUE").addAttribute().backgroundColor("blue").end()
//				.build();
		Theme THM_BACKGROUND_BLUE = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BACKGROUND_BLUE",
				Theme.class, serviceToken.getToken());

		Theme THM_BACKGROUND_INTERNMATCH = Theme.builder("THM_BACKGROUND_INTERNMATCH").addAttribute()
				.backgroundColor("#233a4e").end().build();

//		Theme THM_WIDTH_300 = Theme.builder("THM_WIDTH_300").addAttribute().width(300).end().build();
		Theme THM_WIDTH_300 = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_WIDTH_300", Theme.class,
				serviceToken.getToken());

//		Theme THM_FORM_INPUT_DEFAULT = Theme.builder("THM_FORM_INPUT_DEFAULT").addAttribute().borderBottomWidth(1)
//				.borderColor("#ddd").borderStyle("solid").placeholderColor("#888").end()
//				.addAttribute(ThemeAttributeType.PRI_CONTENT_HOVER).borderColor("#aaa").end()
//				.addAttribute(ThemeAttributeType.PRI_CONTENT_ACTIVE).borderColor("green").end()
//				.addAttribute(ThemeAttributeType.PRI_CONTENT_ERROR).borderColor("red").color("red").end().build();

		Theme THM_FORM_INPUT_DEFAULT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_FORM_INPUT_DEFAULT",
				Theme.class, serviceToken.getToken());

//		Theme THM_FORM_LABEL_DEFAULT = Theme.builder("THM_FORM_LABEL_DEFAULT").addAttribute().bold(true).size("md")
//				.end().build();
		Theme THM_FORM_LABEL_DEFAULT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_FORM_LABEL_DEFAULT",
				Theme.class, serviceToken.getToken());

//		Theme THM_FORM_WRAPPER_DEFAULT = Theme.builder("THM_FORM_WRAPPER_DEFAULT").addAttribute().marginBottom(10)
//				.padding(10).end().addAttribute(ThemeAttributeType.PRI_CONTENT_ERROR).backgroundColor("#fc8e6").end()
//				.build();
		Theme THM_FORM_WRAPPER_DEFAULT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_FORM_WRAPPER_DEFAULT",
				Theme.class, serviceToken.getToken());

//		Theme THM_FORM_ERROR_DEFAULT = Theme.builder("THM_FORM_ERROR_DEFAULT").addAttribute().color("red").end()
//				.build();
		Theme THM_FORM_ERROR_DEFAULT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_FORM_ERROR_DEFAULT",
				Theme.class, serviceToken.getToken());

//		Theme THM_FORM_DEFAULT = Theme.builder("THM_FORM_DEFAULT").addAttribute().backgroundColor("none").end()
//				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_TITLE, true).end()
//				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_DESCRIPTION, true).end()
//				.addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true).end()
//				.addAttribute(ThemeAttributeType.PRI_HAS_REQUIRED, true).end()
//				.addAttribute(ThemeAttributeType.PRI_HAS_ICON, true).end()
//				.build();
		Theme THM_FORM_DEFAULT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_FORM_DEFAULT", Theme.class,
				serviceToken.getToken());

//		Theme THM_FORM_CONTAINER_DEFAULT = Theme.builder("THM_FORM_CONTAINER_DEFAULT").addAttribute()
//				.backgroundColor("none").padding(10).maxWidth(700).width("100%").shadowColor("#000").shadowOpacity(0.4)
//				.shadowRadius(0).shadowOffset().width(0).height(0).end().end()
//				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_TITLE, true).end()
//				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_DESCRIPTION, true).end()
//				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, true).end().build();
		Theme THM_FORM_CONTAINER_DEFAULT = VertxUtils.getObject(serviceToken.getRealm(), "",
				"THM_FORM_CONTAINER_DEFAULT", Theme.class, serviceToken.getToken());

//		Frame3 FRM_DUMMY2 = Frame3.builder("FRM_DUMMY").addTheme(THM_DUMMY).end().build();
//		String td2 = JsonUtils.toJson(THM_DUMMY);
//		ThemeDouble td = new ThemeDouble(THM_DUMMY,1.0);
//		String js2 = JsonUtils.toJson(td);
//		String js = JsonUtils.toJson(FRM_DUMMY2);
//		VertxUtils.putObject(serviceToken.getRealm(), "", FRM_DUMMY2.getCode(), FRM_DUMMY2, serviceToken.getToken());

		Frame3 FRM_DUMMY = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_DUMMY", Frame3.class,
				serviceToken.getToken());

//		Frame3 centre = Frame3.builder("FRM_CENTRE").addFrame(FRM_DUMMY, FramePosition.CENTRE).end().build();
		Frame3 FRM_CENTRE = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_CENTRE", Frame3.class,
				serviceToken.getToken());

//		Frame3 profile = Frame3.builder("FRM_PROFILE").addTheme(THM_DISPLAY_HORIZONTAL).end()
//				.addTheme(THM_BACKGROUND_RED).end().addFrame(FRM_DUMMY, FramePosition.CENTRE).end().build();
		Frame3 FRM_PROFILE = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_PROFILE", Frame3.class,
				serviceToken.getToken());

//		Frame3 header = Frame3.builder("FRM_HEADER").addFrame(FRM_PROFILE, FramePosition.EAST).end().build();
		Frame3 FRM_HEADER = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_HEADER", Frame3.class,
				serviceToken.getToken());

//		Frame3 notes = Frame3.builder("FRM_NOTES").addTheme(THM_WIDTH_300).end()/*
//																				 * .addTheme(THM_DISPLAY_VERTICAL).end()
//																				 */
//				.addTheme(THM_BACKGROUND_RED).end().question("QUE_USER_COMPANY_GRP").end().build();
		Frame3 FRM_NOTES = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_NOTES", Frame3.class,
				serviceToken.getToken());

//		Frame3 FRM_SIDEBAR2 = Frame3.builder("FRM_SIDEBAR2")
//				/* .addTheme(THM_WIDTH_300).end() *//*
//													 * .addTheme(THM_DISPLAY_VERTICAL) .end()
//													 */.addTheme(THM_BACKGROUND_GRAY).end()
//				.question("QUE_USER_PROFILE_GRP").addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT)
//				.weight(2.0).end().addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
//				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
//				.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT)
//				.weight(3.0).end().addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();
		Frame3 FRM_SIDEBAR2 = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_SIDEBAR2", Frame3.class,
				serviceToken.getToken());

//		Frame3 sidebar3 = Frame3.builder("FRM_SIDEBAR3")
//				/* .addTheme(THM_WIDTH_300).end() *//*
//													 * .addTheme(THM_DISPLAY_VERTICAL) .end()
//													 */.addTheme(THM_BACKGROUND_YELLOW).end()
//				.question("QUE_USER_PROFILE_GRP").addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT)
//				.weight(2.0).end().addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
//				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
//				.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT)
//				.weight(3.0).end().addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();
		Frame3 FRM_SIDEBAR3 = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_SIDEBAR3", Frame3.class,
				serviceToken.getToken());

//		Frame3 sidebar = Frame3.builder("FRM_SIDEBAR")
//				// .addTheme(THM_WIDTH_300).end()
//
//				/*
//				 * .addTheme().addAttribute().width(400).end().end().addTheme(
//				 * THM_DISPLAY_VERTICAL).end()
//				 */
//				.addTheme(THM_BACKGROUND_GREEN).end().question("QUE_FIRSTNAME")
//				// .question("QUE_USER_PROFILE_GRP")
//
//				.addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT).weight(2.0).end()
//				.addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
//				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
//				.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT)
//				.weight(3.0).end().addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();

		Frame3 FRM_SIDEBAR = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_SIDEBAR", Frame3.class,
				serviceToken.getToken());

//		Frame3 footer = Frame3.builder("FRM_FOOTER").addFrame(FRM_DUMMY, FramePosition.CENTRE).end()
//				.addTheme(THM_BACKGROUND_BLUE).end().build();
		Frame3 FRM_FOOTER = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_FOOTER", Frame3.class,
				serviceToken.getToken());

//		Frame3 FRM_MAINFRAME = Frame3.builder("FRM_MAIN").addTheme(THM_BACKGROUND_WHITE).end()
//				.addFrame(FRM_SIDEBAR, FramePosition.WEST).end().addFrame(FRM_SIDEBAR2, FramePosition.WEST).end()
//				.addFrame(FRM_SIDEBAR3, FramePosition.WEST).end()
//				// .addFrame(notes,FramePosition.EAST).end()
//				.addFrame(FRM_FOOTER, FramePosition.SOUTH).end().addFrame(FRM_CENTRE, FramePosition.CENTRE).end()
//				.addFrame(FRM_HEADER, FramePosition.NORTH).end().build();

		Frame3 FRM_MAINFRAME = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_MAINFRAME", Frame3.class,
				serviceToken.getToken());

//		Frame3 FRM_ROOT = Frame3.builder("FRM_ROOT")
//				.addFrame(FRM_MAINFRAME).end().build();

		Frame3 FRM_ROOT = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_ROOT", Frame3.class,
				serviceToken.getToken());

		Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();

		QDataBaseEntityMessage msg = FrameUtils2.toMessage(FRM_ROOT, serviceToken, askMsgs);

//		VertxUtils.putObject(serviceToken.getRealm(), "", "FRM_ROOT_MSG", msg, serviceToken.getToken());

		QDataBaseEntityMessage msg2 = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_ROOT_MSG",
				QDataBaseEntityMessage.class, serviceToken.getToken());

		msg2.setToken(userToken.getToken());
		/* send message */
		// rules.publishCmd(msg2); // Send QDataBaseEntityMessage
		VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg2));
//		String askMsgsStr = JsonUtils.toJson(askMsgs);
//		VertxUtils.putObject(serviceToken.getRealm(), "", "DESKTOP_ASKS", askMsgsStr, serviceToken.getToken());

		Type setType = new TypeToken<Set<QDataAskMessage>>() {
		}.getType();

		String askMsgs2Str = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_ROOT_ASKS", String.class,
				serviceToken.getToken());

		Set<QDataAskMessage> askMsgs2 = JsonUtils.fromJson(askMsgs2Str, setType);

		System.out.println("Sending Asks");
		for (QDataAskMessage askMsg : askMsgs) {
			rules.publishCmd(askMsg, serviceToken.getUserCode(), userToken.getUserCode()); // Send associated
																							// QDataAskMessage
		}

		System.out.println("Sent");
	}

	// @Test
	public void testCacheTheme() {

		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		System.out.println("Starting");

		QDataBaseEntityMessage msg2 = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_ROOT_MSG",
				QDataBaseEntityMessage.class, serviceToken.getToken());

		/* send message */
		rules.publishCmd(msg2); // Send QDataBaseEntityMessage

		Type setType = new TypeToken<Set<QDataAskMessage>>() {
		}.getType();

		String askMsgs2Str = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_ROOT_ASKS", String.class,
				serviceToken.getToken());

		Set<QDataAskMessage> askMsgs2 = JsonUtils.fromJson(askMsgs2Str, setType);

		System.out.println("Sending Asks");
		for (QDataAskMessage askMsg : askMsgs2) {
			rules.publishCmd(askMsg, serviceToken.getUserCode(), userToken.getUserCode()); // Send associated
																							// QDataAskMessage
		}

		System.out.println("Sent2");

	}

	// @Test
	public void testLogout() {

	}

	// @Test
	public void formsTest() {
		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		String apiUrl = GennySettings.qwandaServiceUrl + "/service/forms";
		System.out.println("Fetching setup info from " + apiUrl);
		System.out.println("userToken (ensure user has test role) = " + userToken);
		try {
			String jsonFormCodes = QwandaUtils.apiGet(apiUrl, userToken.getToken());
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

// public static Map<Operation, List<OperationCommand>> initMVELOperations() {
//
//     Map<String, Object> vars = new HashMap<String, Object>();
//
//     // Search operations-dsl.mvel, if necessary using superclass if TaskService is subclassed
//     InputStream is = null;
//     // for (Class<?> c = getClass(); c != null; c = c.getSuperclass()) {
//     is = MVELLifeCycleManager.class.getResourceAsStream("/operations-dsl.mvel");
////         if (is != null) {
////             break;
////         }
//     //}
//     if (is == null) {
//         throw new RuntimeException("Unable To initialise TaskService, could not find Operations DSL");
//     }
//     Reader reader = new InputStreamReader(is);
//     try {
//         return (Map<Operation, List<OperationCommand>>) eval(toString(reader), vars);
//     } catch (IOException e) {
//         throw new RuntimeException("Unable To initialise TaskService, could not load Operations DSL");
//     }
//
//
// }

	private static void createUser(final String userCode, String name, boolean makeExisting) {
		// Add this user to the map

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
