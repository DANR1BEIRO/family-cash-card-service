package example.cashcard;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Autowired
    private JacksonTester<CashCard[]> jsonList;

    CashCard[] cashCards = Arrays.array(
            new CashCard(99L, 123.45),
            new CashCard(100L, 1.00),
            new CashCard(101L, 150.00)
    );

    /**
     * Verifica se o Spring consegue transformar um objeto Java em JSON corretamente.
     * Passo a passo simples:<br>

     * 1. Cria um cartão: new CashCard(99L, 123.45)<br>
     * 2. Usa o json.write(cashCard) → isso converte o objeto em JSON<br><br>
     * Depois faz várias verificações:<br>
     * - O JSON gerado tem que ser exatamente igual ao arquivo expected.json que está na pasta de testes.<br>
     * - Tem que ter um campo chamado id com número.<br>
     * - O id tem que ser 99.<br>
     * - Tem que ter um campo chamado amount com número.<br>
     * - O amount tem que ser 123.45.<br>
     *
     * Em palavras de iniciante:
     * É como se você pegasse um cartão de dinheiro (objeto Java) e dissesse:
     * “Transforma isso num texto JSON e me mostra se ficou igual ao que eu espero.”<br><br>
     *
     * Serialização = Java vira JSON<br>
     * Deserialização = JSON vira Java<br><br>
     * os testes garantem que:<br>
     *
     * Quando a API devolver um cartão ou uma lista, o JSON vai estar correto.
     * Quando a API receber um JSON, ele vai virar o objeto certo.
     */
    @Test
    void cashCardSerializationTest() throws IOException {
        CashCard cashCard = new CashCard(99L, 123.45);
        assertThat(json.write(cashCard)).isStrictlyEqualToJson("expected.json");
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.id");
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.id").isEqualTo(99);
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.amount");
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.amount").isEqualTo(123.45);
    }

    /**
     * Faz o mesmo que o teste 1, mas agora com vários cartões (uma lista).<br>
     * Passo a passo simples:<br><br>

     * 1. Pega o array cashCards (que tem 3 cartões).<br>
     * 2. Usa jsonList.write(cashCards) → converte a lista inteira em JSON.<br>
     * 3. Verifica se o JSON gerado é exatamente igual ao arquivo list.json.<br><br>
     *
     * Em palavras de iniciante:
     * É como se você tivesse 3 cartões (objeto Java) e quisesse transformar os 3 de uma vez num texto JSON.
     * O teste pergunta: “O JSON que saiu está igual ao que eu tinha guardado no arquivo list.json?”
     *
     */
    @Test
    @DisplayName("CashCard list serialization test")
    void cashCardListSerializationTest() throws IOException {
        assertThat(jsonList.write(cashCards)).isStrictlyEqualToJson("list.json");
    }

    /**
     * Faz o caminho contrário do úultimo teste: pega um texto JSON e transforma de volta em objetos Java.
     * Passo a passo simples:<br><br>
     *
     * 1. Cria uma String grande chamada expected com um JSON escrito à mão (com 3 cartões).<br>
     * 2. Usa jsonList.parse(expected) → pega esse texto JSON e converte de volta em objetos CashCard[].<br>
     * 3. Verifica se o resultado é exatamente igual ao array cashCards que está na classe.<br><br>
     *
     * Em palavras de iniciante:
     * É como se você recebesse uma carta escrita em JSON e dissesse:
     * “Transforma essa carta de volta em cartões (objetos Java) e confere se eles ficaram
     * iguais aos que eu já tenho na mão.”
     */
    @Test
    @DisplayName("CashCard deserialization test")
    void cashCardListDeserializationTest() throws IOException {
        String expected = """
                [
                        { "id": 99, "amount": 123.45 },
                        { "id": 100, "amount": 1.00 },
                        { "id": 101, "amount": 150.00 }
                ]
                """;
        assertThat(jsonList.parse(expected)).isEqualTo(cashCards);
    }
}

