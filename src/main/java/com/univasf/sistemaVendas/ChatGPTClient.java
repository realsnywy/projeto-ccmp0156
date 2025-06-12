package com.univasf.sistemaVendas;

import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;

import java.util.List;

public class ChatGPTClient {
    private final OpenAiService service;

    public ChatGPTClient(String apiKey) {
        this.service = new OpenAiService(apiKey);
    }

    public String gerarInsight(String prompt) {
        ChatMessage userMsg = new ChatMessage("user", prompt);
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")                 // ou "gpt-3.5-turbo"
                .messages(List.of(userMsg))
                .build();

        List<ChatCompletionChoice> choices = service.createChatCompletion(request).getChoices();
        return choices.isEmpty()
                ? ""
                : choices.get(0).getMessage().getContent().trim();
    }


/*
ChatGPTClient ai = new ChatGPTClient(System.getenv("OPENAI_API_KEY"));
String prompt = "Aqui está o perfil do cliente CRM:\n" + crm.gerarPerfilClienteCRM(1)
    + "\n\nCom base nisso, identifique segmentos inexplorados e sugira ações de marketing.";
String resposta = ai.gerarInsight(prompt);
System.out.println(resposta);
 */
}
