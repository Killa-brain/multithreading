import concurrent.CustomThreadPool;

public class Main {
    public static void main(String[] args) {
        CustomThreadPool threadPool = new CustomThreadPool(5);

        for (int i = 0; i < 10; i++) {
            int taskNumber = i;
            threadPool.execute(() -> {
                System.out.println("Task " + taskNumber + " is running on thread " + Thread.currentThread().getName());
            });
        }
        for (int i = 10; i < 20; i++) {
            int taskNumber = i;
            threadPool.execute(() -> {
                System.out.println("Task " + taskNumber + " is running on thread " + Thread.currentThread().getName());
            });
        }

        threadPool.shutdown();
        threadPool.awaitTermination();

        // must throw Exception with message: CustomThreadPool is already shut down
        threadPool.execute(() -> System.out.println("Check shutdown"));
    }
}