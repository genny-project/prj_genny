package life.genny.test;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
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
import org.junit.jupiter.api.Test;

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
import life.genny.rules.QRules;
import life.genny.utils.BaseEntityUtils;
import life.genny.utils.GennyJbpmBaseTest;

import life.genny.utils.JunitCache;
import life.genny.utils.VertxUtils;


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
	
	@Test
	public void gennyTest()
	{
		System.out.println("This is a test");
	}
	
	
	@Test
	public void AdamTest1()
	{
		System.out.println("Local Genny test");
		GennyToken userToken = null;
		GennyToken serviceToken = null;
		QRules qRules = null;


			// VertxUtils.cachedEnabled = false;
			VertxUtils.cachedEnabled = false;
			eventBusMock = new EventBusMock();
			vertxCache = new JunitCache(); // MockCache
			VertxUtils.init(eventBusMock, vertxCache);

			qRules = GennyJbpmBaseTest.setupLocalService();
			userToken = new GennyToken("userToken", qRules.getToken());
			serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());

		BaseEntityUtils beUtils = new BaseEntityUtils(userToken);
		beUtils.setServiceToken(serviceToken);

		
	}
	
	//@Test
	public void AdamTest2()
	{
			System.out.println("fix HCR status test");
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

			SearchEntity searchBE = new SearchEntity("SBE_TEST", "hcrs")
					.addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
					.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "PER_%").addFilter("PRI_IS_HOST_CPY_REP", true)
					.addColumn("PRI_CODE", "Name");

			searchBE.setRealm(realm);
			searchBE.setPageStart(0);
			searchBE.setPageSize(100000);

			List<BaseEntity> items = beUtils.getBaseEntitys(searchBE);

			for (BaseEntity item : items) {

				try {
					// check if there
					Optional<EntityAttribute> ea = item.findEntityAttribute("PRI_STATUS");
					if (ea.isPresent()) {

						String status = item.getValue("PRI_STATUS", null);
						if (StringUtils.isBlank(status)) {
							Answer activeStatus = new Answer(beUtils.getGennyToken().getUserCode(), item.getCode(),
									"PRI_STATUS", "ACTIVE");
							beUtils.saveAnswer(activeStatus);

						}

					} else {
						Answer activeStatus = new Answer(beUtils.getGennyToken().getUserCode(), item.getCode(),
								"PRI_STATUS", "ACTIVE");
						beUtils.saveAnswer(activeStatus);

					}

				} catch (Exception e1) {
					e1.printStackTrace();
				}

			}

			System.out.println("Finished");
		}
	
	   @BeforeClass
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
	            qRules = GennyJbpmBaseTest.setupLocalService();
	            userToken = new GennyToken("userToken", qRules.getToken());
	            serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
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
