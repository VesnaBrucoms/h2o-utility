package com.kerneweksoftware.h2outility.services;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.kerneweksoftware.h2outility.models.ArchivedData;
import com.kerneweksoftware.h2outility.models.ArchivedFile;
import com.kerneweksoftware.h2outility.models.ArchivedFolder;

public class DirectoryOutput {
    
    private final ArchivedData archive;

    public DirectoryOutput(ArchivedData archive) {
        this.archive = archive;
    }

    public void write() throws IOException {
        // write top level first
        File parentDirectory = new File(archive.getName());
        if (!parentDirectory.exists()) {
            parentDirectory.mkdir();
        }
        for (ArchivedFile file : archive.getTopLevelFiles()) {
            DataOutputStream output = new DataOutputStream(new FileOutputStream(parentDirectory.getAbsolutePath() + "\\" + file.getName()));
            try {
                output.write(file.getContents());
            } finally {
                output.close();
            }
        }
        // write folders next
        for (ArchivedFolder folder : archive.getFolders()) {
            File newFolder = new File(parentDirectory.getAbsolutePath() + "\\" + folder.getName());
            newFolder.mkdir();

            for (ArchivedFile file : folder.getFiles()) {
                DataOutputStream output = new DataOutputStream(new FileOutputStream(parentDirectory.getAbsolutePath() + "\\" + folder.getName() + "\\" + file.getName()));
                try {
                    output.write(file.getContents());
                } finally {
                    output.close();
                }
            }
        }
    }
}
