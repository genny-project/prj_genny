package life.genny.test;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.drools.core.ClockType;
import org.drools.core.time.impl.PseudoClockScheduler;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.jbpm.test.JbpmJUnitBaseTestCase.Strategy;
import org.kie.api.KieServices;
import org.kie.api.command.Command;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
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
import life.genny.qwandautils.GennySettings;
import life.genny.rules.QRules;
import life.genny.rules.listeners.JbpmInitListener;

public class GennyKieSession extends JbpmJUnitBaseTestCase implements AutoCloseable {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

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
	private List<String> dtables;

	private ClockType clockType = ClockType.PSEUDO_CLOCK;

	private List<Tuple2<String, WorkItemHandler>> workItemHandlers;

	private Map<String, GennyToken> tokens = new HashMap<String, GennyToken>();

	PseudoClockScheduler sessionClock;

	List<Command<?>> cmds = new ArrayList<Command<?>>();

	/**
	 * static factory method for builder
	 */
	public static Builder builder() {
		return new GennyKieSession.Builder();
	}
	

	/**
	 * static factory method for builder
	 */
	public static Builder builder(boolean persistence) {
		return new GennyKieSession.Builder(persistence);
	}

	/**
	 * forces use of the Builder
	 */
	private GennyKieSession(boolean persistence) {
		super(persistence,persistence);
	}

	public ProcessInstance startProcess(String processId) {
		processInstance = kieSession.startProcess(processId);
		sessionClock = kieSession.getSessionClock();

		return processInstance;
	}
	
	public void start()
	{
		sessionClock = kieSession.getSessionClock();
		long startTime = System.nanoTime();
		ExecutionResults results = null;
		try {
			results = kieSession.execute(CommandFactory.newBatchExecution(cmds));
		} catch (Exception ee) {

		} finally {
			long endTime = System.nanoTime();
			double difference = (endTime - startTime) / 1e6; // get ms
			System.out.println("BPMN completed in " + difference + " ms");
		}
			
	}

	public long advanceSeconds(long amount) {
		return advanceSeconds(amount,false);
	}
	
	public void injectMessage(Object object) {
		kieSession.insert(object);
	}
	
	public void injectSignalToProcessInstance(String type, Object object, long processInstanceId) {
		kieSession.signalEvent(type, object, processInstanceId);
	}
	
	public void injectSignal(String type, Object object) {
		kieSession.signalEvent(type, object);
	}
	
	
	

	public long advanceSeconds(long amount,boolean humanTime) {
		long absoluteTime=0;
		for (int sec=0;sec<(amount*2);sec++) {
			absoluteTime = sessionClock.advanceTime(500, TimeUnit.MILLISECONDS); // half a sec should be ok
			if (humanTime) {
				sleepMS(500);
			}
			
		}
		return absoluteTime;
	}
	
	public long advanceMinutes(long amount) {
		return advanceMinutes(amount,false);
	}

	public long advanceMinutes(long amount,boolean humanTime) {
		return advanceSeconds(amount*60,humanTime);
	}
	
	public long advanceHours(long amount) {
		return advanceHours(amount,false);
	}

	public long advanceHours(long amount,boolean humanTime) {
		return advanceMinutes(amount*60,humanTime);
	}
	
	public long advanceDays(long amount) {
		return advanceDays(amount,false);
	}

	public long advanceDays(long amount,boolean humanTime) {
		return advanceHours(amount*24,humanTime);
	}

