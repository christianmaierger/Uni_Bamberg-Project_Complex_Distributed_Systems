package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ParseDirectory;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@ThreadSafe
public class TCPDirectoryServer implements DirectoryServer {
    private ServerSocket serverSocket;
    @GuardedBy(value="itself")
    private final List<ClientHandler> clientHandlers;
    private final ExecutorService threadPool;
    @GuardedBy(value="itself")
    private final ConcurrentHashMap<ParseDirectory, Histogram> cache;
    private int clientCounter;

    public TCPDirectoryServer() {
        this.clientHandlers = Collections.synchronizedList(new LinkedList<>());
        this.threadPool = Executors.newCachedThreadPool();
        this.cache = new ConcurrentHashMap<>();
        this.clientCounter = 1;
    }

    @Override
    public void start(int port) throws DirectoryServerException {
        try {
            this.serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(100);
            threadPool.submit(this);
            printToOut("Server has been started successfully.");
        } catch (IOException exception) {
            throw new DirectoryServerException(exception.getMessage(), exception.getCause());
        }
    }

    @Override
    public void disconnect(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
        if(clientHandler instanceof TCPClientHandler){
            TCPClientHandler test = (TCPClientHandler) clientHandler;
            printToOut("Disconnected ClientHandler #" + test.getNumber() + ".");
        }
    }

    @Override
    public void shutdown() throws DirectoryServerException {
        try {
            threadPool.shutdown();
            if (!threadPool.awaitTermination(1, TimeUnit.MILLISECONDS)) {
                threadPool.shutdownNow();
                if (!threadPool.awaitTermination(20, TimeUnit.SECONDS))
                    printToErr("ThreadPool did not terminate.");
            }
        } catch (InterruptedException ie) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            try {
                serverSocket.close();
                printToOut("Shutdown completed.");
            } catch (IOException io) {
                throw new DirectoryServerException(io.getMessage(), io.getCause());
            }
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Socket client = serverSocket.accept();
                ClientHandler clientHandler = connect(client);
                clientHandlers.add(clientHandler);
            } catch (SocketTimeoutException timeoutException) {
                continue;
            } catch (IOException exception) {
                // TODO: Meistens ist ja dann der socket kaputt --> doch eher shutdown?
                printToErr("Exception occurred while accepting new client: " + exception.getMessage() + ". Try creating a new socket.");
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

    private void printToOut(String message){
        System.out.println("DirectoryServer:\t" + message);
    }

    private void printToErr(String message){
        System.err.println("DirectoryServer:\t" + message);
    }
}