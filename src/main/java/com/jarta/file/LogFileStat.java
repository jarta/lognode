package com.jarta.file;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by wei on 2015/4/5.
 */
public class LogFileStat {

    private Logger logger = LoggerFactory.getLogger(LogFileStat.class);

    private File logFile;
    private long position;

    private AtomicBoolean needFlash = new AtomicBoolean(false);

    public LogFileStat(File f, long position) {
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
        InputStream is = null;
        try {
            is = FileUtils.openInputStream(logFile);
            IOUtils.skip(is, position);
            List<String> lines = IOUtils.readLines(is);

            //ignore the last line for un-finished consideration
            if(!forceRefresh && lines.size() > 0) {
                lines.remove(lines.size() - 1);
                needFlash.set(true);
            }

            //force refresh reset to default
            if(forceRefresh) {
                needFlash.set(false);
            }

            updatePosition(lines);
            processRecords(lines);
        } catch (Exception e) {
            logger.error("fail to continue ..", e);
        } finally {
            IOUtils.closeQuietly(is);
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
            logger.info("todo - {}", line );
        }
    }

    /**
     * update the position
     * @param lines
     */
    private void updatePosition(List<String> lines) {
        //java.io.LineNumberReader
        long bufSize = 0l;
        for(String line: lines) {
            bufSize += line.length() + IOUtils.LINE_SEPARATOR.length();
        }
        this.position += bufSize;
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
