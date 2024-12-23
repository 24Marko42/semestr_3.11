package org.example.dem;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для регистрации пользователей.
 * Обрабатывает ввод данных пользователя, проверяет уникальность имени пользователя
 * и сохраняет данные в файл.
 */
public class RegisterController {
    // Поле для ввода нового имени пользователя
    @FXML
    private TextField newUsernameField;

    // Поле для ввода нового пароля
    @FXML
    private PasswordField newPasswordField;

    // Кнопка для возврата на предыдущий экран
    @FXML
    private Button backButton;

    // Кнопка для регистрации нового пользователя
    @FXML
    private Button registerButton;

    // Объект для работы с JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Файл, в котором хранятся данные пользователей
    private final File userFile = new File("users.json");

    // Основной экран приложения
    private Stage primaryStage;

    // Действие, выполняемое при нажатии на кнопку "Назад"
    private Runnable onBack;

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
     * Метод, вызываемый при нажатии на кнопку "Назад".
     * Закрывает текущее окно регистрации и возвращает на предыдущий экран.
     */
    @FXML
    private void back() {
        // Получаем текущий Stage (окно)
        Stage registerStage = (Stage) backButton.getScene().getWindow();
        // Закрываем окно
        registerStage.close();

        // Если задано действие для кнопки "Назад", выполняем его
        if (onBack != null) {
            onBack.run();
        }
    }

    /**
     * Метод, вызываемый при нажатии на кнопку "Зарегистрироваться".
     * Проверяет уникальность имени пользователя, хеширует пароль и сохраняет данные в файл.
     */
    @FXML
    private void register() {
        // Получаем имя пользователя и пароль из полей ввода
        String username = newUsernameField.getText();
        String password = newPasswordField.getText();

        try {
            // Читаем данные пользователей из файла
            Map<String, String> users = readUsersFromFile();

            // Проверяем, существует ли уже пользователь с таким именем
            if (users.containsKey(username)) {
                System.out.println("Username already exists!");
            } else {
                // Генерируем соль для пароля
                String salt = PasswordUtils.generateSalt();
                // Хешируем пароль с использованием соли
                String hashedPassword = PasswordUtils.hashPassword(password, salt);

                // Сохраняем хешированный пароль и соль в формате "хеш:соль"
                users.put(username, hashedPassword + ":" + salt);
                // Записываем обновленные данные в файл
                objectMapper.writeValue(userFile, users);
                System.out.println("Registration successful!");

                // Закрываем окно регистрации
                Stage registerStage = (Stage) registerButton.getScene().getWindow();
                registerStage.close();

                // Если задано действие для кнопки "Назад", выполняем его
                if (onBack != null) {
                    onBack.run();
                }
            }
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
     * Устанавливает основной Stage (окно) приложения.
     *
     * @param primaryStage основной Stage.
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Устанавливает действие, которое будет выполнено при нажатии на кнопку "Назад".
     *
     * @param onBack действие, которое будет выполнено.
     */
    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }
}