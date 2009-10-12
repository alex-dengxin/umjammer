/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.IOException;
import vavi.io.SerialPortDevice;
import vavi.util.Debug;
import vavi.util.StringUtil;


/**
 * MovaDevice.
 * 
 * @author <a href=mailto:vavivavi@yahoo.co.jp>Naohide Sano</a> (nsano)
 * @version 0.00 040313 nsano initial version <br>
 */
public class MovaDevice extends Device {

    /**
     * @param name COM port device name, e.g. "COM1"
     */
    public MovaDevice(String name) throws IOException {
        super(new SerialPortDevice(name));
    }

    /** */
    public String getPhoneNumber() throws IOException {
        os.write(COMMAND_GETNUMBER);
        os.flush();

        StringBuilder sb = new StringBuilder();
        int retry = 0;

        while (true) {
            int c = is.read();

            if (c == -1) {
                if (retry++ > 10) {
                    throw new IllegalStateException("too many retry");
                }
            } else {
                if (c == 0xaa) {
                    if (sb.length() != 11) {
Debug.println("bad: " + sb);
                        retry++;
                        sb.setLength(0);

                        continue;
                    }

                    break;
                }

                if ((c > 0x90) && (c < 0x9d)) {
                    int d = c - 0x90;

                    if (d < 10) {
                        sb.append(d);
                    } else if (d == 10) {
                        sb.append('0');
                    } else if (d == 11) {
                        sb.append('*');
                    } else if (d == 12) {
                        sb.append('#');
                    }
                }
            }
        }

        return sb.toString();
    }

    /** */
    private static int toTerminalNumber(int i) {
        if (i == 0) {
            return 0x9a;
        } else if ((i > 0) && (i < 10)) {
            return i + 0x90;
        } else {
            throw new IllegalArgumentException(String.valueOf(i));
        }
    }

    /** */
    public String getPassword() throws IOException {
        for (int i = 0; i < 10000; i++) {
            String s = "000" + i;
            s = s.substring(s.length() - 4);
Debug.println(s);
            os.write(0xb3);
            os.write(0x01);

            os.write(toTerminalNumber(s.charAt(0) - 0x30));
            os.write(toTerminalNumber(s.charAt(1) - 0x30));
            os.write(toTerminalNumber(s.charAt(2) - 0x30));
            os.write(toTerminalNumber(s.charAt(3) - 0x30));

            os.write(0x86);
            os.flush();

            int c1;

            do {
                c1 = is.read();
            } while (c1 == -1);

            if (c1 == 0x87) {
                continue;
            }

            int c2;

            do {
                c2 = is.read();
            } while (c2 == -1);

            if ((c1 == 0x86) && (c2 == 0x01)) {
                return s;
            } else {
Debug.println(StringUtil.toHex2(c1));
Debug.println(StringUtil.toHex2(c2));

                break;
            }
        }

        throw new IllegalStateException("password could not find");
    }

    /* */
    public String getDeviceId() {
        try {
            return getPhoneNumber();
        } catch (IOException e) {
Debug.printStackTrace(e);

            return "MovaDevice";
        }
    }

    //----

    /** */
    public static void main(String[] arg) throws Exception {
        MovaDevice md = new MovaDevice("COM1");
        System.out.println(md.getPhoneNumber());

//        System.out.println(md.getPassword());
    }
}

/* */
