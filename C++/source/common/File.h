#pragma once

#ifdef __APPLE__
//int proc_pidpath(int pid, void * buffer, uint32_t  buffersize);
#include <libproc.h>
// libproc pulls in something that defines these values
#undef TRUE
#undef FALSE
#endif

#include <sys/types.h>
#include <sys/stat.h>
#include <dirent.h>
#include <fstream>
#include <unistd.h>

#include "Log.h"
#include "Text.h"

MAKE_PTR_TO(File) {
    private:
        Text path;

        template<typename BufferType>
        PtrTo<BufferType> readIntoBuffer () const {
            PtrTo<BufferType> buffer;

            // open the file with the file pointer at the end of the file, and check if that worked
            ifstream file (path, ios::in | ios::binary | ios::ate);
            if (file.is_open()) {
                // get the size of the file and create the buffer to hold it,
                streampos size = file.tellg ();
                buffer = BufferType::make (size);

                // go back to the beginning of the file and read the contents into the buffer
                file.seekg (0, ios::beg);
                file.read ((char*) buffer->fill (size), size);

                // close the file before returning
                file.close();
            }
            return buffer;
        }

        static Text dirname (Text path, const char* separator = "/") {
            // find the last "/"
            int last;
            int current = -1;
            do  {
                last = current;
                current = path.find (separator, current + 1);
            } while (current >= 0);
            return (last >= 0) ? path.substring(0, last + 1) : path;
        }

    public:
        File (const Text& _path) : path (_path) { }

        ~File () {}

        bool getExists () const {
            struct stat stats;
            return (stat (path, &stats) == 0);
        }

        bool isDirectory () const {
            struct stat stats;
            return (stat (path, &stats) == 0) and S_ISDIR(stats.st_mode);
        }

        // return true if the directory is present after the call
        bool makeDirectory () const {
            return (getExists () or (mkdir (path, S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH) == 0));
        }

        vector<PtrToFile> getFiles () const {
            if (isDirectory ()) {
                vector<PtrToFile> files;
                DIR* directory = opendir (path);
                if (directory) {
                    struct dirent* ent;
                    while ((ent = readdir (directory))) {
                        Text name (ent->d_name);
                        if (name.find (".") != 0) {
                            name = (path + "/") << name;
                            Log::trace () << "Found file in directory (" << path << "): " << name << endl;
                            files.push_back (new File (name));
                        }
                    }
                    closedir (directory);
                }
                sort (files.begin (), files.end ());
                return files;
            }
            throw RuntimeError (Text ("File (") << path << ") is not a directory");
        }

        Text getPath () const {
            return path;
        }

        PtrToFile getDirectory () const {
            return new File (dirname (path));
        }

        Text getBasename () const {
            vector<Text> components = path.split ("/");
            Text basename = components.back ();
            vector<Text> nameComponents = basename.split (".");
            if (nameComponents.size () > 1) {
                uint remove = nameComponents.back ().length () + 1;
                basename = basename.substring (0, basename.length () - remove);
            }
            return basename;
        }

        Text getExtension () const {
            Text extension;
            vector<Text> components = path.split (getBasename () + ".");
            if (components.size () > 1) {
                extension = components.back ();
            }
            return extension;
        }

        PtrToBuffer read () const {
            return readIntoBuffer<Buffer> ();
        }

        Text readText () const {
            return Text (readIntoBuffer<RawText> ());
        }

        void write (const char* ptr, long length) const {
            ofstream file (path, ios::out | ios::binary | ios::trunc);
            if (file.is_open()) {
                file.write (ptr, length);

                // close the file before returning
                file.close();
            }
        }

        void writeText (const Text& text) const {
            write (text.get(), text.length ());
        }

        // return true if the target does not exist on completion
        bool remove () const {
            return ((not getExists ()) or (isDirectory () ? (rmdir (path) == 0) : (::remove (path) == 0)));
        }

        static PtrToFile getExecutable () {
            char buffer[PATH_MAX];
            #ifdef __linux
            ssize_t length = readlink("/proc/self/exe", buffer, PATH_MAX);
            #endif
            #ifdef __APPLE__
            int length = proc_pidpath (getpid (), buffer, PATH_MAX);
            #endif
            return (length != -1) ? new File (Text (buffer, length)) : PtrToFile ();
        }

        // given two strings, a path on the left, and a leaf on the right (could be a path), join
        // them to create a valid path. paths with empty leaves will have trailing slashes.
        static Text makePath (const Text& left, const Text& right) {
            // leading slashes on the right side are to be stripped, as their presence indicates an
            // attempt to separate the leaf from the preceding path, which we will do anyway
            Text rightPath = ((right.length () > 0) and (right[0] == '/')) ? right.substring (1) : right;

            // valid values for left are {empty} or ".", "/", "{xxx}", or "{xxx}/" - we condition
            // this so leftPath is always a valid path with no trailing slashes
            Text leftPath = (left.length () == 0) ? "." : left;
            leftPath = (leftPath[leftPath.length () - 1] == '/') ? leftPath.substring(0, leftPath.length () - 1) : leftPath;

            // return the concatenated new path
            return leftPath + "/" + rightPath;
        }

        // open, close
        // read, write
        // readText, writeText
        // read directory
        // delete
        // rename

};
