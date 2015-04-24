package com.jarta.file;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;

/**
 * Created by wei on 2015/4/4.
 */
public class FileListener extends TailerListenerAdapter {

   private static Logger logger = LoggerFactory.getLogger(FileListener.class);

    public void handle(String line) {
        logger.info(line);
        super.handle(line);
    }

    public static void main(String[] args) {

        TailerListener fListener = new FileListener();
        String fName = "test.log";
        Tailer tailer = Tailer.create(new File(fName), fListener, 200 , true );

        try {
            synchronized (fListener){
                fListener.wait();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
