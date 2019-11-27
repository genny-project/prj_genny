package life.genny.utils;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;

import com.google.gson.reflect.TypeToken;

import life.genny.models.Frame3;
import life.genny.models.GennyToken;
import life.genny.models.TableData;
import life.genny.models.Theme;
import life.genny.models.ThemeAttribute;
import life.genny.models.ThemeAttributeType;
import life.genny.models.ThemePosition;
import life.genny.qwanda.Answer;
import life.genny.qwanda.Ask;
import life.genny.qwanda.Context;
import life.genny.qwanda.ContextList;
import life.genny.qwanda.ContextType;
import life.genny.qwanda.Link;
import life.genny.qwanda.Question;
import life.genny.qwanda.VisualControlType;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.datatype.DataType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.entity.EntityQuestion;
import life.genny.qwanda.entity.SearchEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.message.QBulkMessage;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.validation.Validation;
import life.genny.qwanda.validation.ValidationList;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.rules.QRules;
import life.genny.test.GennyJbpmBaseTest;
import life.genny.utils.ContextUtils;

public class TableUtilsTest {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	static Integer MAX_SEARCH_HISTORY_SIZE = 10;
	static Integer MAX_SEARCH_BAR_TEXT_SIZE = 20;

	BaseEntityUtils beUtils = null;

	public TableUtilsTest(BaseEntityUtils beUtils) {
		this.beUtils = beUtils;
	}