	protected void sleepMS(long ms) {
	    try {
				Thread.sleep(ms);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
	private void setup() {
		if (clockType.equals(ClockType.PSEUDO_CLOCK)) {
			System.setProperty("drools.clockType", "pseudo"/* clockType.name() */);
		} else if (clockType.equals(ClockType.REALTIME_CLOCK)) {
			System.setProperty("drools.clockType", "realtime");
		}
		// KieSessionConfiguration config =
		// KieServices.Factory.get().newKieSessionConfiguration();

		// config.setOption( ClockTypeOption.get("realtime") );

		if (jbpms != null) {
			for (String p : jbpms) {
				String fullJbpmPath = findFullPath(p);
				resources.put(fullJbpmPath, ResourceType.BPMN2);
			}
		}

		if (drls != null) {
			for (String p : drls) {
				String fullDrlPath = findFullPath(p);
				resources.put(fullDrlPath, ResourceType.DRL);
			}
		}

		if (dtables != null) {
			for (String p : dtables) {
				String fullDtablePath = findFullPath(p);
				resources.put(fullDtablePath, ResourceType.DTABLE);
			}
		}

		String[] coredrls = { DRL_PROJECT, DRL_USER_COMPANY, DRL_USER, DRL_EVENT_LISTENER_SERVICE_SETUP,
				DRL_EVENT_LISTENER_USER_SETUP };

		for (String p : coredrls) {
			resources.put(p, ResourceType.DRL);
		}

		String uniqueRuntimeStr = UUID.randomUUID().toString();
		
		createRuntimeManager(Strategy.SINGLETON, resources, uniqueRuntimeStr);
		kieSession = getRuntimeEngine().getKieSession();

		if (kieSession != null) {
			// Register handlers
			addWorkItemHandlers();
			if (tokens.containsKey("userToken")) {
				kieSession.addEventListener(new JbpmInitListener(tokens.get("userToken")));
			}
			// kieSession.setGlobal("log", log);
		} else {
			log.error("KieSession not initialised");
		}

	}

	private void addWorkItemHandlers() {
		kieSession.getWorkItemManager().registerWorkItemHandler("Awesome", new AwesomeHandler());
		kieSession.getWorkItemManager().registerWorkItemHandler("Notification", new NotificationWorkItemHandler());
		kieSession.getWorkItemManager().registerWorkItemHandler("ShowAllForms", new ShowAllFormsHandler());

		if (workItemHandlers != null) {
			for (Tuple2<String, WorkItemHandler> wih : workItemHandlers) {
				kieSession.getWorkItemManager().registerWorkItemHandler(wih._1, wih._2);
			}
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

	public void assertNodeTriggered(String nodeName) {
		assertNodeTriggered(this.getProcessInstance().getId(), nodeName);
	}

	public void assertProcessInstanceCompleted() {
		assertProcessInstanceCompleted(this.getProcessInstance().getId());
	}

	private String findFullPath(String filename) {
		String baseRulesDir = "./rules"; // default for project
		if (!"/rules".equals(GennySettings.rulesDir)) {
			baseRulesDir = GennySettings.rulesDir;
		}
		String testRulesDir = baseRulesDir;
		File base = new File(testRulesDir);
		File found = searchFile(new File(testRulesDir), filename);
		return found.getAbsoluteFile().getPath().substring(base.getAbsoluteFile().getPath().length() + 1);

	}

	private File searchFile(File file, String search) {
		if (file.isDirectory()) {
			File[] arr = file.listFiles();
			for (File f : arr) {
				File found = searchFile(f, search);
				if (found != null)
					return found;
			}
		} else {
			if (file.getName().equals(search)) {
				return file;
			}
		}
		return null;
	}

	public void close() {
		kieSession.dispose();
	}

	/**
	 * more fluent Builder
	 */
	public static class Builder {

		private GennyKieSession managedInstance = null;

		public Builder() {
			 managedInstance = new GennyKieSession(false);
		}

		public Builder(boolean persistence) {
			 managedInstance = new GennyKieSession(persistence);
		}


		/**
		 * fluent setter for jbpms in the list
		 * 
		 * @param none
		 * @return
		 */
		public Builder addJbpm(String... jbpms) {
			if (managedInstance.jbpms == null) {
				managedInstance.jbpms = new ArrayList<String>();
			}
			for (String jbpm : jbpms) {
				managedInstance.jbpms.add(jbpm);
			}
			return this;
		}
		
		
		/**
		 * fluent setter for drls in the list
		 * 
		 * @param none
		 * @return
		 */
		public Builder addDrl(String... drls) {
			if (managedInstance.drls == null) {
				managedInstance.drls = new ArrayList<String>();
			}
			for (String drl : drls) {
				managedInstance.drls.add(drl);
			}

			return this;
		}

		/**
		 * fluent setter for dtables in the list
		 * 
		 * @param none
		 * @return
		 */
		public Builder addDecisionTable(String... dtables) {
			if (managedInstance.dtables == null) {
				managedInstance.dtables = new ArrayList<String>();
			}
			for (String dtable : dtables) {
				managedInstance.dtables.add(dtable);
			}
			return this;
		}

		public Builder addWorkItemHandler(WorkItemHandler workItemHandler) {
			if (managedInstance.workItemHandlers == null) {
				managedInstance.workItemHandlers = new ArrayList<Tuple2<String, WorkItemHandler>>();
			}
			String randomStr = UUID.randomUUID().toString();
			managedInstance.workItemHandlers.add(Tuple.of(randomStr, workItemHandler));
			return this;
		}

		public Builder addWorkItemHandler(String title, WorkItemHandler workItemHandler) {
			if (managedInstance.workItemHandlers == null) {
				managedInstance.workItemHandlers = new ArrayList<Tuple2<String, WorkItemHandler>>();
			}
			managedInstance.workItemHandlers.add(Tuple.of(title, workItemHandler));
			return this;
		}

		public Builder clockType(ClockType clockType) {
			managedInstance.clockType = clockType;
			return this;
		}

		public Builder addToken(GennyToken token) {
			managedInstance.tokens.put(token.getCode(), token);
			return this;
		}

		public Builder addFact(String key, Object object) {
			managedInstance.cmds.add(CommandFactory.newInsert(object, key));
			return this;
		}

		public GennyKieSession build() {
			managedInstance.setup();
			return managedInstance;
		}

	}

}