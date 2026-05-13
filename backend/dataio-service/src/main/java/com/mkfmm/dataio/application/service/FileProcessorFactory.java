package com.mkfmm.dataio.application.service;

import com.mkfmm.dataio.application.port.outbound.FileProcessor;
import com.mkfmm.dataio.domain.model.FileFormat;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FileProcessorFactory {

    private final Map<FileFormat, FileProcessor> processors;

    public FileProcessorFactory(Map<FileFormat, FileProcessor> processors) {
        this.processors = processors;
    }

    public FileProcessor getProcessor(FileFormat format) {
        FileProcessor processor = processors.get(format);
        if (processor == null) {
            throw new IllegalArgumentException("No processor for format: " + format);
        }
        return processor;
    }
}
