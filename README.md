# Apache kafka 트랜잭션과 동시성 관리
Apeche Kafka와 queryDSL 실습을 통해서 좀 더 손에 익도록 연습하기 위해 준비한 공부용 프로젝트. </br></br>

### 2024-06-28 업데이트 내용
2024-06-19 추가) Kafka의 `Consumer Group`에 대해 공부하기 위해서 신규 기능을 추가했습니다.</br>
- Kafka의 이벤트 브로커를 이용해서 상품 구매 비즈니스(`Order -> Payment -> Shipment`)와 전체 과정의 `Notification`을 구현 

2024-06-28 추가) 기존의 Coupon 발급 시스템과 상품 구매 비즈니스를 통합했습니다.</br>
- Redis의 _Redisson_ 라이브러리를 통해 **분산 락(`distributed lock`) 구현**</br>
- 동시 요청에 대한 **배타적 락(`xlock`) 사용 여부**에 따라 테스트코드 작성

</br></br>


### 프로젝트 주의사항
- docker-compose로 zookeeper, Kafka, kafka-ui, mysql 컴포넌트 실행 </br>
  _kafka zraft mode: docker-composer-cluster.yaml_
- Terminal 에서 다음 명령어로 Docker-compose 파일을 실행  
  ```
  docker-compose up -d
  ```
- JUnit 테스트코드를 위해 로컬에서 직접 실행

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



</br></br>

## 프로젝트 개요

### 상품 구매 비즈니스 설계

- 주문 생성(Order) : 주문 정보를 DB에 저장하고, 메시징 큐에 주문 생성 이벤트를 발행
- 재고 관리 서비스(Inventory) : 재고 수량을 조절 - **Redisson을 이용한 동시성(`Concurrency`) 제어**
- 알림 서비스(Notificaion) : 모든 이벤트를 구독해서 상태를 로그로 출력
  </br></br>

</br></br>

### Kafka 사용 전후간 성능 비교
- `useCoupon *` : 함수들은 발급된 쿠폰들에 대해서 전체 총량을 줄이고, `Vendor`에게 비용을 지급
- `useCoupon`와 Kafka를 통해 작업하는 `useCouponToKafka` 간의 성능 테스트를 진행</br></br>
  
- 대용량 트래픽에 대해서 TPS, Latency 등의 성능 지표로 비교
  - [기술 블로그](https://blog.naver.com/downfa11/223474922882) 기록
    </br>
</br></br>

### 동시성(Concurrency) 제어 확인
Redis에서 제공하는 Redisson 라이브러리에서 분산 락(distributed lock) 제공</br>
  - `tryLock(waitTime, leaseTime, TimeUnit)` : waitTime동안 Lock 점유를 시도하고, leaseTime만큼 락을 사용하거나 lock 해제</br>
  lock 접근시 선행 쓰레드가 존재하면 waitTime동안 Lock 점유를 기다린다.  </br>
  leaseTime만큼 지나면 lock이 해제되기 때문에 다른 쓰레드도 일정 시간이 지나면 Lock을 점유할 수 있다는 장점
</br></br></br>
**InventoryServiceTest의 `BuyWithoutLock`, `BuyWithLock` 간의 차이를 확인**
  - Lock 사용시 동시 접근이 제한되어 원하는 로직이 잘 수행됨
  - Lock 미사용시 중복해서 개수를 소모하지 않는 경우를 확인

</br></br> 

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