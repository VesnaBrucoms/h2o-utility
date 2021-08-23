package com.kerneweksoftware.h2outility;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import com.kerneweksoftware.h2outility.models.ArchiveHeader;
import com.kerneweksoftware.h2outility.models.FileHeader;

public class H2oUtility 
{
    static int UNSIGNED_INT_BYTE_SIZE = 4;
    static int FLOAT_BYTE_SIZE = 4;
    static int UNSIGNED_LONG_BYTE_SIZE = 8;
    static int COMMENT_TERMINATOR = 0x1A;

    static int pointer = 0;

    public static void main(String[] args) throws IOException {
        byte[] archive = Files.readAllBytes(Paths.get("C:\\Users\\etste\\Documents\\Projects\\h2o-unpacker\\archives\\Text.H2O"));

        ArchiveHeader header = new ArchiveHeader();
        header.setHeader(getString(archive, pointer + 8));
        header.setVersion1(getFloat(archive));
        header.setComments(getString(archive));
        header.setVersion2(getUnsignedInt(archive));
        header.setFileCount(getUnsignedInt(archive));
        header.setCompressedSize(getUnsignedLong(archive));
        header.setRawSize(getUnsignedLong(archive));
        System.out.println(header.toString());

        FileHeader[] fileHeaders = new FileHeader[header.getFileCount()];
        for (int i = 0; i < header.getFileCount(); i++) {
            FileHeader newHeader = new FileHeader();
            newHeader.setCompressionTag(getUnsignedInt(archive));
            newHeader.setDirectoryNameIndex(getInt(archive));
            newHeader.setFileNameIndex(getInt(archive));
            newHeader.setFileId(getUnsignedInt(archive));
            newHeader.setRawSize(getUnsignedInt(archive));
            newHeader.setCompressedSize(getUnsignedInt(archive));
            newHeader.setOffset(getUnsignedLong(archive));
            newHeader.setChecksum(getBytes(archive, pointer + 4));
            newHeader.setUnknownField(getInt(archive));
            fileHeaders[i] = newHeader;
            System.out.println(newHeader.toString());
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
}
