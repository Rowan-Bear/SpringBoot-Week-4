package com.promineotech.jeep.controller;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.params.provider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.jdbc.JdbcTestUtils;

import com.promineotech.jeep.Constants;
import com.promineotech.jeep.entity.Jeep;
import com.promineotech.jeep.entity.JeepModel;
import com.promineotech.jeep.service.JeepSalesService;

import lombok.Getter;







class FetchJeepTest {
	
	@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
	@ActiveProfiles("test")
	@Sql(scripts = {
	    "classpath:flyway/migrations/v1.0_Jeep_Schema.sql",
	    "classpath:flyway/migrations/v1.1_Jeep_Data.sql"}, 
	    config = @SqlConfig(encoding = "utf-8"))
	@Nested
	class TestsThatDoNotPolluteTheApplicationContext {
		
		@Test
		  void testThatJeepsAreReturnedWhenAValidModelAndTrimAreSupplied() {
		    //given: a valid model, trim and URI
		    JeepModel model = JeepModel.WRANGLER;
		    String trim = "Sport";
		    String uri = String.format("%s?model=%s&trim=%s", getBaseUri(), model, trim);
//		    // When: a connection is made to the URI
		    ResponseEntity<List<Jeep>> response = getRestTemplate().exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
		//    
//		    // Then: a success (OK - 200) status code is returned
		   assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		   
		   //And: the actual list returned is the same as the expected list
		   List<Jeep> actual = response.getBody();
		   List<Jeep> expected = buildExpected();
		   
		 
		   
		   assertThat(actual).isEqualTo(expected);
		  }
		  
			@Test
		  void testThatAnErrorMessageIsReturnedWhenAnUnknownTrimIsSupplied() {
			    //given: a valid model, trim and URI
			    JeepModel model = JeepModel.WRANGLER;
			    String trim = "Invalid Value";
			    String uri = String.format("%s?model=%s&trim=%s", getBaseUri(), model, trim);
//			    // When: a connection is made to the URI
			    ResponseEntity<Map<String, Object>> response = getRestTemplate().exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
			//    
//			    // Then: a not found (404) status code is returned
			   assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
			   
			   //And: an error message is returned
			   
			   Map<String, Object> error = response.getBody();
			   
			   assertErrorMessageValid(error, HttpStatus.NOT_FOUND);
			   
			   
			  }
			
			@ParameterizedTest
			@MethodSource("com.promineotech.jeep.controller.FetchJeepTest#parametersForInvalidInput")
			  void testThatAnErrorMessageIsReturnedWhenAnInvalidValueIsSupplied(String model, String trim, String reason) {
				    //given: a valid model, trim and URI
				    String uri = String.format("%s?model=%s&trim=%s", getBaseUri(), model, trim);
				    
//				    // When: a connection is made to the URI
				    ResponseEntity<Map<String, Object>> response = getRestTemplate().exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
				//    
//				    // Then: a not found (404) status code is returned
				   assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
				   
				   //And: an error message is returned
				   
				   Map<String, Object> error = response.getBody();
				   
				   assertErrorMessageValid(error, HttpStatus.BAD_REQUEST);
				   
				   
				  }
			
			protected void assertErrorMessageValid(Map<String, Object> error, HttpStatus status) {
				assertThat(error)
				   .containsKey("message")
				   .containsEntry("status code", status.value())
				   .containsEntry("uri", "/jeeps")
				   .containsKey("timestamp")
				   .containsEntry("reason", status.getReasonPhrase());
			}
		  
		  protected List<Jeep> buildExpected() {
		    List<Jeep> list = new LinkedList<>();
		    
		  
		    
		    list.add(Jeep.builder()
		        .modelId(JeepModel.WRANGLER)
		        .trimLevel("Sport")
		        .numDoors(4)
		        .wheelSize(17)
		        .basePrice(new BigDecimal("31975.00"))
		        .build());
		    
		    list.add(Jeep.builder()
		            .modelId(JeepModel.WRANGLER)
		            .trimLevel("Sport")
		            .numDoors(2)
		            .wheelSize(17)
		            .basePrice(new BigDecimal("28475.00"))
		            .build());
		    
		    
		 
		    Collections.sort(list);
		    return list;
		  }
		  
