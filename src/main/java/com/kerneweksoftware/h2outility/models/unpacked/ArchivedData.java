package com.kerneweksoftware.h2outility.models.unpacked;

import java.util.List;

import lombok.Data;

@Data
public class ArchivedData {
    private String name;
    private ArchivedFolder[] folders;
    private List<ArchivedFile> topLevelFiles;
}
