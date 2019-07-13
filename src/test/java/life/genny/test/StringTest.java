package life.genny.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import io.vertx.core.json.JsonObject;

public class StringTest {
	@Test
	public void stringTest()
	{
		String input = "\"{\\n  \\\"baseEntityAttributes\\\": [],\n  \\\"links\\\": [],\\\n  \\\"questions\": [],\\\n  \\\"code\\\": \\\"PER_USER1\\\",\\\n  \\\"index\\\": 0,\\\n  \\\"created\\\": \\\"2019-07-13T22:33:32.502\\\",\\\n  \\\"name\\\": \\\"\\\",\\\n  \\\"realm\\\": \\\"internmatch\\\"\\\n}\"";

		String resultStr2 = input.replaceAll(Pattern.quote("\\\""), Matcher.quoteReplacement("\""));
		String resultStr3 = resultStr2.replaceAll(Pattern.quote("\\n"), Matcher.quoteReplacement("\n"));
		String resultStr4 = resultStr3.replaceAll(Pattern.quote("\\\n"), Matcher.quoteReplacement("\n"));
		String resultStr5 = resultStr4.replaceAll(Pattern.quote("\"{"), Matcher.quoteReplacement("{"));
		String resultStr6 = resultStr5.replaceAll(Pattern.quote("}\""), Matcher.quoteReplacement("}"));
		JsonObject rs2 = new JsonObject(resultStr6);
		System.out.println(rs2);
	
	}
}
