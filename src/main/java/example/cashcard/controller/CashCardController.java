package example.cashcard.controller;

import example.cashcard.model.CashCard;
import example.cashcard.repository.CashCardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Controller REST para gestão de Cash Cards.
 * Responsável por expor os endpoints de busca, criação e listagem paginada,
 * mediando a comunicação entre as requisições HTTP e a camada de persistência.
 */
@RestController
@RequestMapping("/cashcards")
public class CashCardController {
    private final CashCardRepository cashCardRepository;

    public CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }

    /**
     * Recupera um Cash Card específico através do seu identificador único.
     * <p>Utiliza o padrão {@link Optional} para tratar a existência do recurso,
     * retornando {@code 200 OK} com o corpo do objeto caso encontrado,
     * ou {@code 404 Not Found} caso contrário.</p>
     * * @param requestedId Identificador do cartão solicitado.
     * @return ResponseEntity contendo o CashCard ou status de não encontrado.
     */
    @GetMapping("/{requestedId}")
    private ResponseEntity<CashCard> findById(@PathVariable Long requestedId) {
        Optional<CashCard> cashCardOptional = cashCardRepository.findById(requestedId);
        return cashCardOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Persiste um novo Cash Card no banco de dados.
     * <p>Conforme as boas práticas REST (RFC 9110), após a criação bem-sucedida,
     * o método retorna o status {@code 201 Created} e inclui no cabeçalho 'Location'
     * a URI de acesso ao novo recurso gerado.</p>
     * * @param newCashCardRequest Dados do novo cartão enviados no corpo da requisição.
     * @param ucb Utilitário para construção dinâmica da URI de localização.
     * @return ResponseEntity com status 201 e o cabeçalho Location.
     */
    @PostMapping
    private ResponseEntity<Void> createCashCard(@RequestBody CashCard newCashCardRequest, UriComponentsBuilder ucb) {
        CashCard savedCashCard = cashCardRepository.save(newCashCardRequest);
        URI locationOfNewCashCard = ucb
                .path("/cashcards/{id}")
                .buildAndExpand(savedCashCard.id())
                .toUri();
        return ResponseEntity.created(locationOfNewCashCard).build();
    }

    /**
     * Lista os Cash Cards de forma paginada e ordenada.
     * <p>O método {@code getSortOr()} gerencia os valores padrão para os parâmetros de
     * página (page), tamanho (size) e ordenação (sort). Os valores padrão originam-se
     * de duas fontes distintas:</p>
     * <ul>
     * <li><b>Spring Framework:</b> Fornece automaticamente os valores de página (0)
     * e tamanho (20) de forma nativa ("out of the box").</li>
     * <li><b>Implementação Customizada:</b> Definimos o parâmetro de ordenação
     * padrão via código, passando um objeto {@code Sort.by(Sort.Direction.ASC, "amount")}.</li>
     * </ul>
     * <p>O resultado final garante que, caso o cliente não envie os parâmetros obrigatórios,
     * a aplicação forneça retornos consistentes e seguros.</p>
     * * @param pageable Objeto contendo as informações de paginação enviadas na requisição.
     * @return ResponseEntity contendo a lista de Cash Cards da página solicitada.
     */
    @GetMapping
    private ResponseEntity<List<CashCard>> findAll(Pageable pageable) {
        Page<CashCard> page = cashCardRepository.findAll(
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
                ));
        return ResponseEntity.ok(page.getContent());
    }
}