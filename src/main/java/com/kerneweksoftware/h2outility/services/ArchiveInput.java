package com.kerneweksoftware.h2outility.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import com.github.gcms.blast.BlastInputStream;
import com.kerneweksoftware.h2outility.models.ArchiveHeader;
import com.kerneweksoftware.h2outility.models.ArchivedData;
import com.kerneweksoftware.h2outility.models.ArchivedFile;
import com.kerneweksoftware.h2outility.models.ArchivedFolder;
import com.kerneweksoftware.h2outility.models.CompressedDataHeader;
import com.kerneweksoftware.h2outility.models.FileEntry;

/**
 * Handles reading of a H2O archive from a ByteBuffer.
 */
public class ArchiveInput {

    static final int UNSIGNED_INT_MASK = 0xFF;
    static final int UNSIGNED_LONG_MASK = 0xFFFFFFFF;
    static final int COMMENT_TERMINATOR = 0x1A;
    static final int STRING_ARRAY_DELIMITER = 0x00;

    private final ByteBuffer archive;
    
    /**
     * Instantiate new ArchiveInput from ByteBuffer.
     * 
     * @param archive Buffer of read H2O archive in little endian order.
     */
    public ArchiveInput(ByteBuffer archive) {
        this.archive = archive;
    }

    /**
     * Instantiate new ArchiveInput from byte array.
     * 
     * Wraps the given byte array into a ByteBuffer and sets the byte order to little endian.
     * 
     * @param archive Array of read H2O archive.
     */
    public ArchiveInput(byte[] archive) {
        ByteBuffer buffer = ByteBuffer.wrap(archive);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        this.archive = buffer;
    }

    /**
     * Reads the contents of the given archive.
     * 
     * @return {@link ArchivedData} containing the folders and files of the archive.
     */
    public ArchivedData readContents() {
        FileEntry[] fileEntries = processTopLevelInfo();

        List<String> folderNames = getNames();
        List<String> fileNames = getNames();
        int[] folderParentIndexes = readFolderParentIndexes();

        ArchivedFolder[] folders = buildFolders(folderNames, folderParentIndexes);
        ArchivedFile[] files = buildFiles(fileEntries, fileNames);

        readAndAssignFileData(fileEntries, files);

        return buildArchivedData(folders, files);
    }

    private FileEntry[] processTopLevelInfo() {
        ArchiveHeader header = readHeader();
        return readFileEntries(header.getFileCount());
    }

    private ArchiveHeader readHeader() {
        ArchiveHeader header = new ArchiveHeader();
        header.setMagicNumber(getString(archive, archive.position() + 8));
        header.setVersion1(archive.getFloat());
        header.setComments(getString(archive));
        header.setVersion2(archive.getInt() & UNSIGNED_INT_MASK);
        header.setFileCount(archive.getInt());
        header.setCompressedSize(archive.getLong() & UNSIGNED_LONG_MASK);
        header.setRawSize(archive.getLong() & UNSIGNED_LONG_MASK);
        if (header.getMagicNumber() != "LIQDLH2O") {
            System.out.println("WRONG FILE TYPE");
        }
        return header;
    }

    private FileEntry[] readFileEntries(int fileCount) {
        FileEntry[] fileEntries = new FileEntry[fileCount];
        for (int i = 0; i < fileCount; i++) {
            FileEntry fileEntry = new FileEntry();
            fileEntry.setCompressionTag(archive.getInt() & UNSIGNED_INT_MASK);
            fileEntry.setFolderNameIndex(archive.getInt());
            fileEntry.setFileNameIndex(archive.getInt());
            fileEntry.setFileId(archive.getInt());
            fileEntry.setRawSize(archive.getInt() & UNSIGNED_INT_MASK);
            fileEntry.setCompressedSize(archive.getInt() & UNSIGNED_INT_MASK);
            fileEntry.setOffset(archive.getLong() & UNSIGNED_LONG_MASK);
            byte[] checksum = new byte[4];
            archive.get(checksum, 0, 4);
            fileEntry.setChecksum(checksum);
            fileEntry.setUnknownField(archive.getInt());

            fileEntries[i] = fileEntry;
        }
        return fileEntries;
    }

    private List<String> getNames() {
        CompressedDataHeader header = new CompressedDataHeader();
        header.setCompressedSize(archive.getInt());
        header.setRawSize(archive.getInt());
        header.setChecksum(archive.getInt());

        List<String> names = new ArrayList<>();
        if (header.getCompressedSize() == header.getRawSize()) {
            int count = archive.getInt();
            int size = archive.getInt();

            names = getStrings(archive, count);
        } else {
            byte[] compressed = new byte[header.getCompressedSize()];
            archive.get(compressed, 0, header.getCompressedSize());
            byte[] decompressedBytes = decompress(compressed);

            // check decompressed data is accurate using checksum

            ByteBuffer decompressed = ByteBuffer.wrap(decompressedBytes);
            decompressed.order(ByteOrder.LITTLE_ENDIAN);
            int count = decompressed.getInt();
            int size = decompressed.getInt();

            names = getStrings(decompressed, count);
        }
        return names;
    }

