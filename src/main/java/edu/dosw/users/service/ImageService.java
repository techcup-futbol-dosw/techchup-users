package edu.dosw.users.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    String upload(MultipartFile file, Long sportProfileId);

    void delete(String photoId);
}