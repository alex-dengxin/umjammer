/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.gps.vendor.nmea;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.TimeZone;

import vavi.gps.BasicGpsData;
import vavi.gps.Channel;
import vavi.gps.GpsData;
import vavi.gps.GpsFormat;
import vavi.util.Debug;
import vavi.util.StringUtil;


/**
 * NMEA-0183 Version 2.0?
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030318 nsano initial version <br>
 *          0.01 030324 nsano add sentence selector <br>
 */
public class NmeaGpsFormat implements GpsFormat {

    /** TODO not implemented */
    public GpsData parse(byte[] line) {
        BasicGpsData data = new BasicGpsData();
        data.setRawData(line);
        return data;
    }

    /** */
    private static final String crlf = "\r\n";

    /** */
    private boolean useGGA = true;
    /** */
    private boolean useGSA = true;
    /** */
    private boolean useGSV = true;
    /** */
    private boolean useGLL = true;
    /** */
    private boolean useRMC = true;
    /** */
    private boolean useVTG = false;

    /** without cr,lf */
    public byte[] format(GpsData gpsData) {

        StringBuilder sb = new StringBuilder();

        if (useGGA) {
            sb.append(toGGA(gpsData));
        }
        if (useGSA) {
            sb.append(toGSA(gpsData));
        }
        if (useGSV) {
            sb.append(toGSV(gpsData));
        }
        if (useGLL) {
            sb.append(toGLL(gpsData));
        }
        if (useRMC) {
            sb.append(toRMC(gpsData));
        }
        if (useVTG) {
            sb.append(toVTG(gpsData));
        }

        sb.setLength(sb.length() - 2); // remove last cr, lf

        return sb.toString().getBytes();
    }

    /** */
    protected String toSentence(String type, String data) {
        String sentence = "GP" + type + "," + data;
        String checksum = "*" + StringUtil.toHex2(getChecksum(sentence));
        String line = "$" + sentence + checksum + crlf;
//System.err.print(line);
        return line;
    }

    /**
     * GGA - Global Positioning System Fix Data
     * <pre>
     * GGA,123519,4807.038,N,01131.324,E,1,08,0.9,545.4,M,46.9,M, , *42
     *     123519       Fix taken at 12:35:19 UTC
     *     4807.038,N   Latitude 48 deg 07.038' N
     *     01131.324,E  Longitude 11 deg 31.324' E
     *     1            Fix quality: 0 = invalid
     *                               1 = GPS fix
     *                               2 = DGPS fix
     *     08           Number of satellites being tracked
     *     0.9          Horizontal dilution of position
     *     545.4,M      Altitude, Metres, above mean sea level
     *     46.9,M       Height of geoid (mean sea level) above WGS84
     *                  ellipsoid
     *     (empty field) time in seconds since last DGPS update
     *     (empty field) DGPS station ID number
     * </pre>
     */
    protected String toGGA(GpsData gpsData) {

        StringBuilder sb = new StringBuilder();

        final DecimalFormat df3_1 = new DecimalFormat("##0.0");
//  	final DecimalFormat df2_1 = new DecimalFormat("00.0");
        final DecimalFormat df1_1 = new DecimalFormat("0.0");

        if (gpsData.getTimeOfFix() != null) {
            sb.append(toNmeaTimeString(gpsData.getTimeOfFix(), false));
        }
        sb.append(",");
        sb.append(gpsData.getPoint().getLatitude().toNmeaString());
        sb.append(",");
        sb.append(gpsData.getPoint().getLongitude().toNmeaString());
        sb.append(",");
        sb.append(gpsData.ready() ? 1 : 0);
        sb.append(",");
        sb.append(gpsData.getAvailableChannelsCount());
        sb.append(",");
        if (gpsData.getHDop() != GpsData.INVALID_DOP) {
            sb.append(df1_1.format(gpsData.getHDop()));
        }
        sb.append(",");
//	mean sea level
        sb.append(",");
//	sb.append("M");
        sb.append(",");
        sb.append(df3_1.format(gpsData.getPoint().getAltitude()));
        sb.append(",");
        sb.append("M");
        sb.append(",");
//	dgps
        sb.append(",");
//	dgps

        return toSentence("GGA", sb.toString());
    }

    /** */
    protected String toNmeaTimeString(Date date, boolean withMillis) {
//Debug.println(date);
        String format = withMillis ? "HHmmss.SS" : "HHmmss";
        DateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(date);
    }

    /** */
    protected String toNmeaDateString(Date date) {
        String format = "ddMMyy";
        DateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(date);
    }

