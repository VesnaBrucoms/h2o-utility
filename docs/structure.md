# Structure of the H2O File Format

H2O files are broken down into six sections:
* header
* file entries
* folder names
* file names
* folder structure
* data

# Header

The header stores the general archive information. Such as total compressed archive size, and
number of files archived.

    char    (8)     magicNumber - always LIQDLH2O
    float   (4)     version1 - always 6.0
    char    (x)     comments - terminated with 0x1A
    uint32  (4)     version2 - always 6
    int32   (4)     fileCount - number of files archived
    ulong   (8)     compressedSize - compressed size of archive in bytes
    ulong   (8)     rawSize - uncompressed size of archive in bytes

## File Entries

The file entries is a sequence of the following structure repeated by `header.fileCount`. Each file
entry stores specific information about each archived file.

    uint32  (4)     compressionTag
    int32   (4)     folderNameIndex
    int32   (4)     fileNameIndex
    int32   (4)     fileId
    uint32  (4)     rawSize
    uint32  (4)     compressedSize
    ulong   (8)     offset
    int32   (4)     checksum
    int32   (4)     unknownField

## Folder Names

todo

## File Names

todo

## Folder Structure

todo

## Data

todo