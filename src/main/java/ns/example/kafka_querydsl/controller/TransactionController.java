package ns.example.kafka_querydsl.controller;

import ns.example.kafka_querydsl.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionController {

    @Autowired
    TransactionService transactionService;


    @GetMapping("/transaction")
    public void register() throws InterruptedException {
        transactionService.register(1L,100L,"남석");
    }
}
