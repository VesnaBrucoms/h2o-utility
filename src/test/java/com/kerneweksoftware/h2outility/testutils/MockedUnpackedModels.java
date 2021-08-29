package com.kerneweksoftware.h2outility.testutils;

import java.util.ArrayList;
import java.util.List;

import com.kerneweksoftware.h2outility.models.unpacked.ArchivedFile;
import com.kerneweksoftware.h2outility.models.unpacked.ArchivedFolder;

/** Provides methods for quickly building models from the models.unpacked package.
 * 
 * @see ArchivedData
 * @see ArchivedFolder
 * @see ArchivedFile
 */
public class MockedUnpackedModels {

    public static ArchivedFolder buildArchivedFolder(String name, int parentIndex) {
        return buildArchivedFolder(name, parentIndex, new ArrayList<ArchivedFile>());
    }
    
    public static ArchivedFolder buildArchivedFolder(String name, int parentIndex, List<ArchivedFile> files) {
        ArchivedFolder folder = new ArchivedFolder();
        folder.setName(name);
        folder.setParentIndex(parentIndex);
        folder.setFiles(files);
        return folder;
    }
    
    public static ArchivedFile buildArchivedFile(int folderIndex) {
        return buildArchivedFile(0, "Test", folderIndex, new byte[1]);
    }

    public static ArchivedFile buildArchivedFile(int folderIndex, byte[] contents) {
        return buildArchivedFile(0, "Test", folderIndex, contents);
    }
    
    public static ArchivedFile buildArchivedFile(int id, String name, int folderIndex, byte[] contents) {
        ArchivedFile file = new ArchivedFile();
        file.setId(id);
        file.setName(name);
        file.setContents(contents);
        file.setFolderIndex(folderIndex);
        return file;
    }
}
