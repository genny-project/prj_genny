
package life.genny.utils;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.junit.Test;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.InvalidKeyException;
import io.jsonwebtoken.security.Keys;
import life.genny.qwandautils.SecurityUtils;

public class SecurityTest {
	
@Test
public void jwtTest()
{
	LocalDateTime now = LocalDateTime.now();
	LocalDateTime expiryTime = now.plusSeconds(24*60*60);

	Map<String,Object> adecodedTokenMap = new HashMap<String, Object>();
	try {
		adecodedTokenMap.put("preferred_username", "user1");
		adecodedTokenMap.put("name", "user1".getBytes("UTF-8"));
		adecodedTokenMap.put("realm", "genny");
		adecodedTokenMap.put("azp", "genny");
		adecodedTokenMap.put("aud", "genny");
		adecodedTokenMap.put("realm_access", "[user," + "service" + "]");
		adecodedTokenMap.put("exp", expiryTime.atZone(ZoneId.of("UTC")).toEpochSecond());
		adecodedTokenMap.put("iat", LocalDateTime.now().atZone(ZoneId.of("UTC")).toEpochSecond());
		adecodedTokenMap.put("auth_time", LocalDateTime.now().atZone(ZoneId.of("UTC")).toEpochSecond());
		adecodedTokenMap.put("session_state", UUID.randomUUID().toString().substring(0, 32).getBytes("UTF-8")); // TODO set size ot same
																								// as keycloak
	} catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}



	String jwtToken = null;

	jwtToken = createJwt("ABCD", "Genny", "JWT test", 24*60*60, "secret", adecodedTokenMap);
}
	
public static String createJwt(String id, String issuer, String subject, long ttlMillis, String apiSecret, Map<String,Object> claims) {
	 
	    //The JWT signature algorithm we will be using to sign the token
	    SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
	    String aud = issuer;
	    if (claims.containsKey("aud")) {
	    	aud = (String) claims.get("aud");
	    	claims.remove("aud");
	    }
	    long nowMillis = System.currentTimeMillis();
	    Date now = new Date(nowMillis);
	 
	    //We will sign our JWT with our ApiKey secret
	    byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(apiSecret);
	    
	    Map<String,Object> claims2 = new HashMap<String,Object>();
		claims2.put("preferred_username", (String)claims.get("preferred_username"));
		
				
//		claims2.put("name", name.getBytes("UTF-8"));
//		claims2.put("realm", realm);
//		claims2.put("azp", realm);
//		claims2.put("aud", realm);
//		claims2.put("realm_access", "[user," + role + "]");
//		claims2.put("exp", expiryDateTime.atZone(ZoneId.of("UTC")).toEpochSecond());
//		claims2.put("iat", LocalDateTime.now().atZone(ZoneId.of("UTC")).toEpochSecond());
//		claims2.put("auth_time", LocalDateTime.now().atZone(ZoneId.of("UTC")).toEpochSecond());
//		claims2.put("session_state", UUID.randomUUID().toString().substring(0, 32).getBytes("UTF-8")); // TODO set size ot same

	    
//	    SecretKey key = MacProvider.generateKey(SignatureAlgorithm.HS256);
//	    String base64Encoded = TextCodec.BASE64.encode(key.getEncoded());
	 
	    //Let's set the JWT Claims
	    JwtBuilder builder = Jwts.builder().setId(id)
	                                .setIssuedAt(now)
	                                .setSubject(subject)
	                                .setIssuer(issuer)
	                                .setAudience(aud)
	                                .setClaims(claims2);
	                           //     .signWith(signatureAlgorithm, signingKey);
	                                
	    
		Key key = null;
		
		try {
			key = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
		//	key  = Keys.secretKeyFor(SignatureAlgorithm.HS256);
			builder.signWith(SignatureAlgorithm.HS256, key);
		} catch (Exception e) {
			try {
				key = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
				builder.signWith(SignatureAlgorithm.HS256,key);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				try {
					Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
					builder.signWith(signatureAlgorithm, signingKey);
				} catch (InvalidKeyException e2) {
					//log.error("Cannot creating key foor JWT");
				}
			}
		}

	 
	    //if it has been specified, let's add the expiration
	    if (ttlMillis >= 0) {
	    long expMillis = nowMillis + ttlMillis;
	        Date exp = new Date(expMillis);
	        builder.setExpiration(exp);
	    }
	 
	    //Builds the JWT and serializes it to a compact, URL-safe string
	    return builder.compact();
	}
}
