package com.rahulshettyacademy.Courses;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import com.rahulshettyacademy.controller.AllCourseData;
import com.rahulshettyacademy.repository.CoursesRepository;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.StateChangeAction;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerAuth;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // Starts the real provider server on a random port
@Provider("CoursesCatalogue") // Must match the name of the provider in the contract
@PactFolder("src/main/java/pacts") // The default value of the url (pacts) is src/main/pacts
public class PactProviderTest {

    /**
     * Retrieves the dynamically assigned port of the embedded provider server,
     * allowing Pact tests to verify interactions against the real endpoints
     * during the execution of the @TestTemplate
     */
    @LocalServerPort
    public int port;

    @Autowired
    CoursesRepository repository;

    /* @TestTemplate enables Pact framework-based verification for
     *  running tests against each interaction defined in the Pact file.
     *  For example, if the Pact file's "interactions" array has two entries
     *  representing consumer requests, the test will execute twice,
     *  once for each interaction.
     */
    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    public void pactVerificationTest(PactVerificationContext context) {
        /* Uses Pact to verify the real endpoint defined in the contract.
         * It checks whether the provider's response matches the expected
         * interaction, including response structure, status, headers,
         * data types, and response content, as defined in the Pact file.
         */
        context.verifyInteraction();
    }

    /**
     * Runs before every test
     * The `setup` method is executed before each test to configure the
     * PactVerificationContext with an HTTP target pointing to the provider
     * service running on `localhost` and the dynamically assigned `port`.
     * This ensures that Pact tests interact with the correct endpoint of
     * the provider.
     */
    @BeforeEach
    public void setup(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port));
    }

    /**
     * The `@State` annotation in Pact testing is used to define the states
     * that the provider needs to be in so the consumer's contract can be validated.
     * The `coursesExist` method sets up the provider for the state "courses exist",
     * ensuring it is ready for the consumer's expectations during the test.
     * <p>
     * Note:
     * - The "providerStates" property in the consumer contract specifies this state name.
     * - The `StateChangeAction.SETUP` ensures the provider is initialized appropriately
     *   prior to the interaction test.
     * - The `StateChangeAction.TEARDOWN` ensures cleanup or rollback after the test if required.
     * <p>
     * Ensure the state name specified here ("courses exist") matches the name given
     * in the consumer contract's `providerStates` property.
     */
    @State(value = "courses exist", action = StateChangeAction.SETUP)
    public void coursesExist() {
    }

    @State(value = "courses exist", action = StateChangeAction.TEARDOWN)
    public void coursesExistTearDown() {
    }

    /* Final step: In the setup method for Appium, a test record is inserted.
       The teardown method is used to clean up the Appium state.

        Example scenarios:
         - GET /getCourseName/Appium -> Returns: {name: "appium", id: "", price: ""}.
         - GET /getCourseName/Appium -> Returns: {msg: "course does not exist"} when the course is absent.
    */
    @State(value = "Course Appium exist", action = StateChangeAction.SETUP)
    public void appiumCourseExistSetup() {}

    @State(value = "Course Appium exist", action = StateChangeAction.TEARDOWN)
    public void appiumCourseExistTearDown() {}

    @State(value = "Course Appium does not exist", action = StateChangeAction.SETUP)
    public void appiumCourseDoNotExist(Map<String, Object> params) {
        String name = (String) params.get("name");
        // Delete the appium record from the database
        Optional<AllCourseData> del = repository.findById(name);//mock
        if (del.isPresent()) {
            repository.deleteById("Appium");
        }
    }

    @State(value = "Course Appium does not exist", action = StateChangeAction.TEARDOWN)
    public void appiumCourseDoNotExistTearDown(Map<String, Object> params) {
        // Insert the appium record in the database
        String name = (String) params.get("name");
        Optional<AllCourseData> del = repository.findById(name);//mock
        if (!del.isPresent()) {
            AllCourseData allCourseData = new AllCourseData();
            allCourseData.setCourse_name("Appium");
            allCourseData.setCategory("mobile");
            allCourseData.setPrice(120);
            allCourseData.setId("12");
            repository.save(allCourseData);
        }
    }
}
