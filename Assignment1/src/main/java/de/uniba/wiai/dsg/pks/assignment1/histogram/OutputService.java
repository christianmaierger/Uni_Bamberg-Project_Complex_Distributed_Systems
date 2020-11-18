package de.uniba.wiai.dsg.pks.assignment1.histogram;

/**
 * Diese Klasse kümmert sich nur um den Output auf der Konsole. Die kann später
 * hoffentlich einfach in einen Thread gepackt werden.
 */
public class OutputService {
    private int lineNumber = 0;

    /**
     * Prints the following message to console where
     * - lineNumber = current line number of output logs
     * - directoryPath = finished directory
     * - parameters distr, lines, files, processedFiles, directories shall be entered.
     * For the values, a histogram has to be consulted.
     *
     * Message:
     * N:[lineNumber] Directory [directoryName] finished
     * [ distr = [  ],
     * lines=, files=, processedFiles=, directories=]
     *
     * @param directoryPath
     */
    void logProcessedDirectory(String directoryPath){

    }

    /**
     * Logs the following message to console with entered filePath:
     *
     * N:[lineNumber] − File [filePath]] finished !
     *
     *
     * @param filePath
     */
    void logProcessedFile(String filePath){

    }


}
