import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.CopyOnWriteArrayList;

public class PrimeNumberTask {

    // Клас для пошуку простих чисел в заданому діапазоні
    static class PrimeNumberCallable implements Callable<List<Integer>> {
        private final int start;
        private final int end;

        public PrimeNumberCallable(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public List<Integer> call() {
            List<Integer> primes = new ArrayList<>();
            for (int i = start; i <= end; i++) {
                if (isPrime(i)) {
                    primes.add(i);
                }
            }
            return primes;
        }

        // Метод для перевірки простоти числа
        private boolean isPrime(int num) {
            if (num <= 1) return false;
            for (int i = 2; i <= Math.sqrt(num); i++) {
                if (num % i == 0) return false;
            }
            return true;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int N = 0;

        // Запитуємо користувача ввести N, поки N не буде меншим або рівним 1000
        while (true) {
            System.out.println("Enter the value of N (upper bound, must be <= 1000): ");
            N = scanner.nextInt();
            if (N <= 1000) {
                break;  // Виходимо з циклу, якщо число коректне
            } else {
                System.out.println("N must be less than or equal to 1000. Please enter again.");
            }
        }

        // Задаємо кількість потоків і розмір ділянок
        int numThreads = 4;  // Кількість потоків
        int rangeSize = N / numThreads;  // Розмір ділянки для кожного потоку

        // Створюємо пул потоків
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<Future<List<Integer>>> futures = new ArrayList<>();

        // Розбиваємо діапазон на частини і створюємо Callable для кожного діапазону
        for (int i = 0; i < numThreads; i++) {
            int start = i * rangeSize;
            int end = (i + 1) * rangeSize - 1;
            if (i == numThreads - 1) {
                end = N;  // Останній потік обробляє залишок діапазону
            }

            PrimeNumberCallable task = new PrimeNumberCallable(start, end);
            futures.add(executorService.submit(task));
        }

        // Збираємо результати з усіх потоків
        CopyOnWriteArrayList<Integer> primes = new CopyOnWriteArrayList<>();
        long startTime = System.currentTimeMillis();

        for (Future<List<Integer>> future : futures) {
            try {
                List<Integer> result = future.get();  // Отримуємо результат з потоку
                primes.addAll(result);  // Додаємо прості числа до загального списку
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        long endTime = System.currentTimeMillis();

        // Виводимо результати
        System.out.println("Primes up to " + N + ": " + primes);
        System.out.println("Execution time: " + (endTime - startTime) + " milliseconds");

        // Завершуємо роботу з пулом потоків
        executorService.shutdown();
    }
}
