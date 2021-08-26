package com.kerneweksoftware.h2outility.models;

import lombok.Data;

/**
 * Top level information of each archived file.
 * 
 * Second of six structures.
 */
@Data
public class FileEntry {
    private int compressionTag;
    private int folderNameIndex;
    private int fileNameIndex;
    private int fileId;
    private int rawSize;
    private int compressedSize;
    private long offset;
    private byte[] checksum;
    private int unknownField;
    private String name;
    private byte[] data;
}
