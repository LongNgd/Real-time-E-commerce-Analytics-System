package com.vieguys.productservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ftp")
public class FtpProperties {
    private String host;
    private Integer port;
    private String username;
    private String password;
    private String basePath;
}
