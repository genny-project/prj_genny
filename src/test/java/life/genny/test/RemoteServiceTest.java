package life.genny.test;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import io.vertx.core.json.JsonObject;
import life.genny.bootxport.bootx.DEFBaseentityAttribute;
import life.genny.eventbus.EventBusInterface;
import life.genny.eventbus.EventBusMock;
import life.genny.eventbus.VertxCache;
import life.genny.qwanda.*;
import life.genny.qwanda.attribute.AttributeText;
import life.genny.qwanda.message.QEventBtnClickMessage;
import life.genny.qwandautils.*;
import life.genny.utils.*;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;

import life.genny.models.GennyToken;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.SearchEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.message.QCmdTableMessage;

import org.dmg.pmml.Entity;
import org.dmg.pmml.True;
import org.infinispan.client.hotrod.Search;
import org.jboss.logging.Logger;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.admin.ProcessInstanceAdminService;
import org.jbpm.services.api.model.DeploymentUnit;
import org.jbpm.services.api.query.QueryService;
import org.jbpm.services.api.utils.KieServiceConfigurator;
import org.json.JSONObject;
import org.junit.Rule;
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
//          eventBusMock = new EventBusMock();
//          vertxCache = new JunitCache(); // MockCache
//          VertxUtils.init(eventBusMock, vertxCache);
//
//          qRules = GennyJbpmBaseTest.setupLocalService();
//          userToken = new GennyToken("userToken", qRules.getToken());
//          serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());

        beUtils = new BaseEntityUtils(userToken);
        beUtils.setServiceToken(serviceToken);
//         }

        // System.out.println("serviceToken=" + serviceToken.getToken());

    }


    @Test
    public void remoteServicesTest() throws Exception {
        VertxUtils.cachedEnabled = false;

        if (beUtils == null) {
            return;
        }

//        Set up the defs
        setUpDefs();

//      Use a search BE for the api test
        SearchEntity searchBEForApi = new SearchEntity("SBE_AI_JOURNAL", "JNL Search")
                .addSort("PRI_JOURNAL_DATE", "Created", SearchEntity.Sort.ASC)
                .addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "JNL_%")
                .addColumn("PRI_CODE", "Code")
                .addColumn("PRI_JOURNAL_LEARNING_OUTCOMES", "LearningOutcomes")
                .addColumn("PRI_JOURNAL_TASKS","JournalTasks")
                .addAssociatedColumn("LNK_INTERN", "Intern Name", "PRI_NAME")
                .addColumn("PRI_STATUS","Status");
        searchBEForApi.setRealm(realm);
        searchBEForApi.setPageStart(0);
        searchBEForApi.setPageSize(1000);

        beUtils.saveBaseEntity(searchBEForApi);

//       Get an authentication token
        String authToken = userToken.getToken();

        // create 5 remote services
        BaseEntity remoteServiceTest1 = createRemoteService("RMS_JNL_NLP_01", "NLP Journals", "http://localhost:5000/api/response","PER_3F1B3AC3-F8AA-4C20-B85D-4D43C4E2BF7D", "SBE_AI_JOURNAL");
        BaseEntity remoteServiceTest2 = createRemoteService("RMS_HC_RECSYS_01", "Recommended Host Companies", "http://localhost:5001/api/response", "PER_3F1B3AC3-F8AA-4C20-B85D-4D43C4E2BF7D","SBE_AI_JOURNAL");
        BaseEntity remoteServiceTest3 = createRemoteService("RMS_JNL_NLP_02", "NLP Journals Sub Research", "http://10.12.13.1:5002/api/response", "PER_3F1B3AC3-F8AA-4C20-B85D-4D43C4E2BF7D","SBE_AI_JOURNAL");
        BaseEntity remoteServiceTest4 = createRemoteService("RMS_INTERN_RECSYS_02", "Recommended Interns", "http://10.12.13.1:5003/api/response","PER_3F1B3AC3-F8AA-4C20-B85D-4D43C4E2BF7D","SBE_AI_JOURNAL");
        BaseEntity remoteServiceTest5 = createRemoteService("RMS_INTERN_CRED_01", "ID Intern Credentials", "http://ai.gada.io/interns/cred/api/response","PER_3F1B3AC3-F8AA-4C20-B85D-4D43C4E2BF7D","SBE_AI_JOURNAL");

