package com.tnb.solr;

import java.io.*;

import static com.tnb.solr.Document.indexFilesSolrCell2;
import static com.tnb.solr.Document.needUpdate;
import static com.tnb.solr.Search.deleteById;

/**
 * Created by SFM on 2017/6/15.
 * Transfer the file to the browser
 */
public class loadFile {

    /**
     * @Description Transfer the file to the browser and meanwhile update its index.
     *              If the file is not found, delete its index.
     * @param url The canonical path of the file
     * @Return String the byte stream of the file
     * */
    public static String loadFileService(String url){
        System.out.println(url);
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        String docType = url.substring(url.lastIndexOf(".") + 1);
        String solrId = url.replaceAll("/", "\\\\");
        String ret = null;
        try {
            String encoding;
            File f = new File(url);
            String doctype = new FileCharsetDetector().guessFileEncoding(f);
            encoding = doctype.split(",")[0];
            Reader reader = null;
            StringBuffer buf = new StringBuffer();
            reader = new InputStreamReader(new FileInputStream(f), encoding);
            int tmp;
            while((tmp=reader.read())!=-1){
                buf.append(((char)tmp));
            }
            ret = buf.toString();
            //文件索引更新的lazy操作
            if (needUpdate(url)){
                indexFilesSolrCell2(fileName, solrId, docType);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            //文件索引删除的lazy操作
            deleteById(solrId);
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        return ret;
    }
}
