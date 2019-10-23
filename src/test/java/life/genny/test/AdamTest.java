package life.genny.test;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

import org.apache.logging.log4j.Logger;
import org.assertj.core.util.Arrays;
import org.codehaus.plexus.util.StringUtils;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.bpmn2.handler.ServiceTaskHandler;
import org.jbpm.kie.services.impl.model.UserTaskInstanceDesc;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.admin.ProcessInstanceAdminService;
import org.jbpm.services.api.model.DeploymentUnit;
import org.jbpm.services.api.query.QueryService;
import org.jbpm.services.api.utils.KieServiceConfigurator;
import org.jbpm.services.task.impl.factories.TaskFactory;
import org.jbpm.services.task.internals.lifecycle.MVELLifeCycleManager;
import org.jbpm.services.task.internals.lifecycle.OperationCommand;
import org.jbpm.services.task.utils.TaskFluent;
import org.jbpm.services.task.wih.NonManagedLocalHTWorkItemHandler;
import org.jbpm.services.task.wih.util.PeopleAssignmentHelper;
import org.jbpm.test.services.TestIdentityProvider;
import org.jbpm.test.services.TestUserGroupCallbackImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.task.model.Comment;
import org.kie.api.task.model.Group;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.api.task.model.User;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.internal.task.api.model.InternalComment;
import org.kie.internal.task.api.model.Operation;

import com.google.gson.reflect.TypeToken;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vertx.core.json.JsonObject;
import life.genny.eventbus.EventBusInterface;
import life.genny.eventbus.EventBusMock;
import life.genny.eventbus.VertxCache;
import life.genny.jbpm.customworkitemhandlers.AdamTest1WorkItemHandler;
import life.genny.jbpm.customworkitemhandlers.AskQuestionTaskWorkItemHandler;
import life.genny.jbpm.customworkitemhandlers.ShowFrame;
import life.genny.models.Frame3;
import life.genny.models.FramePosition;
import life.genny.models.GennyToken;
import life.genny.models.TableData;
import life.genny.models.Theme;
import life.genny.models.ThemeAttributeType;
import life.genny.models.ThemePosition;
import life.genny.qwanda.Answer;
import life.genny.qwanda.Ask;
import life.genny.qwanda.Context;
import life.genny.qwanda.ContextType;
import life.genny.qwanda.VisualControlType;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeLink;
import life.genny.qwanda.attribute.AttributeText;
import life.genny.qwanda.datatype.DataType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.SearchEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.message.MessageData;
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
import life.genny.utils.FrameUtils2;
import life.genny.utils.OutputParam;
import life.genny.utils.RulesUtils;
import life.genny.utils.SessionFacts;
import life.genny.utils.TableUtils;
import life.genny.utils.TableUtilsTest;
import life.genny.utils.VertxUtils;

public class AdamTest {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

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

	    protected TestIdentityProvider identityProvider;
	    protected TestUserGroupCallbackImpl userGroupCallback;

	    protected KieServiceConfigurator serviceConfigurator;
	    
	    protected DeploymentUnit deploymentUnit;  
	    
 

	public AdamTest() {
		 loadServiceConfigurator();
	}
	
	   protected void loadServiceConfigurator() {
	        this.serviceConfigurator = ServiceLoader.load(KieServiceConfigurator.class).iterator().next();
	    }

	@Test
		public void askQuestionTest()
		{
			System.out.println("AskQuestion Test");
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
			VertxUtils.writeCachedJson(GennySettings.GENNY_REALM,
					"TOKEN" + realm.toUpperCase(),serviceToken.getToken());
			JsonObject tokenObj = VertxUtils.readCachedJson(GennySettings.GENNY_REALM,
					"TOKEN" + realm.toUpperCase());
			String token = tokenObj.getString("value");

			createTestUsersGroups(serviceToken);

			GennyKieSession gks = null;

			try {
				gks = GennyKieSession.builder(serviceToken,true)
						.addDrl("SignalProcessing")
						.addDrl("DataProcessing")
						.addDrl("EventProcessing")
						.addJbpm("Lifecycles")
						.addJbpm("adam_user1.bpmn")
						.addJbpm("adam_user2.bpmn")
						.addJbpm("adam_user3.bpmn")
						.addDrl("AuthInit")
						.addJbpm("AuthInit")
						.addDrl("InitialiseProject")
						.addJbpm("InitialiseProject")
						.addToken(userToken)
						.build();
				gks.start();
			//	 gks.startProcess("adam_user3");
				gks.injectEvent(authInitMsg); // This should create a new process
				gks.advanceSeconds(5, false);

				System.out.println("Invoking AskQuestionTask workItem");
				// Send an AskQuestion
				AskQuestionTaskWorkItemHandler askQ = new AskQuestionTaskWorkItemHandler(GennyKieSession.class,gks.getGennyRuntimeEngine(),gks.getKieSession());
				WorkItemManager workItemManager = gks.getKieSession().getWorkItemManager();
				WorkItemImpl workItem = new WorkItemImpl();
		        workItem.setParameter("userToken",
		                              userToken);
		        workItem.setParameter("questionCode",
		                              "QUE_USER_PROFILE_GRP");
		        workItem.setParameter("callingWorkflow", "AdamTest");
		        workItem.setParameter("Priority", "10");  // if l;eft out defaults to 0
		        workItem.setDeploymentId(userToken.getRealm());
		        workItem.setName("AskQuestion");
		        workItem.setProcessInstanceId(1234L); // made up processId
		        askQ.executeWorkItem(workItem, workItemManager);
				
		        showStatuses(gks);
		        
				gks.injectEvent(msgLogout);
			} catch (Exception e) {
				e.printStackTrace();
				
			}
			finally {
				if (gks!=null) {
					gks.close();
				}
			}
		}

	   
		//@Test
		public void userTaskTest()
		{
			System.out.println("UserTask Test");
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
			VertxUtils.writeCachedJson(GennySettings.GENNY_REALM,
					"TOKEN" + realm.toUpperCase(),serviceToken.getToken());
			JsonObject tokenObj = VertxUtils.readCachedJson(GennySettings.GENNY_REALM,
					"TOKEN" + realm.toUpperCase());
			String token = tokenObj.getString("value");

			createTestUsersGroups(serviceToken);

			GennyKieSession gks = null;

			try {
				gks = GennyKieSession.builder(serviceToken,true)
					//	.addDrl("SignalProcessing")
					//	.addDrl("DataProcessing")
					//	.addDrl("EventProcessing")
					//	.addJbpm("Lifecycles")
						.addJbpm("adam_user1.bpmn")
						.addJbpm("adam_user2.bpmn")
					//	.addDrl("AuthInit")
					//	.addJbpm("AuthInit")
					//	.addDrl("InitialiseProject")
					//	.addJbpm("InitialiseProject")

						.addToken(userToken)
						.build();
				gks.start();
				

				
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
		        List<TaskSummary> tasks = null;

		        
		        // Start a process
		        gks.startProcess("adam_user2");
		        
		        Task tasky = gks.getTaskService().getTaskById(1L);
		        System.out.println(tasky);
		        System.out.println("Formname: "+tasky.getFormName());
		        System.out.println("Description: "+tasky.getDescription());
		        System.out.println("People Assignments: "+tasky.getPeopleAssignments().getPotentialOwners());
		        showStatuses(gks);
		        
		        gks.advanceSeconds(5, false);
		        Map<String,Object> params = new HashMap<String,Object>();
		        Task task = new TaskFluent().setName("Amazing GADA Stuff")
		        		.setAdminUser(realm+"+PER_ADAMCROW63_AT_GMAIL_COM")
		        		.setAdminGroup("Administrators")
		                .addPotentialGroup(realm+"+GRP_GADA")
		                .setAdminUser(realm+"+PER_ADAMCROW63_AT_GMAIL_COM")
		             //   .addPotentialUser("acrow")
		                .setProcessId("direct")
		                .setDeploymentID(realm)
		                .getTask();


		        Task task2 = new TaskFluent().setName("Awesome GADA stuff")
		              //  .addPotentialGroup("GADA")
		                .setAdminUser(realm+"+PER_ADAMCROW63_AT_GMAIL_COM")
		                .setAdminGroup("Administrators")
		                .addPotentialUser(realm+"+PER_DOMENIC_AT_OUTCOME_LIFE")
		                .setDeploymentID(realm)
		                .setProcessId("direct")
		                .getTask();

		        Task task3 = new TaskFluent().setName("Boring Outcome Stuff")
		                .addPotentialGroup(realm+"+GRP_OUTCOME")
		                .setAdminUser(realm+"+PER_ADAMCROW63_AT_GMAIL_COM")
		                .setAdminGroup("Administrators")
		                .addPotentialUser(realm+"+PER_GERARD_AT_OUTCOME_LIFE")
		                .setProcessId("direct")
		                .setDeploymentID(realm)
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
	              User JbpmUser = (User) TaskModelProvider.getFactory().newUser(realm+"+PER_ADAMCROW63_AT_GMAIL_COM");
	            InternalComment commentImpl = (InternalComment) TaskModelProvider.getFactory().newComment();
	            commentImpl.setAddedAt(new Date());
	            commentImpl.setAddedBy(JbpmUser);
	            gks.getTaskService().addComment(taskId2, commentImpl);
	            
	             Map<String, Object> content = gks.getTaskService().getTaskContent(taskId2 );
	             System.out.println(content);
	              // Start Task
	              gks.getTaskService().start(taskId, realm+"+PER_ADAMCROW63_AT_GMAIL_COM");    
	              showStatuses(gks);

	              gks.getTaskService().suspend(taskId, realm+"+PER_ADAMCROW63_AT_GMAIL_COM");    
	              showStatuses(gks);

	              gks.getTaskService().resume(taskId, realm+"+PER_ADAMCROW63_AT_GMAIL_COM");    
	              showStatuses(gks);
	              
	              gks.getTaskService().forward(taskId2, realm+"+PER_DOMENIC_AT_OUTCOME_LIFE", realm+"+PER_ANISH_AT_GADA_IO");

	              // Claim Task
//	              gks.getTaskService().claim(taskId, "acrow");
//	              showStatuses(gks);
//              
	              Map<String, Object> results = new HashMap<String, Object>();
	              results.put("Result", "Done");
	              gks.getTaskService().complete(taskId, realm+"+PER_ADAMCROW63_AT_GMAIL_COM", results);
	              showStatuses(gks);
 
	              results.put("Result", "some document data");

//	              long processInstanceId =
//	            		  processService.startProcess(deployUnit.getIdentifier(), "org.jbpm.writedocument");
//	            		  List<Long> taskIds =
//	            		  runtimeDataService.getTasksByProcessInstanceId(processInstanceId);
//	            		  Long taskId4 = taskIds.get(0);
//	            		  userTaskService.start(taskId, "john");
//	            		  UserTaskInstanceDesc task4 = runtimeDataService.getTaskById(taskId4);
//	            		  Map<String, Object> results = new HashMap<String, Object>();
//	            		  results.put("Result", "some document data");
//	            		  userTaskService.complete(taskId4, "john", results);
	              
				gks.injectEvent(authInitMsg); // This should create a new process
				gks.advanceSeconds(5, false);

				BaseEntity user = VertxUtils.getObject(serviceToken.getRealm(), "", userToken.getUserCode(),
						BaseEntity.class, serviceToken.getToken());

				
				// Ok, let's close the PER_USER1 task and see if the workflow continues....
				System.out.println("CLosing adam_user2 task");
				// first claim
				List<TaskSummary> per_user1_tasks = gks.getTaskService().getTasksAssignedAsPotentialOwner(realm+"+PER_USER1", null);
				TaskSummary taskSummary = per_user1_tasks.get(0);
	            gks.getTaskService().claim(taskSummary.getId(), realm+"+PER_USER1");
		        System.out.println("PER_USER1 CLAIMED "+showTaskNames(gks.getTaskService().getTasksAssignedAsPotentialOwner(realm+"+PER_USER1", null)));
	            gks.getTaskService().start(taskSummary.getId(), realm+"+PER_USER1");
		        System.out.println("PER_USER1 STARTED "+showTaskNames(gks.getTaskService().getTasksAssignedAsPotentialOwner(realm+"+PER_USER1", null)));
	            gks.getTaskService().suspend(taskSummary.getId(), realm+"+PER_USER1");
		        System.out.println("PER_USER1 SUSPENDED "+showTaskNames(gks.getTaskService().getTasksAssignedAsPotentialOwner(realm+"+PER_USER1", null)));
	            gks.getTaskService().resume(taskSummary.getId(), realm+"+PER_USER1");
		        System.out.println("PER_USER1 RESUMED "+showTaskNames(gks.getTaskService().getTasksAssignedAsPotentialOwner(realm+"+PER_USER1", null)));
	            
		        
		        Map<String, Object> results2 = new HashMap<String, Object>();
		        results2.put("Status", "good");
		        gks.getTaskService().complete(taskSummary.getId(), realm+"+PER_USER1",results2);
		        System.out.println("PER_USER1 COMPLETED  "+showTaskNames(gks.getTaskService().getTasksAssignedAsPotentialOwner(realm+"+PER_USER1", null))+":results sent="+results2);


				
				gks.injectEvent(msgLogout);
			} catch (Exception e) {
				e.printStackTrace();
				
			}
			finally {
				if (gks!=null) {
					gks.close();
				}
			}
		}


