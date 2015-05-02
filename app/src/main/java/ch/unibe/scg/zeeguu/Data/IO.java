package ch.unibe.scg.zeeguu.Data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Zeeguu Application
 * Created by Pascal on 02/05/15.
 */
public interface IO {

    void write(DataOutputStream out) throws IOException;

    void read(DataInputStream in) throws IOException;
}
