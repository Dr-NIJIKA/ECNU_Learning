import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    public static void main(String[] args) {
        String serverAddress = "localhost";//默认地址为localhost
        int serverPort = 12345;//默认端口
        if (args.length > 0) {
            serverAddress = args[0];
        }
        if (args.length > 1) {
            try {
                serverPort = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number: " + args[1]);
                return;
            }
        }//命令行参数，第一个参数为ip地址，第二个参数为端口号

        try (Socket socket = new Socket(serverAddress, serverPort)) {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            //buffered的输入输出，提供了缓冲区；
            String userInput;

            System.out.println("Enter message (end with two newlines) or file path:");//输入提示词
            StringBuilder message = new StringBuilder();
            while (true) {
                userInput = stdIn.readLine();
                if (userInput.isEmpty()) {
                    if (message.length() > 0) {
                        break;
                    }
                } else {
                    message.append(userInput).append("\n");
                }
            }

            String input = message.toString().trim();//在这里将输入前后的空格删去，以防止
            if (input.endsWith(".txt")) {
                try (BufferedReader fileReader = new BufferedReader(new FileReader(input))) {
                    String line;
                    while ((line = fileReader.readLine())!= null) {
                        out.write(line + "\n");
                        out.flush();
                    }
                } catch (IOException e) {
                    System.out.println("Error reading file: " + e.getMessage());
                }
            } else {
                out.write(message.toString());
                out.flush();
            }

            System.out.println("Message sent to server. Waiting for response...");
            String response;
            while ((response = in.readLine())!= null) {
                System.out.println("Received from server: " + response);
                System.out.print("Enter message to send to server (end with two newlines): ");
                message.setLength(0);
                while (true) {
                    userInput = stdIn.readLine();
                    if (userInput.isEmpty()) {
                        if (message.length() > 0) {
                            break;
                        }
                    } else {
                        message.append(userInput).append("\n");
                    }
                }
                out.write(message.toString());
                out.flush();
            }
        } catch (UnknownHostException e) {
            System.out.println("Don't know about host " + serverAddress);
        } catch (IOException e) {
            System.out.println("Couldn't get I/O for the connection to " + serverAddress);
        }
    }
}