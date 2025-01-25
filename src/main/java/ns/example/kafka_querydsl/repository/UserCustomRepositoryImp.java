package ns.example.kafka_querydsl.repository;


import com.querydsl.jpa.impl.JPAQueryFactory;
import ns.example.kafka_querydsl.domain.QUser;
import ns.example.kafka_querydsl.domain.User;
import org.springframework.stereotype.Repository;

@Repository
public class UserCustomRepositoryImp implements UserCustomRepository{

    private final JPAQueryFactory factory;
    private final QUser qUser;

    public UserCustomRepositoryImp(JPAQueryFactory jpaQueryFactory) {
        this.factory = jpaQueryFactory;
        this.qUser= QUser.user;
    }

    @Override
    public User findByName(String name) {
        return null;
        /*return jpaQueryFactory.select(Projections.fields(BoardList.class,
                        qBoard.boardId,
                        qBoard.category.id,
                        qBoard.sortStatus,
                        qBoard.region,
                        qBoard.membership.nickname.as("nickname"),
                        qBoard.title,
                        qBoard.hits,
                        qBoard.createdAt
                ))
                .from(qBoard)
                .leftJoin(qBoard.membership)
                .leftJoin(qBoard.category)
                .where(inSortStatus(sortStatuses)
                        ,inCategory(categories)
                        ,inRegion(regions)
                        ,qBoard.title.contains(title))
                .orderBy(qBoard.createdAt.desc())
                .offset(offset)
                .limit(10).fetch();
    }*/
    }
}
