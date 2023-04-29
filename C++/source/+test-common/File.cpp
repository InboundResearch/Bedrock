#include "Test.h"
#include "File.h"

TEST_MODULE_DEPENDENCIES (File, "Text")

TEST_CASE (Exists) {
    File file ("test.txt");
    TEST_TRUE(file.getExists ());

    File directory ("..");
    TEST_TRUE(directory.getExists ());;

    File junk ("nonexistent-file.txt");
    TEST_FALSE(junk.getExists ());
}

TEST_CASE(Basename) {
    File file ("./test.txt");
    TEST_EQUALS(file.getPath (), "./test.txt");
    TEST_EQUALS(file.getBasename (), "test");
    TEST_EQUALS(file.getExtension (), "txt");

    File file2 ("./dir/dir2");
    TEST_TRUE(file2.getExists ());
    TEST_TRUE(file2.isDirectory ());
    TEST_EQUALS(file2.getPath (), "./dir/dir2");
    TEST_EQUALS(file2.getBasename (), "dir2");
    TEST_EQUALS(file2.getExtension (), "");

    File file3 ("./dir/dir2/test.long.txt");
    TEST_TRUE(file3.getExists ());
    TEST_EQUALS(file3.getPath (), "./dir/dir2/test.long.txt");
    TEST_EQUALS(file3.getBasename (), "test.long");
    TEST_EQUALS(file3.getExtension (), "txt");
}

TEST_CASE(IsDirectory) {
    //Log::Scope scope (Log::DEBUG);

    File file ("test.txt");
    TEST_FALSE(file.isDirectory ());

    File directory ("..");
    TEST_TRUE(directory.isDirectory ());

    File junk ("nonexistent-file.txt");
    TEST_FALSE(junk.isDirectory ());

    vector<PtrToFile> files =  directory.getFiles ();
    TEST_XYOP(files.size (), 0, >);
    TEST_TRUE((files[0]->getBasename () == "debug") || (files[0]->getBasename () == "release"));

    Log::debug () << files.size () << " files in directory (" << directory.getPath () << ")" << endl;
}

TEST_CASE(Dirname) {
    //Log::Scope scope (Log::DEBUG);

    PtrToFile executable = File::getExecutable();
    Log::debug() << "Executable: " << executable->getPath() << endl;
    TEST_TRUE(executable);

    PtrToFile directory = executable->getDirectory();
    TEST_TRUE(directory->isDirectory ());
    Log::debug() << "Executable Path: " << directory->getPath() << endl;
}

TEST_CASE(Read) {
    File    file ("test.txt");
    PtrToBuffer buffer = file.read ();
    byte compare[] = { 'T', 'e', 's', 't', '\n' };
    TEST_EQUALS(buffer->getLength (), 5);
    TEST_EQUALS(buffer->compare (compare, 5), 0);
}

TEST_CASE(ReadText) {
    File file ("test.txt");
    Text text = file.readText ();
    TEST_EQUALS(text, "Test\n");
}

TEST_CASE(MakePath) {
    // happy paths
    TEST_EQUALS(File::makePath("/", "yyy"), "/yyy");
    TEST_EQUALS(File::makePath("xxx", "yyy"), "xxx/yyy");
    TEST_EQUALS(File::makePath("xxx/", "yyy"), "xxx/yyy");
    TEST_EQUALS(File::makePath("xxx", "/yyy"), "xxx/yyy");
    TEST_EQUALS(File::makePath("xxx/", "/yyy"), "xxx/yyy");

    // adversarial paths
    TEST_EQUALS(File::makePath("xxx", ""), "xxx/");
    TEST_EQUALS(File::makePath("xxx", "/"), "xxx/");

    TEST_EQUALS(File::makePath("", ""), "./");
    TEST_EQUALS(File::makePath("", "/"), "./");
    TEST_EQUALS(File::makePath("", "yyy"), "./yyy");

    TEST_EQUALS(File::makePath("/", ""), "/");
    TEST_EQUALS(File::makePath("/", "/"), "/");
}

TEST_CASE(MakeDirectory) {
    // make sure there is no directory, create one, make sure it's there, then clean up and make
    // sure there is no directory again
    File testDirectory ("testDir");
    TEST_TRUE(testDirectory.remove ());
    TEST_TRUE(testDirectory.makeDirectory ());
    TEST_TRUE(testDirectory.getExists ());
    TEST_TRUE(testDirectory.remove ());
    TEST_FALSE(testDirectory.getExists ());
}
