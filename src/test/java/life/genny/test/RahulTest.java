package life.genny.test;

import org.kie.api.runtime.process.ProcessContext;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.reflect.TypeToken;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vertx.core.json.JsonObject;
import life.genny.eventbus.EventBusInterface;
import life.genny.eventbus.EventBusMock;
import life.genny.eventbus.VertxCache;
import life.genny.models.Frame3;
import life.genny.models.FramePosition;
import life.genny.models.GennyToken;
import life.genny.models.TableData;
import life.genny.models.Theme;
import life.genny.models.ThemeAttributeType;
import life.genny.models.ThemeDouble;
import life.genny.models.ThemePosition;
import life.genny.qwanda.Answer;
import life.genny.qwanda.Ask;
import life.genny.qwanda.Context;
import life.genny.qwanda.ContextType;
import life.genny.qwanda.Question;
import life.genny.qwanda.VisualControlType;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.datatype.DataType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.entity.SearchEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.message.MessageData;
import life.genny.qwanda.message.QBulkMessage;
import life.genny.qwanda.message.QDataAnswerMessage;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwanda.validation.Validation;
import life.genny.qwanda.validation.ValidationList;
import life.genny.qwandautils.GennyCacheInterface;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.rules.QRules;
import life.genny.utils.BaseEntityUtils;
import life.genny.utils.ContextUtils;
import life.genny.utils.FrameUtils2;
import life.genny.utils.GennyJbpmBaseTest;
import life.genny.utils.GennyKieSession;
import life.genny.utils.RulesUtils;
import life.genny.utils.SessionFacts;
import life.genny.utils.TableUtils;

import life.genny.utils.VertxUtils;

public class RahulTest {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	protected static String realm = GennySettings.mainrealm;
	protected static Set<String> realms;

	protected static EventBusInterface eventBusMock;
	protected static GennyCacheInterface vertxCache;

	private static final String DRL_SEND_USER_DATA_DIR = "SendUserData";

	public RahulTest() {

	}
	
	//@Test
    public void AgreementDocTest() {

        GennyToken userToken = null;
        GennyToken serviceToken = null;
        QRules qRules = null;

        if (false) {
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
//        BaseEntity xyz = new BaseEntity("BE_XYZ");

        HashMap<String, BaseEntity> hashBeg = new HashMap<String, BaseEntity>();
        
        hashBeg.put("intern", intern);
        hashBeg.put("internship", internship);
        hashBeg.put("hostCompany", hostCompany);
//        hashBeg.put("xyz", xyz);

        System.out.println("session     =" + userToken.getSessionCode());
        System.out.println("userToken   =" + userToken.getToken());
        System.out.println("serviceToken=" + serviceToken.getToken());
                
		SessionFacts initFacts = new SessionFacts(serviceToken, null, new QEventMessage("EVT_MSG", "INIT_STARTUP"));
        QEventMessage authInitMsg = new QEventMessage("EVT_MSG", "AUTH_INIT");
        authInitMsg.setToken(userToken.getToken());
        QEventMessage msg1 = new QEventMessage("EVT_MSG", "INIT_1");
        QEventMessage msgLogout = new QEventMessage("EVT_MSG", "LOGOUT");
        QEventMessage menu = new QEventMessage("EVT_MSG", "MENU");
        
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
            		
// ADD THE JBPM WORKFLOWS HERE	
//                    .addJbpm("workDude.bpmn")
                      .addJbpm("sendSignatures.bpmn")
                      .addJbpm("dynamicCards.bpmn")

                    
// ADD THE DROOLS RULES HERE
//        			.addDrl("IsBaseEntity") 
        			
                    .addToken(userToken)
                    .build();
            
            gks.start();
            gks.startProcess("dynamicCards");

//            gks.injectSignal("sendSig", hashBeg);
//            
//            gks.advanceSeconds(5, false);
//            
//            gks.injectSignal("START_INTERN_S1", "FORWARD");            
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        } finally {
            gks.close();
        }
        
    }

