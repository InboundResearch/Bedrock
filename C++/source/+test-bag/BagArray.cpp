#include "Test.h"
#include "Bag.h"

TEST_CASE(EmptyBagArray) {
    PtrToBagArray bagArray = new BagArray ();
    TEST_EQUALS(bagArray->size (), 0);
    TEST_EQUALS(bagArray->toJson (), "[]");
}

TEST_CASE(BagArray) {
    PtrToBagArray bagArray = (new BagArray ())
        ->add ("Hello World")
        ->add (5)
        ->add (true)
        ->add (6.5);
    TEST_EQUALS(bagArray->size (), 4);
    TEST_EQUALS(bagArray->toJson (), "[\"Hello World\",5,true,6.50000000]");

    TEST_EQUALS(bagArray->get (0)->toJson (), "\"Hello World\"");
    TEST_EQUALS(bagArray->get (1)->toJson (), "5");
    TEST_EQUALS(bagArray->get (2)->toJson (), "true");
    TEST_EQUALS(bagArray->get (3)->toJson (), "6.50000000");
    TEST_EQUALS(bagArray->get (4), (BagThing*) 0);

    TEST_EQUALS(bagArray->get (0)->getType (), BagType::TEXT);
    TEST_EQUALS(bagArray->get (1)->getType (), BagType::INTEGER);
    TEST_EQUALS(bagArray->get (2)->getType (), BagType::BOOL);
    TEST_EQUALS(bagArray->get (3)->getType (), BagType::FLOAT);
}

TEST_CASE(BagArraySimpleSort) {
    //Log::Scope scope (Log::DEBUG);
    PtrToBagArray bagArray = new BagArray ();
    bagArray->add(5);
    bagArray->add(false);
    bagArray->add(5.6);
    bagArray->add("4.5");
    bagArray->add(4);
    bagArray->add(4.0);
    bagArray->add(true);
    bagArray->add(0.5);
    bagArray->add("0.75");
    Log::debug () << "TestBagArraySort: " << "unsorted " << bagArray->toJson() << endl;
    TEST_EQUALS(bagArray->get(0)->sortValue(), 5);

    PtrToBagArray sortedBagArray = BagArray::sort (bagArray);
    Log::debug () << "TestBagArraySort: " << "sorted " << sortedBagArray->toJson () << endl;
    TEST_EQUALS(bagArray->size(), sortedBagArray->size ());
    TEST_EQUALS(sortedBagArray->get(0)->sortValue(), 0);
    TEST_EQUALS(sortedBagArray->get(8)->sortValue(), 5.6);

    sortedBagArray = BagArray::sort (bagArray, DESCENDING);
    Log::debug () << "TestBagArraySort: " << "sorted " << sortedBagArray->toJson () << endl;
    TEST_EQUALS(bagArray->size(), sortedBagArray->size ());
    TEST_EQUALS(sortedBagArray->get(0)->sortValue(), 5.6);
    TEST_EQUALS(sortedBagArray->get(8)->sortValue(), 0);
}

