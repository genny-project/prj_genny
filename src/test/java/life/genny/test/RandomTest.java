package life.genny.test;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

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
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.util.JsonSerialization;

import life.genny.eventbus.EventBusInterface;
import life.genny.eventbus.EventBusMock;
import life.genny.eventbus.VertxCache;
import life.genny.models.GennyToken;

import life.genny.qwanda.Answer;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.SearchEntity;
import life.genny.qwandautils.GennyCacheInterface;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.KeycloakUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.rules.QRules;
import life.genny.utils.BaseEntityUtils;
import life.genny.utils.GennyJbpmBaseTest;

import life.genny.utils.JunitCache;
import life.genny.utils.VertxUtils;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

public class RandomTest {
	private static final Logger log = Logger.getLogger(RandomTest.class);

	protected static String realm = GennySettings.mainrealm;
	protected static Set<String> realms;

	public static JsonObject projectParms;

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

	public RandomTest() {
		super();
	}

	@Test
	public void gennyTest() {
		System.out.println("This is a Gennytest");
	}

	@Test
	public void AdamTest1() {
		System.out.println("Local Genny test1 AdamTest");

	}

	@Test
	public void Randoise() {
		System.out.println("Randomise test");
		GennyToken userToken = null;
		GennyToken serviceToken = null;

		// VertxUtils.cachedEnabled = false;
		VertxUtils.cachedEnabled = false;
//		try {
//			GennyJbpmBaseTest.init();// .setupLocalService();
//			// qRules = GennyJbpmBaseTest.plement();
//		} catch (FileNotFoundException | SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		userToken = new GennyToken("userToken", GennyJbpmBaseTest.projectParms.getString("userToken"));
//		serviceToken = new GennyToken("PER_SERVICE", GennyJbpmBaseTest.projectParms.getString("serviceToken"));
//		eventBusMock = new EventBusMock();
//		vertxCache = new JunitCache(); // MockCache
//		VertxUtils.init(eventBusMock, vertxCache);

//		BaseEntityUtils beUtils = new BaseEntityUtils(userToken);
//		beUtils.setServiceToken(serviceToken);

		if (beUtils == null) {
			return;
		}

		boolean ok = true;
		Integer pageStart = 0;
		Integer pageSize = 5;

		while (ok) {

			SearchEntity searchBE = new SearchEntity("SBE_PER", "person fix")
					.addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
					.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "PER_%")

					.addColumn("PRI_CODE", "Name");

			searchBE.setRealm(realm);
			searchBE.setPageStart(pageStart);
			searchBE.setPageSize(pageSize);
			pageStart += pageSize;

			List<BaseEntity> items = beUtils.getBaseEntitys(searchBE);
			if (items.isEmpty()) {
				ok = false;
				break;
			} else {
				log.info("Loaded " + items.size() + " baseentitys");
			}

			BaseEntity project = beUtils.getBaseEntityByCode("PRJ_" + serviceToken.getRealm().toUpperCase());
			int icount = 0;
			for (BaseEntity item : items) {

				try {

					String jsonStr = QwandaUtils.apiGet(
							"https://randomuser.me/api/?results=1&nat=au&format=json&dl&inc=name,email,picture,cell,gender,timezone",
							null);

					JsonObject json = new JsonObject(jsonStr);

					// {"results":[{"gender":"male","name":{"title":"Mr","first":"Brandon","last":"Stone"},
					// "location":{"street":{"number":2787,"name":"Thornridge
					// Cir"},"city":"Kalgoorlie","state":"Western
					// Australia","country":"Australia","postcode":8870,"coordinates":{"latitude":"82.7676","longitude":"-47.9134"},
					// "timezone":{"offset":"+1:00","description":"Brussels, Copenhagen, Madrid,
					// Paris"}},
					// "email":"brandon.stone@example.com",
					// "login":{"uuid":"1e74d7ef-b98e-4008-99df-7094ea2985b2","username":"ticklishzebra524","password":"matthew1","salt":"RJG5caIf","md5":"cb753e10b245ff24a4930f2388b45c61","sha1":"4016472b5e44d581d14789b221bac29b0bd198a9","sha256":"11f68f754d3268900002ad9d2792b3b516e106c5ac6c099af43db04f61151809"},
					// "dob":{"date":"1944-11-25T15:51:56.452Z","age":77},"registered":{"date":"2012-12-18T08:04:55.143Z","age":9},
					// "phone":"08-6165-3715","cell":"0434-344-332","id":{"name":"TFN","value":"837223755"},
					// "picture":{"large":"https://randomuser.me/api/portraits/men/27.jpg","medium":"https://randomuser.me/api/portraits/med/men/27.jpg","thumbnail":"https://randomuser.me/api/portraits/thumb/men/27.jpg"},"nat":"AU"}],
					// "info":{"seed":"da1c48ae9a193d9f","results":1,"page":1,"version":"1.3"}}

					saveAnswer(item.getCode(), "PRI_EMAIL",
							json.getJsonArray("results").getJsonObject(0).getString("email"));
					String firstname = json.getJsonArray("results").getJsonObject(0).getJsonObject("name")
							.getString("first");
					String lastname = json.getJsonArray("results").getJsonObject(0).getJsonObject("name")
							.getString("last");
					String name = firstname + " " + lastname;
					saveAnswer(item.getCode(), "PRI_NAME", name);

					String number = json.getJsonArray("results").getJsonObject(0).getJsonObject("location")
							.getString("number");
					String street = json.getJsonArray("results").getJsonObject(0).getJsonObject("location")
							.getString("street");
					String city = json.getJsonArray("results").getJsonObject(0).getJsonObject("location")
							.getString("city");
					String state = json.getJsonArray("results").getJsonObject(0).getJsonObject("location")
							.getString("state");
					String country = json.getJsonArray("results").getJsonObject(0).getJsonObject("location")
							.getString("country");
					String postcode = json.getJsonArray("results").getJsonObject(0).getJsonObject("location")
							.getString("postcode");
					JsonObject gps = json.getJsonArray("results").getJsonObject(0).getJsonObject("location")
							.getJsonObject("coordinates");
					Double latitude = Double.valueOf(gps.getString("latitude"));
					Double longitude = Double.valueOf(gps.getString("longitude"));

					System.out.println(item.getCode() + " done "
							+ json.getJsonArray("results").getJsonObject(0).getString("email"));

				} catch (Exception e1) {
					e1.printStackTrace();
				}
				icount++;
				if (icount > 3) {
					break;
				}
			}

		}

