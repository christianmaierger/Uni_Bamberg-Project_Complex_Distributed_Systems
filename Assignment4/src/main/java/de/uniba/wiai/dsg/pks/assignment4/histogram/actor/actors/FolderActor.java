package de.uniba.wiai.dsg.pks.assignment4.histogram.actor.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages.ExeptionMessage;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages.FileMessage;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages.ReturnResult;


import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class FolderActor extends AbstractActor {

    private final String folder;
    private final String fileExtension;

    private int filesToProcess;
    private int filesProcessed;

    // checke noch nicht, wie ich mit dem loadBalancer bzw dessen gemanagten FileActors umgehen soll
    private final ActorRef loadBalancer;
    private Histogram histogram;
    //brauchen wir den project actor vielleicht? Denke ja dann spare ich den handshake komplett ein
    private ActorRef projectActor;
    private ActorRef outputActor;

    // evtl auch wieder hashmap um zu schauen ob was verloren ging durch ex?
    HashMap<Path, Histogram> fileHistogramMap;
    List<Path> retriedPathList = new LinkedList<>();
    List<Path> pathFileList = new LinkedList<>();


    public FolderActor(String folder, String fileExtension, ActorRef loadBalancer, ActorRef projectActor, ActorRef outputActor) {
        this.folder = folder;
        this.fileExtension = fileExtension;
        this.loadBalancer = loadBalancer;
        this.histogram = new Histogram();
        this.projectActor=projectActor;
        this.fileHistogramMap = new HashMap<>();
        this.outputActor=outputActor;
    }

    static Props props(String folder, String fileExtension, ActorRef loadBalancer, ActorRef projectActor, ActorRef outputActor) {
        return Props.create(FolderActor.class, ()-> new FolderActor(folder, fileExtension, loadBalancer, projectActor, outputActor));
    }


    //FIXME: evtl hier doch lieber outputActor und loadBalances am Anfang einmal übergeben und danach die parseDir message?
    // Entspricht dem, wie wir es bei Ping/Pong in der Übung gemacht haben?
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                // actor funktioniert ja über messages gibt ja kein main oder call, ProjectActor muss den irgendwie anstoßen
                // und ich muss die histograme der einzelnen FIleActors entgegen nehmen
                // sonst brauch ich eigentlich nix, warum nicht ungefragt wenn fertig die Ergebnisse an den OutPutActor und ProjectActor eifnach senden und
                // die reagieren in Ihrem recieve BUilder darauf?
                //todo neue start message
                //  .match(.class, this::calculateFolderHistogram)
                .match(ReturnResult.class, this::proccessFileResults)
                .match(ExeptionMessage.class, this::handleException)
                // hier könnte man auch ex anch oben propagieren
                .matchAny(any -> System.out.println("Unknown Message: " + any))
                .build();

    }

    private <P> void handleException(ExeptionMessage exeptionMessage) {
        Exception exceptionFromFile = exeptionMessage.getException();
        // eigentlich kann nur ne io drin sein

        if (exceptionFromFile instanceof IOException) {
            exceptionFromFile.getCause();
            Path missingResultPath = exeptionMessage.getPath();
            if(!retriedPathList.contains(missingResultPath))
                retriedPathList.add(missingResultPath);
            FileMessage retryMessage = new FileMessage(missingResultPath, outputActor);
            loadBalancer.tell(retryMessage, getSelf());

        }

    }


    // fehlt hier nich was
    private <P> void proccessFileResults(ReturnResult fileResult) {

        // aufzählen vielleicht besser erst wenn file wieder da ist
        histogram.setProcessedFiles(histogram.getProcessedFiles() + 1);
        // von p alles getten und aufzählen schätze ich

        Histogram subResult = fileResult.getHistogram();


        // jetzt sind wir fertig mit einem file
        histogram = addUpAllFields(subResult, histogram);

        // fertigesn path mit ergebniss in hashmap legen
        //!!!!!!!!!!fileHistogramMap.putIfAbsent(fileResult.getFilePath(), subResult);
        filesProcessed++;

    }


    // hier darf ich schon die model histogram methoden verwenden?
    /**
     * Adds up all fields of two histograms and returns a new histogram with their values from all fields added
     * together.
     *
     * @param subResultHistogram a new result as histogram of which the fields should be added on the fields of a given histogram
     * @param oldHistogram the histogram to which the method should add to
     * @return a Histogrom holding the addition of the two input Histograms
     */
    public static Histogram addUpAllFields(Histogram subResultHistogram, Histogram oldHistogram) {

        long[] oldHistogramDistribution= oldHistogram.getDistribution();
        long[] newHistogramDistribution= subResultHistogram.getDistribution();

        for(int i=0; i<26 ; i++) {
            oldHistogramDistribution[i]= oldHistogramDistribution[i] + newHistogramDistribution[i];
        }

        Histogram result = new Histogram();
        result.setDistribution(oldHistogramDistribution);
        result.setFiles(oldHistogram.getFiles() + subResultHistogram.getFiles());
        result.setProcessedFiles(oldHistogram.getProcessedFiles() + subResultHistogram.getProcessedFiles());
        result.setDirectories(oldHistogram.getDirectories() + subResultHistogram.getDirectories());
        result.setLines(oldHistogram.getLines() + subResultHistogram.getLines());

        return result;
    }


    // was mache ich jetzt ohne call, alles in processFilles reinstopfen denk ich, oder Übermethode!
