package life.genny.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jboss.logging.Logger;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.admin.ProcessInstanceAdminService;
import org.jbpm.services.api.model.DeploymentUnit;
import org.jbpm.services.api.query.QueryService;
import org.jbpm.services.api.utils.KieServiceConfigurator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.vertx.core.json.JsonArray;


import life.genny.eventbus.EventBusInterface;
import life.genny.eventbus.EventBusMock;
import life.genny.eventbus.VertxCache;
import life.genny.models.GennyToken;
import life.genny.qwanda.Answer;
import life.genny.qwanda.Ask;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeText;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.datatype.DataType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.SearchEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.message.QBulkMessage;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.message.QEventDropdownMessage;
import life.genny.qwanda.validation.Validation;
import life.genny.qwandautils.GennyCacheInterface;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.KeycloakUtils;
import life.genny.qwandautils.MergeUtil;
import life.genny.qwandautils.QwandaUtils;
import life.genny.utils.BaseEntityUtils;
import life.genny.utils.QuestionUtils;
import life.genny.utils.RulesUtils;
import life.genny.utils.SearchUtils;
import life.genny.utils.VertxUtils;

public class JasperTest {
	private static final Logger log = Logger.getLogger(RandomTest.class);

	protected static String realm = GennySettings.mainrealm;
	protected static Set<String> realms;

	protected static Optional<Boolean> isUsingRemote = Optional.empty();

	public static GennyToken userToken;
	public static GennyToken serviceToken;

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

	protected KieServiceConfigurator serviceConfigurator;

	protected DeploymentUnit deploymentUnit;

	protected static GennyToken newUserToken;

	protected static BaseEntityUtils beUtils;

	public JasperTest() {
		super();
	}
	
	@Test
	public void MergeTest() {
		System.out.println("Merge Test");

		VertxUtils.cachedEnabled = false;

		if (beUtils == null) {
			return;
		}
		BaseEntity project = beUtils.getBaseEntityByCode("PRJ_" + serviceToken.getRealm().toUpperCase());
		
		String url = "https://raw.githubusercontent.com/genny-project/layouts/master/internmatch-new/document_templates/TestAgreementDocView.html";
		
		
		BaseEntity app = beUtils.getBaseEntityByCode("APP_34F9E0DD-8628-4C52-8653-DA27DB10541D");
		
        HashMap<String, Object> contextMap = new HashMap<String, Object>();
        
        contextMap.put("APPLICATION", app);
        
		QDataAskMessage askMessage = QuestionUtils.getAsks("PER_SOURCE", "SBE_INTERNS_046382629", "QUE_ADD_FILTER_GRP", beUtils.getGennyToken().getToken());

		System.out.println(JsonUtils.toJson(askMessage));
		
		Ask addFilterGrpAsk = askMessage.getItems()[0];

	}
	
	@Test
	public void ArrayTest() {
		System.out.println("Detail View Test");

		VertxUtils.cachedEnabled = false;

		if (beUtils == null) {
			return;
		}
		BaseEntity project = beUtils.getBaseEntityByCode("PRJ_" + serviceToken.getRealm().toUpperCase());
		
		Attribute attribute = RulesUtils.getAttribute("LNK_EDU_PROVIDER", userToken);
			// Get Group Code
		DataType dt = attribute.getDataType();
		// log.info("DATATYPE IS " + dt);
		String groupCode = null;
		List<Validation> vl = dt.getValidationList();
		System.out.println("vl = " + vl);

		if ((vl != null) && (!vl.isEmpty())) {
			Validation val = vl.get(0);
			if ((val.getSelectionBaseEntityGroupList() != null)
					&& (!val.getSelectionBaseEntityGroupList().isEmpty())) {
				groupCode = val.getSelectionBaseEntityGroupList().get(0);
				System.out.println("GroupCode = " + groupCode);
			}
			System.out.println("val = " + val);
		}

	}


	@Test
	public void DetailViewTest() {
		System.out.println("Detail View Test");

		VertxUtils.cachedEnabled = false;

		if (beUtils == null) {
			return;
		}
		BaseEntity project = beUtils.getBaseEntityByCode("PRJ_" + serviceToken.getRealm().toUpperCase());
		
		SearchEntity searchBE = new SearchEntity("SBE_APP", "App search")
				.addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.EQUAL, "APP_0D6C7105-AE01-47DA-B90B-7205AC7149CA")
				
				.addColumn("_PRI_INTERN_CODE", "INTERN");

