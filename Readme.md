# 使用说明

## 1.下载完整项目
&ensp;&ensp; **使用git clone 下载完整项目**

* solr5.5的git地址为：`git@60.30.69.73:tnb/solr_5.5.0.git`;

* 搜索引擎的SearchEngine_on_Solr_5.5.0地址为：`git@60.30.69.73:tnb/SearchEngine_on_Solr_5.5.0.git`;


## 2.启动项目
&ensp;&ensp; **利用提供的solr5.5包，搭建solr运行环境，从而为程序提供运行环境。**

* `Solr`服务正常启动
    * 确保本地配置Java环境变量，具体教程可以参考 `http://jingyan.baidu.com/article/f96699bb8b38e0894e3c1bef.html`
    * 解压缩提供的`solr5.5`包
    * 进入到cmd模式，进入解压完的solr目录下的bin文件夹下
    * 使用`solr start`启动solr服务
    * 在浏览器中访问`localhost:8983/solr`，出现solr界面，说明成功启动；
	* 若要停止服务，只需要输入：`solr stop -port 8983`;

    
&ensp;&ensp;&ensp;&ensp; **利用下载的SearchEngine_on_Solr_5.5.0包，搭建搜索服务，从而对于特定文件建立索引，同时可以使用提供的demo，进行搜索服务。**

* `SearchEngine_on_Solr_5.5.0`服务正常启动
	* 进入提供SearchEngine包下的target目录，路径为：`SearchEngine\target`；
	* 进入cmd模式，进入到上面路径；
	* 使用`java -jar searchengine-1.0.jar`启动搜索服务；
	* 启动中后续可以添加参数类似下面格式：`java -jar searchengine-1.0.jar http://localhost:8983/solr/collection1 oracle.jdbc.driver.OracleDriver`
	* 第一个参数为：`http://localhost:8983/solr/collection1`,指定在哪个Multicore上建索引，默认也是这个参数；
	* 第二个参数为：`oracle.jdbc.driver.OracleDriver`,指定要对那种类型数据建立连接，默认为oracle，也可以将其设置为mysql连接；
	* 如果需要指定第二个参数必须首先指定第一个参数；
	* 在浏览器中访问：`http://localhost:8080/`进入搜索主页；
	* 在浏览器中访问：`http://localhost:8080/manage`进入管理者界面，对文件构建索引	；

&ensp;&ensp; **关于solr服务器的其他一些配置，这一部分为使用solr本身接口，与项目使用关系不大，为拓展内容。**

* Solr5.5多核MultiCore（实例）配置
	* 以为数据库建立的Core为例，将solr-5.5.0\example\example-DIH\solr\db复制到		solr-5.5.0\solr-5.5.0\server\solr下；
	* 重启服务器，发现新的core已经配置完成；


* 自定义字段
    * 配置managed-schema,位于目录为：`solr-5.5.0\server\solr\collection1\conf`，用	于配置data-config.xml文件中用到的字段类型，可以结尾位置开始，便于以后查看。
	* 定义格式为：`<field name="geo_name" type="text_general" indexed="true" stored="true" /> `
	* 其中name为自定义字段名称，type为字段类型以及索引、存储等设置

* 改写配置文件，连接到数据库（以Oracle为例）
    * 下载Oracle连接的驱动包
    * 将压缩包拷贝到solr目录下，路径为`solr-5.5.0\server\solr-webapp\webapp\WEB-INF\lib`
    * 配置solrconfig.xml，路径为`\solr-5.5.0\server\solr\collection1\conf`，加入下面内容，保证solr读取访问连接数据的文件data-config.xml

```javascript

    <requestHandler name="/dataimport"  class="org.apache.solr.handler.dataimport.DataImportHandler">
    <lst name="defaults">
    <str name="config">data-config.xml</str> 
    </lst> 
   </requestHandler>

```

   * 在solrconfig.xml的87行加入需要使用的包，内容如下：
	
```javascript

 	<lib dir="../../../dist/" regex="solr-dataimporthandler-5.5.0.jar" />
  	<lib dir="../../../dist/" regex="solr-dataimporthandler-extras-5.5.0.jar" />

```

   * 在solrconfig.xml同文件夹下创建data-config.xml文件，其中dataSource中为数据库连	接的内容，entity中的内容为所要查询的表以及建立索引的列，field中column的值为数据库中列名，name为要映射到solr中的建立索引的列名需要保证两者数据类型相同， **需要注意这里使用到的name字段名称必须在schema.xml中已经定义，同时必须保证name中有一列为id，对应的是数据表的主键**，示例内容：

