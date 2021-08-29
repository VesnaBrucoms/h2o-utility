package com.kerneweksoftware.h2outility.models.unpacked;

import lombok.Data;

@Data
public class ArchivedFile {
    private int id;
    private String name;
    private int folderIndex;
    private byte[] contents;
}
