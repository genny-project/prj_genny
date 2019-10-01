package life.genny.test;

import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

	public AdamTest() {

	}

	@Test
	public void searchTableTest()
	{
		System.out.println("SearchTable Test");
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
		
		Integer pageSize = 30;
		Integer pageIndex = 0;
		
        SearchEntity searchBE = new SearchEntity("SBE_TEST","Adam Search")
 	     .addSort("PRI_NAME","Name",SearchEntity.Sort.ASC)
 	     .addFilter("PRI_NAME",SearchEntity.StringFilter.LIKE,"%%")
	  		  	     .addColumn("PRI_NAME", "Name")
	  		      	 .addColumn("PRI_LANDLINE", "Phone")
	  		  	     .addColumn("PRI_EMAIL", "Email")
	  		  	     .addColumn("PRI_MOBILE", "Mobile") 
	  		  	     .addColumn("PRI_ADDRESS_CITY","City")
	  		  	     .addColumn("PRI_ADDRESS_STATE","State")
 	     .setPageStart(pageIndex)
 	     .setPageSize(pageSize);
 	     
  	     
 	     searchBE.setRealm(serviceToken.getRealm());
 	     
 	    BaseEntityUtils beUtils = new BaseEntityUtils(userToken);
 	    Answer answer = new Answer(userToken.getUserCode(),userToken.getUserCode(),"PRI_SEARCH_TXT","monash");
 		TableUtils.performSearch(serviceToken , beUtils, "SBE_SEARCHBAR", answer);
 	     
 	     /* Send to front end */
  	     
  	     
 		new ShowFrame().display(userToken, "FRM_TABLE_VIEW","FRM_CONTENT", "Junit Test");

	}
	
	//@Test
	public void searchTest()
	{
		System.out.println("Search Test");
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
		
		Integer pageSize = 30;
		Integer pageIndex = 0;
		
        SearchEntity searchBE = new SearchEntity("SBE_TEST","Adam Search")
 	     .addSort("PRI_NAME","Name",SearchEntity.Sort.ASC)
 	     .addFilter("PRI_NAME",SearchEntity.StringFilter.LIKE,"%univ%")
	  		  	     .addColumn("PRI_NAME", "Name")
	  		      	 .addColumn("PRI_LANDLINE", "Phone")
	  		  	     .addColumn("PRI_EMAIL", "Email")
	  		  	     .addColumn("PRI_MOBILE", "Mobile") 
	  		  	     .addColumn("PRI_ADDRESS_CITY","City")
	  		  	     .addColumn("PRI_ADDRESS_STATE","State")
 	     .setPageStart(pageIndex)
 	     .setPageSize(pageSize);
 	     
  	     
 	     searchBE.setRealm(serviceToken.getRealm());
 	     
 	    BaseEntityUtils beUtils = new BaseEntityUtils(userToken);
 	    TableUtils tableUtils = new TableUtils(beUtils);
		QDataBaseEntityMessage msg = tableUtils.fetchSearchResults(searchBE, beUtils.getGennyToken());
		log.info("PageCount = "+msg.getReturnCount()+" , total = "+msg.getTotal());
 		 
		pageIndex += pageSize;
		searchBE.setPageStart(pageIndex);
		msg = tableUtils.fetchSearchResults(searchBE, beUtils.getGennyToken());
		log.info("PageCount = "+msg.getReturnCount()+" , total = "+msg.getTotal());

		pageIndex += pageSize;
		searchBE.setPageStart(pageIndex);
		msg = tableUtils.fetchSearchResults(searchBE, beUtils.getGennyToken());
		log.info("PageCount = "+msg.getReturnCount()+" , total = "+msg.getTotal());

		pageIndex += pageSize;
		searchBE.setPageStart(pageIndex);
		msg = tableUtils.fetchSearchResults(searchBE, beUtils.getGennyToken());
		log.info("PageCount = "+msg.getReturnCount()+" , total = "+msg.getTotal());

		pageIndex += pageSize;
		searchBE.setPageStart(pageIndex);
		msg = tableUtils.fetchSearchResults(searchBE, beUtils.getGennyToken());
		log.info("PageCount = "+msg.getReturnCount()+" , total = "+msg.getTotal());

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



	
	//@Test
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
	
    // @Test
    public void virtualAskAndContextTest() {
            QRules rules = GennyJbpmBaseTest.setupLocalService();
            GennyToken userToken = new GennyToken("userToken", rules.getToken());
            GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

            rules.sendAllAttributes();

            Frame3 FRM_HEADER = this.generateHeader();

            Theme THM_MAIN = Theme.builder("THM_MAIN").addAttribute().backgroundColor("grey").color("#18639F")
                            // .height(80)
                            .end().build();

            Frame3 FRM_MAIN = Frame3.builder("FRM_MAIN").addTheme(THM_MAIN).end().question("QUE_NAME_TWO").end()
                            .build();

            try {
                    /* frame-root */
                    Frame3 FRM_ROOT = Frame3.builder("FRM_ROOT").addFrame(FRM_HEADER, FramePosition.NORTH).end()
                                    .addFrame(FRM_MAIN, FramePosition.CENTRE).end().build();

                    Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();
                    QDataBaseEntityMessage msg = FrameUtils2.toMessage(FRM_ROOT, serviceToken, askMsgs);
                    rules.publishCmd(msg);
                    for (QDataAskMessage askMsg : askMsgs) {
                            rules.publishCmd(askMsg, serviceToken.getUserCode(), userToken.getUserCode());
                    }
                    System.out.println("Sent");
            } catch (Exception e) {
                    System.out.println("Failed " + e.getLocalizedMessage());
            }

    }

    public Frame3 generateHeader() {
            QRules rules = GennyJbpmBaseTest.setupLocalService();
            GennyToken userToken = new GennyToken("userToken", rules.getToken());
            GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
            BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
            BaseEntity project = beUtils.getBaseEntityByCode("PRJ_" + serviceToken.getRealm().toUpperCase());

            try {

                    Theme THM_DROPDOWN_BEHAVIOUR_GENNY = Theme.builder("THM_DROPDOWN_BEHAVIOUR_GENNY")
                                    .addAttribute(ThemeAttributeType.PRI_IS_DROPDOWN, true).end()
                                    .addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_LABEL, true).end()
                                    .addAttribute(ThemeAttributeType.PRI_IS_QUESTION_GRP_LABEL_CLICKABLE, true)
                                    .end().addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                    Theme THM_DROPDOWN_PROFILE_BEHAVIOUR_GENNY = Theme.builder("THM_DROPDOWN_PROFILE_BEHAVIOUR_GENNY")
                                    .addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_LABEL, false).end()
                                    .build();

                    Theme THM_BACKGROUND_NONE = Theme.builder("THM_BACKGROUND_NONE").addAttribute()
                                    .backgroundColor("none").end().build();

                    Theme THM_DROPDOWN_HEADER_WRAPPER_GENNY = Theme.builder("THM_DROPDOWN_HEADER_WRAPPER_GENNY")
                                    .addAttribute()
                                            .padding(5)
                                            .backgroundColor(project.getValue("PRI_COLOR_PRIMARY_VARIANT_LIGHT", "#395268"))
                                    .end()
                                    .build();

                    Theme THM_DROPDOWN_GROUP_LABEL_GENNY = Theme.builder("THM_DROPDOWN_GROUP_LABEL_GENNY")
                                    .addAttribute().marginBottom(0).size("sm").bold(false).end().build();

                    Theme THM_DROPDOWN_CONTENT_WRAPPER_GENNY = Theme.builder("THM_DROPDOWN_CONTENT_WRAPPER_GENNY")
                                    .addAttribute()
                                    .backgroundColor(project.getValue("PRI_COLOR_SURFACE", "#FFFFFF"))
                                    .color("green").width(200).end()
                                    .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                    Theme THM_BOX_SHADOW_SM = Theme.builder("THM_BOX_SHADOW_SM").addAttribute().shadowColor("#000")
                                    .shadowOpacity(0.4).shadowRadius(10).shadowOffset().width(0).height(0).end()
                                    .end().addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                    Theme THM_DROPDOWN_VCL_GENNY = Theme.builder("THM_DROPDOWN_VCL_GENNY").addAttribute()
                                    .color(project.getValue("PRI_COLOR_SURFACE_ON", "#000000"))
                                    .end().build();

                    Theme THM_DASHBOARD_ITEM_INPUT = Theme.builder("THM_DASHBOARD_ITEM_INPUT")
                                                    .addAttribute().dynamicWidth(true).end()
                                                    .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                                    .build();

                    Theme THM_HEADER_PROFILE_PICTURE = Theme.builder("THM_HEADER_PROFILE_PICTURE")
                                            .addAttribute()
                                                    .height(32)
                                                    .width(32)
                                                    .fit("cover")
                                                    .borderRadius(50)
                                            .end()
                                            .build();
                    
                    Theme THM_PANEL_CONTENT_FIT = Theme.builder("THM_PANEL_CONTENT_FIT")
                                            .addAttribute()
                                                    .flexShrink(0)
                                                    .flexBasis("auto")
                                                    .flexGrow(0)
                                                    .width("auto")
                                            .end()
                                            .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                            .build();

                    Theme THM_PADDING_RIGHT_10 = Theme.builder("THM_PADDING_RIGHT_10")
                                            .addAttribute()
                                                    .paddingRight(10)
                                            .end()
                                            .build();
                    
                    Theme THM_BORDER_RADIUS_50 = Theme.builder("THM_BORDER_RADIUS_50")
                                            .addAttribute()
                                                    .borderRadius(50)
                                            .end()
                                            .build();

                    Frame3 FRM_HEADER_FIRSTNAME = Frame3.builder("FRM_HEADER_FIRSTNAME")                               
                                            .question("QUE_FIRSTNAME")
                                            .end()
                                            .build();
                    Frame3 FRM_HEADER_LASTNAME = Frame3.builder("FRM_HEADER_LASTNAME")                                        
                                            .question("QUE_LASTNAME")
                                            .end()
                                            .build();

                    Frame3 FRM_HEADER_USERNAME = Frame3.builder("FRM_HEADER_USERNAME")                                                         
                                            .addTheme(THM_PANEL_CONTENT_FIT, ThemePosition.WRAPPER).end()
                                            .question("QUE_NAME")
                                                    .targetAlias("PER_USER1")
                                                    .addTheme(THM_DASHBOARD_ITEM_INPUT).vcl(VisualControlType.VCL_INPUT).end()
                                            .end()
                                            .build();

                    
                    Frame3 FRM_HEADER_PROFILE_PICTURE = Frame3.builder("FRM_HEADER_PROFILE_PICTURE")                                                                                        
                                            .addTheme(THM_PANEL_CONTENT_FIT, ThemePosition.WRAPPER).end()
                                            .addTheme(THM_PADDING_RIGHT_10, ThemePosition.WRAPPER).end()
                                            .question("QUE_IMAGE_URL")
                                                    .addTheme(THM_HEADER_PROFILE_PICTURE).vcl(VisualControlType.VCL_INPUT).end()
                                                    .addTheme(THM_BORDER_RADIUS_50).vcl(VisualControlType.INPUT_FIELD).end()
                                            .end()
                                            .build();

                    Frame3 FRM_HEADER_OPTIONS = Frame3.builder("FRM_HEADER_OPTIONS")
                                            .addTheme(THM_PANEL_CONTENT_FIT, ThemePosition.WRAPPER).end()
                                            .question("QUE_OPTIONS_GRP")
                                                    .addTheme(THM_BACKGROUND_NONE).vcl(VisualControlType.GROUP).weight(1.0).end()
                                                    .addTheme(THM_DROPDOWN_BEHAVIOUR_GENNY).vcl(VisualControlType.GROUP).weight(2.0).end()
                                                    .addTheme(THM_DROPDOWN_PROFILE_BEHAVIOUR_GENNY).vcl(VisualControlType.GROUP).weight(1.0).end()
                                                    .addTheme(THM_DROPDOWN_HEADER_WRAPPER_GENNY).vcl(VisualControlType.GROUP_HEADER_WRAPPER).weight(2.0).end()
                                                    .addTheme(THM_DROPDOWN_GROUP_LABEL_GENNY)
                                                            .vcl(VisualControlType.GROUP_LABEL).end()
                                                    .addTheme(THM_DROPDOWN_CONTENT_WRAPPER_GENNY)
                                                            .vcl(VisualControlType.GROUP_CONTENT_WRAPPER).end()
                                                    .addTheme(THM_BOX_SHADOW_SM)
                                                            .vcl(VisualControlType.GROUP_CONTENT_WRAPPER).end()
                                                    .addTheme(THM_DROPDOWN_VCL_GENNY)
                                                            .vcl(VisualControlType.VCL).end()
                                            .end()
                                            .build();

                    Frame3 FRM_HEADER_ADD_ITEMS = Frame3.builder("FRM_HEADER_ADD_ITEMS")
                                    .addTheme(THM_PANEL_CONTENT_FIT, ThemePosition.WRAPPER).end()
                                    .question("QUE_ADD_ITEMS_GRP").addTheme(THM_BACKGROUND_NONE)
                                    .vcl(VisualControlType.GROUP).weight(2.0).end()
                                    .addTheme(THM_DROPDOWN_BEHAVIOUR_GENNY).vcl(VisualControlType.GROUP).end()
                                    .addTheme(THM_DROPDOWN_HEADER_WRAPPER_GENNY)
                                    .vcl(VisualControlType.GROUP_HEADER_WRAPPER).end()
                                    .addTheme(THM_DROPDOWN_GROUP_LABEL_GENNY).vcl(VisualControlType.GROUP_LABEL)
                                    .end().addTheme(THM_DROPDOWN_CONTENT_WRAPPER_GENNY)
                                    .vcl(VisualControlType.GROUP_CONTENT_WRAPPER).end().addTheme(THM_BOX_SHADOW_SM)
                                    .vcl(VisualControlType.GROUP_CONTENT_WRAPPER).end()
                                    .addTheme(THM_DROPDOWN_VCL_GENNY).vcl(VisualControlType.VCL).end().end()
                                    .build();

                    Theme THM_PADDING = Theme.builder("THM_PADDING").addAttribute().padding(10).flexGrow(0)
                                    .flexShrink(0).width(0).flexBasis("initial").end().build();

                    Frame3 FRM_PADDING = Frame3.builder("FRM_PADDING").addTheme(THM_PADDING).end().build();

                    Theme THM_HEADER = Theme.builder("THM_HEADER")
                                    .addAttribute()
                                            .height(80)
                                            //.paddingRight(20)
                                    .end()
                                    .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                    .build();

                    BaseEntity sortIconBe = beUtils.getBaseEntityByCode("ICN_SORT");

                    Context context = new Context(ContextType.ICON, sortIconBe, VisualControlType.VCL_ICON, 1.0);

                    /* Test Context */
                    Frame3 FRM_HAMBURGER_MENU = Frame3.builder("FRM_HAMBURGER_MENU").question("QUE_NAME_TWO")
                                    .addContext(context).end()
                                    .end().build();

                    /* Test Virtual Ask */
                    /*
                     * Frame3 FRM_HAMBURGER_MENU = Frame3.builder("FRM_HAMBURGER_MENU")
                     * .question("PRI-EVENT", "Nothing") .addContext(context).end() .end() .build();
                     *
                     */

                    Theme THM_FRAME_ALIGN_EAST = Theme.builder("THM_FRAME_ALIGN_EAST").addAttribute()
                                    .marginLeft("auto").flexGrow(0).flexBasis("initial").end()
                                    .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();
                    Theme THM_FRAME_ALIGN_WEST = Theme.builder("THM_FRAME_ALIGN_WEST").addAttribute()
                                    .marginRight("auto").flexGrow(0).flexBasis("initial").end()
                                    .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();
                 

                    Theme THM_SEARCH_BAR = Theme.builder("THM_SEARCH_BAR")
                                    .addAttribute()
                                            .backgroundColor("white")
                                            .color("black")
                                            .end()
                                            .build();
                                            
                    Theme THM_SEARCH_BAR_WRAPPER = Theme.builder("THM_SEARCH_BAR_WRAPPER")
                                            .addAttribute()
                                                    .maxWidth(700)
                                                    .padding(10)
                                                    .borderRadius(5)
                                                    .width("100%")
                                            .end()
                                            .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                            .build();
                    
                    Theme THM_SEARCH_BAR_CENTRE = Theme.builder("THM_SEARCH_BAR_CENTRE")
                                            .addAttribute()
                                                    .justifyContent("flex-start")
                                                    .flexDirection("row")
                                            .end()
                                            .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                            .build();
                    
                    
                    Frame3 FRM_SEARCH_BAR = Frame3.builder("FRM_SEARCH_BAR")
                                    .addTheme(THM_SEARCH_BAR_CENTRE, ThemePosition.CENTRE).end()
                                    .question("QUE_SEARCH")
                                            .addTheme(THM_SEARCH_BAR)
                                            .vcl(VisualControlType.VCL)
                                            .end()
                                            .addTheme(THM_SEARCH_BAR_WRAPPER)
                                            .vcl(VisualControlType.VCL_WRAPPER)
                                            .end()
                                    .end()
                                    .build();
                    
                    Theme THM_PROJECT_NAME = Theme.builder("THM_PROJECT_NAME")
                                    .addAttribute()
                                            .flexGrow(0)
                                    .end()
                                    .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE,false).end()
                                    .build();


                    Frame3  FRM_PROJECT_NAME = Frame3.builder("FRM_PROJECT_NAME")
                                    .addTheme("THM_SIDEBAR_WIDTH", ThemePosition.WRAPPER, serviceToken).end()                        
                                    .addTheme(THM_PROJECT_NAME, ThemePosition.WRAPPER).end()                        
                                    .question("QUE_NAME_TWO")
                                            .targetAlias("PRJ_"+serviceToken.getRealm().toUpperCase())
                                            .addTheme("THM_TITLE_LABEL", serviceToken).vcl(VisualControlType.VCL_LABEL).end()
                                    .end()
                                    .build();


                    Frame3 FRM_HEADER = Frame3.builder("FRM_HEADER").addTheme(THM_HEADER).end()
                                    .addTheme(THM_HEADER).end()
                                    //.addTheme(THM_FRAME_ALIGN_WEST, ThemePosition.WEST).end()
                                    //.addTheme(THM_FRAME_ALIGN_EAST, ThemePosition.EAST).end()
                                    .addFrame(FRM_HAMBURGER_MENU, FramePosition.WEST).end()
                                    .addFrame(FRM_PROJECT_NAME, FramePosition.WEST).end()
                                    .addFrame(FRM_SEARCH_BAR, FramePosition.CENTRE).end()
                                    .addFrame(FRM_HEADER_OPTIONS, FramePosition.EAST).end()
                                    .addFrame(FRM_HEADER_USERNAME, FramePosition.EAST).end()
                                    .addFrame(FRM_HEADER_PROFILE_PICTURE, FramePosition.EAST).end()
                                    .addFrame(FRM_PADDING, FramePosition.EAST).end()
                                    .addFrame(FRM_HEADER_ADD_ITEMS, FramePosition.EAST).end().build();

                    System.out.println("Generated Header Frame");
                    return FRM_HEADER;

            } catch (Exception e) {
                    System.out.println("Error " + e.getLocalizedMessage());
            }
            return null;
    }

    public Frame3 generateSidebar() {
            QRules rules = GennyJbpmBaseTest.setupLocalService();
            GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
            BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
            BaseEntity project = beUtils.getBaseEntityByCode("PRJ_" + serviceToken.getRealm().toUpperCase());

            try {

                    Theme THM_SIDEBAR = Theme.builder("THM_SIDEBAR").addAttribute()
                                    .backgroundColor(project.getValue("PRI_COLOR_PRIMARY", "#233A4E")).end()
                                    .build();

                    Theme THM_SIDEBAR_WIDTH = Theme.builder("THM_SIDEBAR_WIDTH").addAttribute().minWidth(300)
                                    .width(100).end().addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
                                    .end().build();

                    Theme THM_TREE_ITEM = Theme.builder("THM_TREE_ITEM").addAttribute().color("white")
                                    .height("auto").flexGrow(0).flexBasis("auto").end().build();

                    Theme THM_LOGO = Theme.builder("THM_LOGO")
                                    .addAttribute()
                                            .fit("contain")
                                            .height(100)
                                            .width(100)
                                    .end()
                                    .build();
                    
                    Theme THM_LOGO_CENTRE = Theme.builder("THM_LOGO_CENTRE")
                                    .addAttribute()
                                            .justifyContent("center")
                                            .width("100%")
                                    .end()
                                    .build();
                    
                    Theme THM_MARGIN_BOTTOM_20 = Theme.builder("THM_MARGIN_BOTTOM_20")
                                    .addAttribute()
                                            .marginBottom(20)
                                    .end()
                                    .build();

                    Frame3 FRM_LOGO = Frame3.builder("FRM_LOGO")
                                    .addTheme(THM_TREE_ITEM, ThemePosition.WRAPPER).end()
                                    .addTheme(THM_MARGIN_BOTTOM_20, ThemePosition.WRAPPER).end()
                                    .addTheme(THM_LOGO_CENTRE, ThemePosition.CENTRE).end()
                                    .question("QUE_PROJECT_SIDEBAR_GRP")
                                            .addTheme(THM_LOGO)
                                            .vcl(VisualControlType.VCL_WRAPPER).end().end().build();

                    Theme THM_TREE_GROUP_BEHAVIOUR = Theme.builder("THM_TREE_GROUP_BEHAVIOUR")
                                    .addAttribute(ThemeAttributeType.PRI_IS_EXPANDABLE, true).end()
                                    .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                    .addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_LABEL, true).end()
                                    .addAttribute(ThemeAttributeType.PRI_IS_QUESTION_GRP_LABEL_CLICKABLE, true)
                                    .end().build();

                    Theme THM_TREE_GROUP_LABEL = Theme.builder("THM_TREE_GROUP_LABEL").addAttribute().bold(false)
                                    .size("sm").end().addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
                                    .end().build();

                    Theme THM_TREE_GROUP_WRAPPER = Theme.builder("THM_TREE_GROUP_WRAPPER").addAttribute()
                                    .width("100%").paddingX(20).end()
                                    .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                    Theme THM_TREE_GROUP_CLICKABLE_WRAPPER = Theme.builder("THM_TREE_GROUP_CLICKABLE_WRAPPER")
                                    .addAttribute().width("100%").justifyContent("space-between").end()
                                    .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                    Theme THM_TREE_GROUP_CONTENT_WRAPPER = Theme.builder("THM_TREE_GROUP_CONTENT_WRAPPER")
                                    .addAttribute().paddingLeft(10).end()
                                    .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                    Frame3 FRM_TREE_FORM_VIEW = Frame3.builder("FRM_TREE_FORM_VIEW")
                                    .addTheme(THM_TREE_ITEM, ThemePosition.WRAPPER).end()
                                    .question("QUE_TREE_FORM_VIEW_GRP").addTheme(THM_TREE_GROUP_BEHAVIOUR).end()
                                    .addTheme(THM_TREE_GROUP_CLICKABLE_WRAPPER)
                                    .vcl(VisualControlType.GROUP_CLICKABLE_WRAPPER).end()
                                    .addTheme(THM_TREE_GROUP_CONTENT_WRAPPER)
                                    .vcl(VisualControlType.GROUP_CONTENT_WRAPPER).end()
                                    .addTheme(THM_TREE_GROUP_WRAPPER).vcl(VisualControlType.GROUP_WRAPPER).end()
                                    .addTheme(THM_TREE_GROUP_LABEL).vcl(VisualControlType.GROUP_LABEL).end().end()
                                    .build();

                    Frame3 FRM_TREE_BUCKET_VIEW = Frame3.builder("FRM_TREE_BUCKET_VIEW")
                                    .addTheme(THM_TREE_ITEM, ThemePosition.WRAPPER).end()
                                    .question("QUE_TREE_BUCKET_VIEW").end().build();

                    Frame3 FRM_TREE_DETAIL_VIEW = Frame3.builder("FRM_TREE_DETAIL_VIEW")
                                    .addTheme(THM_TREE_ITEM, ThemePosition.WRAPPER).end()
                                    .question("QUE_TREE_DETAIL_VIEW").addTheme(THM_TREE_GROUP_WRAPPER)
                                    .vcl(VisualControlType.VCL_WRAPPER).end().end().build();

                    Frame3 FRM_TREE_TABLE_VIEW = Frame3.builder("FRM_TREE_TABLE_VIEW")
                                    .addTheme(THM_TREE_ITEM, ThemePosition.WRAPPER).end()
                                    .question("QUE_TREE_TABLE_VIEW").end().build();

                    Frame3 FRM_SIDEBAR = Frame3.builder("FRM_SIDEBAR").addTheme(THM_SIDEBAR).end()
                                    .addTheme(THM_SIDEBAR_WIDTH).end().addFrame(FRM_LOGO, FramePosition.NORTH).end()
                                    .addFrame(FRM_TREE_TABLE_VIEW, FramePosition.NORTH).end()
                                    .addFrame(FRM_TREE_BUCKET_VIEW, FramePosition.NORTH).end()
                                    .addFrame(FRM_TREE_DETAIL_VIEW, FramePosition.NORTH).end()
                                    .addFrame(FRM_TREE_FORM_VIEW, FramePosition.NORTH).end().build();

                    System.out.println("Generated Sidebar Frame");
                    return FRM_SIDEBAR;
            } catch (Exception e) {
                    System.out.println("Error " + e.getLocalizedMessage());
            }
            return null;
    }

    public Frame3 generateFooter() {
            QRules rules = GennyJbpmBaseTest.setupLocalService();
            GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
            BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
            BaseEntity project = beUtils.getBaseEntityByCode("PRJ_" + serviceToken.getRealm().toUpperCase());

            try {
                    Theme THM_LABEL_BOLD = Theme.builder("THM_LABEL_BOLD").addAttribute().bold(true).size("md")
                                    .paddingX(10).end().addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true).end()
                                    .build();

                    Theme THM_FOOTER = Theme.builder("THM_FOOTER").addAttribute()
                                    .backgroundColor(project.getValue("PRI_COLOR_PRIMARY", "#233A4E")).height(50)
                                    .paddingX(20).end().addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
                                    .end().build();

                    Frame3 FRM_POWERED_BY = Frame3.builder("FRM_POWERED_BY").question("QUE_POWERED_BY_GRP")
                                    .addTheme(THM_LABEL_BOLD).vcl(VisualControlType.VCL_LABEL).end()

                                    .addTheme("THM_DISPLAY_HORIZONTAL", serviceToken)
                                    .vcl(VisualControlType.VCL_WRAPPER).end()

                                    .end().build();

                    Theme THM_FRAME_ALIGN_EAST = Theme.builder("THM_FRAME_ALIGN_EAST").addAttribute()
                                    .marginLeft("auto").flexGrow(0).flexBasis("initial").end()
                                    .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();
                    Theme THM_FRAME_ALIGN_WEST = Theme.builder("THM_FRAME_ALIGN_WEST").addAttribute()
                                    .marginRight("auto").flexGrow(0).flexBasis("initial").end()
                                    .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                    Frame3 FRM_FOOTER = Frame3.builder("FRM_FOOTER").addTheme(THM_FOOTER).end()
                                    .addTheme(THM_FRAME_ALIGN_EAST, ThemePosition.EAST).end()
                                    .addFrame(FRM_POWERED_BY, FramePosition.EAST).end().build();

                    System.out.println("Generated Footer Frame");
                    return FRM_FOOTER;
            } catch (Exception e) {
                    System.out.println("Error :: " + e.getLocalizedMessage());
            }
            return null;
    }

    public Frame3 getDashboard() {
        QRules rules = GennyJbpmBaseTest.setupLocalService();
        GennyToken userToken = new GennyToken("userToken", rules.getToken());
        GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
        BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
        BaseEntity project = beUtils.getBaseEntityByCode("PRJ_" + serviceToken.getRealm().toUpperCase());

        try {

                /* -------------------- THEMES ------------------------------ */

                Theme THM_PROJECT_COLOR_BACKGROUND = Theme.builder("THM_PROJECT_COLOR_BACKGROUND")
                                .addAttribute()
                                .backgroundColor(project.getValue("PRI_COLOR_BACKGROUND", "#F6F6F6"))
                                .color(project.getValue("PRI_COLOR_BACKGROUND_ON", "#000000")).end().build();

                Theme THM_DASHBOARD = Theme.builder("THM_DASHBOARD").addAttribute()
                                // .backgroundColor("green")
                                .maxWidth(900)
                                .width("100%")
                                .end()
                                .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                Theme THM_VERTICAL_SCROLL = Theme.builder("THM_VERTICAL_SCROLL")
                                .addAttribute()
                                        .overflowY("auto")
                                        .padding(40)
                                .end()
                                .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                .build();
                
                Theme THM_PADDING_40 = Theme.builder("THM_PADDING_40")
                                .addAttribute()
                                        .padding(40)
                                .end()
                                .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                .build();

                Theme THM_DASHBOARD_WRAPPER = Theme.builder("THM_DASHBOARD_WRAPPER").addAttribute()
                                .flexShrink(0)
                                .height("initial")
                                .flexBasis("auto")
                                .end()
                                .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                Theme THM_DASHBOARD_ITEM = Theme.builder("THM_DASHBOARD_ITEM").addAttribute()
                                .borderStyle("solid").borderColor("grey").borderBottomWidth(1)
                                .maxHeight("fit-content").padding(10).end()
                                .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                Theme THM_DASHBOARD_ITEM_COLOR = Theme.builder("THM_DASHBOARD_ITEM_COLOR").addAttribute()
                                .backgroundColor(project.getValue("PRI_COLOR_SURFACE", "#FFFFFF"))
                                .color(project.getValue("PRI_COLOR_SURFACE_ON", "#000000")).end().build();

                Theme THM_DASHBOARD_ITEM_INPUT = Theme.builder("THM_DASHBOARD_ITEM_INPUT").addAttribute()
                                .dynamicWidth(true).end()
                                .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                Theme THM_DASHBOARD_ITEM_LABEL = Theme.builder("THM_DASHBOARD_ITEM_LABEL").addAttribute()
                                .bold(true).end().addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
                                .end().build();

                Theme THM_DASHBOARD_ITEM_WRAPPER = Theme.builder("THM_DASHBOARD_ITEM_WRAPPER").addAttribute()
                                .flexDirection("row").justifyContent("space-between").width("100%").end()
                                .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                Theme THM_DASHBOARD_ITEM_BEHAVIOUR = Theme.builder("THM_DASHBOARD_ITEM_BEHAVIOUR")
                                .addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true).end().build();

                Theme THM_TITLE_LABEL = Theme.builder("THM_TITLE_LABEL").addAttribute().textAlign("center")
                                .bold(true).size("lg").end().build();

                Theme THM_TITLE_WRAPPER = Theme.builder("THM_TITLE_WRAPPER").addAttribute().padding(20).end()
                                .build();

                Theme THM_TITLE_BEHAVIOUR = Theme.builder("THM_TITLE_BEHAVIOUR")
                                .addAttribute(ThemeAttributeType.PRI_HAS_INPUT, false).end()
                                .addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true).end().build();

                Theme THM_DASHBOARD_CONTENT_WRAPPER = Theme.builder("THM_DASHBOARD_CONTENT_WRAPPER")
                                .addAttribute().flexGrow(0).flexBasis("initial").height("initial")
                                .end().addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                Theme THM_DASHBOARD_CONTENT_NORTH = Theme.builder("THM_DASHBOARD_CONTENT_NORTH").addAttribute()
                                .flexBasis("initial").end()
                                .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                /* -------------------- INTERNS - FRAMES ------------------------------ */

                Frame3 FRM_COUNT_ALL_INTERNS = Frame3.builder("FRM_COUNT_ALL_INTERNS")
                                .addTheme(THM_DASHBOARD_ITEM, ThemePosition.WRAPPER).end()
                                .addTheme(THM_DASHBOARD_ITEM_COLOR).end().question("QUE_COUNT_ALL_INTERNS")
                                .addTheme(THM_DASHBOARD_ITEM_WRAPPER).vcl(VisualControlType.VCL_WRAPPER).end()
                                .addTheme(THM_DASHBOARD_ITEM_INPUT).vcl(VisualControlType.VCL_INPUT).end()
                                .addTheme(THM_DASHBOARD_ITEM_LABEL).vcl(VisualControlType.VCL_LABEL).end()
                                .addTheme(THM_DASHBOARD_ITEM_BEHAVIOUR).end().end().build();

                Frame3 FRM_COUNT_AVAILABLE_INTERNS = Frame3.builder("FRM_COUNT_AVAILABLE_INTERNS")
                                .addTheme(THM_DASHBOARD_ITEM, ThemePosition.WRAPPER).end()
                                .addTheme(THM_DASHBOARD_ITEM_COLOR).end()
                                .question("QUE_COUNT_AVAILABLE_INTERNS").addTheme(THM_DASHBOARD_ITEM_WRAPPER)
                                .vcl(VisualControlType.VCL_WRAPPER).end().addTheme(THM_DASHBOARD_ITEM_INPUT)
                                .vcl(VisualControlType.VCL_INPUT).end().addTheme(THM_DASHBOARD_ITEM_LABEL)
                                .vcl(VisualControlType.VCL_LABEL).end().addTheme(THM_DASHBOARD_ITEM_BEHAVIOUR)
                                .end().end().build();

                Frame3 FRM_COUNT_APPLIED_SHORTLISTED_INTERVIEWED_INTERNS = Frame3
                                .builder("FRM_COUNT_APPLIED_SHORTLISTED_INTERVIEWED_INTERNS")
                                .addTheme(THM_DASHBOARD_ITEM, ThemePosition.WRAPPER).end()
                                .addTheme(THM_DASHBOARD_ITEM_COLOR).end()
                                .question("QUE_COUNT_APPLIED_SHORTLISTED_INTERVIEWED_INTERNS")
                                .addTheme(THM_DASHBOARD_ITEM_WRAPPER).vcl(VisualControlType.VCL_WRAPPER).end()
                                .addTheme(THM_DASHBOARD_ITEM_INPUT).vcl(VisualControlType.VCL_INPUT).end()
                                .addTheme(THM_DASHBOARD_ITEM_LABEL).vcl(VisualControlType.VCL_LABEL).end()
                                .addTheme(THM_DASHBOARD_ITEM_BEHAVIOUR).end().end().build();

                Frame3 FRM_COUNT_OFFERED_INTERNS = Frame3.builder("FRM_COUNT_OFFERED_INTERNS")
                                .addTheme(THM_DASHBOARD_ITEM, ThemePosition.WRAPPER).end()
                                .addTheme(THM_DASHBOARD_ITEM_COLOR).end().question("QUE_COUNT_OFFERED_INTERNS")
                                .addTheme(THM_DASHBOARD_ITEM_WRAPPER).vcl(VisualControlType.VCL_WRAPPER).end()
                                .addTheme(THM_DASHBOARD_ITEM_INPUT).vcl(VisualControlType.VCL_INPUT).end()
                                .addTheme(THM_DASHBOARD_ITEM_LABEL).vcl(VisualControlType.VCL_LABEL).end()
                                .addTheme(THM_DASHBOARD_ITEM_BEHAVIOUR).end().end().build();

                Frame3 FRM_COUNT_PLACED_INTERNS = Frame3.builder("FRM_COUNT_PLACED_INTERNS")
                                .addTheme(THM_DASHBOARD_ITEM, ThemePosition.WRAPPER).end()
                                .addTheme(THM_DASHBOARD_ITEM_COLOR).end().question("QUE_COUNT_PLACED_INTERNS")
                                .addTheme(THM_DASHBOARD_ITEM_WRAPPER).vcl(VisualControlType.VCL_WRAPPER).end()
                                .addTheme(THM_DASHBOARD_ITEM_INPUT).vcl(VisualControlType.VCL_INPUT).end()
                                .addTheme(THM_DASHBOARD_ITEM_LABEL).vcl(VisualControlType.VCL_LABEL).end()
                                .addTheme(THM_DASHBOARD_ITEM_BEHAVIOUR).end().end().build();

                Frame3 FRM_COUNT_IN_PROGRESS_INTERNS = Frame3.builder("FRM_COUNT_IN_PROGRESS_INTERNS")
                                .addTheme(THM_DASHBOARD_ITEM, ThemePosition.WRAPPER).end()
                                .addTheme(THM_DASHBOARD_ITEM_COLOR).end()
                                .question("QUE_COUNT_IN_PROGRESS_INTERNS").addTheme(THM_DASHBOARD_ITEM_WRAPPER)
                                .vcl(VisualControlType.VCL_WRAPPER).end().addTheme(THM_DASHBOARD_ITEM_INPUT)
                                .vcl(VisualControlType.VCL_INPUT).end().addTheme(THM_DASHBOARD_ITEM_LABEL)
                                .vcl(VisualControlType.VCL_LABEL).end().addTheme(THM_DASHBOARD_ITEM_BEHAVIOUR)
                                .end().end().build();

                Frame3 FRM_COUNT_COMPLETED_INTERNS = Frame3.builder("FRM_COUNT_COMPLETED_INTERNS")
                                .addTheme(THM_DASHBOARD_ITEM, ThemePosition.WRAPPER).end()
                                .addTheme(THM_DASHBOARD_ITEM_COLOR).end()
                                .question("QUE_COUNT_COMPLETED_INTERNS").addTheme(THM_DASHBOARD_ITEM_WRAPPER)
                                .vcl(VisualControlType.VCL_WRAPPER).end().addTheme(THM_DASHBOARD_ITEM_INPUT)
                                .vcl(VisualControlType.VCL_INPUT).end().addTheme(THM_DASHBOARD_ITEM_LABEL)
                                .vcl(VisualControlType.VCL_LABEL).end().addTheme(THM_DASHBOARD_ITEM_BEHAVIOUR)
                                .end().end().build();

                /* DASHBOARD - INTERN - CONTENT */

                Frame3 FRM_DASHBOARD_INTERNS_CONTENT = Frame3.builder("FRM_DASHBOARD_INTERNS_CONTENT")
                                .addTheme(THM_DASHBOARD_CONTENT_WRAPPER, ThemePosition.WRAPPER).end()
                                .addTheme(THM_DASHBOARD_CONTENT_NORTH, ThemePosition.NORTH).end()
                                .addFrame(FRM_COUNT_ALL_INTERNS, FramePosition.NORTH).end()
                                .addFrame(FRM_COUNT_AVAILABLE_INTERNS, FramePosition.NORTH).end()
                                .addFrame(FRM_COUNT_APPLIED_SHORTLISTED_INTERVIEWED_INTERNS,
                                                FramePosition.NORTH)
                                .end().addFrame(FRM_COUNT_OFFERED_INTERNS, FramePosition.NORTH).end()
                                .addFrame(FRM_COUNT_PLACED_INTERNS, FramePosition.NORTH).end()
                                .addFrame(FRM_COUNT_IN_PROGRESS_INTERNS, FramePosition.NORTH).end()
                                .addFrame(FRM_COUNT_COMPLETED_INTERNS, FramePosition.NORTH).end().build();

                /* DASHBOARD - INTERN - HEADER */

                Frame3 FRM_DASHBOARD_INTERNS_HEADER = Frame3.builder("FRM_DASHBOARD_INTERNS_HEADER")
                                .addTheme(THM_DASHBOARD_CONTENT_WRAPPER, ThemePosition.WRAPPER).end()
                                .question("QUE_DASHBOARD_ALL_INTERNS").addTheme(THM_TITLE_LABEL)
                                .vcl(VisualControlType.VCL_LABEL).end().addTheme(THM_TITLE_WRAPPER)
                                .vcl(VisualControlType.VCL_WRAPPER).end().addTheme(THM_TITLE_BEHAVIOUR).end()
                                .end().build();

                /* DASHBOARD - INTERN */

                Frame3 FRM_DASHBOARD_INTERNS = Frame3.builder("FRM_DASHBOARD_INTERNS")
                                .addTheme(THM_DASHBOARD).end()
                                .addTheme(THM_DASHBOARD_WRAPPER, ThemePosition.WRAPPER).end()
                                .addFrame(FRM_DASHBOARD_INTERNS_HEADER, FramePosition.NORTH).end()
                                .addFrame(FRM_DASHBOARD_INTERNS_CONTENT, FramePosition.NORTH).end().build();

                /*
                 * =============================================================================
                 * ===========================================
                 * =============================================================================
                 * ===========================================
                 */

                /* -------------------- INTERNSHIPS - FRAMES ------------------------------ */

                Frame3 FRM_COUNT_ALL_INTERNSHIPS = Frame3.builder("FRM_COUNT_ALL_INTERNSHIPS")
                                .addTheme(THM_DASHBOARD_ITEM, ThemePosition.WRAPPER).end()
                                .addTheme(THM_DASHBOARD_ITEM_COLOR).end().question("QUE_COUNT_ALL_INTERNSHIPS")
                                .addTheme(THM_DASHBOARD_ITEM_WRAPPER).vcl(VisualControlType.VCL_WRAPPER).end()
                                .addTheme(THM_DASHBOARD_ITEM_INPUT).vcl(VisualControlType.VCL_INPUT).end()
                                .addTheme(THM_DASHBOARD_ITEM_LABEL).vcl(VisualControlType.VCL_LABEL).end()
                                .addTheme(THM_DASHBOARD_ITEM_BEHAVIOUR).end().end().build();

                Frame3 FRM_COUNT_AVAILABLE_INTERNSHIPS = Frame3.builder("FRM_COUNT_AVAILABLE_INTERNSHIPS")
                                .addTheme(THM_DASHBOARD_ITEM, ThemePosition.WRAPPER).end()
                                .addTheme(THM_DASHBOARD_ITEM_COLOR).end()
                                .question("QUE_COUNT_AVAILABLE_INTERNSHIPS")
                                .addTheme(THM_DASHBOARD_ITEM_WRAPPER).vcl(VisualControlType.VCL_WRAPPER).end()
                                .addTheme(THM_DASHBOARD_ITEM_INPUT).vcl(VisualControlType.VCL_INPUT).end()
                                .addTheme(THM_DASHBOARD_ITEM_LABEL).vcl(VisualControlType.VCL_LABEL).end()
                                .addTheme(THM_DASHBOARD_ITEM_BEHAVIOUR).end().end().build();

                Frame3 FRM_COUNT_APPLIED_SHORTLISTED_INTERVIEWED_INTERNSHIPS = Frame3
                                .builder("FRM_COUNT_APPLIED_SHORTLISTED_INTERVIEWED_INTERNSHIPS")
                                .addTheme(THM_DASHBOARD_ITEM, ThemePosition.WRAPPER).end()
                                .addTheme(THM_DASHBOARD_ITEM_COLOR).end()
                                .question("QUE_COUNT_APPLIED_SHORTLISTED_INTERVIEWED_INTERNSHIPS")
                                .addTheme(THM_DASHBOARD_ITEM_WRAPPER).vcl(VisualControlType.VCL_WRAPPER).end()
                                .addTheme(THM_DASHBOARD_ITEM_INPUT).vcl(VisualControlType.VCL_INPUT).end()
                                .addTheme(THM_DASHBOARD_ITEM_LABEL).vcl(VisualControlType.VCL_LABEL).end()
                                .addTheme(THM_DASHBOARD_ITEM_BEHAVIOUR).end().end().build();

                Frame3 FRM_COUNT_OFFERED_INTERNSHIPS = Frame3.builder("FRM_COUNT_OFFERED_INTERNSHIPS")
                                .addTheme(THM_DASHBOARD_ITEM, ThemePosition.WRAPPER).end()
                                .addTheme(THM_DASHBOARD_ITEM_COLOR).end()
                                .question("QUE_COUNT_OFFERED_INTERNSHIPS").addTheme(THM_DASHBOARD_ITEM_WRAPPER)
                                .vcl(VisualControlType.VCL_WRAPPER).end().addTheme(THM_DASHBOARD_ITEM_INPUT)
                                .vcl(VisualControlType.VCL_INPUT).end().addTheme(THM_DASHBOARD_ITEM_LABEL)
                                .vcl(VisualControlType.VCL_LABEL).end().addTheme(THM_DASHBOARD_ITEM_BEHAVIOUR)
                                .end().end().build();

                Frame3 FRM_COUNT_PLACED_INTERNSHIPS = Frame3.builder("FRM_COUNT_PLACED_INTERNSHIPS")
                                .addTheme(THM_DASHBOARD_ITEM, ThemePosition.WRAPPER).end()
                                .addTheme(THM_DASHBOARD_ITEM_COLOR).end()
                                .question("QUE_COUNT_PLACED_INTERNSHIPS").addTheme(THM_DASHBOARD_ITEM_WRAPPER)
                                .vcl(VisualControlType.VCL_WRAPPER).end().addTheme(THM_DASHBOARD_ITEM_INPUT)
                                .vcl(VisualControlType.VCL_INPUT).end().addTheme(THM_DASHBOARD_ITEM_LABEL)
                                .vcl(VisualControlType.VCL_LABEL).end().addTheme(THM_DASHBOARD_ITEM_BEHAVIOUR)
                                .end().end().build();

                Frame3 FRM_COUNT_IN_PROGRESS_INTERNSHIPS = Frame3.builder("FRM_COUNT_IN_PROGRESS_INTERNSHIPS")
                                .addTheme(THM_DASHBOARD_ITEM, ThemePosition.WRAPPER).end()
                                .addTheme(THM_DASHBOARD_ITEM_COLOR).end()
                                .question("QUE_COUNT_IN_PROGRESS_INTERNSHIPS")
                                .addTheme(THM_DASHBOARD_ITEM_WRAPPER).vcl(VisualControlType.VCL_WRAPPER).end()
                                .addTheme(THM_DASHBOARD_ITEM_INPUT).vcl(VisualControlType.VCL_INPUT).end()
                                .addTheme(THM_DASHBOARD_ITEM_LABEL).vcl(VisualControlType.VCL_LABEL).end()
                                .addTheme(THM_DASHBOARD_ITEM_BEHAVIOUR).end().end().build();

                Frame3 FRM_COUNT_COMPLETED_INTERNSHIPS = Frame3.builder("FRM_COUNT_COMPLETED_INTERNSHIPS")
                                .addTheme(THM_DASHBOARD_ITEM, ThemePosition.WRAPPER).end()
                                .addTheme(THM_DASHBOARD_ITEM_COLOR).end()
                                .question("QUE_COUNT_COMPLETED_INTERNSHIPS")
                                .addTheme(THM_DASHBOARD_ITEM_WRAPPER).vcl(VisualControlType.VCL_WRAPPER).end()
                                .addTheme(THM_DASHBOARD_ITEM_INPUT).vcl(VisualControlType.VCL_INPUT).end()
                                .addTheme(THM_DASHBOARD_ITEM_LABEL).vcl(VisualControlType.VCL_LABEL).end()
                                .addTheme(THM_DASHBOARD_ITEM_BEHAVIOUR).end().end().build();

                /* DASHBOARD - INTERNSHIPS - CONTENT */

                Frame3 FRM_DASHBOARD_INTERNSHIPS_CONTENT = Frame3.builder("FRM_DASHBOARD_INTERNSHIPS_CONTENT")
                                // .addTheme("THM_BOX_SHADOW_SM", ThemePosition.WRAPPER, serviceToken).end()
                                .addTheme(THM_DASHBOARD_CONTENT_WRAPPER, ThemePosition.WRAPPER).end()
                                .addTheme(THM_DASHBOARD_CONTENT_NORTH, ThemePosition.NORTH).end()
                                .addFrame(FRM_COUNT_ALL_INTERNSHIPS, FramePosition.NORTH).end()
                                .addFrame(FRM_COUNT_AVAILABLE_INTERNSHIPS, FramePosition.NORTH).end()
                                .addFrame(FRM_COUNT_APPLIED_SHORTLISTED_INTERVIEWED_INTERNSHIPS,
                                                FramePosition.NORTH)
                                .end().addFrame(FRM_COUNT_OFFERED_INTERNSHIPS, FramePosition.NORTH).end()
                                .addFrame(FRM_COUNT_PLACED_INTERNSHIPS, FramePosition.NORTH).end()
                                .addFrame(FRM_COUNT_IN_PROGRESS_INTERNSHIPS, FramePosition.NORTH).end()
                                .addFrame(FRM_COUNT_COMPLETED_INTERNSHIPS, FramePosition.NORTH).end().build();

                /* DASHBOARD - INTERNSHIPS - HEADER */

                Frame3 FRM_DASHBOARD_INTERNSHIPS_HEADER = Frame3.builder("FRM_DASHBOARD_INTERNSHIPS_HEADER")
                               .addTheme(THM_DASHBOARD_CONTENT_WRAPPER, ThemePosition.WRAPPER).end()
                                .question("QUE_DASHBOARD_ALL_INTERNSHIPS").addTheme(THM_TITLE_LABEL)
                                .vcl(VisualControlType.VCL_LABEL).end().addTheme(THM_TITLE_WRAPPER)
                                .vcl(VisualControlType.VCL_WRAPPER).end().addTheme(THM_TITLE_BEHAVIOUR).end()
                                .end().build();

                /* DASHBOARD - INTERNSHIPS */

                Frame3 FRM_DASHBOARD_INTERNSHIPS = Frame3.builder("FRM_DASHBOARD_INTERNSHIPS")
                                .addTheme(THM_DASHBOARD).end()
                                .addTheme(THM_DASHBOARD_WRAPPER, ThemePosition.WRAPPER).end()
                                .addFrame(FRM_DASHBOARD_INTERNSHIPS_HEADER, FramePosition.NORTH).end()
                                .addFrame(FRM_DASHBOARD_INTERNSHIPS_CONTENT, FramePosition.NORTH).end().build();

                Frame3 FRM_CONTENT = Frame3.builder("FRM_CONTENT")
                                .addTheme(THM_PROJECT_COLOR_BACKGROUND).end()
                                .addTheme(THM_VERTICAL_SCROLL, ThemePosition.NORTH).end()
                                .addTheme(THM_PADDING_40, ThemePosition.NORTH).end()
                                .addFrame(FRM_DASHBOARD_INTERNS, FramePosition.NORTH).end()
                                .addFrame(FRM_DASHBOARD_INTERNSHIPS, FramePosition.NORTH).end().build();

                Theme THM_TREE_GROUP_BEHAVIOUR = Theme.builder("THM_TREE_GROUP_BEHAVIOUR")
                                .addAttribute(ThemeAttributeType.PRI_IS_EXPANDABLE, true).end()
                                .addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_LABEL, true).end()
                                .addAttribute(ThemeAttributeType.PRI_IS_QUESTION_GRP_LABEL_CLICKABLE, true)
                                .end().addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                                .addAttribute(ThemeAttributeType.PRI_HAS_CHILD_ASKS, false).end().build();

                return FRM_CONTENT;

        } catch (Exception e) {
                // TODO: handle exception
        }
        return null;
}

	@BeforeClass
	public static void init() throws FileNotFoundException, SQLException {

		System.out.println("BridgeUrl=" + GennySettings.bridgeServiceUrl);
		System.out.println("QwandaUrl=" + GennySettings.qwandaServiceUrl);

		// Set up realm
		realms = new HashSet<String>();
		realms.add(realm);
		realms.stream().forEach(System.out::println);
		realms.remove("genny");

		// Enable the PseudoClock using the following system property.
		System.setProperty("drools.clockType", "pseudo");

		eventBusMock = new EventBusMock();
		vertxCache = new VertxCache(); // MockCache
		VertxUtils.init(eventBusMock, vertxCache);

	}

}