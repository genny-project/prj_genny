package life.genny.test;

import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import life.genny.eventbus.EventBusInterface;
import life.genny.eventbus.EventBusMock;
import life.genny.eventbus.VertxCache;
import life.genny.models.GennyToken;
import life.genny.qwanda.message.QCmdMessage;
import life.genny.qwandautils.GennyCacheInterface;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.utils.VertxUtils;

public class LinTest extends GennyJbpmBaseTest {

    protected static final Logger log = org.apache.logging.log4j.LogManager
	    .getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

    protected static String realm = GennySettings.mainrealm;
    protected static Set<String> realms;

    protected static EventBusInterface eventBusMock;
    protected static GennyCacheInterface vertxCache;

    public LinTest() {
	super(false);

    }

    @BeforeClass
    public static void init() throws FileNotFoundException, SQLException {

	System.out.println("BridgeUrl=" + GennySettings.bridgeServiceUrl);
	System.out.println("QwandaUrl=" + GennySettings.qwandaServiceUrl);

	// Set up realm\
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

    
    /*
     * This message to frontend to control the panel toggling.
     * 
     * 
        {
          "cmd_type": "PANEL_TOGGLE",
          "codes": [
            "FRM_APP_CONTENT:WEST"
          ],
          "msg_type": "CMD_MSG",
          "cache": "QUE_SIDEBAR_MENU_GRP:QUE_SIDEBAR_MENU_GRP:QUE_SIDEBAR_TOGGLE",
          "exec": false,
          "send": false
        }
     */
    
    @Test
    public void sendCmdMessage() {
	System.out.println("Return Message to Frontend");
	GennyToken userToken = getToken(realm, "user1000", "Barry Allan", "hero");
	
	//open waiting 5 seca
	//The frontend need to send the current status of the menu
	QCmdMessage cmdMessage = new QCmdMessage("PANEL_TOGGLE","FRM_APP_CONTENT:WEST");
	cmdMessage.setToken(userToken.getToken());
	cmdMessage.setCache("QUE_SIDEBAR_MENU_GRP:QUE_SIDEBAR_MENU_GRP:QUE_SIDEBAR_TOGGLE");
	
	//Send message to frontend by using Qwanda
//	BaseEntity baseEntity = new BaseEntity("LinBAseEntity");
//	QDataBaseEntityMessage beMessage = new QDataBaseEntityMessage(baseEntity);
	
	//Send message to frontend by using Vertix
	VertxUtils.writeMsg("webcmds", JsonUtils.toJson(cmdMessage));
    }

};