		private void createTestUsersGroups(GennyToken serviceToken) {
			
			if (!serviceToken.getUserCode().equals("PER_SERVICE")) {
				log.error("serviceToken needs to be serviceToken!");
				return;
			}
			PeopleAssignmentHelper peopleAssignmentHelper;
			peopleAssignmentHelper = new PeopleAssignmentHelper();
			List<OrganizationalEntity> organizationalEntities = new ArrayList<OrganizationalEntity>();
			String ids = "Software Developer,Project Manager";
			//peopleAssignmentHelper.processPeopleAssignments(ids, organizationalEntities, false);
			//PeopleAssignmentHelper.ACTOR_ID
			//PeopleAssignmentHelper.BUSINESSADMINISTRATOR_ID
			//PeopleAssignmentHelper.BUSINESSADMINISTRATOR_GROUP_ID
			//peopleAssignmentHelper.
			BaseEntity project = new BaseEntity("PRJ_" + serviceToken.getRealm().toUpperCase(),
					StringUtils.capitaliseAllWords(serviceToken.getRealm()));
			project.setRealm(serviceToken.getRealm());
			VertxUtils.writeCachedJson(serviceToken.getRealm(), "PRJ_" + serviceToken.getRealm().toUpperCase(),
					JsonUtils.toJson(project), serviceToken.getToken());
			VertxUtils.writeCachedJson(realm,  ":" + "PRJ_" + serviceToken.getRealm().toUpperCase(),JsonUtils.toJson(project), serviceToken.getToken());
			
			createTestGroup(realm, "GRP_USERS", "Users",serviceToken);
			createTestGroup(realm, "GRP_GADA", "GADA",serviceToken);
			createTestGroup(realm, "GRP_CROWTECH", "Crowtech",serviceToken);
			createTestGroup(realm, "GRP_OUTCOME", "Outcome.Life",serviceToken);
			createTestGroup(realm, "Administrators", "Administrators",serviceToken);
			List<String> gada =new ArrayList<String>();
			gada.add("GRP_USERS");
			gada.add("GRP_GADA");
			List<String> outcome =new ArrayList<String>();
			outcome.add("GRP_USERS");
			outcome.add("GRP_OUTCOME");
			List<String>crowtech =new ArrayList<String>();
			crowtech.add("GRP_USERS");
			crowtech.add("GRP_CROWTECH");
			crowtech.add("GRP_GADA");
			crowtech.add("Administrators"); // TODO
			List<String>both =new ArrayList<String>();
			both.add("GRP_USERS");
			both.add("GRP_GADA");
			both.add("GRP_OUTCOME");
		

			BaseEntity PER_USER1 = createTestUser(realm, "PER_USER1", "Ginger", gada,serviceToken);
			BaseEntity acrow = createTestUser(realm, "PER_ADAMCROW63_AT_GMAIL_COM", "Adam", crowtech,serviceToken);
			BaseEntity domenic = createTestUser(realm, "PER_DOMENIC_AT_OUTCOME_LIFE", "Domenic", gada,serviceToken);
			BaseEntity gerard = createTestUser(realm, "PER_GERARD_AT_OUTCOME_LIFE", "Gerard ", outcome,serviceToken);
			BaseEntity anish = createTestUser(realm, "PER_ANISH_AT_GADA_IO", "Anish", new ArrayList<String>(),serviceToken);
			BaseEntity chris = createTestUser(realm, "PER_CHRIS_AT_GADA_IO", "Chris", both,serviceToken);

		}

