package life.genny.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.json.Json;
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

import io.vavr.Tuple2;
import io.vertx.core.json.JsonObject;
import life.genny.eventbus.EventBusInterface;
import life.genny.eventbus.EventBusMock;

import life.genny.models.GennyToken;
import life.genny.qwanda.Answer;
import life.genny.qwanda.EEntityStatus;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeText;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.datatype.Allowed;
import life.genny.qwanda.datatype.CapabilityMode;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.SearchEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.message.QCmdMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwandautils.GennyCacheInterface;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.KeycloakUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.utils.BaseEntityUtils;
import life.genny.utils.RulesUtils;
import life.genny.utils.VertxUtils;
import java.util.Base64;
import life.genny.qwandautils.JsonUtils;
import life.genny.eventbus.VertxCache;



public class EmailSearchTest {

    private static final org.jboss.logging.Logger log = Logger.getLogger(EmailSearchTest.class);

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


    protected static BaseEntityUtils beUtils;

    public EmailSearchTest() {
        super();
    }



@Test
public void emailTest() throws Exception {
    VertxUtils.cachedEnabled = false;

    if (beUtils == null) {
        return;
    }

    setUpDefs();
    
    BaseEntity userBe = beUtils.getPersonFromEmail("testuser@gmail.com");
    Allowed allowed = new Allowed("EDIT_AGENT",CapabilityMode.EDIT);
 
    List<Answer> answersToSave = new ArrayList<>();
    Answer answer = new Answer(userBe.getCode(),"PER_2BD3424C-5BAF-4A11-B6A1-90F998BF5943","PRI_EMAIL","eduard_neira@hotmail.com");
    
	System.out.println("Email test   user=" + beUtils.getGennyToken().getUserCode()+" : "+answer); 
		
	String sourceCode = beUtils.getGennyToken().getUserCode();
	String targetCode = answer.getTargetCode();
	JsonObject resultJson = null;
	String duplicateCodes = null;
	String number = null; 
	
	/* check if email is already in use for a person */
	
	SearchEntity searchBE = new SearchEntity("SBE_PRI_EMAIL_TEST", "Email People")
			.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "PER_%") 
			.addFilter("PRI_EMAIL", SearchEntity.StringFilter.EQUAL, answer.getValue()) 
			.addColumn("PRI_NAME", "Name")
			.setPageStart(0)
			.setPageSize(100);
	
	searchBE.setRealm(beUtils.getServiceToken().getRealm());
		
	/* Check number of duplicates */
	Tuple2<String, List<String>> data = beUtils.getHql(searchBE);
	String hql = data._1;

