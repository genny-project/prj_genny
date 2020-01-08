package life.genny.test;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.flow.core.TryCatch;
import com.google.gson.reflect.TypeToken;

import io.vertx.core.json.JsonObject;
import life.genny.models.Frame3;
import life.genny.models.FramePosition;
import life.genny.models.FrameTuple3;
import life.genny.models.GennyToken;
import life.genny.models.Theme;
import life.genny.models.ThemeAttribute;
import life.genny.models.ThemeAttributeType;
import life.genny.models.ThemePosition;
import life.genny.qwanda.Answer;
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
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.validation.Validation;
import life.genny.qwanda.validation.ValidationList;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.rules.QRules;
import life.genny.utils.BaseEntityUtils;
import life.genny.utils.TableUtils;
import life.genny.utils.TableUtilsTest;
import life.genny.utils.FrameUtils2;
import life.genny.utils.GennyJbpmBaseTest;
import life.genny.utils.OutputParam;
import life.genny.utils.RulesUtils;
import life.genny.utils.SearchUtilsTest;
import life.genny.utils.TableUtils;
import life.genny.utils.TableUtilsNew;
//import life.genny.utils.//TableUtilsTest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import life.genny.utils.VertxUtils;

public class TableView extends GennyJbpmBaseTest {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	public TableView() {
		super(false);
	}

	

