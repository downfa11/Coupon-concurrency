package ns.example.kafka_querydsl.controller;

import lombok.AllArgsConstructor;
import ns.example.kafka_querydsl.domain.OrderRequest;
import ns.example.kafka_querydsl.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/order")
public class OrderController {
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest){
        orderService.createOrder(orderRequest.getClient(),orderRequest.getVendor(), orderRequest.getKind(),orderRequest.getCount());
        return ResponseEntity.ok().build();
    }
}
