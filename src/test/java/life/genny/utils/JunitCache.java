package life.genny.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.lang.invoke.MethodHandles;

import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import life.genny.eventbus.WildflyCacheInterface;
import life.genny.models.GennyToken;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import io.vertx.core.json.JsonObject;
import life.genny.qwandautils.QwandaUtils;

//@ApplicationScoped
public class JunitCache implements WildflyCacheInterface {

	/**
	 * Stores logger object.
	 */
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	public JunitCache() {

	}

	@Override
	public Object readCache(String realm, String key, String token) {
		// log.info("WildflyCache read:"+realm+":"+key);
		String askMsgs2Str = "";
		String ret= new String();
		GennyToken userToken = new GennyToken(token);
		try {
			askMsgs2Str = QwandaUtils.apiGet(GennySettings.ddtUrl + "/read/" + userToken.getRealm() + "/" + key,
					userToken.getToken());
			askMsgs2Str = askMsgs2Str.replaceAll(token, "");

			String jsonStr = VertxUtils.fixJson(askMsgs2Str);
			Pattern p = Pattern.compile(".*value\\\":\\\"(.*)\\\"\\}");

			Matcher m = p.matcher(askMsgs2Str);
			String value = "";
			if (m.find()) {
				value = m.group(1
						);

			}
			value = VertxUtils.fixJson(value);
		//	com.google.gson.JsonObject json = new JsonParser().parse(value).getAsJsonObject();
			// JsonObject json = new JsonObject(jsonStr);
			// JsonObject json = JsonUtils.fromJson(jsonStr, JsonObject.class);
			// JsonArray ja = json.getAsJsonArray("value");
			ret = value; // ja.getAsString(); // TODO - assumes always works.....not always case
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Object ret = JsonUtils.fromJson(askMsgs2Str, Object.class);

		return (Object)ret;
	}

	@Override
	public void writeCache(String realm, String key, String value, String token, long ttl_seconds) {
		synchronized (this) {
			String askMsgs2Str = "";
			GennyToken userToken = new GennyToken(token);
			try {
				askMsgs2Str = QwandaUtils.apiPostEntity(GennySettings.ddtUrl + "/write/", value, userToken.getToken());
				JsonObject json = new JsonObject(askMsgs2Str);
				askMsgs2Str = json.getString("value"); // TODO - assumes always works.....not always case
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Object ret = JsonUtils.fromJson(askMsgs2Str, Object.class);
		}
	}

	@Override
	public void clear(String realm) {
//		inDb.getMapBaseEntitys(realm).clear();
	}

}
