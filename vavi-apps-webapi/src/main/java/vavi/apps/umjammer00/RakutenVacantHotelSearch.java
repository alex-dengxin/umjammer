/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.umjammer00;

import java.util.Date;

import vavi.net.rest.Formatted;
import vavi.net.rest.Parameter;
import vavi.net.rest.Rest;
import vavi.net.rest.SimpleDateFormatFormatter;


/**
 * RakutenVacantHotelSearch. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070310 nsano initial version <br>
 */
@Rest(protocol = "HTTP",
      method="GET",
      url="http://api.rakuten.co.jp/rws/1.4/rest")
public class RakutenVacantHotelSearch {

    @Parameter(name = "developerId", required = true)
    private String token; 

    @Parameter(required = true)
    private String operation = "VacantHotelSearch"; 

    @Parameter(required = true)
    private String version = "2007-02-28"; 

    @Parameter(required = true)
    @Formatted(formatter = SimpleDateFormatFormatter.class, value = "yyyy-MM-dd")
    private Date checkinDate; 

    @Parameter(required = true)
    @Formatted(formatter = SimpleDateFormatFormatter.class, value = "yyyy-MM-dd")
    private Date checkoutDate; 

    @Parameter(required = true)
    private int maxCharge; 

    @Parameter(required = true)
    private int hotelNo;

    /** */
    public String getToken() {
        return token;
    }

    /** */
    public void setToken(String token) {
        this.token = token;
    }

    /** */
    public String getVersion() {
        return version;
    }

    /** */
    public void setVersion(String version) {
        this.version = version;
    }

    /** */
    public Date getCheckinDate() {
        return checkinDate;
    }

    /** */
    public void setCheckinDate(Date checkinDate) {
        this.checkinDate = checkinDate;
    }

    /** */
    public Date getCheckoutDate() {
        return checkoutDate;
    }

    /** */
    public void setCheckoutDate(Date checkoutDate) {
        this.checkoutDate = checkoutDate;
    }

    /** */
    public int getMaxCharge() {
        return maxCharge;
    }

    /** */
    public void setMaxCharge(int maxCharge) {
        this.maxCharge = maxCharge;
    }

    /** */
    public int getHotelNo() {
        return hotelNo;
    }

    /** */
    public void setHotelNo(int hotelNo) {
        this.hotelNo = hotelNo;
    }

    /** */
    public String getOperation() {
        return operation;
    } 
}

/* */
