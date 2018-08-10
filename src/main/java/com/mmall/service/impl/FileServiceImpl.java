package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.common.ResponseCode;
import com.mmall.service.FileService;
import com.mmall.util.FTPUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @author Sunsongoing
 */
@Service
public class FileServiceImpl implements FileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    /**
     * 上传文件
     *
     * @param file 上传的文件名
     * @param path 上传文件的路径
     * @return 上传成功返回文件名，失败返回error
     */
    @Override
    public String upload(MultipartFile file, String path, String remotePath) {
        //getName获取表单中文件组件的名字
        //getOriginalFilename获取上传文件的原名
        logger.info(file.getOriginalFilename());
        String fileName = file.getOriginalFilename();
        //获取扩展名
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".") + 1);
        //为上传的文件生成新的文件名
        String uploadFileName = UUID.randomUUID().toString() + "." + fileExtensionName;
        logger.info("------开始上传文件,上传的文件名:{},上传的路径:{},新文件名:{}", fileName, path, uploadFileName);

        File fileDir = new File(path);
        //如果不存在upload就创建
        if (!fileDir.exists()) {
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }

        File targetFile = new File(path, uploadFileName);
        try {
            file.transferTo(targetFile);
            if (StringUtils.isBlank(remotePath)) {
                remotePath = "img";
//                remotePath = "images";
            }
            //将targetFile上传到FTP服务器
            boolean result = FTPUtil.uploadFile(Lists.newArrayList(targetFile), remotePath);
            if (!result) {
                //上传失败
                logger.error("上传文件失败");
                return ResponseCode.ERROR.getDesc();
            }
            //上传完之后，删除upload中的文件
            targetFile.delete();
            return targetFile.getName();
        } catch (IOException e) {
            logger.error("上传文件异常", e);
        }
        return ResponseCode.ERROR.getDesc();
    }
}
