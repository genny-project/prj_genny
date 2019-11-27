package life.genny.test;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.flow.core.TryCatch;

import life.genny.models.Frame3;
import life.genny.models.Frame3.Builder;
import life.genny.models.FramePosition;
import life.genny.models.FrameTuple3;
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
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityQuestion;
import life.genny.qwanda.entity.SearchEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwanda.validation.Validation;
import life.genny.qwanda.validation.ValidationList;
import life.genny.qwandautils.JsonUtils;
import life.genny.rules.QRules;
import life.genny.utils.BaseEntityUtils;
import life.genny.utils.FrameUtils2;
import life.genny.utils.RulesUtils;
import life.genny.utils.TableUtils;
import life.genny.utils.TableUtilsTest;
import life.genny.utils.VertxUtils;
import life.genny.qwanda.datatype.DataType;
import io.vertx.core.json.JsonObject;

public class BucketView extends GennyJbpmBaseTest {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

    
    public BucketView() {
        super(false);
    }

   // @Test
    public void smallTest() {

      QRules rules = GennyJbpmBaseTest.setupLocalService();
      GennyToken userToken = new GennyToken("userToken", rules.getToken());
      GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
      BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
      TableUtilsTest tableUtils = new TableUtilsTest(beUtils);

      /* initialize  virtualAskMap */
      Map<String, QDataAskMessage> virtualAskMap = new HashMap<String, QDataAskMessage>();
      
      /* initialize  contextListMap */
      Map<String, ContextList> contextListMap = new HashMap<String, ContextList>();

      JsonObject tokenObj = VertxUtils.readCachedJson(userToken.getRealm(),"ASK_FRM_BUCKET_VIEW",userToken.getToken());
		
      QDataAskMessage[] askSet = JsonUtils.fromJson(tokenObj.getString("value"), QDataAskMessage[].class);
      
      System.out.println("Sending Asks");
      for(QDataAskMessage ask : askSet) {
        
        ask.setToken(userToken.getToken());
        ask.setReplace(false);	
        String askJson = JsonUtils.toJson(ask);
        VertxUtils.writeMsg("webcmds",askJson );
      }
    
    }
    
