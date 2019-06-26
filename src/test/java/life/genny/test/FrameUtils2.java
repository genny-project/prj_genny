package life.genny.test;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Tuple4;
import io.vertx.core.json.JsonObject;
import life.genny.models.GennyToken;
import life.genny.qwanda.Ask;
import life.genny.qwanda.ContextType;
import life.genny.qwanda.Link;
import life.genny.qwanda.Question;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeLink;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.datatype.DataType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.entity.EntityQuestion;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.llama.Frame;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.utils.QuestionUtils;
import life.genny.utils.VertxUtils;

public class FrameUtils2 {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	static public QDataBaseEntityMessage toMessage(final Frame2 rootFrame, GennyToken serviceToken, List<QDataAskMessage> asks) {

		List<BaseEntity> baseEntityList = new ArrayList<BaseEntity>();
		List<Ask> askList = new ArrayList<>();


		BaseEntity root = getBaseEntity(rootFrame, serviceToken);

		log.info(root.toString());

		baseEntityList.add(root);

		// Traverse the frame tree and build BaseEntitys and links
		processFrames(rootFrame, serviceToken, baseEntityList, root, askList);

		QDataBaseEntityMessage msg = new QDataBaseEntityMessage(baseEntityList);
		msg.setTotal(msg.getReturnCount());  // fudge the total.
		msg.setReplace(true);
		
		for (Ask ask : askList) {
			QDataAskMessage askMsg = QuestionUtils.getAsks(serviceToken.getUserCode(), serviceToken.getUserCode(), ask.getQuestionCode(), serviceToken.getToken());
			asks.add(askMsg);
		}
		return msg;
	}

	private static BaseEntity getBaseEntity(final Frame2 rootFrame, final GennyToken serviceToken) {
		return getBaseEntity(rootFrame.getCode(), rootFrame.getName(), serviceToken);

	}

