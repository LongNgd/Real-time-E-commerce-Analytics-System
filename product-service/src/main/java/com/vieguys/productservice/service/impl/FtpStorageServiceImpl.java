package com.vieguys.productservice.service.impl;

import com.vieguys.productservice.config.FtpProperties;
import com.vieguys.productservice.service.FtpStorageService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FtpStorageServiceImpl implements FtpStorageService {

    private static final String PRODUCT_DIRECTORY = "products";

    private final FtpProperties ftpProperties;

    @Override
    public List<String> uploadFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        FTPClient ftpClient = connect();
        List<String> uploadedFiles = new ArrayList<>();

        try {
            String targetDirectory = buildTargetDirectory();
            ensureDirectoryExists(ftpClient, targetDirectory);

            for (MultipartFile file : files) {
                validateFile(file);

                String remotePath = targetDirectory + "/" + generateFileName(file.getOriginalFilename());
                try (InputStream inputStream = file.getInputStream()) {
                    boolean stored = ftpClient.storeFile(remotePath, inputStream);
                    if (!stored) {
                        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to upload image to FTP");
                    }
                }
                uploadedFiles.add(remotePath);
            }

            return uploadedFiles;
        } catch (IOException exception) {
            deleteFiles(uploadedFiles);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to upload image to FTP", exception);
        } catch (RuntimeException exception) {
            deleteFiles(uploadedFiles);
            throw exception;
        } finally {
            disconnectQuietly(ftpClient);
        }
    }

    @Override
    public void deleteFiles(List<String> remotePaths) {
        if (remotePaths == null || remotePaths.isEmpty()) {
            return;
        }

        FTPClient ftpClient = connect();
        try {
            for (String remotePath : remotePaths) {
                ftpClient.deleteFile(remotePath);
            }
        } catch (IOException ignored) {
        } finally {
            disconnectQuietly(ftpClient);
        }
    }

    private FTPClient connect() {
        validateConfiguration();

        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(ftpProperties.getHost(), ftpProperties.getPort());
            boolean loggedIn = ftpClient.login(ftpProperties.getUsername(), ftpProperties.getPassword());
            if (!loggedIn) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to login to FTP server");
            }

            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            return ftpClient;
        } catch (IOException exception) {
            disconnectQuietly(ftpClient);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to connect to FTP server", exception);
        }
    }

    private void ensureDirectoryExists(FTPClient ftpClient, String directoryPath) throws IOException {
        String[] directories = directoryPath.split("/");
        String currentPath = "";

        for (String directory : directories) {
            if (!StringUtils.hasText(directory)) {
                continue;
            }

            currentPath += "/" + directory;
            if (!ftpClient.changeWorkingDirectory(currentPath)) {
                boolean created = ftpClient.makeDirectory(currentPath);
                if (!created && !ftpClient.changeWorkingDirectory(currentPath)) {
                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to prepare FTP directory");
                }
            }
        }
    }

    private String buildTargetDirectory() {
        String basePath = ftpProperties.getBasePath();
        if (!StringUtils.hasText(basePath)) {
            return "/" + PRODUCT_DIRECTORY;
        }

        String normalizedBasePath = basePath.startsWith("/") ? basePath : "/" + basePath;
        return normalizedBasePath.endsWith("/")
                ? normalizedBasePath + PRODUCT_DIRECTORY
                : normalizedBasePath + "/" + PRODUCT_DIRECTORY;
    }

    private String generateFileName(String originalFilename) {
        String sanitizedFilename = StringUtils.cleanPath(originalFilename == null ? "image" : originalFilename);
        String extension = "";
        int extensionIndex = sanitizedFilename.lastIndexOf('.');
        if (extensionIndex >= 0) {
            extension = sanitizedFilename.substring(extensionIndex);
        }

        return UUID.randomUUID() + extension;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file must not be empty");
        }
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(ftpProperties.getHost())
                || ftpProperties.getPort() == null
                || !StringUtils.hasText(ftpProperties.getUsername())
                || !StringUtils.hasText(ftpProperties.getPassword())) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "FTP configuration is missing");
        }
    }

    private void disconnectQuietly(FTPClient ftpClient) {
        if (ftpClient == null) {
            return;
        }

        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        } catch (IOException ignored) {
        }
    }
}
