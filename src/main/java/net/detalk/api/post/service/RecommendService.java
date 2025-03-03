package net.detalk.api.post.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.post.controller.v1.request.CreateRecommendRequest;
import net.detalk.api.post.domain.Recommend;
import net.detalk.api.post.domain.RecommendProduct;
import net.detalk.api.post.domain.exception.DuplicateRecommendationException;
import net.detalk.api.post.repository.RecommendProductRepository;
import net.detalk.api.post.repository.RecommendRepository;
import net.detalk.api.support.util.TimeHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class RecommendService {

    private final RecommendRepository recommendRepository;
    private final RecommendProductRepository recommendProductRepository;
    private final ProductPostService productPostService;
    private final TimeHolder timeHolder;

    @Transactional
    public void addRecommendation(Long postId, Long memberId, CreateRecommendRequest createRecommendRequest) {

        // 추천 하려는 게시글이 존재하는지 검증
        productPostService.validatePostExists(postId);

        Instant now = timeHolder.now();
        // 사용자가 입력한 “추천 내용” (예: 비용이 싸고 디자인이 깔끔해요)
        String content = createRecommendRequest.content();
        // 기본 추천 내용 목록 (예: 디자인이 이뻐요, 가격이 저렴해요)
        List<String> reasons = createRecommendRequest.reasons();

        List<RecommendProduct> recommendProducts = new ArrayList<>();

        for (String reason : reasons) {

            // 추천 이유 존재하면 그대로 사용, 없으면 DB 저장
            Long recommendId = findByReason(reason)
                .map(Recommend::getId)
                .orElseGet(() -> save(reason, now).getId());

            // 중복 추천 예외
            if (recommendProductRepository.isAlreadyRecommended(memberId, recommendId, postId)) {
                log.warn("[addRecommendation] 중복 추천 시도 : 회원 ID={}, 게시글 ID={}, 추천 이유 ID={}, 추천 이유={}",
                    memberId, postId, recommendId, reason);
                throw new DuplicateRecommendationException(memberId, postId, recommendId, reason);
            }

            RecommendProduct recommendProduct = RecommendProduct.builder()
                .recommendId(recommendId)
                .productPostId(postId)
                .memberId(memberId)
                .content(content)
                .createdAt(now)
                .build();

            recommendProducts.add(recommendProduct);
        }

        // 게시글 추천 <==> 게시글 연관관계 맺기
        recommendProductRepository.saveAll(recommendProducts);

        // 추천수 증가
        productPostService.incrementRecommendCount(postId, recommendProducts.size());
    }

    private Recommend save(String reason, Instant now) {
        return recommendRepository.save(reason, now);
    }

    public Optional<Recommend> findByReason(String reason) {
        return recommendRepository.findByReason(reason);
    }

}