//      Search Entity to be displayed as a table
        SearchEntity searchRemoteServices = new SearchEntity("SBE_REMOTE_SERVICES", "Remote AI Services")
                .addSort("PRI_NAME","Name", SearchEntity.Sort.ASC)
                .addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "RMS_%")
                .addColumn("PRI_NAME", "Name")
                .addColumn("PRI_URL", "URL")
                .addColumn("PRI_DESCRIPTION", "Description")
                .addAssociatedColumn("LNK_AUTHOR", "PRI_NAME", "Author Name")
                /* Add Action */
                .addAction("PRI_EVENT_VIEW_REMOTE_JOBS", "View Remote Jobs");
        searchRemoteServices.setRealm(realm);
        searchRemoteServices.setPageStart(0);
        searchRemoteServices.setPageSize(1000);

        System.out.println(searchRemoteServices);


        VertxUtils.putObject(beUtils.getGennyToken().getRealm(), "", searchRemoteServices.getCode(), searchRemoteServices,
                beUtils.getGennyToken().getToken());
//        beUtils.saveBaseEntity(searchRemoteServices);

        List<BaseEntity> remoteServiceBES = beUtils.getBaseEntitys(searchRemoteServices);

// Display this table search on Alysson
        String searchCode = "SBE_REMOTE_SERVICES";
        System.out.println("searchCode  ::  " + searchCode);
        TableUtils tableUtils = new TableUtils(beUtils);
        SearchEntity searchBE = searchRemoteServices;
//        SearchEntity searchBE = tableUtils.getSessionSearch(searchCode);
        if(searchBE != null) {
            /* Reset page to start */
//            Answer pageAnswer = new Answer(beUtils.getGennyToken().getUserCode(), searchBE.getCode(),
//                    "SCH_PAGE_START", "0");
//            Answer pageNumberAnswer = new Answer(beUtils.getGennyToken().getUserCode(), searchBE.getCode(),
//                    "PRI_INDEX", "1");

//            searchBE = beUtils.updateBaseEntity(searchBE, pageAnswer, SearchEntity.class);
//            searchBE = beUtils.updateBaseEntity(searchBE, pageNumberAnswer, SearchEntity.class);

            VertxUtils.putObject(beUtils.getGennyToken().getRealm(), "", searchBE.getCode(), searchBE,
                    beUtils.getGennyToken().getToken());

        }else{
            System.out.println("searchBE is null");
        }


//  Create Remote Service Jobs
        BaseEntity remoteJob1 = createRemoteJob("RJB_NLP_01","NLP Remote Job ADL","RMS_JNL_NLP_01");
        BaseEntity remoteJob2 = createRemoteJob("RJB_REC_01","Rec Sys Host Companies","RMS_HC_RECSYS_01");
        BaseEntity remoteJob3 = createRemoteJob("RJB_NLP_02","NLP Remote Job MEL","RMS_JNL_NLP_02");
        BaseEntity remoteJob4 = createRemoteJob("RJB_REC_02","Rec Sys Intern Remote Job","RMS_INTERN_RECSYS_02");
        BaseEntity remoteJob5 = createRemoteJob("RJB_CRED_01","Intern CRED Remote Job","RMS_INTERN_CRED_01");

//  List Remote Service Jobs
        //      Search Entity to be displayed as a table
        SearchEntity searchRemoteJobs = new SearchEntity("SBE_REMOTE_JOB", "Remote AI Jobs")
                .addSort("PRI_NAME","Name", SearchEntity.Sort.ASC)
                .addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "RJB_%")
                .addColumn("PRI_NAME","Job Name")
                .addColumn("PRI_STATUS","Status")
                .addAssociatedColumn("LNK_REMOTE_SERVICE", "PRI_CODE", "RemoteServiceCode");
        searchRemoteJobs.setRealm(realm);
        searchRemoteJobs.setPageStart(0);
        searchRemoteJobs.setPageSize(1000);

        VertxUtils.putObject(beUtils.getGennyToken().getRealm(), "", searchRemoteJobs.getCode(), searchRemoteJobs,
                beUtils.getGennyToken().getToken());
