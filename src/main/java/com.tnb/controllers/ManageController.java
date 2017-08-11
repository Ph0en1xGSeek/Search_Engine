package com.tnb.controllers;


import com.tnb.solr.FileUpload;
import com.tnb.solr.ImportExportHelper;
import com.tnb.solr.Search;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import static com.tnb.solr.FileVisitor.getDoc;
import static com.tnb.solr.ImportExportHelper.importFromFileImpl;
import static com.tnb.solr.Search.buildStructual;
import static com.tnb.solr.Document.documentIndex;

/**
 * Created by Ph0en1x on 2017/6/5.
 * 索引管理界面跳转逻辑
 */

@Controller
public class ManageController   {

//    private ServletContext servletContext;
//    @Override
//    public void setServletContext(ServletContext arg0) {
//        this.servletContext = arg0;
//    }
//    private ServletConfig servletConfig;
//    @Override
//    public void setServletConfig(ServletConfig arg0) {
//        this.servletConfig = arg0;
//    }

    /**
     * 创建数据库索引
     * @param database the url of databases like "jdbc:oracle:thin:@172.0.0.1:1521:adc"
     * @param table 表名
     * @param fieldName 字段名，用;隔开
     * @return String 结果success还是fail
     */
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public String createIndex(String database, String table, String user, String password, String fieldName){
        String [] fieldArr = fieldName.split(";");
        if(fieldArr == null)
            return "fail";
        try{

            buildStructual(database, user, password, table, fieldArr,true);
            return "success";
        }catch (Exception e){
            return e.getMessage();
        }
    }

    /**
     * 上传文件并建立索引
     * @param file <input type="file"></input> 传来的ultipartFile文件
     * @return 跳转结果页面
     */
    @RequestMapping(value="/upload-file", method=RequestMethod.POST)
    @ResponseBody
    public String fileupload(@RequestParam("file") MultipartFile file) {
        String prefix = "D:\\NEXT\\searchengine\\fileSave\\";
        String returnString = FileUpload.upload(file, prefix);
        String[] realFileName = file.getOriginalFilename().split("\\\\");
        String fileName = prefix + realFileName[realFileName.length-1];
        if(!returnString.equals("文件为空")){
            try{
                System.out.println(fileName);
                importFromFileImpl(fileName, true);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return returnString;
    }

    /**
     * 上传配置文件并应用配置
     * @param file <input type="file"></input> 传来的ultipartFile文件
     * @return 跳转结果页面
     */
    @RequestMapping(value="/upload-config", method=RequestMethod.POST)
    @ResponseBody
    public String configupload(@RequestParam("test") MultipartFile file) {
        String prefix = "D:\\NEXT\\searchengine\\configSave\\";
        String returnString = FileUpload.upload(file, prefix);
        String[] realFileName = file.getOriginalFilename().split("\\\\");
        String fileName = prefix + realFileName[realFileName.length-1];
        ImportExportHelper.setConfigFileName(fileName);
        if(!returnString.equals("文件为空")){
            try{
                ImportExportHelper.TNBSolrDataSourcesConfigParser();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return returnString;
    }


    /**
     * 初次创建目录的索引
     * @param dir 路径
     * @param type 所要爬取的该路径下所有的某类型文件
     * @return String "success" or "error"
     */
    @RequestMapping(value="/import-dir", method=RequestMethod.POST)
    @ResponseBody
    public String importDir(String dir, String type) {
        try {
            getDoc(dir, type, false);
            return "success";
        }catch (Exception e){
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * 增量更新目录
     * @param dir 路径
     * @param type 针对文件类型更新
     * @return String "success" or "error"
     */
    @RequestMapping(value="/update-dir", method=RequestMethod.POST)
    @ResponseBody
    public String updateDir(String dir, String type) {
        try {
            getDoc(dir, type, true);
            return "success";
        }catch (Exception e){
            e.printStackTrace();
            return "error";
        }
    }


}
