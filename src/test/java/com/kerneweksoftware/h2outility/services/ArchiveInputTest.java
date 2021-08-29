package com.kerneweksoftware.h2outility.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

import com.kerneweksoftware.h2outility.models.unpacked.ArchivedData;
import com.kerneweksoftware.h2outility.models.unpacked.ArchivedFile;
import com.kerneweksoftware.h2outility.models.unpacked.ArchivedFolder;
import com.kerneweksoftware.h2outility.testutils.MockedUnpackedModels;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArchiveInputTest {

    ArchiveInput service;

    @BeforeEach
    void setup() {
        byte[] stringBytes = {0x54, 0x65, 0x73, 0x74, 0x00, 0x41, 0x6E, 0x6F, 0x74, 0x68, 0x65, 0x72, 0x00};
        ByteBuffer buffer = ByteBuffer.wrap(stringBytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        service = new ArchiveInput(buffer);
    }

    @Test
    void test_buildArchivedData_NoFoldersAllFilesUsed() {
        ArchivedFolder[] folders = new ArchivedFolder[0];
        ArchivedFile[] files = new ArchivedFile[3];
        files[0] = MockedUnpackedModels.buildArchivedFile(-1);
        files[1] = MockedUnpackedModels.buildArchivedFile(-1);
        files[2] = MockedUnpackedModels.buildArchivedFile(-1);

        ArchivedData producedData = service.buildArchivedData(folders, files);

        assertEquals(0, producedData.getFolders().length);
        assertEquals(3, producedData.getTopLevelFiles().size());
        assertEquals(0, producedData.getUnusedFiles().size());
    }

    @Test
    void test_buildArchivedData_NoFoldersOneUnusedFile() {
        ArchivedFolder[] folders = new ArchivedFolder[0];
        ArchivedFile[] files = new ArchivedFile[3];
        files[0] = MockedUnpackedModels.buildArchivedFile(-1, null);
        files[1] = MockedUnpackedModels.buildArchivedFile(-1);
        files[2] = MockedUnpackedModels.buildArchivedFile(-1);

        ArchivedData producedData = service.buildArchivedData(folders, files);

        assertEquals(0, producedData.getFolders().length);
        assertEquals(2, producedData.getTopLevelFiles().size());
        assertEquals(1, producedData.getUnusedFiles().size());
    }

    @Test
    void test_buildArchivedData_SmallFolderHierarchy() {
        ArchivedFolder[] folders = new ArchivedFolder[3];
        folders[0] = MockedUnpackedModels.buildArchivedFolder("Example", -1);
        folders[1] = MockedUnpackedModels.buildArchivedFolder("Example\\Child One", 0);
        folders[2] = MockedUnpackedModels.buildArchivedFolder("Example\\Child Two", 0);
        ArchivedFile[] files = new ArchivedFile[3];
        files[0] = MockedUnpackedModels.buildArchivedFile(0);
        files[1] = MockedUnpackedModels.buildArchivedFile(1);
        files[2] = MockedUnpackedModels.buildArchivedFile(2);

        ArchivedData producedData = service.buildArchivedData(folders, files);

        assertEquals(3, producedData.getFolders().length);
        assertEquals(1, producedData.getFolders()[0].getFiles().size());
        assertEquals(1, producedData.getFolders()[1].getFiles().size());
        assertEquals(1, producedData.getFolders()[2].getFiles().size());
        assertEquals(0, producedData.getTopLevelFiles().size());
        assertEquals(0, producedData.getUnusedFiles().size());
    }

    @Test
    void test_buildArchivedData_SmallFolderHierarchyOneUnusedFile() {
        ArchivedFolder[] folders = new ArchivedFolder[3];
        folders[0] = MockedUnpackedModels.buildArchivedFolder("Example", -1);
        folders[1] = MockedUnpackedModels.buildArchivedFolder("Example\\Child One", 0);
        folders[2] = MockedUnpackedModels.buildArchivedFolder("Example\\Child Two", 0);
        ArchivedFile[] files = new ArchivedFile[3];
        files[0] = MockedUnpackedModels.buildArchivedFile(0, null);
        files[1] = MockedUnpackedModels.buildArchivedFile(1);
        files[2] = MockedUnpackedModels.buildArchivedFile(2);

        ArchivedData producedData = service.buildArchivedData(folders, files);

        assertEquals(3, producedData.getFolders().length);
        assertEquals(0, producedData.getFolders()[0].getFiles().size());
        assertEquals(1, producedData.getFolders()[1].getFiles().size());
        assertEquals(1, producedData.getFolders()[2].getFiles().size());
        assertEquals(0, producedData.getTopLevelFiles().size());
        assertEquals(1, producedData.getUnusedFiles().size());
    }
    
    @Test
    void test_getStrings_TwoStrings() {
        byte[] stringBytes = {0x54, 0x00, 0x65, 0x00, 0x73, 0x00, 0x74, 0x00, 0x00, 0x00,
            0x41, 0x00, 0x6E, 0x00, 0x6F, 0x00, 0x74, 0x00, 0x68, 0x00, 0x65, 0x00, 0x72, 0x00, 0x00, 0x00};
        ByteBuffer buffer = ByteBuffer.wrap(stringBytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int count = 2;

        List<String> producedStrings = service.getStrings(buffer, count);

        List<String> expected = Arrays.asList("Test", "Another");
        assertEquals(expected, producedStrings);
    }

    @Test
    void test_getStrings_FourStrings() {
        byte[] stringBytes = {0x54, 0x00, 0x65, 0x00, 0x73, 0x00, 0x74, 0x00, 0x00, 0x00,
            0x41, 0x00, 0x6E, 0x00, 0x6F, 0x00, 0x74, 0x00, 0x68, 0x00, 0x65, 0x00, 0x72, 0x00, 0x00, 0x00,
            0x54, 0x00, 0x68, 0x00, 0x69, 0x00, 0x72, 0x00, 0x64, 0x00, 0x00, 0x00,
            0x54, 0x00, 0x68, 0x00, 0x69, 0x00, 0x72, 0x00, 0x64, 0x00, 0x5c, 0x00, 0x5c, 0x00,
            0x46, 0x00, 0x6f, 0x00, 0x75, 0x00, 0x72, 0x00, 0x74, 0x00, 0x68, 0x00, 0x00, 0x00};
        ByteBuffer buffer = ByteBuffer.wrap(stringBytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int count = 4;

        List<String> producedStrings = service.getStrings(buffer, count);

        List<String> expected = Arrays.asList("Test", "Another", "Third", "Third\\\\Fourth");
        assertEquals(expected, producedStrings);
    }
}
