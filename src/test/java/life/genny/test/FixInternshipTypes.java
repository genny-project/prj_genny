package life.genny.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.json.Json;
import javax.json.JsonReader;
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

import io.vavr.Tuple2;
import life.genny.eventbus.EventBusInterface;
import life.genny.eventbus.EventBusMock;
import life.genny.eventbus.VertxCache;
import life.genny.models.GennyToken;
import life.genny.qwanda.Answer;
import life.genny.qwanda.EEntityStatus;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeText;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.SearchEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwandautils.GennyCacheInterface;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.KeycloakUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.utils.BaseEntityUtils;
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

		Attribute statusAttribute = RulesUtils.getAttribute("PRI_STATUS", serviceToken.getToken());
		Attribute deletedAttribute = RulesUtils.getAttribute("PRI_DISABLED", serviceToken.getToken());
		Attribute lnkInternAttribute = RulesUtils.getAttribute("LNK_INTERN", serviceToken.getToken());
		Attribute nameAttribute = RulesUtils.getAttribute("PRI_NAME", serviceToken.getToken());
		Attribute internCodeAttribute = RulesUtils.getAttribute("PRI_INTERN_CODE", serviceToken.getToken());
		Attribute applicantCodeAttribute = RulesUtils.getAttribute("PRI_APPLICANT_CODE", serviceToken.getToken());

		Attribute internNameAttribute = RulesUtils.getAttribute("PRI_INTERN_NAME", serviceToken.getToken());
		Attribute internMobileAttribute = RulesUtils.getAttribute("PRI_INTERN_MOBILE", serviceToken.getToken());
		Attribute internEmailAttribute = RulesUtils.getAttribute("PRI_INTERN_EMAIL", serviceToken.getToken());
		Attribute internStudentIdAttribute = RulesUtils.getAttribute("PRI_INTERN_STUDENT_ID", serviceToken.getToken());
		Attribute assocHCAttribute = RulesUtils.getAttribute("PRI_ASSOC_HC", serviceToken.getToken());
		Attribute abnAttribute = RulesUtils.getAttribute("PRI_ABN", serviceToken.getToken());
		Attribute addressFullAttribute = RulesUtils.getAttribute("PRI_ADDRESS_FULL", serviceToken.getToken());
		Attribute superNameAttribute = RulesUtils.getAttribute("PRI_SUPER_NAME", serviceToken.getToken());
		Attribute superJobTitleAttribute = RulesUtils.getAttribute("PRI_SUPER_JOB_TITLE", serviceToken.getToken());
		Attribute superMobileAttribute = RulesUtils.getAttribute("PRI_SUPER_MOBILE", serviceToken.getToken());
		Attribute superEmailAttribute = RulesUtils.getAttribute("PRI_SUPER_EMAIL", serviceToken.getToken());
		Attribute assocWorksiteAttribute = RulesUtils.getAttribute("PRI_ASSOC_WORKSITE", serviceToken.getToken());
		Attribute startDateAttribute = RulesUtils.getAttribute("PRI_START_DATE", serviceToken.getToken());
		Attribute endDateAttribute = RulesUtils.getAttribute("PRI_END_DATE", serviceToken.getToken());
		Attribute assocDurationAttribute = RulesUtils.getAttribute("PRI_ASSOC_DURATION", serviceToken.getToken());
		Attribute businessHoursAttribute = RulesUtils.getAttribute("PRI_BUSINESS_HOURS", serviceToken.getToken());
		Attribute hoursPerWeekAttribute = RulesUtils.getAttribute("PRI_HOURS_PER_WEEK", serviceToken.getToken());
		Attribute daysPerWeekAttribute = RulesUtils.getAttribute("PRI_DAYS_PER_WEEK", serviceToken.getToken());
		Attribute whichDaysStrippedAttribute = RulesUtils.getAttribute("PRI_WHICH_DAYS_STRIPPED",
				serviceToken.getToken());
		Attribute dressCodeAttribute = RulesUtils.getAttribute("PRI_DRESS_CODE", serviceToken.getToken());
		Attribute agDocNotesAttribute = RulesUtils.getAttribute("PRI_AG_DOC_NOTES", serviceToken.getToken());
		Attribute outcomeLifeRepNameAttribute = RulesUtils.getAttribute("PRI_OUTCOME_LIFE_REP_NAME",
				serviceToken.getToken());

		Attribute agrDocHcSignatureAttribute = RulesUtils.getAttribute("PRI_AGR_DOC_HC_SIGNATURE",
				serviceToken.getToken());
		Attribute agrDocIntSignatureAttribute = RulesUtils.getAttribute("PRI_AGR_DOC_INT_SIGNATURE",
				serviceToken.getToken());
		Attribute agrDocOutcomeSignatureAttribute = RulesUtils.getAttribute("PRI_AGR_DOC_OUTCOME_SIGNATURE",
				serviceToken.getToken());

		Attribute agrDocHcSignDateAttribute = RulesUtils.getAttribute("PRI_AGR_DOC_HC_SIGN_DATE",
				serviceToken.getToken());
		Attribute agrDocIntSignDateAttribute = RulesUtils.getAttribute("PRI_AGR_DOC_INT_SIGN_DATE",
				serviceToken.getToken());
		Attribute agrDocOutcomeSignDateAttribute = RulesUtils.getAttribute("PRI_AGR_DOC_OUTCOME_SIGN_DATE",
				serviceToken.getToken());

		BaseEntity defIntern = beUtils.getDEFByCode("DEF_INTERN");

		while (ok) {
			List<BaseEntity> interns = beUtils.getBaseEntitys(searchBE); // load 100 at a time
			if (interns.isEmpty() || (index > 5000)) {
				ok = false;
				break;
			}

			for (BaseEntity intern : interns) {
				index++;

			//	intern = beUtils.getBaseEntityByCode("PER_CD81FE03-9DF4-4104-A575-9CF5C60639B3");

				// intern =
				// beUtils.getBaseEntityByCode("PER_D620320A-EF19-451E-8E45-0DCBD8F3232D"); //

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

				// ok, we should have all the apps now..
				if (index == 15) {
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
//					if (!"APP_71FDF68A-2638-4CF8-BB1B-389BEFAE7DC1".equals(app.getCode())) {
//						continue;
//					}
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

					// fix up missing attributes
					BaseEntity internship = beUtils.getBaseEntityFromLNKAttr(app, "LNK_INTERNSHIP");

					BaseEntity internshipSupervisor = beUtils.getBaseEntityFromLNKAttr(internship,
							"LNK_INTERN_SUPERVISOR");

					app = appok("PRI_INTERN_NAME", "LNK_INTERN", "PRI_NAME", app);
					app = appok("PRI_INTERN_MOBILE", "LNK_INTERN", "PRI_MOBILE", app);
					app = appok("PRI_INTERN_EMAIL", "LNK_INTERN", "PRI_EMAIL", app);
					app = appok("PRI_INTERN_STUDENT_ID", "LNK_INTERN", "PRI_STUDENT_ID", app);
					app = appok("PRI_ASSOC_HC", "LNK_HOST_COMPANY", "PRI_NAME", app);
					app = appok("PRI_ABN", "LNK_HOST_COMPANY", "PRI_ABN", app);
					app = appok("PRI_ADDRESS_FULL", "LNK_INTERNSHIP", "PRI_ADDRESS_FULL", app);
					app = appok("PRI_SUPER_NAME", "LNK_INTERNSHIP", "PRI_ASSOC_SUPERVISOR", app);
					app = beUtils.saveAnswer(new Answer(app, app, superJobTitleAttribute,
							internshipSupervisor.getValueAsString("PRI_JOB_TITLE")));
					app = appok("PRI_SUPER_MOBILE", "LNK_INTERNSHIP", "PRI_SUPER_MOBILE", app);
					app = appok("PRI_SUPER_EMAIL", "LNK_INTERNSHIP", "PRI_SUPER_EMAIL", app);
					app = appok("PRI_ASSOC_WORKSITE", "LNK_INTERNSHIP", "PRI_WORKSITE", app);
					app = appok("PRI_START_DATE", "LNK_INTERNSHIP", "PRI_INTERNSHIP_START_DATE", app);
					app = appok("PRI_END_DATE", "LNK_INTERNSHIP", "PRI_INTERNSHIP_END_DATE", app);
					app = appok("PRI_ASSOC_DURATION", "LNK_INTERN", "PRI_ASSOC_DURATION", app);
					app = appok("PRI_BUSINESS_HOURS", "LNK_INTERNSHIP", "PRI_BUSINESS_HOURS", app);
					BaseEntity daysPerWeek = beUtils.getBaseEntityFromLNKAttr(intern, "LNK_DAYS_PER_WEEK");
					if (daysPerWeek != null) {
						if (StringUtils.isBlank(app.getValueAsString("PRI_DAYS_PER_WEEK"))) {
							app = beUtils.saveAnswer(new Answer(app, app, daysPerWeekAttribute, daysPerWeek.getName()));
						}
					}
					BaseEntity hoursPerWeek = beUtils.getBaseEntityFromLNKAttr(intern, "LNK_HOURS_PER_WEEK");
					if (hoursPerWeek != null) {
						if ((StringUtils.isBlank(app.getValueAsString("PRI_HOURS_PER_WEEK")))||("0.0".equals(app.getValueAsString("PRI_HOURS_PER_WEEK")))) {
							app = beUtils
									.saveAnswer(new Answer(app, app, hoursPerWeekAttribute, hoursPerWeek.getName()));
						}
					} else {
						if ((StringUtils.isBlank(app.getValueAsString("PRI_HOURS_PER_WEEK")))||("0.0".equals(app.getValueAsString("PRI_HOURS_PER_WEEK")))) {
							if (daysPerWeek != null) {
								Integer dpw = Integer.parseInt(daysPerWeek.getName());
							app = beUtils
									.saveAnswer(new Answer(app, app, hoursPerWeekAttribute, ""+(dpw*8)));
							}
						}
					}

					app = appok("PRI_HOURS_PER_WEEK", "LNK_INTERN", "PRI_HOURS_PER_WEEK", app);
					app = appok("PRI_DAYS_PER_WEEK", "LNK_INTERN", "PRI_DAYS_PER_WEEK", app);
					app = appok("PRI_WHICH_DAYS_STRIPPED", "LNK_INTERNSHIP", "PRI_WHICH_DAYS_STRIPPED", app);
					app = appok("PRI_DRESS_CODE", "LNK_INTERNSHIP", "PRI_DRESS_CODE", app);
					app = appok("PRI_OUTCOME_LIFE_REP_NAME", "LNK_AGENT", "PRI_NAME", app);
					// app = appok("PRI_AG_DOC_NOTES","LNK_AGENT","PRI_NAME",app);

					// beUtils.saveBaseEntity(beUtils.getDEFByCode("DEF_APPLICATION"), app);

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
					intern = beUtils.saveAnswer(new Answer(intern, intern, deletedAttribute, "FALSE"));
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

	public BaseEntity appok(String attributeCode, String copyFromLinkCode, String copyFromLinkAttribte,
			BaseEntity copyTo) {
		BaseEntity ret = null;

		Attribute attribute = RulesUtils.getAttribute(attributeCode, serviceToken.getToken());

		if (copyTo.containsEntityAttribute(attributeCode)) {
			String valString = copyTo.getValueAsString(attributeCode);
			if ((!StringUtils.isBlank(valString)) && (!"[\"\"]".equals(valString)) && (!"0.0".equals(valString))) {
				return copyTo;
			}
		}
		BaseEntity copyFromBe = beUtils.getBaseEntityFromLNKAttr(copyTo, copyFromLinkCode);
		if (copyFromBe != null) {
			try {
				copyTo.setValue(attribute, copyFromBe.getValue(copyFromLinkAttribte, null));
				System.out.println("Fixed for copyFrom " + copyFromBe.getCode() + " for copyTo " + copyTo.getCode()
						+ " " + attributeCode);
				beUtils.saveAnswer(
						new Answer(copyTo, copyTo, attribute, copyFromBe.getValueAsString(copyFromLinkAttribte)));
				return copyTo;
			} catch (BadDataException e) {
				System.out.println("Bad data for copyFrom " + copyFromBe.getCode() + " for copyTo " + copyTo.getCode());
			}
		}
		return copyTo;
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

		Attribute nameAttribute = RulesUtils.getAttribute("PRI_NAME", serviceToken.getToken());
		Attribute lnkInternAttribute = RulesUtils.getAttribute("LNK_INTERN", serviceToken.getToken());

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

	@Test
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

		Attribute lnkHCAttribute = RulesUtils.getAttribute("LNK_HOST_COMPANY", serviceToken.getToken());
		Attribute timezoneAttribute = RulesUtils.getAttribute("PRI_TIMEZONE_ID", serviceToken.getToken());

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

	@Test
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

		Attribute lnkCompInternshipAttribute = RulesUtils.getAttribute("LNK_COMP_INTERNSHIP", serviceToken.getToken());
		Attribute lnkInternAttribute = RulesUtils.getAttribute("LNK_INTERN", serviceToken.getToken());

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
			JsonReader jsonReader = Json.createReader(new StringReader(keycloakJson));
			projectParms = jsonReader.readObject();
			jsonReader.close();

			String authServer = projectParms.getString("auth-server-url");
			authServer = StringUtils.removeEnd(authServer, "/auth");
			javax.json.JsonObject credentials = projectParms.getJsonObject("credentials");
			String secret = credentials.getString("secret");
			String username = System.getenv("USERNAME");
			String password = System.getenv("PASSWORD");

			String token = KeycloakUtils.getAccessToken(authServer, realm, realm, secret, username, password);
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

			String serviceTokenStr = KeycloakUtils.getAccessToken(authServer, realm, realm, secret, "service",
					System.getenv("SERVICE_PASSWORD"));
			serviceToken = new GennyToken(serviceTokenStr);

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			return;
		}
		serviceToken = new GennyToken("PER_SERVICE", serviceToken.getToken());
		VertxUtils.cachedEnabled = false;

		beUtils = new BaseEntityUtils(userToken);
		beUtils.setServiceToken(serviceToken);

	}

}