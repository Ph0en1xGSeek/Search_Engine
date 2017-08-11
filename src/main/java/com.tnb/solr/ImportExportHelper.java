package com.tnb.solr;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.tnb.solr.Document.*;
import static com.tnb.solr.Search.getServer;

/**
 * Created by SFM on 2017/6/6.
 */
public class ImportExportHelper {

    private static final String SOLR_URL = "http://localhost:8983/solr";

    private static HttpSolrClient server = null;

    private static String CONFIG_FILE_NAME = "TNBSolrDataSourcesConfig.xml";

    public static void setConfigFileName(String fileName){
        CONFIG_FILE_NAME = fileName;
    }

    private static void test(){
        exportToDirectoryConfig("d:solrSample/");
        exportToFileConfig("d:/solrSample.pdf");
        String url = "jdbc:oracle:thin:@60.30.69.61:1521:adc";
        String user = "CVDEV2";
        String password = "CVDEV2";
        String table = "T_AUTOMAKER_INFO";
        String [] cols = new String[]{"AUTOMAKER_ID","AUTOMAKER_NAME","CREATE_TIME"};
        exportToDatabaseConfig(url,user,password,table,cols);
    }

//    public static void indexFilesSolrCell(String fileName, String solrId) throws IOException, SolrServerException {
//        indexFilesSolrCell(fileName,solrId,"application/txt");
//    }
//
//    public static void indexFilesSolrCell(String fileName, String solrId, String contentType) throws IOException, SolrServerException{
//        System.setProperty("org.apache.pdfbox.baseParser.pushBackSize", "10000000");
//        try{
//            server = new HttpSolrServer(SOLR_URL);
//        }catch (Exception e) {
//            System.out.println(e);
//        }
//        ContentStreamUpdateRequest up = new ContentStreamUpdateRequest("/update/extract");
//        up.addFile(new File(fileName),contentType);
//
//        up.setParam("literal.id",solrId);
//        //up.setParam("id",solrId);
//        up.setParam("uprefix","attr_");
//        up.setParam("fmap.content","content");
//        //up.setParam("content","attr_content");
//        up.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
//
//        server.request(up);
//        server.commit();
//        QueryResponse res = server.query(new SolrQuery("*:*"));
//
//        System.out.println(res);
//    }

    public static void TNBSolrDataSourcesConfigParser() throws Exception {
        Document xmlDoc = parseDocument();
        if(xmlDoc == null){
            System.out.println("解析失败");
        }else{
            importData(xmlDoc);
        }
    }


    public static void importData(@NotNull Document xmlDoc) throws Exception {
        importData(xmlDoc, true,true ,true );
    }

    public static void importData(@NotNull Document xmlDoc, boolean fromDatabases, boolean fromDirectory, boolean fromFile) throws Exception {
        if(fromDatabases){
            importFromDatabases(xmlDoc);
        }
        if(fromDirectory){
            importFromDirectory(xmlDoc,false);
        }
        if(fromFile){
            importFromFile(xmlDoc,false);
        }
    }

    public static void importFromDatabases(Document xmlDoc) throws Exception {
        System.out.println(xmlDoc);
        String url, user, password, table;
        List<String> cols = new ArrayList<String>();
        Element rootElement = xmlDoc.getDocumentElement();
//        NodeList nodes = rootElement.getChildNodes();
//        for(int i=0;i<nodes.getLength();i++){
//            Node node = nodes.item(i);
//            if(node.getNodeType() == Node.ELEMENT_NODE) {
//                Element child = (Element) node;
//                System.out.println(child);
//                System.out.println(child.getAttribute("url"));
//            }
//        }
//        nodes = rootElement.getElementsByTagName("table");
//        for(int i=0;i<nodes.getLength();i++){
//            Node node = nodes.item(i);
//            if(node.getNodeType() == Node.ELEMENT_NODE){
//                Element child = (Element) node;
//                System.out.println(child.getAttribute("tableName"));
//            }
//        }
        NodeList DBList = rootElement.getElementsByTagName("database");
        if(DBList==null){
            System.out.println("未指定数据库");
        }else{
            for(int i=0;i<DBList.getLength();i++){
                System.out.println("### haha ###");
                Element db = (Element)DBList.item(i);
                url = db.getAttribute("url");
                user = db.getAttribute("user");
                password = db.getAttribute("password");
                NodeList TabList = db.getElementsByTagName("table");
                for(int j=0;j<TabList.getLength();j++){
                    Element tb = (Element)TabList.item(j);
                    table = tb.getAttribute("tableName");
                    NodeList colList = tb.getElementsByTagName("column");
                    for(int k=0;k<colList.getLength();k++){
                        Element col = (Element)colList.item(k);
                        cols.add(col.getAttribute("name"));
                    }
                    importFromDatabasesImpl(url,user,password,table,cols.toArray());
                    cols.clear();
                }
            }
        }
    }

