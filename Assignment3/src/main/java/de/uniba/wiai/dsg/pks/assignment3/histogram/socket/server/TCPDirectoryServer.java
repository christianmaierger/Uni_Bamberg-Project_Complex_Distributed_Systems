package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ParseDirectory;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.TerminateConnection;

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
    //TODO: Was ist denn mit dieser Liste?
    private final List<ClientHandler> clientHandlers;
    private final ExecutorService threadPool;
    private final ConcurrentHashMap<ParseDirectory, Histogram> cache;
    private volatile boolean running;
    private int serialNumberClientHandlers;

    public TCPDirectoryServer() {
        this.clientHandlers = new LinkedList<>();
        this.threadPool = Executors.newCachedThreadPool();
        this.cache = new ConcurrentHashMap<>();
        this.serialNumberClientHandlers = 1;
        this.running = true;
    }

    @Override
    public void start(int port) throws DirectoryServerException {
        try {
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
                if (!threadPool.awaitTermination(10, TimeUnit.SECONDS))
                    System.err.println("DirectoryServer:\tThreadPool did not terminate.");
            }
        } catch (InterruptedException ie) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException io) {
                throw new DirectoryServerException(io.getMessage(), io.getCause());
            }
            System.out.println("DirectoryServer:\tShutdown completed");
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                Socket client = serverSocket.accept();
                ClientHandler clientHandler = connect(client);
                clientHandlers.add(clientHandler);
            } catch (SocketTimeoutException exception) {
                continue;
            } catch (IOException exception) {
                // TODO: Hier überhaupt mit der Verarbeitung aufhören?? Eventuell weitermachen!
                System.err.println("DirectoryServer:\tException: " + exception.getMessage() + ".");
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
        ClientHandler clientHandler = new TCPClientHandler(socket, this, serialNumberClientHandlers);
        threadPool.submit(clientHandler);
        serialNumberClientHandlers++;
        return clientHandler;
    }
}