		private BaseEntity createTestGroup(String realm, String code, String name , GennyToken serviceToken) {
			BaseEntity group = createTestBaseEntity(realm, code, serviceToken);
			// save the new group
			VertxUtils.writeCachedJson(serviceToken.getRealm(), code,
					JsonUtils.toJson(group), serviceToken.getToken());
			Group JbpmGroup = (Group) TaskModelProvider.getFactory().newGroup(code);
			return group;
			
		}
		private BaseEntity createTestUser(String realm, String code, String name , List<String> groups, GennyToken serviceToken) {
			BaseEntity user = createTestBaseEntity(realm, code, serviceToken);
			// Link to each group
			Attribute linkAttribute = RulesUtils.getAttribute("LNK_CORE",serviceToken.getToken());

			for (String groupCode : groups) {
				 BaseEntity group = VertxUtils.getObject(serviceToken.getRealm(), "", groupCode,
							BaseEntity.class, serviceToken.getToken());
				 // now link the new user
				 try {
					group.addTarget(user, linkAttribute, 1.0);
					 VertxUtils.writeCachedJson(serviceToken.getRealm(), groupCode,
								JsonUtils.toJson(group), serviceToken.getToken());
				} catch (BadDataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			List<String> realmGroups = new ArrayList<String>();
			for (String str : groups) {
				realmGroups.add(realm+"+"+str);
			}
			String newGroupString = JsonUtils.toJson(realmGroups);
			Attribute groupsAttribute = RulesUtils.getAttribute("PRI_GROUPS",serviceToken.getToken());
			try {
				user.addAnswer(new Answer(user,user,groupsAttribute,newGroupString));
			} catch (BadDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// save the new user
			VertxUtils.writeCachedJson(serviceToken.getRealm(), code,
					JsonUtils.toJson(user), serviceToken.getToken());

			User JbpmUser = (User) TaskModelProvider.getFactory().newUser(code);
			

			return user;
			
		}
		
		private BaseEntity createTestBaseEntity(String realm, String code, GennyToken serviceToken)
		{
			code = code.toUpperCase();
			BaseEntity be = new BaseEntity(code,
					StringUtils.capitaliseAllWords(serviceToken.getRealm()));
			be.setRealm(serviceToken.getRealm());
			VertxUtils.writeCachedJson(serviceToken.getRealm(), code,
					JsonUtils.toJson(be), serviceToken.getToken());
		//	VertxUtils.writeCachedJson(realm,  ":" + code,JsonUtils.toJson(be), serviceToken.getToken());
			return be;

		}

		private void showStatuses(GennyKieSession gks)
		{
		       List<Status> statuses = new ArrayList<Status>();
		        statuses.add(Status.Ready);
		        statuses.add(Status.Completed);
		        statuses.add(Status.Created);
		        statuses.add(Status.Error);
		        statuses.add(Status.Exited);
		        statuses.add(Status.InProgress);
		        statuses.add(Status.Obsolete);
		        statuses.add(Status.Reserved);
		        statuses.add(Status.Suspended);
		        
	            List<String> groups = new ArrayList<String>();
	            groups.add(realm+"+GRP_GADA");


	        System.out.println("POTENTIAL PER_USER1  "+showTaskNames(gks.getTaskService().getTasksAssignedAsPotentialOwnerByStatus(realm+"+PER_USER1", statuses, null)));
            System.out.println("POTENTIAL acrow      "+showTaskNames(gks.getTaskService().getTasksAssignedAsPotentialOwnerByStatus(realm+"+PER_ADAMCROW63_AT_GMAIL_COM", statuses, null)));
            System.out.println("POTENTIAL dom        "+showTaskNames(gks.getTaskService().getTasksAssignedAsPotentialOwner(realm+"+PER_DOMENIC_AT_OUTCOME_LIFE", null)));
            System.out.println("POTENTIAL gerard     "+showTaskNames(gks.getTaskService().getTasksAssignedAsPotentialOwner(realm+"+PER_GERARD_AT_OUTCOME_LIFE",  null)));
            System.out.println("POTENTIAL chris      "+showTaskNames(gks.getTaskService().getTasksAssignedAsPotentialOwner(realm+"+PER_CHRIS_AT_GADA_IO", null)));
            System.out.println("POTENTIAL anish      "+showTaskNames(gks.getTaskService().getTasksAssignedAsPotentialOwner(realm+"+PER_ANISH_AT_GADA_IO",  null)));
            System.out.println("POTENTIAL chris+gada "+showTaskNames(gks.getTaskService().getTasksAssignedAsPotentialOwner(realm+"+PER_CHRIS_AT_GADA_IO", groups, "en-AU", 0,10)));
            System.out.println("POTENTIAL gada       "+showTaskNames(gks.getTaskService().getTasksAssignedAsPotentialOwner(null, groups, "en-AU", 0,10)));
            
            System.out.println("OWNED acrow          "+showTaskNames(gks.getTaskService().getTasksOwned(realm+"+PER_ADAMCROW63_AT_GMAIL_COM", null)));
            System.out.println();
		}
		
//	@Test
	public void headerTest()
	{
		System.out.println("Header test");
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
			GennyKieSession.loadAttributesJsonFromResources(userToken);

		} else {
			qRules = GennyJbpmBaseTest.setupLocalService();
			userToken = new GennyToken("userToken", qRules.getToken());
			serviceToken = new GennyToken("PER_SERVICE", qRules.getServiceToken());

		}

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		  BaseEntityUtils beUtils = new BaseEntityUtils(userToken);
		  beUtils.setServiceToken(serviceToken);
		  
//		  ShowFrame.display(userToken, "FRM_TABLE_VIEW", "FRM_CONTENT", "Test");
//		  String searchCode = "SBE_SEARCH_TEST";
//		  
//		  Answer answer = new Answer(userToken.getUserCode(),userToken.getUserCode(),"PRI_SEARCH_TEXT","univ");
//
// 
//   		  SearchEntity searchBE = new SearchEntity(searchCode,"Test Search")
//   		  	     .addSort("PRI_NAME","Created",SearchEntity.Sort.ASC)
//   		  	     .addFilter("PRI_NAME",SearchEntity.StringFilter.LIKE,"%"+answer.getValue()+"%")
//   		  	     .addColumn("PRI_NAME", "Name")
//   		      	 .addColumn("PRI_LANDLINE", "Phone")
//   		  	     .addColumn("PRI_EMAIL", "Email")
//   		  	     .addColumn("PRI_ADDRESS_CITY","City")
//   		  	     .addColumn("PRI_ADDRESS_STATE","State")
//   		  	     .setPageStart(0)
//   		  	     .setPageSize(20);
//
//   		VertxUtils.putObject(userToken.getRealm(), "", searchCode, searchBE,
//				userToken.getToken());  
//   		
//   		TableUtils.performSearch(serviceToken , beUtils, searchCode, answer);
//
 		
		BaseEntityUtils beUtils2 = new BaseEntityUtils(userToken); 
		
		/* get current search */
//		SearchEntity searchBE2 = TableUtils.getSessionSearch("SBE_SEARCHBAR",userToken);
//
//		
//		System.out.println("NEXT for "+searchBE2.getCode()); 
//		
//		Integer pageIndex = searchBE2.getValue("SCH_PAGE_START",0);
//		Integer pageSize = searchBE2.getValue("SCH_PAGE_SIZE", GennySettings.defaultPageSize);
//		pageIndex = pageIndex + pageSize;
//		
//		Integer pageNumber = 1;
//
//		if(pageIndex != 0){
//			pageNumber = pageIndex / pageSize;
//		}
//
//		Answer pageAnswer = new Answer(userToken.getUserCode(),searchBE2.getCode(), "SCH_PAGE_START", pageIndex+"");
//		Answer pageNumberAnswer = new Answer(userToken.getUserCode(),searchBE2.getCode(), "PRI_INDEX", pageNumber+"");
//
//		searchBE2 = beUtils2.updateBaseEntity(searchBE2, pageAnswer,SearchEntity.class);
//		searchBE2 = beUtils2.updateBaseEntity(searchBE2, pageNumberAnswer,SearchEntity.class);
//		
//		VertxUtils.putObject(beUtils2.getGennyToken().getRealm(), "", searchBE2.getCode(), searchBE2,
//			beUtils2.getGennyToken().getToken());
//		
//
//        ShowFrame.display(userToken, "FRM_TABLE_VIEW", "FRM_CONTENT", "Test");
//		TableUtils.performSearch(userToken , beUtils2, "SBE_SEARCHBAR", null);
		
		/* get current search */
		SearchEntity searchBE = TableUtils.getSessionSearch("SBE_SEARCHBAR",userToken);
			
		System.out.println("PREV for "+searchBE.getCode()); 
		
		Integer pageIndex = searchBE.getValue("SCH_PAGE_START",0);
		Integer pageSize = searchBE.getValue("SCH_PAGE_SIZE",GennySettings.defaultPageSize); // TODO, don't let this be 0
		pageIndex = pageIndex - pageSize;
		
		if (pageIndex <0) {
			pageIndex = 0;
		}
		
		Integer pageNumber = (pageIndex / pageSize) + 1;
		

		
			Answer pageAnswer = new Answer(beUtils.getGennyToken().getUserCode(),searchBE.getCode(), "SCH_PAGE_START", pageIndex+"");
			Answer pageNumberAnswer = new Answer(beUtils.getGennyToken().getUserCode(),searchBE.getCode(), "PRI_INDEX", pageNumber+"");

		searchBE = beUtils.updateBaseEntity(searchBE, pageAnswer,SearchEntity.class);
		searchBE = beUtils.updateBaseEntity(searchBE, pageNumberAnswer,SearchEntity.class);
			
			VertxUtils.putObject(beUtils.getGennyToken().getRealm(), "", searchBE.getCode(), searchBE,
				beUtils.getGennyToken().getToken());
			
	        ShowFrame.display(userToken, "FRM_TABLE_VIEW", "FRM_CONTENT", "Test");
 		    TableUtils.performSearch(userToken , beUtils, "SBE_SEARCHBAR", null);
	
   		
	}
		
	//@Test
	public void paginationTest()
	{
		System.out.println("Pagination Test");
		GennyToken userToken = null;
		GennyToken userToken2 = null;
		GennyToken serviceToken = null;
		QRules qRules = null;

		if (false) {
			userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
			userToken2 = GennyJbpmBaseTest.createGennyToken(realm, "user2", "Barry2 Allan2", "user");
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

		QEventMessage initMsg = new QEventMessage("EVT_MSG", "INIT_STARTUP");

		QEventMessage authInitMsg1 = new QEventMessage("EVT_MSG", "AUTH_INIT"); authInitMsg1.setToken(userToken.getToken());
		//QEventMessage authInitMsg2 = new QEventMessage("EVT_MSG", "AUTH_INIT");authInitMsg2.setToken(userToken2.getToken());
		QEventMessage msg1 = new QEventMessage("EVT_MSG", "INIT_1");
		

		/*  table next btn event */
		
		MessageData data = new MessageData("QUE_TABLE_NEXT_BTN");
		data.setParentCode("QUE_TABLE_FOOTER_GRP");
		data.setCode("QUE_TABLE_FOOTER_GRP");

		QEventMessage nextEvtMsg = new QEventMessage("EVT_MSG", "BTN_CLICK");
		nextEvtMsg.setToken(userToken.getToken());
		nextEvtMsg.setData(data);
		
		
		QEventMessage msgLogout1 = new QEventMessage("EVT_MSG", "LOGOUT");msgLogout1.setToken(userToken.getToken());
	//	QEventMessage msgLogout2 = new QEventMessage("EVT_MSG", "LOGOUT");msgLogout2.setToken(userToken2.getToken());

		
		List<Answer> answers = new ArrayList<Answer>();
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_FIRSTNAME", "Bruce"));
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_LASTNAME", "Wayne"));
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_ADDRESS_JSON", 
			//	"{\"street_number\":\"64\",\"street_name\":\"Fakenham Road\",\"suburb\":\"Ashburton\",\"state\":\"Victoria\",\"country\":\"AU\",\"postal_code\":\"3147\",\"full_address\":\"64 Fakenham Rd, Ashburton VIC 3147, Australia\",\"street_address\":\"64 Fakenham Road\"}"));
		"{\"street_number\":\"64\",\"street_name\":\"Fakenham Road\",\"suburb\":\"Ashburton\",\"state\":\"Victoria\",\"country\":\"AU\",\"postal_code\":\"3147\",\"full_address\":\"64 Fakenham Rd, Ashburton VIC 3147, Australia\",\"latitude\":-37.863208,\"longitude\":145.092359,\"street_address\":\"64 Fakenham Road\"}"));
		
		QDataAnswerMessage answerMsg = new QDataAnswerMessage(answers.toArray(new Answer[0]));
		answerMsg.setToken(userToken.getToken());
	
		Answer searchBarAnswer = new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_SEARCH_TEXT2", "univ");
		QDataAnswerMessage searchMsg = new QDataAnswerMessage(searchBarAnswer);
		searchMsg.setToken(userToken.getToken());

		// NOW SET UP Some baseentitys
		BaseEntity project = new BaseEntity("PRJ_" + serviceToken.getRealm().toUpperCase(),
				StringUtils.capitaliseAllWords(serviceToken.getRealm()));
		project.setRealm(serviceToken.getRealm());
		VertxUtils.writeCachedJson(serviceToken.getRealm(), "PRJ_" + serviceToken.getRealm().toUpperCase(),
				JsonUtils.toJson(project), serviceToken.getToken());
		VertxUtils.writeCachedJson(realm,  ":" + "PRJ_" + serviceToken.getRealm().toUpperCase(),JsonUtils.toJson(project), serviceToken.getToken());
		 BaseEntity project2 = VertxUtils.getObject(serviceToken.getRealm(), "", "PRJ_" + serviceToken.getRealm().toUpperCase(),
				BaseEntity.class, serviceToken.getToken());



		GennyKieSession gks = null;

		try {
			gks = GennyKieSession.builder(serviceToken,true)
					.addDrl("SignalProcessing")
					.addDrl("DataProcessing")
					.addDrl("EventProcessing")
					//.addDrl("InitialiseProject")
					//.addDrl("XXXPRI_SEARCH_TEXT2.drl")
					.addDrl("QUE_TABLE_NEXT_BTN.drl")
					//.addDrl("XXXQUE_TABLE_NEXT_BTN.drl")
					//.addJbpm("InitialiseProject")
					.addJbpm("Lifecycles")
					.addDrl("AuthInit")
					.addJbpm("AuthInit")

					.addToken(userToken)
					.build();
			gks.start();
			

			
			BaseEntity icn_sort = new BaseEntity("ICN_SORT","Icon Sort");
			try {
				
				icn_sort.addAttribute(RulesUtils.getAttribute("PRI_ICON_CODE", serviceToken.getToken()), 1.0, "sort");
				icn_sort.setRealm(realm);
				VertxUtils.writeCachedJson(realm,   "ICN_SORT",JsonUtils.toJson(icn_sort), serviceToken.getToken());

			} catch (BadDataException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			
			gks.injectEvent(initMsg); // This should create a new process

			gks.injectEvent(authInitMsg1); // This should create a new process
			gks.advanceSeconds(5, false);
			gks.injectEvent(authInitMsg1); // This should attach to existing process
			gks.advanceSeconds(5, false);

			gks.injectEvent(answerMsg); // This sends an answer to the first userSessio
			gks.advanceSeconds(5, false);
			
			BaseEntity user = VertxUtils.getObject(serviceToken.getRealm(), "", userToken.getUserCode(),
					BaseEntity.class, serviceToken.getToken());

			gks.injectEvent(searchMsg); // This sends a search bar request
			
			QEventMessage pageNextMsg = new QEventMessage("EVT_MSG", "QUE_TABLE_NEXT_BTN");pageNextMsg.setToken(userToken.getToken());
			QEventMessage pagePrevMsg = new QEventMessage("EVT_MSG", "QUE_TABLE_PREV_BTN");pagePrevMsg.setToken(userToken.getToken());
			gks.injectEvent(pageNextMsg); // This sends a page Next request
			//gks.injectEvent(pageNextMsg); // This sends a page Next request
		//	gks.injectEvent(pagePrevMsg); // This sends a page Prev request

			gks.injectEvent(msgLogout1);
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		finally {
			if (gks!=null) {
				gks.close();
			}
		}
	}

//@Test
public void testTableHeader() {
	System.out.println("Table test");
	GennyToken userToken = null;
	GennyToken userToken2 = null;
	GennyToken serviceToken = null;
	QRules qRules = null;

	if (false) {
		userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
		userToken2 = GennyJbpmBaseTest.createGennyToken(realm, "user2", "Barry2 Allan2", "user");
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
		GennyKieSession.loadAttributesJsonFromResources(userToken);
		
	}

	System.out.println("session     =" + userToken.getSessionCode());
	System.out.println("userToken   =" + userToken.getToken());
	System.out.println("serviceToken=" + serviceToken.getToken());

        BaseEntityUtils beUtils = new BaseEntityUtils(userToken);
        BaseEntity project = beUtils.getBaseEntityByCode("PRJ_" + serviceToken.getRealm().toUpperCase());

		GennyKieSession gks = null;

		try {
			gks = GennyKieSession.builder(serviceToken,true)
					.addDrl("SignalProcessing")
					.addDrl("DataProcessing")
					.addDrl("EventProcessing")
					.addDrl("InitialiseProject")
					.addDrl("XXXPRI_SEARCH_TEXT2.drl")
					.addJbpm("InitialiseProject")
					.addJbpm("Lifecycles")
					.addDrl("AuthInit")
					.addJbpm("AuthInit")

					.addToken(userToken)
					.build();
			gks.start();
			
          String searchBarString = "Adam";
        
		  SearchEntity searchBE = new SearchEntity("SBE_SEARCH","Search")
	  		  	     .addSort("PRI_NAME","Name",SearchEntity.Sort.ASC)
	  		  	     .addFilter("PRI_NAME",SearchEntity.StringFilter.LIKE,"%"+searchBarString+"%")
	  		  	     .addColumn("PRI_NAME", "Name")
	  		      	 .addColumn("PRI_LANDLINE", "Phone")
	  		  	     .addColumn("PRI_EMAIL", "Email")
	  		  	     .addColumn("PRI_MOBILE", "Mobile") 
	  		  	     .addColumn("PRI_ADDRESS_CITY","City")
	  		  	     .addColumn("PRI_ADDRESS_STATE","State")
	  		  	     .setPageStart(0)
	  		  	     .setPageSize(10);

	 	     searchBE.setRealm(serviceToken.getRealm());
	  	     
	  		 VertxUtils.putObject(serviceToken.getRealm(), "", searchBE.getCode(), searchBE, serviceToken.getToken());
	 
		  Answer answer = new Answer(userToken.getUserCode(),userToken.getUserCode(),"PRI_SEARCH_TEXT",searchBarString);
		  
		  			TableUtilsTest.performSearch(serviceToken , beUtils, "SBE_SEARCHBAR", answer);
   		  	     
  		  	     /* Send to front end */
   					
  		  	     GennyKieSession.displayForm("FRM_TABLE_VIEW", "FRM_CONTENT", userToken);
                System.out.println("Sent");
        } catch (Exception e) {
                System.out.println("Error " + e.getLocalizedMessage());
        }
}



	public void tableTest()
	{
		System.out.println("Table test");
		GennyToken userToken = null;
		GennyToken userToken2 = null;
		GennyToken serviceToken = null;
		QRules qRules = null;

		if (false) {
			userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
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
			GennyKieSession.loadAttributesJsonFromResources(userToken);
		}

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		/* Look up Search */
		  SearchEntity searchBE = new SearchEntity("SBE_SEARCH","Search")
	     .addSort("PRI_CREATED","Created",SearchEntity.Sort.DESC)
	     .addFilter("PRI_NAME",SearchEntity.StringFilter.LIKE,"%univ%")
	     .addColumn("PRI_NAME", "Name")
	     .addColumn("PRI_LANDLINE", "Phone")
	     .setPageStart(0)
	     .setPageSize(10);
	     
		  
	        Frame3 headerFrame = null;
			try {

				Validation tableCellValidation = new Validation("VLD_ANYTHING", "Anything", ".*");
        
				List<Validation> tableCellValidations = new ArrayList<>();
				tableCellValidations.add(tableCellValidation);
				
				ValidationList tableCellValidationList = new ValidationList();
				tableCellValidationList.setValidationList(tableCellValidations);

				DataType tableCellDataType = new DataType("DTT_TABLE_CELL_GRP", tableCellValidationList, "Table Cell Group", "");

				headerFrame = Frame3.builder("FRM_TABLE_HEADER")
				        .addTheme("THM_TABLE_BORDER",serviceToken).end()
				         .question("QUE_NAME_GRP")
							.addTheme("THM_QUESTION_GRP_LABEL", serviceToken).vcl(VisualControlType.GROUP).dataType(tableCellDataType).end()
							.addTheme("THM_DISPLAY_HORIZONTAL", serviceToken).weight(2.0).end()
							.addTheme("THM_TABLE_HEADER_CELL_WRAPPER", serviceToken).vcl(VisualControlType.VCL_WRAPPER).end()
							.addTheme("THM_TABLE_HEADER_CELL_GROUP_LABEL", serviceToken).vcl(VisualControlType.GROUP_LABEL).end()
							.addTheme("THM_DISPLAY_VERTICAL", serviceToken).dataType(tableCellDataType).weight(1.0).end()
				         .end()
				         .build();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	     
	         headerFrame.setRealm(serviceToken.getRealm());
	         FrameUtils2.toMessage(headerFrame, serviceToken);

		  
		  BaseEntityUtils beUtils = new BaseEntityUtils(userToken);
		  
          /* frame-root */
		Frame3 FRM_ROOT = null;
		try {
			Frame3 FRM_HEADER = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_HEADER",
						Frame3.class, serviceToken.getToken());//generateHeader();
			  Frame3 FRM_SIDEBAR = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_SIDEBAR",
						Frame3.class, serviceToken.getToken());//generateHeader();
			   Frame3 FRM_CONTENT = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_CONTENT",
						Frame3.class, serviceToken.getToken());//generateHeader();
			  Frame3 FRM_FOOTER = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_FOOTER",
						Frame3.class, serviceToken.getToken());//generateHeader();
			  Frame3 FRM_APP = Frame3.builder("FRM_APP")
			          .addTheme("THM_PROJECT", ThemePosition.FRAME, serviceToken).end()
			          .addFrame(FRM_HEADER, FramePosition.NORTH).end()
			          .addFrame(FRM_SIDEBAR, FramePosition.WEST).end()
    /*              .addFrame(FRM_TABS, FramePosition.CENTRE).end() */
			          .addFrame(FRM_CONTENT, FramePosition.CENTRE).end()
			          .addFrame(FRM_FOOTER, FramePosition.SOUTH).end().build();
			  FRM_ROOT = Frame3.builder("FRM_ROOT").addFrame(FRM_APP, FramePosition.CENTRE)
					  .end()
			          .build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

  Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();
  QDataBaseEntityMessage msg = FrameUtils2.toMessage(FRM_ROOT, serviceToken, askMsgs);
  msg.setToken(userToken.getToken());
  //qRules.publishCmd(msg);
  VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg));
//  for (QDataAskMessage askMsg : askMsgs) {
//          rules.publishCmd(askMsg, serviceToken.getUserCode(), userToken.getUserCode());
//  }