    private static void importFromDatabasesImpl(String url, String user, String password, String table, Object[] objects) throws Exception {
        System.out.println(url);
        System.out.println(user);
        System.out.println(password);
        System.out.println(table);
        //System.out.println(objects);
        String [] strings = new String[objects.length];
        for(int i=0;i<strings.length;i++){
            strings[i] = objects[i].toString();
        }
        Search.buildStructual(url,user,password,table,strings,false);
    }

    public static void importFromDirectory(Document xmlDoc,boolean saveConfig){
        String url;
        Element rootElement = xmlDoc.getDocumentElement();
        NodeList nodes = rootElement.getElementsByTagName("directory");
        for(int i=0;i<nodes.getLength();i++){
            Node node = nodes.item(i);
            Element dir = (Element) node;
            url = dir.getAttribute("url");
            importFromDirectoryImpl(url,saveConfig);
        }
    }

    private static void importFromDirectoryImpl(String url, boolean saveConfig) {
        try {

            documentIndex(url, "", false);
        }catch (Exception e){
            e.printStackTrace();
        }
        if(saveConfig){
            exportToFileConfig(url);
        }
    }

    public static void importFromFile(Document xmlDoc, boolean saveConfig) throws IOException, SolrServerException {
        String url;
        Element rootElement = xmlDoc.getDocumentElement();
        NodeList nodes = rootElement.getElementsByTagName("file");
        for(int i=0;i<nodes.getLength();i++){
            Node node = nodes.item(i);
            Element dir = (Element) node;
            url = dir.getAttribute("url");
            importFromFileImpl(url,saveConfig);
        }
    }

    public static void importFromFileImpl(String url, boolean saveConfig) throws IOException, SolrServerException {
        //
        indexFile(url);
        if(saveConfig){
            exportToFileConfig(url);
        }
    }

    public static void exportToDatabaseConfig(String url, String user, String password, String table, Object[] objects){
        System.out.println("export to database config");
        Document xmlDoc = parseDocument();
        Element rootElement = xmlDoc.getDocumentElement();
        Element databaseTag = xmlDoc.createElement("database");
        databaseTag.setAttribute("url",url);
        databaseTag.setAttribute("user",user);
        databaseTag.setAttribute("password",password);
        Element tableTag = xmlDoc.createElement("table");
        tableTag.setAttribute("tableName",table);
        System.out.println("# "+objects.length);
        for(int i=0;i<objects.length;i++){
            Element columnTag = xmlDoc.createElement("column");
            columnTag.setAttribute("name",objects[i].toString());
            columnTag.setAttribute("solrAlias",objects[i].toString());
            columnTag.setAttribute("indexed","true");
            tableTag.appendChild(columnTag);
        }
        databaseTag.appendChild(tableTag);
        rootElement.appendChild(databaseTag);
        writeToFile(xmlDoc);
    }

    public static void exportToDirectoryConfig(String url){
        Document xmlDoc = parseDocument();
        Element rootElement = xmlDoc.getDocumentElement();
        Element dirTag = xmlDoc.createElement("directory");
        dirTag.setAttribute("url",url);
        rootElement.appendChild(dirTag);
        writeToFile(xmlDoc);
    }

