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
        $.post('',{date:startId},function (data){
            console.log(data);
            let box = document.getElementsById('test');
            for (let i = 0; i < data.length; i++) {
                let li = document.createElement('li');
                li.innerHTML = `<li><span>用户ID:${data[i].userId}</span><span>用户步数：${data[i].steps}</span></li>`;
                box.appendChild(li);
            }
        },'josn');
    }
};
$(function () {
    Step1Page.init();
});