    /**
     * GLL - Geographic Position, Latitude and Longitude
     * <pre>
     * GLL,ddmm.mmmm,N,dddmm.mmmm,E,HHmmss,x,a
     *     4916.46,N    Latitude 49 deg. 16.45 min. North
     *     12311.12,W   Longitude 123 deg. 11.12 min. West
     *     225444       Fix taken at 22:54:44 UTC
     *     x            A, V Data valid
     *     a            N, A, D
     *       (Garmin 65 does not include time and status)
     * </pre>
     */
    protected String toGLL(GpsData gpsData) {

        StringBuilder sb = new StringBuilder();

        sb.append(gpsData.getPoint().getLatitude().toNmeaString());
        sb.append(",");
        sb.append(gpsData.getPoint().getLongitude().toNmeaString());
        sb.append(",");
        if (gpsData.getTimeOfFix() != null) {
            sb.append(toNmeaTimeString(gpsData.getTimeOfFix(), false));
        }
        sb.append(",");
        sb.append(gpsData.ready() ? 'A' : 'V');
        sb.append(",");
        sb.append(gpsData.ready() ? 'A' : 'N');

        return toSentence("GLL", sb.toString());
    }

    /**
     * GSA - GNSS DOP and Active Satellites
     * <pre>
     * GSA,A,3,04,05,,09,12,,,24,,,,,2.5,1.3,2.1*39
     *     A            Auto selection of 2D or 3D fix (M = manual)
     *     3            3D fix
     *     04,05...     PRNs of satellites used for fix (space for 12)
     *     2.5          PDOP (dilution of precision)
     *     1.3          Horizontal dilution of precision (HDOP)
     *     2.1          Vertical dilution of precision (VDOP)
     *       DOP is an indication of the effect of satellite geometry on
     *       the accuracy of the fix.
     * </pre>
     */
    protected String toGSA(GpsData gpsData) {

        StringBuilder sb = new StringBuilder();

        sb.append("A");	// TODO or "M"
        sb.append(",");
        sb.append(gpsData.getMeasurementMode());
        sb.append(",");

        final DecimalFormat df2 = new DecimalFormat("00");
//      final DecimalFormat df1_1 = new DecimalFormat("0.0");

        int count = 0;
        Iterator<Channel> channels = gpsData.getChannels().iterator();
        for (int i = 0; i < 12; i++) {
            if (channels.hasNext()) {
                Channel channel = channels.next();

                if (channel.getInfo() == Channel.INFO_OK) {
                    int prn = channel.getPrn();
                    sb.append(df2.format(prn));
                    sb.append(",");

                    count++;
                }
            }
        }

        for (int i = count; i < 12; i++) {
            sb.append(",");
        }

        if (gpsData.getPDop() != GpsData.INVALID_DOP) {
            sb.append(gpsData.getPDop());
        }
        sb.append(",");
        if (gpsData.getHDop() != GpsData.INVALID_DOP) {
            sb.append(gpsData.getHDop());
        }
        sb.append(",");
        if (gpsData.getVDop() != GpsData.INVALID_DOP) {
            sb.append(gpsData.getVDop());
        }

        return toSentence("GSA", sb.toString());
    }

    /**
     * GSV - Satellites in View
     * <pre>
     * GSV,2,1,08,01,40,083,46,02,17,308,41,12,07,344,39,14,22,228,45*75
     *     2            Number of sentences for full data
     *     1            sentence 1 of 2
     *     08           Number of satellites in view
     *     01           Satellite PRN number
     *     40           Elevation, degrees
     *     083          Azimuth, degrees
     *     46           Signal strength - higher is better
     *     <repeat for up to 4 satellites per sentence>
     *          There my be up to three GSV sentences in a data packet
     * </pre>
     * @param gpsData
     */
    protected String toGSV(GpsData gpsData) {

        StringBuilder sb = new StringBuilder();

        Iterator<Channel> channels = gpsData.getChannels().iterator();

        int max = (gpsData.getAvailableChannelsCount() - 1) / 4 + 1;
        for (int i = 0; i < max; i++) {
            sb.append(toGSV(gpsData, i, channels));
        }

        return sb.toString();
    }

