package org.example.dem;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Сервер чата, который обрабатывает подключения клиентов,
 * рассылает сообщения между клиентами и управляет списком пользователей.
 */
public class ChatServer {
    // Порт, на котором сервер будет слушать подключения
    private static int PORT;

    // Максимальное количество одновременных подключений
    private static int MAX_CONNECTIONS;

    // Список активных клиентов
    private static List<ClientHandler> clients = new ArrayList<>();

    // Логгер для записи событий сервера
    private static final Logger logger = LoggerFactory.getLogger(ChatServer.class);

    // Текущее количество пользователей в чате
    private static int userCount = 0;

    /**
     * Основной метод, который запускает сервер.
     *
     * @param args Аргументы командной строки (не используются).
     */
    public static void main(String[] args) {
        // Загружаем конфигурацию сервера
        loadConfig();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Server is listening on port {}", PORT);

            // Бесконечный цикл для принятия новых подключений
            while (true) {
                try {
                    // Принимаем новое подключение
                    Socket socket = serverSocket.accept();

                    // Проверяем, не превышено ли максимальное количество подключений
                    if (clients.size() >= MAX_CONNECTIONS) {
                        logger.warn("Max connections reached. Rejecting new client.");
                        socket.close();
                        continue;
                    }

                    logger.info("New client connected");

                    // Создаем обработчик клиента и добавляем его в список
                    ClientHandler client = new ClientHandler(socket);
                    clients.add(client);

                    // Запускаем обработчик клиента в отдельном потоке
                    new Thread(client).start();
                } catch (IOException ex) {
                    throw new ServerException("Error accepting client connection", ex);
                }
            }
        } catch (IOException ex) {
            throw new ServerException("Server error", ex);
        }
    }

    /**
     * Метод для загрузки конфигурации сервера из файла application.properties.
     */
    private static void loadConfig() {
        Properties properties = new Properties();
        try (InputStream input = ChatServer.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new ServerException("Configuration file not found: application.properties", null);
            }
            properties.load(input);

            // Загружаем порт сервера из конфигурации (по умолчанию 12345)
            PORT = Integer.parseInt(properties.getProperty("server.port", "12345"));

            // Загружаем максимальное количество подключений (по умолчанию 100)
            MAX_CONNECTIONS = Integer.parseInt(properties.getProperty("server.maxConnections", "100"));
        } catch (IOException ex) {
            throw new ServerException("Error loading configuration", ex);
        }
    }

    /**
     * Внутренний класс, который обрабатывает подключение одного клиента.
     */
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String clientName;

        /**
         * Конструктор обработчика клиента.
         *
         * @param socket Сокет, через который подключился клиент.
         */
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        /**
         * Метод, который выполняется в отдельном потоке для обработки клиента.
         */
        @Override
        public void run() {
            try {
                // Инициализируем потоки ввода-вывода
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Читаем первое сообщение от клиента (имя пользователя)
                String message = in.readLine();
                JSONObject jsonMessage = new JSONObject(message);
                clientName = jsonMessage.getString("username");

                // Увеличиваем счетчик пользователей
                userCount++;
                logger.info("{} has joined the chat.", clientName);

                // Уведомляем всех пользователей о новом участнике
                broadcastMessage(new JSONObject().put("type", "message").put("content", clientName + " has joined the chat.").toString());
                broadcastUserCount();
                broadcastUserList();

                // Обрабатываем сообщения от клиента
                while ((message = in.readLine()) != null) {
                    jsonMessage = new JSONObject(message);
                    String type = jsonMessage.getString("type");

                    // Если тип сообщения "message", обрабатываем его
                    if (type.equals("message")) {
                        String content = jsonMessage.getString("content");
                        String recipient = jsonMessage.optString("recipient", null);

                        // Если указан получатель, отправляем личное сообщение
                        if (recipient != null && !recipient.isEmpty()) {
                            sendPrivateMessage(recipient, new JSONObject().put("type", "message").put("content", clientName + " (private): " + content).toString());
                        } else {
                            // Иначе отправляем сообщение всем
                            broadcastMessage(new JSONObject().put("type", "message").put("content", clientName + ": " + content).toString());
                        }
                    }
                }
            } catch (IOException ex) {
                throw new ClientException("Client error", ex);
            } finally {
                // Закрываем соединения и удаляем клиента из списка
                closeConnections();
                clients.remove(this);
                userCount--;
                logger.info("{} has left the chat.", clientName);

                // Уведомляем всех пользователей об уходе клиента
                broadcastMessage(new JSONObject().put("type", "message").put("content", clientName + " has left the chat.").toString());
                broadcastUserCount();
                broadcastUserList();
            }
        }

        /**
         * Метод для отправки сообщения всем подключенным клиентам.
         *
         * @param message Сообщение в формате JSON.
         */
        private void broadcastMessage(String message) {
            for (ClientHandler client : clients) {
                client.out.println(message);
            }
        }

        /**
         * Метод для отправки личного сообщения конкретному клиенту.
         *
         * @param recipient Имя получателя.
         * @param message   Сообщение в формате JSON.
         */
        private void sendPrivateMessage(String recipient, String message) {
            for (ClientHandler client : clients) {
                if (client.clientName.equals(recipient)) {
                    client.out.println(message);
                    break;
                }
            }
        }

        /**
         * Метод для отправки текущего количества пользователей всем клиентам.
         */
        private void broadcastUserCount() {
            for (ClientHandler client : clients) {
                client.out.println(new JSONObject().put("type", "user_count").put("count", userCount).toString());
            }
        }

        /**
         * Метод для отправки списка пользователей всем клиентам.
         */
        private void broadcastUserList() {
            StringBuilder userList = new StringBuilder();
            for (ClientHandler client : clients) {
                if (userList.length() > 0) {
                    userList.append(",");
                }
                userList.append(client.clientName);
            }
            for (ClientHandler client : clients) {
                client.out.println(new JSONObject().put("type", "user_list").put("users", userList.toString()).toString());
            }
        }

        /**
         * Метод для закрытия соединений с клиентом.
         */
        private void closeConnections() {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ex) {
                throw new ClientException("Error closing connections", ex);
            }
        }
    }
}