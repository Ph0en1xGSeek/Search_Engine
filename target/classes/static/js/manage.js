/**
 * Created by Ph0en1x on 2017/6/5.
 * 索引管理界面逻辑
 */


$(function () {

    /**
     * 创建数据库索引请求
     */
    $('#build-btn').on('click', function () {

        var table = $('#table').val();
        var database = $('#database').val();
        var field = $('#field').val();
        var user = $('#user').val();
        var password = $('#password').val();
        // alert(database + '\n' + table + '\n' + field);
        if(database == ''){
            alert('数据库连接不能为空');
        }
        else if(table == ''){
            alert('表名不能为空');
        }
        else if(field == ''){
            alert('字段名不能为空');
        }
        else if(user == ''){
            alert('用户名不能为空');
        }
        else if(password == ''){
            alert('请输入密码');
        }
        else{

            var fieldArr = new Array(field.split(";"));
            var params = {database:database, table:table, user: user, password: password, fieldName:field};
            // alert(params);
            $.ajax({
                type:"post",
                url:"create",
                data:params,
                success: function(data){
                    console.log(data);
                    if (data =="success"){
                        alert('索引建立成功');
                    }
                    else{
                        alert("索引建立失败\n" + data);
                    }
                }

            });
        }
    });

    /**
     * 创建目录索引请求
     */
    $('#import-dir-btn').on('click', function () {
        var dir = $('#direction').val();
        var type = $('#doctype').val();
        if(dir != ''){
            $.ajax({
                type:"post",
                url:"/import-dir",
                data:{"dir":dir, "type":type},
                success:function (data) {
                    if(data == 'success')
                        alert('建立成功');
                    else
                        alert('建立失败');
                }
            });
        }

    });

    /**
     * 更新目录索引请求
     */
    $('#update-dir-btn').on('click', function () {
        var dir = $('#update-direction').val();
        var type = $('#update-doctype').val();
        if(dir != ''){
            $.ajax({
                type:"post",
                url:"/update-dir",
                data:{"dir":dir, "type":type},
                success:function (data) {
                    if(data == 'success')
                        alert('更新成功');
                    else
                        alert('更新失败');
                }
            });
        }

    });

    // $('#import-btn').on('click', function () {
    //     var filename = $('#file').val();
    //     alert(filename);
    //
    //     $('#import-form').submit();
    // });
});