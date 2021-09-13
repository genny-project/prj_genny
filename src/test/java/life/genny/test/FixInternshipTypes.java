package life.genny.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
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
import life.genny.eventbus.EventBusInterface;
import life.genny.eventbus.EventBusMock;
import life.genny.eventbus.VertxCache;
import life.genny.models.GennyToken;
import life.genny.qwanda.Answer;
import life.genny.qwanda.EEntityStatus;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeText;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.SearchEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwandautils.GennyCacheInterface;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.KeycloakUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.utils.BaseEntityUtils;
import life.genny.utils.RulesUtils;
import life.genny.utils.VertxUtils;



public class FixInternshipTypes {

    private static final org.jboss.logging.Logger log = Logger.getLogger(FixInternshipTypes.class);

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

    public FixInternshipTypes() {
        super();
    }

 


    @Test
    public void internshipFixTest() throws Exception {
        VertxUtils.cachedEnabled = false;

        if (beUtils == null) {
            return;
        }

        setUpDefs();

//      Use a search BE for the api test
        SearchEntity searchBE = new SearchEntity("SBE_APPS", "APP Search")
                .addSort("PRI_CREATED", "Created", SearchEntity.Sort.ASC)
                .addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "APP_%")
                .addColumn("PRI_CODE", "Code")
                .addColumn("LNK_INTERN", "Link Intern")
                .addColumn("PRI_INTERN_CODE", "Code Intern")
                .addColumn("PRI_JOURNAL_LEARNING_OUTCOMES", "LearningOutcomes")
            //    .addAssociatedColumn("LNK_INTERN", "Intern Name", "LNK_COMP_INTERNSHIP")
                .addColumn("PRI_STATUS","Status");
        searchBE.setRealm(realm);
        
        

        Boolean ok = true;
        Integer index = 0;
        Integer fixedInterns = 0;
        Integer fixedApps = 0;
        searchBE.setPageStart(index);
        Integer pageSize = 100;
        searchBE.setPageSize(pageSize);
        Long total = beUtils.getCount(searchBE);
        
		Attribute lnkCompInternshipAttribute = RulesUtils.getAttribute("LNK_COMP_INTERNSHIP", serviceToken.getToken());
		Attribute lnkInternAttribute = RulesUtils.getAttribute("LNK_INTERN", serviceToken.getToken());
			 
        while (ok) {
        	List<BaseEntity> bes = beUtils.getBaseEntitys(searchBE); // load 100 at a time
        	if (bes.isEmpty() || (index > 5000)) {
        		ok = false;
        		break;
        	}
        	
        	for (BaseEntity be : bes) {
        		String fixedApp = "";
        		String fixedIntern = "";
        		String internInternshipType ="[\"SEL_COURSE_CREDIT\"]";
        		
        		// ok, now get the intern's application type
        		
        		//LNK_COMP_INTERNSHIP
         		BaseEntity intern = beUtils.getLinkedBaseEntity(be,"LNK_INTERN");
         		if (intern == null) {
         			String internStr = be.getValueAsString("PRI_INTERN_CODE");
         			if (!StringUtils.isBlank(internStr)) {
         				intern = beUtils.getBaseEntityByCode(internStr);
         				if (intern != null) {
         					beUtils.saveAnswer(new Answer(be,be,lnkInternAttribute,"[\""+intern.getCode()+"\"]"));
         				}
         			} else {
         				be = beUtils.getBaseEntityByCode(be.getCode());
         				// any intern hints?
         				log.info("No intern associated with app ");
         				// setting app to be deleted
         				beUtils.saveNameStatus(be,null,EEntityStatus.DELETED);
         			}
         		}
         		
         		
         		
        		String appInternshipType = be.getValueAsString("LNK_COMP_INTERNSHIP");
        		if (intern!=null) {
        			internInternshipType = intern.getValue("LNK_COMP_INTERNSHIP",null);
        		
        			if (StringUtils.isBlank(internInternshipType)) {
        				intern = beUtils.saveAnswer(new Answer(intern,intern,lnkCompInternshipAttribute,"[\"SEL_COURSE_CREDIT\"]"));
        				fixedInterns++;
        				fixedIntern = "Fixed Intern";
        			}
        		}
           		if (StringUtils.isBlank(appInternshipType)) {
           			if (StringUtils.isBlank(internInternshipType)) {
           				internInternshipType = "[\"SEL_COURSE_CREDIT\"]";
           			}
           			be = beUtils.getBaseEntityByCode(be.getCode());
        			be = beUtils.saveAnswer(new Answer(be,be,lnkCompInternshipAttribute,internInternshipType));
        			fixedApps++;
        			fixedApp = "Fixed App";
        		}
        		
           		System.out.println(index+" of "+total+" :BE: "+be.getCode()+":"+be.getName()+" --> "+fixedIntern+"   "+fixedApp+" "+internInternshipType);
           	 
        		index++;
        	}
        	
        	searchBE.setPageStart(index);
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
        vertxCache = new VertxCache(); // MockCache
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