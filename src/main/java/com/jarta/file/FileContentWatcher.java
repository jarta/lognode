package com.jarta.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by wei on 2015/4/5.
 */
public class FileContentWatcher {

    private Logger logger = LoggerFactory.getLogger(FileContentWatcher.class);

    private File logFile;
    private long position;

    //TODO: to be removed by spring injection
    private CommonUpdateListener listener = new FileChangeHandler();

    private AtomicBoolean needFlash = new AtomicBoolean(false);

    public FileContentWatcher(File f, long position) {
        logFile = f;
        this.position = position;
    }

    public void create() {
        if(position != 0) {
            logger.error("unable to handle new file event sent for the processing one");
            position = 0;
        }
    }

    /**
     * File is updated
     * @param forceRefresh - whether force refresh
     * @return if any dirty line needs to be processed later.
     */
    public void modify(boolean forceRefresh) {
        RandomAccessFile raReader = null;
        try {
            raReader = new RandomAccessFile(logFile, "r");
            if(position > 0) {
                raReader.seek(position);
            }

            List<String> lines = new ArrayList<String>();
            String line = raReader.readLine();
            while(line != null) {
                lines.add(line);
                line = raReader.readLine();
            }

            //ignore the last line for un-finished consideration
            if(!forceRefresh && lines.size() > 0) {
                lines.remove(lines.size() - 1);
                needFlash.set(true);
            }

            //force refresh reset to default
            if(forceRefresh) {
                needFlash.set(false);
            }

            updatePosition(lines, raReader.getFilePointer());
            processRecords(lines);
        } catch (Exception e) {
            logger.error("fail to continue ..", e);
        } finally {
            try {
                raReader.close();
            } catch (IOException e) {
               logger.error("fail to close the file - " + logFile , e);
            }
        }
    }

    /**
     * Whether to be flash in time
     * @return
     */
    public boolean isNeedFlash() {
        return needFlash.get();
    }

    /**
     * TODO - publish to redis
     * @param lines
     */
    void processRecords(List<String> lines) {
        for(String line: lines) {
            listener.handle(line);
        }
    }

    /**
     * update the position
     * @param lines
     */
    private void updatePosition(List<String> lines, long logPointer) {
        //java.io.LineNumberReader
        long lastLine = 0l;
        if(lines.size() > 0) {
            lastLine = lines.get(lines.size() - 1).length();
        }
        this.position = logPointer - lastLine;
    }

    /**
     * clean up the buffer
     */
    public void remove() {
        //flash
        modify(true);
        //reset position
        position = 0;
    }

    /**
     * Check if the file has been changed for while
     * @param timeout
     * @return
     */
    public boolean isOverTime(long timeout) {
        long pastTime = Calendar.getInstance().getTimeInMillis() - logFile.lastModified();
        return pastTime > timeout;
    }
}
