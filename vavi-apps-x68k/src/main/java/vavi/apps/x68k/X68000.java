/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.MemoryImageSource;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.zip.GZIPInputStream;


public class X68000 extends Applet implements ItemListener, KeyListener, Runnable {
    public NullDevice null_device = null;
    public MainMemory main_memory = null;
    public GraphicScreen graphic_screen = null;
    public TextScreen text_screen = null;
    public CRTC crtc = null;
    public Video video = null;
    public DMAC dmac = null;
    public AreaSet area_set = null;
    public MFP mfp = null;
    public RTC rtc = null;
    public PrinterPort printer_port = null;
    public SystemPort system_port = null;
    public Sound sound = null;
    public FDC fdc = null;
    public HDC hdc = null;
    public SCC scc = null;
    public PPI ppi = null;
    public INT1 int1 = null;
    public SpriteScreen sprite_screen = null;
    public SRAM sram = null;
    public CGROM cgrom = null;
    public IPLROM iplrom = null;

    final private String device_name[] = {
        "ヌルデバイス", "メインメモリ", "グラフィック画面", "テキスト画面", "CRTC", "ビデオコントローラ", "DMAC", "エリアセットポート", "MFP", "RTC", "プリンタポート", "システムポート", "FM音源とADPCM", "FDC", "HDC", "SCC", "PPI", "INT1", "スプライト画面", "SRAM", "CGROM", "IPLROM"
    };

    public String documentBase;
    public Graphics graphics;
    private Panel controlPanel, leftPanel, centerPanel, rightPanel;
    private Choice choice;
    private CardLayout cardLayout;
    public Monitor monitor;
    public Keyboard keyboard;
    public Config1 config1;

    private X68000Device device_list[];

    private MemoryMappedDevice read_user_mmap[];
    private MemoryMappedDevice read_supervisor_mmap[];
    private MemoryMappedDevice write_user_mmap[];
    private MemoryMappedDevice write_supervisor_mmap[];

    private int fc2;

    private MemoryMappedDevice current_read_mmap[];
    private MemoryMappedDevice current_write_mmap[];

    public int interrupt_request_int1;
    private int interrupt_acknowledged_int1;
    private int interrupt_vector_int1;
    public int interrupt_request_dmac;
    private int interrupt_acknowledged_dmac;
    private int interrupt_vector_dmac;
    public int interrupt_request_scc;
    private int interrupt_acknowledged_scc;
    private int interrupt_vector_scc;
    public int interrupt_request_mfp;
    private int interrupt_acknowledged_mfp;
    private int interrupt_vector_mfp;
    public int interrupt_request_system_port;
    private int interrupt_acknowledged_system_port;
    private int interrupt_vector_system_port;

    private int ipl_8;

    public void init() {
        setBackground(Color.darkGray);
        setLayout(new BorderLayout(0, 0));
        setFont(new Font("Serif", Font.PLAIN, 14));
        graphics = getGraphics();
        documentBase = getDocumentBase().toString();
        documentBase = documentBase.substring(0, documentBase.lastIndexOf("/") + 1);
        controlPanel = new FixedSizePanel(768, 104, null);
        controlPanel.setBackground(Color.darkGray);
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        leftPanel = new FixedSizePanel(100, 104, null);
        leftPanel.setBackground(Color.darkGray);
        leftPanel.setLayout(new BorderLayout());
        choice = new Choice();
        leftPanel.add(choice, BorderLayout.CENTER);
        controlPanel.add(leftPanel);
        centerPanel = new FixedSizePanel(568, 104, null);
        cardLayout = new CardLayout();
        centerPanel.setLayout(cardLayout);
        choice.addItem("Monitor");
        monitor = new Monitor();
        centerPanel.add("Monitor", monitor);
        choice.addItem("Keyboard");
        Image keyboardImage = null;
        try {
            keyboardImage = getImage(new URL(documentBase + "keyboard.png"));
        } catch (Exception e) {
        }
        keyboard = new Keyboard(keyboardImage);
        centerPanel.add("Keyboard", keyboard);
        choice.addItem("Config1");
        config1 = new Config1();
        centerPanel.add("Config1", config1);
        controlPanel.add(centerPanel);
        choice.addItemListener(this);
        rightPanel = new FixedSizePanel(100, 104, null);
        rightPanel.setBackground(Color.darkGray);
        rightPanel.setLayout(new BorderLayout());
        Label versionLabel = new Label("54VII", Label.CENTER);
        versionLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        versionLabel.setForeground(Color.white);
        rightPanel.add(versionLabel, BorderLayout.SOUTH);
        controlPanel.add(rightPanel);
        add("South", controlPanel);
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
        setFocusTraversalKeys(KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS, Collections.EMPTY_SET);
        read_user_mmap = new MemoryMappedDevice[2049];
        read_supervisor_mmap = new MemoryMappedDevice[2049];
        write_user_mmap = new MemoryMappedDevice[2049];
        write_supervisor_mmap = new MemoryMappedDevice[2049];
        fc2 = 1;
        current_read_mmap = read_supervisor_mmap;
        current_write_mmap = write_supervisor_mmap;
        interrupt_request_int1 = 0;
        interrupt_acknowledged_int1 = 0;
        interrupt_vector_int1 = -1;
        interrupt_request_dmac = 0;
        interrupt_acknowledged_dmac = 0;
        interrupt_vector_dmac = -1;
        interrupt_request_scc = 0;
        interrupt_acknowledged_scc = 0;
        interrupt_vector_scc = -1;
        interrupt_request_mfp = 0;
        interrupt_acknowledged_mfp = 0;
        interrupt_vector_mfp = -1;
        interrupt_request_system_port = 0;
        interrupt_acknowledged_system_port = 0;
        interrupt_vector_system_port = -1;
        ipl_8 = 1792;
        device_list = new X68000Device[22];
        null_device = new NullDevice();
        device_list[0] = null_device;
        map(null_device, 0, 16785408, false, false);
        main_memory = new MainMemory();
        device_list[1] = main_memory;
        map(main_memory, 0, 12582912, false, false);
        graphic_screen = new GraphicScreen();
        device_list[2] = graphic_screen;
        map(graphic_screen, 12582912, 2097152, true, false);
        text_screen = new TextScreen();
        device_list[3] = text_screen;
        map(text_screen, 14680064, 524288, true, false);
        crtc = new CRTC();
        device_list[4] = crtc;
        map(crtc, 15204352, 8192, true, false);
        video = new Video();
        device_list[5] = video;
        map(video, 15212544, 8192, true, false);
        dmac = new DMAC();
        device_list[6] = dmac;
        map(dmac, 15220736, 8192, true, false);
        area_set = new AreaSet();
        device_list[7] = area_set;
        map(area_set, 15228928, 8192, true, false);
        mfp = new MFP();
        device_list[8] = mfp;
        map(mfp, 15237120, 8192, true, false);
        rtc = new RTC();
        device_list[9] = rtc;
        map(rtc, 15245312, 8192, true, false);
        printer_port = new PrinterPort();
        device_list[10] = printer_port;
        map(printer_port, 15253504, 8192, true, false);
        system_port = new SystemPort();
        device_list[11] = system_port;
        map(system_port, 15261696, 8192, true, false);
        sound = new Sound();
        device_list[12] = sound;
        map(sound, 15269888, 16384, true, false);
        fdc = new FDC();
        device_list[13] = fdc;
        map(fdc, 15286272, 8192, true, false);
        hdc = new HDC();
        device_list[14] = hdc;
        map(hdc, 15294464, 8192, true, false);
        scc = new SCC();
        device_list[15] = scc;
        map(scc, 15302656, 8192, true, false);
        ppi = new PPI();
        device_list[16] = ppi;
        map(ppi, 15310848, 8192, true, false);
        int1 = new INT1();
        device_list[17] = int1;
        map(int1, 15319040, 8192, true, false);
        sprite_screen = new SpriteScreen();
        device_list[18] = sprite_screen;
        map(sprite_screen, 15400960, 65536, true, false);
        sram = new SRAM();
        device_list[19] = sram;
        map(sram, 15532032, 16384, true, true);
        cgrom = new CGROM();
        device_list[20] = cgrom;
        map(cgrom, 15728640, 786432, true, true);
        iplrom = new IPLROM();
        device_list[21] = iplrom;
        map(iplrom, 16515072, 262144, true, true);
        config1.init(this);
        initCustomCursor();
    }

    private void bus_reset() {
        for (int i = 0; i < 22; i++) {
            device_list[i].reset();
        }
    }

    public void map(MemoryMappedDevice device, int address, int length, boolean supervisor_only, boolean read_only) {
        int page = (address & 16777215) >>> 13;
        int count = length >>> 13;
        for (int i = page; i < page + count; i++) {
            read_supervisor_mmap[i] = device;
            read_user_mmap[i] = supervisor_only ? null_device : device;
            write_supervisor_mmap[i] = read_only ? null_device : device;
            write_user_mmap[i] = supervisor_only || read_only ? null_device : device;
        }
    }

    public void userProtect(int address, int length, boolean supervisor_only) {
        int page = (address & 16777215) >>> 13;
        int count = length >>> 13;
        for (int i = page; i < page + count; i++) {
            if (supervisor_only) {
                read_user_mmap[i] = null_device;
                write_user_mmap[i] = null_device;
            } else {
                read_user_mmap[i] = read_supervisor_mmap[i];
                write_user_mmap[i] = write_supervisor_mmap[i];
            }
        }
    }

    public void writeProtect(int address, int length, boolean read_only) {
        int page = (address & 16777215) >>> 13;
        int count = length >>> 13;
        for (int i = page; i < page + count; i++) {
            if (read_only) {
                write_supervisor_mmap[i] = null_device;
                write_user_mmap[i] = null_device;
            } else {
                write_supervisor_mmap[i] = read_supervisor_mmap[i];
                write_user_mmap[i] = read_user_mmap[i];
            }
        }
    }

    public byte read_byte(int a) throws MC68000Exception {
        int ma = a & 16777215;
        return current_read_mmap[ma >>> 13].read_byte(ma);
    }

    public short read_short_big(int a) throws MC68000Exception {
        int ma = a & 16777215;
        return current_read_mmap[ma >>> 13].read_short_big(ma);
    }

    public int read_int_big(int a) throws MC68000Exception {
        int ma = a & 16777215, page = ma >>> 13;
        MemoryMappedDevice device = current_read_mmap[page];
        if ((ma & 8191) >= 8189 && current_read_mmap[page + 1] != device) {
            short temp = device.read_short_big(8190);
            return (temp << 16) + (current_read_mmap[page + 1].read_short_big(0) & 65535);
        }
        return device.read_int_big(ma);
    }

    public void write_byte(int a, byte b) throws MC68000Exception {
        int ma = a & 16777215;
        current_write_mmap[ma >>> 13].write_byte(ma, b);
    }

    public void write_short_big(int a, short s) throws MC68000Exception {
        int ma = a & 16777215;
        current_write_mmap[ma >>> 13].write_short_big(ma, s);
    }

    public void write_int_big(int a, int i) throws MC68000Exception {
        int ma = a & 16777215, page = ma >>> 13;
        MemoryMappedDevice device = current_write_mmap[page];
        if ((ma & 8191) >= 8189 && current_write_mmap[page + 1] != device) {
            device.write_short_big(8190, (short) (i >> 16));
            current_write_mmap[page + 1].write_short_big(0, (short) i);
            return;
        }
        device.write_int_big(ma, i);
    }

    public byte read_byte(int a, int fc2) throws MC68000Exception {
        int ma = a & 16777215;
        return (fc2 == 0 ? read_user_mmap : read_supervisor_mmap)[ma >>> 13].read_byte(ma);
    }

    public short read_short_big(int a, int fc2) throws MC68000Exception {
        int ma = a & 16777215;
        return (fc2 == 0 ? read_user_mmap : read_supervisor_mmap)[ma >>> 13].read_short_big(ma);
    }

    public int read_int_big(int a, int fc2) throws MC68000Exception {
        int ma = a & 16777215, page = ma >>> 13;
        MemoryMappedDevice mmap[] = fc2 == 0 ? read_user_mmap : read_supervisor_mmap;
        MemoryMappedDevice device = mmap[page];
        if ((ma & 8191) >= 8189 && mmap[page + 1] != device) {
            short temp = device.read_short_big(8190);
            return (temp << 16) + (mmap[page + 1].read_short_big(0) & 65535);
        }
        return device.read_int_big(ma);
    }

    public void write_byte(int a, byte b, int fc2) throws MC68000Exception {
        int ma = a & 16777215;
        (fc2 == 0 ? write_user_mmap : write_supervisor_mmap)[ma >>> 13].write_byte(ma, b);
    }

    public void write_short_big(int a, short s, int fc2) throws MC68000Exception {
        int ma = a & 16777215;
        (fc2 == 0 ? write_user_mmap : write_supervisor_mmap)[ma >>> 13].write_short_big(ma, s);
    }

    public void write_int_big(int a, int i, int fc2) throws MC68000Exception {
        int ma = a & 16777215, page = ma >>> 13;
        MemoryMappedDevice mmap[] = fc2 == 0 ? write_user_mmap : write_supervisor_mmap;
        MemoryMappedDevice device = mmap[page];
        if ((ma & 8191) >= 8189 && mmap[page + 1] != device) {
            device.write_short_big(8190, (short) (i >> 16));
            mmap[page + 1].write_short_big(0, (short) i);
            return;
        }
        device.write_int_big(ma, i);
    }

    private int bus_error_address;

    public void bus_error_on_write(int a) throws MC68000Exception {
        bus_error_address = a;
        throw MC68000Exception.BUS_ERROR_ON_WRITE;
    }

    public void bus_error_on_read(int a) throws MC68000Exception {
        bus_error_address = a;
        throw MC68000Exception.BUS_ERROR_ON_READ;
    }

    private void initMC68000() {
        crtc_interrupt_clock = 9223372036854775807L;
        mfp_interrupt_clock = 9223372036854775807L;
        sound_interrupt_clock = 9223372036854775807L;
        r = new int[16];
    }

    public boolean customCursorMode = false;

    public Cursor cursor0, cursor1;

    private void initCustomCursor() {
        monitor.outputString("カスタムカーソルを設定します...");
        setCustomCursor: {
            Toolkit toolkit;
            try {
                toolkit = Toolkit.getDefaultToolkit();
            } catch (Exception e) {
                break setCustomCursor;
            }
            Dimension bestCursorSize;
            try {
                bestCursorSize = toolkit.getBestCursorSize(32, 32);
            } catch (Exception e) {
                break setCustomCursor;
            }
            if (bestCursorSize.width != 32 || bestCursorSize.height != 32) {
                break setCustomCursor;
            }
            int bitmap0[] = {
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
            };
            int bitmap1[] = {
                -16777216, -16777216, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -16777216, -1, -16777216, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -16777216, -1, -1, -16777216, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -16777216, -1, -1, -1, -16777216, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -16777216, -1, -1, -1, -1, -16777216, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -16777216, -1, -1, -1, -1, -1, -16777216, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -16777216, -1, -1, -1, -1, -1, -1, -16777216, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -16777216, -1, -1, -1, -1, -1, -1, -1, -16777216, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -16777216, -1, -1, -1, -1, -1, -1, -1, -1, -16777216, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -16777216, -1, -1, -1, -1, -1, -16777216, -16777216, -16777216, -16777216, -16777216, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -16777216, -1, -1, -16777216, -1, -1, -16777216, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -16777216, -1, -16777216, 0, -16777216, -1, -1, -16777216, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -16777216, -16777216, 0, 0,
                -16777216, -1, -1, -16777216, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -16777216, -1, -1, -16777216, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -16777216, -1, -1, -16777216, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -16777216, -16777216, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
            };
            try {
                cursor0 = toolkit.createCustomCursor(createImage(new MemoryImageSource(32, 32, bitmap0, 0, 32)), new Point(0, 0), "X68000_0");
                cursor1 = toolkit.createCustomCursor(createImage(new MemoryImageSource(32, 32, bitmap1, 0, 32)), new Point(0, 0), "X68000_1");
            } catch (Exception e) {
                break setCustomCursor;
            }
            setCursor(cursor1);
            controlPanel.setCursor(cursor1);
            customCursorMode = true;
            monitor.outputString("完了\n");
            return;
        }
        monitor.outputString("エラー\n");
    }

