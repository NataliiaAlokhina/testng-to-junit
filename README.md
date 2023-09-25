# How to Migrate

## Expected result

- All your tests migrated to jUnit 5
- You removed all dependencies to TestNG and `platform:test` module from `quarkus-platform`
- You don't use anymore `microservice plugin - testing` gradle tasks (deprecated, will be removed soon, during migration
  plugin still exist)
- You have your own gradle tasks for each test suite which are using jUnit runner
- You updated Jenkins file, and it uses your new tasks and still publishing reports as before

## Set up

1. Clone this repo, then call `gradle publishToMavenLocal`
2. Open your project, where you'll migrate your tests
3. Adapt your `build.gradle` file same as this example

```groovy
plugins {
	// your code
	id 'org.openrewrite.rewrite' version '6.3.6'
}
rewrite {
	activeRecipe("org.example.bundles.UnitTestsBundleRecipe")
	exclusion("**/*.yaml")
}
dependencies {
	// your code
	rewrite "org.example.rewrite:recipes:{version}"
}
repositories {
	// your code
	mavenLocal()
	mavenCentral()
}
```

4. We're done with set up for rewrite!

## Don't do those things, or you'll struggle

1. Don't do migration all in bulk, migrate suite by suite and commit after each suite working or more often
2. TODO: Add some more

## Create gradle task with jUnit runner

This is not end result, you'll need this gradle task only for verifying your changes, will be modified at the end of
this migration guide. For now at the end of your `build.gradle` file add this task and ypu're ready to migrate unit
tests

```groovy
// ============================= NEW TESTING TASKS =============================
tasks.register('junitTest', Test) {

	description = 'Run junit tests'
	group = 'awesome migration'

	useJUnitPlatform()

	filter {
		includeTestsMatching "*Test"
	}
}
```

## Migrate Unit Test suite

At this suite most work will be done automatically, except "exception tests" (what an irony). You'll need manually
adjust tests annotated with `@Test(expectedExceptions = SomeException.class)`. Follow the steps

1. Call `gradle clean` in your project
2. Make sure `rewrite` block in your `build.gradle` is set
   to `activeRecipe("org.example.bundles.UnitTestsBundleRecipe")`
3. Call `gradle rewriteDryRun` and wait for results, it will generate patch file
   in `build/reports/rewrite/rewrite.patch` directory
4. Apply patch, it will add `@QuarkusTest` annotation to your test classes, will replace TestNG annotations and asserts
   with corresponding jUnit5 methods
5. Call `gradle compileTestJava`, you'll get failures for exception tests
6. Time to fix exception tests

#### How to fix exception tests

Let's go with example, how it should be reworked

```java
public class TestClass {
	// This is before 
	@Test(expectedExceptions = ModuleException.class)
	public void should_test_something() {
		// given some mockito code here   
		// when - then
		try {
			responseExceptionFilter.filter(requestContext, responseContext);
		} catch (ModuleException moduleException) {
			pfValidator.validateAndRethrowException(moduleException, PlatformErrorCode.SERVICE_NOT_WORKING_PROPERLY);
		} catch (IOException exception) {
			// do nothing
		} finally {
			verify(exceptionHandler).getModuleException(any());
		}
	}

	// And this is after
	@Test
	public void should_test_something() {
		// given some mockito code here   
		// when
		ModuleException exception = assertThrows(ModuleException.class, () -> {
			responseExceptionFilter.filter(requestContext, responseContext);
		});

		// then
		assertEquals(PlatformErrorCode.SERVICE_NOT_WORKING_PROPERLY, exception.getErrorCode());
		verify(exceptionHandler).getModuleException(any());
	}
}
```

Good luck! 