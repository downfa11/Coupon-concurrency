package ns.example.kafka_querydsl.repository;


import ns.example.kafka_querydsl.domain.User;

public interface UserCustomRepository {
    User findByName(String name);
}
