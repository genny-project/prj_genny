package life.genny.test;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.concurrent.Immutable;

import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeBoolean;
import life.genny.qwanda.datatype.DataType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.exception.BadDataException;

@Immutable
public class Theme extends BaseEntity {

	private String code;
	private String name;
	private Boolean directLink = false;

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
		super();
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	
	

	/**
	 * @return the directLink
	 */
	public Boolean getDirectLink() {
		return directLink;
	}

	/**
	 * @param directLink the directLink to set
	 */
	public void setDirectLink(Boolean directLink) {
		this.directLink = directLink;
	}

	public Set<ThemeAttribute> getAttributes() {
		return Collections.unmodifiableSet(attributes);
	}

	/**
	 * more fluent Builder
	 */
	public static class Builder {
		private Theme managedInstance = new Theme();
		private Frame3.Builder parentBuilder;
		private Consumer<Theme> callback;

		
		public Builder(final String code) {
			managedInstance.code = code;
			managedInstance.name = code;
		}


		public Builder(Frame3.Builder b, Consumer<Theme> c, String code) {
			managedInstance.code = code;
			managedInstance.name = code;
			parentBuilder = b;
			callback = c;
		}

		public Builder(Frame3.Builder b, Consumer<Theme> c, Theme theme) {
			managedInstance = theme;
			parentBuilder = b;
			callback = c;
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
		
		/**
		 * fluent setter for attributes in the list
		 * 
		 * @param none
		 * @return
		 */
		public ThemeAttribute.Builder addAttribute(ThemeAttributeType attributeType, Boolean value) {
			if (managedInstance.attributes == null) {
				managedInstance.attributes = new HashSet<ThemeAttribute>();
			}
			Consumer<ThemeAttribute> f = obj -> { managedInstance.attributes.add(obj);};
			Attribute attribute = new AttributeBoolean(attributeType.name(), attributeType.name());
			try {
				managedInstance.addAttribute(attribute, 1.0, value);
			} catch (BadDataException e) {

			}
			return new ThemeAttribute.Builder(this, f, attributeType);
			
		}

		/**
		 * fluent setter for attributes in the list
		 * 
		 * @param none
		 * @return
		 */
		public ThemeAttribute.Builder addAttribute(ThemeAttributeType attributeType, String value) {
			if (managedInstance.attributes == null) {
				managedInstance.attributes = new HashSet<ThemeAttribute>();
			}
			Consumer<ThemeAttribute> f = obj -> { managedInstance.attributes.add(obj);};
			return new ThemeAttribute.Builder(this, f, attributeType, value);
		}
		
		/**
		 * fluent setter for attributes in the list
		 * 
		 * @param none
		 * @return
		 */
		public ThemeAttribute.Builder addAttribute(ThemeAttributeType attributeType, Integer value) {
			if (managedInstance.attributes == null) {
				managedInstance.attributes = new HashSet<ThemeAttribute>();
			}
			Consumer<ThemeAttribute> f = obj -> { managedInstance.attributes.add(obj);};
			return new ThemeAttribute.Builder(this, f, attributeType, value);
		}
		
		/**
		 * fluent setter for attributes in the list
		 * 
		 * @param none
		 * @return
		 */
		public ThemeAttribute.Builder addAttribute(ThemeAttributeType attributeType, Double value) {
			if (managedInstance.attributes == null) {
				managedInstance.attributes = new HashSet<ThemeAttribute>();
			}
			Consumer<ThemeAttribute> f = obj -> { managedInstance.attributes.add(obj);};
			return new ThemeAttribute.Builder(this, f, attributeType, value);
		}
		
		public Theme build() {
			return managedInstance;
		}
		
		public Frame3.Builder end() {
			callback.accept(managedInstance);
			return parentBuilder;
		}


	}

	@Override
	public String toString() {
		return getCode();
	}
	
	public BaseEntity getBaseEntity()
	{
		// ugly
		BaseEntity copy = new BaseEntity(this.getCode(),this.getName());
		copy.setRealm(this.getRealm());
		copy.getBaseEntityAttributes().addAll(this.getBaseEntityAttributes());
		
		return copy;
	}
	
}