	hql = Base64.getUrlEncoder().encodeToString(hql.getBytes());
	try {
		String resultJsonStr = QwandaUtils.apiGet(
				GennySettings.qwandaServiceUrl + "/qwanda/baseentitys/count24/" + hql ,
				beUtils.getServiceToken().getToken(), 120);

		System.out.println("  Search EMAIL result is "+resultJsonStr);
		
		if (resultJsonStr.equals("0")) {
			System.out.println("  This email "+answer.getValue()+" is unique");
			/* Now sync to their keycloak account */
		/*	if (answer.getValue().startsWith("adamcrow")) { */
			BaseEntity target = beUtils.getBaseEntityByCode(answer.getTargetCode());
			if ((answer.getTargetCode().startsWith("PER_")) && (target != null)) {
			
				Boolean allowedToChange = false;
				String keycloakUUID = answer.getTargetCode().substring("PER_".length());
				if (userToken.getUserCode().equals(answer.getTargetCode())) {
					allowedToChange = true;
				}
				System.out.println(" the allowed.code is "+allowed.code);
				/* check if capability to change passwords is there */
				switch (allowed.code) {
				case "EDIT_INTERN": {allowedToChange = true;} break;
				case "EDIT_AGENT":  {allowedToChange = true;} break;
				case "EDIT_HCR":    {allowedToChange = true;} break;
				case "EDIT_EPR":    {allowedToChange = true;} break;
				case "EDIT_REF_PART_REP": {allowedToChange = true;} break;
				}

			
				if (allowedToChange) {
					int statusCode =  KeycloakUtils.updateUserEmail(keycloakUUID,serviceToken.getToken(), userToken.getRealm(),answer.getValue());
					System.out.println("  Changing email for "+answer.getTargetCode()+" to "+answer.getValue()+" statusCode="+statusCode);
					if (statusCode > 204) {
						System.out.println("  Error in setting email."+answer.getTargetCode()+" to "+answer.getValue()+" statusCode="+statusCode);
					} else {
						System.out.println("  Saving the good email");
						Answer goodEmail = new Answer(answer.getSourceCode(), answer.getTargetCode(), "PRI_EMAIL", answer.getValue());
						answersToSave.add(goodEmail);
					}
					
				
				}
				
			/*}*/
		}	
			
		} else {
				if(true /*allowed.code.equals("AGENT")*/) { /* TODO , why limit this to agents??? */
					List<BaseEntity> duplicateBes = beUtils.getBaseEntitys(searchBE);
					System.out.println("  duplicateBE's: " + duplicateBes);
					
					QDataBaseEntityMessage msg = new QDataBaseEntityMessage(duplicateBes);
					msg.setToken(userToken.getToken());
					msg.setReplace(true);
					VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg));
					
					for (BaseEntity duplicateBe : duplicateBes) {
							System.out.println("  duplicateBe: " + duplicateBe);
							duplicateCodes = duplicateCodes + "," + duplicateBe.getCode();
							System.out.println("  duplicateCodes: " + duplicateCodes);
					}
					
					duplicateCodes = duplicateCodes.substring(5);
					System.out.println("  Final duplicateCodes: " + duplicateCodes);
					
					QCmdMessage duplicateEmailMsg = new QCmdMessage("DUPLICATE_EMAILS", duplicateCodes);
					duplicateEmailMsg.setMessage(number);
					duplicateEmailMsg.setToken(beUtils.getGennyToken().getToken());
					duplicateEmailMsg.setSend(true);
					VertxUtils.writeMsg("webcmds", duplicateEmailMsg);
					
					
					if (resultJsonStr.equals("0")) {
						number = "There is 1 duplicate email";
					} else {
						number = "There are " + resultJsonStr + " duplicate emails";
					}
					
					QCmdMessage toastMsg = new QCmdMessage("TOAST", "INFO");
					toastMsg.setMessage(number);
					toastMsg.setToken(beUtils.getGennyToken().getToken());
					toastMsg.setSend(true);
					VertxUtils.writeMsg("webcmds", toastMsg);
				}
				
				
				String message = "Email Address "+answer.getValue()+" is already taken. Please delete any unwanted copies or merge data with an existing";
				System.out.println(" "+message);
				Answer feedbackAnswer = new Answer(answer.getSourceCode(),answer.getTargetCode(),answer.getAttributeCode(),"");
				VertxUtils.sendFeedbackError(beUtils.getGennyToken(),feedbackAnswer,message);
				System.out.println(" Sent error message back to frontend");
		
		}
	} catch (Exception e) {
	
	}
    

  



}



    public void setUpDefs() throws BadDataException {
        BaseEntityUtils beUtils = new BaseEntityUtils(userToken);
        beUtils.setServiceToken(serviceToken);

        SearchEntity searchBE = new SearchEntity("SBE_DEF", "DEF check")
                .addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
                .addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "DEF_%")
                .addColumn("PRI_CODE", "Name");

        searchBE.setRealm(realm);
        searchBE.setPageStart(0);
        searchBE.setPageSize(1000);

        List<BaseEntity> items = beUtils.getBaseEntitys(searchBE);
        // Load up RuleUtils.defs

        RulesUtils.defs.put(realm,new ConcurrentHashMap<String,BaseEntity>());

        for (BaseEntity item : items) {
//            if the item is a def appointment, then add a default datetime for the start (Mandatory)
            if (item.getCode().equals("DEF_APPOINTMENT")){
                Attribute attribute = new AttributeText("DFT_PRI_START_DATETIME", "Default Start Time");
                attribute.setRealm(realm);
                EntityAttribute newEA = new EntityAttribute(item, attribute, 1.0, "2021-07-28 00:00:00");
                item.addAttribute(newEA);

                Optional<EntityAttribute> ea = item.findEntityAttribute("ATT_PRI_START_DATETIME");
                if (ea.isPresent()){
                    ea.get().setValue(true);
                }
            }

//            Save the BaseEntity created
            item.setFastAttributes(true); // make fast
            RulesUtils.defs.get(realm).put(item.getCode(),item);
            log.info("Saving ("+realm+") DEF "+item.getCode());
        }
    }
    
    @BeforeAll
    public static void init() throws FileNotFoundException, SQLException {

        System.out.println("BridgeUrl=" + GennySettings.bridgeServiceUrl);
        System.out.println("QwandaUrl=" + GennySettings.qwandaServiceUrl);

        // Set up realm
        realms = new HashSet<String>();
        realms.add(realm);
        // realms.stream().forEach(System.out::println);
        realms.remove("genny");

        // Enable the PseudoClock using the following system property.
        System.setProperty("drools.clockType", "pseudo");

        eventBusMock = new EventBusMock();
        vertxCache = new life.genny.eventbus.VertxCache(); // MockCache
        VertxUtils.init(eventBusMock, vertxCache);

         String apiUrl = GennySettings.projectUrl + "/api/events/init?url=" + GennySettings.projectUrl;
        System.out.println("Fetching setup info from " + apiUrl);
        try {
            javax.json.JsonObject projectParms = null;
            String keycloakJson = QwandaUtils.apiGet(apiUrl, null);
            JsonReader jsonReader = Json.createReader(new StringReader(keycloakJson));
            projectParms = jsonReader.readObject();
            jsonReader.close();

            String authServer = projectParms.getString("auth-server-url");
            authServer = StringUtils.removeEnd(authServer, "/auth");
            javax.json.JsonObject credentials = projectParms.getJsonObject("credentials");
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
            e.printStackTrace();
        } catch (IOException e) {
            return;
        }
        serviceToken = new GennyToken("PER_SERVICE", serviceToken.getToken());
        VertxUtils.cachedEnabled = false;

        beUtils = new BaseEntityUtils(userToken);
        beUtils.setServiceToken(serviceToken);

       
    }

 
    
}