package example.cashcard.controller;

import example.cashcard.CashCard;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CashCardController {

    @GetMapping("/cashcards/{requestedId}")
    private ResponseEntity<CashCard> findById(@PathVariable Long requestedId) {
        CashCard cashCard = new CashCard(requestedId, 100.00);
        return ResponseEntity.ok(cashCard);
    }
}
