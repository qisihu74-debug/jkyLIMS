var Step1Page = {
    init: function () {
        var me = this;
        me.setEvents();
    },
    setEvents: function () {
        var me = this;
        $("#submit").click(function (e) {
            me.operate();
            e.preventDefault();
        });
    },
    operate: function () {
        var startId = $("#startId").val();

        $.post('/jkyService/dingservice/get_all_user_steps_test',{date:startId},function (data) {
            console.log(data);
            let box= document.getElementById("test");
            for (var i = 0; i <data.length ; i++) {
                let li = document.createElement("li");
                li.innerHTML=`<li><span>日期：${data[i].date}</span><span>用户ID：${data[i].userId}</span><span>用户姓名：${data[i].name}</span><span>用户步数：${data[i].steps}</span><br></li>`;
                box.appendChild(li);
            }
        },'json')
    }
};
$(function () {
    Step1Page.init();
});