    public void update(Graphics g) {
        paint(g);
    }

    public void paint(Graphics g) {
        Image image_temp = video.image;
        if (image_temp != null) {
            g.drawImage(image_temp, video.offset_x, video.offset_y, this);
            return;
        }
    }

    public int getIntParameter(String name, int minValue, int maxValue, int defaultValue) {
        String stringValue = getStringParameter(name, null);
        if (stringValue == null) {
            monitor.outputString("パラメータ " + name + " = " + defaultValue + " （デフォルト）\n");
            return defaultValue;
        }
        int value;
        try {
            value = Integer.parseInt(stringValue);
        } catch (Exception e) {
            monitor.outputString("整数として解釈できないのでデフォルト値 " + defaultValue + " を使用します\n");
            return defaultValue;
        }
        if (value < minValue || value > maxValue) {
            monitor.outputString("範囲外なのでデフォルト値 " + defaultValue + " を使用します\n");
            return defaultValue;
        }
        return value;
    }

    public String getStringParameter(String name, String defaultValue) {
        String value = null;
        try {
            value = getParameter(name);
        } catch (Exception e) {
            value = null;
        }
        if (value != null) {
            monitor.outputString("パラメータ " + name + " = " + value + "\n");
        } else {
            value = defaultValue;
        }
        return value;
    }

    public int getFile(String fileName, byte buffer[], int offset, int length, int step) {
        return getFile(fileName, buffer, offset, length, length, step);
    }

    public int getFile(String fileName, byte buffer[], int offset, int minLength, int maxLength, int step) {
        InputStream inputStream = openFile(fileName);
        if (inputStream == null) {
            return -1;
        }
        return readFile(inputStream, buffer, offset, minLength, maxLength, step, true);
    }

    public InputStream openFile(String fileName) {
        monitor.outputString("ファイル " + fileName + " を読み込み中...");
        InputStream inputStream;
        try {
            inputStream = new URL(documentBase + fileName).openStream();
            if (fileName.endsWith(".gz")) {
                inputStream = new GZIPInputStream(inputStream);
            }
        } catch (Exception e) {
            monitor.outputString("\n" + e.toString() + "\n");
            return null;
        }
        return inputStream;
    }

    public int readFile(InputStream inputStream, byte buffer[], int offset, int length, int step, boolean close) {
        return readFile(inputStream, buffer, offset, length, length, step, close);
    }

    public int readFile(InputStream inputStream, byte buffer[], int offset, int minLength, int maxLength, int step, boolean close) {
        int limit = offset + maxLength;
        int current = offset;
        int next = current + step;
        while (current < limit) {
            int length = limit - current;
            if (length > step) {
                length = step;
            }
            try {
                length = inputStream.read(buffer, current, length);
            } catch (Exception e) {
                monitor.outputString("\n" + e.toString() + "\n");
                if (close) {
                    closeFile(inputStream, false);
                }
                return -1;
            }
            if (length < 0) {
                break;
            }
            current += length;
            if (current >= next) {
                monitor.outputChar('.');
                next += step;
            }
        }
        int read = current - offset;
        boolean completed = read >= minLength;
        if (!completed) {
            monitor.outputString("エラー\n");
        }
        if (close) {
            closeFile(inputStream, completed);
        }
        return completed ? read : -1;
    }

    public void closeFile(InputStream inputStream, boolean completed) {
        try {
            inputStream.close();
        } catch (Exception e) {
        }
        if (completed) {
            monitor.outputString("完了\n");
        }
    }

