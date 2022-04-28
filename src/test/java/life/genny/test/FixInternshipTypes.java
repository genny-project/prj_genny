package life.genny.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.bind.JsonbBuilder;
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

import com.google.gson.JsonObject;
import com.thoughtworks.xstream.mapper.SystemAttributeAliasingMapper;

import io.vavr.Tuple2;
import life.genny.eventbus.EventBusInterface;
import life.genny.eventbus.EventBusMock;
import life.genny.eventbus.VertxCache;
import life.genny.models.GennyToken;
import life.genny.qwanda.Answer;
import life.genny.qwanda.Ask;
import life.genny.qwanda.EEntityStatus;
import life.genny.qwanda.Question;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeText;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.SearchEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.message.QCmdMessage;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwandautils.GennyCacheInterface;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.KeycloakUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.utils.BaseEntityUtils;
import life.genny.utils.DefUtils;
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
	public void fixInternshipCountTest() throws Exception {
		VertxUtils.cachedEnabled = false;

		if (beUtils == null) {
			return;
		}

		setUpDefs();

		SearchEntity searchBE = new SearchEntity("SBE_INTERNSHIPS", "Internship Search")
				.addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "BEG_%").addColumn("PRI_CODE", "Code");
		// .addAssociatedColumn("LNK_INTERN", "Intern Name", "LNK_COMP_INTERNSHIP")
		searchBE.setRealm(realm);

		Boolean ok = true;
		Integer index = 0;
		Integer fixedInterns = 0;
		Integer fixedApps = 0;
		searchBE.setPageStart(index);
		Integer pageSize = 100;
		searchBE.setPageSize(pageSize);
		Long total = beUtils.getCount(searchBE);

		Attribute lnkNoOfInternsAttribute = RulesUtils.getAttribute("LNK_NO_OF_INTERNS", serviceToken);
		Attribute noOfInternsAttribute = RulesUtils.getAttribute("PRI_NO_OF_INTERNS", serviceToken);

		BaseEntity defInternship = beUtils.getDEFByCode("DEF_INTERNSHIP");

		while (ok) {
			List<BaseEntity> internships = beUtils.getBaseEntitys(searchBE); // load 100 at a time
			if (internships.isEmpty() || (index > 5000)) {
				ok = false;
				break;
			}

			for (BaseEntity internship : internships) {
				index++;
				Optional<Integer> optNumOfInterns = internship.getValue("PRI_NO_OF_INTERNS");
				if (!optNumOfInterns.isPresent()) {
					Optional<String> selection = internship.getValue("LNK_NO_OF_INTERNS");
					
					if (selection.isPresent()) {
						String sel = selection.get();
						sel = sel.substring(2);
						sel = sel.substring(0, sel.length()-2);
						Integer noOfInterns = 0;
						switch (sel) {
						case "SEL_NO_OF_INTERNS_ONE":
							noOfInterns = 1;
							break;
						case "SEL_NO_OF_INTERNS_TWO":
							noOfInterns = 2;
							break;
						case "SEL_NO_OF_INTERNS_THREE":
							noOfInterns = 3;
							break;
						case "SEL_NO_OF_INTERNS_FOUR":
							noOfInterns = 4;
							break;
						case "SEL_NO_OF_INTERNS_FIVE":
							noOfInterns = 5;
							break;
						case "SEL_NO_OF_INTERNS_SIX":
							noOfInterns = 6;
							break;
						case "SEL_NO_OF_INTERNS_SEVEN":
							noOfInterns = 7;
							break;
						case "SEL_NO_OF_INTERNS_EIGHT":
							noOfInterns = 8;
							break;
						case "SEL_NO_OF_INTERNS_NINE":
							noOfInterns = 9;
							break;
						case "SEL_NO_OF_INTERNS_TEN":
							noOfInterns = 10;
							break;
						case "SEL_NO_OF_INTERNS_ELEVEN":
							noOfInterns = 11;
							break;
						case "SEL_NO_OF_INTERNS_TWELVE":
							noOfInterns = 12;
							break;
						case "SEL_NO_OF_INTERNS_13":
							noOfInterns = 13;
							break;
						case "SEL_NO_OF_INTERNS_14":
							noOfInterns = 14;
							break;
						case "SEL_NO_OF_INTERNS_15":
							noOfInterns = 15;
							break;
						case "SEL_NO_OF_INTERNS_16":
							noOfInterns = 16;
							break;
						case "SEL_NO_OF_INTERNS_17":
							noOfInterns = 17;
							break;
						case "SEL_NO_OF_INTERNS_18":
							noOfInterns = 18;
							break;
						case "SEL_NO_OF_INTERNS_19":
							noOfInterns = 19;
							break;
						case "SEL_NO_OF_INTERNS_20":
							noOfInterns = 20;
							break;
						default:
							System.out.println("ERROR - UNKNOWN SEL "+sel);
						}
						internship = beUtils.saveAnswer(new Answer(internship, internship, noOfInternsAttribute, ""+noOfInterns));
						
						System.out.println(index + " of " + total + " Internship -> " + internship.getCode() + " "
								+ internship.getName() + " SET TO NoInterns "+noOfInterns);
					}

				} else {
					System.out.println(index + " of " + total + " Internship -> " + internship.getCode() + " "
							+ internship.getName() + "");
				}
			}

			searchBE.setPageStart(index);
		}

	}

	@Test
	public void setAllInternshipsActiveIfPending() throws Exception {
		VertxUtils.cachedEnabled = false;

		if (beUtils == null) {
			return;
		}

		setUpDefs();

		SearchEntity searchBE = new SearchEntity("SBE_INTERNSHIPS", "Internship Search")
				.addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "BEG_%").addColumn("PRI_CODE", "Code");
		// .addAssociatedColumn("LNK_INTERN", "Intern Name", "LNK_COMP_INTERNSHIP")
		searchBE.setRealm(realm);

		Boolean ok = true;
		Integer index = 0;
		Integer fixedInterns = 0;
		Integer fixedApps = 0;
		searchBE.setPageStart(index);
		Integer pageSize = 100;
		searchBE.setPageSize(pageSize);
		Long total = beUtils.getCount(searchBE);

		Attribute titleAttribute = RulesUtils.getAttribute("PRI_INTERNSHIP_TITLE", serviceToken);
		Attribute hostCompanyAttribute = RulesUtils.getAttribute("LNK_HOST_COMPANY", serviceToken);

		BaseEntity defInternship = beUtils.getDEFByCode("DEF_INTERNSHIP");

		while (ok) {
			List<BaseEntity> internships = beUtils.getBaseEntitys(searchBE); // load 100 at a time
			if (internships.isEmpty() || (index > 5000)) {
				ok = false;
				break;
			}

			for (BaseEntity internship : internships) {
				index++;

				if (internship.getStatus().equals(EEntityStatus.PENDING)) {
					internship.setStatus(EEntityStatus.ACTIVE);
					beUtils.saveBaseEntity(defInternship, internship);
					System.out.println(index + " of " + total + " Internship -> " + internship.getCode() + " "
							+ internship.getName() + " SET TO ACTIVE FROm PENDING");

				} else {
					System.out.println(index + " of " + total + " Internship -> " + internship.getCode() + " "
							+ internship.getName() + "");
				}
			}

			searchBE.setPageStart(index);
		}

	}

	@Test
	public void archiveIncompleteInternshipsText() throws Exception {
		VertxUtils.cachedEnabled = false;

		if (beUtils == null) {
			return;
		}

		setUpDefs();

		SearchEntity searchBE = new SearchEntity("SBE_INTERNSHIPS", "Internship Search")
				.addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "BEG_%").addColumn("PRI_CODE", "Code");
		// .addAssociatedColumn("LNK_INTERN", "Intern Name", "LNK_COMP_INTERNSHIP")
		searchBE.setRealm(realm);

		Boolean ok = true;
		Integer index = 0;
		Integer fixedInterns = 0;
		Integer fixedApps = 0;
		searchBE.setPageStart(index);
		Integer pageSize = 100;
		searchBE.setPageSize(pageSize);
		Long total = beUtils.getCount(searchBE);

		Attribute titleAttribute = RulesUtils.getAttribute("PRI_INTERNSHIP_TITLE", serviceToken);
		Attribute hostCompanyAttribute = RulesUtils.getAttribute("LNK_HOST_COMPANY", serviceToken);

		BaseEntity defInternship = beUtils.getDEFByCode("DEF_INTERNSHIP");

		while (ok) {
			List<BaseEntity> internships = beUtils.getBaseEntitys(searchBE); // load 100 at a time
			if (internships.isEmpty() || (index > 5000)) {
				ok = false;
				break;
			}

			for (BaseEntity internship : internships) {
				index++;

				/*
				 * if ("BEG_E14E9BFF-7BF5-4AAC-99AE-6CE98E7C85CA".equals(internship.getCode()))
				 */ {

					String title = internship.getValue("PRI_INTERNSHIP_TITLE", null);
					String name = internship.getValue("PRI_NAME", null);

					String lnkHostCompany = internship.getValue("LNK_HOST_COMPANY", null);
					if (StringUtils.isBlank(name) || (name.length() < 2)) {
						internship.setStatus(EEntityStatus.ARCHIVED);
						beUtils.saveBaseEntity(defInternship, internship);
						System.out.println(index + " of " + total + " Internship -> " + internship.getCode() + " "
								+ internship.getName() + " -> archived no name");
					} else {

						if (StringUtils.isBlank(lnkHostCompany) || ("[]".equals(lnkHostCompany))) {
							internship.setStatus(EEntityStatus.ARCHIVED);
							beUtils.saveBaseEntity(defInternship, internship);
							System.out.println(index + " of " + total + " Internship -> " + internship.getCode() + " "
									+ internship.getName() + " -> archived no host company");

						} else {

							System.out.println(index + " of " + total + " Internship -> " + internship.getCode() + " "
									+ internship.getName() + "");
						}
					}

				}
//				else {
//					System.out.println(index + " of " + total + " Internship -> " + internship.getCode() + " "
//							+ internship.getName() + "");
//				}
			}

			searchBE.setPageStart(index);
		}

	}

	@Test
	public void fixupThingsText() throws Exception {
		VertxUtils.cachedEnabled = false;

		if (beUtils == null) {
			return;
		}

		setUpDefs();

		SearchEntity searchBE = new SearchEntity("SBE_INTERNS", "Intern Search")
				.addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "PER_%").addFilter("PRI_IS_INTERN", true)
				.addColumn("PRI_CODE", "Code");
		// .addAssociatedColumn("LNK_INTERN", "Intern Name", "LNK_COMP_INTERNSHIP")
		searchBE.setRealm(realm);

		Boolean ok = true;
		Integer index = 0;
		Integer fixedInterns = 0;
		Integer fixedApps = 0;
		searchBE.setPageStart(index);
		Integer pageSize = 100;
		searchBE.setPageSize(pageSize);
		Long total = beUtils.getCount(searchBE);

		Attribute statusAttribute = RulesUtils.getAttribute("PRI_STATUS", serviceToken);
		Attribute stageAttribute = RulesUtils.getAttribute("PRI_STAGE", serviceToken);
		Attribute deletedAttribute = RulesUtils.getAttribute("PRI_DISABLED", serviceToken);
		Attribute lnkInternAttribute = RulesUtils.getAttribute("LNK_INTERN", serviceToken);
		Attribute nameAttribute = RulesUtils.getAttribute("PRI_NAME", serviceToken);
		Attribute internCodeAttribute = RulesUtils.getAttribute("PRI_INTERN_CODE", serviceToken);
		Attribute applicantCodeAttribute = RulesUtils.getAttribute("PRI_APPLICANT_CODE", serviceToken);

		BaseEntity defIntern = beUtils.getDEFByCode("DEF_INTERN");

		while (ok) {
			List<BaseEntity> interns = beUtils.getBaseEntitys(searchBE); // load 100 at a time
			if (interns.isEmpty() || (index > 5000)) {
				ok = false;
				break;
			}

			for (BaseEntity intern : interns) {
				index++;

				// intern =
				// beUtils.getBaseEntityByCode("PER_D620320A-EF19-451E-8E45-0DCBD8F3232D"); //

				Set<BaseEntity> applications = getApplications(intern);

				// ok, we should have all the apps now..
				if (index == 6) {
					System.out.println("here");
				}
				System.out.println(index + " of " + total + " Intern -> " + intern.getCode() + " " + intern.getName()
						+ " " + (applications.isEmpty() ? "No APPS" : ("" + applications.size() + " APPS")));
				if (applications.isEmpty()) {

					String email = intern.getValue("PRI_EMAIL", null);
					String firstname = intern.getValue("PRI_FIRSTNAME", null);
					if (((!StringUtils.isBlank(email)) && (!StringUtils.isBlank(firstname)))) { // must have both

						intern = beUtils.saveAnswer(new Answer(intern, intern, statusAttribute, "AVAILABLE"));
						intern = beUtils.saveAnswer(new Answer(intern, intern, deletedAttribute, "FALSE"));
					} else {
						if (!intern.getStatus().equals(EEntityStatus.PENDING)) {
							intern = beUtils.saveAnswer(new Answer(intern, intern, statusAttribute, "DODGY"));
							intern = beUtils.saveAnswer(new Answer(intern, intern, deletedAttribute, "TRUE"));
							intern.setStatus(EEntityStatus.DELETED);
							beUtils.saveBaseEntity(defIntern, intern);
							System.out.println(index + " of " + total + " Intern -> DELETED " + intern.getCode()
									+ " firstname " + firstname + " email " + email);
						}
					}
					continue;
				}

				// Now check for the most progressed one
				BaseEntity mostAdvancedApp = null;
				Integer maxScore = -2;
				Map<String, Integer> statusValueMap = new HashMap<>();
				statusValueMap.put("INACTIVE", -2);
				statusValueMap.put("REJECT", -1);
				statusValueMap.put("WITHDRAWN", 0);

				statusValueMap.put("APPLIED", 1);
				statusValueMap.put("SHORTLISTED", 2);
				statusValueMap.put("INTERVIEWED", 3);
				statusValueMap.put("OFFERED", 4);
				statusValueMap.put("PLACED", 5);
				statusValueMap.put("PROGRESS", 6);
				statusValueMap.put("COMPLETED", 7);

				for (BaseEntity app : applications) {
					String stage = app.getValue("PRI_STAGE", null);
					if (stage == null) {
						stage = "APPLIED";
						app = beUtils.saveAnswer(new Answer(app, app, stageAttribute, stage));
					}

					String status = app.getValue("PRI_STATUS", null);
					if (status == null) {
						status = "REJECT";
						app = beUtils.saveAnswer(new Answer(app, app, statusAttribute, status));
					}
					if (status != null) {
						if (mostAdvancedApp == null) {
							if ("REJECTED".equals(status)) {
								status = "REJECT"; // fix
								app = beUtils.saveAnswer(new Answer(app, app, statusAttribute, "REJECT"));
							}
							mostAdvancedApp = app;

							maxScore = statusValueMap.get(status);
							continue;
						}
						Integer appScore = -1;

						try {
							if ("REJECTED".equals(status)) {
								status = "REJECT"; // fix
								app = beUtils.saveAnswer(new Answer(app, app, statusAttribute, "REJECT"));
							}
							appScore = statusValueMap.get(status);
							if (appScore == null) {
								System.out.println(index + " of " + total + " appScore is null - status=" + status);
							}
						} catch (Exception e) {
							System.out.println("NULL STATUS!");
						}

						if (appScore > maxScore) {
							maxScore = appScore;
							mostAdvancedApp = app;
						}

					} else {

					}
					// Force the name for each App
					app = beUtils.saveAnswer(new Answer(app, app, nameAttribute, intern.getName()));
					app = beUtils
							.saveAnswer(new Answer(app, app, lnkInternAttribute, "[\"" + intern.getCode() + "\"]"));
					app = beUtils.saveAnswer(new Answer(app, app, internCodeAttribute, intern.getCode()));
					app = beUtils.saveAnswer(new Answer(app, app, applicantCodeAttribute, intern.getCode()));
				}

				// Fix up WITHDRAWN statuses

				// Now if mostadvancedApp is in progress then withdraw the others
				if ("PROGRESS".equalsIgnoreCase(mostAdvancedApp.getValueAsString("PRI_STAGE"))
						|| "PLACED".equalsIgnoreCase(mostAdvancedApp.getValueAsString("PRI_STAGE"))
						|| "COMPLETED".equalsIgnoreCase(mostAdvancedApp.getValueAsString("PRI_STAGE"))
						|| "ARCHIVED".equalsIgnoreCase(mostAdvancedApp.getValueAsString("PRI_STAGE"))) {
					// Now withdraw all the existing apps
					// Now we do not want to lose the maximum stage reached for each application
					// and we want to be able to show buckets with 'withdrawn' shown.
					// So we essentially want to 'disable the buckets and not overwrite their normal
					// status with a WITHDRAWN status

					// set the intern stage to reflect the latest
					String maxstage = mostAdvancedApp.getValueAsString("PRI_STAGE");
					if (("REJECT".equalsIgnoreCase(maxstage)) || ("WITHDRAWN".equalsIgnoreCase(maxstage))) {
						maxstage = "AVAILABLE";
					}
					intern = beUtils.saveAnswer(new Answer(intern, intern, statusAttribute, maxstage)); // THIS IS
																										// ANNOYING
					intern = beUtils.saveAnswer(new Answer(intern, intern, stageAttribute, maxstage));
					intern = beUtils.saveAnswer(new Answer(intern, intern, deletedAttribute, "TRUE"));
					// Set all applications to WITHDRAWN

					for (BaseEntity app : applications) {
						String stage = app.getValue("PRI_STAGE", null);
						if (stage != null) {
							if (!mostAdvancedApp.getValueAsString("PRI_STAGE").equalsIgnoreCase(stage)) {
								if (!"REJECT".equalsIgnoreCase(app.getValueAsString("PRI_STATUS"))) {
									app = beUtils.saveAnswer(new Answer(app, app, statusAttribute, "WITHDRAWN"));
								}
							}
						}
					}
				}

				System.out.println(index + " of " + total + " Intern -> " + intern.getCode() + " " + intern.getName()
						+ " -> fixed up " + mostAdvancedApp.getValueAsString("PRI_STAGE") + " and status= "
						+ mostAdvancedApp.getValueAsString("PRI_STATUS"));

			}

			searchBE.setPageStart(index);
		}

	}

	/**
	 * @param intern
	 * @param applications
	 */
	private Set<BaseEntity> getApplications(BaseEntity intern) {
		Set<BaseEntity> applications = new HashSet<>();
		// ok, now get the intern's applications
		// First look for LNK_INTERN
		SearchEntity searchAppsBE = new SearchEntity("SBE_INTERN_APPS", "Intern Apps Search")
				.addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "APP_%")
				.addFilter("LNK_INTERN", SearchEntity.StringFilter.LIKE, intern.getCode())
				.addColumn("PRI_CODE", "Code");
		// .addAssociatedColumn("LNK_INTERN", "Intern Name", "LNK_COMP_INTERNSHIP")
		searchAppsBE.setRealm(realm);
		List<BaseEntity> apps = beUtils.getBaseEntitys(searchAppsBE); // load 100 at a time

		applications.addAll(apps);

		// Now look for PRI_INTERN_CODE
		searchAppsBE = new SearchEntity("SBE_INTERN_APPS", "Intern Apps Search")
				.addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "APP_%")
				.addFilter("PRI_INTERN_CODE", SearchEntity.StringFilter.LIKE, intern.getCode())
				.addColumn("PRI_CODE", "Code");
		// .addAssociatedColumn("LNK_INTERN", "Intern Name", "LNK_COMP_INTERNSHIP")
		searchAppsBE.setRealm(realm);
		apps = beUtils.getBaseEntitys(searchAppsBE); // load 100 at a time

		applications.addAll(apps);

		// Now look for PRI_APPLICANT_CODE
		searchAppsBE = new SearchEntity("SBE_INTERN_APPS", "Intern Apps Search")
				.addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "APP_%")
				.addFilter("PRI_APPLICANT_CODE", SearchEntity.StringFilter.LIKE, intern.getCode())
				.addColumn("PRI_CODE", "Code");
		// .addAssociatedColumn("LNK_INTERN", "Intern Name", "LNK_COMP_INTERNSHIP")
		searchAppsBE.setRealm(realm);
		apps = beUtils.getBaseEntitys(searchAppsBE); // load 100 at a time

		applications.addAll(apps);
		return applications;
	}

	@Test
	public void createPCMs() {
		VertxUtils.cachedEnabled = false;

		if (beUtils == null) {
			return;
		}

		try {
			// DefUtils.loadDEFS(realm,serviceToken);
			moreCreatePCMs();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void moreCreatePCMs() throws Exception {

		BaseEntity defBeIntern = beUtils.getDEFByCode("DEF_INTERN");
		BaseEntity intern = beUtils.create(defBeIntern, "Domenic");

		BaseEntity defBe = beUtils.getDEFByCode("DEF_PCM");
		Attribute attTpl = RulesUtils.getAttribute("PRI_TEMPLATE_CODE", serviceToken);

		Attribute attLoc1 = RulesUtils.getAttribute("PRI_LOC1", serviceToken);
		Attribute attLoc2 = RulesUtils.getAttribute("PRI_LOC2", serviceToken);

		BaseEntity pcmTest1 = beUtils.create(defBe, "PCM Test1", "PCM_TEST1");

		beUtils.saveAnswer(new Answer(pcmTest1, pcmTest1, attTpl, "TPL_CARD1"));
		beUtils.saveAnswer(new Answer(pcmTest1, pcmTest1, attLoc1, "PRI_FIRSTNAME"));
		beUtils.saveAnswer(new Answer(pcmTest1, pcmTest1, attLoc2, "PRI_LASTNAME"));
//	beUtils.saveBaseEntity(defBe,pcmTest1);

		BaseEntity pcmTest2 = beUtils.create(defBe, "PCM Test2", "PCM_TEST2");

		beUtils.saveAnswer(new Answer(pcmTest2, pcmTest2, attTpl, "TPL_CARD1"));
		beUtils.saveAnswer(new Answer(pcmTest2, pcmTest2, attLoc1, "PRI_LASTNAME"));
		beUtils.saveAnswer(new Answer(pcmTest2, pcmTest2, attLoc2, "PRI_FIRSTNAME"));

//	beUtils.saveBaseEntity(defBe,pcmTest2);

		SearchEntity searchBE = new SearchEntity("SBE_PCMS", "PCM Search")
				.addSort("PRI_CREATED", "Created", SearchEntity.Sort.ASC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "PCM_%").addColumn("PRI_CODE", "Code")
				.addColumn("PRI_STATUS", "Status");
		searchBE.setRealm(userToken.getRealm());

		List<BaseEntity> pcms = beUtils.getBaseEntitys(searchBE);

		/* Now send to frontend user client */

		QDataBaseEntityMessage beMsg = new QDataBaseEntityMessage(pcms);
		beMsg.setToken(userToken.getToken());
		beMsg.setReplace(true);
		VertxUtils.writeMsg("webcmds", JsonUtils.toJson(beMsg));

		QCmdMessage msg = new QCmdMessage("DISPLAY", "FORM");
		msg.setToken(beUtils.getGennyToken().getToken());
		VertxUtils.writeMsg("webcmds", msg);

		/* We generate the question */
		Attribute attr = RulesUtils.getAttribute("QQQ_QUESTION_GROUP", beUtils.getGennyToken().getToken());
		Attribute firstnameAttr = RulesUtils.getAttribute("PRI_FIRSTNAME", beUtils.getGennyToken().getToken());
		Attribute lastnameAttr = RulesUtils.getAttribute("PRI_LASTNAME", beUtils.getGennyToken().getToken());
		Question groupQuestion = new Question("QUE_TEST_PCM_GRP", "Adam\'s Test Questions", attr, false);
//	        Use TaskUtils.getQuestion(questionCode, userToken);
		Question childQuestion = new Question("QUE_PCM_TEST_FIRSTNAME", "TEST PCM", firstnameAttr, false);
		groupQuestion.addTarget(childQuestion, 1.0);
		Question childQuestion2 = new Question("QUE_PCM_TEST2_LASTNAME", "TEST PCM2", lastnameAttr, false);
		groupQuestion.addTarget(childQuestion2, 2.0);

		/* We generate the ask */
		Ask ask = new Ask(groupQuestion, userToken.getCode(), intern.getCode(), false, 1.0, false, false, false);
		Ask childAsk = new Ask(childQuestion, userToken.getCode(), intern.getCode(), false, 1.0, false, false, false);
		List<Ask> childAsksArray = new ArrayList<>();
		childAsksArray.add(childAsk);
		ask.setChildAsks(childAsksArray.toArray(new Ask[0]));
		List<Ask> asksArray = new ArrayList<>();
		asksArray.add(ask);

		QDataBaseEntityMessage beMsg2 = new QDataBaseEntityMessage(intern);
		beMsg2.setToken(beUtils.getGennyToken().getToken());
		VertxUtils.writeMsg("webcmds", beMsg2);

		QDataAskMessage askMsg = new QDataAskMessage(ask);
		askMsg.setToken(beUtils.getGennyToken().getToken());
		askMsg.setAttributeCode("QQQ_QUESTION_GROUP");
		askMsg.setQuestionCode("QUE_TEST_PCM_GRP");
		askMsg.setSourceCode(userToken.getCode());
		askMsg.setTargetCode(intern.getCode());
		askMsg.setItems(asksArray.toArray(new Ask[0]));
		VertxUtils.writeMsg("webcmds", askMsg);
		VertxUtils.writeMsgEnd(beUtils.getGennyToken());

	}

	// @Test
	public void createPriStageTest() throws Exception {
		VertxUtils.cachedEnabled = false;

		if (beUtils == null) {
			return;
		}

		setUpDefs();

		SearchEntity searchBE = new SearchEntity("SBE_APPS", "APP Search")
				.addSort("PRI_CREATED", "Created", SearchEntity.Sort.ASC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "APP_%").addColumn("PRI_CODE", "Code")
				.addColumn("PRI_STATUS", "Status");
		searchBE.setRealm(realm);

		Boolean ok = true;
		Integer index = 0;
		Integer fixedInterns = 0;
		Integer fixedApps = 0;
		searchBE.setPageStart(index);
		Integer pageSize = 100;
		searchBE.setPageSize(pageSize);
		Long total = beUtils.getCount(searchBE);

		Attribute nameAttribute = RulesUtils.getAttribute("PRI_NAME", serviceToken);
		Attribute statusAttribute = RulesUtils.getAttribute("PRI_STATUS", serviceToken);
		Attribute stageAttribute = RulesUtils.getAttribute("PRI_STAGE", serviceToken);

		Integer stage = 0;
		Integer status = 0;

		while (ok) {
			List<BaseEntity> bes = beUtils.getBaseEntitys(searchBE); // load 100 at a time
			if (bes.isEmpty() || (index > 5000)) {
				ok = false;
				break;
			}

			for (BaseEntity be : bes) {

				// ok, now get the intern's application type

				String statusStr = be.getValueAsString("PRI_STATUS");
				String stageStr = be.getValueAsString("PRI_STAGE");
				if (!StringUtils.isBlank(statusStr)) {
					switch (statusStr) {
					case "AVAILABLE":
					case "APPLIED":
					case "SHORTLISTED":
					case "INTERVIEWED":
					case "OFFERED":
					case "PLACED":
					case "PROGRESS":
					case "COMPLETED":
						stageStr = statusStr;
						statusStr = "ACTIVE";
						be = beUtils.saveAnswer(new Answer(be, be, stageAttribute, stageStr));
						be = beUtils.saveAnswer(new Answer(be, be, statusAttribute, statusStr));
						stage++;
						break;
					default:
						stageStr = "TBD";
						status++;
						be = beUtils.saveAnswer(new Answer(be, be, stageAttribute, stageStr));
					}

				}

				System.out.println(index + " of " + total + " stage:" + stage + " status:" + status + " :BE: "
						+ be.getCode() + ":" + be.getName() + " --> " + statusStr + " " + stageStr);

				index++;
			}

			searchBE.setPageStart(index);
		}

	}

	@Test
	public void fixInternshipTitleTest() throws Exception {

		if (beUtils == null) {
			return;
		}

		SearchEntity searchBE = new SearchEntity("SBE_BES", "InternshipTitle Search")
				.addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "BEG_%").addColumn("PRI_CODE", "Code");
		// .addAssociatedColumn("LNK_INTERN", "Intern Name", "LNK_COMP_INTERNSHIP")
		searchBE.setRealm(realm);

		Boolean ok = true;
		Integer index = 0;
		Integer fixedInterns = 0;
		Integer fixedApps = 0;
		searchBE.setPageStart(index);
		Integer pageSize = 100;
		searchBE.setPageSize(pageSize);
		Long total = beUtils.getCount(searchBE);

		Attribute statusAttribute = RulesUtils.getAttribute("PRI_STATUS", serviceToken);
		Attribute deletedAttribute = RulesUtils.getAttribute("PRI_DISABLED", serviceToken);
		Attribute lnkInternAttribute = RulesUtils.getAttribute("LNK_INTERN", serviceToken);
		Attribute internshipTitleAttribute = RulesUtils.getAttribute("PRI_INTERNSHIP_TITLE", serviceToken);
		Attribute titleAttribute = RulesUtils.getAttribute("PRI_TITLE", serviceToken);
		Attribute nameAttribute = RulesUtils.getAttribute("PRI_NAME", serviceToken);
		Attribute internCodeAttribute = RulesUtils.getAttribute("PRI_INTERN_CODE", serviceToken);
		Attribute applicantCodeAttribute = RulesUtils.getAttribute("PRI_APPLICANT_CODE", serviceToken);

		BaseEntity defInternship = beUtils.getDEFByCode("DEF_INTERNSHIP");

		while (ok) {
			List<BaseEntity> items = beUtils.getBaseEntitys(searchBE); // load 100 at a time
			if (items.isEmpty() || (index > 5000)) {
				ok = false;
				break;
			}

			for (BaseEntity item : items) {
				index++;

				// check if PRI_INTERNSHIP_TITLE is there
				String title = item.getValue("PRI_INTERNSHIP_TITLE", null);
				String name = item.getValue("PRI_NAME", null);
				if (StringUtils.isBlank(name)) {
					name = item.getName();
					item = beUtils.saveAnswer(new Answer(item, item, nameAttribute, name));
				}
				if ((StringUtils.isBlank(title))) {
					item = beUtils.saveAnswer(new Answer(item, item, internshipTitleAttribute, name));
					item = beUtils.saveAnswer(new Answer(item, item, titleAttribute, name));
					System.out.println("FIXED *************** NAME IS BEG but title is " + title);

				}

				System.out.println(index + " of " + total + " BEs -> " + item.getCode() + " " + item.getName());

			}

			searchBE.setPageStart(index);
		}

	}

	// @Test
	public void removeTestsTest() throws Exception {
		VertxUtils.cachedEnabled = false;

		if (beUtils == null) {
			return;
		}

		setUpDefs();

		SearchEntity searchBE = new SearchEntity("SBE_INTERNS", "Intern Search")
				.addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "PER_%").addFilter("PRI_IS_INTERN", true)
				.addColumn("PRI_CODE", "Code");
		// .addAssociatedColumn("LNK_INTERN", "Intern Name", "LNK_COMP_INTERNSHIP")
		searchBE.setRealm(realm);

		Boolean ok = true;
		Integer index = 0;
		Integer fixedInterns = 0;
		Integer fixedApps = 0;
		searchBE.setPageStart(index);
		Integer pageSize = 100;
		searchBE.setPageSize(pageSize);
		Long total = beUtils.getCount(searchBE);

		Attribute statusAttribute = RulesUtils.getAttribute("PRI_STATUS", serviceToken);
		Attribute deletedAttribute = RulesUtils.getAttribute("PRI_DISABLED", serviceToken);
		Attribute lnkInternAttribute = RulesUtils.getAttribute("LNK_INTERN", serviceToken);
		Attribute nameAttribute = RulesUtils.getAttribute("PRI_NAME", serviceToken);
		Attribute internCodeAttribute = RulesUtils.getAttribute("PRI_INTERN_CODE", serviceToken);
		Attribute applicantCodeAttribute = RulesUtils.getAttribute("PRI_APPLICANT_CODE", serviceToken);

		BaseEntity defIntern = beUtils.getDEFByCode("DEF_INTERN");

		while (ok) {
			List<BaseEntity> interns = beUtils.getBaseEntitys(searchBE); // load 100 at a time
			if (interns.isEmpty() || (index > 5000)) {
				ok = false;
				break;
			}

			for (BaseEntity intern : interns) {
				index++;

				Boolean remove = false;

				String name = intern.getValueAsString("PRI_NAME");
				if (!StringUtils.isBlank(name)) {
					if (name.contains("test")) {
						remove = true;
					}
					if (name.contains("Test")) {
						remove = true;
					}
					if (name.contains("Fake ")) {
						remove = true;
					}
					if (name.contains("Console")) {
						remove = true;
					}
					if (name.contains("Chris Pyke")) {
						remove = true;
					}
				}

				String email = intern.getValueAsString("PRI_EMAIL");
				if (!StringUtils.isBlank(email)) {
					if (email.contains("adamcrow63")) {
						remove = true;
					}
					if (email.contains("christopher.pyke")) {
						remove = true;
					}

					if (email.contains("console")) {
						remove = true;
					}
					if (email.contains("test.com")) {
						remove = true;
					}
					if (email.contains("testing.com")) {
						remove = true;
					}
					if (email.contains("byron.co")) {
						remove = true;
					}
				}

				if ("Lindsey Shema".equalsIgnoreCase(name)) {
					System.out.println("test");
				}
				if (email != null) {
					if (email.equalsIgnoreCase("wijesinghe345@gmail.com")) {
						remove = false;
					}
				}
				if (remove) {
					System.out.println(
							index + " of " + total + " " + name + ":" + email + " REMOVED @@@@@@@@@@@@@@@@@@@@@@@@@@@");
					intern.setStatus(EEntityStatus.DELETED);
					beUtils.saveBaseEntity(defIntern, intern);
				} else {
					System.out.println(index + " of " + total + " " + name + ":" + email);
				}
			}

			searchBE.setPageStart(index);
		}

	}

