// FileUtils
// $Id: FileUtils.java,v 1.33 2003/08/03 09:45:28 axelwernicke Exp $
//
// Copyright (C) 2002-2003 Axel Wernicke <axel.wernicke@gmx.de>
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

package de.axelwernicke.mypod.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;


/**
 * Class that provides some helpers related to file operations.
 *
 * @author axel wernicke
 */
public class FileUtils {
    /** instance of this (singleton) class */
    private static FileUtils INSTANCE = null;

    /** Pattern to recognize mp3 files */
    public static final Pattern mp3FilePattern = Pattern.compile(".+mp[23]", Pattern.CASE_INSENSITIVE);

    /** jdk1.4 logger */
    private static Logger logger = Logger.getLogger("de.axelwernicke.mypod.util");

    /** Creates a new instance of FileUtils */
    private FileUtils() {
    }

    /**
     * This is a singleton class, so use getInstance to get in instance :)
     *
     * @return FileUtils instance
     */
    public FileUtils getInstance() {
        return (INSTANCE != null) ? INSTANCE : new FileUtils();
    }

    /**
     * Checks if a file is writable. If its not yet it waits a defined time
     * @param file
     * @param maxWait
     * @return
     */
    public static boolean isWritable(File file, int maxWait) {
        boolean writable = false;

        try {
            if (file.exists()) {
                int cnt = 0;
                writable = file.canWrite();
                while (!writable && (cnt < maxWait)) {
                    // wait 1/100 second
                    logger.warning("is writable check for file " + file + " delayed");
                    Thread.currentThread().wait(100);
                    cnt += 100;
                    writable = file.canWrite();
                }
            }
        } catch (Exception e) { /* nothing to report */
        }

        return writable;
    }

    /**
     * Copies file using nio transfer method.
     * Since we get somtimes exceptions, lets try it up to ten times.
     *
     * @param source file
     * @param destination file
     * @return number of bytes copied
     */
    public static long copy(File source, File destination) {
        logger.entering("de.axelwernicke.mypod.util.FileUtils", "copy");

        long bytesCopied = -1;

        try {
            boolean copied = false;
            int tries = 0;
            FileChannel fic = new FileInputStream(source).getChannel();
            FileChannel foc = new FileOutputStream(destination).getChannel();

            do {
                try {
                    bytesCopied = foc.transferFrom(fic, 0, fic.size());
                    copied = true;
                } catch (IOException e) {
                    logger.info("copy try " + tries + " of 10 failed: " + destination + " " + e.getMessage());
                    tries++;
                }
            } while (!copied && (tries < 10));

            fic.close();
            foc.close();
        } catch (Exception e) {
            logger.warning("exception raised :" + e.getMessage());
        }

        logger.exiting("de.axelwernicke.mypod.util.FileUtils", "copy");

        return bytesCopied;
    }

    /**
     * Copies file using old io with custom buffered streams
     *
     * @return number of bytes copied
     * @param streamBufferSize in bytes
     * @param localBufferSize in bytes
     * @param source file
     * @param destination file
     */
    public static long bufferedCopy(File source, File destination, int streamBufferSize, int localBufferSize) {
        logger.entering("de.axelwernicke.mypod.util.FileUtils", "bufferedCopy");

        long readBytes = 0;
        long bytesCopied = 0;

        byte[] localBuffer = new byte[localBufferSize]; // init local buffer
        BufferedInputStream fis = null;
        BufferedOutputStream fos = null;

        try {
            fis = new BufferedInputStream(new FileInputStream(source), streamBufferSize);
            fos = new BufferedOutputStream(new FileOutputStream(destination), streamBufferSize);

            // read first chunk of data
            readBytes = fis.read(localBuffer);

            // copy as long as we get a full chunk
            while (readBytes == localBufferSize) {
                // write chunk
                fos.write(localBuffer);

                // read next chunk
                readBytes = fis.read(localBuffer);

                // just for statistics
                bytesCopied += localBufferSize;
            }

            logger.finer("copied " + bytesCopied + " bytes");

            // copy the last couple of bytes
            for (int i = 0; i < readBytes; i++) {
                fos.write(localBuffer[i]);
            }

            logger.finer("copied " + readBytes + " additional bytes");

            fis.close();
            fos.close();
            localBuffer = null;
        } catch (Exception e) {
            logger.warning("exception raised :" + e.getMessage());
            e.printStackTrace();
        }

        logger.exiting("de.axelwernicke.mypod.util.FileUtils", "bufferedCopy");

        return bytesCopied;
    }

