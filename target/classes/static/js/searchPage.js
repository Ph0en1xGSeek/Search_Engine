/**
 * Created by SFM on 2017/6/12.
 * 搜索结果页面逻辑
 */

/**
 * 设置时间降序排列
 * @returns {string}
 */
function getTimeDescSortUrl() {
    var prefix = getUrlPrefix();
    var params = getUrlParms();
    return prefix + '?' + 'query=' + params + '&' + 'sort=timeDesc';
}

/**
 * 设置时间升序排列（越早越前）
 * @returns {string}
 */
function getTimeAscSortUrl() {
    var prefix = getUrlPrefix();
    var params = getUrlParms();
    return prefix + '?' + 'query=' + params + '&' + 'sort=timeAsc';
}

/**
 * 设置默认排序
 * @returns {string}
 */
function getDefaultSortUrl() {
    var prefix = getUrlPrefix();
    var params = getUrlParms();
    return prefix + '?' + 'query=' + params;
}


$(document).ready(function(){
    var runOut = 'false';
    var range = 50;             //距下边界长度/单位px
    var elemt = 500;           //插入元素高度/单位px
    var num = 0;
    var step = 5;
    var sort = getUrlSortParms();
    var totalheight = 500;
    var keyWords = getUrlParms();
    var visible = false;
    var main = $("#search-result");
    $('#search-input').val(keyWords);
    $('#search-input').keyup(function (e) {

        var key = e.which;
        if(key == 13){
            $('#search-btn').click();
        }
        else{
            getComplete();
        }
    });


    if(sort == 'timeDesc')
        $('#SortdropdownMenu').html('时间降序');
    else if(sort == 'timeAsc')
        $('#SortdropdownMenu').html('时间升序');
    else if(sort == 'default')
        $('#SortdropdownMenu').html('默认排序');
    $('#default').attr('href',getDefaultSortUrl());
    $('#time-asc').attr('href',getTimeAscSortUrl());
    $('#time-desc').attr('href',getTimeDescSortUrl());
    // console.log($('#default').attr('href'));

    for(var i=0; i<1 && runOut==='false'; i++){
        $.ajax({
            url:'/search',
            type:'GET',
            dataType:'json',
            async:false,
            data:{
                "keyWords" : keyWords,
                "startIndex" : num,
                "step" : step,
                "sort" : sort
            },
            success:function (data) {
                var obj = eval(data);
                // console.log('success');
                runOut = data.runOutFlag[0].flag;
                // console.log('line #29: '+runOut);
                for(var i=0;i<data.results.length;i++){
                    var d = data.results[i];
                    str='';
                    str+='<div class="jumbotron wow fadeIn" data-wow-delay="0.2s" style="visibility: visible; animation-name: fadeIn; animation-delay: 0.2s;">';
                    str+='<h1 class="h1-responsive">'+d.title+'</h1>';
                    str+='<p class="lead">'+d.content+'</p>';
                    str+='<hr class="my-2">';
                    str+='<p>'+d.information+'</p>';
                    str+='<p class="lead">';
                    str+='<a class="btn btn-primary btn-lg" role="button" onclick="getFile(\''+d.url+'\')">Learn more</a>';
                    str+='</p> </div>';
                    // console.log(str);
                    main.append(str);
                    num++;
                }
            }

        });
        //console.log(runOut);
        // console.log('visible: ' +  visible);
        // console.log('runOut: ' + runOut);
        if((runOut==='true') && !visible){
            $("#tnb-not-found-container").css({visibility:"visible"});
            visible = true;
        }

    }
    //主体元素
    $(window).scroll(function(){
        var srollPos = $(window).scrollTop();    //滚动条距顶部距离(页面超出窗口的高度)
        //console.log("滚动条到顶部的垂直高度: "+$(document).scrollTop());
        //console.log("页面的文档高度 ："+$(document).height());
        //console.log('浏览器的高度：'+$(window).height());
        // console.log(srollPos);
        if(srollPos>100){
            $("#toTop").css({"display":"block"});
        }else{
            $("#toTop").css({"display":"none"});
        }
        totalheight = parseFloat($(window).height()) + parseFloat(srollPos);
        if(($(document).height()-range) <= totalheight && runOut === 'false') {

            $.ajax({
                url:'/search',
                type:'GET',
                dataType:'json',
                async:false,
                data:{
                    "keyWords" : keyWords,
                    "startIndex" : num,
                    "step" : step,
                    "sort" : sort
                },
                success:function (data) {
                    //console.log(data);
                    var obj = eval(data);
                    runOut = data.runOutFlag[0].flag;
                    // console.log('success');
                    for(var i=0;i<data.results.length;i++){
                        var d = data.results[i];
                        str='';
                        str+='<div class="jumbotron wow fadeIn" data-wow-delay="0.2s" style="visibility: visible; animation-name: fadeIn; animation-delay: 0.2s;">';
                        str+='<h1 class="h1-responsive">'+d.title+'</h1>';
                        str+='<p class="lead">'+d.content+'</p>';
                        str+='<hr class="my-2">';
                        str+='<p>'+d.information+'</p>';
                        str+='<p class="lead">';
                        str+='<a class="btn btn-primary btn-lg" role="button" onclick="getFile(\''+d.url+'\')">Learn more</a>';
                        str+='</p> </div>';
                        // console.log(str);
                        main.append(str);
                        num++;
                    }
                }
            });
        }else if((runOut==='true') && !visible){
            $("#tnb-not-found-container").css({"visibility":"visible"});
            visible = true;
        }
        // console.log("runOut : "+runOut);
        // console.log("visible : "+visible);

    });

    /**
     * 搜索按钮
     */
    $('#search-btn').on('click', function () {
        var searchStr = $('#search-input').val();
        if(searchStr != ''){
            window.location.href = '/searchPage?query=' + searchStr;
        }
        else {
            window.location.href = '/';
        }
    }) ;

    /**
     * 返回顶部
     */
    $("#toTop").on('click',function(){
        $('body,html').animate({scrollTop:0},300);
    });


    /**
     * 搜索框获得焦点，请求搜索建议
     */
    $('#search-input').focus(function () {
        $('#complete-list').hide();
        // console.log('1' + $('#search-input').val() + '1');
        // if($('#search-input').val() == ''){
        //     $('#complete-list').hide();
        // }
        // else{
        //     getComplete();
        // }
    });
    //
    // /**
    //  * 搜索框失去焦点隐藏建议
    //  */
    // $('#search-input').blur(function () {
    //     $('#complete-list').hide();
    //     // $('#complete-list').css({"display":"none"});
    // });
});

