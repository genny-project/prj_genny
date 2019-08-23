package life.genny.utils;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;

import life.genny.models.GennyToken;
import life.genny.qwanda.Ask;
import life.genny.qwanda.Context;
import life.genny.qwanda.ContextList;
import life.genny.qwanda.ContextType;
import life.genny.qwanda.Question;
import life.genny.qwanda.VisualControlType;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.datatype.DataType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.SearchEntity;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.validation.Validation;
import life.genny.qwanda.validation.ValidationList;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.test.GennyJbpmBaseTest;

public class TableUtilsTest {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	BaseEntityUtils beUtils = null;
	
	public TableUtilsTest(BaseEntityUtils beUtils) {
		this.beUtils = beUtils;
	}

	public void sendQuestions(SearchEntity searchBe, GennyToken gennyToken,  QDataBaseEntityMessage msg)
	{
		log.info("Search Results for "+searchBe.getCode()+" and user "+gennyToken.getUserCode()+" = "+msg); //use  QUE_TABLE_VIEW_TEST
		log.info("Search result items = "+msg.getReturnCount());
		if (msg.getReturnCount()>0) {
			BaseEntity result0 = msg.getItems()[0];
			log.info("Search first result = "+result0);
			if (msg.getReturnCount()>1) {
				BaseEntity result1 = msg.getItems()[1];
				log.info("Search second result = "+result1);
			}
		}
		
		// Show columns
		Map<String, String> columns = getTableColumns(searchBe);
		log.info(columns);
		
		List<QDataBaseEntityMessage> themeMsgList = new ArrayList<QDataBaseEntityMessage>();

		generateTableHeaderAsks(searchBe,themeMsgList);
		
		log.info("*** ThemeMsgList *****");
		log.info(themeMsgList);
		
	}
	
	
	public Map<String, String> getTableColumns(SearchEntity searchBe) {

		Map<String, String> columns = new HashMap<>();

		for (EntityAttribute ea : searchBe.getBaseEntityAttributes()) {
			String attributeCode = ea.getAttributeCode();
			String attributeName = ea.getAttributeName();
			if (attributeCode.startsWith("COL_")) {
				columns.put(attributeCode.split("COL_")[1], attributeName);
			} else if (attributeCode.startsWith("CAL_")) {
				columns.put(attributeCode.split("CAL_")[1], attributeName);
			} else if (attributeCode.startsWith("QUE_")) {
				columns.put(attributeCode, attributeName);
			}

		}

		log.info("the Columns is :: " + columns);
		return columns;
	}
	
	
	public QDataBaseEntityMessage  fetchSearchResults(SearchEntity searchBE, GennyToken gennyToken)
	{
		QDataBaseEntityMessage msg = new QDataBaseEntityMessage(new ArrayList<BaseEntity>());

		if (gennyToken == null) {
			log.error("GENNY TOKEN IS NULL!!! in getSearchResults");
			return msg;
		}
		searchBE.setRealm(gennyToken.getRealm());
		log.info("The search BE is :: " + JsonUtils.toJson(searchBE));

		if (VertxUtils.cachedEnabled) {
			List<BaseEntity> results = new ArrayList<BaseEntity>();
			results.add(createTestPerson(gennyToken,"The Phantom","kit.walker@phantom.bg"));
			results.add(createTestPerson(gennyToken,"Phantom Menace","menace43r@starwars.net"));
			results.add(createTestPerson(gennyToken,"The Phantom Ranger","phantom@rangers.com"));
			
			
			msg = new QDataBaseEntityMessage(results);
			return msg;
		}
		
		String jsonSearchBE = JsonUtils.toJson(searchBE);
		String resultJson;
		try {
			resultJson = QwandaUtils.apiPostEntity(GennySettings.qwandaServiceUrl + "/qwanda/baseentitys/search",
					jsonSearchBE, gennyToken.getToken());
			final BaseEntity[] items = new BaseEntity[0];
			final String parentCode = "GRP_ROOT";
			final String linkCode = "LINK";
			final Long total = 0L;

			if (resultJson == null) {
				msg = new QDataBaseEntityMessage(items, parentCode, linkCode, total);
				log.info("The result of getSearchResults was null  ::  " + msg);
			} else {
				try {
					msg = JsonUtils.fromJson(resultJson, QDataBaseEntityMessage.class);
					if (msg == null) {
						msg = new QDataBaseEntityMessage(items, parentCode, linkCode, total);
						log.info("The result of getSearchResults was null Exception ::  " + msg);
					} else {
						log.info("The result of getSearchResults was not null  ::  " + msg);
					}
				} catch (Exception e) {
					log.info("The result of getSearchResults was null Exception ::  " + msg);
					msg = new QDataBaseEntityMessage(items, parentCode, linkCode, total);
				}

			}

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return msg;	
	
	}

	static BaseEntity createTestPerson(GennyToken gennyToken,String name, String email)
	{
		String usercode = "PER_"+QwandaUtils.getNormalisedUsername(email);
		BaseEntity result1 = new BaseEntity(usercode,name);
		result1.setRealm(gennyToken.getRealm());
		
		return result1;
	}
	
	public Ask generateTableHeaderAsks(SearchEntity searchBe,List<QDataBaseEntityMessage> themeMsgList) {

		List<Ask> asks = new ArrayList<>();

		/* Validation for Search Attribute */
		Validation validation = new Validation("VLD_NON_EMPTY", "EmptyandBlankValues", "(?!^$|\\s+)");
		List<Validation> validations = new ArrayList<>();
		validations.add(validation);
		ValidationList searchValidationList = new ValidationList();
		searchValidationList.setValidationList(validations);

		Attribute eventAttribute = RulesUtils.attributeMap.get("PRI_SORT");
		Attribute questionAttribute = RulesUtils.attributeMap.get("QQQ_QUESTION_GROUP");

		/* get table columns */
		Map<String, String> columns = getTableColumns(searchBe);

		/* get vertical display theme */
		BaseEntity verticalTheme = beUtils.getBaseEntityByCode("THM_DISPLAY_VERTICAL");
		

		for (Map.Entry<String, String> column : columns.entrySet()) {

			String attributeCode = column.getKey();
			String attributeName = column.getValue();

			Attribute searchAttribute = new Attribute(attributeCode, attributeName,
					new DataType("Text", searchValidationList, "Text"));

			/* Initialize Column Header Ask group */
			Question columnHeaderQuestion = new Question("QUE_" + attributeCode + "_GRP", attributeName,
					questionAttribute, true);
			Ask columnHeaderAsk = new Ask(columnHeaderQuestion, beUtils.getGennyToken().getUserCode(), searchBe.getCode());

			/* creating ask for table header label-sort */
			Ask columnSortAsk = getAskForTableHeaderSort(searchBe, attributeCode, attributeName, eventAttribute,themeMsgList);

			/* creating Ask for table header search input */
			Question columnSearchQues = new Question("QUE_SEARCH_" + attributeCode, "Search " + attributeName + "..",
					searchAttribute, false);
			Ask columnSearchAsk = new Ask(columnSearchQues, beUtils.getGennyToken().getUserCode(), searchBe.getCode());

			/* adding label-sort & search asks to header-ask Group */
			List<Ask> tableColumnChildAsks = new ArrayList<>();
			tableColumnChildAsks.add(columnSortAsk);
			tableColumnChildAsks.add(columnSearchAsk);

			/* Convert List to Array */
			Ask[] tableColumnChildAsksArray = tableColumnChildAsks.toArray(new Ask[0]);

			/* set the child asks */
			columnHeaderAsk.setChildAsks(tableColumnChildAsksArray);

			/* set Vertical Theme to columnHeaderAsk */
			columnHeaderAsk = this.createVirtualContext(columnHeaderAsk, verticalTheme, ContextType.THEME,themeMsgList);
			asks.add(columnHeaderAsk);
		}

		/* Convert List to Array */
		Ask[] asksArray = asks.toArray(new Ask[0]);

		/*
		 * we create a table-header ask grp and set all the column asks as it's childAsk
		 */
		Question tableHeaderQuestion = new Question("QUE_TABLE_HEADER_GRP", "Table Header Question Group",
				questionAttribute, true);

		Ask tableHeaderAsk = new Ask(tableHeaderQuestion, beUtils.getGennyToken().getUserCode(), searchBe.getCode());
		tableHeaderAsk.setChildAsks(asksArray);

		return tableHeaderAsk;
	}
	
	public Ask createVirtualContext(Ask ask, BaseEntity theme, ContextType linkCode, List<QDataBaseEntityMessage> themeMsgList) {
		List<BaseEntity> themeList = new ArrayList<>();
		themeList.add(theme);
		return createVirtualContext(ask, themeList, linkCode, VisualControlType.VCL_INPUT,themeMsgList);
	}

	public Ask createVirtualContext(Ask ask, List<BaseEntity> themeList, ContextType linkCode, List<QDataBaseEntityMessage> themeMsgList) {
		return createVirtualContext(ask, themeList, linkCode, VisualControlType.VCL_INPUT,themeMsgList);
	}

	public Ask createVirtualContext(Ask ask, BaseEntity theme, ContextType linkCode,
			VisualControlType visualControlType, List<QDataBaseEntityMessage> themeMsgList) {
		List<BaseEntity> themeList = new ArrayList<>();
		themeList.add(theme);
		return createVirtualContext(ask, themeList, linkCode, visualControlType,themeMsgList);
	}

	public Ask createVirtualContext(Ask ask, BaseEntity theme, ContextType linkCode,
			VisualControlType visualControlType, Double weight, List<QDataBaseEntityMessage> themeMsgList) {
		List<BaseEntity> themeList = new ArrayList<>();
		themeList.add(theme);
		return createVirtualContext(ask, themeList, linkCode, visualControlType, weight,themeMsgList);
	}

	public Ask createVirtualContext(Ask ask, List<BaseEntity> themes, ContextType linkCode,
			VisualControlType visualControlType, List<QDataBaseEntityMessage> themeMsgList) {
		return createVirtualContext(ask, themes, linkCode, visualControlType, 2.0,themeMsgList);
	}

	/**
	 * Embeds the list of contexts (themes, icon) into an ask and also publishes the
	 * themes
	 *
	 * @param ask
	 * @param themes
	 * @param linkCode
	 * @param weight
	 * @return
	 */
	public Ask createVirtualContext(Ask ask, List<BaseEntity> themes, ContextType linkCode,
			VisualControlType visualControlType, Double weight, List<QDataBaseEntityMessage> themeMsgList) {

		List<Context> completeContext = new ArrayList<>();

		for (BaseEntity theme : themes) {
			Context context = new Context(linkCode, theme, visualControlType, weight);
			completeContext.add(context);

			/* publish the theme baseentity message */
			QDataBaseEntityMessage themeMsg = new QDataBaseEntityMessage(theme);
			themeMsgList.add(themeMsg);
		}

		ContextList contextList = ask.getContextList();
		if (contextList != null) {
			List<Context> contexts = contextList.getContexts();
			if (contexts.isEmpty()) {
				contexts = new ArrayList<>();
				contexts.addAll(completeContext);
			} else {
				contexts.addAll(completeContext);
			}
			contextList = new ContextList(contexts);
		} else {
			List<Context> contexts = new ArrayList<>();
			contexts.addAll(completeContext);
			contextList = new ContextList(contexts);
		}
		ask.setContextList(contextList);
		return ask;
	}
	
	private Ask getAskForTableHeaderSort(SearchEntity searchBe, String attributeCode, String attributeName,
			Attribute eventAttribute, List<QDataBaseEntityMessage> themeMsgList) {

		/* creating Ask for table header column sort */
		Question columnSortQues = new Question("QUE_SORT_" + attributeCode, attributeName, eventAttribute, false);
		Ask columnSortAsk = new Ask(columnSortQues, beUtils.getGennyToken().getUserCode(), searchBe.getCode());

		/* ADDING DEFAULT TABLE HEADER THEMES */

		/* showing the icon */
		BaseEntity sortIconBe = beUtils.getBaseEntityByCode("ICN_SORT");

		/* create visual baseentity for question with label */
		BaseEntity visualBaseEntity = beUtils.getBaseEntityByCode("THM_TABLE_HEADER_VISUAL_CONTROL");

		/* get the BaseEntity for wrapper context */
		BaseEntity horizontalWrapperBe = beUtils.getBaseEntityByCode("THM_DISPLAY_HORIZONTAL");

		/* get the theme for Label and Sort */
		BaseEntity headerLabelSortThemeBe = beUtils.getBaseEntityByCode("THM_TABLE_HEADER_SORT_THEME");

		/* set the contexts to the ask */
		createVirtualContext(columnSortAsk, horizontalWrapperBe, ContextType.THEME, VisualControlType.VCL_WRAPPER,themeMsgList);
		createVirtualContext(columnSortAsk, sortIconBe, ContextType.ICON, VisualControlType.VCL_ICON,themeMsgList);
		createVirtualContext(columnSortAsk, visualBaseEntity, ContextType.THEME, VisualControlType.VCL_INPUT,themeMsgList);
		createVirtualContext(columnSortAsk, headerLabelSortThemeBe, ContextType.THEME, VisualControlType.VCL_LABEL,themeMsgList);

		return columnSortAsk;
	}
}