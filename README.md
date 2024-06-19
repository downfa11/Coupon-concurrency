# 토이프로젝트 - Kafka와 QueryDSL
Apeche Kafka와 queryDSL 실습을 통해서 좀 더 손에 익도록 연습하기 위해 준비한 공부용 프로젝트. </br></br>
2024-06-19 추가) Kafka의 `Consumer Group`에 대해 공부하기 위해서 신규 기능을 추가했습니다.</br></br>
Kafka의 이벤트 브로커를 이용해서 상품 구매 비즈니스(`Order -> Payment -> Shipment`)와 전체 과정의 `Notification`을 구현 
</br></br>
**상품 구매 비즈니스 설계**

- 주문 생성 : 주문 정보를 DB에 저장하고, 메시징 큐에 주문 생성 이벤트를 발행
- 재고 관리 서비스 : 재고 수량을 확인
- 결제 서비스 : 결제에 성공하면 결제 완료 이벤트를 발행
- 배송 서비스 : 결제 완료된 주문에 대해서 배송 준비 이벤트를 발행
- 알림 서비스 : 모든 이벤트를 구독해서 상태를 로그로 출력
  </br></br>
QueryDSL은 진행중
</br></br>

성능 테스트 결과는 블로그에 기록  </br>
https://blog.naver.com/downfa11/223474922882 
</br></br>

**Kafka 사용 전 후의 성능 비교**
- `useCoupon *` 함수들은 발급된 쿠폰들에 대해서 전체 총량을 줄이고, 쿠폰 발급사(`Vendor`)에게 비용을 지급한다.
- `useCoupon`와 Kafka를 통해 작업하는 `useCouponToKafka` 간의 성능 테스트를 진행
- 대용량 트래픽에 대해서 TPS, Latency 등의 성능 지표로 비교  
</br>

**동시성(Concurrency) 제어 확인**
- `@KafkaListener()`의 `concurrency` 인자를 통해서 동시에 처리할 쓰레드를 지정
- 전체 쿠폰 개수의 총량이 지켜지는지 동시에 검사
  </br></br>


## 프로젝트 주의사항
- docker-compose로 Zookeeper, Kafka, kafka-ui, mysql 컴포넌트 실행  

- 프로젝트는 Junit 테스트코드를 위해 로컬에서 수행  
  
- Terminal 에서 다음 명령어로 Docker-compose 파일을 실행  
  ```
  docker-compose up -d
  ```

  </br></br>
  
## 프로젝트 구성

`generateRandomCoupon`, `getRandomUser` : 랜덤으로 사용자 혹은 쿠폰을 발급하는 함수

`getUserCoupons` : 해당 사용자가 가진 쿠폰을 표시하는 함수

`useCoupon` : 쿠폰을 소모해서 사용자의 coupon에 등록하는 함수

`payToVendor` : 쿠폰 소모시 업체에 비용을 지급하는 함수  

</br></br>


`handleOrderCreated` ,`createOrder` : 주문 생성해서 DB에 기록하고 order_created 이벤트 발행

`handleOrderCreated`, `checkStock` : 재고 수량은 무조건 있다고 구성

`processPayment` : 임시로 그냥 무조건 구매에 성공하도록 구성

`handlePaymentCompleted`, `prepareShipping` : 배송 준비를 시작하고, 완료시 shipment_completed 이벤트 발행

`notification*` : 일련의 모든 이벤트를 모니터링하면서 로깅



</br></br></br>
### 모델

쿠폰은 종류마다 전체 100개를 초과할 수 없다.

업체는 쿠폰을 발행하며, 쿠폰 사용시 비용을 비급받는다.
</br></br>
[User]  
```
@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "user_coupons",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "coupon_id"))
    private List<Coupon> coupons = new ArrayList<>();
}
```    
</br></br>
[Coupon]
```
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
``` 
</br></br>
[Vendor]
``` 
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
```

</br></br>
[Order]
```
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String kind;
    private Long count;
}
```