		searchBE.setRealm(realm);
		searchBE.setPageStart(0);
		searchBE.setPageSize(1000);
		
		AttributeText attribute = new AttributeText("SCH_QUESTION_CODE", "Question Code");
		try {
			searchBE.addAttribute(attribute, 1.0, "QUE_APPLICATION_DETAIL_VIEW_GRP");
		} catch (BadDataException e) {
			log.error("Bad Question Code!");
		}
		
		BaseEntity target = beUtils.getBaseEntityByCode("APP_0D6C7105-AE01-47DA-B90B-7205AC7149CA");
		
		QBulkMessage bulk = SearchUtils.getAskEntityData(beUtils, searchBE, target); 
		
		System.out.println(JsonUtils.toJson(bulk));

	}


	private void scrub(String attributeCodeLike, String fakedata) {

		System.out.println("Replacing all the " + attributeCodeLike + " with " + fakedata);
		try {
			String encodedsql = encodeValue("update baseentity_attribute set valueString='" + fakedata
					+ "' where attributeCode like '" + attributeCodeLike + "'");
			String resultJson = QwandaUtils.apiGet(GennySettings.qwandaServiceUrl + "/service/executesql/" + encodedsql,
					serviceToken.getToken());
		} catch (Exception e) {

		}
	}

	private void scrubname(String fakedata) {

		System.out.println("Replacing all the names with " + fakedata);
		try {
			String encodedsql = encodeValue("update baseentity set name='" + fakedata + "';");
			String resultJson = QwandaUtils.apiGet(GennySettings.qwandaServiceUrl + "/service/executesql/" + encodedsql,
					serviceToken.getToken());
		} catch (Exception e) {

		}
	}

	/**
	 * @param state
	 * @return
	 */
	private String ShortenState(String state) {
		switch (state) {
		case "Victoria":
			state = "VIC";
			break;
		case "New South Wales":
			state = "NSW";
			break;
		case "Queensland":
			state = "QLD";
			break;
		case "South Australia":
			state = "SA";
			break;
		case "Western Australia":
			state = "WA";
			break;
		case "Tasmania":
			state = "TAS";
			break;
		case "Australian Capital Territory":
			state = "ACT";
			break;
		case "Northern Territory":
			state = "NT";
			break;
		default:
		}
		return state;
	}

	/**
	 * @param state
	 * @return
	 */
	private String shortenCountry(String country) {
		if (StringUtils.isBlank(country)) {
			return "au";
		}
		switch (country) {
		case "Australia":
			country = "AU";
			break;
		case "New Zealand":
			country = "NZ";
			break;
		case "South Africa":
			country = "SA";
			break;
		case "United States":
			country = "US";
			break;
		case "United Kingdom":
			country = "UK";
			break;
		default:
		}
		return country;
	}

	@BeforeAll
	public static void init() throws FileNotFoundException, SQLException {

		System.out.println("BridgeUrl=" + GennySettings.bridgeServiceUrl);
		System.out.println("QwandaUrl=" + GennySettings.qwandaServiceUrl);

//		GennyToken tokenUser = GennyJbpmBaseTest.createGennyToken("ABCDEFGH", "internmatch", "adam.crow@gada.io",
//				"Adam Crow", "intern");
//		GennyToken tokenSupervisor = GennyJbpmBaseTest.createGennyToken("BCDEFGSHS", "internmatch",
//				"kanika.gulati@gada.io", "Kanika Gulati", "supervisor");
//		System.out.println(tokenUser.getToken());
//		System.out.println(tokenSupervisor.getToken());

		// Set up realm
		realms = new HashSet<String>();
		realms.add(realm);
		// realms.stream().forEach(System.out::println);
		realms.remove("genny");

		// Enable the PseudoClock using the following system property.
		System.setProperty("drools.clockType", "pseudo");

		eventBusMock = new EventBusMock();
		vertxCache = new VertxCache(); // MockCache
		VertxUtils.init(eventBusMock, vertxCache);

		// QRules qRules = null;

		String apiUrl = GennySettings.projectUrl + "/api/events/init?url=" + GennySettings.projectUrl;
		System.out.println("Fetching setup info from " + apiUrl);
		try {
			JsonObject projectParms = null;
			String keycloakJson = QwandaUtils.apiGet(apiUrl, null);
			JsonReader jsonReader = Json.createReader(new StringReader(keycloakJson));
			projectParms = jsonReader.readObject();
			jsonReader.close();

			String authServer = projectParms.getString("auth-server-url");
			authServer = StringUtils.removeEnd(authServer, "/auth");
			JsonObject credentials = projectParms.getJsonObject("credentials");
			String secret = credentials.getString("secret");
			String username = System.getenv("USERNAME");
			String password = System.getenv("PASSWORD");

			String token = KeycloakUtils.getAccessToken(authServer, realm, realm, secret, username, password);
			GennyToken uToken = new GennyToken(token);
			// check if user token already exists
			String userCode = uToken.getUserCode();// "PER_"+QwandaUtils.getNormalisedUsername(username);
			io.vertx.core.json.JsonObject cacheJson = VertxUtils.readCachedJson(realm, "TOKEN:" + userCode, token);
			String status = cacheJson.getString("status");

			if ("ok".equals(status)) {
				String userTokenStr = cacheJson.getString("value");
				userToken = new GennyToken("userToken", userTokenStr);
				System.out.println(
						"User " + username + " is logged in! " + userToken.getAdecodedTokenMap().get("session_state"));

			} else {
				System.out.println("User " + username + " is NOT LOGGED IN!");
				;
				userToken = new GennyToken(token);
				return;
			}

			String serviceTokenStr = KeycloakUtils.getAccessToken(authServer, realm, realm, secret, "service",
					System.getenv("SERVICE_PASSWORD"));
			serviceToken = new GennyToken(serviceTokenStr);

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			return;
		}
		serviceToken = new GennyToken("PER_SERVICE", serviceToken.getToken());

		// VertxUtils.cachedEnabled = false;
		VertxUtils.cachedEnabled = false;
//				eventBusMock = new EventBusMock();
//				vertxCache = new JunitCache(); // MockCache
//				VertxUtils.init(eventBusMock, vertxCache);
//
//				qRules = GennyJbpmBaseTest.setupLocalService();
//				userToken = new GennyToken("userToken", qRules.getToken());
//				serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());

		beUtils = new BaseEntityUtils(userToken);
		beUtils.setServiceToken(serviceToken);
//	        }

		// System.out.println("serviceToken=" + serviceToken.getToken());

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

	// @Test
	void backFillProcessViewTest() {
		if (beUtils == null) {
			return;
		}
		SearchEntity searchBE = new SearchEntity("SBE_PROGRESS", "progress")
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "APP_%")
				.addFilter("PRI_STATUS", SearchEntity.StringFilter.EQUAL, "PROGRESS");
//				.addColumn("PRI_ASSOC", "Address")
//				.addColumn("PRI_ASSOC_DURATION", "Address");
		searchBE.setRealm(realm);
		searchBE.setPageStart(0);
		searchBE.setPageSize(100000);
		List<BaseEntity> items = beUtils.getBaseEntitys(searchBE);
		boolean useDefault = false;
		String duration = null;
		for (BaseEntity item : items) {
			useDefault = false;
			Optional<EntityAttribute> ea1 = item.findEntityAttribute("PRI_PROGRESS");
			if (ea1.isEmpty()) {
				Optional<EntityAttribute> ea2 = item.findEntityAttribute("PRI_ASSOC_DURATION");
				if (ea2.isPresent()) {
					duration = item.getValue("PRI_ASSOC_DURATION", "12");
					// SEL_DURATION_8_WEEKS
					if ((duration.startsWith("SEL_DURATION_")) && duration.endsWith("_WEEKS")) {
						duration = duration.split("_")[2];
					}
				} else {
					duration = "12";
					useDefault = true;
				}
//				if (!StringUtils.isBlank(duration)) {
//					JsonObject progress_json = new JsonObject();
//					progress_json.put("completedPercentage", 0);
//					progress_json.put("steps", duration);
//					progress_json.put("completedJournals", 0);
//
//					String PRI_PROGRESS_JSON = progress_json.toString();
//
//					if (useDefault)
//						System.out.println(item.getCode() + " doesn't have PRI_ASSOC_DURATION, use default value:12");
//
//					System.out.println(
//							item.getCode() + ", duration:" + duration + ", PRI_PROGRESS_JSON=" + PRI_PROGRESS_JSON);
//
//					Answer fixedAddress = new Answer(userToken.getUserCode(), item.getCode(), "PRI_PROGRESS",
//							PRI_PROGRESS_JSON, false, true);
//					beUtils.saveAnswer(fixedAddress);
//				}
			}
		}
	}

	private static String encodeValue(String value) {
		try {
			return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex.getCause());
		}
	}

}