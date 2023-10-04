# How to Migrate

## Expected result

- All your tests migrated to jUnit 5
- You removed all dependencies to TestNG and `platform:test` module from `quarkus-platform`
- You will reconfigure `microservice plugin - testing` gradle tasks (what to do with it should be discussed in Arch round)

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
	activeRecipe("org.example.bundles.ReplaceTestNGBundle")
	exclusion("**/*.yaml")
}
dependencies {
	// your code
	rewrite "org.example.rewrite:recipes:{version}"
    
    // dependency for integration test
    // if you're on Quarkus 3 use with jakarta classifier
	testImplementation ('com.github.database-rider:rider-cdi:1.41.0:jakarta') {
		exclude group: "stax"
	}
    // if you're on Quarkus 2 use regular one 
	testImplementation 'com.github.database-rider:rider-cdi:1.41.0'
    
}
repositories {
	// your code
	mavenLocal()
	mavenCentral()
}
```

4. We're done with set up for rewrite!


## Overwrite gradle task with jUnit runner

At the moment of this manual ops testing plugin uses TestNG runner, we will fix it. At the end of your `build.gradle` file add this

```groovy
/*--------------------------------------------
Overwrite testing tasks from ops plugin to use jUnit runner instead of TestNG runner
-------------------------------------------- */
def testTasks = [
		'unitTest',
		'migrationTest',
		'integrationTest',
		'componentTest'
]
project.tasks
		.matching({ t -> testTasks.contains(t.name) })
		.withType(Test.class)
		.each { task -> task.useJUnitPlatform() }
```
## Get rid of TestNG Annotations
This recipe will affect only tests files which ends with `Test`, `EmMig`, `IT`, `E2EComp`, other suffixes will be ignored.
1. Call `gradle clean` in your project
2. Make sure `rewrite` block in your `build.gradle` is set
   to `activeRecipe("org.example.bundles.ReplaceTestNGBundle")`
3. Call `gradle rewriteDryRun` and wait for results, it will generate patch file
   in `build/reports/rewrite/rewrite.patch` directory
4. Apply patch, it will replace TestNG annotations and asserts
   with corresponding jUnit5 methods

## Fix compilation for exception tests
Most tests don't need any changes after this step, except "exception tests" (what an irony). You'll need manually
adjust tests annotated with `@Test(expectedExceptions = SomeException.class)`. Follow the steps
1. Call `gradle compileTestJava`, you'll get failures for exception tests
2. Time to fix exception tests, unfortunately it is manual work

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
		// some move verify methods which you had before
	}
}
```
If you want to do it faster than copy-paste, you can create live-template for intelliJ -> https://www.jetbrains.com/help/idea/using-live-templates.html 
Here is snippet 
```
// when
$CLASS$ exception = org.junit.jupiter.api.Assertions.assertThrows($CLASS$.class, () -> {$METHOD$;});
// then
org.junit.jupiter.api.Assertions.assertEquals($ERROR_CODE$, exception.getErrorCode());
```
Do not forget remove injection of `PlatformValidator` into your tests class, you don't need it anymore.
Good luck! 

## Big bang time
I had not found nice way to do it without complete removal of platform dependencies. So go to your `build.gradle` file and remove this
```groovy
dependencies{
	testImplementation 'org.testng:testng:7.7.0'
	testImplementation group: 'com.fntsoftware.lib.quarkus', name: 'platform-test', version: "${platformVersion}"
    
    // optional, if you don't use it, you don't needed. Feel free to remove
	testImplementation 'io.rest-assured:rest-assured'
    
}
```
- Call `gradle clean compileJava compileTestJava`
- If any of your classes had `extends ApplicationJPAIT` remove this extends, you'll not need it anymore. 
- If you used class `ArgumentMatcher` from platform, depends on amount of usages or just temporary copy this class into your repo you'll fix it later, or fix it immediately if it is few usages. See Migration tests section to know how. 

## Migrate Unit Test -> *Test suite
1. After you fixed exception tests and removed dependencies time to run `gradle unitTest`, make sure your test runner works
2. Works? Cool, unit tests suite migrated. 

## Migrate Migration Test -> *EmMig suite
1. Add `@QuarkusTest` annotation to test class
2. Remove `extends ApplicationJPAIT` from test class definition
3. Add to class body `@Inject EntityManager em;`
4. Run `gradle migrationTest`
5. Suite migrated. 

## Migrate Integration test -> *IT suite
1. Make sure that you have DBRider dependency.
2. Add annotations to your test classes
```java
import com.github.database.rider.cdi.api.DBRider;
import com.github.database.rider.core.api.dataset.DataSet;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@DBRider
@DataSet(value = "servicerepositoryimplemit.xml")
@TestTransaction
public class ServiceRepositoryImplEmIT {}
```
3. Remove `META-INF` package with `orm.xml` and `persistence.xml` from `src/test/resources` if it exists
4. Add to `src/test/resources` new file `dbunit.yml` with content
```yaml
cacheConnection: false
caseInsensitiveStrategy: LOWERCASE
alwaysCleanBefore: true
properties:
  allowEmptyFields: true
```
5. Call `gradle IntegrationTest`
6. Suite migrated

## Migrate Component Test -> *E2EComp suite 
To be done later.... 

