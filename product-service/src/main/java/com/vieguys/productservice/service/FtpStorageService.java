package com.vieguys.productservice.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FtpStorageService {
    List<String> uploadFiles(List<MultipartFile> files);

    void deleteFiles(List<String> remotePaths);
}
