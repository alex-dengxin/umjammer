/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;


class SCC extends MemoryMappedDevice implements X68000Device, InterruptDevice, MouseListener, MouseMotionListener, MonitorInputListener {
    private int port_b_request;
    private int port_b_acknowledged;
    private int port_a_request;
    private int port_a_acknowledged;
    private int port_a_register_select;
    private int port_b_register_select;
    private boolean mouseLeftButton, mouseRightButton;
    private int mouseX, mouseY;
    private int deaccelerate_table[];
    private int port_b_data_counter;
    private byte port_a_input_buffer[];
    private int port_a_input_read_ptr;
    private int port_a_input_write_ptr;
    private boolean x68000MouseCursorMode;

    private X68000 x68000;
    private CRTC crtc;
    private Video video;

    public SCC() {
        port_b_request = 0;
        port_b_acknowledged = 0;
        port_a_request = 0;
        port_a_acknowledged = 0;
        port_b_register_select = 0;
        port_a_register_select = 0;
        mouseLeftButton = false;
        mouseRightButton = false;
        mouseX = 0;
        mouseY = 0;
        deaccelerate_table = new int[1025];
        {
            int k = 0;
            for (int i = 1; i <= 127; i++) {
                int j = i < 8 ? i : i * (i >> 3);
                j += j >> 2;
                if (j > 1025) {
                    j = 1025;
                }
                while (k < j) {
                    deaccelerate_table[k++] = i - 1;
                }
                if (k == 1025) {
                    break;
                }
            }
        }
        port_b_data_counter = 0;
        port_a_input_buffer = new byte[256];
        port_a_input_read_ptr = 0;
        port_a_input_write_ptr = 0;
        x68000MouseCursorMode = false;
    }

    public boolean init(X68000 x68000) {
        this.x68000 = x68000;
        crtc = x68000.crtc;
        video = x68000.video;
        x68000.addMouseListener(this);
        x68000.addMouseMotionListener(this);
        return true;
    }

    public void reset() {
    }

    private void port_b_interrupt() {
        port_b_request++;
        x68000.interrupt_request_scc++;
    }

    private void port_a_interrupt() {
        port_a_request++;
        x68000.interrupt_request_scc++;
    }

    public int acknowledge() {
        int request;
        request = port_b_request;
        if (request != port_b_acknowledged) {
            port_b_acknowledged = request;
            return 84;
        }
        request = port_a_request;
        if (request != port_a_acknowledged) {
            port_a_acknowledged = request;
            return 92;
        }
        return 0;
    }

