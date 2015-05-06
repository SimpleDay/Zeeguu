package ch.unibe.scg.zeeguu.Data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Zeeguu Application
 * Created by Pascal on 02/05/15.
 */
public interface IO {

    void write(BufferedWriter bufferedWriter) throws IOException;

    void read(BufferedReader bufferedReader) throws IOException;
}
