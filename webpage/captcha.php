<?php
$host = "localhost";
$username = "x";
$password = "x";
$database = "Recaptcha";
$mysqli = new mysqli($host, $username, $password, $database);

// Google recaptcha keys (make sure to use recaptcha v2)
$site_key = "x";
$secret = "x";

// Dont touch anything below
$expectedCode = "";
$code = $_GET['code'];

$query = "SELECT * FROM `users` WHERE `code` = '$code' AND `passed` = '0'";

echo "
<html>
	<body>
		<div>
			<img src=\"logo.png\" height=\"300px\" width=\"auto\"></img>
		</div>
		<div>
			<h1>Please complete the captcha</h1>
		</div>
		<script src='https://www.google.com/recaptcha/api.js' async defer ></script>
	</body>
	<style>
		body {
			display: flex;
			flex-direction: column;
			justify-content: center;
			align-items: center;
			font-family: sans-serif;
			background-color: #17223b;
		}
		h1 {
		  color: #6b778d;
		}
		h3 {
			margin: 0;
			color: #FF5555;
		}
		a {
		  text-decoration: underline;
		  color: white;
		}
		input {
			width: 100%;
			margin-top: 10px;
			height: 30px;
			border-radius: 25px;
			border: none;
			font-size: 16px;
		}
		input:hover {
			cursor: pointer;
			opacity: 0.7;
			font-size: 14px;
		}
		.success {
			margin-top: 10px;
			color: green;
		}
		.failed {
			margin-top: 10px;
			color: red;
		}
	</style>
</html>
";
 
// grab expected code from database
if ($result = $mysqli->query($query)) {
 
    while ($row = $result->fetch_assoc()) {
        $expectedCode = $row["code"];
    }
$result->free();
}

// if code in url doesn't match expected code, give error instead of captcha
if ($code != $expectedCode) {
	echo '<h3>Error: The code provided in your url is not marked as pending.</h3>';
} else {
	echo "
	<form action=\"\" method=\"post\">
		<div class=\"g-recaptcha\" data-sitekey=\"$site_key\"></div>
		<input type=\"submit\"/>
	</form>
	<a href=\"#\">How-to?</a>
	";
}

// handle recaptcha response
if(isset($_POST['g-recaptcha-response']) && !empty($_POST['g-recaptcha-response']))
  {
        $verifyResponse = file_get_contents('https://www.google.com/recaptcha/api/siteverify?secret='.$secret.'&response='.$_POST['g-recaptcha-response']);
        $responseData = json_decode($verifyResponse);
        if($responseData->success) {
			$query = "UPDATE `users` SET `passed`='1',`completion_time`=CURRENT_TIMESTAMP WHERE `code` = '$code'";
			$mysqli->query("$query");
			$mysqli->close();
            echo "<div><h3 class=\"success\">Success, return in-game!</h3></div>";
        }
        else {
            echo "<div><h3 class=\"failed\">Captcha failed, please try again!</h3></div>";
        }
   }
?>