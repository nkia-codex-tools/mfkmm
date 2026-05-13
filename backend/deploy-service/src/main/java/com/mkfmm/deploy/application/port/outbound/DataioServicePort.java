package com.mkfmm.deploy.application.port.outbound;

public interface DataioServicePort {
    byte[] exportAll(String format, String userId);
}