    private ArchivedFolder[] buildFolders(List<String> folderNames, int[] folderParentIndexes) {
        ArchivedFolder[] folders = new ArchivedFolder[folderNames.size()];
        for (int i = 0; i < folderNames.size(); i++) {
            ArchivedFolder newFolder = new ArchivedFolder();
            newFolder.setName(folderNames.get(i));
            newFolder.setParentIndex(folderParentIndexes[i]);
            folders[i] = newFolder;
        }
        return folders;
    }

    private ArchivedFile[] buildFiles(FileEntry[] fileEntries, List<String> fileNames) {
        ArchivedFile[] files = new ArchivedFile[fileEntries.length];
        for (int i = 0; i < fileEntries.length; i++) {
            ArchivedFile newFile = new ArchivedFile();
            newFile.setId(fileEntries[i].getFileId());
            int fileNameIndex = fileEntries[i].getFileNameIndex();
            if (fileNameIndex >= 0) {
                newFile.setName(fileNames.get(fileNameIndex));
            } else {
                newFile.setName("UNUSED");
            }
            newFile.setFolderIndex(fileEntries[i].getFolderNameIndex());
            files[i] = newFile;
        }
        return files;
    }

    private int[] readFolderParentIndexes() {
        int count = archive.getInt();
        int[] folders = new int[count];
        for (int i = 0; i < count; i++) {
            folders[i] = archive.getInt();
        }
        return folders;
    }

    private void readAndAssignFileData(FileEntry[] fileEntries, ArchivedFile[] files) {
        for (int i = 0; i < fileEntries.length; i++) {
            if (fileEntries[i].getCompressionTag() == 0) {
                byte[] data = new byte[fileEntries[i].getRawSize()];
                archive.get(data, 0, fileEntries[i].getRawSize());
                files[i].setContents(data);
            } else {
                if (fileEntries[i].getOffset() != 0) {
                    CompressedDataHeader comp = new CompressedDataHeader();
                    comp.setCompressedSize(archive.getInt());
                    comp.setRawSize(archive.getInt());
                    comp.setChecksum(archive.getInt());

                    byte[] data = new byte[comp.getCompressedSize()];
                    archive.get(data, 0, comp.getCompressedSize());
                    byte[] decompressedBytes = decompress(data);

                    if (decompressedBytes.length == comp.getRawSize()) {
                        files[i].setContents(decompressedBytes);
                    }
                } else {
                    System.out.println("Unused file");
                }
            }
        }
    }

    private ArchivedData buildArchivedData(ArchivedFolder[] folders, ArchivedFile[] files) {
        List<ArchivedFile> topLevelFiles = new ArrayList<>();
        for (ArchivedFile file : files) {
            if (file.getContents() == null) {
                continue;
            }

            int folderIndex = file.getFolderIndex();
            if (folderIndex == -1) {
                topLevelFiles.add(file);
            } else {
                folders[folderIndex].getFiles().add(file);
            }
        }

        ArchivedData data = new ArchivedData();
        data.setFolders(folders);
        data.setTopLevelFiles(topLevelFiles);
        return data;
    }

    static String getString(ByteBuffer archive, int to) {
        StringBuilder sb = new StringBuilder();
        for (int i = archive.position(); i < to; i++) {
            sb.append((char) archive.get());
        }
        return sb.toString();
    }

    static String getString(ByteBuffer archive) {
        StringBuilder sb = new StringBuilder();
        boolean hasReachedTerminator = false;
        while (!hasReachedTerminator) {
            byte currentByte = archive.get();
            if (currentByte == COMMENT_TERMINATOR) {
                hasReachedTerminator = true;
            } else {
                sb.append((char) currentByte);
            }
        }
        return sb.toString();
    }

    static byte[] decompress(byte[] compressedData) {
        try {
            ByteArrayInputStream compressedStream = new ByteArrayInputStream(compressedData);
            BlastInputStream blastStream = new BlastInputStream(compressedStream);
            byte[] result = blastStream.readAllBytes();
            blastStream.close();
            return result;
        } catch (IOException e) {
            return new byte[1];
        }
    }

    static List<String> getStrings(ByteBuffer buffer, int count) {
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            StringBuilder sb = new StringBuilder();
            boolean isDelimiter = false;
            while (!isDelimiter) {
                char character = (char) buffer.getShort();
                if (character == STRING_ARRAY_DELIMITER) {
                    strings.add(sb.toString());
                    isDelimiter = true;
                } else {
                    sb.append(character);
                }
            }
        }
        return strings;
    }
}