		  @Autowired
		  private TestRestTemplate restTemplate;
		  
		  @LocalServerPort
		  private int serverPort;
		  
		private TestRestTemplate getRestTemplate() {
		    
		    return this.restTemplate;
		  }

		private String getBaseUri() {
		  
		  return String.format("http://localhost:%d/jeeps", serverPort);
		}
		
	
		
	}
	
	static Stream<Arguments> parametersForInvalidInput() {
		return Stream.of(
				arguments("WRANGLER", "!@#$@#%", "Trim contains non-alpha-numeric chars"),
				arguments("WRANGLER", "C".repeat(Constants.TRIM_MAX_LENGTH + 1), "Trim length too long"),
				arguments("INVALID", "Sport", "Model is not enum value")
				);
				
				
	}
	
	@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
	@ActiveProfiles("test")
	@Sql(scripts = {
	    "classpath:flyway/migrations/v1.0_Jeep_Schema.sql",
	    "classpath:flyway/migrations/v1.1_Jeep_Data.sql"}, 
	    config = @SqlConfig(encoding = "utf-8"))
	@Nested
	class TestsThatDoPolluteTheApplicationContext {
		
		@MockBean
		private JeepSalesService jeepSalesService;
		
		@Test
		  void testThatAnUnplannedErrorResultsInA500Status() {
			    //given: a valid model, trim and URI
			    JeepModel model = JeepModel.WRANGLER;
			    String trim = "INVALID";
			    String uri = String.format("%s?model=%s&trim=%s", getBaseUri(), model, trim);
			    
			    doThrow(new RuntimeException("Ouch!"))
			    .when(jeepSalesService)
			    .fetchJeeps(model, trim);
			    
			    
//			    // When: a connection is made to the URI
			    ResponseEntity<Map<String, Object>> response = getRestTemplate().exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
			//    
//			    // Then: an internal server error (500) status is returned
			   assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
			   
			   //And: an error message is returned
			   
			   Map<String, Object> error = response.getBody();
			   
			   assertErrorMessageValid(error, HttpStatus.INTERNAL_SERVER_ERROR);
			   
			   
			  }
		
		
		
		
		
		
		
		protected void assertErrorMessageValid(Map<String, Object> error, HttpStatus status) {
			assertThat(error)
			   .containsKey("message")
			   .containsEntry("status code", status.value())
			   .containsEntry("uri", "/jeeps")
			   .containsKey("timestamp")
			   .containsEntry("reason", status.getReasonPhrase());
		}
	  
	  protected List<Jeep> buildExpected() {
	    List<Jeep> list = new LinkedList<>();
	    
	  
	    
	    list.add(Jeep.builder()
	        .modelId(JeepModel.WRANGLER)
	        .trimLevel("Sport")
	        .numDoors(4)
	        .wheelSize(17)
	        .basePrice(new BigDecimal("31975.00"))
	        .build());
	    
	    list.add(Jeep.builder()
	            .modelId(JeepModel.WRANGLER)
	            .trimLevel("Sport")
	            .numDoors(2)
	            .wheelSize(17)
	            .basePrice(new BigDecimal("28475.00"))
	            .build());
	    
	    
	 
	    Collections.sort(list);
	    return list;
	  }
	  
	  @Autowired
	  private TestRestTemplate restTemplate;
	  
	  @LocalServerPort
	  private int serverPort;
	  
	private TestRestTemplate getRestTemplate() {
	    
	    return this.restTemplate;
	  }

	private String getBaseUri() {
	  
	  return String.format("http://localhost:%d/jeeps", serverPort);
	}
		
	}
	
  
//Tests in-memory database

	
  
	
	

	
 
  
 
  }








