package com.kerneweksoftware.h2outility.models;

import lombok.Data;

/**
 * File header of the H2O format.
 * 
 * First of six structures. Details the number of archived files, the archive's raw size in bytes, and the archive's
 * compressed size in bytes, amongst other fields.
 */
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
