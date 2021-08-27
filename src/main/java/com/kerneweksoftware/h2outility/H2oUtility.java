package com.kerneweksoftware.h2outility;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.DataFormatException;

import com.kerneweksoftware.h2outility.models.ArchivedData;
import com.kerneweksoftware.h2outility.services.ArchiveInput;
import com.kerneweksoftware.h2outility.services.DirectoryOutput;

public class H2oUtility 
{

    public static void main(String[] args) throws IOException, DataFormatException {
        byte[] rawArchive = Files.readAllBytes(Paths.get("C:\\Users\\etste\\Documents\\Projects\\h2o-utility\\archives\\Models.H2O"));
        ByteBuffer archive = ByteBuffer.wrap(rawArchive);
        archive.order(ByteOrder.LITTLE_ENDIAN);

        ArchiveInput archiveInput = new ArchiveInput(archive);
        ArchivedData archiveData = archiveInput.readContents();

        archiveData.setName("Models");

        DirectoryOutput output = new DirectoryOutput(archiveData);
        output.write();
    }
}
