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