package com.tnb.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.tnb.solr.Search.autoComplete;
import static com.tnb.solr.Search.query_by_page;
import com.tnb.solr.loadFile;
/**
 * Created by Ph0en1x on 2017/5/25.
 */
@Controller
public class MainController {

    /**
     * 搜索首页
     * @return
     */
    @RequestMapping("/")
    public String index(){
        return "index";
    }

    /**
     * solr索引管理界面
     * @return
     */
    @RequestMapping("/manage")
    public String manage(){
        return "manage";
    }

    /**
     * 搜索结果页面
     * @return
     */
    @RequestMapping("/searchPage")
    public String searchPage(){
        return "searchPage";
    }

    /**
     * 调用搜索功能
     * @param keyWords
     * @param startIndex
     * @param step
     * @param sort
     * @return
     */
    @RequestMapping("/search")
    @ResponseBody
    public String search(String keyWords, int startIndex, int step, String sort){
        String returnString = query_by_page("md", keyWords, startIndex, step, true, sort);
//        System.out.println(returnString + " jsghf");
        return returnString;

    }

    /**
     * 下载文件
     * @param url 文件url
     * @return 文件字节流
     */
    @RequestMapping("/loadFile")
    @ResponseBody
    public String loadFile(String url){
        return loadFile.loadFileService(url);
    }

    /**
     * 获取搜索建议
     * @param query
     * @return 搜索建议的json串
     * "["and","as","an","are","all"]"
     */
    @RequestMapping("/auto-complete")
    @ResponseBody
    public String complete(String query){
        if(query == null || query.equals(""))
            return "[\"\"]";
        return autoComplete("text", query, 5);
    }
}
