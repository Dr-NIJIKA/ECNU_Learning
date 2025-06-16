import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static void main(String[] args) {
        int port = 12345;//默认端口号
        if (args.length > 0) {//读取命令行参数
            try {
                port = Integer.parseInt(args[0]);//Server端的第一个参数是端口号
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number: " + args[0]);//不可用的端口则报错
                return;
            }
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {//监听端口
            System.out.println("Server started on port " + port);
            ExecutorService executor = Executors.newFixedThreadPool(10);//创建线程池
            while (true) {
                Socket clientSocket = serverSocket.accept();//当Client端成功时建立连接
                System.out.println("Client connected: " + clientSocket);//连接成功的信息
                executor.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        //这里类ClientHandler实现Runnable接口，负责处理每个客户端的输入，并将其打印.
        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                 BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {
                //使用java输入输出流，套用Buffered形成缓冲区，便于整体的输入输出。
                String inputLine;
                while ((inputLine = in.readLine())!= null) {//读入数据
                    System.out.println("Received from client: " + inputLine);//提示读入的数据
                    System.out.print("Enter response to send to client: ");
                    String response = stdIn.readLine();//双工通信对Client进行回复
                    out.write(response + "\n");
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }//异常处理和对流进行关闭
            }
        }
    }
}