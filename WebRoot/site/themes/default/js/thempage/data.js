$(function() {
                $(".y_area_1_data a").hover(function() {
                    var colors = $(this).css("color");
                    $(this).css({
                        background: colors
                    });
                    $(this).find(".y_a_1_d_title").css({
                        color: "#fff"
                    });
                    $(this).find(".y_a_1_d_value").css({
                        display: "none"
                    });
                    $(this).find(".y_a_1_d_value_1").css({
                        color: "#fff",
                        display: "block"
                    });
                }, function() {
                    $(this).css({
                        background: "#fff"
                    });
                    $(this).find(".y_a_1_d_value").css({
                        display: "block"
                    });
                    $(this).find(".y_a_1_d_value_1").css({
                        display: "none"
                    });
                    $(this).find(".y_a_1_d_title").css({
                        color: "#3c3c3c"
                    });
                })
                var num = 0;
                $($(".y_contentTopRightIn li")[0]).clone(true).insertAfter($($(".y_contentTopRightIn li")[9]));
                $($(".y_contentTopRightIn li")[1]).clone(true).insertAfter($($(".y_contentTopRightIn li")[10]));
                $($(".y_contentTopRightIn li")[2]).clone(true).insertAfter($($(".y_contentTopRightIn li")[11]));
                function move() {
                    num = num - 30;
                    document.title=num;
                    if (num >= -270) {
                        $(".y_contentTopRightIn").animate({
                            marginTop: num
                        }, 3000);
                    } else {          
                        $(".y_contentTopRightIn").animate({marginTop: num
                        }, 3000, function() {
                            num = 0;
                            $(".y_contentTopRightIn").css({
                                marginTop: 0
                            });
                        });
                    }
                };
                var t = setInterval(move, 6000);
                $(".y_contentTopRightIn").hover(function() {
                    clearInterval(t);
                }, function() {
                    t = setInterval(move, 6000);
                })
            });
            $(function() {
                //数据可以动态生成，格式自己定义，cha对应china-zh.js中省份的简称
                var dataStatus = [{
                    cha: 'HKG',
                    name: '香港',
                    des: '15%'
                }, {
                    cha: 'HAI',
                    name: '海南',
                    des: '15%'
                }, {
                    cha: 'YUN',
                    name: '云南',
                    des: '15%'
                }, {
                    cha: 'BEJ',
                    name: '北京',
                    des: '15%'
                }, {
                    cha: 'TAJ',
                    name: '天津',
                    des: '15%'
                }, {
                    cha: 'XIN',
                    name: '新疆',
                    des: '15%'
                }, {
                    cha: 'TIB',
                    name: '西藏',
                    des: '15%'
                }, {
                    cha: 'QIH',
                    name: '青海',
                    des: '15%'
                }, {
                    cha: 'GAN',
                    name: '甘肃',
                    des: '15%'
                }, {
                    cha: 'NMG',
                    name: '内蒙古',
                    des: '15%'
                }, {
                    cha: 'NXA',
                    name: '宁夏',
                    des: '15%'
                }, {
                    cha: 'SHX',
                    name: '山西',
                    des: '15%'
                }, {
                    cha: 'LIA',
                    name: '辽宁',
                    des: '15%'
                }, {
                    cha: 'JIL',
                    name: '吉林',
                    des: '15%'
                }, {
                    cha: 'HLJ',
                    name: '黑龙江',
                    des: '15%'
                }, {
                    cha: 'HEB',
                    name: '河北',
                    des: '15%'
                }, {
                    cha: 'SHD',
                    name: '山东',
                    des: '15%'
                }, {
                    cha: 'HEN',
                    name: '河南',
                    des: '15%'
                }, {
                    cha: 'SHA',
                    name: '陕西',
                    des: '15%'
                }, {
                    cha: 'SCH',
                    name: '四川',
                    des: '15%'
                }, {
                    cha: 'CHQ',
                    name: '重庆',
                    des: '15%'
                }, {
                    cha: 'HUB',
                    name: '湖北',
                    des: '15%'
                }, {
                    cha: 'ANH',
                    name: '安徽',
                    des: '15%'
                }, {
                    cha: 'JSU',
                    name: '江苏',
                    des: '15%'
                }, {
                    cha: 'SHH',
                    name: '上海',
                    des: '15%'
                }, {
                    cha: 'ZHJ',
                    name: '浙江',
                    des: '15%'
                }, {
                    cha: 'FUJ',
                    name: '福建',
                    des: '15%'
                }, {
                    cha: 'TAI',
                    name: '台湾',
                    des: '15%'
                }, {
                    cha: 'JXI',
                    name: '江西',
                    des: '15%'
                }, {
                    cha: 'HUN',
                    name: '湖南',
                    des: '15%'
                }, {
                    cha: 'GUI',
                    name: '贵州',
                    des: '15%'
                }, {
                    cha: 'GXI',
                    name: '广西',
                    des: '15%'
                }, {
                    cha: 'GUD',
                    name: '广东',
                    des: '20%'
                }];
                $('#container').vectorMap({
                    map: 'china_zh',
                    color: "#9dcbdb", //地图颜色
                    onLabelShow: function(event, label, code) { //动态显示内容
                        $.each(dataStatus, function(i, items) {
                            if (code == items.cha) {
                                label.html(items.name + items.des);
                            }
                        });
                    }
                })
                $.each(dataStatus, function(i, items) {
                    if (parseInt(items.des.slice(0, -1)) >= 20) { //动态设定颜色，此处用了自定义简单的判断
                        var josnStr = "{" + items.cha + ":'#1e8dc9'}";
                        $('#container').vectorMap('set', 'colors', eval('(' + josnStr + ')'));
                    }
                });
            });