    @Test
  	public void userTaskTest()
  	{
  		System.out.println("Process View Test");
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
  			GennyKieSession.loadAttributesJsonFromResources(userToken);

  		} else {
  			qRules = GennyJbpmBaseTest.setupLocalService();
  			userToken = new GennyToken("userToken", qRules.getToken());
  			serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());
  		}

  		System.out.println("session     =" + userToken.getSessionCode());
  		System.out.println("userToken   =" + userToken.getToken());
  		//System.out.println("userToken2   =" + userToken2.getToken());
  		System.out.println("serviceToken=" + serviceToken.getToken());
  		
          BaseEntity intern = new BaseEntity("PRI_INTERN");
          BaseEntity internship = new BaseEntity("BE_INTERNSHIP");        
          BaseEntity hostCompany = new BaseEntity("CPY_HOSTCOMPANY");

          /*HashMap<String, BaseEntity> hashBeg = new HashMap<String, BaseEntity>();*/
          HashMap<String, String> hashBeg = new HashMap<String, String>();
          
          hashBeg.put("begstatus", "DUDE");
          
          /*hashBeg.put("intern", intern);
          hashBeg.put("internship", internship);
          hashBeg.put("hostCompany", hostCompany);*/

  		SessionFacts initFacts = new SessionFacts(serviceToken, null, new QEventMessage("EVT_MSG", "INIT_STARTUP"));
  		QEventMessage authInitMsg = new QEventMessage("EVT_MSG", "AUTH_INIT"); authInitMsg.setToken(userToken.getToken());
  		QEventMessage msgLogout = new QEventMessage("EVT_MSG", "LOGOUT");msgLogout.setToken(userToken.getToken());


  		// NOW SET UP Some baseentitys
  		BaseEntity project = new BaseEntity("PRJ_" + serviceToken.getRealm().toUpperCase(),
  				StringUtils.capitaliseAllWords(serviceToken.getRealm()));
  		project.setRealm(serviceToken.getRealm());
  		VertxUtils.writeCachedJson(serviceToken.getRealm(), "PRJ_" + serviceToken.getRealm().toUpperCase(),
  				JsonUtils.toJson(project), serviceToken.getToken());
  		VertxUtils.writeCachedJson(realm,  ":" + "PRJ_" + serviceToken.getRealm().toUpperCase(),JsonUtils.toJson(project), serviceToken.getToken());


  		GennyKieSession gks = null;

  		try {
  			gks = GennyKieSession
  					.builder(serviceToken,true)
  					
  // ADD THE JBPM WORKFLOWS HERE					
//  					.addJbpm("cardsA.bpmn")
//  					.addJbpm("cardsB.bpmn")
//  					.addJbpm("cardsC.bpmn")
  					.addJbpm("notificationHub2.bpmn")
  					.addJbpm("baseEntityValidation.bpmn")
  					.addJbpm("dynamicCards.bpmn")
  					.addJbpm("placedCards.bpmn")
  					.addJbpm("progressCards.bpmn")
  					.addJbpm("sendSignatures.bpmn")
//  					.addJbpm("userSession.bpmn")
  					
  // ADD THE DROOLS RULES HERE
  					.addDrl("MoveBucket")
  					.addDrl("CommonEnter")
  					.addDrl("SpecificEnter")
  					.addDrl("SpecificReminder")
  					.addDrl("Timer")

  					.addDrl("CardStatus")
  					

  					.addToken(userToken)
  					.build();
  			
  			gks.start();

  			gks.startProcess("dynamicCards");
  			

//              gks.advanceSeconds(1, false);
//              gks.injectSignal("dynamicStatus", "Reactivate");
              gks.advanceSeconds(5, false);
              gks.injectSignal("dynamicControl", "FORWARD"); 			// Applied to Shortlist
              gks.advanceSeconds(5, false);
              gks.injectSignal("dynamicControl", "FORWARD"); 			// Shortlist to Interview
              gks.advanceSeconds(5, false);
//              gks.injectSignal("status", "BACKWARD");		// Interview back to Shortlist 
//              gks.advanceSeconds(5, false);
//              gks.injectSignal("appTarget", "FORWARD");		// Shortlist to Interview
//              gks.advanceSeconds(5, false);
              gks.injectSignal("dynamicControl", "FORWARD");			// Interview to Offered
              gks.advanceSeconds(5, false);
              gks.injectSignal("dynamicControl", "FORWARD");			// Offered to Place
              gks.advanceSeconds(5, false);
//              gks.injectSignal("status", "FORWARD");		
//              gks.advanceSeconds(1, false);
//              gks.injectSignal("placedStatus", "Withdraw");
//              gks.advanceSeconds(5, false);
//              gks.injectSignal("placedControl", "FORWARD"); 			// Placed to Progress
//              gks.advanceSeconds(1, false);
//              gks.injectSignal("progressStatus", "Onhold");
//              gks.advanceSeconds(5, false);
//              gks.injectSignal("progressControl", "FORWARD"); 		// Progress to Complete
//              gks.advanceSeconds(5, false);
              
              
  			/*
  			BaseEntity icn_sort = new BaseEntity("ICN_SORT","Icon Sort");
  			try {
  				
  				icn_sort.addAttribute(RulesUtils.getAttribute("PRI_ICON_CODE", serviceToken.getToken()), 1.0, "sort");
  				icn_sort.setRealm(realm);
  				VertxUtils.writeCachedJson(realm,   "ICN_SORT",JsonUtils.toJson(icn_sort), serviceToken.getToken());

  			} catch (BadDataException e1) {
  				e1.printStackTrace();
  			}

  			//gks.injectSignal("initProject", initFacts); // This should initialise everything
  			gks.advanceSeconds(5, false);
  			
  			gks.getKieSession().getWorkItemManager().registerWorkItemHandler("Human Task", new NonManagedLocalHTWorkItemHandler(gks.getKieSession(),gks.getTaskService()));
  			
  		       // One potential owner, should go straight to state Reserved
//  	        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
//  	        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [new User('Bobba Fet'), new User('Darth Vader') ], excludedOwners = [new User('Darth Vader')],businessAdministrators = [ new User('Administrator') ], }),";
//  	        str += "name =  'This is my task name' })";
//  	        Task task = TaskFactory.evalTask(new StringReader(str));
//  	        gks.getTaskService().addTask(task, new HashMap<String, Object>());
  			//gks.getTaskService().
  	        List<TaskSummary> tasks = null;

  	        User acrow = (User) TaskModelProvider.getFactory().newUser("acrow");
  	        // Start a process
  	        gks.startProcess("adam_user1");
  	        gks.advanceSeconds(5, false);
  	        Map<String,Object> params = new HashMap<String,Object>();
  	        Task task = new TaskFluent().setName("Amazing GADA Stuff")
  	                .addPotentialGroup("GADA")
  	                .setAdminUser("acrow")
  	             //   .addPotentialUser("acrow")
  	                .setProcessId("direct")
  	                .setDeploymentID("genny")
  	                .getTask();

  	        Task task2 = new TaskFluent().setName("Awesome GADA stuff")
  	              //  .addPotentialGroup("GADA")
  	                .setAdminUser("Administrator")
  	                .addPotentialUser("domenic")
  	                .setDeploymentID("genny")
  	                .setProcessId("direct")
  	                .getTask();

  	        Task task3 = new TaskFluent().setName("Boring Outcome Stuff")
  	                .addPotentialGroup("OUTCOME")
  	                .setAdminUser("Administrator")
  	                .addPotentialUser("gerard")
  	                .setProcessId("direct")
  	                .setDeploymentID("genny")
  	                .getTask();


  	        gks.getTaskService().addTask(task, params);
  	        gks.getTaskService().addTask(task2, params);
  	        gks.getTaskService().addTask(task3, params);
  	        long taskId = task.getId();
  	        long taskId2 = task2.getId();
  	        long taskId3 = task3.getId();

                // Do Task Operations
              
                showStatuses(gks);

              
              // Add Comment
              InternalComment commentImpl = (InternalComment) TaskModelProvider.getFactory().newComment();
              
              commentImpl.setAddedAt(new Date());
              commentImpl.setAddedBy(acrow);
              gks.getTaskService().addComment(taskId2, commentImpl);
              
               Map<String, Object> content = gks.getTaskService().getTaskContent(taskId2 );
               System.out.println(content);
                // Start Task
                gks.getTaskService().start(taskId, "acrow");    
                showStatuses(gks);

                gks.getTaskService().suspend(taskId, "acrow");    
                showStatuses(gks);

                gks.getTaskService().resume(taskId, "acrow");    
                showStatuses(gks);
                
                gks.getTaskService().forward(taskId2, "domenic", "anish");

                // Claim Task
//                gks.getTaskService().claim(taskId, "acrow");
//                showStatuses(gks);
//            
                Map<String, Object> results = new HashMap<String, Object>();
                results.put("Result", "Done");
                gks.getTaskService().complete(taskId, "acrow", results);
                showStatuses(gks);

                results.put("Result", "some document data");

//                long processInstanceId =
//              		  processService.startProcess(deployUnit.getIdentifier(), "org.jbpm.writedocument");
//              		  List<Long> taskIds =
//              		  runtimeDataService.getTasksByProcessInstanceId(processInstanceId);
//              		  Long taskId4 = taskIds.get(0);
//              		  userTaskService.start(taskId, "john");
//              		  UserTaskInstanceDesc task4 = runtimeDataService.getTaskById(taskId4);
//              		  Map<String, Object> results = new HashMap<String, Object>();
//              		  results.put("Result", "some document data");
//              		  userTaskService.complete(taskId4, "john", results);
               
  			gks.injectEvent(authInitMsg); // This should create a new process
  			gks.advanceSeconds(5, false);

  			BaseEntity user = VertxUtils.getObject(serviceToken.getRealm(), "", userToken.getUserCode(),
  					BaseEntity.class, serviceToken.getToken());

  			gks.injectEvent(msgLogout);
  			
  			*/
  		} catch (Exception e) {
  			e.printStackTrace();
  			
  		}
  		finally {
  			if (gks!=null) {
  				gks.close();
  			}
  		}
  	}
}
