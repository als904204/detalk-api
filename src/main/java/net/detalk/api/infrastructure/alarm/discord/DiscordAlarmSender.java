package net.detalk.api.infrastructure.alarm.discord;

import java.util.EnumSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.alarm.domain.AlarmErrorMessage;
import net.detalk.api.alarm.service.AlarmSender;
import net.detalk.api.support.util.EnvironmentHolder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class DiscordAlarmSender implements AlarmSender {

    private final DiscordConfig config;
    private final EnvironmentHolder env;

    private JDA jda;
    private TextChannel defaultChannel;
    private String activeProfile;

    @Override
    public void initialize() {
        try {
            jda = JDABuilder.createDefault(config.getToken())
                .setActivity(Activity.playing("알람봇"))
                .disableCache(
                    EnumSet.of(
                        CacheFlag.ACTIVITY,
                        CacheFlag.EMOJI,
                        CacheFlag.CLIENT_STATUS,
                        CacheFlag.MEMBER_OVERRIDES,
                        CacheFlag.STICKER
                    ))
                .setMemberCachePolicy(MemberCachePolicy.NONE)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build();

            // JDA가 완전히 초기화될 때까지 대기
            jda.awaitReady();

            // Profile 따라 동적으로 channelId 할당
            defaultChannel = jda.getTextChannelById(config.getChannelId());

            if (defaultChannel == null) {
                log.warn("디스코드 채널을 찾지 못했습니다. channelId={}", config.getChannelId());
            }

            activeProfile = env.getActiveProfile();

            log.info("Discord JDA initialized successfully in '{}' profile. (Channel ID: {})",
                activeProfile, config.getChannelId());

            if ("prod".equals(activeProfile)) {
                sendMessage("프로덕션 환경으로 Discord봇이 성공적으로 실행되었습니다.");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("JDA awaitReady was interrupted.", e);
        }

    }

    @Override
    public void sendMessage(String message) {
        if (!isReady()) {
            log.warn("JDA not initialized or channel is null. Cannot send message: {}", message);
            return;
        }
        sendToChannel(message);
    }

    @Override
    public void sendError(AlarmErrorMessage message) {
        if (!isReady()) {
            log.warn("JDA not initialized or channel is null. Cannot send message: {}", message.toString());
            return;
        }
        String formattedMessage = message.toString();

        sendToChannel(formattedMessage);
    }

    private boolean isReady() {
        return (jda != null && defaultChannel != null);
    }

    private void sendToChannel(String message) {
        defaultChannel.sendMessage(message).queue(
            success -> log.debug("디스코드 알림 전송 성공"),
            failure -> log.error("디스코드 알림 전송 실패", failure)
        );
    }


}
