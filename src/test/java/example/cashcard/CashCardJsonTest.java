package example.cashcard;

import example.cashcard.model.CashCard;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de fatia (Slice Test) para a camada de serialização JSON.
 * A anotação {@code @JsonTest} isola o contexto para testar especificamente o framework Jackson,
 * garantindo que a conversão entre Objetos Java e JSON respeite o contrato da API.
 */
@JsonTest
public class CashCardJsonTest {

    /**
     * {@link JacksonTester} é um utilitário do Spring que facilita a asserção de conteúdos JSON.
     * Utilizado para validar a estrutura e o conteúdo de um único objeto {@link CashCard}.
     */
    @Autowired
    private JacksonTester<CashCard> json;

    /**
     * Utilizado para validar a serialização e desserialização de arrays de Cash Cards.
     */
    @Autowired
    private JacksonTester<CashCard[]> jsonList;

    CashCard[] cashCards = Arrays.array(
            new CashCard(99L, 123.45, "goku"),
            new CashCard(100L, 1.00, "goku"),
            new CashCard(101L, 150.00, "goku")
    );

    /**
     * Valida a Serialização (Java -> JSON) de um objeto individual.
     * <p>O teste garante que o JSON gerado seja idêntico ao arquivo de referência
     * e que os campos {@code id} e {@code amount} contenham os valores corretos.</p>
     */
    @Test
    void cashCardSerializationTest() throws IOException {
        CashCard cashCard = new CashCard(99L, 123.45, "goku");
        assertThat(json.write(cashCard)).isStrictlyEqualToJson("expected.json");
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.id");
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.id").isEqualTo(99);
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.amount");
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.amount").isEqualTo(123.45);
        assertThat(json.write(cashCard)).hasJsonPathStringValue("@.owner");
        assertThat(json.write(cashCard)).extractingJsonPathStringValue("@.owner").isEqualTo("goku");
    }

    /**
     * Valida a Serialização de uma coleção de objetos.
     * Converte o array {@code cashCards} em JSON e compara com o arquivo {@code list.json},
     * assegurando a integridade da estrutura de lista da API.
     */
    @Test
    @DisplayName("CashCard list serialization test")
    void cashCardListSerializationTest() throws IOException {
        assertThat(jsonList.write(cashCards)).isStrictlyEqualToJson("list.json");
    }

    /**
     * Valida a Desserialização (JSON -> Java).
     * Simula o recebimento de uma String JSON e verifica se o Jackson reconstrói
     * corretamente o array de objetos Java, comparando-o com o estado esperado.
     */
    @Test
    @DisplayName("CashCard deserialization test")
    void cashCardListDeserializationTest() throws IOException {
        String expected = """
                [
                        { "id": 99, "amount": 123.45, "owner": "goku" },
                        { "id": 100, "amount": 1.00, "owner": "goku" },
                        { "id": 101, "amount": 150.00, "owner": "goku" }
                ]
                """;
        assertThat(jsonList.parse(expected)).isEqualTo(cashCards);
    }
}