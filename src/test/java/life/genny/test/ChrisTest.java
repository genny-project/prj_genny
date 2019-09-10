 package life.genny.test;

import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import life.genny.eventbus.EventBusInterface;
import life.genny.eventbus.EventBusMock;
import life.genny.eventbus.VertxCache;
import life.genny.models.GennyToken;
import life.genny.qwanda.Answer;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QDataAnswerMessage;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwandautils.GennyCacheInterface;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.rules.QRules;
import life.genny.utils.VertxUtils;

public class ChrisTest {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	protected static String realm = GennySettings.mainrealm;
	protected static Set<String> realms;

	protected static EventBusInterface eventBusMock;
	protected static GennyCacheInterface vertxCache;

	public ChrisTest() {

	}
	
	@Test
    public void lifecycleTest() {
        GennyToken userToken = null;
        GennyToken serviceToken = null;
        QRules qRules = null;

        if (true) {
            userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
            serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
            qRules = new QRules(eventBusMock, userToken.getToken());
            qRules.set("realm", userToken.getRealm());
            qRules.setServiceToken(serviceToken.getToken());
            VertxUtils.cachedEnabled = true; // don't send to local Service Cache
        } else {
            qRules = GennyJbpmBaseTest.setupLocalService();
            userToken = new GennyToken("userToken", qRules.getToken());
            serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
        }
        
        BaseEntity intern = new BaseEntity("PRI_INTERN");
        BaseEntity internship = new BaseEntity("BE_INTERNSHIP");        
        BaseEntity hostCompany = new BaseEntity("CPY_HOSTCOMPANY");

        HashMap<String, BaseEntity> hashBeg = new HashMap<String, BaseEntity>();
        
        hashBeg.put("intern", intern);
        hashBeg.put("internship", internship);
        hashBeg.put("hostCompany", hostCompany);

        System.out.println("session     =" + userToken.getSessionCode());
        System.out.println("userToken   =" + userToken.getToken());
        System.out.println("serviceToken=" + serviceToken.getToken());

        QEventMessage authInitMsg = new QEventMessage("EVT_MSG", "AUTH_INIT");
        QEventMessage msg1 = new QEventMessage("EVT_MSG", "INIT_1");
        QEventMessage msgLogout = new QEventMessage("EVT_MSG", "LOGOUT");

        // NOW SET UP Some baseentitys
        BaseEntity project = new BaseEntity("PRJ_" + serviceToken.getRealm().toUpperCase(),
                StringUtils.capitaliseAllWords(serviceToken.getRealm()));
        project.setRealm(serviceToken.getRealm());
        VertxUtils.writeCachedJson(serviceToken.getRealm(), "PRJ_" + serviceToken.getRealm().toUpperCase(),
                JsonUtils.toJson(project), serviceToken.getToken());
        
        // Log out to begin
        VertxUtils.writeCachedJson(userToken.getRealm(),userToken.getSessionCode(),null,userToken.getToken());

        GennyKieSession gks = null;

        try {
            gks = GennyKieSession
            		.builder(serviceToken, true)
                    .addJbpm("internshipLifecycle.bpmn")
                    .addJbpm("applicationLifecycle.bpmn")
                    .addToken(userToken)
                    .build();
            
            gks.start();
            
            //gks.advanceSeconds(5, false);
        
            System.out.println("TEST:: Injecting signal into internshipLifecycle");
            gks.injectSignal("newInternship", hashBeg);
            
            gks.injectSignal("beginEvent", hashBeg);
            gks.injectSignal("beginInternalSignal", hashBeg);
            gks.injectSignal("beginData", hashBeg);
            
            gks.advanceSeconds(5, false);
          

            System.out.println("TEST:: Injecting signal into applicationLifecycle");
            gks.injectSignal("newApplication", hashBeg);
            
            gks.advanceSeconds(5, false);
            
            System.out.println("TEST:: Injecting \"Forward\" controlSignal");
            gks.injectSignal("controlSignal", "FOWARD");

            gks.advanceSeconds(5, false);
            
            System.out.println("TEST:: Injecting \"Forward\" controlSignal");
            gks.injectSignal("controlSignal", "FOWARD");

            gks.advanceSeconds(5, false);
            
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        } finally {
            gks.close();
        }
        
    }
	
