package life.genny.test;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
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
import life.genny.models.FramePosition;
import life.genny.models.GennyToken;
import life.genny.models.TableData;
import life.genny.models.Theme;
import life.genny.models.ThemeAttribute;
import life.genny.models.ThemeAttributeType;
import life.genny.models.ThemePosition;
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

public class AnishTest extends GennyJbpmBaseTest {

        private static final Logger log = LoggerFactory
                        .getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

        public AnishTest() {
                super(false);
        }
        
        public void cacheProcessView() {

        }
        
        // @Test
        public void testEmptyProcessView() {

                QRules rules = GennyJbpmBaseTest.setupLocalService();
                GennyToken userToken = new GennyToken("userToken", rules.getToken());
                GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
                BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
                TableUtils tableUtils = new TableUtils(beUtils);

               // try {
                        /* list to collect baseentity */
                        List<BaseEntity> beList = new ArrayList<BaseEntity>();
                        
                        /* list to collect the asks  */
                        Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();

                        // get the list of bucket searchBEs from the cache
                        List<SearchEntity> searchBeList = getBucketSearchBeListFromCache();
                        System.out.println("size" + searchBeList.size());
                        
                        /* add the searchList to beList as well s*/
                        beList.addAll(searchBeList);

                        /* list to collect frames */
                        List<Frame3> bucketFrames = new ArrayList<Frame3>();
                        

                        /* get the templat ask for card */
                        Ask templateAsk = tableUtils.getCardTemplate(serviceToken, rules);

                        /* get the themes from cache */
                        Theme THM_BACKGROUND_NONE = VertxUtils.getObject(serviceToken.getRealm(), "",
                                        "THM_BACKGROUND_NONE", Theme.class, serviceToken.getToken());
                        Theme THM_BACKGROUND_E4E4E4 = VertxUtils.getObject(serviceToken.getRealm(), "",
                                        "THM_BACKGROUND_E4E4E4", Theme.class, serviceToken.getToken());
                        Theme THM_BUCKET_COLUMN = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BUCKET_COLUMN",
                                        Theme.class, serviceToken.getToken());
                        Theme THM_BUCKET_COLUMN_PADDING = VertxUtils.getObject(serviceToken.getRealm(), "",
                                        "THM_BUCKET_COLUMN_PADDING", Theme.class, serviceToken.getToken());
                        Theme THM_BUCKET = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BUCKET", Theme.class,
                                        serviceToken.getToken());
                        Theme THM_BUCKET_WRAPPER = VertxUtils.getObject(serviceToken.getRealm(), "",
                                        "THM_BUCKET_WRAPPER", Theme.class, serviceToken.getToken());
                        Theme THM_JUSTIFY_CONTENT_FLEX_START = VertxUtils.getObject(serviceToken.getRealm(), "",
                                        "THM_JUSTIFY_CONTENT_FLEX_START", Theme.class, serviceToken.getToken());
                        Theme THM_DISPLAY_HORIZONTAL = VertxUtils.getObject(serviceToken.getRealm(), "",
                                        "THM_DISPLAY_HORIZONTAL", Theme.class, serviceToken.getToken());

                        /* loop through the  searchList */
                        for (SearchEntity searchBe : searchBeList) {

                                String code = searchBe.getCode().split("SBE_")[1];
                                
                                /* get the attributes from searchObj */
                                Map<String, String> columns = tableUtils.getTableColumns(searchBe);

                                // generate bucket header asks from searchBe
                                Ask bucketHeaderAsk = tableUtils.getBucketHeaderAsk(searchBe, serviceToken);

                                // generate bucket content ask group from searchBe
                                Ask bucketContentAsk = tableUtils.generateBucketContentAsk(searchBe, serviceToken);
                                
                                // generate bucket footer ask group from searchBe
                                Ask bucketFooterAsk = tableUtils.generateBucketFooterAsk(searchBe, serviceToken);

                                askMsgs.add(new QDataAskMessage(bucketHeaderAsk));
                                askMsgs.add(new QDataAskMessage(bucketFooterAsk));
                                askMsgs.add(new QDataAskMessage(bucketContentAsk));

                                /*
                                 * =============================================================================
                                 * ============= FRAMES
                                 * =============================================================================
                                 * =============
                                 */

                                // generate bucket header
                                Frame3 frameBucketHeader = Frame3.builder("FRM_BUCKET_HEADER_" + code)
                                                .addTheme(THM_JUSTIFY_CONTENT_FLEX_START, ThemePosition.CENTRE).end()
                                                .build();

                                // generate bucket content
                                Frame3 frameBucketContent = Frame3.builder("FRM_BUCKET_CONTENT_" + code).build();
                                
                                // generate bucket footer
                                Frame3 frameBucketFooter = Frame3.builder("FRM_BUCKET_FOOTER_" + code).build();

                                // link bucket frames to asks
                                BaseEntity frameBucketHeaderBe = FrameUtils2.getBaseEntity(frameBucketHeader,
                                                serviceToken);
                                BaseEntity frameBucketContentBe = FrameUtils2.getBaseEntity(frameBucketContent,
                                                serviceToken);
                                BaseEntity frameBucketFooterBe = FrameUtils2.getBaseEntity(frameBucketFooter,
                                                serviceToken);

                                frameBucketHeaderBe = rules.createVirtualLink(frameBucketHeaderBe, bucketHeaderAsk,
                                                "LNK_ASK", "CENTRE");

                                frameBucketContentBe = rules.createVirtualLink(frameBucketContentBe,
                                                (BaseEntity) THM_JUSTIFY_CONTENT_FLEX_START, "LNK_THEME", "CENTRE",
                                                1.0);

                                frameBucketContentBe = rules.createVirtualLink(frameBucketContentBe, bucketContentAsk,
                                                "LNK_ASK", "CENTRE");

                                frameBucketFooterBe = rules.createVirtualLink(frameBucketFooterBe, bucketFooterAsk,
                                                "LNK_ASK", "CENTRE");

                                beList.add(frameBucketHeaderBe);
                                beList.add(frameBucketContentBe);
                                beList.add(frameBucketFooterBe);

                                Frame3 frameBucket = Frame3.builder("FRM_BUCKET_" + code).addTheme(THM_BACKGROUND_NONE)
                                                .end().addTheme(THM_BACKGROUND_E4E4E4, ThemePosition.WRAPPER).end()
                                                .addTheme(THM_BUCKET_COLUMN, ThemePosition.WRAPPER).end()
                                                .addTheme(THM_BUCKET_COLUMN_PADDING, ThemePosition.CENTRE).end()
                                                .addFrame(frameBucketHeader, FramePosition.NORTH).end()
                                                .addFrame(frameBucketContent, FramePosition.CENTRE).end()
                                                .addFrame(frameBucketFooter, FramePosition.SOUTH).end().build();

                                bucketFrames.add(frameBucket);

                        }


                        Frame3 FRM_BUCKET_TITLE = Frame3.builder("FRM_BUCKET_TITLE").question("QUE_BUCKET_TITLE").end()
                                        .build();

                        Frame3 FRM_BUCKET_WRAPPER = Frame3.builder("FRM_BUCKET_WRAPPER")
                                        .addFrame(bucketFrames.get(0), FramePosition.WEST).end()
                                        .addFrame(bucketFrames.get(1), FramePosition.WEST).end()
                                        .addFrame(bucketFrames.get(2), FramePosition.WEST).end()
                                        .addFrame(bucketFrames.get(3), FramePosition.WEST).end()
                                        .addFrame(bucketFrames.get(4), FramePosition.WEST).end()
                                        .addFrame(bucketFrames.get(5), FramePosition.WEST).end()
                                        .addTheme(THM_BUCKET)
                                        .end().addTheme(THM_BUCKET_WRAPPER, ThemePosition.WEST).end().build();

                        Frame3 FRM_BUCKET_VIEW = Frame3.builder("FRM_BUCKET_VIEW")
                                        .addFrame(FRM_BUCKET_TITLE, FramePosition.NORTH).end()
                                        .addFrame(FRM_BUCKET_WRAPPER, FramePosition.CENTRE).end().build();

                        Frame3 FRM_TAB_CONTENT = Frame3.builder("FRM_TAB_CONTENT")
                                        .addFrame(FRM_BUCKET_VIEW, FramePosition.NORTH).end().build();

                        QDataBaseEntityMessage msg = FrameUtils2.toMessage(FRM_TAB_CONTENT, serviceToken, askMsgs);
                        rules.publishCmd(msg);
                      
                        /* publish the asks */
                        for (QDataAskMessage askMsg : askMsgs) {
                                askMsg.setToken(beUtils.getGennyToken().getToken());
                                askMsg.setReplace(false);
                                rules.publishCmd(askMsg);
                                //VertxUtils.writeMsg("webcmds", JsonUtils.toJson(footerAskMsg));
                        }
                        System.out.println("Sent");

                        // publish all the baseentity as well
                        QDataBaseEntityMessage appMsg = new QDataBaseEntityMessage(
                                        beList.toArray(new BaseEntity[beList.size()]));
                        rules.publishCmd(appMsg);

