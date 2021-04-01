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
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.logging.Logger;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.admin.ProcessInstanceAdminService;
import org.jbpm.services.api.model.DeploymentUnit;
import org.jbpm.services.api.query.QueryService;
import org.jbpm.services.api.utils.KieServiceConfigurator;
import org.junit.BeforeClass;
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

public class GennyTest {
	private static final Logger log = Logger.getLogger(GennyTest.class);

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

	protected KieServiceConfigurator serviceConfigurator;

	protected DeploymentUnit deploymentUnit;

	protected static GennyToken userToken;
	protected static GennyToken newUserToken;
	protected static GennyToken serviceToken;

	protected static BaseEntityUtils beUtils;

	public GennyTest() {
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
	public void DEFTest2() {
		System.out.println("DEF Test test");
		GennyToken userToken = null;
		GennyToken serviceToken = null;

		// VertxUtils.cachedEnabled = false;
		VertxUtils.cachedEnabled = false;
		try {
			GennyJbpmBaseTest.init();// .setupLocalService();
			// qRules = GennyJbpmBaseTest.plement();
		} catch (FileNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		userToken = new GennyToken("userToken", GennyJbpmBaseTest.projectParms.getString("userToken"));
		serviceToken = new GennyToken("PER_SERVICE", GennyJbpmBaseTest.projectParms.getString("serviceToken"));
		eventBusMock = new EventBusMock();
		vertxCache = new JunitCache(); // MockCache
		VertxUtils.init(eventBusMock, vertxCache);

		BaseEntityUtils beUtils = new BaseEntityUtils(userToken);
		beUtils.setServiceToken(serviceToken);

		if (beUtils == null) {
			return;
		}

		boolean ok = true;
		Integer pageStart = 0;
		Integer pageSize = 100;


			SearchEntity searchBE = new SearchEntity("SBE_DEF", "DEF test")
					.addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
					.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "DEF_%")
					
					.addColumn("PRI_NAME", "Name");

			searchBE.setRealm(realm);
			searchBE.setPageStart(pageStart);
			searchBE.setPageSize(pageSize);
			pageStart += pageSize;

			List<BaseEntity> items = beUtils.getBaseEntitys(searchBE);
				log.info("Loaded "+items.size()+" baseentitys");


			BaseEntity project = beUtils.getBaseEntityByCode("PRJ_" + serviceToken.getRealm().toUpperCase());
			log.info("Project = "+project);

	
	}
	
	@Test
	public void fixKeycloaks()
	{
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
        Integer pageStart = 0;
        Integer pageSize = 200;
        List<LinkedHashMap>  pageResults = null;
        
        do {
        String resultsJson = "";
        try {
			resultsJson = QwandaUtils.apiGet("https://keycloak.gada.io/auth/admin/realms/" + realm + "/users?first="+pageStart+"&max="+pageSize, accessToken);
			InputStream is = new ByteArrayInputStream(resultsJson.getBytes());
			try {
				 pageResults = JsonSerialization.readValue(is, (new ArrayList<UserRepresentation>()).getClass());
				 for (LinkedHashMap user : pageResults) {
					 String username = (String) user.get("username");
					 if (username.startsWith("gentest")) {
						 results.add(user);
					 }
				 }
          } finally {
              is.close();
          }
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        pageStart += pageSize;
        System.out.println("count = "+pageStart+" with "+results.size()+" gentests");
        } while (!pageResults.isEmpty());
        
//        final HttpClient client = new DefaultHttpClient();
//
//        try {
//            final HttpGet get = new HttpGet(
//                    "https://keycloak.gada.io/auth/admin/realms/" + realm + "/users?first=0&max=200");
//            get.addHeader("Authorization", "Bearer " + accessToken);
//            try {
//                final HttpResponse response = client.execute(get);
//                if (response.getStatusLine().getStatusCode() != 200) {
//                    throw new IOException();
//                }
//                final HttpEntity entity = response.getEntity();
//                final InputStream is = entity.getContent();
//                try {
//                    results = JsonSerialization.readValue(is, (new ArrayList<UserRepresentation>()).getClass());
//                } finally {
//                    is.close();
//                }
//            } catch (final IOException e) {
//                throw new RuntimeException(e);
//            }
//        } finally {
//            client.getConnectionManager().shutdown();
//        }

        Map<String, String> keycloakEmailUuidMap = new HashMap<String, String>();

        System.out.println("Number of keycloak users = " + results.size());
        int count = 0;
        LocalDateTime latest = LocalDateTime.of(2000, 1, 1, 1, 1);
        LocalDateTime earliest = LocalDateTime.of(2200, 1, 1, 1, 1);
        try {
            accessToken = KeycloakUtils.getAccessToken(keycloakUrl, "master", "admin-cli", null, "admin",
                    System.getenv("KEYCLOAK_PASSWORD"));
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        for (LinkedHashMap userMap : results) {
        	Long createdTimestamp = (Long)userMap.get("createdTimestamp");
  //      	Long test_timestamp = Long.parseLong(createdTimestamp);
        	LocalDateTime triggerTime =
        	        LocalDateTime.ofInstant(Instant.ofEpochMilli(createdTimestamp), 
        	                                TimeZone.getDefault().toZoneId());
        	try {
				if (triggerTime.isAfter(latest)) {
					latest = triggerTime;
				}
				if (triggerTime.isBefore(earliest)) {
					earliest = triggerTime;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	String uuid = (String) userMap.get("id");
            String username = (String) userMap.get("username");
            String email = (String) userMap.get("email");
            String code = QwandaUtils.getNormalisedUsername("PER_" + uuid.toUpperCase());
            String id = (String) userMap.get("id");
            keycloakEmailUuidMap.put(email, id);
            String deleteUrl = keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + uuid;
            try {
				KeycloakUtils.sendDELETE(deleteUrl,accessToken);
 
				KeycloakUtils.setPassword(accessToken, realm, uuid, UUID.randomUUID().toString().replaceAll("-", "").substring(0, 15));
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

        log.info("Earliest = "+earliest);
        log.info("Latest = "+latest);
        
	}
	
	
	//@Test
	public void AdamTest2() {
		System.out.println("fix Timezones test");
		GennyToken userToken = null;
		GennyToken serviceToken = null;

		// VertxUtils.cachedEnabled = false;
		VertxUtils.cachedEnabled = false;
		try {
			GennyJbpmBaseTest.init();// .setupLocalService();
			// qRules = GennyJbpmBaseTest.plement();
		} catch (FileNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		userToken = new GennyToken("userToken", GennyJbpmBaseTest.projectParms.getString("userToken"));
		serviceToken = new GennyToken("PER_SERVICE", GennyJbpmBaseTest.projectParms.getString("serviceToken"));
		eventBusMock = new EventBusMock();
		vertxCache = new JunitCache(); // MockCache
		VertxUtils.init(eventBusMock, vertxCache);

		BaseEntityUtils beUtils = new BaseEntityUtils(userToken);
		beUtils.setServiceToken(serviceToken);

		if (beUtils == null) {
			return;
		}

		boolean ok = true;
		Integer pageStart = 0;
		Integer pageSize = 100;

		while (ok) {

			SearchEntity searchBE = new SearchEntity("SBE_GPS", "timezone fix")
					.addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
					.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "PER_%")
					
					.addColumn("PRI_CODE", "Name").addColumn("PRI_ADDRESS_FULL", "Address").addColumn("PRI_TIMEZONE_ID","TimezoneId");

			searchBE.setRealm(realm);
			searchBE.setPageStart(pageStart);
			searchBE.setPageSize(pageSize);
			pageStart += pageSize;

			List<BaseEntity> items = beUtils.getBaseEntitys(searchBE);
			if (items.isEmpty()) {
				ok = false;
				break;
			} else {
				log.info("Loaded "+items.size()+" baseentitys");
			}

			BaseEntity project = beUtils.getBaseEntityByCode("PRJ_" + serviceToken.getRealm().toUpperCase());
			int icount = 0;
			for (BaseEntity item : items) {

				try {
					
					// check if there
					Optional<EntityAttribute> tz = item.findEntityAttribute("PRI_TIMEZONE_ID");
					if (tz.isPresent()) {
						log.info("Already has a Timezone ID "+tz.get().getAsString());
						continue;
					
					}
					
					// check if there
					Optional<EntityAttribute> ea = item.findEntityAttribute("PRI_ADDRESS_FULL");
					if (ea.isPresent()) {

						String address = item.getValue("PRI_ADDRESS_FULL", null);
						if (!StringUtils.isBlank(address)) {
//							Answer activeStatus = new Answer(beUtils.getGennyToken().getUserCode(), item.getCode(),
//									"PRI_STATUS", "ACTIVE");
//							beUtils.saveAnswer(activeStatus);
							log.info(item.getCode() + " address: " + address);

							String encodedAddress = null;
							try {
								encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8.toString());
							} catch (UnsupportedEncodingException ex) {
								throw new RuntimeException(ex.getCause());
							}
							//System.out.println("IMPORT ADDRESS: encodedAddress=" + encodedAddress);

							String googleApiUrl = "https://maps.googleapis.com/maps/api/geocode/json?address="
									+ encodedAddress;

							String googleApiKey = project.getValue("PRI_GOOGLE_API_KEY","");
							//System.out.println("IMPORT ADDRESS: google api key=" + googleApiKey);
							googleApiUrl = googleApiUrl + "&key=" + googleApiKey;

							String addressJsonStr = QwandaUtils.apiGet(googleApiUrl, null);
							JsonObject json = new JsonObject(addressJsonStr);
							if ("OK".equals(json.getString("status"))) {
								JsonObject results = json.getJsonArray("results").getJsonObject(0);
								System.out.println(results);
								JsonArray address_components = results.getJsonArray("address_components");

								/* loop through address_components */
								Integer index = 0;
								Integer count = address_components.size();
								Map<String, String> addressMap = new HashMap<String, String>();
								for (index = 0; index < count; index++) {

									JsonObject addressComponent = address_components.getJsonObject(index);
									JsonArray types = addressComponent.getJsonArray("types");
									String mainType = types.getString(0);
									addressMap.put(mainType, addressComponent.getString("short_name"));
								}

								String streetNumber = (addressMap.get("street_number") == null) ? ""
										: addressMap.get("street_number");
								String streetName = (addressMap.get("route") == null) ? "" : addressMap.get("route");

								String street_address = (streetNumber + " " + streetName).trim();

								JsonObject geometry = results.getJsonObject("geometry");
								JsonObject location = geometry.getJsonObject("location");
								Double lat = location.getDouble("lat");
								Double lng = location.getDouble("lng");
								String full_address = results.getString("formatted_address");
								System.out.println(address_components);

								JsonObject address_json = new JsonObject();
								address_json.put("street_address", street_address);
								address_json.put("suburb",
										(addressMap.get("locality") == null ? "" : addressMap.get("locality")));
								address_json.put("state", (addressMap.get("administrative_area_level_1") == null ? ""
										: addressMap.get("administrative_area_level_1")));
								address_json.put("country",
										(addressMap.get("country") == null ? "" : addressMap.get("country")));
								address_json.put("postcode",
										(addressMap.get("postal_code") == null ? "" : addressMap.get("postal_code")));
								address_json.put("full_address", full_address);
								address_json.put("latitude", lat);
								address_json.put("longitude", lng);

								String PRI_ADDRESS_JSON = address_json.toString();
								System.out.println("PRI_ADDRESS_JSON=" + PRI_ADDRESS_JSON);
								Answer fixedAddress = new Answer(userToken.getUserCode(), item.getCode(),
										"PRI_ADDRESS_JSON", PRI_ADDRESS_JSON, false, false);
								beUtils.saveAnswer(fixedAddress);
								// Timezone

								String url = "https://maps.googleapis.com/maps/api/timezone/json?location=" + lat + ","
										+ lng + "&timestamp=1458000000&key="+googleApiKey;
								String timezoneJsonStr = QwandaUtils.apiGet(url, null);
								json = new JsonObject(timezoneJsonStr);
								String timezoneID = json.getString("timeZoneId");
								Answer fixedTimezone = new Answer(userToken.getUserCode(), item.getCode(),
										"PRI_TIMEZONE_ID", timezoneID, false, false);
								beUtils.saveAnswer(fixedTimezone);
								System.out.println(item.getCode() + " timezone: " + json);

							}

						}

					}

				} catch (Exception e1) {
					e1.printStackTrace();
				}
				icount++;
//				if (icount> 3) {
//					break;
//				}
			}

		}

		System.out.println("Finished");
	}

	@BeforeAll
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

//	        if (USE_STANDALONE) {
//	            serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
//	            VertxUtils.cachedEnabled = true; // don't send to local Service Cache
//	            GennyKieSession.loadAttributesJsonFromResources(serviceToken);
//
//	        } else {
		// qRules = GennyJbpmBaseTest.setupLocalService();
		try {
			GennyJbpmBaseTest.init();// .setupLocalService();
			String uToken = GennyJbpmBaseTest.projectParms.getString("userToken");
			userToken = new GennyToken("userToken", uToken);
			serviceToken = new GennyToken("PER_SERVICE", GennyJbpmBaseTest.projectParms.getString("serviceToken"));

		} catch (FileNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

	//@Test
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
