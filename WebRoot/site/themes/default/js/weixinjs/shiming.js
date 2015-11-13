$(function(){
	$(".menu li a").click(function(){
		$(this).addClass("current");
		$(this).parent("li").siblings().find("a").removeClass("current");
		return false;
	});
	$(".rule dt").click(function(){
		$(".rule dd").toggleClass("sild");
		return false;
	});
})
$(function(){
	$(".menu li a").click(function(){
		var indexs = $(this).parent().index();
		$(".use_mant .use_cont").eq(indexs).addClass("Tshow").siblings().removeClass("Tshow");
	});
});