		System.out.println("Finished");
	}

	private void saveAnswer(String targetCode, String attributeCode, String value) {
		Answer ans = new Answer(userToken.getUserCode(), targetCode, attributeCode, value, false, false);
		beUtils.saveAnswer(ans);
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
			String keycloakJson = QwandaUtils.apiGet(apiUrl, null);
			projectParms = new JsonObject(keycloakJson);
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
			JsonObject cacheJson = VertxUtils.readCachedJson(realm, "TOKEN:" + userCode, token);
			String status = cacheJson.getString("status");

			if ("ok".equals(status)) {
				String userToken = cacheJson.getString("value");
				GennyToken userGennyToken = new GennyToken("userToken", userToken);
				System.out.println("User " + username + " is logged in! "
						+ userGennyToken.getAdecodedTokenMap().get("session_state"));
				;
				projectParms.put("userToken", userToken);
			} else {
				System.out.println("User " + username + " is NOT LOGGED IN!");
				;
				projectParms.put("userToken", token); // use non alyson token
				return;
			}

			JsonObject serviceTokenJson = VertxUtils.readCachedJson(GennySettings.GENNY_REALM,
					"TOKEN" + realm.toUpperCase(), token);
			status = serviceTokenJson.getString("status");

			if ("ok".equals(status)) {
				String serviceToken = serviceTokenJson.getString("value");
				System.out.println("Service Account available!");
				;
				projectParms.put("serviceToken", serviceToken);
			} else {
				log.error("Service Token UNAVAILABLE!");
				;
				projectParms.put("serviceToken", token); // use non alyson token
				//return;
			}

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			return;
		}
		String uToken = projectParms.getString("userToken");
		userToken = new GennyToken("userToken", uToken);
	//	serviceToken = new GennyToken("PER_SERVICE", projectParms.getString("serviceToken"));

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
	//	beUtils.setServiceToken(serviceToken);
//	        }

		//System.out.println("serviceToken=" + serviceToken.getToken());

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
				if (!StringUtils.isBlank(duration)) {
					JsonObject progress_json = new JsonObject();
					progress_json.put("completedPercentage", 0);
					progress_json.put("steps", duration);
					progress_json.put("completedJournals", 0);

					String PRI_PROGRESS_JSON = progress_json.toString();

					if (useDefault)
						System.out.println(item.getCode() + " doesn't have PRI_ASSOC_DURATION, use default value:12");

					System.out.println(
							item.getCode() + ", duration:" + duration + ", PRI_PROGRESS_JSON=" + PRI_PROGRESS_JSON);

					Answer fixedAddress = new Answer(userToken.getUserCode(), item.getCode(), "PRI_PROGRESS",
							PRI_PROGRESS_JSON, false, true);
					beUtils.saveAnswer(fixedAddress);
				}
			}
		}
	}

}
