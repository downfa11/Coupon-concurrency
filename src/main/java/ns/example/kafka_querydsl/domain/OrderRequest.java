package ns.example.kafka_querydsl.domain;

import lombok.Data;

@Data
public class OrderRequest {
    private String client;
    private String vendor;
    private String kind;
    private Long count;
}
