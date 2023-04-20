package com.server.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author asyncio886(wuTao)
 * @date 2023/4/14
 */
@Getter
@AllArgsConstructor
public enum Events {
    REPEAT_INIT(1, "重复初始化"),
    WRONG_PASSWORD(2, "用户名或密码错误"),
    CLIENT_METHODS_NOT_SUPPORT(3, "客户端协议不支持");
    private final int code;
    private final String message;
}
