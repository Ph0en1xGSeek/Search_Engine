package com.tnb.solr;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;

import static com.tnb.solr.Search.solrServer;

/**
 * For building index of direction
 * */


public class Document {

    /**
    * @Description create index for files under a certain direction
    * @param indexFile The direction
    * @param type type of file that index built on, "" means all types
    * @param update whether to use incremental update
    * @Return void
    * */
    public static void documentIndex(String indexFile, String type, boolean update) throws Exception{
        LinkedList L = getAllDoc(indexFile, type);
        String fa[] = new String[L.size()];
        for(int i=0;i<fa.length;i++){
            fa[i]=L.get(i).toString();
        }
        try {
            for (int i = 0; i < fa.length; i++) {
                String fs = fa[i];
                String solrId = fs;
                String fileName = solrId.substring(solrId.lastIndexOf("\\") + 1);
                String docType = solrId.substring(solrId.lastIndexOf(".") + 1);
                System.out.println("正在建立索引：" + fileName + "  "+solrId+"  " +docType);
                try {
                    if(docType.equals("md") || docType.equals("xml") || docType.equals("txt")){
                        //初次建立索引或者文件有过修改才执行该文件索引的建立
                        if(!update || needUpdate(solrId))
                            indexFilesSolrCell2(fileName, solrId, docType);
                    }
                    else if(docType.equals("pdf"))
                        indexFilesSolrCell(fileName, solrId, docType);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SolrServerException e) {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @Description Judge whether the index of file need update
     * @param url The canonical path of the file
     * @Return boolean
     * */
    protected static boolean needUpdate(String url){
        SolrQuery solrQuery = new SolrQuery();

        String modifiedTime = getModifiedTime(url);
        url = url.replaceAll("\\\\", "\\\\\\\\");
        url = "\"" + url.replaceFirst(":", "\\\\:") + "\"";
        solrQuery.set("df", "id");
        solrQuery.set("q", url);
        try{
            QueryResponse rsp = solrServer.query(solrQuery);
            SolrDocumentList docs = rsp.getResults();
            if(docs.getNumFound() == 0 || docs == null){
                return true;
            }
            else if(docs.get(0).get("modified_time").toString().equals(modifiedTime)){
                System.out.println("跳过" + url);
                return false;
            }
            else{
                return true;
            }

        }catch (Exception e){
            e.printStackTrace();
            return true;
        }
    }

    /**
     * @Description Get the list of all documents under a direction(recursion)
     * @param indexFile The canonical path of the direction
     * @param type Type of file that index built on, "" means all types
     * @Return LinkedList<String> the list of names of all files found
     * */
    private static LinkedList<String> getAllDoc(String indexFile, String type){
//        String path=indexFile;
//        File f=new File(path);
//        if(!f.exists()){
//            return null;
//        }else{
//            return f.listFiles();
//        }
        File file=new File(indexFile);
        LinkedList<String> list2 =new LinkedList<String >();
        if(file.exists()){
            LinkedList<File> list =new LinkedList<File>();
            File[] files = file.listFiles();
            for(File file2 : files){
                if(file2.isDirectory()){
                    list.add(file2);
                } else{
                    String dir = file2.getAbsolutePath();
                    String fileType = dir.substring(dir.lastIndexOf(".") + 1);;
                    if(fileType.equals(type) || type.equals("")){
                        list2.add(file2.getAbsolutePath());
                        System.out.println("已找到" + file2.getAbsolutePath());
                    }
                }
            }
            File tempFile;
            while(!list.isEmpty()){
                tempFile=list.removeFirst();
                files=tempFile.listFiles();
                if(files == null) continue;
                for(File file2 :files){
                    if(file2.isDirectory()){
                        list.add(file2);
                    }else {
                        String dir = file2.getAbsolutePath();
                        String fileType = dir.substring(dir.lastIndexOf(".") + 1);;
                        if(fileType.equals(type) || type.equals("")){
                            list2.add(file2.getAbsolutePath());
                            System.out.println("已找到" + file2.getAbsolutePath());
                        }

                    }
                }
            }
        }else{
            return null;
        }
        return list2;
    }

    /**
     * @Description Create index for file (pdf .etc)
     * @param fileName the name of file
     * @param solrId The canonical path of the file
     * @param docType the type of the file
     * @Return void
     * */
    public static void indexFilesSolrCell(String fileName, String solrId, String docType) throws IOException, SolrServerException
    {
        ContentStreamUpdateRequest up = new ContentStreamUpdateRequest("/update/extract");

        String contentType="application/"+docType;
        up.addFile(new File(solrId), contentType);
        up.setParam("literal.id", solrId);
        //up.setParam("name",fileName);
        up.setParam("uprefix", "attr_");
        up.setParam("fmap.content", "content");
        up.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
        Search.solrServer.request(up);
        Search.solrServer.commit();
    }

    /**
     * @Description Create index for file (txt, md, csv .etc)
     * @param fileName the name of file
     * @param solrId The canonical path of the file
     * @param docType the type of the file
     * @Return void
     * */
    public static void indexFilesSolrCell2(String fileName, String solrId, String docType) throws IOException, SolrServerException {

        String text=new String();
        try{
            String encoding;
            File file=new File(solrId);
            String doctype = new FileCharsetDetector().guessFileEncoding(file);
            if(file.isFile() && file.exists()){

                encoding = doctype.split(",")[0];
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null)
                    text=text + lineTxt;
                read.close();

            }
        }catch(Exception e) {
            e.printStackTrace();
        }


        SolrInputDocument doc = new SolrInputDocument();
        UpdateRequest updateRequest = new UpdateRequest();
        doc.addField("id",solrId);
        doc.addField("title", fileName);
        doc.addField("doctype",docType);
        doc.addField("text",text);
        doc.addField("modified_time",getModifiedTime(solrId));
//        Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
//        docs.add( doc );
        //更新id=bean_0的索引
//        下面是solr4.9所用代码
        Search.solrServer.add(doc);
        Search.solrServer.commit();

    }



    /**
     * @Description Get the last modified time pf a file
     * @param url The canonical path of the file
     * @Return String of the date in format "yyyy-MM-dd HH:mm:ss"
     * */
    public static String getModifiedTime(String url){
//        File f = new File(url);
//        Calendar cal = Calendar.getInstance();
//        long time = f.lastModified();
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        cal.setTimeInMillis(time);
//        return formatter.format(cal.getTime());

        File f = new File(url);
        Calendar cal = Calendar.getInstance();
        long time = f.lastModified();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        cal.setTimeInMillis(time);
        return formatter.format(cal.getTime());
    }
}
