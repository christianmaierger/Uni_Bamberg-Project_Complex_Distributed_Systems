package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.executor;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class MasterCallable implements Callable<Histogram> {
    ExecutorService executorService;
    String rootFolder;
    String fileExtension;
    // liste wird ja nur von diesem thread verwendet? ok? besser concurrent Struktur?
    // oder gleich blockingqueue verwenden?
    List<Future<Histogram>> listOfFuturesRepresentingEachFolder = new LinkedList<>();


    public MasterCallable(ExecutorService masterExcecutor, String rootFolder, String fileExtension) {
        this.executorService= masterExcecutor;
        this.rootFolder = rootFolder;
        this.fileExtension = fileExtension;
    }

    public Histogram call() throws InterruptedException, ExecutionException {
        //TODO: Suchbereich weiter zerlegen ODER Berechnung durchfuehren
        Histogram currentResultHistogram = new Histogram();
        // eigentlich können die doch alle den gleichen Executor verwenden? ABer kann ich den immutable machen?



        Histogram resultHistogram = new Histogram();
        try {
            traverseDirectory(rootFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            // get blockiert immer außerhalb von ForkJoinTasks, eben bis erg in future fertig ist
            for (Future<Histogram> result: listOfFuturesRepresentingEachFolder) {
               // hier jetzt result pro folder, könnte man aufzählen
                resultHistogram = result.get();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            // zb minvalue um auf Fehler hinzuweisen
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        } finally {
            executorService.shutdown();

            try {
                executorService.awaitTermination(20, TimeUnit.SECONDS);
            } catch (InterruptedException e) {

                // shutdown durchsetzen
                executorService.shutdownNow();

                //flag erhalten
                Thread.currentThread().interrupt();
            }
        }
        return resultHistogram;
    }

    public void traverseDirectory(String rootFolder) throws IOException, InterruptedException {
        Path folder = Paths.get(rootFolder);
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
        Future<Histogram> result = processFilesInFolder(rootFolder);
        // vielleicht Future Liste machen und am Ende erst auslesen, bzw an Queue für print übergeben?
        listOfFuturesRepresentingEachFolder.add(result);
    }

    private Future<Histogram> processFilesInFolder(String rootFolder) throws InterruptedException {
        // das wird der lustige teil, wie ich ohne geteilte daten die Verzeichnisse bearbeite und was ich übergebe
        TraverseFolderTask folderTask = new TraverseFolderTask(executorService, rootFolder, fileExtension);
        // submit blockiert nicht, das stost nur an, das get auf das future wartet dann  bis erg echt da ist

        Future<Histogram> result = executorService.submit(folderTask);
        return result;
    }

    }


