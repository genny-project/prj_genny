package life.genny.test;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang.StringUtils;
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

import life.genny.eventbus.EventBusInterface;
import life.genny.eventbus.EventBusMock;
import life.genny.eventbus.VertxCache;
import life.genny.models.GennyToken;

import life.genny.qwanda.Answer;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.SearchEntity;
import life.genny.qwanda.message.QBaseMSGMessageType;
import life.genny.qwanda.message.QMessageGennyMSG;
import life.genny.qwandautils.GennyCacheInterface;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.rules.QRules;
import life.genny.utils.BaseEntityUtils;
import life.genny.utils.GennyJbpmBaseTest;

import life.genny.utils.JunitCache;
import life.genny.utils.VertxUtils;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;


public class RogerTest  {
	private static final Logger log = Logger.getLogger(RogerTest.class);


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
	
	public RogerTest() {
		//super();
	}
	
	
	@Test
	public void gennyTest()
	{
		System.out.println("This is a Gennytest");
	}
	
	
	@Test
	public void AdamTest1()
	{
		System.out.println("Local Genny test1 AdamTest");


		
	}
	
	@Test
	public void RogerTest2()
	{
 			System.out.println("message test");
			GennyToken userToken = null;
			GennyToken serviceToken = null;
			QRules qRules = null;


				// VertxUtils.cachedEnabled = false;
				VertxUtils.cachedEnabled = false;
				qRules = GennyJbpmBaseTest.setupLocalService();
				userToken = new GennyToken("userToken", qRules.getToken());
				serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
				eventBusMock = new EventBusMock();
				vertxCache = new JunitCache(); // MockCache
				VertxUtils.init(eventBusMock, vertxCache);

			BaseEntityUtils beUtils = new BaseEntityUtils(userToken);
			beUtils.setServiceToken(serviceToken);
		
		if (beUtils == null) {
			return;
		}

//			SearchEntity searchBE = new SearchEntity("SBE_GPS", "hcrs")
//					.addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
//					.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "CPY_%").addFilter("PRI_IS_HOST_CPY", true)
//					.addColumn("PRI_CODE", "Name")
//					.addColumn("PRI_ADDRESS_FULL", "Address");
//
//			searchBE.setRealm(realm);
//			searchBE.setPageStart(0);
//			searchBE.setPageSize(100000);
//
//			List<BaseEntity> items = beUtils.getBaseEntitys(searchBE);
			QBaseMSGMessageType messageType=QBaseMSGMessageType.SMS;

			BaseEntity project = null;
					//beUtils.getBaseEntityByCode("PRJ_"+serviceToken.getRealm().toUpperCase());
 			BaseEntity evil = beUtils.create("PER_BAD_GUY","Dr Evil");	
 			BaseEntity superHero = beUtils.create("PER_SUPER_HERO","James Bond");	

 			List<Answer> answers = new ArrayList<Answer>();
 			  
 	    	answers.add(new Answer(evil.getCode(),evil.getCode(),"PRI_NAME", "Dr Evil"));
 	  		answers.add(new Answer(evil.getCode(),evil.getCode(),"PRI_PHONE", "61414222333"));


 	  		answers.add(new Answer(superHero.getCode(),superHero.getCode(),"PRI_NAME", "James Bond"));
 	  		answers.add(new Answer(superHero.getCode(),superHero.getCode(),"PRI_FIRSTNAME", "James"));
 	  		
 	  		String mobile = System.getenv("TEST_RECEIVE_MOBILE");
 	  		answers.add(new Answer(superHero.getCode(),superHero.getCode(),"PRI_MOBILE", mobile));

 	  		answers.add(new Answer(superHero.getCode(),superHero.getCode(),"PRI_PHONE", "61400000000"));

 	  		String email = System.getenv("USER_EMAIL");
 	  		answers.add(new Answer(superHero.getCode(),superHero.getCode(),"PRI_EMAIL", email));

			String templateCode = "MSG_GENNY_INVITE";  
 			BaseEntity msgTemplate = beUtils.create(templateCode,"Test message");	
 	    	answers.add(new Answer(msgTemplate.getCode(),msgTemplate.getCode(),"PRI_NAME", "Test Message"));
 	    	answers.add(new Answer(msgTemplate.getCode(),msgTemplate.getCode(),"PRI_SUBJECT", "This is a test message subject."));// This is should appear on email subject
 	    	answers.add(new Answer(msgTemplate.getCode(),msgTemplate.getCode(),"PRI_TITLE", "GADA test message")); // This is the title would appear on message
 	    	answers.add(new Answer(msgTemplate.getCode(),msgTemplate.getCode(),"PRI_BODY", "This is the main body type of the message from {{SUPER_HERO.PRI_NAME}}")); // This is  the body
 	    	answers.add(new Answer(msgTemplate.getCode(),msgTemplate.getCode(),"PRI_HTML", "<html><body><p>This is the <b>html</b> body type of the message from {{BAD_GUY.PRI_NAME}} and {{SUPER_HERO.PRI_NAME}}.</p></body></html>")); // This is  the body
 	    	answers.add(new Answer(msgTemplate.getCode(),msgTemplate.getCode(),"PRI_SHORT_BODY", "This is a short version of the message regarding {{BAD_GUY.PRI_NAME}}")); // This is  the body
 	    	//answers.add(new Answer(msgTemplate.getCode(),msgTemplate.getCode(),"PRI_SHORT_BODY", "This is a short version of the message")); // This is  the body
 	    	answers.add(new Answer(msgTemplate.getCode(),msgTemplate.getCode(),"PRI_CONTEXT_CODES", "[\"SUPER_HERO\",  \"BAD_GUY\" ]"));
 	    	answers.add(new Answer(msgTemplate.getCode(),msgTemplate.getCode(),"PRI_OPTIONAL_CONTEXT_CODES", "[\"LOVE_INTEREST\"]"));

 	    	answers.add(new Answer(msgTemplate.getCode(),msgTemplate.getCode(),"PRI_SEND_DATETIME", ""));
 	    	answers.add(new Answer(msgTemplate.getCode(),msgTemplate.getCode(),"PRI_SEND_MODE", "IMMEDIATELY"));
 	    	answers.add(new Answer(msgTemplate.getCode(),msgTemplate.getCode(),"PRI_MEDIA", "[\"EMAIL\", \"SMS\"]"));
 	    	answers.add(new Answer(msgTemplate.getCode(),msgTemplate.getCode(),"PRI_RECEIVED_CHECK", "TRUE"));
 	    	answers.add(new Answer(msgTemplate.getCode(),msgTemplate.getCode(),"PRI_UNSUBSCRIBE", "TEST")); // The value represents the tag

 	    	beUtils.saveAnswers(answers);

 	    	/*
 	    	 * 
 	    	 * 
 	    	 * PRI_ContextCodes: ['RECIPIENT','SENDER','COMPANY','INTERNSHIP']
				PRI_OptionalContextCodes: ['APPOINTMENT','SUPERVISOR']
				PRI_SEND_DATETIME: (if blank then send immediately) -> use Shleemy to schedule the message , there will also be codes that indicate when this message should be sent .. 
				PRI_SEND_MODE: IMMEDIATELY, UTC, RECIPIENT_DATETIME,SENDER_DATETIME,  RECIPIENT_START_WORKDAY, RECIPIENT_END_WORKDAY, RECEIPIENT_START_WORKWEEK, 
				PRI_MEDIA : EMAIL,SMS  (if SMS, then add a 'SEND YES TO CONFIRM' at end of text)
				PRI_RECEIVED_CHECK: TRUE (send confirmation event message if true)
				**
			 * 
 	    	 */
  			
			String msg_type = "MSG_MESSAGE";
			Map<String, String> contextMap = new HashMap<String, String>();
 			
			contextMap.put("SUPER_HERO", "PER_SUPER_HERO");
			contextMap.put("BAD_GUY", "PER_BAD_GUY");
			contextMap.put("USER", "PER_SUPER_HERO");
			contextMap.put("PROJECT", "PRJ_INTERNMATCH");

 			
			String[] recipientArr = new String[] { superHero.getCode() };
			
			 
			
			QMessageGennyMSG qMessageGennyMSG = new QMessageGennyMSG(msg_type, messageType, templateCode, contextMap, recipientArr, recipientArr);
			qMessageGennyMSG.setToken(qRules.getToken());
			System.out.println(project);
			
		VertxUtils.writeMsg("messages", JsonUtils.toJson(qMessageGennyMSG));
			
//			int icount=0;
//			for (BaseEntity item : items) {
//
//				try {
//					// check if there
//					Optional<EntityAttribute> ea = item.findEntityAttribute("PRI_ADDRESS_FULL");
//					if (ea.isPresent()) {
//
//						String address = item.getValue("PRI_ADDRESS_FULL", null);
//						if (!StringUtils.isBlank(address)) {
////							Answer activeStatus = new Answer(beUtils.getGennyToken().getUserCode(), item.getCode(),
////									"PRI_STATUS", "ACTIVE");
////							beUtils.saveAnswer(activeStatus);
//							log.info(item.getCode()+" address: "+address);
//							
//							String encodedAddress = null;
//							try {
//					            encodedAddress =  URLEncoder.encode(address, StandardCharsets.UTF_8.toString());
//					        } catch (UnsupportedEncodingException ex) {
//					            throw new RuntimeException(ex.getCause());
//					        }
//							System.out.println("IMPORT ADDRESS: encodedAddress="+encodedAddress);
//							
//							String googleApiUrl = "https://maps.googleapis.com/maps/api/geocode/json?address="+encodedAddress;
//							
//							String googleApiKey = "AIzaSyAe8SEl-uMB_8E7HRj8f_X7Nrmfss8svFQ";//project.getValue("PRI_GOOGLE_API_KEY","");
//							System.out.println("IMPORT ADDRESS: google api key="+googleApiKey);
//					        googleApiUrl  = googleApiUrl  + "&key="+googleApiKey;
//					 
//							String addressJsonStr =  QwandaUtils.apiGet(googleApiUrl , null);
//							JsonObject json = new JsonObject(addressJsonStr);
//						if ("OK".equals(json.getString("status"))) {
//							JsonObject results = json.getJsonArray("results").getJsonObject(0);
//							System.out.println(results);
//							JsonArray address_components = results.getJsonArray("address_components");
//							
//							/* loop through address_components */
//							Integer index = 0;
//							Integer count = address_components.size();
//							Map<String,String> addressMap = new HashMap<String,String>();
//							for (index=0;index<count;index++) {
//								
//								JsonObject addressComponent = address_components.getJsonObject(index);
//								JsonArray types = addressComponent.getJsonArray("types");
//								String mainType = types.getString(0);
//								addressMap.put(mainType, addressComponent.getString("short_name"));
//							}
//							
//							String streetNumber = (addressMap.get("street_number")==null)?"":addressMap.get("street_number");
//							String streetName = (addressMap.get("route")==null)?"":addressMap.get("route");
//							
//							String street_address = (streetNumber+" "+streetName).trim();
//							
//							JsonObject geometry = results.getJsonObject("geometry");
//							JsonObject location = geometry.getJsonObject("location");
//							Double lat = location.getDouble("lat");
//							Double lng = location.getDouble("lng");
//							String full_address = results.getString("formatted_address");
//							System.out.println(address_components);
//							
//							JsonObject address_json = new JsonObject();
//							address_json.put("street_address", street_address);
//							address_json.put("suburb", (addressMap.get("locality")==null?"":addressMap.get("locality")));
//							address_json.put("state",  (addressMap.get("administrative_area_level_1")==null?"":addressMap.get("administrative_area_level_1")));
//							address_json.put("country",  (addressMap.get("country")==null?"":addressMap.get("country")));
//							address_json.put("postcode",  (addressMap.get("postal_code")==null?"":addressMap.get("postal_code")));
//							address_json.put("full_address", full_address);
//							address_json.put("latitude", lat);
//							address_json.put("longitude", lng);
//							
//							String PRI_ADDRESS_JSON = address_json.toString();
//							System.out.println("PRI_ADDRESS_JSON="+PRI_ADDRESS_JSON);		
//							Answer fixedAddress = new Answer(userToken.getUserCode(),item.getCode(), "PRI_ADDRESS_JSON", PRI_ADDRESS_JSON,false,false);			
//							beUtils.saveAnswer(fixedAddress);
//							// Timezone
//
//							String url = "https://maps.googleapis.com/maps/api/timezone/json?location="+lat+","+lng+"&timestamp=1458000000&key=AIzaSyAe8SEl-uMB_8E7HRj8f_X7Nrmfss8svFQ";
//							String timezoneJsonStr =  QwandaUtils.apiGet(url , null);
//							json = new JsonObject(timezoneJsonStr);
//							String timezoneID = json.getString("timeZoneId");
//							Answer fixedTimezone = new Answer(userToken.getUserCode(),item.getCode(), "PRI_TIMEZONE_ID", timezoneID,false,false);			
//							beUtils.saveAnswer(fixedTimezone);
//							System.out.println(item.getCode()+" timezone: "+json);
//
//							}
//
//							
//						}
//
//					}
//
//				} catch (Exception e1) {
//					e1.printStackTrace();
//				}
//				icount++;
////				if (icount> 3) {
////					break;
////				}
//			}

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
//	        } else {fl
	            qRules = GennyJbpmBaseTest.setupLocalService();
	            userToken = new GennyToken("userToken", qRules.getToken());
	            serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
	            
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
}
