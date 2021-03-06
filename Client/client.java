import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.ArrayList;

class client {
    public static void main(String args[]) {
        try {
            SocketChannel sc = SocketChannel.open();
            Console cons = System.console();
            ArrayList<SocketAddress> clients = null;
            String originalName = "";
            String mySubFile;

            // Nhap ip cua server
            String ip = cons.readLine("Enter IP address: ");
            if (!validitycheck(ip)) {
                return;
            }

            // Check cong
            int port = 0;
            try {
                port = Integer.parseInt(cons.readLine("Enter port number: "));
                if (port < 1024 || port > 65535) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException nfe) {
                System.out.println(
                        "Invalid port number. Port must be a integer between 1024 and 65535. Closing program...");
                return;
            }
            // ket noi den server
            sc.connect(new InetSocketAddress(ip, port));

            // getListFile(sc);
            originalName = getOriginalName(sc);
            clients = getClientInfo(sc);
            clients.remove(sc.getLocalAddress());

            mySubFile = receiveFile(sc);
            int localPort = getPort(sc.getLocalAddress()) + 10;
            SendSubFile sendFile = new SendSubFile(localPort, mySubFile);
            GetSubFile getFile = new GetSubFile(clients, originalName);
            sendFile.start();
            Thread.sleep(1000);
            getFile.start();
            
            


        } catch (Exception e) {
            System.out.println("Server Unreachable. Closing program..");
            e.printStackTrace();
            return;
        }
    }

    public static void getListFile(SocketChannel sc) throws IOException {
        ByteBuffer buff = ByteBuffer.allocate(65535); // read
        ByteBuffer buffer; // write
        sc.read(buff);
        String listFile = new String(buff.array()).trim();
        System.out.println(listFile);
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
                    buffer = ByteBuffer.allocate(4096);
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
                }
            } catch (IOException e) {
                System.out.println("There was an error retrieving the file");
            }
        }
        return fileName;
    }

    public static String getOriginalName(SocketChannel sc) throws IOException {
        String originalName = "";
        ByteBuffer buffer;
        buffer = ByteBuffer.allocate(4096);
        sc.read(buffer);
        originalName = new String(buffer.array()).trim();
        System.out.println("Original name: " + originalName);
        return originalName;
    }

    public static ArrayList<SocketAddress> getClientInfo(SocketChannel sc) throws Exception {
        ArrayList<SocketAddress> clients = null;
        InputStream inputStream = sc.socket().getInputStream();
        ObjectInputStream ois = new ObjectInputStream(inputStream);
        clients = (ArrayList<SocketAddress>) ois.readObject();
        System.out.println("Clients : " + clients.toString());
        return clients;
    }

    public static boolean validitycheck(String ip) {
        try {
            String[] iparray = ip.split("\\.");
            int[] ipintarray = new int[iparray.length];
            for (int i = 0; i < iparray.length; i++) {
                ipintarray[i] = Integer.parseInt(iparray[i]);
            }
            if (ipintarray.length != 4) {
                throw new NumberFormatException();
            } else {
                return true;
            }
        } catch (NumberFormatException nfe) {
            System.out.println("Invalid IP address.  Closing program..");
            return false;
        }
    }

    public static void getSubFile(ArrayList<SocketAddress> clients) {
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
        } catch (Exception e) {
            System.out.println("Error to connect Subfile server");
            e.printStackTrace();
        }
    }

    public static String getIp(SocketAddress sa) {
        String ip = "";
        String address = sa.toString();
        for(int i = 0; i < address.length(); i++) {
            if(i > address.indexOf("/") && i < address.indexOf(":")) {
                ip += address.charAt(i);
            }
        }
        return ip;
    }

    public static int getPort(SocketAddress sa) {
        String port = "";
        String address = sa.toString();
        for(int i = 0; i < address.length(); i++) {
            if(i > address.indexOf(":")) {
                port += address.charAt(i);
            }
        }
        return Integer.parseInt(port);
    }


}

