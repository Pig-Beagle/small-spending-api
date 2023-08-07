package com.zzdd.smallspending.member;

import com.zzdd.smallspending.config.RedisRepository;
import com.zzdd.smallspending.mail.EmailRepository;
import com.zzdd.smallspending.token.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final EmailRepository emailRepository;
    private final RedisRepository redisRepository;
    private final JwtUtil jwtUtil;

    @Override
    public int signUp(MemberDto memberDto) {

        if(isIdExist(memberDto.getId())){
            return 0;
        }

        if (isNickExist(memberDto.getNick())) {
            return 0;
        }

        memberDto.setPwd(encodePwd(memberDto.getPwd()));

        return memberRepository.insertMember(memberDto);
    }

    @Override
    public int deleteMember(String authorization) {
        String token = authorization.split(" ")[1];
        String userId = jwtUtil.getUserId(token);
        MemberDto member = memberRepository.selectOneById(userId);

        if(member == null){
            return 0;
        }
        int result = memberRepository.deleteMember(member);

        authService.logout(authorization);

        return result;
    }

    @Override
    public MemberDto myPage(String authorization) {
        String token = authorization.split(" ")[1];
        String userId = jwtUtil.getUserId(token);

        return memberRepository.selectOneById(userId);
    }

    @Override
    public int editMyPage(String authorization, String nick, String introduce) {
        String token = authorization.split(" ")[1];
        String userId = jwtUtil.getUserId(token);

        boolean nickExist = isNickExist(nick);

        if(!nickExist){
            return 0;
        }

        if(nick == null || nick.equals("")){
            return 0;
        }

        return memberRepository.updateMyPage(userId, nick, introduce);
    }

    @Override
    public int introduce(String authorization, String introduce) {
        String token = authorization.split(" ")[1];
        String userId = jwtUtil.getUserId(token);
        return memberRepository.updateIntroduce(userId, introduce);
    }

    @Override
    public boolean validatePwd(String authorization, String pwd) {
        String token = authorization.split(" ")[1];
        String userId = jwtUtil.getUserId(token);

        MemberDto selectMember = memberRepository.selectOneById(userId);

        return passwordEncoder.matches(pwd, selectMember.getPwd());
    }

    @Override
    public boolean sendMail(String userId) {
        if(!isIdExist(userId)){
            return false;
        }
        emailRepository.sendEmail(userId);
        return true;
    }

    @Override
    public boolean validateNum(String userId, String num) {
        String numData = redisRepository.getNum(userId);
        boolean result = num.equals(numData);

        if(!result){
            return false;
        }

        redisRepository.deleteNum(userId);
        return true;
    }

    @Override
    public int resetPwd(MemberDto memberDto) {
        memberDto.setPwd(encodePwd(memberDto.getPwd()));
        return memberRepository.updatePwd(memberDto);
    }

    @Override
    public boolean isIdExist(String id){
        MemberDto memberDto = memberRepository.selectOneById(id);
        return memberDto == null;
    }

    @Override
    public boolean isNickExist(String nick){
        MemberDto memberDto = memberRepository.selectOneByNick(nick);
        return memberDto == null;
    }

    private String encodePwd(String pwd){
        return passwordEncoder.encode(pwd);
    }
}
