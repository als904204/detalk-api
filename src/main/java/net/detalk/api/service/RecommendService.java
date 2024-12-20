package net.detalk.api.service;

import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.domain.CreateRecommend;
import net.detalk.api.domain.Recommend;
import net.detalk.api.repository.RecommendProductRepository;
import net.detalk.api.repository.RecommendRepository;
import net.detalk.api.support.TimeHolder;
import net.detalk.api.support.error.ApiException;
import net.detalk.api.support.error.ErrorCode;
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
    public void addRecommendation(Long postId, CreateRecommend createRecommend) {

        Instant now = timeHolder.now();
        Long memberId = createRecommend.getMemberId();
        String reason = createRecommend.getReason();

        // 추천 하려는 게시글이 존재하는지 검증
        productPostService.validatePostExists(postId);

        // 추천 이유 존재하면 그대로 사용, 없으면 DB 저장
        Long recommendId = findByReason(reason)
            .map(Recommend::getId)
            .orElseGet(() -> save(createRecommend, now).getId());

        // 중복 추천 예외
        if (recommendProductRepository.isAlreadyRecommended(memberId, recommendId, postId)) {
            log.warn("[addRecommendation] 중복 추천 시도 : 회원 ID={}, 게시글 ID={}, 추천 이유 ID={}, 추천 이유={}",
                memberId, postId, recommendId, reason);
            throw new ApiException(ErrorCode.CONFLICT);
        }

        // 게시글 추천, 게시글 연관관계 맺기
        recommendProductRepository.save(recommendId, postId, memberId, now);

        // 추천수 증가
        productPostService.incrementRecommendCount(postId);
    }

    public Recommend save(CreateRecommend createRecommend, Instant now) {
        return recommendRepository.save(createRecommend, now);
    }

    public Optional<Recommend> findByReason(String reason) {
        return recommendRepository.findByReason(reason);
    }

}
