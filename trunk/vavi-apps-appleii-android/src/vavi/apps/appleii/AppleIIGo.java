/*
 * AppleIIGo
 * The Java Apple II Emulator 
 * (C) 2006 by Marc S. Ressl(ressl@lonetree.com)
 * Released under the GPL
 */

package vavi.apps.appleii;


/**
 * AppleIIGo class<p>
 * Connects EmAppleII, AppleCanvas
 */
public class AppleIIGo {
	// Class instances
	private EmAppleII apple;
	private AppleDisplay display;
//	private AppleSpeaker speaker;
	private DiskII disk;

	// Machine variables
	private boolean isCpuPaused;
	private boolean isCpuDebugEnabled;

    /** */
    public boolean isCpuDebugEnabled() {
        return isCpuDebugEnabled;
    }   

    // Keyboard variables
	private boolean keyboardUppercaseOnly;

    // Paddle variables
	private boolean isPaddleInverted;

	// Disk variables
	private String diskDriveResource[] = new String[2];

    public String getDiskDriveResource(int drive) {
        return diskDriveResource[drive];
    }

	private boolean diskWritable;

    /** */
    public interface View {
        /** */
        void repaint();
        /** */
        void getCharSet(int[] buffer, int w, int h, int s);
        /** */
        void setDisplayScaledSizeX(int w);
        /** */
        void setDisplayScaledSizeY(int h);
        /** */
        void debug(Throwable t);
        /** */
        void debug(String s);
    }

    /** */
    public void setView(View view) {
        this.view = view;
    }

    /** */
    private View view;

    /** */
    public interface Dao {
        /** */
        String getParameter(String parameter);
        /** */
        void openInputStream(String resource);
        /** */
        void read(byte[] bytes, int offset, int length);
        /** */
        void closeInputStream();
    }

    /** */
    public void setDao(Dao dao) {
        this.dao = dao;
    }

    /** */
    private Dao dao;

    public void setKeyLatch(int key) {
        if (key < 128) {
            if (keyboardUppercaseOnly && (key >= 97) && (key <= 122)) {
                key -= 32;
            }
            apple.setKeyLatch(key);
        }
    }

    public void setButton(int button, boolean flag) {
        apple.paddle.setButton(button, flag);
    }

    /** for key */
    public void setPaddle(int paddle, int value) {
        apple.paddle.setPaddlePos(paddle, value);
    }

    /** for mouse */
    public void setPaddlePos(int x, int y) {
        if (isPaddleInverted) {
            apple.paddle.setPaddlePos(0, (int) (display.getScale() * (255 - y * 256 / 192)));
            apple.paddle.setPaddlePos(1, (int) (display.getScale() * (255 - x * 256 / 280)));
        } else {
            apple.paddle.setPaddlePos(0, (int) (x * display.getScale() * 256 / 280));
            apple.paddle.setPaddlePos(1, (int) (y * display.getScale() * 256 / 192));
        }
    }

    public void setVolume(boolean up) {
//        speaker.setVolume(speaker.getVolume() + (up ? 1 : -1));
    }

    public void toggleStatMode() {
        setStatMode(!isStatMode);
    }

    public void toggleStepMode() {
        apple.setStepMode(!apple.getStepMode());
    }

    public void stepInstructions(int step) {
        apple.setStepMode(apple.getStepMode());
        apple.stepInstructions(step);
    }

    public int[] getDisplayImageBuffer() {
        return display.getDisplayImageBuffer();
    }

    public boolean isPaused() {
        return display.isPaused();
    }

    private boolean isGlare;
    private boolean isStatMode;

    /**
     * Set glare
     */
    public void setGlare(boolean value) {
        isGlare = value;
        display.requestRefresh();
    }
    
    /**
     * Get glare
     */
    public boolean isGlare() {
        return isGlare;
    }

    /**
     * Set stat mode
     */
    public void setStatMode(boolean value) {
        isStatMode = value;
        display.requestRefresh();
    }

    /**
     * Get stat mode
     */
    public boolean isStatMode() {
        return isStatMode;
    }

    public String getStatInfo() {
        StringBuffer statInfo = new StringBuffer();
        statInfo.append(apple.getStatInfo()).append("\n").append(display.getStatInfo());
        return statInfo.toString();
    }

    /**
     * Parameters
     */
    private String getParameter(String parameter, String defaultValue) {
        String value = dao.getParameter(parameter);
        if ((value == null) || (value.length() == 0)) {
            return defaultValue;
        }
        return value;
    }

