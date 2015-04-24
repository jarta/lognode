package com.jarta.file;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wei on 2015/4/4.
 */
public class EventFileListener {

    private static Logger logger = LoggerFactory.getLogger(EventFileListener.class);

    private static Map<String, EventFileListener> watchRegistry = new HashMap<String, EventFileListener>();

    private Map<String,LogFileStat> fileRegistry = new HashMap<String, LogFileStat>();

    private String filePath;

    private WatchService watcher;

    private EventFileListener(String filePath){
        this.filePath = filePath;

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
                    final Path file = watchEventPath.context();

                    logger.info("event coming for " + file.toFile().getName() + " , event:" + watchEvent.kind().name());
                    LogFileStat curStat = getLogFileStat(file);

                    if(StandardWatchEventKinds.ENTRY_CREATE == watchEvent.kind()) {
                        curStat.create();
                    }

                    if(StandardWatchEventKinds.ENTRY_MODIFY == watchEvent.kind()) {
                        curStat.modify();
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
    private LogFileStat getLogFileStat(Path file) {
        LogFileStat curStat = null;
        String chgFile = file.toFile().getName();
        synchronized (fileRegistry) {
            if(!fileRegistry.containsKey(chgFile)) {
                //TODO: the default position should be read when process up
                fileRegistry.put(chgFile, new LogFileStat(file, 0));
            }
        }
        curStat = fileRegistry.get(chgFile);
        return curStat;
    }


    /**
     * initiate new file watcher for the given file path
     * @param filePath
     */
    public static void addWatch(String filePath) {
        File target = new File(filePath);
        if(target.exists() && target.isDirectory()) {
            synchronized (watchRegistry) {
                if(!watchRegistry.containsKey(filePath)) {
                    final EventFileListener fileWatcher = new EventFileListener(filePath);
                    fileWatcher.startWatch();

                    watchRegistry.put(filePath, fileWatcher);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {


    }

}