    /**
     * Moves a file.
     * <li>  check parameter
     * <li>  check if there is something to do
     * <li>  try to rename file instead physically moving
     * <li>  if renaming failed, copy using nio transfer method
     * <li>  if copy succeded, delete source, else delete destination
     *
     * @param source file
     * @param destination file
     *
     * @return true, if moving succeded
     */
    public static boolean move(File source, File destination) {
        logger.entering("de.axelwernicke.mypod.util.FileUtils", "move");

        boolean success = false;

        // check parameter
        if ((source == null) || (destination == null)) {
            return false;
        }

        // we only have to do something if source != destination
        if (source.equals(destination)) {
            return true;
        }

        // try renaming
        success = source.renameTo(destination);
        if (success) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("moved file from " + source + " to " + destination + " by renaming");
            }
        } else {
            // copy physically
            long bytesCopied = copy(source, destination);

            // delete source if success, destination otherwise
            if (bytesCopied == source.length()) {
                int cnt = 0;
                boolean srcDel = false;
                boolean destDel = false;

                // mysteriously it takes sometimes up to one minute until the source file can be deleted...
                do {
                    // try to delete source
                    srcDel = source.delete();
                    if (srcDel) {
                        success = true;
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("moved file from " + source + " to " + destination + " by copying");
                        }
                    }
                    // try at least to delete destination to avoid file duplication
                    else {
                        success = false;
                        destDel = destination.delete();
                    }
                    cnt++;
                    logger.warning("mv " + source + " to " + destination + " " + cnt + ". src deleted: " + srcDel + " dest deleted: " + destDel);

                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        ;
                    }
                } while (!(srcDel || destDel) && (cnt < 1000));
            } else {
                success = false;
                boolean delDest = destination.delete();
                logger.warning("moving file from " + source + " to " + destination + " by copying failed (copied bytes != filesize), destination file deleted: " + delDest);
            }

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("moved file from " + source + " to " + destination + " by copying");
            }
        }

        logger.exiting("de.axelwernicke.mypod.util.FileUtils", "move");

        return success;
    }

    /**
     * Deletes a file.
     * If file can not deleted now, its scheduled to be deleted on myPod exit
     *
     * @param file to be deleted
     * @return true, if file was successfully deleted or scheduled to be deleted on exit.
     */
    public static boolean delete(File file) {
        logger.entering("de.axelwernicke.mypod.util.FileUtils", "delete");

        boolean success = false;

        try {
            if (file.canRead() && file.canWrite()) {
                boolean deleted = file.delete();
                if (!deleted) {
                    file.deleteOnExit();
                }

                success = true;
            }
        } catch (Exception e) {
            logger.warning("Exception raised: " + e.getMessage());
            e.printStackTrace();
        }

        logger.exiting("de.axelwernicke.mypod.util.FileUtils", "delete");

        return success;
    }

    /**
     * Validates a filename by replacing invalid characters.
     * Up to now the validation is for win32 (ntfs)
     *
     * @return validated filename
     * @param _filename to validate
     */
    public static String validateFileName(String _filename) {
        logger.entering("de.axelwernicke.mypod.util.FileUtils", "validateFileName");

        String filename = _filename;
        if (filename != null) {
            // separators
            filename = filename.replace('\\', ' ');
            filename = filename.replace('/', ' ');
            filename = filename.replace('*', ' ');
            filename = filename.replace('?', ' ');
            filename = filename.replace('"', '\'');
            filename = filename.replace('<', ' ');
            filename = filename.replace('>', ' ');
            filename = filename.replace('|', ' ');
            filename = filename.replaceAll(":", " -");

            // remove double spaces
            // TODO change to regex
            filename = filename.replaceAll("  ", " ");
            filename = filename.replaceAll("  ", " ");
            filename = filename.replaceAll("  ", " ");

            filename = filename.trim();
        }

        logger.exiting("de.axelwernicke.mypod.util.FileUtils", "validateFileName");

        return filename;
    }

    /**
     * Validates a directory name by replacing invalid characters.
     * Up to now the validation is for win32 (ntfs)
     *
     * @return validated directory name
     * @param _filename to validate
     */
    public static String validateDirectoryName(String _filename) {
        logger.entering("de.axelwernicke.mypod.util.FileUtils", "validateDirectoryName");

        String filename = _filename;

        if (filename != null) {
            while (filename.startsWith(".")) {
                filename = filename.substring(1);
            }

            while (filename.endsWith(".")) {
                filename = filename.substring(0, filename.length() - 1);
            }

            // remove double spaces
            // TODO use regex instead....
            filename = filename.replaceAll("  ", " ");
            filename = filename.replaceAll("  ", " ");
            filename = filename.replaceAll("  ", " ");

            filename = validateFileName(filename);
        }

        logger.exiting("de.axelwernicke.mypod.util.FileUtils", "validateDirectoryName");

        return filename;
    }

    /**
     * Gets all subdirectories recursively.
     *
     * @param dir base directory
     * @return all directories below the base directory
     */
    public static List<File> getAllDirectories(File dir) {
        logger.entering("de.axelwernicke.mypod.util.FileUtils", "getAllDirectories");

        List<File> dirs = new ArrayList<File>();

        try {
            // determine all subdirectories
            File[] files = dir.listFiles();

            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    dirs.addAll(getAllDirectories(files[i]));
                }
            }

            // add current dir
            dirs.add(dir);
        } catch (Exception e) {
            logger.warning("exception raised: " + e.getMessage());
            e.printStackTrace();
        }

        logger.exiting("de.axelwernicke.mypod.util.FileUtils", "getAllDirectories");

        return dirs;
    }

    /**
     * Gets all files below the given directory.
     *
     * @param filter file filter that determines the files to find
     * @param dir base directory
     * @return all files below the base directory as File objects
     */
    public static List<File> getAllFiles(File dir, Pattern filter) {
        logger.entering("de.axelwernicke.mypod.util.FileUtils", "getAllFiles");

        List<File> allFiles = new ArrayList<File>();

        try {
            // determine all subdirectories
            File[] files = dir.listFiles();
            int fileLength = files.length;
            for (int i = 0; i < fileLength; i++) {
                if (files[i].isDirectory()) {
                    allFiles.addAll(getAllFiles(files[i], filter));
                } else {
                    if (filter.matcher(files[i].getPath()).matches()) {
                        allFiles.add(files[i]);
                    }
                }
            }
        } catch (Exception e) {
            logger.warning("exception raised: " + e.getMessage());
            e.printStackTrace();
        }

        logger.exiting("de.axelwernicke.mypod.util.FileUtils", "getAllFiles");

        return allFiles;
    }

    /**
     * Gets the total filesize recursively
     * @param dir to get file size for
     * @param pattern to select files
     * @return total filesize
     */
    public static long getTotalFilesize(File dir, Pattern pattern) {
        long totalFilesize = 0;

        try {
            // get all files below the given directory
            List<File> allFiles = FileUtils.getAllFiles(dir, pattern);

            // and summarize file sizes
            Iterator<File> fileIter = allFiles.iterator();
            while (fileIter.hasNext()) {
                totalFilesize += fileIter.next().length();
            }

            logger.fine("Found " + allFiles.size() + " files at " + dir);
            logger.fine("Total filesize was " + totalFilesize);
        } catch (Exception ex) {
            logger.warning("Exception raised: " + ex.getMessage());
            ex.printStackTrace();
        }

        return totalFilesize;
    }

    /**
     * Determines the space left on a disc
     * The space is determined by executing a native command ( e.g. dir for windows, df for linux )
     * This is platform dependend, but ther's no solution provided by java :(
     *
     * @param path path to the disc to get free space for
     * @return space left on the disc, -1 if determination failed
     */
    public static long getSpaceLeft(String path) {
        logger.entering("de.axelwernicke.mypod.util.FileUtils", "getSpaceLeft");

        long spaceLeft = -1;

        String os = System.getProperty("os.name");

        try {
            if (os.equalsIgnoreCase("LINUX") || os.equalsIgnoreCase("FreeBSD")) {
                spaceLeft = getSpaceLeftLinux(path);
            } else if (os.equalsIgnoreCase("Windows 98") || os.equalsIgnoreCase("Windows NT") || os.equalsIgnoreCase("Windows 2000") || os.equalsIgnoreCase("Windows XP")) {
                spaceLeft = getSpaceLeftWin32(path);
            } else {
                // TODO aw extend for other operating systems: Windows 95, Windows CE, Mac OS, Solaris, SunOS
                // TODO aw extend for other operating systems: HP-UX, AIX, Irix, Digital Unix
                // inform user to contact me
                logger.warning("Operating system " + os + " not supported yet." + "Please contact me at axel.wernicke@gmx.de and leave a message " + "at http://www.sourceforge.net/projects/mypod");
            }
        } catch (Exception e) {
            logger.warning("Exception raised: " + e.getMessage());
            e.printStackTrace();
        }

        logger.exiting("de.axelwernicke.mypod.util.FileUtils", "getSpaceLeft");

        return spaceLeft;
    }

    /**
     * Determines the space left on a disc
     * The space is determined by executing a native command ( e.g. dir for windows, df for linux )
     * This is platform dependend, but ther's no solution provided by java :(
     *
     * @param path path to the disc to get free space for
     * @return space left on the disc, -1 if determination failed
     */
    private static long getSpaceLeftLinux(String path) {
        logger.entering("de.axelwernicke.mypod.util.FileUtils", "getSpaceLeftLinux");
        long spaceLeft = -1;

        try {
            // execute native command
            Process cpProcess = Runtime.getRuntime().exec("df -k | grep " + path);

            logger.info("Executed command at system level was: " + "df -k | grep " + path);

            // prepare and parse the result
            // axel@lachs:~ > df /dev/sda3
            // Filesystem           1k-blocks      Used Available Use% Mounted on
            // /dev/sda3              4022400   1672696   2145368  44% /
            BufferedReader reader = new BufferedReader(new InputStreamReader(cpProcess.getInputStream()));
            reader.readLine(); // throw away the first line
            String tmp = reader.readLine();

            logger.info("line to get available space from was: " + tmp);

            java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(tmp);
            tokenizer.nextToken(); // throw away device
            tokenizer.nextToken(); // throw away total size
            tokenizer.nextToken(); // throw away	used
            tmp = tokenizer.nextToken().trim(); // thats our boy

            logger.info("token to get available space from was: " + tmp);

            // we get k blocks, but need bytes
            spaceLeft = 1024 * Long.parseLong(tmp); // get numeric value
        } catch (Exception e) {
            logger.warning("Exception raised: " + e.getMessage());
            e.printStackTrace();
        }

        logger.exiting("de.axelwernicke.mypod.util.FileUtils", "getSpaceLeftLinux");
        return spaceLeft;
    }

    /**
     * Determines the space left on a disc
     * The space is determined by executing a native command ( e.g. dir for windows, df for linux )
     * This is platform dependend, but ther's no solution provided by java :(
     * This method works for windows 98, NT, 2000 and XP
     *
     * @param path path to the disc to get free space for
     * @return space left on the disc, -1 if determination failed
     */
    private static long getSpaceLeftWin32(String path) {
        logger.entering("de.axelwernicke.mypod.util.FileUtils", "getSpaceLeftWin32");

        long spaceLeft = -1;

        try {
            // execute native command
            Process dirProcess = Runtime.getRuntime().exec("cmd.exe /C dir \"" + path + "\"");
            BufferedReader reader = new BufferedReader(new InputStreamReader(dirProcess.getInputStream()));

            // get the last line of the result, looking somewhat like:
            // " 2 Dir(s)   6.273.284.608 bytes free "
            // catch the last line of the result
            String lastLine;
            String tmp = null;
            do {
                lastLine = tmp;
                tmp = reader.readLine();
            } while (tmp != null);

            // clean last line up
            lastLine.trim();

            // prepare and parse the result
            java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(lastLine);
            tokenizer.nextToken();
            tokenizer.nextToken(); // throw away first and second
            String token = tokenizer.nextToken().trim(); // thats our boy
            token = token.replaceAll("\\D", ""); // remove any non numeric characters
            spaceLeft = Long.parseLong(token); // get numeric value
        } catch (Exception e) {
            logger.warning("Exception raised: " + e.getMessage());
            e.printStackTrace();
        }

        logger.exiting("de.axelwernicke.mypod.util.FileUtils", "getSpaceLeftWin32");

        return spaceLeft;
    }

    /**
     * Deletes empty directories recursively.
     */
    public static int deleteEmptyDirectories(List<File> dirs) {
        int dirsDeleted = 0;

        boolean success = false;
        File dir;

        // iterate over all dirs
        for (Iterator<File> dirIter = dirs.iterator(); dirIter.hasNext();) {
            dir = dirIter.next();

            // delete if dir exists and is empty
            if ((dir != null) && dir.exists() && (dir.list().length == 0)) {
                success = dir.delete();
                if (success) {
                    dirsDeleted++;
                }
            }
        }

        return dirsDeleted;
    }
}
