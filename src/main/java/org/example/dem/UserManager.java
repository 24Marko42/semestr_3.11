package org.example.dem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Класс UserManager отвечает за управление пользователями: регистрацию, аутентификацию,
 * хранение данных пользователей в файле и работу с паролями (хеширование и соль).
 */
public class UserManager {
    // Имя файла, в котором будут храниться данные пользователей
    private static final String FILE_NAME = "users.json";

    /**
     * Метод для хеширования пароля с использованием соли.
     *
     * @param password Пароль, который нужно хешировать.
     * @param salt     Соль, которая добавляется к паролю перед хешированием.
     * @return Хешированный пароль в формате Base64.
     */
    public static String hashPassword(String password, String salt) {
        try {
            // Создаем экземпляр MessageDigest с алгоритмом SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // Добавляем соль к процессу хеширования
            digest.update(salt.getBytes());
            // Хешируем пароль вместе с солью
            byte[] hashedBytes = digest.digest(password.getBytes());
            // Преобразуем хешированный пароль в строку Base64
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            // Если алгоритм хеширования недоступен, выбрасываем исключение
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Метод для генерации случайной соли.
     *
     * @return Случайная соль в формате Base64.
     */
    public static String generateSalt() {
        // Используем SecureRandom для генерации случайных чисел
        SecureRandom random = new SecureRandom();
        // Создаем массив байтов для соли
        byte[] salt = new byte[16];
        // Заполняем массив случайными байтами
        random.nextBytes(salt);
        // Преобразуем соль в строку Base64
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Метод для регистрации нового пользователя.
     *
     * @param username Имя пользователя.
     * @param password Пароль пользователя.
     * @return true, если регистрация прошла успешно, иначе false.
     */
    public static boolean registerUser(String username, String password) {
        // Читаем данные пользователей из файла
        JSONArray usersArray = readUsersFromFile();

        // Проверяем, существует ли уже пользователь с таким именем
        for (int i = 0; i < usersArray.length(); i++) {
            JSONObject userObject = usersArray.getJSONObject(i);
            if (userObject.getString("username").equals(username)) {
                // Если пользователь уже существует, возвращаем false
                return false;
            }
        }

        // Генерируем соль для нового пользователя
        String salt = generateSalt();
        // Хешируем пароль с использованием соли
        String hashedPassword = hashPassword(password, salt);

        // Создаем объект JSON для нового пользователя
        JSONObject newUser = new JSONObject();
        newUser.put("username", username);
        newUser.put("password", hashedPassword);
        newUser.put("salt", salt);

        // Добавляем нового пользователя в массив
        usersArray.put(newUser);
        // Сохраняем обновленный массив в файл
        saveUsersToFile(usersArray);

        // Возвращаем true, если регистрация прошла успешно
        return true;
    }

    /**
     * Метод для аутентификации пользователя.
     *
     * @param username Имя пользователя.
     * @param password Пароль пользователя.
     * @return true, если аутентификация прошла успешно, иначе false.
     */
    public static boolean loginUser(String username, String password) {
        // Читаем данные пользователей из файла
        JSONArray usersArray = readUsersFromFile();

        // Ищем пользователя с указанным именем
        for (int i = 0; i < usersArray.length(); i++) {
            JSONObject userObject = usersArray.getJSONObject(i);
            if (userObject.getString("username").equals(username)) {
                // Получаем соль и хешированный пароль из файла
                String storedSalt = userObject.getString("salt");
                String storedHashedPassword = userObject.getString("password");
                // Хешируем введенный пароль с использованием соли из файла
                String inputHashedPassword = hashPassword(password, storedSalt);

                // Сравниваем хешированные пароли
                if (storedHashedPassword.equals(inputHashedPassword)) {
                    // Если пароли совпадают, возвращаем true
                    return true;
                }
            }
        }
        // Если пользователь не найден или пароль не совпадает, возвращаем false
        return false;
    }

    /**
     * Метод для чтения данных пользователей из файла.
     *
     * @return JSONArray с данными пользователей.
     */
    private static JSONArray readUsersFromFile() {
        try {
            // Создаем объект файла
            File file = new File(FILE_NAME);
            // Если файл не существует, возвращаем пустой массив
            if (!file.exists()) {
                return new JSONArray();
            }
            // Читаем содержимое файла
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
            reader.close();
            // Преобразуем содержимое файла в JSONArray
            return new JSONArray(jsonContent.toString());
        } catch (IOException e) {
            // Если произошла ошибка чтения, выбрасываем исключение
            throw new RuntimeException("Error reading users from file", e);
        }
    }

    /**
     * Метод для сохранения данных пользователей в файл.
     *
     * @param usersArray JSONArray с данными пользователей.
     */
    private static void saveUsersToFile(JSONArray usersArray) {
        try {
            // Записываем данные в файл с отступами для удобства чтения
            BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME));
            writer.write(usersArray.toString(4));
            writer.close();
        } catch (IOException e) {
            // Если произошла ошибка записи, выбрасываем исключение
            throw new RuntimeException("Error writing users to file", e);
        }
    }
}