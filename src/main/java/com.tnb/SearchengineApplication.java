package com.tnb;

import com.tnb.solr.Search;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@SpringBootApplication
public class SearchengineApplication {

	public static void main(String[] args) {
		String user = "CVDEV2";
		String pwd = "CVDEV2";
		String urlString = "http://localhost:8983/solr/collection1";
		String dataBaseType = "oracle.jdbc.driver.OracleDriver";
		Search t;
		if(args.length == 0){
			System.out.println("无参数");
			urlString = "http://localhost:8983/solr/collection1";
			t = new Search(urlString);
		}
		else if(args.length == 1 && args[0].startsWith("http")){
			System.out.println("一个参数 " + args[0]);
			urlString = args[0];
			t = new Search(urlString);
		}
		else{
			System.out.println("多个参数");
			urlString = args[0];
			dataBaseType = args[1];
			t = new Search(urlString, dataBaseType);
		}
		//启动应用时自动创建一个solr连接
		//若无配置文件，创建配置文件
		File file=new File("TNBSolrDataSourcesConfig.xml");
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
		SpringApplication.run(SearchengineApplication.class, args);
	}
}
