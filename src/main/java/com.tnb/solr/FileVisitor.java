package com.tnb.solr;
import java.io.IOException;
import java.io.File;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import com.tnb.solr.Document.*;

import static com.tnb.solr.Document.indexFilesSolrCell;
import static com.tnb.solr.Document.needUpdate;


/**
 * 一个更快的文件系统遍历类
 */

public class FileVisitor extends SimpleFileVisitor<Path> {
    String solrId;
    String type;
    boolean ifupdate;
    private void find(Path path){
        if(!Files.isDirectory(path))
            //System.out.println(path.toString());
            //list.add(path.toString());
            //System.out.println(path.toString()); String fs = fa[i];
            if(path!= null){
                solrId= path.toString();
                String fileName = solrId.substring(solrId.lastIndexOf("\\") + 1);
                String docType = solrId.substring(solrId.lastIndexOf(".") + 1);
                if(docType.equals(type)){
                    System.out.println(solrId+" "+fileName+" "+docType);
                    try {
                        if(docType.equals("md") || docType.equals("xml") || docType.equals("txt")){
                            if(!ifupdate || needUpdate(solrId))
                        Document.indexFilesSolrCell2(fileName, solrId, docType);
                        }
                        else if(docType.equals("pdf"))
                            indexFilesSolrCell(fileName, solrId, docType);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            }
    }
    @Override
    public FileVisitResult visitFile(Path file,BasicFileAttributes attrs){
        find(file);

        return FileVisitResult.CONTINUE;

    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir,BasicFileAttributes attrs){
        find(dir);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file,IOException e){
        e.printStackTrace();
        return FileVisitResult.CONTINUE;
    }

    public static void getDoc(String args, String doctype, boolean update) throws IOException{
        FileVisitor fileVisitor = new FileVisitor();
        fileVisitor.type = doctype;
        fileVisitor.ifupdate = update;
        try {
            Files.walkFileTree(Paths.get(args), fileVisitor);
        }catch (Exception e){
            System.out.println("建立失败，访问权限不允许");
        }
        //System.out.println(fileVisitorTest.list);


    }
}