//                } catch (Exception e) {
//                        System.out.println("Error " + e.getLocalizedMessage());
//                }

        }
        
       // @Test
        public void updateCards(){
        	QRules rules = GennyJbpmBaseTest.setupLocalService();
        	GennyToken userToken = new GennyToken("userToken", rules.getToken());
        	GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
        	BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
        	TableUtilsTest tableUtils = new TableUtilsTest(beUtils);

        	/* list to collect baseentity */
        	List<BaseEntity> beList = new ArrayList<BaseEntity>();

        	/* list to collect the asks  */
        	Ask bucketContentAsk = null;

        	/* get the list of bucket searchBEs from the cache */
        	List<SearchEntity> searchBeList = getBucketSearchBeListFromCache();

        	/* get the templat ask for card */
        	Ask templateAsk = tableUtils.getCardTemplate(serviceToken, rules);

        	/* list to collect the asks  */
        	Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();

        	try {

        		/* loop through the  searchList */
        		for (SearchEntity searchBe : searchBeList) {

        			/* get the attributes from searchObj */
        			Map<String, String> columns = tableUtils.getTableColumns(searchBe);

        			/* fetch the search results */
        			QDataBaseEntityMessage msg = tableUtils.fetchSearchResults(searchBe, beUtils.getGennyToken());

        			/* get the applications */
        			List<BaseEntity> appList = Arrays.asList(msg.getItems());

        			/* add the application to the baseentity list */
        			beList.addAll(appList);

        			/* convert app to asks */
        			List<Ask> appAsksList = tableUtils.generateQuestions(beUtils.getGennyToken(), beUtils,
        					appList, columns, beUtils.getGennyToken().getUserCode());

        			/* implement template ask to appAks list */
        			List<Ask> askList = implementCardTemplate(appAsksList, templateAsk);

        			/* generate bucket content ask group from searchBe */
        			bucketContentAsk = tableUtils.generateBucketContentAsk(searchBe, serviceToken);

        			/* link bucketContentAsk to application asks */
        			bucketContentAsk.setChildAsks(askList.toArray(new Ask[askList.size()]));

        			/* add the bucketContentAsk msg to the list */
        			askMsgs.add(new QDataAskMessage(bucketContentAsk));                        

        		}

        	}catch(Exception e){
        		System.out.println("something went wrong");
        	}


        	/* publish all the app BE */
        	QDataBaseEntityMessage appMsg = new QDataBaseEntityMessage(beList.toArray(new BaseEntity[beList.size()]));
        	rules.publishCmd(appMsg);

        	/* publish the asks */
        	for (QDataAskMessage askMsg : askMsgs) {
        		askMsg.setToken(beUtils.getGennyToken().getToken());
        		askMsg.setReplace(true);
        		rules.publishCmd(askMsg);
        		//VertxUtils.writeMsg("webcmds", JsonUtils.toJson(footerAskMsg));
        	}
        }

        
        @Test
        public void testProcessView() {

                QRules rules = GennyJbpmBaseTest.setupLocalService();
                GennyToken userToken = new GennyToken("userToken", rules.getToken());
                GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
                BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
                TableUtilsTest tableUtils = new TableUtilsTest(beUtils);

                try {
                        // get the list of bucket searchBEs from the cache
                        List<SearchEntity> searchBeList = getBucketSearchBeListFromCache();
                        System.out.println("size" + searchBeList.size());

                        /* list to collect frames */
                        List<Frame3> bucketFrames = new ArrayList<Frame3>();
                        
                        /* list to collect baseentity */
                        List<BaseEntity> beList = new ArrayList<BaseEntity>();

                        /* list to collect the asks  */
                        Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();

                        /* get the templat ask for card */
                        Ask templateAsk = tableUtils.getCardTemplate(serviceToken, rules);

                        /* get the themes from cache */
                        Theme THM_BACKGROUND_NONE = VertxUtils.getObject(serviceToken.getRealm(), "",
                                        "THM_BACKGROUND_NONE", Theme.class, serviceToken.getToken());
                        Theme THM_BACKGROUND_E4E4E4 = VertxUtils.getObject(serviceToken.getRealm(), "",
                                        "THM_BACKGROUND_E4E4E4", Theme.class, serviceToken.getToken());
                        Theme THM_BUCKET_COLUMN = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BUCKET_COLUMN",
                                        Theme.class, serviceToken.getToken());
                        Theme THM_BUCKET_COLUMN_PADDING = VertxUtils.getObject(serviceToken.getRealm(), "",
                                        "THM_BUCKET_COLUMN_PADDING", Theme.class, serviceToken.getToken());
                        Theme THM_BUCKET = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BUCKET", Theme.class,
                                        serviceToken.getToken());
                        Theme THM_BUCKET_WRAPPER = VertxUtils.getObject(serviceToken.getRealm(), "",
                                        "THM_BUCKET_WRAPPER", Theme.class, serviceToken.getToken());
                        Theme THM_JUSTIFY_CONTENT_FLEX_START = VertxUtils.getObject(serviceToken.getRealm(), "",
                                        "THM_JUSTIFY_CONTENT_FLEX_START", Theme.class, serviceToken.getToken());
                        Theme THM_DISPLAY_HORIZONTAL = VertxUtils.getObject(serviceToken.getRealm(), "",
                                        "THM_DISPLAY_HORIZONTAL", Theme.class, serviceToken.getToken());

                        /* loop through the  searchList */
                        for (SearchEntity searchBe : searchBeList) {

                                String code = searchBe.getCode().split("SBE_")[1];
                                
                                /* get the attributes from searchObj */
                                Map<String, String> columns = tableUtils.getTableColumns(searchBe);

                                // generate bucket header asks from searchBe
                                Ask bucketHeaderAsk = tableUtils.getBucketHeaderAsk(searchBe, serviceToken);

                                // generate bucket content ask group from searchBe
                                Ask bucketContentAsk = tableUtils.generateBucketContentAsk(searchBe, serviceToken);
                                
                                // generate bucket footer ask group from searchBe
                                Ask bucketFooterAsk = tableUtils.generateBucketFooterAsk(searchBe, serviceToken);

                                // fetch the search results
                                QDataBaseEntityMessage msg = tableUtils.fetchSearchResults(searchBe, beUtils.getGennyToken());
                                
                                /* get the applications */
                                List<BaseEntity> appList = Arrays.asList(msg.getItems());
                                
                                /* add the application to the baseentity list */
                                beList.addAll(appList);
                                
                                /* convert app to asks */
                                List<Ask> appAsksList = tableUtils.generateQuestions(beUtils.getGennyToken(), beUtils,
                                                appList, columns, beUtils.getGennyToken().getUserCode());

                                /* implement template ask to appAks list */
                                List<Ask> askList = implementCardTemplate(appAsksList, templateAsk);

                                // link bucketContentAsk to application asks
                                bucketContentAsk.setChildAsks(askList.toArray(new Ask[askList.size()]));

                                askMsgs.add(new QDataAskMessage(bucketHeaderAsk));
                                askMsgs.add(new QDataAskMessage(bucketFooterAsk));
                                askMsgs.add(new QDataAskMessage(bucketContentAsk));

                                /*
                                 * =============================================================================
                                 * ============= FRAMES
                                 * =============================================================================
                                 * =============
                                 */

                                // generate bucket header
                                Frame3 frameBucketHeader = Frame3.builder("FRM_BUCKET_HEADER_" + code)
                                                .addTheme(THM_JUSTIFY_CONTENT_FLEX_START, ThemePosition.CENTRE).end()
                                                .build();

                                // generate bucket content
                                Frame3 frameBucketContent = Frame3.builder("FRM_BUCKET_CONTENT_" + code).build();
                                
                                // generate bucket footer
                                Frame3 frameBucketFooter = Frame3.builder("FRM_BUCKET_FOOTER_" + code).build();

                                // link bucket frames to asks
                                BaseEntity frameBucketHeaderBe = FrameUtils2.getBaseEntity(frameBucketHeader,
                                                serviceToken);
                                BaseEntity frameBucketContentBe = FrameUtils2.getBaseEntity(frameBucketContent,
                                                serviceToken);
                                BaseEntity frameBucketFooterBe = FrameUtils2.getBaseEntity(frameBucketFooter,
                                                serviceToken);

                                frameBucketHeaderBe = rules.createVirtualLink(frameBucketHeaderBe, bucketHeaderAsk,
                                                "LNK_ASK", "CENTRE");

                                frameBucketContentBe = rules.createVirtualLink(frameBucketContentBe,
                                                (BaseEntity) THM_JUSTIFY_CONTENT_FLEX_START, "LNK_THEME", "CENTRE",
                                                1.0);

                                frameBucketContentBe = rules.createVirtualLink(frameBucketContentBe, bucketContentAsk,
                                                "LNK_ASK", "CENTRE");

                                frameBucketFooterBe = rules.createVirtualLink(frameBucketFooterBe, bucketFooterAsk,
                                                "LNK_ASK", "CENTRE");

                                beList.add(frameBucketHeaderBe);
                                beList.add(frameBucketContentBe);
                                beList.add(frameBucketFooterBe);

                                Frame3 frameBucket = Frame3.builder("FRM_BUCKET_" + code).addTheme(THM_BACKGROUND_NONE)
                                                .end().addTheme(THM_BACKGROUND_E4E4E4, ThemePosition.WRAPPER).end()
                                                .addTheme(THM_BUCKET_COLUMN, ThemePosition.WRAPPER).end()
                                                .addTheme(THM_BUCKET_COLUMN_PADDING, ThemePosition.CENTRE).end()
                                                .addFrame(frameBucketHeader, FramePosition.NORTH).end()
                                                .addFrame(frameBucketContent, FramePosition.CENTRE).end()
                                                .addFrame(frameBucketFooter, FramePosition.SOUTH).end().build();

                                bucketFrames.add(frameBucket);

                        }


                        Frame3 FRM_BUCKET_TITLE = Frame3.builder("FRM_BUCKET_TITLE").question("QUE_BUCKET_TITLE").end()
                                        .build();

                        Frame3 FRM_BUCKET_WRAPPER = Frame3.builder("FRM_BUCKET_WRAPPER")
                                        .addFrame(bucketFrames.get(0), FramePosition.WEST).end()
                                        .addFrame(bucketFrames.get(1), FramePosition.WEST).end()
                                        .addFrame(bucketFrames.get(2), FramePosition.WEST).end()
                                        .addFrame(bucketFrames.get(3), FramePosition.WEST).end()
                                        .addFrame(bucketFrames.get(4), FramePosition.WEST).end()
                                        .addFrame(bucketFrames.get(5), FramePosition.WEST).end()
                                        .addTheme(THM_BUCKET)
                                        .end().addTheme(THM_BUCKET_WRAPPER, ThemePosition.WEST).end().build();

                        Frame3 FRM_BUCKET_VIEW = Frame3.builder("FRM_BUCKET_VIEW")
                                        .addFrame(FRM_BUCKET_TITLE, FramePosition.NORTH).end()
                                        .addFrame(FRM_BUCKET_WRAPPER, FramePosition.CENTRE).end().build();

                        Frame3 FRM_TAB_CONTENT = Frame3.builder("FRM_TAB_CONTENT")
                                        .addFrame(FRM_BUCKET_VIEW, FramePosition.NORTH).end().build();

                        QDataBaseEntityMessage msg = FrameUtils2.toMessage(FRM_TAB_CONTENT, serviceToken, askMsgs);
                        rules.publishCmd(msg);
                      
                        /* publish the asks*/
                        for (QDataAskMessage askMsg : askMsgs) {
                                askMsg.setToken(beUtils.getGennyToken().getToken());
                                askMsg.setReplace(false);
                                rules.publishCmd(askMsg);
                                //VertxUtils.writeMsg("webcmds", JsonUtils.toJson(footerAskMsg));
                        }
                        System.out.println("Sent");

                        // publish all the baseentity as well
                        QDataBaseEntityMessage appMsg = new QDataBaseEntityMessage(
                                        beList.toArray(new BaseEntity[beList.size()]));
                        rules.publishCmd(appMsg);

                } catch (Exception e) {
                        System.out.println("Error " + e.getLocalizedMessage());
                }

        }

        public Ask getCardTemplate(GennyToken serviceToken, QRules rules) {

                BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
                TableUtilsTest tableUtils = new TableUtilsTest(beUtils);
                List<BaseEntity> beList = new ArrayList<BaseEntity>();

                try {
                        // get the themes from cache
                BaseEntity THM_DISPLAY_VERTICAL = VertxUtils.getObject(serviceToken.getRealm(), "",
                "THM_DISPLAY_VERTICAL", BaseEntity.class, serviceToken.getToken());

                BaseEntity THM_JUSTIFY_CONTENT_FLEX_START = VertxUtils.getObject(serviceToken.getRealm(), "",
                                "THM_JUSTIFY_CONTENT_FLEX_START", BaseEntity.class, serviceToken.getToken());
               
                Theme THM_CARD = VertxUtils.getObject(serviceToken.getRealm(), "",
                                "THM_CARD", Theme.class, serviceToken.getToken());


                BaseEntity THM_DISPLAY_HORIZONTAL = Theme.builder("THM_DISPLAY_HORIZONTAL").addAttribute()
                                .flexDirection("row").end().addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
                                .end().build();

                BaseEntity THM_DROPDOWN_ICON_ALT = VertxUtils.getObject(serviceToken.getRealm(), "",
                                "THM_DROPDOWN_ICON_ALT", BaseEntity.class, serviceToken.getToken());

                BaseEntity THM_DROPDOWN_BEHAVIOUR_GENNY = VertxUtils.getObject(serviceToken.getRealm(), "",
                                "THM_DROPDOWN_BEHAVIOUR_GENNY", BaseEntity.class, serviceToken.getToken());

                BaseEntity THM_BACKGROUND_NONE = VertxUtils.getObject(serviceToken.getRealm(), "",
                                "THM_BACKGROUND_NONE", BaseEntity.class, serviceToken.getToken());

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

                BaseEntity THM_DROPDOWN_VCL_GENNY = VertxUtils.getObject(serviceToken.getRealm(), "",
                                "THM_DROPDOWN_VCL_GENNY", BaseEntity.class, serviceToken.getToken());

                BaseEntity THM_IMAGE_PLACEHOLDER = VertxUtils.getObject(serviceToken.getRealm(), "",
                                "THM_IMAGE_PLACEHOLDER", BaseEntity.class, serviceToken.getToken());
                BaseEntity THM_HEADER_PROFILE_PICTURE = VertxUtils.getObject(serviceToken.getRealm(), "",
                                "THM_HEADER_PROFILE_PICTURE", BaseEntity.class, serviceToken.getToken());
                BaseEntity THM_BORDER_RADIUS_50 = VertxUtils.getObject(serviceToken.getRealm(), "",
                                "THM_BORDER_RADIUS_50", BaseEntity.class, serviceToken.getToken());
                BaseEntity THM_EXPANDABLE = VertxUtils.getObject(serviceToken.getRealm(), "",
                                "THM_EXPANDABLE", BaseEntity.class, serviceToken.getToken());
                BaseEntity THM_WIDTH_100_PERCENT = VertxUtils.getObject(serviceToken.getRealm(), "",
                                "THM_WIDTH_100_PERCENT", BaseEntity.class, serviceToken.getToken());
                BaseEntity THM_JUSTIFY_CONTENT_CENTRE = VertxUtils.getObject(serviceToken.getRealm(), "",
                                "THM_JUSTIFY_CONTENT_CENTRE", BaseEntity.class, serviceToken.getToken());
                Theme THM_IMAGE_PLACEHOLDER_PERSON = VertxUtils.getObject(serviceToken.getRealm(), "",
                                "THM_IMAGE_PLACEHOLDER_PERSON", Theme.class, serviceToken.getToken());
                BaseEntity THM_PROFILE_IMAGE = VertxUtils.getObject(serviceToken.getRealm(), "",
                                "THM_PROFILE_IMAGE", BaseEntity.class, serviceToken.getToken());
                BaseEntity THM_PROJECT_COLOR_SURFACE = VertxUtils.getObject(serviceToken.getRealm(), "",
                                "THM_PROJECT_COLOR_SURFACE", BaseEntity.class, serviceToken.getToken());
                BaseEntity THM_PADDING_X_10 = VertxUtils.getObject(serviceToken.getRealm(), "",
                                "THM_PADDING_X_10", BaseEntity.class, serviceToken.getToken());

                BaseEntity THM_FLEX_ONE = new BaseEntity("THM_FLEX_ONE", "Theme Flex One");
                Attribute contentAttribute = new Attribute("PRI_CONTENT", "content", new DataType(String.class));
                Attribute inheritableAttribute = new Attribute("PRI_IS_INHERITABLE", "inheritable",
                                new DataType(Boolean.class));
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
                Question cardQuestion = new Question("QUE_CARD_APPLICATION_TEMPLATE_GRP", "Card", questionAttribute,
                                true);
                Ask cardAsk = new Ask(cardQuestion, sourceCode, targetCode);
                cardAsk = rules.createVirtualContext(cardAsk, THM_DISPLAY_HORIZONTAL, ContextType.THEME,
                                VisualControlType.GROUP_CONTENT_WRAPPER);
                cardAsk = rules.createVirtualContext(cardAsk, tableUtils.getThemeBe(THM_CARD), ContextType.THEME,
                                VisualControlType.GROUP_WRAPPER);

                // status ask
                Question cardStatusQuestion = new Question("QUE_CARD_STATUS_GRP", "Card Status", questionAttribute,
                                true);
                Ask cardStatusAsk = new Ask(cardStatusQuestion, sourceCode, targetCode);
                cardStatusAsk = rules.createVirtualContext(cardStatusAsk, THM_FLEX_ONE, ContextType.THEME,
                                VisualControlType.GROUP_CONTENT_WRAPPER);
                cardStatusAsk = rules.createVirtualContext(cardStatusAsk, THM_FLEX_ONE2, ContextType.THEME,
                                VisualControlType.VCL_WRAPPER);
                cardStatusAsk = rules.createVirtualContext(cardStatusAsk,
                THM_DROPDOWN_ICON_ALT, ContextType.THEME,
                VisualControlType.GROUP_ICON);

                // main ask
                Question cardMainQuestion = new Question("QUE_CARD_MAIN_GRP", "Card Main", questionAttribute, true);
                Ask cardMainAsk = new Ask(cardMainQuestion, sourceCode, targetCode);
                cardMainAsk = rules.createVirtualContext(cardMainAsk, THM_DISPLAY_VERTICAL, ContextType.THEME,
                                VisualControlType.GROUP_CONTENT_WRAPPER);
                cardMainAsk = rules.createVirtualContext(cardMainAsk, THM_FLEX_ONE, ContextType.THEME,
                                VisualControlType.GROUP_WRAPPER);

                // content ask
                Question cardContentQuestion = new Question("QUE_CARD_CONTENT_GRP", "Card Content", questionAttribute,
                                true);
                Ask cardContentAsk = new Ask(cardContentQuestion, sourceCode, targetCode);
                cardContentAsk = rules.createVirtualContext(cardContentAsk, THM_DISPLAY_HORIZONTAL, ContextType.THEME,
                                VisualControlType.GROUP_CONTENT_WRAPPER);

                // left ask
                Question cardLeftQuestion = new Question("QUE_CARD_LEFT_GRP", "Card Content", questionAttribute, true);
                Ask cardLeftAsk = new Ask(cardLeftQuestion, sourceCode, targetCode);

                //cardLeftAsk = rules.createVirtualContext(cardLeftAsk, THM_IMAGE_PLACEHOLDER, ContextType.THEME, VisualControlType.INPUT_PLACEHOLDER);
                cardLeftAsk = rules.createVirtualContext(cardLeftAsk, THM_HEADER_PROFILE_PICTURE, ContextType.THEME, VisualControlType.INPUT_SELECTED);
                cardLeftAsk = rules.createVirtualContext(cardLeftAsk, THM_BORDER_RADIUS_50, ContextType.THEME, VisualControlType.INPUT_FIELD);

                cardLeftAsk = rules.createVirtualContext(cardLeftAsk, THM_PROFILE_IMAGE, ContextType.THEME, VisualControlType.INPUT_SELECTED);
                cardLeftAsk = rules.createVirtualContext(cardLeftAsk, tableUtils.getThemeBe(THM_IMAGE_PLACEHOLDER_PERSON), ContextType.THEME, VisualControlType.INPUT_PLACEHOLDER);


                // centre ask
                Question cardCentreQuestion = new Question("QUE_CARD_CENTRE_GRP", "Card Content", questionAttribute,
                                true);
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
                cardRightAsk = rules.createVirtualContext(cardRightAsk, THM_DROPDOWN_PROFILE_BEHAVIOUR_GENNY, ContextType.THEME, VisualControlType.GROUP, 1.0);
                cardRightAsk = rules.createVirtualContext(cardRightAsk, THM_PROJECT_COLOR_SURFACE, ContextType.THEME, VisualControlType.GROUP_CONTENT_WRAPPER, 1.0);


                Question cardForwardQuestion = new Question("QUE_FORWARD", "Forward", questionAttribute, true);
                Ask cardForwardAsk = new Ask(cardForwardQuestion, sourceCode, targetCode);

                Question cardBackwardQuestion = new Question("QUE_BACKWARD", "Backward", questionAttribute,
                true);
                Ask cardBackwardAsk = new Ask(cardBackwardQuestion, sourceCode, targetCode);

                //Ask[] cardRightChildAsks = { cardViewAsk, cardEditAsk, cardDeleteAsk };
                Ask[] cardRightChildAsks = { cardForwardAsk, cardBackwardAsk };
                cardRightAsk.setChildAsks(cardRightChildAsks);

                Ask[] cardContentChildAsks = { cardLeftAsk, cardCentreAsk, cardRightAsk };
                cardContentAsk.setChildAsks(cardContentChildAsks);

                // bottom ask
                Question cardBottomQuestion = new Question("QUE_CARD_BOTTOM_GRP", "Card Bottom", questionAttribute,
                true);
                Ask cardBottomAsk = new Ask(cardBottomQuestion, sourceCode, targetCode);
                cardBottomAsk = rules.createVirtualContext(cardBottomAsk, THM_JUSTIFY_CONTENT_CENTRE, ContextType.THEME, VisualControlType.GROUP_CLICKABLE_WRAPPER);
                cardBottomAsk = rules.createVirtualContext(cardBottomAsk, THM_EXPANDABLE, ContextType.THEME, VisualControlType.GROUP);
                cardBottomAsk = rules.createVirtualContext(cardBottomAsk, THM_WIDTH_100_PERCENT, ContextType.THEME, VisualControlType.GROUP);
                cardBottomAsk = rules.createVirtualContext(cardBottomAsk, THM_PADDING_X_10, ContextType.THEME, VisualControlType.GROUP_CONTENT_WRAPPER);

                Ask[] cardMainChildAsks = { cardContentAsk, cardBottomAsk };
                cardMainAsk.setChildAsks(cardMainChildAsks);

                Ask[] cardChildAsks = { cardStatusAsk, cardMainAsk };
                cardAsk.setChildAsks(cardChildAsks);

                return cardAsk;

                } catch (Exception e) {
                        //TODO: handle exception
                }
                return null;

                
        }

        public List<Ask> implementCardTemplate(List<Ask> askList, Ask templateAsk) {

                Ask[] templateAsks = templateAsk.getChildAsks();
                ContextList contextList = templateAsk.getContextList();

                Ask cardStatusAsk = templateAsks[0];
                Ask cardMainAsk = templateAsks[1];
                Ask cardContentAsk = cardMainAsk.getChildAsks()[0];
                Ask cardLeftAsk = cardContentAsk.getChildAsks()[0];
                Ask cardCentreAsk = cardContentAsk.getChildAsks()[1];
                Ask cardRightAsk = cardContentAsk.getChildAsks()[2];
                Ask cardBottomAsk = cardMainAsk.getChildAsks()[1];

                for (Ask app : askList) {

                        Ask[] attributeArr = app.getChildAsks();

                        List<Ask> attributeList = new ArrayList<Ask>(Arrays.asList(attributeArr));

                        Ask[] cardStatusChildAsks = { attributeList.get(0) };                        cardStatusAsk.setChildAsks(cardStatusChildAsks);

                        Ask[] cardLeftChildAsks = { attributeList.get(1) };
                        cardLeftAsk.setReadonly(true);
                        cardLeftAsk.setChildAsks(cardLeftChildAsks);

                        Ask[] cardCentreChildAsks = { attributeList.get(2), attributeList.get(3), attributeList.get(4),
                                        attributeList.get(5) };
                        cardCentreAsk.setReadonly(true);
                        cardCentreAsk.setChildAsks(cardCentreChildAsks);

                        Ask[] cardBottomChildAsks = { attributeList.get(6), attributeList.get(7),
                                        attributeList.get(8) };
                        cardBottomAsk.setReadonly(true);
                        cardBottomAsk.setChildAsks(cardBottomChildAsks);

                        // we create a new ask with all the new groups
                        app.setChildAsks(templateAsks);

                        // we set the themes from cardAsk group to the the appAsk
                        app.setContextList(contextList);
                        app.setReadonly(true);


                }

                // return the updated askList
                return askList;
        }

        // @Test
        public void testBucket() {
                QRules rules = GennyJbpmBaseTest.setupLocalService();
                GennyToken userToken = new GennyToken("userToken", rules.getToken());
                GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
                BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
                BaseEntity project = beUtils.getBaseEntityByCode("PRJ_" + serviceToken.getRealm().toUpperCase());
                TableUtilsTest tableUtils = new TableUtilsTest(beUtils);

                try {
                        // get list of searches for bucket
                        List<SearchEntity> searchBeList = getBucketSearchBeList();
                        System.out.println("size" + searchBeList.size());

                        List<Frame3> buckets = new ArrayList<Frame3>();
                        List<Ask> bucketHeaderAsks = new ArrayList<Ask>();

                        // generate overall bucket column (header, content, footer)
                        for (SearchEntity searchBe : searchBeList) {
                                String code = searchBe.getCode().split("SBE_")[1];
                                System.out.println("code :: " + code);

                                // generate bucket footer
                                Frame3 footer = Frame3.builder("FRM_BUCKET_FOOTER_" + code).build();

                                // generate bucket content
                                Frame3 content = Frame3.builder("FRM_BUCKET_CONTENT_" + code).build();

                                // generate bucket header
                                Frame3 header = Frame3.builder("FRM_BUCKET_HEADER_" + code).build();

                                // generate bucket header asks
                                Ask bucketHeaderAsk = tableUtils.getBucketHeaderAsk(searchBe, serviceToken);

                                // collect bucket header asks
                                bucketHeaderAsks.add(bucketHeaderAsk);

                                // link header to bucketHeaderAsk
                                header = tableUtils.createVirtualLink(header, bucketHeaderAsk, "LNK_FRAME", "LINK");

                                Frame3 bucket = Frame3.builder("FRM_BUCKET_VIEW").addFrame(header, FramePosition.NORTH)
                                                .end().addFrame(content, FramePosition.CENTRE).end()
                                                .addFrame(footer, FramePosition.SOUTH).end().build();

                                // System.out.println("Test");

                                buckets.add(bucket);

                        }

                        System.out.println("buckets" + buckets.size());

                        Frame3 FRM_BUCKET_TITLE = Frame3.builder("FRM_BUCKET_TITLE").question("QUE_BUCKET_TITLE").end()
                                        .build();

                        Frame3 FRM_BUCKET_WRAPPER = Frame3.builder("FRM_BUCKET_WRAPPER")
                                        .addFrame(buckets, FramePosition.WEST).build();

                        Frame3 FRM_BUCKET_VIEW = Frame3.builder("FRM_BUCKET_VIEW")
                                        .addFrame(FRM_BUCKET_TITLE, FramePosition.NORTH).end()
                                        .addFrame(FRM_BUCKET_WRAPPER, FramePosition.CENTRE).end().build();

                        System.out.println("Test Completed !");

                } catch (Exception e) {
                        System.out.println("Error " + e.getLocalizedMessage());
                }

        }

        public List<SearchEntity> getBucketSearchBeList() {

                List<SearchEntity> bucketSearchBeList = new ArrayList<SearchEntity>();

                SearchEntity SBE_APPLIED_APPLICATIONS = new SearchEntity("SBE_APPLIED_APPLICATIONS", "Available")
                                .addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC)
                                .addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "APP_%")
                                .addFilter("PRI_STATUS", SearchEntity.StringFilter.EQUAL, "APPLIED")
                                .addColumn("PRI_STATUS_COLOR", "Status").addColumn("PRI_INTERN_IMAGE_URL", "Image")
                                .addColumn("PRI_HOST_COMPANY_NAME", "Host Company")
                                .addColumn("PRI_INTERNSHIP_TITLE", "Internship Title")
                                .addColumn("PRI_INTERN_NAME", "Name").addColumn("PRI_EDU_PROVIDER_NAME", "Edu Provider")
                                .addColumn("PRI_INTERN_EMAIL", "Email").addColumn("PRI_INTERN_MOBILE", "Mobile")
                                .addColumn("PRI_INTERN_STUDENT_ID", "Student ID").setPageStart(0).setPageSize(10);

                SearchEntity SBE_SHORTLISTED_APPLICATIONS = new SearchEntity("SBE_SHORTLISTED_APPLICATIONS",
                                "Shortlisted").addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC)
                                                .addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "APP_%")
                                                .addFilter("PRI_STATUS", SearchEntity.StringFilter.EQUAL, "SHORTLISTED")
                                                .addColumn("PRI_STATUS_COLOR", "Status")
                                                .addColumn("PRI_INTERN_IMAGE_URL", "Image")
                                                .addColumn("PRI_INTERN_NAME", "Name")
                                                .addColumn("PRI_INTERNSHIP_TITLE", "Internship Title")
                                                .addColumn("PRI_HOST_COMPANY_NAME", "Host Company")
                                                .addColumn("PRI_EDU_PROVIDER_NAME", "Edu Provider")
                                                .addColumn("PRI_INTERN_EMAIL", "Email")
                                                .addColumn("PRI_INTERN_MOBILE", "Mobile")
                                                .addColumn("PRI_INTERN_STUDENT_ID", "Student ID").setPageStart(0)
                                                .setPageSize(10);

                SearchEntity SBE_INTERVIEWED_APPLICATIONS = new SearchEntity("SBE_INTERVIEWED_APPLICATIONS",
                                "Interviewed").addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC)
                                                .addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "APP_%")
                                                .addFilter("PRI_STATUS", SearchEntity.StringFilter.EQUAL, "INTERVIEWED")
                                                .addColumn("PRI_STATUS_COLOR", "Status")
                                                .addColumn("PRI_INTERN_IMAGE_URL", "Image")
                                                .addColumn("PRI_INTERN_NAME", "Name")
                                                .addColumn("PRI_INTERNSHIP_TITLE", "Internship Title")
                                                .addColumn("PRI_HOST_COMPANY_NAME", "Host Company")
                                                .addColumn("PRI_EDU_PROVIDER_NAME", "Edu Provider")
                                                .addColumn("PRI_INTERN_EMAIL", "Email")
                                                .addColumn("PRI_INTERN_MOBILE", "Mobile")
                                                .addColumn("PRI_INTERN_STUDENT_ID", "Student ID").setPageStart(0)
                                                .setPageSize(10);
                SearchEntity SBE_OFFERED_APPLICATIONS = new SearchEntity("SBE_OFFERED_APPLICATIONS", "Offered")
                                .addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC)
                                .addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "APP_%")
                                .addFilter("PRI_STATUS", SearchEntity.StringFilter.EQUAL, "OFFERED")
                                .addColumn("PRI_STATUS_COLOR", "Status").addColumn("PRI_INTERN_IMAGE_URL", "Image")
                                .addColumn("PRI_INTERN_NAME", "Name")
                                .addColumn("PRI_INTERNSHIP_TITLE", "Internship Title")
                                .addColumn("PRI_HOST_COMPANY_NAME", "Host Company")
                                .addColumn("PRI_EDU_PROVIDER_NAME", "Edu Provider")
                                .addColumn("PRI_INTERN_EMAIL", "Email").addColumn("PRI_INTERN_MOBILE", "Mobile")
                                .addColumn("PRI_INTERN_STUDENT_ID", "Student ID").setPageStart(0).setPageSize(10);
                SearchEntity SBE_PLACED_APPLICATIONS = new SearchEntity("SBE_PLACED_APPLICATIONS", "Placed")
                                .addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC)
                                .addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "APP_%")
                                .addFilter("PRI_STATUS", SearchEntity.StringFilter.EQUAL, "PLACED")
                                .addColumn("PRI_STATUS_COLOR", "Status").addColumn("PRI_INTERN_IMAGE_URL", "Image")
                                .addColumn("PRI_INTERN_NAME", "Name")
                                .addColumn("PRI_INTERNSHIP_TITLE", "Internship Title")
                                .addColumn("PRI_HOST_COMPANY_NAME", "Host Company")
                                .addColumn("PRI_EDU_PROVIDER_NAME", "Edu Provider")
                                .addColumn("PRI_INTERN_EMAIL", "Email").addColumn("PRI_INTERN_MOBILE", "Mobile")
                                .addColumn("PRI_INTERN_STUDENT_ID", "Student ID").setPageStart(0).setPageSize(10);
                SearchEntity SBE_INPROGRESS_APPLICATIONS = new SearchEntity("SBE_INPROGRESS_APPLICATIONS",
                                "In Progress").addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC)
                                                .addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "APP_%")
                                                .addFilter("PRI_STATUS", SearchEntity.StringFilter.EQUAL, "INPROGRESS")
                                                .addColumn("PRI_STATUS_COLOR", "Status")
                                                .addColumn("PRI_INTERN_IMAGE_URL", "Image")
                                                .addColumn("PRI_INTERN_NAME", "Name")
                                                .addColumn("PRI_INTERNSHIP_TITLE", "Internship Title")
                                                .addColumn("PRI_HOST_COMPANY_NAME", "Host Company")
                                                .addColumn("PRI_EDU_PROVIDER_NAME", "Edu Provider")
                                                .addColumn("PRI_INTERN_EMAIL", "Email")
                                                .addColumn("PRI_INTERN_MOBILE", "Mobile")
                                                .addColumn("PRI_INTERN_STUDENT_ID", "Student ID").setPageStart(0)
                                                .setPageSize(10);

                // bucketSearchBeList.add(SBE_AVAILABLE_INTERNS);
                bucketSearchBeList.add(SBE_APPLIED_APPLICATIONS);
                bucketSearchBeList.add(SBE_SHORTLISTED_APPLICATIONS);
                bucketSearchBeList.add(SBE_INTERVIEWED_APPLICATIONS);
                bucketSearchBeList.add(SBE_OFFERED_APPLICATIONS);
                bucketSearchBeList.add(SBE_PLACED_APPLICATIONS);
                bucketSearchBeList.add(SBE_INPROGRESS_APPLICATIONS);

                return bucketSearchBeList;

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
		            
		

                }catch(Exception e) {
                	
                }
                return bucketSearchBeList;
        }

        public List<Frame3> getBucketFrames(List<SearchEntity> searchBeList) {

                List<Frame3> frames = new ArrayList<Frame3>();

                for (SearchEntity searchBe : searchBeList) {

                        String code = searchBe.getCode().split("SBE_")[1];

                        Frame3 frameBucketHeader = Frame3.builder("FRM_BUCKET_HEADER_" + code).end().build();
                        Frame3 frameBucketContent = Frame3.builder("FRM_BUCKET_CONTENT_" + code).end().build();
                        Frame3 frameBucketFooter = Frame3.builder("FRM_BUCKET_FOOTER_" + code).end().build();

                        Frame3 frameBucket = Frame3.builder("FRM_BUCKET_" + code)
                                        .addFrame(frameBucketHeader, FramePosition.NORTH).end()
                                        .addFrame(frameBucketContent, FramePosition.CENTRE).end()
                                        .addFrame(frameBucketFooter, FramePosition.SOUTH).end().build();

                        frames.add(frameBucket);
                }

                return frames;
        }

        public List<Frame3> getBucketHeaderFrames(List<Ask> asks) {

                List<Frame3> frames = new ArrayList<Frame3>();

                for (Ask ask : asks) {

                        Frame3 frame = Frame3.builder("FRM_" + ask.getQuestionCode()).question(ask.getQuestionCode())
                                        .end().build();

                        frames.add(frame);
                }

                return frames;
        }

        // @Test
        public void testContext() {
                QRules rules = GennyJbpmBaseTest.setupLocalService();
                GennyToken userToken = new GennyToken("userToken", rules.getToken());
                GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
                BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
                BaseEntity project = beUtils.getBaseEntityByCode("PRJ_" + serviceToken.getRealm().toUpperCase());

                try {
                        rules.sendAllAttributes();

                        /* get the icon baseentity */
                        BaseEntity sortIconBe = beUtils.getBaseEntityByCode("ICN_SORT");

                        /* create a context object using the iconBE */
                        Context context = new Context(ContextType.ICON, sortIconBe, VisualControlType.VCL_ICON, 1.0);

                        /* add the context to the question */
                        Frame3 FRM_HAMBURGER_MENU = Frame3.builder("FRM_HAMBURGER_MENU").question("QUE_WEBSITE")
                                        .addContext(context).end().end().build();

                        /*
                         * in the question, the context should appear same as the themes that appear for
                         * the question.
                         */

                        /* frame-root */
                        Frame3 FRM_ROOT = Frame3.builder("FRM_ROOT").addFrame(FRM_HAMBURGER_MENU, FramePosition.CENTRE)
                                        .end().build();

                        Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();
                        QDataBaseEntityMessage msg = FrameUtils2.toMessage(FRM_ROOT, serviceToken, askMsgs);
                        rules.publishCmd(msg);
                        for (QDataAskMessage askMsg : askMsgs) {
                                rules.publishCmd(askMsg, serviceToken.getUserCode(), userToken.getUserCode());
                        }
                        System.out.println("Sent");

                } catch (Exception e) {
                        System.out.println("Error " + e.getLocalizedMessage());
                }
        }

        /// @Test
        public void testDesktop() {
                QRules rules = GennyJbpmBaseTest.setupLocalService();
                GennyToken userToken = new GennyToken("userToken", rules.getToken());
                GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
                BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
                BaseEntity project = beUtils.getBaseEntityByCode("PRJ_" + serviceToken.getRealm().toUpperCase());

                try {
                        rules.sendAllAttributes();

                        /* frame-header */
                        Frame3 FRM_HEADER = generateHeader();
                        /* Frame3 FRM_SIDEBAR = generateSidebar(); */
                        Frame3 FRM_SIDEBAR = generateInternmatchSidebar();
                        /* Frame3 FRM_CONTENT = getDashboard(); */
                        /* Frame3 FRM_TABLE = generateTable(); */
                        Frame3 FRM_FOOTER = generateFooter();
                        // Frame3 FRM_TABS = generateTabs();
                        // Frame3 FRM_TABS = generateTabs();
                        Frame3 FRM_TABS = generateProcessView();

                        /* need to call performSearch in TableUtils */

                        // TableUtilsTest tableUtils = new TableUtilsTest(beUtils);

                        // Answer answer = new
                        // Answer(userToken.getUserCode(),userToken.getUserCode(),"PRI_SEARCH_TEXT",
                        // "univ");
                        // TableUtilsTest.performSearch(serviceToken , beUtils, "SBE_SEARCHBAR",
                        // answer);

                        /* frame-root */
                        Frame3 FRM_APP = Frame3.builder("FRM_APP")
                                        .addTheme("THM_PROJECT", ThemePosition.FRAME, serviceToken).end()
                                        // .addFrame(FRM_HEADER, FramePosition.NORTH).end()
                                        .addFrame(FRM_SIDEBAR, FramePosition.WEST).end()

                                        /* .addFrame(FRM_CONTENT, FramePosition.CENTRE).end() */
                                        /* .addFrame(FRM_TABLE, FramePosition.CENTRE).end() */
                                        .addFrame(FRM_FOOTER, FramePosition.SOUTH).end()
                                        .addFrame(FRM_TABS, FramePosition.CENTRE).end().build();

                        /* frame-root */
                        Frame3 FRM_ROOT = Frame3.builder("FRM_ROOT").addFrame(FRM_APP, FramePosition.CENTRE).end()
                                        .build();

                        Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();
                        QDataBaseEntityMessage msg = FrameUtils2.toMessage(FRM_ROOT, serviceToken, askMsgs);
                        rules.publishCmd(msg);
                        for (QDataAskMessage askMsg : askMsgs) {
                                rules.publishCmd(askMsg, serviceToken.getUserCode(), userToken.getUserCode());
                        }
                        System.out.println("Sent");
                } catch (Exception e) {
                        System.out.println("Error " + e.getLocalizedMessage());
                }
        }

        // @Test
        public void userSessionANishTest() {

                // VertxUtils.cachedEnabled = true; // don't try and use any local services

                QRules rules = GennyJbpmBaseTest.setupLocalService();
                GennyToken userToken = getToken(realm, "user1", "Barry Allan", "hero");

                GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
                QEventMessage newLoginMessage = new QEventMessage("EVT_MSG", "AUTH_INIT");
                newLoginMessage.getData().setValue("NEW_SESSION");

                QEventMessage logOutMessage = new QEventMessage("EVT_MSG", "LOGOUT");
                logOutMessage.getData().setValue("LOGOUT");
                QEventMessage displayTableMessage = new QEventMessage("EVT_MSG", "DISPLAY_DETAILS");
                QEventMessage AuthINit = new QEventMessage("EVT_MSG", "AUTH_INIT");

                GennyKieSession gks = GennyKieSession.builder(serviceToken).addJbpm("user_lifecycle2.bpmn")
                                .addJbpm("userSession2.bpmn").addJbpm("show_dashboard2.bpmn")
                                .addJbpm("user_validation.bpmn").addJbpm("auth_init.bpmn").addJbpm("bucket_page2.bpmn")
                                .addJbpm("detailpage.bpmn").addToken(userToken).addFact("rules", rules).build();

                gks.start();
                gks.advanceSeconds(3, true);
                gks.injectSignal("newSession", newLoginMessage);
                gks.advanceSeconds(5, true);

                /*
                 * gks.injectSignal("event",AuthINit); gks.advanceSeconds(5, true);
                 *
                 * gks.injectSignal("event",displayTableMessage); gks.advanceSeconds(5, true);
                 *
                 *
                 * gks.injectSignal("event",AuthINit); gks.advanceSeconds(5, true);
                 */

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
                                        .maxWidth(900).width("100%").end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                        Theme THM_VERTICAL_SCROLL = Theme.builder("THM_VERTICAL_SCROLL").addAttribute()
                                        .overflowY("auto").padding(40).end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                        Theme THM_PADDING_40 = Theme.builder("THM_PADDING_40").addAttribute().padding(40).end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                        Theme THM_DASHBOARD_WRAPPER = Theme.builder("THM_DASHBOARD_WRAPPER").addAttribute()
                                        .flexShrink(0).height("initial").flexBasis("auto").end()
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
                                        .addAttribute().flexGrow(0).flexBasis("initial").height("initial").end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

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

                        Frame3 FRM_DASHBOARD_INTERNS = Frame3.builder("FRM_DASHBOARD_INTERNS").addTheme(THM_DASHBOARD)
                                        .end().addTheme(THM_DASHBOARD_WRAPPER, ThemePosition.WRAPPER).end()
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

                        Frame3 FRM_CONTENT = Frame3.builder("FRM_CONTENT").addTheme(THM_PROJECT_COLOR_BACKGROUND).end()
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

        public Frame3 generateTable() {

                QRules rules = GennyJbpmBaseTest.setupLocalService();
                GennyToken userToken = new GennyToken("userToken", rules.getToken());
                GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
                BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
                BaseEntity project = beUtils.getBaseEntityByCode("PRJ_" + serviceToken.getRealm().toUpperCase());

                try {

                        Theme THM_TABLE_HEADER = Theme.builder("THM_TABLE_HEADER").addAttribute().width("100%").end()
                                        .build();
                        Theme THM_TABLE_BORDER = Theme.builder("THM_TABLE_BORDER").addAttribute().borderBottomWidth(1)
                                        .borderColor("#f6f6f6").borderStyle("solid").end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                        Theme THM_TABLE_CONTENT_CENTRE = Theme.builder("THM_TABLE_CONTENT_CENTRE").addAttribute()
                                        .justifyContent("flex-start").paddingX(10).end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                        Theme THM_TABLE_HEADER_CELL_WRAPPER = Theme.builder("THM_TABLE_HEADER_CELL_WRAPPER")
                                        .addAttribute().width("initial").flexGrow(1).flexShrink(1).flexBasis("auto")
                                        .padding(10).justifyContent("flex-start").end().build();

                        Theme THM_TEXT_ALIGN_CENTER = Theme.builder("THM_TEXT_ALIGN_CENTER").addAttribute()
                                        .textAlign("center").end().build();

                        Theme THM_TABLE_HEADER_CELL_GROUP_LABEL = Theme.builder("THM_TABLE_HEADER_CELL_GROUP_LABEL")
                                        .addAttribute().paddingLeft(10).alignSelf("flex-start").end().build();

                        Theme THM_TABLE_CONTENT = Theme.builder("THM_TABLE_CONTENT").addAttribute().width("100%").end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();
                        Theme THM_TABLE_ROW = Theme.builder("THM_TABLE_ROW").addAttribute().width("100%").marginLeft(30)
                                        .backgroundColor("yellow").end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();
                        Theme THM_TABLE_ROW_CONTENT_WRAPPER = Theme.builder("THM_TABLE_ROW_CONTENT_WRAPPER")
                                        .addAttribute().width("100%").end().build();
                        Theme THM_TABLE_ROW_CELL = Theme.builder("THM_TABLE_ROW_CELL").addAttribute().flexGrow(1)
                                        .flexBasis(0).end().build();
                        Theme THM_TABLE_BODY = Theme.builder("THM_TABLE_BODY").addAttribute().width("100%").end()
                                        .build();
                        Theme THM_TABLE_FOOTER = Theme.builder("THM_TABLE_FOOTER").addAttribute()
                                        .backgroundColor("#f4f5f7").width("100%").color("black").end().build();

                        Theme THM_TABLE = Theme.builder("THM_TABLE").addAttribute().width("100%").end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();
                        Theme THM_WIDTH_100_PERCENT_NO_INHERIT = Theme.builder("THM_WIDTH_100_PERCENT_NO_INHERIT")
                                        .addAttribute().width("100%").end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                        Theme THM_DISPLAY_VERTICAL = Theme.builder("THM_DISPLAY_VERTICAL").addAttribute()
                                        .flexDirection("column").end().build();

                        Theme THM_QUESTION_GRP_LABEL = Theme.builder("THM_QUESTION_GRP_LABEL")
                                        .addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_LABEL, true).end()
                                        .build();

                        Validation tableCellValidation = new Validation("VLD_ANYTHING", "Anything", ".*");
                        List<Validation> tableCellValidations = new ArrayList<>();
                        tableCellValidations.add(tableCellValidation);

                        ValidationList tableCellValidationList = new ValidationList();
                        tableCellValidationList.setValidationList(tableCellValidations);

                        DataType tableCellDataType = new DataType("DTT_TABLE_CELL_GRP", tableCellValidationList,
                                        "Table Cell Group", "");

                        String searchBarString = "univ";

                        SearchEntity searchBE = new SearchEntity("SBE_SEARCH", "Search")
                                        .addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC)
                                        .addFilter("PRI_NAME", SearchEntity.StringFilter.LIKE,
                                                        "%" + searchBarString + "%")
                                        .addColumn("PRI_NAME", "Name").addColumn("PRI_LANDLINE", "Phone")
                                        .addColumn("PRI_EMAIL", "Email").addColumn("PRI_ADDRESS_CITY", "City")
                                        .addColumn("PRI_ADDRESS_STATE", "State").setPageStart(0).setPageSize(10);

                        TableUtils tableUtils = new TableUtils(beUtils);

                        QDataBaseEntityMessage msg = tableUtils.fetchSearchResults(searchBE, beUtils.getGennyToken());

                        TableData tableData = tableUtils.generateTableAsks(searchBE, beUtils.getGennyToken());
                        VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg));

                        Ask headerAsk = tableData.getAsk();
                        Ask[] askArray = new Ask[1];
                        askArray[0] = headerAsk;
                        QDataAskMessage headerAskMsg = new QDataAskMessage(askArray);
                        headerAskMsg.setToken(beUtils.getGennyToken().getToken());
                        VertxUtils.writeMsg("webcmds", JsonUtils.toJson(headerAskMsg));
                        String headerAskCode = headerAsk.getQuestionCode();

                        // Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();
                        // msg =
                        // TableUtils.changeQuestion("FRM_TABLE_HEADER",headerAskCode,serviceToken,beUtils.getGennyToken(),askMsgs);
                        //
                        //
                        // VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg));

                        Frame3 FRM_TABLE_HEADER = Frame3.builder("FRM_TABLE_HEADER")
                                        /* .addTheme(THM_TABLE_HEADER).end() */
                                        .addTheme(THM_TABLE_BORDER).end().question("QUE_TEST_TABLE_HEADER_GRP") // QUE_TEST_TABLE_HEADER_GRP
                                        .addTheme(THM_QUESTION_GRP_LABEL).vcl(VisualControlType.GROUP)
                                        .dataType(tableCellDataType).end().addTheme(THM_WIDTH_100_PERCENT_NO_INHERIT)
                                        .vcl(VisualControlType.GROUP).end().addTheme(THM_TABLE_ROW_CELL)
                                        .dataType(tableCellDataType).vcl(VisualControlType.GROUP_WRAPPER).end()
                                        .addTheme("THM_DISPLAY_HORIZONTAL", serviceToken).weight(2.0).end()
                                        .addTheme(THM_TABLE_HEADER_CELL_WRAPPER).vcl(VisualControlType.VCL_WRAPPER)
                                        .end().addTheme(THM_TABLE_HEADER_CELL_GROUP_LABEL)
                                        .vcl(VisualControlType.GROUP_LABEL).end().addTheme(THM_DISPLAY_VERTICAL)
                                        .dataType(tableCellDataType).weight(1.0).end().end().build();

                        Validation tableRowValidation = new Validation("VLD_ANYTHING", "Anything", ".*");
                        List<Validation> tableRowValidations = new ArrayList<>();
                        tableRowValidations.add(tableRowValidation);

                        ValidationList tableRowValidationList = new ValidationList();
                        tableRowValidationList.setValidationList(tableRowValidations);

                        DataType tableRowDataType = new DataType("DTT_TABLE_ROW_GRP", tableRowValidationList,
                                        "Table Row Group", "");

                        Frame3 FRM_TABLE_CONTENT = Frame3.builder("FRM_TABLE_CONTENT").addTheme(THM_TABLE_BORDER).end()
                                        .addTheme(THM_TABLE_CONTENT_CENTRE, ThemePosition.CENTRE).end()
                                        .question("QUE_TEST_TABLE_RESULTS_GRP")
                                        .addTheme("THM_DISPLAY_HORIZONTAL", serviceToken).dataType(tableRowDataType)
                                        .weight(1.0).end().addTheme(THM_TABLE_ROW_CONTENT_WRAPPER)
                                        .dataType(tableRowDataType).vcl(VisualControlType.GROUP).weight(1.0).end()
                                        .addTheme(THM_TABLE_ROW).dataType(tableRowDataType).weight(1.0).end()
                                        .addTheme(THM_TABLE_CONTENT).vcl(VisualControlType.GROUP).end()
                                        .addTheme(THM_TABLE_ROW_CELL).vcl(VisualControlType.VCL_WRAPPER).end().end()
                                        .build();

                        Frame3 FRM_TABLE_BODY = Frame3.builder("FRM_TABLE_BODY")
                                        .addFrame(FRM_TABLE_HEADER, FramePosition.NORTH).end()
                                        .addFrame(FRM_TABLE_CONTENT, FramePosition.CENTRE).end().build();

                        Validation validation = new Validation("VLD_ANYTHING", "Anything", ".*");
                        List<Validation> validations = new ArrayList<>();
                        validations.add(validation);

                        ValidationList buttonValidationList = new ValidationList();
                        buttonValidationList.setValidationList(validations);

                        DataType buttonDataType = new DataType("DTT_EVENT", buttonValidationList, "Event", "");

                        Frame3 FRM_TABLE_FOOTER_PAGINATION = Frame3.builder("FRM_TABLE_FOOTER_PAGINATION")
                                        .addTheme("THM_DISPLAY_HORIZONTAL", serviceToken).end()
                                        .addTheme("THM_FRAME_ALIGN_EAST", ThemePosition.WRAPPER, serviceToken).end()
                                        .question("QUE_TABLE_FOOTER_GRP")
                                        .addTheme("THM_DISPLAY_HORIZONTAL", serviceToken).end()
                                        .addTheme("THM_PROJECT_COLOR_PRIMARY_VARIANT_LIGHT", serviceToken)
                                        .dataType(buttonDataType).end().addTheme(THM_TABLE_HEADER_CELL_WRAPPER)
                                        .vcl(VisualControlType.VCL_WRAPPER).end().addTheme(THM_TEXT_ALIGN_CENTER)
                                        .vcl(VisualControlType.VCL_INPUT).end().end().build();

                        /*  */
                        Frame3 FRM_TABLE_RESULT_COUNT = Frame3.builder("FRM_TABLE_RESULT_COUNT")
                                        .question("QUE_TABLE_TOTAL_RESULT_COUNT")
                                        .addTheme("THM_LABEL_BOLD", serviceToken).vcl(VisualControlType.VCL_LABEL).end()
                                        .addTheme("THM_DISPLAY_HORIZONTAL", serviceToken)
                                        .vcl(VisualControlType.VCL_WRAPPER).end().end().build();

                        Frame3 FRM_TABLE_PAGE_INDEX = Frame3.builder("FRM_TABLE_PAGE_INDEX")
                                        .question("QUE_TABLE_PAGE_INDEX").addTheme("THM_LABEL_BOLD", serviceToken)
                                        .vcl(VisualControlType.VCL_LABEL).end()
                                        .addTheme("THM_DISPLAY_HORIZONTAL", serviceToken)
                                        .vcl(VisualControlType.VCL_WRAPPER).end().end().build();

                        Frame3 FRM_TABLE_FOOTER = Frame3.builder("FRM_TABLE_FOOTER")
                                        .addTheme("THM_DISPLAY_HORIZONTAL", serviceToken).end()
                                        .addFrame(FRM_TABLE_RESULT_COUNT, FramePosition.WEST).end()
                                        .addFrame(FRM_TABLE_PAGE_INDEX, FramePosition.WEST).end()
                                        .addFrame(FRM_TABLE_FOOTER_PAGINATION, FramePosition.EAST).end().build();

                        Theme THM_BOX_SHADOW_XS = Theme.builder("THM_BOX_SHADOW_XS").addAttribute()
                                        .shadowColor("#dedede").shadowOpacity(0.5).shadowRadius(90).shadowOffset()
                                        .width(0).height(0).end().end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                        Frame3 FRM_TABLE_WRAPPER = Frame3.builder("FRM_TABLE_WRAPPER")
                                        .addTheme(THM_BOX_SHADOW_XS, ThemePosition.WRAPPER).end()
                                        .addTheme("THM_PROJECT_COLOR_SURFACE", serviceToken).end()
                                        .addFrame(FRM_TABLE_BODY, FramePosition.CENTRE).end()
                                        .addFrame(FRM_TABLE_FOOTER, FramePosition.SOUTH).end().build();

                        Theme THM_PADDING_20 = Theme.builder("THM_PADDING_20").addAttribute().padding(20).end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                        Theme THM_TITLE = Theme.builder("THM_TITLE").addAttribute().bold(true).size("lg")
                                        .textAlign("left").end().build();

                        Theme THM_WIDTH_100_PERCENT = Theme.builder("THM_WIDTH_100_PERCENT").addAttribute()
                                        .width("100%").end()
                                        /* .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end() */
                                        .build();

                        Frame3 FRM_TABLE_TITLE = Frame3.builder("FRM_TABLE_TITLE").question("QUE_TABLE_TITLE_TEST")
                                        .addTheme(THM_TITLE).vcl(VisualControlType.VCL_LABEL).end()
                                        /*
                                         * .addTheme("THM_TITLE_LABEL",
                                         * serviceToken).vcl(VisualControlType.VCL_LABEL).end()
                                         */
                                        .addTheme("THM_TITLE_WRAPPER", serviceToken).vcl(VisualControlType.VCL_WRAPPER)
                                        .end()
                                        /*
                                         * .addTheme("THM_WIDTH_100_PERCENT",
                                         * serviceToken).vcl(VisualControlType.VCL_WRAPPER).end()
                                         */
                                        .addTheme("THM_TITLE_BEHAVIOUR", serviceToken).end().end().build();

                        Frame3 FRM_TABLE = Frame3.builder("FRM_TABLE")
                                        /* .addTheme(THM_TABLE, ThemePosition.WRAPPER).end() */
                                        .addTheme(THM_WIDTH_100_PERCENT, ThemePosition.WRAPPER).end()
                                        .addTheme("THM_PROJECT_COLOR_BACKGROUND", serviceToken).end()
                                        .addTheme(THM_PADDING_20, ThemePosition.WRAPPER).end()
                                        .addFrame(FRM_TABLE_TITLE, FramePosition.NORTH).end()
                                        .addFrame(FRM_TABLE_WRAPPER, FramePosition.CENTRE).end().build();

                        return FRM_TABLE;

                } catch (Exception e) {
                        System.out.println("Error " + e.getLocalizedMessage());
                }

                System.out.println("Generated TableView Frame");
                return null;
        }

        // @Test
        public void testCachedDesktop() {
                QRules rules = GennyJbpmBaseTest.setupLocalService();
                GennyToken userToken = new GennyToken("userToken", rules.getToken());
                GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());

                rules.sendAllAttributes();

                GennyKieSession gks = null;

                try {
                        gks = GennyKieSession.builder(serviceToken, false).addToken(userToken).build();
                        gks.start();

                        GennyKieSession.displayForm("FRM_DESKTOP2", "FRM_ROOT", userToken);

                        System.out.println("Sent");

                } catch (Exception e) {
                        System.out.println(e.getLocalizedMessage());
                } finally {
                        gks.close();
                }

                // try {
                // /* frame-tabs */
                // Frame3 FRM_TABS = VertxUtils.getObject(serviceToken.getRealm(), "",
                // "FRM_TABS", Frame3.class,
                // serviceToken.getToken());
                // System.out.print("desktop-tabs :: " + FRM_TABS.getCode());
                //
                // /* frame-header */
                // Frame3 FRM_HEADER = VertxUtils.getObject(serviceToken.getRealm(), "",
                // "FRM_HEADER",
                // Frame3.class, serviceToken.getToken());
                // System.out.print("desktop-header :: " + FRM_HEADER.getCode());
                //
                // /* frame-footer */
                // Frame3 FRM_FOOTER = VertxUtils.getObject(serviceToken.getRealm(), "",
                // "FRM_FOOTER",
                // Frame3.class, serviceToken.getToken());
                // System.out.print("desktop-footer :: " + FRM_FOOTER.getCode());
                //
                // /* frame-sidebar */
                // Frame3 FRM_SIDEBAR = VertxUtils.getObject(serviceToken.getRealm(), "",
                // "FRM_SIDEBAR",
                // Frame3.class, serviceToken.getToken());
                //
                // System.out.print("desktop-sidebar :: " + FRM_SIDEBAR.getCode());
                // /* frame-root */
                // Frame3 FRM_DESKTOP = VertxUtils.getObject(serviceToken.getRealm(), "",
                // "FRM_DESKTOP",
                // Frame3.class, serviceToken.getToken());
                //
                // System.out.print("desktop-frame :: " + FRM_DESKTOP.getCode());
                //
                // Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();
                // QDataBaseEntityMessage msg = FrameUtils2.toMessage(FRM_DESKTOP, serviceToken,
                // askMsgs);
                // rules.publishCmd(msg);
                // for (QDataAskMessage askMsg : askMsgs) {
                // rules.publishCmd(askMsg, serviceToken.getUserCode(),
                // userToken.getUserCode());
                // }
                // System.out.println("Sent");
                // } catch (Exception e) {
                // System.out.println("Error " + e.getLocalizedMessage());
                // }
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

                        Theme THM_DROPDOWN_PROFILE_BEHAVIOUR_GENNY = Theme
                                        .builder("THM_DROPDOWN_PROFILE_BEHAVIOUR_GENNY")
                                        .addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_LABEL, false).end()
                                        .build();

                        Theme THM_BACKGROUND_NONE = Theme.builder("THM_BACKGROUND_NONE").addAttribute()
                                        .backgroundColor("none").end().build();

                        Theme THM_DROPDOWN_HEADER_WRAPPER_GENNY = Theme.builder("THM_DROPDOWN_HEADER_WRAPPER_GENNY")
                                        .addAttribute().padding(5)
                                        .backgroundColor(project.getValue("PRI_COLOR_PRIMARY_VARIANT_LIGHT", "#395268"))
                                        .end().build();

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
                                        .color(project.getValue("PRI_COLOR_SURFACE_ON", "#000000")).end().build();

                        Theme THM_DASHBOARD_ITEM_INPUT = Theme.builder("THM_DASHBOARD_ITEM_INPUT").addAttribute()
                                        .dynamicWidth(true).end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                        // Theme THM_HEADER_PROFILE_PICTURE =
                        // Theme.builder("THM_HEADER_PROFILE_PICTURE")
                        // .addAttribute()
                        // .height(32)
                        // .width(32)
                        // .fit("cover")
                        // .borderRadius(50)
                        // .end()
                        // .build();

                        Theme THM_PANEL_CONTENT_FIT = Theme.builder("THM_PANEL_CONTENT_FIT").addAttribute()
                                        .flexShrink(0).flexBasis("auto").flexGrow(0).width("auto").end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                        Theme THM_PADDING_RIGHT_10 = Theme.builder("THM_PADDING_RIGHT_10").addAttribute()
                                        .paddingRight(10).end().build();

                        Theme THM_BORDER_RADIUS_50 = Theme.builder("THM_BORDER_RADIUS_50").addAttribute()
                                        .borderRadius(50).end().build();

                        Frame3 FRM_HEADER_FIRSTNAME = Frame3.builder("FRM_HEADER_FIRSTNAME").question("QUE_FIRSTNAME")
                                        .end().build();
                        Frame3 FRM_HEADER_LASTNAME = Frame3.builder("FRM_HEADER_LASTNAME").question("QUE_LASTNAME")
                                        .end().build();

                        Frame3 FRM_HEADER_USERNAME = Frame3.builder("FRM_HEADER_USERNAME")
                                        .addTheme(THM_PANEL_CONTENT_FIT, ThemePosition.WRAPPER).end()
                                        .question("QUE_NAME").targetAlias("PER_USER1")
                                        .addTheme(THM_DASHBOARD_ITEM_INPUT).vcl(VisualControlType.VCL_INPUT).end().end()
                                        .build();

                        Frame3 FRM_HEADER_PROFILE_PICTURE = Frame3.builder("FRM_HEADER_PROFILE_PICTURE")
                                        .addTheme(THM_PANEL_CONTENT_FIT, ThemePosition.WRAPPER).end()
                                        .addTheme(THM_PADDING_RIGHT_10, ThemePosition.WRAPPER).end()
                                        .question("QUE_IMAGE_URL")
                                        // .addTheme(THM_HEADER_PROFILE_PICTURE).vcl(VisualControlType.VCL_INPUT).end()
                                        .addTheme(THM_BORDER_RADIUS_50).vcl(VisualControlType.INPUT_FIELD).end().end()
                                        .build();

                        Frame3 FRM_HEADER_OPTIONS = Frame3.builder("FRM_HEADER_OPTIONS")
                                        .addTheme(THM_PANEL_CONTENT_FIT, ThemePosition.WRAPPER).end()
                                        .question("QUE_OPTIONS_GRP").addTheme(THM_BACKGROUND_NONE)
                                        .vcl(VisualControlType.GROUP).weight(1.0).end()
                                        .addTheme(THM_DROPDOWN_BEHAVIOUR_GENNY).vcl(VisualControlType.GROUP).weight(2.0)
                                        .end().addTheme(THM_DROPDOWN_PROFILE_BEHAVIOUR_GENNY)
                                        .vcl(VisualControlType.GROUP).weight(1.0).end()
                                        .addTheme(THM_DROPDOWN_HEADER_WRAPPER_GENNY)
                                        .vcl(VisualControlType.GROUP_HEADER_WRAPPER).weight(2.0).end()
                                        .addTheme(THM_DROPDOWN_GROUP_LABEL_GENNY).vcl(VisualControlType.GROUP_LABEL)
                                        .end().addTheme(THM_DROPDOWN_CONTENT_WRAPPER_GENNY)
                                        .vcl(VisualControlType.GROUP_CONTENT_WRAPPER).end().addTheme(THM_BOX_SHADOW_SM)
                                        .vcl(VisualControlType.GROUP_CONTENT_WRAPPER).end()
                                        .addTheme(THM_DROPDOWN_VCL_GENNY).vcl(VisualControlType.VCL).end().end()
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

                        Theme THM_HEADER = Theme.builder("THM_HEADER").addAttribute().height(80)
                                        // .paddingRight(20)
                                        .end().addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                        BaseEntity sortIconBe = beUtils.getBaseEntityByCode("ICN_SORT");

                        Context context = new Context(ContextType.ICON, sortIconBe, VisualControlType.VCL_ICON, 1.0);

                        /* Test Context */
                        Frame3 FRM_HAMBURGER_MENU = Frame3.builder("FRM_HAMBURGER_MENU").question("QUE_WEBSITE")
                                        .addContext(context).end().end().build();

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

                        Theme THM_SEARCH_BAR = Theme.builder("THM_SEARCH_BAR").addAttribute().backgroundColor("white")
                                        .color("black").end().build();

                        Theme THM_SEARCH_BAR_WRAPPER = Theme.builder("THM_SEARCH_BAR_WRAPPER").addAttribute()
                                        .maxWidth(700).padding(10).borderRadius(5).width("100%").end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                        Theme THM_SEARCH_BAR_CENTRE = Theme.builder("THM_SEARCH_BAR_CENTRE").addAttribute()
                                        .justifyContent("flex-start").flexDirection("row").end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                        Frame3 FRM_SEARCH_BAR = Frame3.builder("FRM_SEARCH_BAR")
                                        .addTheme(THM_SEARCH_BAR_CENTRE, ThemePosition.CENTRE).end()
                                        .question("QUE_SEARCH").addTheme(THM_SEARCH_BAR).vcl(VisualControlType.VCL)
                                        .end().addTheme(THM_SEARCH_BAR_WRAPPER).vcl(VisualControlType.VCL_WRAPPER).end()
                                        .end().build();

                        Theme THM_PROJECT_NAME = Theme.builder("THM_PROJECT_NAME").addAttribute().flexGrow(0).end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                        Frame3 FRM_PROJECT_NAME = Frame3.builder("FRM_PROJECT_NAME")
                                        .addTheme("THM_SIDEBAR_WIDTH", ThemePosition.WRAPPER, serviceToken).end()
                                        .addTheme(THM_PROJECT_NAME, ThemePosition.WRAPPER).end()
                                        .question("QUE_NAME_TWO")
                                        .targetAlias("PRJ_" + serviceToken.getRealm().toUpperCase())
                                        .addTheme("THM_TITLE_LABEL", serviceToken).vcl(VisualControlType.VCL_LABEL)
                                        .end().end().build();

                        Frame3 FRM_LOGO = Frame3.builder("FRM_LOGO").question("QUE_PROJECT_SIDEBAR_GRP")
                                        .addTheme("THM_LOGO", serviceToken).vcl(VisualControlType.VCL_WRAPPER).end()
                                        .end().build();

                        Frame3 FRM_HEADER = Frame3.builder("FRM_HEADER").addTheme(THM_HEADER).end().addTheme(THM_HEADER)
                                        .end()
                                        // .addTheme(THM_FRAME_ALIGN_WEST, ThemePosition.WEST).end()
                                        // .addTheme(THM_FRAME_ALIGN_EAST, ThemePosition.EAST).end()
                                        .addFrame(FRM_LOGO, FramePosition.WEST).end()
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

                        Theme THM_TREE_ITEM_GROUP_WRAPPER = Theme.builder("THM_TREE_ITEM_GROUP_WRAPPER").addAttribute()
                                        .justifyContent("flex-start").flexDirection("row").end().build();

                        Theme THM_LOGO = Theme.builder("THM_LOGO").addAttribute().fit("contain").height(100).width(100)
                                        .end().build();

                        Theme THM_LOGO_CENTRE = Theme.builder("THM_LOGO_CENTRE").addAttribute().justifyContent("center")
                                        .width("100%").end().build();

                        Theme THM_MARGIN_BOTTOM_20 = Theme.builder("THM_MARGIN_BOTTOM_20").addAttribute()
                                        .marginBottom(20).end().build();

                        Frame3 FRM_LOGO = Frame3.builder("FRM_LOGO").addTheme(THM_TREE_ITEM, ThemePosition.WRAPPER)
                                        .end().addTheme(THM_MARGIN_BOTTOM_20, ThemePosition.WRAPPER).end()
                                        .addTheme(THM_LOGO_CENTRE, ThemePosition.CENTRE).end()
                                        .question("QUE_PROJECT_SIDEBAR_GRP").addTheme(THM_LOGO)
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

                        /* Internmatch Trees */
                        Frame3 FRM_TREE_DASHBOARD_VIEW = Frame3.builder("FRM_TREE_DASHBOARD_VIEW")
                                        .addTheme(THM_TREE_ITEM, ThemePosition.WRAPPER).end()
                                        .addTheme(THM_TREE_ITEM_GROUP_WRAPPER, ThemePosition.CENTRE).end()
                                        .question("QUE_TREE_DASHBOARD_VIEW").end().build();
                        Frame3 FRM_TREE_INTERNSHIPS_VIEW = Frame3.builder("FRM_TREE_INTERNSHIPS_VIEW")
                                        .addTheme(THM_TREE_ITEM, ThemePosition.WRAPPER).end()
                                        .addTheme(THM_TREE_ITEM_GROUP_WRAPPER, ThemePosition.CENTRE).end()
                                        .question("QUE_TREE_INTERNSHIPS_VIEW").end().build();
                        Frame3 FRM_TREE_CONTACTS_VIEW = Frame3.builder("FRM_TREE_CONTACTS_VIEW")
                                        .addTheme(THM_TREE_ITEM, ThemePosition.WRAPPER).end()
                                        .addTheme(THM_TREE_ITEM_GROUP_WRAPPER, ThemePosition.CENTRE).end()
                                        .question("QUE_TREE_CONTACTS_VIEW").end().build();
                        Frame3 FRM_TREE_COMPANIES_VIEW = Frame3.builder("FRM_TREE_COMPANIES_VIEW")
                                        .addTheme(THM_TREE_ITEM, ThemePosition.WRAPPER).end()
                                        .addTheme(THM_TREE_ITEM_GROUP_WRAPPER, ThemePosition.CENTRE).end()
                                        .question("QUE_TREE_COMPANIES_VIEW").end().build();
                        Frame3 FRM_TREE_INTERNSHIP_TEMPLATES_VIEW = Frame3.builder("FRM_TREE_INTERNSHIP_TEMPLATES_VIEW")
                                        .addTheme(THM_TREE_ITEM, ThemePosition.WRAPPER).end()
                                        .addTheme(THM_TREE_ITEM_GROUP_WRAPPER, ThemePosition.CENTRE).end()
                                        .question("QUE_TREE_INTERNSHIP_TEMPLATES_VIEW").end().build();
                        /*  */

                        Frame3 FRM_SIDEBAR = Frame3.builder("FRM_SIDEBAR").addTheme(THM_SIDEBAR).end()
                                        .addTheme(THM_SIDEBAR_WIDTH).end().addFrame(FRM_LOGO, FramePosition.NORTH).end()
                                        /*
                                         * .addFrame(FRM_TREE_TABLE_VIEW, FramePosition.NORTH).end()
                                         * .addFrame(FRM_TREE_BUCKET_VIEW, FramePosition.NORTH).end()
                                         * .addFrame(FRM_TREE_DETAIL_VIEW, FramePosition.NORTH).end()
                                         * .addFrame(FRM_TREE_FORM_VIEW, FramePosition.NORTH).end()
                                         */
                                        /* internmatch trees */
                                        .addFrame(FRM_TREE_DASHBOARD_VIEW, FramePosition.NORTH).end()
                                        .addFrame(FRM_TREE_INTERNSHIPS_VIEW, FramePosition.NORTH).end()
                                        .addFrame(FRM_TREE_CONTACTS_VIEW, FramePosition.NORTH).end()
                                        .addFrame(FRM_TREE_COMPANIES_VIEW, FramePosition.NORTH).end()
                                        .addFrame(FRM_TREE_INTERNSHIP_TEMPLATES_VIEW, FramePosition.NORTH).end()
                                        .build();

                        System.out.println("Generated Sidebar Frame");
                        return FRM_SIDEBAR;
                } catch (Exception e) {
                        System.out.println("Error " + e.getLocalizedMessage());
                }
                return null;
        }

        public Frame3 generateInternmatchSidebar() {
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
                                        .height("auto").flexGrow(0).flexBasis("auto").paddingLeft(20).paddingY(5).end()
                                        .build();

                        Theme THM_TREE_ITEM_GROUP_WRAPPER = Theme.builder("THM_TREE_ITEM_GROUP_WRAPPER").addAttribute()
                                        .justifyContent("flex-start").flexDirection("row").end().build();

                        /* Internmatch Trees */
                        Frame3 FRM_TREE_ITEM_DASHBOARD_VIEW = Frame3.builder("FRM_TREE_ITEM_DASHBOARD_VIEW")
                                        .addTheme(THM_TREE_ITEM, ThemePosition.WRAPPER).end()
                                        .addTheme(THM_TREE_ITEM_GROUP_WRAPPER, ThemePosition.CENTRE).end()
                                        .question("QUE_TREE_DASHBOARD_VIEW").end().build();
                        Frame3 FRM_TREE_ITEM_INTERNSHIPS_VIEW = Frame3.builder("FRM_TREE_ITEM_INTERNSHIPS_VIEW")
                                        .addTheme(THM_TREE_ITEM, ThemePosition.WRAPPER).end()
                                        .addTheme(THM_TREE_ITEM_GROUP_WRAPPER, ThemePosition.CENTRE).end()
                                        .question("QUE_TREE_INTERNSHIPS_VIEW").end().build();
                        Frame3 FRM_TREE_ITEM_CONTACTS_VIEW = Frame3.builder("FRM_TREE_ITEM_CONTACTS_VIEW")
                                        .addTheme(THM_TREE_ITEM, ThemePosition.WRAPPER).end()
                                        .addTheme(THM_TREE_ITEM_GROUP_WRAPPER, ThemePosition.CENTRE).end()
                                        .question("QUE_TREE_CONTACTS_VIEW").end().build();
                        Frame3 FRM_TREE_ITEM_COMPANIES_VIEW = Frame3.builder("FRM_TREE_ITEM_COMPANIES_VIEW")
                                        .addTheme(THM_TREE_ITEM, ThemePosition.WRAPPER).end()
                                        .addTheme(THM_TREE_ITEM_GROUP_WRAPPER, ThemePosition.CENTRE).end()
                                        .question("QUE_TREE_COMPANIES_VIEW").end().build();
                        Frame3 FRM_TREE_ITEM_INTERNSHIP_TEMPLATES_VIEW = Frame3
                                        .builder("FRM_TREE_ITEM_INTERNSHIP_TEMPLATES_VIEW")
                                        .addTheme(THM_TREE_ITEM, ThemePosition.WRAPPER).end()
                                        .addTheme(THM_TREE_ITEM_GROUP_WRAPPER, ThemePosition.CENTRE).end()
                                        .question("QUE_TREE_INTERNSHIP_TEMPLATES_VIEW").end().build();
                        /*  */

                        Frame3 FRM_SIDEBAR = Frame3.builder("FRM_SIDEBAR").addTheme(THM_SIDEBAR).end()
                                        .addTheme(THM_SIDEBAR_WIDTH).end()
                                        .addFrame(FRM_TREE_ITEM_DASHBOARD_VIEW, FramePosition.NORTH).end()
                                        .addFrame(FRM_TREE_ITEM_INTERNSHIPS_VIEW, FramePosition.NORTH).end()
                                        .addFrame(FRM_TREE_ITEM_CONTACTS_VIEW, FramePosition.NORTH).end()
                                        .addFrame(FRM_TREE_ITEM_COMPANIES_VIEW, FramePosition.NORTH).end()
                                        .addFrame(FRM_TREE_ITEM_INTERNSHIP_TEMPLATES_VIEW, FramePosition.NORTH).end()
                                        .build();

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

        public Frame3 generateBucket() {

                Theme THM_FORM_INPUT_DEFAULT = Theme.builder("THM_FORM_INPUT_DEFAULT").addAttribute()
                                .borderBottomWidth(5).color("black").borderColor("#ddd").borderStyle("solid")
                                .placeholderColor("#888").height(70).end().build();

                Theme THM_BUCKET_LABEL = Theme.builder("THM_BUCKET_LABEL").addAttribute().textAlign("center").bold(true)
                                .color("black").end().build();

                Theme THM_BUCKET = Theme.builder("THM_BUCKET").addAttribute().backgroundColor("#F8F9FA")
                                .overflowX("auto").overflowY("auto").width("100%").end()
                                .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                Theme THM_BUCKET_COLUMN = Theme.builder("THM_BUCKET_COLUMN").addAttribute().backgroundColor("#EAEAEA")
                                .minWidth(300).width("100%").margin(20).textAlign("center").flexDirection("column")
                                .end().addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();
                Theme THM_BUCKET_COLUMN_PADDING = Theme.builder("THM_BUCKET_COLUMN_PADDING").addAttribute().padding(20)
                                .justifyContent("flex-start").end()
                                .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                Theme THM_TEST1 = Theme.builder("THM_TEST1").addAttribute().backgroundColor("none").width("100%")
                                .height(70).flexGrow(0).flexBasis("initial").end().build();

                Theme THM_BUCKET_HEADER = Theme.builder("THM_BUCKET_HEADER").addAttribute().backgroundColor("red").end()
                                .build();

                Theme THM_COLOR_BACKGROUND = Theme.builder("THM_COLOR_BACKGROUND").addAttribute()
                                .backgroundColor("#F6F6F6").color("#000000").end().build();

                Frame3 available = Frame3.builder("FRM_GRP_AVAILABLE").addTheme(THM_TEST1).end()
                                // .addTheme(THM_BUCKET_HEADER).weight(1.0).end()
                                .question("QUE_GRP_NOT_APPLIED").addTheme(THM_BUCKET_LABEL).end().end().build();

                Frame3 applied = Frame3.builder("FRM_GRP_APPLIED").addTheme(THM_TEST1).end().question("QUE_GRP_APPLIED")
                                .addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT).weight(2.0).end()
                                .end().addTheme(THM_BUCKET_LABEL).end().build();
                Frame3 shortlisted = Frame3.builder("FRM_GRP_SHORTLISTED").addTheme(THM_TEST1).end()
                                .question("QUE_GRP_SHORTLISTED").addTheme(THM_FORM_INPUT_DEFAULT)
                                .vcl(VisualControlType.VCL_INPUT).weight(2.0).end().end().addTheme(THM_BUCKET_LABEL)
                                .end().build();
                Frame3 interviews = Frame3.builder("FRM_GRP_INTERVIEWS").addTheme(THM_TEST1).end()
                                .question("QUE_GRP_INTERVIEWS").addTheme(THM_FORM_INPUT_DEFAULT)
                                .vcl(VisualControlType.VCL_INPUT).weight(2.0).end().end().addTheme(THM_BUCKET_LABEL)
                                .end().build();
                Frame3 offered = Frame3.builder("FRM_GRP_OFFERED").addTheme(THM_TEST1).end().question("QUE_GRP_OFFERED")
                                .addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT).weight(2.0).end()
                                .end().addTheme(THM_BUCKET_LABEL).end().build();
                Frame3 placed = Frame3.builder("FRM_GRP_PLACED").addTheme(THM_TEST1).end().question("QUE_GRP_PLACED")
                                .addTheme(THM_FORM_INPUT_DEFAULT).vcl(VisualControlType.VCL_INPUT).weight(2.0).end()
                                .end().addTheme(THM_BUCKET_LABEL).end().build();
                Frame3 inProgress = Frame3.builder("FRM_GRP_IN_PROGRESS").addTheme(THM_TEST1).end()
                                .question("QUE_GRP_IN_PROGRESS").addTheme(THM_FORM_INPUT_DEFAULT)
                                .vcl(VisualControlType.VCL_INPUT).weight(2.0).end().end().addTheme(THM_BUCKET_LABEL)
                                .end().build();

                Theme THM_GREEN = Theme.builder("THM_GREEN").addAttribute().backgroundColor("green").end().build();

                Frame3 frameCard1 = generateCards("1");
                Frame3 frameCard2 = generateCards("2");
                Frame3 frameCard3 = generateCards("3");
                Frame3 frameCard4 = generateCards("4");
                Frame3 frameCard5 = generateCards("5");
                Frame3 frameCard6 = generateCards("6");
                Frame3 frameCard7 = generateCards("7");
                Frame3 frameCard8 = generateCards("8");
                Frame3 frameCard9 = generateCards("9");
                Frame3 frameCard10 = generateCards("1");
                Frame3 frameCard11 = generateCards("1");
                Frame3 frameCard12 = generateCards("2");
                Frame3 frameCard13 = generateCards("3");
                Frame3 frameCard14 = generateCards("4");
                Frame3 frameCard15 = generateCards("5");
                Frame3 frameCard16 = generateCards("6");
                Frame3 frameCard17 = generateCards("7");
                Frame3 frameCard18 = generateCards("8");
                Frame3 frameCard19 = generateCards("9");

                Frame3 bucketColumn1 = Frame3.builder("FRM_BUCKET_COLUMN_ONE")
                                .addTheme(THM_BUCKET_COLUMN, ThemePosition.FRAME).end()
                                .addTheme(THM_BUCKET_COLUMN_PADDING, ThemePosition.CENTRE).end()
                                .addFrame(available, FramePosition.NORTH).end()
                                .addFrame(frameCard1, FramePosition.CENTRE).end()
                                .addFrame(frameCard2, FramePosition.CENTRE).end()
                                .addFrame(frameCard3, FramePosition.CENTRE).end()
                                .addFrame(frameCard4, FramePosition.CENTRE).end()
                                .addFrame(frameCard5, FramePosition.CENTRE).end()
                                .addFrame(frameCard6, FramePosition.CENTRE).end()
                                .addFrame(frameCard7, FramePosition.CENTRE).end()
                                .addFrame(frameCard8, FramePosition.CENTRE).end()
                                .addFrame(frameCard9, FramePosition.CENTRE).end().build();
                Frame3 bucketColumn2 = Frame3.builder("FRM_BUCKET_COLUMN_TWO")
                                .addTheme(THM_BUCKET_COLUMN, ThemePosition.FRAME).end()
                                .addTheme(THM_BUCKET_COLUMN_PADDING, ThemePosition.CENTRE).end()
                                .addFrame(applied, FramePosition.NORTH).end().addFrame(frameCard1, FramePosition.CENTRE)
                                .end().addFrame(frameCard2, FramePosition.CENTRE).end().build();
                Frame3 bucketColumn3 = Frame3.builder("FRM_BUCKET_COLUMN_THREE")
                                .addTheme(THM_BUCKET_COLUMN, ThemePosition.FRAME).end()
                                .addTheme(THM_BUCKET_COLUMN_PADDING, ThemePosition.CENTRE).end()
                                .addFrame(shortlisted, FramePosition.NORTH).end().build();
                Frame3 bucketColumn4 = Frame3.builder("FRM_BUCKET_COLUMN_FOUR")
                                .addTheme(THM_BUCKET_COLUMN, ThemePosition.FRAME).end()
                                .addTheme(THM_BUCKET_COLUMN_PADDING, ThemePosition.CENTRE).end()
                                .addFrame(interviews, FramePosition.NORTH).end()
                                .addFrame(frameCard1, FramePosition.CENTRE).end()
                                .addFrame(frameCard2, FramePosition.CENTRE).end()
                                .addFrame(frameCard3, FramePosition.CENTRE).end()
                                .addFrame(frameCard4, FramePosition.CENTRE).end()
                                .addFrame(frameCard5, FramePosition.CENTRE).end()
                                .addFrame(frameCard6, FramePosition.CENTRE).end()
                                .addFrame(frameCard7, FramePosition.CENTRE).end()
                                .addFrame(frameCard8, FramePosition.CENTRE).end()
                                .addFrame(frameCard9, FramePosition.CENTRE).end()
                                .addFrame(frameCard10, FramePosition.CENTRE).end()
                                .addFrame(frameCard11, FramePosition.CENTRE).end()
                                .addFrame(frameCard12, FramePosition.CENTRE).end()
                                .addFrame(frameCard13, FramePosition.CENTRE).end()
                                .addFrame(frameCard14, FramePosition.CENTRE).end()
                                .addFrame(frameCard15, FramePosition.CENTRE).end()
                                .addFrame(frameCard16, FramePosition.CENTRE).end()
                                .addFrame(frameCard17, FramePosition.CENTRE).end()
                                .addFrame(frameCard18, FramePosition.CENTRE).end()
                                .addFrame(frameCard19, FramePosition.CENTRE).end().build();

                Frame3 bucketColumn5 = Frame3.builder("FRM_BUCKET_COLUMN_FIVE")
                                .addTheme(THM_BUCKET_COLUMN, ThemePosition.FRAME).end()
                                .addTheme(THM_BUCKET_COLUMN_PADDING, ThemePosition.CENTRE).end()
                                .addFrame(offered, FramePosition.NORTH).end().build();
                Frame3 bucketColumn6 = Frame3.builder("FRM_BUCKET_COLUMN_SIX")
                                .addTheme(THM_BUCKET_COLUMN, ThemePosition.FRAME).end()
                                .addTheme(THM_BUCKET_COLUMN_PADDING, ThemePosition.CENTRE).end()
                                .addFrame(placed, FramePosition.NORTH).end().build();
                Frame3 bucketColumn7 = Frame3.builder("FRM_BUCKET_COLUMN_SEVEN")
                                .addTheme(THM_BUCKET_COLUMN, ThemePosition.FRAME).end()
                                .addTheme(THM_BUCKET_COLUMN_PADDING, ThemePosition.CENTRE).end()
                                .addFrame(inProgress, FramePosition.NORTH).end().build();

                Frame3 bucket = Frame3.builder("FRM_BUCKETSSSS").addTheme(THM_BUCKET).end()
                                .addTheme(THM_COLOR_BACKGROUND).end().addFrame(bucketColumn1, FramePosition.WEST).end()
                                .addFrame(bucketColumn2, FramePosition.WEST).end()
                                .addFrame(bucketColumn3, FramePosition.WEST).end()
                                .addFrame(bucketColumn4, FramePosition.WEST).end()
                                .addFrame(bucketColumn5, FramePosition.WEST).end()
                                .addFrame(bucketColumn6, FramePosition.WEST).end()
                                .addFrame(bucketColumn7, FramePosition.WEST).end().build();

                return bucket;
        }

        public Frame3 generateProcessView() {

                try {

                        Theme THM_FORM_INPUT_DEFAULT = Theme.builder("THM_FORM_INPUT_DEFAULT").addAttribute()
                                        .borderBottomWidth(5).color("black").borderColor("#ddd").borderStyle("solid")
                                        .placeholderColor("#888").height(70).end().build();

                        Theme THM_BUCKET_LABEL = Theme.builder("THM_BUCKET_LABEL").addAttribute().textAlign("center")
                                        .bold(true).color("black").end().build();

                        Theme THM_BUCKET = Theme.builder("THM_BUCKET").addAttribute().overflowX("auto")
                                        .overflowY("auto").width("100%").end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                        Theme THM_BUCKET_COLUMN = Theme.builder("THM_BUCKET_COLUMN").addAttribute()
                                        .backgroundColor("#EAEAEA").minWidth(300).width("100%").margin(20)
                                        .textAlign("center").end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();
                        Theme THM_BUCKET_COLUMN_PADDING = Theme.builder("THM_BUCKET_COLUMN_PADDING").addAttribute()
                                        .padding(20).justifyContent("flex-start").end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                        Theme THM_TEST1 = Theme.builder("THM_TEST1").addAttribute().backgroundColor("none")
                                        .width("100%").height(70).flexGrow(0).flexBasis("initial").end().build();

                        Theme THM_BUCKET_HEADER = Theme.builder("THM_BUCKET_HEADER").addAttribute()
                                        .backgroundColor("red").end().build();

                        Frame3 available = Frame3.builder("FRM_GRP_AVAILABLE").addTheme(THM_TEST1).end()
                                        // .addTheme(THM_BUCKET_HEADER).weight(1.0).end()
                                        .question("QUE_GRP_NOT_APPLIED").addTheme(THM_BUCKET_LABEL).end().end().build();

                        Frame3 applied = Frame3.builder("FRM_GRP_APPLIED").addTheme(THM_TEST1).end()
                                        .question("QUE_GRP_APPLIED").addTheme(THM_FORM_INPUT_DEFAULT)
                                        .vcl(VisualControlType.VCL_INPUT).weight(2.0).end().end()
                                        .addTheme(THM_BUCKET_LABEL).end().build();
                        Frame3 shortlisted = Frame3.builder("FRM_GRP_SHORTLISTED").addTheme(THM_TEST1).end()
                                        .question("QUE_GRP_SHORTLISTED").addTheme(THM_FORM_INPUT_DEFAULT)
                                        .vcl(VisualControlType.VCL_INPUT).weight(2.0).end().end()
                                        .addTheme(THM_BUCKET_LABEL).end().build();
                        Frame3 interviews = Frame3.builder("FRM_GRP_INTERVIEWS").addTheme(THM_TEST1).end()
                                        .question("QUE_GRP_INTERVIEWS").addTheme(THM_FORM_INPUT_DEFAULT)
                                        .vcl(VisualControlType.VCL_INPUT).weight(2.0).end().end()
                                        .addTheme(THM_BUCKET_LABEL).end().build();
                        Frame3 offered = Frame3.builder("FRM_GRP_OFFERED").addTheme(THM_TEST1).end()
                                        .question("QUE_GRP_OFFERED").addTheme(THM_FORM_INPUT_DEFAULT)
                                        .vcl(VisualControlType.VCL_INPUT).weight(2.0).end().end()
                                        .addTheme(THM_BUCKET_LABEL).end().build();
                        Frame3 placed = Frame3.builder("FRM_GRP_PLACED").addTheme(THM_TEST1).end()
                                        .question("QUE_GRP_PLACED").addTheme(THM_FORM_INPUT_DEFAULT)
                                        .vcl(VisualControlType.VCL_INPUT).weight(2.0).end().end()
                                        .addTheme(THM_BUCKET_LABEL).end().build();
                        Frame3 inProgress = Frame3.builder("FRM_GRP_IN_PROGRESS").addTheme(THM_TEST1).end()
                                        .question("QUE_GRP_IN_PROGRESS").addTheme(THM_FORM_INPUT_DEFAULT)
                                        .vcl(VisualControlType.VCL_INPUT).weight(2.0).end().end()
                                        .addTheme(THM_BUCKET_LABEL).end().build();

                        Theme THM_GREEN = Theme.builder("THM_GREEN").addAttribute().backgroundColor("green").end()
                                        .build();

                        Theme THM_DISPLAY_HORIZONTAL = Theme.builder("THM_DISPLAY_HORIZONTAL").addAttribute()
                                        .flexDirection("row").end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();
                        Theme THM_DISPLAY_VERTICAL = Theme.builder("THM_DISPLAY_VERTICAL").addAttribute()
                                        .flexDirection("column").end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                        Frame3 frameCard = generateNewCard();
                        Frame3 frameCard1 = generateCards("1");

                        Frame3 bucketColumn1 = Frame3.builder("FRM_BUCKET_COLUMN_ONE")
                                        .addTheme(THM_DISPLAY_VERTICAL, ThemePosition.WRAPPER).end()
                                        .addTheme(THM_BUCKET_COLUMN, ThemePosition.FRAME).end()
                                        .addTheme(THM_BUCKET_COLUMN_PADDING, ThemePosition.CENTRE).end()
                                        .addFrame(available, FramePosition.NORTH).end()
                                        .addFrame(frameCard, FramePosition.CENTRE).end()
                                        .addFrame(frameCard1, FramePosition.CENTRE).end().build();

                        Frame3 bucketColumn2 = Frame3.builder("FRM_BUCKET_COLUMN_TWO")
                                        .addTheme(THM_BUCKET_COLUMN, ThemePosition.WRAPPER).end()
                                        .addTheme(THM_BUCKET_COLUMN_PADDING, ThemePosition.CENTRE).end()
                                        .addFrame(applied, FramePosition.NORTH).end()
                                        .addFrame(frameCard, FramePosition.CENTRE).end().build();
                        Frame3 bucketColumn3 = Frame3.builder("FRM_BUCKET_COLUMN_THREE")
                                        .addTheme(THM_BUCKET_COLUMN, ThemePosition.WRAPPER).end()
                                        .addTheme(THM_BUCKET_COLUMN_PADDING, ThemePosition.CENTRE).end()
                                        .addFrame(shortlisted, FramePosition.NORTH).end().build();
                        Frame3 bucketColumn4 = Frame3.builder("FRM_BUCKET_COLUMN_FOUR")
                                        .addTheme(THM_BUCKET_COLUMN, ThemePosition.WRAPPER).end()
                                        .addTheme(THM_BUCKET_COLUMN_PADDING, ThemePosition.CENTRE).end()
                                        .addFrame(interviews, FramePosition.NORTH).end().build();

                        Frame3 bucketColumn5 = Frame3.builder("FRM_BUCKET_COLUMN_FIVE")
                                        .addTheme(THM_BUCKET_COLUMN, ThemePosition.WRAPPER).end()
                                        .addTheme(THM_BUCKET_COLUMN_PADDING, ThemePosition.CENTRE).end()
                                        .addFrame(offered, FramePosition.NORTH).end().build();
                        Frame3 bucketColumn6 = Frame3.builder("FRM_BUCKET_COLUMN_SIX")
                                        .addTheme(THM_BUCKET_COLUMN, ThemePosition.WRAPPER).end()
                                        .addTheme(THM_BUCKET_COLUMN_PADDING, ThemePosition.CENTRE).end()
                                        .addFrame(placed, FramePosition.NORTH).end().build();
                        Frame3 bucketColumn7 = Frame3.builder("FRM_BUCKET_COLUMN_SEVEN")
                                        .addTheme(THM_BUCKET_COLUMN, ThemePosition.WRAPPER).end()
                                        .addTheme(THM_BUCKET_COLUMN_PADDING, ThemePosition.CENTRE).end()
                                        .addFrame(inProgress, FramePosition.NORTH).end().build();

                        Frame3 bucket = Frame3.builder("FRM_BUCKET").addTheme(THM_BUCKET).end()
                                        .addTheme(THM_DISPLAY_HORIZONTAL).end()
                                        .addFrame(bucketColumn1, FramePosition.WEST).end()
                                        .addFrame(bucketColumn2, FramePosition.WEST).end()
                                        .addFrame(bucketColumn3, FramePosition.WEST).end()
                                        .addFrame(bucketColumn4, FramePosition.WEST).end()
                                        .addFrame(bucketColumn5, FramePosition.WEST).end()
                                        .addFrame(bucketColumn6, FramePosition.WEST).end()
                                        .addFrame(bucketColumn7, FramePosition.WEST).end().build();

                        return bucket;

                } catch (Exception e) {
                        // TODO: handle exception
                }
                return null;
        }

        public Frame3 generateNewCard() {
                GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
                GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User",
                                "service");
                QRules rules = new QRules(eventBusMock, userToken.getToken());
                rules.set("realm", userToken.getRealm());
                rules.setServiceToken(serviceToken.getToken());

                try {

                        /* create themes */

                        Theme THM_CARD_DEFAULT = Theme.builder("THM_CARD_DEFAULT").addAttribute()
                                        .backgroundColor("none").end()
                                        // .addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true).end()
                                        // .addAttribute(ThemeAttributeType.PRI_HAS_INPUT, false).end()
                                        .build();

                        Theme THM_CARD = Theme.builder("THM_CARD")
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().addAttribute()
                                        .marginBottom(20).height(150).flexGrow(0).flexShrink(0).flexBasis("initial")
                                        .backgroundColor("#F8F9FA").width("100%").shadowColor("#000").shadowOpacity(0.4)
                                        .shadowRadius(5).end().addAttribute().shadowOffset().width(0).height(0).end()
                                        .end().build();

                        Theme THM_TEST1 = Theme.builder("THM_TEST1").addAttribute().backgroundColor("none")
                                        .width("100%").height(70).flexGrow(0).flexBasis("initial").end().build();

                        /* new card themes */
                        Theme THM_CARD_TEST = Theme.builder("THM_CARD_TEST").addAttribute().backgroundColor("white")
                                        .height("auto").width("100%").backgroundColor("#F8F9FA").flexBasis("initial")
                                        .flexGrow(0).flexShrink(0).marginBottom(20).shadowColor("#000").shadowOffset()
                                        .height(0).width(0).end().shadowOpacity(0.4).shadowRadius(5).end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();
                        // *
                        Theme THM_FORM_LABEL_DEFAULT = Theme.builder("THM_FORM_LABEL_DEFAULT").addAttribute()
                                        .backgroundColor("white").end().build();

                        Theme THM_CARD_STATUS = Theme.builder("THM_CARD_STATUS").addAttribute().height("100%").end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                        Theme THM_PROFILE_IMAGE = Theme.builder("THM_PROFILE_IMAGE").addAttribute().imageHeight(40)
                                        .imageWidth(40).showName(false).end().build();

                        Theme THM_PANEL_ALIGN_TOP_LEFT = Theme.builder("THM_PANEL_ALIGN_TOP_LEFT").addAttribute()
                                        .alignItems("flex-start").justifyContent("flex-start").end().build();
                        // *
                        Theme THM_CARD_TEST_CENTRE = Theme.builder("THM_CARD_TEST_CENTRE").addAttribute().padding(5)
                                        .end().addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                        Theme THM_WIDTH_DYNAMIC = Theme.builder("THM_WIDTH_DYNAMIC").addAttribute().dynamicWidth(true)
                                        .end().build();

                        Theme THM_WIDTH_100_PERCENT = Theme.builder("THM_WIDTH_100_PERCENT").addAttribute()
                                        .width("100%").end().build();

                        Theme THM_EXPANDABLE = Theme.builder("THM_EXPANDABLE")
                                        .addAttribute(ThemeAttributeType.PRI_IS_EXPANDABLE, true).end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                        Theme THM_JUSTIFY_CONTENT_CENTRE = Theme.builder("THM_JUSTIFY_CONTENT_CENTRE").addAttribute()
                                        .justifyContent("center").end().build();
                        Theme THM_JUSTIFY_CONTENT_FLEX_START = Theme.builder("THM_JUSTIFY_CONTENT_FLEX_START")
                                        .addAttribute().justifyContent("flex-start").end().build();

                        Theme THM_IMAGE_PLACEHOLDER = Theme.builder("THM_IMAGE_PLACEHOLDER").addAttribute()
                                        .name("image").type("icon").color("white").end().build();

                        /* new card frames */

                        Frame3 FRM_CARD_STATUS = Frame3.builder("FRM_CARD_STATUS").question("QUE_CARD_STATUS")
                                        .addTheme(THM_CARD_STATUS).vcl(VisualControlType.VCL_WRAPPER).end().end()
                                        .build();

                        Frame3 FRM_CARD_LEFTS = Frame3.builder("FRM_CARD_LEFTS").question("QUE_IMAGE_URL")
                                        .addTheme(THM_PROFILE_IMAGE).vcl(VisualControlType.INPUT_SELECTED).end()
                                        .addTheme(THM_IMAGE_PLACEHOLDER).vcl(VisualControlType.INPUT_PLACEHOLDER).end()
                                        .end().build();

                        Frame3 FRM_CARD_CENTRES = Frame3.builder("FRM_CARD_CENTRES")
                                        .question("QUE_TEST_CARD_CONTENT_GRP").addTheme(THM_PANEL_ALIGN_TOP_LEFT).end()
                                        .addTheme(THM_CARD_TEST_CENTRE).end().end().build();

                        Frame3 FRM_CARD_RIGHTS = Frame3.builder("FRM_CARD_RIGHTS")
                                        .addTheme(THM_JUSTIFY_CONTENT_FLEX_START, ThemePosition.CENTRE).end()
                                        .question("QUE_TEST_CARD_OPTIONS_GRP")
                                        .addTheme("THM_BACKGROUND_NONE", serviceToken).vcl(VisualControlType.GROUP)
                                        .weight(2.0).end().addTheme("THM_DROPDOWN_BEHAVIOUR_GENNY", serviceToken)
                                        .vcl(VisualControlType.GROUP).weight(2.0).end()
                                        .addTheme("THM_DROPDOWN_PROFILE_BEHAVIOUR_GENNY", serviceToken)
                                        .vcl(VisualControlType.GROUP).weight(1.0).end()
                                        .addTheme("THM_DROPDOWN_HEADER_WRAPPER_GENNY", serviceToken)
                                        .vcl(VisualControlType.GROUP_HEADER_WRAPPER).weight(2.0).end()
                                        .addTheme("THM_DROPDOWN_GROUP_LABEL_GENNY", serviceToken)
                                        .vcl(VisualControlType.GROUP_LABEL).end()
                                        .addTheme("THM_DROPDOWN_CONTENT_WRAPPER_GENNY", serviceToken)
                                        .vcl(VisualControlType.GROUP_CONTENT_WRAPPER).end()
                                        .addTheme("THM_PROJECT_COLOR_SURFACE", serviceToken)
                                        .vcl(VisualControlType.GROUP_CONTENT_WRAPPER).end()
                                        .addTheme("THM_BOX_SHADOW_SM", serviceToken)
                                        .vcl(VisualControlType.GROUP_CONTENT_WRAPPER).end()
                                        .addTheme("THM_DROPDOWN_VCL_GENNY", serviceToken).vcl(VisualControlType.VCL)
                                        .end().end().build();

                        Frame3 FRM_CARD_BOTTOM = Frame3.builder("FRM_CARD_BOTTOM")
                                        .addTheme(THM_PANEL_ALIGN_TOP_LEFT, ThemePosition.CENTRE).end()
                                        .question("QUE_TEST_CARD_BOTTOM_GRP").addTheme(THM_WIDTH_DYNAMIC)
                                        .vcl(VisualControlType.VCL_INPUT).end()
                                        .addTheme("THM_WIDTH_100_PERCENT", serviceToken).vcl(VisualControlType.GROUP)
                                        .end().addTheme(THM_JUSTIFY_CONTENT_CENTRE)
                                        .vcl(VisualControlType.GROUP_CLICKABLE_WRAPPER).end().addTheme(THM_EXPANDABLE)
                                        .end().end().build();

                        Frame3 FRM_CARD_MAIN = Frame3.builder("FRM_CARD_MAIN")
                                        .addFrame(FRM_CARD_LEFTS, FramePosition.WEST).end()
                                        .addFrame(FRM_CARD_CENTRES, FramePosition.CENTRE).end()
                                        .addFrame(FRM_CARD_RIGHTS, FramePosition.EAST).end()
                                        .addFrame(FRM_CARD_BOTTOM, FramePosition.SOUTH).end().build();

                        /* create frames */
                        Frame3 FRM_CARDS = Frame3.builder("FRM_CARDS").addTheme(THM_CARD_TEST, ThemePosition.WRAPPER)
                                        .end().addFrame(FRM_CARD_STATUS, FramePosition.WEST).end()
                                        .addFrame(FRM_CARD_MAIN, FramePosition.CENTRE).end().build();

                        return FRM_CARDS;

                } catch (Exception e) {
                        System.out.println("Error " + e.getLocalizedMessage());
                }
                return null;

        }

        public Frame3 generateCards(String count) {
                GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
                GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User",
                                "service");
                QRules rules = new QRules(eventBusMock, userToken.getToken());
                rules.set("realm", userToken.getRealm());
                rules.setServiceToken(serviceToken.getToken());

                /* create themes */

                Theme THM_FORM_LABEL_DEFAULT = Theme.builder("THM_FORM_LABEL_DEFAULT").addAttribute()
                                .backgroundColor("white").end().build();

                Theme THM_CARD_DEFAULT = Theme.builder("THM_CARD_DEFAULT").addAttribute().backgroundColor("none").end()
                                // .addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true).end()
                                // .addAttribute(ThemeAttributeType.PRI_HAS_INPUT, false).end()
                                .build();

                Theme THM_CARD = Theme.builder("THM_CARD").addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false)
                                .end().addAttribute().marginBottom(20).height(150).flexGrow(0).flexShrink(0)
                                .flexBasis("initial").backgroundColor("#F8F9FA").width("100%").shadowColor("#000")
                                .shadowOpacity(0.4).shadowRadius(5).end().addAttribute().shadowOffset().width(0)
                                .height(0).end().end().build();

                Theme THM_CARD_LEFT = Theme.builder("THM_CARD_LEFT").addAttribute().backgroundColor("#EBECF0").end()
                                .build();
                Theme THM_CARD_CENTRE = Theme.builder("THM_CARD_CENTRE").addAttribute().backgroundColor("#FFFFFF").end()
                                .build();
                Theme THM_CARD_RIGHT = Theme.builder("THM_CARD_RIGHT").addAttribute().backgroundColor("#E1E2E1").end()
                                .build();

                /* create frames for each question */
                Frame3 frameName = Frame3.builder("FRM_NAME").question("QUE_NAME_TWO").addTheme(THM_CARD_DEFAULT)
                                .vcl(VisualControlType.VCL_LABEL).end().end().build();
                Frame3 frameEmail = Frame3.builder("FRM_EMAIL").question("QUE_EMAIL").addTheme(THM_CARD_DEFAULT)
                                .vcl(VisualControlType.VCL_LABEL).end().end().build();
                Frame3 frameMobile = Frame3.builder("FRM_MOBILE").question("QUE_MOBILE").addTheme(THM_CARD_DEFAULT)
                                .vcl(VisualControlType.VCL_LABEL).end().end().build();
                Frame3 frameDob = Frame3.builder("FRM_DOB").question("QUE_DOB").addTheme(THM_CARD_DEFAULT)
                                .vcl(VisualControlType.VCL_LABEL).end().end().build();

                /* build left, center, card frame-card */

                Frame3 frameLeftCard = Frame3.builder("FRM_CARD_LEFT").addTheme(THM_CARD_LEFT).end()
                                .question("QUE_IMAGE").end().build();
                Frame3 frameCentreCard = Frame3.builder("FRM_CARD_CENTRE").addTheme(THM_CARD_CENTRE).end()
                                // .question("QUE_NAME_TWO")
                                .addFrame(frameName, FramePosition.NORTH).end()
                                .addFrame(frameEmail, FramePosition.NORTH).end()
                                .addFrame(frameMobile, FramePosition.NORTH).end()
                                .addFrame(frameDob, FramePosition.NORTH).end()

                                .build();
                Frame3 frameRightCard = Frame3.builder("FRM_CARD_RIGHT").addTheme(THM_CARD_RIGHT).end()
                                .question("QUE_NAME_TWO").end().build();

                Theme THM_TEST1 = Theme.builder("THM_TEST1").addAttribute().backgroundColor("none").width("100%")
                                .height(70).flexGrow(0).flexBasis("initial").end().build();

                /* create frames */
                Frame3 frameCard = Frame3.builder("FRM_CARD" + "_" + count).addTheme(THM_CARD, ThemePosition.WRAPPER)
                                .end().question("QUE_NAME_TWO").addTheme(THM_FORM_LABEL_DEFAULT)
                                .vcl(VisualControlType.VCL_WRAPPER).end().end()
                                .addFrame(frameLeftCard, FramePosition.WEST).end()
                                .addFrame(frameCentreCard, FramePosition.WEST).end()
                                // .addFrame(frameRightCard, FramePosition.WEST).end()
                                .build();

                return frameCard;

        }

        public Frame3 generateTabs() {

                QRules rules = GennyJbpmBaseTest.setupLocalService();
                GennyToken userToken = new GennyToken("userToken", rules.getToken());
                GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
                BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
                BaseEntity project = beUtils.getBaseEntityByCode("PRJ_" + serviceToken.getRealm().toUpperCase());

                try {

                        Theme THM_TABS = Theme.builder("THM_TABS").addAttribute().backgroundColor("green").end()
                                        .addAttribute().flexDirection("column").end().build();

                        Theme THM_TAB_HEADER = Theme.builder("THM_TAB_HEADER").addAttribute().flexDirection("row").end()
                                        .build();

                        Theme THM_TAB_CONTENT = Theme.builder("THM_TAB_CONTENT").addAttribute()
                                        .backgroundColor(project.getValue("PRI_COLOR_BACKGROUND", "#F6F6F6")).end()
                                        .build();

                        Theme THM_TAB = Theme.builder("THM_TAB").addAttribute()
                                        .backgroundColor(project.getValue("PRI_COLOR_PRIMARY_VARIANT_LIGHT", "#395268"))
                                        .flexBasis("auto").color("white").height(40).paddingX(10).end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();
                        Theme THM_TAB_WIDTH = Theme.builder("THM_TAB_WIDTH").addAttribute().width("auto").end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                        Frame3 tab1 = Frame3.builder("FRM_TAB_ONE").addTheme(THM_TAB).end()
                                        .addTheme(THM_TAB_WIDTH, ThemePosition.WRAPPER).end()
                                        .question("QUE_TAB_BUCKET_VIEW").end().build();
                        Frame3 tab2 = Frame3.builder("FRM_TAB_TWO").addTheme(THM_TAB).end()
                                        .addTheme(THM_TAB_WIDTH, ThemePosition.WRAPPER).end()
                                        .question("QUE_TAB_TABLE_VIEW").end().build();
                        Frame3 tab3 = Frame3.builder("FRM_TAB_THREE").addTheme(THM_TAB).end()
                                        .addTheme(THM_TAB_WIDTH, ThemePosition.WRAPPER).end()
                                        .question("QUE_TAB_DETAIL_VIEW").end().build();

                        Theme THM_FRAME_ALIGN_WEST = Theme.builder("THM_FRAME_ALIGN_WEST").addAttribute()
                                        .marginRight("auto").flexGrow(0).flexBasis("initial").end()
                                        .addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end().build();

                        Theme THM_PROJECT_COLOR_PRIMARY_VARIANT_LIGHTSSSS = Theme
                                        .builder("THM_PROJECT_COLOR_PRIMARY_VARIANT_LIGHTSSSS").addAttribute()
                                        .backgroundColor(project.getValue("PRI_COLOR_PRIMARY_VARIANT_LIGHT", "#395268"))
                                        .color(project.getValue("PRI_COLOR_PRIMARY_ON", "#FFFFFF")).end().build();

                        Theme THM_PROJECT_COLOR_DEFAULTSSS = Theme.builder("THM_PROJECT_COLOR_DEFAULTSSS")
                                        .addAttribute()
                                        .backgroundColor(project.getValue("PRI_COLOR_PRIMARY", "#233A4E"))
                                        .color("white").end().build();

                        Frame3 tabHeader = Frame3.builder("FRM_TAB_HEADERSSSS")
                                        .addTheme(THM_PROJECT_COLOR_PRIMARY_VARIANT_LIGHTSSSS, ThemePosition.FRAME)
                                        .end().addTheme(THM_TAB_HEADER, ThemePosition.FRAME).end()
                                        .addTheme(THM_FRAME_ALIGN_WEST, ThemePosition.WEST).end()
                                        .addFrame(tab1, FramePosition.WEST).end().addFrame(tab2, FramePosition.WEST)
                                        .end().addFrame(tab3, FramePosition.WEST).end().build();

                        // Frame3 bucket = generateBucket();
                        Frame3 bucket = generateProcessView();
                        // Frame3 detailView = generateDetailView();

                        Frame3 tabContent = Frame3.builder("FRM_TAB_CONTENT").addTheme(THM_TAB_CONTENT).end()
                                        .addFrame(bucket)
                                        // .addFrame(detailView) /* comment out this to see the detail view */
                                        .end().build();

                        Frame3 tabs = Frame3.builder("FRM_TABS").addTheme("THM_DISPLAY_VERTICAL", serviceToken).end()
                                        .addTheme(THM_PROJECT_COLOR_DEFAULTSSS).end()
                                        .addFrame(tabHeader, FramePosition.NORTH).end()
                                        .addFrame(tabContent, FramePosition.CENTRE).end().build();

                        System.out.println("Generated Tabs Frame");
                        return tabs;

                } catch (Exception e) {
                        // TODO: handle exception
                }

                return null;
        }

        public Frame3 generateDetailView() {

                Theme THM_DETAIL_VIEW = Theme.builder("THM_DETAIL_VIEW").addAttribute().backgroundColor("#E2E1E0")
                                .flexDirection("column").end().build();

                Theme THM_CONTAINER = Theme.builder("THM_CONTAINER").addAttribute().backgroundColor("white")
                                .width("90%").height(300).end().build();

                Theme THM_FORM_LABEL_DEFAULT = Theme.builder("THM_FORM_LABEL_DEFAULT")
                                .addAttribute(ThemeAttributeType.PRI_HAS_LABEL, true).end()
                                .addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_TITLE, true).end()
                                .addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_DESCRIPTION, true).end().build();

                Frame3 FRM_EMAIL = Frame3.builder("FRM_EMAIL").question("QUE_EMAIL").addTheme(THM_FORM_LABEL_DEFAULT)
                                .vcl(VisualControlType.VCL_LABEL).end().end().build();

                Frame3 FRM_NAME = Frame3.builder("FRM_NAME").question("QUE_NAME_TWO").addTheme(THM_FORM_LABEL_DEFAULT)
                                .vcl(VisualControlType.VCL_LABEL).end().end().build();
                Frame3 FRM_DOB = Frame3.builder("FRM_DOB").question("QUE_DOB").end().build();
                Frame3 FRM_MOBILE = Frame3.builder("FRM_MOBILE").question("QUE_MOBILE").end().build();
                Frame3 FRM_IMAGE = Frame3.builder("FRM_IMAGE").question("QUE_IMAGE").end().build();

                Frame3 FRM_DETAIL_VIEW_CONTAINER = Frame3.builder("FRM_DETAIL_VIEW_CONTAINER").addTheme(THM_CONTAINER)
                                .end().addFrame(FRM_NAME, FramePosition.WEST).end()
                                .addFrame(FRM_NAME, FramePosition.WEST).end().addFrame(FRM_EMAIL, FramePosition.WEST)
                                .end().addFrame(FRM_DOB, FramePosition.WEST).end()
                                .addFrame(FRM_MOBILE, FramePosition.WEST).end().build();

                Frame3 FRM_DETAIL_VIEW = Frame3.builder("FRM_DETAIL_VIEW").addTheme(THM_DETAIL_VIEW).end()
                                .question("QUE_NAME_TWO").end().addFrame(FRM_DETAIL_VIEW_CONTAINER, FramePosition.NORTH)
                                .end().build();

                System.out.println("Generated DetailView Frame");
                return FRM_DETAIL_VIEW;

        }

        // @Test
        public void desktopWfTest() {
                GennyToken userToken = GennyJbpmBaseTest.createGennyToken(realm, "user1", "Barry Allan", "user");
                GennyToken serviceToken = GennyJbpmBaseTest.createGennyToken(realm, "service", "Service User",
                                "service");
                QRules qRules = new QRules(eventBusMock, userToken.getToken());
                qRules.set("realm", userToken.getRealm());
                qRules.setServiceToken(serviceToken.getToken());

                System.out.println("session=" + userToken.getSessionCode());
                System.out.println("userToken=" + userToken.getToken());
                System.out.println("serviceToken=" + serviceToken.getToken());

                GennyKieSession gks = null;
                try {
                        Frame3 FRM_DESKTOP = VertxUtils.getObject(serviceToken.getRealm(), "", "FRM_DESKTOP",
                                        Frame3.class, serviceToken.getToken());

                        System.out.print("desktop-frame :: " + FRM_DESKTOP.getCode());

                        // Insert emoji for success here :)

                        gks = GennyKieSession.builder(serviceToken, false).addJbpm("desktop_test.bpmn")
                                        // .addFact("qRules", qRules)
                                        // .addFact("msg", msg)
                                        .addToken(serviceToken).addToken(userToken).build();

                        gks.start();
                        gks.advanceSeconds(5, true);
                        gks.injectSignal("runDesktop", FRM_DESKTOP);

                } finally {
                        gks.close();
                }

        }

        public List<BaseEntity> processThemes(List<Theme> themes, GennyToken gennyToken){
        	
        	
        List<BaseEntity> baseEntityList = new ArrayList<BaseEntity>();
		for (Theme theme : themes) {

                System.out.println("Processing Theme     " + theme.getCode());
                BaseEntity themeBe = null;
                themeBe = theme.getBaseEntity();

                if ((theme != null) && (theme.getAttributes() != null) && (!theme.getAttributes().isEmpty())) {

                    for (ThemeAttribute themeAttribute : theme.getAttributes()) {
                            Attribute attribute = null;

                        //     if (themeAttribute.getCode() == null) {
                        //             System.out.println("themeAttribute code is null");
                        //     } else {
                        //             if (!VertxUtils.cachedEnabled) {
                        //                     attribute = RulesUtils.getAttribute(themeAttribute.getCode(), gennyToken.getToken());
                        //             }
                        //     }

                        //     if (attribute == null) {
                                    attribute = new Attribute(themeAttribute.getCode(), themeAttribute.getCode(),
                                                    new DataType("DTT_THEME"));
                        //     }

                            try {
                                    if (themeBe.containsEntityAttribute(themeAttribute.getCode())) {
                                            EntityAttribute themeEA = themeBe.findEntityAttribute(themeAttribute.getCode()).get();
                                            String existingSetValue = themeEA.getAsString();
                                            JSONObject json = new JSONObject(existingSetValue);
                                            JSONObject merged = new JSONObject(json, JSONObject.getNames(json));
                                            JSONObject jo = themeAttribute.getJsonObject();
                                            for (Object key : jo.names()/* JSONObject.getNames(themeAttribute.getJsonObject()) */) {
                                                    merged.put((String) key, themeAttribute.getJsonObject().get((String) key));
                                            }

                                            themeEA.setValue(merged.toString());
                                            //themeEA.setWeight(weight);
                                    } else {
                                            if (attribute.dataType.getClassName().equals(Boolean.class.getCanonicalName())) {
                                                    themeBe.addAttribute(new EntityAttribute(themeBe, attribute, 1.0,
                                                                    themeAttribute.getValueBoolean()));
                                            } else if (attribute.dataType.getClassName().equals(Double.class.getCanonicalName())) {
                                                    themeBe.addAttribute(new EntityAttribute(themeBe, attribute, 1.0,
                                                                    themeAttribute.getValueDouble()));
                                            } else if (attribute.dataType.getClassName().equals(String.class.getCanonicalName())) {
                                                    themeBe.addAttribute(new EntityAttribute(themeBe, attribute, 1.0,
                                                                    themeAttribute.getValueString()));
                                            } else {
                                                    themeBe.addAttribute(
                                                                    new EntityAttribute(
                                                                                    themeBe, new Attribute(themeAttribute.getCode(),
                                                                                                    themeAttribute.getCode(), new DataType("DTT_THEME")),
                                                                                    1.0, themeAttribute.getJson()));
                                            }
                                    }
                            } catch (BadDataException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                            }

                            
                            baseEntityList.add(themeBe);

                    }
                } 
        	}
			return baseEntityList;
        }

}
