package net.detalk.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.controller.v1.request.UpdateProfileRequest;
import net.detalk.api.controller.v1.response.GetMemberPublicProfileResponse;
import net.detalk.api.domain.Member;
import net.detalk.api.domain.MemberDetail;
import net.detalk.api.domain.MemberProfile;
import net.detalk.api.domain.exception.InvalidMemberStatusException;
import net.detalk.api.domain.exception.MemberNeedSignUpException;
import net.detalk.api.domain.exception.MemberNotFoundException;
import net.detalk.api.domain.exception.MemberProfileNotFoundException;
import net.detalk.api.domain.exception.UserHandleDuplicatedException;
import net.detalk.api.repository.MemberProfileRepository;
import net.detalk.api.repository.MemberRepository;
import net.detalk.api.support.TimeHolder;
import net.detalk.api.support.UUIDGenerator;
import net.detalk.api.support.error.InvalidStateException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final TimeHolder timeHolder;
    private final UUIDGenerator uuidGenerator;

    public MemberDetail me(Long memberId) {
        Member member = findMemberById(memberId);

        if (member.isPendingExternalMember()) {
            log.debug("[me] 회원가입이 필요한 외부 회원");
            throw new MemberNeedSignUpException(memberId, member.getLoginType(),
                member.getStatus());
        }

        return memberProfileRepository.findWithAvatarByMemberId(member.getId()).orElseThrow(
            () -> new InvalidStateException("[me] 회원 " + member.getId() + "의 프로필이 존재하지 않습니다")
        );
    }

    @Transactional
    public MemberDetail registerProfile(Long memberId, String userhandle, String nickname) {
        log.debug("[registerProfile] userhandle 중복검사 {}", userhandle);
        memberProfileRepository.findByUserHandle(userhandle).ifPresent(m -> {
            log.error("[registerProfile] 이미 존재하는 userhandle({}) 입니다.", userhandle);
            throw new UserHandleDuplicatedException(userhandle);
        });

        Member member = findMemberById(memberId);

        if (!member.isPendingExternalMember()) {
            throw new InvalidMemberStatusException(memberId, member.getStatus());
        }

        member.active(timeHolder);
        memberRepository.update(member);
        MemberProfile memberProfile = findProfileByMemberId(member.getId());

         memberProfile = memberProfileRepository.update(
            MemberProfile.builder()
                .id(memberProfile.getId())
                .memberId(member.getId())
                .userhandle(userhandle)
                .nickname(nickname)
                .avatarId(memberProfile.getAvatarId())
                .updatedAt(timeHolder.now())
                .build()
        );

        return MemberDetail.builder()
            .id(member.getId())
            .userhandle(memberProfile.getUserhandle())
            .nickname(memberProfile.getNickname())
            .description(memberProfile.getDescription())
            .build();
    }

    @Transactional
    public void updateProfile(Long memberId, UpdateProfileRequest updateRequest) {

        Member member = findMemberById(memberId);
        MemberProfile memberProfile = findProfileByMemberId(member.getId());

        /**
         * 새로운 userHandle 요청이라면, 이미 존재하는지 검사한다.
         */
        if (!memberProfile.hasSameUserHandle(updateRequest.userandle())) {
            checkDuplicateUserHandle(updateRequest.userandle());
        }

        /**
         * avatarId는 null일 경우 기존꺼 사용
         * 새로 요청올 경우, 새거 사용
         */
        memberProfile = memberProfile.update(
            updateRequest,
            updateRequest.avatarId() != null
                ? uuidGenerator.fromString(updateRequest.avatarId())
                : memberProfile.getAvatarId(),
            timeHolder.now()
        );

        memberProfileRepository.update(memberProfile);
    }

    /**
     * userhandle로 회원 프로필 조회
     */
    @Transactional(readOnly = true)
    public GetMemberPublicProfileResponse getMemberDetailByUserhandle(String userhandle) {

        MemberProfile memberProfile = findProfileByUserhandle(userhandle);

        MemberDetail memberDetail = memberProfileRepository.findWithAvatarByMemberId(
                memberProfile.getMemberId())
            .orElseThrow(()->{
                log.error("[GetMemberPublicProfileResponse] 존재하지 않는 회원 프로필 입니다. memberId={}",
                    memberProfile.getMemberId());return new MemberProfileNotFoundException();
            });

        return GetMemberPublicProfileResponse.builder()
            .userhandle(memberProfile.getUserhandle())
            .nickname(memberProfile.getNickname())
            .description(memberProfile.getDescription())
            .avatarUrl(memberDetail.getAvatarUrl())
            .build();
    }

    public Member findMemberById(Long id) {
        return memberRepository.findById(id).orElseThrow(() -> {
            log.error("[findMemberById] 회원 ID {}는 존재하지 않습니다", id);
            return new MemberNotFoundException();
        });
    }

    public MemberProfile findProfileByMemberId(Long memberId) {
        return memberProfileRepository.findByMemberId(memberId)
            .orElseThrow(()->{
                log.error("[findProfileByMemberId] 존재하지 않는 회원 프로필 입니다. memberId={}", memberId);
                return new MemberProfileNotFoundException();
            });
    }

    public void checkDuplicateUserHandle(String userHandle) {
        if (memberProfileRepository.existsByUserHandle(userHandle)) {
            log.error("[duplicateUserHandleValidation] 이미 존재하는 userhandle입니다. userhandle={}",
                userHandle);
            throw new UserHandleDuplicatedException(userHandle);
        }
    }

    public Long findMemberIdByUserHandle(String userHandle) {
        return findProfileByUserhandle(userHandle).getId();
    }

    public MemberProfile findProfileByUserhandle(String userhandle) {
        return memberProfileRepository.findByUserHandle(userhandle)
            .orElseThrow(() -> {
                    log.error("[findMemberIdByUserHandle] 회원 userHandle {}은 존재하지 않는 회원입니다", userhandle);
                    return new MemberProfileNotFoundException(userhandle);
                }
            );
    }

}
