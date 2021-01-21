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
import life.genny.utils.BucketUtils;
import life.genny.utils.BucketUtilsTest;
import life.genny.utils.FrameUtils2;
import life.genny.utils.GennyJbpmBaseTest;
import life.genny.utils.OutputParam;
import life.genny.utils.RulesUtils;
import life.genny.utils.SearchUtilsTest;
import life.genny.utils.TableUtils;
//import life.genny.utils.//TableUtilsTest;
import org.apache.commons.lang3.StringUtils;
import life.genny.utils.VertxUtils;

public class SideBarTest extends GennyJbpmBaseTest {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	public SideBarTest() {
		super(false);
	}

	@Test
	public void testSideBar() {

		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
		BucketUtilsTest bucketUtils = new BucketUtilsTest(beUtils);
		
		System.out.println("running the test for sidebar");
		
		Frame3 FRM_SIDEBAR = getSideBarFame();
		Frame3 FRM_TREE_ITEM_CONTACTS = getContactsTreeFrame();
		
		/*  add the tree item to sidebar */
		FRM_SIDEBAR.getFrames().add(new FrameTuple3(FRM_TREE_ITEM_CONTACTS, FramePosition.NORTH, 1.0));
		
		Set<QDataAskMessage> askMsgs = new HashSet<QDataAskMessage>();
		
		QDataBaseEntityMessage msg = FrameUtils2.toMessage(FRM_SIDEBAR, serviceToken, askMsgs);
		msg.setToken(userToken.getToken());

		/* sending processed beMsg */
		VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg));

		/* sending asks */
		for (QDataAskMessage askMsg : askMsgs) {
			
			askMsg.setToken(userToken.getToken());
			String json = JsonUtils.toJson(askMsg);
			VertxUtils.writeMsg("webcmds", json);

		}

	}

	public Frame3 getSideBarFame() {

		/* build the themes */
		Theme THM_SIDEBAR = Theme.builder("THM_SIDEBAR")
					.addAttribute()
						.backgroundColor("#233A4E")
					.end()
					.build();
		
		Theme THM_SIDEBAR_WIDTH = Theme.builder("THM_SIDEBAR_WIDTH")
							.addAttribute()
								.minWidth(300)
								.width(100)
							.end()
							.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE,false).end()
							.build();

		Theme THM_SCROLL_VERTICAL = Theme.builder("THM_SCROLL_VERTICAL")
					.addAttribute()
						.overflowY("auto")
					.end()
					.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
					.build(); 
		
		/* build frame */
		Frame3 FRM_SIDEBAR = Frame3.builder("FRM_SIDEBAR")
									.addTheme(THM_SIDEBAR).end()
									.addTheme(THM_SIDEBAR_WIDTH).end()
									.addTheme(THM_SCROLL_VERTICAL, ThemePosition.WRAPPER).end()
									.build();
		
		/* return the frame */
		return FRM_SIDEBAR;
	}

	public Frame3 getContactsTreeFrame() {

		/* build the themes */
		
		Theme THM_TREE_ITEM = Theme.builder("THM_TREE_ITEM")
                .addAttribute()
                  .color("white")
                  .height("auto")
                  .flexGrow(0)
                  .flexShrink(0)
                  .flexBasis("auto")
                  .paddingLeft(20)
                  .paddingY(5)
                .end()
                .build();
		
		Theme THM_TREE_GROUP_BEHAVIOUR = Theme.builder("THM_TREE_GROUP_BEHAVIOUR")
		.addAttribute(ThemeAttributeType.PRI_IS_EXPANDABLE, true).end()
		.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, true).end()
		.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_LABEL, true).end()
		.addAttribute(ThemeAttributeType.PRI_IS_QUESTION_GRP_LABEL_CLICKABLE, true).end()
		.addAttribute(ThemeAttributeType.PRI_HAS_CHILD_ASKS, true).end()
		.addAttribute(ThemeAttributeType.PRI_IS_DROPDOWN, false).end()
		.build();

		Theme THM_TREE_GROUP_CLICKABLE_WRAPPER = Theme.builder("THM_TREE_GROUP_CLICKABLE_WRAPPER")
									.addAttribute()
										.width("100%")
										.justifyContent("space-between")
									.end()
									.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, true).end()
									.build();

		Theme THM_TREE_GROUP_CONTENT_WRAPPER = Theme.builder("THM_TREE_GROUP_CONTENT_WRAPPER")
									.addAttribute()
										.paddingLeft(20)
										.end()
									.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, true).end()
									.build();

		Theme THM_TREE_GROUP_WRAPPER = Theme.builder("THM_TREE_GROUP_WRAPPER")
									.addAttribute()
										.width("100%")
										.paddingX(20)
										.end()
									.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, true).end()
									.build();
		
		Theme THM_TREE_GROUP_LABEL = Theme.builder("THM_TREE_GROUP_LABEL")
										.addAttribute()
											.bold(false)
											.size("sm")
										.end()
										.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, true).end()
										.build();
		
//		Theme THM_TREE_GROUP_SECOND_TIER = Theme.builder("THM_TREE_GROUP_LABEL")
//				.addAttribute()
//					.bold(false)
//					.size("sm")
//				.end()
//				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE, false).end()
//				.build();
		
//		Frame3 FRM_TREE_ITEM_INTERNS_GRP = Frame3.builder("FRM_TREE_ITEM_ITEMS_GRP")
//				.addTheme(THM_TREE_ITEM, ThemePosition.WRAPPER).end()
//				.question("QUE_TREE_ITEM_INTERNS_GRP")
//					.addTheme(THM_TREE_GROUP_BEHAVIOUR).end()
//					.addTheme(THM_TREE_GROUP_CLICKABLE_WRAPPER)
//								.vcl(VisualControlType.GROUP_CLICKABLE_WRAPPER)
//					.end()
//					.addTheme(THM_TREE_GROUP_CONTENT_WRAPPER)
//						.vcl(VisualControlType.GROUP_CONTENT_WRAPPER)
//					.end()
//					.addTheme(THM_TREE_GROUP_WRAPPER)
//								.vcl(VisualControlType.GROUP_WRAPPER)
//					.end()
//					.addTheme(THM_TREE_GROUP_LABEL)
//							.vcl(VisualControlType.GROUP_LABEL)
//					.end()
//				.end()
//				.build();
				
		/* build the frame */
		Frame3 FRM_TREE_ITEM_CONTACTS = Frame3.builder("FRM_TREE_ITEM_CONTACTS")
				.addTheme(THM_TREE_ITEM, ThemePosition.WRAPPER).end()
				.question("QUE_TREE_ITEM_CONTACTS_GRP")
					.addTheme(THM_TREE_GROUP_BEHAVIOUR).end()
					.addTheme(THM_TREE_GROUP_CLICKABLE_WRAPPER)
								.vcl(VisualControlType.GROUP_CLICKABLE_WRAPPER)
					.end()
					.addTheme(THM_TREE_GROUP_CONTENT_WRAPPER)
						.vcl(VisualControlType.GROUP_CONTENT_WRAPPER)
					.end()
					.addTheme(THM_TREE_GROUP_WRAPPER)
								.vcl(VisualControlType.GROUP_WRAPPER)
					.end()
					.addTheme(THM_TREE_GROUP_LABEL)
							.vcl(VisualControlType.GROUP_LABEL)
					.end()
				.end()
				.build();

				return FRM_TREE_ITEM_CONTACTS;
	}

}