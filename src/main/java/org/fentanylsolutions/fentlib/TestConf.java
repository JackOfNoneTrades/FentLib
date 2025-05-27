package org.fentanylsolutions.fentlib;

import org.fentanylsolutions.fentlib.carbonannotations.CarbonConfigAnnotations;
import org.fentanylsolutions.fentlib.carbonannotations.CarbonConfigAnnotations.ConfigInt;
import org.fentanylsolutions.fentlib.carbonannotations.CarbonConfigAnnotations.FentConfig;

@FentConfig(name = "testfentconf")
public class TestConf {

    @ConfigInt(
        name = "test_value",
        comment = "This is a test integer value",
        defaultValue = 5,
        min = -10,
        max = 10,
        category = "general")
    public static int testValue;

    @CarbonConfigAnnotations.ConfigArray(name = "testStrArray", category = "general", comment = "hmmmmm")
    public static String[] testStrings = { "bruh", "lmao" };

    @CarbonConfigAnnotations.ConfigArray(name = "testIntArray", category = "general", comment = "hmmmmm")
    public static int[] testInts = { 1, 3, -8 };

    @CarbonConfigAnnotations.ConfigArray(name = "testBoolArray", category = "general", comment = "hmmmmm")
    public static boolean[] testBools = { true, false, false };
}
