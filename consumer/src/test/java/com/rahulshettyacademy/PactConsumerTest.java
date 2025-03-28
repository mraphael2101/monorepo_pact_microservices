package com.rahulshettyacademy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahulshettyacademy.controller.LibraryController;
import com.rahulshettyacademy.controller.ProductsPrices;
import com.rahulshettyacademy.controller.SpecificProduct;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;

import java.util.Objects;

//Consumer tests are targeting the provider

@SpringBootTest // Loads the full application context for the Spring Boot Ms Test i.e. Spring Boot components
@ExtendWith(PactConsumerTestExt.class) // Enables testing with JUnit5 consumer-provider contracts and Pact lifecycle
/*
  The provider must be defined on a class level
  Name should match what is defined in the Pact file along with provider settings
  */
@PactTestFor(providerName = "CoursesCatalogue")
public class PactConsumerTest {

    @Autowired //Gives you an instance of the class via dependency injection
    private LibraryController libraryController;

    // RequestResponsePact object is a representation of the Pact request-response contract
    @Pact(consumer = "BooksCatalogue")
    public RequestResponsePact PactallCoursesDetailsPriceCheck(PactDslWithProvider builder) {
        return builder.given("courses exist")
                .uponReceiving("getting all courses details") // this is just a description
                .path("/allCourseDetails")
                .willRespondWith()
                .status(200)
                .body(Objects.requireNonNull(PactDslJsonArray.arrayMinLike(3) // Creates x3 list values
                        .integerType("price", 10) // Assigns 10 to each price
                        .closeObject())).toPact();
    }

    // This test is calling the consumer ms which indirectly calls the provider contract mock service
    // we are testing the consumer logic by mocking the provider response

    /* The unit test verifies the "BooksCatalogue" app's price aggregation logic. The Pact contract acts
    as a rulebook, defining the expected interaction with the "Course MS" and creating a mock provider.
    This allows the test to isolate and validate the "BooksCatalogue" app's behavior without relying on a
    live "Course MS" service, ensuring it correctly handles the provider's expected response */
    @Test
    @PactTestFor(pactMethod = "PactallCoursesDetailsPriceCheck", port = "9999")
    public void testAllProductsSum(MockServer mockServer) throws JsonMappingException, JsonProcessingException {
        String expectedJson = "{\"booksPrice\":250,\"coursesPrice\":30}";
        libraryController.setBaseUrl(mockServer.getUrl());  // Override the real end-point
        ProductsPrices productsPrices = libraryController.getProductPrices();
        ObjectMapper obj = new ObjectMapper();
        String jsonActual = obj.writeValueAsString(productsPrices);
        Assertions.assertEquals(expectedJson, jsonActual);
    }

}
