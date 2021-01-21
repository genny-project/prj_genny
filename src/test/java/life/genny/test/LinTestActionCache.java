package life.genny.test;

import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;

import life.genny.eventbus.EventBusInterface;
import life.genny.eventbus.EventBusMock;
import life.genny.eventbus.VertxCache;
import life.genny.models.GennyToken;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QCmdMessageToggleFrame;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwandautils.GennyCacheInterface;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.rules.QRules;
import life.genny.utils.GennyJbpmBaseTest;
import life.genny.utils.GennyKieSession;
import life.genny.utils.VertxUtils;

/**
 * 
 * @author Dominic Lin
 * 
 * 
 *         This test class will test Template Lifecycle by using assertions.
 *
 */
public class LinTestActionCache extends GennyJbpmBaseTest {

    protected static final Logger log = org.apache.logging.log4j.LogManager
	    .getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

    protected static String realm = GennySettings.mainrealm;
    protected static Set<String> realms;

    protected static EventBusInterface eventBusMock;
    protected static GennyCacheInterface vertxCache;

    /* Constant(s): */
    public static final String AMQ_BROKER_URL = "tcp://localhost:61616";
    public static final String QUEUE_NAME = "queue/KIE.SERVER.EXECUTOR";

    /* Instance variable(s): */
    protected ConnectionFactory mActiveMQConnectionFactory;
    protected JmsTemplate mJmsTemplate;

    // Constructor
    public LinTestActionCache() {
	super(false);
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

    @Before
    public void setUp() {
	mActiveMQConnectionFactory = new ActiveMQConnectionFactory(AMQ_BROKER_URL);
	mJmsTemplate = new JmsTemplate(mActiveMQConnectionFactory);
	final Destination theTestDestination = new ActiveMQQueue(QUEUE_NAME);
	mJmsTemplate.setDefaultDestination(theTestDestination);
	mJmsTemplate.setReceiveTimeout(500L);
    }

    /*
     * Send the toggle frame Qmessage to the frontend by using Workflow and Drools.
     */
    @Test
    public void testActionCacheMessageBySignal() {

	String injectSignal = "SignalToggleFrame";
	
	System.out.println("Start Action Cache Test");
	GennyToken userToken = null;
	GennyToken serviceToken = null;
	QRules qRules = null;
	boolean isCreateToken = false;

	if (isCreateToken) {
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

	System.out.println("session     =" + userToken.getSessionCode());
	System.out.println("userToken   =" + userToken.getToken());
	System.out.println("serviceToken=" + serviceToken.getToken());

	// This will message pass into the Drools rule
	QEventMessage actionCacheLayoutMessage = new QEventMessage("EVT_LAYOUT", "actionCacheLayoutMessage");

	QEventMessage authInitMsg1 = new QEventMessage("EVT_MSG", "AUTH_INIT");
	authInitMsg1.setToken(userToken.getToken());

	QEventMessage msgLogout1 = new QEventMessage("EVT_MSG", "LOGOUT");
	msgLogout1.setToken(userToken.getToken());

	// NOW SET UP Some baseentitys
	BaseEntity project = new BaseEntity("PRJ_" + serviceToken.getRealm().toUpperCase(),
		StringUtils.capitaliseAllWords(serviceToken.getRealm()));
	project.setRealm(serviceToken.getRealm());
	VertxUtils.writeCachedJson(serviceToken.getRealm(), "PRJ_" + serviceToken.getRealm().toUpperCase(),
		JsonUtils.toJson(project), serviceToken.getToken());

	GennyKieSession gks = null;

	System.setProperty("org.kie.executor.jms", "false");
	try {
	    gks = GennyKieSession.builder(serviceToken, true)//
		    .addDrl("ActionCache.drl")//
		    .addJbpm("ActionCache.bpmn")//
		    .addToken(userToken)//
		    .build();

	    gks.start();

	    gks.injectEvent(authInitMsg1); // This should create a new process
	    gks.advanceSeconds(5, false);

	    // Maunally trigger the workflow
//	    gks.startProcess("ActionCacheProcessID");

	    // This will inject into all started workflows
	    // with a signal call "SignalToggleFrame" and event object
	    // The workflow will fire automatically after receive the signal
	    gks.injectSignal(injectSignal, actionCacheLayoutMessage);
	    gks.advanceSeconds(5, false);

	    //Trigger the Logout Event
	    gks.injectEvent(msgLogout1);
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    if (gks != null) {
		gks.close();
	    }
	}
	
	//Following assertion must be fulfilled that to make the test success.
	Assert.assertEquals(userToken.getUserCode(), "PER_USER1");
	Assert.assertFalse(isCreateToken);
	Assert.assertEquals(authInitMsg1.getData().getCode(), "AUTH_INIT");
	Assert.assertEquals(actionCacheLayoutMessage.getData().getCode(), "actionCacheLayoutMessage");
	Assert.assertEquals(injectSignal, "SignalToggleFrame");

    }

    /*
     * This test case will send a Frame Toogle QMessage to the frontend
     */
//    @Test
    public void sendCmdMessage() {
	System.out.println("Send Message to Frontend");
	GennyToken userToken = null;
	GennyToken serviceToken = null;
	QRules qRules = null;
	boolean isCreateToken = false;

	if (isCreateToken) {
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
	System.out.println("session     =" + userToken.getSessionCode());
	System.out.println("userToken   =" + userToken.getToken());
	System.out.println("serviceToken=" + serviceToken.getToken());

	GennyToken msgUserToken = getToken(realm, "user1", "Barry Allan", "user");

	String toggleFrame = "QUE_SIDEBAR_MENU_GRP:QUE_SIDEBAR_MENU_GRP:QUE_SIDEBAR_TOGGLEXXXXX";
	// The frontend need to send the current status of the menu
	QCmdMessageToggleFrame cmdMessage = new QCmdMessageToggleFrame();
//	QCmdMessage cmdMessage = new QCmdMessage("PANEL_TOGGLE","FRM_APP_CONTENT:WEST");
	cmdMessage.setToken(msgUserToken.getToken());
	cmdMessage.setCache(toggleFrame);

	// Send message to frontend by using Vertix
	System.out.println(cmdMessage);
	VertxUtils.writeMsg("webcmds", JsonUtils.toJson(cmdMessage));
    }
}
