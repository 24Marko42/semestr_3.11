package org.example.dem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ChatClient extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Устанавливает заголовок окна
        primaryStage.setTitle("Login/Register");

        // Создает объект FXMLLoader для загрузки FXML файла
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/dem/login_register.fxml"));

        // Загружает корневой элемент из FXML файла
        Parent root = loader.load();

        // Создает новую сцену с корневым элементом
        Scene scene = new Scene(root);

        // Устанавливает сцену в основное окно
        primaryStage.setScene(scene);

        // Отображает основное окно
        primaryStage.show();

        // Устанавливает минимальную ширину окна
        primaryStage.setMinWidth(381);

        // Устанавливает минимальную высоту окна
        primaryStage.setMinHeight(507);

        // Устанавливает максимальную ширину окна
        primaryStage.setMaxWidth(381);

        // Устанавливает максимальную высоту окна
        primaryStage.setMaxHeight(507);
    }

    public static void main(String[] args) {
        // Запускает JavaFX приложение
        launch(args);
    }
}
