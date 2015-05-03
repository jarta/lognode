package com.jarta.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

/**
 * Created by wei on 2015/4/4.
 */
public class FileChangeHandler implements CommonUpdateListener {

   private static Logger logger = LoggerFactory.getLogger(FileChangeHandler.class);

    public void handle(String line) {
        try {
            logger.info("process chg {}", new String(line.getBytes(), "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
