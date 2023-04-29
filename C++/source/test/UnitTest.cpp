#include "UnitTest.h"

UnitTest* UnitTest::currentUnitTest = 0;
BagObject* UnitTest::configurationObject;
TextMap<TextMap<UnitTest*>>* UnitTest::registry;
