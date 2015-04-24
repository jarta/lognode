package com.jarta.ws.po;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by wei on 2015/4/23.
 */
public class CommonMsg {

    private int id;
    private String path;
    private String content;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
