package com.server;

import lombok.Data;

import java.io.IOException;
import java.io.InputStream;
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
    private static final String START_DNS_PROXY_KEY = "dns.start";
    private static final String DNS_PORT_KEY = "dns.port";
    private static final String HTTP_PORT_KEY = "http.port";
    private static final String START_HTTP_PROXY_KEY = "http.start";
    private static final String HTTP_USERNAME_KEY = "http.username";
    private static final String HTTP_PASSWORD_KEY = "http.password";
    private static final String USE_HTTP_PASSWORD = "http.use-password";
    @Data
    public static class ServerConfig {
        boolean usePassword = false;
        int workerNum;
        int bossNum;
        String username = "";
        String password = "";
        int socksPort;
        int dnsPort;
        int httpPort;
        boolean startHttpProxy;
        boolean startDNSProxy;
        boolean useHttpPassword;
        String httpUsername;
        String httpPassword;
    }
    private static final Properties PROPERTIES = new Properties();
    public static final ServerConfig SERVER_CONFIG = new ServerConfig();
    static {
        InputStream resource = null;
        try {
            resource = ConfigLoader.class.getResourceAsStream("/server.config.properties");
            PROPERTIES.load(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SERVER_CONFIG.setBossNum(Integer.parseInt(PROPERTIES.getProperty(BOSSES_KEY, "12")));
        SERVER_CONFIG.setWorkerNum(Integer.parseInt(PROPERTIES.getProperty(WORKERS_KEY, "12")));
        SERVER_CONFIG.setUsePassword(Boolean.parseBoolean(PROPERTIES.getProperty(USE_PASSWORD_KEY, "false")));
        SERVER_CONFIG.setSocksPort(Integer.parseInt(PROPERTIES.getProperty(PORT_KEY, "8888")));
        SERVER_CONFIG.setDnsPort(Integer.parseInt(PROPERTIES.getProperty(DNS_PORT_KEY, "9999")));
        SERVER_CONFIG.setUseHttpPassword(Boolean.parseBoolean(PROPERTIES.getProperty(USE_HTTP_PASSWORD, "false")));
        SERVER_CONFIG.setHttpPort(Integer.parseInt(PROPERTIES.getProperty(HTTP_PORT_KEY, "10086")));
        SERVER_CONFIG.setStartDNSProxy(Boolean.parseBoolean(PROPERTIES.getProperty(START_DNS_PROXY_KEY, "false")));
        SERVER_CONFIG.setStartHttpProxy(Boolean.parseBoolean(PROPERTIES.getProperty(START_HTTP_PROXY_KEY, "false")));
        if (SERVER_CONFIG.isUsePassword()) {
            SERVER_CONFIG.setPassword(PROPERTIES.getProperty(PASSWORD_KEY));
            SERVER_CONFIG.setUsername(PROPERTIES.getProperty(USERNAME_KEY));
        }
        if (SERVER_CONFIG.isUseHttpPassword()) {
            SERVER_CONFIG.setHttpPassword(PROPERTIES.getProperty(HTTP_PASSWORD_KEY, ""));
            SERVER_CONFIG.setHttpUsername(PROPERTIES.getProperty(HTTP_USERNAME_KEY, ""));
        }
        try {
            assert resource != null;
            resource.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
