import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DAS {
    public static String mode;
    public static void main(String[] args) {
        DatagramSocket socket=null;
        int port = Integer.parseInt(args[0]);
        int number = Integer.parseInt(args[1]);
        try {
            System.out.println("Port: " + port);
            System.out.println("Input: " + number);
            socket = new DatagramSocket(port);
            mode="Master";
        }catch (Exception e) {
            mode="Slave";
        }
        switch (mode) {
            case "Master":
                System.out.println("Running in master mode");
                List<Integer> numbers=new ArrayList<>();
                try {
                    byte[] buffer = new byte[1024];
                    numbers.add(Integer.parseInt(args[1]));
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                    while(true) {
                        socket.receive(packet);
                        byte[] data = packet.getData();
                        String message = new String(data, 0, packet.getLength()).trim();
                        if (isInteger(message)) {
                            int dataInt = Integer.parseInt(message);
                            if (dataInt == 0) {
                                if (!numbers.isEmpty()) {
                                    int sum=0;
                                    for (int i : numbers) {
                                        sum += i;
                                    }
                                    double average = sum / (double) numbers.size();
                                    System.out.println("Average: "+average);
                                    broadcast(String.valueOf(average), port);
                                }
                            } else if (dataInt == -1) {
                                broadcast("-1",port);
                                System.out.println("Ending program...");
                                socket.close();
                                break;
                            } else {
                                numbers.add(dataInt);
                            }
                        }
                        Arrays.fill(buffer, (byte) 0);
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "Slave":
                System.out.println("Running in slave mode");
                try{
                    int masterPort = Integer.parseInt(args[0]);
                    DatagramSocket slaveSocket=new DatagramSocket();
                    String message=Integer.parseInt(args[1]) + "";
                    InetAddress address = InetAddress.getByName("localhost");
                    byte[] buffer = message.getBytes();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, masterPort);
                    slaveSocket.send(packet);
                    slaveSocket.close();
                }catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }
    public static void broadcast(String message, int port) {
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true); // Enable broadcasting
            InetAddress address = InetAddress.getByName("255.255.255.255"); // Broadcast address
            byte[] buffer = message.getBytes(); // Convert the String to bytes
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            socket.send(packet); // Send the packet
            socket.close(); // Close the socket
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
