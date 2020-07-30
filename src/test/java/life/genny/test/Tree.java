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
import life.genny.jbpm.customworkitemhandlers.ShowFrame;


public class Tree extends GennyJbpmBaseTest {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	public Tree() {
		super(false);
	}
	
		@Test
	public void testSearchBe() {

		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);
		BucketUtilsTest bucketUtils = new BucketUtilsTest(beUtils);

		System.out.println("start");

		String sourceCode = "PER_SERVICE";
		String targetCode = "PER_SERVICE";
		
		Attribute questionAttribute = new Attribute("QQQ_QUESTION_GROUP", "link", new DataType(String.class));
		Attribute eventAttribute = new Attribute("PRI_EVENT", "link", new DataType(String.class));
		
		/* sidebar group */
		Question sidebarQues = new Question("QUE_PROJECT_SIDEBAR_GRP", "Sidebar", questionAttribute, true);
		Ask sidebarAsk = new Ask(sidebarQues, sourceCode, targetCode);
		
		Question dashboardQues = new Question("QUE_DASHBOARD", "Dashboard", eventAttribute, true);
		Ask dashboardAsk = new Ask(dashboardQues, sourceCode, targetCode);
		
		Question processViewQues = new Question("QUE_TAB_BUCKET_VIEW", "Process View", eventAttribute, true);
		Ask processViewAsk = new Ask(processViewQues, sourceCode, targetCode);
		
		Question contactsQues = new Question("QUE_TREE_ITEM_CONTACTS_GRP", "Contacts", questionAttribute, true);
		Ask contactsAsk = new Ask(contactsQues, sourceCode, targetCode);
		
		Question internshipsQues = new Question("QUE_TREE_ITEM_INTERNSHIPS_GRP", "Internships", questionAttribute, true);
		Ask internshipsAsk = new Ask(internshipsQues, sourceCode, targetCode);
		
		Question hostCompaniesQues = new Question("QUE_TREE_ITEM_HOST_COMPANIES_GRP", "Host Companies", questionAttribute, true);
		Ask hostCompaniesAsk = new Ask(hostCompaniesQues, sourceCode, targetCode);
		
		Question eduProvidersQues = new Question("QUE_TREE_ITEM_EDU_PROVIDERS_GRP", "Edu Providers", questionAttribute, true);
		Ask eduProvidersAsk = new Ask(eduProvidersQues, sourceCode, targetCode);
		
		Ask[] sidebarItems = { dashboardAsk, processViewAsk };
		sidebarAsk.setChildAsks(sidebarItems);
		
		VertxUtils.putObject(serviceToken.getRealm(), "", "SIDEBAR_AGENT", sidebarAsk, serviceToken.getToken());

		Ask cachedAsk = VertxUtils.getObject(serviceToken.getRealm(), "", "SIDEBAR_AGENT", Ask.class, serviceToken.getToken());
		System.out.println("end");

	}
	

}