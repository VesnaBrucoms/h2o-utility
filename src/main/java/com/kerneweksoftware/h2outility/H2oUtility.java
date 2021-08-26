package com.kerneweksoftware.h2outility;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

import com.github.gcms.blast.BlastInputStream;
import com.kerneweksoftware.h2outility.models.ArchiveHeader;
import com.kerneweksoftware.h2outility.models.DirectoryData;
import com.kerneweksoftware.h2outility.models.DirectoryHeader;
import com.kerneweksoftware.h2outility.models.FileHeader;

public class H2oUtility 
{
    static int UNSIGNED_INT_BYTE_SIZE = 4;
    static int FLOAT_BYTE_SIZE = 4;
    static int UNSIGNED_LONG_BYTE_SIZE = 8;
    static int COMMENT_TERMINATOR = 0x1A;

    static int pointer = 0;

    public static void main(String[] args) throws IOException, DataFormatException {
        byte[] archive = Files.readAllBytes(Paths.get("C:\\Users\\etste\\Documents\\Projects\\h2o-utility\\archives\\Models.H2O"));

        ArchiveHeader header = new ArchiveHeader();
        header.setHeader(getString(archive, pointer + 8));
        header.setVersion1(getFloat(archive));
        header.setComments(getString(archive));
        header.setVersion2(getUnsignedInt(archive));
        header.setFileCount(getInt(archive));
        header.setCompressedSize(getUnsignedLong(archive));
        header.setRawSize(getUnsignedLong(archive));
        System.out.println(header.toString());

        FileHeader[] fileHeaders = new FileHeader[header.getFileCount()];
        for (int i = 0; i < header.getFileCount(); i++) {
            FileHeader newHeader = new FileHeader();
            newHeader.setCompressionTag(getUnsignedInt(archive));
            newHeader.setDirectoryNameIndex(getInt(archive));
            newHeader.setFileNameIndex(getInt(archive));
            newHeader.setFileId(getInt(archive));
            newHeader.setRawSize(getUnsignedInt(archive));
            newHeader.setCompressedSize(getUnsignedInt(archive));
            newHeader.setOffset(getUnsignedLong(archive));
            newHeader.setChecksum(getBytes(archive, pointer + 4));
            newHeader.setUnknownField(getInt(archive));
            fileHeaders[i] = newHeader;
            System.out.println(newHeader.toString());
        }

        DirectoryHeader folderNameDirectory = new DirectoryHeader();
        folderNameDirectory.setCompressedSize(getInt(archive));
        folderNameDirectory.setRawSize(getInt(archive));
        folderNameDirectory.setChecksum(getBytes(archive, pointer + 4));
        DirectoryData folderNames = new DirectoryData();
        if (folderNameDirectory.getCompressedSize() == folderNameDirectory.getRawSize()) {
            folderNames.setCount(getUnsignedInt(archive));
            folderNames.setSize(getUnsignedInt(archive));
        } else {
            byte[] compressed = Arrays.copyOfRange(archive, pointer, pointer + folderNameDirectory.getCompressedSize());
            byte[] decompressed = decompress(compressed);

            ByteBuffer wrappedBytes = ByteBuffer.wrap(decompressed);
            wrappedBytes.order(ByteOrder.LITTLE_ENDIAN);
            folderNames.setCount(wrappedBytes.getInt() & 0xFF);
            folderNames.setSize(wrappedBytes.getInt() & 0xFF);

            List<String> data = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            while (wrappedBytes.remaining() > 0) {
                char character = (char) wrappedBytes.getShort();
                if (character != 0x00) {
                    sb.append(character);
                } else {
                    data.add(sb.toString());
                    sb = new StringBuilder();
                }
            }

            pointer += folderNameDirectory.getRawSize(); // add check to ensure decomp'd array equals this
        }
        System.out.println(folderNameDirectory.toString());

        DirectoryHeader filenameDirectory = new DirectoryHeader();
        filenameDirectory.setCompressedSize(getInt(archive));
        filenameDirectory.setRawSize(getInt(archive));
        filenameDirectory.setChecksum(getBytes(archive, pointer + 4));
        DirectoryData fileNames = new DirectoryData();
        if (filenameDirectory.getCompressedSize() == filenameDirectory.getRawSize()) {
            fileNames.setCount(getUnsignedInt(archive));
            fileNames.setSize(getUnsignedInt(archive));
        } else {
            byte[] compressed = Arrays.copyOfRange(archive, pointer, pointer + filenameDirectory.getCompressedSize());
            byte[] decompressed = decompress(compressed);

            ByteBuffer wrappedBytes = ByteBuffer.wrap(decompressed);
            wrappedBytes.order(ByteOrder.LITTLE_ENDIAN);
            fileNames.setCount(wrappedBytes.getInt() & 0xFF);
            fileNames.setSize(wrappedBytes.getInt() & 0xFF);

            List<String> data = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            while (wrappedBytes.remaining() > 0) {
                char character = (char) wrappedBytes.getShort();
                if (character != 0x00) {
                    sb.append(character);
                } else {
                    data.add(sb.toString());
                    sb = new StringBuilder();
                }
            }
            // NEXT: do string decoding properly AND catch up of licensing for PKWARE DCL Java library
            fileNames.setData((String[]) data.toArray());

            pointer += filenameDirectory.getRawSize(); // add check to ensure decomp'd array equals this
        }
    }