		  // Test sending a page
			QDataBaseEntityMessage msg2 = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_QUE_DASHBOARD_VIEW_MSG",
					QDataBaseEntityMessage.class, serviceToken.getToken());

			msg2.setToken(userToken.getToken());
			/* send message */
			// rules.publishCmd(msg2); // Send QDataBaseEntityMessage
			VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg2));
	
	 	     TableUtilsTest tableUtils = new TableUtilsTest(beUtils);
	  	     
	  	     QDataBaseEntityMessage  msg4 = tableUtils.fetchSearchResults(searchBE,beUtils.getGennyToken());
	  	     TableData tableData = tableUtils.generateTableAsks(searchBE,beUtils.getGennyToken(),  msg4);
	  	     log.info(tableData);

			//"FRM_QUE_DASHBOARD_VIEW","FRM_CONTENT"
	  	     Set<Ask> asksSet = new HashSet<Ask>();
	  	    asksSet.add(tableData.getAsk());
	  	    Ask[] askArray = asksSet.stream().toArray(Ask[]::new);
	  	    QDataAskMessage askMsg = new QDataAskMessage(askArray);
	  	    Set<QDataAskMessage> askSet = new HashSet<QDataAskMessage>();
	  	    askSet.add(askMsg);
	  	  List<QDataBaseEntityMessage> msgs = new ArrayList<QDataBaseEntityMessage>();
	  	  msgs.add(msg2);
	  	    
			GennyKieSession.sendData(serviceToken, userToken,"FRM_QUE_DASHBOARD_VIEW", "FRM_CONTENT", msgs, askSet);
	  	     
	  	     VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg)); // Send the results to the frontend to be put into the redux store
	  	     
	  	     
	  	     
	  	    
	  	    List<QDataBaseEntityMessage> msgs3 = tableData.getThemeMsgList();
	  	     // 
	  		GennyKieSession.sendData(serviceToken, userToken,"FRM_TABLE_VIEW", "FRM_CONTENT", msgs3, askSet);

	}
	
	
	
	//@Test
	public void newUserTest()
	{
		System.out.println("New User test");
		GennyToken userToken = null;
		GennyToken userToken2 = null;
		GennyToken serviceToken = null;
		QRules qRules = null;

		if (true) {
			userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
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

		QEventMessage authInitMsg1 = new QEventMessage("EVT_MSG", "AUTH_INIT"); authInitMsg1.setToken(userToken.getToken());
		QEventMessage authInitMsg2 = new QEventMessage("EVT_MSG", "AUTH_INIT");authInitMsg2.setToken(userToken2.getToken());
		
		MessageData data = new MessageData("QUE_TABLE_NEXT_BTN");
		data.setParentCode("QUE_TABLE_FOOTER_GRP");
		data.setRootCode("QUE_TABLE_FOOTER_GRP");

		QEventMessage nextEvtMsg = new QEventMessage("EVT_MSG", "BTN_CLICK");
		nextEvtMsg.setToken(userToken.getToken());
		nextEvtMsg.setData(data);

		QEventMessage msg1 = new QEventMessage("EVT_MSG", "INIT_1");
		QEventMessage msgLogout1 = new QEventMessage("EVT_MSG", "LOGOUT");msgLogout1.setToken(userToken.getToken());
		QEventMessage msgLogout2 = new QEventMessage("EVT_MSG", "LOGOUT");msgLogout2.setToken(userToken2.getToken());

		List<Answer> answers = new ArrayList<Answer>();
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_FIRSTNAME", "Bruce"));
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_LASTNAME", "Wayne"));
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_ADDRESS_JSON", 
				"{\"street_number\":\"64\",\"street_name\":\"Fakenham Road\",\"suburb\":\"Ashburton\",\"state\":\"Victoria\",\"country\":\"AU\",\"postal_code\":\"3147\",\"full_address\":\"64 Fakenham Rd, Ashburton VIC 3147, Australia\",\"street_address\":\"64 Fakenham Road\"}"));
		
		
		QDataAnswerMessage answerMsg = new QDataAnswerMessage(answers.toArray(new Answer[0]));
		answerMsg.setToken(userToken.getToken());
	
		Answer searchBarAnswer = new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_SEARCH_TEXT2", "Phantom");
		QDataAnswerMessage searchMsg = new QDataAnswerMessage(searchBarAnswer);
		searchMsg.setToken(userToken.getToken());

		// NOW SET UP Some baseentitys
		BaseEntity project = new BaseEntity("PRJ_" + serviceToken.getRealm().toUpperCase(),
				StringUtils.capitaliseAllWords(serviceToken.getRealm()));
		project.setRealm(serviceToken.getRealm());
		VertxUtils.writeCachedJson(serviceToken.getRealm(), "PRJ_" + serviceToken.getRealm().toUpperCase(),
				JsonUtils.toJson(project), serviceToken.getToken());
		VertxUtils.writeCachedJson(realm,  ":" + "PRJ_" + serviceToken.getRealm().toUpperCase(),JsonUtils.toJson(project), serviceToken.getToken());
		 BaseEntity project2 = VertxUtils.getObject(serviceToken.getRealm(), "", "PRJ_" + serviceToken.getRealm().toUpperCase(),
				BaseEntity.class, serviceToken.getToken());



		GennyKieSession gks = null;

		try {
			gks = GennyKieSession.builder(serviceToken,true)
					.addDrl("SignalProcessing")
					.addDrl("DataProcessing")
					.addDrl("EventProcessing")
					.addDrl("InitialiseProject")
					.addDrl("XXXPRI_SEARCH_TEXT2.drl")
					.addJbpm("InitialiseProject")
					.addJbpm("Lifecycles")
					.addDrl("AuthInit")
					.addJbpm("AuthInit")

					.addToken(userToken)
					.build();
			gks.start();
			
			
			BaseEntity icn_sort = new BaseEntity("ICN_SORT","Icon Sort");
			try {
				
				icn_sort.addAttribute(RulesUtils.getAttribute("PRI_ICON_CODE", serviceToken.getToken()), 1.0, "sort");
				icn_sort.setRealm(realm);
				VertxUtils.writeCachedJson(realm,   "ICN_SORT",JsonUtils.toJson(icn_sort), serviceToken.getToken());

			} catch (BadDataException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			
			gks.injectEvent(initMsg); // This should create a new process

			gks.injectEvent(authInitMsg1); // This should create a new process
			gks.advanceSeconds(5, false);
//			gks.injectEvent(authInitMsg2); // This should create a new process
			gks.advanceSeconds(5, false);
			gks.injectEvent(authInitMsg1); // This should attach to existing process
			gks.advanceSeconds(5, false);

			gks.injectEvent(answerMsg); // This sends an answer to the first userSessio
			gks.advanceSeconds(5, false);
			
			BaseEntity user = VertxUtils.getObject(serviceToken.getRealm(), "", userToken.getUserCode(),
					BaseEntity.class, serviceToken.getToken());

			gks.injectEvent(searchMsg); // This sends a search bar request
			
			
			
			gks.injectEvent(msgLogout1);
//			gks.injectEvent(msgLogout2);
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		finally {
			if (gks!=null) {
				gks.close();
			}
		}
	}
	
	//@Test
	public void answerRulesTest()
	{
		System.out.println("Test Answer Rules");
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

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage initMsg = new QEventMessage("EVT_MSG", "INIT_STARTUP");

		QEventMessage authInitMsg = new QEventMessage("EVT_MSG", "AUTH_INIT");
		QEventMessage msg1 = new QEventMessage("EVT_MSG", "INIT_1");
		QEventMessage msgLogout = new QEventMessage("EVT_MSG", "LOGOUT");

		List<Answer> answers = new ArrayList<Answer>();
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_FIRSTNAME", "Bruce"));
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_LASTNAME", "Wayne"));
		answers.add(new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_ADDRESS_JSON", 
				"{\"street_number\":\"64\",\"street_name\":\"Fakenham Road\",\"suburb\":\"Ashburton\",\"state\":\"Victoria\",\"country\":\"AU\",\"postal_code\":\"3147\",\"full_address\":\"64 Fakenham Rd, Ashburton VIC 3147, Australia\",\"street_address\":\"64 Fakenham Road\"}"));
		
		
		QDataAnswerMessage answerMsg = new QDataAnswerMessage(answers.toArray(new Answer[0]));
		
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
			gks = GennyKieSession.builder(serviceToken,true)
					.addDrl("DataProcessing")
					.addDrl("EventProcessing")
					.addDrl("InitialiseProject")
					.addJbpm("InitialiseProject")
					.addJbpm("userValidation")
					.addJbpm("Lifecycles")
					.addDrl("AuthInit")
					.addJbpm("AuthInit")

					.addToken(userToken)
					.build();
			gks.start();
			gks.injectEvent(initMsg); // This should create a new process

			gks.injectEvent(authInitMsg); // This should create a new process
			gks.advanceSeconds(5, false);

			gks.injectEvent(answerMsg); // This should create a new process
			gks.advanceSeconds(5, false);
			gks.injectEvent(msgLogout);

		} catch (Exception e) {
			e.printStackTrace();
			
		}
		finally {
			if (gks!=null) {
				gks.close();
			}
		}
	}
	//@Test
	public void processAddress()
	{
		
		Answer address = new Answer("PER_USER1", "PER_USER1", "PRI_ADDRESS_JSON", 
				"{\"street_number\":\"64\",\"street_name\":\"Fakenham Road\",\"suburb\":\"Ashburton\",\"state\":\"Victoria\",\"country\":\"AU\",\"postal_code\":\"3147\",\"full_address\":\"64 Fakenham Rd, Ashburton VIC 3147, Australia\",\"street_address\":\"64 Fakenham Road\"}");
		
		JsonObject addressDataJson = new JsonObject(address.getValue());

			System.out.println("The Address Json is  :: " + addressDataJson);

			List<Answer> answers = new ArrayList<Answer>();
			answers.add(new Answer("PER_USER1", "PER_USER1", "PRI_ADDRESS_COUNTRY", addressDataJson.getString("country")));
			answers.add(new Answer("PER_USER1", "PER_USER1", "PRI_ADDRESS_POSTCODE", addressDataJson.getString("postal_code")));
			answers.add(new Answer("PER_USER1", "PER_USER1", "PRI_ADDRESS_SUBURB", addressDataJson.getString("suburb")));
			answers.add(new Answer("PER_USER1", "PER_USER1", "PRI_ADDRESS_STATE", addressDataJson.getString("state")));
			answers.add(new Answer("PER_USER1", "PER_USER1", "PRI_ADDRESS_ADDRESS1", addressDataJson.getString("street_address")));
			answers.add(new Answer("PER_USER1", "PER_USER1", "PRI_ADDRESS_FULL", addressDataJson.getString("full_address")));
	
			
				
	}
	
	//@Test
	public void virtualQuestionTest() {
		System.out.println("Send Virtual Question");
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

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());


		// NOW SET UP Some baseentitys
		BaseEntity user = VertxUtils.readFromDDT(serviceToken.getRealm(),userToken.getUserCode(), true,
				serviceToken.getToken());

		BaseEntity project = VertxUtils.readFromDDT(serviceToken.getRealm(),"PRJ_"+realm.toUpperCase(), true,
				serviceToken.getToken());

		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
		BaseEntity sortIconBe = beUtils.getBaseEntityByCode("ICN_SORT");
        
		Context context = new Context(ContextType.ICON, sortIconBe, VisualControlType.VCL_ICON, 1.0);

		
		Frame3 FRM_POWERED_BY = null;
		
		try {
			FRM_POWERED_BY = Frame3.builder("FRM_POWERED_BY")
					.addTheme("THM_WIDTH_200",serviceToken).end()
					.addTheme("THM_COLOR_WHITE",serviceToken).end()   
					.question("QUE_POWERED_BY_GRP")
						.sourceAlias("PRJ_"+realm.toUpperCase())
						.targetAlias("PRJ_"+realm.toUpperCase())
						.addTheme("THM_FORM_LABEL_DEFAULT",serviceToken)
						.vcl(VisualControlType.VCL_LABEL)
						.end()
					.addTheme("THM_FORM_DEFAULT_REPLICA",serviceToken)
						.weight(3.0)
						.end()
					.end()
					.build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		BaseEntity[] bea = new BaseEntity[1];
//		bea[0] = project;
//		QDataBaseEntityMessage prjtest = new QDataBaseEntityMessage();
		qRules.publishCmd(project, "PROJECT");
		
		Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();

		QDataBaseEntityMessage msg = FrameUtils2.toMessage(FRM_POWERED_BY, serviceToken, askMsgs);
   //     qRules.publishCmd(msg);

        List<Tuple2<String,String>> sourceTargetCodes = new ArrayList<Tuple2<String,String>>();
        sourceTargetCodes.add(Tuple.of(serviceToken.getUserCode(),userToken.getUserCode()));
        
        for (QDataAskMessage askMsg : askMsgs) {
                qRules.publishCmd(askMsg, sourceTargetCodes);
        }
        System.out.println("Sent");
		
		System.out.println(user);
	}

	
	//@Test
	public void userSessionTest() {
		System.out.println("Show UserSession");
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
			gks = GennyKieSession.builder(serviceToken, true)
					.addJbpm("userLifecycle.bpmn")
					.addJbpm("userSession.bpmn")
					.addJbpm("auth_init.bpmn")
					.addJbpm("showDashboard.bpmn")
					.addJbpm("userValidation.bpmn")
					.addDrl("SendUserData")
					.addToken(userToken)
					.build();
			gks.start();

			gks.injectEvent(authInitMsg); // This should create a new process
			gks.advanceSeconds(5, true);
			gks.injectEvent(authInitMsg); // check that auth init with same session is ok and that process Id is looked up!
			gks.advanceSeconds(5, true);
			gks.injectEvent(msgLogout);

			BaseEntity user = VertxUtils.getObject(serviceToken.getRealm(), "", userToken.getUserCode(),
					BaseEntity.class, serviceToken.getToken());
			System.out.println("final user created " + user);
			System.out.println("Sent");

		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			gks.close();
		}
	}

	// @Test
	public void userSessionTest2() {
		System.out.println("Show UserSession");
		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage authInitMsg = new QEventMessage("EVT_MSG", "AUTH_INIT");
		QEventMessage msg1 = new QEventMessage("EVT_MSG", "INIT_1");
		QEventMessage msgLogout = new QEventMessage("EVT_MSG", "LOGOUT");

		GennyKieSession gks = null;

		try {
			gks = GennyKieSession.builder(serviceToken, false).addJbpm("test_session_1.bpmn")
					.addJbpm("test_session_2.bpmn").addJbpm("dashboard.bpmn").addToken(userToken).build();
			gks.start();

//				gks.advanceSeconds(2, true);
//				gks.injectSignal("userMessage", msg1);
//				gks.advanceSeconds(2, true);

			gks.injectEvent(authInitMsg);
			gks.advanceSeconds(2, true);
//				gks.injectSignal("userMessage", msgLogout);

//			for (int i=0;i<2;i++) {
//				gks.displayForm("FRM_DASHBOARD",userToken);
//				gks.advanceSeconds(2, true);
//				gks.displayForm("FRM_DASHBOARD2",userToken);
//				gks.advanceSeconds(2, true);
//			}

			// gks.sendLogout(userToken);

			System.out.println("Sent");

		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			gks.close();
		}
	}

