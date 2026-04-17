package com.example.ai.service;

package com.example.ai.stream;

import com.example.ai.dto.StreamChatResponse;
import dev.langchain4j.service.TokenStream;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class SseTokenStream implements TokenStream {

    private final SseEmitter emitter;
    private final Long conversationId;
    private final Consumer<String> onCompleteCallback;

    private final StringBuilder full = new StringBuilder();

    @Override
    public void onNext(String token) {
        try {
            full.append(token);

            StreamChatResponse response = StreamChatResponse.builder()
                    .type("token")
                    .content(token)
                    .conversationId(conversationId)
                    .build();

            emitter.send(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onComplete() {
        try {
            String answer = full.toString();

            // 回调（保存数据库）
            onCompleteCallback.accept(answer);

            StreamChatResponse response = StreamChatResponse.builder()
                    .type("complete")
                    .content(answer)
                    .conversationId(conversationId)
                    .build();

            emitter.send(response);
            emitter.complete();

        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    @Override
    public void onError(Throwable error) {
        try {
            StreamChatResponse response = StreamChatResponse.builder()
                    .type("error")
                    .content(error.getMessage())
                    .conversationId(conversationId)
                    .build();

            emitter.send(response);
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }
}
