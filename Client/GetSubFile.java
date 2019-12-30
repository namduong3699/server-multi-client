import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.ArrayList;

class GetSubFile extends Thread {
    ArrayList<SocketAddress> clients = null;
    String folderName = "SubFiles";
    String originalName = "";

    public GetSubFile(ArrayList<SocketAddress> clients, String originalName) {
        this.clients = clients;
        this.originalName = originalName;
    }

    @Override
    public void run() {
        try {
            for (SocketAddress socketAddress : clients) {
                String ip = getIp(socketAddress);
                int port = getPort(socketAddress) + 10;
                SocketChannel sc = SocketChannel.open();
                System.out.println("Try to connect Subfile server :" + ip + "/" + port);
                sc.connect(new InetSocketAddress(ip, port));
                System.out.println("Connected Subfile server :" + ip + "/" + port);
                receiveFile(sc);
            }
            System.out.println("Complete receive subfile!");
            mergeFile(folderName, clients.size() + 1, originalName);
        } catch (Exception e) {
            
        }
    }

    public static void mergeFile(String folderName, int quantity, String originalName) {
        try {
            System.out.println("Merging " + quantity + " subfiles.. to " + originalName);
            File file = new File(originalName);
            
            OutputStream os = new FileOutputStream(file);
            InputStream is = null;
            for (int i = 0; i < quantity; i++) {
                File subFile = new File(folderName + "/" + i + ".client");
                if (subFile.exists()) {
                    is = new FileInputStream(subFile);
                    int j = 0;
                    byte[] arr = new byte[1024];
                    while ((j = is.read(arr)) != -1) {
                        os.write(arr, 0, j);
                    }
                    os.flush();
                    is.close();
                } else {
                    break;
                }
            }
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getIp(SocketAddress sa) {
        String ip = "";
        String address = sa.toString();
        for (int i = 0; i < address.length(); i++) {
            if (i > address.indexOf("/") && i < address.indexOf(":")) {
                ip += address.charAt(i);
            }
        }
        return ip;
    }

    public static int getPort(SocketAddress sa) {
        String port = "";
        String address = sa.toString();
        for (int i = 0; i < address.length(); i++) {
            if (i > address.indexOf(":")) {
                port += address.charAt(i);
            }
        }
        return Integer.parseInt(port);
    }

    public static String receiveFile(SocketChannel sc) throws Exception {
        System.out.println("Receiving file...");
        String subFolder = "SubFiles";
        String fileName = "";
        ByteBuffer buff = ByteBuffer.allocate(65535); // read
        ByteBuffer buffer; // write
        sc.read(buff);
        String code = new String(buff.array());
        code = code.trim();
        if (code.equals("error")) {
            System.out.println("There was an error retrieving the file");
        } else if (code.equals("filenotfound")) {
            System.out.println("The file was not found.");
        } else {
            try {
                try {
                    // gui server thong diep san sang nhan kich thuoc file
                    String sendIt = "sendit";
                    buffer = ByteBuffer.allocate(4096);
                    buffer = ByteBuffer.wrap(sendIt.getBytes());
                    sc.write(buffer);

                    ByteBuffer fileBuff = ByteBuffer.allocate(4096);
                    
                    buffer = ByteBuffer.allocate(4096);
                    sc.read(buffer);
                    String sizeString = new String(buffer.array());
                    sizeString = sizeString.trim();
                    System.out.println(sizeString);
                    long fileSize = Long.valueOf(sizeString).longValue();
                    System.out.println("File size: " + fileSize);

                    // Nhan ten file
                    buffer = ByteBuffer.allocate(8192);
                    sc.read(buffer);
                    fileName = new String(buffer.array()).trim();
                    System.out.println("Subfile name: " + fileName);

                    // gui server thong diep san sang nhan file
                    buffer = ByteBuffer.allocate(4096);
                    buffer = ByteBuffer.wrap(sendIt.getBytes());
                    sc.write(buffer);

                    System.out.println("waiting for data..");

                    File f = new File(subFolder + "/" + fileName);
                    fileBuff = ByteBuffer.allocate(4096);
                    int inBytes = 0;
                    FileChannel fc = new FileOutputStream(f, false).getChannel();

                    while (inBytes != fileSize) {
                        inBytes += sc.read(fileBuff);
                        fileBuff.flip();
                        fc.write(fileBuff);
                        fileBuff = ByteBuffer.allocate(4096);
                    }
                    fc.close();
                    System.out.println("Success!");

                } catch (NumberFormatException nfe) {
                    System.out.println("Error");
                    nfe.printStackTrace();
                }
            } catch (IOException e) {
                System.out.println("There was an error retrieving the file");
            }
        }
        return fileName;
    }
}