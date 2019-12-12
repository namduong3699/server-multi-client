import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;

class client{
    public static void main(String args[]){
        try{
            SocketChannel sc = SocketChannel.open();
            Console cons = System.console();

			//Nhap ip cua server
            String ip = cons.readLine("Enter IP address: ");
            if(!validitycheck(ip)){
                return;
            }

            //Check cong
            int port = 0;
            try{
                port = Integer.parseInt(cons.readLine("Enter port number: "));
                if(port < 1024 || port > 65535){
                    throw new NumberFormatException();
                }
            }catch(NumberFormatException nfe){
                System.out.println("Invalid port number. Port must be a integer between 1024 and 65535. Closing program...");
                return;
            }
            //ket noi den server
            sc.connect(new InetSocketAddress(ip,port));

            while(true){
                //nhan lenh cua client
                String fileName = "";
                while(fileName.equals("")){
                    fileName = cons.readLine("Enter command or file to send: ");
                    fileName = fileName.trim();
                }
                String message;
                ByteBuffer buff = ByteBuffer.allocate(65535);
                ByteBuffer buffer;
                switch(fileName){
					//In ra danh sach cac file co the tai xuong
                    case "ls":
                        buffer = ByteBuffer.wrap(fileName.getBytes());
                        sc.write(buffer);
                        sc.read(buff);
                        message = new String(buff.array());
						//message = message.replace("\n", "");
                        System.out.println(message);
                        break;
						
					//Liet ke cac cau lenh
                    case "help":
                        System.out.println("@logout - to close the program \n" +
                                            "ls - list available files to download \n" +
                                            "download {filename} - to download a file");
                        break;

						
                    case "@logout":
                        buffer = ByteBuffer.wrap(fileName.getBytes());
                        sc.write(buffer);
                        return;
                    default:
						if(fileName.length() < 8 || !fileName.substring(0,8).equals("download")) {
							System.out.println("To download file must start with 'download'. Type help for more info");
                            break;
                        }
                       
                        fileName = fileName.replace("download ", "");
                        buffer = ByteBuffer.wrap(fileName.getBytes());
                        sc.write(buffer);

                        sc.read(buff);
                        String code = new String(buff.array());
                        code = code.trim();
                        if(code.equals("error")){
                            System.out.println("There was an error retrieving the file");
                        }else if(code.equals("filenotfound")){
                            System.out.println("The file was not found.");
                        }else{
                            try {
                                try {
                                    //gui server thong diep san sang nhan kich thuoc file
                                    String sendIt = "sendit";
                                    buffer = ByteBuffer.wrap(sendIt.getBytes());
                                    sc.write(buffer);

                                    ByteBuffer fileBuff = ByteBuffer.allocate(4096);
                                    buffer = ByteBuffer.allocate(4096);
                                    sc.read(buffer);
                                    String sizeString = new String(buffer.array());
                                    sizeString = sizeString.trim();
                                    System.out.println(sizeString);
                                    long fileSize = Long.valueOf(sizeString).longValue();

                                    //gui server thong diep san sang nhan file
                                    buffer = ByteBuffer.wrap(sendIt.getBytes());
                                    sc.write(buffer);

                                    System.out.println("waiting for data..");

                                    File f = new File(fileName.substring(1));
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

                                }catch(NumberFormatException nfe){
                                    System.out.println("Error");
                                }
                            }catch(IOException e){
                                System.out.println("There was an error retrieving the file");
                            }
                        }
                }
            }
        }catch(IOException e){
            System.out.println("Server Unreachable. Closing program..");
            return;
        }
    }


	//Kiem tra ip
    public static boolean validitycheck(String ip){
        try{
            String[] iparray = ip.split("\\.");
            int[] ipintarray = new int[iparray.length];
            for(int i = 0; i < iparray.length; i++){
                ipintarray[i] = Integer.parseInt(iparray[i]);
            }
            if(ipintarray.length != 4){
                throw new NumberFormatException();
            }else{
                return true;
            }
        }catch(NumberFormatException nfe){
            System.out.println("Invalid IP address.  Closing program..");
            return false;
        }
    }

}
