package com.zzdd.smallspending.chat;

import com.zzdd.smallspending.common.ApiMessage;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "채팅방 조회", description = "채팅방 조회 메소드 입니다.")
    @GetMapping("/{postNo}")
    public ResponseEntity<ApiMessage<Page<ChatDto>>> getOrCreateChatRoom(@PathVariable("postNo") int postNo, int page) {
        Page<ChatDto> chatList = chatService.findByRoomNo(postNo, page);
        if(chatList == null){
            return ResponseEntity.badRequest().body(new ApiMessage<>(HttpStatus.OK, "채팅방 조회 실패", null));
        }
        return ResponseEntity.ok(new ApiMessage<>(HttpStatus.OK, "채팅 조회 성공", chatList));
    }

    @Operation(summary = "메세지 저장", description = "메세지 저장 메소드 입니다.")
    @MessageMapping("/pub/chat/room/{postNo}")
    @SendTo("/sub/chat/room/{postNo}")
    public ChatDto message(@DestinationVariable("postNo") int postNo, ChatDto chatDto) {
        return chatService.saveMessage(postNo, chatDto);
    }

    @Operation(summary = "메세지 수정", description = "메세지 수정 메소드 입니다.")
    @PatchMapping()
    public ResponseEntity<ApiMessage<ChatDto>> updateChat(HttpServletRequest request, @RequestBody ChatDto chatDto) {
        String authorization = request.getHeader("Authorization");
        if(authorization == null){
            return ResponseEntity.ok(new ApiMessage<>(HttpStatus.BAD_REQUEST, "채팅 수정 실패", null));
        }
        ChatDto chat = chatService.updateChat(authorization, chatDto);
        if(chat == null){
            return ResponseEntity.badRequest().body(new ApiMessage<>(HttpStatus.BAD_REQUEST, "채팅 수정 실패", null));
        }
        return ResponseEntity.ok(new ApiMessage<>(HttpStatus.OK, "채팅 수정 성공", chat));
    }

    @Operation(summary = "메세지 삭제", description = "메세지 삭제 메소드 입니다.")
    @DeleteMapping()
    public ResponseEntity<ApiMessage<Boolean>> deleteChat(HttpServletRequest request, @RequestBody ChatDto chatDto) {
        String authorization = request.getHeader("Authorization");
        if(authorization == null){
            return ResponseEntity.ok(new ApiMessage<>(HttpStatus.BAD_REQUEST, "채팅 삭제 실패", false));
        }

        int result = chatService.deleteChat(authorization, chatDto);

        if(result != 1){
            return ResponseEntity.badRequest().body(new ApiMessage<>(HttpStatus.BAD_REQUEST, "채팅 삭제 실패", false));
        }
        return ResponseEntity.ok(new ApiMessage<>(HttpStatus.OK, "채팅 삭제 성공", true));
    }

}
