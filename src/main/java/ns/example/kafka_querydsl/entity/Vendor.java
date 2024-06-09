package ns.example.kafka_querydsl.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "vendors")
@Data
public class Vendor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int balance;
}
