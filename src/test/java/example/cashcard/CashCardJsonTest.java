package example.cashcard;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

/**
 * The @JsonTest annotation marks the CashCardJsonTest as a test class which uses the
 * Jackson framework (which is included as part of Spring). This provides extensive JSON
 * testing and parsing support. It also establishes all the related behavior to test JSON objects.
 */
@JsonTest
public class CashCardJsonTest {

    /**
     * JacksonTester is a convenience wrapper to the Jackson JSON parsing library.
     * It handles serialization and deserialization of JSON objects.
     */
    @Autowired
    private JacksonTester<CashCard> json;

    @Test
    void cashCardSerializationTest() throws IOException {
        CashCard cashCard = new CashCard(99L, 123.45);
        Assertions.assertThat(json.write(cashCard)).isStrictlyEqualToJson("expected.json");
        Assertions.assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.id");
        Assertions.assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.id").isEqualTo(99);
        Assertions.assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.amount");
        Assertions.assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.amount").isEqualTo(123.45);
    }
}

