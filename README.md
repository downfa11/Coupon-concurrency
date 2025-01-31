# 쿠폰 발급으로 알아보는 동시성(Concurrency) 로직

25-01-31 현재 새로운 도메인에서 필요한 기능을 고민하면서 새로운 프로젝트([Github](https://github.com/downfa11/ecommerce))로 개선중입니다.

<br>

**약속**

- queryDSL을 통한 쿼리 생성을 좀 더 몸에 습관들이기
- Apache Kafka를 이용한 비즈니스 동작과 도입 전후의 성능 테스트
- 비즈니스 특성상 동시에 요청하는 **쿠폰 등록 로직에 대한 동시성 처리**
- 기존 Redisson 방식의 분산 락(Distributed Lock)에 대한 성능 개선점 모색
<br>

**2025-01-25**

- Redisson을 이용한 분산 락 적용 전후로 성능 차이를 발견함
Redis EVAL을 통해 스크립트로 전달하여 개선(RPS 764.5 -> )

**2024-06-28**
- 기존의 Coupon 발급 시스템과 상품 구매 비즈니스 통합</br>
- Redis의 _Redisson_ 라이브러리를 통해 **분산 락(`distributed lock`) 구현**</br>
- 동시 요청에 대한 **배타적 락(`xlock`) 사용 여부**에 따라 테스트코드 작성

**2024-06-19**
- Kafka의 `Consumer Group`에 대해 공부하기 위해서 신규 기능 추가</br>
- 상품 구매 비즈니스(`Order -> Payment -> Shipment`)와 전체 과정의 Notification을 구현

</br></br>

## 프로젝트 개요
- Terminal 에서 다음 명령어로 Docker-compose 파일을 실행 (zraft: docker-composer-cluster.yaml)
  ```
  docker-compose up -d
  ```

- **Github Actions을 이용한 테스트 자동화**

  </br></br>

### 상품 구매 비즈니스 설계

- 주문 생성(Order) : 주문 정보를 DB에 저장하고, 메시징 큐에 주문 생성 이벤트를 발행
- 재고 관리 서비스(Inventory) : 재고 수량을 조절 - **Redisson을 이용한 동시성(`Concurrency`) 제어**
- 알림 서비스(Notificaion) : 모든 이벤트를 구독해서 상태를 로그로 출력

### Kafka 사용 전후간 성능 비교
- `useCoupon *` : 함수들은 발급된 쿠폰들에 대해서 전체 총량을 줄이고, `Vendor`에게 비용을 지급
- `useCoupon`와 Kafka를 통해 작업하는 `useCouponToKafka` 간의 성능 테스트를 진행</br>
- 대용량 트래픽에 대해서 TPS, Latency 등의 성능 지표로 비교
  - [기술 블로그](https://blog.naver.com/downfa11/223474922882) 기록
    </br>

### 쿠폰 발급 과정에 대한 동시성(Concurrency) 제어 확인
Redis에서 제공하는 Redisson 라이브러리에서 분산 락(distributed lock) 제공
  - `tryLock(waitTime, leaseTime, TimeUnit)` : waitTime동안 Lock 점유 시도, leaseTime만큼 락을 사용하거나 lock 해제
  - lock 접근시 선행 쓰레드가 존재하면 waitTime동안 Lock 점유 대기
  - leaseTime만큼 지나면 lock이 해제되기 때문에 다른 쓰레드도 일정 시간이 지나면 Lock을 점유함

InventoryServiceTest의 `BuyWithoutLock`, `BuyWithLock` 간의 차이를 확인
  - Lock 사용시 동시 접근이 제한되어 원하는 로직이 잘 수행됨
  - Lock 미사용시 중복해서 개수를 소모하지 않는 경우를 확인

### 메시지 송수신간 Kafka의 트랜잭션 처리 범위에 대한 연구
본 내용은 블로그에 기록했습니다.

[기술 블로그](https://blog.naver.com/downfa11/223495519589) 


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