      @Test
    public void testProcessView() {

      QRules rules = GennyJbpmBaseTest.setupLocalService();
      GennyToken userToken = new GennyToken("userToken", rules.getToken());
      GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
      BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
      TableUtilsTest tableUtils = new TableUtilsTest(beUtils);

      /* initialize  virtualAskMap */
      Map<String, QDataAskMessage> virtualAskMap = new HashMap<String, QDataAskMessage>();
      
      /*  initialize ask set */
      Set<QDataAskMessage> askSet = new HashSet<QDataAskMessage>();
      
      /* initialize  contextListMap */
      Map<String, ContextList> contextListMap = new HashMap<String, ContextList>();

      /* get the theme */
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
      
      List<Context> bucketHeaderContext = new ArrayList<>();
      bucketHeaderContext.add(new Context(ContextType.THEME, tableUtils.getThemeBe(THM_DISPLAY_VERTICAL), VisualControlType.GROUP_CONTENT_WRAPPER, 1.0));
      bucketHeaderContext.add(new Context(ContextType.THEME, tableUtils.getThemeBe(THM_BH_GROUP_WRAPPER), VisualControlType.GROUP_WRAPPER, 1.0));
      
      try {
        
        /* get all the bucket frames */
        Frame3 FRM_BUCKET_HEADER = getBucketHeaderFame("FRM_BUCKET_HEADER", "test", "test" );
        Frame3 FRM_BUCKET_CONTENT = getBucketContentFrame("FRM_BUCKET_CONTENT", "test", "test" );
        Frame3 FRM_BUCKET_FOOTER = getBucketFooterFrame("FRM_BUCKET_FOOTER", "test", "test" );
        Frame3 FRM_BUCKET_WRAPPER = getBucketWrapperFrame("FRM_BUCKET_WRAPPER", "test", "test" );
        
        Frame3 FRM_BUCKET = getBucketFrame("FRM_BUCKET", "test", "test" );

        /* get all the bucket related asks group */
        Ask FRM_BUCKET_HEADER_ASK = this.getBucketHeaderAsk(contextListMap, serviceToken);
        Ask FRM_BUCKET_FOOTER_ASK = this.getBucketFooterAsk(serviceToken);
        Ask FRM_BUCKET_CONTENT_ASK = this.getBucketContentAsk(serviceToken);

       /* get the searchBeList */
       List<SearchEntity> searchBeList = tableUtils.getBucketSearchBeListFromCache();
       System.out.println("size   ::    " + searchBeList.size());
       
       /* loop through the searchList */
       for (SearchEntity searchBe : searchBeList) {
    	   
          String code = searchBe.getCode().split("SBE_")[1];

          contextListMap.put("QUE_BUCKET_HEADER_" + code + "_GRP", new ContextList(bucketHeaderContext));
          
          /* clone the bucket */
          Frame3 bucketHeader = Frame3.clone(FRM_BUCKET_HEADER);
          bucketHeader.setCode("FRM_BUCKET_HEADER_" + code);
          bucketHeader.setQuestionCode("QUE_BUCKET_HEADER_" + code + "_GRP");
         
          Frame3 bucketContent = Frame3.clone(FRM_BUCKET_CONTENT);
          bucketContent.setCode("FRM_BUCKET_CONTENT_" + code);
          bucketContent.setQuestionCode("QUE_BUCKET_CONTENT_" + code);

          Frame3 bucketFooter = Frame3.clone(FRM_BUCKET_FOOTER);
          bucketFooter.setCode("FRM_BUCKET_FOOTER_" + code);
          bucketFooter.setQuestionCode("QUE_BUCKET_FOOTER_" + code);
          
          Frame3 bucket = Frame3.clone(FRM_BUCKET);
          bucket.setCode("FRM_BUCKET_"+code);
          bucket.getFrames().add(new FrameTuple3(bucketHeader, FramePosition.NORTH, 1.0));

          /* add the cloned bucket to wrapper */
          FRM_BUCKET_WRAPPER.getFrames().add(new FrameTuple3(bucket, FramePosition.WEST, 1.0));
          
          /* clone asks*/
          
          /* bucketHeader asks */
          Ask bucketHeaderAsk = Ask.clone(FRM_BUCKET_HEADER_ASK);
          bucketHeaderAsk.setQuestionCode("QUE_BUCKET_HEADER_" + code + "_GRP");
          bucketHeaderAsk.setName(searchBe.getName());

          /* bucketContent asks */
          Ask bucketContentAsk = Ask.clone(FRM_BUCKET_CONTENT_ASK);
          bucketContentAsk.setQuestionCode("QUE_BUCKET_CONTENT_" + code);
          bucketContentAsk.setName(searchBe.getName());
         
          /* bucketFooter asks */
          Ask bucketFooterAsk = Ask.clone(FRM_BUCKET_FOOTER_ASK);
          bucketFooterAsk.setQuestionCode("QUE_BUCKET_FOOTER_" + code);
          bucketFooterAsk.setName(searchBe.getName());

          virtualAskMap.put(bucketHeaderAsk.getQuestionCode(), new QDataAskMessage(bucketHeaderAsk));
       }
        
        /* build the bucket view frame */
        Frame3 FRM_BUCKET_VIEW = Frame3.builder("FRM_BUCKET_VIEW")
                    .addFrame(FRM_BUCKET_WRAPPER, FramePosition.CENTRE).end().build();

        /* build the tab content frame */
        Frame3 FRM_TAB_CONTENT = Frame3.builder("FRM_ROOT")
                      .addFrame(FRM_BUCKET_VIEW, FramePosition.NORTH)
                    .end().build();

        QDataBaseEntityMessage msg = FrameUtils2.toMessage(FRM_TAB_CONTENT, serviceToken, askSet, contextListMap,virtualAskMap);
        msg.setToken(userToken.getToken());
        VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg));

        for (QDataAskMessage askMsg : askSet) {
					
          askMsg.setToken(userToken.getToken());
          
          String json = JsonUtils.toJson(askMsg);
          VertxUtils.writeMsg("webcmds", json);
  
        }

