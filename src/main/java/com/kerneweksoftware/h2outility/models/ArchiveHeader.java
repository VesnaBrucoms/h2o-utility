package com.kerneweksoftware.h2outility.models;

import lombok.Data;

/**
 * File header of the H2O format.
 * 
 * First of six structures. Details the number of archived files, the archive's raw size in bytes, and the archive's
 * compressed size in bytes, amongst other fields. The header contains (read in this order):
 * 
 * <p><b>magicNumber</b> - Format identifier. Always LIQDLH20.</p>
 * <p><b>version1</b> - Float representation of format version. Always 6.0.</p>
 * <p><b>comments</b> - User comments.</p>
 * <p><b>version2</b> - Int representation of format version. Always 6.</p>
 * <p><b>fileCount</b> - Number of files in this archive.</p>
 * <p><b>compressedSize</b> - Size of the archive compressed.</p>
 * <p><b>rawSize</b> - Size of the archive uncompressed.</p>
 */
@Data
public class ArchiveHeader {
    private String magicNumber;
    private float version1;
    private String comments;
    private int version2;
    private int fileCount;
    private long compressedSize;
    private long rawSize;
}
