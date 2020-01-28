package com.harokad.goona.service.ocr;

import org.apache.tika.Tika;


public class TikaInstance {

    private static final Tika tika = new Tika();

    public static Tika tika() {
        return tika;
    }
}