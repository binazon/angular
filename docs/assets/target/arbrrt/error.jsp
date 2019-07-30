<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"
    import="java.io.*,java.util.*" %>
<%@ page isErrorPage="true" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Error <% out.print(pageContext.getErrorData().getStatusCode()); %></title>
<style type="text/css">
	table{
		border-collapse:collapse; 
	}
	
	table th{
		color: #FFFFFF;
	}
	
	table th, table td {
		border-collapse:collapse; 
		border-style:solid; 
		border-color:#d6d6d6; 
		border-width:1px;
	}
	
	.message{
		font-size:24px; 
		color:#FF6600; 
		font-weight:bold;
	}
	
	table.message td{
		border-color:#ffffff; 
	}
	
	span{
		cursor: pointer; 
		cursor: hand;
	}

</style>

<script type="text/javascript">
	function toggle(span, id) {
	    var target = document.getElementById(id);
	    if(target.style.display == 'none'){
	        target.style.display = 'block';
	        span.innerHTML = "&#9660;";
	    }
	    else {
	        target.style.display = 'none';
	        span.innerHTML = "&#9658;";
	    }
	}
</script>

</head>
<body style="font-family:arial; font-size:12px; padding-left:20px; padding-right:20px;">
		<table class="message"><tr height="100">
			<td valign="middle">
				<img src="data:image/png;base64,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" />			
			</td>
			<td width="20"/>
			<td width="100%" valign="middle" align="center">
				<% 

				String errorMessage = "[The Custom Error Page was accessed directly]";

				// Make sure we have a pageContext.
		        if(pageContext != null) {

		            // Get error data
		            ErrorData ed = null;
		            try {
		                ed = pageContext.getErrorData();
		            } catch(NullPointerException ne) {

		                // Sometimes this call causes a NullPointerException (PageContext.java:514)
		                // Catch and ignore it.. it effectively means we can't use the ErrorData
		                
		                // Times this happens:
		                // - error.jsp is accessed directly
		            }
		            
		            // Prepare error report
		            if(ed != null) {
		                String url = ed.getRequestURI();
		                int errorCode = ed.getStatusCode();
		                Throwable e = ed.getThrowable();

		                errorMessage = "Undefined error";
		                if(e != null) {
		                    // Handle JSP exceptions differently, show the lines in a <pre> tag
		                    Throwable cause = e;
		                    if (e instanceof ServletException) {
			                    cause = ((ServletException) e).getRootCause();
		                    }else{
		                    	cause = e.getCause();
		                    }
		                    if( cause != null && cause.getMessage() != null && cause.getMessage().indexOf("Exception in JSP") != -1)
		                        errorMessage = "An error occurred in a JSP file ...\n\n<pre>" + e.getMessage() + "</pre>";
		                    else
		                        errorMessage = cause.getMessage();
		    
		                } else {   
		                    // HTTP Error (404 or similar)
		                    if (errorCode == 403){
		                    	errorMessage = "Page Forbidden";
		                    }else if (errorCode == 404){
		                    	errorMessage = "Page Not Found";
		                    }else if (errorCode == 405){
		                    	errorMessage = "Method Not Allowed";
		                    }
		                }
		                errorMessage = "Error " + errorCode + " - "+ errorMessage;
		            }
		        }
				
		        out.println(errorMessage);
				
				%>
			</td>
		</tr></table>
		<br/>
		<table width="100%" height="100%" border="0"><tr align="left" style="border-width:0px">
		<td width="15px" style="border-width:0px"/>
		<td valign="top" style="border-width:0px">
		<p><span onclick="toggle(this,'remoteTable')">&#9660;</span>&nbsp;&nbsp;<b>Remote:</b> 
			<table id="remoteTable" width="100%" border="0" align="center">
			<tr bgcolor="#949494">
				<th>Name</th><th>Value(s)</th>
			</tr>
			<tr>
				<td>Address</td>
				<td><%
					out.print(request.getRemoteAddr());
				%></td>
			</tr>
			<tr>
				<td>Host</td>
				<td><%
					out.print(request.getRemoteHost());
				%></td>
			</tr>
			<tr>
				<td>User</td>
				<td><%
					out.print(request.getRemoteUser());
				%></td>
			</tr>			
			</table>
		</p>
		<br/>
		<p><span onclick="toggle(this,'requestTable')">&#9660;</span>&nbsp;&nbsp;<b>Request:</b>
			<table id="requestTable" width="100%" border="1" align="center">
			<tr bgcolor="#949494">
				<th>Name</th><th>Value(s)</th>
			</tr>
			<tr>
				<td>URI</td>
				<td><%
					out.print(request.getAttribute("javax.servlet.error.request_uri"));
				%></td>
			</tr>
			<tr>
				<td>Method</td>
				<td><%
					out.print(request.getMethod());
				%></td>
			</tr>
			<tr>
				<td>Protocol</td>
				<td><%
					out.print(request.getProtocol());
				%></td>
			</tr>
			<tr>
				<td>Secure</td>
				<td><%
					out.print(request.isSecure());
				%></td>
			</tr>
			<tr>
				<td>ContentType</td>
				<td><%
					out.print(request.getContentType());
				%></td>
			</tr>
			<tr>
				<td>CharacterEncoding</td>
				<td><%
					out.print(request.getCharacterEncoding());
				%></td>
			</tr>					
		</table>
		</p>
		<br/>
		<p><span onclick="toggle(this,'headersTable')">&#9660;</span>&nbsp;&nbsp;<b>Headers:</b>
			<table id="headersTable" width="100%" border="1" align="center">
			<tr bgcolor="#949494">
				<th>Name</th><th>Value(s)</th>
			</tr>
			<%
			   Enumeration<String> headerNames = request.getHeaderNames();
			   while(headerNames.hasMoreElements()) {
				  String paramName = headerNames.nextElement();
				  out.print("<tr><td>" + paramName + "</td>\n");
				  String paramValue = request.getHeader(paramName);
				  out.println("<td> " + paramValue + "</td></tr>\n");
			   }
			%>
			</table>
		</p>
		<%

			Enumeration<String> paramNames = request.getParameterNames();
			if (paramNames.hasMoreElements()){
				out.print("<br/>\n");
				out.print("<p><span onclick=\"toggle(this,'parametersTable')\">&#9660;</span>&nbsp;&nbsp;<b>Parameters:</b>\n");
				out.print("<table id=\"parametersTable\" width=\"100%\" border=\"1\" align=\"center\">\n");
				out.print("<tr bgcolor=\"#949494\">\n");
				out.print("<th>Name</th><th>Value(s)</th>\n");
				out.print("</tr>\n");

				while (paramNames.hasMoreElements()) {
					String paramName = paramNames.nextElement();
					out.print("<tr><td>" + paramName + "</td>\n");
					String paramValue = request.getParameter(paramName);
					out.println("<td> " + paramValue + "</td></tr>\n");
				}

				out.print("</table>\n");
				out.print("</p>\n");
			}

	        if(pageContext != null) {
	            ErrorData ed = null;
	            try {
	                ed = pageContext.getErrorData();
	            } catch(NullPointerException ne) {}
	            
	            if(ed != null) {
	                Throwable e = ed.getThrowable();                
	                
	                if(e != null) {
	                	
	                    Throwable cause = e;
	                    if (e instanceof ServletException) {
		                    cause = ((ServletException) e).getRootCause();
	                    }else{
	                    	cause = e.getCause();
	                    }
	                    if(cause != null) {
							out.print("<br/>\n");
							out.print("<p><span onclick=\"toggle(this,'stackTable')\">&#9660;</span>&nbsp;&nbsp;<b>Stack:</b>\n");
							out.print("<table id=\"stackTable\" width=\"100%\" border=\"1\" align=\"center\">\n");
							out.print("<tr bgcolor=\"#949494\">\n");
							out.print("<th>Trace</th>\n");
							out.print("</tr>\n");
							out.print("<tr><td><pre style=\"font-family:arial; font-size:12px;\">");
							e.printStackTrace(new java.io.PrintWriter(out));
		                    out.print("</pre></td></tr>\n");
		    				out.print("</table>\n");
		    				out.print("</p>\n");
	                    }
	                }
	            }
	        }
		%>
		</td>
		<td width="15px" style="border-width:0px"/>
		</tr></table>	
</body>
</html>