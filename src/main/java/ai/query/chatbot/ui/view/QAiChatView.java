package ai.query.chatbot.ui.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;

@Route("chat")
@PermitAll
@PageTitle("Q-AI")
@Menu(order = 0, icon = "vaadin:clipboard-check", title = "Chat Bot")
public class QAiChatView extends VerticalLayout {

    //@Value("${qai.base-url}")
    String baseUrl = "http://localhost:8080";

    //@Value("${qai.chat-path}")
    String chatPath = "/ai/chat";

    private final WebClient webClient;

    private final Div chatHistory = new Div();

    private final Div chatPanel = new Div();
    private final TextArea inputArea = new TextArea();
    private final Button sendButton = new Button("Send");

    public QAiChatView() {
        this.webClient = WebClient.create(baseUrl);;
        setSizeFull();
        setPadding(true);
        setSpacing(false);
        getStyle().set("background-color", "#f0f2f5");

        // Title bar
        add(Collections.singleton(buildTitleBar()));

        // Chat panel setup
        chatPanel.getStyle().set("border", "1px solid #ccc");
        chatPanel.getStyle().set("padding", "10px");
        chatPanel.getStyle().set("background-color", "#ffffff");
        chatPanel.getStyle().set("overflow-y", "auto");
        chatPanel.getStyle().set("height", "70vh");
        chatPanel.getStyle().set("flex-grow", "1");
        chatPanel.getStyle().set("border-radius", "10px");

        //chatPanel.setWidthFull();
        chatPanel.setWidth("99%");

        // Input section
        inputArea.setPlaceholder("Type your message...");
        inputArea.setWidthFull();
        inputArea.setMinHeight("60px");

        sendButton.getStyle().set("margin-left", "10px");
        sendButton.getStyle().set("margin", "10px");
        //sendButton.getStyle().set("background-color", "#007bff");
        sendButton.addClickListener(e -> handleSend());

        // Pressing Enter sends, Shift+Enter creates newline
        sendButton.addClickShortcut(Key.ENTER).listenOn(inputArea);

        HorizontalLayout inputLayout = new HorizontalLayout(inputArea, sendButton);
        inputLayout.setWidthFull();
        inputLayout.setAlignItems(Alignment.END);

        add(chatPanel, inputLayout);
        expand(chatPanel);
    }

    private Component buildTitleBar() {
        HorizontalLayout titleBar = new HorizontalLayout();
        titleBar.setAlignItems(Alignment.CENTER);
        titleBar.setWidthFull();
        titleBar.getStyle()
                .set("background-color", "#2c3e50")
                .set("padding", "10px")
                .set("color", "#fff")
                .set("border-radius", "10px");

        //Image logo = new Image("images/logo.png", "Q-AI Logo"); // Place your logo in src/main/resources/META-INF/resources/images/logo.png
        //logo.setHeight("40px");

        H1 title = new H1("Q-AI");
        title.getStyle()
                .set("margin", "0")
                .set("color", "white")
                .set("font-size", "1.8rem");

        titleBar.add(title);
        return titleBar;
    }

    private void handleSend() {
        String prompt = inputArea.getValue().trim();
        if (prompt.isEmpty()) return;

        addUserMessage(prompt);
        inputArea.clear();

        try {
            String response = callChatAPI(prompt);
            addBotMessage(response);
        } catch (Exception ex) {
            addBotMessage("❌ Error: " + ex.getMessage());
        }
    }

    private void addUserMessage(String text) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setJustifyContentMode(JustifyContentMode.END);

        Div bubble = createMessageBubble(text, "#DCF8C6", "right");
        layout.add(bubble);

        chatPanel.add(layout);
        scrollToBottom();
    }

    private void addBotMessage(String text) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setJustifyContentMode(JustifyContentMode.START);

        Div bubble = createMessageBubble(text, "#FFFFFF", "left");
        layout.add(bubble);

        chatPanel.add(layout);
        scrollToBottom();
    }

    private Div createMessageBubble(String text, String bgColor, String align) {
        Div bubble = new Div(new Span(text));
        bubble.getStyle()
                .set("background-color", bgColor)
                .set("padding", "10px")
                .set("border-radius", "10px")
                .set("max-width", "60%")
                .set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)");

        if ("right".equals(align)) {
            bubble.getStyle().set("border-top-right-radius", "0");
        } else {
            bubble.getStyle().set("border-top-left-radius", "0");
        }

        return bubble;
    }

    private void scrollToBottom() {
        getElement().executeJs("var c = $0; c.scrollTop = c.scrollHeight;", chatPanel.getElement());
    }

    private String callChatAPI(String message) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(chatPath)
                        .queryParam("prompt", message)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
