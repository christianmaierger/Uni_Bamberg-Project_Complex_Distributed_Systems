package de.uniba.wiai.dsg.pks.assignment4.histogram.actor.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ParseDirectory;
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
   // wahrscheinlich brauchen wir auch den OutputActor
    private ActorRef outputActor;

    // evtl auch wieder hashmap um zu schauen ob was verloren ging durch ex?
    HashMap<Path, Histogram> fileHistogramMap;
    List<Path> pathFileList = new LinkedList<>();


    public FolderActor(String folder, String fileExtension, ActorRef loadBalancer, ActorRef projectActor) {
        this.folder = folder;
        this.fileExtension = fileExtension;
        this.loadBalancer = loadBalancer;
        this.histogram = new Histogram();
        // evtl die adneren Felder vom projectActor getten wie dessen loadbalancer, fileEx etc?!
        this.projectActor=projectActor;
        this.fileHistogramMap = new HashMap<>();
    }



    @Override
    public Receive createReceive() {
        return receiveBuilder()
                // actor funktioniert ja über messages gibt ja kein main oder call, ProjectActor muss den irgendwie anstoßen
                // und ich muss die histograme der einzelnen FIleActors entgegen nehmen
                // sonst brauch ich eigentlich nix, warum nicht ungefragt wenn fertig die Ergebnisse an den OutPutActor und ProjectActor eifnach senden und
                // die reagieren in Ihrem recieve BUilder darauf?
                .match(ParseDirectory.class, this::calculateFolderHistogram)
                .match(ReturnResult.class, this::proccessFileResults)
                .build();

    }


    // fehlt hier nich was
    private <P> void proccessFileResults(ReturnResult fileResult) {

        // aufzählen vielleicht besser erst wenn file wieder da ist
        histogram.setProcessedFiles(histogram.getProcessedFiles() + 1);
        // von p alles getten und aufzählen schätze ich

        Histogram subResult = fileResult.getHistogram();


        // jetzt sind wir fertig mit einem file
        histogram = addUpAllFields(subResult, histogram);

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

    public void calculateFolderHistogram(ParseDirectory message) throws Exception {
      // eher kein eigenes anlegen oder doch eig egal ob Feld, je nachdem wie Aggregation der Zwischenwerte erfolgt
        // ich könnte auch aus der message die hier übergeben wird was auslesen!!
        //Histogram histogram = new Histogram();

       // Optional<Histogram> cachedHistogram = parentServer.getCachedResult(parseDirectory);
       // if(cachedHistogram.isPresent()){
       //     histogram = cachedHistogram.get();
      //  } else {
            processFiles();



        //hier quasi Ergebniss zurücksenden und Folder aufzählen, weil final fertig mit einem
        //parentClientHandler.addToHistogram(histogram);

        // ab hier eigentlich nur wenn wir echt fertig sind, also alle toPrecess processed sind!
        //todo

        // easy mode bissel sleepen damit alles fertig werden kann

        Thread.currentThread().sleep(1000);

        if (filesProcessed!=filesToProcess) {

            // todo schauen was fehlt und nochmal machen lassen
            // aber ab wann, kann ja auch einfach sein, dass die anderen noch brauchen
            // easy abe rnicht so schön wäre ein Schläfchen, weiß aber nicht ob CompFutures ne Lösung wären,
            // aber wäre sau wierd dass die wo anders bearbetiet und dann hier fertig werden....
            for (Path filePath: pathFileList) {
                if(fileHistogramMap.get(filePath)==null) {
                    // second Processing if there was an error
                    FileMessage secondMessage = new FileMessage(filePath, getSelf(), outputActor);
                    loadBalancer.tell(message, getSelf());
                }

            }
        } else {
            histogram.setDirectories(1);

            // histogram hier dann an ProjectActor schicken der rechnet dass dann zusammen denke ich
            ReturnResult folderResultMessage = new ReturnResult(histogram);


            projectActor.tell(folderResultMessage, getSelf());

        }


    }

    private void checkForInterrupt() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Execution has been interrupted.");
        }
    }

    private void processFiles() throws IOException, InterruptedException {
        Path folderPath = Paths.get(folder);
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
                        FileMessage message = new FileMessage(path, getSelf(), outputActor);
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
