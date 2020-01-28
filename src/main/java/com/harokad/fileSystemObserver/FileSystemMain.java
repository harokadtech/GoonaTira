package com.harokad.fileSystemObserver;

import java.io.IOException;


public class FileSystemMain {

    public static void main(String[] args) throws IOException{
        FileSystemObserver observer = new FileSystemObserver();

        FileSystemManager.watch("D:/devWin/TiraManager/searchdocuments/VisaDocs", observer);
        FileSystemManager.watch("D:/devWin/TiraManager/searchdocuments/Dev4Africa", observer);

        while(true) { }
    }
}
