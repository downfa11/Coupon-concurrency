package ns.example.kafka_querydsl.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCoupon is a Querydsl query type for Coupon
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCoupon extends EntityPathBase<Coupon> {

    private static final long serialVersionUID = -1779697602L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCoupon coupon = new QCoupon("coupon");

    public final NumberPath<Integer> count = createNumber("count", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath type = createString("type");

    public final NumberPath<Double> value = createNumber("value", Double.class);

    public final QVendor vendor;

    public QCoupon(String variable) {
        this(Coupon.class, forVariable(variable), INITS);
    }

    public QCoupon(Path<? extends Coupon> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCoupon(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCoupon(PathMetadata metadata, PathInits inits) {
        this(Coupon.class, metadata, inits);
    }

    public QCoupon(Class<? extends Coupon> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.vendor = inits.isInitialized("vendor") ? new QVendor(forProperty("vendor")) : null;
    }

}