	@Test
	public void test() {

//		QRules rules = GennyJbpmBaseTest.setupLocalService();
//		GennyToken userToken = new GennyToken("userToken", rules.getToken());
//		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
//		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
//
//		/* initialize bucketUtils */
//		TableUtilsTest bucketUtils = new TableUtilsTest(beUtils);
//		//TableUtilsTest tableUtils = new TableUtilsTest(beUtils);
//		TableUtilsNew tableUtils = new TableUtilsNew(beUtils);
//
//		/* get the list of bucket searchBEs to generate tables */
//		//List<SearchEntity> searchBeList = getSearchBeList();
//		List<SearchEntity> searchBeList = tableUtils.getSearchBeList();
//
//
//		/* get all the table frames */
//		Frame3 FRM_TABLE= getTableViewFrame("FRM_TABLE", "test", "test");
//
//		/* TITLE */
//			Frame3 FRM_TABLE_TITLE= getTableTitleFame("FRM_TABLE_TITLE", "test", "test");
//			
//			/* WRAPPER */
//			Frame3 FRM_TABLE_WRAPPER= getTableWrapperFrame("FRM_TABLE_WRAPPER", "test", "test");
//			
//				/* BODY */
//				Frame3 FRM_TABLE_BODY= getTableBodyFrame("FRM_TABLE_BODY", "test", "test");
//				/*Frame3 FRM_TABLE_HEADER= getTableHeaderFrame("FRM_TABLE_HEADER", "test", "test");*/
//				Frame3 FRM_TABLE_CONTENT= getTableContentFrame("FRM_TABLE_CONTENT", "test", "test");
//				
//				/* FOOTER */
//				Frame3 FRM_TABLE_FOOTER_PAGINATION= getTableFooterPaginationFrame("FRM_TABLE_FOOTER_PAGINATION", "test", "test");
//				Frame3 FRM_TABLE_RESULT_COUNT= getTableResultCountFrame("FRM_TABLE_RESULT_COUNT", "test", "test");
//				Frame3 FRM_TABLE_PAGE_INDEX= getTablePageIndexFrame("FRM_TABLE_PAGE_INDEX", "test", "test");
//				Frame3 FRM_TABLE_FOOTER= getTableFooterFame("FRM_TABLE_FOOTER", "test", "test");
//				
//				//Frame3 FRM_CONTENT = Frame3.builder("FRM_CONTENT").build();
//				
//				/* loop through the search */
//				for (SearchEntity searchBe : searchBeList) {
//
//					/* initialize virtualAskMap */
//					Map<String, QDataAskMessage> virtualAskMap = new HashMap<String, QDataAskMessage>();
//
//					/* initialize ask set */
//					Set<QDataAskMessage> askSet = new HashSet<QDataAskMessage>();
//
//					/* initialize contextListMap */
//					Map<String, ContextList> contextListMap = new HashMap<String, ContextList>();
//					
//					String code = searchBe.getCode().split("SBE_")[1];
//
//					Ask tableHeaderAsk = tableUtils.generateTableHeaderAsk(searchBe, contextListMap, serviceToken);
//					virtualAskMap.put(tableHeaderAsk.getQuestionCode(), new QDataAskMessage(tableHeaderAsk));
//					
//					Frame3 tableTitle = Frame3.clone(FRM_TABLE_TITLE);
//					tableTitle.setCode("FRM_TABLE_TITLE_" + code);
//				
////					Frame3 tableHeader = Frame3.clone(FRM_TABLE_HEADER);
////					tableHeader.setCode("FRM_TABLE_HEADER_" + code);
////					tableHeader.setQuestionCode(tableHeaderAsk.getQuestionCode());
//				
////					Frame3 tableContent = Frame3.clone(FRM_TABLE_HEADER);
////					tableContent.setCode("FRM_TABLE_CONTENT_" + code);
//				
////					Frame3 tableBody = Frame3.clone(FRM_TABLE_BODY);
////					tableBody.setCode("FRM_TABLE_BODY_" + code);
////					tableBody.getFrames().add(new FrameTuple3(tableHeader, FramePosition.NORTH, 1.0));
////					tableBody.getFrames().add(new FrameTuple3(FRM_TABLE_CONTENT, FramePosition.CENTRE, 1.0));
//
//					Frame3 tableFooter = Frame3.clone(FRM_TABLE_FOOTER);
//					tableFooter.setCode("FRM_TABLE_FOOTER_" + code);
//					tableFooter.getFrames().add(new FrameTuple3(FRM_TABLE_RESULT_COUNT, FramePosition.WEST, 1.0));
//					tableFooter.getFrames().add(new FrameTuple3(FRM_TABLE_PAGE_INDEX, FramePosition.WEST, 1.0));
//					tableFooter.getFrames().add(new FrameTuple3(FRM_TABLE_FOOTER_PAGINATION, FramePosition.EAST, 1.0));
//
////					Frame3 tableWrapper = Frame3.clone(FRM_TABLE_WRAPPER);
////					tableWrapper.setCode("FRM_TABLE_WRAPPER_" + code);
////					tableWrapper.getFrames().add(new FrameTuple3(tableBody, FramePosition.CENTRE, 1.0));
////					tableWrapper.getFrames().add(new FrameTuple3(tableFooter, FramePosition.SOUTH, 1.0));
//
////					Frame3 frame = Frame3.clone(FRM_TABLE);
////					frame.setCode("FRM_TABLE_" + code);
////					frame.getFrames().add(new FrameTuple3(tableTitle, FramePosition.NORTH, 1.0));
////					frame.getFrames().add(new FrameTuple3(tableWrapper, FramePosition.CENTRE, 1.0));
//
//					/* build the tab content frame */
////					Frame3 frameContent = Frame3.clone(FRM_CONTENT);
////					frameContent.getFrames().add(new FrameTuple3(frame, FramePosition.NORTH, 1.0));
//					
//					Frame3 frameContent = Frame3.builder("FRM_CONTENT")
//							.addFrame(frame, FramePosition.NORTH).end()
//							.build();
//
//					QDataBaseEntityMessage msg = FrameUtils2.toMessage(frameContent, serviceToken, askSet, contextListMap,
//					virtualAskMap);
//					msg.setToken(userToken.getToken());
//					msg.setReplace(true);
//					
//					System.out.println("code :: " + "FRM_TABLE_" + code);
//					VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg));
//
//					for (QDataAskMessage askMsg : askSet) {
//
//							askMsg.setToken(userToken.getToken());
//
//							String json = JsonUtils.toJson(askMsg);
//							VertxUtils.writeMsg("webcmds", json);
//
//					}
//					System.out.println("Sent");
//
//					// /* generating frame msg and saving to cache */
//          // QDataBaseEntityMessage frameMsg = FrameUtils2.toMessage(frame, serviceToken, askSet, contextListMap, virtualAskMap);
//
//          // /* cache the frame */
//          // VertxUtils.putObject(serviceToken.getRealm(), "", frame.getCode(), frame, serviceToken.getToken());
//
//          // /* cache the QDataBaseEntityMessage */
//          // VertxUtils.putObject(serviceToken.getRealm(), "", frame.getCode() + "_MSG", frameMsg, serviceToken.getToken());
//          
//          // /* cache the ask */
//          // VertxUtils.writeCachedJson(serviceToken.getRealm(),"ASK_" + frame.getCode(), JsonUtils.toJson(askSet.toArray()),serviceToken.getToken());
//
//				}
//				
//
//			// 	/* build the tab content frame */
//			// Frame3 FRM_CONTENT = Frame3.builder("FRM_CONTENT")
//			// 					.addFrame(table, FramePosition.NORTH).end()
//			// 					.build();
//
//			// QDataBaseEntityMessage msg = FrameUtils2.toMessage(FRM_CONTENT, serviceToken, askSet, contextListMap,
//			// 		virtualAskMap);
//			// msg.setToken(userToken.getToken());
//			// VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg));
//
//			// for (QDataAskMessage askMsg : askSet) {
//
//			// 	askMsg.setToken(userToken.getToken());
//
//			// 	String json = JsonUtils.toJson(askMsg);
//			// 	VertxUtils.writeMsg("webcmds", json);
//
//			// }
//			
//			System.out.println("Success");
//	
	}


	//@Test
	public void treeTest() {

		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);

		/* initialize bucketUtils */
		TableUtilsTest bucketUtils = new TableUtilsTest(beUtils);
		//TableUtilsTest tableUtils = new TableUtilsTest(beUtils);
		TableUtilsNew tableUtils = new TableUtilsNew(beUtils);

		/* initialize virtualAskMap */
		Map<String, QDataAskMessage> virtualAskMap = new HashMap<String, QDataAskMessage>();

		/* initialize contextListMap */
		Map<String, ContextList> contextListMap = new HashMap<String, ContextList>();

