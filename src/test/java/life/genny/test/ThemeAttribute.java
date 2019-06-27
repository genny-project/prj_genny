package life.genny.test;


import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.concurrent.Immutable;

import org.json.JSONObject;

@Immutable
public final class ThemeAttribute {
	private String code;
	private Optional<String> flexDirection=Optional.empty();
	private Optional<String> justifyContent=Optional.empty();
	private Optional<String> backgroundColor=Optional.empty();
	private Optional<Integer> margin=Optional.empty();
	private Optional<Integer> width=Optional.empty();
	private Optional<Integer> height=Optional.empty();
	private Optional<Integer> maxWidth=Optional.empty();
	private Optional<Integer> padding=Optional.empty();
	private Optional<String> shadowColor=Optional.empty();
	private Optional<String> shadowOpacity=Optional.empty();
	private Optional<Integer> shadowRadius=Optional.empty();
	private Optional<ShadowOffset> shadowOffset=Optional.empty();

	/**
	 * static factory method for builder
	 */
	public static Builder builder() {
		return new ThemeAttribute.Builder();
	}
	
	/**
	 * forces use of the Builder
	 */
	private ThemeAttribute() {
	}
	
	public String getCode() {
		return code;
	}


	/**
	 * @return the flexDirection
	 */
	public String getFlexDirection() {
		return flexDirection.orElse("");
	}

	/**
	 * @return the justifyContent
	 */
	public String getJustifyContent() {
		return justifyContent.orElse("");
	}

	/**
	 * @return the backgroundColor
	 */
	public String getBackgroundColor() {
		return backgroundColor.orElse("");
	}
	



	/**
	 * @return the shadowColor
	 */
	public String getShadowColor() {
		return shadowColor.orElse("");
	}

	/**
	 * @return the shadowOpacity
	 */
	public String getShadowOpacity() {
		return shadowOpacity.orElse("0.0");
	}





	public static class Builder {
		private ThemeAttribute managedInstance = new ThemeAttribute();
		private Theme.Builder parentBuilder;
		private Consumer<ThemeAttribute> callback;


		public Builder() {
		}

		public Builder(Theme.Builder b, Consumer<ThemeAttribute> c, ThemeAttributeType attributeType) {
			managedInstance.code = attributeType.name();
			parentBuilder = b;
			callback = c;
		}


		public Builder flexDirection(String value) {
			managedInstance.flexDirection = Optional.of(value);
			return this;
		}

		public Builder justifyContent(String value) {
			managedInstance.justifyContent = Optional.of(value);
			return this;
		}

		public Builder backgroundColor(String value) { // Accept Spelling errors
			managedInstance.backgroundColor = Optional.of(value);
			return this;
		}

		public Builder backgroundColour(String value) { 
			managedInstance.backgroundColor = Optional.of(value);
			return this;
		}

		public Builder margin(Integer value) {
			managedInstance.margin = Optional.of(value);
			return this;
		}

		public Builder width(Integer value) {
			managedInstance.width = Optional.of(value);
			return this;
		}

		public Builder height(Integer value) {
			managedInstance.height = Optional.of(value);
			return this;
		}

		public Builder maxWidth(Integer value) {
			managedInstance.maxWidth = Optional.of(value);
			return this;
		}

		public Builder padding(Integer value) {
			managedInstance.padding = Optional.of(value);
			return this;
		}

		public Builder shadowRadius(Integer value) {
			managedInstance.shadowRadius = Optional.of(value);
			return this;
		}

		public Builder shadowColor(String value) { // Accept Spelling errors
			managedInstance.shadowColor = Optional.of(value);
			return this;
		}

		public Builder shadowColour(String value) {
			managedInstance.shadowColor = Optional.of(value);
			return this;
		}

		public Builder shadowOpacity(String value) {
			managedInstance.shadowOpacity = Optional.of(value);
			return this;
		}

		
		public ThemeAttribute build() {
			return managedInstance;
		}
		
		/**
		 * more fluent setter for Supplier
		 * @return
		 */
		public ShadowOffset.Builder shadowOffset() {
			Consumer<ShadowOffset> f = obj -> { managedInstance.shadowOffset = Optional.of(obj);};
			return new ShadowOffset.Builder(this, f);
		}



		public Theme.Builder end() {
			callback.accept(managedInstance);
			return parentBuilder;
		}

	}
	
	
	
	
	@Override
	public String toString() {
		return getJson();
	}

	public JSONObject getJsonObject()
	{
		JSONObject json = new JSONObject();
		if (flexDirection.isPresent()) json.put("flexDirection", flexDirection.get());
		if (justifyContent.isPresent()) json.put("justifyContent", justifyContent.get());
		if (backgroundColor.isPresent()) json.put("backgroundColor", backgroundColor.get());
		if (shadowColor.isPresent()) json.put("shadowColor", shadowColor.get());
		if (shadowOpacity.isPresent()) json.put("shadowOpacity", shadowOpacity.get());
		if (width.isPresent()) json.put("width", width.get());
		if (margin.isPresent()) json.put("margin", margin.get());
		if (height.isPresent()) json.put("height", height.get());
		if (maxWidth.isPresent()) json.put("maxWidth", maxWidth.get());
		if (padding.isPresent()) json.put("padding", padding.get());
		if (shadowRadius.isPresent()) json.put("shadowRadius", shadowRadius.get());
		if (shadowOffset.isPresent()) json.put("shadowOffset", shadowOffset.get().getJsonObject());
		
		return json;
	}
	
	public String getJson()
	{
		return getJsonObject().toString();
	}
	
}