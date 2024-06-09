package ns.example.kafka_querydsl.repository;


import ns.example.kafka_querydsl.entity.User;

public interface UserCustomRepository {
    User findByName(String name);
}
