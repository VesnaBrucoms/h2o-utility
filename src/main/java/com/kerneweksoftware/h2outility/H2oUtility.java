package com.kerneweksoftware.h2outility;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

import com.github.gcms.blast.BlastInputStream;
import com.kerneweksoftware.h2outility.models.ArchiveHeader;
import com.kerneweksoftware.h2outility.models.FileEntry;

public class H2oUtility 
{
    static final int UNSIGNED_INT_BYTE_SIZE = 4;
    static final int FLOAT_BYTE_SIZE = 4;
    static final int UNSIGNED_LONG_BYTE_SIZE = 8;
    static final int COMMENT_TERMINATOR = 0x1A;
    static final int STRING_ARRAY_DELIMITER = 0x00;
    static final int UNSIGNED_INT_MASK = 0xFF;
    static final int UNSIGNED_LONG_MASK = 0xFFFFFFFF;

    static int pointer = 0;

    public static void main(String[] args) throws IOException, DataFormatException {
        byte[] rawArchive = Files.readAllBytes(Paths.get("C:\\Users\\etste\\Documents\\Projects\\h2o-utility\\archives\\Meshes.H2O"));
        ByteBuffer archive = ByteBuffer.wrap(rawArchive);
        archive.order(ByteOrder.LITTLE_ENDIAN);

        ArchiveHeader header = new ArchiveHeader();
        header.setHeader(getString(archive, archive.position() + 8));
        header.setVersion1(archive.getFloat());
        header.setComments(getString(archive));
        header.setVersion2(archive.getInt() & UNSIGNED_INT_MASK);
        header.setFileCount(archive.getInt());
        header.setCompressedSize(archive.getLong() & UNSIGNED_LONG_MASK);
        header.setRawSize(archive.getLong() & UNSIGNED_LONG_MASK);
        System.out.println(header.toString());

        FileEntry[] fileEntries = new FileEntry[header.getFileCount()];
        for (int i = 0; i < header.getFileCount(); i++) {
            FileEntry newHeader = new FileEntry();
            newHeader.setCompressionTag(archive.getInt() & UNSIGNED_INT_MASK);
            newHeader.setFolderNameIndex(archive.getInt());
            newHeader.setFileNameIndex(archive.getInt());
            newHeader.setFileId(archive.getInt());
            newHeader.setRawSize(archive.getInt() & UNSIGNED_INT_MASK);
            newHeader.setCompressedSize(archive.getInt() & UNSIGNED_INT_MASK);
            newHeader.setOffset(archive.getLong() & UNSIGNED_LONG_MASK);
            byte[] checksum = new byte[4];
            archive.get(checksum, 0, 4);
            newHeader.setChecksum(checksum);
            newHeader.setUnknownField(archive.getInt());
            fileEntries[i] = newHeader;
            System.out.println(newHeader.toString());
        }

        List<String> folderNames = getNames(archive);
        System.out.println(folderNames.toString());

        List<String> fileNames = getNames(archive);
        System.out.println(fileNames.toString());

        // FOLDER STRUCTURE/MAP
        int folderCount = archive.getInt();
        System.out.println("Folder count: " + folderCount);
        for (int i = 0; i < folderCount; i++) {
            System.out.println("Parent folder index: " + archive.getInt());
        }

        for (int i = 0; i < fileEntries.length; i++) {
            if (fileEntries[i].getCompressionTag() == 0) {
                // get all bytes, do nothing extra. Use fileheaders[i].getRawSize()
            } else {
                // get usual compression header
            }
        }
        System.out.println("Unknown: " + archive.getInt());
    }

    static String getString(ByteBuffer archive, int to) {
        StringBuilder sb = new StringBuilder();
        for (int i = pointer; i < to; i++) {
            sb.append((char) archive.get());
        }
        pointer = to;
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

    static List<String> getNames(ByteBuffer archive) {
        int compressedSize = archive.getInt();
        int rawSize = archive.getInt();
        byte[] checksum = new byte[4];
        archive.get(checksum, 0, 4);

        List<String> names = new ArrayList<>();
        if (compressedSize == rawSize) {
            int count = archive.getInt();
            int size = archive.getInt();

            names = getStrings(archive, count);
        } else {
            byte[] compressed = new byte[compressedSize];
            archive.get(compressed, 0, compressedSize);
            byte[] decompressedBytes = decompress(compressed);

            // check decompressed data is accurate

            ByteBuffer decompressed = ByteBuffer.wrap(decompressedBytes);
            decompressed.order(ByteOrder.LITTLE_ENDIAN);
            int count = decompressed.getInt();
            int size = decompressed.getInt();

            names = getStrings(decompressed, count);
        }
        return names;
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
