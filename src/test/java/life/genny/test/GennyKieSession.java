package life.genny.test;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.drools.core.time.impl.PseudoClockScheduler;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.jbpm.test.JbpmJUnitBaseTestCase.Strategy;
import org.kie.api.command.Command;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.internal.command.CommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import life.genny.jbpm.customworkitemhandlers.AwesomeHandler;
import life.genny.jbpm.customworkitemhandlers.NotificationWorkItemHandler;
import life.genny.jbpm.customworkitemhandlers.ShowAllFormsHandler;
import life.genny.models.GennyToken;
import life.genny.rules.listeners.JbpmInitListener;

public class GennyKieSession extends  JbpmJUnitBaseTestCase {
	
	private static final Logger log
    = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());


	private static final String DRL_PROJECT = "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/SendUserData/project.drl";
	private static final String DRL_USER_COMPANY = "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/SendUserData/user_company.drl";
	private static final String DRL_USER = "rulesCurrent/shared/_BPMN_WORKFLOWS/AuthInit/SendUserData/user.drl";
	private static final String DRL_EVENT_LISTENER_SERVICE_SETUP = "rulesCurrent/shared/_BPMN_WORKFLOWS/Initialise_Project/eventListenerServiceSetup.drl";
	private static final String DRL_EVENT_LISTENER_USER_SETUP = "rulesCurrent/shared/_BPMN_WORKFLOWS/Initialise_Project/eventListenerUserSetup.drl";

	Map<String, ResourceType> resources = new HashMap<String, ResourceType>();

	
	private KieSession kieSession;
	ProcessInstance processInstance;

	private List<String> jbpms;
	private List<String> drls;
	private List<Tuple2<String,WorkItemHandler>> workItemHandlers;
	
	private Map<String,GennyToken> tokens = new HashMap<String,GennyToken>();
	
    PseudoClockScheduler sessionClock = kieSession.getSessionClock();

	
	List<Command<?>> cmds = new ArrayList<Command<?>>();

	/**
	 * static factory method for builder
	 */
	public static Builder builder() {
		return new GennyKieSession.Builder();
	}

	/**
	 * forces use of the Builder
	 */
	private GennyKieSession() {
	}
	
	public ProcessInstance startProcess(String processId)
	{
		processInstance = kieSession.startProcess(processId);
		sessionClock = kieSession.getSessionClock();
		
		return processInstance;
	}
	
	public long advanceSeconds(long amount)
	{
		long absoluteTime = sessionClock.advanceTime(amount, TimeUnit.SECONDS);
		return absoluteTime;
	}
	
	private void setup()
	{
		System.setProperty("drools.clockType", "pseudo");

		for (String p : jbpms) {
			resources.put(p, ResourceType.BPMN2);
		}
		for (String p : drls) {
			resources.put(p, ResourceType.DRL);
		}
		
		String[] coredrls = { DRL_PROJECT, DRL_USER_COMPANY, DRL_USER, DRL_EVENT_LISTENER_SERVICE_SETUP,
				DRL_EVENT_LISTENER_USER_SETUP };

		for (String p : coredrls) {
			resources.put(p, ResourceType.DRL);
		}

		createRuntimeManager(Strategy.SINGLETON, resources, null);
		KieSession kieSession = getRuntimeEngine().getKieSession();
		// Register handlers
		addWorkItemHandlers();
		if (tokens.containsKey("userToken")) {
			kieSession.addEventListener(new JbpmInitListener(tokens.get("userToken")));
		}
		kieSession.setGlobal("log", log);

	}

	
	protected void addWorkItemHandlers()
	{
		kieSession.getWorkItemManager().registerWorkItemHandler("Awesome", new AwesomeHandler());
		kieSession.getWorkItemManager().registerWorkItemHandler("Notification", new NotificationWorkItemHandler());
		kieSession.getWorkItemManager().registerWorkItemHandler("ShowAllForms", new ShowAllFormsHandler());
		
		for (Tuple2<String,WorkItemHandler> wih : workItemHandlers) {
			kieSession.getWorkItemManager().registerWorkItemHandler(wih._1,wih._2);
		}

	}
	
	/**
	 * @return the kieSession
	 */
	public KieSession getKieSession() {
		return kieSession;
	}

	
	
	/**
	 * @return the processInstance
	 */
	public ProcessInstance getProcessInstance() {
		return processInstance;
	}

	/**
	 * @param processInstance the processInstance to set
	 */
	public void setProcessInstance(ProcessInstance processInstance) {
		this.processInstance = processInstance;
	}

	/**
	 * @param kieSession the kieSession to set
	 */
	public void setKieSession(KieSession kieSession) {
		this.kieSession = kieSession;
	}

	public void assertNodeTriggered(String nodeName)
	{
		assertNodeTriggered(this.getProcessInstance().getId(), nodeName);
	}
	
	public void assertProcessInstanceCompleted()
	{
		assertProcessInstanceCompleted(this.getProcessInstance().getId());
	}

	/**
	 * more fluent Builder
	 */
	public static class Builder {

		private GennyKieSession managedInstance = new GennyKieSession();

		public Builder() {
		}



		/**
		 * fluent setter for frameCodes in the list
		 * 
		 * @param none
		 * @return
		 */
		public Builder addJbpm(String jbpm) {
			if (managedInstance.jbpms == null) {
				managedInstance.jbpms = new ArrayList<String>();
			}
			managedInstance.jbpms.add(jbpm);
			return this;
		}
		
		/**
		 * fluent setter for frameCodes in the list
		 * 
		 * @param none
		 * @return
		 */
		public Builder addDrl(String drl) {
			if (managedInstance.drls == null) {
				managedInstance.drls = new ArrayList<String>();
			}
			managedInstance.drls.add(drl);
			return this;
		}

		
		public Builder addWorkItemHandler(WorkItemHandler workItemHandler) {
			if (managedInstance.workItemHandlers == null) {
				managedInstance.workItemHandlers = new ArrayList<Tuple2<String,WorkItemHandler>>();
			}
			String randomStr = UUID.randomUUID().toString();
			managedInstance.workItemHandlers.add(Tuple.of(randomStr,workItemHandler));
			return this;		
		}
		
		public Builder addWorkItemHandler(String title,WorkItemHandler workItemHandler) {
			if (managedInstance.workItemHandlers == null) {
				managedInstance.workItemHandlers = new ArrayList<Tuple2<String,WorkItemHandler>>();
			}
			managedInstance.workItemHandlers.add(Tuple.of(title,workItemHandler));
			return this;		
		}
		
		
		public Builder addToken(GennyToken token) {
			managedInstance.tokens.put(token.getCode(),token);
			return this;
		}
		
		public Builder addFact(String key,Object object)
		{
			managedInstance.cmds.add(CommandFactory.newInsert(object, key));
			return this;
		}
		
		public GennyKieSession build() {
			managedInstance.setup();
			return managedInstance;
		}

	}

}