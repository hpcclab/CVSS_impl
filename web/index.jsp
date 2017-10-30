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
  <script src="javascript/videoplayer.js"></script>
  <link href="https://unpkg.com/video.js/dist/video-js.css" rel="stylesheet">
  <script src="https://unpkg.com/video.js/dist/video.js"></script>
  <script src="https://unpkg.com/videojs-contrib-hls/dist/videojs-contrib-hls.js"></script>

  <script src="http://code.jquery.com/jquery-latest.min.js"></script>
 <!-- <script>
      $(document).ready(function() {
          $('#somebutton').click(function() {
              $.get('/processrequest', function(responseText) {
                  $('#somediv').text(responseText);
              });
          });
      });

      $("#somebutton").click(function(){
              $.ajax({
                      url:'/processrequest',
                      data:{name:'abc'},
                      type:'get',
                      cache:false,
                      success:function(data){
                          alert(data);
                          $('#somediv').text(responseText);
                      },
                      error:function(){
                          alert('error');
                      }
                  }
              );
          }
      );
  </script>

  <script>
      $.ajaxSetup({ cache: false });
      $(document).ready(function() {
          $('#mybutton').click(function(event) {
              var form = (event.target.form),
                  url = "/processrequest"
                      + "?name=" + escape(form.elements.videoName.value())
                      + "&resolution=" + escape(form.elements.resolution.value());

              $.get(url, function(getData) {
                  $('#somediv').text(getData);
              });
          });
      });
  </script>-->
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

    <div id="VideoPlayer">

      <video id=example-video width=600 height=300 class="video-js vjs-default-skin" controls preload auto>
        <source
                src="repositoryvideos/ff_trailer_part1/out.m3u8"
                type="application/x-mpegURL">
      </video>
      <script src="video.js"></script>
      <script src="videojs-contrib-hls.min.js"></script>
      <script>
          var player = videojs('example-video');
          player.play();
      </script>
      <!--<video id="Video" controls>
      </video>-->

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
          <option value="ff_trailer_part3">Fantastic Four Part 2</option>
        </select>

        <select name="resolution">
          <option value="resolution" selected disabled>Resolution</option>
          <option value="256x144">256x144</option>
          <option value="352x240">352x240</option>
          <option value="480x360">480x360</option>
          <option value="640x480">640x480</option>
          <option value="1280x720">1280x720</option>
        </select>

        <!--
        <input type="text" name="name" id="name" value="${name}">
        <input type="text" name="resolution" id="resolution" value="${resolution}">
        <input type="text" name="rHeight" id="rHeight" value="${rHeight}">
        <input type="text" name="rWidth" value="${rWidth}">-->

        <!--<button id="ScreenCapButton" class="button" title="screenCap" onclick=''>Screen Cap</button>

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
        </select> -->
       <!-- <button id="testing button" onclick="SendMyRequest()">Play</button>
        <!--<input type="submit" name="play" value="Play">-->
      <!--</form>-->
      <button id="mybutton">press here</button>
      <div id="somediv"> </div>

      <!--
      <script type="javascript" src="jquery-3.2.1.js">
          function pst(){
              $.post("http://localhost:8080/servlet", function(data){
                  $(".result").html(data);
                  $.append(data)
              });
          }
      </script>
      -->

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
<script type="text/javascript" src="js/jq.js"></script>
<script type="text/javascript">

    var form = $('#form1');
    form.submit(function () {

        $.ajax({
            type: form.attr('method'),
            url: form.attr('action'),
            data: form.serialize(),
            success: function (data) {
                var result=data;
                $('#result').attr("value",result);

            }
        });

        return false;
    });
</script>

</html>
