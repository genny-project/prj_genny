package life.genny.test;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.drools.core.base.MapGlobalResolver;
import org.drools.core.impl.EnvironmentFactory;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import life.genny.qwandautils.GennySettings;

public class PersistentTests  extends JbpmJUnitBaseTestCase {
	   private static final Logger LOG = LoggerFactory.getLogger(PersistentTests.class);

		protected static EntityManagerFactory emf;
		protected static EntityManager em;

		protected static String realm = GennySettings.mainrealm;
		protected static Set<String> realms;


		@BeforeClass
		public static void init() throws FileNotFoundException, SQLException {
			System.out.println("Setting up EntityManagerFactory");
			try {
				emf = Persistence.createEntityManagerFactory("h2-pu");
				env = EnvironmentFactory.newEnvironment(); // KnowledgeBaseFactory.newEnvironment();
				env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
				// env.set(EnvironmentName.TRANSACTION_MANAGER,
				// TransactionManagerServices.getTransactionManager());
				env.set(EnvironmentName.GLOBALS, new MapGlobalResolver());
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (emf == null) {
				LOG.error("EMF is null");
			} else {
				System.out.println("Setting up EntityManager");
				em = emf.createEntityManager();
			}

			// Set up realm
			realms = new HashSet<String>();
			realms.add(realm);
			realms.stream().forEach(System.out::println);
			realms.remove("genny");

			// Enable the PseudoClock using the following system property.
			System.setProperty("drools.clockType", "pseudo");
		}

		static Environment env; // drools persistence

		public PersistentTests() {
			// configure this tests to not use persistence in this case

			// super(false);
		}
	//   @Test
	    public void test() {
	        PersistentTests.LOG.debug("jBPM unit test sample");

	        final RuntimeManager runtimeManager = createRuntimeManager("rulescurrent/shared/00_Startup/sample.bpmn");
	        final RuntimeEngine runtimeEngine = getRuntimeEngine(null);
	        final KieSession kieSession = runtimeEngine.getKieSession();

	        final ProcessInstance processInstance = kieSession.startProcess("rulescurrent.shared.00_Startup.sample");

	        assertProcessInstanceNotActive(processInstance.getId(), kieSession);
	        assertNodeTriggered(processInstance.getId(), "Hello");

	        runtimeManager.disposeRuntimeEngine(runtimeEngine);
	        runtimeManager.close();
	    }
	   
	  // @Test
	    public void timerTest() {
	        PersistentTests.LOG.debug("jBPM unit test Timer Teast");

	        final RuntimeManager runtimeManager = createRuntimeManager("rulescurrent/shared/00_Startup/Ttimer3.bpmn");
	        final RuntimeEngine runtimeEngine = getRuntimeEngine(null);
	        final KieSession kieSession = runtimeEngine.getKieSession();

	        final ProcessInstance processInstance = kieSession.startProcess("rulescurrent.shared.00_Startup.timer6");

	        assertProcessInstanceNotActive(processInstance.getId(), kieSession);
	    //    assertNodeTriggered(processInstance.getId(), "Hello");

	        runtimeManager.disposeRuntimeEngine(runtimeEngine);
	        runtimeManager.close();
	    }
}
