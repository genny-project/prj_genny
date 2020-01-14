package life.genny.utils;

import org.junit.Test;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class JsonTest {
@Test
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
	JsonObject json = new JsonObject(addressJsonStr);
	if ("OK".equals(json.getString("status"))) {
		JsonObject results = json.getJsonArray("results").getJsonObject(0);
		System.out.println(results);
		JsonArray address_components = results.getJsonArray("address_components");
		JsonObject streetNumberJson = address_components.getJsonObject(0);

		JsonObject streetJson = address_components.getJsonObject(1);
		String street_address = streetNumberJson.getString("short_name")+" "+streetJson.getString("short_name");
		JsonObject cityJson = address_components.getJsonObject(2);
		String suburb = cityJson.getString("short_name");
		JsonObject countryJson = address_components.getJsonObject(5);
		String country = countryJson.getString("short_name");
		JsonObject stateJson = address_components.getJsonObject(4);
		String state = stateJson.getString("short_name");

		JsonObject postcodeJson = address_components.getJsonObject(6);
		String postcode = postcodeJson.getString("short_name");
		JsonObject geometry = results.getJsonObject("geometry");
		JsonObject location = geometry.getJsonObject("location");
		Double lat = location.getDouble("lat");
		Double lng = location.getDouble("lng");
		String full_address = results.getString("formatted_address");
		System.out.println(address_components);
		
		JsonObject address_json = new JsonObject();
		address_json.put("street_address", street_address);
		address_json.put("suburb", suburb);
		address_json.put("state", state);
		address_json.put("country", country);
		address_json.put("postcode", postcode);
		address_json.put("full_address", full_address);
		address_json.put("latitude", lat);
		address_json.put("longitude", lng);
		
		String PRI_ADDRESS_JSON = address_json.toString();
		System.out.println("PRI_ADDRESS_JSON="+PRI_ADDRESS_JSON);

	}
}
}
