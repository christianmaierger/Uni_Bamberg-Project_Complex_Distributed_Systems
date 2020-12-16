package de.uniba.wiai.dsg.pks.assignment.model;

public interface HistogramService {

	/**
	 * Computes the histogram of all files for a given file extension in the
	 * root directory and its subdirectories.
	 * 
	 * @param rootDirectory
	 *            the directory to start from
	 * @param fileExtension
	 *            the filter which files are used to compute the histogram
	 * @return the text histogram of the found files
	 * @throws HistogramServiceException
	 *             when the input is invalid (null/empty values, root directory
	 *             does not exist)
	 */
	Histogram calculateHistogram(String rootDirectory, String fileExtension) throws HistogramServiceException;
}