/**
 * 选中搜索建议查找
 * @param e
 */
function autoCompleteSearch(e) {
    console.log(e.innerText);
    $('#search-input').val(e.innerText);
    $('#search-btn').click();
}


/**
 * 请求文件
 * @param url
 */
function getFile(url) {
    // console.log(url);
    // console.log(url.substr(7,url.length));
    $.ajax({
        url:'/loadFile?url='+url,
        type:'GET',
        dataType:'text',
        success:function (data) {
            // console.log('data: \n'+data);
            var converter = new showdown.Converter();
            var text = data;
            var html = converter.makeHtml(text);
            $('.modal-body').html(html);
            var fileName = url.substr(url.lastIndexOf('/') + 1);
            $('#myModalLabel').html(fileName);
            $('#myModal').modal();
        }
    });
}

/**
 * 获得url中的query参数
 * @returns {*}
 */
function getUrlParms(){
    var args =  new Object();   // 声明并初始化一个 "类"
    var query = location.search.substring(1);// 获得地址(URL)"?"后面的字符串.
    // console.log(location.href);
    var pairs=query.split("&");  // 分割URL(别忘了'&'是用来连接下一个参数)
    for(var i=0;i<pairs.length;i++){
        var pos=pairs[i].indexOf('=');
        if(pos==-1)   continue; // 它在找有等号的 数组[i]
        var argname=pairs[i].substring(0,pos); // 参数名字
        var value=pairs[i].substring(pos+1);  // 参数值     // 以键值对的形式存放到"args"对象中
        args[argname]=decodeURI(value);
    }  // 这个不需要解释吧.除非你不懂什么叫做 键值对
    creditId = parseInt(args['query']);
    // console.log(args['query']);
    return args['query'];

}

/**
 * 获得url中的sort参数
 * @returns {*}
 */
function getUrlSortParms() {
    var args = new Object();
    var query = location.search.substring(1);
    console.log(location.href);
    var pairs=query.split("&");
    for(var i=0;i<pairs.length;i++){
        var pos=pairs[i].indexOf('=');
        if(pos==-1) continue;
        var argname=pairs[i].substring(0,pos);
        var value=pairs[i].substring(pos+1);
        args[argname]=decodeURI(value);
    }
    creditId = parseInt(args['sort']);
    // console.log(args['sort']);
    if(args['sort'] != null)
        return args['sort'];
    else
        return 'default';
}

/**
 * 获得当前url，除去参数
 * @returns {*}
 */
function getUrlPrefix() {
    var args =  new Object();
    var query = location.href;
    //console.log(location.href);
    var pairs=query.split("?");
    for(var i=0;i<pairs.length;i++){
        // console.log(pairs[i]);
        var pos=pairs[i].indexOf('=');
        if(pos==-1) return pairs[i];
    }
}

/**
 * 向后端请求搜索建议
 */
function getComplete() {
    var searchinput = $('#search-input').val();
    $.ajax({
        url:'/auto-complete',
        type:'GET',
        datatype:'json',
        async:false,
        data:{
            "query":searchinput
        },
        success:function (data) {
            var obj = eval(data);
            console.log(obj);
            var str = '';
            for(var i = 0; i < obj.length; i++){
                // console.log(obj[i]);
                if(obj[i] != '')
                    str += '<a class="nav-link complete-item" onclick="autoCompleteSearch(this)">'+ obj[i] +'</a>'
            }
            $('#complete-list').html(str);
            if(str == ''){
                $('#complete-list').hide();
            }
            else{
                console.log(str);
                $('#complete-list').show();
            }
        }
    });
}

