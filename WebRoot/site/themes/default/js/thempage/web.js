/*拉手*/
$(function(){
	var widthObj = $(document).width();
	$('.leftImg').css('right',widthObj/2-450)
	$('.rightImg').css('left',widthObj/2-250)
	var leftObj = parseInt($('.leftImg').css('right'))
	if(leftObj>-165)
	{
   			window.onscroll = function(){
			   var numLeft,numRight,scrollObj;
			   numLeft = parseInt($('.leftImg').css('right'))
			   numRight = parseInt($('.rightImg').css('left'))
			   scrollObj = $(document).scrollTop();
			   if(numLeft-scrollObj>-74)
			   {
					 $('.leftImg').stop().animate({right:numLeft-scrollObj})
					 $('.rightImg').stop().animate({left:numRight-scrollObj})
				}
				
				else
				{
					$('.leftImg').stop().animate({right:-165})
					$('.rightImg').stop().animate({left:17})
					}

			}
	}
	else
	{
		$('.leftImg').stop().animate({right:-165})
		$('.rightImg').stop().animate({left:17})
		}
})


/*放大镜*/
$(document).ready(function(){
	var $leftBtn = $(".leftBtn"),
		$rightBtn = $(".rightBtn"),
		$ulList = $(".ulList"),
		$tabMiddle = $(".tabMiddle"),
		$tabLeft = $(".tabLeft"),
		$tabRight = $(".tabRight"),
		arrImg = [
			["/site/themes/default/images/thempage/h-5.png","/site/themes/default/images/thempage/h-5-on.png"],
			["/site/themes/default/images/thempage/h-10.png","/site/themes/default/images/thempage/h-10-on.png"],
			["/site/themes/default/images/thempage/h-6.png","/site/themes/default/images/thempage/h-6-on.png"],
			["/site/themes/default/images/thempage/h-12.png","/site/themes/default/images/thempage/h-12-on.png"],
			["/site/themes/default/images/thempage/h-13.png","/site/themes/default/images/thempage/h-13-on.png"],
			["/site/themes/default/images/thempage/h-5.png","/site/themes/default/images/thempage/h-5-on.png"],
			["/site/themes/default/images/thempage/h-10.png","/site/themes/default/images/thempage/h-10-on.png"],
			["/site/themes/default/images/thempage/h-6.png","/site/themes/default/images/thempage/h-6-on.png"],
			["/site/themes/default/images/thempage/h-12.png","/site/themes/default/images/thempage/h-12-on.png"],
			["/site/themes/default/images/thempage/h-13.png","/site/themes/default/images/thempage/h-13-on.png"],
		];
	function Menu(){
		this.init();
	};
	var proto = {
		init: function(){
			this.before();
			this.clickEvent();
			this.timerEvent();
		},
		before: function(){
			var nb = this.nb = {};
			nb.index = 0;	//头部参数
			nb.index2 = 2;	//中间主要显示
			nb.indexLeft = 2-1;
			nb.indexRight = 2+1;
			nb.num = 2;
			nb.timer = null;
			nb.canMove = true;
			this.amend();
		},
		amend: function(){
			var nb = this.nb;
			$ulList.css("width", $ulList.width()*2);
			$tabLeft.css({
						"width": $tabLeft.width()*2,
						"left": -nb.indexLeft*$tabLeft.find("li").width()
					});
			$tabMiddle.css({
						"width": $tabMiddle.width()*2,
						"left": -nb.index2*$tabMiddle.find("li").width()
					});
			$tabRight.css({
						"width": $tabRight.width()*2,
						"left": -nb.indexRight*$tabRight.find("li").width()
					});
			var tmp = nb.tmp = [$tabLeft,$tabMiddle,$tabRight];
			fnClone(tmp);
			function fnClone(tmp){
				$ulList.append( $ulList.children().clone() );
				$(tmp).each(function(i,v){
					var _child=v.children().clone();
					$(_child).each(function(i2,v2){
						v.append(v2);
					});
				});
			};
			nb.$li = $ulList.find("li");
			nb.$li.eq(7).find("img").attr("src", arrImg[7][0]);
		},
		clickEvent: function(){
			var This = this,
				nb = this.nb;
			$leftBtn.on("click", function(){
				count("left");
			});
			$rightBtn.on("click", function(){
				count("right");
			});
			function count(direction){
				if(!nb.canMove) return;
				nb.canMove = false;
				This.checkImg();
				if( direction == "left" ){
					nb.indexLeft--;
					nb.indexRight--;
					nb.num--;
					nb.index--;
					nb.index2--;
				}else{
					nb.num++;
					nb.indexLeft++;
					nb.indexRight++;
					nb.index++;
					nb.index2++;
				};
				This.moveEvent();
			};
		},
		timerEvent: function(){
			var nb = this.nb;
			$ulList.add($tabMiddle).on('mouseover',function(){
				clearInterval(nb.timer);
			});
			$ulList.add($tabMiddle).on('mouseout',function(){
				clearInterval(nb.timer);
				go();
			});
			go();
			function go(){
				nb.timer = setInterval(function(){
					$rightBtn.trigger("click");
				},3000);
			};
		},
		checkImg: function(one){
			var nb = this.nb;
			if( one ){
				nb.$li.eq(nb.num).find("img").attr("src", arrImg[nb.num][1]);
			}else{
				nb.$li.eq(nb.num).find("img").attr("src", arrImg[nb.num][0]);
			};
		},
		moveEvent: function(){
			var This = this;
				nb = this.nb;
			//头部
			if( nb.index == 6 ){
				fnLeft0('top');
				nb.index = 1;
			}else if( nb.index == -1 ){
				fnLeftMiddle('top');
				nb.index = 4;
			};
			//内容
			if( nb.index2 == 6 ){
				fnLeft0('middle');
				nb.index2 = 1;
			}else if( nb.index2 == -1 ){
				fnLeftMiddle('middle');
				nb.index2 = 4;
			};
			//left
			if( nb.indexLeft == 6 ){
				fnLeft0('left');
				nb.indexLeft = 1;
			}else if( nb.indexLeft == -1 ){
				fnLeftMiddle('left');
				nb.indexLeft = 4;
			};
			//right
			if( nb.indexRight == 6 ){
				fnLeft0('right');
				nb.indexRight = 1;
			}else if( nb.indexRight == -1 ){
				fnLeftMiddle('right');
				nb.indexRight = 4;
			};
			//图片
			if( nb.num == 1 ){
				nb.num = 6;
			}else if( nb.num == 8 ){
				nb.num = 3;
			};
			fnMove();
			function fnLeft0(str){
				if( str == 'top' ){
					$ulList.css("left",0);
				};
				if( str == 'middle' ){
					$tabMiddle.css("left",0);
				};
				if( str == 'left' ){
					$tabLeft.css("left",0);
				};
				if( str == 'right' ){
					$tabRight.css("left",0);
				};
			};
			function fnLeftMiddle(str){
				if( str == 'top' ){
					$ulList.css("left", -$ulList.width()/2);
				}
				if( str == "middle" ){
					$tabMiddle.css("left",-$tabMiddle.width()/2);
				};
				if( str == "left" ){
					$tabLeft.css("left",-$tabLeft.width()/2);
				};
				if( str == "right" ){
					$tabRight.css("left",-$tabRight.width()*5);
				};
			};
			function fnMove(){
				$ulList.animate({"left": -nb.index*$ulList.children().width()},500,"linear",function(){
									This.checkImg(1);
									nb.canMove = true;
								});
				$tabLeft.animate({"left": -nb.indexLeft*$tabLeft.children().width()},500,"linear");
				$tabMiddle.animate({"left": -nb.index2*$tabMiddle.children().width()},500,"linear");
				$tabRight.animate({"left": -nb.indexRight*$tabRight.children().width()},500,"linear");
			};
		}
	};
	Menu.prototype = proto;
	var m = new Menu();
});
