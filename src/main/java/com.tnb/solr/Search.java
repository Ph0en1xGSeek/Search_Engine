package com.tnb.solr;

import net.sf.json.JSONArray;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Search {

//    static String urlString = "http://localhost:8983/solr/collection1";
    public static HttpSolrClient solrServer;

    static String databaseUrl = "jdbc:oracle:thin:@60.30.69.61:1521:adc";
    static String user = "CVDEV2";
    static String pwd = "CVDEV2";
    static String tableName = "T_AUTOMAKER_INFO";
    static String dataBaseType = "oracle.jdbc.driver.OracleDriver";


    public Search(String url){
        solrServer = getServer(url);
    }

    public Search(String url, String type){
        solrServer = getServer(url);
        dataBaseType = type;
    }

    /**
     * @Description create the solr server
     * @param urlString url of solr server. For example "http://localhost:8983/solr"
     * @Return HttpSolrServer
     * */
    public static HttpSolrClient getServer(String urlString){
        try{

            HttpSolrClient solr = new HttpSolrClient(urlString);
            return solr;
        }catch(Exception e){
            System.out.println("solr服务器可能未开启");
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] arg){
        String[] fieldArr = new String[3];
        fieldArr[0] = "AUTOMAKER_ID";
        fieldArr[1] = "AUTOMAKER_NAME";
        fieldArr[2] = "CREATE_TIME";
//        Search sh = new Search("http://localhost:8983/solr/collection1");
//        query();
//        try {
//            buildStructual(databaseUrl, user, pwd, tableName, fieldArr, true);
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }


//        String[] auto = autoComplete("text", "大", 5);
//        System.out.println("自动补全");
//        for(int i = 0; i < auto.length; i++) {
//            System.out.println(auto[i]);
//        }
        //query_by_page("md", "示例", 0, 100, true);
//        deleteByQuery("*:*");
        /*String text="还有一种强化提高的办法。一是自己先说一遍；二是同步录音，自己再听一下，找出不足；三是列个详细提纲，或者干脆把面试题当作笔试来作答，写出来，再大声读一遍；四是放下写的材料，再说一遍，并实现用口语化。如此反复做几套题就可以了。";
        StringReader sr=new StringReader(text);
        IKSegmenter ik=new IKSegmenter(sr, true);
        Lexeme lex=null;
        try{
            while((lex=ik.next())!=null){
                System.out.print(lex.getLexemeText()+"|");
            }
        }catch (Exception e){
            e.printStackTrace();
        }*/

    }


    //test
    public static void query(){
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.set("q", "斯柯达");
        System.out.println("搜索：斯柯达");
        System.out.println("返回：");
        try{
            QueryResponse rsp = solrServer.query(solrQuery);
            SolrDocumentList docs = rsp.getResults();
            findInDatabase(user,pwd, 0, 2,rsp);
            /*for(SolrDocument doc:docs){
                System.out.println(doc.toString());
            }*/

        }catch (Exception e){
            e.printStackTrace();
        }


        solrQuery.set("q", "上汽");
//                setFacet(true).
//                setFacetMinCount(1);
//                setFacetLimit(8).
//                setHighlight(true).
//                addFacetField("category").
//                addFacetField("inStock");
        System.out.println("搜索：上汽");
        System.out.println("返回：");
        try{
            QueryResponse rsp = solrServer.query(solrQuery);
            SolrDocumentList docs = rsp.getResults();
            findInDatabase(user,pwd, 0, 2, rsp);
//            for(SolrDocument doc:docs){
//                System.out.println(doc.get("text").toString());
//            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * @Description query for the file index and return by page
     * @param type the type of file we need
     * @param query query string
     * @param start the position that search start
     * @param row the number of rows from the start that return
     * @Return String the json string formatted as
     * {
     *  "results":[
     *      {
     *          "title":"",
     *          "content":"",
     *          "information":"2017-06-12 00:00:00",
     *          "url":"C:\README.md"
     *      }
     *  ],
     *  "runOutFlag":[
     *      {
     *          "flag":"false"
     *      }
     *  ]
     * }
     *
     * the flag is whether it is the end of the index
     * */
    public static String query_by_page(String type, String query, int start, int row, boolean hightlight, String sortType){
//        String[] queryArr = query.split(" ");
//        query = "";
//        for(String q:queryArr){
//            query += q + " id:" + q + " ";
//        }
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.set("q", query);
        solrQuery.setStart(start);
        solrQuery.setRows(row);
        solrQuery.addFilterQuery("doctype" + ":" + type);
        if(sortType != null){
            if(sortType.equals("timeDesc"))
                solrQuery.setSort("modified_time", SolrQuery.ORDER.desc);
            else if(sortType.equals("timeAsc"))
                solrQuery.setSort("modified_time", SolrQuery.ORDER.asc);
        }
        if(hightlight == true){
            solrQuery.setHighlight(true); // 开启高亮组件
            solrQuery.addHighlightField("text");// 高亮字段
            solrQuery.setHighlightSimplePre("<font color='red'>");// 标记
            solrQuery.setHighlightSimplePost("</font>");
            solrQuery.setHighlightSnippets(1);// 结果分片数，默认为1
            solrQuery.setHighlightFragsize(150);// 每个分片的最大长度，默认为100
        }
        try {
            QueryResponse rsp = solrServer.query(solrQuery);
            SolrDocumentList docs = rsp.getResults();
            Map<String,Map<String,List<String>>> hl = rsp.getHighlighting();
//            System.out.println(docs.toString());
            List<Map<String, String>> results = new ArrayList<Map<String, String>>();
            for(SolrDocument doc:docs){
                Map<String, String> map1 = new HashMap<String, String>();
                String url = doc.get("id").toString();
                String[] fileName = url.split("\\\\");
                map1.put("title", fileName[fileName.length-1]);
                String textHighLight, textHighLight2;
//                try{
//                    idHighLight = hl.get(doc.get("id")).get("id").get(0);
//                }catch (Exception e){
//                    idHighLight = "";
//                }
                try {
                    textHighLight = hl.get(doc.get("id")).get("text").get(0) ;
                }catch (Exception e){
                    textHighLight = "";
                }
                map1.put("content", textHighLight);
                String id = doc.get("id").toString();
                id = id.replaceAll("\\\\", "/");
                System.out.println(id);
                String info = doc.get("modified_time").toString();
                map1.put("information", info);
                map1.put("url", id);
                results.add(map1);
            }
            Map<String, String> map2 = new HashMap<String, String>();
            //返回是否还有剩余
            if(docs.getNumFound()-start + 1 < row){
                map2.put("flag", "true");
            }
            else{

                map2.put("flag", "false");
            }
            List<Map<String, String>> runOutFlag = new ArrayList<Map<String, String>>();
            runOutFlag.add(map2);
            Map<String, List<Map<String, String>>> response = new HashMap<String, List<Map<String,String>>>();
            JSONArray ja1 = JSONArray.fromObject(results);
            JSONArray ja2 = JSONArray.fromObject(runOutFlag);
            response.put("results", ja1);
            response.put("runOutFlag", ja2);
            JSONArray ja3 = JSONArray.fromObject(response);
            //标准json串格式
            String tmp = ja3.toString().substring(1, ja3.toString().length()-1);
            System.out.println(ja3.toString());
            return tmp;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    // Facet的一个应用：自动补全
    // prefix为前缀，min为最大返回结果数
    // field需要查询并返回补全的字段，prefix需要查询并返回的字段补全值
    // 返回补全结果Json串

    /**
     * @Description return the searching suggestion
     * @param field the field create the suggestion from
     * @param prefix the prefix to query for suggestion
     * @param min the maximum number of the suggestion
     * @return the json string of the suggesstion formatted as
     * "["and","as","an","are","all"]"
     */
    public static String autoComplete(String field, String prefix, int min) {
        String words[] = null;
        StringBuffer sb = new StringBuffer("");
        SolrQuery query = new SolrQuery(field + ":" + prefix);
        QueryResponse rsp = new QueryResponse();
        // Facet为solr中的层次分类查询
        try {
            query.setFacet(true);
            // query.setQuery("*:*");
            query = new SolrQuery(field + ":" + prefix);
            query.setFacetPrefix(prefix);
            query.addFacetField(field);
            rsp = solrServer.query(query);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return null;
        }
        if (null != rsp) {
            FacetField ff = rsp.getFacetField(field);
            List<FacetField.Count> countList = ff.getValues();
            if (null == countList) {
                return null;
            }
            for (int i = 0; i < countList.size(); i++) {
                String tmp[] = countList.get(i).toString().split(" ");
                // 排除单个字
                if (tmp[0].length() < 2) {
                    continue;
                }
                sb.append(tmp[0] + " ");
                min--;
                if (min == 0) {
                    break;
                }
            }
            words = sb.toString().split(" ");
        } else {
            return null;
        }
        JSONArray ja1 = JSONArray.fromObject(words);
        System.out.println(ja1.toString());
        return ja1.toString();
    }

    //建立结构化索引

    /**
     * @Description create index for databases
     * @param database the url of databases like "jdbc:oracle:thin:@172.0.0.1:1521:adc"
     * @param user username of database
     * @param pwd password of database
     * @param table name of table to build index on
     * @param fieldName the name array of field to build index on
     * @param saveConfig whether to save the configure file
     * @throws Exception
     */
    public static void buildStructual(String database, String user, String pwd, String table, String[] fieldName, boolean saveConfig) throws Exception {
        for(int i=0;i<fieldName.length;i++){
            System.out.println(fieldName[i]);
        }
        Connection con = null;// 创建一个数据库连接
        PreparedStatement pre = null;// 创建预编译语句对象，一般都是用这个而不用Statement
        ResultSet result = null;// 创建一个结果集对象
        try
        {
            Class.forName(dataBaseType);// 加载Oracle驱动程序
            System.out.println("开始尝试连接数据库！");
            con = DriverManager.getConnection(database, user, pwd);// 获取连接
            System.out.println("连接成功！");
            String sql = "select * from " + table;//
            pre = con.prepareStatement(sql);// 实例化预编译语句
            result = pre.executeQuery();// 执行查询，注意括号中不需要再加参数
            while (result.next()){
                // 当结果集不为空时
                SolrInputDocument document = new SolrInputDocument();
                String[] textList = new String[5];
                if(fieldName[0] != null)
                    document.addField("id", database + "^" + table + "^" +result.getString(fieldName[0]));
                for(int i = 1; i < fieldName.length; i++){
                    if(fieldName[i] != null){
                        textList[i] = result.getString(fieldName[i]);
                    }
                }
                document.addField("text", textList);
                document.addField("idName", fieldName[0]);
                try {
                    solrServer.add(document);
                    solrServer.commit();
                }catch (Exception e){
                    e.printStackTrace();
                    throw e;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("索引创建失败");
            throw e;
        }
        finally
        {
            try
            {
                // 逐一将上面的几个对象关闭，因为不关闭的话会影响性能、并且占用资源
                // 注意关闭的顺序，最后使用的最先关闭
                if(saveConfig){
                    ImportExportHelper.exportToDatabaseConfig(database,user,pwd,table,fieldName);
                }
                if (result != null)
                    result.close();
                if (pre != null)
                    pre.close();
                if (con != null)
                    con.close();
                System.out.println("数据库连接已关闭！");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

    }

    /**
     * delete the query result
     * @param query query string
     */
    public static void deleteByQuery(String query) {
        try {
            // 删除所有的索引
            solrServer.deleteByQuery(query);
            solrServer.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 根据索引号删除索引：

    /**
     * delete the index by id
     * @param id the id of the index
     */
    public static void deleteById(String id) {
        try {
            System.out.println("delete " + id);
            solrServer.deleteById(id);
            solrServer.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * fetch data from database according to the index
     * @param user username of database
     * @param pwd password of database
     * @param rsp the search result
     * @return String json string: a list of data from databases, specially, tableName is the table where
     *          the data from
     *          for example:
     *          [
                    {
                        "LOGO":"SD0SD1.jpg",
                        "CREATE_TIME":"2016-05-16 00:00:00.0",
                        "AUTOMAKER_ID":"SD0",
                        "tableName":"T_AUTOMAKER_INFO",
                        "REMARK":"斯柯达",
                        "AUTOMAKER_NAME":"上汽斯柯达"
                    },
                    {
                        "LOGO":"SD0SD1.jpg",
                        "CREATE_TIME":"2016-05-16 00:00:00.0",
                        "AUTOMAKER_ID":"SD1",
                        "tableName":"T_AUTOMAKER_INFO",
                        "REMARK":"斯柯达",
                        "AUTOMAKER_NAME":"进口斯柯达"
                    }
                ]
     */
    public static String findInDatabase(String user, String pwd, int start, int row,  QueryResponse rsp){
        try{
            Class.forName(dataBaseType);// 加载Oracle驱动程序
            System.out.println("开始尝试连接数据库！查找索引");
        }
        catch (Exception e ){
            e.printStackTrace();
        }

        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        Connection con = null;// 创建一个数据库连接
        PreparedStatement pre = null;// 创建预编译语句对象，一般都是用这个而不用Statement
        ResultSet result = null;// 创建一个结果集对象
        try {

            //con = DriverManager.getConnection(database, user, pwd);// 获取连接
           // System.out.println("连接成功！");
            //String sql = "select * from " + table;//
            //pre = con.prepareStatement(sql);// 实例化预编译语句
            // result = pre.executeQuery();// 执行查询，注意括号中不需要再加参数
            SolrDocumentList docs = rsp.getResults();

            for(SolrDocument doc:docs){
                String infoString=doc.get("id").toString();
                String idName=doc.get("idName").toString();
                String linkToDb=infoString.substring(0,infoString.indexOf("^"));
                String tableName=infoString.substring(infoString.indexOf("^")+1,infoString.lastIndexOf("^"));
                String id=infoString.substring(infoString.lastIndexOf("^")+1);
                //System.out.println(idName+" "+linkToDb+" "+tableName+" "+id);
                con = DriverManager.getConnection(linkToDb, user, pwd);
                String sql = "select * from " + tableName + " where " + idName + "=" + "\'" + id + "\'";
                pre = con.prepareStatement(sql);
                result = pre.executeQuery();
//                System.out.println(result.toString());
                Map<String, String> mp1 = new HashMap<String, String>();
                mp1.put("tableName", tableName);
                while (result.next()){

                    int cnum = result.getMetaData().getColumnCount();
                    String[] re = new String[cnum];
                    String[] cn = new String[cnum];
                    for(int i=0;i<cnum;i++){
                        re[i]=result.getString(i+1);
                        cn[i]=result.getMetaData().getColumnName(i+1);
                        mp1.put(cn[i], re[i]);
                        System.out.print(cn[i]+" "+re[i]+"   ");
                    }
                    list.add(mp1);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (result != null)
                    result.close();
                if (pre != null)
                    pre.close();
                if (con != null)
                    con.close();
                System.out.println("数据库连接已关闭！");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            JSONArray ja1 = JSONArray.fromObject(list);
            System.out.println("json串 " + ja1.toString());
            return ja1.toString();
        }
    }

    static public String getUser(){
        return user;
    }

    static public String getPwd(){
        return pwd;
    }
}
