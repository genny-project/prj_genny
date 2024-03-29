package life.genny.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import io.vavr.Tuple2;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import life.genny.models.BaseEntityImport;
import life.genny.qwanda.Answer;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeBoolean;
import life.genny.qwanda.attribute.AttributeDate;
import life.genny.qwanda.attribute.AttributeDateTime;
import life.genny.qwanda.attribute.AttributeDouble;
import life.genny.qwanda.attribute.AttributeLong;
import life.genny.qwanda.attribute.AttributeText;
import life.genny.qwanda.converter.ValidationListConverter;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.Person;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.message.QBulkMessage;
import life.genny.qwanda.message.QDataAnswerMessage;
import life.genny.qwanda.message.QDataB2BMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.validation.Validation;
import life.genny.qwandautils.JsonUtils;

public class JsonTest {
	
@Test
public void b2bJsonTest()
{
	String b2bStr = "{\"msg_type\":\"DATA_MSG\",\"option\":\"EXEC\",\"aliasCode\":\"STATELESS\",\"data_type\":\"GennyItem\",\"delete\":false,\"replace\":false,\"items\":[{\"b2bdata\":[{\"attributeCode\":\"PRI_FIRSTNAME\",\"value\":\"Kevin\"},{\"attributeCode\":\"PRI_MOBILE\",\"value\":\"0434321230\"},{\"attributeCode\":\"PRI_EMAIL\",\"value\":\"adamcrow63+km@gmail.com\"},{\"attributeCode\":\"PRI_LASTNAME\",\"value\":\"Murray\"},{\"attributeCode\":\"PRI_USERNAME\",\"value\":\"testuser@gada.io\"},{\"attributeCode\":\"PRI_USERCODE\",\"value\":\"PER_086CDF1F-A98F-4E73-9825-0A4CFE2BB943\"}]}]}";
	  QDataB2BMessage dataB2BMsg = null;
      try {
      	Jsonb jsonb = JsonbBuilder.create();
          dataB2BMsg = jsonb.fromJson(b2bStr, QDataB2BMessage.class);
          System.out.println(dataB2BMsg);
      } catch (com.google.gson.JsonSyntaxException e) {
      }
}
	
@Test
public void stringTest()
{
	String str = "GANGODAGE";
	str = str.toLowerCase();
	String strc = StringUtils.capitalize(str);
	//if (strc.startsWith(prefix))
	System.out.println(strc);
}
	

@Test
public void jsonTest()
{
	ValidationListConverter vc = new ValidationListConverter();
	List<Validation> validationList = vc.convertToEntityAttribute("\"VLD_EMAIL\",\"Email\",\"^(\\w[-._+\\w]*\\w@\\w[-._\\w]*\\w\\.\\w{2,20})$\",\"[\"\"]\",\"FALSE\",\"FALSE\"");
	AttributeText attributeText1 = new AttributeText(AttributeText.getDefaultCodePrefix()+"TEST1","Test 1");
	
	Attribute attributeDouble = new AttributeDouble(AttributeDouble.getDefaultCodePrefix()+"TEST4","Test Double 4");
	Attribute attributeLong = new AttributeLong(AttributeLong.getDefaultCodePrefix()+"TEST5","Test Long 5");	
	Attribute attributeBoolean = new AttributeBoolean(AttributeBoolean.getDefaultCodePrefix()+"TEST6","Test Boolean 6");	
	Attribute attributeDateTime = new AttributeDateTime(AttributeDateTime.getDefaultCodePrefix()+"TEST7","Test DateTiume 7");	
	Attribute attributeDate = new AttributeDate(AttributeDate.getDefaultCodePrefix()+"TEST8","Test Date 8");	
	
	
	BaseEntity person = new Person("PER_BARRY_ALLEN","Barry Allen");
	BaseEntity person2 = new Person("PER_CLARK_KENT","Clark Kent");
	
	try {
		person.addAttribute(attributeText1, 1.0);
		person.addAttribute(attributeDouble, 1.0);
		person.addAttribute(attributeDateTime, 1.0);
	} catch (BadDataException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	try {
		person2.addAttribute(attributeText1, 1.0);
		person2.addAttribute(attributeDouble, 1.0);
		person2.addAttribute(attributeDateTime, 1.0);
	} catch (BadDataException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	QDataBaseEntityMessage beMsg1 = new QDataBaseEntityMessage(person);
	beMsg1.setAliasCode("USER");
	
	QDataBaseEntityMessage beMsg2 = new QDataBaseEntityMessage(person2);
	beMsg2.setAliasCode("SUPERVISOR");
	
	QBulkMessage msg = new QBulkMessage();
	msg.add(beMsg1);
	msg.add(beMsg2);
	
	String json = JsonUtils.toJson(msg);
	System.out.println(json);
}

//@Test
public void AddressTest()
{
	
	
	
	String addressJsonStr =  "{\n" + 
			"   \"results\" : [\n" + 
			"      {\n" + 
			"         \"address_components\" : [\n" + 
			"            {\n" + 
			"               \"long_name\" : \"1600\",\n" + 
			"               \"short_name\" : \"1600\",\n" + 
			"               \"types\" : [ \"street_number\" ]\n" + 
			"            },\n" + 
			"            {\n" + 
			"               \"long_name\" : \"Amphitheatre Pkwy\",\n" + 
			"               \"short_name\" : \"Amphitheatre Pkwy\",\n" + 
			"               \"types\" : [ \"route\" ]\n" + 
			"            },\n" + 
			"            {\n" + 
			"               \"long_name\" : \"Mountain View\",\n" + 
			"               \"short_name\" : \"Mountain View\",\n" + 
			"               \"types\" : [ \"locality\", \"political\" ]\n" + 
			"            },\n" + 
			"            {\n" + 
			"               \"long_name\" : \"Santa Clara County\",\n" + 
			"               \"short_name\" : \"Santa Clara County\",\n" + 
			"               \"types\" : [ \"administrative_area_level_2\", \"political\" ]\n" + 
			"            },\n" + 
			"            {\n" + 
			"               \"long_name\" : \"California\",\n" + 
			"               \"short_name\" : \"CA\",\n" + 
			"               \"types\" : [ \"administrative_area_level_1\", \"political\" ]\n" + 
			"            },\n" + 
			"            {\n" + 
			"               \"long_name\" : \"United States\",\n" + 
			"               \"short_name\" : \"US\",\n" + 
			"               \"types\" : [ \"country\", \"political\" ]\n" + 
			"            },\n" + 
			"            {\n" + 
			"               \"long_name\" : \"94043\",\n" + 
			"               \"short_name\" : \"94043\",\n" + 
			"               \"types\" : [ \"postal_code\" ]\n" + 
			"            }\n" + 
			"         ],\n" + 
			"         \"formatted_address\" : \"1600 Amphitheatre Parkway, Mountain View, CA 94043, USA\",\n" + 
			"         \"geometry\" : {\n" + 
			"            \"location\" : {\n" + 
			"               \"lat\" : 37.4224764,\n" + 
			"               \"lng\" : -122.0842499\n" + 
			"            },\n" + 
			"            \"location_type\" : \"ROOFTOP\",\n" + 
			"            \"viewport\" : {\n" + 
			"               \"northeast\" : {\n" + 
			"                  \"lat\" : 37.4238253802915,\n" + 
			"                  \"lng\" : -122.0829009197085\n" + 
			"               },\n" + 
			"               \"southwest\" : {\n" + 
			"                  \"lat\" : 37.4211274197085,\n" + 
			"                  \"lng\" : -122.0855988802915\n" + 
			"               }\n" + 
			"            }\n" + 
			"         },\n" + 
			"         \"place_id\" : \"ChIJ2eUgeAK6j4ARbn5u_wAGqWA\",\n" + 
			"         \"types\" : [ \"street_address\" ]\n" + 
			"      }\n" + 
			"   ],\n" + 
			"   \"status\" : \"OK\"\n" + 
			"}";
	
	
	String addressJsonStr2 = "{\"address_components\":[{\"long_name\":\"Tarneit\",\"short_name\":\"Tarneit\",\"types\":[\"locality\",\"political\"]},{\"long_name\":\"City of Wyndham\",\"short_name\":\"Wyndham\",\"types\":[\"administrative_area_level_2\",\"political\"]},{\"long_name\":\"Victoria\",\"short_name\":\"VIC\",\"types\":[\"administrative_area_level_1\",\"political\"]},{\"long_name\":\"Australia\",\"short_name\":\"AU\",\"types\":[\"country\",\"political\"]},{\"long_name\":\"3029\",\"short_name\":\"3029\",\"types\":[\"postal_code\"]}],\"formatted_address\":\"Tarneit VIC 3029, Australia\",\"geometry\":{\"bounds\":{\"northeast\":{\"lat\":-37.80094589999999,\"lng\":144.7139713},\"southwest\":{\"lat\":-37.87168399999999,\"lng\":144.614677}},\"location\":{\"lat\":-37.8091678,\"lng\":144.6672079},\"location_type\":\"APPROXIMATE\",\"viewport\":{\"northeast\":{\"lat\":-37.80094589999999,\"lng\":144.7139713},\"southwest\":{\"lat\":-37.87168399999999,\"lng\":144.614677}}},\"partial_match\":true,\"place_id\":\"ChIJwwOmKjL11moRcNiMIXVWBAU\",\"types\":[\"locality\",\"political\"]}";
	
	
	JsonObject json = new JsonObject(addressJsonStr2);
if (true || "OK".equals(json.getString("status"))) {
	//	JsonObject results = json.getJsonArray("results").getJsonObject(0);
	//	System.out.println(results);
		JsonArray address_components = json.getJsonArray("address_components");
		
		/* loop through address_components */
		Integer index = 0;
		Integer count = address_components.size();
		Map<String,String> addressMap = new HashMap<String,String>();
		for (index=0;index<count;index++) {
			
			JsonObject addressComponent = address_components.getJsonObject(index);
			JsonArray types = addressComponent.getJsonArray("types");
			String mainType = types.getString(0);
			addressMap.put(mainType, addressComponent.getString("short_name"));
		}
		
		String streetNumber = (addressMap.get("street_number")==null)?"":addressMap.get("street_number");
		String streetName = (addressMap.get("route")==null)?"":addressMap.get("route");
		
		String street_address = (streetNumber+" "+streetName).trim();
		
		JsonObject geometry = json.getJsonObject("geometry");
		JsonObject location = geometry.getJsonObject("location");
		Double lat = location.getDouble("lat");
		Double lng = location.getDouble("lng");
		String full_address = json.getString("formatted_address");
		System.out.println(address_components);
		
		JsonObject address_json = new JsonObject();
		address_json.put("street_address", street_address);
		address_json.put("suburb", (addressMap.get("locality")==null?"":addressMap.get("locality")));
		address_json.put("state",  (addressMap.get("administrative_area_level_1")==null?"":addressMap.get("administrative_area_level_1")));
		address_json.put("country",  (addressMap.get("country")==null?"":addressMap.get("country")));
		address_json.put("postcode",  (addressMap.get("postal_code")==null?"":addressMap.get("postal_code")));
		address_json.put("full_address", full_address);
		address_json.put("latitude", lat);
		address_json.put("longitude", lng);
		
		String PRI_ADDRESS_JSON = address_json.toString();
		System.out.println("PRI_ADDRESS_JSON="+PRI_ADDRESS_JSON);			

	}
}



}
