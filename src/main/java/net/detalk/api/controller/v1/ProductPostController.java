package net.detalk.api.controller.v1;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import net.detalk.api.controller.v1.request.UpdateProductPostRequest;
import net.detalk.api.controller.v1.response.CreateProductPostResponse;
import net.detalk.api.controller.v1.response.GetProductPostResponse;
import net.detalk.api.controller.v1.request.CreateRecommendRequest;
import net.detalk.api.service.RecommendService;
import net.detalk.api.support.CursorPageData;
import net.detalk.api.controller.v1.request.CreateProductPostRequest;
import net.detalk.api.service.ProductPostService;
import net.detalk.api.support.security.HasRole;
import net.detalk.api.support.security.SecurityRole;
import net.detalk.api.support.security.SecurityUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products/posts")
@RequiredArgsConstructor
public class ProductPostController {

    private final ProductPostService productPostService;

    private final RecommendService recommendService;

    @PostMapping
    public ResponseEntity<CreateProductPostResponse> create(
        @Valid @RequestBody CreateProductPostRequest createProductPostRequest,
        @HasRole(SecurityRole.MEMBER) SecurityUser user
        ) {
        Long productPostId = productPostService.create(createProductPostRequest, user.getId());
        return ResponseEntity.ok(new CreateProductPostResponse(productPostId));
    }

    @GetMapping
    public ResponseEntity<CursorPageData<GetProductPostResponse>> getProductPosts(
        @RequestParam(name = "size", defaultValue = "5") @Max(20) int pageSize,
        @RequestParam(name = "startId", required = false) Long nextId) {
        CursorPageData<GetProductPostResponse> result = productPostService.getProductPosts(pageSize,
            nextId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetProductPostResponse> getProductPost(@PathVariable("id") Long id) {
        GetProductPostResponse result = productPostService.getProductPostDetailsById(id);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateProductPost(
        @PathVariable("id") Long id,
        @Valid @RequestBody UpdateProductPostRequest updateProductPostRequest,
        @HasRole(SecurityRole.MEMBER) SecurityUser user
    ) {
        productPostService.update(id, updateProductPostRequest, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/recommend")
    public ResponseEntity<Void> createRecommend(
        @PathVariable("id") Long postId,
        @Valid @RequestBody CreateRecommendRequest createRecommendRequest,
        @HasRole(SecurityRole.MEMBER) SecurityUser user
        ) {
        recommendService.addRecommendation(postId, user.getId(), createRecommendRequest);
        return ResponseEntity.noContent().build();
    }
}
