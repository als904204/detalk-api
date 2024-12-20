package net.detalk.api.repository;

import static net.detalk.jooq.tables.JProduct.PRODUCT;

import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.detalk.api.domain.Product;
import net.detalk.api.domain.ProductCreate;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ProductRepository {

    private final DSLContext dsl;

    public Optional<Product> findByName(String name) {
        return dsl.selectFrom(PRODUCT)
            .where(PRODUCT.NAME.eq(name))
            .fetchOptionalInto(Product.class);
    }

    public Product save(ProductCreate productCreate, Instant now) {
        return dsl.insertInto(PRODUCT)
            .set(PRODUCT.NAME, productCreate.getName())
            .set(PRODUCT.CREATED_AT, now)
            .returning()
            .fetchOneInto(Product.class);
    }
}