    /**
     * @param index	0 -
     */
    private String toGSV(GpsData gpsData, int index, Iterator<Channel> channels) {

        StringBuilder sb = new StringBuilder();

        sb.append((gpsData.getAvailableChannelsCount() - 1) / 4 + 1);
        sb.append(",");
        sb.append(index + 1);
        sb.append(",");
        sb.append(gpsData.getAvailableChannelsCount());
        sb.append(",");

        final DecimalFormat df2 = new DecimalFormat("00");
        final DecimalFormat df3 = new DecimalFormat("000");

        for (int i = 0; i < 4; i++) {
            if (channels.hasNext()) {
                Channel channel = null;
                do {
                    if (channels.hasNext()) {
                        channel = channels.next();
                    } else {
                        channel = null;
                        break;
                    }
                } while (!channel.available());

                if (channel != null) {
                    sb.append(df2.format(channel.getPrn()));
                    sb.append(",");
                    sb.append(df2.format(channel.getElevation()));
                    sb.append(",");
                    sb.append(df3.format(channel.getAzimuth()));
                    sb.append(",");
                    sb.append(df2.format(channel.getSignalStrength()));
                    sb.append(",");
                } else {
                    sb.append(",");
                    sb.append(",");
                    sb.append(",");
                    sb.append(",");
                }
            } else {
                sb.append(",");
                sb.append(",");
                sb.append(",");
                sb.append(",");
            }
        }

        sb.setLength(sb.length() - 1);	// remove ',' at end

        return toSentence("GSV", sb.toString());
    }

    /**
     * RMC - Recommended Minimum Specific GNSS Data
     * <pre>
     * RMC,225446,A,4916.45,N,12311.12,W,000.5,054.7,191194,020.3,E*68
     *     225446       Time of fix 22:54:46 UTC
     *     A            Navigation receiver warning A = OK, V = warning
     *     4916.45,N    Latitude 49 deg. 16.45 min North
     *     12311.12,W   Longitude 123 deg. 11.12 min West
     *     000.5        Speed over ground, Knots
     *     054.7        Course Made Good, True
     *     191194       Date of fix  19 November 1994
     *     020.3,E      Magnetic variation 20.3 deg East
     *     *68          mandatory checksum
     * </pre>
     */
    protected String toRMC(GpsData gpsData) {

        StringBuilder sb = new StringBuilder();

        final DecimalFormat df3_1 = new DecimalFormat("##0.0");
//      final DecimalFormat df2_1 = new DecimalFormat("#0.0");

        if (gpsData.getTimeOfFix() != null) {
            sb.append(toNmeaTimeString(gpsData.getTimeOfFix(), false));
        }
        sb.append(",");
        sb.append(gpsData.ready() ? 'A' : 'V');
        sb.append(",");
        sb.append(gpsData.getPoint().getLatitude().toNmeaString());
        sb.append(",");
        sb.append(gpsData.getPoint().getLongitude().toNmeaString());
        sb.append(",");
        sb.append(df3_1.format(gpsData.getVector().getVelocity() * 0.5144));
        sb.append(",");
        sb.append(df3_1.format(gpsData.getVector().getBearingDirection()));
        sb.append(",");
        if (gpsData.getTimeOfFix() != null) {
            sb.append(toNmeaDateString(gpsData.getTimeOfFix()));
        }
        sb.append(",");
//	magnetic variation
        sb.append(",");
//	E

        return toSentence("RMC", sb.toString());
    }

    /**
     * VTG - Course Over Ground and Ground Speed
     * <pre>
     * VTG,054.7,T,034.4,M,005.5,N,010.2,K
     *     054.7,T      True track made good
     *     034.4,M      Magnetic track made good
     *     005.5,N      Ground speed, knots
     *     010.2,K      Ground speed, Kilometers per hour
     * </pre>
     */
    protected String toVTG(GpsData gpsData) {
        StringBuilder sb = new StringBuilder();

        final DecimalFormat df3_1 = new DecimalFormat("000.0");

        float direction = gpsData.getVector().getBearingDirection();
        float velocity = gpsData.getVector().getVelocity();

        sb.append(df3_1.format(direction));
        sb.append(",");
        sb.append("T");
        sb.append(",");
//	magnetic north
        sb.append(",");
//	sb.append("M");
        sb.append(",");
        sb.append(df3_1.format(velocity * 0.5144));
        sb.append(",");
        sb.append("N");
        sb.append(",");
        sb.append(df3_1.format(velocity));
        sb.append(",");
        sb.append("K");

        return toSentence("VTG", sb.toString());
    }

