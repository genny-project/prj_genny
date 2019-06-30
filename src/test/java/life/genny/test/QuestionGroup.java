package life.genny.test;


import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.concurrent.Immutable;

@Immutable
public class QuestionGroup {

	private String code;
	

	private Set<QuestionTheme> questionThemes = new HashSet<QuestionTheme>();
	private Set<String> themeCodes;

	/**
	 * static factory method for builder
	 */
	public static Builder builder(final String code) {
		return new QuestionGroup.Builder(code);
	}
	
	/**
	 * forces use of the Builder
	 */
	private QuestionGroup() {
	}

	public String getCode() {
		return code;
	}



	public Set<QuestionTheme> getQuestionThemes() {
		return Collections.unmodifiableSet(questionThemes);
	}
	
	public Set<String> getThemeCodes() {
		return Collections.unmodifiableSet(themeCodes);
	}

	/**
	 * more fluent Builder
	 */
	public static class Builder {
		private QuestionGroup managedInstance = new QuestionGroup();
		private Frame3.Builder parentBuilder;
		private Consumer<QuestionGroup> callback;

		
		public Builder(final String code) {
			managedInstance.code = code;
		}

		public Builder(Frame3.Builder b, Consumer<QuestionGroup> c, String code) {
			managedInstance.code = code;
			parentBuilder = b;
			callback = c;
		}

		public Builder(Frame3.Builder b, Consumer<QuestionGroup> c, QuestionGroup questionGroup) {
			managedInstance = questionGroup;
			parentBuilder = b;
			callback = c;
		}

				
		/**
		 * fluent setter for questionThemes in the list
		 * 
		 * @param none
		 * @return
		 */
		public QuestionTheme.Builder addTheme(Theme theme) {
			if (managedInstance.questionThemes == null) {
				managedInstance.questionThemes = new HashSet<QuestionTheme>();
			}
			Consumer<QuestionTheme> f = obj -> { managedInstance.questionThemes.add(obj);};
			return new QuestionTheme.Builder(this, f, theme);
		}



		
		public QuestionGroup build() {
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
	
	
	
}