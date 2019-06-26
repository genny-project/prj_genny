package life.genny.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.google.gson.JsonObject;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Tuple4;
import io.vavr.Tuple5;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.llama.Frame.ThemeAttribute;

/* Llama class implements the frame of base entities 
 */
public class Frame2 extends BaseEntity {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private String questionCode;
	private FramePosition position;
	private BaseEntity parent;
	private List<Tuple4<String, Frame2.ThemeAttribute,  JSONObject, Double>> themeObjects = new ArrayList<Tuple4<String, Frame2.ThemeAttribute, JSONObject, Double>>();

	private List<Tuple3<String, FramePosition, Double>> frameCodes = new ArrayList<Tuple3<String, FramePosition, Double>>();
	private List<Tuple3<Frame2, FramePosition, Double>> frames = new ArrayList<Tuple3<Frame2, FramePosition, Double>>();

	public enum ThemeAttribute {
		PRI_CONTENT("PRI_CONTENT"), PRI_CONTENT_HOVER("PRI_CONTENT_HOVER"), PRI_CONTENT_ACTIVE("PRI_CONTENT_ACTIVE"),
		PRI_CONTENT_DISABLED("PRI_CONTENT_DISABLED"), PRI_CONTENT_CLOSED("PRI_CONTENT_CLOSED"),
		PRI_IS_INHERITABLE("PRI_IS_INHERITABLE"), PRI_IS_EXPANDABLE("PRI_IS_EXPANDABLE"),
		PRI_HAS_QUESTION_GRP_INPUT("PRI_HAS_QUESTION_GRP_INPUT"), PRI_HAS_LABEL("PRI_HAS_LABEL"),
		PRI_HAS_REQUIRED("PRI_HAS_REQUIRED"), PRI_HAS_HINT("PRI_HAS_HINT"), PRI_HAS_DESCRIPTION("PRI_HAS_DESCRIPTION"),
		PRI_HAS_ICON("PRI_HAS_ICON"),

		codeOnly("codeOnly"); // used to pass an existing theme

		private final String name;

		private ThemeAttribute(String s) {
			name = s;
		}

		public boolean equalsName(String otherName) {
			// (otherName == null) check is not needed because name.equals(null) returns
			// false
			return name.equals(otherName);
		}

		public String toString() {
			return this.name;
		}
	}


	public enum FramePosition {
		NORTH("NORTH"), EAST("EAST"), WEST("WEST"), SOUTH("SOUTH"), CENTRE("CENTRE");

		private final String name;

		private FramePosition(String s) {
			name = s;
		}

		public boolean equalsName(String otherName) {
			// (otherName == null) check is not needed because name.equals(null) returns
			// false
			return name.equals(otherName);
		}

		public String toString() {
			return this.name;
		}
	}

	private Frame2() {

	}

	public Frame2(Builder builder) {
		/* Set Root */
		super(builder.code, builder.name);
		this.position = builder.position;

		//
		this.themeObjects = builder.themeObjects;
		this.frames = builder.frames;
		this.questionCode = builder.questionCode;
	}

	// Static class Builder
	public static class Builder {
		Double frameWeight = 1.0; // used to set the order
		Double themeWeight = 1000.0; // themes weight goes backward

		/// instance fields
		private String questionCode = null;
		private String code;
		private String name;
		private FramePosition position;
		List<Tuple4<String, Frame2.ThemeAttribute,  JSONObject, Double>> themeObjects = new ArrayList<Tuple4<String, Frame2.ThemeAttribute, JSONObject, Double>>();;

		List<Tuple3<Frame2, FramePosition, Double>> frames = new ArrayList<Tuple3<Frame2, FramePosition, Double>>();

		
		
		public static Builder newInstance(final String code) {
			return new Builder(code);
		}

		private Builder(final String code) {
			this.code = code;

		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder position(FramePosition position) {
			this.position = position;
			return this;
		}

		public Builder setQuestion(final String questionCode)
		{
			this.questionCode = questionCode;
			return this;
		}
		
		public Builder addTheme(final String themeCode, String property, Object value) {
			return addTheme(themeCode,ThemeAttribute.PRI_CONTENT,property,value);
		}
		
		public Builder addTheme(final String themeCode, Frame2.ThemeAttribute attributeCode, String property, Object value) {
			JSONObject keyValue = new JSONObject();
			
			keyValue.put(property, value);
			
			Tuple4<String, Frame2.ThemeAttribute, JSONObject, Double> theme = Tuple.of(themeCode, attributeCode, keyValue,
					themeWeight);
			themeObjects.add(theme);
			themeWeight = themeWeight - 1.0;
			return this;
		}

		public Builder addTheme(final String themeCode) {
			Frame2.ThemeAttribute codeOnly = Frame2.ThemeAttribute.codeOnly;
			Tuple4<String, Frame2.ThemeAttribute, JSONObject, Double> theme = Tuple.of(themeCode, codeOnly, new JSONObject("{\"codeOnly\":true}"),
					themeWeight);
			themeObjects.add(theme);
			themeWeight = themeWeight - 1.0;
			return this;
		}

		public Builder addFrame(final Frame2 frame, FramePosition position) {
			Tuple3<Frame2, FramePosition, Double> frameTuple = Tuple.of(frame, position, frameWeight);
			frames.add(frameTuple);
			frameWeight = frameWeight + 1.0;
			return this;

		}

		public Builder addFrame(final Frame2 frame) {
			return this.addFrame(frame, Frame2.FramePosition.CENTRE);
		}

		public Builder addFrame(final String frameCode) {
			return this.addFrame(frameCode, Frame2.FramePosition.CENTRE);
		}

		public Builder addFrame(final String frameCode, FramePosition position) {
			Frame2 frame = new Frame2.Builder(frameCode).build();
			Tuple3<Frame2, FramePosition, Double> frameTuple = Tuple.of(frame, Frame2.FramePosition.CENTRE, frameWeight);
			frames.add(frameTuple);
			frameWeight = frameWeight + 1.0;
			return this;
		}

		// build method to deal with outer class
		// to return outer instance
		public Frame2 build() {
			if (StringUtils.isBlank(name)) {
				this.name = this.code;
			}

			if (position == null) {
				position = FramePosition.CENTRE;
			}
			return new Frame2(this);
		}
	}

	/**
	 * @return the position
	 */
	public FramePosition getPosition() {
		return position;
	}

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * @return the frameCodes
	 */
	public List<Tuple3<String, FramePosition, Double>> getFrameCodes() {
		return frameCodes;
	}

	/**
	 * @return the frames
	 */
	public List<Tuple3<Frame2, FramePosition, Double>> getFrames() {
		return frames;
	}

	/**
	 * @return the themeObjects
	 */
	public List<Tuple4<String, Frame2.ThemeAttribute, JSONObject, Double>> getThemeObjects() {
		return themeObjects;
	}

	/**
	 * @return the parent
	 */
	public BaseEntity getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(BaseEntity parent) {
		this.parent = parent;
	}

	/**
	 * @return the questionCode
	 */
	public String getQuestionCode() {
		return questionCode;
	}

	/**
	 * @param questionCode the questionCode to set
	 */
	public void setQuestionCode(String questionCode) {
		this.questionCode = questionCode;
	}

	
	
}
