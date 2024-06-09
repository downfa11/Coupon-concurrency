package ns.example.kafka_querydsl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ns.example.kafka_querydsl.entity.User;


@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserCustomRepository{
}
