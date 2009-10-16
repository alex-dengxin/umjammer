<?xml version="1.0" encoding="Windows-31J"?>
<jsp:root
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core"
    xmlns:x="http://java.sun.com/jsp/jstl/xml"
    xmlns:fmt="http://java.sun.com/jsp/jstl/fmt"
    xmlns:fn="http://java.sun.com/jsp/jstl/functions"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:h="http://java.sun.com/jsf/html"
    version="2.1">
  <jsp:directive.page
       language="java"
       contentType="text/html; charset=Windows-31J"
       pageEncoding="Windows-31J" />
  <jsp:directive.page import="vavi.apps.umjammer00.jaxb1.*"/>
  <jsp:directive.page import="vavi.apps.umjammer00.*" />
  <jsp:directive.page import="org.restlet.*" />
  <jsp:directive.page import="org.restlet.data.*" />
  <jsp:directive.page import="org.restlet.resource.*" />
<html>
<body>

<jsp:scriptlet>
    if ("POST".equals(request.getMethod()) &amp;&amp;
        "make".equals(request.getParameter("mode"))) {

        System.setProperty("sen.home", "c:/tmp/000/sen-1.2.2.1");
        System.setProperty("yjws.appid", "umjammer_00");

        String name = request.getParameter("name");

        Test6 test6 = new Test6();

        String url = test6.query(test6.x(name));
        if (url == null) {
            url = test6.query(test6.x(name).split(" ")[0]);
        }

        request.setAttribute("name", name);
        request.setAttribute("url", url);
</jsp:scriptlet>
        名前： ${name}<br/>
        生年月日： bbbb 年 c 月 d 日
        <div style="">
        <img src="${url}" style="width:120;height:160" /><br/>
        </div>
        <hr/>
<jsp:scriptlet>
        Test5 test5 = new Test5();

        String target = "小学校";
        ResultSet result1 = test5.query1(name + " " + target);
        String result2 = test5.query2(result1, "名詞-固有名詞-地域", "名詞");
        String result3 = test5.query2(result1, "名詞-固有名詞", "名詞");
        request.setAttribute("target", target);
        request.setAttribute("result2", result2);
        request.setAttribute("result3", result3);
</jsp:scriptlet>
        xxxx 年 3 月 ${result2}${result3} ${target} 卒業 <br/>
        <hr />
<jsp:scriptlet>
        target = "中学校";
        result1 = test5.query1(name + " " + target);
        result2 = test5.query2(result1, "名詞-固有名詞-地域", "名詞");
        result3 = test5.query2(result1, "名詞-固有名詞", "名詞");
        request.setAttribute("target", target);
        request.setAttribute("result2", result2);
        request.setAttribute("result3", result3);
</jsp:scriptlet>
        yyyy 年 3 月 ${result2}${result3} ${target} 卒業 <br/>
        <hr />
<jsp:scriptlet>
        target = "高等学校";
        result1 = test5.query1(name + " " + target);
        result2 = test5.query2(result1, "名詞-固有名詞-地域", "名詞");
        result3 = test5.query2(result1, "名詞-固有名詞", "名詞");
        request.setAttribute("target", target);
        request.setAttribute("result2", result2);
        request.setAttribute("result3", result3);
</jsp:scriptlet>
        zzzz 年 3 月 ${result2}${result3} ${target} 卒業 <br/>
        <hr />
<jsp:scriptlet>
        target = test5.random.nextInt(2) == 1 ? "大学" : "専門学校";
        result1 = test5.query1(test5.x(name) + " " + target);
        result2 = test5.query2(result1, "名詞-固有名詞-地域", "名詞");
        result3 = test5.query2(result1, "名詞-固有名詞", "名詞");
        request.setAttribute("target", target);
        request.setAttribute("result2", result2);
        request.setAttribute("result3", result3);
</jsp:scriptlet>
        aaaa 年 3 月 ${result2}${result3} ${target} 卒業 <br/>
        <hr />
<jsp:scriptlet>
        target = "趣味";
        result1 = test5.query1(test5.x(name) + " " + target);
        result2 = test5.query2(result1, "名詞-一般", "名詞");
        result3 = test5.query2(result1, "動詞", "名詞");
        request.setAttribute("target", target);
        request.setAttribute("result2", result2);
        request.setAttribute("result3", result3);
</jsp:scriptlet>
        ${target}： ${result2}を${result3}こと <br/>
        <hr />
<jsp:scriptlet>
        target = "性格";
        result1 = test5.query1(test5.x(name) + " " + target);
        result2 = test5.query2(result1, "名詞-一般", "名詞");
        result3 = test5.query2(result1, "動詞", "名詞");
        request.setAttribute("target", target);
        request.setAttribute("result2", result2);
        request.setAttribute("result3", result3);
</jsp:scriptlet>
        ${target}： ${result2}が${result3}がち <br/>
        <hr />
<jsp:scriptlet>
        target = "志望動機";
        result1 = test5.query1(test5.x(name) + " " + target);
        result2 = test5.query2(result1, "名詞-一般", "名詞");
        result3 = test5.query2(result1, "動詞", "名詞");
        request.setAttribute("target", target);
        request.setAttribute("result2", result2);
        request.setAttribute("result3", result3);
</jsp:scriptlet>
        ${target}： 御社の${result2}に心が${result3}した <br/>
        <hr />
        <a href="index.jsp">もう一度作り直す</a><br/>
        <a href="mailto:recruit@klab.org">KLab 株式会社にこの履歴書で応募する</a>
<jsp:scriptlet>
    } else { 
</jsp:scriptlet>

<h2>りれきしょめーかー</h2>

<form action="index.jsp" method="post">
 あなたのお名前<br/>
 <input type="text" name="name" value="" />
 <input type="submit" value="さくせい" />
 <input type="hidden" name="mode" value="make" />
</form>

<jsp:scriptlet>
    } 
</jsp:scriptlet>

</body>

</html>

</jsp:root>