//todo neue message
    public void calculateFolderHistogram(Object message)  {
        // eher kein eigenes anlegen oder doch eig egal ob Feld, je nachdem wie Aggregation der Zwischenwerte erfolgt
        // ich könnte auch aus der message die hier übergeben wird was auslesen!!
        //Histogram histogram = new Histogram();

        // Optional<Histogram> cachedHistogram = parentServer.getCachedResult(parseDirectory);
        // if(cachedHistogram.isPresent()){
        //     histogram = cachedHistogram.get();
        //  } else {

        // io aus dem directory stream in der methode
        try {
            processFiles();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // todo würde nochmal einlesen des Folder probieren und wenn das nicht geht an projekt actor melden oder ex werfen, dass es kaputt ist?
            try {
                processFiles();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            } catch (IOException ioException) {
                // todo jetzt retried ist kaputt und sollte propagiert werden per supervision denke ich

            }
        }


        //hier quasi Ergebniss zurücksenden und Folder aufzählen, weil final fertig mit einem
        //parentClientHandler.addToHistogram(histogram);

        // ab hier eigentlich nur wenn wir echt fertig sind, also alle toPrecess processed sind!
        //todo

        // easy mode bissel sleepen damit alles fertig werden kann

        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (filesProcessed!=filesToProcess) {

            // todo schauen was fehlt und nochmal machen lassen
            // aber ab wann, kann ja auch einfach sein, dass die anderen noch brauchen
            // easy abe rnicht so schön wäre ein Schläfchen, weiß aber nicht ob CompFutures ne Lösung wären,
            // aber wäre sau wierd dass die wo anders bearbetiet und dann hier fertig werden....
            for (Path filePath: pathFileList) {
                if(fileHistogramMap.get(filePath)==null) {
                    // second Processing if there was an error
                    FileMessage secondMessage = new FileMessage(filePath, outputActor);
                    loadBalancer.tell(message, getSelf());
                }

            }
        } else {
            histogram.setDirectories(1);

            // histogram hier dann an ProjectActor schicken der rechnet dass dann zusammen denke ich
            Path folderPath = Paths.get(folder);


            //!!!!ReturnResult folderResultMessage = new ReturnResult(histogram, folderPath);


            //!!!!projectActor.tell(folderResultMessage, getSelf());

        }


    }


    // denke das machen wir nicht mehr!
    private void checkForInterrupt() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Execution has been interrupted.");
        }
    }


    // dass kann doch jetzt eh nimmer interupted werden?

    /**
     *
     *
     *
     * @throws IOException
     * @throws InterruptedException
     */
    private void processFiles() throws InterruptedException, IOException {
        Path folderPath = Paths.get(folder);
        // io kommt hier vom directoryStream
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath)) {
            for (Path path : stream) {
                checkForInterrupt();
                if (Files.isRegularFile(path)) {

                    histogram.setFiles(histogram.getFiles() + 1);
                    filesProcessed++;
                    boolean fileExtensionCorrect = path.getFileName().toString().endsWith(fileExtension);
                    if (fileExtensionCorrect) {

                        // aufzählen wie viele zu verarbeiten sind
                        this.filesToProcess++;
                        // hier senden
                        FileMessage message = new FileMessage(path, outputActor);
                        // der loadBalancer braucht doch jetzt eigene Logik, wie er das verteilt unter seinen Actoren für Files
                        // denke ich bin hier aber erstmal fertig?
                        loadBalancer.tell(message, getSelf());

                        // path zur pathList damit wir wissen welche paths bearbeiten sien müssen
                        pathFileList.add(path);

                    }
                }
            }
        }
    }


}
