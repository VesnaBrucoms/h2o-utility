package com.kerneweksoftware.h2outility.services;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.kerneweksoftware.h2outility.models.ArchivedData;
import com.kerneweksoftware.h2outility.models.ArchivedFile;
import com.kerneweksoftware.h2outility.models.ArchivedFolder;

public class DirectoryOutput {
    
    private final ArchivedData archive;

    public DirectoryOutput(ArchivedData archive) {
        this.archive = archive;
    }

    public void write() throws IOException {
        File parentDirectory = new File(archive.getName());
        if (!parentDirectory.exists()) {
            parentDirectory.mkdir();
        }
        writeFiles(archive.getTopLevelFiles(), parentDirectory.getAbsolutePath());

        for (ArchivedFolder folder : archive.getFolders()) {
            File newFolder = new File(parentDirectory.getAbsolutePath() + "\\" + folder.getName());
            newFolder.mkdir();

            writeFiles(folder.getFiles(), parentDirectory.getAbsolutePath() + "\\" + folder.getName());
        }
    }

    private void writeFiles(List<ArchivedFile> files, String parentDirectories) throws IOException {
        for (ArchivedFile file : files) {
            DataOutputStream output = new DataOutputStream(new FileOutputStream(parentDirectories + "\\" + file.getName()));
            try {
                output.write(file.getContents());
            } finally {
                output.close();
            }
        }
    }
}
