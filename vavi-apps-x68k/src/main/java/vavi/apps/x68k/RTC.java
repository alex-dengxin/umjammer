/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


class RTC extends MemoryMappedDevice implements X68000Device {
    private Calendar calendar;

    private boolean bank;

    private X68000 x68000;

    public RTC() {
    }

    public boolean init(X68000 x68000) {
        this.x68000 = x68000;
        calendar = Calendar.getInstance(Locale.JAPAN);
        bank = false;
        return true;
    }

    public void reset() {
        bank = false;
    }

    public byte read_byte(int a) throws MC68000Exception {
        calendar.setTime(new Date());
        if (!bank) {
            switch (a) {
            case 15245313:
                return (byte) (calendar.get(Calendar.SECOND) % 10);
            case 15245315:
                return (byte) (calendar.get(Calendar.SECOND) / 10);
            case 15245317:
                return (byte) (calendar.get(Calendar.MINUTE) % 10);
            case 15245319:
                return (byte) (calendar.get(Calendar.MINUTE) / 10);
            case 15245321:
                return (byte) (calendar.get(Calendar.HOUR_OF_DAY) % 10);
            case 15245323:
                return (byte) (calendar.get(Calendar.HOUR_OF_DAY) / 10);
            case 15245325:
                switch (calendar.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.SUNDAY:
                    return 0;
                case Calendar.MONDAY:
                    return 1;
                case Calendar.TUESDAY:
                    return 2;
                case Calendar.WEDNESDAY:
                    return 3;
                case Calendar.THURSDAY:
                    return 4;
                case Calendar.FRIDAY:
                    return 5;
                case Calendar.SATURDAY:
                    return 6;
                }
                return 0;
            case 15245327:
                return (byte) (calendar.get(Calendar.DAY_OF_MONTH) % 10);
            case 15245329:
                return (byte) (calendar.get(Calendar.DAY_OF_MONTH) / 10);
            case 15245331:
                return (byte) ((calendar.get(Calendar.MONTH) + 1) % 10);
            case 15245333:
                return (byte) ((calendar.get(Calendar.MONTH) + 1) / 10);
            case 15245335:
                return (byte) ((calendar.get(Calendar.YEAR) - 1980) % 10);
            case 15245337:
                return (byte) ((calendar.get(Calendar.YEAR) - 1980) / 10);
            case 15245339:
                return (byte) (bank ? 0 : 1);
            case 15245341:
                return 0;
            case 15245343:
                return 0;
            }
        } else {
            switch (a) {
            case 15245313:
                return 0;
            case 15245315:
                return 0;
            case 15245317:
                return 0;
            case 15245319:
                return 0;
            case 15245321:
                return 0;
            case 15245323:
                return 0;
            case 15245325:
                return 0;
            case 15245327:
                return 0;
            case 15245329:
                return 0;
            case 15245331:
                return 0;
            case 15245333:
                return 0;
            case 15245335:
                return 0;
            case 15245337:
                return 0;
            case 15245339:
                return 0;
            case 15245341:
                return 0;
            case 15245343:
                return 0;
            }
        }
        x68000.bus_error_on_read(a);
        return 0;
    }

    public short read_short_big(int a) throws MC68000Exception {
        return read_byte(a | 1);
    }

    public void write_byte(int a, byte b) {
    }
}

