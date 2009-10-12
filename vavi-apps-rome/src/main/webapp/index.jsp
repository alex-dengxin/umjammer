<%@ page contentType="text/html; charset=Windows-31J" %>
<%@ page pageEncoding="Windows-31J" %>
 

<%
    try {

        String name = "test01.mp3";
        String title = "修羅場";

	request.setAttribute("name", name);
	request.setAttribute("title", title);

    } catch (Exception e) {
	throw new JspException(e);
    }

%>

<html>
<head>
<title>VaviCast</title>
<meta http-equiv="Content-Type" content="text/html; charset=Shift_JIS" />
</head>
<body style="background-color:#ddffee">
<center>VaviCast</center>
<hr />
<ul>
<li><a href="rss.jsp">${title}</a></li>
<li><a href="test00.rss">Physical File</a></li>
<li><a href="test02.rss">Generated File</a></li>
</ul>
<hr />
<center>Copyright (c) 2006<br /> by vavi.org</center>
</body>
</html>