    /**
 	 * On applet initialization
	 */
	public void init() {
System.err.println("init()");

		// Activate listeners

		// Initialize Apple II emulator
		apple = new EmAppleII(view);
		loadRom(getParameter("cpuRom", ""));
		apple.setCpuSpeed(new Integer(getParameter("cpuSpeed", "1000")).intValue());
		isCpuPaused = getParameter("cpuPaused", "false").equals("true");
		isCpuDebugEnabled = getParameter("cpuDebugEnabled", "false").equals("true");
		apple.setStepMode(getParameter("cpuStepMode", "false").equals("true"));
	
		// Keyboard
		keyboardUppercaseOnly = getParameter("keyboardUppercaseOnly", "true").equals("true");

		// Display
		display = new AppleDisplay(apple);
		display.setScale(new Float(getParameter("displayScale", "1")).floatValue());
		display.setRefreshRate(new Integer(getParameter("displayRefreshRate", "10")).intValue());
		display.setColorMode(new Integer(getParameter("displayColorMode", "1")).intValue());
		setStatMode(getParameter("displayStatMode", "false").equals("true"));
		setGlare(getParameter("displayGlare", "false").equals("true"));

		// Speaker
//		speaker = new AppleSpeaker(apple);
//		speaker.setVolume(new Integer(getAppletParameter("speakerVolume", "3")).intValue());
		
		// Peripherals
		disk = new DiskII();
		apple.setPeripheral(disk, 6);

		// Initialize disk drives
		diskWritable = getParameter("diskWritable", "false").equals("true");
		mountDisk(0, getParameter("diskDrive1", ""));
		mountDisk(1, getParameter("diskDrive2", ""));
	}

	public void start() {
		// Start CPU
		if (!isCpuPaused) {
			resume();
		}
	}

    /**
 	 * On applet destruction
	 */
	public void destroy() {
System.err.println("destroy()");
		unmountDisk(0);
		unmountDisk(1);
	}
	
	/**
 	 * Pause emulator
	 */
	public void pause() {
System.err.println("pause()");
		isCpuPaused = true;
		apple.setPaused(isCpuPaused);
		display.setPaused(isCpuPaused);
//		speaker.setPaused(isCpuPaused);
	}

	/**
 	 * Resume emulator
	 */
	public void resume() {
System.err.println("resume()");
		isCpuPaused = false;
//		speaker.setPaused(isCpuPaused);
		display.setPaused(isCpuPaused);
		apple.setPaused(isCpuPaused);
	}

	/**
 	 * Restarts emulator
	 */
	public void restart() {
System.err.println("restart()");
		apple.restart();
	}
	
    /**
     * Resets emulator
     */
    public void reset() {
System.err.println("reset()");
        apple.reset();
    }

	/**
	 * Load ROM
	 */
	public void loadRom(String resource) {
System.err.println("loadRom(resource: " + resource + ")");
        apple.loadRom(dao, resource);
	}
	
	/**
 	 * Mount a disk
	 */
	public boolean mountDisk(int drive, String resource) {
System.err.println("mountDisk(drive: " + drive + ", resource: " + resource + ")");

		if ((drive < 0) || (drive > 2)) {
			return false;
		}
			
		try {
			unmountDisk(drive);

			diskDriveResource[drive] = resource;

System.err.println("mount: dirve: " + drive  + ", " + resource);
			disk.readDisk(dao, drive, resource, 254, false);

			return true;
		} catch (Throwable e) {
if (e instanceof IllegalStateException) {
 System.err.println("mount: drive: " + drive + ": no disk");
} else {
 e.printStackTrace(System.err);
}
            return false;
		}
	}

	/**
 	 * Unmount a disk
	 */
	public void unmountDisk(int drive) {
System.err.println("unmount: drive: " + drive);
		if ((drive < 0) || (drive > 2)) {
			return;
		}

		if (!diskWritable) {
System.err.println("unmount: drive: " + drive + ", not writable");
			return;
		}
			
		try {
			disk.writeDisk(drive, diskDriveResource[drive]);
		} catch (Throwable e) {
if (e instanceof NullPointerException) {
 System.err.println("unmount: drive: " + drive + ": no disk");
} else {
 e.printStackTrace(System.err);
}
		}
	}

	/**
 	 * Set color mode
	 */
	public void setColorMode(int value) {
System.err.println("setColorMode(value: " + value + ")");
		display.setColorMode(value);
	}

	/**
 	 * Get disk activity
	 */
	public boolean getDiskActivity() {
		return (!isCpuPaused && disk.isMotorOn());
	}
}

/* */