    /**
     * ZDA - Time & Date
     * <pre>
     *  ZDA,052608.22,06,05,2001,14,26*3D
     *     052608.22	測位時刻（UTC）　05:26:08.22 
     *     06	日（UTC）　6日 
     *     05	月（UTC）　5月 
     *     2001	西暦（UTC）　2001年 
     *     14	時（現地時間）　14時 
     *     26	分（現地時間）　26分 
     *     *3D	チェックサム 
     * </pre>
     */
    protected String toZDA(GpsData gpsData) {

        StringBuilder sb = new StringBuilder();

        sb.append(toNmeaTimeString(gpsData.getTimeOfFix(), true));
        sb.append(",");

        DateFormat sdf = new SimpleDateFormat("dd,MM,yyyy,HH,mm");
        sb.append(sdf.format(gpsData.getDateTime()));

        return toSentence("ZDA", sb.toString());
    }

    /**
     * APB - Autopilot format B
     * <pre>
     * APB,A,A,0.10,R,N,V,V,011,M,DEST,011,M,011,M
     *     A            Loran-C blink/SNR warning
     *     A            Loran-C cycle warning
     *     0.10         cross-track error distance
     *     R            steer Right to correct (or L for Left)
     *     N            cross-track error units - nautical miles
     *     V            arrival alarm - circle
     *     V            arrival alarm - perpendicular
     *     011,M        magnetic bearing, origin to destination
     *     DEST         destination waypoint ID
     *     011,M        magnetic bearing, present position to destination
     *     011,M        magnetic heading to steer
     *                  (bearings could be given in True as 033,T)
     *     (note: some pilots, Roberston in particular, misinterpret "bearing
     *     from origin to destination" as "bearing from present position to
     *     destination".  This apparently results in poor performance if the
     *     boat is sufficiently off-course that the two bearings are
     *     different.)
     * </pre>
     * TODO implement
     */
    protected String toAPB(GpsData gpsData) {
        StringBuilder sb = new StringBuilder();
        return toSentence("APB", sb.toString());
    }

    /**
     * BOD - Bearing - origin to destination waypoint
     * <pre>
     * BOD,045.,T,023.,M,DEST,START
     *     045.,T       bearing 045 True from "START" to "DEST"
     *     023.,M       bearing 023 Magnetic from "START" to "DEST"
     *     DEST         destination waypoint ID
     *     START        origin waypoint ID
     * </pre>
     * TODO implement
     */
    protected String toBOD(GpsData gpsData) {
        StringBuilder sb = new StringBuilder();
        return toSentence("BOD", sb.toString());
    }

    /**
     * BWC - Bearing and distance to waypoint - great circle
     * <pre>
     * BWC,225444,4917.24,N,12309.57,W,051.9,T,031.6,M,001.3,N,004*29
     *     225444       UTC time of fix 22:54:44
     *     4917.24,N    Latitude of waypoint
     *     12309.57,W   Longitude of waypoint
     *     051.9,T      Bearing to waypoint, degrees true
     *     031.6,M      Bearing to waypoint, degrees magnetic
     *     001.3,N      Distance to waypoint, Nautical miles
     *     004          Waypoint ID
     * </pre>
     * TODO implement
     */
    protected String toBWC(GpsData gpsData) {
        StringBuilder sb = new StringBuilder();
        return toSentence("BWC", sb.toString());
    }

    /**
     * same as BWC 
     *
     * TODO implement
     */
    protected String toBWR(GpsData gpsData) {
        StringBuilder sb = new StringBuilder();
        return toSentence("BWR", sb.toString());
    }

    /**
     * DBT - Depth below transducer
     * <pre>
     * DBT,0017.6,f,0005.4,M
     *     0017.6,f     17.6 feet
     *     0005.4,M     5.4 Meters
     * </pre>
     * TODO implement
     */
    protected String toDBT(GpsData gpsData) {
        StringBuilder sb = new StringBuilder();
        return toSentence("DBT", sb.toString());
    }

    /**
     * HDM - Heading, Magnetic
     * <pre>
     * HDM,235.,M
     *     HDM          Heading, Magnetic
     *     235.,M       Heading 235 deg. Magnetic
     *      (HDG, which includes deviation and variation, is recommended
     *      instead)
     * </pre>
     * TODO implement
     */
    protected String toHDM(GpsData gpsData) {
        StringBuilder sb = new StringBuilder();
        return toSentence("HDM", sb.toString());
    }

    /**
     * HSC - Command heading to steer
     * <pre>
     * HSC,258.,T,236.,M
     *     258.,T       258 deg. True
     *     236.,M       136 deg. Magnetic
     * </pre>
     * TODO implement
     */
    protected String toHSC(GpsData gpsData) {
        StringBuilder sb = new StringBuilder();
        return toSentence("HSC", sb.toString());
    }

