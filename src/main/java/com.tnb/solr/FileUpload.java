package com.tnb.solr;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Created by Ph0en1x on 2017/6/8.
 */


public class FileUpload {

    /**
     * @Description upload the file from browsers
     * @param file MultipartFile upload from the tag <input type="file"></>
     * @param filePath the target direction of the uploaded file
     * @Return String of the result to ajax to determine whether succeed
     * */
    public static String upload(MultipartFile file, String filePath){
        if (file.isEmpty()) {
            return "文件为空";
        }
        // 获取文件名
        String fileName = file.getOriginalFilename();
        System.out.println("上传的文件名为：" + fileName);
//        String realFileName = fileName.substring(fileName.lastIndexOf("\\"));
        String[] realFileName = fileName.split("\\\\");

        File dest = new File(filePath + realFileName[realFileName.length-1]);
        // 检测是否存在目录
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        try {
            file.transferTo(dest);
            return "上传成功";
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "上传失败";
    }
}
