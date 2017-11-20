<%@page import="testpackage.Test" %>
<%--
  Created by IntelliJ IDEA.
  User: pi
  Date: 6/29/17
  Time: 3:31 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>

<html lang="en">
<head>

  <title>Cloud Video Streaming</title>
  <meta charset="utf-8">
  <meta name="description" content="Cloud Video Streaming Service Interface">
  <meta name="author" content="HPCCLab">

  <link rel="stylesheet" href="css/style.css">
  <!--<script src="javascript/videoplayer.js"></script>-->
  <link href="https://unpkg.com/video.js/dist/video-js.css" rel="stylesheet">
  <script src="https://unpkg.com/video.js/dist/video.js"></script>
  <script src="https://unpkg.com/videojs-contrib-hls/dist/videojs-contrib-hls.js"></script>

  <script src="http://code.jquery.com/jquery-latest.js"></script>
  <script>
      $(document).ready(function(){
          $('#submit').click(function(event){
              var videoname=$('#Videos').val();
              var resolution=$('#resolution').val();

              if(videoname !== null){
                $.get('RequestController',{videoname:videoname, resolution:resolution},function(responseText){
                    //$('#somediv').text(responseText);
                    var player = videojs('example-video');
                    var text = responseText;
                   // /*
                    player.src({
                        src: text,
                        type: 'application/x-mpegURL',
                        withCredentials: true
                    });
                   // */
                    /*
                    $('#vidsrc').attr('src',$(this).data(text));
                    return false;
                    */
                    player.play();
                });
              }
          });
      });
  </script>
</head>

<body>

<div id="Header">

  <h1>Cloud Video Streaming Interface
    <img src="images/hpccLogo.png" alt="HPCC Lab" height=50 width=100>
    <img src="images/ulLogo.png" alt="University of Louisiana" height=60 width=150 style="float: right; margin-left: 15px;">
    <form action="/action_page.php" id="SearchBox">
      <input type="text" name="query">
      <input type="button" name="search" value="Search">
    </form>
  </h1>

</div>

<div id="Content">

  <div id="Wrapper">

    <div id="VideoPlayer" style="margin: auto">
      <!--src="repositoryvideos/bbb_trailer/out.m3u8"-->
      <video id=example-video width=600 height=300 class="video-js vjs-default-skin" controls="" autoplay="true">
        <source id="videosrc"
                type="application/x-mpegURL">
      </video>
      <script>
          var player = videojs('example-video');
          player.play();
      </script>
    </div>

  </div>

  <div id="Options">
    <div id="Label">More Options</div>
    <div id="OptionPanel">

      <form id="request form" action="${pageContext.request.contextPath}/processrequest" method="post">
        <select id="Videos" name="videoName" >
          <option value="selectVideo.png" selected disabled>Videos</option>
          <option value="bbb_trailer">Big Buck Bunny</option>
          <option value="ff_trailer_part1">Fantastic Four Part 1</option>
          <option value="ff_trailer_part3">Fantastic Four Part 3</option>
        </select>

        <select id="resolution" name="resolution">
          <option value="resolution" selected disabled>Resolution</option>
          <option value="256x144">256x144</option>
          <option value="352x240">352x240</option>
          <option value="480x360">480x360</option>
          <option value="640x480">640x480</option>
          <option value="1280x720">1280x720</option>
        </select>

        <div id="ConversionPanel">

          <select name="Format">
            <option value="convert" selected disabled>Convert</option>
            <option value="mp4">.mp4</option>
            <option value="flv">.flv</option>
            <option value="ogg">.ogg</option>
            <option value="webm">.webm</option>
            <option value="gif">.gif</option>
          </select>

          <input type="text" name="start" value="Start Time" size=4 >
          <input type="text" name="end" value="End Time" size=4 >

        </div>


        <button id="SubtitleButton" class="button" title="subtitles" onclick=''>Subtitles</button>

        <select id="PlaybackRate" name="PlaybackRate" onchange="changePlaybackRate();">
          <option value=1 selected disabled>Playback Rate</option>
          <option value=.25>.25</option>
          <option value=.5>.5</option>
          <option value=1>1</option>
          <option value=1.5>1.5</option>
          <option value=2>2</option>
        </select>


        <input id="submit" type="button" value="Play">
      </form>
      <div id="somediv"> </div>
    </div>

  </div>

  <div id="Footer">
    <h2>
      <small>&copy; Copyright <script language="javascript" type="text/javascript">
          var today = new Date()
          var year = today.getFullYear()
          document.write(year)
      </script>, HPCC Lab
      </small>
    </h2>
  </div>

</div>


</body>
</html>