	public static void performSearch(GennyToken serviceToken, BaseEntityUtils beUtils, final String searchBarCode,
			Answer answer) {
		TableUtilsTest tableUtils = new TableUtilsTest(beUtils);

		/* Perform a search bar search */
		String searchBarString = null;
		if (answer != null) {
			searchBarString = answer.getValue();
			// Clean up search Text
			searchBarString = searchBarString.trim();
			searchBarString = searchBarString.replaceAll("[^a-zA-Z0-9\\ ]", "");
			Integer max = searchBarString.length();
			Integer realMax = (max > MAX_SEARCH_BAR_TEXT_SIZE) ? MAX_SEARCH_BAR_TEXT_SIZE : max;
			searchBarString.substring(0, realMax);
			log.info("Search text = [" + searchBarString + "]");
		}

		/* Get the SearchBE */
		String sessionSearchCode = searchBarCode + "_" + beUtils.getGennyToken().getSessionCode();
		SearchEntity searchBE = VertxUtils.getObject(serviceToken.getRealm(), "", sessionSearchCode, SearchEntity.class,
				serviceToken.getToken());

		if (searchBE == null) {
			searchBE = VertxUtils.getObject(serviceToken.getRealm(), "", searchBarCode, SearchEntity.class,
					serviceToken.getToken());

			/* we need to set the searchBe's code to session Search Code */
			searchBE.setCode(sessionSearchCode);

			/*
			 * Save Session Search in cache , ideally this should be in OutputParam and
			 * saved to workflow
			 */
			VertxUtils.putObject(serviceToken.getRealm(), "", sessionSearchCode, searchBE, serviceToken.getToken());
		}

		log.info("search code coming from searchBE getCode  :: " + searchBE.getCode());

		/* fetch Session SearchBar List from User */
		BaseEntity user = VertxUtils.getObject(beUtils.getGennyToken().getRealm(), "",
				beUtils.getGennyToken().getUserCode(), BaseEntity.class, beUtils.getGennyToken().getToken());
		Type type = new TypeToken<List<String>>() {
		}.getType();
		List<String> defaultList = new ArrayList<String>();
		String defaultListString = JsonUtils.toJson(defaultList);
		String historyStr = user.getValue("PRI_SEARCH_HISTORY", defaultListString);
		List<String> searchHistory = JsonUtils.fromJson(historyStr, type);

		/* Add new SearchBarString to Session SearchBar List */
		/* look for existing search term and bring to front - slow */
		if (answer != null) { // no need to set history if no data sent
			int index = searchHistory.indexOf(searchBarString);
			if (index >= 0) {
				searchHistory.remove(index);
			}
			searchHistory.add(0, searchBarString);
			if (searchHistory.size() > MAX_SEARCH_HISTORY_SIZE) {
				searchHistory.remove(MAX_SEARCH_HISTORY_SIZE);
			}
			String newHistoryString = JsonUtils.toJson(searchHistory);
			Answer history = new Answer(beUtils.getGennyToken().getUserCode(), beUtils.getGennyToken().getUserCode(),
					"PRI_SEARCH_HISTORY", newHistoryString);
			beUtils.saveAnswer(history);
			log.info("Search History for " + beUtils.getGennyToken().getUserCode() + " = " + searchHistory.toString());
		} else {
			// so grab the latest search history
			if (!searchHistory.isEmpty()) {
				searchBarString = searchHistory.get(0);
			} else {
				searchBarString = ""; // fetch everything
			}
		}

		searchBE.addFilter("PRI_NAME", SearchEntity.StringFilter.LIKE, "%" + searchBarString + "%");

		// Send out Search Results

		QDataBaseEntityMessage msg = tableUtils.fetchSearchResults(searchBE, beUtils.getGennyToken());

		/* get the total count of the results */
		long totalResults = msg.getTotal();

		/* print the total */
		log.info("total count is  :: " + totalResults + "");
		Answer totalAnswer = new Answer(beUtils.getGennyToken().getUserCode(), searchBE.getCode(), "PRI_TOTAL",
				totalResults + "");
		beUtils.addAnswer(totalAnswer);

		beUtils.updateBaseEntity(searchBE, totalAnswer);

		Map<String, String> columns = tableUtils.getTableColumns(searchBE);

		VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg));

		// Now Send out Table Header Ask and Question
		TableData tableData = tableUtils.generateTableAsks(searchBE, beUtils.getGennyToken(), msg);
		Ask headerAsk = tableData.getAsk();
		Ask[] askArray = new Ask[1];
		askArray[0] = headerAsk;
		QDataAskMessage headerAskMsg = new QDataAskMessage(askArray);
		headerAskMsg.setToken(beUtils.getGennyToken().getToken());
		headerAskMsg.setReplace(true);
		// VertxUtils.writeMsg("webcmds", JsonUtils.toJson(headerAskMsg));

		// create virtual context

		// Now link the FRM_TABLE_HEADER to that new Question
		String headerAskCode = headerAsk.getQuestionCode();
		Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();
		QDataBaseEntityMessage msg2 = null;
		msg2 = TableUtilsTest.changeQuestion(searchBE, "FRM_TABLE_HEADER", headerAskCode, serviceToken,
				beUtils.getGennyToken(), askMsgs);
		msg2.setToken(beUtils.getGennyToken().getToken());
		msg2.setReplace(true);
		VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg2));

		QDataAskMessage[] askMsgArr = askMsgs.toArray(new QDataAskMessage[0]);
		if (askMsgArr.length > 0) {
			if (askMsgArr[0].getItems().length > 0) {
				ContextList contextList = askMsgArr[0].getItems()[0].getContextList();
				headerAskMsg.getItems()[0].setContextList(contextList);

				VertxUtils.writeMsg("webcmds", JsonUtils.toJson(headerAskMsg));

				askMsgs.clear();

				/* Now to display the rows */

				Type setType = new TypeToken<Set<QDataAskMessage>>() {
				}.getType();

				String askMsgs2Str = VertxUtils.getObject(beUtils.getGennyToken().getRealm(), "", "FRM_TABLE_CONTENT_ASKS",
						String.class, beUtils.getGennyToken().getToken());

				if (askMsgs2Str == null) {
					Frame3 frame = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_TABLE_CONTENT", Frame3.class,
							serviceToken.getToken());

					FrameUtils2.toMessage2(frame, serviceToken);
				}

				Set<QDataAskMessage> askMsgs2 = JsonUtils.fromJson(askMsgs2Str, setType);
				QDataAskMessage[] askMsg2Array = askMsgs2.stream().toArray(QDataAskMessage[]::new);
				ContextList rowsContextList = askMsg2Array[0].getItems()[0].getContextList();

				List<BaseEntity> rowList = Arrays.asList(msg.getItems());
				List<Ask> rowAsks = generateQuestions(beUtils.getGennyToken(), beUtils, rowList, columns,
						beUtils.getGennyToken().getUserCode());

				/* converting rowAsks list to array */
				Ask[] rowAsksArr = rowAsks.stream().toArray(Ask[]::new);

				/* Now send out the question rows and themes etc */

				/* Link row asks to a single ask: QUE_TEST_TABLE_RESULTS_GRP */
				Attribute questionAttribute = new Attribute("QQQ_QUESTION_GROUP", "link", new DataType(String.class));
				Question tableResultQuestion = new Question("QUE_TABLE_RESULTS_GRP", "Table Results Question Group",
						questionAttribute, true);
				Ask tableResultAsk = new Ask(tableResultQuestion, beUtils.getGennyToken().getUserCode(),
						beUtils.getGennyToken().getUserCode());
				tableResultAsk.setChildAsks(rowAsksArr);
				tableResultAsk.setContextList(rowsContextList);
				Set<QDataAskMessage> tableResultAskMsgs = new HashSet<QDataAskMessage>();

				tableResultAskMsgs.add(new QDataAskMessage(tableResultAsk));

				/* link single ask QUE_TEST_TABLE_RESULTS_GRP to FRM_TABLE_CONTENT ? */
				String tableResultAskCode = tableResultAsk.getQuestionCode();

				QDataBaseEntityMessage msg3 = null;
				msg3 = TableUtilsTest.changeQuestion(searchBE, "FRM_TABLE_CONTENT", tableResultAskCode, serviceToken,
						beUtils.getGennyToken(), tableResultAskMsgs);
				msg3.setToken(beUtils.getGennyToken().getToken());
				msg3.setReplace(true);

				for (QDataAskMessage askMsg : tableResultAskMsgs) {
					askMsg.setToken(beUtils.getGennyToken().getToken());
					// askMsg.getItems()[0] = headerAsk;
					askMsg.setReplace(true);
					VertxUtils.writeMsg("webcmds", JsonUtils.toJson(askMsg));
				}
				VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg3));

			}
		}

		/* need to send the footer question again here */
		Attribute totalAttribute = new Attribute("PRI_TOTAL_RESULTS", "link", new DataType(String.class));
		Attribute indexAttribute = new Attribute("PRI_INDEX", "link", new DataType(String.class));

		/* create total count ask */
		Question totalQuestion = new Question("QUE_TABLE_TOTAL_RESULT_COUNT", "Total Results", totalAttribute, true);

		Ask totalAsk = new Ask(totalQuestion, beUtils.getGennyToken().getUserCode(), searchBE.getCode());

		/* create index ask */
		Question indexQuestion = new Question("QUE_TABLE_PAGE_INDEX", "Page Number", indexAttribute, true);

		Ask indexAsk = new Ask(indexQuestion, beUtils.getGennyToken().getUserCode(), searchBE.getCode());

		/* collect the asks to bbe sent ouy */
		Set<QDataAskMessage> footerAskMsgs = new HashSet<QDataAskMessage>();
		footerAskMsgs.add(new QDataAskMessage(totalAsk));
		footerAskMsgs.add(new QDataAskMessage(indexAsk));

		/* publish the new asks with searchBe set as targetCode */
		for (QDataAskMessage footerAskMsg : footerAskMsgs) {
			footerAskMsg.setToken(beUtils.getGennyToken().getToken());
			footerAskMsg.setReplace(true);
			VertxUtils.writeMsg("webcmds", JsonUtils.toJson(footerAskMsg));
		}

		/* publishing the searchBE to frontEnd */
		QDataBaseEntityMessage searchBeMsg = new QDataBaseEntityMessage(searchBE);
		searchBeMsg.setToken(beUtils.getGennyToken().getToken());
		VertxUtils.writeMsg("webcmds", JsonUtils.toJson((searchBeMsg)));

	}

	public TableData generateTableAsks(SearchEntity searchBe, GennyToken gennyToken, QDataBaseEntityMessage msg) {

		log.info("Search Results for " + searchBe.getCode() + " and user " + gennyToken.getUserCode() + " = " + msg); // use
		// QUE_TABLE_VIEW_TEST
		log.info("Search result items = " + msg.getReturnCount());
		if (msg.getReturnCount() > 0) {
			BaseEntity result0 = msg.getItems()[0];
			log.info("Search first result = " + result0);
			if (msg.getReturnCount() > 1) {
				BaseEntity result1 = msg.getItems()[1];
				log.info("Search second result = " + result1);
			}
		}

		// Show columns
		Map<String, String> columns = getTableColumns(searchBe);
		log.info(columns);

		List<QDataBaseEntityMessage> themeMsgList = new ArrayList<QDataBaseEntityMessage>();

		Ask tableHeaderAsk = generateTableHeaderAsk(searchBe, themeMsgList);

		log.info("*** ThemeMsgList *****");
		log.info(themeMsgList);

		TableData tableData = new TableData(themeMsgList, tableHeaderAsk);
		return tableData;
	}

	public Map<String, String> getTableColumns(SearchEntity searchBe) {

		Map<String, String> columns = new LinkedHashMap<String, String>();
		List<EntityAttribute> cols = searchBe.getBaseEntityAttributes().stream().filter(x -> {
			return (x.getAttributeCode().startsWith("COL_") || x.getAttributeCode().startsWith("CAL_"));
		}).sorted(Comparator.comparing(EntityAttribute::getWeight)) // comparator - how you want to sort it
				.collect(Collectors.toList()); // collector - what you want to collect it to

		for (EntityAttribute ea : cols) {
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

	public QDataBaseEntityMessage fetchSearchResults(SearchEntity searchBE, GennyToken gennyToken) {
		QDataBaseEntityMessage msg = new QDataBaseEntityMessage(new ArrayList<BaseEntity>());

		if (gennyToken == null) {
			log.error("GENNY TOKEN IS NULL!!! in getSearchResults");
			return msg;
		}
		searchBE.setRealm(gennyToken.getRealm());
		log.info("The search BE is :: " + JsonUtils.toJson(searchBE));

		if (VertxUtils.cachedEnabled) {
			List<BaseEntity> results = new ArrayList<BaseEntity>();
			// tests.add(createTestPerson(gennyToken, "The Phantom",
			// "kit.walker@phantom.bg"));
			// tests.add(createTestPerson(gennyToken, "Phantom Menace",
			// "menace43r@starwars.net"));
			// tests.add(createTestPerson(gennyToken, "The Phantom Ranger",
			// "phantom@rangers.com"));
			Integer pageStart = searchBE.getValue("SCH_PAGE_START", 0);
			Integer pageSize = searchBE.getValue("SCH_PAGE_SIZE", 10);

			List<BaseEntity> tests = new ArrayList<>();

			tests.add(createTestCompany(gennyToken, "Melbourne University", "0398745321", "support@melbuni.edu.au",
					"MELBOURNE", "Victoria", "3001"));
			tests.add(createTestCompany(gennyToken, "Monash University", "0398744421", "support@melbuni.edu.au", "CLAYTON",
					"Victoria", "3142"));
			tests.add(createTestCompany(gennyToken, "Latrobe University", "0398733321", "support@melbuni.edu.au", "MELBOURNE",
					"Victoria", "3001"));
			tests.add(createTestCompany(gennyToken, "University Of Warracknabeal", "0392225321", "support@melbuni.edu.au",
					"WARRACKNABEAL", "Victoria", "3993"));
			tests.add(createTestCompany(gennyToken, "Ashburton University", "0398741111", "support@melbuni.edu.au",
					"ASHBURTON", "Victoria", "3147"));
			tests.add(createTestCompany(gennyToken, "Outcome Academy", "0398745777", "support@melbuni.edu.au", "MELBOURNE",
					"Victoria", "3001"));
			tests.add(createTestCompany(gennyToken, "Holland University", "0298555521", "support@melbuni.edu.au", "MELBOURNE",
					"Victoria", "3001"));
			tests.add(createTestCompany(gennyToken, "University of Greenvale", "0899995321", "support@melbuni.edu.au",
					"MELBOURNE", "Victoria", "3001"));
			tests.add(createTestCompany(gennyToken, "Crow University", "0398749999", "support@melbuni.edu.au", "MELBOURNE",
					"Victoria", "3001"));
			tests.add(createTestCompany(gennyToken, "RMIT University", "0398748787", "support@melbuni.edu.au", "MELBOURNE",
					"Victoria", "3001"));
			tests.add(createTestCompany(gennyToken, "Mt Buller University", "0398836421", "support@melbuni.edu.au",
					"MELBOURNE", "Victoria", "3001"));
			tests.add(createTestCompany(gennyToken, "Australian National University", "0198876541", "support@melbuni.edu.au",
					"MELBOURNE", "Victoria", "3001"));
			tests.add(createTestCompany(gennyToken, "Dodgy University", "0390000001", "support@melbuni.edu.au", "MELBOURNE",
					"Victoria", "3001"));
			tests.add(createTestCompany(gennyToken, "Australian Catholic University", "0398711121", "support@melbuni.edu.au",
					"MELBOURNE", "Victoria", "3001"));
			tests.add(createTestCompany(gennyToken, "Australian Jedi University", "0798788881", "support@melbuni.edu.au",
					"MELBOURNE", "Victoria", "3001"));
			tests.add(createTestCompany(gennyToken, "Brisbane Lions University", "0401020319", "support@melbuni.edu.au",
					"BRISBANE", "Queensland", "4000"));
			tests.add(createTestCompany(gennyToken, "AFL University", "0390000001", "support@melbuni.edu.au", "MELBOURNE",
					"Victoria", "3001"));
			tests.add(createTestCompany(gennyToken, "Uluru University", "0398711441", "support@melbuni.edu.au", "MELBOURNE",
					"Victoria", "3001"));
			tests.add(createTestCompany(gennyToken, "University Of Hard Knocks", "0798744881", "support@melbuni.edu.au",
					"MELBOURNE", "Victoria", "3001"));
			tests.add(createTestCompany(gennyToken, "Scam University", "0705020319", "support@melbuni.edu.au", "MELBOURNE",
					"Victoria", "3001"));

			for (Integer pageIndex = pageStart; pageIndex < (pageStart + pageSize); pageIndex++) {
				if (pageIndex < tests.size()) {
					results.add(tests.get(pageIndex));
				}
			}

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
						log.info("The result of getSearchResults was " + msg.getItems().length + " items ");
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
		msg.setToken(gennyToken.getToken());
		return msg;

	}

	static BaseEntity createTestPerson(GennyToken gennyToken, String name, String email) {
		String usercode = "PER_" + QwandaUtils.getNormalisedUsername(email);
		BaseEntity result1 = new BaseEntity(usercode, name);
		result1.setRealm(gennyToken.getRealm());

		return result1;
	}

	static BaseEntity createTestCompany(GennyToken gennyToken, String name, String phone, String email, String city,
			String state, String postcode) {
		String usercode = "CPY_" + UUID.randomUUID().toString().substring(0, 15).toUpperCase().replaceAll("-", "");

		BaseEntity result1 = new BaseEntity(usercode, name);
		result1.setRealm(gennyToken.getRealm());
		try {
			result1.addAnswer(new Answer(result1, result1, attribute("PRI_EMAIL", gennyToken), email));
			result1.addAnswer(new Answer(result1, result1, attribute("PRI_ADDRESS_STATE", gennyToken), state));
			result1.addAnswer(new Answer(result1, result1, attribute("PRI_ADDRESS_CITY", gennyToken), city));
			result1.addAnswer(new Answer(result1, result1, attribute("PRI_ADDRESS_POSTCODE", gennyToken), postcode));
			result1.addAnswer(new Answer(result1, result1, attribute("PRI_LANDLINE", gennyToken), phone));
		} catch (BadDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result1;
	}

	private static Attribute attribute(final String attributeCode, GennyToken gToken) {
		Attribute attribute = RulesUtils.getAttribute(attributeCode, gToken.getToken());
		return attribute;
	}

	public Ask generateTableHeaderAsk(SearchEntity searchBe, List<QDataBaseEntityMessage> themeMsgList) {

		List<Ask> asks = new ArrayList<>();

		/* Validation for Search Attribute */
		Validation validation = new Validation("VLD_NON_EMPTY", "EmptyandBlankValues", "(?!^$|\\s+)");
		List<Validation> validations = new ArrayList<>();
		validations.add(validation);
		ValidationList searchValidationList = new ValidationList();
		searchValidationList.setValidationList(validations);

		Attribute eventAttribute = RulesUtils.attributeMap.get("PRI_SORT");
		Attribute questionAttribute = RulesUtils.attributeMap.get("QQQ_QUESTION_GROUP");
		Attribute tableCellAttribute = RulesUtils.attributeMap.get("QQQ_QUESTION_GROUP_TABLE_CELL");

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
			Question columnHeaderQuestion = new Question("QUE_" + attributeCode + "_GRP", attributeName, tableCellAttribute,
					true);
			Ask columnHeaderAsk = new Ask(columnHeaderQuestion, beUtils.getGennyToken().getUserCode(), searchBe.getCode());

			/* creating ask for table header label-sort */
			Ask columnSortAsk = getAskForTableHeaderSort(searchBe, attributeCode, attributeName, eventAttribute,
					themeMsgList);

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
			columnHeaderAsk = this.createVirtualContext(columnHeaderAsk, verticalTheme, ContextType.THEME, themeMsgList);
			asks.add(columnHeaderAsk);
		}

		/* Convert List to Array */
		Ask[] asksArray = asks.toArray(new Ask[0]);

		/*
		 * we create a table-header ask grp and set all the column asks as it's childAsk
		 */
		Question tableHeaderQuestion = new Question("QUE_TABLE_HEADER_GRP", searchBe.getName(), questionAttribute, true);

		Ask tableHeaderAsk = new Ask(tableHeaderQuestion, beUtils.getGennyToken().getUserCode(), searchBe.getCode());
		tableHeaderAsk.setChildAsks(asksArray);
		tableHeaderAsk.setName(searchBe.getName());

		return tableHeaderAsk;
	}

	public Ask createVirtualContext(Ask ask, BaseEntity theme, ContextType linkCode,
			List<QDataBaseEntityMessage> themeMsgList) {
		List<BaseEntity> themeList = new ArrayList<>();
		themeList.add(theme);
		return createVirtualContext(ask, themeList, linkCode, VisualControlType.VCL_INPUT, themeMsgList);
	}

	public Ask createVirtualContext(Ask ask, List<BaseEntity> themeList, ContextType linkCode,
			List<QDataBaseEntityMessage> themeMsgList) {
		return createVirtualContext(ask, themeList, linkCode, VisualControlType.VCL_INPUT, themeMsgList);
	}

	public Ask createVirtualContext(Ask ask, BaseEntity theme, ContextType linkCode, VisualControlType visualControlType,
			List<QDataBaseEntityMessage> themeMsgList) {
		List<BaseEntity> themeList = new ArrayList<>();
		themeList.add(theme);
		return createVirtualContext(ask, themeList, linkCode, visualControlType, themeMsgList);
	}

	public Ask createVirtualContext(Ask ask, BaseEntity theme, ContextType linkCode, VisualControlType visualControlType,
			Double weight, List<QDataBaseEntityMessage> themeMsgList) {
		List<BaseEntity> themeList = new ArrayList<>();
		themeList.add(theme);
		return createVirtualContext(ask, themeList, linkCode, visualControlType, weight, themeMsgList);
	}

	public Ask createVirtualContext(Ask ask, List<BaseEntity> themes, ContextType linkCode,
			VisualControlType visualControlType, List<QDataBaseEntityMessage> themeMsgList) {
		return createVirtualContext(ask, themes, linkCode, visualControlType, 2.0, themeMsgList);
	}

	public Ask createVirtualContext(Ask ask, BaseEntity theme, ContextType linkCode,
			VisualControlType visualControlType) {
		List<BaseEntity> themeList = new ArrayList<>();
		themeList.add(theme);
		List<QDataBaseEntityMessage> themeMsgList = new ArrayList<>();
		return createVirtualContext(ask, themeList, linkCode, visualControlType, 2.0, themeMsgList);
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
		createVirtualContext(columnSortAsk, horizontalWrapperBe, ContextType.THEME, VisualControlType.VCL_WRAPPER,
				themeMsgList);
		createVirtualContext(columnSortAsk, sortIconBe, ContextType.ICON, VisualControlType.VCL_ICON, themeMsgList);
		createVirtualContext(columnSortAsk, visualBaseEntity, ContextType.THEME, VisualControlType.VCL_INPUT, themeMsgList);
		createVirtualContext(columnSortAsk, headerLabelSortThemeBe, ContextType.THEME, VisualControlType.VCL_LABEL,
				themeMsgList);

		return columnSortAsk;
	}

	/**
	 * @param serviceToken
	 * @return
	 */
	public static QDataBaseEntityMessage changeQuestion(SearchEntity searchBE, final String frameCode,
			final String questionCode, GennyToken serviceToken, GennyToken userToken, Set<QDataAskMessage> askMsgs) {
		Frame3 frame = null;
		try {

			if (frameCode.equals("FRM_TABLE_CONTENT")) {

				Validation tableRowValidation = new Validation("VLD_ANYTHING", "Anything", ".*");

				List<Validation> tableRowValidations = new ArrayList<>();
				tableRowValidations.add(tableRowValidation);

				ValidationList tableRowValidationList = new ValidationList();
				tableRowValidationList.setValidationList(tableRowValidations);

				DataType tableRowDataType = new DataType("DTT_TABLE_ROW_GRP", tableRowValidationList, "Table Row Group", "");

				frame = Frame3.builder(frameCode).addTheme("THM_TABLE_BORDER", serviceToken).end()
						.addTheme("THM_TABLE_CONTENT_CENTRE", ThemePosition.CENTRE, serviceToken).end().question(questionCode)
						.addTheme("THM_DISPLAY_HORIZONTAL", serviceToken).dataType(tableRowDataType).weight(1.0).end()
						.addTheme("THM_TABLE_ROW_CONTENT_WRAPPER", serviceToken).dataType(tableRowDataType)
						.vcl(VisualControlType.GROUP).weight(1.0).end().addTheme("THM_TABLE_ROW", serviceToken)
						.dataType(tableRowDataType).weight(1.0).end().addTheme("THM_TABLE_CONTENT", serviceToken)
						.vcl(VisualControlType.GROUP).end().addTheme("THM_TABLE_ROW_CELL", serviceToken)
						.vcl(VisualControlType.VCL_WRAPPER).end().end().build();

			} else {

				System.out.println("it's a FRM_TABLE_HEADER");

				Validation tableCellValidation = new Validation("VLD_ANYTHING", "Anything", ".*");

				List<Validation> tableCellValidations = new ArrayList<>();
				tableCellValidations.add(tableCellValidation);

				ValidationList tableCellValidationList = new ValidationList();
				tableCellValidationList.setValidationList(tableCellValidations);

				DataType tableCellDataType = new DataType("DTT_TABLE_CELL_GRP", tableCellValidationList, "Table Cell Group",
						"");

				frame = Frame3.builder(frameCode).addTheme("THM_TABLE_BORDER", serviceToken).end().question(questionCode) // QUE_TEST_TABLE_HEADER_GRP
						.addTheme("THM_QUESTION_GRP_LABEL", serviceToken).vcl(VisualControlType.GROUP).dataType(tableCellDataType)
						.end().addTheme("THM_WIDTH_100_PERCENT_NO_INHERIT", serviceToken).vcl(VisualControlType.GROUP).end()
						.addTheme("THM_TABLE_ROW_CELL", serviceToken).dataType(tableCellDataType)
						.vcl(VisualControlType.GROUP_WRAPPER).end().addTheme("THM_DISPLAY_HORIZONTAL", serviceToken).weight(2.0)
						.end().addTheme("THM_TABLE_HEADER_CELL_WRAPPER", serviceToken).vcl(VisualControlType.VCL_WRAPPER).end()
						.addTheme("THM_TABLE_HEADER_CELL_GROUP_LABEL", serviceToken).vcl(VisualControlType.GROUP_LABEL).end()
						.addTheme("THM_DISPLAY_VERTICAL", serviceToken).dataType(tableCellDataType).weight(1.0).end().end().build();

			}

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		QDataBaseEntityMessage msg = FrameUtils2.toMessage(frame, serviceToken, askMsgs);
		msg.setReplace(true);

		String rootFrameCode = frameCode;

		for (BaseEntity targetFrame : msg.getItems()) {
			if (targetFrame.getCode().equals(questionCode)) {

				log.info("ShowFrame : Found Targeted Frame BaseEntity : " + targetFrame);

				/* Adding the links in the targeted BaseEntity */
				Attribute attribute = new Attribute("LNK_ASK", "LNK_ASK", new DataType(String.class));

				for (BaseEntity sourceFrame : msg.getItems()) {
					if (sourceFrame.getCode().equals(rootFrameCode)) {

						log.info("ShowFrame : Found Source Frame BaseEntity : " + sourceFrame);
						EntityEntity entityEntity = new EntityEntity(sourceFrame, targetFrame, attribute, 1.0, "CENTRE");
						// Set<EntityEntity> entEntList = sourceFrame.getLinks();
						// entEntList.add(entityEntity);
						sourceFrame.getLinks().add(entityEntity);
						sourceFrame.setName(searchBE.getName());
						/* Adding Frame to Targeted Frame BaseEntity Message */
						// msg.add(targetFrame);
						break;
					}
				}
				break;
			}
		}
		msg.setToken(userToken.getToken());
		return msg;
	}

	/*
	 * Generate List of asks from a SearchEntity
	 */
	public static List<Ask> generateQuestions(GennyToken userToken, BaseEntityUtils beUtils, List<BaseEntity> bes,
			Map<String, String> columns, String targetCode) {

		/* initialize an empty ask list */
		List<Ask> askList = new ArrayList<>();
		List<QDataBaseEntityMessage> themeMsgList = new ArrayList<QDataBaseEntityMessage>();
		TableUtilsTest tableUtils = new TableUtilsTest(beUtils);

		if (columns != null) {
			if (bes != null && bes.isEmpty() == false) {

				/* loop through baseentities to generate ask */
				for (BaseEntity be : bes) {

					/* we add attributes for each be */
					beUtils.addAttributes(be);

					/* initialize child ask list */
					List<Ask> childAskList = new ArrayList<>();

					for (Map.Entry<String, String> column : columns.entrySet()) {

						String attributeCode = column.getKey();
						String attributeName = column.getValue();
						Attribute attr = RulesUtils.attributeMap.get(attributeCode);

						Question childQuestion = new Question("QUE_" + attributeCode + "_" + be.getCode(), attributeName, attr,
								true);
						Ask childAsk = new Ask(childQuestion, targetCode, be.getCode());

						/* add the entityAttribute ask to list */
						childAskList.add(childAsk);

					}

					/* converting childAsks list to array */
					Ask[] childAsArr = childAskList.stream().toArray(Ask[]::new);

					/* Get the on-the-fly question attribute */
					Attribute questionAttribute = new Attribute("QQQ_QUESTION_GROUP", "link", new DataType(String.class));

					Attribute questionTableRowAttribute = new Attribute("QQQ_QUESTION_GROUP_TABLE_ROW", "link",
							new DataType(String.class));

					/* Generate ask for the baseentity */
					Question parentQuestion = new Question("QUE_" + be.getCode() + "_GRP", be.getName(),
							questionTableRowAttribute, true);
					Ask parentAsk = new Ask(parentQuestion, targetCode, be.getCode());

					/* setting weight to parent ask */
					parentAsk.setWeight(be.getIndex().doubleValue());

					/* set all the childAsks to parentAsk */
					parentAsk.setChildAsks(childAsArr);

					/* add the baseentity asks to a list */
					askList.add(parentAsk);
				}

			}

		}

		/* return list of asks */
		return askList;
	}

	public static void paginateTable(GennyToken serviceToken, BaseEntityUtils beUtils, final String searchBarCode,
			Answer answer) {

		TableUtilsTest tableUtils = new TableUtilsTest(beUtils);

		/* Perform a search bar search */
		String searchBarString = null;
		if (answer != null) {
			searchBarString = answer.getValue();
			// Clean up search Text
			searchBarString = searchBarString.trim();
			searchBarString = searchBarString.replaceAll("[^a-zA-Z0-9\\ ]", "");
			Integer max = searchBarString.length();
			Integer realMax = (max > MAX_SEARCH_BAR_TEXT_SIZE) ? MAX_SEARCH_BAR_TEXT_SIZE : max;
			searchBarString.substring(0, realMax);
			log.info("Search text = [" + searchBarString + "]");
		}

		/* Get the SearchBE */
		String sessionSearchCode = searchBarCode + "_" + beUtils.getGennyToken().getSessionCode();
		SearchEntity searchBE = VertxUtils.getObject(serviceToken.getRealm(), "", sessionSearchCode, SearchEntity.class,
				serviceToken.getToken());

		if (searchBE == null) {
			searchBE = VertxUtils.getObject(serviceToken.getRealm(), "", searchBarCode, SearchEntity.class,
					serviceToken.getToken());
			/*
			 * Save Session Search in cache , ideally this should be in OutputParam and
			 * saved to workflow
			 */
			VertxUtils.putObject(serviceToken.getRealm(), "", sessionSearchCode, searchBE, serviceToken.getToken());
		}

		/* fetch Session SearchBar List from User */
		BaseEntity user = VertxUtils.getObject(beUtils.getGennyToken().getRealm(), "",
				beUtils.getGennyToken().getUserCode(), BaseEntity.class, beUtils.getGennyToken().getToken());
		Type type = new TypeToken<List<String>>() {
		}.getType();
		List<String> defaultList = new ArrayList<String>();
		String defaultListString = JsonUtils.toJson(defaultList);
		String historyStr = user.getValue("PRI_SEARCH_HISTORY", defaultListString);
		List<String> searchHistory = JsonUtils.fromJson(historyStr, type);

		/* Add new SearchBarString to Session SearchBar List */
		/* look for existing search term and bring to front - slow */
		if (answer != null) { // no need to set history if no data sent
			int index = searchHistory.indexOf(searchBarString);
			if (index >= 0) {
				searchHistory.remove(index);
			}
			searchHistory.add(0, searchBarString);
			if (searchHistory.size() > MAX_SEARCH_HISTORY_SIZE) {
				searchHistory.remove(MAX_SEARCH_HISTORY_SIZE);
			}
			String newHistoryString = JsonUtils.toJson(searchHistory);
			Answer history = new Answer(beUtils.getGennyToken().getUserCode(), beUtils.getGennyToken().getUserCode(),
					"PRI_SEARCH_HISTORY", newHistoryString);
			beUtils.saveAnswer(history);
			log.info("Search History for " + beUtils.getGennyToken().getUserCode() + " = " + searchHistory.toString());
		} else {
			// so grab the latest search history
			if (!searchHistory.isEmpty()) {
				searchBarString = searchHistory.get(0);
			} else {
				searchBarString = ""; // fetch everything
			}
		}

		searchBE.addFilter("PRI_NAME", SearchEntity.StringFilter.LIKE, "%" + searchBarString + "%");

		// Send out Search Results

		QDataBaseEntityMessage msg = tableUtils.fetchSearchResults(searchBE, beUtils.getGennyToken());

		/* send the baseentity from the search */
		VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg));

		/* get the table header columns */
		Map<String, String> columns = tableUtils.getTableColumns(searchBE);

		/* generatethe row asks */
		List<BaseEntity> rowList = Arrays.asList(msg.getItems());

		List<Ask> rowAsks = generateQuestions2(beUtils.getGennyToken(), beUtils, rowList, columns,
				beUtils.getGennyToken().getUserCode());

		/* converting rowAsks list to array */
		Ask[] rowAsksArr = rowAsks.stream().toArray(Ask[]::new);

		/* get the row themes for table */
		Type setType = new TypeToken<Set<QDataAskMessage>>() {
		}.getType();

		String askMsgs2Str = VertxUtils.getObject(beUtils.getGennyToken().getRealm(), "", "FRM_TABLE_CONTENT_ASKS",
				String.class, beUtils.getGennyToken().getToken());

		Set<QDataAskMessage> askMsgs2 = JsonUtils.fromJson(askMsgs2Str, setType);
		QDataAskMessage[] askMsg2Array = askMsgs2.stream().toArray(QDataAskMessage[]::new);
		ContextList rowsContextList = askMsg2Array[0].getItems()[0].getContextList();

		/* Link row asks to a single ask: QUE_TABLE_RESULTS_GRP */
		Attribute questionAttribute = new Attribute("QQQ_QUESTION_GROUP", "link", new DataType(String.class));
		Question tableResultQuestion = new Question("QUE_TABLE_RESULTS_GRP", "Table Results Question Group",
				questionAttribute, true);

		Ask tableResultAsk = new Ask(tableResultQuestion, beUtils.getGennyToken().getUserCode(),
				beUtils.getGennyToken().getUserCode());
		tableResultAsk.setChildAsks(rowAsksArr);
		tableResultAsk.setContextList(rowsContextList);

		/* sending the row asks */
		Set<QDataAskMessage> tableResultAskMsgs = new HashSet<QDataAskMessage>();
		tableResultAskMsgs.add(new QDataAskMessage(tableResultAsk));
		/* VertxUtils.writeMsg("webcmds", JsonUtils.toJson(tableResultAskMsgs)); */

		/* sending individual asks */
		for (QDataAskMessage askMsg : tableResultAskMsgs) {

			askMsg.setToken(beUtils.getGennyToken().getToken());
			// askMsg.getItems()[0] = headerAsk;
			askMsg.setReplace(true);
			VertxUtils.writeMsg("webcmds", JsonUtils.toJson(askMsg));

		}

		/* change the question from frame */
		QDataBaseEntityMessage msg3 = null;
		msg3 = TableUtilsTest.changeQuestion(searchBE, "FRM_TABLE_CONTENT", tableResultAsk.getQuestionCode(), serviceToken,
				beUtils.getGennyToken(), tableResultAskMsgs);
		msg3.setToken(beUtils.getGennyToken().getToken());
		msg3.setReplace(true);

		VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg3));

	}

	public static List<Ask> generateQuestions2(GennyToken userToken, BaseEntityUtils beUtils, List<BaseEntity> bes,
			Map<String, String> columns, String targetCode) {

		/* initialize an empty ask list */
		List<Ask> askList = new ArrayList<>();
		TableUtilsTest tableUtils = new TableUtilsTest(beUtils);

		if (columns != null) {
			if (bes != null && bes.isEmpty() == false) {

				/* loop through baseentities to generate row ask */
				for (BaseEntity be : bes) {

					/* we add attributes for each be */
					// ? why
					beUtils.addAttributes(be);

					/* initialize child ask list */
					List<Ask> childAskList = new ArrayList<>();

					for (Map.Entry<String, String> column : columns.entrySet()) {

						String attributeCode = column.getKey();
						String attributeName = column.getValue();
						Attribute attr = RulesUtils.attributeMap.get(attributeCode);

						Question childQuestion = new Question("QUE_" + attributeCode + "_" + be.getCode(), attributeName, attr,
								true);
						Ask childAsk = new Ask(childQuestion, targetCode, be.getCode());

						/* add the entityAttribute ask to list */
						childAskList.add(childAsk);

					}

					/* converting childAsks list to array */
					Ask[] childAsArr = childAskList.stream().toArray(Ask[]::new);

					/* Get the on-the-fly question attribute */
					Attribute questionAttribute = new Attribute("QQQ_QUESTION_GROUP", "link", new DataType(String.class));

					Attribute questionTableRowAttribute = new Attribute("QQQ_QUESTION_GROUP_TABLE_ROW", "link",
							new DataType(String.class));

					/* Generate ask for the baseentity */
					Question parentQuestion = new Question("QUE_" + be.getCode() + "_GRP", be.getName(),
							questionTableRowAttribute, true);
					Ask parentAsk = new Ask(parentQuestion, targetCode, be.getCode());

					/* setting weight to parent ask */
					parentAsk.setWeight(be.getIndex().doubleValue());

					/* set all the childAsks to parentAsk */
					parentAsk.setChildAsks(childAsArr);

					/* add the baseentity asks to a list */
					askList.add(parentAsk);
				}
			}
		}

		/* return list of asks */
		return askList;
	}

	public Ask getBucketHeaderAsk2(SearchEntity searchBe, GennyToken serviceToken) {

		QRules rules = GennyJbpmBaseTest.setupLocalService();

		List<Ask> asks = new ArrayList<>();
		String code = searchBe.getCode().split("SBE_")[1];
		System.out.println("code " + code);

		Theme THM_QUESTION_GRP_LABEL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_QUESTION_GRP_LABEL",
				Theme.class, serviceToken.getToken());
		Theme THM_DISPLAY_VERTICAL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_DISPLAY_VERTICAL", Theme.class,
				serviceToken.getToken());
		Theme THM_DISPLAY_HORIZONTAL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_DISPLAY_HORIZONTAL",
				Theme.class, serviceToken.getToken());
		Theme THM_WIDTH_100_PERCENT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_WIDTH_100_PERCENT",
				Theme.class, serviceToken.getToken());
		Theme THM_BH_ROW_ONE_GRP_WRAPPER = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BH_ROW_ONE_GRP_WRAPPER",
				Theme.class, serviceToken.getToken());
		Theme THM_BH_ROW_ONE_GRP_LABEL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BH_ROW_ONE_GRP_LABEL",
				Theme.class, serviceToken.getToken());
		Theme THM_BH_ROW_ONE_GRP_CONTENT_WRAPPER = VertxUtils.getObject(serviceToken.getRealm(), "",
				"THM_BH_ROW_ONE_GRP_CONTENT_WRAPPER", Theme.class, serviceToken.getToken());
		Theme THM_BH_ROW_ONE_VCL_INPUT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BH_ROW_ONE_VCL_INPUT",
				Theme.class, serviceToken.getToken());
		Theme THM_BH_ROW_TWO_VCL_WRAPPER = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BH_ROW_TWO_VCL_WRAPPER",
				Theme.class, serviceToken.getToken());
		Theme THM_BH_ROW_TWO_GRP_CONTENT_WRAPPER = VertxUtils.getObject(serviceToken.getRealm(), "",
				"THM_BH_ROW_TWO_GRP_CONTENT_WRAPPER", Theme.class, serviceToken.getToken());
		Theme THM_BH_ROW_TWO_INPUT_FIELD = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BH_ROW_TWO_INPUT_FIELD",
				Theme.class, serviceToken.getToken());
		Theme THM_ICON = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_ICON", Theme.class,
				serviceToken.getToken());
		Theme THM_BH_GROUP_WRAPPER = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BH_GROUP_WRAPPER", Theme.class,
				serviceToken.getToken());

		BaseEntity ICN_SORT = beUtils.getBaseEntityByCode("ICN_SORT");

		List<BaseEntity> themes = new ArrayList<>();
		themes.add(THM_QUESTION_GRP_LABEL);
		themes.add(getThemeBe(THM_DISPLAY_VERTICAL));
		themes.add(getThemeBe(THM_WIDTH_100_PERCENT));
		themes.add(getThemeBe(THM_BH_ROW_ONE_GRP_WRAPPER));
		themes.add(getThemeBe(THM_BH_ROW_ONE_GRP_LABEL));
		themes.add(getThemeBe(THM_BH_ROW_ONE_GRP_CONTENT_WRAPPER));
		themes.add(getThemeBe(THM_BH_ROW_ONE_VCL_INPUT));
		themes.add(getThemeBe(THM_BH_ROW_TWO_VCL_WRAPPER));
		themes.add(getThemeBe(THM_BH_ROW_TWO_GRP_CONTENT_WRAPPER));
		themes.add(getThemeBe(THM_BH_GROUP_WRAPPER));
		themes.add(getThemeBe(THM_BH_ROW_TWO_INPUT_FIELD));
		themes.add(ICN_SORT);
		themes.add(getThemeBe(THM_ICON));

		QDataBaseEntityMessage msg = new QDataBaseEntityMessage(themes);
		msg.setToken(beUtils.getGennyToken().getToken());
		rules.publishCmd(msg);

		/* Validation for Search Attribute */
		Validation validation = new Validation("VLD_NON_EMPTY", "EmptyandBlankValues", "(?!^$|\\s+)");
		List<Validation> validations = new ArrayList<>();
		validations.add(validation);
		ValidationList searchValidationList = new ValidationList();
		searchValidationList.setValidationList(validations);

		Attribute countAttribute = RulesUtils.getAttribute("PRI_TOTAL_RESULTS", serviceToken.getToken());
		Attribute sortAttribute = RulesUtils.getAttribute("PRI_SORT", serviceToken.getToken());
		Attribute nameAttribute = RulesUtils.getAttribute("PRI_NAME", serviceToken.getToken());

		Attribute searchAttribute = new Attribute("PRI_NAME", "Search", new DataType("Text", searchValidationList, "Text"));

		Attribute questionAttribute = RulesUtils.getAttribute("QQQ_QUESTION_GROUP", serviceToken.getToken());
		Attribute tableCellAttribute = RulesUtils.getAttribute("QQQ_QUESTION_GROUP_TABLE_CELL", serviceToken.getToken());

		/* Initialize Bucket Header Ask group */
		Question bucketHeaderQuestion = new Question("QUE_BUCKET_HEADER_" + code + "_GRP", searchBe.getName(),
				questionAttribute, true);

		Ask bucketHeaderAsk = new Ask(bucketHeaderQuestion, beUtils.getGennyToken().getUserCode(), searchBe.getCode());

		/* row-one-ask */
		Question row1Ques = new Question("QUE_BUCKET_HEADER_ROW_ONE_" + code, searchBe.getName(), tableCellAttribute,
				false);
		Ask row1Ask = new Ask(row1Ques, beUtils.getGennyToken().getUserCode(), searchBe.getCode());
		row1Ask = this.createVirtualContext(row1Ask, getThemeBe(THM_DISPLAY_HORIZONTAL), ContextType.THEME,
				VisualControlType.GROUP_WRAPPER);
		row1Ask = this.createVirtualContext(row1Ask, getThemeBe(THM_BH_ROW_ONE_GRP_WRAPPER), ContextType.THEME,
				VisualControlType.GROUP_WRAPPER);
		row1Ask = this.createVirtualContext(row1Ask, getThemeBe(THM_BH_ROW_ONE_GRP_LABEL), ContextType.THEME,
				VisualControlType.GROUP_LABEL);
		row1Ask = this.createVirtualContext(row1Ask, getThemeBe(THM_BH_ROW_ONE_GRP_CONTENT_WRAPPER), ContextType.THEME,
				VisualControlType.GROUP_CONTENT_WRAPPER);
		row1Ask = this.createVirtualContext(row1Ask, getThemeBe(THM_BH_ROW_ONE_VCL_INPUT), ContextType.THEME,
				VisualControlType.VCL_INPUT);

		/* count ask */
		Question bucketCountQues = new Question("QUE_COUNT_" + code, countAttribute.getName(), countAttribute, false);
		Ask bucketCountAsk = new Ask(bucketCountQues, beUtils.getGennyToken().getUserCode(), searchBe.getCode());
		// bucketCountAsk.setReadonly(true);

		Ask[] row1ChildAsks = { bucketCountAsk };
		row1Ask = this.createVirtualContext(row1Ask, getThemeBe(THM_QUESTION_GRP_LABEL), ContextType.THEME,
				VisualControlType.GROUP);
		row1Ask.setChildAsks(row1ChildAsks);

		/* row-two-ask */
		Question row2Ques = new Question("QUE_BUCKET_HEADER_ROW_TWO_" + code, questionAttribute.getName(),
				questionAttribute, false);
		Ask row2Ask = new Ask(row2Ques, beUtils.getGennyToken().getUserCode(), searchBe.getCode());
		row2Ask = this.createVirtualContext(row2Ask, getThemeBe(THM_DISPLAY_HORIZONTAL), ContextType.THEME,
				VisualControlType.GROUP_CONTENT_WRAPPER);
		row2Ask = this.createVirtualContext(row2Ask, getThemeBe(THM_BH_ROW_TWO_VCL_WRAPPER), ContextType.THEME,
				VisualControlType.VCL_WRAPPER);
		row2Ask = this.createVirtualContext(row2Ask, getThemeBe(THM_BH_ROW_TWO_GRP_CONTENT_WRAPPER), ContextType.THEME,
				VisualControlType.GROUP_CONTENT_WRAPPER);

		/* search ask */
		Question bucketSearchQues = new Question("QUE_SEARCH_" + code, searchAttribute.getName(), searchAttribute, false);
		Ask bucketSearchAsk = new Ask(bucketSearchQues, beUtils.getGennyToken().getUserCode(), searchBe.getCode());
		bucketSearchAsk = this.createVirtualContext(bucketSearchAsk, getThemeBe(THM_BH_ROW_TWO_INPUT_FIELD),
				ContextType.THEME, VisualControlType.VCL_WRAPPER);

		/* sort ask */
		Question bucketSortQues = new Question("QUE_SORT_" + code, sortAttribute.getName(), sortAttribute, false);
		Ask bucketSortAsk = new Ask(bucketSortQues, beUtils.getGennyToken().getUserCode(), searchBe.getCode());

		bucketSortAsk = createVirtualContext(bucketSortAsk, ICN_SORT, ContextType.ICON, VisualControlType.VCL_ICON);
		bucketSortAsk = createVirtualContext(bucketSortAsk, getThemeBe(THM_ICON), ContextType.THEME, VisualControlType.VCL);

		Ask[] row2ChildAsks = { bucketSearchAsk, bucketSortAsk };
		row2Ask.setChildAsks(row2ChildAsks);

		/* set the bucketHeader child asks */
		Ask[] bucketChildAsks = { row1Ask, row2Ask };
		bucketHeaderAsk.setChildAsks(bucketChildAsks);

		bucketHeaderAsk = this.createVirtualContext(bucketHeaderAsk, getThemeBe(THM_DISPLAY_VERTICAL), ContextType.THEME,
				VisualControlType.GROUP_CONTENT_WRAPPER);
		bucketHeaderAsk = this.createVirtualContext(bucketHeaderAsk, getThemeBe(THM_BH_GROUP_WRAPPER), ContextType.THEME,
				VisualControlType.GROUP_WRAPPER);

		return bucketHeaderAsk;
	}

	public Ask generateBucketContentAsk(SearchEntity searchBe, GennyToken serviceToken) {

		Attribute questionAttribute = RulesUtils.attributeMap.get("QQQ_QUESTION_GROUP");

		/* Initialize Bucket Header Ask group */
		Question bucketContentQuestion = new Question("QUE_BUCKET_CONTENT_" + searchBe.getCode() + "_GRP", "",
				questionAttribute, true);

		Ask bucketContentAsk = new Ask(bucketContentQuestion, beUtils.getGennyToken().getUserCode(), searchBe.getCode());

		return bucketContentAsk;
	}

	public Ask generateBucketFooterAsk(SearchEntity searchBe, GennyToken serviceToken) {

		QRules rules = GennyJbpmBaseTest.setupLocalService();

		List<BaseEntity> themes = new ArrayList<>();

		Theme THM_DISPLAY_HORIZONTAL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_DISPLAY_HORIZONTAL",
				Theme.class, serviceToken.getToken());

		Theme THM_WIDTH_100_PERCENT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_WIDTH_100_PERCENT",
				Theme.class, serviceToken.getToken());

		Theme THM_ICON = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_ICON", Theme.class,
				serviceToken.getToken());

		Theme THM_JUSTIFY_CONTENT_SPACE_AROUND = Theme.builder("THM_JUSTIFY_CONTENT_SPACE_AROUND").addAttribute()
				.justifyContent("space-around").end().build();

		BaseEntity ICN_ARROW_FORWARD_IOS = beUtils.getBaseEntityByCode("ICN_ARROW_FORWARD_IOS");
		BaseEntity ICN_ARROW_BACK_IOS = beUtils.getBaseEntityByCode("ICN_ARROW_BACK_IOS");

		themes.add(ICN_ARROW_FORWARD_IOS);
		themes.add(ICN_ARROW_BACK_IOS);
		themes.add(getThemeBe(THM_ICON)); /* already publishing on header */
		themes.add(getThemeBe(THM_WIDTH_100_PERCENT));
		themes.add(getThemeBe(THM_JUSTIFY_CONTENT_SPACE_AROUND));

		QDataBaseEntityMessage msg = new QDataBaseEntityMessage(themes);
		msg.setToken(beUtils.getGennyToken().getToken());
		rules.publishCmd(msg);

		Attribute questionAttribute = RulesUtils.attributeMap.get("QQQ_QUESTION_GROUP");
		Attribute nextAttribute = RulesUtils.attributeMap.get("PRI_NEXT_BTN");
		Attribute prevAttribute = RulesUtils.attributeMap.get("PRI_PREVIOUS_BTN");

		/* Initialize Bucket Footer Ask group */
		Question bucketFooterQuestion = new Question("QUE_BUCKET_FOOTER_" + searchBe.getCode() + "_GRP", searchBe.getName(),
				questionAttribute, true);

		Ask bucketFooterAsk = new Ask(bucketFooterQuestion, beUtils.getGennyToken().getUserCode(), searchBe.getCode());
		bucketFooterAsk = this.createVirtualContext(bucketFooterAsk, THM_DISPLAY_HORIZONTAL, ContextType.THEME,
				VisualControlType.GROUP_CONTENT_WRAPPER);
		bucketFooterAsk = this.createVirtualContext(bucketFooterAsk, getThemeBe(THM_WIDTH_100_PERCENT), ContextType.THEME,
				VisualControlType.GROUP_WRAPPER);
		bucketFooterAsk = this.createVirtualContext(bucketFooterAsk, getThemeBe(THM_JUSTIFY_CONTENT_SPACE_AROUND),
				ContextType.THEME, VisualControlType.GROUP_CONTENT_WRAPPER);

		/* next ask */
		Question nextBucketQues = new Question("QUE_NEXT_BUCKET", "", nextAttribute, false);
		Ask nextBucketAsk = new Ask(nextBucketQues, beUtils.getGennyToken().getUserCode(), searchBe.getCode());
		nextBucketAsk = this.createVirtualContext(nextBucketAsk, ICN_ARROW_FORWARD_IOS, ContextType.ICON,
				VisualControlType.VCL_ICON);
		nextBucketAsk = this.createVirtualContext(nextBucketAsk, getThemeBe(THM_ICON), ContextType.THEME,
				VisualControlType.VCL);

		/* prev ask */
		Question prevBucketQues = new Question("QUE_PREV_BUCKET", "", prevAttribute, false);
		Ask prevBucketAsk = new Ask(prevBucketQues, beUtils.getGennyToken().getUserCode(), searchBe.getCode());
		prevBucketAsk = this.createVirtualContext(prevBucketAsk, ICN_ARROW_BACK_IOS, ContextType.ICON,
				VisualControlType.VCL_ICON);
		prevBucketAsk = this.createVirtualContext(prevBucketAsk, getThemeBe(THM_ICON), ContextType.THEME,
				VisualControlType.VCL);

		/* set the child asks */
		Ask[] bucketChildAsksArray = { prevBucketAsk, nextBucketAsk };
		bucketFooterAsk.setChildAsks(bucketChildAsksArray);

		return bucketFooterAsk;
	}

	/*
	 * Virtual Link
	 */

	public BaseEntity createVirtualLink(BaseEntity source, List<Ask> asks, String linkCode, String linkValue) {

		if (source != null) {

			Set<EntityQuestion> entityQuestionList = source.getQuestions();

			for (Ask ask : asks) {

				Link link = new Link(source.getCode(), ask.getQuestion().getCode(), linkCode, linkValue);
				link.setWeight(ask.getWeight());
				EntityQuestion ee = new EntityQuestion(link);
				entityQuestionList.add(ee);
			}

			source.setQuestions(entityQuestionList);
		}
		return source;
	}

	public BaseEntity createVirtualLink(BaseEntity source, Ask ask, String linkCode, String linkValue) {

		if (source != null) {

			Set<EntityQuestion> entityQuestionList = source.getQuestions();

			Link link = new Link(source.getCode(), ask.getQuestion().getCode(), linkCode, linkValue);
			link.setWeight(ask.getWeight());
			EntityQuestion ee = new EntityQuestion(link);
			entityQuestionList.add(ee);

			source.setQuestions(entityQuestionList);
		}
		return source;
	}

	public Frame3 createVirtualLink(Frame3 frame, Ask ask, String linkCode, String linkValue) {

		BaseEntity source = (BaseEntity) frame;
		source.getCode();
		System.out.println("after casting to BE :: " + source.getClass());
		if (source != null) {

			Set<EntityQuestion> entityQuestionList = source.getQuestions();

			Link link = new Link(source.getCode(), ask.getQuestion().getCode(), linkCode, linkValue);
			link.setWeight(ask.getWeight());
			EntityQuestion ee = new EntityQuestion(link);
			entityQuestionList.add(ee);

			source.setQuestions(entityQuestionList);
		}

		Frame3 updatedFrame = (Frame3) source;
		return updatedFrame;
	}

	// public BaseEntity createVirtualLink(String sourceCode, Ask ask, String
	// linkCode, String linkValue) {
	//
	// BaseEntity source = this.baseEntity.getBaseEntityByCode(sourceCode);
	// return this.createVirtualLink(source, ask, linkCode, linkValue);
	// }
	//
	// public BaseEntity createVirtualLink(String sourceCode, List<Ask> asks, String
	// linkCode, String linkValue) {
	//
	// BaseEntity source = this.baseEntity.getBaseEntityByCode(sourceCode);
	// return this.createVirtualLink(source, asks, linkCode, linkValue);
	// }

	public BaseEntity createVirtualLink(BaseEntity source, BaseEntity target, String linkCode, String linkValue,
			Double weight) {

		Attribute attribute = new Attribute(linkCode, linkCode, new DataType(String.class));

		EntityEntity entityEntity = new EntityEntity(source, target, attribute, weight, linkValue);
		Set<EntityEntity> entEntList = source.getLinks();
		entEntList.add(entityEntity);

		source.setLinks(entEntList);
		return source;
	}

	public BaseEntity getThemeBe(Theme theme) {

		BaseEntity themeBe = null;
		themeBe = theme.getBaseEntity();
		if (theme.getAttributes() != null) {
			for (ThemeAttribute themeAttribute : theme.getAttributes()) {

				try {
					themeBe.addAttribute(new EntityAttribute(themeBe,
							new Attribute(themeAttribute.getCode(), themeAttribute.getCode(), new DataType("DTT_THEME")), 1.0,
							themeAttribute.getJson()));
				} catch (BadDataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return themeBe;
	}

	public Ask getCardTemplate(GennyToken serviceToken, QRules rules) {

		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
		TableUtilsTest tableUtils = new TableUtilsTest(beUtils);
		List<BaseEntity> beList = new ArrayList<BaseEntity>();

		try {
			// get the themes from cache
			BaseEntity THM_DISPLAY_VERTICAL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_DISPLAY_VERTICAL",
					BaseEntity.class, serviceToken.getToken());

			BaseEntity THM_JUSTIFY_CONTENT_FLEX_START = VertxUtils.getObject(serviceToken.getRealm(), "",
					"THM_JUSTIFY_CONTENT_FLEX_START", BaseEntity.class, serviceToken.getToken());

			Theme THM_CARD = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_CARD", Theme.class,
					serviceToken.getToken());

			BaseEntity THM_DISPLAY_HORIZONTAL = Theme.builder("THM_DISPLAY_HORIZONTAL").addAttribute().flexDirection("row")
					.end().addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

			BaseEntity THM_DROPDOWN_ICON_ALT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_DROPDOWN_ICON_ALT",
					BaseEntity.class, serviceToken.getToken());

			BaseEntity THM_DROPDOWN_BEHAVIOUR_GENNY = VertxUtils.getObject(serviceToken.getRealm(), "",
					"THM_DROPDOWN_BEHAVIOUR_GENNY", BaseEntity.class, serviceToken.getToken());

			BaseEntity THM_BACKGROUND_NONE = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BACKGROUND_NONE",
					BaseEntity.class, serviceToken.getToken());

			BaseEntity THM_DROPDOWN_PROFILE_BEHAVIOUR_GENNY = VertxUtils.getObject(serviceToken.getRealm(), "",
					"THM_DROPDOWN_PROFILE_BEHAVIOUR_GENNY", BaseEntity.class, serviceToken.getToken());

			BaseEntity THM_DROPDOWN_HEADER_WRAPPER_GENNY = VertxUtils.getObject(serviceToken.getRealm(), "",
					"THM_DROPDOWN_HEADER_WRAPPER_GENNY", BaseEntity.class, serviceToken.getToken());

			BaseEntity THM_DROPDOWN_GROUP_LABEL_GENNY = VertxUtils.getObject(serviceToken.getRealm(), "",
					"THM_DROPDOWN_GROUP_LABEL_GENNY", BaseEntity.class, serviceToken.getToken());

			BaseEntity THM_DROPDOWN_CONTENT_WRAPPER_GENNY = VertxUtils.getObject(serviceToken.getRealm(), "",
					"THM_DROPDOWN_CONTENT_WRAPPER_GENNY", BaseEntity.class, serviceToken.getToken());

			BaseEntity THM_BOX_SHADOW_SM = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BOX_SHADOW_SM",
					BaseEntity.class, serviceToken.getToken());

			BaseEntity THM_DROPDOWN_VCL_GENNY = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_DROPDOWN_VCL_GENNY",
					BaseEntity.class, serviceToken.getToken());

			BaseEntity THM_IMAGE_PLACEHOLDER = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_IMAGE_PLACEHOLDER",
					BaseEntity.class, serviceToken.getToken());
			BaseEntity THM_HEADER_PROFILE_PICTURE = VertxUtils.getObject(serviceToken.getRealm(), "",
					"THM_HEADER_PROFILE_PICTURE", BaseEntity.class, serviceToken.getToken());
			BaseEntity THM_BORDER_RADIUS_50 = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BORDER_RADIUS_50",
					BaseEntity.class, serviceToken.getToken());
			BaseEntity THM_EXPANDABLE = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_EXPANDABLE", BaseEntity.class,
					serviceToken.getToken());
			BaseEntity THM_WIDTH_100_PERCENT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_WIDTH_100_PERCENT",
					BaseEntity.class, serviceToken.getToken());
			BaseEntity THM_JUSTIFY_CONTENT_CENTRE = VertxUtils.getObject(serviceToken.getRealm(), "",
					"THM_JUSTIFY_CONTENT_CENTRE", BaseEntity.class, serviceToken.getToken());
			Theme THM_IMAGE_PLACEHOLDER_PERSON = VertxUtils.getObject(serviceToken.getRealm(), "",
					"THM_IMAGE_PLACEHOLDER_PERSON", Theme.class, serviceToken.getToken());
			BaseEntity THM_PROFILE_IMAGE = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_PROFILE_IMAGE",
					BaseEntity.class, serviceToken.getToken());
			BaseEntity THM_PROJECT_COLOR_SURFACE = VertxUtils.getObject(serviceToken.getRealm(), "",
					"THM_PROJECT_COLOR_SURFACE", BaseEntity.class, serviceToken.getToken());
			BaseEntity THM_PADDING_X_10 = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_PADDING_X_10",
					BaseEntity.class, serviceToken.getToken());

			BaseEntity THM_FLEX_ONE = new BaseEntity("THM_FLEX_ONE", "Theme Flex One");
			Attribute contentAttribute = new Attribute("PRI_CONTENT", "content", new DataType(String.class));
			Attribute inheritableAttribute = new Attribute("PRI_IS_INHERITABLE", "inheritable", new DataType(Boolean.class));
			EntityAttribute entAttr = new EntityAttribute(THM_FLEX_ONE, contentAttribute, 1.0, "{  \"flex\": 1 }");
			EntityAttribute inheritEntAtt = new EntityAttribute(THM_FLEX_ONE, inheritableAttribute, 1.0, "FALSE");
			Set<EntityAttribute> entAttrSet = new HashSet<>();
			entAttrSet.add(entAttr);
			entAttrSet.add(inheritEntAtt);
			THM_FLEX_ONE.setBaseEntityAttributes(entAttrSet);

			BaseEntity THM_FLEX_ONE2 = new BaseEntity("THM_FLEX_ONE2", "Theme Flex One");
			Set<EntityAttribute> entAttrSets = new HashSet<>();
			entAttrSets.add(entAttr);
			THM_FLEX_ONE2.setBaseEntityAttributes(entAttrSets);

			beList.add(THM_DISPLAY_VERTICAL);
			beList.add(THM_DISPLAY_HORIZONTAL);
			beList.add(tableUtils.getThemeBe(THM_CARD));
			beList.add(THM_FLEX_ONE);
			beList.add(THM_FLEX_ONE2);
			beList.add(THM_PROJECT_COLOR_SURFACE);
			beList.add(THM_PADDING_X_10);

			String sourceCode = "PER_SERVICE";
			String targetCode = "PER_SERVICE";

			Attribute questionAttribute = new Attribute("QQQ_QUESTION_GROUP", "link", new DataType(String.class));

			// card ask
			Question cardQuestion = new Question("QUE_CARD_APPLICATION_TEMPLATE_GRP", "Card", questionAttribute, true);
			Ask cardAsk = new Ask(cardQuestion, sourceCode, targetCode);
			cardAsk = rules.createVirtualContext(cardAsk, THM_DISPLAY_HORIZONTAL, ContextType.THEME,
					VisualControlType.GROUP_CONTENT_WRAPPER);
			cardAsk = rules.createVirtualContext(cardAsk, tableUtils.getThemeBe(THM_CARD), ContextType.THEME,
					VisualControlType.GROUP_WRAPPER);

			// status ask
			Question cardStatusQuestion = new Question("QUE_CARD_STATUS_GRP", "Card Status", questionAttribute, true);
			Ask cardStatusAsk = new Ask(cardStatusQuestion, sourceCode, targetCode);
			cardStatusAsk = rules.createVirtualContext(cardStatusAsk, THM_FLEX_ONE, ContextType.THEME,
					VisualControlType.GROUP_CONTENT_WRAPPER);
			cardStatusAsk = rules.createVirtualContext(cardStatusAsk, THM_FLEX_ONE2, ContextType.THEME,
					VisualControlType.VCL_WRAPPER);
			cardStatusAsk = rules.createVirtualContext(cardStatusAsk, THM_DROPDOWN_ICON_ALT, ContextType.THEME,
					VisualControlType.GROUP_ICON);

			// main ask
			Question cardMainQuestion = new Question("QUE_CARD_MAIN_GRP", "Card Main", questionAttribute, true);
			Ask cardMainAsk = new Ask(cardMainQuestion, sourceCode, targetCode);
			cardMainAsk = rules.createVirtualContext(cardMainAsk, THM_DISPLAY_VERTICAL, ContextType.THEME,
					VisualControlType.GROUP_CONTENT_WRAPPER);
			cardMainAsk = rules.createVirtualContext(cardMainAsk, THM_FLEX_ONE, ContextType.THEME,
					VisualControlType.GROUP_WRAPPER);
			cardMainAsk.setReadonly(true);

			// content ask
			Question cardContentQuestion = new Question("QUE_CARD_CONTENT_GRP", "Card Content", questionAttribute, true);
			Ask cardContentAsk = new Ask(cardContentQuestion, sourceCode, targetCode);
			cardContentAsk = rules.createVirtualContext(cardContentAsk, THM_DISPLAY_HORIZONTAL, ContextType.THEME,
					VisualControlType.GROUP_CONTENT_WRAPPER);

			// left ask
			Question cardLeftQuestion = new Question("QUE_CARD_LEFT_GRP", "Card Content", questionAttribute, true);
			Ask cardLeftAsk = new Ask(cardLeftQuestion, sourceCode, targetCode);

			// cardLeftAsk = rules.createVirtualContext(cardLeftAsk, THM_IMAGE_PLACEHOLDER,
			// ContextType.THEME, VisualControlType.INPUT_PLACEHOLDER);
			cardLeftAsk = rules.createVirtualContext(cardLeftAsk, THM_HEADER_PROFILE_PICTURE, ContextType.THEME,
					VisualControlType.INPUT_SELECTED);
			cardLeftAsk = rules.createVirtualContext(cardLeftAsk, THM_BORDER_RADIUS_50, ContextType.THEME,
					VisualControlType.INPUT_FIELD);

			cardLeftAsk = rules.createVirtualContext(cardLeftAsk, THM_PROFILE_IMAGE, ContextType.THEME,
					VisualControlType.INPUT_SELECTED);
			cardLeftAsk = rules.createVirtualContext(cardLeftAsk, tableUtils.getThemeBe(THM_IMAGE_PLACEHOLDER_PERSON),
					ContextType.THEME, VisualControlType.INPUT_PLACEHOLDER);

			// centre ask
			Question cardCentreQuestion = new Question("QUE_CARD_CENTRE_GRP", "Card Content", questionAttribute, true);
			Ask cardCentreAsk = new Ask(cardCentreQuestion, sourceCode, targetCode);
			cardCentreAsk = rules.createVirtualContext(cardCentreAsk, THM_DISPLAY_VERTICAL, ContextType.THEME,
					VisualControlType.GROUP_CONTENT_WRAPPER);
			cardCentreAsk = rules.createVirtualContext(cardCentreAsk, THM_FLEX_ONE, ContextType.THEME,
					VisualControlType.GROUP_WRAPPER);

			// right ask
			Question cardRightQuestion = new Question("QUE_CARD_RIGHT_GRP", "Card Right", questionAttribute, true);
			Ask cardRightAsk = new Ask(cardRightQuestion, sourceCode, targetCode);
			cardRightAsk = rules.createVirtualContext(cardRightAsk, THM_DISPLAY_VERTICAL, ContextType.THEME,
					VisualControlType.GROUP_CONTENT_WRAPPER);
			cardRightAsk = rules.createVirtualContext(cardRightAsk, THM_DROPDOWN_BEHAVIOUR_GENNY, ContextType.THEME,
					VisualControlType.GROUP);
			cardRightAsk = rules.createVirtualContext(cardRightAsk, THM_DROPDOWN_PROFILE_BEHAVIOUR_GENNY, ContextType.THEME,
					VisualControlType.GROUP, 1.0);
			cardRightAsk = rules.createVirtualContext(cardRightAsk, THM_PROJECT_COLOR_SURFACE, ContextType.THEME,
					VisualControlType.GROUP_CONTENT_WRAPPER, 1.0);

			Question cardForwardQuestion = new Question("QUE_FORWARD", "Forward", questionAttribute, true);
			Ask cardForwardAsk = new Ask(cardForwardQuestion, sourceCode, targetCode);

			Question cardBackwardQuestion = new Question("QUE_BACKWARD", "Backward", questionAttribute, true);
			Ask cardBackwardAsk = new Ask(cardBackwardQuestion, sourceCode, targetCode);

			// Ask[] cardRightChildAsks = { cardViewAsk, cardEditAsk, cardDeleteAsk };
			Ask[] cardRightChildAsks = { cardForwardAsk, cardBackwardAsk };
			cardRightAsk.setChildAsks(cardRightChildAsks);

			Ask[] cardContentChildAsks = { cardLeftAsk, cardCentreAsk, cardRightAsk };
			cardContentAsk.setChildAsks(cardContentChildAsks);

			// bottom ask
			Question cardBottomQuestion = new Question("QUE_CARD_BOTTOM_GRP", "Card Bottom", questionAttribute, true);
			Ask cardBottomAsk = new Ask(cardBottomQuestion, sourceCode, targetCode);
			cardBottomAsk = rules.createVirtualContext(cardBottomAsk, THM_JUSTIFY_CONTENT_CENTRE, ContextType.THEME,
					VisualControlType.GROUP_CLICKABLE_WRAPPER);
			cardBottomAsk = rules.createVirtualContext(cardBottomAsk, THM_EXPANDABLE, ContextType.THEME,
					VisualControlType.GROUP);
			cardBottomAsk = rules.createVirtualContext(cardBottomAsk, THM_WIDTH_100_PERCENT, ContextType.THEME,
					VisualControlType.GROUP);
			cardBottomAsk = rules.createVirtualContext(cardBottomAsk, THM_PADDING_X_10, ContextType.THEME,
					VisualControlType.GROUP_CONTENT_WRAPPER);

			Ask[] cardMainChildAsks = { cardContentAsk, cardBottomAsk };
			cardMainAsk.setChildAsks(cardMainChildAsks);

			Ask[] cardChildAsks = { cardStatusAsk, cardMainAsk };
			cardAsk.setChildAsks(cardChildAsks);

			return cardAsk;

		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;

	}

	public List<SearchEntity> getBucketSearchBeListFromCache() {

		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
		TableUtilsTest tableUtils = new TableUtilsTest(beUtils);

		List<SearchEntity> bucketSearchBeList = new ArrayList<SearchEntity>();

		try {
				SearchEntity SBE_APPLIED_APPLICATIONS = VertxUtils.getObject(serviceToken.getRealm(), "",
								"SBE_APPLIED_APPLICATIONS", SearchEntity.class, serviceToken.getToken());
				SearchEntity SBE_SHORTLISTED_APPLICATIONS = VertxUtils.getObject(serviceToken.getRealm(), "",
								"SBE_SHORTLISTED_APPLICATIONS", SearchEntity.class, serviceToken.getToken());
				SearchEntity SBE_INTERVIEWED_APPLICATIONS = VertxUtils.getObject(serviceToken.getRealm(), "",
								"SBE_INTERVIEWED_APPLICATIONS", SearchEntity.class, serviceToken.getToken());
				SearchEntity SBE_OFFERED_APPLICATIONS = VertxUtils.getObject(serviceToken.getRealm(), "",
								"SBE_OFFERED_APPLICATIONS", SearchEntity.class, serviceToken.getToken());
				SearchEntity SBE_PLACED_APPLICATIONS = VertxUtils.getObject(serviceToken.getRealm(), "",
								"SBE_PLACED_APPLICATIONS", SearchEntity.class, serviceToken.getToken());
				SearchEntity SBE_INPROGRESS_APPLICATIONS = VertxUtils.getObject(serviceToken.getRealm(), "",
								"SBE_INPROGRESS_APPLICATIONS", SearchEntity.class, serviceToken.getToken());

				bucketSearchBeList.add(SBE_APPLIED_APPLICATIONS);
				bucketSearchBeList.add(SBE_SHORTLISTED_APPLICATIONS);
				bucketSearchBeList.add(SBE_INTERVIEWED_APPLICATIONS);
				bucketSearchBeList.add(SBE_OFFERED_APPLICATIONS);
				bucketSearchBeList.add(SBE_PLACED_APPLICATIONS);
				bucketSearchBeList.add(SBE_INPROGRESS_APPLICATIONS);

		} catch (Exception e) {

		}
		return bucketSearchBeList;
	}

	public Ask getBucketHeaderAsk(Map<String, ContextList> contextListMap, GennyToken serviceToken) {

		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
		TableUtilsTest tableUtils = new TableUtilsTest(beUtils);

		Theme THM_QUESTION_GRP_LABEL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_QUESTION_GRP_LABEL",
				Theme.class, serviceToken.getToken());
		Theme THM_DISPLAY_VERTICAL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_DISPLAY_VERTICAL", Theme.class,
				serviceToken.getToken());
		Theme THM_DISPLAY_HORIZONTAL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_DISPLAY_HORIZONTAL",
				Theme.class, serviceToken.getToken());
		Theme THM_WIDTH_100_PERCENT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_WIDTH_100_PERCENT",
				Theme.class, serviceToken.getToken());
		Theme THM_BH_ROW_ONE_GRP_WRAPPER = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BH_ROW_ONE_GRP_WRAPPER",
				Theme.class, serviceToken.getToken());
		Theme THM_BH_ROW_ONE_GRP_LABEL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BH_ROW_ONE_GRP_LABEL",
				Theme.class, serviceToken.getToken());
		Theme THM_BH_ROW_ONE_GRP_CONTENT_WRAPPER = VertxUtils.getObject(serviceToken.getRealm(), "",
				"THM_BH_ROW_ONE_GRP_CONTENT_WRAPPER", Theme.class, serviceToken.getToken());
		Theme THM_BH_ROW_ONE_VCL_INPUT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BH_ROW_ONE_VCL_INPUT",
				Theme.class, serviceToken.getToken());
		Theme THM_BH_ROW_TWO_VCL_WRAPPER = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BH_ROW_TWO_VCL_WRAPPER",
				Theme.class, serviceToken.getToken());
		Theme THM_BH_ROW_TWO_GRP_CONTENT_WRAPPER = VertxUtils.getObject(serviceToken.getRealm(), "",
				"THM_BH_ROW_TWO_GRP_CONTENT_WRAPPER", Theme.class, serviceToken.getToken());
		Theme THM_BH_ROW_TWO_INPUT_FIELD = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BH_ROW_TWO_INPUT_FIELD",
				Theme.class, serviceToken.getToken());
		Theme THM_ICON = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_ICON", Theme.class,
				serviceToken.getToken());
		Theme THM_BH_GROUP_WRAPPER = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BH_GROUP_WRAPPER", Theme.class,
				serviceToken.getToken());
		
		BaseEntity ICN_SORT = beUtils.getBaseEntityByCode("ICN_SORT");

		/* 
			we create context here 
		*/

		/* row1Context context */
		List<Context> row1Context = new ArrayList<>();
		row1Context.add(new Context(ContextType.THEME, tableUtils.getThemeBe(THM_DISPLAY_HORIZONTAL), VisualControlType.GROUP_WRAPPER, 1.0));
		row1Context.add(new Context(ContextType.THEME, tableUtils.getThemeBe(THM_BH_ROW_ONE_GRP_WRAPPER), VisualControlType.GROUP_WRAPPER, 1.0));
		row1Context.add(new Context(ContextType.THEME, tableUtils.getThemeBe(THM_BH_ROW_ONE_GRP_LABEL), VisualControlType.GROUP_LABEL, 1.0));
		row1Context.add(new Context(ContextType.THEME, tableUtils.getThemeBe(THM_BH_ROW_ONE_GRP_CONTENT_WRAPPER), VisualControlType.GROUP_CONTENT_WRAPPER, 1.0));
		row1Context.add(new Context(ContextType.THEME, tableUtils.getThemeBe(THM_BH_ROW_ONE_VCL_INPUT), VisualControlType.VCL_INPUT, 1.0));

		/* row2Context context */
		List<Context> row2Context = new ArrayList<>();
		row2Context.add(new Context(ContextType.THEME, tableUtils.getThemeBe(THM_DISPLAY_HORIZONTAL), VisualControlType.GROUP_CONTENT_WRAPPER, 1.0));
		row2Context.add(new Context(ContextType.THEME, tableUtils.getThemeBe(THM_BH_ROW_TWO_VCL_WRAPPER), VisualControlType.VCL_WRAPPER, 1.0));
		row2Context.add(new Context(ContextType.THEME, tableUtils.getThemeBe(THM_BH_ROW_TWO_GRP_CONTENT_WRAPPER), VisualControlType.GROUP_CONTENT_WRAPPER, 1.0));

		
		/* bucketCountContextList context */
		List<Context> bucketCountContextList = new ArrayList<>();
		bucketCountContextList.add(new Context(ContextType.THEME, tableUtils.getThemeBe(THM_QUESTION_GRP_LABEL), VisualControlType.GROUP_WRAPPER, 1.0));
		
		/* bucketSearchContextList context */
		List<Context> bucketSearchContextList = new ArrayList<>();
		bucketSearchContextList.add(new Context(ContextType.THEME, tableUtils.getThemeBe(THM_BH_ROW_TWO_INPUT_FIELD), VisualControlType.VCL_WRAPPER, 1.0));
		
		/* bucketSortContextList context */
		List<Context> bucketSortContextList = new ArrayList<>();
		bucketSortContextList.add(new Context(ContextType.THEME, tableUtils.getThemeBe(THM_ICON), VisualControlType.VCL, 1.0));
		bucketSortContextList.add(new Context(ContextType.ICON, ICN_SORT, VisualControlType.VCL_ICON, 1.0));
		
		/* add the contextList to contextMap */
		contextListMap.put("QUE_BUCKET_HEADER_ROW_ONE_GRP", new ContextList(row1Context));
		contextListMap.put("QUE_BUCKET_HEADER_ROW_TWO_GRP", new ContextList(row2Context));
		contextListMap.put("QUE_BUCKET_COUNT", new ContextList(bucketCountContextList));  
		contextListMap.put("QUE_BUCKET_SEARCH", new ContextList(bucketSearchContextList));
		contextListMap.put("QUE_BUCKET_SORT", new ContextList(bucketSortContextList));

		/* Validation for Search Attribute */
		Validation validation = new Validation("VLD_NON_EMPTY", "EmptyandBlankValues", "(?!^$|\\s+)");
		List<Validation> validations = new ArrayList<>();
		validations.add(validation);
		ValidationList searchValidationList = new ValidationList();
		searchValidationList.setValidationList(validations);

		Attribute countAttribute = RulesUtils.getAttribute("PRI_TOTAL_RESULTS", serviceToken.getToken());
		Attribute sortAttribute = RulesUtils.getAttribute("PRI_SORT", serviceToken.getToken());
		Attribute nameAttribute = RulesUtils.getAttribute("PRI_NAME", serviceToken.getToken());

		Attribute searchAttribute = new Attribute("PRI_NAME", "Search", new DataType("Text", searchValidationList, "Text"));

		Attribute questionAttribute = RulesUtils.getAttribute("QQQ_QUESTION_GROUP", serviceToken.getToken());
		Attribute tableCellAttribute = RulesUtils.getAttribute("QQQ_QUESTION_GROUP_TABLE_CELL", serviceToken.getToken());

		/* Initialize Bucket Header Ask group */
		Question bucketHeaderQuestion = new Question("QUE_BUCKET_HEADER_GRP", "Bucket Header", questionAttribute, true);
		Ask bucketHeaderAsk = new Ask(bucketHeaderQuestion, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");

		/* row-one-ask */
		Question row1Ques = new Question("QUE_BUCKET_HEADER_ROW_ONE_GRP", "SearchEntity Name", tableCellAttribute,false);
		Ask row1Ask = new Ask(row1Ques, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");

		/* count ask */
		Question bucketCountQues = new Question("QUE_BUCKET_COUNT", countAttribute.getName(), countAttribute, false);
		Ask bucketCountAsk = new Ask(bucketCountQues, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");

		Ask[] row1ChildAsks = { bucketCountAsk };
		row1Ask = tableUtils.createVirtualContext(row1Ask, getThemeBe(THM_QUESTION_GRP_LABEL), ContextType.THEME,
				VisualControlType.GROUP);
		row1Ask.setChildAsks(row1ChildAsks);

		/* row-two-ask */
		Question row2Ques = new Question("QUE_BUCKET_HEADER_ROW_TWO_GRP", questionAttribute.getName(), questionAttribute, false);
		Ask row2Ask = new Ask(row2Ques, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");

		/* search ask */
		Question bucketSearchQues = new Question("QUE_BUCKET_SEARCH", searchAttribute.getName(), searchAttribute, false);
		Ask bucketSearchAsk = new Ask(bucketSearchQues, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");

		bucketSearchAsk = tableUtils.createVirtualContext(bucketSearchAsk, getThemeBe(THM_BH_ROW_TWO_INPUT_FIELD),
				ContextType.THEME, VisualControlType.VCL_WRAPPER);

		/* sort ask */
		Question bucketSortQues = new Question("QUE_BUCKET_SORT", sortAttribute.getName(), sortAttribute, false);
		Ask bucketSortAsk = new Ask(bucketSortQues, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");

		Ask[] row2ChildAsks = { bucketSearchAsk, bucketSortAsk };
		row2Ask.setChildAsks(row2ChildAsks);

		/* set the bucketHeader child asks */
		Ask[] bucketChildAsks = { row1Ask, row2Ask };
		bucketHeaderAsk.setChildAsks(bucketChildAsks);
		
		return bucketHeaderAsk;
	}

}