		List<SearchEntity> searchBeList = tableUtils.getSearchBeList();
		for (SearchEntity searchBe : searchBeList) {

			String code = searchBe.getCode().split("SBE_")[1];

			QDataBaseEntityMessage FRM_MSG = VertxUtils.getObject(userToken.getRealm(), "", "FRM_TABLE_" + code + "" + "_MSG", QDataBaseEntityMessage.class, userToken.getToken());

			String payload = JsonUtils.toJson(FRM_MSG);
			JSONObject js = new JSONObject(payload);
			String payload2 = js.toString();
			VertxUtils.writeMsg("webcmds", payload2);
			
			JsonObject tokenObj = VertxUtils.readCachedJson(userToken.getRealm(),"ASK_FRM_TABLE_" + code,userToken.getToken());
		
			QDataAskMessage[] askSet = JsonUtils.fromJson(tokenObj.getString("value"), QDataAskMessage[].class);
			
			System.out.println("Sending Asks");
			for(QDataAskMessage ask : askSet) {
				
				ask.setToken(userToken.getToken());
				ask.setReplace(false);	
				String askJson = JsonUtils.toJson(ask);
				VertxUtils.writeMsg("webcmds",askJson );
			}
		}

	}



	public Frame3 getTableViewFrame(String name, String target, String questionCode) {

		/* build the theme */
		Theme THM_WIDTH_100_PERCENT = Theme.builder("THM_WIDTH_100_PERCENT")
							.addAttribute()
								.width("100%")
							.end()
							.build();

		Theme THM_PROJECT_COLOR_BACKGROUND = Theme.builder("THM_PROJECT_COLOR_BACKGROUND")
							.addAttribute()
								.backgroundColor("#F6F6F6")
								.color("#000000")
							.end()
							.build();  

		Theme THM_PADDING_20 = Theme.builder("THM_PADDING_20")
							.addAttribute()
								.padding(20)
							.end()
							.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
							.build();   

		/* build the frame */
		Frame3 frame = Frame3.builder(name)
								.addTheme(THM_WIDTH_100_PERCENT, ThemePosition.WRAPPER).end()
								.addTheme(THM_PROJECT_COLOR_BACKGROUND).end()
								.addTheme(THM_PADDING_20, ThemePosition.WRAPPER).end()
								.build();

		/* return the frame */
		return frame;
	}

	public Frame3 getTableTitleFame(String name, String target, String questionCode) {

		/* build the theme */
		Theme THM_TITLE = Theme.builder("THM_TITLE")
										.addAttribute()
											.bold(true)
											.size("lg")        
											.textAlign("left")
										.end()
										.build();

		Theme THM_TITLE_WRAPPER = Theme.builder("THM_TITLE_WRAPPER")
										.addAttribute()
											.padding(20)
											.end()
										.build();   
										
		Theme THM_QUESTION_GRP_LABEL = Theme.builder("THM_QUESTION_GRP_LABEL")
										.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_LABEL, true).end()
										.build();  

		Theme THM_BH_GROUP_WRAPPER = Theme.builder("THM_BH_GROUP_WRAPPER")
			.addAttribute()
				.width("100%")
				.padding(10)
			.end()
			.build();

			Theme THM_LABEL_BOLD = Theme.builder("THM_LABEL_BOLD")
			.addAttribute()
							.bold(true)
							.size("md")
							.paddingX(10)
			.end()
			.addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true).end()
			.build();
                        

		/* build the frame */
		Frame3 frame = Frame3.builder("FRM_TABLE_TITLE")
									.question("QUE_TABLE_TITLE_TEST")
										.addTheme(THM_TITLE).vcl(VisualControlType.VCL_LABEL).end()
										.addTheme(THM_TITLE_WRAPPER).vcl(VisualControlType.VCL_WRAPPER).end()
										.addTheme(THM_QUESTION_GRP_LABEL).vcl(VisualControlType.GROUP).end()
										//.addTheme(THM_LABEL_BOLD).vcl(VisualControlType.VCL_LABEL).end()

										//.addTheme(THM_BH_GROUP_WRAPPER).vcl(VisualControlType.GROUP_WRAPPER).end()
									.end()
									.build();

		/* return the frame */
		return frame;
	}

	public Frame3 getTableWrapperFrame(String name, String target, String questionCode) {

		/* build the theme */
		Theme THM_SCROLL_HORIZONTAL = Theme.builder("THM_SCROLL_HORIZONTAL")
										.addAttribute()
											.overflowX("auto")
										.end()
										.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
										.build();  

		Theme THM_BOX_SHADOW_XS = Theme.builder("THM_BOX_SHADOW_XS")
                    .addAttribute()
                     .shadowColor("#dedede")
                      .shadowOpacity(0.5)
                      .shadowRadius(90)
                      .shadowOffset()
                      .width(0)
                      .height(0)
                      .end()
                    .end()
                    .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
										.build();       
										
		Theme THM_PROJECT_COLOR_SURFACE = Theme.builder("THM_PROJECT_COLOR_SURFACE")
									.addAttribute()
										.backgroundColor("#FFFFFF")
										.color("#000000")
									.end()
									.build();  

		/* build the frame */
		Frame3 frame = Frame3.builder(name)
									.addTheme(THM_SCROLL_HORIZONTAL,ThemePosition.CENTRE).end()
									.addTheme(THM_BOX_SHADOW_XS,ThemePosition.WRAPPER).end()
									.addTheme(THM_PROJECT_COLOR_SURFACE).end()
									.build();

		/* return the frame */
		return frame;
	}
	
	public Frame3 getTableBodyFrame(String name, String target, String questionCode) {

		/* build the theme */
		Theme THM_SCROLL_VERTICAL = Theme.builder("THM_SCROLL_VERTICAL")
																.addAttribute()
																	.overflowY("auto")
																.end()
																.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
																.build();  

		/* build the frame */
		Frame3 frame = Frame3.builder(name)
									.addTheme(THM_SCROLL_VERTICAL,ThemePosition.CENTRE).end()
									.build();

		/* return the frame */
		return frame;
	}
	
	public Frame3 getTableHeaderFrame(String name, String target, String questionCode) {

		/* build the theme */
		Theme THM_TABLE_BORDER = Theme.builder("THM_TABLE_BORDER")
                  .addAttribute()
                    .borderBottomWidth(1)
                    .borderColor("#f6f6f6")
                    .borderStyle("solid")
                  .end()
                  .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                  .build();
		
		Theme THM_QUESTION_GRP_LABEL = Theme.builder("THM_QUESTION_GRP_LABEL")
                  .addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_LABEL, true).end()
									.build(); 
									
		Theme THM_WIDTH_100_PERCENT_NO_INHERIT = Theme.builder("THM_WIDTH_100_PERCENT_NO_INHERIT")
                  .addAttribute()
                    .width("100%")                
                  .end()
                  .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
									.build();
									
		Theme THM_DISPLAY_HORIZONTAL = Theme.builder("THM_DISPLAY_HORIZONTAL")
									.addAttribute()
										.flexDirection("row").end()
									.build();

		Theme THM_TABLE_HEADER_CELL_WRAPPER = Theme.builder("THM_TABLE_HEADER_CELL_WRAPPER")
                  .addAttribute()
                    .width("initial")
                    .flexGrow(1)
                    .flexShrink(1)
                    .flexBasis("auto")
                    .padding(10)
                    .justifyContent("flex-start")
                  .end()
									.build();
									
		Theme THM_TABLE_HEADER_CELL_GROUP_LABEL = Theme.builder("THM_TABLE_HEADER_CELL_GROUP_LABEL")
                  .addAttribute()
                    .paddingLeft(10)
                    .alignSelf("flex-start")
                  .end()
									.build();
									
		Theme THM_DISPLAY_VERTICAL = Theme.builder("THM_DISPLAY_VERTICAL")
			 						.addAttribute().flexDirection("column").end()
									.build();

		Theme THM_PADDING_X_10 = Theme.builder("THM_PADDING_X_10")
									.addAttribute()
										.paddingX(10)
									.end()
									.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
									.build();

		Theme THM_TABLE_ROW_CELL = Theme.builder("THM_TABLE_ROW_CELL")
                  .addAttribute()
                    .flexGrow(1)
                    .flexBasis("auto")
                  .end()
                  .build();

		Validation tableCellValidation = new Validation("VLD_ANYTHING", "Anything", ".*");

		List<Validation> tableCellValidations = new ArrayList<>();
		tableCellValidations.add(tableCellValidation);
		
		ValidationList tableCellValidationList = new ValidationList();
		tableCellValidationList.setValidationList(tableCellValidations);

		DataType tableCellDataType = new DataType("DTT_TABLE_CELL_GRP", tableCellValidationList, "Table Cell Group", "");
												
		Frame3 frame = Frame3.builder(name)
						.addTheme(THM_TABLE_BORDER).end()
						.question("QUE_TABLE_HEADER_GRP")
							.addTheme(THM_PADDING_X_10).vcl(VisualControlType.GROUP_CONTENT_WRAPPER).end()
							.addTheme(THM_QUESTION_GRP_LABEL).vcl(VisualControlType.GROUP).dataType(tableCellDataType).end()
							.addTheme(THM_WIDTH_100_PERCENT_NO_INHERIT).vcl(VisualControlType.GROUP).end()
							.addTheme(THM_DISPLAY_HORIZONTAL).weight(2.0).end()
							.addTheme(THM_TABLE_HEADER_CELL_WRAPPER).vcl(VisualControlType.VCL_WRAPPER).end()
							.addTheme(THM_TABLE_HEADER_CELL_GROUP_LABEL).vcl(VisualControlType.GROUP_LABEL).end()
							.addTheme(THM_DISPLAY_VERTICAL).dataType(tableCellDataType).weight(1.0).end()
							.addTheme(THM_TABLE_ROW_CELL).dataType(tableCellDataType).weight(1.0).end()
						.end()
						.build();

		/* return the frame */
		return frame;
	}

	public Frame3 getTableContentFrame(String name, String target, String questionCode) {

		/* build the theme */
		Theme THM_TABLE_BORDER = Theme.builder("THM_TABLE_BORDER")
		.addAttribute()
			.borderBottomWidth(1)
			.borderColor("#f6f6f6")
			.borderStyle("solid")
		.end()
		.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
		.build();

		Theme THM_TABLE_CONTENT_CENTRE = Theme.builder("THM_TABLE_CONTENT_CENTRE")
                  .addAttribute()
                    .justifyContent("flex-start")
                    .paddingX(10)
                  .end()
                  .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
									.build();
									

    Theme THM_WIDTH_100_PERCENT_NO_INHERIT = Theme.builder("THM_WIDTH_100_PERCENT_NO_INHERIT")
		.addAttribute()
			.width("100%")                
		.end()
		.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
		.build();

		Theme THM_DISPLAY_HORIZONTAL = Theme.builder("THM_DISPLAY_HORIZONTAL")
		.addAttribute()
			.flexDirection("row").end()
		.build();

		Theme THM_TABLE_ROW_CONTENT_WRAPPER = Theme.builder("THM_TABLE_ROW_CONTENT_WRAPPER")
		.addAttribute()
			.width("100%")
			.flexDirection("row")
		.end()
		.build();

		Theme THM_TABLE_ROW = Theme.builder("THM_TABLE_ROW")
		.addAttribute()
			.width("100%")
			.padding(10)
		.end()
		.build();

		Theme THM_TABLE_CONTENT = Theme.builder("THM_TABLE_CONTENT")
		.addAttribute()
			.width("100%")
		.end()
		.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
		.build();  

		Theme THM_TABLE_ROW_CELL = Theme.builder("THM_TABLE_ROW_CELL")
		.addAttribute()
			.flexGrow(1)
			.flexBasis("auto")
		.end()
		.build();


		Validation tableRowValidation = new Validation("VLD_ANYTHING", "Anything", ".*");

		List<Validation> tableRowValidations = new ArrayList<>();
		tableRowValidations.add(tableRowValidation);

		ValidationList tableRowValidationList = new ValidationList();
		tableRowValidationList.setValidationList(tableRowValidations);

		DataType tableRowDataType = new DataType("DTT_TABLE_ROW_GRP", tableRowValidationList, "Table Row Group", "");

		/* build the frame */

		Frame3 frame =  Frame3.builder(name)
										.addTheme(THM_TABLE_BORDER).end()
										.addTheme(THM_TABLE_CONTENT_CENTRE, ThemePosition.CENTRE).end()
										.question("QUE_TABLE_RESULTS_GRP")
											.addTheme(THM_WIDTH_100_PERCENT_NO_INHERIT).vcl(VisualControlType.GROUP_WRAPPER).end()
											.addTheme(THM_TABLE_BORDER).dataType(tableRowDataType).end()
											.addTheme(THM_DISPLAY_HORIZONTAL).dataType(tableRowDataType).weight(1.0).end()
											.addTheme(THM_TABLE_ROW_CONTENT_WRAPPER).dataType(tableRowDataType).vcl(VisualControlType.GROUP).weight(1.0).end()
											.addTheme(THM_TABLE_ROW).dataType(tableRowDataType).weight(1.0).end()
											.addTheme(THM_TABLE_CONTENT).vcl(VisualControlType.GROUP).end()			
											.addTheme(THM_TABLE_ROW_CELL).vcl(VisualControlType.VCL_WRAPPER).end()			
										.end()
										.build();

		/* return the frame */
		return frame;
	}

	public Frame3 getTableFooterPaginationFrame(String name, String target, String questionCode) {

		/* build the theme */
		Theme THM_DISPLAY_HORIZONTAL = Theme.builder("THM_DISPLAY_HORIZONTAL")
																	.addAttribute()
																		.flexDirection("row").end()
																	.build();
		
		Theme THM_JUSTIFY_CONTENT_FLEX_END = Theme.builder("THM_JUSTIFY_CONTENT_FLEX_END")
																	.addAttribute().justifyContent("flex-end").end()
																	.build();			
																	
		Theme THM_PROJECT_COLOR_PRIMARY_VARIANT_LIGHT = Theme.builder("THM_PROJECT_COLOR_PRIMARY_VARIANT_LIGHT")
																	.addAttribute()
																		.backgroundColor("#395268")
																		.color("#FFFFFF")
																	.end()
																	.build();  

		Theme THM_TABLE_HEADER_CELL_WRAPPER = Theme.builder("THM_TABLE_HEADER_CELL_WRAPPER")
																	.addAttribute()
																		.width("initial")
																		.flexGrow(1)
																		.flexShrink(1)
																		.flexBasis("auto")
																		.padding(10)
																		.justifyContent("flex-start")
																	.end()
																	.build();

		Theme THM_TEXT_ALIGN_CENTER = Theme.builder("THM_TEXT_ALIGN_CENTER")
																	.addAttribute()
																		.textAlign("center")
																	.end()
																	.build();

		Validation validation = new Validation("VLD_ANYTHING", "Anything", ".*");
		List<Validation> validations = new ArrayList<>();
		validations.add(validation);
										
		ValidationList buttonValidationList = new ValidationList();
		buttonValidationList.setValidationList(validations);

		DataType buttonDataType = new DataType("DTT_EVENT", buttonValidationList, "Event", "");		

		/* build the frame */
	  Frame3 frame = Frame3.builder(name)
                      .addTheme(THM_DISPLAY_HORIZONTAL).end()
                      .addTheme(THM_JUSTIFY_CONTENT_FLEX_END, ThemePosition.CENTRE).end()
                      .question("QUE_TABLE_FOOTER_GRP")
                        .addTheme(THM_DISPLAY_HORIZONTAL).end()
                        .addTheme(THM_PROJECT_COLOR_PRIMARY_VARIANT_LIGHT).dataType(buttonDataType).end()
                        .addTheme(THM_TABLE_HEADER_CELL_WRAPPER).vcl(VisualControlType.VCL_WRAPPER).end()
                        .addTheme(THM_TEXT_ALIGN_CENTER).vcl(VisualControlType.VCL_INPUT).end()
                      .end()
                      .build();

		/* return the frame */
		return frame;
	}

	public Frame3 getTableResultCountFrame(String name, String target, String questionCode) {

		/* build the theme */
	
		Theme THM_DISPLAY_HORIZONTAL = Theme.builder("THM_DISPLAY_HORIZONTAL")
						.addAttribute()
							.flexDirection("row").end()
						.build(); 

		Theme THM_LABEL_BOLD = Theme.builder("THM_LABEL_BOLD")
						.addAttribute()
										.bold(true)
										.size("md")
										.paddingX(10)
						.end()
						.addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true).end()
						.build();

		/* build the frame */
		Frame3 frame = Frame3.builder(name)
									.question("QUE_TABLE_TOTAL_RESULT_COUNT")
										.addTheme(THM_LABEL_BOLD).vcl(VisualControlType.VCL_LABEL).end()
										.addTheme(THM_DISPLAY_HORIZONTAL).vcl(VisualControlType.VCL_WRAPPER).end()
									.end()
									.build();

		/* return the frame */
		return frame;
	}

	public Frame3 getTablePageIndexFrame(String name, String target, String questionCode) {

		/* build the theme */
		Theme THM_LABEL_BOLD = Theme.builder("THM_LABEL_BOLD")
									.addAttribute()
										.bold(true)
										.size("md")
										.paddingX(10)
									.end()
									.addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true).end()
									.build();

		Theme THM_DISPLAY_HORIZONTAL = Theme.builder("THM_DISPLAY_HORIZONTAL")
									.addAttribute()
										.flexDirection("row").end()
									.build();

		/* build the frame */
		Frame3 frame = Frame3.builder(name)
									.question("QUE_TABLE_PAGE_INDEX")
										.addTheme(THM_LABEL_BOLD).vcl(VisualControlType.VCL_LABEL).end()
										.addTheme(THM_DISPLAY_HORIZONTAL).vcl(VisualControlType.VCL_WRAPPER).end()
									.end()
									.build();

		/* return the frame */
		return frame;
	}

	public Frame3 getTableFooterFame(String name, String target, String questionCode) {

		/* build the theme */
		Theme THM_DISPLAY_HORIZONTAL = Theme.builder("THM_DISPLAY_HORIZONTAL")
																			.addAttribute()
																				.flexDirection("row").end()
																			.build();

		/* build the frame */
		Frame3 frame = Frame3.builder(name)
					.addTheme(THM_DISPLAY_HORIZONTAL).end()                     
					.build();

		/* return the frame */
		return frame;
	}
	
	/* Generate Asks */
	public Ask getTableHeaderAsk(Map<String, ContextList> contextListMap, GennyToken serviceToken) {

		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
		TableUtilsTest bucketUtils = new TableUtilsTest(beUtils);

		Theme THM_QUESTION_GRP_LABEL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_QUESTION_GRP_LABEL",
				Theme.class, serviceToken.getToken());
		Theme THM_DISPLAY_VERTICAL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_DISPLAY_VERTICAL",
				Theme.class, serviceToken.getToken());
		Theme THM_DISPLAY_HORIZONTAL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_DISPLAY_HORIZONTAL",
				Theme.class, serviceToken.getToken());
		Theme THM_WIDTH_100_PERCENT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_WIDTH_100_PERCENT",
				Theme.class, serviceToken.getToken());
		Theme THM_BH_ROW_ONE_GRP_WRAPPER = VertxUtils.getObject(serviceToken.getRealm(), "",
				"THM_BH_ROW_ONE_GRP_WRAPPER", Theme.class, serviceToken.getToken());
		Theme THM_BH_ROW_ONE_GRP_LABEL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BH_ROW_ONE_GRP_LABEL",
				Theme.class, serviceToken.getToken());
		Theme THM_BH_ROW_ONE_GRP_CONTENT_WRAPPER = VertxUtils.getObject(serviceToken.getRealm(), "",
				"THM_BH_ROW_ONE_GRP_CONTENT_WRAPPER", Theme.class, serviceToken.getToken());
		Theme THM_BH_ROW_ONE_VCL_INPUT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BH_ROW_ONE_VCL_INPUT",
				Theme.class, serviceToken.getToken());
		Theme THM_BH_ROW_TWO_VCL_WRAPPER = VertxUtils.getObject(serviceToken.getRealm(), "",
				"THM_BH_ROW_TWO_VCL_WRAPPER", Theme.class, serviceToken.getToken());
		Theme THM_BH_ROW_TWO_GRP_CONTENT_WRAPPER = VertxUtils.getObject(serviceToken.getRealm(), "",
				"THM_BH_ROW_TWO_GRP_CONTENT_WRAPPER", Theme.class, serviceToken.getToken());
		Theme THM_BH_ROW_TWO_INPUT_FIELD = VertxUtils.getObject(serviceToken.getRealm(), "",
				"THM_BH_ROW_TWO_INPUT_FIELD", Theme.class, serviceToken.getToken());
		Theme THM_ICON = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_ICON", Theme.class,
				serviceToken.getToken());
		Theme THM_BH_GROUP_WRAPPER = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BH_GROUP_WRAPPER",
				Theme.class, serviceToken.getToken());

		BaseEntity ICN_SORT = beUtils.getBaseEntityByCode("ICN_SORT");

		/*
		 * we create context here
		 */

		/* row1Context context */
		List<Context> row1Context = new ArrayList<>();
		row1Context.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_DISPLAY_HORIZONTAL),
				VisualControlType.GROUP_WRAPPER, 1.0));
		row1Context.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_BH_ROW_ONE_GRP_WRAPPER),
				VisualControlType.GROUP_WRAPPER, 1.0));
		row1Context.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_BH_ROW_ONE_GRP_LABEL),
				VisualControlType.GROUP_LABEL, 1.0));
		row1Context.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_BH_ROW_ONE_GRP_CONTENT_WRAPPER),
				VisualControlType.GROUP_CONTENT_WRAPPER, 1.0));
		row1Context.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_BH_ROW_ONE_VCL_INPUT),
				VisualControlType.VCL_INPUT, 1.0));

		/* row2Context context */
		List<Context> row2Context = new ArrayList<>();
		row2Context.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_DISPLAY_HORIZONTAL),
				VisualControlType.GROUP_CONTENT_WRAPPER, 1.0));
		row2Context.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_BH_ROW_TWO_VCL_WRAPPER),
				VisualControlType.VCL_WRAPPER, 1.0));
		row2Context.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_BH_ROW_TWO_GRP_CONTENT_WRAPPER),
				VisualControlType.GROUP_CONTENT_WRAPPER, 1.0));

		/* bucketCountContextList context */
		List<Context> bucketCountContextList = new ArrayList<>();
		bucketCountContextList.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_QUESTION_GRP_LABEL),
				VisualControlType.GROUP_WRAPPER, 1.0));

		/* bucketSearchContextList context */
		List<Context> bucketSearchContextList = new ArrayList<>();
		bucketSearchContextList.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_BH_ROW_TWO_INPUT_FIELD),
				VisualControlType.VCL_WRAPPER, 1.0));

		/* bucketSortContextList context */
		List<Context> bucketSortContextList = new ArrayList<>();
		bucketSortContextList
				.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_ICON), VisualControlType.VCL, 1.0));
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

		Attribute searchAttribute = new Attribute("PRI_NAME", "Search",
				new DataType("Text", searchValidationList, "Text"));

		Attribute questionAttribute = RulesUtils.getAttribute("QQQ_QUESTION_GROUP", serviceToken.getToken());
		Attribute tableCellAttribute = RulesUtils.getAttribute("QQQ_QUESTION_GROUP_TABLE_CELL",
				serviceToken.getToken());

		/* Initialize Table Header Ask group */
		Question bucketHeaderQuestion = new Question("QUE_BUCKET_HEADER_GRP", "Table Header", questionAttribute, true);
		Ask bucketHeaderAsk = new Ask(bucketHeaderQuestion, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");

		/* row-one-ask */
		Question row1Ques = new Question("QUE_BUCKET_HEADER_ROW_ONE_GRP", "Row One", tableCellAttribute, false);
		Ask row1Ask = new Ask(row1Ques, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");

		/* count ask */
		Question bucketCountQues = new Question("QUE_BUCKET_COUNT", countAttribute.getName(), countAttribute, false);
		Ask bucketCountAsk = new Ask(bucketCountQues, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");

		Ask[] row1ChildAsks = { bucketCountAsk };
		row1Ask.setChildAsks(row1ChildAsks);

		/* row-two-ask */
		Question row2Ques = new Question("QUE_BUCKET_HEADER_ROW_TWO_GRP", "Row Two", tableCellAttribute, false);
		Ask row2Ask = new Ask(row2Ques, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");

		/* search ask */
		Question bucketSearchQues = new Question("QUE_BUCKET_SEARCH", searchAttribute.getName(), searchAttribute,
				false);
		Ask bucketSearchAsk = new Ask(bucketSearchQues, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");

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

	public Ask getTableFooterAsk(Map<String, ContextList> contextListMap, GennyToken serviceToken) {

		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
		TableUtilsTest bucketUtils = new TableUtilsTest(beUtils);

		/* get the themes */
		Theme THM_DISPLAY_HORIZONTAL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_DISPLAY_HORIZONTAL",
				Theme.class, serviceToken.getToken());
		Theme THM_WIDTH_100_PERCENT = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_WIDTH_100_PERCENT",
				Theme.class, serviceToken.getToken());
		Theme THM_ICON = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_ICON", Theme.class,
				serviceToken.getToken());
		Theme THM_JUSTIFY_CONTENT_SPACE_AROUND = Theme.builder("THM_JUSTIFY_CONTENT_SPACE_AROUND").addAttribute()
				.justifyContent("space-around").end().build();

		/* get the baseentities */
		BaseEntity ICN_ARROW_FORWARD_IOS = beUtils.getBaseEntityByCode("ICN_ARROW_FORWARD_IOS");
		BaseEntity ICN_ARROW_BACK_IOS = beUtils.getBaseEntityByCode("ICN_ARROW_BACK_IOS");

		/* get the attributes */
		Attribute questionAttribute = RulesUtils.attributeMap.get("QQQ_QUESTION_GROUP");
		Attribute nextAttribute = RulesUtils.attributeMap.get("PRI_NEXT_BTN");
		Attribute prevAttribute = RulesUtils.attributeMap.get("PRI_PREVIOUS_BTN");

		/* we create context here */

		/* bucketFooter context */
		List<Context> bucketFooterContext = new ArrayList<>();
		bucketFooterContext.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_DISPLAY_HORIZONTAL),
				VisualControlType.GROUP_CONTENT_WRAPPER, 1.0));
		bucketFooterContext.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_WIDTH_100_PERCENT),
				VisualControlType.GROUP_WRAPPER, 1.0));
		bucketFooterContext.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_JUSTIFY_CONTENT_SPACE_AROUND),
				VisualControlType.GROUP_CONTENT_WRAPPER, 1.0));

		/* nextTable context */
		List<Context> nextTableContext = new ArrayList<>();
		nextTableContext.add(new Context(ContextType.ICON, ICN_ARROW_FORWARD_IOS, VisualControlType.VCL_ICON, 1.0));
		nextTableContext
				.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_ICON), VisualControlType.VCL, 1.0));

		/* prevTable context */
		List<Context> prevTableContext = new ArrayList<>();
		prevTableContext.add(new Context(ContextType.ICON, ICN_ARROW_BACK_IOS, VisualControlType.VCL_ICON, 1.0));
		prevTableContext
				.add(new Context(ContextType.THEME, bucketUtils.getThemeBe(THM_ICON), VisualControlType.VCL, 1.0));

		/* add the contextList to contextMap */
		contextListMap.put("QUE_BUCKET_FOOTER_GRP", new ContextList(bucketFooterContext));
		contextListMap.put("QUE_NEXT_BUCKET", new ContextList(nextTableContext));
		contextListMap.put("QUE_PREV_BUCKET", new ContextList(prevTableContext));

		/* Initialize Table Footer Ask group */
		Question bucketFooterQuestion = new Question("QUE_BUCKET_FOOTER_GRP", "Footer Group", questionAttribute, true);
		Ask bucketFooterAsk = new Ask(bucketFooterQuestion, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");

		/* next ask */
		Question nextTableQues = new Question("QUE_NEXT_BUCKET", "", nextAttribute, false);
		Ask nextTableAsk = new Ask(nextTableQues, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");

		/* prev ask */
		Question prevTableQues = new Question("QUE_PREV_BUCKET", "", prevAttribute, false);
		Ask prevTableAsk = new Ask(prevTableQues, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");

		/* set the child asks */
		Ask[] bucketChildAsksArray = { prevTableAsk, nextTableAsk };
		bucketFooterAsk.setChildAsks(bucketChildAsksArray);

		return bucketFooterAsk;

	}

	public Ask getTableContentAsk(Map<String, ContextList> contextListMap, GennyToken serviceToken) {

		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);

		Attribute questionAttribute = RulesUtils.getAttribute("QQQ_QUESTION_GROUP", serviceToken.getToken());
		Question bucketContentQuestion = new Question("QUE_BUCKET_CONTENT_GRP", "", questionAttribute, true);
		Ask bucketContentAsk = new Ask(bucketContentQuestion, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");
		return bucketContentAsk;

	}

	public BaseEntity getThemeBe(Theme theme) {

		BaseEntity themeBe = null;
		themeBe = theme.getBaseEntity();
		if (theme.getAttributes() != null) {
			for (ThemeAttribute themeAttribute : theme.getAttributes()) {

				try {
					themeBe.addAttribute(new EntityAttribute(themeBe, new Attribute(themeAttribute.getCode(),
							themeAttribute.getCode(), new DataType("DTT_THEME")), 1.0, themeAttribute.getJson()));
				} catch (BadDataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return themeBe;
	}

	public List<SearchEntity> getSearchBeList(){

		List<SearchEntity> bucketSearchBeList = new ArrayList<SearchEntity>();
		
		SearchEntity SBE_INTERNS = new SearchEntity("SBE_INTERNS", "Interns")
													.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "PER_%")
													.addFilter("PRI_IS_INTERN", true)
													.addColumn("PRI_INTERN_NAME", "Name")
													.addColumn("PRI_STATUS", "Status")
													.addColumn("PRI_EDU_PROVIDER_NAME", "Edu Provider")
													.addColumn("PRI_INTERN_MOBILE", "Mobile")
													.addColumn("PRI_ADDRESS_FULL", "Address")
													.setPageStart(0).setPageSize(10);
		SearchEntity SBE_CONTACTS = new SearchEntity("SBE_CONTACTS", "Contacts")
													.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "PER_%")
													.addFilter("PRI_IS_INTERN", true)
													.addColumn("PRI_INTERN_NAME", "Contacts Name")
													.addColumn("PRI_STATUS", "Status")
													.addColumn("PRI_EDU_PROVIDER_NAME", "Company")
													.addColumn("PRI_INTERN_MOBILE", "Mobile")
													.addColumn("PRI_ADDRESS_FULL", "Address")
													.setPageStart(0).setPageSize(10);
		SearchEntity SBE_COMPANIES = new SearchEntity("SBE_COMPANIES", "Companies")
													.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "PER_%")
													.addFilter("PRI_IS_INTERN", true)
													.addColumn("PRI_INTERN_NAME", "Name")
													.addColumn("PRI_STATUS", "Status")
													.addColumn("PRI_EDU_PROVIDER_NAME", "Companies")
													.addColumn("PRI_INTERN_MOBILE", "Mobile")
													.addColumn("PRI_ADDRESS_FULL", "Address")
													.setPageStart(0).setPageSize(10);

		bucketSearchBeList.add(SBE_COMPANIES);
		bucketSearchBeList.add(SBE_INTERNS);
		bucketSearchBeList.add(SBE_CONTACTS);
		return bucketSearchBeList;

	}

}