    /**
     * MTW - Water temperature, Celcius
     * <pre>
     * MTW,11.,C
     *     11.,C        11 deg. C
     * </pre>
     * TODO implement
     */
    protected String toMTW(GpsData gpsData) {
        StringBuilder sb = new StringBuilder();
        return toSentence("MTW", sb.toString());
    }

    /**
     * R00 - List of waypoint IDs in currently active route
     * <pre>
     * R00,MINST,CHATN,CHAT1,CHATW,CHATM,CHATE,003,004,005,006,007,,,*05
     *     (This sentence is produced by a Garmin 65, but is not listed
     *     in Version 2.0 of the standard.  The standard lists RTE for
     *     this purpose.)
     * </pre>
     * TODO implement
     */
    protected String toR00(GpsData gpsData) {
        StringBuilder sb = new StringBuilder();
        return toSentence("R00", sb.toString());
    }

    /**
     * RMB - Recommended minimum navigation information (sent by nav.
     *       receiver when a destination waypoint is active)
     * <pre>
     * RMB,A,0.66,L,003,004,4917.24,N,12309.57,W,001.3,052.5,000.5,V*0B
     *     A            Data status A = OK, V = warning
     *     0.66,L       Cross-track error (nautical miles, 9.9 max.),
     *                          steer Left to correct (or R = right)
     *     003          Origin waypoint ID
     *     004          Destination waypoint ID
     *     4917.24,N    Destination waypoint latitude 49 deg. 17.24 min. N
     *     12309.57,W   Destination waypoint longitude 123 deg. 09.57 min. W
     *     001.3        Range to destination, nautical miles
     *     052.5        True bearing to destination
     *     000.5        Velocity towards destination, knots
     *     V            Arrival alarm  A = arrived, V = not arrived
     *     *0B          mandatory checksum
     * </pre>
     * TODO implement
     */
    protected String toRMB(GpsData gpsData) {
        StringBuilder sb = new StringBuilder();
        return toSentence("RMB", sb.toString());
    }

    /**
     * RTE - Waypoints in active route
     * <pre>
     * RTE,2,1,c,0,W3IWI,DRIVWY,32CEDR,32-29,32BKLD,32-I95,32-US1,BW-32,BW-198*69
     *     2            two sentences for full data
     *     1            this is sentence 1 of 2
     *     c            c = complete list of waypoints in this route
     *                  w = first listed waypoint is start of current leg
     *     0            Route identifier
     *     W3IWI...     Waypoint identifiers
     * </pre>
     * TODO implement
     */
    protected String toRTE(GpsData gpsData) {
        StringBuilder sb = new StringBuilder();
        return toSentence("RTE", sb.toString());
    }

    /**
     * VHW - Water speed and heading
     * <pre>
     * VHW,259.,T,237.,M,05.00,N,09.26,K
     *     259.,T       Heading 259 deg. True
     *     237.,M       Heading 237 deg. Magnetic
     *     05.00,N      Speed 5 knots through the water
     *     09.26,K      Speed 9.26 KPH
     * </pre>
     * TODO implement
     */
    protected String toVHW(GpsData gpsData) {
        StringBuilder sb = new StringBuilder();
        return toSentence("VHW", sb.toString());
    }

    /**
     * VWR - Relative wind direction and speed
     * <pre>
     * VWR,148.,L,02.4,N,01.2,M,04.4,K
     *     148.,L       Wind from 148 deg Left of bow
     *     02.4,N       Speed 2.4 Knots
     *     01.2,M       1.2 Metres/Sec
     *     04.4,K       Speed 4.4 Kilometers/Hr
     * </pre>
     * TODO implement
     */
    protected String toVMW(GpsData gpsData) {
        StringBuilder sb = new StringBuilder();
        return toSentence("VMW", sb.toString());
    }

    /**
     * WCV - Waypoint Closure Velocity
     *
     * TODO implement
     */
    protected String toWCV(GpsData gpsData) {
        StringBuilder sb = new StringBuilder();
        return toSentence("WCV", sb.toString());
    }

    /**
     * WDC - Distance to Waypoint
     *
     * TODO implement
     */
    protected String toWDC(GpsData gpsData) {
        StringBuilder sb = new StringBuilder();
        return toSentence("WDC", sb.toString());
    }