```javascript
	
    <dataConfig>

    <dataSource type="JdbcDataSource" driver="oracle.jdbc.driver.OracleDriver"  
    url="jdbc:oracle:thin:@60.30.69.61:1521:adc"  
    user="CVDEV2"  
    password="CVDEV2"/>  


	<document>

	<entity name="T_AUTOMAKER_INFO" query="SELECT AUTOMAKER_ID, AUTOMAKER_NAME, 
	CREATE_TIME from T_AUTOMAKER_INFO">

	<field column="AUTOMAKER_ID" name="id"/> 
	<field column="AUTOMAKER_NAME" name="use_cname"/> 
	<field column="CREATE_TIME" name="geo_name"/> 


	</entity>>
	</document>

	</dataConfig>

```

* 分词器

&ensp;&ensp;&ensp;	Jcseg是基于mmseg算法的一个轻量级开源中文分词器，同时集成了关键字提取，关键短语提取，关键句子提取和文章自动摘要等功能，并且提供了最新版本的lucene, solr, elasticsearch的分词接口， Jcseg自带了一个 jcseg.properties文件用于快速配置而得到适合不同场合的分词应用，例如：最大匹配词长，是否开启中文人名识别，是否追加拼音，是否追加同义词等。 

* 配置Jcseg： 
	* 下载Jcseg，需要自行下载,并进行解压；
	*  将jcseg.properties，lexicon\，以及\output下的jcseg-core-1.9.5.jar与jcseg-solr-1.9.5.jar，拷贝到solr-5.5.0\server\solr-webapp\webapp\WEB-INF\lib下。
	*  在solr-5.5.0\server\solr\collection1\conf中加入以下代码：

```javascript

    <!--jcseg分词 -->
	<fieldType name="text_jcseg" class="solr.TextField">
  	<analyzer type="index">
    <!-简单模式分词: -->
    <tokenizer class="org.lionsoul.jcseg.solr.JcsegTokenizerFactory" mode="simple"/>
 	 </analyzer>
  	<analyzer type="query">
    <!-- 复杂模式分词: -->
    <tokenizer class="org.lionsoul.jcseg.solr.JcsegTokenizerFactory" mode="complex"/>
 	 </analyzer>
	</fieldType>

  
```

定义了一个叫作text_jcseg的类型，之后所有被定义为这个类型的字段(Field)，在建立索引和查询的时候都会使用jcseg analyzer进行分词。

* 重启solr服务，访问`http://localhost:8983/solr/#/collection1/analysis`，即可对新添加的text_ik分词效果测试。

&ensp;&ensp; **以上这部分内容为扩展内容，已经为开发者配置了相关内容，如果开发想要进行拓展，可以参照这部分。**


## 3.使用图形界面建立文件索引
&ensp;&ensp;在项目成功启动以后在浏览器中访问：`http://localhost:8080/manage`，即可进入到管理者界面，对于文件建立索引。


* 对于数据库建立索引
	* 数据库连接栏输入类似：`jdbc:oracle:thin:@60.30.69.61:15211:qwewe`，即数据库的地址；
	* 表名栏输入要建立索引的表名，注意一次只能对一个表数据建立索引；
	* 用户名和密码栏，分别输入数据库用户名和密码即可；
	* 字段名栏输入要建立索引的字段名，如果有多个字段，使用`；`隔开；
	* 单击`建立`，即可完成对与这张表指定字段索引建立，完成建立后会返回一个提示框。

	<img src="https://github.com/chengxuyuanjunjujn/imageurl/blob/master/a11.jpg?raw=true" width="400" />


* 对于文档建立索引
	* 单击`选择文件`，选择你要建立索引的文档
	* 单击`上传并建立`，完成建立工作，成功建立索引后返回一个提示框
	* 注意一次只能上传单个文档
	
	<img src="https://github.com/chengxuyuanjunjujn/imageurl/blob/master/a2.jpg?raw=true" width="400" />

