package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author Sunsongoing
 */

public interface FileService {

    String upload(MultipartFile file,String path);
}
