package nc.redstone.opt.test;


import static java.lang.System.currentTimeMillis;
import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;


public class JUnit5BenchmarkExtension
		implements BeforeAllCallback, BeforeTestExecutionCallback, AfterTestExecutionCallback, AfterAllCallback {

	private static final Namespace NAMESPACE = Namespace.create("nc", "redstone", "opt", "test", "JUnit5BenchmarkExtension");

	// EXTENSION POINTS

	@Override
	public void beforeAll(ExtensionContext context) {
		if (!shouldBeBenchmarked(context))
			return;

		storeNowAsLaunchTime(context, LaunchTimeKey.CLASS);
	}

	@Override
	public void beforeTestExecution(ExtensionContext context) {
		if (!shouldBeBenchmarked(context))
			return;

		storeNowAsLaunchTime(context, LaunchTimeKey.TEST);
	}

	@Override
	public void afterTestExecution(ExtensionContext context) {
		if (!shouldBeBenchmarked(context))
			return;

		long launchTime = loadLaunchTime(context, LaunchTimeKey.TEST);
		long elapsedTime = currentTimeMillis() - launchTime;
		report("Test", context, elapsedTime);
	}

	@Override
	public void afterAll(ExtensionContext context) {
		if (!shouldBeBenchmarked(context))
			return;

		long launchTime = loadLaunchTime(context, LaunchTimeKey.CLASS);
		long elapsedTime = currentTimeMillis() - launchTime;
		report("Test container", context, elapsedTime);
		
	}

	// HELPER

	private static boolean shouldBeBenchmarked(ExtensionContext context) {
		return context.getElement().map(el -> isAnnotated(el, JUnit5Benchmark.class)).orElse(false);
	}

	private static void storeNowAsLaunchTime(ExtensionContext context, LaunchTimeKey key) {
		context.getStore(NAMESPACE).put(key, currentTimeMillis());
	}

	private static long loadLaunchTime(ExtensionContext context, LaunchTimeKey key) {
		return context.getStore(NAMESPACE).get(key, long.class);
	}

	private static void report(String unit, ExtensionContext context, long elapsedTime) {
		String message = String.format("%d", elapsedTime);
		System.out.println(message);
		context.publishReportEntry("Benchmark", message);
	}

	private enum LaunchTimeKey {
		CLASS, TEST
	}

}
