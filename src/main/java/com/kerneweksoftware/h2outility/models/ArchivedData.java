package com.kerneweksoftware.h2outility.models;

import java.util.List;

import lombok.Data;

@Data
public class ArchivedData {
    private ArchivedFolder[] folders;
    private List<ArchivedFile> topLevelFiles;
}
