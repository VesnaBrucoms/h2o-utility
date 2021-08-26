package com.kerneweksoftware.h2outility.models;

import lombok.Data;

@Data
public class DirectoryHeader {
    private int compressedSize;
    private int rawSize;
    private byte[] checksum;
    private DirectoryData data;
}
