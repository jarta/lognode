package com.jarta.file;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by wei on 2015/4/4.
 */
public class EventFileListener implements Runnable{

    private static Logger logger = LoggerFactory.getLogger(EventFileListener.class);

    private final int TIMEOUT = 5*1000;

    private static Map<String, EventFileListener> watchRegistry = new HashMap<String, EventFileListener>();

    private Map<String,LogFileStat> fileRegistry = new HashMap<String, LogFileStat>();

    private Map<String,Long> fileInitSize = new HashMap<String, Long>();

    private String filePath;

    private WatchService watcher;

    private boolean runFlag = true;

    private EventFileListener(String filePath) throws Exception {
        this.filePath = filePath;
        //record the init file size
        File fDir = new File(filePath);
        if(!fDir.isDirectory()) {
            logger.error("target location is not a valid folder - {}", filePath);
            throw new Exception(String.format("Invalid monitoring path - %s", filePath));
        }

        File[] files = fDir.listFiles();
        for(int i=0;i<files.length;i++){
            fileInitSize.put(files[i].getName(), files[i].length());
        }
    }

    /**
     * stop and release resource of file watcher
     */
    public void stopWatch() {
        if(watcher!=null) {
            try {
                watcher.close();
            }catch (IOException e) {
                logger.warn("fail to close watcher", e);
            }
        }
    }

    /**
     * start file event monitoring
     * @throws IOException
     */
    public void startWatch() {

        try {
            watcher = FileSystems.getDefault().newWatchService();

            Path watchPath = Paths.get(this.filePath);
            watchPath.register(watcher,StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

            while(true) {

                final WatchKey wKey = watcher.take();

                for(WatchEvent<?> watchEvent: wKey.pollEvents()) {

                    final WatchEvent<Path> watchEventPath = (WatchEvent<Path>) watchEvent;
                    final Path filename = watchEventPath.context();
                    File file = new File(this.filePath + File.separator + filename.toFile().getName());

                    logger.info("event coming for " + file.getPath() + " , event:" + watchEvent.kind().name());
                    LogFileStat curStat = getLogFileStat(file);

                    if(StandardWatchEventKinds.ENTRY_CREATE == watchEvent.kind()) {
                        curStat.create();
                    }

                    if(StandardWatchEventKinds.ENTRY_MODIFY == watchEvent.kind()) {
                        curStat.modify(false);
                    }

                    if(StandardWatchEventKinds.ENTRY_DELETE == watchEvent.kind()) {
                        curStat.remove();
                    }
                }
            }

        } catch (Exception e) {
            logger.error("unexpected exception", e);
            logger.info("quit and remove watcher from the registry");

            stopWatch();
            watchRegistry.remove(filePath);
        }
    }

    /**
     * file processing handler
     * @param file
     * @return
     */
    private LogFileStat getLogFileStat(File file) {
        LogFileStat curStat = null;
        String chgFile =file.getName();
        synchronized (fileRegistry) {
            if(!fileRegistry.containsKey(chgFile)) {
                //TODO: the default position should be read when process up

                fileRegistry.put(chgFile, new LogFileStat(file, toLastLine(file)));
            }
        }
        curStat = fileRegistry.get(chgFile);
        return curStat;
    }

    /**
     * Remove the stale file
     * @param fileName
     */
    private void removeLogFileStat(String fileName) {
        synchronized (fileRegistry) {
            fileRegistry.remove(fileName);
        }
    }

    /**
     *
     * @param file
     * @return
     */
    private long toLastLine(File file) {
        Long initSize = fileInitSize.remove(file.getName());
        if(initSize!=null && file.length() > initSize) {
            return initSize;
        }
        return 0;
    }


    /**
     * initiate new file watcher for the given file path
     * @param filePath
     */
    public static void addWatch(String filePath) throws Exception {
        File target = new File(filePath);
        if(target.exists() && target.isDirectory()) {
            synchronized (watchRegistry) {
                if(!watchRegistry.containsKey(filePath)) {
                    final EventFileListener fileWatcher = new EventFileListener(filePath);
                    watchRegistry.put(filePath, fileWatcher);
                    new Thread(fileWatcher).start();
                }
            }
        }
    }

    @Override
    public void run() {
        startWatch();
        while(runFlag) {
            synchronized (fileRegistry) {
                Iterator<LogFileStat> fileIter = fileRegistry.values().iterator();
                while(fileIter.hasNext()) {
                    LogFileStat f = fileIter.next();
                    if(f.isNeedFlash() && f.isOverTime(TIMEOUT)) {
                        f.modify(true);
                    }
                }
            }

            try {
                Thread.sleep(30 * 1000);
            } catch (InterruptedException e) {
            }
        }
    }

    public static void main(String[] args) throws Exception {
        EventFileListener.addWatch("D:\\works");
        Thread.currentThread().join();
    }
}