	private static BaseEntity getBaseEntity(final String beCode, final String beName, final GennyToken serviceToken) {
		BaseEntity be = VertxUtils.getObject(serviceToken.getRealm(), "", beCode, BaseEntity.class,
				serviceToken.getToken());
		if (be == null) {
			try {
				be = QwandaUtils.getBaseEntityByCodeWithAttributes(beCode, serviceToken.getToken());
				if (be == null) {
					be = QwandaUtils.createBaseEntityByCode(beCode, beName, GennySettings.qwandaServiceUrl,
							serviceToken.getToken());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		be.setLinks(new HashSet<EntityEntity>()); // clear
		return be;

	}

	private static Question getQuestion(final String questionCode, final GennyToken serviceToken) {
		JsonObject qJson = VertxUtils.readCachedJson(serviceToken.getRealm(),questionCode,serviceToken.getToken());
		Question q = JsonUtils.fromJson(qJson.getString("value"), Question.class);
		if ((q != null)) {
			try {
				String questionStr = QwandaUtils.apiGet(
						GennySettings.qwandaServiceUrl + "/qwanda/questions/" + questionCode, serviceToken.getToken());
				if (questionStr == null) {
					log.error("Question Code :" + questionCode + " does not exist");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return q;

	}

	/**
	 * @param frame
	 * @param gennyToken
	 * @param messages
	 * @param root
	 */
	private static void processFrames(final Frame2 frame, GennyToken serviceToken, List<BaseEntity> baseEntityList,
			BaseEntity parent, List<Ask> askList) {
		


		// Go through the frames and fetch them
		for (Tuple3<Frame2, Frame2.FramePosition, Double> frameTuple3 : frame.getFrames()) {
			System.out.println("Processing Frame     " + frameTuple3._1.getCode());
			Frame2 childFrame = frameTuple3._1;
			Frame2.FramePosition position = frameTuple3._2;
			Double weight = frameTuple3._3;

			childFrame.setParent(parent); // Set the parent sop that we can link the childs themes to it.

			BaseEntity childBe = getBaseEntity(childFrame, serviceToken);



			// link to the parent
			EntityEntity link = null;
			Attribute linkFrame = new AttributeLink("LNK_FRAME", "frame");
			link = new EntityEntity(parent, childBe, linkFrame, position.name(), weight);
			if (!parent.getLinks().contains(link)) {
				parent.getLinks().add(link);
			}
			baseEntityList.add(childBe);

			// Traverse the frame tree and build BaseEntitys and links
			if (!childFrame.getFrames().isEmpty()) {
				processFrames(childFrame, serviceToken, baseEntityList, childBe,askList);
			}

			if (!childFrame.getThemeObjects().isEmpty()) {
				processThemes(childFrame, position, serviceToken, baseEntityList, childBe);
			}

			if (childFrame.getQuestionCode() != null) {
				System.out.println("Processing Question  " + childFrame.getQuestionCode());
				/* create an ask */
					BaseEntity askBe = new BaseEntity(childFrame.getQuestionCode(), childFrame.getQuestionCode());

					Ask ask = QuestionUtils.createQuestionForBaseEntity2(askBe, true, serviceToken);
					Set<EntityQuestion> entityQuestionList = askBe.getQuestions();

					Link linkAsk = new Link(frame.getCode(), childFrame.getQuestionCode(), "LNK_ASK", Frame2.FramePosition.CENTRE.name());
					linkAsk.setWeight(ask.getWeight());
					EntityQuestion ee = new EntityQuestion(linkAsk);
					entityQuestionList.add(ee);

					childBe.setQuestions(entityQuestionList);
					baseEntityList.add(askBe);
					
					askList.add(ask); // add to the ask list

					
			}
			

		}
	}

	/**
	 * @param frame
	 * @param gennyToken
	 * @param messages
	 * @param root
	 */
	private static void processThemes(final Frame2 frame, Frame2.FramePosition position, GennyToken gennyToken,
			List<BaseEntity> baseEntityList, BaseEntity parent) {
		// Go through the theme codes and fetch the
		for (Tuple4<String, Frame2.ThemeAttribute, JSONObject, Double> themeTuple4 : frame.getThemeObjects()) {
			System.out.println("Processing Theme     " + themeTuple4._1);
			String themeCode = themeTuple4._1;
			Frame2.ThemeAttribute themeAttribute = themeTuple4._2;
			if (!themeAttribute.name().equalsIgnoreCase("codeOnly")) {
				JSONObject themeJson = themeTuple4._3;
				Double weight = themeTuple4._4;

				BaseEntity childBe = getBaseEntity(themeCode, themeCode, gennyToken);

				// Attribute attribute = RulesUtils.getAttribute(themeAttribute.name(),
				// gennyToken.getToken());
				Attribute attribute = new Attribute(themeAttribute.name(), themeAttribute.name(),
						new DataType("DTT_THEME"));
				try {
					if (childBe.containsEntityAttribute(themeAttribute.name())) {
						EntityAttribute themeEA = childBe.findEntityAttribute(themeAttribute.name()).get();
						String existingSetValue = themeEA.getAsString();
						JSONObject json = new JSONObject(existingSetValue);
						Iterator<String> keys = themeJson.keys();

						while (keys.hasNext()) {
							String key = keys.next();
							Object value = json.get(key);
							json.put(key, value);
						}

						themeEA.setValue(json.toString());
						themeEA.setWeight(weight);
					} else {
						childBe.addAttribute(new EntityAttribute(childBe, attribute, weight, themeJson.toString()));
					}
				} catch (BadDataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// link to the parent
				EntityEntity link = null;
				Attribute linkFrame = new AttributeLink("LNK_THEME", "theme");
				link = new EntityEntity(frame.getParent(), childBe, linkFrame, position.name(), weight);
				if (!frame.getParent().getLinks().contains(link)) {
					frame.getParent().getLinks().add(link);
				}
				baseEntityList.add(childBe);
				

			}
		}


	}

}