//@Test
	public void signaturesFixTest() throws Exception {
		VertxUtils.cachedEnabled = false;

		if (beUtils == null) {
			return;
		}

		setUpDefs();

		SearchEntity searchBE = new SearchEntity("SBE_APPS", "APP Search")
				.addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "APP_%")
				.addFilter("PRI_AGR_DOC_INT_SIGNATURE", SearchEntity.StringFilter.LIKE, "https://bit.ly/3A2xdjI")
				.addOr("PRI_AGR_DOC_HC_SIGNATURE", SearchEntity.StringFilter.LIKE, "https://bit.ly/3A2xdjI")
				.addOr("PRI_AGR_DOC_OUTCOME_SIGNATURE", SearchEntity.StringFilter.LIKE, "https://bit.ly/3A2xdjI")
				.addColumn("PRI_CODE", "Code").addColumn("LNK_INTERN", "Link Intern")
				.addColumn("PRI_INTERN_CODE", "Code Intern")
				.addColumn("PRI_JOURNAL_LEARNING_OUTCOMES", "LearningOutcomes")
				// .addAssociatedColumn("LNK_INTERN", "Intern Name", "LNK_COMP_INTERNSHIP")
				.addColumn("PRI_STATUS", "Status");
		searchBE.setRealm(realm);

		Boolean ok = true;
		Integer index = 0;
		Integer fixedInterns = 0;
		Integer fixedApps = 0;
		searchBE.setPageStart(index);
		Integer pageSize = 100;
		searchBE.setPageSize(pageSize);
		Long total = beUtils.getCount(searchBE);

		Attribute nameAttribute = RulesUtils.getAttribute("PRI_NAME", serviceToken);
		Attribute lnkInternAttribute = RulesUtils.getAttribute("LNK_INTERN", serviceToken);
		Attribute lnkIntSigAttribute = RulesUtils.getAttribute("PRI_AGR_DOC_INT_SIGNATURE", serviceToken);
		Attribute lnkHcrSigAttribute = RulesUtils.getAttribute("PRI_AGR_DOC_HC_SIGNATURE", serviceToken);
		Attribute lnkOutcomeSigAttribute = RulesUtils.getAttribute("PRI_AGR_DOC_OUTCOME_SIGNATURE",
				serviceToken.getToken());

		while (ok) {
			List<BaseEntity> bes = beUtils.getBaseEntitys(searchBE); // load 100 at a time
			if (bes.isEmpty() || (index > 5000)) {
				ok = false;
				break;
			}

			for (BaseEntity be : bes) {
				String fixedApp = "";
				String fixedIntern = "";

				// ok, now get the intern's application type

				String hcrSigStr = be.getValueAsString("PRI_AGR_DOC_HC_SIGNATURE");
				if (!StringUtils.isBlank(hcrSigStr)) {
					System.out.println("HCR Signature = " + hcrSigStr);
				}

				System.out.println(index + " of " + total + " :BE: " + be.getCode() + ":" + be.getName() + " --> "
						+ fixedIntern + "   " + fixedApp + " " + be.getName());

				index++;
			}

			searchBE.setPageStart(index);
		}

	}

	@Test
	public void appProgressFixTest() throws Exception {
		VertxUtils.cachedEnabled = false;

		if (beUtils == null) {
			return;
		}

		setUpDefs();

		SearchEntity searchBE = new SearchEntity("SBE_INTERNS", "Intern Search")
				.addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "PER_%").addFilter("PRI_IS_INTERN", true)
				.addColumn("PRI_CODE", "Code");
		// .addAssociatedColumn("LNK_INTERN", "Intern Name", "LNK_COMP_INTERNSHIP")
		searchBE.setRealm(realm);

		Boolean ok = true;
		Integer index = 0;
		Integer fixedInterns = 0;
		Integer fixedApps = 0;
		searchBE.setPageStart(index);
		Integer pageSize = 100;
		searchBE.setPageSize(pageSize);
		Long total = beUtils.getCount(searchBE);

		Attribute statusAttribute = RulesUtils.getAttribute("PRI_STATUS", serviceToken);
		Attribute deletedAttribute = RulesUtils.getAttribute("PRI_DISABLED", serviceToken);
		Attribute lnkInternAttribute = RulesUtils.getAttribute("LNK_INTERN", serviceToken);
		Attribute nameAttribute = RulesUtils.getAttribute("PRI_NAME", serviceToken);
		Attribute internCodeAttribute = RulesUtils.getAttribute("PRI_INTERN_CODE", serviceToken);
		Attribute applicantCodeAttribute = RulesUtils.getAttribute("PRI_APPLICANT_CODE", serviceToken);

		BaseEntity defIntern = beUtils.getDEFByCode("DEF_INTERN");

		while (ok) {
			List<BaseEntity> interns = beUtils.getBaseEntitys(searchBE); // load 100 at a time
			if (interns.isEmpty() || (index > 5000)) {
				ok = false;
				break;
			}

			for (BaseEntity intern : interns) {
				index++;

				// intern =
				// beUtils.getBaseEntityByCode("PER_D620320A-EF19-451E-8E45-0DCBD8F3232D"); //

				Set<BaseEntity> applications = new HashSet<>();

				// getApplications(intern, applications);

				// ok, we should have all the apps now..
				if (index == 6) {
					System.out.println("here");
				}
				System.out.println(index + " of " + total + " Intern -> " + intern.getCode() + " " + intern.getName()
						+ " " + (applications.isEmpty() ? "No APPS" : ("" + applications.size() + " APPS")));
				if (applications.isEmpty()) {

					String email = intern.getValue("PRI_EMAIL", null);
					String firstname = intern.getValue("PRI_FIRSTNAME", null);
					if (((!StringUtils.isBlank(email)) && (!StringUtils.isBlank(firstname)))) { // must have both

						intern = beUtils.saveAnswer(new Answer(intern, intern, statusAttribute, "AVAILABLE"));
						intern = beUtils.saveAnswer(new Answer(intern, intern, deletedAttribute, "FALSE"));
					} else {
						if (!intern.getStatus().equals(EEntityStatus.PENDING)) {
							intern = beUtils.saveAnswer(new Answer(intern, intern, statusAttribute, "DODGY"));
							intern = beUtils.saveAnswer(new Answer(intern, intern, deletedAttribute, "TRUE"));
							intern.setStatus(EEntityStatus.DELETED);
							beUtils.saveBaseEntity(defIntern, intern);
							System.out.println(index + " of " + total + " Intern -> DELETED " + intern.getCode()
									+ " firstname " + firstname + " email " + email);
						}
					}
					continue;
				}

				// Now check for the most progressed one
				BaseEntity mostAdvancedApp = null;
				Integer maxScore = -2;
				Map<String, Integer> statusValueMap = new HashMap<>();
				statusValueMap.put("INACTIVE", -2);
				statusValueMap.put("REJECT", -1);
				statusValueMap.put("WITHDRAWN", 0);

				statusValueMap.put("APPLIED", 1);
				statusValueMap.put("SHORTLISTED", 2);
				statusValueMap.put("INTERVIEWED", 3);
				statusValueMap.put("OFFERED", 4);
				statusValueMap.put("PLACED", 5);
				statusValueMap.put("PROGRESS", 6);
				statusValueMap.put("COMPLETED", 7);

				for (BaseEntity app : applications) {
					String status = app.getValue("PRI_STATUS", null);
					if (status == null) {
						status = "REJECT";
						app = beUtils.saveAnswer(new Answer(app, app, statusAttribute, status));
					}
					if (status != null) {
						if (mostAdvancedApp == null) {
							if ("REJECTED".equals(status)) {
								status = "REJECT"; // fix
								app = beUtils.saveAnswer(new Answer(app, app, statusAttribute, "REJECT"));
							}
							mostAdvancedApp = app;

							maxScore = statusValueMap.get(status);
							continue;
						}
						Integer appScore = -1;

						try {
							if ("REJECTED".equals(status)) {
								status = "REJECT"; // fix
								app = beUtils.saveAnswer(new Answer(app, app, statusAttribute, "REJECT"));
							}
							appScore = statusValueMap.get(status);
							if (appScore == null) {
								System.out.println(index + " of " + total + " appScore is null - status=" + status);
							}
						} catch (Exception e) {
							System.out.println("NULL STATUS!");
						}

						if (appScore > maxScore) {
							maxScore = appScore;
							mostAdvancedApp = app;
						}

					} else {

					}
					// Force the name for each App
					app = beUtils.saveAnswer(new Answer(app, app, nameAttribute, intern.getName()));
					app = beUtils
							.saveAnswer(new Answer(app, app, lnkInternAttribute, "[\"" + intern.getCode() + "\"]"));
					app = beUtils.saveAnswer(new Answer(app, app, internCodeAttribute, intern.getCode()));
					app = beUtils.saveAnswer(new Answer(app, app, applicantCodeAttribute, intern.getCode()));
				}

				// Fix up WITHDRAWN statuses

				// Now if mostadvancedApp is in progress then withdraw the others
				if (("PROGRESS".equalsIgnoreCase(mostAdvancedApp.getValueAsString("PRI_STATUS")))
						|| ("PLACED".equalsIgnoreCase(mostAdvancedApp.getValueAsString("PRI_STATUS")))) {
					// Now withdraw all the existing apps
					// Now we do not want to lose the maximum stage reached for each application
					// and we want to be able to show buckets with 'withdrawn' shown.
					// So we essentially want to 'disable the buckets and not overwrite their normal
					// status with a WITHDRAWN status

					// set the intern status to reflect the latest
					String maxstatus = mostAdvancedApp.getValueAsString("PRI_STATUS");
					if (("REJECT".equalsIgnoreCase(maxstatus)) || ("WITHDRAWN".equalsIgnoreCase(maxstatus))) {
						maxstatus = "AVAILABLE";
					}
					intern = beUtils.saveAnswer(new Answer(intern, intern, statusAttribute, maxstatus));
					intern = beUtils.saveAnswer(new Answer(intern, intern, deletedAttribute, "TRUE"));
					// Set all applications to WITHDRAWN

					for (BaseEntity app : applications) {
						String status = app.getValue("PRI_STATUS", null);
						if (status != null) {
							if (!mostAdvancedApp.getValueAsString("PRI_STATUS").equalsIgnoreCase(status)) {
								if (!"REJECT".equalsIgnoreCase(app.getValueAsString("PRI_STATUS"))) {
									app = beUtils.saveAnswer(new Answer(app, app, statusAttribute, "WITHDRAWN"));
								}
							}
						}
					}
				}

				System.out.println(index + " of " + total + " Intern -> " + intern.getCode() + " " + intern.getName()
						+ " -> fixed up " + mostAdvancedApp.getValueAsString("PRI_STATUS"));

			}

			searchBE.setPageStart(index);
		}

	}

