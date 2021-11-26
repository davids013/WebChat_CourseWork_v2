package methods;

public abstract class Methods {
    public static int testCounter;
    public static long startTime;
    public static long startTestTime;

    public static void start() {
        startTime = System.currentTimeMillis();
        System.out.println("\t\tTesting...");
        testCounter = 0;
    }

    public static void newTest() {
        testCounter++;
        System.out.println("\tTest " + testCounter + " started");
        startTestTime = System.currentTimeMillis();
    }

    public static void endTest() {
        System.out.println("\tTest " + testCounter + " completed in "
                + (System.currentTimeMillis() - startTestTime) + " ms\n");
    }

    public static void end() {
        System.out.println("\tTesting completed in "
                + (System.currentTimeMillis() - startTime) + " ms");
    }
}