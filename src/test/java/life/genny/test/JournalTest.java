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
import life.genny.qwanda.message.QBaseMSGAttachment;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.message.QBaseMSGAttachment.AttachmentType;
import life.genny.qwanda.message.QCmdMessage;
import life.genny.qwanda.validation.Validation;
import life.genny.qwanda.validation.ValidationList;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.PDFHelper;
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

//import life.genny.utils.//TableUtilsTest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import life.genny.utils.VertxUtils;

public class JournalTest extends GennyJbpmBaseTest {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	public JournalTest() {
		super(false);
	}

	@Test
	public void generatePdf() {

		QRules rules = GennyJbpmBaseTest.setupLocalService();
		GennyToken userToken = new GennyToken("userToken", rules.getToken());
		GennyToken serviceToken = new GennyToken("PER_SERVICE", rules.getServiceToken());
		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken);

		// URL for the template
		String journalTemplate = "https://raw.githubusercontent.com/genny-project/layouts/2020-05-25-journal-report-update/internmatch-new/document_templates/journal-report.html";

		// GET dummy Journal BE or get a real JNL if in db
		BaseEntity journal = getDummyJournal(beUtils);
		
		// Create context Hash Map 
		HashMap<String, Object> contextMap = new HashMap<String, Object>();
		contextMap.put("JOURNAL", journal);
		
		//List<BaseEntity> journals = getDummyJournals(beUtils);

		// Integer i =2;
		// for (BaseEntity jnl : journals) {
		// 	//Integer i = jnl.getIndex() + 1;
		// 	contextMap.put("JNL_" + i, jnl);
		// 	i++;
		// }
		
		String pdfUrl = PDFHelper.getDownloadablePdfLinkForHtml(journalTemplate, contextMap);

		System.out.println("Journal Pdf URL :: " + pdfUrl);
		
		QCmdMessage cmdMsg = new QCmdMessage("DOWNLOAD_FILE",pdfUrl);
		cmdMsg.setToken(beUtils.getGennyToken().getToken());
		String json = JsonUtils.toJson(cmdMsg);
		VertxUtils.writeMsg("webcmds", JsonUtils.toJson((json)));


	}

	public BaseEntity getDummyJournal(BaseEntityUtils beUtils) {
		BaseEntity be = beUtils.create("JNL_ONE", "Wed 15-04-2020 Kanika");

		List<Answer> answers = new ArrayList<Answer>();
		answers.add(new Answer(be.getCode(), be.getCode(), "PRI_NAME", "JNL_ONE"));
		answers.add(new Answer(be.getCode(), be.getCode(), "PRI_CODE", "JNL_ONE"));
		answers.add(new Answer(be.getCode(), be.getCode(), "PRI_JOURNAL_DATE", "2020-04-15"));
		answers.add(new Answer(be.getCode(), be.getCode(), "PRI_JOURNAL_HOURS", "7.0"));
		answers.add(new Answer(be.getCode(), be.getCode(), "PRI_JOURNAL_TASKS", "Produce a vaccine for COVID19"));
		answers.add(new Answer(be.getCode(), be.getCode(), "PRI_JOURNAL_LEARNING_OUTCOMES",
				"Failed to find a vaccine that didn't kill the test subject."));
		beUtils.saveAnswers(answers);

		BaseEntity JNL_ONE = beUtils.getBaseEntityByCode("JNL_ONE");
		return JNL_ONE;

	}

	public List<BaseEntity> getDummyJournals(BaseEntityUtils beUtils) {
		BaseEntity be1 = beUtils.create("JNL_ONE", "JNL_ONE");
		BaseEntity be2 = beUtils.create("JNL_TWO", "JNL_TWO");

		List<Answer> answers = new ArrayList<Answer>();
		answers.add(new Answer(be1.getCode(), be1.getCode(), "PRI_NAME", "JNL_ONE"));
		answers.add(new Answer(be1.getCode(), be1.getCode(), "PRI_CODE", "JNL_ONE"));
		answers.add(new Answer(be1.getCode(), be1.getCode(), "PRI_JOURNAL_DATE", "2020-04-15"));
		answers.add(new Answer(be1.getCode(), be1.getCode(), "PRI_JOURNAL_HOURS", "7.0"));
		answers.add(new Answer(be1.getCode(), be1.getCode(), "PRI_JOURNAL_TASKS", "Produce a vaccine for COVID19"));
		answers.add(new Answer(be1.getCode(), be1.getCode(), "PRI_JOURNAL_LEARNING_OUTCOMES",
				"Failed to find a vaccine that didn't kill the test subject."));
		beUtils.saveAnswers(answers);

		answers.add(new Answer(be2.getCode(), be2.getCode(), "PRI_NAME", "JNL_ONE"));
		answers.add(new Answer(be2.getCode(), be2.getCode(), "PRI_CODE", "JNL_ONE"));
		answers.add(new Answer(be2.getCode(), be2.getCode(), "PRI_JOURNAL_DATE", "2020-04-16"));
		answers.add(new Answer(be2.getCode(), be2.getCode(), "PRI_JOURNAL_HOURS", "7.6"));
		answers.add(new Answer(be2.getCode(), be2.getCode(), "PRI_JOURNAL_TASKS", "Learn how to turn on a computer, Learn how to turn off a computer, Find the toilet"));
		answers.add(new Answer(be2.getCode(), be2.getCode(), "PRI_JOURNAL_LEARNING_OUTCOMES",
				"I found the toilet. Can turn a computer on and off."));
		beUtils.saveAnswers(answers);

		BaseEntity JNL_ONE = beUtils.getBaseEntityByCode("JNL_ONE");
		BaseEntity JNL_TWO = beUtils.getBaseEntityByCode("JNL_TWO");
		List<BaseEntity> journals = new ArrayList<BaseEntity>();
		journals.add(JNL_ONE);
		journals.add(JNL_TWO);

		return journals;

	}
}