//        beUtils.saveBaseEntity(searchRemoteJobs);

        List<BaseEntity> remoteJobBES = beUtils.getBaseEntitys(searchRemoteJobs);

// Send the display table data
        QCmdTableMessage msg = new QCmdTableMessage("SBE_TEST","RemoteServiceTest");
        JsonObject msgJson = new JsonObject();
        msgJson.put("cmd_type","DISPLAY");
        msgJson.put("code","TABLE");
        msgJson.put("exec",true);
        msgJson.put("msg_type","CMD_MSG");
        msgJson.put("option","EXEC");
        msgJson.put("send",true);
        msgJson.put("token",beUtils.getGennyToken().getToken());

        VertxUtils.writeMsg("webcmds",msgJson.toString());
        long totalTime = TableUtils.searchTable(beUtils,searchBE, true);
        System.out.println("total took " + (totalTime) + " ms");
        /* Send out the Filter question group */
        TableUtils.sendFilterQuestions(beUtils, searchBE.getCode());
        VertxUtils.writeMsgEnd(beUtils.getGennyToken());


//  Trigger one of the Remote Service Jobs



//  Progress Status




//  Fetch results from remote service



//  Send the results to Alysson - Table of results






//      Make the api call
//        String apiPostRequest = QwandaUtils.apiPostEntity2(apiRoute, remoteServiceTest.getValueAsString("LNK_SEARCH_BES"), authToken , null);
//        System.out.println(apiPostRequest);
    }


    public BaseEntity createRemoteService(String code, String name, String url, String authorCode,String... searchBECodes) throws Exception {

        // Use this bit to test creation of BEs by using their DEF_ names
        BaseEntity remoteServiceDef = beUtils.getDEFByCode("DEF_REMOTE_SERVICE");
        BaseEntity remoteServiceBE = beUtils.create(remoteServiceDef, name, code);
        remoteServiceBE.setValue("PRI_NAME",name);
        remoteServiceBE.setValue("PRI_URL",url);
        remoteServiceBE.setValue("LNK_SEARCH_BES", "[\""+searchBECodes[0]+"\"]");
        Attribute lnkAuthorAttr = RulesUtils.getAttribute("LNK_AUTHOR", beUtils.getGennyToken().getToken());
        remoteServiceBE.addAnswer(new Answer(remoteServiceBE, remoteServiceBE, lnkAuthorAttr, "[\""+authorCode+"\"]"));
//        remoteServiceBE.setValue("LNK_AUTHOR","[\""+authorCode+"\"]");
        remoteServiceBE.setStatus(EEntityStatus.ACTIVE);
        beUtils.saveBaseEntity(remoteServiceBE);
        return remoteServiceBE;
    }

    public BaseEntity createRemoteJob(String code, String name, String... remoteServiceCodes) throws Exception{

        BaseEntity remoteJobDef = beUtils.getDEFByCode("DEF_REMOTE_JOB");
        BaseEntity remoteJobBE = beUtils.create(remoteJobDef, name, code);
        remoteJobBE.setValue("PRI_NAME", name);
        Attribute lnkRemoteService = RulesUtils.getAttribute("LNK_REMOTE_SERVICE", beUtils.getGennyToken().getToken());
        remoteJobBE.addAnswer(new Answer(remoteJobBE, remoteJobBE, lnkRemoteService, "[\""+remoteServiceCodes[0]+"\"]"));
//        remoteJobBE.setValue("LNK_REMOTE_SERVICE", "[\""+remoteServiceCodes[0]+"\"]");
        remoteJobBE.setValue("PRI_STATUS", "PROCESSING");
        remoteJobBE.setStatus(EEntityStatus.ACTIVE);
        beUtils.saveBaseEntity(remoteJobBE);
        return remoteJobBE;

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

}