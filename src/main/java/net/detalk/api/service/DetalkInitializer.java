package net.detalk.api.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DetalkInitializer implements ApplicationRunner {

    private final PricingPlanCache pricingPlanCache;

    private final DiscordService discordService;

    @Transactional
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 가격 정책 캐싱
        pricingPlanCache.loadPricingPlans();

        // 트랜잭션과 무관한 Discord 연동 초기화
        initializeDiscord();
    }

    private void initializeDiscord() {
        try {
            discordService.initialize();
        } catch (Exception e) {
            log.error("Discord 봇 초기화 실패: {}", e.getMessage(), e);
        }
    }

}
