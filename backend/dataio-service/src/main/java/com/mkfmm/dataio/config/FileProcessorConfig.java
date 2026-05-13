package com.mkfmm.dataio.config;

import com.mkfmm.dataio.adapter.outbound.file.ExcelFileProcessor;
import com.mkfmm.dataio.adapter.outbound.file.JsonFileProcessor;
import com.mkfmm.dataio.adapter.outbound.file.TsvFileProcessor;
import com.mkfmm.dataio.application.port.outbound.FileProcessor;
import com.mkfmm.dataio.domain.model.FileFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class FileProcessorConfig {

    @Bean
    public Map<FileFormat, FileProcessor> fileProcessors(
            ExcelFileProcessor excel,
            TsvFileProcessor tsv,
            JsonFileProcessor json) {
        return Map.of(
            FileFormat.EXCEL, excel,
            FileFormat.TSV, tsv,
            FileFormat.JSON, json
        );
    }
}
