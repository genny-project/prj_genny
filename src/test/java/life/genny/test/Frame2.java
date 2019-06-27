package life.genny.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Tuple4;
import life.genny.qwanda.entity.BaseEntity;

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
	private List<Tuple4<String, ThemeAttributeType,  JSONObject, Double>> themeObjects = new ArrayList<Tuple4<String, ThemeAttributeType, JSONObject, Double>>();
	private List<Tuple2<Theme,Double>> themes = new ArrayList<Tuple2<Theme,Double>>();
	
	private List<Tuple3<String, FramePosition, Double>> frameCodes = new ArrayList<Tuple3<String, FramePosition, Double>>();
	private List<Tuple3<Frame2, FramePosition, Double>> frames = new ArrayList<Tuple3<Frame2, FramePosition, Double>>();




	private Frame2() {

	}

	public Frame2(Builder builder) {
		/* Set Root */
		super(builder.code, builder.name);
		this.position = builder.position;

		//
		this.themeObjects = builder.themeObjects;
		this.themes = builder.themes;
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
		List<Tuple4<String, ThemeAttributeType,  JSONObject, Double>> themeObjects = new ArrayList<Tuple4<String, ThemeAttributeType, JSONObject, Double>>();;
		private List<Tuple2<Theme,Double>> themes = new ArrayList<Tuple2<Theme,Double>>();

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
	
		public Builder addTheme(final Theme theme) {
			themes.add(Tuple.of(theme,themeWeight));
			themeWeight = themeWeight - 1.0;
			
			return this;
		}

		
		public Builder addTheme(final String themeCode, String property, Object value) {
			return addTheme(themeCode,ThemeAttributeType.PRI_CONTENT,property,value);
		}
		
		public Builder addTheme(final String themeCode, ThemeAttributeType attributeCode, String property, Object value) {
			JSONObject keyValue = new JSONObject();
			
			keyValue.put(property, value);
			
			Tuple4<String, ThemeAttributeType, JSONObject, Double> theme = Tuple.of(themeCode, attributeCode, keyValue,
					themeWeight);
			themeObjects.add(theme);
			themeWeight = themeWeight - 1.0;
			return this;
		}

		public Builder addTheme(final String themeCode) {
			ThemeAttributeType codeOnly = ThemeAttributeType.codeOnly;
			Tuple4<String, ThemeAttributeType, JSONObject, Double> theme = Tuple.of(themeCode, codeOnly, new JSONObject("{\"codeOnly\":true}"),
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
			return this.addFrame(frame, FramePosition.CENTRE);
		}

		public Builder addFrame(final String frameCode) {
			return this.addFrame(frameCode, FramePosition.CENTRE);
		}

		public Builder addFrame(final String frameCode, FramePosition position) {
			Frame2 frame = new Frame2.Builder(frameCode).build();
			Tuple3<Frame2, FramePosition, Double> frameTuple = Tuple.of(frame, FramePosition.CENTRE, frameWeight);
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
	public List<Tuple4<String, ThemeAttributeType, JSONObject, Double>> getThemeObjects() {
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

	/**
	 * @return the themes
	 */
	public List<Tuple2<Theme, Double>> getThemes() {
		return themes;
	}

	/**
	 * @param themes the themes to set
	 */
	public void setThemes(List<Tuple2<Theme, Double>> themes) {
		this.themes = themes;
	}

	
	
}
