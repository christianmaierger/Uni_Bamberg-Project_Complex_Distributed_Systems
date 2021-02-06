package de.uniba.wiai.dsg.pks.assignment.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents a histogram, i.e., a distribution of used alphabetical letters and
 * the amount of directories, lines, files in total and processed files which have actually been used to compute the histogram.
 */
public class Histogram implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final int ALPHABET_SIZE = 26;

	private long[] distribution = new long[ALPHABET_SIZE];
	private long lines;
	private long files;
	private long processedFiles;
	private long directories;

	public Histogram() {
		super();
	}

	public Histogram(long[] distribution, int lines, int files, int processedFiles, int directories) {
		super();
		this.distribution = distribution;
		this.files = files;
		this.processedFiles = processedFiles;
		this.directories = directories;
		this.lines = lines;
	}

	public long[] getDistribution() {
		return distribution;
	}

	public void setDistribution(long[] distribution) {
		this.distribution = distribution;
	}

	public long getFiles() {
		return files;
	}

	public void setFiles(long files) {
		this.files = files;
	}

	public long getDirectories() {
		return directories;
	}

	public void setDirectories(long directories) {
		this.directories = directories;
	}

	public long getLines() {
		return lines;
	}

	public void setLines(long lines) {
		this.lines = lines;
	}

	public long getProcessedFiles() {
		return processedFiles;
	}

	public void setProcessedFiles(long processedFiles) {
		this.processedFiles = processedFiles;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (directories ^ (directories >>> 32));
		result = prime * result + Arrays.hashCode(distribution);
		result = prime * result + (int) (files ^ (files >>> 32));
		result = prime * result + (int) (processedFiles ^ (processedFiles >>> 32));
		result = prime * result + (int) (lines ^ (lines >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Histogram other = (Histogram) obj;
		if (directories != other.directories)
			return false;
		if (!Arrays.equals(distribution, other.distribution))
			return false;
		if (files != other.files)
			return false;
		if (processedFiles != other.processedFiles)
			return false;
		if (lines != other.lines)
			return false;
		return true;
	}

	@Override
	public String toString(){
		StringBuilder stringBuilder = new StringBuilder();
		Formatter formatter = new Formatter(stringBuilder);
		formatter.format("[distr = %s, ", Arrays.toString(distribution));
		formatter.format("lines=%d, ", lines);
		formatter.format("files=%d, ", files);
		formatter.format("processedFiles=%d, ", processedFiles);
		formatter.format("directories=%d]", directories);
		return stringBuilder.toString();
	}
}