// @Test
	public void appnameFixTest() throws Exception {
		VertxUtils.cachedEnabled = false;

		if (beUtils == null) {
			return;
		}

		setUpDefs();

		SearchEntity searchBE = new SearchEntity("SBE_APPS", "APP Search")
				.addSort("PRI_CREATED", "Created", SearchEntity.Sort.ASC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "APP_%").addColumn("PRI_CODE", "Code")
				.addColumn("LNK_INTERN", "Link Intern").addColumn("PRI_INTERN_CODE", "Code Intern")
				.addColumn("PRI_JOURNAL_LEARNING_OUTCOMES", "LearningOutcomes")
				// .addAssociatedColumn("LNK_INTERN", "Intern Name", "LNK_COMP_INTERNSHIP")
				.addColumn("PRI_STATUS", "Status");
		searchBE.setRealm(realm);

		Boolean ok = true;
		Integer index = 0;
		Integer fixedInterns = 0;
		Integer fixedApps = 0;
		searchBE.setPageStart(index);
		Integer pageSize = 100;
		searchBE.setPageSize(pageSize);
		Long total = beUtils.getCount(searchBE);

		Attribute nameAttribute = RulesUtils.getAttribute("PRI_NAME", serviceToken);
		Attribute lnkInternAttribute = RulesUtils.getAttribute("LNK_INTERN", serviceToken);

		while (ok) {
			List<BaseEntity> bes = beUtils.getBaseEntitys(searchBE); // load 100 at a time
			if (bes.isEmpty() || (index > 5000)) {
				ok = false;
				break;
			}

			for (BaseEntity be : bes) {
				String fixedApp = "";
				String fixedIntern = "";

				// ok, now get the intern's application type

				// LNK_COMP_INTERNSHIP
				BaseEntity intern = beUtils.getLinkedBaseEntity(be, "LNK_INTERN");
				if (intern == null) {
					String internStr = be.getValueAsString("PRI_INTERN_CODE");
					if (!StringUtils.isBlank(internStr)) {
						intern = beUtils.getBaseEntityByCode(internStr);
						if (intern != null) {
							beUtils.saveAnswer(
									new Answer(be, be, lnkInternAttribute, "[\"" + intern.getCode() + "\"]"));
						}
					} else {
						internStr = be.getValueAsString("PRI_APPLICANT_CODE");
						if (!StringUtils.isBlank(internStr)) {
							intern = beUtils.getBaseEntityByCode(internStr);
							beUtils.saveNameStatus(be, null, EEntityStatus.ACTIVE);
							if (intern != null) {
								beUtils.saveAnswer(
										new Answer(be, be, lnkInternAttribute, "[\"" + intern.getCode() + "\"]"));
							}
						} else {
							be = beUtils.getBaseEntityByCode(be.getCode());
							// any intern hints?
							log.info("No intern associated with app ");
							// setting app to be deleted
							beUtils.saveNameStatus(be, null, EEntityStatus.DELETED);
							continue;
						}
					}
				}

				String appName = be.getValueAsString("PRI_NAME");
				if (appName == null) {
					appName = intern.getValue("PRI_NAME", null);

					if (!StringUtils.isBlank(appName)) {
						if (be.getName() != null) {
							appName = be.getName();
						}
						intern = beUtils.saveAnswer(new Answer(be, be, nameAttribute, appName));
						fixedInterns++;
						fixedIntern = "Fixed Intern";
					}
				}

				System.out.println(index + " of " + total + " :BE: " + be.getCode() + ":" + be.getName() + " --> "
						+ fixedIntern + "   " + fixedApp + " " + appName);

				index++;
			}

			searchBE.setPageStart(index);
		}

	}

