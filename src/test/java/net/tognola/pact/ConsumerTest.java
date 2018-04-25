package net.tognola.pact;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRuleMk2;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

public class ConsumerTest {

    @Rule
    public PactProviderRuleMk2 mockProvider = new PactProviderRuleMk2("ExampleProvider", this);


    private DslPart requestBody = new PactDslJsonBody()
            .stringType("somethingId")
            .stringType("somethingName")
            .integerType("somethingSize");

    private DslPart responseBody = new PactDslJsonBody()
            .stringType("somethingId")
            .decimalType("somethingPrice");



    @Pact(state = "default", provider = "ExampleProvider", consumer = "ExampleConsumer")
    public RequestResponsePact createNewSomething(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("content-type", "application/json");

        return builder
                .uponReceiving("something")
                .path("/")
                .body(requestBody)
                .method("POST")
                .willRespondWith()
                .status(200)
                .headers(headers)
                .body(responseBody)
                .toPact();
    }



    @Test
    @PactVerification(value = "ExampleProvider", fragment = "createNewSomething")
    public void validCreateNewSomething() throws Exception {
        final RestTemplate call = new RestTemplate();

        RequestObject requestObject = new RequestObject();
        requestObject.setSomethingId("123");
        requestObject.setSomethingName("Bluebeard");

        ResponseObject expectedResponse = new ResponseObject();
        expectedResponse.setSomethingId(requestObject.getSomethingId());
        expectedResponse.setSomethingPrice(42.99f);

        final ResponseObject actualResponse = call.postForEntity(mockProvider.getUrl() + "/", requestObject, ResponseObject.class).getBody();
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getSomethingId()).isNotEmpty();
        assertThat(actualResponse.getSomethingPrice()).isNotNegative();

    }

}
