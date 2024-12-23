package org.example.dem;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для окна входа и регистрации пользователей.
 * Обрабатывает ввод данных пользователя, проверяет аутентификацию
 * и открывает окно регистрации или чата.
 */
public class LoginRegisterController {
    // Поле для ввода имени пользователя
    @FXML
    private TextField usernameField;

    // Поле для ввода пароля
    @FXML
    private PasswordField passwordField;

    // Кнопка для входа
    @FXML
    private Button loginButton;

    // Кнопка для перехода к регистрации
    @FXML
    private Button registerButton;

    // Объект для работы с JSON
    private ObjectMapper objectMapper = new ObjectMapper();

    // Файл, в котором хранятся данные пользователей
    private File userFile = new File("users.json");

    // Основной Stage (окно) приложения
    private Stage primaryStage;

    /**
     * Метод, вызываемый при инициализации контроллера.
     * Проверяет, существует ли файл с пользователями, и создает его, если нет.
     */
    @FXML
    public void initialize() {
        if (!userFile.exists()) {
            try {
                userFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Метод, вызываемый при нажатии на кнопку "Вход".
     * Проверяет введенные данные и открывает окно чата, если аутентификация успешна.
     */
    @FXML
    private void login() {
        // Получаем имя пользователя и пароль из полей ввода
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            // Читаем данные пользователей из файла
            Map<String, String> users = readUsersFromFile();

            // Проверяем, существует ли пользователь с таким именем
            if (users.containsKey(username)) {
                // Получаем хешированный пароль и соль из файла
                String storedPasswordHashAndSalt = users.get(username);
                String[] parts = storedPasswordHashAndSalt.split(":");
                String storedHash = parts[0];
                String storedSalt = parts[1];

                // Проверяем, совпадает ли введенный пароль с хешированным паролем
                if (PasswordUtils.verifyPassword(password, storedHash, storedSalt)) {
                    System.out.println("Login successful!");
                    // Открываем окно чата
                    openChatWindow(username);
                    // Закрываем окно входа
                    closeLoginWindow();
                } else {
                    System.out.println("Invalid username or password!");
                }
            } else {
                System.out.println("Invalid username or password!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод, вызываемый при нажатии на кнопку "Регистрация".
     * Открывает окно регистрации.
     */
    @FXML
    private void register() {
        try {
            // Загружаем FXML-файл окна регистрации
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/dem/register.fxml"));
            Parent root = loader.load();

            // Получаем контроллер окна регистрации
            RegisterController controller = loader.getController();
            controller.setPrimaryStage((Stage) registerButton.getScene().getWindow());

            // Закрываем текущее окно входа
            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            loginStage.close();

            // Устанавливаем действие для кнопки "Назад" в окне регистрации
            controller.setOnBack(() -> {
                loginStage.show();
            });

            // Создаем новое окно регистрации
            Stage registerStage = new Stage();
            registerStage.initModality(Modality.APPLICATION_MODAL);
            registerStage.setTitle("Register");
            registerStage.setScene(new Scene(root));
            registerStage.show();

            // Устанавливаем минимальные и максимальные размеры окна
            registerStage.setMinWidth(340);
            registerStage.setMinHeight(425);
            registerStage.setMaxWidth(340);
            registerStage.setMaxHeight(425);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод для чтения данных пользователей из файла.
     *
     * @return Map с данными пользователей (ключ - имя пользователя, значение - хешированный пароль и соль).
     * @throws IOException если произошла ошибка при чтении файла.
     */
    private Map<String, String> readUsersFromFile() throws IOException {
        // Если файл пустой, возвращаем пустую Map
        if (userFile.length() == 0) {
            return new HashMap<>();
        }
        // Читаем данные из файла и преобразуем их в Map
        return objectMapper.readValue(userFile, HashMap.class);
    }

    /**
     * Метод для открытия окна чата.
     *
     * @param username имя пользователя, который вошел в систему.
     */
    private void openChatWindow(String username) {
        try {
            // Загружаем FXML-файл окна чата
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/dem/chat_client.fxml"));
            Parent root = loader.load();

            // Получаем контроллер окна чата
            ChatClientController controller = loader.getController();
            controller.setUsername(username);
            controller.connectToServer();

            // Создаем новое окно чата
            Stage chatStage = new Stage();
            chatStage.setTitle("Chat");
            chatStage.setScene(new Scene(root));
            chatStage.show();

            // Устанавливаем минимальные и максимальные размеры окна
            chatStage.setMinWidth(420);
            chatStage.setMinHeight(684);
            chatStage.setMaxWidth(420);
            chatStage.setMaxHeight(684);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод для закрытия окна входа.
     */
    public void closeLoginWindow() {
        Stage loginStage = (Stage) loginButton.getScene().getWindow();
        loginStage.close();
    }
}