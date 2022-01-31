package life.genny.test;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import io.vertx.core.json.JsonObject;
import life.genny.eventbus.EventBusInterface;
import life.genny.eventbus.EventBusMock;
import life.genny.eventbus.VertxCache;

import life.genny.qwanda.*;
import life.genny.qwanda.attribute.AttributeText;
import life.genny.qwanda.message.*;
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
import javax.json.bind.JsonbBuilder;
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


        String apiUrl = GennySettings.projectUrl + "/api/events/init?url=" + GennySettings.projectUrl;
        System.out.println("Fetching setup info from " + apiUrl);
        try {
            javax.json.JsonObject projectParms = null;
            String keycloakJson = QwandaUtils.apiGet(apiUrl, null);
            javax.json.bind.Jsonb jsonb = JsonbBuilder.create();
            // JsonReader jsonReader = Json.createReader(new StringReader(keycloakJson));
            projectParms = jsonb.fromJson(keycloakJson, javax.json.JsonObject.class);
            // jsonReader.close();


            String authServer = projectParms.getString("ENV_KEYCLOAK_REDIRECTURI");
            authServer = StringUtils.removeEnd(authServer, "/auth");
            //    javax.json.JsonObject credentials = projectParms.getJsonObject("credentials");
            //    String secret = credentials.getString("secret");
            String username = System.getenv("USERNAME");
            String password = System.getenv("PASSWORD");

            String token = KeycloakUtils.getAccessToken(authServer, realm, "alyson",null, username, password);
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

            String serviceTokenStr = KeycloakUtils.getAccessToken(authServer, realm, "alyson", null, "service",
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

        DefUtils.loadDEFS(realm, serviceToken);

    }

    @Test
    public void apiTest() throws Exception {
        if (beUtils == null) {
            return;
        }
        SearchEntity internSearch = new SearchEntity("SBE_INTERNS", "INTERN SEARCH")
                .addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "PER_%")
                .addFilter("PRI_IS_INTERN", true)
                .addFilter("LNK_EDU_PROVIDER", SearchEntity.StringFilter.EQUAL, "[\"CPY_9D998907-4A2E-44B9-956F-B0FB6645C1AD\"]")
                .addColumn("PRI_CODE", "Intern Code")
                .addColumn("PRI_ASSOC_CURRENT_SOFTWARE", "CurrentSoftware")
                .addColumn("PRI_ASSOC_FUTURE_SOFTWARE", "FutureSoftware")
                .addColumn("PRI_STATUS","Status");
        internSearch.setRealm(realm);
        internSearch.setPageStart(0);
        internSearch.setPageSize(24885);

//        SearchEntity jnlSearch = new SearchEntity("SBE_AI_JOURNAL", "JNL SEARCH")
//                .addSort("PRI_JOURNAL_DATE", "Created", SearchEntity.Sort.ASC)
//                .addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "JNL_%")
//                .addColumn("PRI_CODE", "Code")
//                .addColumn("PRI_JOURNAL_LEARNING_OUTCOMES", "LearningOutcomes")
//                .addColumn("PRI_JOURNAL_TASKS","JournalTasks")
//                .addAssociatedColumn("LNK_INTERN", "PRI_CODE", "InternCode")
//                .addFilter("LNK_INTERN.LNK_EDU_PROVIDER", SearchEntity.StringFilter.EQUAL, "[\"CPY_9D998907-4A2E-44B9-956F-B0FB6645C1AD\"]")
//                .addColumn("PRI_STATUS","Status");
//        jnlSearch.setRealm(realm);
//        jnlSearch.setPageStart(0);
//        jnlSearch.setPageSize(24885);

//        SearchEntity internSearch = new SearchEntity("SBE_INTERNS", "INTERN SEARCH")
//                .addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "PER_%")
//                .addFilter("PRI_IS_INTERN", true)
//                .addColumn("PRI_CODE", "Intern Code")
//                .addFilter("LNK_EDU_PROVIDER", SearchEntity.StringFilter.EQUAL, "[\"CPY_9D998907-4A2E-44B9-956F-B0FB6645C1AD\"]")
//                .addColumn("PRI_STATUS","Status");
//        internSearch.setRealm(realm);
//        internSearch.setPageStart(0);
//        internSearch.setPageSize(24885);

        String apiRoute = "http://127.0.0.1:5000/api/response";
//        String authToken = beUtils.getGennyToken().getToken();
        String authToken = beUtils.getServiceToken().getToken();

//      Make the api call
//        String apiJnlPostRequest = QwandaUtils.apiPostEntity2(apiRoute, "[\""+jnlSearch+"\"]", authToken , null);
//        System.out.println(apiJnlPostRequest);
        String apiInternPostRequest = QwandaUtils.apiPostEntity2(apiRoute, "[\""+internSearch+"\"]", authToken , null);
        System.out.println(apiInternPostRequest);
    }


    @Test
    public void tasksTest() throws Exception {
//        VertxUtils.cachedEnabled = false;

        if (beUtils == null) {
            return;
        }

        // Set up the defs
//        DefUtils.loadDEFS(realm,serviceToken);


        String aSourceCode = beUtils.getGennyToken().getUserCode();
        String token = beUtils.getGennyToken().getToken();
        BaseEntity be = createRemoteService("RMS_REC_SYS_HC_001", "Recommendation System for HCs", "http://localhost:9003/api/response",aSourceCode, "SBE_REC_SYS_HC");
        QDataAskMessage askMsg = QuestionUtils.getAsks(aSourceCode,be.getCode(), "QUE_REMOTE_SERVICE_GRP", token);

        QCmdMessage msg = new QCmdMessage("DISPLAY","FORM");
        msg.setToken(beUtils.getGennyToken().getToken());
        VertxUtils.writeMsg("webcmds",msg);

        QDataBaseEntityMessage beMsg = new QDataBaseEntityMessage(be);
        beMsg.setToken(beUtils.getGennyToken().getToken());
        VertxUtils.writeMsg("webcmds",beMsg);

        askMsg.setToken(beUtils.getGennyToken().getToken());
        VertxUtils.writeMsg("webcmds",askMsg);
        VertxUtils.writeMsgEnd(beUtils.getGennyToken());

//        TaskUtils.createTask(beUtils.getGennyToken(), "QUE_REMOTE_SERVICE_GRP");



    }


    @Test
    public void formsTest() throws Exception {
        VertxUtils.cachedEnabled = false;

        if (beUtils == null) {
            return;
        }

//        Set up the defs
        DefUtils.loadDEFS(realm);


        QCmdMessage msg = new QCmdMessage("DISPLAY","FORM");
        msg.setToken(beUtils.getGennyToken().getToken());
        VertxUtils.writeMsg("webcmds",msg);

        String aSourceCode = beUtils.getGennyToken().getUserCode();
        BaseEntity be = createRemoteService("RMS_JNL_NLP_0002", "NLP Journals 0002", "http://localhost:5000/api/response",aSourceCode, "SBE_AI_JOURNAL");

        /* We generate the question */
        Attribute attr = RulesUtils.getAttribute("QQQ_QUESTION_GROUP",beUtils.getGennyToken().getToken());
        Attribute nameAttr = RulesUtils.getAttribute("PRI_NAME",beUtils.getGennyToken().getToken());
        Question groupQuestion = new Question("QUE_REMOTE_SERVICE_GRP", "Sub\'s Test Questions", attr, false);
//        Use TaskUtils.getQuestion(questionCode, userToken);
        Question childQuestion = new Question("QUE_REMOTE_SERVICE_NAME","RMS NAME", nameAttr, false );
        groupQuestion.addTarget(childQuestion,1.0);

        /* We generate the ask */
        Ask ask = new Ask(groupQuestion, aSourceCode, be.getCode(), false, 1.0, false, false, false);
        Ask childAsk = new Ask(childQuestion, aSourceCode,be.getCode(), false, 1.0, false, false, false);
        List<Ask> childAsksArray = new ArrayList<>();
        childAsksArray.add(childAsk);
        ask.setChildAsks(childAsksArray.toArray(new Ask[0]));
        List<Ask> asksArray = new ArrayList<>();
        asksArray.add(ask);

        QDataBaseEntityMessage beMsg = new QDataBaseEntityMessage(be);
        beMsg.setToken(beUtils.getGennyToken().getToken());
        VertxUtils.writeMsg("webcmds",beMsg);

        QDataAskMessage askMsg = new QDataAskMessage(ask);
        askMsg.setToken(beUtils.getGennyToken().getToken());
        askMsg.setAttributeCode("QQQ_QUESTION_GROUP");
        askMsg.setQuestionCode("QUE_REMOTE_SERVICE_GRP");
        askMsg.setSourceCode(aSourceCode);
        askMsg.setTargetCode(be.getCode());
        askMsg.setItems(asksArray.toArray(new Ask[0]));
        VertxUtils.writeMsg("webcmds",askMsg);
        VertxUtils.writeMsgEnd(beUtils.getGennyToken());

    }



    @Test
    public void remoteServicesTest() throws Exception {
        VertxUtils.cachedEnabled = false;

        if (beUtils == null) {
            return;
        }


//        Set up the defs
        DefUtils.loadDEFS(realm);



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


// Send the display table data
//        QCmdTableMessage msg = new QCmdTableMessage("SBE_TEST","RemoteServiceTest");
//        JsonObject msgJson = new JsonObject();
//        msgJson.put("cmd_type","DISPLAY");
//        msgJson.put("code","TABLE");
//        msgJson.put("exec",true);
//        msgJson.put("msg_type","CMD_MSG");
//        msgJson.put("option","EXEC");
//        msgJson.put("send",true);
//        msgJson.put("token",beUtils.getGennyToken().getToken());

        QCmdMessage msg = new QCmdMessage("DISPLAY","TABLE");
        msg.setToken(beUtils.getGennyToken().getToken());
        VertxUtils.writeMsg("webcmds",msg);

//        VertxUtils.writeMsg("webcmds",msgJson.toString());
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


}