    /**
     * WDR - Waypoint Distance, Rhumb Line
     *
     * TODO implement
     */
    protected String toWDR(GpsData gpsData) {
        StringBuilder sb = new StringBuilder();
        return toSentence("WDR", sb.toString());
    }

    /**
     * WPL - waypoint location
     * <pre>
     * WPL,4917.16,N,12310.64,W,003*65
     *     4917.16,N    Latitude of waypoint
     *     12310.64,W   Longitude of waypoint
     *     003          Waypoint ID
     *       When a route is active, this sentence is sent once for each
     *       waypoint in the route, in sequence. When all waypoints have
     *       been reported, GPR00 is sent in the next data set. In any
     *       group of sentences, only one WPL sentence, or an R00
     *       sentence, will be sent.
     * </pre>
     * TODO implement
     */
    protected String toWPL(GpsData gpsData) {
        StringBuilder sb = new StringBuilder();
        return toSentence("WPL", sb.toString());
    }

    /**
     * XTE - Cross track error, measured
     * <pre>
     *  XTE,A,A,0.67,L,N
     *     A            General warning flag V = warning
     *                          (Loran-C Blink or SNR warning)
     *     A            Not used for GPS (Loran-C cycle lock flag)
     *     0.67         cross track error distance
     *     L            Steer left to correct error (or R for right)
     *     N            Distance units - Nautical miles
     * </pre>
     * TODO implement
     */
    protected String toXTE(GpsData gpsData) {
        StringBuilder sb = new StringBuilder();
        return toSentence("XTE", sb.toString());
    }

    /**
     * XTR - Cross-Track Error - Dead Reckoning
     * <pre>
     * XTR,0.67,L,N
     *     0.67         cross track error distance
     *     L            Steer left to correct error (or R for right)
     *     N            Distance units - Nautical miles
     * </pre>
     * TODO implement
     */
    protected String toXTR(GpsData gpsData) {
        StringBuilder sb = new StringBuilder();
        return toSentence("XTR", sb.toString());
    }

    /**
     * Calculate the checksum of this NMEA sentence
     *
     * @return the calculated checksum
     */
    protected int getChecksum(String sentence) {
        int sum = 0;

        for (int i = 0; i < sentence.length(); i++) {
            char c = sentence.charAt(i);

            if (i == 0) {
                sum = (c + 256) & 0xff;
            } else {
                sum ^= (c + 256) & 0xff;
            }
        }

        return sum;
    }

    /** */
    {
        try {
            Properties props = new Properties();

            props.load(Nmea.class.getResourceAsStream("Nmea.properties"));

            String value = props.getProperty("nmea.gga");
            if (value != null) {
                useGGA = new Boolean(value).booleanValue();
            }

            value = props.getProperty("nmea.gsa");
            if (value != null) {
                useGSA = new Boolean(value).booleanValue();
            }

            value = props.getProperty("nmea.gsv");
            if (value != null) {
                useGSV = new Boolean(value).booleanValue();
            }

            value = props.getProperty("nmea.gll");
            if (value != null) {
                useGLL = new Boolean(value).booleanValue();
            }

            value = props.getProperty("nmea.rmc");
            if (value != null) {
                useRMC = new Boolean(value).booleanValue();
            }

            value = props.getProperty("nmea.vtg");
            if (value != null) {
                useVTG = new Boolean(value).booleanValue();
            }

Debug.println("GGA: " + useGGA);
Debug.println("GSA: " + useGSA);
Debug.println("GSV: " + useGSV);
Debug.println("GLL: " + useGLL); 
Debug.println("RMC: " + useRMC);
Debug.println("VTG: " + useVTG);
        } catch (IOException e) {
Debug.printStackTrace(e);
            throw new InternalError(e.toString());
        }
    }

    //-------------------------------------------------------------------------

    /** */
    public static void main(String[] args) throws Exception {
        NmeaGpsFormat ngf = new NmeaGpsFormat();

        // tests checksum
        String[] testTypes = {
            "GGA", "GLL", "VTG", "GGA"
        };

        String[] testData = {
          "050945.00,3504.227794,N,13545.810149,E,1,06,1.4,151.00,M,34.53,M,,",
          "3504.227794,N,13545.810149,E,050945.00,A,A",
          "57.1,T,,,000.0,N,000.0,K,A",
          "123519,4807.038,N,01131.324,E,1,08,0.9,545.4,M,46.9,M,,"
        };

        for (int i = 0; i < testTypes.length; i++) {
            String sentence = ngf.toSentence(testTypes[i], testData[i]);
            System.err.print(sentence);
        }
    }
}

/* */
