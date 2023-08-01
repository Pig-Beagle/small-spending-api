package com.zzdd.smallspending.post;

import com.zzdd.smallspending.token.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService{

    private final PostRepository postRepository;
    private final JwtUtil jwtUtil;

    @Override
    public int write(String authorization, PostDto postDto) {
        String token = authorization.split(" ")[1];
        Integer userNo = jwtUtil.getuserNo(token);
        postDto.setMemberNo(userNo);

        return postRepository.insertPost(postDto);
    }

    @Override
    public List<PostDto> list(PageDto pageDto) {
        return postRepository.selectList(pageDto);
    }
}