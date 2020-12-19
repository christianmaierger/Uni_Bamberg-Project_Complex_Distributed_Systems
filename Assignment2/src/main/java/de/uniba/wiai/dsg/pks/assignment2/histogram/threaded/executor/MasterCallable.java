package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.executor;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.Message;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.MessageType;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.OutputServiceCallable;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class MasterCallable implements Callable<Histogram> {
    private final ExecutorService executorService;
    private final String rootFolder;
    private final String fileExtension;
    // liste wird ja nur von diesem thread verwendet? ok? besser concurrent Struktur?
    // oder gleich blockingqueue verwenden?
    private final List<Future<Histogram>> listOfFuturesRepresentingEachFolder = new LinkedList<>();

    private final OutputServiceCallable outputCallable;

    private final Histogram resultHistogram = new Histogram();




    public MasterCallable(ExecutorService masterExcecutor, String rootFolder, String fileExtension, OutputServiceCallable outputCallable) {
        this.executorService= masterExcecutor;
        this.rootFolder = rootFolder;
        this.fileExtension = fileExtension;
        this.outputCallable = outputCallable;
    }

    public Histogram call() throws InterruptedException, ExecutionException, HistogramServiceException {
        //TODO: Suchbereich weiter zerlegen ODER Berechnung durchfuehren

        // Outputservice erzeigen und starten
        // Anlegen eines Singlethreadpools für den OutputService alleine
        // so kann beim herunterfahren der getrennt bearbeitet werden



        try {
            traverseDirectory(rootFolder);
        } catch (IOException e) {
           //todo
        }


        try {
            // get blockiert immer außerhalb von ForkJoinTasks, eben bis erg in future fertig ist
            for (Future<Histogram> result: listOfFuturesRepresentingEachFolder) {
               // hier jetzt result pro folder, könnte man aufzählen
                // so grad ist es quatsch überschreibt ja immer nur resultHist

                Histogram subResult = result.get();
                subResult.getDirectories();
                resultHistogram.addUpAllFields(subResult);

            }
        } catch (InterruptedException e) {
            HistogramServiceException exception = new HistogramServiceException(e);
            throw exception;

        } catch (ExecutionException e) {
            HistogramServiceException exception = new HistogramServiceException(e);
            throw exception;
        }

        outputCallable.put(new Message(MessageType.FINISH));

        return resultHistogram;
    }

    public void traverseDirectory(String currentFolder) throws IOException, InterruptedException {
        Path folder = Paths.get(currentFolder);
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(folder)){
            for(Path path: stream){
                if(Thread.currentThread().isInterrupted()){
                    executorService.shutdown();
                    return;
                }
                if (Files.isDirectory(path)){
                    // statt reku Funktionsaufruf einfach neues MasterCallable für neues Verzeichnis?
                    // Aber brächte das was aus komplexeren Code?
                    traverseDirectory(path.toString());
                }
            }
        }
        // so jetzt ist hier ein dir fertig mit seinen files wenn processFilesInFolder returned
        Future<Histogram> result = processFilesInFolder(currentFolder);
        // vielleicht Future Liste machen und am Ende erst auslesen, bzw an Queue für print übergeben?
        listOfFuturesRepresentingEachFolder.add(result);
    }

    private Future<Histogram> processFilesInFolder(String folder) throws InterruptedException {
        // das wird der lustige teil, wie ich ohne geteilte daten die Verzeichnisse bearbeite und was ich übergebe
        TraverseFolderTask folderTask = new TraverseFolderTask(executorService, folder, fileExtension, outputCallable);
        // submit blockiert nicht, das stost nur an, das get auf das future wartet dann  bis erg echt da ist

        Future<Histogram> result = executorService.submit(folderTask);

        return result;
    }

    }


