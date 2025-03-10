package net.detalk.api.post.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductPostSnapshotTag {

    private Long id;
    private Long postId;
    private Long tagId;

    @Builder
    public ProductPostSnapshotTag(Long id, Long postId, Long tagId) {
        this.id = id;
        this.postId = postId;
        this.tagId = tagId;
    }

    public ProductPostSnapshotTag(Long postId, Long tagId) {
        this.postId = postId;
        this.tagId = tagId;
    }
}
