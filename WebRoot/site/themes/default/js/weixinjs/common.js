// JavaScript Document
window.onorientationchange=function(){ 
		var obj=document.getElementById('orientation');

		if(window.orientation==0){
                obj.style.display='none';
        }else
        {
                alert('横屏内容太少啦，请使用竖屏观看！');
                obj.style.display='block';
        }
		};