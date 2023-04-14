package com.socks.server;

import lombok.Data;

import java.io.IOException;
import java.util.Properties;

/**
 * @author asyncio886(wuTao)
 * @date 2023/4/14
 */
public class ConfigLoader {
    private static final String PORT_KEY = "socks.port";
    private static final String WORKERS_KEY = "socks.workers";
    private static final String BOSSES_KEY = "socks.boss";
    private static final String USERNAME_KEY = "socks.username";
    private static final String PASSWORD_KEY = "socks.password";
    private static final String USE_PASSWORD_KEY = "socks.use-password";
    @Data
    public static class ServerConfig {
        boolean usePassword = false;
        int workerNum;
        int bossNum;
        String username = "";
        String password = "";
        int port;
    }
    private static final Properties PROPERTIES = new Properties();
    public static final ServerConfig SERVER_CONFIG = new ServerConfig();
    static {
        try {
            PROPERTIES.load(ConfigLoader.class.getResourceAsStream("/server.config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        SERVER_CONFIG.setBossNum(Integer.parseInt(PROPERTIES.getProperty(BOSSES_KEY, "12")));
        SERVER_CONFIG.setWorkerNum(Integer.parseInt(PROPERTIES.getProperty(WORKERS_KEY, "12")));
        SERVER_CONFIG.setUsePassword(Boolean.parseBoolean(PROPERTIES.getProperty(USE_PASSWORD_KEY, "false")));
        SERVER_CONFIG.setPort(Integer.parseInt(PROPERTIES.getProperty(PORT_KEY, "8888")));
        if (SERVER_CONFIG.isUsePassword()) {
            SERVER_CONFIG.setPassword(PROPERTIES.getProperty(PASSWORD_KEY));
            SERVER_CONFIG.setUsername(PROPERTIES.getProperty(USERNAME_KEY));
        }
    }
}