//@Test
	public void displayBucketPage() {
		System.out.println("Show Bucket Page");
		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage msg = new QEventMessage("EVT_MSG", "INIT_STARTUP");

		GennyKieSession gks = null;
		try {
			gks = GennyKieSession.builder(serviceToken, false).addJbpm("show_bucket_page.bpmn").addFact("msg", msg)
					.addToken(userToken).build();

			gks.start();
			gks.injectSignal("inputSignal", "Hello");
			gks.advanceSeconds(20, false);

			System.out.println("Sent");

		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			gks.close();
		}
	}

	// @Test
	public void displayTestPage1() {
		System.out.println("Show test page 1");
		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage msg = new QEventMessage("EVT_MSG", "INIT_STARTUP");

		GennyKieSession gks = null;
		try {
			gks = GennyKieSession.builder(serviceToken, false).addJbpm("test_page_1.bpmn").addFact("msg", msg)
					.addToken(userToken).build();

			gks.start();
			gks.injectSignal("inputSignal", "Hello");
			gks.advanceSeconds(20, false);

			System.out.println("Sent");

		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			gks.close();
		}
	}

//@Test
	public void sendAuthInit() {

		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		QEventMessage msg = new QEventMessage("EVT_MSG", "AUTH_INIT");

		GennyKieSession gks = null;
		try {
			gks = GennyKieSession.builder(serviceToken).addDrl(DRL_SEND_USER_DATA_DIR) // send the initial User data
																						// using the rules
					.addJbpm("adhoc.bpmn").addFact("qRules", rules).addFact("msg", msg).addToken(serviceToken)
					.addToken(userToken).build();

			gks.start();

			gks.advanceSeconds(5, true);
			gks.injectMessage(msg);
			gks.advanceSeconds(3, true);
		} finally {
			gks.close();
		}
	}

	// @Test
	public void simpleTest() {
		GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
		GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
		QRules qRules = new QRules(eventBusMock, userToken.getToken());
		qRules.set("realm", userToken.getRealm());
		qRules.setServiceToken(serviceToken.getToken());

		Theme THM_NOT_INHERITBALE = Theme.builder("THM_NOT_INHERITBALE")
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();
		Frame3 logo = Frame3.builder("FRM_PROJECT_LOGO").addTheme(THM_NOT_INHERITBALE).end().build();

		Frame3 frameRoot = Frame3.builder("FRM_ROOT").addFrame(logo, FramePosition.NORTH).end().build();

		Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();
		QDataBaseEntityMessage msg = FrameUtils2.toMessage(frameRoot, serviceToken, askMsgs);

		String test = JsonUtils.toJson(msg);
		System.out.println(test);
	}

	// @Test
	public void quickTest() {

		GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
		GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
		QRules qRules = new QRules(eventBusMock, userToken.getToken());
		qRules.set("realm", userToken.getRealm());
		qRules.setServiceToken(serviceToken.getToken());

		System.out.println("session=" + userToken.getSessionCode());
		System.out.println("userToken=" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage msg = new QEventMessage("EVT_MSG", "AUTH_INIT");

		GennyKieSession gks = null;
		try {
			gks = GennyKieSession.builder(serviceToken, true).addJbpm("adam_test_1.bpmn").addFact("qRules", qRules)
					.addFact("msg", msg).addToken(serviceToken).addToken(userToken).build();

			gks.start();

			gks.advanceSeconds(10, false);
			gks.broadcastSignal("inputSignal", "Hello");
			// gks.getKieSession().getQueryResults(query, arguments)
			gks.advanceSeconds(1, false);
		} finally {
			gks.close();
		}

	}

	// Only run if no background service running, used to test GenerateRules

	// @Test
	public void initLocalRulesTest() {
		System.out.println("Run the Project Initialisation");
		VertxUtils.cachedEnabled = true; // don't try and use any local services
		GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "userToken");
		GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "serviceToken");
		QRules qRules = new QRules(eventBusMock, userToken.getToken());
		qRules.set("realm", userToken.getRealm());
		qRules.setServiceToken(serviceToken.getToken());

		System.out.println("session     =" + userToken.getSessionCode());
		System.out.println("userToken   =" + userToken.getToken());
		System.out.println("serviceToken=" + serviceToken.getToken());

		QEventMessage msg = new QEventMessage("EVT_MSG", "INIT_STARTUP");

		GennyKieSession gks = null;
		try {
			gks = GennyKieSession.builder(serviceToken, false).addJbpm("init_project.bpmn").addDrl("GenerateSearches")
					.addDrl("GenerateThemes").addDrl("GenerateFrames").addFact("qRules", qRules).addFact("msg", msg)

					.build();

			gks.start();

			gks.advanceSeconds(20, false);
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			gks.close();
		}

	}

	// @Test
	public void testTheme() {
		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		// Theme THM_DUMMY =
		// Theme.builder("THM_DUMMY").addAttribute().height(100).end().addAttribute().width(90).end()
		// .build();
		Theme THM_DUMMY = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_DUMMY", Theme.class,
				serviceToken.getToken());

