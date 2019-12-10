
package life.genny.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.json.simple.parser.JSONParser;
import org.junit.Test;
import org.kie.api.runtime.process.ProcessInstance;

import com.google.gson.Gson;

import io.vertx.core.json.JsonObject;
import life.genny.eventbus.EventBusInterface;
import life.genny.eventbus.EventBusMock;
import life.genny.eventbus.VertxCache;
import life.genny.model.NodeStatus;
import life.genny.models.Frame3;
import life.genny.models.FramePosition;
import life.genny.models.GennyToken;
import life.genny.models.Theme;
import life.genny.models.ThemeAttributeType;
import life.genny.models.ThemePosition;
import life.genny.qwanda.Answer;
import life.genny.qwanda.Ask;
import life.genny.qwanda.ContextList;
import life.genny.qwanda.Question;
import life.genny.qwanda.VisualControlType;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.datatype.DataType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.SearchEntity;
import life.genny.qwanda.message.QDataAnswerMessage;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataAttributeMessage;
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
import life.genny.utils.DropdownUtils;
import life.genny.utils.FrameUtils2;
import life.genny.utils.QuestionUtils;
import life.genny.utils.TableUtils;
import life.genny.utils.VertxUtils;

public class SafalTest extends GennyJbpmBaseTest {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	protected static String realm = GennySettings.mainrealm;
	protected static Set<String> realms;

	protected static EventBusInterface eventBusMock;
	protected static GennyCacheInterface vertxCache;

	private static final String DRL_SEND_USER_DATA_DIR = "SendUserData";
	private static final String WFE_SEND_FORMS = "send_forms.bpmn";
	private static final String WFE_SHOW_FORM = "show_form.bpmn";
	private static final String WFE_AUTH_INIT = "auth_init.bpmn";
	private static final String WFE_SEND_LLAMA = "send_llama.bpmn";

	private static Theme THM_SHADOW = null;
	private static Theme THM_CONTENT_WRAPPER = null;
	private static BaseEntity project = null;
	private static GennyToken userToken = null;
	private static QRules rules = null;
	private static GennyToken serviceToken = null;

	public SafalTest() {

		super(false);
	}

//	@Test
	public void Test1() {

		initItem();

		String code = "ASK_" + "FRM_PERSON_DETAIL_VIEW";

		JsonObject tokenObj = VertxUtils.readCachedJson(userToken.getRealm(), code, userToken.getToken());

		String items = tokenObj.getString("value");

		QDataAskMessage[] result = JsonUtils.fromJson(items, QDataAskMessage[].class);
		for (QDataAskMessage a : result) {
			System.out.println(a);
		}
	}

	@Test
	public void DropdownUtilsTest() throws IOException {
		initItem();
		
		       
		DropdownUtils dropDownUtils = new DropdownUtils();
		
		dropDownUtils.setNewSearch("IndustryClassification"," Classificcatino of industry")
			.addFilter("PRI_CODE",SearchEntity.StringFilter.LIKE,"SEL_%")
	        .setSourceCode("GRP_INDUSTRY_SELECTION")
	        .setPageStart(0)
	        .setPageSize(10000);
			
		dropDownUtils.sendSearchResults("GRP_INDUSTRY_SELECTION", "LNK_CORE", "INDUSTRY", userToken);
		
	}
	
	//@Test
	public void test1000() {
		initItem();
		try {
		
		Map<String, Attribute> attributeMap = new ConcurrentHashMap<String, Attribute>();
		String token = serviceToken.getToken();
		String jsonString = QwandaUtils.apiGet(GennySettings.qwandaServiceUrl + "/qwanda/attributes", token);
		
		if (!StringUtils.isBlank(jsonString)) {
		VertxUtils.writeCachedJson(realm, "attributes", jsonString, token);
		
		QDataAttributeMessage attributesMsg = JsonUtils.fromJson(jsonString, QDataAttributeMessage.class);
		Attribute[] attributeArray = attributesMsg.getItems();
		
		Attribute result = null;
		
		for (Attribute attribute : attributeArray) {
			if(attribute.getCode().equals("LNK_INDUSTRY")) {
				result = attribute;
			}
			attributeMap.put(attribute.getCode(), attribute);
		}
		
		System.out.println("All the attributes have been loaded from api in" + attributeMap.size() + " attributes");
		} else {
			log.error("NO ATTRIBUTES LOADED FROM API");
		}
		
		
		
		QDataAskMessage sss = QuestionUtils.getAsks("PER_SERVICE", "PER_SERVICE", "QUE_ADD_INTERNSHIP_TEMPLATE_AGENT_GRP",serviceToken.getToken());
		rules.sendAllAttributes();
		}catch(Exception e) {
			
		}
	}
	