    public static void exportToFileConfig(String url){
        Document xmlDoc = parseDocument();
        Element rootElement = xmlDoc.getDocumentElement();
        Element fileTag = xmlDoc.createElement("file");
        fileTag.setAttribute("url",url);
        rootElement.appendChild(fileTag);
        writeToFile(xmlDoc);
    }

    private static void writeToFile(Document doc) {
        saveXml(CONFIG_FILE_NAME, doc);
    }

    public static void saveXml(String fileName, Document doc) {//将Document输出到文件
        TransformerFactory transFactory=TransformerFactory.newInstance();
        try {
            Transformer transformer = transFactory.newTransformer();
            transformer.setOutputProperty("indent", "yes");
            DOMSource source=new DOMSource();
            source.setNode(doc);
            StreamResult result=new StreamResult();
            result.setOutputStream(new FileOutputStream(fileName));

            transformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static @Nullable
    Element getRootElement() {
        Element rootElement = null;
        Document xmlDoc = parseDocument();
        rootElement = xmlDoc.getDocumentElement();
        return rootElement;
    }

    public static @Nullable Document parseDocument() {
        return parseDocument(CONFIG_FILE_NAME);
    }

    public static @Nullable Document parseDocument(@NotNull String fileName){
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try{
            factory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder db = factory.newDocumentBuilder();
            File file = createConfigFileIfNotExists(fileName);
            Document xmlDoc = db.parse(file);
            return xmlDoc;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private static File createConfigFileIfNotExists(String fileName){
        File file=new File(fileName);
        FileOutputStream fop = null;
        if(!file.exists())
        {
            try {
                file.createNewFile();
                fop = new FileOutputStream(file);
                String content = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>\n<dataSources>\n</dataSources>\n";
                byte [] buf = content.getBytes();
                fop.write(buf);
                fop.flush();
                fop.close();
                CONFIG_FILE_NAME = fileName;
                file = new File(fileName);
                return file;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }finally {
                try {
                    if(fop != null){
                        fop.close();
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        return file;
    }
//    public static void documentIndex(String indexFile)
//
//    {
//        File fa[] = getAllDoc(indexFile);
//        try {
//            for (int i = 0; i < fa.length; i++) {
//                File fs = fa[i];
//                String fileName = fs.toString();
//                String solrId = fs.getName();
//                String docType = fileName.substring(fileName.lastIndexOf(".") + 1);
//                //String docLocation = fileName;
//                System.out.println(fileName + " "+solrId+" " +docType);
//                try {
//                    indexFilesSolrCell_HYQ(fileName, solrId, docType);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (SolrServerException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        catch (Exception e){
//            e.printStackTrace();
//            throw e;
//        }
//    }

    private static void indexFile(String fileName) throws IOException, SolrServerException{
        File file = new File(fileName);
        String solrId = file.getCanonicalPath();
        System.out.println(solrId);
        String docType = fileName.substring(fileName.lastIndexOf(".") + 1);
        if(docType.equals("md") || docType.equals("xml") || docType.equals("txt"))
            indexFilesSolrCell2(fileName, solrId, docType);
        else if(docType.equals("pdf"))
            indexFilesSolrCell(fileName, solrId, docType);
    }

    private static void indexFilesSolrCell_HYQ(String fileName, String solrId, String docType) throws IOException, SolrServerException
    {
        server = getServer(SOLR_URL);
        ContentStreamUpdateRequest up = new ContentStreamUpdateRequest("/update/extract");

        String contentType="application/"+docType;
        up.addFile(new File(fileName), contentType);
        up.setParam("literal.id", solrId);
        //up.setParam("name",fileName);
        up.setParam("uprefix", "attr_");
        up.setParam("fmap.content", "content");
        //up.setParam(param:"location")
        up.setParam("modified_time",getModifiedTime(solrId));
        up.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
        server.request(up);
        server.commit();
    }

//    private static File[] getAllDoc(String indexFile){
//        String path=indexFile;
//        File f=new File(path);
//        if(!f.exists()){
//            return null;
//        }else{
//            return f.listFiles();
//        }
//    }
}