    public void done(int vector) {
        if (port_a_request != port_a_acknowledged || port_b_request != port_b_acknowledged) {
            x68000.interrupt_request_scc++;
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (!x68000.isFocusOwner()) {
            x68000.requestFocusInWindow();
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        int modifiers = e.getModifiers();
        if ((modifiers & InputEvent.BUTTON1_MASK) != 0) {
            mouseLeftButton = true;
        } else if ((modifiers & InputEvent.BUTTON3_MASK) != 0) {
            mouseRightButton = true;
        }
    }

    public void mouseReleased(MouseEvent e) {
        int modifiers = e.getModifiers();
        if ((modifiers & InputEvent.BUTTON1_MASK) != 0) {
            mouseLeftButton = false;
        } else if ((modifiers & InputEvent.BUTTON3_MASK) != 0) {
            mouseRightButton = false;
        }
    }

    public void mouseDragged(MouseEvent e) {
        convertMouseAxis(e);
    }

    public void mouseMoved(MouseEvent e) {
        convertMouseAxis(e);
    }

    private void convertMouseAxis(MouseEvent e) {
        if (crtc.h_stretch_mode != 0) {
            mouseX = (e.getX() - video.offset_x << 3) / crtc.h_stretch_mode;
            mouseY = e.getY() - video.offset_y >> (crtc.duplicate_raster || crtc.slit ? 1 : 0);
        }
    }

    public byte read_byte(int a) {
        switch (a) {
        case 15302657:
            return port_b_command_read();
        case 15302659:
            return port_b_data_read();
        case 15302661:
            return port_a_command_read();
        case 15302663:
            return port_a_data_read();
        }
        return 0;
    }

    public short read_short_big(int a) {
        switch (a) {
        case 15302656:
            return (short) (port_b_command_read() & 255);
        case 15302658:
            return (short) (port_b_data_read() & 255);
        case 15302660:
            return (short) (port_a_command_read() & 255);
        case 15302662:
            return (short) (port_a_data_read() & 255);
        }
        return 0;
    }

    public int read_int_big(int a) {
        return 0;
    }

    public void write_byte(int a, byte b) {
        switch (a) {
        case 15302657:
            port_b_command_write(b);
            break;
        case 15302659:
            port_b_data_write(b);
            break;
        case 15302661:
            port_a_command_write(b);
            break;
        case 15302663:
            port_a_data_write(b);
            break;
        }
    }

    public void write_short_big(int a, short s) {
        switch (a) {
        case 15302656:
            port_b_command_write((byte) s);
            break;
        case 15302658:
            port_b_data_write((byte) s);
            break;
        case 15302660:
            port_a_command_write((byte) s);
            break;
        case 15302662:
            port_a_data_write((byte) s);
            break;
        }
    }

    public void write_int_big(int a, int i) {
    }

    private byte port_b_command_read() {
        return 0;
    }

    private void port_b_command_write(byte b) {
        if (port_b_register_select != 0) {
            if (port_b_register_select == 5 && (b & 2) != 0) {
                port_b_data_counter = 0;
                port_b_interrupt();
            }
            port_b_register_select = 0;
        } else if ((b & 240) == 0) {
            port_b_register_select = b;
        } else {
            if (b == 56) {
                if (port_b_data_counter < 3) {
                    port_b_interrupt();
                }
            }
        }
    }

    private byte port_b_data_read() {
        int dx, dy;
        dx = mouseX - ((m[2766] << 8) + (m[2767] & 255));
        dy = mouseY - ((m[2768] << 8) + (m[2769] & 255));
        if (port_b_data_counter == 0) {
            if (x68000.customCursorMode) {
                if (m[2722] == 0 && x68000MouseCursorMode) {
                    x68000MouseCursorMode = false;
                    x68000.setCursor(x68000.cursor1);
                } else if (m[2722] != 0 && !x68000MouseCursorMode) {
                    x68000MouseCursorMode = true;
                    x68000.setCursor(x68000.cursor0);
                }
            }
            port_b_data_counter++;
            return (byte) ((dy < -128 ? 128 : 0) + (dy > 127 ? 64 : 0) + (dx < -128 ? 32 : 0) + (dx > 127 ? 16 : 0) + (mouseRightButton ? 2 : 0) + (mouseLeftButton ? 1 : 0));
        } else if (port_b_data_counter == 1) {
            port_b_data_counter++;
            return (byte) (dx >= 0 ? deaccelerate_table[dx <= 1024 ? dx : 1024] : -deaccelerate_table[dx >= -1024 ? -dx : 1024]);
        } else if (port_b_data_counter == 2) {
            port_b_data_counter++;
            return (byte) (dy >= 0 ? deaccelerate_table[dy <= 1024 ? dy : 1024] : -deaccelerate_table[dy >= -1024 ? -dy : 1024]);
        }
        return 0;
    }

    private void port_b_data_write(byte b) {
    }

    private byte port_a_command_read() {
        if (port_a_register_select != 0) {
            port_a_register_select = 0;
        } else {
            return (byte) (36 + (port_a_input_read_ptr != port_a_input_write_ptr ? 1 : 0));
        }
        return 0;
    }

    private void port_a_command_write(byte b) {
        if (port_a_register_select != 0) {
            port_a_register_select = 0;
        } else if ((b & 240) == 0) {
            port_a_register_select = b;
        } else {
        }
    }

    private byte port_a_data_read() {
        if (port_a_input_read_ptr != port_a_input_write_ptr) {
            byte b = port_a_input_buffer[port_a_input_read_ptr++];
            if (port_a_input_read_ptr == 256) {
                port_a_input_read_ptr = 0;
            }
            return b;
        }
        return 0;
    }

    public void port_a_data_write(byte b) {
        x68000.monitor.outputChar(b & 255);
    }

    private void port_a_input(byte b) {
        int temp = port_a_input_write_ptr + 1;
        if (temp == 256) {
            temp = 0;
        }
        if (temp == port_a_input_read_ptr) {
            return;
        }
        port_a_input_buffer[port_a_input_write_ptr] = b;
        port_a_input_write_ptr = temp;
        port_a_interrupt();
    }

    public void monitorInput(int keyChar) {
        if (keyChar >= 0 && keyChar <= 127) {
            port_a_input((byte) keyChar);
        }
    }
}

