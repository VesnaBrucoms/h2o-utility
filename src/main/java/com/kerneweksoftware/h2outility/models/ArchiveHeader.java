package com.kerneweksoftware.h2outility.models;

import lombok.Data;

@Data
public class ArchiveHeader {
    private String header;
    private float version1;
    private String comments;
    private int version2;
    private int fileCount;
    private long compressedSize;
    private long rawSize;
}
