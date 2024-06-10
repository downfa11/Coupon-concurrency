# 토이프로젝트 - Kafka와 QueryDSL
Apeche Kafka와 queryDSL 실습을 통해서 좀 더 손에 익도록 연습하기 위해 준비한 공부용 프로젝트.  
</br>근데 QueryDSL은 아직 진행중
</br></br>

**Kafka 사용 전 후의 성능 비교**
- `useCoupon *` 함수들은 발급된 쿠폰들에 대해서 전체 총량을 줄이고, 쿠폰 발급사(`Vendor`)에게 비용을 지급한다.
- `useCoupon`와 Kafka를 통해 작업하는 `useCouponToKafka` 간의 성능 테스트를 진행
- 대용량 트래픽에 대해서 TPS, Latency 등의 성능 지표로 비교  
</br>

**동시성(Concurrency) 제어 확인**
- `@KafkaListener()`의 `concurrency` 인자를 통해서 동시에 처리할 쓰레드를 지정
- 전체 쿠폰 개수의 총량이 지켜지는지 동시에 검사
  </br></br></br>

### 프로젝트 주의사항
- docker-compose로 Zookeeper, Kafka, kafka-ui, mysql 컴포넌트 실행  

- 프로젝트는 Junit 테스트코드를 위해 로컬에서 수행  
  
- Terminal 에서 다음 명령어로 Docker-compose 파일을 실행  
  ```
  docker-compose up -d
  ```
  

  </br></br></br>
  
## 프로젝트 구성

`generateRandomCoupon`, `getRandomUser` : 랜덤으로 사용자 혹은 쿠폰을 발급하는 함수

`getUserCoupons` : 해당 사용자가 가진 쿠폰을 표시하는 함수

`useCoupon` : 쿠폰을 소모해서 사용자의 coupon에 등록하는 함수

`payToVendor` : 쿠폰 소모시 업체에 비용을 지급하는 함수
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