package life.genny.test.FrameBuilderTests;

import org.junit.Test;

import life.genny.qwanda.Context.VisualControlType;
import life.genny.test.Frame3;
import life.genny.test.Theme;
import life.genny.test.ThemeAttributeType;

public class FrameBuilderTest {
	@Test
	public void themeBuilderTest()
	{


		Theme THM_FORM_CONTAINER_DEFAULT = Theme.builder("THM_FORM_CONTAINER_DEFAULT")
				.addAttribute().backgroundColor("white")
					.padding(10)
					.maxWidth(700)
					.width("100%")
					.shadowColor("#000")
					.shadowOpacity(0.4)
					.shadowRadius(5)
					.shadowOffset()
						.width(0)
						.height(0)
						.end()
				.end()
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_TITLE,true).end()
				.addAttribute(ThemeAttributeType.PRI_HPRI_HAS_QUESTION_GRP_DESCRIPTION,true)
					.PRI_HAS_LABEL(true)
					.PRI_HAS_REQUIRED(true)
					.PRI_HAS_ICON(true)
					.end()
				.build();	
		
		
	

	}
	
	@Test
	public void frameBuilderTest()
	{


		Theme THM_FORM_CONTAINER_DEFAULT = Theme.builder("THM_FORM_CONTAINER_DEFAULT")
				.addAttribute().backgroundColor("white")
					.padding(10)
					.maxWidth(700)
					.width("100%")
					.shadowColor("#000")
					.shadowOpacity(0.4)
					.shadowRadius(5)
					.shadowOffset()
						.width(0)
						.height(0)
						.end()
				.end()
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_TITLE,true).end()
				.addAttribute(ThemeAttributeType.PRI_HPRI_HAS_QUESTION_GRP_DESCRIPTION,true)
					.PRI_HAS_LABEL(true)
					.PRI_HAS_REQUIRED(true)
					.PRI_HAS_ICON(true)
					.end()
				.build();	
		
		

	
		Frame3 notes = Frame3.builder("FRM_NOTES")
				.addTheme(THM_FORM_CONTAINER_DEFAULT).end()
				.question("QUE_USER_PROFILE_GRP")
					.addTheme(THM_FORM_CONTAINER_DEFAULT)
						.vcl(VisualControlType.INPUT)
						.weight(2.0)
						.end()
					.end()											
				.build();

	

	}
}
