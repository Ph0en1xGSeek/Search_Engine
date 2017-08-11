/**
 * Created by Ph0en1x on 2017/6/14.
 * 搜索主页逻辑
 */

$(function () {
   $('#search-btn').on('click', function () {
       var searchStr = $('#search-input').val();
       if(searchStr != ''){
           window.location.href = '/searchPage?query=' + searchStr;
       }
   }) ;

    $('#search-input').keyup(function (e) {
        var key = e.which;
        if(key == 13){
            $('#search-btn').click();
        }
    });
});

$(document).ready();
