package ai.query.chatbot.ui.view;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.web.reactive.function.client.WebClient;

//@Route("chat-old")
//@PageTitle("Chat Bot - old")
//@Menu(order = 0, icon = "vaadin:clipboard-check", title = "Chat Bot - old")
//@PermitAll // When security is enabled, allow all authenticated users
public class ChatView extends VerticalLayout {

    private final WebClient webClient;
    private final VerticalLayout chatArea;
    private final TextField inputField;

    public ChatView() {
        this.webClient = WebClient.create("http://localhost:8080"); // Point to your existing API base URL
        this.chatArea = new VerticalLayout();
        this.inputField = new TextField();

        inputField.setPlaceholder("Type your message...");
        inputField.setWidthFull();
        inputField.addKeyPressListener(Key.ENTER, e -> sendMessage());

        Button sendButton = new Button("Send", e -> sendMessage());

        chatArea.setWidthFull();
        chatArea.setHeight("500px");
        chatArea.getStyle().set("overflow-y", "auto");
        chatArea.setSpacing(true);

        add(chatArea, inputField, sendButton);
        setSizeFull();
    }

    private void sendMessage() {
        String userInput = inputField.getValue().trim();
        if (userInput.isEmpty()) return;

        chatArea.add(new Div(new Div("👤 You: " + userInput)));
        inputField.clear();

        webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/ai/chat") // your actual endpoint
                        .queryParam("prompt", userInput)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(response -> {
                    getUI().ifPresent(ui -> ui.access(() ->
                            chatArea.add(new Div(new Div("🤖 Bot: " + response)))
                    ));
                }, error -> {
                    getUI().ifPresent(ui -> ui.access(() ->
                            chatArea.add(new Div(new Div("❌ Error: " + error.getMessage())))
                    ));
                });
    }
}