	//@Test
	public void quickndirty() {
		System.out.println("Running quick and dirty");
		VertxUtils.cachedEnabled = true; // don't try and use any local services
		
		GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "userToken");
		GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "serviceToken");
		
		QRules qRules = new QRules(eventBusMock, userToken.getToken());
		qRules.set("realm", userToken.getRealm());
		qRules.setServiceToken(serviceToken.getToken());

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage initMsg = new QEventMessage("EVT_MSG", "INIT_STARTUP");
		
		QEventMessage authInitMsg1 = new QEventMessage("EVT_MSG", "AUTH_INIT");
		authInitMsg1.setToken(userToken.getToken());

		GennyKieSession gks = null;
		try {
			gks = GennyKieSession
					.builder(serviceToken, true)
					.addJbpm("process_1.bpmn")
					.addToken(userToken)
					.build();

			gks.start();
			
			gks.injectEvent(authInitMsg1);

			gks.advanceSeconds(5, false);
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			gks.close();
		}

	}
	
	//@Test
	public void quickTest() {
		GennyToken userToken = null;
		GennyToken serviceToken = null;
		QRules qRules = null;

		if (true) {
			userToken = GennyJbpmBaseTest.createGennyToken(realm, "user13", "Barry Allan", "user");
			serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
			qRules = new QRules(eventBusMock, userToken.getToken());
			qRules.set("realm", userToken.getRealm());
			qRules.setServiceToken(serviceToken.getToken());
			VertxUtils.cachedEnabled = true; // don't send to local Service Cache
		} else {
			qRules = GennyJbpmBaseTest.setupLocalService();
			userToken = new GennyToken("userToken", qRules.getToken());
			serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
		}
		
		BaseEntity intern = new BaseEntity("PRI_INTERN");
		BaseEntity internship = new BaseEntity("PRI_INTERNSHIP");

	    HashMap<String, BaseEntity> hashBeg = new HashMap<String, BaseEntity>();
	    
	    hashBeg.put("intern", intern);
	    hashBeg.put("internship", internship);
	    
	    String processSignal = "processSignal";

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage authInitMsg = new QEventMessage("EVT_MSG", "AUTH_INIT");
		QEventMessage msg1 = new QEventMessage("EVT_MSG", "INIT_1");
		QEventMessage msgLogout = new QEventMessage("EVT_MSG", "LOGOUT");

		// Now set up some Base Entities
		BaseEntity project = new BaseEntity("PRJ_" + serviceToken.getRealm().toUpperCase(), StringUtils.capitaliseAllWords(serviceToken.getRealm()));
		project.setRealm(serviceToken.getRealm());
		VertxUtils.writeCachedJson(serviceToken.getRealm(), "PRJ_" + serviceToken.getRealm().toUpperCase(), JsonUtils.toJson(project), serviceToken.getToken());
		
		// Log out to begin
		VertxUtils.writeCachedJson(userToken.getRealm(),userToken.getSessionCode(),null,userToken.getToken());

		GennyKieSession gks = null;

		try {
			gks = GennyKieSession
					.builder(serviceToken, true)
					.addJbpm("applicationLifecycle.bpmn")
					.addToken(userToken)
					.build();
			
			gks.start();
			
			gks.advanceSeconds(1, true);

			gks.injectSignal("newApplication", hashBeg);
			
			gks.advanceSeconds(5, true);
			
			//gks.injectSignal("processSignal", processSignal);
			
			gks.advanceSeconds(1, true);
			
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			gks.close();
		}
		
	}

	//@Test
	public void newUserTest() {
		System.out.println("New User test");
		GennyToken userToken = null;
		GennyToken userToken2 = null;
		GennyToken serviceToken = null;
		QRules qRules = null;

		if (true) {
			userToken = GennyJbpmBaseTest.createGennyToken(realm, "user15", "Barry Allan", "user");
			userToken2 = GennyJbpmBaseTest.createGennyToken(realm, "user2", "Barry2 Allan2", "user");
			serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
			qRules = new QRules(eventBusMock, userToken.getToken());
			qRules.set("realm", userToken.getRealm());
			qRules.setServiceToken(serviceToken.getToken());
			VertxUtils.cachedEnabled = true; // don't send to local Service Cache
		} else {
			qRules = GennyJbpmBaseTest.setupLocalService();
			userToken = new GennyToken("userToken", qRules.getToken());
			serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
		}

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("userToken2   =" + userToken2.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage initMsg = new QEventMessage("EVT_MSG", "INIT_STARTUP");

		QEventMessage authInitMsg1 = new QEventMessage("EVT_MSG", "AUTH_INIT");
		authInitMsg1.setToken(userToken.getToken());

		QEventMessage msgLogout1 = new QEventMessage("EVT_MSG", "LOGOUT");
		msgLogout1.setToken(userToken.getToken());


		List<Answer> answers = new ArrayList<Answer>();
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_FIRSTNAME", "Bruce"));
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_LASTNAME", "Wayne"));
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_ADDRESS_JSON",
				"{\"street_number\":\"64\",\"street_name\":\"Fakenham Road\",\"suburb\":\"Ashburton\",\"state\":\"Victoria\",\"country\":\"AU\",\"postal_code\":\"3147\",\"full_address\":\"64 Fakenham Rd, Ashburton VIC 3147, Australia\",\"street_address\":\"64 Fakenham Road\"}"));

		QDataAnswerMessage answerMsg = new QDataAnswerMessage(answers.toArray(new Answer[0]));
		answerMsg.setToken(userToken.getToken());

		// NOW SET UP Some baseentitys
		BaseEntity project = new BaseEntity("PRJ_" + serviceToken.getRealm().toUpperCase(), StringUtils.capitaliseAllWords(serviceToken.getRealm()));
		project.setRealm(serviceToken.getRealm());
		VertxUtils.writeCachedJson(serviceToken.getRealm(), "PRJ_" + serviceToken.getRealm().toUpperCase(), JsonUtils.toJson(project), serviceToken.getToken());

		GennyKieSession gks = null;

		try {
			gks = GennyKieSession.builder(serviceToken, true)
					.addDrl("SignalProcessing")
					.addDrl("DataProcessing")
					.addDrl("EventProcessing")
					.addDrl("InitialiseProject")
					.addJbpm("InitialiseProject")
					.addJbpm("Lifecycles")
					.addDrl("AuthInit")
					.addJbpm("AuthInit")
					.addToken(userToken)
					.build();
			
			gks.start();
			
			// Injects the event -> ("EVT_MSG", "INIT_STARTUP")
			gks.injectEvent(initMsg);
			gks.advanceSeconds(5, false);
			
			// Injects the event -> ("EVT_MSG", "AUTH_INIT")
			gks.injectEvent(authInitMsg1);
			gks.advanceSeconds(5, false);
			gks.advanceSeconds(5, false);
			gks.advanceSeconds(5, false);
		
			
			// Injects the event -> ("EVT_MSG", "LOGOUT")
			gks.injectEvent(msgLogout1);
			
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			if (gks != null) {
				gks.close();
			}
		}
	}

	@BeforeClass
	public static void init() throws FileNotFoundException, SQLException {

		System.out.println("BridgeUrl=" + GennySettings.bridgeServiceUrl);
		System.out.println("QwandaUrl=" + GennySettings.qwandaServiceUrl);

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

	}

}