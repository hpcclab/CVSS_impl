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

      <video id="Video" controls>
      </video>

    </div>

  </div>

  <div id="Options">
    <div id="Label">More Options</div>
    <div id="OptionPanel">

      <form action="${pageContext.request.contextPath}/processrequest" method="post">
        <!--<select id="Videos" name="${name}" >
          <option value="selectVideo.png" selected disabled>Videos</option>
          <option value="videos/bbb_trailer.mp4">Big Buck Bunny</option>
          <option value="videos/ff_trailer_part1.mp4">Fantastic Four Part 1</option>
          <option value="videos/ff_trailer_part3.mp4">Fantastic Four Part 2</option>
        </select>

        <select name="${resolution}" >
          <option value="resolution" selected disabled>Resolution</option>
          <option value="320x240">320x240</option>
          <option value="640x480">640x480</option>
          <option value="720x540">720x540</option>
        </select>-->

        <input type="text" name="name" id="name" value="${name}">
        <input type="text" name="resolution" id="resolution" value="${resolution}">
        <input type="text" name="rHeight" id="rHeight" value="${rHeight}">
        <input type="text" name="rWidth" value="${rWidth}">

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
        <input type="submit" name="play" value="Play">
      </form>

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

</html>
