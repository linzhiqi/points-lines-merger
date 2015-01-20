package merger;

import java.util.List;

import util.Coord;

public class LinesInFile {
	private String fileName;
	private List<List<Coord>> lines;

	public LinesInFile(String fileName, List<List<Coord>> lines) {
		this.setFileName(fileName);
		this.setLines(lines);
	}

	public List<List<Coord>> getLines() {
		return lines;
	}

	public void setLines(List<List<Coord>> lines) {
		this.lines = lines;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
