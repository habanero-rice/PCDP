package edu.rice.pcdp.divideandconquer;

import static edu.rice.pcdp.PCDP.async;
import static edu.rice.pcdp.PCDP.finish;

import java.util.Random;
import java.util.concurrent.Executors;

import org.junit.Test;

import edu.rice.pcdp.runtime.Runtime;

public class ArraySumDivideAndConquerTest {
//	private void arraySumLowerUpperSplit(int[] array, int min, int maxExclusive) {
//		int mid = (min + maxExclusive) / 2;
//		finish(() -> {
//			int[] subSums = { 0, 0 };
//			async(() -> {
//				for (int i = min; i < mid; i++) {
//					subSums[0] += array[i];
//				}
//			});
//			for (int i = mid; i < maxExclusive; i++) {
//				subSums[1] += array[i];
//			}
//		});
//	}

	private int arraySumKernel(int[] array, int min, int maxExclusive) {
		if( min == maxExclusive-1 ) {
			return array[min];
		} else {
			int mid = (min + maxExclusive) / 2;
			int[] subSums = { 0, 0 };
			finish(() -> {
				async(() -> {
					subSums[0] = arraySumKernel(array, min, mid);
				});
				subSums[1] = arraySumKernel(array, mid, maxExclusive);
			});
			return subSums[0] + subSums[1];
		}
	}

	private static int[] generateRandomArray(int length) {
		Random random = new Random();
		int[] result = new int[length];
		for (int i = 0; i < result.length; i++) {
			result[i] = random.nextInt();
		}
		return result;
	}

	@Test
	public void testArraySum() throws InterruptedException {
		Runtime.setExecutorService(Executors.newCachedThreadPool());
		System.out.println("testArraySum");
		int[] array = generateRandomArray(1000);
		int sum = arraySumKernel(array, 0, array.length);
		System.out.println(sum);
		System.out.println("done");
	}
}