* 使用xml文件建立相应文件索引
	* 用户改写提供项目下的TNBSolrDataSourcesConfig.xml文件；
		* 数据库类型，按照格式写入需要建立数据库表名以及字段名；
		* 文件类型，按照格式写入需要建立的文档路径；
	* 单击`选择文件`，导入提供项目下的`TNBSolrDataSourcesConfig.xml`
	* 单击`导入`，对于配置文件中的数据库和文件建立索引；

	<img src="https://github.com/chengxuyuanjunjujn/imageurl/blob/master/a3.jpg?raw=true" width="400" />

* 对于整个目录下的文件建立索引
	* 将要建立索引目录的路径填入，类似：`E:\ACM`;
	* 如果对于整个目录下的文件建立索引，第二栏空白即可；
	* 如果选择某一类型文件，可以在第二栏添上`md`、`csv`、`txt`等文件类型，注意每次只能选择一种文件类型进行建立索引；
	* 单击`建立`按钮，即可对选择目录建立索引；

	<img src="https://github.com/chengxuyuanjunjujn/imageurl/blob/master/a4.jpg?raw=true" width="400" />


* 对于已经建立索引的文件夹进行更新
	* 更新的目的提高目录索引更新速度，同时保证索引时效性；
	* 将需要更新的索引目录的路径填入，类似：`E:\ACM`;
	* 如果对于整个目录下的文件进行索引更新，第二栏空白即可；
	* 如果选择某一类型文件，可以在第二栏添上`md`、`csv`、`txt`等文件类型，注意每次只能选择一种文件类型进行建立索引；
	* 单击`更新`按钮，即可对选择目录建立索引；
	
	<img src="https://github.com/chengxuyuanjunjujn/imageurl/blob/master/a11.jpg?raw=true" width="400" />

## 4.搜索引擎页面及功能介绍

**主界面**

<img src="https://github.com/chengxuyuanjunjujn/imageurl/blob/master/a12.jpg?raw=true" width="400" />

**搜索结果展示界面**

<img src="https://github.com/chengxuyuanjunjujn/imageurl/blob/master/a13.jpg?raw=true" width="400" />

**选择合适排序方式**

<img src="https://github.com/chengxuyuanjunjujn/imageurl/blob/master/a14.jpg?raw=true" width="400" />


## 5.组件提供jar包中可用的方法函数
&ensp;&ensp;&ensp;&ensp; **将SearchCore\target下的searchengine-1.1.jar包，从而在项目中使用接口方法实现对于数据库文件以及文档建立索引，并进行搜索和结果展示的操作。**

&ensp;&ensp;&ensp;&ensp; **引用包名为com.tnb.solr.***

&ensp;&ensp;&ensp;&ensp; **使用前需创建一个Search实例，以创建与solr对应内核的连接**

&ensp;&ensp;&ensp;&ensp; **对于下面这些接口方法的具体实现代码可以将整个项目导入IDEA中，即可看到源码**

### Document类中的方法
* **public static void documentIndex(String indexFile, String type)**，用于构建指定文件下的索引，
	* indexFile为文件路径;
	* type指定哪种类型文件，""为默认为所有文件 
	* 调用了本身提供的public方法 **private static void indexFilesSolrCell(String fileName, String solrId, String docType)**，用于对于文件构建列表索引；
	* 调用private方法 **private static LinkedList<String> getAllDoc(String indexFile, String type)**，用于确定整个目录结构并构建为一个列表，方便documentIndex使用；
	* **该方法在后续调整中被废除**

实际使用示例：  
  
```java

    @RequestMapping(value="/import-dir", method=RequestMethod.POST)
    @ResponseBody
    public String importDir(String dir, String type) {
        try {
            documentIndex(dir, type);
            return "success";
        }catch (Exception e){
            e.printStackTrace();
            return "error";
        }
    }

```        

* **protected static boolean needUpdate(String url)**，用于判定一个文件目录是否更新，从而确定是否对索引进行更新
	* url为文件路径

实际调用示例：

```java

    if(needUpdate(url)){

        indexFilesSolrCell2(fileName, solrId, docType);
     }

``` 

* **public static void indexFilesSolrCell(String fileName, String solrId, String docType)**，用于为pdf类型文件构建索引
	* filename为指定文件名；
	* solrId为文件绝对路径；
	* docType为文件类型；

