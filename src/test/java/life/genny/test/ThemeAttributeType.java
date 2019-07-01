package life.genny.test;

public enum ThemeAttributeType {
	PRI_CONTENT("PRI_CONTENT"), PRI_CONTENT_HOVER("PRI_CONTENT_HOVER"), PRI_CONTENT_ACTIVE("PRI_CONTENT_ACTIVE"),
	PRI_CONTENT_DISABLED("PRI_CONTENT_DISABLED"), PRI_CONTENT_CLOSED("PRI_CONTENT_CLOSED"),PRI_CONTENT_ERROR("PRI_CONTENT_ERROR"),
	PRI_IS_INHERITABLE("PRI_IS_INHERITABLE"), PRI_IS_EXPANDABLE("PRI_IS_EXPANDABLE"),
	PRI_HAS_QUESTION_GRP_INPUT("PRI_HAS_QUESTION_GRP_INPUT"), PRI_HAS_LABEL("PRI_HAS_LABEL"),PRI_HAS_QUESTION_GRP_TITLE("PRI_HAS_QUESTION_GRP_TITLE"),
	PRI_HAS_QUESTION_GRP_DESCRIPTION("PRI_HAS_QUESTION_GRP_DESCRIPTION"),AS_REQUIRED("PRI_HAS_REQUIRED"), PRI_HAS_HINT("PRI_HAS_HINT"), PRI_HAS_DESCRIPTION("PRI_HAS_DESCRIPTION"),
	PRI_HAS_ICON("PRI_HAS_ICON"),PRI_HAS_REQUIRED("PRI_HAS_REQUIRED"),

	codeOnly("codeOnly"); // used to pass an existing theme

	private final String name;

	private ThemeAttributeType(String s) {
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