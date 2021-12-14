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
import javax.json.bind.JsonbBuilder;
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

import com.google.gson.JsonObject;
import com.thoughtworks.xstream.mapper.SystemAttributeAliasingMapper;

import io.vavr.Tuple2;
import life.genny.eventbus.EventBusInterface;
import life.genny.eventbus.EventBusMock;
import life.genny.eventbus.VertxCache;
import life.genny.models.GennyToken;
import life.genny.qwanda.Answer;
import life.genny.qwanda.Ask;
import life.genny.qwanda.EEntityStatus;
import life.genny.qwanda.Question;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeText;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.SearchEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.message.QCmdMessage;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwandautils.GennyCacheInterface;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.KeycloakUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.utils.BaseEntityUtils;
import life.genny.utils.DefUtils;
import life.genny.utils.RulesUtils;
import life.genny.utils.VertxUtils;




public class SendWebCmds {

    private static final org.jboss.logging.Logger log = Logger.getLogger(SendWebCmds.class);

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

    public SendWebCmds() {
        super();
    }

    
    @Test
    public void sendWebCmds()  
    {
        VertxUtils.cachedEnabled = false;

        if (beUtils == null) {
            return;
        }

        try {
            sendWebCmd("PCM_TEST1");
    	} catch (Exception e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    }
    

public void sendWebCmd(final String pcmCode) throws Exception 
{			
	      QCmdMessage msg = new QCmdMessage("DISPLAY",pcmCode);
	      msg.setToken(beUtils.getGennyToken().getToken());
	      VertxUtils.writeMsg("webcmds",msg);
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


       // DefUtils.loadDEFS(realm, serviceToken);
    }

 
    
}
