import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.Path.*;
import java.nio.file.*;

class server{

    public static void main(String args[]){
        try{
            ServerSocketChannel c = ServerSocketChannel.open();
            Console cons = System.console();

            //check cong
            try{
                int port = Integer.parseInt(cons.readLine("Enter port number: "));
                if(port < 1024 || port > 65535){
                    throw new NumberFormatException();
                }
                c.bind(new InetSocketAddress(port));
            }catch(NumberFormatException nfe){
                System.out.println("Port must be a valid integer between 1024 and 65535. Closing program...");
                return;
            }

            //Tiep nhan ket noi tu client
            while(true){
                SocketChannel sc = c.accept();
                if(sc != null){
                    System.out.println("A client has connected!");
                }
                serverThread s = new serverThread(sc);
                s.start();
            }
        }catch(IOException e){
            System.out.println("Got an IO exception. Closing program...");
            return;
        }
    }
}

class serverThread extends Thread{
    SocketChannel sc;
    serverThread(SocketChannel channel){
        sc = channel;
    }
    public void run(){
        while(true){
            try{
                ByteBuffer buffer = ByteBuffer.allocate(32768);
                    sc.read(buffer);
                    String fileName = new String(buffer.array());
                    fileName = fileName.trim();

                    //Nhan va thuc thi lenh tu client
                    if(fileName.equals("@logout")){
                        System.out.println("Client disconnected");
                        sc.close();
                        return;

                    //Gui client danh sach nhung file co the tai xuong
                    }else if(fileName.equals("ls")){
                        File flocation = new File(server.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "/SharedFolder");
                        File[] files = flocation.listFiles();
                        String fileList="";
                        for(File f: files){
                            fileList += (f.getName() + "\n");
                        }
                        buffer = ByteBuffer.wrap(fileList.getBytes());
                        sc.write(buffer);

                    }else if(fileName != null){
                        try{
                            System.out.println("Client trying to recieve " + fileName);

                            try{
                                Path filelocation = null;
                                String l = null;
                                try{
									fileName = "SharedFolder/" + fileName;
                                    filelocation = Paths.get(server.class.getResource(fileName).toURI());
                                    File f = new File(filelocation.toString());

                                    String incoming = "incoming";
                                    buffer = ByteBuffer.wrap(incoming.getBytes());
                                    sc.write(buffer);

                                    //cho cho den khi client san sang nhan tin nhan
                                    buffer = ByteBuffer.allocate(4096);
                                    sc.read(buffer);
                                    buffer.compact();

                                    //gui do dai tep va doi cho den khi client chap nhan
                                    Long size = f.length();
                                    String fileSize = size.toString();
                                    buffer = ByteBuffer.wrap(fileSize.getBytes());

                                    sc.write(buffer);
                                    buffer = ByteBuffer.allocate(4096);
                                    sc.read(buffer);

                                    byte[] fileBytes;
                                    FileInputStream fis = new FileInputStream(f);
                                    BufferedInputStream bis = new BufferedInputStream(fis);
                                    long bytesRead = 0;
                                    while(bytesRead != size){
                                        int bytesToSend = 4096;
                                        if(size - bytesRead >= bytesToSend){
                                            bytesRead += bytesToSend;
                                        }else{
                                            bytesToSend = (int)(size-bytesRead);
                                            bytesRead = size;
                                        }
                                        fileBytes = new byte[bytesToSend];
                                        bis.read(fileBytes, 0, bytesToSend);
                                        buffer = ByteBuffer.wrap(fileBytes);
                                        sc.write(buffer);
                                    }
                                }catch(URISyntaxException u){
                                    System.out.println("Error converting file");
                                }
                                System.out.println("The file has been sent.");
                            }catch(IOException ioe){
                                String error = "error";

                                //Error: line 132
                                buffer = ByteBuffer.wrap(error.getBytes());
                                sc.write(buffer);
                            }
                        }catch(NullPointerException npe){
                            String error = "filenotfound";
                            System.out.println("The client's file doesn't exist.");
                            buffer = ByteBuffer.wrap(error.getBytes());
                            sc.write(buffer);
                        }
                    }
            }catch(IOException e){
                System.out.println("Client use ctrl+c to close program!!");
                return;
            }
        }
    }
}
