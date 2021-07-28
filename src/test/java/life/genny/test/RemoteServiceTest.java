package life.genny.test;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.text.AttributedString;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import life.genny.eventbus.EventBusInterface;
import life.genny.eventbus.EventBusMock;
import life.genny.eventbus.VertxCache;
import life.genny.qwanda.*;
import life.genny.qwanda.attribute.AttributeBoolean;
import life.genny.qwanda.attribute.AttributeText;
import life.genny.qwandautils.*;
import life.genny.utils.*;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;

import com.google.gson.reflect.TypeToken;

import io.vertx.core.json.JsonObject;
import life.genny.models.Frame3;
import life.genny.models.GennyToken;
import life.genny.models.TableData;
import life.genny.models.Theme;
import life.genny.models.ThemeAttribute;
import life.genny.models.ThemeAttributeType;
import life.genny.models.ThemePosition;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.datatype.DataType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.entity.SearchEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.message.QBulkMessage;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.validation.Validation;
import life.genny.qwanda.validation.ValidationList;
import life.genny.rules.QRules;

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

import javax.json.Json;
import javax.json.JsonReader;
import javax.persistence.EntityManagerFactory;



public class RemoteServiceTest {

    private static final org.jboss.logging.Logger log = Logger.getLogger(RemoteServiceTest.class);

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

    public RemoteServiceTest() {
        super();
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
        vertxCache = new VertxCache(); // MockCache
        VertxUtils.init(eventBusMock, vertxCache);

        // QRules qRules = null;

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


    @Test
    public void createRemoteService() throws Exception {
        VertxUtils.cachedEnabled = false;

        if (beUtils == null) {
            return;
        }

//        Set up the defs
        setUpDefs();

        BaseEntity previousItem = beUtils.getBaseEntityByCode("APT_289F4E9B-264E-4E25-B43C-118BE6B0");

        if (previousItem!=null){
            System.out.println(previousItem);
        }

        // Use this bit to test creation of BEs by using their DEF_ names
        // For example im creating an Appointment BE
        BaseEntity remoteJobBE = create("DEF_INTERN");
        System.out.println(remoteJobBE);
        remoteJobBE.setStatus(EEntityStatus.PENDING);
        for(EntityAttribute ea : remoteJobBE.getBaseEntityAttributes()){
            System.out.println(ea);
        }


//        List<BaseEntity> searchItems = beUtils.getBaseEntitys(searchBE);
//        System.out.println(searchItems);

//        String remoteServiceID = "Python-Job-1";
//        String remoteServiceUUID = RemoteServiceUtils.InvokeRemoteService(remoteServiceID, searchItems);
//
//        System.out.println(remoteServiceUUID);

    }

    public void setUpDefs() throws BadDataException {
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

    public BaseEntity create(final String defCode) throws Exception{
        return create(beUtils, defCode);
    }

    public BaseEntity create(final BaseEntityUtils localBeUtils, final String defCode) throws Exception{
        String localRealm = localBeUtils.getGennyToken().getRealm();
        BaseEntity defBE = RulesUtils.defs.get(localRealm).get(defCode);
        return create(localBeUtils, defBE);
    }

    public BaseEntity create(final BaseEntityUtils localBeUtils, final BaseEntity defBE) throws Exception{
        return create(localBeUtils, defBE, null, null);
    }

    public BaseEntity create(final BaseEntityUtils localBeUtils, final BaseEntity defBE, String name) throws Exception{
        return create(localBeUtils, defBE, name, null);
    }


    public BaseEntity create(final BaseEntityUtils localBeUtils, final BaseEntity defBE, String name, String code) throws Exception{
        BaseEntity item = null;
        Optional<EntityAttribute> uuidEA = defBE.findEntityAttribute("ATT_PRI_UUID");
        if (uuidEA.isPresent()){
            // if the defBE is a user without an email provided, create a keycloak acc using a unique random uuid
            String randomEmail = "random+" + UUID.randomUUID().toString().substring(0,20) + "@gada.io";
            item = createUser(localBeUtils, defBE, randomEmail);
        }
        if (item == null){
            String prefix = defBE.getValueAsString("PRI_PREFIX");
            if (StringUtils.isBlank(prefix)){
                log.error("No prefix set for the def: "+ defBE.getCode());
                throw new Exception("No prefix set for the def: "+ defBE.getCode());
            }
            if (StringUtils.isBlank(code)){
                code = prefix + "_" + UUID.randomUUID().toString().substring(0,32).toUpperCase();
            }

            if (StringUtils.isBlank(name)){
                name = defBE.getName();
            }
            item = new BaseEntity(code.toUpperCase(), name);

            // Establish all mandatory base entity attributes
            for(EntityAttribute ea : defBE.getBaseEntityAttributes()){
                if (ea.getAttribute().getCode().startsWith("ATT_")){
//                    Only process mandatory attributes
                    if(ea.getValueBoolean()){
                        String attrCode = ea.getAttributeCode().substring("ATT_".length());
                        Attribute attribute = RulesUtils.getAttribute(attrCode, localBeUtils.getGennyToken().getToken());

                        String defaultDefValue = "DFT_" + attrCode;

                        String value = defBE.getValue(defaultDefValue, attribute.getDefaultValue());

                        EntityAttribute newEA = new EntityAttribute(item, attribute, ea.getWeight(),value);

                        item.addAttribute(newEA);
                    }

                }
            }

        }
        localBeUtils.saveBaseEntity(item);
        return item;
    }

    public BaseEntity createUser(final BaseEntityUtils localBeUtils, final BaseEntity defBE, final String email) throws Exception {
        BaseEntity item = null;
        String uuid = null;
        Optional<EntityAttribute> uuidEA = defBE.findEntityAttribute("ATT_PRI_UUID");
        if (uuidEA.isPresent()){

            if (!StringUtils.isBlank(email)){
//                TODO: run a regexp check to see if the email is valid

                if (!email.startsWith("random+")){
                    //  Check to see if the email exists
//                    TODO: check to see if the email exists in the database and keycloak
                }
            }
        // this is a user, generate keycloak id
        uuid = KeycloakUtils.createDummyUser(serviceToken.getToken(), serviceToken.getRealm());
            Optional<String> optCode = defBE.getValue("PRI_PREFIX");
            if (optCode.isPresent()){
                String name = defBE.getName();
                item = new BaseEntity(optCode.get() + "_" + uuid.toUpperCase(), name);
                //Add PRI_UUID
                //Add Email
                if (!email.startsWith("random+")){
                    //  Check to see if the email exists
//                    TODO: check to see if the email exists in the database and keycloak
                    Attribute emailAttribute = RulesUtils.getAttribute("PRI_EMAIL", localBeUtils.getGennyToken().getToken());
                    item.addAnswer(new Answer(item, item, emailAttribute, email));
                }

                Attribute uuidAttribute = RulesUtils.getAttribute("PRI_UUID", localBeUtils.getGennyToken().getToken());
                item.addAnswer(new Answer(item, item, uuidAttribute, uuid.toUpperCase()));

            }else{
                log.error("Prefix not provided");
                throw new Exception("Prefix not provided" + defBE.getCode());
            }
        }else{
            log.error("Passed defBE is not a user def!");
            throw new Exception("Passed defBE is not a user def!" + defBE.getCode());
        }

        return item;
    }



}
