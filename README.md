# H2O Utility

A tool to view and unpack H2O archives found in the Battle Realms and War of the Ring games, both
developed by Liquid Entertainment.

More information on the structure of the H2O file format can be found in the docs: [Structure of the H2O File Format](docs/structure.md).

## Credits

I would not have been able to get as far as I have, and as quickly, without the work that has already gone into
understanding Liquid Entertainment's H2O file format:
* [H2O File Format](https://battlerealms.fandom.com/wiki/H2O_File_Format)
* [Lord Of The Rings: War Of The Ring H2O](http://wiki.xentax.com/index.php/Lord_Of_The_Rings:_War_Of_The_Ring_H2O)

This project uses the following library to aid in decompression of the archived data:
* [dbc-reader](https://github.com/gcms/dbc-reader)