//		Theme THM_DISPLAY_VERTICAL = Theme.builder("THM_DISPLAY_VERTICAL")
//				.name("Display Visual Controls Vertically") /* Optional - defaults to the code */
//				.addAttribute(ThemeAttributeType.PRI_CONTENT).flexDirection("column").shadowOffset().height(5).width(5)
//				.end().maxWidth(600).padding(10).end().addAttribute() /* defaults to ThemeAttributeType.PRI_CONTENT */
//				.justifyContent("flex-start").end().build();
		Theme THM_DISPLAY_VERTICAL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_DISPLAY_VERTICAL",
				Theme.class, serviceToken.getToken());

//		Theme THM_DISPLAY_HORIZONTAL = Theme.builder("THM_DISPLAY_HORIZONTAL").addAttribute().flexDirection("row").end()
//				.build();
		Theme THM_DISPLAY_HORIZONTAL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_DISPLAY_HORIZONTAL",
				Theme.class, serviceToken.getToken());

//		Theme THM_BACKGROUND_WHITE = Theme.builder("THM_BACKGROUND_WHITE").addAttribute().backgroundColor("white").end()
//				.build();
		Theme THM_BACKGROUND_WHITE = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BACKGROUND_WHITE",
				Theme.class, serviceToken.getToken());

