package example.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import example.cashcard.model.CashCard;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Let's refer to the official Request for Comments for HTTP Semantics and Content (RFC 9110)
 * for guidance as to how our API should behave.Let's refer to the official Request for Comments
 * for HTTP Semantics and Content (RFC 9110) for guidance as to how our API should behave.
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
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("goku", "123")
                .getForEntity("/cashcards/99", String.class);
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
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("goku", "123")
                .getForEntity("/cashcards/1000", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isBlank();
    }

    @Test
    @DisplayName("Should return all cash cards when List is requested")
    void shouldReturnAllCashCardsWhenListIsRequested() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("goku", "123")
                .getForEntity("/cashcards", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        // calculate the length of the array
        int cashCardCount = documentContext.read("$.length()");
        assertThat(cashCardCount).isEqualTo(3);

        // retrieves the list of all `id` values returned
        List<Integer> ids = documentContext.read("$..id");
        assertThat(ids).containsExactlyInAnyOrder(99, 100, 101);

        // retrieves the list of all `amount` values returned
        List<Double> amounts = documentContext.read("$..amount");
        assertThat(amounts).containsExactlyInAnyOrder(123.45, 150.00, 1.00);
    }

    /**
     * If one or more resources has been created on the origin server as a result of successfully
     * processing a POST request, the origin server SHOULD send a 201 (Created) response containing a
     * Location header field that provides an identifier for the primary resource created
     * and a representation that describes the status of the request while referring to the new resource(s).
     */
    @Test
    @DirtiesContext
    @DisplayName("Should create a new cash card")
    void shouldCreateANewCashCard() {
        CashCard newCashCard = new CashCard(null, 250.00, "goku");
        ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth("goku", "123")
                .postForEntity("/cashcards", newCashCard, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfNewCashCard = createResponse.getHeaders().getLocation();

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("goku", "123")
                .getForEntity(locationOfNewCashCard, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        Number id = documentContext.read("$.id");
        Double amount = documentContext.read("$.amount");

        assertThat(id).isNotNull();
        assertThat(amount).isEqualTo(250.00);
    }

    @Test
    @DisplayName("Should return a page of cash cards")
    void shouldReturnAPageOfCashCards() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("goku", "123")
                .getForEntity("/cashcards?page=0&size=1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should return a list of cash card in a descending order")
    void shouldReturnAListOfCashCardInADescendingOrder() {

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("goku", "123")
                .getForEntity("/cashcards?page=0&size=1&sort=amount,desc", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray read = documentContext.read("$[*]");
        assertThat(read.size()).isEqualTo(1);

        Double amount = documentContext.read("$[0].amount");
        assertThat(amount).isEqualTo(150.00);
    }

    @Test
    @DisplayName("Should return a sorted page of cash cards with no parameter and use default values")
    void shouldReturnASortedPageOfCashCardsWithNoParametersAndUseDefaultValues() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("goku", "123")
                .getForEntity("/cashcards", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(3);

        JSONArray amounts = documentContext.read("$..amount");
        assertThat(amounts).containsExactly(1.00, 123.45, 150.00);
    }

    @Test
    @DisplayName("Should reject user who are not card owner")
    void shouldRejectUserWhoAreNotCardOwner() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("vegeta", "123")
                .getForEntity("/cashcards/99", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Should not allow access to cash card they don't own")
    void shouldNotAllowAccessToCashCardTheyDontOwn() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("goku", "123")
                .getForEntity("/cashcards/102", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