//@Test
	public void hcrFixTest() throws Exception {
		VertxUtils.cachedEnabled = false;

		if (beUtils == null) {
			return;
		}

		setUpDefs();

//  Use a search BE for the api test
		SearchEntity searchBE = new SearchEntity("SBE_HCRS", "HCR Search")
				.addSort("PRI_CREATED", "Created", SearchEntity.Sort.ASC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "PER_%").addFilter("PRI_IS_HOST_CPY_REP", true)
				.addColumn("PRI_CODE", "Code").addColumn("LNK_HOST_COMPANY", "Link Intern")
				.addColumn("PRI_TIMEZONE_ID", "Timezone")
				// .addAssociatedColumn("LNK_INTERN", "Intern Name", "LNK_COMP_INTERNSHIP")
				.addColumn("PRI_STATUS", "Status");
		searchBE.setRealm(realm);

		Boolean ok = true;
		Integer index = 0;
		Integer fixedInterns = 0;
		Integer fixedApps = 0;
		searchBE.setPageStart(index);
		Integer pageSize = 100;
		searchBE.setPageSize(pageSize);
		Long total = beUtils.getCount(searchBE);

		Attribute lnkHCAttribute = RulesUtils.getAttribute("LNK_HOST_COMPANY", serviceToken);
		Attribute timezoneAttribute = RulesUtils.getAttribute("PRI_TIMEZONE_ID", serviceToken);

		while (ok) {
			List<BaseEntity> bes = beUtils.getBaseEntitys(searchBE); // load 100 at a time
			if (bes.isEmpty() || (index > 5000)) {
				ok = false;
				break;
			}

			for (BaseEntity be : bes) {
				String fixedHC = "";

				// Check if HCR has timezone

				String timezoneId = be.getValue("PRI_TIMEZONE_ID", null);
				if (timezoneId != null) {
					System.out.println("HCR has timezone " + timezoneId);
					continue;
				}

				// ok, now get the host company rep's hopst company

				BaseEntity hc = beUtils.getLinkedBaseEntity(be, "LNK_HOST_COMPANY");
				if (hc == null) {
					System.out.println("Error -> No Host company link for HCR");
					be = beUtils.getBaseEntityByCode(be.getCode());
					System.out.println("Finding possible ASSOC");
//     			String hcStr = be.getValueAsString("PRI_INTERN_CODE");
//     			if (!StringUtils.isBlank(internStr)) {
//     				intern = beUtils.getBaseEntityByCode(internStr);
//     				if (intern != null) {
//     					beUtils.saveAnswer(new Answer(be,be,lnkInternAttribute,"[\""+intern.getCode()+"\"]"));
//     				}
//     			} else {
//     				be = beUtils.getBaseEntityByCode(be.getCode());
//     				// any intern hints?
//     				log.info("No intern associated with app ");
//     				// setting app to be deleted
//     				beUtils.saveNameStatus(be,null,EEntityStatus.DELETED);
//     			}
					continue;
				}

				// Now copy the timezone for the company to the HC
				timezoneId = hc.getValue("PRI_TIMEZONE_ID", null);
				if (timezoneId == null) {
					// maybe try and work it out? get the country. ...
					System.out.println("Error -> No Host company Timezone");
					continue;
				}

				be = beUtils.saveAnswer(new Answer(be, be, timezoneAttribute, timezoneId));
				System.out.println("Updated HCR timezone " + timezoneId);
				index++;
			}

			searchBE.setPageStart(index);
		}

	}