        System.out.print("Completed");
      }catch(Exception e){
          System.out.print("Error");
      }
    }

    public Frame3 getBucketHeaderFame(String name, String target, String questionCode) {

      /* build the theme */
      Theme THM_JUSTIFY_CONTENT_FLEX_START = Theme.builder("THM_JUSTIFY_CONTENT_FLEX_START")
                    .addAttribute().justifyContent("flex-start").end()
                    .build();
      
      /* build the frame */
      Frame3 bucketHeader = Frame3.builder(name)
                        .question("QUE_BUCKET_HEADER_GRP").end()
                        .addTheme(THM_JUSTIFY_CONTENT_FLEX_START, ThemePosition.CENTRE).end().build();

      /* return the frame */
      return bucketHeader;
    }
    public Frame3 getBucketContentFrame(String name, String target, String questionCode) {
      
      /* build the theme */
      Theme THM_JUSTIFY_CONTENT_FLEX_START = Theme.builder("THM_JUSTIFY_CONTENT_FLEX_START")
                    .addAttribute().justifyContent("flex-start").end()
                    .build();

      /* build the frame */
      Frame3 bucketContent = Frame3.builder(name)
                          .question("QUE_BUCKET_CONTENT_GRP").end()                    
                          .addTheme(THM_JUSTIFY_CONTENT_FLEX_START, ThemePosition.CENTRE).end()
                          .build();
      
      return bucketContent;                    
    }
    public Frame3 getBucketFooterFrame(String name, String target, String questionCode) {

      Frame3 bucketFooter = Frame3.builder(name)
                  .question("QUE_BUCKET_FOOTER_GRP").end()
    		  				.build();
      
      return bucketFooter;                    
    }
    public Frame3 getBucketFrame(String name, String target, String questionCode) {
      
      /* build the theme */
      Theme THM_JUSTIFY_CONTENT_FLEX_START = Theme.builder("THM_JUSTIFY_CONTENT_FLEX_START")
                    .addAttribute().justifyContent("flex-start").end()
                    .build();

      Theme THM_BACKGROUND_NONE = Theme.builder("THM_BACKGROUND_NONE")
                    .addAttribute()
                      .backgroundColor("none")
                    .end()
                    .build();
      
      Theme THM_BACKGROUND_E4E4E4 = Theme.builder("THM_BACKGROUND_E4E4E4")
                    .addAttribute()
                      .backgroundColor("#E4E4E4")
                      .end()
                    .build();
      
      Theme THM_BUCKET_COLUMN = Theme.builder("THM_BUCKET_COLUMN")
                    .addAttribute()
                      .minWidth(300)
                      .width("100%")
                      .marginLeft(10)
                      .marginRight(10)
                      .textAlign("center")
                      .flexDirection("column")
                    .end()
                    .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                    .build();   
      
      Theme THM_BUCKET_COLUMN_PADDING = Theme.builder("THM_BUCKET_COLUMN_PADDING")
                    .addAttribute()
                      .padding(20)
                      .justifyContent("flex-start")
                    .end()
                    .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                    .build();                         

      /* build the frame */
      Frame3 bucket = Frame3.builder(name)
                      .addTheme(THM_BACKGROUND_NONE).end()
                      .addTheme(THM_BACKGROUND_E4E4E4, ThemePosition.WRAPPER).end()
                      .addTheme(THM_BUCKET_COLUMN, ThemePosition.WRAPPER).end()
                      .addTheme(THM_BUCKET_COLUMN_PADDING, ThemePosition.CENTRE).end()
                      .build();
      
      return bucket;                    
    }
    public Frame3 getBucketWrapperFrame(String name, String target, String questionCode) {
      
      /* build the theme */
      Theme THM_BUCKET = Theme.builder("THM_BUCKET")
                        .addAttribute()
                          .backgroundColor("#F8F9FA")
                          .overflowX("auto")
                          .overflowY("auto")
                          .width("100%")
                          .end()
                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                        .build();

      Theme THM_BUCKET_WRAPPER = Theme.builder("THM_BUCKET_WRAPPER")
                    .addAttribute()
                      .paddingY(20)
                    .end()
                    .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                    .build();

      /* build the frame */
      Frame3 FRM_BUCKET_WRAPPER = Frame3.builder(name)
                            .addTheme(THM_BUCKET).end()
                            .addTheme(THM_BUCKET_WRAPPER, ThemePosition.WEST).end()
                            .build();
      
      return FRM_BUCKET_WRAPPER;                    
    }

    /* Generate Asks */
    public Ask getBucketHeaderAsk(Map<String, ContextList> contextListMap, GennyToken serviceToken) {

      QRules rules = GennyJbpmBaseTest.setupLocalService();
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

    public Ask getBucketFooterAsk(GennyToken serviceToken) {

      QRules rules = GennyJbpmBaseTest.setupLocalService();
      BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
      TableUtilsTest tableUtils = new TableUtilsTest(beUtils);
  
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
  
      Attribute questionAttribute = RulesUtils.attributeMap.get("QQQ_QUESTION_GROUP");
      Attribute nextAttribute = RulesUtils.attributeMap.get("PRI_NEXT_BTN");
      Attribute prevAttribute = RulesUtils.attributeMap.get("PRI_PREVIOUS_BTN");
  
      /* Initialize Bucket Footer Ask group */
      Question bucketFooterQuestion = new Question("QUE_BUCKET_FOOTER_GRP", "Footer Group",
          questionAttribute, true);
  
      Ask bucketFooterAsk = new Ask(bucketFooterQuestion, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");
      bucketFooterAsk = tableUtils.createVirtualContext(bucketFooterAsk, THM_DISPLAY_HORIZONTAL, ContextType.THEME,
          VisualControlType.GROUP_CONTENT_WRAPPER);
      bucketFooterAsk = tableUtils.createVirtualContext(bucketFooterAsk, getThemeBe(THM_WIDTH_100_PERCENT), ContextType.THEME,
          VisualControlType.GROUP_WRAPPER);
      bucketFooterAsk = tableUtils.createVirtualContext(bucketFooterAsk, getThemeBe(THM_JUSTIFY_CONTENT_SPACE_AROUND),
          ContextType.THEME, VisualControlType.GROUP_CONTENT_WRAPPER);
  
      /* next ask */
      Question nextBucketQues = new Question("QUE_NEXT_BUCKET", "", nextAttribute, false);
      Ask nextBucketAsk = new Ask(nextBucketQues, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");
      nextBucketAsk = tableUtils.createVirtualContext(nextBucketAsk, ICN_ARROW_FORWARD_IOS, ContextType.ICON,
          VisualControlType.VCL_ICON);
      nextBucketAsk = tableUtils.createVirtualContext(nextBucketAsk, getThemeBe(THM_ICON), ContextType.THEME,
          VisualControlType.VCL);
  
      /* prev ask */
      Question prevBucketQues = new Question("QUE_PREV_BUCKET", "", prevAttribute, false);
      Ask prevBucketAsk = new Ask(prevBucketQues, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");
      prevBucketAsk = tableUtils.createVirtualContext(prevBucketAsk, ICN_ARROW_BACK_IOS, ContextType.ICON,
          VisualControlType.VCL_ICON);
      prevBucketAsk = tableUtils.createVirtualContext(prevBucketAsk, getThemeBe(THM_ICON), ContextType.THEME,
          VisualControlType.VCL);
  
      /* set the child asks */
      Ask[] bucketChildAsksArray = { prevBucketAsk, nextBucketAsk };
      bucketFooterAsk.setChildAsks(bucketChildAsksArray);
  
      return bucketFooterAsk;
    }

    public Ask getBucketContentAsk(GennyToken serviceToken) {
    	
	 QRules rules = GennyJbpmBaseTest.setupLocalService();
	 BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
	 TableUtilsTest tableUtils = new TableUtilsTest(beUtils);
 

      Attribute questionAttribute = RulesUtils.attributeMap.get("QQQ_QUESTION_GROUP");
      Question bucketContentQuestion = new Question("QUE_BUCKET_CONTENT_GRP", "",questionAttribute, true);
      Ask bucketContentAsk = new Ask(bucketContentQuestion, beUtils.getGennyToken().getUserCode(), "SBE_DUMMY");
  
      return bucketContentAsk;
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
  
  
}