//存放主要交互逻辑的js代码
//javascript 模块化,模拟分包书写JS
var seckill = {
    //封装秒杀相关的ajax地址
    URL: {
        now:function () {
            return '/seckill/time/now';
        },
        //秒杀地址
        exposer:function (seckillId) {
            return '/seckill/' + seckillId + '/exposer';
        },
        //秒杀地址
        execution:function (seckillId,md5) {
            return '/seckill/'+seckillId+'/'+md5+'/execution';
        }
    },
    //验证手机号
    validatePhone: function (phone) {
        if(phone&&phone.length==11 && !isNaN(phone)){
            return true;//直接判断对象会看对象是否为空,空就是undefine就是false; isNaN 非数字返回true
        }else {
            return false;
        }
    },
    handleSeckillkill:function (seckillId,node) {
        //处理秒杀逻辑
        node.hide()
            .html('<button class="btn btn-primary btn-lg" id="killBtn">开始秒杀</button>');
        $.post(seckill.URL.exposer(seckillId),{},function (result) {
            //在回调函数中执行交互流程，判断是否存在，是否成功
            if(result&&result['success']){
                var exposer = result['data'];
                if(exposer['exposed']){
                    //开启秒杀
                    //获取秒杀地址
                    var md5 = exposer['md5'];
                    var killUrl=seckill.URL.execution(seckillId,md5);
                    //绑定一次秒杀事件，减少服务器端的压力
                    $('#killBtn').one('click',function () {
                        //执行秒杀请求的操作
                        //1、先禁用按钮
                        $(this).addClass('disable');
                        //2：发送秒杀请求
                        $.post(killUrl,{},function (result) {
                            if(result&&result['success']){
                                var killResult = result['data'];
                                var state=killResult['state'];
                                var stateInfo = killResult['stateInfo'];
                                //显示秒杀结果
                                node.html('<span class="label label-success">'+stateInfo+'</span>');
                            }
                        });
                    });
                    node.show();
                }else{
                    //未开启秒杀
                    var now=exposer['now'];
                    var start = exposer['start'];
                    var end = exposer['end'];
                    //重新计算计时逻辑
                    seckill.countdown(seckillId, now, start, end);
                }
            }
        });
    },
    countdown:function (seckillId,nowTime,startTime,endTime) {
        var seckillBox = $('#seckill-box');
        if(nowTime>endTime) {
            seckillBox.html('秒杀结束!');
        }else if(nowTime<startTime) {
            //秒杀未开始，计时事件绑定
            var killTime=new Date(startTime+1000);
            seckillBox.countdown(killTime,function (event) {
                //控制时间格式
                var format=event.strftime('秒杀倒计时：%D天 %H时 %M分 %S秒 ');
                seckillBox.html(format);
                //时间完成后回调事件
            }).on('finish.countdown',function () {
                //获取秒杀地址，控制显示逻辑，执行秒杀
                seckill.handleSeckillkill(seckillId,seckillBox);
            });
        }else{
            //秒杀开始
            seckill.handleSeckillkill(seckillId,seckillBox);
        }
    },
    detail: {
        //详情页初始化
        init: function (params) {
            //用户手机验证和登录，计时和交互
            //规划我们的交互流程
            //在cookie中查找手机号
            var killPhone = $.cookie('killPhone');
            if(!seckill.validatePhone(killPhone)){
                //绑定手机号
                var killPhoneModal = $('#killPhoneModal');
                killPhoneModal.modal({
                    //显示弹出层
                    show:true,
                    //禁止位置关闭
                    backdrop:'static',
                    //关闭键盘事件
                    keyboard:false
                });
                $('#killPhoneBtn').click(function () {
                    var inputPhone = $('#killphoneKey').val();
                    if(seckill.validatePhone(inputPhone)){
                        //电话写入到cookie
                        $.cookie('killPhone',inputPhone,{expires:7,path:'/seckill'});
                        //刷新页面
                        window.location.reload();
                    }else{
                        $('#killphoneMessage').hide().html('<label class="label label-danger">手机号码错误！</label>').show(300);
                    }
                });
            }else{

            }
            //已经登陆，开启记时交互
            var startTime = params['startTime'];
            var endTime = params['endTime'];
            var seckillId = params['seckillId'];
            $.get(seckill.URL.now(),{},function (result) {
                if(result&&result['success']){
                    //获取当前时间
                    var nowTime = result['data'];
                    //时间判断，计时交互
                    seckill.countdown(seckillId, nowTime, startTime, endTime);
                }else{
                    console.log('result:' + result);
                }

            })

        }
    }
};