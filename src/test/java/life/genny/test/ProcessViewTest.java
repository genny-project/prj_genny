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

public class ProcessViewTest extends GennyJbpmBaseTest {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

    public ProcessViewTest() {
        super(false);
    }

    //@Test
    public void testProcessView() {

        QRules rules = GennyJbpmBaseTest.setupLocalService();
        GennyToken userToken = new GennyToken("userToken", rules.getToken());
        GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
        BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
        TableUtilsTest tableUtils = new TableUtilsTest(beUtils);
        VertxUtils.cachedEnabled = false;

        try {
            // get the list of bucket searchBEs from the cache
            List<SearchEntity> searchBeList = getBucketSearchBeListFromCache();
            System.out.println("size" + searchBeList.size());

            /* list to collect frames */
            List<Frame3> bucketFrames = new ArrayList<Frame3>();

            /* list to collect baseentity */
            //List<BaseEntity> beList = new ArrayList<BaseEntity>();
            Set<BaseEntity> beList = new HashSet<BaseEntity>();

            /* list to collect the asks */
            Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();

            /* get the templat ask for card */
            Ask templateAsk = tableUtils.getCardTemplate(serviceToken, rules);

            /* get the themes from cache */
            Theme THM_BACKGROUND_NONE = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BACKGROUND_NONE",
                    Theme.class, serviceToken.getToken());
            Theme THM_BACKGROUND_E4E4E4 = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BACKGROUND_E4E4E4",
                    Theme.class, serviceToken.getToken());
            Theme THM_BUCKET_COLUMN = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BUCKET_COLUMN",
                    Theme.class, serviceToken.getToken());
            Theme THM_BUCKET_COLUMN_PADDING = VertxUtils.getObject(serviceToken.getRealm(), "",
                    "THM_BUCKET_COLUMN_PADDING", Theme.class, serviceToken.getToken());
            Theme THM_BUCKET = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BUCKET", Theme.class,
                    serviceToken.getToken());
            Theme THM_BUCKET_WRAPPER = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BUCKET_WRAPPER",
                    Theme.class, serviceToken.getToken());
            Theme THM_JUSTIFY_CONTENT_FLEX_START = VertxUtils.getObject(serviceToken.getRealm(), "",
                    "THM_JUSTIFY_CONTENT_FLEX_START", Theme.class, serviceToken.getToken());
            Theme THM_DISPLAY_HORIZONTAL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_DISPLAY_HORIZONTAL",
                    Theme.class, serviceToken.getToken());

            /* loop through the searchList */
            for (SearchEntity searchBe : searchBeList) {

                String code = searchBe.getCode().split("SBE_")[1];

                /* get the attributes from searchObj */
                Map<String, String> columns = tableUtils.getTableColumns(searchBe);

                // generate bucket header asks from searchBe
                Ask bucketHeaderAsk = tableUtils.getBucketHeaderAsk(searchBe, beList, serviceToken);

                // generate bucket content ask group from searchBe
                Ask bucketContentAsk = tableUtils.generateBucketContentAsk(searchBe, serviceToken);

                // generate bucket footer ask group from searchBe
                Ask bucketFooterAsk = tableUtils.generateBucketFooterAsk(searchBe, serviceToken);

                // fetch the search results
                QDataBaseEntityMessage msg = tableUtils.fetchSearchResults(searchBe, beUtils.getGennyToken());

                /* get the application count */
                long totalResults = msg.getItems().length;

                /* also update the searchBe with the attribute */
                Answer totalAnswer = new Answer(beUtils.getGennyToken().getUserCode(), searchBe.getCode(),
                        "PRI_TOTAL_RESULTS", totalResults + "");
                beUtils.addAnswer(totalAnswer);
                beUtils.updateBaseEntity(searchBe, totalAnswer);

                /* get the applications */
                List<BaseEntity> appList = Arrays.asList(msg.getItems());

                /* add the application to the baseentity list */
                beList.addAll(appList);

                /* convert app to asks */
                List<Ask> appAsksList = tableUtils.generateQuestions(beUtils.getGennyToken(), beUtils, appList, columns,
                        beUtils.getGennyToken().getUserCode());

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
                        .addTheme(THM_JUSTIFY_CONTENT_FLEX_START, ThemePosition.CENTRE).end().build();

                // generate bucket content
                Frame3 frameBucketContent = Frame3.builder("FRM_BUCKET_CONTENT_" + code).build();

                // generate bucket footer
                Frame3 frameBucketFooter = Frame3.builder("FRM_BUCKET_FOOTER_" + code).build();

                // link bucket frames to asks
                BaseEntity frameBucketHeaderBe = FrameUtils2.getBaseEntity(frameBucketHeader, serviceToken);
                BaseEntity frameBucketContentBe = FrameUtils2.getBaseEntity(frameBucketContent, serviceToken);
                BaseEntity frameBucketFooterBe = FrameUtils2.getBaseEntity(frameBucketFooter, serviceToken);

                frameBucketHeaderBe = rules.createVirtualLink(frameBucketHeaderBe, bucketHeaderAsk, "LNK_ASK",
                        "CENTRE");

                frameBucketContentBe = rules.createVirtualLink(frameBucketContentBe,
                        (BaseEntity) THM_JUSTIFY_CONTENT_FLEX_START, "LNK_THEME", "CENTRE", 1.0);

                frameBucketContentBe = rules.createVirtualLink(frameBucketContentBe, bucketContentAsk, "LNK_ASK",
                        "CENTRE");

                frameBucketFooterBe = rules.createVirtualLink(frameBucketFooterBe, bucketFooterAsk, "LNK_ASK",
                        "CENTRE");

                beList.add(frameBucketHeaderBe);
                beList.add(frameBucketContentBe);
                beList.add(frameBucketFooterBe);

                Frame3 frameBucket = Frame3.builder("FRM_BUCKET_" + code).addTheme(THM_BACKGROUND_NONE).end()
                        .addTheme(THM_BACKGROUND_E4E4E4, ThemePosition.WRAPPER).end()
                        .addTheme(THM_BUCKET_COLUMN, ThemePosition.WRAPPER).end()
                        .addTheme(THM_BUCKET_COLUMN_PADDING, ThemePosition.CENTRE).end()
                        .addFrame(frameBucketHeader, FramePosition.NORTH).end()
                        .addFrame(frameBucketContent, FramePosition.CENTRE).end()
                        .addFrame(frameBucketFooter, FramePosition.SOUTH).end().build();

                bucketFrames.add(frameBucket);

            }
            /* add the updated searchbeList to beList */
            beList.addAll(searchBeList);

            // Frame3 FRM_BUCKET_TITLE =
            // Frame3.builder("FRM_BUCKET_TITLE").question("QUE_BUCKET_TITLE").end().build();

            Frame3 FRM_BUCKET_WRAPPER = Frame3.builder("FRM_BUCKET_WRAPPER")
                    .addFrame(bucketFrames.get(0), FramePosition.WEST).end()
                    .addFrame(bucketFrames.get(1), FramePosition.WEST).end()
                    .addFrame(bucketFrames.get(2), FramePosition.WEST).end()
                    .addFrame(bucketFrames.get(3), FramePosition.WEST).end()
                    .addFrame(bucketFrames.get(4), FramePosition.WEST).end()
                    .addFrame(bucketFrames.get(5), FramePosition.WEST).end().addTheme(THM_BUCKET).end()
                    .addTheme(THM_BUCKET_WRAPPER, ThemePosition.WEST).end().build();

            Frame3 FRM_BUCKET_VIEW = Frame3.builder("FRM_BUCKET_VIEW")
                    // .addFrame(FRM_BUCKET_TITLE, FramePosition.NORTH).end()
                    .addFrame(FRM_BUCKET_WRAPPER, FramePosition.CENTRE).end().build();

            Frame3 FRM_TAB_CONTENT = Frame3.builder("FRM_TAB_CONTENT").addFrame(FRM_BUCKET_VIEW, FramePosition.NORTH)
                    .end().build();

            QDataBaseEntityMessage msg = FrameUtils2.toMessage(FRM_TAB_CONTENT, serviceToken, askMsgs);
            rules.publishCmd(msg);
            //VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg));

            /* publish the asks */
            for (QDataAskMessage askMsg : askMsgs) {
                askMsg.setToken(beUtils.getGennyToken().getToken());
                askMsg.setReplace(false);
                rules.publishCmd(askMsg);
                //VertxUtils.writeMsg("webcmds", JsonUtils.toJson(askMsg));
            }
            System.out.println("Sent");

            // publish all the baseentity as well
            QDataBaseEntityMessage appMsg = new QDataBaseEntityMessage(beList.toArray(new BaseEntity[beList.size()]));
            rules.publishCmd(appMsg);
            //VertxUtils.writeMsg("webcmds", JsonUtils.toJson(appMsg));

        } catch (Exception e) {
            System.out.println("Error " + e.getLocalizedMessage());
        }

    }

    @Test
    public void testEmptyProcessView() {

        QRules rules = GennyJbpmBaseTest.setupLocalService();
        GennyToken userToken = new GennyToken("userToken", rules.getToken());
        GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
        BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
        TableUtilsTest tableUtils = new TableUtilsTest(beUtils);

        // try {
        /* list to collect baseentity */
        //List<BaseEntity> beList = new ArrayList<BaseEntity>();
        Set<BaseEntity> beList = new HashSet<BaseEntity>();

        /* list to collect the asks */
        Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();

        // get the list of bucket searchBEs from the cache
        List<SearchEntity> searchBeList = getBucketSearchBeListFromCache();
        System.out.println("size" + searchBeList.size());

        /* add the searchList to beList as well s */
        beList.addAll(searchBeList);

        /* list to collect frames */
        List<Frame3> bucketFrames = new ArrayList<Frame3>();

        /* get the templat ask for card */
        Ask templateAsk = tableUtils.getCardTemplate(serviceToken, rules);

        /* get the themes from cache */
        Theme THM_BACKGROUND_NONE = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BACKGROUND_NONE",
                Theme.class, serviceToken.getToken());
        Theme THM_BACKGROUND_E4E4E4 = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BACKGROUND_E4E4E4",
                Theme.class, serviceToken.getToken());
        Theme THM_BUCKET_COLUMN = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BUCKET_COLUMN", Theme.class,
                serviceToken.getToken());
        Theme THM_BUCKET_COLUMN_PADDING = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BUCKET_COLUMN_PADDING",
                Theme.class, serviceToken.getToken());
        Theme THM_BUCKET = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BUCKET", Theme.class,
                serviceToken.getToken());
        Theme THM_BUCKET_WRAPPER = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BUCKET_WRAPPER", Theme.class,
                serviceToken.getToken());
        Theme THM_JUSTIFY_CONTENT_FLEX_START = VertxUtils.getObject(serviceToken.getRealm(), "",
                "THM_JUSTIFY_CONTENT_FLEX_START", Theme.class, serviceToken.getToken());
        Theme THM_DISPLAY_HORIZONTAL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_DISPLAY_HORIZONTAL",
                Theme.class, serviceToken.getToken());

        /* loop through the searchList */
        for (SearchEntity searchBe : searchBeList) {

            String code = searchBe.getCode().split("SBE_")[1];

            /* get the attributes from searchObj */
            Map<String, String> columns = tableUtils.getTableColumns(searchBe);

            // generate bucket header asks from searchBe
            Ask bucketHeaderAsk = tableUtils.getBucketHeaderAsk(searchBe, beList, serviceToken);

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
                    .addTheme(THM_JUSTIFY_CONTENT_FLEX_START, ThemePosition.CENTRE).end().build();

            // generate bucket content
            Frame3 frameBucketContent = Frame3.builder("FRM_BUCKET_CONTENT_" + code).build();

            // generate bucket footer
            Frame3 frameBucketFooter = Frame3.builder("FRM_BUCKET_FOOTER_" + code).build();

            // link bucket frames to asks
            BaseEntity frameBucketHeaderBe = FrameUtils2.getBaseEntity(frameBucketHeader, serviceToken);
            BaseEntity frameBucketContentBe = FrameUtils2.getBaseEntity(frameBucketContent, serviceToken);
            BaseEntity frameBucketFooterBe = FrameUtils2.getBaseEntity(frameBucketFooter, serviceToken);

            frameBucketHeaderBe = rules.createVirtualLink(frameBucketHeaderBe, bucketHeaderAsk, "LNK_ASK", "CENTRE");

            frameBucketContentBe = rules.createVirtualLink(frameBucketContentBe,
                    (BaseEntity) THM_JUSTIFY_CONTENT_FLEX_START, "LNK_THEME", "CENTRE", 1.0);

            frameBucketContentBe = rules.createVirtualLink(frameBucketContentBe, bucketContentAsk, "LNK_ASK", "CENTRE");

            frameBucketFooterBe = rules.createVirtualLink(frameBucketFooterBe, bucketFooterAsk, "LNK_ASK", "CENTRE");

            beList.add(frameBucketHeaderBe);
            beList.add(frameBucketContentBe);
            beList.add(frameBucketFooterBe);

            Frame3 frameBucket = Frame3.builder("FRM_BUCKET_" + code).addTheme(THM_BACKGROUND_NONE).end()
                    .addTheme(THM_BACKGROUND_E4E4E4, ThemePosition.WRAPPER).end()
                    .addTheme(THM_BUCKET_COLUMN, ThemePosition.WRAPPER).end()
                    .addTheme(THM_BUCKET_COLUMN_PADDING, ThemePosition.CENTRE).end()
                    .addFrame(frameBucketHeader, FramePosition.NORTH).end()
                    .addFrame(frameBucketContent, FramePosition.CENTRE).end()
                    .addFrame(frameBucketFooter, FramePosition.SOUTH).end().build();

            bucketFrames.add(frameBucket);

        }

        Frame3 FRM_BUCKET_WRAPPER = Frame3.builder("FRM_BUCKET_WRAPPER")
                .addFrame(bucketFrames.get(0), FramePosition.WEST).end()
                .addFrame(bucketFrames.get(1), FramePosition.WEST).end()
                .addFrame(bucketFrames.get(2), FramePosition.WEST).end()
                .addFrame(bucketFrames.get(3), FramePosition.WEST).end()
                .addFrame(bucketFrames.get(4), FramePosition.WEST).end()
                .addFrame(bucketFrames.get(5), FramePosition.WEST).end().addTheme(THM_BUCKET).end()
                .addTheme(THM_BUCKET_WRAPPER, ThemePosition.WEST).end().build();

        Frame3 FRM_BUCKET_VIEW = Frame3.builder("FRM_BUCKET_VIEW")
                .addFrame(FRM_BUCKET_WRAPPER, FramePosition.CENTRE).end()
                .build();

        Frame3 FRM_TAB_CONTENT = Frame3.builder("FRM_TAB_CONTENT")
                                .addFrame(FRM_BUCKET_VIEW, FramePosition.NORTH).end()
                                .build();

        QDataBaseEntityMessage msg = FrameUtils2.toMessage(FRM_TAB_CONTENT, serviceToken, askMsgs);
        rules.publishCmd(msg);
        //VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg));

        /* publish the asks */
        for (QDataAskMessage askMsg : askMsgs) {
            askMsg.setToken(beUtils.getGennyToken().getToken());
            askMsg.setReplace(false);
            rules.publishCmd(askMsg);
            //VertxUtils.writeMsg("webcmds", JsonUtils.toJson(askMsg));
        }
        System.out.println("Sent");

        // publish all the baseentity as well
        QDataBaseEntityMessage appMsg = new QDataBaseEntityMessage(beList.toArray(new BaseEntity[beList.size()]));
        rules.publishCmd(appMsg);
        //VertxUtils.writeMsg("webcmds", JsonUtils.toJson(appMsg));

        // } catch (Exception e) {
        // System.out.println("Error " + e.getLocalizedMessage());
        // }

    }

    // @Test
    public void updateCards() {
        QRules rules = GennyJbpmBaseTest.setupLocalService();
        GennyToken userToken = new GennyToken("userToken", rules.getToken());
        GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
        BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
        TableUtilsTest tableUtils = new TableUtilsTest(beUtils);

        /* list to collect baseentity */
        List<BaseEntity> beList = new ArrayList<BaseEntity>();

        /* list to collect the asks */
        Ask bucketContentAsk = null;

        /* get the list of bucket searchBEs from the cache */
        List<SearchEntity> searchBeList = getBucketSearchBeListFromCache();

        /* get the templat ask for card */
        Ask templateAsk = tableUtils.getCardTemplate(serviceToken, rules);

        /* list to collect the asks */
        Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();

        try {

            /* loop through the searchList */
            for (SearchEntity searchBe : searchBeList) {

                /* get the attributes from searchObj */
                Map<String, String> columns = tableUtils.getTableColumns(searchBe);

                /* fetch the search results */
                QDataBaseEntityMessage msg = tableUtils.fetchSearchResults(searchBe, beUtils.getGennyToken());

                /* get the application count */
                long totalResults = msg.getItems().length;

                /* also update the searchBe with the attribute */
                Answer totalAnswer = new Answer(beUtils.getGennyToken().getUserCode(), searchBe.getCode(),
                        "PRI_TOTAL_RESULTS", totalResults + "");
                beUtils.addAnswer(totalAnswer);
                beUtils.updateBaseEntity(searchBe, totalAnswer);

                /* get the applications */
                List<BaseEntity> appList = Arrays.asList(msg.getItems());

                /* add the application to the baseentity list */
                beList.addAll(appList);

                /* convert app to asks */
                List<Ask> appAsksList = tableUtils.generateQuestions(beUtils.getGennyToken(), beUtils, appList, columns,
                        beUtils.getGennyToken().getUserCode());

                /* implement template ask to appAks list */
                List<Ask> askList = implementCardTemplate(appAsksList, templateAsk);

                /* generate bucket content ask group from searchBe */
                bucketContentAsk = tableUtils.generateBucketContentAsk(searchBe, serviceToken);

                /* link bucketContentAsk to application asks */
                bucketContentAsk.setChildAsks(askList.toArray(new Ask[askList.size()]));

                /* add the bucketContentAsk msg to the list */
                askMsgs.add(new QDataAskMessage(bucketContentAsk));

            }

            /* add the updated searchBeList to the beList */
            beList.addAll(searchBeList);

        } catch (Exception e) {
            System.out.println("something went wrong");
        }

        /* publish all the app BE */
        QDataBaseEntityMessage appMsg = new QDataBaseEntityMessage(beList.toArray(new BaseEntity[beList.size()]));
        rules.publishCmd(appMsg);
        //VertxUtils.writeMsg("webcmds", JsonUtils.toJson(appMsg));

        /* publish the asks */
        for (QDataAskMessage askMsg : askMsgs) {
            askMsg.setToken(beUtils.getGennyToken().getToken());
            askMsg.setReplace(true);
            rules.publishCmd(askMsg);
            //VertxUtils.writeMsg("webcmds", JsonUtils.toJson(askMsg));
        }
    }

    public Ask getCardTemplate(GennyToken serviceToken, QRules rules) {

        BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
        TableUtilsTest tableUtils = new TableUtilsTest(beUtils);
        //List<BaseEntity> beList = new ArrayList<BaseEntity>();
        Set<BaseEntity> beList = new HashSet<BaseEntity>();

        try {
            // get the themes from cache
            BaseEntity THM_DISPLAY_VERTICAL = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_DISPLAY_VERTICAL",
                    BaseEntity.class, serviceToken.getToken());

            BaseEntity THM_JUSTIFY_CONTENT_FLEX_START = VertxUtils.getObject(serviceToken.getRealm(), "",
                    "THM_JUSTIFY_CONTENT_FLEX_START", BaseEntity.class, serviceToken.getToken());

            Theme THM_CARD = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_CARD", Theme.class,
                    serviceToken.getToken());

            BaseEntity THM_DISPLAY_HORIZONTAL = Theme.builder("THM_DISPLAY_HORIZONTAL").addAttribute()
                    .flexDirection("row").end().addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
                    .build();

            BaseEntity THM_DROPDOWN_ICON_ALT = VertxUtils.getObject(serviceToken.getRealm(), "",
                    "THM_DROPDOWN_ICON_ALT", BaseEntity.class, serviceToken.getToken());

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

            BaseEntity THM_DROPDOWN_VCL_GENNY = VertxUtils.getObject(serviceToken.getRealm(), "",
                    "THM_DROPDOWN_VCL_GENNY", BaseEntity.class, serviceToken.getToken());

            BaseEntity THM_IMAGE_PLACEHOLDER = VertxUtils.getObject(serviceToken.getRealm(), "",
                    "THM_IMAGE_PLACEHOLDER", BaseEntity.class, serviceToken.getToken());
            BaseEntity THM_HEADER_PROFILE_PICTURE = VertxUtils.getObject(serviceToken.getRealm(), "",
                    "THM_HEADER_PROFILE_PICTURE", BaseEntity.class, serviceToken.getToken());
            BaseEntity THM_BORDER_RADIUS_50 = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_BORDER_RADIUS_50",
                    BaseEntity.class, serviceToken.getToken());
            BaseEntity THM_EXPANDABLE = VertxUtils.getObject(serviceToken.getRealm(), "", "THM_EXPANDABLE",
                    BaseEntity.class, serviceToken.getToken());
            BaseEntity THM_WIDTH_100_PERCENT = VertxUtils.getObject(serviceToken.getRealm(), "",
                    "THM_WIDTH_100_PERCENT", BaseEntity.class, serviceToken.getToken());
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

            // content ask
            Question cardContentQuestion = new Question("QUE_CARD_CONTENT_GRP", "Card Content", questionAttribute,
                    true);
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
            cardRightAsk = rules.createVirtualContext(cardRightAsk, THM_DROPDOWN_PROFILE_BEHAVIOUR_GENNY,
                    ContextType.THEME, VisualControlType.GROUP, 1.0);
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

            Ask[] cardStatusChildAsks = { attributeList.get(0) };
            cardStatusAsk.setChildAsks(cardStatusChildAsks);

            Ask[] cardLeftChildAsks = { attributeList.get(1) };
            cardLeftAsk.setReadonly(true);
            cardLeftAsk.setChildAsks(cardLeftChildAsks);

            Ask[] cardCentreChildAsks = { attributeList.get(2), attributeList.get(3), attributeList.get(4),
                    attributeList.get(5) };
            cardCentreAsk.setReadonly(true);
            cardCentreAsk.setChildAsks(cardCentreChildAsks);

            Ask[] cardBottomChildAsks = { attributeList.get(6), attributeList.get(7), attributeList.get(8) };
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

    public List<SearchEntity> getBucketSearchBeList() {

        List<SearchEntity> bucketSearchBeList = new ArrayList<SearchEntity>();

        SearchEntity SBE_APPLIED_APPLICATIONS = new SearchEntity("SBE_APPLIED_APPLICATIONS", "Available")
                .addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC)
                .addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "APP_%")
                .addFilter("PRI_STATUS", SearchEntity.StringFilter.EQUAL, "APPLIED")
                .addColumn("PRI_STATUS_COLOR", "Status").addColumn("PRI_INTERN_IMAGE_URL", "Image")
                .addColumn("PRI_HOST_COMPANY_NAME", "Host Company")
                .addColumn("PRI_INTERNSHIP_TITLE", "Internship Title").addColumn("PRI_INTERN_NAME", "Name")
                .addColumn("PRI_EDU_PROVIDER_NAME", "Edu Provider").addColumn("PRI_INTERN_EMAIL", "Email")
                .addColumn("PRI_INTERN_MOBILE", "Mobile").addColumn("PRI_INTERN_STUDENT_ID", "Student ID")
                .setPageStart(0).setPageSize(10);

        SearchEntity SBE_SHORTLISTED_APPLICATIONS = new SearchEntity("SBE_SHORTLISTED_APPLICATIONS", "Shortlisted")
                .addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC)
                .addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "APP_%")
                .addFilter("PRI_STATUS", SearchEntity.StringFilter.EQUAL, "SHORTLISTED")
                .addColumn("PRI_STATUS_COLOR", "Status").addColumn("PRI_INTERN_IMAGE_URL", "Image")
                .addColumn("PRI_INTERN_NAME", "Name").addColumn("PRI_INTERNSHIP_TITLE", "Internship Title")
                .addColumn("PRI_HOST_COMPANY_NAME", "Host Company").addColumn("PRI_EDU_PROVIDER_NAME", "Edu Provider")
                .addColumn("PRI_INTERN_EMAIL", "Email").addColumn("PRI_INTERN_MOBILE", "Mobile")
                .addColumn("PRI_INTERN_STUDENT_ID", "Student ID").setPageStart(0).setPageSize(10);

        SearchEntity SBE_INTERVIEWED_APPLICATIONS = new SearchEntity("SBE_INTERVIEWED_APPLICATIONS", "Interviewed")
                .addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC)
                .addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "APP_%")
                .addFilter("PRI_STATUS", SearchEntity.StringFilter.EQUAL, "INTERVIEWED")
                .addColumn("PRI_STATUS_COLOR", "Status").addColumn("PRI_INTERN_IMAGE_URL", "Image")
                .addColumn("PRI_INTERN_NAME", "Name").addColumn("PRI_INTERNSHIP_TITLE", "Internship Title")
                .addColumn("PRI_HOST_COMPANY_NAME", "Host Company").addColumn("PRI_EDU_PROVIDER_NAME", "Edu Provider")
                .addColumn("PRI_INTERN_EMAIL", "Email").addColumn("PRI_INTERN_MOBILE", "Mobile")
                .addColumn("PRI_INTERN_STUDENT_ID", "Student ID").setPageStart(0).setPageSize(10);
        SearchEntity SBE_OFFERED_APPLICATIONS = new SearchEntity("SBE_OFFERED_APPLICATIONS", "Offered")
                .addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC)
                .addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "APP_%")
                .addFilter("PRI_STATUS", SearchEntity.StringFilter.EQUAL, "OFFERED")
                .addColumn("PRI_STATUS_COLOR", "Status").addColumn("PRI_INTERN_IMAGE_URL", "Image")
                .addColumn("PRI_INTERN_NAME", "Name").addColumn("PRI_INTERNSHIP_TITLE", "Internship Title")
                .addColumn("PRI_HOST_COMPANY_NAME", "Host Company").addColumn("PRI_EDU_PROVIDER_NAME", "Edu Provider")
                .addColumn("PRI_INTERN_EMAIL", "Email").addColumn("PRI_INTERN_MOBILE", "Mobile")
                .addColumn("PRI_INTERN_STUDENT_ID", "Student ID").setPageStart(0).setPageSize(10);
        SearchEntity SBE_PLACED_APPLICATIONS = new SearchEntity("SBE_PLACED_APPLICATIONS", "Placed")
                .addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC)
                .addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "APP_%")
                .addFilter("PRI_STATUS", SearchEntity.StringFilter.EQUAL, "PLACED")
                .addColumn("PRI_STATUS_COLOR", "Status").addColumn("PRI_INTERN_IMAGE_URL", "Image")
                .addColumn("PRI_INTERN_NAME", "Name").addColumn("PRI_INTERNSHIP_TITLE", "Internship Title")
                .addColumn("PRI_HOST_COMPANY_NAME", "Host Company").addColumn("PRI_EDU_PROVIDER_NAME", "Edu Provider")
                .addColumn("PRI_INTERN_EMAIL", "Email").addColumn("PRI_INTERN_MOBILE", "Mobile")
                .addColumn("PRI_INTERN_STUDENT_ID", "Student ID").setPageStart(0).setPageSize(10);
        SearchEntity SBE_INPROGRESS_APPLICATIONS = new SearchEntity("SBE_INPROGRESS_APPLICATIONS", "In Progress")
                .addSort("PRI_CREATED", "Created", SearchEntity.Sort.DESC)
                .addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "APP_%")
                .addFilter("PRI_STATUS", SearchEntity.StringFilter.EQUAL, "INPROGRESS")
                .addColumn("PRI_STATUS_COLOR", "Status").addColumn("PRI_INTERN_IMAGE_URL", "Image")
                .addColumn("PRI_INTERN_NAME", "Name").addColumn("PRI_INTERNSHIP_TITLE", "Internship Title")
                .addColumn("PRI_HOST_COMPANY_NAME", "Host Company").addColumn("PRI_EDU_PROVIDER_NAME", "Edu Provider")
                .addColumn("PRI_INTERN_EMAIL", "Email").addColumn("PRI_INTERN_MOBILE", "Mobile")
                .addColumn("PRI_INTERN_STUDENT_ID", "Student ID").setPageStart(0).setPageSize(10);

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

        } catch (Exception e) {

        }
        return bucketSearchBeList;
    }

}