//		Theme THM_BACKGROUND_GREEN = Theme.builder("THM_BACKGROUND_GREEN").addAttribute().backgroundColor("green").end()
//				.build();
		Theme THM_BACKGROUND_GREEN = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BACKGROUND_GREEN",
				Theme.class, serviceToken.getToken());

//		Theme THM_BACKGROUND_YELLOW = Theme.builder("THM_BACKGROUND_YELLOW").addAttribute().backgroundColor("yellow")
//				.end().build();
		Theme THM_BACKGROUND_YELLOW = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BACKGROUND_YELLOW",
				Theme.class, serviceToken.getToken());

//		Theme THM_BACKGROUND_RED = Theme.builder("THM_BACKGROUND_RED").addAttribute().backgroundColor("red").end()
//				.build();
		Theme THM_BACKGROUND_RED = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BACKGROUND_RED", Theme.class,
				serviceToken.getToken());

//		Theme THM_BACKGROUND_GRAY = Theme.builder("THM_BACKGROUND_GRAY").addAttribute().backgroundColor("gray").end()
//				.build();
		Theme THM_BACKGROUND_GRAY = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BACKGROUND_GRAY",
				Theme.class, serviceToken.getToken());

		Theme THM_BACKGROUND_ORANGE = Theme.builder("THM_BACKGROUND_ORANGE").addAttribute().backgroundColor("orange")
				.end().build();

		Theme THM_BACKGROUND_BLACK = Theme.builder("THM_BACKGROUND_BLACK").addAttribute().backgroundColor("black").end()
				.build();

//		Theme THM_BACKGROUND_BLUE = Theme.builder("THM_BACKGROUND_BLUE").addAttribute().backgroundColor("blue").end()
//				.build();
		Theme THM_BACKGROUND_BLUE = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BACKGROUND_BLUE",
				Theme.class, serviceToken.getToken());

		Theme THM_BACKGROUND_INTERNMATCH = Theme.builder("THM_BACKGROUND_INTERNMATCH").addAttribute()
				.backgroundColor("#233a4e").end().build();

//		Theme THM_WIDTH_300 = Theme.builder("THM_WIDTH_300").addAttribute().width(300).end().build();
		Theme THM_WIDTH_300 = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_WIDTH_300", Theme.class,
				serviceToken.getToken());

//		Theme THM_FORM_INPUT_DEFAULT = Theme.builder("THM_FORM_INPUT_DEFAULT").addAttribute().borderBottomWidth(1)
//				.borderColor("#ddd").borderStyle("solid").placeholderColor("#888").end()
//				.addAttribute(ThemeAttributeType.PRI_CONTENT_HOVER).borderColor("#aaa").end()
//				.addAttribute(ThemeAttributeType.PRI_CONTENT_ACTIVE).borderColor("green").end()
//				.addAttribute(ThemeAttributeType.PRI_CONTENT_ERROR).borderColor("red").color("red").end().build();

		Theme THM_FORM_INPUT_DEFAULT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_FORM_INPUT_DEFAULT",
				Theme.class, serviceToken.getToken());

//		Theme THM_FORM_LABEL_DEFAULT = Theme.builder("THM_FORM_LABEL_DEFAULT").addAttribute().bold(true).size("md")
//				.end().build();
		Theme THM_FORM_LABEL_DEFAULT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_FORM_LABEL_DEFAULT",
				Theme.class, serviceToken.getToken());

//		Theme THM_FORM_WRAPPER_DEFAULT = Theme.builder("THM_FORM_WRAPPER_DEFAULT").addAttribute().marginBottom(10)
//				.padding(10).end().addAttribute(ThemeAttributeType.PRI_CONTENT_ERROR).backgroundColor("#fc8e6").end()
//				.build();
		Theme THM_FORM_WRAPPER_DEFAULT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_FORM_WRAPPER_DEFAULT",
				Theme.class, serviceToken.getToken());

//		Theme THM_FORM_ERROR_DEFAULT = Theme.builder("THM_FORM_ERROR_DEFAULT").addAttribute().color("red").end()
//				.build();
		Theme THM_FORM_ERROR_DEFAULT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_FORM_ERROR_DEFAULT",
				Theme.class, serviceToken.getToken());

//		Theme THM_FORM_DEFAULT = Theme.builder("THM_FORM_DEFAULT").addAttribute().backgroundColor("none").end()
//				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_TITLE, true).end()
//				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_DESCRIPTION, true).end()
//				.addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true).end()
//				.addAttribute(ThemeAttributeType.PRI_HAS_REQUIRED, true).end()
//				.addAttribute(ThemeAttributeType.PRI_HAS_ICON, true).end()
//				.build();
		Theme THM_FORM_DEFAULT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_FORM_DEFAULT", Theme.class,
				serviceToken.getToken());

//		Theme THM_FORM_CONTAINER_DEFAULT = Theme.builder("THM_FORM_CONTAINER_DEFAULT").addAttribute()
//				.backgroundColor("none").padding(10).maxWidth(700).width("100%").shadowColor("#000").shadowOpacity(0.4)
//				.shadowRadius(0).shadowOffset().width(0).height(0).end().end()
//				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_TITLE, true).end()
//				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_DESCRIPTION, true).end()
//				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, true).end().build();
		Theme THM_FORM_CONTAINER_DEFAULT = VertxUtils.getObject(serviceToken.getRealm(), "",
				"THM_FORM_CONTAINER_DEFAULT", Theme.class, serviceToken.getToken());

//		Frame3 FRM_DUMMY2 = Frame3.builder("FRM_DUMMY").addTheme(THM_DUMMY).end().build();
//		String td2 = JsonUtils.toJson(THM_DUMMY);
//		ThemeDouble td = new ThemeDouble(THM_DUMMY,1.0);
//		String js2 = JsonUtils.toJson(td);
//		String js = JsonUtils.toJson(FRM_DUMMY2);
//		VertxUtils.putObject(serviceToken.getRealm(), "", FRM_DUMMY2.getCode(), FRM_DUMMY2, serviceToken.getToken());

		Frame3 FRM_DUMMY = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_DUMMY", Frame3.class,
				serviceToken.getToken());

