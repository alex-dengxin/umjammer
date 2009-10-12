/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEnclosureImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.XmlReader;
import com.totsp.xml.syndication.itunes.EntryInformation;
import com.totsp.xml.syndication.itunes.EntryInformationImpl;
import com.totsp.xml.syndication.itunes.FeedInformation;


/**
 * PodCast.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 060222 nsano initial version <br>
 */
public class PodCast {

    /**
     */
    public static void main(String[] args) throws Exception {
        new PodCast().write(args[0]);
    }

    /** */
    void read(String url) throws FeedException, IOException {
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed syndfeed = input.build(new XmlReader(new URL(url)));

        Module module = syndfeed.getModule("http://www.itunes.com/DTDs/Podcast-1.0.dtd");
        FeedInformation feedInfo = (FeedInformation) module;

        System.out.println(feedInfo.getImage());
        System.out.println(feedInfo.getCategory());
    }

    /** */
    void write(String outputType) throws IOException, FeedException {
        // channel
        SyndFeed channel = new SyndFeedImpl();
        channel.setFeedType(outputType);

        channel.setTitle("VaviCast");
        channel.setLink("http://localhost:8080/v/");
        channel.setDescription("VaviCast is now construction.");
        channel.setLanguage("Japanese");

        List<EntryInformation> modules = new ArrayList<EntryInformation>();
        EntryInformation itunesChannelEntry = new EntryInformationImpl();
        itunesChannelEntry.setAuthor("Copyright (c) 2006 vavi.org");
        itunesChannelEntry.setSubtitle("Subtitle");
        itunesChannelEntry.setSummary("Summary");
        modules.add(itunesChannelEntry);
        channel.setModules(modules);

        // item
        SyndEntry item = new SyndEntryImpl();
        SyndContent itemEntry = new SyndContentImpl();
        itemEntry.setValue("ドラマ大奥タイアップ");
        item.setDescription(itemEntry);
        item.setTitle("修羅場");
        item.setLink("http://localhost:8080/v/");

        modules = new ArrayList<EntryInformation>();
        EntryInformation itunesItemEntry = new EntryInformationImpl();
        itunesItemEntry.setAuthor("Naohide Sano");
        modules.add(itunesItemEntry);
        item.setModules(modules);
        item.setPublishedDate(new Date());

        List<SyndEnclosure> enclosures = new ArrayList<SyndEnclosure>();
        SyndEnclosure enclosure = new SyndEnclosureImpl();
        enclosure.setUrl("http://localhost:8080/v/data/test01.mp3");
        enclosure.setType("audio/mpeg");
        enclosures.add(enclosure);
        item.setEnclosures(enclosures);

        List<SyndEntry> entries = new ArrayList<SyndEntry>();
        entries.add(item);
        channel.setEntries(entries);
        channel.setPublishedDate(new Date());

        //
        SyndFeedOutput output = new SyndFeedOutput();
        output.output(channel, new PrintWriter(System.err));
    }
}

/* */