//@Test
	public void internshipFixTest() throws Exception {
		VertxUtils.cachedEnabled = false;

		if (beUtils == null) {
			return;
		}

		setUpDefs();

//  Use a search BE for the api test
		SearchEntity searchBE = new SearchEntity("SBE_APPS", "APP Search")
				.addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "APP_%").addColumn("PRI_CODE", "Code")
				.addColumn("LNK_INTERN", "Link Intern").addColumn("PRI_INTERN_CODE", "Code Intern")
				.addColumn("PRI_JOURNAL_LEARNING_OUTCOMES", "LearningOutcomes")
				// .addAssociatedColumn("LNK_INTERN", "Intern Name", "LNK_COMP_INTERNSHIP")
				.addColumn("PRI_STATUS", "Status");
		searchBE.setRealm(realm);

		Boolean ok = true;
		Integer index = 0;
		Integer fixedInterns = 0;
		Integer fixedApps = 0;
		searchBE.setPageStart(index);
		Integer pageSize = 100;
		searchBE.setPageSize(pageSize);
		Long total = beUtils.getCount(searchBE);

		Attribute lnkCompInternshipAttribute = RulesUtils.getAttribute("LNK_COMP_INTERNSHIP", serviceToken);
		Attribute lnkInternAttribute = RulesUtils.getAttribute("LNK_INTERN", serviceToken);

		BaseEntity defIntern = beUtils.getDEFByCode("DEF_INTERN");

		while (ok) {
			List<BaseEntity> bes = beUtils.getBaseEntitys(searchBE); // load 100 at a time
			if (bes.isEmpty() || (index > 5000)) {
				ok = false;
				break;
			}

			for (BaseEntity be : bes) {
				String fixedApp = "";
				String fixedIntern = "";
				String internInternshipType = "[\"SEL_COURSE_CREDIT\"]";

				// ok, now get the intern's application type

				// LNK_COMP_INTERNSHIP
				BaseEntity intern = beUtils.getLinkedBaseEntity(be, "LNK_INTERN");
				if (intern == null) {
					String internStr = be.getValueAsString("PRI_INTERN_CODE");
					if (!StringUtils.isBlank(internStr)) {
						intern = beUtils.getBaseEntityByCode(internStr);
						if (intern != null) {
							beUtils.saveAnswer(
									new Answer(be, be, lnkInternAttribute, "[\"" + intern.getCode() + "\"]"));
						}
					} else {
						internStr = be.getValueAsString("PRI_APPLICANT_CODE");
						if (!StringUtils.isBlank(internStr)) {
							intern = beUtils.getBaseEntityByCode(internStr);
							beUtils.saveNameStatus(be, null, EEntityStatus.ACTIVE);
							if (intern != null) {
								beUtils.saveAnswer(
										new Answer(be, be, lnkInternAttribute, "[\"" + intern.getCode() + "\"]"));
							}
						} else {
							be = beUtils.getBaseEntityByCode(be.getCode());
							// any intern hints?
							log.info("No intern associated with app ");
							// setting app to be deleted
							beUtils.saveNameStatus(be, null, EEntityStatus.DELETED);
							continue;
						}
					}
				}

				String appInternshipType = be.getValueAsString("LNK_COMP_INTERNSHIP");
				if (intern != null) {
					internInternshipType = intern.getValue("LNK_COMP_INTERNSHIP", null);

					if (StringUtils.isBlank(internInternshipType)) {
						intern = beUtils.saveAnswer(defIntern,
								new Answer(intern, intern, lnkCompInternshipAttribute, "[\"SEL_COURSE_CREDIT\"]"));
						fixedInterns++;
						fixedIntern = "Fixed Intern";
					}
				}
				if (StringUtils.isBlank(appInternshipType)) {
					if (StringUtils.isBlank(internInternshipType)) {
						internInternshipType = "[\"SEL_COURSE_CREDIT\"]";
					}
					be = beUtils.getBaseEntityByCode(be.getCode());
					be = beUtils.saveAnswer(new Answer(be, be, lnkCompInternshipAttribute, internInternshipType));
					fixedApps++;
					fixedApp = "Fixed App";
				}

				System.out.println(index + " of " + total + " :BE: " + be.getCode() + ":" + be.getName() + " --> "
						+ fixedIntern + "   " + fixedApp + " " + internInternshipType);

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
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "DEF_%").addColumn("PRI_CODE", "Name");

		searchBE.setRealm(realm);
		searchBE.setPageStart(0);
		searchBE.setPageSize(1000);

		List<BaseEntity> items = beUtils.getBaseEntitys(searchBE);
		// Load up RuleUtils.defs

		RulesUtils.defs.put(realm, new ConcurrentHashMap<String, BaseEntity>());

		for (BaseEntity item : items) {
//            if the item is a def appointment, then add a default datetime for the start (Mandatory)
			if (item.getCode().equals("DEF_APPOINTMENT")) {
				Attribute attribute = new AttributeText("DFT_PRI_START_DATETIME", "Default Start Time");
				attribute.setRealm(realm);
				EntityAttribute newEA = new EntityAttribute(item, attribute, 1.0, "2021-07-28 00:00:00");
				item.addAttribute(newEA);

				Optional<EntityAttribute> ea = item.findEntityAttribute("ATT_PRI_START_DATETIME");
				if (ea.isPresent()) {
					ea.get().setValue(true);
				}
			}

//            Save the BaseEntity created
			item.setFastAttributes(true); // make fast
			RulesUtils.defs.get(realm).put(item.getCode(), item);
			log.info("Saving (" + realm + ") DEF " + item.getCode());
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
			javax.json.bind.Jsonb jsonb = JsonbBuilder.create();
			// JsonReader jsonReader = Json.createReader(new StringReader(keycloakJson));
			projectParms = jsonb.fromJson(keycloakJson, javax.json.JsonObject.class);
			// jsonReader.close();

			String authServer = projectParms.getString("ENV_KEYCLOAK_REDIRECTURI");
			authServer = StringUtils.removeEnd(authServer, "/auth");
			// javax.json.JsonObject credentials =
			// projectParms.getJsonObject("credentials");
			// String secret = credentials.getString("secret");
			String username = System.getenv("USERNAME");
			String password = System.getenv("PASSWORD");

			String token = KeycloakUtils.getAccessToken(authServer, realm, "alyson", null, username, password);
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

			String serviceTokenStr = KeycloakUtils.getAccessToken(authServer, realm, "alyson", null, "service",
					System.getenv("SERVICE_PASSWORD"));
			serviceToken = new GennyToken(serviceTokenStr);

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			return;
		}
		serviceToken = new GennyToken("PER_SERVICE", serviceToken);
		VertxUtils.cachedEnabled = false;

		beUtils = new BaseEntityUtils(userToken);
		beUtils.setServiceToken(serviceToken);

		DefUtils.loadDEFS(realm, serviceToken);
	}

}