* **public static void indexFilesSolrCell2(String fileName, String solrId, String docType)**，用于为txt, md, csv类型文件构建索引
	* filename为指定文件名；
	* solrId为文件绝对路径；
	* docType为文件类型；

* **public static String getModifiedTime(String url)**，用于获取文件的最后更新时间
	* url为文件的绝对路径；
	* 返回的string格式为："yyyy-MM-dd HH:mm:ss"，即标准时间格式；


### FileCharsetDetector类中的方法
* **public String guessFileEncoding(File file)**，用于获取文件编码，其中file为文件类型变量，这个方法在为文件构建索引的时候使用，检查编码从而防止乱码。
	* 调用了私有化方法 **private String guessFileEncoding(File file, nsDetector det)方法**，进行检查编码，这个方法是透明的，不需要开发者了解。
	* 返回string为文件编码，eg：UTF-8,GBK,GB2312形式(不确定的时候，返回可能的字符编码序列)；若无，则返回null；

实际代码调用：

```java

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

```

### FileUpload类中的方法
* **public static String upload(MultipartFile file, String filePath)**，用于文件上传
	* file为前端向后传输文件，前端标签为 `<input type="file"></>`；
	* filePath为目标路径；

实际使用示例(下面为配置文件更新使用代码示例)：  
  
```java

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
   
```

下面为上传单个文件更新使用示例：
	
```java

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
```

### FileVisitor类中的方法

* **public static void getDoc(String args, String doctype, boolean update)**，用于遍历文件目录并构建索引，或更新已建立索引并进行修改过的文件
	* args为目录地址；
	* doctype为构建索引的文件类型，默认为所有类型文件；
	* update为是否开启更新，如果为true会对目录地址进行更新而不是从新构建；

使用示例：

对目录首次建立时使用：

```java

    @RequestMapping(value="/import-dir", method=RequestMethod.POST)
    @ResponseBody
    public String importDir(String dir, String type) {
        try {
            documentIndex(dir, type, false);
            return "success";
        }catch (Exception e){
            e.printStackTrace();
            return "error";
        }
    }
```

对目录进行更新时使用：

```java

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

```

### ImportExportHelper类中的方法
* **public static void TNBSolrDataSourcesConfigParser()**，用于将配置文件中的内容导入并建立索引，调用 **public static void importData(@NotNull Document xmlDoc)**方法。

使用示例：    

```java

    @RequestMapping(value = "/fullimporttest",method = RequestMethod.GET)
    @ResponseBody
    public void fullImport() throws Exception {
        ImportExportHelper.TNBSolrDataSourcesConfigParser();
    }

```

* **public static void importData(@NotNull Document xmlDoc)**，调用真正导入配置文件的 **public static void importData(@NotNull Document xmlDoc, boolean fromDatabases, boolean fromDirectory, boolean fromFile)**。

* **public static void importData(@NotNull Document xmlDoc, boolean fromDatabases, boolean fromDirectory, boolean fromFile)**      
	* xmlDoc为将要导入的配置文件；
	* 其他几个参数为配置文件中包含几方面的即将建立索引类型，如果存在就将值设为true；
	* fromDatabases数据库类型数据，fromDirectory为从目录导入，fromFile为单个文件；
	* 上述三种数据类型分别调用了各自的导入数据方法，分别为public static void importFromDatabases(Document xmlDoc)、  public static void importFromDirectory(Document xmlDoc,boolean saveConfig)和public static void importFromFile(Document xmlDoc, boolean saveConfig)；



* **public static void exportToDatabaseConfig(String url, String user, String password, String table, Object[] objects)**，用于将对于某个数据表的内容建立索引保存到配置文件中；

* **public static void exportToDirectoryConfig(String url)**，用于将对于目录建立索引保存到配置文件中；

* **public static void exportToFileConfig(String url)**，用于将某个文件建立索引保存到配置文件中；

* 配置文件格式为：

```javascript

数据库：

	<database password="CVDEV2" url="jdbc:oracle:thin:@60.30.69.61:1521:adc" user="CVDEV2">
	<table tableName="T_AUTOMAKER_INFO">
	<column indexed="true" name="AUTOMAKER_ID" solrAlias="AUTOMAKER_ID"/>
	<column indexed="true" name="AUTOMAKER_NAME" solrAlias="AUTOMAKER_NAME"/>
	<column indexed="true" name="CREATE_TIME" solrAlias="CREATE_TIME"/>
	</table>
	</database>

文件：

	<file url="D:\NEXT\searchengine\fileSave\LICENSE.md"/>
	<file url="D:\NEXT\searchengine\fileSave\毛概论文.pdf"/>

```


