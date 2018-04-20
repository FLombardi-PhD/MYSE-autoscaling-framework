<%@ page language="java" import="midlab.myse.cm.ssh.SSHConnection" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Configuration Manager</title>
</head>
<body>

	<b>VMs List</b>
	<br/>
	<br/>
		
	<form action="configMan" method="POST">
	
	<%! SSHConnection sshc = new SSHConnection(); %>
	<%= createTable(sshc.get())%>
		
	
	
	<%! public String createTable (String[][] vm){
			
			String color = "";
			String table =""
			+"<table border=\"1\">"
			+"<tr>  <th>Blade</th>  <th>VMID</th> <th>Name</th> <th>Status VM</th> <th>Switch Status</th>  </tr>";
			for(int i=0; i<vm.length; ++i){
				if(vm[i][3].equals("ON")) color = "green";
				else color = "red";
				table += "<tr> <th>" + vm[i][0] + "</th>"
					+ "<th>" +vm[i][1] + "</th>"
					+ "<th>" +vm[i][2] + "</th>"
					+ "<th>" + "<font color=\"" + color + "\">" + vm[i][3] + "</font></th>"
					+ "<th><input type=\"checkbox\" id=\"" + vm[i][0] + "&" + vm[i][1] + "\" name=\"" + vm[i][0] + "&" + vm[i][1] + "\"></input></th> </tr>";
			}
			table += "</table>";
			return table;
		}
	%>
	
	<br/>
	<br/>
	
	<input type="submit" value="Confirm">
	
	</form>
	
	<% Thread.sleep(1); %>
	
</body>
</html>