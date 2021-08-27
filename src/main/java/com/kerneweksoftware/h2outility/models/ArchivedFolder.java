package com.kerneweksoftware.h2outility.models;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ArchivedFolder {
    private String name;
    private int parentIndex;
    private List<ArchivedFile> files = new ArrayList<>();
}