    public void itemStateChanged(ItemEvent e) {
        String item = choice.getSelectedItem();
        cardLayout.show(centerPanel, item);
        if (item == "Keyboard") {
            keyboard.requestFocusInWindow();
        } else {
            requestFocusInWindow();
        }
    }

    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_F12) {
            if ((e.getModifiersEx() & 64) != 0) {
                monitor.toggleSilent();
            } else {
                system_port.nmi();
            }
            e.consume();
            return;
        } else if (keyCode == KeyEvent.VK_F11) {
        }
        keyboard.keyPressed(e);
    }

    public void keyReleased(KeyEvent e) {
        keyboard.keyReleased(e);
    }

    public void keyTyped(KeyEvent e) {
        keyboard.keyTyped(e);
    }

    public int clock_unit = 500;
    public long clock_count;
    private int elk;
    public long crtc_interrupt_clock;
    public long mfp_interrupt_clock;
    public long sound_interrupt_clock;
    private int ssp;
    private int usp;
    private int r[];
    private short srh;
    private byte ccr;
    private int pc;
    private short srh0;
    private int pc0;
    private short op;
    private short ss;
    private int address_error_address;

    private int address_error_on_write(int a) throws MC68000Exception {
        pc = pc0;
        address_error_address = a;
        throw MC68000Exception.ADDRESS_ERROR_ON_WRITE;
    }

    private int address_error_on_read(int a) throws MC68000Exception {
        pc = pc0;
        address_error_address = a;
        throw MC68000Exception.ADDRESS_ERROR_ON_READ;
    }

    private void ccr_nz00(int z) {
        ccr = (byte) ((ccr & 16) + (z < 0 ? 8 : 0) + (z == 0 ? 4 : 0));
    }

    private boolean halt;

    Thread thread, currentthread;

    public void start() {
        thread = new Thread(this);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    public synchronized void stop() {
        thread = null;
        notify();
    }

    public void run() {
        currentthread = Thread.currentThread();
        initMC68000();
        for (int i = 0; i < 22; i++) {
            monitor.outputString(device_name[i]);
            monitor.outputString("を初期化中\n");
            if (!device_list[i].init(this)) {
                return;
            }
        }
        monitor.setMonitorInputListener(scc);
        keyboard.setKeyboardInputListener(mfp);
        addKeyListener(this);
        choice.select("Keyboard");
        cardLayout.show(centerPanel, "Keyboard");
        keyboard.requestFocusInWindow();
        halt = false;
        while (thread == currentthread) {
            if (halt) {
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    if (thread != currentthread) {
                        return;
                    }
                }
            }
            ccr = 0;
            srh = 9984;
            for (int i = 0; i <= 15; i++) {
                r[i] = 0;
            }
            usp = 0;
            fc2 = srh >>> 13 & 1;
            current_read_mmap = fc2 == 0 ? read_user_mmap : read_supervisor_mmap;
            current_write_mmap = fc2 == 0 ? write_user_mmap : write_supervisor_mmap;
            {
                int new_ipl_8 = srh & 1792;
                switch (new_ipl_8) {
                case 1536:
                    if (ipl_8 < 1792) {
                        break;
                    }
                    if (interrupt_vector_system_port >= 0) {
                        system_port.done(interrupt_vector_system_port);
                        interrupt_vector_system_port = -1;
                    }
                case 1280:
                    if (ipl_8 < 1536) {
                        break;
                    }
                    if (interrupt_vector_mfp >= 0) {
                        mfp.done(interrupt_vector_mfp);
                        interrupt_vector_mfp = -1;
                    }
                case 1024:
                    if (ipl_8 < 1280) {
                        break;
                    }
                    if (interrupt_vector_scc >= 0) {
                        scc.done(interrupt_vector_scc);
                        interrupt_vector_scc = -1;
                    }
                case 768:
                case 512:
                    if (ipl_8 < 768) {
                        break;
                    }
                    if (interrupt_vector_dmac >= 0) {
                        dmac.done(interrupt_vector_dmac);
                        interrupt_vector_dmac = -1;
                    }
                case 256:
                case 0:
                    if (ipl_8 < 256) {
                        break;
                    }
                    if (interrupt_vector_int1 >= 0) {
                        int1.done(interrupt_vector_int1);
                        interrupt_vector_int1 = -1;
                    }
                }
                ipl_8 = new_ipl_8;
            }
            try {
                ssp = read_int_big(16711680);
                {
                    int tmp_a = read_int_big(16711684);
                    pc = (tmp_a & 1) != 0 ? address_error_on_read(tmp_a) : tmp_a;
                }
            } catch (MC68000Exception e) {
                halt = true;
                continue;
            }
            clock_count = 0;
            while (thread == currentthread) {
                try {
                    while (thread == currentthread) {
                        srh0 = srh;
                        pc0 = pc;
                        op = read_short_big((pc += 2) - 2);
                        short ea, rrr, mmm, qqq;
                        short sz;
                        int clk = 0;
                        op_root_switch: switch (op >> 12 & 15) {
                        case 0:
                            ea = (short) (op & 63);
                            rrr = (short) (ea & 7);
                            mmm = (short) (ea >> 3);
                            ss = (short) (op >> 6 & 3);
                            qqq = (short) (op >> 9 & 7);
                            sz = (short) (ss == 0 ? 1 : ss == 1 ? 2 : 4);
                            if ((op & 256) == 0) {
                                int data = qqq == 4 ? read_short_big((pc += 2) - 2) : ss == 0 ? read_byte((pc += 2) - 1) : ss == 1 ? read_short_big((pc += 2) - 2) : read_int_big((pc += 4) - 4);
                                if (ea == 60) {
                                    if (ss == 0) {
                                        if (qqq == 0) {
                                            ccr |= data & 31;
                                        } else if (qqq == 1) {
                                            ccr &= data;
                                        } else {
                                            ccr ^= data & 31;
                                        }
                                    } else {
                                        if ((srh & 8192) == 0) {
                                            exception(8, pc0, srh);
                                            clk = 34;
                                            break op_root_switch;
                                        }
                                        short sr = (short) (srh + ccr);
                                        if (qqq == 0) {
                                            sr |= data;
                                        } else if (qqq == 1) {
                                            sr &= data;
                                        } else {
                                            sr ^= data;
                                        }
                                        if ((sr & 8192) == 0) {
                                            ssp = r[15];
                                            r[15] = usp;
                                            fc2 = sr >>> 13 & 1;
                                            current_read_mmap = fc2 == 0 ? read_user_mmap : read_supervisor_mmap;
                                            current_write_mmap = fc2 == 0 ? write_user_mmap : write_supervisor_mmap;
                                        }
                                        srh = (short) (sr & 42752);
                                        ccr = (byte) (sr & 31);
                                        {
                                            int new_ipl_8 = sr & 1792;
                                            switch (new_ipl_8) {
                                            case 1536:
                                                if (ipl_8 < 1792) {
                                                    break;
                                                }
                                                if (interrupt_vector_system_port >= 0) {
                                                    system_port.done(interrupt_vector_system_port);
                                                    interrupt_vector_system_port = -1;
                                                }
                                            case 1280:
                                                if (ipl_8 < 1536) {
                                                    break;
                                                }
                                                if (interrupt_vector_mfp >= 0) {
                                                    mfp.done(interrupt_vector_mfp);
                                                    interrupt_vector_mfp = -1;
                                                }
                                            case 1024:
                                                if (ipl_8 < 1280) {
                                                    break;
                                                }
                                                if (interrupt_vector_scc >= 0) {
                                                    scc.done(interrupt_vector_scc);
                                                    interrupt_vector_scc = -1;
                                                }
                                            case 768:
                                            case 512:
                                                if (ipl_8 < 768) {
                                                    break;
                                                }
                                                if (interrupt_vector_dmac >= 0) {
                                                    dmac.done(interrupt_vector_dmac);
                                                    interrupt_vector_dmac = -1;
                                                }
                                            case 256:
                                            case 0:
                                                if (ipl_8 < 256) {
                                                    break;
                                                }
                                                if (interrupt_vector_int1 >= 0) {
                                                    int1.done(interrupt_vector_int1);
                                                    interrupt_vector_int1 = -1;
                                                }
                                            }
                                            ipl_8 = new_ipl_8;
                                        }
                                    }
                                    clk = 20;
                                } else if (mmm == 0) {
                                    switch (qqq) {
                                    case 0:
                                        d_or(rrr, r[rrr], data);
                                        clk = sz < 4 ? 8 : 16;
                                        break;
                                    case 1:
                                        d_and(rrr, r[rrr], data);
                                        clk = sz < 4 ? 8 : 16;
                                        break;
                                    case 2:
                                        d_sub(rrr, r[rrr], data);
                                        clk = sz < 4 ? 8 : 16;
                                        break;
                                    case 3:
                                        d_add(rrr, r[rrr], data);
                                        clk = sz < 4 ? 8 : 16;
                                        break;
                                    case 4: {
                                        int mask = 1 << (data & 31);
                                        ccr = (byte) ((ccr & 27) + ((r[rrr] & mask) == 0 ? 4 : 0));
                                        if (ss == 0) {
                                            clk = 10;
                                        } else if (ss == 1) {
                                            r[rrr] ^= mask;
                                            clk = (mask & 65535) != 0 ? 10 : 12;
                                        } else if (ss == 2) {
                                            r[rrr] &= ~mask;
                                            clk = (mask & 65535) != 0 ? 12 : 14;
                                        } else {
                                            r[rrr] |= mask;
                                            clk = (mask & 65535) != 0 ? 10 : 12;
                                        }
                                    }
                                        break;
                                    case 5:
                                        d_eor(rrr, r[rrr], data);
                                        clk = sz < 4 ? 8 : 16;
                                        break;
                                    default:
                                        ccr_cmp(r[rrr], data, r[rrr] - data);
                                        clk = sz < 4 ? 8 : 14;
                                    }
                                } else {
                                    int a = qqq == 4 ? c_ea1(ea) : c_ea(ea, sz);
                                    switch (qqq) {
                                    case 0:
                                        m_or(a, ss == 0 ? read_byte(a) : (a & 1) != 0 ? address_error_on_read(a) : ss == 1 ? read_short_big(a) : read_int_big(a), data);
                                        clk = (sz < 4 ? 12 : 20) + elk;
                                        break;
                                    case 1:
                                        m_and(a, ss == 0 ? read_byte(a) : (a & 1) != 0 ? address_error_on_read(a) : ss == 1 ? read_short_big(a) : read_int_big(a), data);
                                        clk = (sz < 4 ? 12 : 20) + elk;
                                        break;
                                    case 2:
                                        m_sub(a, ss == 0 ? read_byte(a) : (a & 1) != 0 ? address_error_on_read(a) : ss == 1 ? read_short_big(a) : read_int_big(a), data);
                                        clk = (sz < 4 ? 12 : 20) + elk;
                                        break;
                                    case 3:
                                        m_add(a, ss == 0 ? read_byte(a) : (a & 1) != 0 ? address_error_on_read(a) : ss == 1 ? read_short_big(a) : read_int_big(a), data);
                                        clk = (sz < 4 ? 12 : 20) + elk;
                                        break;
                                    case 4: {
                                        int mask = 1 << (data & 7);
                                        byte b = read_byte(a);
                                        ccr = (byte) ((ccr & 27) + ((b & mask) == 0 ? 4 : 0));
                                        if (ss == 0) {
                                            clk = 8 + elk;
                                        } else if (ss == 1) {
                                            write_byte(a, (byte) (b ^ mask));
                                            clk = 12 + elk;
                                        } else if (ss == 2) {
                                            write_byte(a, (byte) (b & ~mask));
                                            clk = 12 + elk;
                                        } else {
                                            write_byte(a, (byte) (b | mask));
                                            clk = 12 + elk;
                                        }
                                    }
                                        break;
                                    case 5:
                                        m_eor(a, ss == 0 ? read_byte(a) : (a & 1) != 0 ? address_error_on_read(a) : ss == 1 ? read_short_big(a) : read_int_big(a), data);
                                        clk = (sz < 4 ? 12 : 20) + elk;
                                        break;
                                    default: {
                                        int dst = ss == 0 ? read_byte(a) : (a & 1) != 0 ? address_error_on_read(a) : ss == 1 ? read_short_big(a) : read_int_big(a);
                                        ccr_cmp(dst, data, dst - data);
                                    }
                                        clk = (sz < 4 ? 8 : 12) + elk;
                                    }
                                }
                            } else if (mmm == 0) {
                                int mask = 1 << (r[qqq] & 31);
                                ccr = (byte) ((ccr & 27) + ((r[ea] & mask) == 0 ? 4 : 0));
                                if (ss == 0) {
                                    clk = 6;
                                } else if (ss == 1) {
                                    r[ea] ^= mask;
                                    clk = (mask & 65535) != 0 ? 6 : 8;
                                } else if (ss == 2) {
                                    r[ea] &= ~mask;
                                    clk = (mask & 65535) != 0 ? 8 : 10;
                                } else {
                                    r[ea] |= mask;
                                    clk = (mask & 65535) != 0 ? 6 : 8;
                                }
                            } else if (mmm == 1) {
                                int a = r[ea] + read_short_big((pc += 2) - 2);
                                if (ss == 0) {
                                    r[qqq] = (r[qqq] & -65536) + ((short) ((read_byte(a) << 8) + (read_byte(a + 2) & 255)) & 65535);
                                    clk = 16;
                                } else if (ss == 1) {
                                    r[qqq] = (((read_byte(a) << 8) + (read_byte(a + 2) & 255) << 8) + (read_byte(a + 4) & 255) << 8) + (read_byte(a + 6) & 255);
                                    clk = 24;
                                } else if (ss == 2) {
                                    int t = r[qqq];
                                    write_byte(a, (byte) (t >> 8));
                                    write_byte(a + 2, (byte) t);
                                    clk = 16;
                                } else {
                                    int t = r[qqq];
                                    write_byte(a, (byte) (t >> 24));
                                    write_byte(a + 2, (byte) (t >> 16));
                                    write_byte(a + 4, (byte) (t >> 8));
                                    write_byte(a + 6, (byte) t);
                                    clk = 24;
                                }
                            } else {
                                int a = c_ea1(ea);
                                int mask = 1 << (r[qqq] & 7);
                                byte b = read_byte(a);
                                ccr = (byte) ((ccr & 27) + ((b & mask) == 0 ? 4 : 0));
                                if (ss == 0) {
                                    clk = 4 + elk;
                                } else if (ss == 1) {
                                    write_byte(a, (byte) (b ^ mask));
                                    clk = 8 + elk;
                                } else if (ss == 2) {
                                    write_byte(a, (byte) (b & ~mask));
                                    clk = 8 + elk;
                                } else {
                                    write_byte(a, (byte) (b | mask));
                                    clk = 8 + elk;
                                }
                            }
                            break;
                        case 1:
                            ea = (short) (op & 63);
                            mmm = (short) (ea >> 3);
                            qqq = (short) (op >> 9 & 7);
                            {
                                short nnn = (short) (op >> 6 & 7);
                                byte src = mmm < 2 ? (byte) r[ea] : read_byte(c_ea1(ea));
                                clk = 4 + elk;
                                if (nnn == 0) {
                                    r[qqq] = (r[qqq] & -256) + (src & 255);
                                } else {
                                    write_byte(c_ea1((short) ((nnn << 3) + qqq)), src);
                                    clk += nnn == 4 ? elk - 2 : elk;
                                }
                                ccr = (byte) ((ccr & 16) + (src < 0 ? 8 : 0) + (src == 0 ? 4 : 0));
                            }
                            break;
                        case 2:
                            ea = (short) (op & 63);
                            mmm = (short) (ea >> 3);
                            qqq = (short) (op >> 9 & 7);
                            {
                                short nnn = (short) (op >> 6 & 7);
                                int src;
                                if (mmm < 2) {
                                    src = r[ea];
                                } else {
                                    int a = c_ea4(ea);
                                    src = (a & 1) != 0 ? address_error_on_read(a) : read_int_big(a);
                                }
                                clk = 4 + elk;
                                if (nnn == 0) {
                                    r[qqq] = src;
                                    ccr = (byte) ((ccr & 16) + (src < 0 ? 8 : 0) + (src == 0 ? 4 : 0));
                                } else if (nnn == 1) {
                                    r[8 + qqq] = src;
                                } else {
                                    int a = c_ea4((short) ((nnn << 3) + qqq));
                                    write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, src);
                                    clk += nnn == 4 ? elk - 2 : elk;
                                    ccr = (byte) ((ccr & 16) + (src < 0 ? 8 : 0) + (src == 0 ? 4 : 0));
                                }
                            }
                            break;
                        case 3:
                            ea = (short) (op & 63);
                            mmm = (short) (ea >> 3);
                            qqq = (short) (op >> 9 & 7);
                            {
                                short nnn = (short) (op >> 6 & 7);
                                short src;
                                if (mmm < 2) {
                                    src = (short) r[ea];
                                } else {
                                    int a = c_ea2(ea);
                                    src = (short) ((a & 1) != 0 ? address_error_on_read(a) : read_short_big(a));
                                }
                                clk = 4 + elk;
                                if (nnn == 0) {
                                    r[qqq] = (r[qqq] & -65536) + (src & 65535);
                                    ccr = (byte) ((ccr & 16) + (src < 0 ? 8 : 0) + (src == 0 ? 4 : 0));
                                } else if (nnn == 1) {
                                    r[8 + qqq] = src;
                                } else {
                                    int a = c_ea2((short) ((nnn << 3) + qqq));
                                    write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, src);
                                    clk += nnn == 4 ? elk - 2 : elk;
                                    ccr = (byte) ((ccr & 16) + (src < 0 ? 8 : 0) + (src == 0 ? 4 : 0));
                                }
                            }
                            break;
                        case 4:
                            ea = (short) (op & 63);
                            rrr = (short) (ea & 7);
                            mmm = (short) (ea >> 3);
                            ss = (short) (op >> 6 & 3);
                            qqq = (short) (op >> 9 & 7);
                            sz = (short) (ss == 0 ? 1 : ss == 1 ? 2 : 4);
                            if ((op & 256) == 0) {
                                switch (qqq) {
                                case 0:
                                    if (ss < 3) {
                                        if (mmm < 2) {
                                            d_subx(rrr, 0, r[rrr]);
                                            clk = sz < 4 ? 4 : 6;
                                        } else {
                                            int a = c_ea(ea, sz);
                                            m_subx(a, 0, ss == 0 ? read_byte(a) : (a & 1) != 0 ? address_error_on_read(a) : ss == 1 ? read_short_big(a) : read_int_big(a));
                                            clk = (sz < 4 ? 8 : 12) + elk;
                                        }
                                    } else {
                                        if (mmm < 2) {
                                            r[rrr] = (r[rrr] & -65536) + ((short) (srh + ccr) & 65535);
                                            clk = 6;
                                        } else {
                                            int a = c_ea2(ea);
                                            write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) (srh + ccr));
                                            clk = 8 + elk;
                                        }
                                    }
                                    break;
                                case 1:
                                    if (ss < 3) {
                                        if (mmm < 2) {
                                            r[rrr] = ss == 0 ? r[rrr] & -256 : ss == 1 ? r[rrr] & -65536 : 0;
                                            clk = sz < 4 ? 4 : 6;
                                        } else {
                                            int a = c_ea(ea, sz);
                                            if (ss == 0) {
                                                write_byte(a, (byte) 0);
                                            } else if (ss == 1) {
                                                write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) 0);
                                            } else {
                                                write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, 0);
                                            }
                                            clk = (sz < 4 ? 8 : 12) + elk;
                                        }
                                        ccr = (byte) ((ccr & 16) + 4);
                                    } else {
                                        exception(4, pc0, srh);
                                        clk = 34;
                                    }
                                    break;
                                case 2:
                                    if (ss < 3) {
                                        if (mmm < 2) {
                                            d_sub(rrr, 0, r[rrr]);
                                            clk = sz < 4 ? 4 : 6;
                                        } else {
                                            int a = c_ea(ea, sz);
                                            m_sub(a, 0, ss == 0 ? read_byte(a) : (a & 1) != 0 ? address_error_on_read(a) : ss == 1 ? read_short_big(a) : read_int_big(a));
                                            clk = (sz < 4 ? 8 : 12) + elk;
                                        }
                                    } else {
                                        if (mmm < 2) {
                                            ccr = (byte) (r[rrr] & 31);
                                            clk = 12;
                                        } else {
                                            int a = c_ea2(ea);
                                            ccr = (byte) ((short) ((a & 1) != 0 ? address_error_on_read(a) : read_short_big(a)) & 31);
                                            clk = 12 + elk;
                                        }
                                    }
                                    break;
                                case 3:
                                    if (ss < 3) {
                                        if (mmm < 2) {
                                            d_eor(rrr, r[rrr], -1);
                                            clk = sz < 4 ? 4 : 6;
                                        } else {
                                            int a = c_ea(ea, sz);
                                            m_eor(a, ss == 0 ? read_byte(a) : (a & 1) != 0 ? address_error_on_read(a) : ss == 1 ? read_short_big(a) : read_int_big(a), -1);
                                            clk = (sz < 4 ? 8 : 12) + elk;
                                        }
                                    } else {
                                        if ((srh & 8192) == 0) {
                                            exception(8, pc0, srh);
                                            clk = 34;
                                            break op_root_switch;
                                        }
                                        if (mmm < 2) {
                                            short sr = (short) r[rrr];
                                            if ((sr & 8192) == 0) {
                                                ssp = r[15];
                                                r[15] = usp;
                                                fc2 = sr >>> 13 & 1;
                                                current_read_mmap = fc2 == 0 ? read_user_mmap : read_supervisor_mmap;
                                                current_write_mmap = fc2 == 0 ? write_user_mmap : write_supervisor_mmap;
                                            }
                                            srh = (short) (sr & 42752);
                                            ccr = (byte) (sr & 31);
                                            {
                                                int new_ipl_8 = sr & 1792;
                                                switch (new_ipl_8) {
                                                case 1536:
                                                    if (ipl_8 < 1792) {
                                                        break;
                                                    }
                                                    if (interrupt_vector_system_port >= 0) {
                                                        system_port.done(interrupt_vector_system_port);
                                                        interrupt_vector_system_port = -1;
                                                    }
                                                case 1280:
                                                    if (ipl_8 < 1536) {
                                                        break;
                                                    }
                                                    if (interrupt_vector_mfp >= 0) {
                                                        mfp.done(interrupt_vector_mfp);
                                                        interrupt_vector_mfp = -1;
                                                    }
                                                case 1024:
                                                    if (ipl_8 < 1280) {
                                                        break;
                                                    }
                                                    if (interrupt_vector_scc >= 0) {
                                                        scc.done(interrupt_vector_scc);
                                                        interrupt_vector_scc = -1;
                                                    }
                                                case 768:
                                                case 512:
                                                    if (ipl_8 < 768) {
                                                        break;
                                                    }
                                                    if (interrupt_vector_dmac >= 0) {
                                                        dmac.done(interrupt_vector_dmac);
                                                        interrupt_vector_dmac = -1;
                                                    }
                                                case 256:
                                                case 0:
                                                    if (ipl_8 < 256) {
                                                        break;
                                                    }
                                                    if (interrupt_vector_int1 >= 0) {
                                                        int1.done(interrupt_vector_int1);
                                                        interrupt_vector_int1 = -1;
                                                    }
                                                }
                                                ipl_8 = new_ipl_8;
                                            }
                                            clk = 12;
                                        } else {
                                            int a = c_ea2(ea);
                                            short sr = (short) ((a & 1) != 0 ? address_error_on_read(a) : read_short_big(a));
                                            if ((sr & 8192) == 0) {
                                                ssp = r[15];
                                                r[15] = usp;
                                                fc2 = sr >>> 13 & 1;
                                                current_read_mmap = fc2 == 0 ? read_user_mmap : read_supervisor_mmap;
                                                current_write_mmap = fc2 == 0 ? write_user_mmap : write_supervisor_mmap;
                                            }
                                            srh = (short) (sr & 42752);
                                            ccr = (byte) (sr & 31);
                                            {
                                                int new_ipl_8 = sr & 1792;
                                                switch (new_ipl_8) {
                                                case 1536:
                                                    if (ipl_8 < 1792) {
                                                        break;
                                                    }
                                                    if (interrupt_vector_system_port >= 0) {
                                                        system_port.done(interrupt_vector_system_port);
                                                        interrupt_vector_system_port = -1;
                                                    }
                                                case 1280:
                                                    if (ipl_8 < 1536) {
                                                        break;
                                                    }
                                                    if (interrupt_vector_mfp >= 0) {
                                                        mfp.done(interrupt_vector_mfp);
                                                        interrupt_vector_mfp = -1;
                                                    }
                                                case 1024:
                                                    if (ipl_8 < 1280) {
                                                        break;
                                                    }
                                                    if (interrupt_vector_scc >= 0) {
                                                        scc.done(interrupt_vector_scc);
                                                        interrupt_vector_scc = -1;
                                                    }
                                                case 768:
                                                case 512:
                                                    if (ipl_8 < 768) {
                                                        break;
                                                    }
                                                    if (interrupt_vector_dmac >= 0) {
                                                        dmac.done(interrupt_vector_dmac);
                                                        interrupt_vector_dmac = -1;
                                                    }
                                                case 256:
                                                case 0:
                                                    if (ipl_8 < 256) {
                                                        break;
                                                    }
                                                    if (interrupt_vector_int1 >= 0) {
                                                        int1.done(interrupt_vector_int1);
                                                        interrupt_vector_int1 = -1;
                                                    }
                                                }
                                                ipl_8 = new_ipl_8;
                                            }
                                            clk = 12 + elk;
                                        }
                                    }
                                    break;
                                case 4:
                                    if (ss == 0) {
                                        if (mmm < 2) {
                                            r[rrr] = (r[rrr] & -256) + (sbcd((byte) 0, (byte) r[rrr]) & 255);
                                            clk = 6;
                                        } else {
                                            int a = c_ea1(ea);
                                            write_byte(a, sbcd((byte) 0, read_byte(a)));
                                            clk = 8 + elk;
                                        }
                                    } else if (ss == 1) {
                                        if (mmm == 0) {
                                            r[rrr] = (r[rrr] << 16) + (r[rrr] >>> 16);
                                            ccr = (byte) ((ccr & 16) + (r[rrr] < 0 ? 8 : 0) + (r[rrr] == 0 ? 4 : 0));
                                            clk = 4;
                                        } else {
                                            write_int_big(r[15] - 4, c_ea0(ea));
                                            r[15] -= 4;
                                            clk = (mmm == 6 || ea == 59 ? 10 : 8) + elk;
                                        }
                                    } else {
                                        if (mmm == 0) {
                                            if (ss == 2) {
                                                int src = (byte) r[rrr];
                                                r[rrr] = (r[rrr] & -65536) + (src & 65535);
                                                ccr = (byte) ((ccr & 16) + (src < 0 ? 8 : 0) + (src == 0 ? 4 : 0));
                                            } else {
                                                int src = (short) r[rrr];
                                                r[rrr] = src;
                                                ccr = (byte) ((ccr & 16) + (src < 0 ? 8 : 0) + (src == 0 ? 4 : 0));
                                            }
                                            clk = 4;
                                        } else {
                                            int reglist = read_short_big((pc += 2) - 2);
                                            int a = c_ea0(ea);
                                            clk = (mmm == 4 ? 2 : 4) + elk;
                                            if (mmm == 4) {
                                                if (ss == 2) {
                                                    int a0 = a;
                                                    if ((reglist & 1) != 0) {
                                                        a -= 2;
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[15]);
                                                    }
                                                    if ((reglist & 2) != 0) {
                                                        a -= 2;
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[14]);
                                                    }
                                                    if ((reglist & 4) != 0) {
                                                        a -= 2;
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[13]);
                                                    }
                                                    if ((reglist & 8) != 0) {
                                                        a -= 2;
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[12]);
                                                    }
                                                    if ((reglist & 16) != 0) {
                                                        a -= 2;
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[11]);
                                                    }
                                                    if ((reglist & 32) != 0) {
                                                        a -= 2;
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[10]);
                                                    }
                                                    if ((reglist & 64) != 0) {
                                                        a -= 2;
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[9]);
                                                    }
                                                    if ((reglist & 128) != 0) {
                                                        a -= 2;
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[8]);
                                                    }
                                                    if ((reglist & 256) != 0) {
                                                        a -= 2;
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[7]);
                                                    }
                                                    if ((reglist & 512) != 0) {
                                                        a -= 2;
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[6]);
                                                    }
                                                    if ((reglist & 1024) != 0) {
                                                        a -= 2;
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[5]);
                                                    }
                                                    if ((reglist & 2048) != 0) {
                                                        a -= 2;
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[4]);
                                                    }
                                                    if ((reglist & 4096) != 0) {
                                                        a -= 2;
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[3]);
                                                    }
                                                    if ((reglist & 8192) != 0) {
                                                        a -= 2;
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[2]);
                                                    }
                                                    if ((reglist & 16384) != 0) {
                                                        a -= 2;
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[1]);
                                                    }
                                                    if ((reglist & 32768) != 0) {
                                                        a -= 2;
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[0]);
                                                    }
                                                    clk += a0 - a << 1;
                                                } else {
                                                    int a0 = a;
                                                    if ((reglist & 1) != 0) {
                                                        a -= 4;
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[15]);
                                                    }
                                                    if ((reglist & 2) != 0) {
                                                        a -= 4;
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[14]);
                                                    }
                                                    if ((reglist & 4) != 0) {
                                                        a -= 4;
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[13]);
                                                    }
                                                    if ((reglist & 8) != 0) {
                                                        a -= 4;
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[12]);
                                                    }
                                                    if ((reglist & 16) != 0) {
                                                        a -= 4;
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[11]);
                                                    }
                                                    if ((reglist & 32) != 0) {
                                                        a -= 4;
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[10]);
                                                    }
                                                    if ((reglist & 64) != 0) {
                                                        a -= 4;
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[9]);
                                                    }
                                                    if ((reglist & 128) != 0) {
                                                        a -= 4;
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[8]);
                                                    }
                                                    if ((reglist & 256) != 0) {
                                                        a -= 4;
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[7]);
                                                    }
                                                    if ((reglist & 512) != 0) {
                                                        a -= 4;
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[6]);
                                                    }
                                                    if ((reglist & 1024) != 0) {
                                                        a -= 4;
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[5]);
                                                    }
                                                    if ((reglist & 2048) != 0) {
                                                        a -= 4;
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[4]);
                                                    }
                                                    if ((reglist & 4096) != 0) {
                                                        a -= 4;
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[3]);
                                                    }
                                                    if ((reglist & 8192) != 0) {
                                                        a -= 4;
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[2]);
                                                    }
                                                    if ((reglist & 16384) != 0) {
                                                        a -= 4;
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[1]);
                                                    }
                                                    if ((reglist & 32768) != 0) {
                                                        a -= 4;
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[0]);
                                                    }
                                                    clk += a0 - a << 1;
                                                }
                                                r[8 + rrr] = a;
                                            } else {
                                                if (ss == 2) {
                                                    int a0 = a;
                                                    if ((reglist & 1) != 0) {
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[0]);
                                                        a += 2;
                                                    }
                                                    if ((reglist & 2) != 0) {
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[1]);
                                                        a += 2;
                                                    }
                                                    if ((reglist & 4) != 0) {
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[2]);
                                                        a += 2;
                                                    }
                                                    if ((reglist & 8) != 0) {
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[3]);
                                                        a += 2;
                                                    }
                                                    if ((reglist & 16) != 0) {
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[4]);
                                                        a += 2;
                                                    }
                                                    if ((reglist & 32) != 0) {
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[5]);
                                                        a += 2;
                                                    }
                                                    if ((reglist & 64) != 0) {
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[6]);
                                                        a += 2;
                                                    }
                                                    if ((reglist & 128) != 0) {
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[7]);
                                                        a += 2;
                                                    }
                                                    if ((reglist & 256) != 0) {
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[8]);
                                                        a += 2;
                                                    }
                                                    if ((reglist & 512) != 0) {
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[9]);
                                                        a += 2;
                                                    }
                                                    if ((reglist & 1024) != 0) {
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[10]);
                                                        a += 2;
                                                    }
                                                    if ((reglist & 2048) != 0) {
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[11]);
                                                        a += 2;
                                                    }
                                                    if ((reglist & 4096) != 0) {
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[12]);
                                                        a += 2;
                                                    }
                                                    if ((reglist & 8192) != 0) {
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[13]);
                                                        a += 2;
                                                    }
                                                    if ((reglist & 16384) != 0) {
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[14]);
                                                        a += 2;
                                                    }
                                                    if ((reglist & 32768) != 0) {
                                                        write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) r[15]);
                                                        a += 2;
                                                    }
                                                    clk += a - a0 << 1;
                                                } else {
                                                    int a0 = a;
                                                    if ((reglist & 1) != 0) {
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[0]);
                                                        a += 4;
                                                    }
                                                    if ((reglist & 2) != 0) {
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[1]);
                                                        a += 4;
                                                    }
                                                    if ((reglist & 4) != 0) {
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[2]);
                                                        a += 4;
                                                    }
                                                    if ((reglist & 8) != 0) {
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[3]);
                                                        a += 4;
                                                    }
                                                    if ((reglist & 16) != 0) {
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[4]);
                                                        a += 4;
                                                    }
                                                    if ((reglist & 32) != 0) {
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[5]);
                                                        a += 4;
                                                    }
                                                    if ((reglist & 64) != 0) {
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[6]);
                                                        a += 4;
                                                    }
                                                    if ((reglist & 128) != 0) {
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[7]);
                                                        a += 4;
                                                    }
                                                    if ((reglist & 256) != 0) {
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[8]);
                                                        a += 4;
                                                    }
                                                    if ((reglist & 512) != 0) {
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[9]);
                                                        a += 4;
                                                    }
                                                    if ((reglist & 1024) != 0) {
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[10]);
                                                        a += 4;
                                                    }
                                                    if ((reglist & 2048) != 0) {
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[11]);
                                                        a += 4;
                                                    }
                                                    if ((reglist & 4096) != 0) {
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[12]);
                                                        a += 4;
                                                    }
                                                    if ((reglist & 8192) != 0) {
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[13]);
                                                        a += 4;
                                                    }
                                                    if ((reglist & 16384) != 0) {
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[14]);
                                                        a += 4;
                                                    }
                                                    if ((reglist & 32768) != 0) {
                                                        write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, r[15]);
                                                        a += 4;
                                                    }
                                                    clk += a - a0 << 1;
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case 5:
                                    if (ss < 3) {
                                        if (mmm < 2) {
                                            int src = ss == 0 ? (byte) r[rrr] : ss == 1 ? (short) r[rrr] : r[rrr];
                                            ccr = (byte) ((ccr & 16) + (src < 0 ? 8 : 0) + (src == 0 ? 4 : 0));
                                            clk = 4;
                                        } else {
                                            {
                                                int a = c_ea(ea, sz);
                                                int src = ss == 0 ? (byte) (ss == 0 ? read_byte(a) : (a & 1) != 0 ? address_error_on_read(a) : ss == 1 ? read_short_big(a) : read_int_big(a)) : ss == 1 ? (short) (ss == 0 ? read_byte(a) : (a & 1) != 0 ? address_error_on_read(a) : ss == 1 ? read_short_big(a) : read_int_big(a)) : ss == 0 ? read_byte(a) : (a & 1) != 0 ? address_error_on_read(a) : ss == 1 ? read_short_big(a) : read_int_big(a);
                                                ccr = (byte) ((ccr & 16) + (src < 0 ? 8 : 0) + (src == 0 ? 4 : 0));
                                            }
                                            clk = 4 + elk;
                                        }
                                    } else if (ea == 60) {
                                        exception(4, pc0, srh);
                                        clk = 34;
                                    } else {
                                        if (mmm < 2) {
                                            int src = (byte) r[rrr];
                                            r[rrr] |= 128;
                                            ccr = (byte) ((ccr & 16) + (src < 0 ? 8 : 0) + (src == 0 ? 4 : 0));
                                            clk = 4;
                                        } else {
                                            int a = c_ea1(ea);
                                            int z = read_byte(a);
                                            ccr = (byte) ((ccr & 16) + (z < 0 ? 8 : 0) + (z == 0 ? 4 : 0));
                                            write_byte(a, (byte) (z | 128));
                                            clk = 14 + elk;
                                        }
                                    }
                                    break;
                                case 6: {
                                    short reglist = read_short_big((pc += 2) - 2);
                                    int a = c_ea0(ea);
                                    clk = 8 + elk;
                                    if (ss == 2) {
                                        int a0 = a;
                                        if ((reglist & 1) != 0) {
                                            r[0] = (r[0] & -65536) + ((short) ((a & 1) != 0 ? address_error_on_read(a) : read_short_big(a)) & 65535);
                                            a += 2;
                                        }
                                        if ((reglist & 2) != 0) {
                                            r[1] = (r[1] & -65536) + ((short) ((a & 1) != 0 ? address_error_on_read(a) : read_short_big(a)) & 65535);
                                            a += 2;
                                        }
                                        if ((reglist & 4) != 0) {
                                            r[2] = (r[2] & -65536) + ((short) ((a & 1) != 0 ? address_error_on_read(a) : read_short_big(a)) & 65535);
                                            a += 2;
                                        }
                                        if ((reglist & 8) != 0) {
                                            r[3] = (r[3] & -65536) + ((short) ((a & 1) != 0 ? address_error_on_read(a) : read_short_big(a)) & 65535);
                                            a += 2;
                                        }
                                        if ((reglist & 16) != 0) {
                                            r[4] = (r[4] & -65536) + ((short) ((a & 1) != 0 ? address_error_on_read(a) : read_short_big(a)) & 65535);
                                            a += 2;
                                        }
                                        if ((reglist & 32) != 0) {
                                            r[5] = (r[5] & -65536) + ((short) ((a & 1) != 0 ? address_error_on_read(a) : read_short_big(a)) & 65535);
                                            a += 2;
                                        }
                                        if ((reglist & 64) != 0) {
                                            r[6] = (r[6] & -65536) + ((short) ((a & 1) != 0 ? address_error_on_read(a) : read_short_big(a)) & 65535);
                                            a += 2;
                                        }
                                        if ((reglist & 128) != 0) {
                                            r[7] = (r[7] & -65536) + ((short) ((a & 1) != 0 ? address_error_on_read(a) : read_short_big(a)) & 65535);
                                            a += 2;
                                        }
                                        if ((reglist & 256) != 0) {
                                            r[8] = (short) ((a & 1) != 0 ? address_error_on_read(a) : read_short_big(a));
                                            a += 2;
                                        }
                                        if ((reglist & 512) != 0) {
                                            r[9] = (short) ((a & 1) != 0 ? address_error_on_read(a) : read_short_big(a));
                                            a += 2;
                                        }
                                        if ((reglist & 1024) != 0) {
                                            r[10] = (short) ((a & 1) != 0 ? address_error_on_read(a) : read_short_big(a));
                                            a += 2;
                                        }
                                        if ((reglist & 2048) != 0) {
                                            r[11] = (short) ((a & 1) != 0 ? address_error_on_read(a) : read_short_big(a));
                                            a += 2;
                                        }
                                        if ((reglist & 4096) != 0) {
                                            r[12] = (short) ((a & 1) != 0 ? address_error_on_read(a) : read_short_big(a));
                                            a += 2;
                                        }
                                        if ((reglist & 8192) != 0) {
                                            r[13] = (short) ((a & 1) != 0 ? address_error_on_read(a) : read_short_big(a));
                                            a += 2;
                                        }
                                        if ((reglist & 16384) != 0) {
                                            r[14] = (short) ((a & 1) != 0 ? address_error_on_read(a) : read_short_big(a));
                                            a += 2;
                                        }
                                        if ((reglist & 32768) != 0) {
                                            r[15] = (short) ((a & 1) != 0 ? address_error_on_read(a) : read_short_big(a));
                                            a += 2;
                                        }
                                        clk += a - a0 << 1;
                                    } else {
                                        int a0 = a;
                                        if ((reglist & 1) != 0) {
                                            r[0] = (a & 1) != 0 ? address_error_on_read(a) : read_int_big(a);
                                            a += 4;
                                        }
                                        if ((reglist & 2) != 0) {
                                            r[1] = (a & 1) != 0 ? address_error_on_read(a) : read_int_big(a);
                                            a += 4;
                                        }
                                        if ((reglist & 4) != 0) {
                                            r[2] = (a & 1) != 0 ? address_error_on_read(a) : read_int_big(a);
                                            a += 4;
                                        }
                                        if ((reglist & 8) != 0) {
                                            r[3] = (a & 1) != 0 ? address_error_on_read(a) : read_int_big(a);
                                            a += 4;
                                        }
                                        if ((reglist & 16) != 0) {
                                            r[4] = (a & 1) != 0 ? address_error_on_read(a) : read_int_big(a);
                                            a += 4;
                                        }
                                        if ((reglist & 32) != 0) {
                                            r[5] = (a & 1) != 0 ? address_error_on_read(a) : read_int_big(a);
                                            a += 4;
                                        }
                                        if ((reglist & 64) != 0) {
                                            r[6] = (a & 1) != 0 ? address_error_on_read(a) : read_int_big(a);
                                            a += 4;
                                        }
                                        if ((reglist & 128) != 0) {
                                            r[7] = (a & 1) != 0 ? address_error_on_read(a) : read_int_big(a);
                                            a += 4;
                                        }
                                        if ((reglist & 256) != 0) {
                                            r[8] = (a & 1) != 0 ? address_error_on_read(a) : read_int_big(a);
                                            a += 4;
                                        }
                                        if ((reglist & 512) != 0) {
                                            r[9] = (a & 1) != 0 ? address_error_on_read(a) : read_int_big(a);
                                            a += 4;
                                        }
                                        if ((reglist & 1024) != 0) {
                                            r[10] = (a & 1) != 0 ? address_error_on_read(a) : read_int_big(a);
                                            a += 4;
                                        }
                                        if ((reglist & 2048) != 0) {
                                            r[11] = (a & 1) != 0 ? address_error_on_read(a) : read_int_big(a);
                                            a += 4;
                                        }
                                        if ((reglist & 4096) != 0) {
                                            r[12] = (a & 1) != 0 ? address_error_on_read(a) : read_int_big(a);
                                            a += 4;
                                        }
                                        if ((reglist & 8192) != 0) {
                                            r[13] = (a & 1) != 0 ? address_error_on_read(a) : read_int_big(a);
                                            a += 4;
                                        }
                                        if ((reglist & 16384) != 0) {
                                            r[14] = (a & 1) != 0 ? address_error_on_read(a) : read_int_big(a);
                                            a += 4;
                                        }
                                        if ((reglist & 32768) != 0) {
                                            r[15] = (a & 1) != 0 ? address_error_on_read(a) : read_int_big(a);
                                            a += 4;
                                        }
                                        clk += a - a0 << 1;
                                    }
                                    if (mmm == 3) {
                                        r[8 + rrr] = a;
                                    }
                                }
                                    break;
                                case 7:
                                    if (ss == 1) {
                                        if (ea == 53) {
                                            {
                                                int tmp_a = (r[15] & 1) != 0 ? address_error_on_read(r[15]) : read_int_big((r[15] += 4) - 4);
                                                pc = (tmp_a & 1) != 0 ? address_error_on_read(tmp_a) : tmp_a;
                                            }
                                            clk = 16;
                                        } else if (ea == 51) {
                                            if ((srh & 8192) == 0) {
                                                exception(8, pc0, srh);
                                                clk = 34;
                                                break op_root_switch;
                                            }
                                            short sr = (short) ((r[15] & 1) != 0 ? address_error_on_read(r[15]) : read_short_big(r[15]));
                                            {
                                                int tmp_a = (r[15] + 2 & 1) != 0 ? address_error_on_read(r[15] + 2) : read_int_big(r[15] + 2);
                                                pc = (tmp_a & 1) != 0 ? address_error_on_read(tmp_a) : tmp_a;
                                            }
                                            r[15] += 6;
                                            if ((sr & 8192) == 0) {
                                                ssp = r[15];
                                                r[15] = usp;
                                                fc2 = sr >>> 13 & 1;
                                                current_read_mmap = fc2 == 0 ? read_user_mmap : read_supervisor_mmap;
                                                current_write_mmap = fc2 == 0 ? write_user_mmap : write_supervisor_mmap;
                                            }
                                            srh = (short) (sr & 42752);
                                            ccr = (byte) (sr & 31);
                                            {
                                                int new_ipl_8 = sr & 1792;
                                                switch (new_ipl_8) {
                                                case 1536:
                                                    if (ipl_8 < 1792) {
                                                        break;
                                                    }
                                                    if (interrupt_vector_system_port >= 0) {
                                                        system_port.done(interrupt_vector_system_port);
                                                        interrupt_vector_system_port = -1;
                                                    }
                                                case 1280:
                                                    if (ipl_8 < 1536) {
                                                        break;
                                                    }
                                                    if (interrupt_vector_mfp >= 0) {
                                                        mfp.done(interrupt_vector_mfp);
                                                        interrupt_vector_mfp = -1;
                                                    }
                                                case 1024:
                                                    if (ipl_8 < 1280) {
                                                        break;
                                                    }
                                                    if (interrupt_vector_scc >= 0) {
                                                        scc.done(interrupt_vector_scc);
                                                        interrupt_vector_scc = -1;
                                                    }
                                                case 768:
                                                case 512:
                                                    if (ipl_8 < 768) {
                                                        break;
                                                    }
                                                    if (interrupt_vector_dmac >= 0) {
                                                        dmac.done(interrupt_vector_dmac);
                                                        interrupt_vector_dmac = -1;
                                                    }
                                                case 256:
                                                case 0:
                                                    if (ipl_8 < 256) {
                                                        break;
                                                    }
                                                    if (interrupt_vector_int1 >= 0) {
                                                        int1.done(interrupt_vector_int1);
                                                        interrupt_vector_int1 = -1;
                                                    }
                                                }
                                                ipl_8 = new_ipl_8;
                                            }
                                            clk = 20;
                                        } else if (mmm < 2) {
                                            if (ea == 15) {
                                                int num = r[0] & 255;
                                                if (num >= 64 && num <= 79) {
                                                    if ((r[1] & 4096) != 0) {
                                                        fdc.iocs(r);
                                                    } else {
                                                        r[0] = hdc.iocs(r);
                                                    }
                                                    break op_root_switch;
                                                }
                                            }
                                            exception(32 + ea, pc, srh);
                                            clk = 34;
                                            break op_root_switch;
                                        } else if (mmm == 2) {
                                            write_int_big(r[15] - 4, r[8 + rrr]);
                                            r[15] -= 4;
                                            r[8 + rrr] = r[15];
                                            r[15] += read_short_big((pc += 2) - 2);
                                            clk = 16;
                                        } else if (mmm == 3) {
                                            r[15] = r[8 + rrr];
                                            r[8 + rrr] = (r[15] & 1) != 0 ? address_error_on_read(r[15]) : read_int_big((r[15] += 4) - 4);
                                            clk = 12;
                                        } else if (ea == 55) {
                                            ccr = (byte) ((short) ((r[15] & 1) != 0 ? address_error_on_read(r[15]) : read_short_big((r[15] += 2) - 2)) & 31);
                                            {
                                                int tmp_a = (r[15] & 1) != 0 ? address_error_on_read(r[15]) : read_int_big((r[15] += 4) - 4);
                                                pc = (tmp_a & 1) != 0 ? address_error_on_read(tmp_a) : tmp_a;
                                            }
                                            clk = 20;
                                        } else if (ea == 49) {
                                            clk = 4;
                                        } else if (mmm == 4) {
                                            if ((srh & 8192) == 0) {
                                                exception(8, pc0, srh);
                                                clk = 34;
                                                break op_root_switch;
                                            }
                                            usp = r[8 + rrr];
                                            clk = 4;
                                        } else if (mmm == 5) {
                                            if ((srh & 8192) == 0) {
                                                exception(8, pc0, srh);
                                                clk = 34;
                                                break op_root_switch;
                                            }
                                            r[8 + rrr] = usp;
                                            clk = 4;
                                        } else if (ea == 54) {
                                            if ((ccr & 2) != 0) {
                                                exception(7, pc, srh);
                                                clk = 34;
                                                break op_root_switch;
                                            } else {
                                                clk = 4;
                                            }
                                        } else if (ea == 48) {
                                            if ((srh & 8192) == 0) {
                                                exception(8, pc0, srh);
                                                clk = 34;
                                                break op_root_switch;
                                            }
                                            bus_reset();
                                            clk = 132;
                                        } else if (ea == 50) {
                                            if ((srh & 8192) == 0) {
                                                exception(8, pc0, srh);
                                                clk = 34;
                                                break op_root_switch;
                                            }
                                            short sr = read_short_big((pc += 2) - 2);
                                            if ((sr & 8192) == 0) {
                                                ssp = r[15];
                                                r[15] = usp;
                                                fc2 = sr >>> 13 & 1;
                                                current_read_mmap = fc2 == 0 ? read_user_mmap : read_supervisor_mmap;
                                                current_write_mmap = fc2 == 0 ? write_user_mmap : write_supervisor_mmap;
                                            }
                                            srh = (short) (sr & 42752);
                                            ccr = (byte) (sr & 31);
                                            {
                                                int new_ipl_8 = sr & 1792;
                                                switch (new_ipl_8) {
                                                case 1536:
                                                    if (ipl_8 < 1792) {
                                                        break;
                                                    }
                                                    if (interrupt_vector_system_port >= 0) {
                                                        system_port.done(interrupt_vector_system_port);
                                                        interrupt_vector_system_port = -1;
                                                    }
                                                case 1280:
                                                    if (ipl_8 < 1536) {
                                                        break;
                                                    }
                                                    if (interrupt_vector_mfp >= 0) {
                                                        mfp.done(interrupt_vector_mfp);
                                                        interrupt_vector_mfp = -1;
                                                    }
                                                case 1024:
                                                    if (ipl_8 < 1280) {
                                                        break;
                                                    }
                                                    if (interrupt_vector_scc >= 0) {
                                                        scc.done(interrupt_vector_scc);
                                                        interrupt_vector_scc = -1;
                                                    }
                                                case 768:
                                                case 512:
                                                    if (ipl_8 < 768) {
                                                        break;
                                                    }
                                                    if (interrupt_vector_dmac >= 0) {
                                                        dmac.done(interrupt_vector_dmac);
                                                        interrupt_vector_dmac = -1;
                                                    }
                                                case 256:
                                                case 0:
                                                    if (ipl_8 < 256) {
                                                        break;
                                                    }
                                                    if (interrupt_vector_int1 >= 0) {
                                                        int1.done(interrupt_vector_int1);
                                                        interrupt_vector_int1 = -1;
                                                    }
                                                }
                                                ipl_8 = new_ipl_8;
                                            }
                                            clk = 4;
                                            break op_root_switch;
                                        } else {
                                            exception(4, pc0, srh);
                                            clk = 34;
                                        }
                                    } else if (ss == 2) {
                                        int a = c_ea0(ea);
                                        write_int_big(r[15] -= 4, pc);
                                        {
                                            int tmp_a = a;
                                            pc = (tmp_a & 1) != 0 ? address_error_on_read(tmp_a) : tmp_a;
                                        }
                                        clk = (mmm == 2 || mmm == 6 || ea == 59 ? 12 : mmm == 5 || ea == 56 || ea == 58 ? 10 : 8) + elk;
                                    } else {
                                        {
                                            int tmp_a = c_ea0(ea);
                                            pc = (tmp_a & 1) != 0 ? address_error_on_read(tmp_a) : tmp_a;
                                        }
                                        clk = (mmm == 2 || mmm == 6 || ea == 59 ? 4 : mmm == 5 || ea == 56 || ea == 58 ? 2 : 0) + elk;
                                    }
                                    break;
                                }
                            } else if (ss == 3) {
                                r[8 + qqq] = c_ea0(ea);
                                clk = (mmm == 6 || ea == 59 ? 10 : 8) + elk;
                            } else {
                                short src;
                                if (mmm < 2) {
                                    src = (short) r[rrr];
                                } else {
                                    int a = c_ea2(ea);
                                    src = (short) ((a & 1) != 0 ? address_error_on_read(a) : read_short_big(a));
                                }
                                short dst = (short) r[qqq];
                                if (dst < 0 || dst > src) {
                                    ccr = (byte) ((ccr & 16) + (dst < 0 ? 8 : 0));
                                    exception(6, pc, srh);
                                    clk = 40 + elk;
                                    break op_root_switch;
                                } else {
                                    clk = 10 + elk;
                                }
                            }
                            break;
                        case 5:
                            ea = (short) (op & 63);
                            rrr = (short) (ea & 7);
                            mmm = (short) (ea >> 3);
                            ss = (short) (op >> 6 & 3);
                            qqq = (short) (op >> 9 & 7);
                            sz = (short) (ss == 0 ? 1 : ss == 1 ? 2 : 4);
                            if (ss < 3) {
                                int src = (qqq - 1 & 7) + 1;
                                if ((op & 256) == 0) {
                                    if (mmm == 0) {
                                        d_add(rrr, r[rrr], src);
                                        clk = sz < 4 ? 4 : 8;
                                    } else if (mmm == 1) {
                                        r[ea] += src;
                                        clk = 8;
                                    } else {
                                        int a = c_ea(ea, sz);
                                        m_add(a, ss == 0 ? read_byte(a) : (a & 1) != 0 ? address_error_on_read(a) : ss == 1 ? read_short_big(a) : read_int_big(a), src);
                                        clk = (sz < 4 ? 8 : 12) + elk;
                                    }
                                } else {
                                    if (mmm == 0) {
                                        d_sub(rrr, r[rrr], src);
                                        clk = sz < 4 ? 4 : 8;
                                    } else if (mmm == 1) {
                                        r[ea] -= src;
                                        clk = 8;
                                    } else {
                                        int a = c_ea(ea, sz);
                                        m_sub(a, ss == 0 ? read_byte(a) : (a & 1) != 0 ? address_error_on_read(a) : ss == 1 ? read_short_big(a) : read_int_big(a), src);
                                        clk = (sz < 4 ? 8 : 12) + elk;
                                    }
                                }
                            } else if (mmm == 0) {
                                if (ccmap[((op & 3840) >> 3) + ccr]) {
                                    r[rrr] |= 255;
                                    clk = 6;
                                } else {
                                    r[rrr] &= -256;
                                    clk = 4;
                                }
                            } else if (mmm == 1) {
                                short d = read_short_big((pc += 2) - 2);
                                if (ccmap[((op & 3840) >> 3) + ccr]) {
                                    clk = 12;
                                } else {
                                    r[rrr] = (r[rrr] & -65536) + ((short) (r[rrr] - 1) & 65535);
                                    if ((short) r[rrr] == -1) {
                                        clk = 14;
                                    } else {
                                        {
                                            int tmp_a = pc0 + 2 + d;
                                            pc = (tmp_a & 1) != 0 ? address_error_on_read(tmp_a) : tmp_a;
                                        }
                                        clk = 10;
                                    }
                                }
                            } else {
                                write_byte(c_ea1(ea), (byte) (ccmap[((op & 3840) >> 3) + ccr] ? -1 : 0));
                                clk = 8 + elk;
                            }
                            break;
                        case 6: {
                            int offset = (byte) op;
                            if (offset == 0) {
                                offset = read_short_big((pc += 2) - 2);
                                clk = 12;
                            } else {
                                clk = 8;
                            }
                            if (ccmap[((op & 3840) >> 3) + ccr]) {
                                {
                                    int tmp_a = pc0 + 2 + offset;
                                    pc = (tmp_a & 1) != 0 ? address_error_on_read(tmp_a) : tmp_a;
                                }
                                clk = 10;
                            } else if ((op & 3840) == 256) {
                                write_int_big(r[15] -= 4, pc);
                                {
                                    int tmp_a = pc0 + 2 + offset;
                                    pc = (tmp_a & 1) != 0 ? address_error_on_read(tmp_a) : tmp_a;
                                }
                                clk = 18;
                            }
                        }
                            break;
                        case 7: {
                            int src = (byte) op;
                            r[op >> 9 & 7] = src;
                            ccr = (byte) ((ccr & 16) + (src < 0 ? 8 : 0) + (src == 0 ? 4 : 0));
                            clk = 4;
                        }
                            break;
                        case 8:
                            ea = (short) (op & 63);
                            rrr = (short) (ea & 7);
                            mmm = (short) (ea >> 3);
                            ss = (short) (op >> 6 & 3);
                            qqq = (short) (op >> 9 & 7);
                            sz = (short) (ss == 0 ? 1 : ss == 1 ? 2 : 4);
                            if (ss < 3) {
                                if ((op & 256) == 0) {
                                    if (mmm < 2) {
                                        d_or(qqq, r[qqq], r[ea]);
                                        clk = sz < 4 ? 4 : 8;
                                    } else {
                                        int a = c_ea(ea, sz);
                                        d_or(qqq, r[qqq], ss == 0 ? read_byte(a) : (a & 1) != 0 ? address_error_on_read(a) : ss == 1 ? read_short_big(a) : read_int_big(a));
                                        clk = (sz < 4 ? 4 : ea == 60 ? 8 : 6) + elk;
                                    }
                                } else if (mmm == 0) {
                                    r[qqq] = (r[qqq] & -256) + (sbcd((byte) r[qqq], (byte) r[rrr]) & 255);
                                    clk = 6;
                                } else if (mmm == 1) {
                                    byte src = read_byte(--r[ea]);
                                    int a = --r[8 + qqq];
                                    write_byte(a, sbcd(read_byte(a), src));
                                    clk = 18;
                                } else {
                                    int a = c_ea(ea, sz);
                                    m_or(a, ss == 0 ? read_byte(a) : (a & 1) != 0 ? address_error_on_read(a) : ss == 1 ? read_short_big(a) : read_int_big(a), r[qqq]);
                                    clk = (sz < 4 ? 8 : 12) + elk;
                                }
                            } else {
                                long dividend = r[qqq];
                                elk = 0;
                                long divisor;
                                if (mmm < 2) {
                                    divisor = (short) r[rrr];
                                } else {
                                    int a = c_ea2(ea);
                                    divisor = (short) ((a & 1) != 0 ? address_error_on_read(a) : read_short_big(a));
                                }
                                long quotient, remainder;
                                if (divisor == 0) {
                                    exception(5, pc0, srh);
                                    clk = 38 + elk;
                                    break op_root_switch;
                                }
                                if ((op & 256) == 0) {
                                    if (dividend < 0) {
                                        dividend += 4294967296L;
                                    }
                                    if (divisor < 0) {
                                        divisor += 65536;
                                    }
                                    quotient = dividend / divisor;
                                    if (quotient >= 65536) {
                                        ccr = (byte) ((ccr & 16) + 2);
                                        break op_root_switch;
                                    }
                                    clk = 140 + elk;
                                } else {
                                    quotient = dividend / divisor;
                                    if ((short) quotient != quotient) {
                                        ccr = (byte) ((ccr & 16) + 2);
                                        break op_root_switch;
                                    }
                                    clk = 158 + elk;
                                }
                                remainder = dividend - divisor * quotient;
                                r[qqq] = (int) ((remainder << 16) + (quotient & 65535));
                                ccr = (byte) ((ccr & 16) + (quotient < 0 ? 8 : 0) + (quotient == 0 ? 4 : 0));
                            }
                            break;
                        case 9:
                            ea = (short) (op & 63);
                            rrr = (short) (ea & 7);
                            mmm = (short) (ea >> 3);
                            ss = (short) (op >> 6 & 3);
                            qqq = (short) (op >> 9 & 7);
                            sz = (short) (ss == 0 ? 1 : ss == 1 ? 2 : 4);
                            if (ss < 3) {
                                if ((op & 256) == 0) {
                                    if (mmm < 2) {
                                        d_sub(qqq, r[qqq], r[ea]);
                                        clk = sz < 4 ? 4 : 8;
                                    } else {
                                        int a = c_ea(ea, sz);
                                        d_sub(qqq, r[qqq], ss == 0 ? read_byte(a) : (a & 1) != 0 ? address_error_on_read(a) : ss == 1 ? read_short_big(a) : read_int_big(a));
                                        clk = (ea == 60 ? 8 : 6) + elk;
                                    }
                                } else if (mmm == 0) {
                                    d_subx(qqq, r[qqq], r[rrr]);
                                    clk = sz < 4 ? 4 : 8;
                                } else if (mmm == 1) {
                                    int a = r[ea] -= sz;
                                    int src = ss == 0 ? read_byte(a) : (a & 1) != 0 ? address_error_on_read(a) : ss == 1 ? read_short_big(a) : read_int_big(a);
                                    a = r[8 + qqq] -= sz;
                                    m_subx(a, ss == 0 ? read_byte(a) : (a & 1) != 0 ? address_error_on_read(a) : ss == 1 ? read_short_big(a) : read_int_big(a), src);
                                    clk = sz < 4 ? 18 : 30;
                                } else {
                                    int a = c_ea(ea, sz);
                                    m_sub(a, ss == 0 ? read_byte(a) : (a & 1) != 0 ? address_error_on_read(a) : ss == 1 ? read_short_big(a) : read_int_big(a), r[qqq]);
                                    clk = (sz < 4 ? 8 : 12) + elk;
                                }
                            } else {
                                if (mmm < 2) {
                                    r[8 + qqq] -= (op & 256) == 0 ? (short) r[ea] : r[ea];
                                    clk = 8;
                                } else if ((op & 256) == 0) {
                                    int a = c_ea2(ea);
                                    r[8 + qqq] -= (short) ((a & 1) != 0 ? address_error_on_read(a) : read_short_big(a));
                                    clk = 8 + elk;
                                } else {
                                    int a = c_ea4(ea);
                                    r[8 + qqq] -= (a & 1) != 0 ? address_error_on_read(a) : read_int_big(a);
                                    clk = (ea == 60 ? 6 : 8) + elk;
                                }
                            }
                            break;
                        case 10:
                            exception(10, pc0, srh);
                            clk = 34;
                            break;
                        case 11:
                            ea = (short) (op & 63);
                            rrr = (short) (ea & 7);
                            mmm = (short) (ea >> 3);
                            ss = (short) (op >> 6 & 3);
                            qqq = (short) (op >> 9 & 7);
                            sz = (short) (ss == 0 ? 1 : ss == 1 ? 2 : 4);
                            if (ss < 3) {
                                if ((op & 256) == 0) {
                                    elk = 0;
                                    int src;
                                    if (mmm < 2) {
                                        src = r[ea];
                                    } else {
                                        int a = c_ea(ea, sz);
                                        src = ss == 0 ? read_byte(a) : (a & 1) != 0 ? address_error_on_read(a) : ss == 1 ? read_short_big(a) : read_int_big(a);
                                    }
                                    ccr_cmp(r[qqq], src, r[qqq] - src);
                                    clk = (sz < 4 ? 4 : 6) + elk;
                                } else if (mmm == 0) {
                                    d_eor(rrr, r[rrr], r[qqq]);
                                    clk = sz < 4 ? 4 : 8;
                                } else if (mmm == 1) {
                                    int src = ss == 0 ? read_byte(r[ea]) : (r[ea] & 1) != 0 ? address_error_on_read(r[ea]) : ss == 1 ? read_short_big(r[ea]) : read_int_big(r[ea]);
                                    r[ea] += sz;
                                    int dst = ss == 0 ? read_byte(r[8 + qqq]) : (r[8 + qqq] & 1) != 0 ? address_error_on_read(r[8 + qqq]) : ss == 1 ? read_short_big(r[8 + qqq]) : read_int_big(r[8 + qqq]);
                                    r[8 + qqq] += sz;
                                    ccr_cmp(dst, src, dst - src);
                                    clk = sz < 4 ? 12 : 20;
                                } else {
                                    int a = c_ea(ea, sz);
                                    m_eor(a, ss == 0 ? read_byte(a) : (a & 1) != 0 ? address_error_on_read(a) : ss == 1 ? read_short_big(a) : read_int_big(a), r[qqq]);
                                    clk = sz < 4 ? 8 : 12;
                                }
                            } else {
                                elk = 0;
                                int src;
                                if (mmm < 2) {
                                    src = (op & 256) == 0 ? (short) r[ea] : r[ea];
                                } else {
                                    if ((op & 256) == 0) {
                                        int a = c_ea2(ea);
                                        src = (short) ((a & 1) != 0 ? address_error_on_read(a) : read_short_big(a));
                                    } else {
                                        int a = c_ea4(ea);
                                        src = (a & 1) != 0 ? address_error_on_read(a) : read_int_big(a);
                                    }
                                }
                                ss = 2;
                                sz = 4;
                                ccr_cmp(r[8 + qqq], src, r[8 + qqq] - src);
                                clk = 6 + elk;
                            }
                            break;
                        case 12:
                            ea = (short) (op & 63);
                            rrr = (short) (ea & 7);
                            mmm = (short) (ea >> 3);
                            ss = (short) (op >> 6 & 3);
                            qqq = (short) (op >> 9 & 7);
                            sz = (short) (ss == 0 ? 1 : ss == 1 ? 2 : 4);
                            if (ss < 3) {
                                if ((op & 256) == 0) {
                                    if (mmm < 2) {
                                        d_and(qqq, r[qqq], r[ea]);
                                        clk = sz < 4 ? 4 : 8;
                                    } else {
                                        int a = c_ea(ea, sz);
                                        d_and(qqq, r[qqq], ss == 0 ? read_byte(a) : (a & 1) != 0 ? address_error_on_read(a) : ss == 1 ? read_short_big(a) : read_int_big(a));
                                        clk = (sz < 4 ? 4 : ea == 60 ? 8 : 6) + elk;
                                    }
                                } else if (mmm < 2) {
                                    if (ss == 0) {
                                        if (mmm == 0) {
                                            r[qqq] = (r[qqq] & -256) + (abcd((byte) r[qqq], (byte) r[rrr]) & 255);
                                            clk = 6;
                                        } else {
                                            byte src = read_byte(--r[ea]);
                                            int a = --r[8 + qqq];
                                            write_byte(a, abcd(read_byte(a), src));
                                            clk = 18;
                                        }
                                    } else if (ss == 1) {
                                        if (mmm == 0) {
                                            int t = r[qqq];
                                            r[qqq] = r[rrr];
                                            r[rrr] = t;
                                        } else {
                                            int t = r[8 + qqq];
                                            r[8 + qqq] = r[ea];
                                            r[ea] = t;
                                        }
                                        clk = 6;
                                    } else {
                                        int t = r[qqq];
                                        r[qqq] = r[ea];
                                        r[ea] = t;
                                        clk = 6;
                                    }
                                } else {
                                    int a = c_ea(ea, sz);
                                    m_and(a, ss == 0 ? read_byte(a) : (a & 1) != 0 ? address_error_on_read(a) : ss == 1 ? read_short_big(a) : read_int_big(a), r[qqq]);
                                    clk = (sz < 4 ? 8 : 12) + elk;
                                }
                            } else {
                                long multiplicand = (short) r[qqq];
                                long multiplier;
                                if (mmm < 2) {
                                    multiplier = (short) r[rrr];
                                } else {
                                    int a = c_ea2(ea);
                                    multiplier = (short) ((a & 1) != 0 ? address_error_on_read(a) : read_short_big(a));
                                }
                                if ((op & 256) == 0) {
                                    if (multiplicand < 0) {
                                        multiplicand += 65536;
                                    }
                                    if (multiplier < 0) {
                                        multiplier += 65536;
                                    }
                                }
                                int src = (int) (multiplicand * multiplier);
                                r[qqq] = src;
                                ccr = (byte) ((ccr & 16) + (src < 0 ? 8 : 0) + (src == 0 ? 4 : 0));
                                clk = 70 + elk;
                            }
                            break;
                        case 13:
                            ea = (short) (op & 63);
                            rrr = (short) (ea & 7);
                            mmm = (short) (ea >> 3);
                            ss = (short) (op >> 6 & 3);
                            qqq = (short) (op >> 9 & 7);
                            sz = (short) (ss == 0 ? 1 : ss == 1 ? 2 : 4);
                            if (ss < 3) {
                                if ((op & 256) == 0) {
                                    if (mmm < 2) {
                                        d_add(qqq, r[qqq], r[ea]);
                                        clk = sz < 4 ? 4 : 8;
                                    } else {
                                        int a = c_ea(ea, sz);
                                        d_add(qqq, r[qqq], ss == 0 ? read_byte(a) : (a & 1) != 0 ? address_error_on_read(a) : ss == 1 ? read_short_big(a) : read_int_big(a));
                                        clk = (ea == 60 ? 8 : 6) + elk;
                                    }
                                } else if (mmm == 0) {
                                    d_addx(qqq, r[qqq], r[rrr]);
                                    clk = sz < 4 ? 4 : 8;
                                } else if (mmm == 1) {
                                    int a = r[ea] -= sz;
                                    int src = ss == 0 ? read_byte(a) : (a & 1) != 0 ? address_error_on_read(a) : ss == 1 ? read_short_big(a) : read_int_big(a);
                                    a = r[8 + qqq] -= sz;
                                    m_addx(a, ss == 0 ? read_byte(a) : (a & 1) != 0 ? address_error_on_read(a) : ss == 1 ? read_short_big(a) : read_int_big(a), src);
                                    clk = sz < 4 ? 18 : 30;
                                } else {
                                    int a = c_ea(ea, sz);
                                    m_add(a, ss == 0 ? read_byte(a) : (a & 1) != 0 ? address_error_on_read(a) : ss == 1 ? read_short_big(a) : read_int_big(a), r[qqq]);
                                    clk = (sz < 4 ? 8 : 12) + elk;
                                }
                            } else {
                                if (mmm < 2) {
                                    r[8 + qqq] += (op & 256) == 0 ? (short) r[ea] : r[ea];
                                    clk = 8;
                                } else if ((op & 256) == 0) {
                                    int a = c_ea2(ea);
                                    r[8 + qqq] += (short) ((a & 1) != 0 ? address_error_on_read(a) : read_short_big(a));
                                    clk = 8 + elk;
                                } else {
                                    int a = c_ea4(ea);
                                    r[8 + qqq] += (a & 1) != 0 ? address_error_on_read(a) : read_int_big(a);
                                    clk = (ea == 60 ? 6 : 8) + elk;
                                }
                            }
                            break;
                        case 14:
                            ea = (short) (op & 63);
                            rrr = (short) (ea & 7);
                            mmm = (short) (ea >> 3);
                            ss = (short) (op >> 6 & 3);
                            qqq = (short) (op >> 9 & 7);
                            sz = (short) (ss == 0 ? 1 : ss == 1 ? 2 : 4);
                            if (ss < 3) {
                                int count = mmm < 4 ? (qqq - 1 & 7) + 1 : r[qqq] & 63;
                                clk = (sz < 4 ? 6 : 8) + (count << 1);
                                r[rrr] = ss == 0 ? (r[rrr] & -256) + (sftrot(mmm & 3, (op & 256) >> 8, r[rrr], count, (sz << 3) - 1) & 255) : ss == 1 ? (r[rrr] & -65536) + (sftrot(mmm & 3, (op & 256) >> 8, r[rrr], count, (sz << 3) - 1) & 65535) : sftrot(mmm & 3, (op & 256) >> 8, r[rrr], count, (sz << 3) - 1);
                            } else {
                                int a = c_ea2(ea);
                                clk = 8 + elk;
                                write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) sftrot(qqq & 3, (op & 256) >> 8, (short) ((a & 1) != 0 ? address_error_on_read(a) : read_short_big(a)), 1, 15));
                            }
                            break;
                        default:
                            exception(11, pc0, srh);
                        }
                        if (srh0 < 0) {
                            exception(9, pc, srh);
                            clk += 34;
                        }
                        clock_count += clock_unit * clk;
                        if (clock_count >= crtc_interrupt_clock) {
                            crtc.tick();
                        }
                        if (clock_count >= mfp_interrupt_clock) {
                            mfp.tick();
                        }
                        if (clock_count >= sound_interrupt_clock) {
                            sound.tick();
                        }
                        x68000_acknowledge_block: {
                            int request, vector;
                            short save_srh;
                            if (ipl_8 < 1792) {
                                request = interrupt_request_system_port;
                                if (request != interrupt_acknowledged_system_port) {
                                    interrupt_acknowledged_system_port = request;
                                    vector = system_port.acknowledge();
                                    if (vector != 0) {
                                        interrupt_vector_system_port = vector;
                                        save_srh = srh;
                                        srh = (short) ((srh & -1793) + 1792);
                                        ipl_8 = 1792;
                                        exception(vector, pc, save_srh);
                                        clock_count += clock_unit * 44;
                                        break x68000_acknowledge_block;
                                    }
                                }
                                if (ipl_8 < 1536) {
                                    request = interrupt_request_mfp;
                                    if (request != interrupt_acknowledged_mfp) {
                                        interrupt_acknowledged_mfp = request;
                                        vector = mfp.acknowledge();
                                        if (vector != 0) {
                                            interrupt_vector_mfp = vector;
                                            save_srh = srh;
                                            srh = (short) ((srh & -1793) + 1536);
                                            ipl_8 = 1536;
                                            exception(vector, pc, save_srh);
                                            clock_count += clock_unit * 44;
                                            break x68000_acknowledge_block;
                                        }
                                    }
                                    if (ipl_8 < 1280) {
                                        request = interrupt_request_scc;
                                        if (request != interrupt_acknowledged_scc) {
                                            interrupt_acknowledged_scc = request;
                                            vector = scc.acknowledge();
                                            if (vector != 0) {
                                                interrupt_vector_scc = vector;
                                                save_srh = srh;
                                                srh = (short) ((srh & -1793) + 1280);
                                                ipl_8 = 1280;
                                                exception(vector, pc, save_srh);
                                                clock_count += clock_unit * 44;
                                                break x68000_acknowledge_block;
                                            }
                                        }
                                        if (ipl_8 < 768) {
                                            request = interrupt_request_dmac;
                                            if (request != interrupt_acknowledged_dmac) {
                                                interrupt_acknowledged_dmac = request;
                                                vector = dmac.acknowledge();
                                                if (vector != 0) {
                                                    interrupt_vector_dmac = vector;
                                                    save_srh = srh;
                                                    srh = (short) ((srh & -1793) + 768);
                                                    ipl_8 = 768;
                                                    exception(vector, pc, save_srh);
                                                    clock_count += clock_unit * 44;
                                                    break x68000_acknowledge_block;
                                                }
                                            }
                                            if (ipl_8 < 256) {
                                                request = interrupt_request_int1;
                                                if (request != interrupt_acknowledged_int1) {
                                                    interrupt_acknowledged_int1 = request;
                                                    vector = int1.acknowledge();
                                                    if (vector != 0) {
                                                        interrupt_vector_int1 = vector;
                                                        save_srh = srh;
                                                        srh = (short) ((srh & -1793) + 256);
                                                        ipl_8 = 256;
                                                        exception(vector, pc, save_srh);
                                                        clock_count += clock_unit * 44;
                                                        break x68000_acknowledge_block;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (MC68000Exception e) {
                    if (e.value <= 3) {
                        try {
                            short temp_srh = srh;
                            exception(e.value, pc, temp_srh);
                            write_short_big(r[15] -= 2, op);
                            write_int_big(r[15] -= 4, e.value == 2 ? bus_error_address : address_error_address);
                            write_short_big(r[15] -= 2, (short) ((e.rw << 4) + (temp_srh >> 11 & 4) + 9));
                        } catch (MC68000Exception e2) {
                            halt = true;
                            break;
                        }
                    } else {
                        try {
                            exception(e.value, pc, srh);
                        } catch (MC68000Exception e1) {
                            try {
                                exception(e1.value, pc, srh);
                                write_short_big(r[15] -= 2, op);
                                write_int_big(r[15] -= 4, e1.value == 2 ? bus_error_address : address_error_address);
                                write_short_big(r[15] -= 2, (short) ((e1.rw << 4) + 13));
                            } catch (MC68000Exception e2) {
                                halt = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void exception(int vector_number, int save_pc, short save_srh) throws MC68000Exception {
        if ((srh & 8192) == 0) {
            usp = r[15];
            r[15] = ssp;
            srh |= 8192;
            fc2 = srh >>> 13 & 1;
            current_read_mmap = fc2 == 0 ? read_user_mmap : read_supervisor_mmap;
            current_write_mmap = fc2 == 0 ? write_user_mmap : write_supervisor_mmap;
        }
        srh &= -32769;
        write_int_big(r[15] -= 4, save_pc);
        write_short_big(r[15] -= 2, (short) (save_srh + ccr));
        int a = (vector_number & 255) << 2;
        {
            int tmp_a = (a & 1) != 0 ? address_error_on_read(a) : read_int_big(a);
            pc = (tmp_a & 1) != 0 ? address_error_on_read(tmp_a) : tmp_a;
        }
    }

    private int sftrot(int mode, int direction, int data, int count, int msbb) {
        int msbm = 1 << msbb;
        data = ss == 0 ? (byte) data : ss == 1 ? (short) data : data;
        switch ((mode << 1) + direction) {
        case 0:
            if (count > 0) {
                data >>= count - 1;
                ccr = (byte) ((data & 1) != 0 ? 17 : 0);
                data >>= 1;
            }
            break;
        case 1:
            if (count > 0) {
                ccr = (byte) ((data + (msbm >>> count) & -msbm >> count - 1) != 0 ? 2 : 0);
                data <<= count - 1;
                ccr += (data & msbm) != 0 ? 17 : 0;
                data <<= 1;
            }
            break;
        case 2:
            if (count > 0) {
                data = (data & msbm + msbm - 1) >>> count - 1;
                ccr = (byte) ((data & 1) != 0 ? 17 : 0);
                data = ss == 0 ? (byte) (data >>> 1) : ss == 1 ? (short) (data >>> 1) : data >>> 1;
            }
            break;
        case 3:
            if (count > 0) {
                data <<= count - 1;
                ccr = (byte) ((data & msbm) != 0 ? 17 : 0);
                data = ss == 0 ? (byte) (data << 1) : ss == 1 ? (short) (data << 1) : data << 1;
            }
            break;
        case 4: {
            int c = ccr & 16, x = c;
            for (; count != 0; count--) {
                x = data & 1;
                data = (data >> 1 & ~msbm) + (c != 0 ? msbm : 0);
                c = x;
            }
            ccr = (byte) ((x != 0 ? 16 : 0) + (c != 0 ? 1 : 0));
            data = ss == 0 ? (byte) data : ss == 1 ? (short) data : data;
        }
            break;
        case 5: {
            int c = ccr & 16, x = c;
            for (; count != 0; count--) {
                x = data & msbm;
                data = (data << 1) + (c != 0 ? 1 : 0);
                c = x;
            }
            ccr = (byte) ((x != 0 ? 16 : 0) + (c != 0 ? 1 : 0));
            data = ss == 0 ? (byte) data : ss == 1 ? (short) data : data;
        }
            break;
        case 6:
            if (count > 0) {
                count &= msbb;
                if (count != 0) {
                    data &= msbm + msbm - 1;
                    data = ss == 0 ? (byte) ((data << msbb + 1 - count) + (data >>> count)) : ss == 1 ? (short) ((data << msbb + 1 - count) + (data >>> count)) : (data << msbb + 1 - count) + (data >>> count);
                }
                ccr = (byte) ((ccr & 16) + ((data & msbm) != 0 ? 1 : 0));
            }
            break;
        case 7:
            if (count > 0) {
                count &= msbb;
                if (count != 0) {
                    data &= msbm + msbm - 1;
                    data = ss == 0 ? (byte) ((data << count) + (data >>> msbb + 1 - count)) : ss == 1 ? (short) ((data << count) + (data >>> msbb + 1 - count)) : (data << count) + (data >>> msbb + 1 - count);
                }
                ccr = (byte) ((ccr & 16) + (data & 1));
            }
            break;
        }
        ccr += (data < 0 ? 8 : 0) + (data == 0 ? 4 : 0);
        return data;
    }

    final private static boolean ccmap[] = {
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, true, false, false, false, false, false, true, false, true, false, false,
        false, false, false, true, false, true, false, false, false, false, false, true, false, true, false, false, false, false, false, false, true, false, true, true, true, true, true, false, true, false, true, true, true, true, true, false, true, false, true, true, true, true, true, false, true, false, true, true, true, true, true, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true,
        false, true, false, true, false, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, true, true, true, true, false, false, false, false, true, true, true, true, false, false, false, false, true, true, true, true, false, false, false, false, true, true, true, true, false, false, false, false, false, false, false, false, true, true, true, true,
        false, false, false, false, true, true, true, true, false, false, false, false, true, true, true, true, false, false, false, false, true, true, true, true, true, true, false, false, true, true, false, false, true, true, false, false, true, true, false, false, true, true, false, false, true, true, false, false, true, true, false, false, true, true, false, false, false, false, true, true, false, false, true, true, false, false, true, true, false, false, true, true, false, false, true, true, false,
        false, true, true, false, false, true, true, false, false, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true,
        false, false, true, true, false, false, false, false, true, true, false, false, true, true, true, true, false, false, true, true, false, false, false, false, true, true, false, false, true, true, false, false, true, true, false, false, true, true, true, true, false, false, true, true, false, false, false, false, true, true, false, false, true, true, true, true, false, false, true, true, false, false, true, true, false, false, false, false, false, false, false, false, true, true, false, false,
        false, false, true, true, false, false, false, false, false, false, false, false, true, true, false, false, false, false, false, false, true, true, true, true, true, true, true, true, false, false, true, true, true, true, false, false, true, true, true, true, true, true, true, true, false, false, true, true, true, true
    };

    private void ccr_sub(int x, int y, int z) {
        if (ss == 0) {
            x = (byte) x;
            y = (byte) y;
            z = (byte) z;
        } else if (ss == 1) {
            x = (short) x;
            y = (short) y;
            z = (short) z;
        }
        ccr = (byte) ((z < 0 ? 8 : 0) + (z == 0 ? 4 : 0) + ((x ^ y) < 0 && (x ^ z) < 0 ? 2 : 0) + (x >= 0 && y < 0 || z < 0 && (x ^ y) >= 0 ? 17 : 0));
    }

    private void ccr_subx(int x, int y, int z) {
        if (ss == 0) {
            x = (byte) x;
            y = (byte) y;
            z = (byte) z;
        } else if (ss == 1) {
            x = (short) x;
            y = (short) y;
            z = (short) z;
        }
        ccr = (byte) ((z < 0 ? 8 : 0) + (z == 0 ? ccr & 4 : 0) + ((x ^ y) < 0 && (x ^ z) < 0 ? 2 : 0) + (x >= 0 && y < 0 || z < 0 && (x ^ y) >= 0 ? 17 : 0));
    }

    private void ccr_cmp(int x, int y, int z) {
        if (ss == 0) {
            x = (byte) x;
            y = (byte) y;
            z = (byte) z;
        } else if (ss == 1) {
            x = (short) x;
            y = (short) y;
            z = (short) z;
        }
        ccr = (byte) ((ccr & 16) + (z < 0 ? 8 : 0) + (z == 0 ? 4 : 0) + ((x ^ y) < 0 && (x ^ z) < 0 ? 2 : 0) + (x >= 0 && y < 0 || z < 0 && (x ^ y) >= 0 ? 1 : 0));
    }

    private void ccr_add(int x, int y, int z) {
        if (ss == 0) {
            x = (byte) x;
            y = (byte) y;
            z = (byte) z;
        } else if (ss == 1) {
            x = (short) x;
            y = (short) y;
            z = (short) z;
        }
        ccr = (byte) ((z < 0 ? 8 : 0) + (z == 0 ? 4 : 0) + ((x ^ y) >= 0 && (x ^ z) < 0 ? 2 : 0) + (x < 0 && y < 0 || z >= 0 && (x ^ y) < 0 ? 17 : 0));
    }

    private void ccr_addx(int x, int y, int z) {
        if (ss == 0) {
            x = (byte) x;
            y = (byte) y;
            z = (byte) z;
        } else if (ss == 1) {
            x = (short) x;
            y = (short) y;
            z = (short) z;
        }
        ccr = (byte) ((z < 0 ? 8 : 0) + (z == 0 ? ccr & 4 : 0) + ((x ^ y) >= 0 && (x ^ z) < 0 ? 2 : 0) + (x < 0 && y < 0 || z >= 0 && (x ^ y) < 0 ? 17 : 0));
    }

    private void d_or(int n, int x, int y) {
        int z;
        if (ss == 0) {
            z = (byte) (x | y);
            r[n] = (r[n] & -256) + (z & 255);
        } else if (ss == 1) {
            z = (short) (x | y);
            r[n] = (r[n] & -65536) + (z & 65535);
        } else {
            z = x | y;
            r[n] = z;
        }
        ccr = (byte) ((ccr & 16) + (z < 0 ? 8 : 0) + (z == 0 ? 4 : 0));
    }

    private void m_or(int a, int x, int y) throws MC68000Exception {
        int z;
        if (ss == 0) {
            z = (byte) (x | y);
            write_byte(a, (byte) z);
        } else if (ss == 1) {
            z = (short) (x | y);
            write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) z);
        } else {
            z = x | y;
            write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, z);
        }
        ccr = (byte) ((ccr & 16) + (z < 0 ? 8 : 0) + (z == 0 ? 4 : 0));
    }

    private void d_and(int n, int x, int y) {
        int z;
        if (ss == 0) {
            z = (byte) (x & y);
            r[n] = (r[n] & -256) + (z & 255);
        } else if (ss == 1) {
            z = (short) (x & y);
            r[n] = (r[n] & -65536) + (z & 65535);
        } else {
            z = x & y;
            r[n] = z;
        }
        ccr = (byte) ((ccr & 16) + (z < 0 ? 8 : 0) + (z == 0 ? 4 : 0));
    }

    private void m_and(int a, int x, int y) throws MC68000Exception {
        int z;
        if (ss == 0) {
            z = (byte) (x & y);
            write_byte(a, (byte) z);
        } else if (ss == 1) {
            z = (short) (x & y);
            write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) z);
        } else {
            z = x & y;
            write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, z);
        }
        ccr = (byte) ((ccr & 16) + (z < 0 ? 8 : 0) + (z == 0 ? 4 : 0));
    }

    private void d_eor(int n, int x, int y) {
        int z;
        if (ss == 0) {
            z = (byte) (x ^ y);
            r[n] = (r[n] & -256) + (z & 255);
        } else if (ss == 1) {
            z = (short) (x ^ y);
            r[n] = (r[n] & -65536) + (z & 65535);
        } else {
            z = x ^ y;
            r[n] = z;
        }
        ccr = (byte) ((ccr & 16) + (z < 0 ? 8 : 0) + (z == 0 ? 4 : 0));
    }

    private void m_eor(int a, int x, int y) throws MC68000Exception {
        int z;
        if (ss == 0) {
            z = (byte) (x ^ y);
            write_byte(a, (byte) z);
        } else if (ss == 1) {
            z = (short) (x ^ y);
            write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) z);
        } else {
            z = x ^ y;
            write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, z);
        }
        ccr = (byte) ((ccr & 16) + (z < 0 ? 8 : 0) + (z == 0 ? 4 : 0));
    }

    private void d_sub(int n, int x, int y) {
        int z = x - y;
        r[n] = ss == 0 ? (r[n] & -256) + (z & 255) : ss == 1 ? (r[n] & -65536) + (z & 65535) : z;
        ccr_sub(x, y, z);
    }

    private void m_sub(int a, int x, int y) throws MC68000Exception {
        int z = x - y;
        if (ss == 0) {
            write_byte(a, (byte) z);
        } else if (ss == 1) {
            write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) z);
        } else {
            write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, z);
        }
        ccr_sub(x, y, z);
    }

    private void d_add(int n, int x, int y) {
        int z = x + y;
        r[n] = ss == 0 ? (r[n] & -256) + (z & 255) : ss == 1 ? (r[n] & -65536) + (z & 65535) : z;
        ccr_add(x, y, z);
    }

    private void m_add(int a, int x, int y) throws MC68000Exception {
        int z = x + y;
        if (ss == 0) {
            write_byte(a, (byte) z);
        } else if (ss == 1) {
            write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) z);
        } else {
            write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, z);
        }
        ccr_add(x, y, z);
    }

    private void d_subx(int n, int x, int y) {
        int z = x - y - (ccr >> 4 & 1);
        r[n] = ss == 0 ? (r[n] & -256) + (z & 255) : ss == 1 ? (r[n] & -65536) + (z & 65535) : z;
        ccr_subx(x, y, z);
    }

    private void m_subx(int a, int x, int y) throws MC68000Exception {
        int z = x - y - (ccr >> 4 & 1);
        if (ss == 0) {
            write_byte(a, (byte) z);
        } else if (ss == 1) {
            write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) z);
        } else {
            write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, z);
        }
        ccr_subx(x, y, z);
    }

    private void d_addx(int n, int x, int y) {
        int z = x + y + (ccr >> 4 & 1);
        r[n] = ss == 0 ? (r[n] & -256) + (z & 255) : ss == 1 ? (r[n] & -65536) + (z & 65535) : z;
        ccr_addx(x, y, z);
    }

    private void m_addx(int a, int x, int y) throws MC68000Exception {
        int z = x + y + (ccr >> 4 & 1);
        if (ss == 0) {
            write_byte(a, (byte) z);
        } else if (ss == 1) {
            write_short_big((a & 1) != 0 ? address_error_on_write(a) : a, (short) z);
        } else {
            write_int_big((a & 1) != 0 ? address_error_on_write(a) : a, z);
        }
        ccr_addx(x, y, z);
    }

    private byte abcd(byte x, byte y) {
        int z0 = (x & 15) + (y & 15) + (ccr >> 4 & 1);
        int z1 = (x & 240) + (y & 240);
        ccr &= 4;
        if (z0 >= 10) {
            z0 = z0 - 10 & 15;
            z1 += 16;
        }
        if (z1 >= 160) {
            z1 = z1 - 160 & 240;
            ccr += 17;
        }
        z1 += z0;
        if (z1 != 0) {
            ccr &= -5;
        }
        return (byte) z1;
    }

    private byte sbcd(byte x, byte y) {
        int z0 = (x & 15) - (y & 15) - (ccr >> 4 & 1);
        int z1 = (x & 240) - (y & 240);
        ccr &= 4;
        if (z0 < 0) {
            z0 = z0 + 10 & 15;
            z1 -= 16;
        }
        if (z1 < 0) {
            z1 = z1 + 160 & 240;
            ccr += 17;
        }
        z1 += z0;
        if (z1 != 0) {
            ccr &= -5;
        }
        return (byte) z1;
    }

    private int pcew() throws MC68000Exception {
        short w = read_short_big((pc += 2) - 2);
        return (byte) w + ((w & 2048) == 0 ? (short) r[w >> 12 & 15] : r[w >> 12 & 15]);
    }

    private int c_ea(short ea, int sz) throws MC68000Exception {
        switch (ea) {
        case 16:
            elk = sz < 4 ? 4 : 8;
            return r[8];
        case 17:
            elk = sz < 4 ? 4 : 8;
            return r[9];
        case 18:
            elk = sz < 4 ? 4 : 8;
            return r[10];
        case 19:
            elk = sz < 4 ? 4 : 8;
            return r[11];
        case 20:
            elk = sz < 4 ? 4 : 8;
            return r[12];
        case 21:
            elk = sz < 4 ? 4 : 8;
            return r[13];
        case 22:
            elk = sz < 4 ? 4 : 8;
            return r[14];
        case 23:
            elk = sz < 4 ? 4 : 8;
            return r[15];
        case 24:
            elk = sz < 4 ? 4 : 8;
            return (r[8] += sz) - sz;
        case 25:
            elk = sz < 4 ? 4 : 8;
            return (r[9] += sz) - sz;
        case 26:
            elk = sz < 4 ? 4 : 8;
            return (r[10] += sz) - sz;
        case 27:
            elk = sz < 4 ? 4 : 8;
            return (r[11] += sz) - sz;
        case 28:
            elk = sz < 4 ? 4 : 8;
            return (r[12] += sz) - sz;
        case 29:
            elk = sz < 4 ? 4 : 8;
            return (r[13] += sz) - sz;
        case 30:
            elk = sz < 4 ? 4 : 8;
            return (r[14] += sz) - sz;
        case 31:
            elk = sz < 4 ? 4 : 8;
            sz = sz + 1 & -2;
            return (r[15] += sz) - sz;
        case 32:
            elk = sz < 4 ? 6 : 10;
            return r[8] -= sz;
        case 33:
            elk = sz < 4 ? 6 : 10;
            return r[9] -= sz;
        case 34:
            elk = sz < 4 ? 6 : 10;
            return r[10] -= sz;
        case 35:
            elk = sz < 4 ? 6 : 10;
            return r[11] -= sz;
        case 36:
            elk = sz < 4 ? 6 : 10;
            return r[12] -= sz;
        case 37:
            elk = sz < 4 ? 6 : 10;
            return r[13] -= sz;
        case 38:
            elk = sz < 4 ? 6 : 10;
            return r[14] -= sz;
        case 39:
            elk = sz < 4 ? 6 : 10;
            return r[15] -= sz + 1 & -2;
        case 40:
            elk = sz < 4 ? 8 : 12;
            return r[8] + read_short_big((pc += 2) - 2);
        case 41:
            elk = sz < 4 ? 8 : 12;
            return r[9] + read_short_big((pc += 2) - 2);
        case 42:
            elk = sz < 4 ? 8 : 12;
            return r[10] + read_short_big((pc += 2) - 2);
        case 43:
            elk = sz < 4 ? 8 : 12;
            return r[11] + read_short_big((pc += 2) - 2);
        case 44:
            elk = sz < 4 ? 8 : 12;
            return r[12] + read_short_big((pc += 2) - 2);
        case 45:
            elk = sz < 4 ? 8 : 12;
            return r[13] + read_short_big((pc += 2) - 2);
        case 46:
            elk = sz < 4 ? 8 : 12;
            return r[14] + read_short_big((pc += 2) - 2);
        case 47:
            elk = sz < 4 ? 8 : 12;
            return r[15] + read_short_big((pc += 2) - 2);
        case 48:
            elk = sz < 4 ? 10 : 14;
            return r[8] + pcew();
        case 49:
            elk = sz < 4 ? 10 : 14;
            return r[9] + pcew();
        case 50:
            elk = sz < 4 ? 10 : 14;
            return r[10] + pcew();
        case 51:
            elk = sz < 4 ? 10 : 14;
            return r[11] + pcew();
        case 52:
            elk = sz < 4 ? 10 : 14;
            return r[12] + pcew();
        case 53:
            elk = sz < 4 ? 10 : 14;
            return r[13] + pcew();
        case 54:
            elk = sz < 4 ? 10 : 14;
            return r[14] + pcew();
        case 55:
            elk = sz < 4 ? 10 : 14;
            return r[15] + pcew();
        case 56:
            elk = sz < 4 ? 8 : 12;
            return read_short_big((pc += 2) - 2);
        case 57:
            elk = sz < 4 ? 12 : 16;
            return read_int_big((pc += 4) - 4);
        case 58:
            elk = sz < 4 ? 8 : 12;
            {
                int a = pc;
                return a + read_short_big((pc += 2) - 2);
            }
        case 59:
            elk = sz < 4 ? 10 : 14;
            {
                int a = pc;
                return a + pcew();
            }
        case 60:
            elk = sz < 4 ? 4 : 8;
            return (pc += sz + 1 & -2) - sz;
        }
        return 0;
    }

    private int c_ea0(short ea) throws MC68000Exception {
        switch (ea) {
        case 16:
            elk = 4;
            return r[8];
        case 17:
            elk = 4;
            return r[9];
        case 18:
            elk = 4;
            return r[10];
        case 19:
            elk = 4;
            return r[11];
        case 20:
            elk = 4;
            return r[12];
        case 21:
            elk = 4;
            return r[13];
        case 22:
            elk = 4;
            return r[14];
        case 23:
            elk = 4;
            return r[15];
        case 24:
            elk = 4;
            return r[8];
        case 25:
            elk = 4;
            return r[9];
        case 26:
            elk = 4;
            return r[10];
        case 27:
            elk = 4;
            return r[11];
        case 28:
            elk = 4;
            return r[12];
        case 29:
            elk = 4;
            return r[13];
        case 30:
            elk = 4;
            return r[14];
        case 31:
            elk = 4;
            return r[15];
        case 32:
            elk = 6;
            return r[8];
        case 33:
            elk = 6;
            return r[9];
        case 34:
            elk = 6;
            return r[10];
        case 35:
            elk = 6;
            return r[11];
        case 36:
            elk = 6;
            return r[12];
        case 37:
            elk = 6;
            return r[13];
        case 38:
            elk = 6;
            return r[14];
        case 39:
            elk = 6;
            return r[15];
        case 40:
            elk = 8;
            return r[8] + read_short_big((pc += 2) - 2);
        case 41:
            elk = 8;
            return r[9] + read_short_big((pc += 2) - 2);
        case 42:
            elk = 8;
            return r[10] + read_short_big((pc += 2) - 2);
        case 43:
            elk = 8;
            return r[11] + read_short_big((pc += 2) - 2);
        case 44:
            elk = 8;
            return r[12] + read_short_big((pc += 2) - 2);
        case 45:
            elk = 8;
            return r[13] + read_short_big((pc += 2) - 2);
        case 46:
            elk = 8;
            return r[14] + read_short_big((pc += 2) - 2);
        case 47:
            elk = 8;
            return r[15] + read_short_big((pc += 2) - 2);
        case 48:
            elk = 10;
            return r[8] + pcew();
        case 49:
            elk = 10;
            return r[9] + pcew();
        case 50:
            elk = 10;
            return r[10] + pcew();
        case 51:
            elk = 10;
            return r[11] + pcew();
        case 52:
            elk = 10;
            return r[12] + pcew();
        case 53:
            elk = 10;
            return r[13] + pcew();
        case 54:
            elk = 10;
            return r[14] + pcew();
        case 55:
            elk = 10;
            return r[15] + pcew();
        case 56:
            elk = 8;
            return read_short_big((pc += 2) - 2);
        case 57:
            elk = 12;
            return read_int_big((pc += 4) - 4);
        case 58:
            elk = 8;
            {
                int a = pc;
                return a + read_short_big((pc += 2) - 2);
            }
        case 59:
            elk = 10;
            {
                int a = pc;
                return a + pcew();
            }
        }
        return 0;
    }

    private int c_ea1(short ea) throws MC68000Exception {
        switch (ea) {
        case 16:
            elk = 4;
            return r[8];
        case 17:
            elk = 4;
            return r[9];
        case 18:
            elk = 4;
            return r[10];
        case 19:
            elk = 4;
            return r[11];
        case 20:
            elk = 4;
            return r[12];
        case 21:
            elk = 4;
            return r[13];
        case 22:
            elk = 4;
            return r[14];
        case 23:
            elk = 4;
            return r[15];
        case 24:
            elk = 4;
            return r[8]++;
        case 25:
            elk = 4;
            return r[9]++;
        case 26:
            elk = 4;
            return r[10]++;
        case 27:
            elk = 4;
            return r[11]++;
        case 28:
            elk = 4;
            return r[12]++;
        case 29:
            elk = 4;
            return r[13]++;
        case 30:
            elk = 4;
            return r[14]++;
        case 31:
            elk = 4;
            return (r[15] += 2) - 2;
        case 32:
            elk = 6;
            return --r[8];
        case 33:
            elk = 6;
            return --r[9];
        case 34:
            elk = 6;
            return --r[10];
        case 35:
            elk = 6;
            return --r[11];
        case 36:
            elk = 6;
            return --r[12];
        case 37:
            elk = 6;
            return --r[13];
        case 38:
            elk = 6;
            return --r[14];
        case 39:
            elk = 6;
            return r[15] -= 2;
        case 40:
            elk = 8;
            return r[8] + read_short_big((pc += 2) - 2);
        case 41:
            elk = 8;
            return r[9] + read_short_big((pc += 2) - 2);
        case 42:
            elk = 8;
            return r[10] + read_short_big((pc += 2) - 2);
        case 43:
            elk = 8;
            return r[11] + read_short_big((pc += 2) - 2);
        case 44:
            elk = 8;
            return r[12] + read_short_big((pc += 2) - 2);
        case 45:
            elk = 8;
            return r[13] + read_short_big((pc += 2) - 2);
        case 46:
            elk = 8;
            return r[14] + read_short_big((pc += 2) - 2);
        case 47:
            elk = 8;
            return r[15] + read_short_big((pc += 2) - 2);
        case 48:
            elk = 10;
            return r[8] + pcew();
        case 49:
            elk = 10;
            return r[9] + pcew();
        case 50:
            elk = 10;
            return r[10] + pcew();
        case 51:
            elk = 10;
            return r[11] + pcew();
        case 52:
            elk = 10;
            return r[12] + pcew();
        case 53:
            elk = 10;
            return r[13] + pcew();
        case 54:
            elk = 10;
            return r[14] + pcew();
        case 55:
            elk = 10;
            return r[15] + pcew();
        case 56:
            elk = 8;
            return read_short_big((pc += 2) - 2);
        case 57:
            elk = 12;
            return read_int_big((pc += 4) - 4);
        case 58:
            elk = 8;
            {
                int a = pc;
                return a + read_short_big((pc += 2) - 2);
            }
        case 59:
            elk = 10;
            {
                int a = pc;
                return a + pcew();
            }
        case 60:
            elk = 4;
            return (pc += 2) - 1;
        }
        return 0;
    }

    private int c_ea2(short ea) throws MC68000Exception {
        switch (ea) {
        case 16:
            elk = 4;
            return r[8];
        case 17:
            elk = 4;
            return r[9];
        case 18:
            elk = 4;
            return r[10];
        case 19:
            elk = 4;
            return r[11];
        case 20:
            elk = 4;
            return r[12];
        case 21:
            elk = 4;
            return r[13];
        case 22:
            elk = 4;
            return r[14];
        case 23:
            elk = 4;
            return r[15];
        case 24:
            elk = 4;
            return (r[8] += 2) - 2;
        case 25:
            elk = 4;
            return (r[9] += 2) - 2;
        case 26:
            elk = 4;
            return (r[10] += 2) - 2;
        case 27:
            elk = 4;
            return (r[11] += 2) - 2;
        case 28:
            elk = 4;
            return (r[12] += 2) - 2;
        case 29:
            elk = 4;
            return (r[13] += 2) - 2;
        case 30:
            elk = 4;
            return (r[14] += 2) - 2;
        case 31:
            elk = 4;
            return (r[15] += 2) - 2;
        case 32:
            elk = 6;
            return r[8] -= 2;
        case 33:
            elk = 6;
            return r[9] -= 2;
        case 34:
            elk = 6;
            return r[10] -= 2;
        case 35:
            elk = 6;
            return r[11] -= 2;
        case 36:
            elk = 6;
            return r[12] -= 2;
        case 37:
            elk = 6;
            return r[13] -= 2;
        case 38:
            elk = 6;
            return r[14] -= 2;
        case 39:
            elk = 6;
            return r[15] -= 2;
        case 40:
            elk = 8;
            return r[8] + read_short_big((pc += 2) - 2);
        case 41:
            elk = 8;
            return r[9] + read_short_big((pc += 2) - 2);
        case 42:
            elk = 8;
            return r[10] + read_short_big((pc += 2) - 2);
        case 43:
            elk = 8;
            return r[11] + read_short_big((pc += 2) - 2);
        case 44:
            elk = 8;
            return r[12] + read_short_big((pc += 2) - 2);
        case 45:
            elk = 8;
            return r[13] + read_short_big((pc += 2) - 2);
        case 46:
            elk = 8;
            return r[14] + read_short_big((pc += 2) - 2);
        case 47:
            elk = 8;
            return r[15] + read_short_big((pc += 2) - 2);
        case 48:
            elk = 10;
            return r[8] + pcew();
        case 49:
            elk = 10;
            return r[9] + pcew();
        case 50:
            elk = 10;
            return r[10] + pcew();
        case 51:
            elk = 10;
            return r[11] + pcew();
        case 52:
            elk = 10;
            return r[12] + pcew();
        case 53:
            elk = 10;
            return r[13] + pcew();
        case 54:
            elk = 10;
            return r[14] + pcew();
        case 55:
            elk = 10;
            return r[15] + pcew();
        case 56:
            elk = 8;
            return read_short_big((pc += 2) - 2);
        case 57:
            elk = 12;
            return read_int_big((pc += 4) - 4);
        case 58:
            elk = 8;
            {
                int a = pc;
                return a + read_short_big((pc += 2) - 2);
            }
        case 59:
            elk = 10;
            {
                int a = pc;
                return a + pcew();
            }
        case 60:
            elk = 4;
            return (pc += 2) - 2;
        }
        return 0;
    }

    private int c_ea4(short ea) throws MC68000Exception {
        switch (ea) {
        case 16:
            elk = 8;
            return r[8];
        case 17:
            elk = 8;
            return r[9];
        case 18:
            elk = 8;
            return r[10];
        case 19:
            elk = 8;
            return r[11];
        case 20:
            elk = 8;
            return r[12];
        case 21:
            elk = 8;
            return r[13];
        case 22:
            elk = 8;
            return r[14];
        case 23:
            elk = 8;
            return r[15];
        case 24:
            elk = 8;
            return (r[8] += 4) - 4;
        case 25:
            elk = 8;
            return (r[9] += 4) - 4;
        case 26:
            elk = 8;
            return (r[10] += 4) - 4;
        case 27:
            elk = 8;
            return (r[11] += 4) - 4;
        case 28:
            elk = 8;
            return (r[12] += 4) - 4;
        case 29:
            elk = 8;
            return (r[13] += 4) - 4;
        case 30:
            elk = 8;
            return (r[14] += 4) - 4;
        case 31:
            elk = 8;
            return (r[15] += 4) - 4;
        case 32:
            elk = 10;
            return r[8] -= 4;
        case 33:
            elk = 10;
            return r[9] -= 4;
        case 34:
            elk = 10;
            return r[10] -= 4;
        case 35:
            elk = 10;
            return r[11] -= 4;
        case 36:
            elk = 10;
            return r[12] -= 4;
        case 37:
            elk = 10;
            return r[13] -= 4;
        case 38:
            elk = 10;
            return r[14] -= 4;
        case 39:
            elk = 10;
            return r[15] -= 4;
        case 40:
            elk = 12;
            return r[8] + read_short_big((pc += 2) - 2);
        case 41:
            elk = 12;
            return r[9] + read_short_big((pc += 2) - 2);
        case 42:
            elk = 12;
            return r[10] + read_short_big((pc += 2) - 2);
        case 43:
            elk = 12;
            return r[11] + read_short_big((pc += 2) - 2);
        case 44:
            elk = 12;
            return r[12] + read_short_big((pc += 2) - 2);
        case 45:
            elk = 12;
            return r[13] + read_short_big((pc += 2) - 2);
        case 46:
            elk = 12;
            return r[14] + read_short_big((pc += 2) - 2);
        case 47:
            elk = 12;
            return r[15] + read_short_big((pc += 2) - 2);
        case 48:
            elk = 14;
            return r[8] + pcew();
        case 49:
            elk = 14;
            return r[9] + pcew();
        case 50:
            elk = 14;
            return r[10] + pcew();
        case 51:
            elk = 14;
            return r[11] + pcew();
        case 52:
            elk = 14;
            return r[12] + pcew();
        case 53:
            elk = 14;
            return r[13] + pcew();
        case 54:
            elk = 14;
            return r[14] + pcew();
        case 55:
            elk = 14;
            return r[15] + pcew();
        case 56:
            elk = 12;
            return read_short_big((pc += 2) - 2);
        case 57:
            elk = 16;
            return read_int_big((pc += 4) - 4);
        case 58:
            elk = 12;
            {
                int a = pc;
                return a + read_short_big((pc += 2) - 2);
            }
        case 59:
            elk = 14;
            {
                int a = pc;
                return a + pcew();
            }
        case 60:
            elk = 8;
            return (pc += 4) - 4;
        }
        return 0;
    }
}

