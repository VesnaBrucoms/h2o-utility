package com.kerneweksoftware.h2outility.models;

import lombok.Data;

/**
 * Header structure containing information on the following compressed data in the input stream.
 */
@Data
public class CompressedDataHeader {
    private int compressedSize;
    private int rawSize;
    private int checksum;
}
