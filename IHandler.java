package Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IHandler {
    /**
     * Socket - abstraction of 2-way pipeline
     * Every socket has 2 sides: inputStream, outputStream
     */

    public abstract void handle(InputStream fromClient, OutputStream ToClient)  throws IOException, ClassNotFoundException;

}
