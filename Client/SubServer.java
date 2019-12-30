import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;

class SubServer extends Thread {
    private String fileName = "";
    private SocketChannel sc = null;
    private String subFolder = "SubFiles";

    public SubServer(SocketChannel socketChannel, String fileName) {
        this.fileName = fileName;
        this.sc = socketChannel;
    }

    @Override
    public void run() {
        while (true) {
            sendFile(sc, fileName, subFolder);
        }
        
    }

    public static void sendFile(SocketChannel sc, String fileName, String subFolder) {
        try {
            System.out.println("Sedding file " + fileName + " to " + sc.getRemoteAddress());
            File file = new File(subFolder + "/" + fileName);
            ByteBuffer buffer = ByteBuffer.allocate(32768);
            String incoming = "incoming";
            buffer = ByteBuffer.wrap(incoming.getBytes());
            sc.write(buffer);

            // cho cho den khi client san sang nhan tin nhan
            buffer = ByteBuffer.allocate(4096);
            sc.read(buffer);
            buffer.compact();

            // gui do dai tep va doi cho den khi client chap nhan
            System.out.println("Sending file size");
            Long size = file.length();
            String fileSize = size.toString();
            buffer = ByteBuffer.allocate(4096);
            buffer = ByteBuffer.wrap(fileSize.getBytes());
            sc.write(buffer);

            // Gui ten subFile
            buffer = ByteBuffer.allocate(4096);
            buffer = ByteBuffer.wrap(fileName.getBytes());
            sc.write(buffer);

            buffer = ByteBuffer.allocate(4096);
            sc.read(buffer);

            byte[] fileBytes;
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            long bytesRead = 0;
            while (bytesRead != size) {
                int bytesToSend = 4096;
                if (size - bytesRead >= bytesToSend) {
                    bytesRead += bytesToSend;
                } else {
                    bytesToSend = (int) (size - bytesRead);
                    bytesRead = size;
                }
                fileBytes = new byte[bytesToSend];
                bis.read(fileBytes, 0, bytesToSend);
                buffer = ByteBuffer.wrap(fileBytes);
                sc.write(buffer);
            }

            System.out.println("The file has been sent.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}