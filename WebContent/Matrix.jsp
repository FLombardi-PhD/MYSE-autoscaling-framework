<%@ page language="java" import="midlab.myse.cm.ssh.SSHConnection" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Configuration Manager</title>
</head>
<body>

	<b>Matrix</b>
	<br/>
	<br/>
		
	<form  method="POST">
	
	
	<%= createTable(createMatrix()) %>
		
	<%! public int[][] createMatrix(){
			int[][] mat1 = new int[150][150];
			int[][] mat2 = new int[150][150];
			int[][] mat3 = new int[150][150];
			for(int i=0; i<mat1.length; ++i){
				for(int j=0; j<mat1[0].length; ++j){
					mat1[i][j] = (i*j)^3 - 10;
					mat2[i][j] = (i^3) * (j-10);
				}
			}	
			for(int i=0;i<mat3.length;i++){ 
				for(int j=0;j<mat3[0].length;j++){ 
					for(int k=0;k<mat2.length;k++){ 
						mat3[i][j]+=mat1[i][k]*mat2[k][j]; 
					}
				}
			}
			
			return mat3;
		}			
	%>
	
	<%! public String createTable (int[][] vm){
			
			String matrix = "<table>";
			for(int i=0; i<vm.length; ++i){
				matrix += "<tr>";
				for(int j=0; j<vm[0].length; ++j){
					matrix += "<th>"+vm[i][j]+"</th>";
					if(j==vm[0].length-1) matrix += "</tr>";
				}	
			}
			
			matrix += "</table>";
			return matrix;
		}
	%>
	
	<br/>
	<br/>
	
	<input type="submit" value="Confirm">
	
	</form>
	
	<% Thread.sleep(1); %>
	
</body>
</html>