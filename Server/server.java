import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.ArrayList;

class server {

    public static void main(String args[]) {
        ArrayList<SocketAddress> clients = new ArrayList<SocketAddress>();
        ArrayList<SocketChannel> list = new ArrayList<SocketChannel>();
        int clientLimit = 3;
        boolean isMax = false;

        try {
            ServerSocketChannel c = ServerSocketChannel.open();
            Console cons = System.console();
            // check cong
            try {
                int port = Integer.parseInt(cons.readLine("Enter port number: "));
                if (port < 1024 || port > 65535) {
                    throw new NumberFormatException();
                }
                c.bind(new InetSocketAddress(port));
            } catch (NumberFormatException nfe) {
                System.out.println("Port must be a valid integer between 1024 and 65535. Closing program...");
                return;
            }

            // Tiep nhan ket noi tu client
            while (true) {
                SocketChannel sc = c.accept();
                if (sc != null) {
                    SocketAddress clientAddress = sc.getRemoteAddress();
                    clients.add(clientAddress);
                    list.add(sc);
                    System.out
                            .println("Client " + clients.size() + " with address " + clientAddress + " has connected!");
                    if (clients.size() >= clientLimit) {
                        System.out.println("limit reach " + clients.size() + "  " + clientLimit);
                        isMax = true;
                    }
                }
                if (isMax) {
                    String folderName = "SharedFolder";
                    String fileName = "file.doc";
                    ArrayList<File> files = splitFile(folderName + "/" + fileName, clientLimit);
                    // sendListFile(list);
                    sendOriginalName(fileName, list);
                    sendClientInfo(clients, list);
                    sendFile(list, files);

                }
            }
        } catch (Exception e) {
            System.out.println("Got an IO exception. Closing program...");
            e.printStackTrace();
            return;
        }
    }

    public static void sendClientInfo(ArrayList<SocketAddress> clients, ArrayList<SocketChannel> list) {
        try {
            for (SocketChannel socketChannel : list) {
                OutputStream outputStream = socketChannel.socket().getOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(outputStream);
                oos.writeObject(clients);
                System.out.println("Sending clients info to " + socketChannel.getRemoteAddress());
                // oos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendListFile(ArrayList<SocketChannel> list) {
        try {
            File flocation = new File(
                    server.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "/SharedFolder");
            File[] files = flocation.listFiles();
            String fileList = "";
            for (File f : files) {
                fileList += (f.getName() + "\n");
            }
            for (SocketChannel sc : list) {
                System.out.println("Sending list file to " + sc.getRemoteAddress());
                ByteBuffer buffer = ByteBuffer.allocate(32768);
                buffer = ByteBuffer.wrap(fileList.getBytes());
                sc.write(buffer);
            }
        } catch (IOException e) {
            System.out.println("Client use ctrl+c to close program!!");
            return;
        }
    }

    public static void sendOriginalName(String fileName, ArrayList<SocketChannel> list) throws IOException {
        // Gui ten file
        for (SocketChannel socketChannel : list) {
            ByteBuffer buffer = ByteBuffer.allocate(32768);
            buffer = ByteBuffer.wrap(fileName.getBytes());
            socketChannel.write(buffer);
            System.out.println("Sendding original file name to " + socketChannel.getRemoteAddress());
        }
    }

    public static void sendFile(ArrayList<SocketChannel> list, ArrayList<File> files) {
        try {
            for (SocketChannel sc : list) {
                int index = list.indexOf(sc);
                File file = files.get(index);
                String subFileName = Integer.toString(index) + ".client";
                // System.out.println(subFileName);

                System.out.println("Sedding file " + subFileName + " to " + sc.getRemoteAddress());
                ByteBuffer buffer = ByteBuffer.allocate(32768);
                String incoming = "incoming";
                buffer = ByteBuffer.wrap(incoming.getBytes());
                sc.write(buffer);

                // cho cho den khi client san sang nhan tin nhan
                buffer = ByteBuffer.allocate(4096);
                sc.read(buffer);
                buffer.compact();

                // gui do dai tep va doi cho den khi client chap nhan
                Long size = file.length();
                String fileSize = size.toString();
                buffer = ByteBuffer.allocate(4096);
                buffer = ByteBuffer.wrap(fileSize.getBytes());
                sc.write(buffer);

                // Gui ten subFile
                buffer = ByteBuffer.allocate(4096);
                buffer = ByteBuffer.wrap(subFileName.getBytes());
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
            }
            System.out.println("The file has been sent.");
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public static ArrayList<File> splitFile(String source, int numberFile) throws IOException {
        File sourceFile = new File(source);
        ArrayList<File> files = new ArrayList<File>();
        if (sourceFile.exists() && sourceFile.isFile()) {
            long sizeFile = sourceFile.length();
            long sizeSplitFile = (sizeFile / numberFile);
            InputStream is = new FileInputStream(sourceFile);
            byte[] arr = new byte[1024];
            for (int i = 1; i <= numberFile; i++) {
                File file = new File(Integer.toString(i));
                int j = 0;
                long a = 0;
                OutputStream os = new FileOutputStream(file, false);
                while ((j = is.read(arr)) != -1) {
                    os.write(arr, 0, j);
                    a += j;
                    if (a >= sizeSplitFile) {
                        break;
                    }
                }
                os.flush();
                os.close();
                files.add(file);
            }
            is.close();
            return files;
        } else {
            System.out.println("file không tồn tại");
            return null;
        }
    }
}
