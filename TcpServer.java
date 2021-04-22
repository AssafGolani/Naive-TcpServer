package Server;

import com.sun.jdi.PathSearchingVirtualMachine;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.cert.CertificateRevokedException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TcpServer {

    //every socket has an ip and a port

    private final int port;
    private ThreadPoolExecutor threadPool;
    private IHandler requestConcreteHandler;
    private volatile boolean stopServer;

    public TcpServer(int port){
        this.port = port;
        stopServer = false;
        threadPool = null;
    }

    // Listen to incoming connections, accept if possible and handle clients.
    public void run(IHandler concreteStrategy){
        this.requestConcreteHandler = concreteStrategy;

        Runnable serverMainLogic = ()->{
            new ThreadPoolExecutor(3,5,
                    10, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
            /**
             * 2 types of Sockets:
             * 1.Server Socket - waits for incoming connections. it required a port number.
             * 2.Operational Socket (Client socket) -
             *Server Socket API:
             * 1. create socket
             * 2. bind to a specific potr number
             * 3. listen for incoming connections (a client initiates a tcp connection with server)
             * 4. try to accept (if 3-way handshake is successful)
             * 5. return operational socket (2 way pipeline)
             */
            // create a server socket to listen and accept incoming connections
            try{
                // create and bind to a specific port number
                ServerSocket serverSocket = new ServerSocket(port);
                serverSocket.setSoTimeout(1000);
                while(!stopServer){
                    //request is an operational socket for the specific client
                    // listen and accept (3-way handshake)
                    Socket request = serverSocket.accept(); // 2 operations: listen() + accept()
                    System.out.println("server:: handle client");
                    Runnable runnable= ()->{
                        try{
                            requestConcreteHandler.handle(request.getInputStream(),request.getOutputStream());
                            //Close all streams
                            request.getInputStream().close();
                            request.getOutputStream().close();
                            request.close();

                        }catch (IOException | ClassNotFoundException ioException){
                            ioException.printStackTrace();
                            System.err.println(ioException.getMessage());
                        }
                    };
                    threadPool.execute(runnable);
                }
            }catch (IOException ioException){
            }
        };
        new Thread(serverMainLogic).start();// anonymous thread
    }


    public void stop(){
        if(!stopServer){
            stopServer = true;
            if(threadPool != null) threadPool.shutdown();
        }
    }

    public void jvmInfo(){
        System.out.println(ProcessHandle.current().pid());
        System.out.println(Runtime.getRuntime().maxMemory());
    }