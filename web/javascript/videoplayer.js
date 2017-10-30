var videoPlayer;

document.addEventListener("DOMContentLoaded", function(){ initializeVideoPlayer(); }, false); //waits for video to load

function initializeVideoPlayer(){
	videoPlayer = document.getElementById('Video');
	videoPlayer.controls = true;					//true for default controls, false for custom
}

function togglePlayPause(){
	var btn = document.getElementById('PlayPauseButton');
	if (videoPlayer.paused || videoPlayer.ended){
		changeButtonType(btn, "Pause");
		videoPlayer.play();
	}
	else{
		changeButtonType(btn, "Play");
		videoPlayer.pause();
	}
}

function SendMyRequest(url2get) {

    var req;
    if (window.XMLHttpRequest) {
        req = new XMLHttpRequest();
    } else if (window.ActiveXObject) {
        req = new ActiveXObject("Microsoft.XMLHTTP");
    }
    if (req != undefined) {
        // req.overrideMimeType("application/json"); // if request result is JSON
        try {
            req.open("POST", url2get, false); // 3rd param is whether "async"
        }
        catch(err) {
            alert("couldnt complete request. Is JS enabled for that domain?\\n\\n" + err.message);
            return false;
        }
        req.send(sendstr); // param string only used for POST

        if (req.readyState == 4) { // only if req is "loaded"
            if (req.status == 200)  // only if "OK"
            { return req.responseText ; }
            else    { return "XHR error: " + req.status +" "+req.statusText; }
        }
    }
    alert("req for getAsync is undefined");

    var var_str = "var1=" + var1  + "&var2=" + var2;
    var ret = postAsync(url, var_str) ;
    // hint: encodeURIComponent()

    if (ret.match(/^XHR error/)) {
        console.log(ret);
        return;
    }
}


function loadVideo(){
	var button = document.getElementById('testing button');
	button.setAttribute();
	button
}


function changeButtonType(btn, value){
	btn.title = value;
	btn.innerHTML = value;
	btn.className = value;
}

function loadAnotherVideo(){
	var video = document.getElementById('Video');
	var source = document.getElementById("Videos");
	var src = source.options[source.selectedIndex].value;

	video.setAttribute('src', src);
	changePlaybackRate();
}

function changePlaybackRate(){
	var video = document.getElementById('Video');
	var source = document.getElementById("PlaybackRate");
	var rate = source.options[source.selectedIndex].value;

	video.playbackRate = rate;
}