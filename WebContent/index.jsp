<%@ page language="java" import="midlab.myse.cm.ssh.SSHConnection" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>MYSE</title>
</head>
<body>

	<b>Choose an option</b>
	<br/>
	<br/>
		
	<form action="StartMYSE.jsp" method="POST">
	<br/>
	<input type="submit" value="Start MYSE">
	</form>
	
	<br/>
	
	<form action="ShowVM.jsp" method="POST">
	<br/>
	<input type="submit" value="Manual Config.">
	</form>
	
	<% Thread.sleep(1); %>
	
</body>
</html>