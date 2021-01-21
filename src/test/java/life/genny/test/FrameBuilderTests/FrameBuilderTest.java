package life.genny.test.FrameBuilderTests;

import org.junit.Test;

import life.genny.models.Frame3;
import life.genny.models.GennyToken;
import life.genny.models.Theme;
import life.genny.models.ThemeAttributeType;
import life.genny.qwanda.VisualControlType;
import life.genny.utils.GennyJbpmBaseTest;

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
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_DESCRIPTION,true).end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE,false).end()

				.build();	
		
		System.out.print(THM_FORM_CONTAINER_DEFAULT);
		
		
	

	}
	
	//@Test
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
				.addAttribute(ThemeAttributeType.PRI_HAS_QUESTION_GRP_DESCRIPTION,true).end()
				.addAttribute(ThemeAttributeType.PRI_HAS_LABEL,true).end()
				.addAttribute(ThemeAttributeType.PRI_IS_INHERITABLE,false).end()
				.build();	
		
		

	
		Frame3 notes = Frame3.builder("FRM_NOTES")
				.addTheme(THM_FORM_CONTAINER_DEFAULT).end()
				.question("QUE_USER_PROFILE_GRP")
					.addTheme(THM_FORM_CONTAINER_DEFAULT)
						.vcl(VisualControlType.VCL_INPUT)
						.weight(2.0)
						.end()
					.end()											
				.build();

	

	}
}
