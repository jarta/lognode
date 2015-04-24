package com.jarta.file;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * Created by wei on 2015/4/5.
 */
public class LogFileStat {

    private Logger logger = LoggerFactory.getLogger(LogFileStat.class);

    private Path logFile;
    private long position;

    /** unfinished line or exception stack */
    private String dirtyBuffer;

    public LogFileStat(Path f, long position) {
        logFile = f;
        this.position = position;
    }

    public void create() {
        if(position != 0) {
            logger.error("unable to handle new file event sent for the processing one");
            position = 0;
        }
    }

    public void modify() {
        InputStream is = null;
        try {
            is = FileUtils.openInputStream(logFile.toFile());
            IOUtils.skip(is, position);
            List<String> lines = IOUtils.readLines(is);
            updatePosition(lines);

            dirtyBuffer = lines.remove(lines.size());
            processRecords(lines);
        } catch (Exception e) {
            logger.error("fail to continue ..", e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private void processRecords(List<String> lines) {

    }

    /**
     * update the position
     * @param lines
     */
    private void updatePosition(List<String> lines) {
        long bufSize = 0l;
        for(String line: lines) {
            bufSize += line.length() + IOUtils.LINE_SEPARATOR.length();
        }
        bufSize -= IOUtils.LINE_SEPARATOR.length();
        this.position = bufSize;
    }

    /**
     * clean up the buffer
     */
    public void remove() {
        modify();
        List<String> tmpList = Collections.emptyList();
        tmpList.add(dirtyBuffer);
        processRecords(tmpList);

        //reset position
        position = 0;
    }
}