TEST_CASE(BagArrayFieldSort) {
    //Log::Scope scope (Log::DEBUG);
    PtrToBagArray bagArray = new BagArray ();
    bagArray->add((new BagObject ())->put ("x", 5));
    bagArray->add((new BagObject ())->put ("x", false));
    bagArray->add((new BagObject ())->put ("x", 5.6));
    bagArray->add((new BagObject ())->put ("x", "4.5"));
    bagArray->add((new BagObject ())->put ("x", 4));
    bagArray->add((new BagObject ())->put ("x", 4.0));
    bagArray->add((new BagObject ())->put ("x", true));
    bagArray->add((new BagObject ())->put ("x", 0.5));
    bagArray->add((new BagObject ())->put ("x", "0.75"));
    Log::debug () << "TestBagArraySort: " << "unsorted " << bagArray->toJson() << endl;
    PtrToBagObject bagObject = bagArray->getBagObject(0);
    TEST_EQUALS(bagObject->get ("x")->sortValue(), 5);

    PtrToBagArray sortedBagArray = BagArray::sort (bagArray, "x");
    Log::debug () << "TestBagArraySort: " << "sorted " << sortedBagArray->toJson () << endl;
    TEST_EQUALS(bagArray->size(), sortedBagArray->size ());
    TEST_EQUALS(sortedBagArray->getBagObject(0)->get ("x")->sortValue(), 0);
    TEST_EQUALS(sortedBagArray->getBagObject(8)->get ("x")->sortValue(), 5.6);

    sortedBagArray = BagArray::sort (bagArray, "x", DESCENDING);
    Log::debug () << "TestBagArraySort: " << "sorted " << sortedBagArray->toJson () << endl;
    TEST_EQUALS(bagArray->size(), sortedBagArray->size ());
    TEST_EQUALS(sortedBagArray->getBagObject(0)->get ("x")->sortValue(), 5.6);
    TEST_EQUALS(sortedBagArray->getBagObject(8)->get ("x")->sortValue(), 0);
}

TEST_CASE(BagArrayDbSort) {
    //Log::Scope scope (Log::DEBUG);
    PtrToBagArray bagArray = new BagArray ();
    bagArray->add((new BagObject ())->put ("x", 5)->put ("y", 1));
    bagArray->add((new BagObject ())->put ("x", 5)->put ("y", 2));
    bagArray->add((new BagObject ())->put ("x", 5)->put ("y", 3));
    bagArray->add((new BagObject ())->put ("x", 4)->put ("y", 1));
    bagArray->add((new BagObject ())->put ("x", 4)->put ("y", 2));
    bagArray->add((new BagObject ())->put ("x", 4)->put ("y", 3));
    bagArray->add((new BagObject ())->put ("x", 3)->put ("y", 1));
    bagArray->add((new BagObject ())->put ("x", 3)->put ("y", 2));
    bagArray->add((new BagObject ())->put ("x", 3)->put ("y", 3));
    Log::debug () << "TestBagArraySort: " << "unsorted (" << bagArray->toJson() << ")" << endl;
    PtrToBagObject bagObject = bagArray->getBagObject(0);
    TEST_EQUALS(bagObject->get ("x")->sortValue(), 5);
    TEST_EQUALS(bagObject->get ("y")->sortValue(), 1);

    PtrToBagArray sortedBagArray = BagArray::sort (bagArray, DbSort ("x")("y", DESCENDING));
    Log::debug () << "TestBagArraySort: " << "sorted (" << sortedBagArray->toJson () << ")" << endl;
    TEST_EQUALS(bagArray->size(), sortedBagArray->size ());
    bagObject = sortedBagArray->getBagObject(0);
    TEST_EQUALS(bagObject->get ("x")->sortValue(), 3);
    TEST_EQUALS(bagObject->get ("y")->sortValue(), 3);
    bagObject = sortedBagArray->getBagObject(8);
    TEST_EQUALS(bagObject->get ("x")->sortValue(), 5);
    TEST_EQUALS(bagObject->get ("y")->sortValue(), 1);

    sortedBagArray = BagArray::sort (bagArray, DbSort ("y")("x", DESCENDING));
    Log::debug () << "TestBagArraySort: " << "sorted (" << sortedBagArray->toJson () << ")" << endl;
    TEST_EQUALS(bagArray->size(), sortedBagArray->size ());
    bagObject = sortedBagArray->getBagObject(0);
    TEST_EQUALS(bagObject->get ("x")->sortValue(), 5);
    TEST_EQUALS(bagObject->get ("y")->sortValue(), 1);
    bagObject = sortedBagArray->getBagObject(8);
    TEST_EQUALS(bagObject->get ("x")->sortValue(), 3);
    TEST_EQUALS(bagObject->get ("y")->sortValue(), 3);
}
