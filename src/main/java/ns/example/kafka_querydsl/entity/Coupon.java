package ns.example.kafka_querydsl.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "coupons")
@Data
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type;
    private int count;
    private int value;
    @ManyToOne
    private Vendor vendor;
}
