package org.example.dem;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChatClientController {
    private static final Logger logger = LoggerFactory.getLogger(ChatClientController.class);
    @FXML
    private TextArea chatArea; // Область для отображения чата
    @FXML
    private TextField messageField; // Поле для ввода сообщений
    @FXML
    private Button sendButton; // Кнопка для отправки сообщений
    @FXML
    private Label userCountLabel; // Метка для отображения количества пользователей онлайн
    @FXML
    private Button logoutButton; // Кнопка для выхода из чата
    @FXML
    private ComboBox<String> userComboBox; // Выпадающий список для выбора пользователя
    @FXML
    private Button backToGeneralButton; // Кнопка для возврата в общий чат

    private Socket socket; // Сокет для подключения к серверу
    private BufferedReader in; // Поток для чтения данных с сервера
    private PrintWriter out; // Поток для отправки данных на сервер
    private String username; // Имя пользователя
    private List<String> onlineUsers = new ArrayList<>(); // Список пользователей онлайн
    private String selectedRecipient = null; // Выбранный получатель сообщения

    @FXML
    public void initialize() {
        // Устанавливает обработчик событий для кнопки выхода
        logoutButton.setOnAction(event -> logout());
        // Устанавливает обработчик событий для выпадающего списка пользователей
        userComboBox.setOnAction(event -> updateRecipient());
        // Устанавливает обработчик событий для кнопки возврата в общий чат
        backToGeneralButton.setOnAction(event -> backToGeneralChat());
    }

    public void connectToServer() {
        try {
            // Создает сокет для подключения к серверу
            socket = new Socket("localhost", 12345);
            // Создает поток для чтения данных с сервера
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Создает поток для отправки данных на сервер
            out = new PrintWriter(socket.getOutputStream(), true);
            // Отправляет сообщение о входе пользователя на сервер
            out.println(new JSONObject().put("type", "login").put("username", username).toString());

            // Создает новый поток для чтения сообщений с сервера
            new Thread(() -> {
                try {
                    String message;
                    // Читает сообщения с сервера в цикле
                    while ((message = in.readLine()) != null) {
                        // Парсит JSON сообщение
                        JSONObject jsonMessage = new JSONObject(message);
                        String type = jsonMessage.getString("type");
                        // Обрабатывает сообщение в зависимости от типа
                        if (type.equals("user_count")) {
                            // Обновляет количество пользователей онлайн
                            Platform.runLater(() -> updateUserCount(jsonMessage.getInt("count")));
                        } else if (type.equals("user_list")) {
                            // Обновляет список пользователей онлайн
                            Platform.runLater(() -> updateUserList(jsonMessage.getString("users")));
                        } else if (type.equals("message")) {
                            // Отображает полученное сообщение в области чата
                            Platform.runLater(() -> chatArea.appendText(jsonMessage.getString("content") + "\n"));
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }).start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void sendMessage() {
        // Получает текст сообщения из поля ввода
        String message = messageField.getText();
        if (!message.isEmpty()) {
            // Создает JSON объект для отправки сообщения
            JSONObject jsonMessage = new JSONObject();
            jsonMessage.put("type", "message");
            jsonMessage.put("content", message);
            // Если выбран получатель, добавляет его в сообщение
            if (selectedRecipient != null && !selectedRecipient.isEmpty()) {
                jsonMessage.put("recipient", selectedRecipient);
            }
            // Отправляет сообщение на сервер
            out.println(jsonMessage.toString());
            // Очищает поле ввода
            messageField.clear();
            logger.info("Sent message: {}", jsonMessage.toString());
        }
    }

    public void setUsername(String username) {
        // Устанавливает имя пользователя
        this.username = username;
    }

    private void updateUserCount(int userCount) {
        // Обновляет метку с количеством пользователей онлайн
        userCountLabel.setText("Users Online: " + userCount);
    }

    private void updateUserList(String userList) {
        // Очищает список пользователей онлайн
        onlineUsers.clear();
        // Добавляет всех пользователей из полученного списка
        onlineUsers.addAll(List.of(userList.split(",")));
        // Фильтрует список, исключая текущего пользователя
        List<String> filteredUsers = onlineUsers.stream()
                .filter(user -> !user.equals(username))
                .collect(Collectors.toList());
        // Обновляет выпадающий список пользователей
        userComboBox.getItems().setAll(filteredUsers);
    }

    private void updateRecipient() {
        // Получает выбранного получателя из выпадающего списка
        selectedRecipient = userComboBox.getValue();
        if (selectedRecipient != null && !selectedRecipient.isEmpty()) {
            // Обновляет подсказку в поле ввода
            messageField.setPromptText("Type your message to " + selectedRecipient + "...");
        } else {
            // Сбрасывает подсказку в поле ввода
            messageField.setPromptText("Type your message here...");
            selectedRecipient = null;
        }
    }

    @FXML
    private void backToGeneralChat() {
        // Сбрасывает выбранного получателя
        selectedRecipient = null;
        // Сбрасывает выбранное значение в выпадающем списке
        userComboBox.setValue(null);
        // Сбрасывает подсказку в поле ввода
        messageField.setPromptText("Type your message here...");
    }

    @FXML
    private void logout() {
        // Создает диалоговое окно для подтверждения выхода
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Exit");
        alert.setHeaderText("Хотите покинуть Шарарам?");
        alert.setContentText("Вы перестанете быть смешариком...");

        // Отображает диалоговое окно и ждет ответа пользователя
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Закрывает соединение с сервером
                closeConnection();
                // Закрывает текущее окно
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                stage.close();
            }
        });
    }

    private void closeConnection() {
        try {
            // Закрывает поток для отправки данных
            if (out != null) {
                out.close();
            }
            // Закрывает поток для чтения данных
            if (in != null) {
                in.close();
            }
            // Закрывает сокет
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