    static String getString(byte[] archive, int to) {
        StringBuilder sb = new StringBuilder();
        for (int i = pointer; i < to; i++) {
            sb.append((char) archive[i]);
        }
        pointer = to;
        return sb.toString();
    }

    static String getString(byte[] archive) {
        StringBuilder sb = new StringBuilder();
        for (int i = pointer; i < archive.length; i++) {
            if (archive[i] == COMMENT_TERMINATOR) {
                System.out.println("Comments end at: " + i);
                pointer = i + 1;
                break;
            } else {
                sb.append((char) archive[i]);
            }
        }
        return sb.toString();
    }

    static float getFloat(byte[] archive) {
        byte[] floatBytes = Arrays.copyOfRange(archive, pointer, pointer + FLOAT_BYTE_SIZE);
        ByteBuffer wrappedBytes = ByteBuffer.wrap(floatBytes);
        wrappedBytes.order(ByteOrder.LITTLE_ENDIAN);

        pointer += FLOAT_BYTE_SIZE;

        return wrappedBytes.getFloat();
    }

    static int getUnsignedInt(byte[] archive) {
        byte[] uIntBytes = Arrays.copyOfRange(archive, pointer, pointer + UNSIGNED_INT_BYTE_SIZE);
        ByteBuffer wrappedBytes = ByteBuffer.wrap(uIntBytes);
        wrappedBytes.order(ByteOrder.LITTLE_ENDIAN);

        pointer += UNSIGNED_INT_BYTE_SIZE;

        return wrappedBytes.getInt() & 0xFF;
    }

    static long getUnsignedLong(byte[] archive) {
        byte[] uLongBytes = Arrays.copyOfRange(archive, pointer, pointer + UNSIGNED_LONG_BYTE_SIZE);
        ByteBuffer wrappedBytes = ByteBuffer.wrap(uLongBytes);
        wrappedBytes.order(ByteOrder.LITTLE_ENDIAN);

        pointer += UNSIGNED_LONG_BYTE_SIZE;

        return wrappedBytes.getLong() & 0xFFFFFFFF;
    }

    static int getInt(byte[] archive) {
        byte[] uIntBytes = Arrays.copyOfRange(archive, pointer, pointer + UNSIGNED_INT_BYTE_SIZE);
        ByteBuffer wrappedBytes = ByteBuffer.wrap(uIntBytes);
        wrappedBytes.order(ByteOrder.LITTLE_ENDIAN);

        pointer += UNSIGNED_INT_BYTE_SIZE;

        return wrappedBytes.getInt();
    }

    static byte[] getBytes(byte[] archive, int to) {
        int sizeOfArray = to - pointer;
        byte[] bytes = new byte[sizeOfArray];
        for (int i = 0; i < sizeOfArray; i++) {
            bytes[i] = archive[pointer + i];
        }
        pointer += sizeOfArray;
        return bytes;
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
}
