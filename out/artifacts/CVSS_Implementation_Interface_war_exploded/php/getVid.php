<?php
	echo 'video';
	header('Content-Type: application/x-mpegURL');
	readfile("output/" . $_GET['video']);
?>
