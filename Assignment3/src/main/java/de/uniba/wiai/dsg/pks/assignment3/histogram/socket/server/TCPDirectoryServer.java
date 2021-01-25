package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ParseDirectory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TCPDirectoryServer implements DirectoryServer {
    private ServerSocket serverSocket;
    private int port;
    //TODO: Was ist denn mit dieser Liste?
    private final List<ClientHandler> clientHandlers;
    private final ExecutorService threadPool;
    private final ConcurrentHashMap<ParseDirectory, Histogram> cache;
    private volatile boolean running;
    private int clientCounter;

    public TCPDirectoryServer() {
        this.clientHandlers = new LinkedList<>();
        this.threadPool = Executors.newCachedThreadPool();
        this.cache = new ConcurrentHashMap<>();
        this.clientCounter = 1;
        this.running = true;
        this.port = -1;
    }

    @Override
    public void start(int port) throws DirectoryServerException {
        try {
            this.port = port;
            this.serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(50);
            threadPool.submit(this);
            System.out.println("DirectoryServer:\tServer has been started successfully.");
        } catch (IOException exception) {
            throw new DirectoryServerException(exception.getMessage(), exception.getCause());
        }
    }

    @Override
    public void disconnect(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
    }

    @Override
    public void shutdown() throws DirectoryServerException {
        try {
            this.running = false;
            threadPool.shutdown();
            if (!threadPool.awaitTermination(1, TimeUnit.MILLISECONDS)) {
                threadPool.shutdownNow();
                if (!threadPool.awaitTermination(20, TimeUnit.SECONDS))
                    System.err.println("DirectoryServer:\tThreadPool did not terminate.");
            }
        } catch (InterruptedException ie) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            try {
                serverSocket.close();
                System.out.println("DirectoryServer:\tShutdown completed");
            } catch (IOException io) {
                throw new DirectoryServerException(io.getMessage(), io.getCause());
            }
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                Socket client = serverSocket.accept();
                ClientHandler clientHandler = connect(client);
                clientHandlers.add(clientHandler);
            } catch (SocketTimeoutException timeoutException) {
                continue;
            } catch (IOException exception) {
                // TODO: Meistens ist ja dann der socket kaputt --> doch eher shutdown?
                System.err.println("DirectoryServer:\tException occurred while accepting new client: " + exception.getMessage() + ". Try creating a new socket.");
                createNewServerSocket();
                //TODO: Frage an Tut: sollen wir uns auch um so etwas wie reconnection nach einem absturz kümmern?
                // Also z.B. soll, wenn der ClientHandler abtürzt oder null zurückgibt, der Client eine neue Anfrage starten?
            }
        }
    }

    @Override
    public Optional<Histogram> getCachedResult(ParseDirectory request) {
        Histogram result = cache.get(request);
        if (Objects.isNull(result)) {
            return Optional.empty();
        }
        return Optional.of(result);
    }

    @Override
    public void putInCache(ParseDirectory request, Histogram result) {
        cache.putIfAbsent(request, result);
    }

    @Override
    public ClientHandler connect(Socket socket) {
        ClientHandler clientHandler = new TCPClientHandler(socket, this, clientCounter);
        threadPool.submit(clientHandler);
        clientCounter++;
        return clientHandler;
    }

    private void createNewServerSocket(){
        try{
            serverSocket = new ServerSocket(port);
        } catch (IOException exception){
            this.running = false;
            System.err.println("DirectoryServer:\tUnable to create new socket. New clients cannot be accepted anymore.");
        }
    }
}