	//@Test  	
    public void sss() {
    	
    	initItem();
    	
    	 /* frame-root */
        try {
			Frame3 frame = Frame3.builder("FRM_QUE_TREE_FORM_VIEW")
			              .addTheme("THM_TREE_ITEM", ThemePosition.WRAPPER, serviceToken).end()
			              .question("QUE_TREE_FORM_VIEW_GRP")
			                  .addTheme("THM_TREE_GROUP_BEHAVIOUR", serviceToken).end()
			                  .addTheme("THM_TREE_GROUP_CLICKABLE_WRAPPER", serviceToken)
			                  .vcl(VisualControlType.GROUP_CLICKABLE_WRAPPER).end()
			                  .addTheme("THM_TREE_GROUP_CONTENT_WRAPPER", serviceToken)
			                  .vcl(VisualControlType.GROUP_CONTENT_WRAPPER)
			                  .end()
			                  .addTheme("THM_TREE_GROUP_WRAPPER", serviceToken)
			                  .vcl(VisualControlType.GROUP_WRAPPER)
			                  .end()
			                  .addTheme("THM_TREE_GROUP_LABEL", serviceToken)
			                  .vcl(VisualControlType.GROUP_LABEL)
			              .end()
			               .end()
			              .build();
			
			FrameUtils2.toMessage(frame, serviceToken);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }

	// @Test
	public void queryTest() {

		String sas = "QUE_DETAIL_VIEW_GRP";
		sas = sas.replace("QUE_", "FRM_");
		System.out.println(sas);
	}

	// @Test
	public void test11() {
		initItem();

		SearchEntity searchBE = VertxUtils.getObject(serviceToken.getRealm(), "", "SBE_INTERNSHIP_DETAIL_VIEW",
				SearchEntity.class, serviceToken.getToken());

		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
		TableUtils tableUtils = new TableUtils(beUtils);
		Set<QDataAskMessage> askSet = new HashSet<QDataAskMessage>();
		Map<String, ContextList> contextListMap = new HashMap<String, ContextList>();
		Map<String, QDataAskMessage> virtualAskMap = new HashMap<String, QDataAskMessage>();

		/* Fetching searh resutls and labels for internship detail view */
		QDataBaseEntityMessage fetchResultMsg = tableUtils.fetchSearchResults(searchBE, serviceToken);
		Map<String, String> askLabels = tableUtils.getTableColumns(searchBE);
		List<Object> belist = new ArrayList<>(Arrays.asList(fetchResultMsg.getItems()));
		List<BaseEntity> results = (List<BaseEntity>) (List) belist;
		List<Ask> asks = TableUtils.generateQuestions(serviceToken, beUtils, results, askLabels, "PRJ_INTERNMATCH");

		/* Ask Question code should be similar to the frame question code */
		Ask askInternshipDetail = asks.get(0);
		askInternshipDetail.setQuestionCode("QUE_INTERNSHIP_DETAIL_SUMMARY_GRP");
		askInternshipDetail.setName("Internship Details");

		/* Process and save to cache */
		virtualAskMap.put(askInternshipDetail.getQuestionCode(), new QDataAskMessage(askInternshipDetail));

		/* Fetching searh resutls and labels for internship detail view top summary */
		SearchEntity searchBE2 = VertxUtils.getObject(serviceToken.getRealm(), "",
				"SBE_INTERNSHIP_DETAIL_VIEW_TOP_SUMMARY", SearchEntity.class, serviceToken.getToken());
		fetchResultMsg = tableUtils.fetchSearchResults(searchBE, serviceToken);
		askLabels = tableUtils.getTableColumns(searchBE);
		belist = new ArrayList<>(Arrays.asList(fetchResultMsg.getItems()));
		results = (List<BaseEntity>) (List) belist;
		asks = TableUtils.generateQuestions(serviceToken, beUtils, results, askLabels, "PRJ_INTERNMATCH");

		/* Ask Question code should be similar to the frame question code */
		Ask askTopSummary = asks.get(0);
		askTopSummary.setQuestionCode("QUE_DETAIL_VIEW_TOP_SUMMARY_GRP");
		askTopSummary.setName("Internship");

		/* Process and save to cache */
		virtualAskMap.put(askTopSummary.getQuestionCode(), new QDataAskMessage(askTopSummary));
	}

	public void initItem() {
		boolean runNewLocal = false;

		if (runNewLocal) {
			userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");

			serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User", "service");
			rules = new QRules(eventBusMock, userToken.getToken());

			VertxUtils.cachedEnabled = true; // don't send to local Service Cache
			GennyKieSession.loadAttributesJsonFromResources(userToken);

		} else {

			rules = GennyJbpmBaseTest.setupLocalService();
			userToken = new GennyToken("userToken", rules.getToken());
			serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
		}

		userToken.getCode();
		QDataAskMessage sas = QuestionUtils.getAsks("PER_USER1", "PER_USER1", "QUE_PERSON_DETAIL_GRP",
				userToken.getToken());
		System.out.println(sas);
		System.out.println("__________" + userToken.getCode());

		// rules.sendAllAttributes();

		project = new BaseEntityUtils(serviceToken).getBaseEntityByCode("PRJ_INTERNMATCH");
		rules.sendAllAttributes();

		// Already there THM_BOX_SHADOW_SM
		THM_SHADOW = Theme.builder("THM_SHADOW").addAttribute().shadowColor("#000").shadowOpacity(0.4).shadowRadius(10)
				.shadowOffset().width(0).height(0).end().end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

		THM_CONTENT_WRAPPER = Theme.builder("THM_CONTENT_WRAPPER")
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().addAttribute().borderRadius(5)
				.borderWidth(1).borderColor("black").end().build();

	}

	//@Test
	public void v7DetailsViewInternship() {

		initItem();
		/* Themes and frames */

		try {

			Ask[] askList = new Ask[2];

			Ask internshipDetailAsk = getInternshipDetails();
			askList[0] = internshipDetailAsk;

			Ask studentAsk = internshipTopCard();
			askList[1] = studentAsk;

			// THM_DETAIL_VIEW_BODY
			Theme THM_DETAIL_VIEW_BODY = Theme.builder("THM_DETAIL_VIEW_BODY").addAttribute().flex(15).padding(10)
					.justifyContent("flex-start").end()
					// .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
					.build();

			/* No need already exists THM_JUSTIFY_CONTENT_FLEX_START */

			Theme THM_JUSTIFY_CONTENT_FLEX_START = Theme.builder("THM_JUSTIFY_CONTENT_FLEX_START").addAttribute()
					.justifyContent("flex-start").end().build();

			/* Already exist THM_SCROLL_VERTICAL */
			Theme THM_SCROLL_VERTICAL = Theme.builder("THM_SCROLL_VERTICAL").addAttribute().overflowY("auto").end()
					.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

			Frame3 FRM_INTERNSHIP_DETAILS = getDetailFrame("FRM_INTERNSHIP_DETAILS", "BEG_INTERNSHIP_ONE",
					"QUE_TEST_INTERNSHIP_DETAILS_GRP");

			Frame3 FRM_INTERNSHIP_TOP = getTopFrame("FRM_INTERNSHIP_TOP", "BEG_INTERNSHIP_ONE",
					"QUE_TEST_INTERNSHIP_SUMMARY_GRP");

			Frame3 FRM_DETAIL_VIEW_BODY = Frame3.builder("FRM_DETAIL_VIEW_BODY")
					.addTheme(THM_SCROLL_VERTICAL, ThemePosition.WRAPPER).end()
					.addTheme(THM_DETAIL_VIEW_BODY, ThemePosition.WRAPPER).end()
					.addTheme(THM_JUSTIFY_CONTENT_FLEX_START, ThemePosition.CENTRE).end()
					.addFrame(FRM_INTERNSHIP_TOP, FramePosition.CENTRE).end()
					.addFrame(FRM_INTERNSHIP_DETAILS, FramePosition.CENTRE).end().build();

			Frame3 FRM_ROOT = Frame3.builder("FRM_ROOT").addFrame(FRM_DETAIL_VIEW_BODY).end().build();

			/* end */
			Set<QDataAskMessage> set = new HashSet<QDataAskMessage>();

			Map<String, ContextList> contextListMap = new HashMap<String, ContextList>();

			Map<String, QDataAskMessage> virtualAskMap = new HashMap<String, QDataAskMessage>();

			virtualAskMap.put(askList[0].getQuestionCode(), new QDataAskMessage(askList[0]));
			virtualAskMap.put(askList[1].getQuestionCode(), new QDataAskMessage(askList[1]));
			// virtualAskMap.put(askList[2].getQuestionCode(), new
			// QDataAskMessage(askList[2]));

			QDataBaseEntityMessage msgg = new QDataBaseEntityMessage(rules.getUser());
			msgg.setToken(userToken.getToken());
			msgg.setReplace(true);

			VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msgg));

			BaseEntity application = new BaseEntityUtils(serviceToken).getBaseEntityByCode("BEG_INTERNSHIP_ONE");

			QDataBaseEntityMessage msgIntership = new QDataBaseEntityMessage(application);
			msgIntership.setToken(userToken.getToken());
			msgIntership.setReplace(true);

			VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msgIntership));

			QDataBaseEntityMessage msg = FrameUtils2.toMessage(FRM_ROOT, serviceToken, set, contextListMap,
					virtualAskMap);

			msg.setToken(userToken.getToken());
			msg.setReplace(true);

			/* send message */
			System.out.println("Sending Asks");
			for (QDataAskMessage item : set) {

				item.setToken(userToken.getToken());

				String json = JsonUtils.toJson(item);
				VertxUtils.writeMsg("webcmds", json);

			}

			/* we publish the virtual ask with child asks */

			VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//@Test
	public void v7DetailsViewTimeLine() {

		initItem();
		/* Themes and frames */

		try {

			Ask[] askList = new Ask[3];

			Ask internshipDetailAsk = getApplicationInternshipDetailAsk();
			askList[0] = internshipDetailAsk;

			Ask studentAsk = getStudentAsk();
			askList[1] = studentAsk;

			Ask applicationDetailAsk = getApplicationDetailAsk();
			askList[2] = applicationDetailAsk;

			// THM_DETAIL_VIEW_BODY
			Theme THM_DETAIL_VIEW_BODY = Theme.builder("THM_DETAIL_VIEW_BODY").addAttribute().flex(15).padding(10)
					.justifyContent("flex-start").end()
					// .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
					.build();

			/* No need already exists THM_JUSTIFY_CONTENT_FLEX_START */

			Theme THM_JUSTIFY_CONTENT_FLEX_START = Theme.builder("THM_JUSTIFY_CONTENT_FLEX_START").addAttribute()
					.justifyContent("flex-start").end().build();

			/* Already exist THM_SCROLL_VERTICAL */
			Theme THM_SCROLL_VERTICAL = Theme.builder("THM_SCROLL_VERTICAL").addAttribute().overflowY("auto").end()
					.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

			Frame3 FRM_PERSON_DETAIL = getTimeLineFrame("FRM_TIMELINE", rules.getUser().getCode());
			Frame3 FRM_INTERNSHIP_DETAILS = getDetailFrame("FRM_INTERNSHIP_DETAILS", "APP_APPLICATION_THREE",
					"QUE_TEST_INTERNSHIP_SUMMARY_GRP");

			Frame3 FRM_INTERN_DETAIL_FRAME = Frame3.clone(FRM_INTERNSHIP_DETAILS);
			FRM_INTERN_DETAIL_FRAME.setCode("FRM_INTERN_DETAIL_FRAME");
			FRM_INTERN_DETAIL_FRAME.setQuestionCode("QUE_TEST_INTERN_SUMMARY_GRP");
			FRM_INTERN_DETAIL_FRAME.getQuestionGroup().setCode("QUE_TEST_INTERN_SUMMARY_GRP");

			Frame3 FRM_APPLICATION_TOP = getTopFrame("FRM_APPLICATION_TOP", "APP_APPLICATION_THREE",
					"QUE_TEST_APPLICATION_SUMMARY_GRP");

			Frame3 FRM_DETAIL_VIEW_BODY = Frame3.builder("FRM_DETAIL_VIEW_BODY")
					.addTheme(THM_SCROLL_VERTICAL, ThemePosition.WRAPPER).end()
					.addTheme(THM_DETAIL_VIEW_BODY, ThemePosition.WRAPPER).end()
					.addTheme(THM_JUSTIFY_CONTENT_FLEX_START, ThemePosition.CENTRE).end()
					.addFrame(FRM_APPLICATION_TOP, FramePosition.CENTRE).end()
					.addFrame(FRM_INTERNSHIP_DETAILS, FramePosition.CENTRE).end()
					.addFrame(FRM_INTERN_DETAIL_FRAME, FramePosition.CENTRE).end()
					.addFrame(FRM_PERSON_DETAIL, FramePosition.CENTRE).end().build();

			Frame3 FRM_ROOT = Frame3.builder("FRM_ROOT").addFrame(FRM_DETAIL_VIEW_BODY).end().build();

			/* end */
			Set<QDataAskMessage> set = new HashSet<QDataAskMessage>();

			Map<String, ContextList> contextListMap = new HashMap<String, ContextList>();

			Map<String, QDataAskMessage> virtualAskMap = new HashMap<String, QDataAskMessage>();
			virtualAskMap.put(askList[0].getQuestionCode(), new QDataAskMessage(askList[0]));
			virtualAskMap.put(askList[1].getQuestionCode(), new QDataAskMessage(askList[1]));
			virtualAskMap.put(askList[2].getQuestionCode(), new QDataAskMessage(askList[2]));

			QDataBaseEntityMessage msgg = new QDataBaseEntityMessage(rules.getUser());
			msgg.setToken(userToken.getToken());
			msgg.setReplace(true);

			VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msgg));

			BaseEntity application = new BaseEntityUtils(serviceToken).getBaseEntityByCode("APP_APPLICATION_THREE");

			QDataBaseEntityMessage msgIntership = new QDataBaseEntityMessage(application);
			msgIntership.setToken(userToken.getToken());
			msgIntership.setReplace(true);

			VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msgIntership));

			QDataBaseEntityMessage msg = FrameUtils2.toMessage(FRM_ROOT, serviceToken, set, contextListMap,
					virtualAskMap);

			msg.setToken(userToken.getToken());
			msg.setReplace(true);

			/* send message */
			System.out.println("Sending Asks");
			for (QDataAskMessage item : set) {

				item.setToken(userToken.getToken());

				String json = JsonUtils.toJson(item);
				VertxUtils.writeMsg("webcmds", json);

			}

			/* we publish the virtual ask with child asks */

			VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Frame3 getTimeLineFrame(String name, String target) {

		// THM_DETAIL_VIEW_CARD_WRAPPER
		Theme THM_CONTEXT_SUMMARY = Theme.builder("THM_CONTEXT_SUMMARY").addAttribute().backgroundColor("#faf9fa")
				.justifyContent("flex-start").flexShrink(0).flexBasis("auto").flexGrow(0).height("initial")
				.marginBottom(20).padding(10).maxWidth(700).end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

		Theme THM_TIMELINE_POSITION = Theme.builder("THM_TIMELINE_POSITION").addAttribute().alignItems("flex-start")
				.borderColor("black").borderLeftWidth(4).paddingLeft(5).end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

		Theme THM_TIMELINE_ITEM_BOLD = Theme.builder("THM_TIMELINE_ITEM_BOLD").addAttribute().bold(true).end().build();

		Theme THM_TIMELINE_ITEM_PADDING_BOTTOM = Theme.builder("THM_TIMELINE_ITEM_PADDING_BOTTOM").addAttribute()
				.paddingY(5).end().build();

		try {

			/* ******end******* */
			Frame3 FRM_TIMELINE = Frame3.builder(name).addTheme(THM_CONTEXT_SUMMARY, ThemePosition.WRAPPER).end()
					.addTheme(THM_TIMELINE_POSITION, ThemePosition.CENTRE).end()
					.addTheme(THM_SHADOW, ThemePosition.WRAPPER).end().question("QUE_APPLICATION_TIMELINE_GRP")
					.addTheme(THM_TIMELINE_ITEM_PADDING_BOTTOM).vcl(VisualControlType.VCL_WRAPPER).end()
					.addTheme(THM_TIMELINE_ITEM_BOLD).vcl(VisualControlType.INPUT_FIELD).end().end().build();

			return FRM_TIMELINE;

		} catch (Exception e) {
			System.out.println(e);
			return null;
		}

	}

	// @Test
	public void v7DetailsView() {

		initItem();
		/* Themes and frames */

		try {

			DataType dType = new DataType("DTT_IMAGE");
			Attribute attribute = new Attribute("PRI_IMAGE_URL", "Image Url", dType);
			Question companyImageQuestion = new Question("QUE_COMPANY_LOGO_GRP", "Company logo", attribute);
			companyImageQuestion.setReadonly(true);
			Ask compnayImageAsk = new Ask(companyImageQuestion);
			compnayImageAsk.setReadonly(true);

			Ask[] askList = new Ask[3];
			// Ask summaryAsk = getCompanySummaryAsk();
			// Ask detailViewAsk = getCompanyDetailAsk();
			Ask summaryAsk = getPersonSummaryAsk();
			Ask detailViewAsk = getPersonDetailAsk();
			Ask docViewAsk = getPersonDocumentsAsk();
			askList[0] = summaryAsk;
			askList[1] = detailViewAsk;
			askList[2] = docViewAsk;

			// THM_DETAIL_VIEW_BODY
			Theme THM_DETAIL_VIEW_BODY = Theme.builder("THM_DETAIL_VIEW_BODY").addAttribute().flex(15).padding(10)
					.justifyContent("flex-start").end()
					// .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
					.build();

			/* No need already exists THM_JUSTIFY_CONTENT_FLEX_START */

			Theme THM_JUSTIFY_CONTENT_FLEX_START = Theme.builder("THM_JUSTIFY_CONTENT_FLEX_START").addAttribute()
					.justifyContent("flex-start").end().build();

			/* Already exist THM_SCROLL_VERTICAL */
			Theme THM_SCROLL_VERTICAL = Theme.builder("THM_SCROLL_VERTICAL").addAttribute().overflowY("auto").end()
					.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

			Frame3 FRM_PERSON_DETAIL = getDetailFrame("FRM_PERSON_DETAIL", rules.getUser().getCode(),
					"QUE_TEST_PERSON_DETAIL_GRP");

			// Frame3 FRM_PERSON_DOCUMENTS = getDetailFrame("FRM_PERSON_DOCUMENTS",
			// rules.getUser().getCode(),"QUE_TEST_PERSON_DOC_GRP");
			Frame3 FRM_PERSON_DOCUMENTS = Frame3.clone(FRM_PERSON_DETAIL);
			FRM_PERSON_DOCUMENTS.setCode("FRM_PERSON_DOCUMENTS");
			FRM_PERSON_DOCUMENTS.setQuestionCode("QUE_TEST_PERSON_DOC_GRP");
			FRM_PERSON_DOCUMENTS.getQuestionGroup().setCode("QUE_TEST_PERSON_DOC_GRP");

			System.out.println("Copying the frames");
			Frame3 ss = Frame3.clone(FRM_PERSON_DOCUMENTS);

			ss.setCode("HELLO");
			System.out.println("Copied From " + FRM_PERSON_DOCUMENTS.getCode());
			System.out.println("Copied to" + ss.getCode());

			Frame3 FRM_DETAIL_VIEW_BODY = Frame3.builder("FRM_DETAIL_VIEW_BODY")
					.addTheme(THM_SCROLL_VERTICAL, ThemePosition.WRAPPER).end()
					.addTheme(THM_DETAIL_VIEW_BODY, ThemePosition.WRAPPER).end()
					.addTheme(THM_JUSTIFY_CONTENT_FLEX_START, ThemePosition.CENTRE).end()
					.addFrame(getTopFrame("FRM_PERSON_SUMMARY", rules.getUser().getCode(),
							"QUE_DETAIL_VIEW_TOP_SUMMARY_GRP"), FramePosition.CENTRE)
					.end().addFrame(FRM_PERSON_DETAIL, FramePosition.CENTRE).end()
					.addFrame(FRM_PERSON_DOCUMENTS, FramePosition.CENTRE).end().build();

			Frame3 FRM_ROOT = Frame3.builder("FRM_ROOT")

					.addFrame(FRM_DETAIL_VIEW_BODY).end().build();

			/* end */
			Set<QDataAskMessage> set = new HashSet<QDataAskMessage>();

			Map<String, ContextList> contextListMap = new HashMap<String, ContextList>();

			Map<String, QDataAskMessage> virtualAskMap = new HashMap<String, QDataAskMessage>();
			virtualAskMap.put(askList[0].getQuestionCode(), new QDataAskMessage(askList[0]));
			virtualAskMap.put(askList[1].getQuestionCode(), new QDataAskMessage(askList[1]));
			virtualAskMap.put(askList[2].getQuestionCode(), new QDataAskMessage(askList[2]));

			QDataBaseEntityMessage msgg = new QDataBaseEntityMessage(rules.getUser());
			msgg.setToken(userToken.getToken());
			msgg.setReplace(true);

			VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msgg));

			QDataBaseEntityMessage msg = FrameUtils2.toMessage(FRM_ROOT, serviceToken, set, contextListMap,
					virtualAskMap);

			msg.setToken(userToken.getToken());
			msg.setReplace(true);

			/* send message */
			System.out.println("Sending Asks");
			for (QDataAskMessage item : set) {

				item.setToken(userToken.getToken());

				String json = JsonUtils.toJson(item);
				VertxUtils.writeMsg("webcmds", json);

			}

			/* we publish the virtual ask with child asks */

			VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Frame3 getTopPicture(String name, String target) {

		try {

			Validation validation = new Validation("VLD_IMAGE_URL", "Text", ".*");
			ValidationList validationsList = new ValidationList();
			List<Validation> validations = new ArrayList();
			validations.add(validation);
			validationsList.setValidationList(validations);
			DataType dtt_image = new DataType("DTT_IMAGE", validationsList, "Image", "");

			Theme THM_DETAIL_VIEW_INPUT_FIELD = Theme.builder("THM_DETAIL_VIEW_INPUT_FIELD").addAttribute()
					.backgroundColor("#ddd").end().build();

			/* Exist THM_FORM_IMAGE_RESTRICTIONS */
			Theme THM_TEST_FORM_IMAGE_RESTRICTIONS = Theme.builder("THM_TEST_FORM_IMAGE_RESTRICTIONS").addAttribute()
					.showName(false).maxNumberOfFiles(1).end().build();

			// THM_DETAIL_VIEW_IMAGE_SIZE
			Theme THM_DETAIL_VIEW_IMAGE_SIZE = Theme.builder("THM_DETAIL_VIEW_IMAGE_SIZE").addAttribute().fit("contain")
					.imageHeight(120).imageWidth(120).end().build();

			Frame3 FRM_PROFILE_PICTURE_GRP = Frame3.builder(name)
					.addTheme("THM_PADDING_RIGHT_10", ThemePosition.WRAPPER, serviceToken).end()
					.question("QUE_DETAIL_VIEW_IMAGE_GRP").targetAlias(target).addTheme(THM_DETAIL_VIEW_INPUT_FIELD)
					.vcl(VisualControlType.INPUT_FIELD).dataType(dtt_image).end()
					.addTheme(THM_TEST_FORM_IMAGE_RESTRICTIONS).vcl(VisualControlType.VCL_INPUT).end()
					.addTheme(THM_DETAIL_VIEW_IMAGE_SIZE).vcl(VisualControlType.INPUT_SELECTED).end().end()

					.build();

			return FRM_PROFILE_PICTURE_GRP;
		} catch (Exception e) {

		}
		return null;
	}

	public Frame3 getTopSummaryContent(String name, String target, String questionCode) {

		try {

			/* Already exists THM_JUSTIFY_CONTENT_FLEX_START */
			Theme THM_CONTEXT_SUMMARY_CONTENT = Theme.builder("THM_CONTEXT_SUMMARY_CONTENT").addAttribute()
					.alignItems("flex-start").end().build();

			/* THM_WIDTH_DYNAMIC */
			Theme THM_INPUT = Theme.builder("THM_INPUT").addAttribute().dynamicWidth(true).end().build();

			/* THM_TITLE */
			Theme THM_TITLE = Theme.builder("THM_TITLE").addAttribute().bold(true).size("lg").textAlign("left").end()
					.build();

			// FRM_DETAIL_VIEW_TOP_SUMMARY
			Frame3 FRM_CONTEXT_SUMMARY_CONTENT = Frame3.builder(name).addTheme(THM_CONTEXT_SUMMARY_CONTENT).end()
					.question(questionCode).targetAlias(target).addTheme(THM_INPUT).vcl(VisualControlType.VCL_INPUT)
					.end().addTheme(THM_TITLE).vcl(VisualControlType.INPUT_FIELD).end().end().build();
			BeanUtilsBean odd = BeanUtilsBean.getInstance();

			return FRM_CONTEXT_SUMMARY_CONTENT;

		} catch (Exception e) {

			return null;
		}
	}

	public Frame3 getTopFrame(String name, String target, String questionCode) {

		// THM_DETAIL_VIEW_CARD_WRAPPER
		Theme THM_CONTEXT_SUMMARY = Theme.builder("THM_CONTEXT_SUMMARY").addAttribute().backgroundColor("#faf9fa")
				.justifyContent("flex-start").flexShrink(0).flexBasis("auto").flexGrow(0).height("initial")
				.marginBottom(20).padding(10).maxWidth(700).end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

		/* Theme for Question for profile picture frame */
		// THM_DETAIL_VIEW_IMAGE_FRAME
		Theme THM_CONTEXT_SUMMARY_IMAGE_FRAME = Theme.builder("THM_CONTEXT_SUMMARY_IMAGE_FRAME").addAttribute()
				.alignItems("flex-start").justifyContent("flex-start").flex(2).flexShrink(0).flexBasis("auto")
				.flexGrow(0).width("initial").end().addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
				.build();

		// THM_DETAIL_VIEW_CARD_CONTENT
		Theme THM_CONTEXT_SUMMARY_CONTENT_FRAME = Theme.builder("THM_CONTEXT_SUMMARY_CONTENT_FRAME").addAttribute()
				.width("100%").paddingLeft(10).end().addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
				.build();

		try {

			/* ******end******* */
			Frame3 FRM_DETAIL_VIEW_SUMMARY = Frame3.builder(name).addTheme(THM_CONTEXT_SUMMARY, ThemePosition.WRAPPER)
					.end().addTheme(THM_CONTEXT_SUMMARY_IMAGE_FRAME, ThemePosition.WEST).end()
					.addTheme(THM_CONTEXT_SUMMARY_CONTENT_FRAME, ThemePosition.CENTRE).end()
					.addTheme(THM_SHADOW, ThemePosition.WRAPPER).end()
					.addFrame(getTopPicture("FRM_PROFILE_PICTURE_GRP", target), FramePosition.WEST).end()
					.addFrame(getTopSummaryContent("FRM_CONTEXT_SUMMARY_CONTENT", target, questionCode),
							FramePosition.CENTRE)
					.end().addFrame(getControls(), FramePosition.EAST).end().build();

			return FRM_DETAIL_VIEW_SUMMARY;

		} catch (Exception e) {
			System.out.println(e);
			return null;
		}

	}

	public Frame3 getDetailFrame(String name, String target, String questionCode) {

		/* Already added THM_LABEL */
		Theme THM_LABEL = Theme.builder("THM_LABEL").addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true).end()
				.addAttribute().bold(true).end().build();

		/* Already Exists THM_FORM_BEHAVIOUR_GENNY */
		Theme THM_FORM_BEHAVIOUR = Theme.builder("THM_FORM_BEHAVIOUR").addAttribute().backgroundColor("none").end()
				.addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_LABEL, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_REQUIRED, true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_ICON, true).end().build();

		/*** THM_DETAIL_VIEW_CARD_CONTENT */
		Theme THM_CONTEXT_SUMMARY_CONTENT_FRAME = Theme.builder("THM_CONTEXT_SUMMARY_CONTENT_FRAME").addAttribute()
				.width("100%").paddingLeft(10).end().addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
				.build();

		// THM_DETAIL_VIEW_CONTENT_WRAPPER
		Theme THM_FORM_CONTENT_WRAPPER = Theme.builder("THM_FORM_CONTENT_WRAPPER").addAttribute().borderTopWidth(2)
				.borderStyle("solid").borderColor("#ddd").end().build();
		// THM_DETAIL_VIEW_VCL_WRAPPER
		Theme THM_VCL_WRAPPER = Theme.builder("THM_VCL_WRAPPER").addAttribute().paddingY(5).end().build();

		/* Unnecessary */
		Theme THM_DETAIL_VIEW_INPUT_FIELD = Theme.builder("THM_DETAIL_VIEW_INPUT_FIELD").addAttribute()
				.backgroundColor("#ddd").end().build();

		/* THM_DETAIL_VIEW_CARD_WRAPPER */
		Theme THM_CONTEXT_SUMMARY = Theme.builder("THM_CONTEXT_SUMMARY_1").addAttribute().backgroundColor("#faf9fa")
				.justifyContent("flex-start").marginBottom(20).padding(10).flexShrink(0).flexBasis("auto").flexGrow(0)
				.height("initial").maxWidth(700).end().addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
				.build();

		Validation validation = new Validation("VLD_ANYTHING", "Anything", ".*");
		ValidationList validationsList = new ValidationList();
		List<Validation> validations = new ArrayList();
		validations.add(validation);
		validationsList.setValidationList(validations);
		DataType dtt_upload = new DataType("DTT_UPLOAD", validationsList, "Upload", "");

		try {

			/* ******end******* */
			// FRM_DETAIL_VIEW_CARD
			Frame3 FRM_DETAIL_VIEW_SUMMARY = Frame3.builder(name).addTheme(THM_CONTEXT_SUMMARY, ThemePosition.WRAPPER)
					.end().addTheme(THM_SHADOW, ThemePosition.WRAPPER).end().question(questionCode).targetAlias(target)
					.addTheme(THM_DETAIL_VIEW_INPUT_FIELD).vcl(VisualControlType.INPUT_FIELD).dataType(dtt_upload).end()
					.addTheme(THM_LABEL).vcl(VisualControlType.VCL_LABEL).end().addTheme(THM_FORM_BEHAVIOUR)
					.vcl(VisualControlType.GROUP).end().addTheme(THM_CONTEXT_SUMMARY_CONTENT_FRAME)
					.vcl(VisualControlType.GROUP_WRAPPER).end().addTheme(THM_FORM_CONTENT_WRAPPER)
					.vcl(VisualControlType.GROUP_CONTENT_WRAPPER).end().addTheme(THM_VCL_WRAPPER)
					.vcl(VisualControlType.VCL_WRAPPER).end()

					.end().build();

			return FRM_DETAIL_VIEW_SUMMARY;

		} catch (Exception e) {
			System.out.println(e);
			return null;
		}

	}

	public Ask getStudentAsk() {

		GennyToken serviceToken = getToken(realm, "service", "Service User", "service");

		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
		TableUtils tableUtils = new TableUtils(beUtils);

		SearchEntity searchBE = new SearchEntity("SBE_SEARCH", "Search")
				.addFilter("PRI_CODE", SearchEntity.StringFilter.EQUAL, "APP_APPLICATION_THREE")
				.addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC).addColumn("PRI_INTERN_NAME", "Student Name")
				.addColumn("PRI_EDU_PROVIDER_NAME", "Edu Provider").addColumn("PRI_INTERN_STUDENT_ID", "Student ID")
				.addColumn("PRI_INTERN_EMAIL", "Email").addColumn("PRI_INTERN_MOBILE", "Mobile")
				.addColumn("PRI_INDUSTRY", "Industry").addColumn("PRI_OCCUPATION", "Occupation").setPageStart(0)
				.setPageSize(10);

		QDataBaseEntityMessage msg = tableUtils.fetchSearchResults(searchBE, serviceToken);
		Map<String, String> columns = tableUtils.getTableColumns(searchBE);

		List<Object> belist = new ArrayList<>(Arrays.asList(msg.getItems()));

		List<BaseEntity> results = (List<BaseEntity>) (List) belist;

		List<Ask> asks = TableUtils.generateQuestions(serviceToken, beUtils, results, columns, "PRJ_INTERNMATCH");

		Ask myAsk = asks.get(0);

		// QUE_DETAIL_VIEW_TOP_SUMMARY_GRP
		myAsk.setQuestionCode("QUE_TEST_INTERN_SUMMARY_GRP");
		myAsk.setName("Student Details");

		return myAsk;
	}

	public Ask internshipTopCard() {
		GennyToken serviceToken = getToken(realm, "service", "Service User", "service");

		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
		TableUtils tableUtils = new TableUtils(beUtils);

		SearchEntity searchBE = new SearchEntity("SBE_SEARCH", "Search")
				.addFilter("PRI_CODE", SearchEntity.StringFilter.EQUAL, "BEG_INTERNSHIP_ONE")
				.addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC).addColumn("PRI_NAME", "Name")
				.addColumn("PRI_HOST_COMPANY_NAME", "Host Company").setPageSize(10);

		QDataBaseEntityMessage msg = tableUtils.fetchSearchResults(searchBE, serviceToken);
		Map<String, String> columns = tableUtils.getTableColumns(searchBE);

		List<Object> belist = new ArrayList<>(Arrays.asList(msg.getItems()));

		List<BaseEntity> results = (List<BaseEntity>) (List) belist;

		List<Ask> asks = TableUtils.generateQuestions(serviceToken, beUtils, results, columns, "PRJ_INTERNMATCH");

		Ask myAsk = asks.get(0);
		myAsk.setQuestionCode("QUE_TEST_INTERNSHIP_SUMMARY_GRP");

		return myAsk;

	}

	public Ask getInternshipDetails() {
		GennyToken serviceToken = getToken(realm, "service", "Service User", "service");

		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
		TableUtils tableUtils = new TableUtils(beUtils);

		SearchEntity searchBE = new SearchEntity("SBE_SEARCH", "Search")
				.addFilter("PRI_CODE", SearchEntity.StringFilter.EQUAL, "BEG_INTERNSHIP_ONE")
				.addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC)
				.addColumn("PRI_HOST_COMPANY_NAME", "Host Company").addColumn("PRI_ADDRESS_FULL", "Address")
				.addColumn("PRI_START_DATE", "Starts").addColumn("LNK_INTERNSHIP_DURATION", "Duration")
				.addColumn("PRI_INTERNSHIP_DESCRIPTION", "Internship Description")
				.addColumn("PRI_POSITION_DESCRIPTION", "Position Description")
				.addColumn("PRI_INTERNSHIP_SKILLS", "Prefered Skills")
				.addColumn("PRI_INTERNSHIP_LEARNING_OUTCOMES", "Learning Outcome").addColumn("PRI_SOFTWARE", "Software")
				.addColumn("PRI_CREATOR_NAME", "Representative").addColumn("PRI_EMAIL", "Representative Email")
				.addColumn("PRI_MOBILE", "Representative Mobile").setPageStart(0).setPageSize(10);

		QDataBaseEntityMessage msg = tableUtils.fetchSearchResults(searchBE, serviceToken);
		Map<String, String> columns = tableUtils.getTableColumns(searchBE);

		List<Object> belist = new ArrayList<>(Arrays.asList(msg.getItems()));

		List<BaseEntity> results = (List<BaseEntity>) (List) belist;

		List<Ask> asks = TableUtils.generateQuestions(serviceToken, beUtils, results, columns, "PRJ_INTERNMATCH");

		Ask myAsk = asks.get(0);

		// QUE_DETAIL_VIEW_TOP_SUMMARY_GRP
		myAsk.setQuestionCode("QUE_TEST_INTERNSHIP_DETAILS_GRP");
		myAsk.setName("Internship Details");

		return myAsk;

	}

	public Ask getApplicationDetailAsk() {

		GennyToken serviceToken = getToken(realm, "service", "Service User", "service");

		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
		TableUtils tableUtils = new TableUtils(beUtils);

		SearchEntity searchBE = new SearchEntity("SBE_SEARCH", "Search")
				.addFilter("PRI_CODE", SearchEntity.StringFilter.EQUAL, "APP_APPLICATION_THREE")
				.addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC).addColumn("PRI_INTERNSHIP_TITLE", "Title")
				.addColumn("PRI_INTERNSHIP_DESCRIPTION", "Description").addColumn("PRI_INTERNSHIP_DURATION", "Duration")
				.addColumn("PRI_HOST_COMPANY_NAME", "Host Company").setPageStart(0).setPageSize(10);

		QDataBaseEntityMessage msg = tableUtils.fetchSearchResults(searchBE, serviceToken);
		Map<String, String> columns = tableUtils.getTableColumns(searchBE);

		List<Object> belist = new ArrayList<>(Arrays.asList(msg.getItems()));

		List<BaseEntity> results = (List<BaseEntity>) (List) belist;

		List<Ask> asks = TableUtils.generateQuestions(serviceToken, beUtils, results, columns, "PRJ_INTERNMATCH");

		Ask myAsk = asks.get(0);

		// QUE_DETAIL_VIEW_TOP_SUMMARY_GRP
		myAsk.setQuestionCode("QUE_TEST_INTERNSHIP_SUMMARY_GRP");
		myAsk.setName("Internship Details");

		return myAsk;
	}

	public Ask getApplicationInternshipDetailAsk() {

		GennyToken serviceToken = getToken(realm, "service", "Service User", "service");

		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
		TableUtils tableUtils = new TableUtils(beUtils);

		SearchEntity searchBE = new SearchEntity("SBE_SEARCH", "Search")
				.addFilter("PRI_CODE", SearchEntity.StringFilter.EQUAL, "APP_APPLICATION_THREE")
				.addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC).addColumn("PRI_INTERNSHIP_TITLE", "Title")
				.addColumn("PRI_INTERN_NAME", "Student").setPageStart(0).setPageSize(10);

		QDataBaseEntityMessage msg = tableUtils.fetchSearchResults(searchBE, serviceToken);
		Map<String, String> columns = tableUtils.getTableColumns(searchBE);

		List<Object> belist = new ArrayList<>(Arrays.asList(msg.getItems()));

		List<BaseEntity> results = (List<BaseEntity>) (List) belist;

		List<Ask> asks = TableUtils.generateQuestions(serviceToken, beUtils, results, columns, "PRJ_INTERNMATCH");

		Ask myAsk = asks.get(0);

		// QUE_DETAIL_VIEW_TOP_SUMMARY_GRP
		myAsk.setQuestionCode("QUE_TEST_APPLICATION_SUMMARY_GRP");
		myAsk.setName("Application");

		return myAsk;
	}

	public Ask getPersonSummaryAsk() {

		GennyToken serviceToken = getToken(realm, "service", "Service User", "service");

		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
		TableUtils tableUtils = new TableUtils(beUtils);

		SearchEntity searchBE = new SearchEntity("SBE_SEARCH", "Search")
				.addFilter("PRI_CODE", SearchEntity.StringFilter.EQUAL, rules.getUser().getCode())
				.addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC).addColumn("PRI_FIRSTNAME", "Name")
				.addColumn("PRI_LASTNAME", "Name").setPageStart(0).setPageSize(10);

		QDataBaseEntityMessage msg = tableUtils.fetchSearchResults(searchBE, serviceToken);
		Map<String, String> columns = tableUtils.getTableColumns(searchBE);

		List<Object> belist = new ArrayList<>(Arrays.asList(msg.getItems()));

		List<BaseEntity> results = (List<BaseEntity>) (List) belist;

		List<Ask> asks = TableUtils.generateQuestions(serviceToken, beUtils, results, columns, "PRJ_INTERNMATCH");

		Ask myAsk = asks.get(0);

		// QUE_DETAIL_VIEW_TOP_SUMMARY_GRP
		myAsk.setQuestionCode("QUE_TEST_PERSON_SUMMARY_GRP");

		return myAsk;
	}

	public Ask getPersonDocumentsAsk() {

		GennyToken serviceToken = getToken(realm, "service", "Service User", "service");

		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
		TableUtils tableUtils = new TableUtils(beUtils);

		SearchEntity searchBE = new SearchEntity("SBE_SEARCH", "Search")
				.addFilter("PRI_CODE", SearchEntity.StringFilter.EQUAL, rules.getUser().getCode())
				.addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC).addColumn("PRI_RESUME", "Resume")
				.setPageStart(0).setPageSize(10);

		QDataBaseEntityMessage msg = tableUtils.fetchSearchResults(searchBE, serviceToken);
		Map<String, String> columns = tableUtils.getTableColumns(searchBE);

		List<Object> belist = new ArrayList<>(Arrays.asList(msg.getItems()));

		List<BaseEntity> results = (List<BaseEntity>) (List) belist;

		List<Ask> asks = TableUtils.generateQuestions(serviceToken, beUtils, results, columns, "PRJ_INTERNMATCH");

		Ask myAsk = asks.get(0);
		myAsk.setQuestionCode("QUE_TEST_PERSON_DOC_GRP");
		myAsk.setName("Documents");

		return myAsk;
	}

	public Ask getPersonDetailAsk() {

		GennyToken serviceToken = getToken(realm, "service", "Service User", "service");

		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);

		TableUtils tableUtils = new TableUtils(beUtils);

		SearchEntity searchBE = new SearchEntity("SBE_SEARCH", "Search")
				.addFilter("PRI_CODE", SearchEntity.StringFilter.EQUAL, rules.getUser().getCode())
				.addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC).addColumn("PRI_EMAIL", "Email")
				.addColumn("PRI_MOBILE", "Phone").addColumn("PRI_ADDRESS_FULL", "Address")
				.addColumn("PRI_GENDER", "Gender").setPageStart(0).setPageSize(10);

		QDataBaseEntityMessage msg = tableUtils.fetchSearchResults(searchBE, serviceToken);
		Map<String, String> columns = tableUtils.getTableColumns(searchBE);

		List<Object> belist = new ArrayList<>(Arrays.asList(msg.getItems()));

		List<BaseEntity> results = (List<BaseEntity>) (List) belist;

		List<Ask> asks = TableUtils.generateQuestions(serviceToken, beUtils, results, columns, "PRJ_INTERNMATCH");

		Ask myAsk = asks.get(0);
		myAsk.setQuestionCode("QUE_TEST_PERSON_DETAIL_GRP");
		myAsk.setName("Details");

		return myAsk;
	}

	public Ask getCompanySummaryAsk() {

		GennyToken serviceToken = getToken(realm, "service", "Service User", "service");

		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
		TableUtils tableUtils = new TableUtils(beUtils);

		SearchEntity searchBE = new SearchEntity("SBE_SEARCH", "Search")
				.addFilter("PRI_CODE", SearchEntity.StringFilter.EQUAL, rules.getUserCompany().getCode())
				.addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC).addColumn("PRI_NAME", "Name").setPageStart(0)
				.setPageSize(10);

		QDataBaseEntityMessage msg = tableUtils.fetchSearchResults(searchBE, serviceToken);
		Map<String, String> columns = tableUtils.getTableColumns(searchBE);

		List<Object> belist = new ArrayList<>(Arrays.asList(msg.getItems()));

		List<BaseEntity> results = (List<BaseEntity>) (List) belist;

		List<Ask> asks = TableUtils.generateQuestions(serviceToken, beUtils, results, columns, "PRJ_INTERNMATCH");

		Ask myAsk = asks.get(0);
		myAsk.setQuestionCode("QUE_TEST_PERSON_SUMMARY_GRP");

		return myAsk;
	}

	public Ask getCompanyDetailAsk() {

		GennyToken serviceToken = getToken(realm, "service", "Service User", "service");

		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
		TableUtils tableUtils = new TableUtils(beUtils);

		SearchEntity searchBE = new SearchEntity("SBE_SEARCH", "Search")

				.addFilter("PRI_CODE", SearchEntity.StringFilter.EQUAL, rules.getUserCompany().getCode())
				.addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC).addColumn("PRI_EMAIL", "Email")
				.addColumn("PRI_MOBILE", "LandLine").addColumn("PRI_ADDRESS_FULL", "Address")
				.addColumn("PRI_ABN", "Company ABN").addColumn("PRI_ACN", "Company ACN").setPageStart(0)
				.setPageSize(10);

		QDataBaseEntityMessage msg = tableUtils.fetchSearchResults(searchBE, serviceToken);
		Map<String, String> columns = tableUtils.getTableColumns(searchBE);

		List<Object> belist = new ArrayList<>(Arrays.asList(msg.getItems()));

		List<BaseEntity> results = (List<BaseEntity>) (List) belist;

		List<Ask> asks = TableUtils.generateQuestions(serviceToken, beUtils, results, columns, "PRJ_INTERNMATCH");

		Ask myAsk = asks.get(0);
		myAsk.setQuestionCode("QUE_TEST_PERSON_DETAIL_GRP");
		myAsk.setName("Details");

		return myAsk;
	}

	public Frame3 getControls() {

		Theme THM_DETAIL_VIEW_CONTROL = Theme.builder("THM_DETAIL_VIEW_CONTROL").addAttribute().margin(5).height("70%")
				.justifyContent("center").backgroundColor("#233a4e").padding(5).end().build();

		Theme THM_CONTROLS_COLOR = Theme.builder("THM_CONTROLS_COLOR").addAttribute().color("#ffffff").size("xxs").end()
				.build();

		Frame3 frame = Frame3.builder("FRM_DETAIL_VIEW_HEADER").question("QUE_DETAIL_VIEW_CONTROL_GRP")
				.addTheme(THM_DETAIL_VIEW_CONTROL).vcl(VisualControlType.INPUT_WRAPPER).end()
				.addTheme(THM_CONTROLS_COLOR).vcl(VisualControlType.INPUT_FIELD).end().end().build();
		return frame;
	}

	// @Test
	public void beFetchTest() {

		String a = "abcdef";
		String b = "a";
		String c = a.replaceAll(b, "c");
		System.out.println(c);

	}

	// @Test
	public void eventProcessTest() {

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

		QEventMessage msg = new QEventMessage("EVT_MSG", "AUTH_INIT");
		QEventMessage msg1 = new QEventMessage("EVT_MSG", "AUTH_INIT1");
		msg.getData().setValue("NEW_SESSION");

		System.out.println("Hello");
		GennyKieSession gks = GennyKieSession.builder(serviceToken, true).addJbpm("userSession.bpmn")
				.addJbpm("userValidation.bpmn").addJbpm("userLifecycle.bpmn").addJbpm("auth_init.bpmn")
				.addJbpm("showDashboard.bpmn").addDrl("DEFAULT_EVENT.drl").addToken(userToken).build();
		System.out.println("Hello");

		gks.start();
		gks.injectSignal("newSession", msg);
		gks.advanceSeconds(5, true);
		// gks.injectSignal("event",msg1);

		gks.advanceSeconds(5, true);

	}

	// @Test
	public void timeCHeck() {

		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		System.out.println(userToken.getExpiryDateTime());

		LocalDateTime expTime = userToken.getExpiryDateTime();

		ZonedDateTime ldtZoned = expTime.atZone(ZoneId.systemDefault());
		ZonedDateTime utcZoned = ldtZoned.withZoneSameInstant(ZoneId.of("UTC"));
		System.out.println(utcZoned.toOffsetDateTime());

		LocalDateTime expTime1 = LocalDateTime.now();

		ZonedDateTime ldtZoned1 = expTime1.atZone(ZoneId.systemDefault());
		ZonedDateTime utcZoned1 = ldtZoned1.withZoneSameInstant(ZoneId.of("UTC"));
		System.out.println(utcZoned1.toOffsetDateTime());

	}

	// @Test
	public void userSessionTestToRunnningService() {

		// VertxUtils.cachedEnabled = true; // don't try and use any local services

		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");

		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		Answer ans = new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_NAME", "Safal Shrestha");
		QDataAnswerMessage ansMsg = new QDataAnswerMessage(ans);
		ansMsg.setToken(userToken.getToken());

		VertxUtils.writeMsg("data", JsonUtils.toJson(ansMsg));

	}

	// @Test
	public void SendData() {
		GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");
		QRules rules = getQRules(userToken);
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

		Answer ans = new Answer(userToken.getUserCode(), userToken.getUserCode(), "PRI_NAME", "Safal Shrestha");
		QDataAnswerMessage ansMsg = new QDataAnswerMessage(ans);
		ansMsg.setToken(userToken.getToken());

		QEventMessage msg = new QEventMessage("EVT_MSG", "AUTH_INIT");

		GennyKieSession gks = GennyKieSession.builder(serviceToken).addJbpm("userSession.bpmn")
				.addJbpm("userValidation.bpmn").addJbpm("userLifecycle.bpmn").addJbpm("bucketPage.bpmn")
				.addJbpm("showDashboard.bpmn").addJbpm("auth_init.bpmn").addJbpm("detailpage.bpmn").addDrl("Answer.drl")

				.addFact("rules", rules).addToken(userToken).addToken(serviceToken).build();

		gks.start();
		gks.injectSignal("newSession", msg);
		gks.advanceSeconds(5, true);

		gks.injectSignal("data", ansMsg);
		gks.advanceSeconds(5, true);

	}

	// @Test
	public void simpleTest() {
		QRules rules = GennyJbpmBaseTest.setupLocalService();
		rules.sendAllAttributes();

	}

}