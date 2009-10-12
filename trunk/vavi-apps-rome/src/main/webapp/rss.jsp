<%@ page contentType="text/xml; charset=UTF-8" %>
<%@ page pageEncoding="Windows-31J" %>

<%@ page import="java.util.*" %>
<%@ page import="com.sun.syndication.feed.synd.*" %>
<%@ page import="com.sun.syndication.io.*" %>
<%@ page import="com.totsp.xml.syndication.itunes.*" %>


<%
    try {
        request.setCharacterEncoding("Windows-31J");

	// DI http ->
//      String name = request.getParameter("name");

        String title = "修羅場";
        String description = "ドラマ大奥タイアップ";
        String author = "Naohide Sano";
        String link = "http://localhost:8080/v/data/test01.mp3";
        String type = "rss_2.0";

//	String pathWithContext = new URL(link).getPath();
//System.err.println("pathWithContext: " + pathWithContext);
//      if (pathWithContext.startsWith(request.getContextPath())) {
//            pathWithContext = pathWithContext.substring(request.getContextPath().length());
// 	}
//	String path = application.getRealPath(pathWithContext);
//System.err.println("path: " + path);
//	MP3File file = new MP3File(path);
//	int length = (int) (((Long) file.getProperty("Length")).longValue() * 1000);

	// DI -> RSS
        SyndFeed channel = new SyndFeedImpl();
        channel.setFeedType(type);

        channel.setTitle("VaviCast");
        channel.setLink("http://localhost:8080/v/");
        channel.setDescription("VaviCast is now construction.");
        channel.setLanguage("Japanese");

        List modules = new ArrayList();
        EntryInformation itunesChannelEntry = new EntryInformationImpl();
        itunesChannelEntry.setAuthor("Copyright (c) 2006 vavi.org");
        itunesChannelEntry.setSubtitle("Subtitle");
        itunesChannelEntry.setSummary("Summary");
        modules.add(itunesChannelEntry);
        channel.setModules(modules);

        // item
        SyndEntry item = new SyndEntryImpl();
        SyndContent itemEntry = new SyndContentImpl();
        itemEntry.setValue(description);
        item.setDescription(itemEntry);
        item.setTitle(title);
        item.setLink("http://localhost:8080/v/");

        modules = new ArrayList();
        EntryInformation itunesItemEntry = new EntryInformationImpl();
        itunesItemEntry.setAuthor(author);
        modules.add(itunesItemEntry);
        item.setModules(modules);
        item.setPublishedDate(new Date());

        List enclosures = new ArrayList();
        SyndEnclosure enclosure = new SyndEnclosureImpl();
        enclosure.setUrl(link);
        enclosure.setType("audio/mpeg");
        enclosures.add(enclosure);
        item.setEnclosures(enclosures);

        List entries = new ArrayList();
        entries.add(item);
        channel.setEntries(entries);
        channel.setPublishedDate(new Date());

        // output
	SyndFeedOutput output = new SyndFeedOutput();
	output.output(channel, out);

    } catch (Exception e) {
	throw new JspException(e);
    }
%>