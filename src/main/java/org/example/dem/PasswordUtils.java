package org.example.dem;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtils {

    private static final String ALGORITHM = "SHA-256"; // Используем SHA-256

    // Генерация случайной соли
    public static String generateSalt() {
        byte[] salt = new byte[16]; // 16 байт соли
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt); // Кодируем соль в Base64
    }

    // Хеширование пароля с солью
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(ALGORITHM);
            messageDigest.update(Base64.getDecoder().decode(salt)); // Декодируем соль из Base64
            byte[] hashedBytes = messageDigest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes); // Возвращаем хеш в формате Base64
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Проверка пароля (сравнение хешей)
    public static boolean verifyPassword(String password, String storedHash, String storedSalt) {
        String hashedPassword = hashPassword(password, storedSalt); // Хешируем введенный пароль с солью
        return storedHash.equals(hashedPassword); // Сравниваем хеши
    }
}