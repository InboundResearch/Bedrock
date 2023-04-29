#pragma once

//#include "Log.h"
#include "File.h"
#include "Json.h"
#include "Bag.h"

class UnitTest {
    private:
        static int logLevelFromText (const Text& configuration) {
            if (configuration) {
                if (configuration == "TRACE") {
                    return static_cast<int> (Log::TRACE);
                }
                if (configuration == "DEBUG") {
                    return static_cast<int> (Log::DEBUG);
                }
                if (configuration == "INFO") {
                    return static_cast<int> (Log::INFO);
                }
                if (configuration == "WARN") {
                    return static_cast<int> (Log::WARN);
                }
                if (configuration == "ERROR") {
                    return static_cast<int> (Log::ERROR);
                }
                if (configuration == "SKIP") {
                    return -1;
                }
            }
            return static_cast<int> (Log::WARN);
        }

    protected:
        Text name;
        Text module;
        uint assertionsCount;
        bool call;

        static UnitTest* currentUnitTest;
        static BagObject* configurationObject;

        // registry is a module::testname split, test dependencies are typically at a module level
        static TextMap<TextMap<UnitTest*>>* registry;

        void callTest () {
            if (call) {
                try {
                    currentUnitTest = this;
                    int logLevel = getLogLevel ();
                    if (logLevel >= 0) {
                        Log::info () << "TEST: (" << module << "." << name << ")" << endl;
                        {
                            Log::Scope scope (static_cast<uint> (logLevel));
                            test ();
                        }
                        if (assertionsCount > 0) {
                            Log::debug () << "PASSED: " << name << endl << endl;
                        } else {
                            Log::error () << "EMPTY: " << name << " (made no assertions)" << endl << endl;
                            exit (EXIT_FAILURE);
                        }
                    } else {
                        Log::info () << "SKIP: (" << module << ") " << name << endl;
                    }
                } catch (RuntimeError& runtimeError) {
                    Log::exception (runtimeError);
                    exit (EXIT_FAILURE);
                } catch (exception& exception) {
                    cerr << "[OTHER] " << exception.what () << endl;
                    exit (EXIT_FAILURE);
                } catch (...) {
                    cerr << "[ UNKN] exiting..." << endl;
                    exit (EXIT_FAILURE);
                }
            }
        }

        int getLogLevel () {
            int logLevel = static_cast<int> (Log::WARN);
            if (not configurationObject) {
                PtrToBagObject ptrToConfigurationObject;

                // try to read from a configuration file
                File configurationFile ("unit-test-configuration.json");
                if (configurationFile.getExists ()) {
                    Text configurationText = configurationFile.readText();
                    ptrToConfigurationObject = Json::readBagObject(configurationText).getPtr();
                }

                if (not ptrToConfigurationObject) {
                    ptrToConfigurationObject = new BagObject ();
                }

                // orphan this object so it doesn't get deleted when this scope exits
                ptrToConfigurationObject->addRef();
                configurationObject = ptrToConfigurationObject.getPtr();
            }

            if (configurationObject) {
                // get the module object, creating it if necessary
                if (not configurationObject->contains (module)) {
                    configurationObject->put (module, PtrToBagObject (new BagObject ()));
                }
                PtrToBagObject moduleObject = configurationObject->getBagObject(module);

                // get the test log level, creating it if necessary
                if (not moduleObject->contains (name)) {
                    moduleObject->put (name, "WARN");
                }
                logLevel = logLevelFromText (moduleObject->getText (name));
            }

            return logLevel;
        }

        static TextSet getModuleDependencies (const Text& moduleName) {
            TextSet result;
            TextMap<UnitTest*> module = (*registry)[moduleName];
            for (TextMap<UnitTest*>::const_iterator iter = module.begin (); iter != module.end (); ++iter) {
                Text dependencies = iter->second->getDependencies();
                if (dependencies.length() > 0) {
                    vector<Text> dependencyArray = dependencies.split (",");
                    for (vector<Text>::iterator depIter = dependencyArray.begin (); depIter != dependencyArray.end (); ++depIter) {
                        result.put(*depIter);
                    }
                }
            }
            return result;
        }

        static void traverseDependencies (TextSet& touched, vector<Text>& targetDependencies, const Text& moduleName, const Text& forModuleName) {
            if (registry->contains(moduleName)) {
                if (not touched.contains(moduleName)) {
                    touched.put(moduleName);
                    TextSet dependencies = getModuleDependencies (moduleName);
                    for (TextSet::iterator iter = dependencies.begin (); iter != dependencies.end (); ++iter) {
                        traverseDependencies (touched, targetDependencies, *iter, moduleName);
                    }
                    targetDependencies.push_back(moduleName);
                }
            } else {
                // unknown dependency
                Log::error () << "Dependency (" << moduleName << ") of (" << forModuleName << ") is not a known test." << endl;
            }
        }

    public:
        UnitTest (const Text& _name, const Text& sourceFileName, bool _call) : name (_name), module (File (sourceFileName).getBasename ()), assertionsCount (0), call (_call) {
            if (not registry) {
                registry = new TextMap<TextMap<UnitTest*>> ();
            }
            (*registry)[module][name] = this;
        }

        virtual ~UnitTest () {}

        const Text& getName () const {
            return name;
        }

        const Text& getModule () const {
            return module;
        }

        static bool test (bool assertion) {
            if (currentUnitTest) {
                ++currentUnitTest->assertionsCount;
            }
           return assertion;
        }

        virtual void test () = 0;
        virtual Text getDependencies () const = 0;

        static void runOne (const Text& descriptor) {
            // test descriptors are given by "module.name" - if name is missing, we run all the
            // tests in the requested module - this method runs the requested tests, it does not
            // try to resolve dependencies
            vector<Text> pair = descriptor.split (".");
            TextMap<UnitTest*> module = (*registry)[pair[0]];
            if (pair.size() == 2) {
                module[pair[1]]->callTest();
            } else {
                for (TextMap<UnitTest*>::const_iterator iter = module.begin (); iter != module.end (); ++iter) {
                    iter->second->callTest();
                }
            }
        }

        static void runAll () {
            // the dependencies string with each registered test defines a graph that we have to
            // traverse to get the list of tests in the order we will run them
            TextSet touched;
            vector<Text> dependencies;
            for (typename TextMap<TextMap<UnitTest*>>::const_iterator iter = registry->begin (); iter != registry->end (); ++iter) {
                traverseDependencies (touched, dependencies, iter->first, "ALL");
            }
            for (vector<Text>::const_iterator iter = dependencies.begin (); iter != dependencies.end (); ++iter) {
                runOne (*iter);
            }
            /*
            for (TextMap<UnitTest*>::const_iterator iter = registry->begin (); iter != registry->end (); ++iter) {
                iter->second->callTest();
            }
            */
        }

        static void writeConfiguration () {
            Text json = configurationObject->toJson() << "\n";
            File configurationFile ("unit-test-configuration.json");
            configurationFile.writeText(json);
        }
};
