package com.happyslowly.utils;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class FlatFile implements Iterable<Map<String, Object>>, AutoCloseable {
    private final String inputFile;
    private FlatFileIterator iterator;
    private final String delimiter;

    private boolean emptyValuesIgnored = true;
    private boolean nullValuesIgnored = false;
    private static final String NULL = "NULL";
    private static final String EMPTY = "";

    public FlatFile(String inputFile, String delimiter) throws IOException {
        this.inputFile = inputFile;
        this.delimiter = delimiter;
    }

    public FlatFile(String inputFile) throws IOException {
        this(inputFile, "\\|");
    }

    public void setEmptyValuesIgnored(boolean flag) {
        this.emptyValuesIgnored = flag;
    }

    public void setNullValuesIgnored(boolean flag) {
        this.nullValuesIgnored = flag;
    }

    @Override
    public Iterator<Map<String, Object>> iterator() {
        try {
            iterator = new FlatFileIterator(inputFile);
            return iterator;
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public class FlatFileIterator implements Iterator<Map<String, Object>> {
        private String nextLine;
        private boolean finished = false;
        private BufferedReader reader;
        private String[] header;

        private static final String GZ_SUFFIX = ".gz";

        public FlatFileIterator(String inputFile) throws IOException {
            open(inputFile);
        }

        private void open(String inputFile) throws IOException {
            if (inputFile.endsWith(GZ_SUFFIX)) {
                reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(inputFile))));
            } else {
                reader = new BufferedReader(new FileReader(inputFile));
            }
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("Empty file: " + inputFile);
            } else {
                header = headerLine.split(delimiter);
            }
        }

        @Override
        public boolean hasNext() {
            try {
                if (nextLine != null) {
                    return true;
                } else if (finished) {
                    return false;
                } else {
                    nextLine = reader.readLine();
                    if (nextLine == null) {
                        finished = true;
                        close();
                        return false;
                    } else {
                        return true;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Map<String, Object> next() {
            if (!hasNext()) {
                return null;
            }
            String[] data = nextLine.split(delimiter, -1);
            Map<String, Object> variableMap = new LinkedHashMap<String, Object>();
            for (int i = 0; i < header.length; i++) {
                if (emptyValuesIgnored && data[i].trim().equals(EMPTY)) {
                    continue;
                }
                if (nullValuesIgnored && data[i].trim().equalsIgnoreCase(NULL)) {
                    continue;
                }
                variableMap.put(header[i], data[i]);
            }
            nextLine = null;
            return variableMap;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        public void close() throws IOException {
            if (reader != null) {
                reader.close();
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (iterator != null) {
            iterator.close();
        }
    }

}