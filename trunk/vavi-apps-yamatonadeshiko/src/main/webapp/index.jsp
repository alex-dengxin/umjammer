<%@ page contentType="text/html; charset=Windows-31J" %>
<%@ page import="java.util.*" %>
<%@ page import="vavi.apps.yamatonadeshiko.*" %>
<%@ page import="vavi.apps.yamatonadeshiko.impl.*" %>
<%@ page import="vavi.apps.yamatonadeshiko.Shuffler.Member" %>
<%@ page import="vavi.apps.yamatonadeshiko.Shuffler.Type" %>
<%@ page import="org.apache.commons.logging.*" %>

<%!static Log log = LogFactory.getLog("yamatonadeshiko.index.jsp");

    static MailDAO mailDAO = new MySQLMailDAO();

    static UnitDAO unitDAO = new LocalFileUnitDAO();

    static final ResourceBundle rb = ResourceBundle.getBundle("vavi.apps.yamatonadeshiko.yamatonadeshiko");

    static final String successString = "Success!";%>

<%
    Shuffler shuffler = new Version2Shuffler();
    Mailer mailer = new Mailer();

    String command = null;
    String errorString = successString;

    List<Member> femaleManagers = null;
    List<Member> maleManagers = null;
    List<Member> females = null;
    List<Member> males = null;
    Map<Member, Member> pair = null;

    String unit = null;

    long t1 = 0, t2 = 0, t3 = 0;

    try {
        unit = unitDAO.load();

        command = request.getParameter("command");
log.debug("command: " + command);
        if ("clear".equals(command)) {
            mailDAO.clear(unit);
        }

        List<Member>[] memberLists = mailDAO.load(unit);
        femaleManagers = memberLists[0];
        females = memberLists[1];
        maleManagers = memberLists[2];
        males = memberLists[3];
    
        if ("shuffle".equals(command)) {
            females.addAll(0, femaleManagers);
            males.addAll(0, maleManagers);
t1 = System.currentTimeMillis();
            pair = shuffler.shuffle(females, males);
t2 = System.currentTimeMillis();

            mailer.open(rb.getString("mailer.from"));
            for (Member member : pair.keySet()) {
                Member couple = pair.get(member);
                mailer.write(couple.email,
                     rb.getString("mail.receiver.subject"),
                     String.format(rb.getString("mail.receiver.text"), member.email));
                mailer.write(member.email,
                     rb.getString("mail.sender.subject"),
                     String.format(rb.getString("mail.sender.text"), couple.email));
            }
            mailer.close();
t3 = System.currentTimeMillis();
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
<a href="index.jsp">Project YN</a>
<hr>
Party of <a href="unit.jsp"><%= unit %></a>
<hr>
<%
    if (!"shuffle".equals(command)) {
%>
<font color="red">+ Female Manager</font><br>
<%
        for (Member member : femaleManagers) {
%>
<%= member.email %><br>
<%
        }
    }
%>
<font color="red">+ Females</font><br>
<%
    for (Member member : females) {
%>
<%= member.email %><br>
<%
    }
%>
<%
    if (!"shuffle".equals(command)) {
%>
<font color="blue">+ Male Manager</font><br>
<%
        for (Member member : maleManagers) {
%>
<%= member.email %><br>
<%
        }
    }
%>
<font color="blue">+ Males</font><br>
<%
    for (Member member : males) {
%>
<%= member.email %><br>
<%
    }
%>
<hr>
<%
    if (errorString.equals(successString)) {
%>
<form action="index.jsp" method="POST">
<input type="submit" value="Clear" />
<input type="hidden" name="command" value="clear" />
</form> 
<%
        if ("shuffle".equals(command)) {
%>
<hr>
<%
            for (Member member : pair.keySet()) {
                Member couple = pair.get(member);
%>
<%= member %> &rarr; <%= couple %><br>
<%
            }
%>
<hr>
shuffle: <%= t2 - t1 %> ms<br>
mail: <%= t3 - t2 %> ms
<%
        } else {
%>
<form action="index.jsp" method="POST">
<input type="submit" value="Reload" />
<input type="hidden" name="command" value="reload" />
</form> 
<form action="index.jsp" method="POST">
<input type="submit" value="Shuffle" />
<input type="hidden" name="command" value="shuffle" />
</form> 
<%
        }
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