### loadFile类中的方法
* **public static String loadFileService(String url)**，该方法用于在点击搜索结果展示框的时候将整个文件作为字节流传入前端使用
	* 如果文件建立索引与文件当前状态不一致，会进行更新，从而保证文件内容准确一致，如果文件应景被删除，会将索引删除，从而保持一致性。

实际调用示例：

```java

    @RequestMapping("/loadFile")
    @ResponseBody
    public String loadFile(String url){
        return loadFile.loadFileService(url);
    }
```

### search类中的方法：
* **public static HttpSolrServer getServer(String urlString)**，与solr服务器建立连接
	* 方法返回值为HttpSolrServer类型；
	* 传入的ulrString为本地solr服务器地址，默认情况下为		`http://localhost:8983/solr/collection1`        

实际调用代码：  

```java

    server = getServer(SOLR_URL);  
  
```    
* **public static String query_by_page(String type, String query, int start, int row, boolean hightlight)**，用于进行查询
	* type为所要查询的文件类型，默认为全部类型；
	* query为查询的内容；
	* start与row为指定查询结果的起始位置以及个数；
	* higtlight为指定结果是否高亮，默认为true；   
  
在实际调用代码示例：    

```java

    @ResponseBody   
    public String search(String keyWords, int startIndex, int step){  
        String returnString = query_by_page("md", keyWords, startIndex, step, true);  
        return returnString;    
  
    }     
     
```

返回值类型为json串，需要符合下面格式：

```java

     @Return String the json string formatted as
     {
      "results":[
          {
             "title":"",
             "content":"",
             "information":"2017-06-12 00:00:00",
             "url":"C:\README.md"
          }
       ],
       "runOutFlag":[
           {
               "flag":"false"
           }
       ]
      }
     
      the flag is whether it is the end of the index
      
```

* **public static String[] autoComplete(String field, String prefix, int min)**，用于查询内容的自动补全，方便用户的查询
	* prefix为前缀，即实时的输入内容；
	* min为最大返回结果数，开发人员自定义，尽量不要太大，即不方便显示，后面的相关性也比较差；

返回值类型为json串，假设输入为a，返回为：

```

"["and","as","an","are","all"]"

```



* **public static void buildStructual(String database, String user, String pwd, String table, String[] fieldName, boolean saveConfig)**，用于对于结构化数据，如指定的数据表的列建立索引
	* database为数据库连接池，user和pwd为用户名和密码
	* table指定用户表，filedName指定建立索引的列集合
	* saveconfig指定是否存入配置文件中，方便以后的导入；

调用代码示例：

```java

    private static void importFromDatabasesImpl(String url, String user, String password, String table, Object[] objects) throws Exception {
        String [] strings = new String[objects.length];
        for(int i=0;i<strings.length;i++){
            strings[i] = objects[i].toString();
        }
        Search.buildStructual(url,user,password,table,strings,false);
    }

```

* **public static void deleteByQuery(String query)**，用于删除某些不在需要的索引
	* query内容为定义删除哪些索引；
* **public static void deleteById(String id)**，用于删除指定索引号的索引，id内容为索引号，使用该方法需要已知需要删除的索引号
*  **public static String findInDatabase(String user, String pwd, int start, int row,QueryResponse rsp)**，用户根据查询结果中的索引集合去原数据库查询完整内容
	* user和pwd为数据库的用户名和密码；
	* rsp为查询后返回的索引集合； 
  	* start与row为指定查询结果的起始位置以及个数；
  	* 返回值类型为json串，需要符合下面格式：

```java

				[
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

```

实际使用示例：  

  
```java


    try{
            QueryResponse rsp = solrServer.query(solrQuery);
            SolrDocumentList docs = rsp.getResults();
            findInDatabase(user,pwd,rsp);
            for(SolrDocument doc:docs){
                System.out.println(doc.get("text").toString());
            }

     }catch (Exception e){
            e.printStackTrace();
     }

```