//		Frame3 centre = Frame3.builder("FRM_CENTRE").addFrame(FRM_DUMMY, FramePosition.CENTRE).end().build();
		Frame3 FRM_CENTRE = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_CENTRE", Frame3.class,
				serviceToken.getToken());

//		Frame3 profile = Frame3.builder("FRM_PROFILE").addTheme(THM_DISPLAY_HORIZONTAL).end()
//				.addTheme(THM_BACKGROUND_RED).end().addFrame(FRM_DUMMY, FramePosition.CENTRE).end().build();
		Frame3 FRM_PROFILE = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_PROFILE", Frame3.class,
				serviceToken.getToken());

//		Frame3 header = Frame3.builder("FRM_HEADER").addFrame(FRM_PROFILE, FramePosition.EAST).end().build();
		Frame3 FRM_HEADER = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_HEADER", Frame3.class,
				serviceToken.getToken());

//		Frame3 notes = Frame3.builder("FRM_NOTES").addTheme(THM_WIDTH_300).end()/*
//																				 * .addTheme(THM_DISPLAY_VERTICAL).end()
//																				 */
//				.addTheme(THM_BACKGROUND_RED).end().question("QUE_USER_COMPANY_GRP").end().build();
		Frame3 FRM_NOTES = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_NOTES", Frame3.class,
				serviceToken.getToken());

//		Frame3 FRM_SIDEBAR2 = Frame3.builder("FRM_SIDEBAR2")
//				/* .addTheme(THM_WIDTH_300).end() *//*
//													 * .addTheme(THM_DISPLAY_VERTICAL) .end()
//													 */.addTheme(THM_BACKGROUND_GRAY).end()
//				.question("QUE_USER_PROFILE_GRP").addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT)
//				.weight(2.0).end().addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
//				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
//				.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT)
//				.weight(3.0).end().addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();
		Frame3 FRM_SIDEBAR2 = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_SIDEBAR2", Frame3.class,
				serviceToken.getToken());

//		Frame3 sidebar3 = Frame3.builder("FRM_SIDEBAR3")
//				/* .addTheme(THM_WIDTH_300).end() *//*
//													 * .addTheme(THM_DISPLAY_VERTICAL) .end()
//													 */.addTheme(THM_BACKGROUND_YELLOW).end()
//				.question("QUE_USER_PROFILE_GRP").addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT)
//				.weight(2.0).end().addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
//				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
//				.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT)
//				.weight(3.0).end().addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();
		Frame3 FRM_SIDEBAR3 = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_SIDEBAR3", Frame3.class,
				serviceToken.getToken());

//		Frame3 sidebar = Frame3.builder("FRM_SIDEBAR")
//				// .addTheme(THM_WIDTH_300).end()
//
//				/*
//				 * .addTheme().addAttribute().width(400).end().end().addTheme(
//				 * THM_DISPLAY_VERTICAL).end()
//				 */
//				.addTheme(THM_BACKGROUND_GREEN).end().question("QUE_FIRSTNAME")
//				// .question("QUE_USER_PROFILE_GRP")
//
//				.addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT).weight(2.0).end()
//				.addTheme(THM_FORM_LABEL_DEFAULT).vcl(VisualControlType.VCL_LABEL).end()
//				.addTheme(THM_FORM_WRAPPER_DEFAULT).vcl(VisualControlType.VCL_WRAPPER).end()
//				.addTheme(THM_FORM_ERROR_DEFAULT).vcl(VisualControlType.VCL_ERROR).end().addTheme(THM_FORM_DEFAULT)
//				.weight(3.0).end().addTheme(THM_FORM_CONTAINER_DEFAULT).weight(2.0).end().end().build();

		Frame3 FRM_SIDEBAR = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_SIDEBAR", Frame3.class,
				serviceToken.getToken());

//		Frame3 footer = Frame3.builder("FRM_FOOTER").addFrame(FRM_DUMMY, FramePosition.CENTRE).end()
//				.addTheme(THM_BACKGROUND_BLUE).end().build();
		Frame3 FRM_FOOTER = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_FOOTER", Frame3.class,
				serviceToken.getToken());

//		Frame3 FRM_MAINFRAME = Frame3.builder("FRM_MAIN").addTheme(THM_BACKGROUND_WHITE).end()
//				.addFrame(FRM_SIDEBAR, FramePosition.WEST).end().addFrame(FRM_SIDEBAR2, FramePosition.WEST).end()
//				.addFrame(FRM_SIDEBAR3, FramePosition.WEST).end()
//				// .addFrame(notes,FramePosition.EAST).end()
//				.addFrame(FRM_FOOTER, FramePosition.SOUTH).end().addFrame(FRM_CENTRE, FramePosition.CENTRE).end()
//				.addFrame(FRM_HEADER, FramePosition.NORTH).end().build();

		Frame3 FRM_MAINFRAME = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_MAINFRAME", Frame3.class,
				serviceToken.getToken());

//		Frame3 FRM_ROOT = Frame3.builder("FRM_ROOT")
//				.addFrame(FRM_MAINFRAME).end().build();

		Frame3 FRM_ROOT = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_ROOT", Frame3.class,
				serviceToken.getToken());

		Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();

		QDataBaseEntityMessage msg = FrameUtils2.toMessage(FRM_ROOT, serviceToken, askMsgs);

//		VertxUtils.putObject(serviceToken.getRealm(), "", "FRM_ROOT_MSG", msg, serviceToken.getToken());

		QDataBaseEntityMessage msg2 = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_ROOT_MSG",
				QDataBaseEntityMessage.class, serviceToken.getToken());

		msg2.setToken(userToken.getToken());
		/* send message */
		// rules.publishCmd(msg2); // Send QDataBaseEntityMessage
		VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg2));
//		String askMsgsStr = JsonUtils.toJson(askMsgs);
//		VertxUtils.putObject(serviceToken.getRealm(), "", "DESKTOP_ASKS", askMsgsStr, serviceToken.getToken());

		Type setType = new TypeToken<Set<QDataAskMessage>>() {
		}.getType();

		String askMsgs2Str = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_ROOT_ASKS", String.class,
				serviceToken.getToken());

		Set<QDataAskMessage> askMsgs2 = JsonUtils.fromJson(askMsgs2Str, setType);

		System.out.println("Sending Asks");
		for (QDataAskMessage askMsg : askMsgs) {
			rules.publishCmd(askMsg, serviceToken.getUserCode(), userToken.getUserCode()); // Send associated
																							// QDataAskMessage
		}

		System.out.println("Sent");
	}

	// @Test
	public void testCacheTheme() {

		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		System.out.println("Starting");

		QDataBaseEntityMessage msg2 = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_ROOT_MSG",
				QDataBaseEntityMessage.class, serviceToken.getToken());

		/* send message */
		rules.publishCmd(msg2); // Send QDataBaseEntityMessage

		Type setType = new TypeToken<Set<QDataAskMessage>>() {
		}.getType();

		String askMsgs2Str = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_ROOT_ASKS", String.class,
				serviceToken.getToken());

		Set<QDataAskMessage> askMsgs2 = JsonUtils.fromJson(askMsgs2Str, setType);

		System.out.println("Sending Asks");
		for (QDataAskMessage askMsg : askMsgs2) {
			rules.publishCmd(askMsg, serviceToken.getUserCode(), userToken.getUserCode()); // Send associated
																							// QDataAskMessage
		}

		System.out.println("Sent2");

	}

	// @Test
	public void testLogout() {

	}

	// @Test
	public void formsTest() {
		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		String apiUrl = GennySettings.qwandaServiceUrl + "/service/forms";
		System.out.println("Fetching setup info from " + apiUrl);
		System.out.println("userToken (ensure user has test role) = " + userToken);
		try {
			String jsonFormCodes = QwandaUtils.apiGet(apiUrl, userToken.getToken());
			if (!"You need to be a test.".equals(jsonFormCodes)) {
				Type type = new TypeToken<List<String>>() {
				}.getType();
				List<String> formCodes = JsonUtils.fromJson(jsonFormCodes, type);
				System.out.println("Form Codes=" + formCodes);

				for (String formCode : formCodes) {
					// rules.sendForm("QUE_ADD_HOST_COMPANY_GRP", rules.getUser().getCode(),
					// rules.getUser().getCode());
				}

			} else {
				System.out.println("Ensure that the user you are using has a 'test' role ...");
			}

		} catch (Exception e) {

		}
	}
	
 String showTaskNames(List<TaskSummary> tasks)
 {
	 String ret = "";
	 if (tasks.isEmpty()) {
		return "(empty)"; 
	 } 
	 for (TaskSummary task : tasks) {
		 ret += "["+task.getName()+"("+task.getProcessId()+"):"+task.getStatusId()+"],";
	 }
	 return ret;
 }
 
// public static Map<Operation, List<OperationCommand>> initMVELOperations() {  
//	  
//     Map<String, Object> vars = new HashMap<String, Object>();  
//
//     // Search operations-dsl.mvel, if necessary using superclass if TaskService is subclassed  
//     InputStream is = null;  
//     // for (Class<?> c = getClass(); c != null; c = c.getSuperclass()) {  
//     is = MVELLifeCycleManager.class.getResourceAsStream("/operations-dsl.mvel");  
////         if (is != null) {  
////             break;  
////         }  
//     //}  
//     if (is == null) {  
//         throw new RuntimeException("Unable To initialise TaskService, could not find Operations DSL");  
//     }  
//     Reader reader = new InputStreamReader(is);  
//     try {  
//         return (Map<Operation, List<OperationCommand>>) eval(toString(reader), vars);  
//     } catch (IOException e) {  
//         throw new RuntimeException("Unable To initialise TaskService, could not load Operations DSL");  
//     }  
//
//
// }  
}
