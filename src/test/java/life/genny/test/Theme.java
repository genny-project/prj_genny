package life.genny.test;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Theme {

	private String code;
	private String name;


	private Set<ThemeAttribute> attributes;

	/**
	 * static factory method for builder
	 */
	public static Builder builder(final String code) {
		return new Theme.Builder(code);
	}
	
	/**
	 * forces use of the Builder
	 */
	private Theme() {
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}


	public Set<ThemeAttribute> getAttributes() {
		return Collections.unmodifiableSet(attributes);
	}

	/**
	 * more fluent Builder
	 */
	public static class Builder {
		private Theme managedInstance = new Theme();
		
		public Builder(final String code) {
			managedInstance.code = code;
		}


		/**
		 * fluent setter for name
		 * 
		 * @param name
		 * @return Theme
		 */
		public Builder name(String name) {
			managedInstance.name = name;
			return this;
		}

	
		/**
		 * fluent setter for attributes in the list
		 * 
		 * @param none
		 * @return
		 */
		public ThemeAttribute.Builder addAttribute() {
			if (managedInstance.attributes == null) {
				managedInstance.attributes = new HashSet<ThemeAttribute>();
			}
			Consumer<ThemeAttribute> f = obj -> { managedInstance.attributes.add(obj);};
			return new ThemeAttribute.Builder(this, f,ThemeAttributeType.PRI_CONTENT);
		}
		
		/**
		 * fluent setter for attributes in the list
		 * 
		 * @param none
		 * @return
		 */
		public ThemeAttribute.Builder addAttribute(ThemeAttributeType attributeType) {
			if (managedInstance.attributes == null) {
				managedInstance.attributes = new HashSet<ThemeAttribute>();
			}
			Consumer<ThemeAttribute> f = obj -> { managedInstance.attributes.add(obj);};
			return new ThemeAttribute.Builder(this, f, attributeType);
		}
		
		public Theme build() {
			return managedInstance;
		}

	}

	@Override
	public String toString() {
		return getCode();
	}
	
	
	
}