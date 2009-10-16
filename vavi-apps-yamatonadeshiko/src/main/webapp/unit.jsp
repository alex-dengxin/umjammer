<%@ page contentType="text/html; charset=Windows-31J" %>
<%@ page import="java.util.*" %>
<%@ page import="vavi.apps.yamatonadeshiko.*" %>
<%@ page import="vavi.apps.yamatonadeshiko.impl.*" %>

<%!static final String successString = "Success!";

    static UnitDAO unitDAO = new LocalFileUnitDAO();%>

<%
    String command = null;
    String errorString = successString;

    String unit = null;

    try {
        unit = unitDAO.load();

        command = request.getParameter("command");
        if ("update".equals(command)) {
            unit = request.getParameter("unit");
            unitDAO.save(unit);
        }

    } catch (Exception e) {
        errorString = e.toString() + ": " +
                      e.getStackTrace()[0];
    }
%>

<html>
<head>
<title>Yamatonadeshiko</title>
</head>
<body>
Party Name
<hr>
<%
    if (errorString.equals(successString)) {
%>
<form action="unit.jsp" method="POST">
<input type="text" name="unit" value="<%= unit %>" /><br>
<input type="hidden" name="command" value="update" />
<input type="submit" value="Update" />
</form> 
<hr>
<a href="index.jsp">Top</a>
<%
    } else {
%>
<font color="darkyellow">
<%= errorString %>
</font>
<%
    }
%>
</body>
</html>
