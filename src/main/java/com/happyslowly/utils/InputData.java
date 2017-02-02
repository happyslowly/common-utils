package com.happyslowly.utils;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class InputData implements Iterable<Map<String, Object>> {
    private String inputFile;
    private InputDataIterator iterator;
    private String delimiter;

    public InputData(String inputFile, String delimiter) throws IOException {
        this.inputFile = inputFile;
        this.delimiter = delimiter;
    }

    public InputData(String inputFile) throws IOException {
        this(inputFile, "\\|");
    }

    @Override
    public Iterator<Map<String, Object>> iterator() {
        iterator = new InputDataIterator(inputFile);
        return iterator;
    }

    public class InputDataIterator implements Iterator<Map<String, Object>> {
        private String nextLine;
        private boolean finished = false;
        private BufferedReader reader;
        private String[] header;


        public InputDataIterator(String inputFile) {
            try {
                open(inputFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void open(String unitTestFile) throws IOException {
            if (unitTestFile.endsWith(".gz")) {
                reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(unitTestFile))));
            } else {
                reader = new BufferedReader(new FileReader(unitTestFile));
            }
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("Empty file: " + unitTestFile);
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
                e.printStackTrace();
            }

            return false;
        }

        @Override
        public Map<String, Object> next() {
            if (!hasNext()) {
                return null;
            }
            String[] data = nextLine.split(delimiter, -1);
            Map<String, Object> variableMap = new LinkedHashMap<String, Object>();
            for (int i = 0; i < header.length; i++) {
                if (!data[i].trim().equals(""))
                    variableMap.put(header[i], data[i]);
            }
            nextLine = null;
            return variableMap;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        public void close() {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void close() {
        if (iterator != null) {
            iterator.close();
        }
    }

}