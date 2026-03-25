package example.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

/**
Let's refer to the official Request for Comments for HTTP Semantics and Content (RFC 9110)
 for guidance as to how our API should behave.Let's refer to the official Request for Comments
 for HTTP Semantics and Content (RFC 9110) for guidance as to how our API should behave.
 */

// start our Spring Boot application and make it available for our test to perform requests to it.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CashCardApplicationTests {

    // inject a test helper that'll allow us to make HTTP requests to the locally running application
    @Autowired
    TestRestTemplate restTemplate;

    @Test
    @DisplayName("Should return a Cash Card when data is saved")
    void shouldReturnACashCardWhenDataIsSaved() {
        // use `restTemplate` to make an HTTP GET request to our application endpoint `/cashcards/99`
        ResponseEntity<String> response = restTemplate.getForEntity("/cashcards/99", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Converts the response string into a json-aware object with lots of helped methods
        DocumentContext documentContext = JsonPath.parse(response.getBody());
        Number id = documentContext.read("$.id");
        assertThat(id).isEqualTo(99);

        Double amount = documentContext.read("$.amount");
        assertThat(amount).isEqualTo(123.45);
        // expect that when we request a Cash Card with id of 99 a JSON objet will be return with
        // something in the id field. Assert that the id is not null
    }

    @Test
    @DisplayName("Should not return a Cash Card with an unknown id")
    void shouldNotReturnACashCardWithAnUnknownId() {
        ResponseEntity<String> response = restTemplate.getForEntity("/cashcards/1000", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isBlank();
    }

    /**
     * If one or more resources has been created on the origin server as a result of successfully
     * processing a POST request, the origin server SHOULD send a 201 (Created) response containing a
     * Location header field that provides an identifier for the primary resource created
     * and a representation that describes the status of the request while referring to the new resource(s).
     */
    @Test
    @DisplayName("Should create a new cash card")
    void shouldCreateANewCashCard() {
        CashCard newCashCard = new CashCard(null, 250.00);
        ResponseEntity<Void> createResponse = restTemplate.postForEntity("/cashcards", newCashCard, Void.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfNewCashCard = createResponse.getHeaders().getLocation();

        ResponseEntity<String> getResponse = restTemplate.getForEntity(locationOfNewCashCard, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        Number id = documentContext.read("$.id");
        Double amount = documentContext.read("$.amount");

        assertThat(id).isNotNull();
        assertThat(amount).isEqualTo(250.00);
    }
}
