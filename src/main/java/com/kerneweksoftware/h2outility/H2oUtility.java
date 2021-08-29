package com.kerneweksoftware.h2outility;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.zip.DataFormatException;

import com.kerneweksoftware.h2outility.exceptions.DecompressionException;
import com.kerneweksoftware.h2outility.exceptions.IncorrectFileTypeException;
import com.kerneweksoftware.h2outility.models.unpacked.ArchivedData;
import com.kerneweksoftware.h2outility.services.ArchiveInput;
import com.kerneweksoftware.h2outility.services.DirectoryOutput;

public class H2oUtility 
{

    public static void main(String[] args) throws IOException, DataFormatException {
        String archivePath = "";
        String archiveName = "";
        if (args.length == 0) {
            archivePath = getUserInput();
            archiveName = getArchiveName(archivePath);
        } else {
            archivePath = args[args.length - 1];
            archiveName = getArchiveName(archivePath);
        }

        ByteBuffer archive = readArchive(archivePath);

        try {
            ArchiveInput archiveInput = new ArchiveInput(archive);
            ArchivedData archiveData = archiveInput.readContents();

            archiveData.setName(archiveName.replace(".H2O", ""));
            DirectoryOutput output = new DirectoryOutput(archiveData);
            output.write();
        } catch (IncorrectFileTypeException e) {
            System.out.println(e.getMessage());
        } catch (DecompressionException e) {
            System.out.println(e.getMessage());
        }
    }

    protected static String getUserInput() {
        String path = "";
        System.out.println("Please provide the path of the archive to unpack");
        System.out.print("-> ");
        Scanner sc = new Scanner(System.in);
        try {
            path = sc.next();
        } finally {
            sc.close();
        }
        return path;
    }

    protected static String getArchiveName(String path) {
        var name = "";
        if (path.contains("\\")) {
            String[] splitPath = path.split("\\\\");
            name = splitPath[splitPath.length - 1];
        } else {
            name = path;
        }
        return name;
    }

    protected static ByteBuffer readArchive(String path) throws IOException {
        byte[] rawArchive = Files.readAllBytes(Paths.get(path));
        ByteBuffer archive = ByteBuffer.wrap(rawArchive);
        archive.order(ByteOrder.LITTLE_ENDIAN);
        return archive;
    }
}
