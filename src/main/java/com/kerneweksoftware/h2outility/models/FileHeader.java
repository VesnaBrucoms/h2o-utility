package com.kerneweksoftware.h2outility.models;

import lombok.Data;

@Data
public class FileHeader {
    private int compressionTag;
    private int directoryNameIndex;
    private int fileNameIndex;
    private int fileId;
    private int rawSize;
    private int compressedSize;
    private long offset;
    private byte[] checksum;
    private int unknownField;
}
