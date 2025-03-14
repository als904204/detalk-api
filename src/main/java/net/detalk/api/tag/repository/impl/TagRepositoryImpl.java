package net.detalk.api.tag.repository.impl;

import static net.detalk.jooq.tables.JTag.TAG;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.detalk.api.tag.domain.Tag;
import net.detalk.api.tag.repository.TagRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class TagRepositoryImpl implements TagRepository {

    private final DSLContext dsl;

    public Optional<Tag> findByName(String name) {
        return dsl.selectFrom(TAG)
            .where(TAG.NAME.eq(name))
            .fetchOptionalInto(Tag.class);
    }

    public Tag save(Tag tag) {
        return dsl.insertInto(TAG)
            .set(TAG.NAME, tag.getName())
            .returning()
            .fetchOneInto(Tag.class);
    }

    public List<Tag> findAll() {
        return dsl.selectFrom(TAG)
            .fetchInto(Tag.class);
    }
}
