package life.genny.test;
//
//import java.lang.invoke.MethodHandles;
//import java.lang.reflect.Type;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import org.junit.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.amazonaws.services.simpleworkflow.flow.core.TryCatch;
//import com.google.gson.reflect.TypeToken;
//
//import io.vertx.core.json.JsonObject;
//import life.genny.models.Frame3;
//import life.genny.models.FramePosition;
//import life.genny.models.FrameTuple3;
//import life.genny.models.GennyToken;
//import life.genny.models.Theme;
//import life.genny.models.ThemeAttribute;
//import life.genny.models.ThemeAttributeType;
//import life.genny.models.ThemePosition;
//import life.genny.qwanda.Answer;
//import life.genny.qwanda.Ask;
//import life.genny.qwanda.Context;
//import life.genny.qwanda.ContextList;
//import life.genny.qwanda.ContextType;
//import life.genny.qwanda.Question;
//import life.genny.qwanda.VisualControlType;
//import life.genny.qwanda.attribute.Attribute;
//import life.genny.qwanda.attribute.EntityAttribute;
//import life.genny.qwanda.datatype.DataType;
//import life.genny.qwanda.entity.BaseEntity;
//import life.genny.qwanda.entity.SearchEntity;
//import life.genny.qwanda.exception.BadDataException;
//import life.genny.qwanda.message.QDataAskMessage;
//import life.genny.qwanda.message.QDataBaseEntityMessage;
//import life.genny.qwanda.validation.Validation;
//import life.genny.qwanda.validation.ValidationList;
//import life.genny.qwandautils.GennySettings;
//import life.genny.qwandautils.JsonUtils;
//import life.genny.qwandautils.QwandaUtils;
//import life.genny.rules.QRules;
//import life.genny.utils.BaseEntityUtils;
//import life.genny.utils.BucketUtils;
//import life.genny.utils.BucketUtilsTest;
//import life.genny.utils.FrameUtils2;
//import life.genny.utils.GennyJbpmBaseTest;
//import life.genny.qwanda.utils.OutputParam;
//import life.genny.utils.RulesUtils;
//import life.genny.utils.SearchUtilsTest;
//import life.genny.utils.TableUtils;
////import life.genny.utils.//TableUtilsTest;
//import org.apache.commons.lang3.StringUtils;
//import life.genny.utils.VertxUtils;
//import life.genny.jbpm.customworkitemhandlers.ShowFrame;
//
//
//public class BucketView extends GennyJbpmBaseTest {
//
//	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());
//
//	public BucketView() {
//		super(false);
//	}
//	
//	////@Test
//	public void tableTest() {
//
//		QRules rules = GennyJbpmBaseTest.setupLocalService();
//		GennyToken userToken = new GennyToken("userToken", rules.getToken());
//		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
//		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
//		BucketUtilsTest bucketUtils = new BucketUtilsTest(beUtils);
//		
//		/* initialize  virtualAskMap */
//	      Map<String, QDataAskMessage> virtualAskMap = new HashMap<String, QDataAskMessage>();
//	      
//	      /*  initialize ask set */
//	      Set<QDataAskMessage> askSet = new HashSet<QDataAskMessage>();
//	      
//	      /* initialize  contextListMap */
//	      Map<String, ContextList> contextListMap = new HashMap<String, ContextList>();
//
//		TableUtils tableUtils = new TableUtils(beUtils);
//		
//		System.out.println("running the test");
//
//		String searchBarString = "";
//		
//		SearchEntity searchBE1 = new SearchEntity("SBE_EDU_PROVIDERS_ACTIVE", "Active")
//				.addSort("PRI_LEGAL_NAME","Title",SearchEntity.Sort.ASC)
//				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "CPY_%")
//				.addFilter("PRI_IS_EDU_PRO", true)
//				.addFilter("PRI_STATUS", SearchEntity.StringFilter.EQUAL, "Active")
//				.addColumn("PRI_LEGAL_NAME", "Name")
//				.addColumn("PRI_STATUS", "Status")
//				.addColumn("PRI_LANDLINE", "Phone")
//				.addColumn("PRI_ADDRESS_FULL","Address")
//				.setPageStart(0).setPageSize(10);
//		
//		SearchEntity searchBE = new SearchEntity("SBE_INTERNS", "Interns")
//				.addSort("PRI_INTERN_NAME","Name",SearchEntity.Sort.ASC)
//				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "PER_%")
//				.addFilter("PRI_IS_INTERN", true)
//				.addColumn("PRI_NAME", "Name")
//				.addColumn("PRI_STATUS", "Status")
//				.addColumn("PRI_INTERN_MOBILE", "Mobile")
//				.setPageStart(0).setPageSize(10);
//		
//		//VertxUtils.putObject(beUtils.getGennyToken().getRealm(), "", searchBE.getCode(), searchBE, beUtils.getGennyToken().getToken());
//
//		
//		//tableUtils.performSearch(userToken,searchBE.getCode(), null);
//		
//		Frame3 FRM_TABLE_VIEW = VertxUtils.getObject(userToken.getRealm(), "", "FRM_TABLE_VIEW", Frame3.class,
//				userToken.getToken());
//		
//		Frame3 FRM_CONTENT = Frame3.builder("FRM_CONTENT").addFrame(FRM_TABLE_VIEW, FramePosition.NORTH)
//				.end().build();
//
//		QDataBaseEntityMessage msg = FrameUtils2.toMessage(FRM_CONTENT, serviceToken, askSet, contextListMap,
//				virtualAskMap);
//		msg.setToken(userToken.getToken());
//		VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg));
//
//		for (QDataAskMessage askMsg : askSet) {
//
//			askMsg.setToken(userToken.getToken());
//
//			String json = JsonUtils.toJson(askMsg);
//			VertxUtils.writeMsg("webcmds", json);
//
//		}
//
//		System.out.print("Completed ");
//		
//		tableUtils.performSearch(userToken, serviceToken, searchBE.getCode(), null);
//		//tableUtils.fetchSearchResults(searchBE);
//		
//
//	}
//
//	//@Test
//	public void testSearchBe() {
//
//		QRules rules = GennyJbpmBaseTest.setupLocalService();
//		GennyToken userToken = new GennyToken("userToken", rules.getToken());
//		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
//		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
//		BucketUtilsTest bucketUtils = new BucketUtilsTest(beUtils);
//
//		TableUtils tableUtils = new TableUtils(beUtils);
//		
//		System.out.println("running the test");
//
////		ShowFrame.display(beUtils.getGennyToken(), "FRM_TABLE_VIEW", "FRM_TAB_CONTENT", "Test");
//		
//
//	}
//	
//	////@Test
//	public void sendCards() {
//		Frame3 test = this.getBucketHeaderFame("test","test", "test");
//
//		QRules rules = GennyJbpmBaseTest.setupLocalService();
//		GennyToken userToken = new GennyToken("userToken", rules.getToken());
//		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
//		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
//		BucketUtilsTest bucketUtils = new BucketUtilsTest(beUtils);
//		//BucketUtils bucketUtils = new BucketUtils(beUtils);
//		
//		System.out.println("running the test");
//
//		Theme THM_JUSTIFY_CONTENT_FLEX_START = Theme.builder("THM_JUSTIFY_CONTENT_FLEX_START")
//										.addAttribute().justifyContent("flex-start").end()
//										.build();
//
//		Theme THM_SCROLL_VERTICAL2 = Theme.builder("THM_SCROLL_VERTICAL2")
//									.addAttribute().overflowY("auto").end()
//									.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
//									.build();  
//
//		Theme THM_PADDING_15 = Theme.builder("THM_PADDING_15")
//									.addAttribute()
//										.padding(15)
//									.end()
//									.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
//									.build();  
//
//		Frame3 FRM_BUCKET_CONTENT = Frame3.builder("FRM_BUCKET_CONTENT")
//					//.addTheme(THM_SCROLL_VERTICAL2, ThemePosition.CENTRE).end()
//					.question("QUE_BUCKET_CONTENT_GRP").end()
//					//.addTheme(THM_PADDING_15, ThemePosition.WRAPPER).end()
//					.addTheme(THM_JUSTIFY_CONTENT_FLEX_START, ThemePosition.CENTRE).end()
//					.build();
//
//
//		
//		bucketUtils.sendCards(FRM_BUCKET_CONTENT, userToken, serviceToken);
//		//bucketUtils2.sendCards(FRM_BUCKET_CONTENT, userToken, serviceToken);
//
//	}
//
//
//
//	////@Test
//	public void testProcessView() {
//
//		QRules rules = GennyJbpmBaseTest.setupLocalService();
//		GennyToken userToken = new GennyToken("userToken", rules.getToken());
//		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
//		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
//		// TableUtilsTest bucketUtils = new //TableUtilsTest(beUtils);
//
//		/* initialize bucketUtils */
//		//BucketUtilsTest bucketUtils = new BucketUtilsTest(beUtils);
//		BucketUtils bucketUtils = new BucketUtils(beUtils);
//
//		/* initialize virtualAskMap */
//		Map<String, QDataAskMessage> virtualAskMap = new HashMap<String, QDataAskMessage>();
//
//		/* initialize ask set */
//		Set<QDataAskMessage> askSet = new HashSet<QDataAskMessage>();
//
//		/* initialize contextListMap */
//		Map<String, ContextList> contextListMap = new HashMap<String, ContextList>();
//
//		/* get the theme */
//		Theme THM_DISPLAY_VERTICAL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_DISPLAY_VERTICAL",
//				Theme.class, serviceToken.getToken());
//		Theme THM_QUESTION_GRP_LABEL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_QUESTION_GRP_LABEL",
//				Theme.class, serviceToken.getToken());
//		
//
//		
//		Theme THM_BH_ROW_ONE_GRP_CONTENT_WRAPPER = Theme.builder("THM_BH_ROW_ONE_GRP_CONTENT_WRAPPER")
//													.addAttribute()
//													.justifyContent("center")
//													.alignItems("center")
//													.end()
//													.build();
//		
//		Theme THM_WIDTH_100_PERCENT_NO_INHERIT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_WIDTH_100_PERCENT_NO_INHERIT", 
//				Theme.class, serviceToken.getToken());
//
//		// NEW THEMES START HERE
//		Theme THM_BUCKET_HEADER_GROUP_WRAPPER = Theme.builder("THM_BUCKET_HEADER_GROUP_WRAPPER")
//                    .addAttribute()
//											.flexDirection("row")
//											.width("100%")
//											.paddingLeft(8)
//											.paddingRight(4)
//                    .end()
//                    .build();
//		
//		Theme THM_BUCKET_HEADER_GROUP_HEADER_WRAPPER = Theme.builder("THM_BUCKET_HEADER_GROUP_HEADER_WRAPPER")
//                    .addAttribute()
//											.flexGrow(1)
//											.marginBottom(4)
//                    .end()
//                    .build();		
//
//		Theme THM_BUCKET_HEADER_GROUP_LABEL = Theme.builder("THM_BUCKET_HEADER_GROUP_LABEL")
//                    .addAttribute()
//                      .justifyContent("center")
//					  					.marginBottom(0)
//                    .end()
//                    .build();		
//		Theme THM_FONT_FAMILY_ROBOTO = Theme.builder("THM_FONT_FAMILY_ROBOTO")
//                    .addAttribute()
//                      .fontFamily("Roboto")
//                    .end()
//                    .build();					
//
//		Context bucketHeaderLabelContext = new Context(ContextType.THEME,
//				bucketUtils.getThemeBe(THM_QUESTION_GRP_LABEL), VisualControlType.GROUP, 1.0);
//		//bucketHeaderLabelContext.setDataType("Form Submit");
//		bucketHeaderLabelContext.setDttCode("DTT_SUBMIT_BUTTON");
//
//		List<Context> bucketHeaderContext = new ArrayList<>();
//		bucketHeaderContext.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_DISPLAY_VERTICAL),
//				VisualControlType.GROUP_CONTENT_WRAPPER, 1.0));
//		/* bucketHeaderContext.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_BH_GROUP_WRAPPER),
//				VisualControlType.GROUP_WRAPPER, 1.0)); */
//		bucketHeaderContext.add(bucketHeaderLabelContext);
//		bucketHeaderContext.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_BH_ROW_ONE_GRP_CONTENT_WRAPPER),
//				VisualControlType.GROUP_CONTENT_WRAPPER, 1.0));
//		
//		// NEW
//		bucketHeaderContext.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_BUCKET_HEADER_GROUP_WRAPPER),
//				VisualControlType.GROUP_WRAPPER, 1.0));
//		bucketHeaderContext.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_BUCKET_HEADER_GROUP_HEADER_WRAPPER),
//				VisualControlType.GROUP_HEADER_WRAPPER, 1.0));
//		bucketHeaderContext.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_BUCKET_HEADER_GROUP_LABEL),
//				VisualControlType.GROUP_LABEL, 1.0));
//		bucketHeaderContext.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_FONT_FAMILY_ROBOTO),
//				VisualControlType.VCL_DEFAULT, 1.0));
//
//
//
//		/* cardContext */
//		List<Context> bucketContentContext = new ArrayList<>();
//		bucketContentContext.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_WIDTH_100_PERCENT_NO_INHERIT),
//				VisualControlType.GROUP_WRAPPER, 1.0));
//
//
//		try {
//
//			/* get all the contextListMap for card */
//			contextListMap = bucketUtils.getCardContextListMap(contextListMap, serviceToken);
//
//			/* get the templat ask for card */
//			//Ask templateAsk = bucketUtils.getCardTemplate();
//
//			/* get all the bucket frames */
//			Frame3 FRM_BUCKET_HEADER = getBucketHeaderFame("FRM_BUCKET_HEADER", "test", "test");
//			Frame3 FRM_BUCKET_CONTENT = getBucketContentFrame("FRM_BUCKET_CONTENT", "test", "test");
//			Frame3 FRM_BUCKET_FOOTER = getBucketFooterFrame("FRM_BUCKET_FOOTER", "test", "test");
//			Frame3 FRM_BUCKET_WRAPPER = getBucketWrapperFrame("FRM_BUCKET_WRAPPER", "test", "test");
//
//			Frame3 FRM_BUCKETS = getBucketFrame("FRM_BUCKETS", "test", "test");
//
//			/* get all the bucket related asks group */
//			Ask FRM_BUCKET_HEADER_ASK = this.getBucketHeaderAsk(contextListMap, serviceToken);
//			Ask FRM_BUCKET_CONTENT_ASK = this.getBucketContentAsk(contextListMap, serviceToken);
//			Ask FRM_BUCKET_FOOTER_ASK = this.getBucketFooterAsk(contextListMap, serviceToken);
//
//			/* get the searchBeList */
//			List<SearchEntity> searchBeList = bucketUtils.getBucketSearchBeListFromCache(serviceToken);
//			System.out.println("size   ::    " + searchBeList.size());
//					
//			if(searchBeList != null && !searchBeList.isEmpty()) {
//				for (SearchEntity searchBe : searchBeList) {
//
//					String code = searchBe.getCode().split("SBE_")[1];
//					System.out.println("Current BUCKET Code   ::    " + code);
//
//					/* add the contextList for the bucketHeaderAsk and bucketContentAsk */
//					contextListMap.put("QUE_BUCKET_HEADER_" + code + "_GRP", new ContextList(bucketHeaderContext));
//					contextListMap.put("QUE_BUCKET_CONTENT_" + code + "_GRP", new ContextList(bucketContentContext));
//
//					/* clone the bucket */
//					Frame3 bucketHeader = Frame3.clone(FRM_BUCKET_HEADER);
//					bucketHeader.setCode("FRM_BUCKET_HEADER_" + code);
//					bucketHeader.setQuestionCode("QUE_BUCKET_HEADER_" + code + "_GRP");
//
//					Frame3 bucketContent = Frame3.clone(FRM_BUCKET_CONTENT);
//					bucketContent.setCode("FRM_BUCKET_CONTENT_" + code);
//					bucketContent.setQuestionCode("QUE_BUCKET_CONTENT_" + code + "_GRP");
//
//					Frame3 bucketFooter = Frame3.clone(FRM_BUCKET_FOOTER);
//					bucketFooter.setCode("FRM_BUCKET_FOOTER_" + code);
//					bucketFooter.setQuestionCode("QUE_BUCKET_FOOTER_" + code + "_GRP");
//
//					Frame3 bucket = Frame3.clone(FRM_BUCKETS);
//					bucket.setCode("FRM_BUCKET_" + code);
//					bucket.getFrames().add(new FrameTuple3(bucketHeader, FramePosition.NORTH, 1.0));
//					bucket.getFrames().add(new FrameTuple3(bucketContent, FramePosition.CENTRE, 1.0));
//					//bucket.getFrames().add(new FrameTuple3(bucketFooter, FramePosition.SOUTH, 1.0));
//
//					/* add the cloned bucket to wrapper */
//					FRM_BUCKET_WRAPPER.getFrames().add(new FrameTuple3(bucket, FramePosition.WEST, 1.0));
//
//					/* clone asks */
//
//					/* bucketHeader asks */
//					Ask bucketHeaderAsk = Ask.clone(FRM_BUCKET_HEADER_ASK);
//					bucketHeaderAsk.setQuestionCode("QUE_BUCKET_HEADER_" + code + "_GRP");
//					bucketHeaderAsk.getQuestion().setName(code);
//					bucketHeaderAsk.setName(searchBe.getName());
//					bucketHeaderAsk.setTargetCode(searchBe.getCode());
//
//					/* bucketContent asks */
//					Ask bucketContentAsk = Ask.clone(FRM_BUCKET_CONTENT_ASK);
//					bucketContentAsk.setQuestionCode("QUE_BUCKET_CONTENT_" + code + "_GRP");
//					bucketContentAsk.setName(searchBe.getName());
//					bucketContentAsk.setTargetCode(searchBe.getCode());
//
//					/* bucketFooter asks */
//					Ask bucketFooterAsk = Ask.clone(FRM_BUCKET_FOOTER_ASK);
//					bucketFooterAsk.setQuestionCode("QUE_BUCKET_FOOTER_" + code + "_GRP");
//					bucketFooterAsk.setName(searchBe.getName());
//					bucketFooterAsk.setTargetCode(searchBe.getCode());
//
//					virtualAskMap.put(bucketHeaderAsk.getQuestionCode(), new QDataAskMessage(bucketHeaderAsk));
//					virtualAskMap.put(bucketContentAsk.getQuestionCode(), new QDataAskMessage(bucketContentAsk));
//					//virtualAskMap.put(bucketFooterAsk.getQuestionCode(), new QDataAskMessage(bucketFooterAsk));
//
//				}
//			}else {
//				System.out.println("searchBeList is empty");
//			}
//			/* loop through the searchList */
//			
//
//			/* build the bucket view frame */
//			Frame3 FRM_BUCKET_VIEW = Frame3.builder("FRM_BUCKET_VIEW")
//					.addFrame(FRM_BUCKET_WRAPPER, FramePosition.CENTRE).end().build();
//
//			/* build the tab content frame */
//			Frame3 FRM_CONTENT = Frame3.builder("FRM_CONTENT")
//					.addFrame(FRM_BUCKET_VIEW, FramePosition.NORTH).end()
//					.addTheme(THM_FONT_FAMILY_ROBOTO).end()
//					.build();
//
//			QDataBaseEntityMessage msg = FrameUtils2.toMessage(FRM_CONTENT, serviceToken, askSet, contextListMap,
//					virtualAskMap);
//			msg.setToken(userToken.getToken());
//			VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg));
//
//			for (QDataAskMessage askMsg : askSet) {
//
//				askMsg.setToken(userToken.getToken());
//
//				String json = JsonUtils.toJson(askMsg);
//				VertxUtils.writeMsg("webcmds", json);
//
//			}
//
//			System.out.print("Completed");
//			System.out.print("Sending cards now");
//			this.sendCards();
//		} catch (Exception e) {
//			System.out.print("Error");
//		}
//	}
//	
//	public Frame3 getBucketHeaderFame(String name, String target, String questionCode) {
//
//		/* build the theme */
//		Theme THM_JUSTIFY_CONTENT_FLEX_START = Theme.builder("THM_JUSTIFY_CONTENT_FLEX_START").addAttribute()
//				.justifyContent("flex-start").end().build();
//
//		/* build the frame */
//		Frame3 bucketHeader2 = Frame3.builder(name).question("QUE_BUCKET_HEADER_GRP").end()
//				.addTheme(THM_JUSTIFY_CONTENT_FLEX_START, ThemePosition.CENTRE).end().build();
//		
//		
//		Frame3 bucketHeader = Frame3.clone(bucketHeader2);
//		bucketHeader.setCode("FRM_BUCKET_HEADER_");
//		bucketHeader.setQuestionCode("QUE_BUCKET_HEADER_GRP");
//		bucketHeader.getQuestionGroup().setTargetAlias("bucketHeader");
//
//		/* return the frame */
//		return bucketHeader;
//		
//	}
//
//	public Frame3 getBucketContentFrame(String name, String target, String questionCode) {
//
//		/* build the theme */
//		Theme THM_JUSTIFY_CONTENT_FLEX_START = Theme.builder("THM_JUSTIFY_CONTENT_FLEX_START").addAttribute()
//				.justifyContent("flex-start").end().build();
//
//		Theme THM_PADDING_15 = Theme.builder("THM_PADDING_15")
//				.addAttribute()
//					.padding(15)
//				.end()
//				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
//				.build();  
//
//		/* build the frame */
//		Frame3 bucketContent = Frame3.builder(name)
//				.question("QUE_BUCKET_CONTENT_GRP").end()
//				.addTheme(THM_JUSTIFY_CONTENT_FLEX_START, ThemePosition.CENTRE).end()
//				/* .addTheme(THM_PADDING_15, ThemePosition.WRAPPER).end() */
//				.build();
//
//		return bucketContent;
//	}
//
//	public Frame3 getBucketFooterFrame(String name, String target, String questionCode) {
//
//		Theme THM_DISPLAY_HORIZONTAL = Theme.builder("THM_DISPLAY_HORIZONTAL").addAttribute().flexDirection("row").end()
//				.build();
//
//		Theme THM_WIDTH_100_PERCENT = Theme.builder("THM_WIDTH_100_PERCENT").addAttribute().width("100%").end().build();
//
//		Theme THM_JUSTIFY_CONTENT_SPACE_BETWEEN = Theme.builder("THM_JUSTIFY_CONTENT_SPACE_BETWEEN").addAttribute()
//				.justifyContent("space-between").end().build();
//
//		Frame3 bucketFooter = Frame3.builder(name).question("QUE_BUCKET_FOOTER_GRP").addTheme(THM_DISPLAY_HORIZONTAL)
//				.vcl(VisualControlType.GROUP_CONTENT_WRAPPER).end().addTheme(THM_WIDTH_100_PERCENT)
//				.vcl(VisualControlType.GROUP_WRAPPER).end().addTheme(THM_JUSTIFY_CONTENT_SPACE_BETWEEN)
//				.vcl(VisualControlType.GROUP_CONTENT_WRAPPER).end().end().build();
//
//		return bucketFooter;
//	}
//
//	public Frame3 getBucketFrame(String name, String target, String questionCode) {
//
//		/* build the theme */
//		Theme THM_JUSTIFY_CONTENT_FLEX_START = Theme.builder("THM_JUSTIFY_CONTENT_FLEX_START").addAttribute()
//				.justifyContent("flex-start").end().build();
//
//		Theme THM_BACKGROUND_NONE = Theme.builder("THM_BACKGROUND_NONE").addAttribute().backgroundColor("none").end()
//				.build();
//
//		Theme THM_BACKGROUND_E4E4E4 = Theme.builder("THM_BACKGROUND_E4E4E4").addAttribute().backgroundColor("#E4E4E4")
//				.end().build();
//
//		Theme THM_BUCKET_COLUMN = Theme.builder("THM_BUCKET_COLUMN")
//				.addAttribute().minWidth(344).width("100%")
//				.textAlign("center").flexDirection("column").end()
//				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();
//
//		Theme THM_BUCKET_COLUMN_PADDING2 = Theme.builder("THM_BUCKET_COLUMN_PADDING2")
//																		.addAttribute()
//																			//.padding(20)
//																			.justifyContent("flex-start")
//																		.end()
//																		.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
//																		.build();
//		
//		Theme THM_SCROLL_VERTICAL = Theme.builder("THM_SCROLL_VERTICAL")
//									.addAttribute().overflowY("auto").end()
//									.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
//									.build();  
//		
//		Theme THM_BUCKET_PANEL_NORTH = Theme.builder("THM_BUCKET_PANEL_NORTH")
//									.addAttribute()
//										.paddingX(4)
//										.paddingTop(8)
//									.end()
//									.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
//									.build(); 
//		Theme THM_BUCKET_PANEL_CENTRE = Theme.builder("THM_BUCKET_PANEL_CENTRE")
//									.addAttribute()
//										.padding(4)
//									.end()
//									.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
//									.build();  
//
//		/* build the frame */
//		Frame3 bucket = Frame3.builder(name)
//				.addTheme(THM_BACKGROUND_NONE).end()
//				.addTheme(THM_BACKGROUND_E4E4E4, ThemePosition.WRAPPER).end()
//				.addTheme(THM_BUCKET_COLUMN, ThemePosition.WRAPPER).end()
//				.addTheme(THM_BUCKET_COLUMN_PADDING2, ThemePosition.CENTRE).end()
//				.addTheme(THM_SCROLL_VERTICAL, ThemePosition.CENTRE).end()
//				.addTheme(THM_BUCKET_PANEL_NORTH, ThemePosition.NORTH).end()
//				.addTheme(THM_BUCKET_PANEL_CENTRE, ThemePosition.CENTRE).end()
//				.build();
//
//		return bucket;
//	}
//
//	public Frame3 getBucketWrapperFrame(String name, String target, String questionCode) {
//
//		/* build the theme */
//		Theme THM_BUCKET = Theme.builder("THM_BUCKET").addAttribute().backgroundColor("#F8F9FA").overflowX("auto")
//				.overflowY("auto").width("100%").end().addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
//				.build();
//
//		Theme THM_BUCKET_WRAPPER = Theme.builder("THM_BUCKET_WRAPPER").addAttribute().paddingY(20).end()
//				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();
//
//		/* build the frame */
//		Frame3 FRM_BUCKET_WRAPPER = Frame3.builder(name)
//				.addTheme(THM_BUCKET).end()
//				//.addTheme(THM_BUCKET_WRAPPER, ThemePosition.WEST).end()
//				.build();
//		
//
//		return FRM_BUCKET_WRAPPER;
//	}
//	/* Generate Asks */
//	public Ask getBucketHeaderAsk(Map<String, ContextList> contextListMap, GennyToken serviceToken) {
//
//		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
//		BucketUtilsTest bucketUtils = new BucketUtilsTest(beUtils);
//
//		Theme THM_QUESTION_GRP_LABEL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_QUESTION_GRP_LABEL",
//				Theme.class, serviceToken.getToken());
//		Theme THM_DISPLAY_VERTICAL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_DISPLAY_VERTICAL",
//				Theme.class, serviceToken.getToken());
//		Theme THM_DISPLAY_HORIZONTAL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_DISPLAY_HORIZONTAL",
//				Theme.class, serviceToken.getToken());
//		Theme THM_WIDTH_100_PERCENT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_WIDTH_100_PERCENT",
//				Theme.class, serviceToken.getToken());
//		Theme THM_BH_ROW_ONE_GRP_WRAPPER = VertxUtils.getObject(serviceToken.getRealm(), "",
//				"THM_BH_ROW_ONE_GRP_WRAPPER", Theme.class, serviceToken.getToken());
//		Theme THM_BH_ROW_ONE_GRP_LABEL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BH_ROW_ONE_GRP_LABEL",
//				Theme.class, serviceToken.getToken());
//		Theme THM_BH_ROW_ONE_GRP_CONTENT_WRAPPER = VertxUtils.getObject(serviceToken.getRealm(), "",
//				"THM_BH_ROW_ONE_GRP_CONTENT_WRAPPER", Theme.class, serviceToken.getToken());
//		Theme THM_BH_ROW_ONE_VCL_INPUT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BH_ROW_ONE_VCL_INPUT",
//				Theme.class, serviceToken.getToken());
//		Theme THM_BH_ROW_TWO_VCL_WRAPPER = VertxUtils.getObject(serviceToken.getRealm(), "",
//				"THM_BH_ROW_TWO_VCL_WRAPPER", Theme.class, serviceToken.getToken());
//		Theme THM_BH_ROW_TWO_GRP_CONTENT_WRAPPER = VertxUtils.getObject(serviceToken.getRealm(), "",
//				"THM_BH_ROW_TWO_GRP_CONTENT_WRAPPER", Theme.class, serviceToken.getToken());
//		Theme THM_BH_ROW_TWO_INPUT_FIELD = VertxUtils.getObject(serviceToken.getRealm(), "",
//				"THM_BH_ROW_TWO_INPUT_FIELD", Theme.class, serviceToken.getToken());
//		Theme THM_ICON = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_ICON", Theme.class,
//				serviceToken.getToken());
//		Theme THM_BH_GROUP_WRAPPER = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BH_GROUP_WRAPPER",
//				Theme.class, serviceToken.getToken());
//
//		BaseEntity ICN_SORT = beUtils.getBaseEntityByCode("ICN_SORT");
//
//		// NEW THEMES START HERE
////		THM_BUCKET_HEADER_GROUP_WRAPPER = Theme.builder("THM_BUCKET_HEADER_GROUP_WRAPPER")
////                    .addAttribute()
////                      .flexDirection("row")
////                    .end()
////                    .build();
//
//		
//		// NEW THEMES END HERE
//
//		
//
//		/*
//		 * we create context here
//		 */
//
//		/* row1Context context */
//		List<Context> row1Context = new ArrayList<>();
//		// row1Context.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_DISPLAY_HORIZONTAL),
//		// 		VisualControlType.GROUP_WRAPPER, 1.0));
//		// row1Context.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_BH_ROW_ONE_GRP_WRAPPER),
//		// 		VisualControlType.GROUP_WRAPPER, 1.0));
//		// row1Context.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_BH_ROW_ONE_GRP_LABEL),
//		// 		VisualControlType.GROUP_LABEL, 1.0));
//		// // row1Context.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_BH_ROW_ONE_GRP_CONTENT_WRAPPER),
//		// // 		VisualControlType.GROUP_CONTENT_WRAPPER, 1.0));
//		row1Context.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_BH_ROW_ONE_VCL_INPUT),
//				VisualControlType.VCL_INPUT, 1.0));
//
//		/* row2Context context */
//		List<Context> row2Context = new ArrayList<>();
//		row2Context.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_DISPLAY_HORIZONTAL),
//				VisualControlType.GROUP_CONTENT_WRAPPER, 1.0));
//		row2Context.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_BH_ROW_TWO_VCL_WRAPPER),
//				VisualControlType.VCL_WRAPPER, 1.0));
//		row2Context.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_BH_ROW_TWO_GRP_CONTENT_WRAPPER),
//				VisualControlType.GROUP_CONTENT_WRAPPER, 1.0));
//
//		/* bucketCountContextList context */
//		List<Context> bucketCountContextList = new ArrayList<>();
//		bucketCountContextList.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_QUESTION_GRP_LABEL),
//				VisualControlType.GROUP, 1.0));
//
//		/* bucketSearchContextList context */
//		List<Context> bucketSearchContextList = new ArrayList<>();
//		bucketSearchContextList.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_BH_ROW_TWO_INPUT_FIELD),
//				VisualControlType.VCL_WRAPPER, 1.0));
//
//		/* bucketSortContextList context */
//		List<Context> bucketSortContextList = new ArrayList<>();
//		bucketSortContextList
//				.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_ICON), VisualControlType.VCL, 1.0));
//		bucketSortContextList.add(new Context(ContextType.ICON, ICN_SORT, VisualControlType.VCL_ICON, 1.0));
//
//		/* add the contextList to contextMap */
//		contextListMap.put("QUE_BUCKET_HEADER_ROW_ONE_GRP", new ContextList(row1Context));
//		contextListMap.put("QUE_BUCKET_COUNT", new ContextList(bucketCountContextList));
//		// contextListMap.put("QUE_BUCKET_HEADER_ROW_TWO_GRP", new ContextList(row2Context));
//		// contextListMap.put("QUE_BUCKET_SEARCH", new ContextList(bucketSearchContextList));
//		// contextListMap.put("QUE_BUCKET_SORT", new ContextList(bucketSortContextList));
//
//		/* Validation for Search Attribute */
//		Validation validation = new Validation("VLD_NON_EMPTY", "EmptyandBlankValues", "(?!^$|\\s+)");
//		List<Validation> validations = new ArrayList<>();
//		validations.add(validation);
//		ValidationList searchValidationList = new ValidationList();
//		searchValidationList.setValidationList(validations);
//
//		/* get the attributes */
//		Attribute countAttribute = RulesUtils.getAttribute("PRI_TOTAL_RESULTS", serviceToken.getToken());
//		Attribute sortAttribute = RulesUtils.getAttribute("PRI_SORT", serviceToken.getToken());
//		Attribute nameAttribute = RulesUtils.getAttribute("PRI_NAME", serviceToken.getToken());
//
//		Attribute searchAttribute = new Attribute("PRI_NAME", "Search",
//				new DataType("Text", searchValidationList, "Text"));
//
//		Attribute questionAttribute = RulesUtils.getAttribute("QQQ_QUESTION_GROUP", serviceToken.getToken());
//		Attribute tableCellAttribute = RulesUtils.getAttribute("QQQ_QUESTION_GROUP_TABLE_CELL",
//				serviceToken.getToken());
//
//		/* Initialize Bucket Header Ask group */
//		Question bucketHeaderQuestion = new Question("QUE_BUCKET_HEADER_GRP", "Bucket Header", questionAttribute, true);
//		Ask bucketHeaderAsk = new Ask(bucketHeaderQuestion, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");
//
//		/* row-one-ask */
//		Question row1Ques = new Question("QUE_BUCKET_HEADER_ROW_ONE_GRP", "Row One", tableCellAttribute, false);
//		Ask row1Ask = new Ask(row1Ques, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");
//
//		/* count ask */
//		Question bucketCountQues = new Question("QUE_BUCKET_COUNT", countAttribute.getName(), countAttribute, false);
//		Ask bucketCountAsk = new Ask(bucketCountQues, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");
//
//		Ask[] row1ChildAsks = { bucketCountAsk };
//		row1Ask.setChildAsks(row1ChildAsks);
//
//		/* row-two-ask */
//		Question row2Ques = new Question("QUE_BUCKET_HEADER_ROW_TWO_GRP", "Row Two", tableCellAttribute, false);
//		Ask row2Ask = new Ask(row2Ques, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");
//
//		/* search ask */
//		Question bucketSearchQues = new Question("QUE_BUCKET_SEARCH", searchAttribute.getName(), searchAttribute,
//				false);
//		Ask bucketSearchAsk = new Ask(bucketSearchQues, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");
//
//		/* sort ask */
//		Question bucketSortQues = new Question("QUE_BUCKET_SORT", sortAttribute.getName(), sortAttribute, false);
//		Ask bucketSortAsk = new Ask(bucketSortQues, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");
//
//		Ask[] row2ChildAsks = { bucketSearchAsk, bucketSortAsk };
//		row2Ask.setChildAsks(row2ChildAsks);
//
//		/* set the bucketHeader child asks */
//		//Ask[] bucketChildAsks = { row1Ask, row2Ask };
//		Ask[] bucketChildAsks = { row1Ask }; // NEW
//		bucketHeaderAsk.setChildAsks(bucketChildAsks);
//
//		return bucketHeaderAsk;
//	}
//	public Ask getBucketFooterAsk(Map<String, ContextList> contextListMap, GennyToken serviceToken) {
//
//		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
//		BucketUtilsTest bucketUtils = new BucketUtilsTest(beUtils);
//
//		/* get the themes */
//		Theme THM_DISPLAY_HORIZONTAL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_DISPLAY_HORIZONTAL",
//				Theme.class, serviceToken.getToken());
//		Theme THM_WIDTH_100_PERCENT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_WIDTH_100_PERCENT",
//				Theme.class, serviceToken.getToken());
//		Theme THM_ICON = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_ICON", Theme.class,
//				serviceToken.getToken());
//		Theme THM_JUSTIFY_CONTENT_SPACE_AROUND = Theme.builder("THM_JUSTIFY_CONTENT_SPACE_AROUND").addAttribute()
//				.justifyContent("space-around").end().build();
//
//		/* get the baseentities */
//		BaseEntity ICN_ARROW_FORWARD_IOS = beUtils.getBaseEntityByCode("ICN_ARROW_FORWARD_IOS");
//		BaseEntity ICN_ARROW_BACK_IOS = beUtils.getBaseEntityByCode("ICN_ARROW_BACK_IOS");
//
//		/* get the attributes */
//		Attribute questionAttribute = RulesUtils.attributeMap.get("QQQ_QUESTION_GROUP");
//		Attribute nextAttribute = RulesUtils.attributeMap.get("PRI_NEXT_BTN");
//		Attribute prevAttribute = RulesUtils.attributeMap.get("PRI_PREVIOUS_BTN");
//
//		/* we create context here */
//
//		/* bucketFooter context */
//		List<Context> bucketFooterContext = new ArrayList<>();
//		bucketFooterContext.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_DISPLAY_HORIZONTAL),
//				VisualControlType.GROUP_CONTENT_WRAPPER, 1.0));
//		bucketFooterContext.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_WIDTH_100_PERCENT),
//				VisualControlType.GROUP_WRAPPER, 1.0));
//		bucketFooterContext.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_JUSTIFY_CONTENT_SPACE_AROUND),
//				VisualControlType.GROUP_CONTENT_WRAPPER, 1.0));
//
//		/* nextBucket context */
//		List<Context> nextBucketContext = new ArrayList<>();
//		nextBucketContext.add(new Context(ContextType.ICON, ICN_ARROW_FORWARD_IOS, VisualControlType.VCL_ICON, 1.0));
//		nextBucketContext
//				.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_ICON), VisualControlType.VCL, 1.0));
//
//		/* prevBucket context */
//		List<Context> prevBucketContext = new ArrayList<>();
//		prevBucketContext.add(new Context(ContextType.ICON, ICN_ARROW_BACK_IOS, VisualControlType.VCL_ICON, 1.0));
//		prevBucketContext
//				.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_ICON), VisualControlType.VCL, 1.0));
//
//		/* add the contextList to contextMap */
//		contextListMap.put("QUE_BUCKET_FOOTER_GRP", new ContextList(bucketFooterContext));
//		contextListMap.put("QUE_NEXT_BUCKET", new ContextList(nextBucketContext));
//		contextListMap.put("QUE_PREV_BUCKET", new ContextList(prevBucketContext));
//
//		/* Initialize Bucket Footer Ask group */
//		Question bucketFooterQuestion = new Question("QUE_BUCKET_FOOTER_GRP", "Footer Group", questionAttribute, true);
//		Ask bucketFooterAsk = new Ask(bucketFooterQuestion, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");
//
//		/* next ask */
//		Question nextBucketQues = new Question("QUE_NEXT_BUCKET", "", nextAttribute, false);
//		Ask nextBucketAsk = new Ask(nextBucketQues, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");
//
//		/* prev ask */
//		Question prevBucketQues = new Question("QUE_PREV_BUCKET", "", prevAttribute, false);
//		Ask prevBucketAsk = new Ask(prevBucketQues, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");
//
//		/* set the child asks */
//		Ask[] bucketChildAsksArray = { prevBucketAsk, nextBucketAsk };
//		bucketFooterAsk.setChildAsks(bucketChildAsksArray);
//
//		return bucketFooterAsk;
//
//	}
//
//	public Ask getBucketContentAsk(Map<String, ContextList> contextListMap, GennyToken serviceToken) {
//
//		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
//
//		Attribute questionAttribute = RulesUtils.getAttribute("QQQ_QUESTION_GROUP", serviceToken.getToken());
//		Question bucketContentQuestion = new Question("QUE_BUCKET_CONTENT_GRP", "", questionAttribute, true);
//		Ask bucketContentAsk = new Ask(bucketContentQuestion, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");
//		return bucketContentAsk;
//
//	}
//
//	public BaseEntity getThemeBe(Theme theme) {
//
//		BaseEntity themeBe = null;
//		themeBe = theme.getBaseEntity();
//		if (theme.getAttributes() != null) {
//			for (ThemeAttribute themeAttribute : theme.getAttributes()) {
//
//				try {
//					themeBe.addAttribute(new EntityAttribute(themeBe, new Attribute(themeAttribute.getCode(),
//							themeAttribute.getCode(), new DataType("DTT_THEME")), 1.0, themeAttribute.getJson()));
//				} catch (BadDataException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//		return themeBe;
//	}
//
//	////@Test
//	public void sendCards2() {
//
//		QRules rules = GennyJbpmBaseTest.setupLocalService();
//		GennyToken userToken = new GennyToken("userToken", rules.getToken());
//		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
//		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
//		// TableUtilsTest bucketUtils = new //TableUtilsTest(beUtils);
//		
//		System.out.println("running the test");
//
//		/* initialize bucketUtils */
//		BucketUtilsTest bucketUtils = new BucketUtilsTest(beUtils);
//
//		/* initialize searchUtils */
//		SearchUtilsTest searchUtils = new SearchUtilsTest(beUtils);
//
//		/* initialize virtualAskMap */
//		Map<String, QDataAskMessage> virtualAskMap = new HashMap<String, QDataAskMessage>();
//
//		/* initialize ask set */
//		Set<QDataAskMessage> askSet = new HashSet<QDataAskMessage>();
//
//		/* initialize contextListMap */
//		Map<String, ContextList> contextListMap = new HashMap<String, ContextList>();
//
//		/* list to collect baseentity */
//		List<BaseEntity> beList = new ArrayList<BaseEntity>();
//
//		/* get the bucket-content ask */
//		Ask FRM_BUCKET_CONTENT_ASK = bucketUtils.getBucketContentAsk(contextListMap, serviceToken);
//
//		/* get the bucket-content ask */
//		Frame3 FRM_BUCKET_CONTENT = getBucketContentFrame("FRM_BUCKET_CONTENT", "test", "test");
//
//		try {
//
//			/* get the list of bucket searchBEs from the cache */
//			List<SearchEntity> searchBeList = bucketUtils.getBucketSearchBeListFromCache(serviceToken);
//			
//			/* get all the contextListMap for card */
//			contextListMap = bucketUtils.getCardContextListMap(contextListMap, serviceToken);
//			List<Context> cardContext = contextListMap.get("QUE_CARD_APPLICATION_TEMPLATE_GRP").getContextList();
//			
//			/* publish SBE_DUMMY */
//			BaseEntity SBE_DUMMY = new BaseEntity("SBE_DUMMY", "SBE_DUMMY");
//
//			Attribute contentAttribute = new Attribute("PRI_CONTENT", "content", new DataType(String.class));
//			EntityAttribute entAttr = new EntityAttribute(SBE_DUMMY, contentAttribute, 1.0, "{  \"flex\": 1 }");
//			Set<EntityAttribute> entAttrSet = new HashSet<>();
//			entAttrSet.add(entAttr);
//			SBE_DUMMY.setBaseEntityAttributes(entAttrSet);
//			
//			QDataBaseEntityMessage SBE_DUMMY_MSG = new QDataBaseEntityMessage(SBE_DUMMY);
//			SBE_DUMMY_MSG.setToken(userToken.getToken());
//			
//			String msgJson = JsonUtils.toJson(SBE_DUMMY_MSG);
//			VertxUtils.writeMsg("webcmds",msgJson);
//
//
//			/* loop through the s */
//			for (SearchEntity searchBe : searchBeList) {
//
//				String code = searchBe.getCode().split("SBE_")[1];
//
//				/* get the attributes from searchObj */
//				Map<String, String> columns = searchUtils.getTableColumns(searchBe);
//
//				/* fetch the search results */
//				QDataBaseEntityMessage msg = searchUtils.fetchSearchResults(searchBe, beUtils.getGennyToken());
//
//				/* get the application counts */
//				long totalResults = msg.getItems().length;
//
//				/* also update the searchBe with the attribute */
//				Answer totalAnswer = new Answer(beUtils.getGennyToken().getUserCode(), searchBe.getCode(),
//						"PRI_TOTAL_RESULTS", totalResults + "");
//				beUtils.addAnswer(totalAnswer);
//				beUtils.updateBaseEntity(searchBe, totalAnswer);
//
//				/* get the applications */
//				List<BaseEntity> appList = Arrays.asList(msg.getItems());
//
//				/* add the application to the baseentity list */
//				beList.addAll(appList);
//
//				/* convert app to asks */
//				List<Ask> appAsksList = searchUtils.generateQuestions(beUtils.getGennyToken(), beUtils, appList,
//						columns, beUtils.getGennyToken().getUserCode());
//				
//				/* get the templat ask for card */
//				Ask templateAsk = bucketUtils.getCardTemplate();
//
//				/* implement template ask to appAks list */
//				List<Ask> askList = bucketUtils.implementCardTemplate(code, appAsksList, templateAsk, contextListMap);
//
//				/* generate bucketContent asks for each bucket */
//				Ask bucketContentAsk = Ask.clone(FRM_BUCKET_CONTENT_ASK);
//				bucketContentAsk.setQuestionCode("QUE_BUCKET_CONTENT_" + code + "_GRP");
//				bucketContentAsk.setName(searchBe.getName());
//
//				/* link bucketContentAsk to application asks */
//				bucketContentAsk.setChildAsks(askList.toArray(new Ask[askList.size()]));
//
//				/* add the bucketContent ask to virtualAskMap */
//				virtualAskMap.put("QUE_BUCKET_CONTENT_" + code + "_GRP", new QDataAskMessage(bucketContentAsk));
//
//				/* link the bucket-content ask to bucket-content frame */
//				Frame3 bucketContent = Frame3.clone(FRM_BUCKET_CONTENT);
//				bucketContent.setCode("FRM_BUCKET_CONTENT_" + code);
//				bucketContent.setQuestionCode("QUE_BUCKET_CONTENT_" + code + "_GRP");
//				
//				/* add the contextList for the cardQuestion */
//				contextListMap.put("QUE_CARD_APPLICATION_TEMPLATE_GRP", new ContextList(cardContext));
//
//				QDataBaseEntityMessage msg2 = FrameUtils2.toMessage(bucketContent, serviceToken, askSet, contextListMap,
//						virtualAskMap);
//				msg2.setToken(userToken.getToken());
//				VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg2));
//
//			}
//
//			/* Send */
//			System.out.println("Sending application entitites");
//
//			QDataBaseEntityMessage appMsg = new QDataBaseEntityMessage(beList.toArray(new BaseEntity[0]));
//			appMsg.setToken(userToken.getToken());
//			VertxUtils.writeMsg("webcmds", JsonUtils.toJson(appMsg));
//
//			System.out.println("Sending asks from outside the loop");
//
//			/* Send asks */
//			for (QDataAskMessage askMsg : askSet) {
//
//				askMsg.setToken(userToken.getToken());
//				
//				System.out.println("Cards in this bucket :: " + askMsg.getItems()[0].getName() + "are" + askMsg.getItems()[0].getChildAsks().length);
//
//				String json = JsonUtils.toJson(askMsg);
//				VertxUtils.writeMsg("webcmds", json);
//
//			}
//
//			System.out.print("Completed");
//
//		} catch (Exception e) {
//			// TODO: handle exception
//
//		}
//	}
//
//}