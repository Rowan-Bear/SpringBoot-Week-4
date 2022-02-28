package com.promineotech.jeep.controller.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

public class CreateOrderTestSupport {

	 @Autowired
	  private TestRestTemplate restTemplate;
	  
	  @LocalServerPort
	  private int serverPort;
	  
	public TestRestTemplate getRestTemplate() {
	    
	    return this.restTemplate;
	  }

	public String getBaseUriForOrders() {
	  
	  return String.format("http://localhost:%d/orders", serverPort);
	}
	
	protected String createOrderBody() {
		// @formatter: off
		return "{\n"
			 + "\"customer\":\"MORISON_LINA\",\n"
			 + "\"model\":\"WRANGLER\",\n"
			 + "\"trim\":\"Sport Altitude\",\n"
			 + "\"doors\":4,\n"
			 + "\"color\":\"EXT_NACHO\",\n"
			 + "\"engine\":\"2_0_TURBO\",\n"
			 + "\"tire\":\"35_TOYO\",\n"
			 + "\"options\":[\n"
			 + "\"DOOR_QUAD_4\",\n"
			 + "\"EXT_AEV_LIFT\",\n"
			 + "\"EXT_WARN_WINCH\",\n"
			 + "\"EXT_WARN_BUMPER_FRONT\",\n"
			 + "\"EXT_WARN_BUMPER_REAR\",\n"
			 + "\"EXT_ARB_COMPRESSOR\"\n"
			 + "]\n"
			 + "}";
			
		
	
	
					  // @formatter: on
		}
	

	
	}
		
	
	
	

