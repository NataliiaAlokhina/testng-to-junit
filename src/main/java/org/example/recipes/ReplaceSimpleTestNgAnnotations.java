package org.example.recipes;

import org.openrewrite.*;
import org.openrewrite.java.*;
import org.openrewrite.java.tree.J;

public class ReplaceSimpleTestNgAnnotations extends Recipe {

	private static final String ORG_TESTNG_ANNOTATIONS_TEST = "org.testng.annotations.Test";
	private static final String ORG_JUNIT_JUPITER_API_TEST = "org.junit.jupiter.api.Test";

	// ===============================================================
	private static final String ORG_TESTNG_ANNOTATIONS_BEFORE_METHOD = "org.testng.annotations.BeforeMethod";
	private static final String ORG_JUNIT_JUPITER_API_BEFORE_EACH = "org.junit.jupiter.api.BeforeEach";
	// ===============================================================
	private static final String ORG_TESTNG_ANNOTATIONS_AFTER_METHOD = "org.testng.annotations.AfterMethod";
	private static final String ORG_JUNIT_JUPITER_API_AFTER_EACH = "org.junit.jupiter.api.AfterEach";
	// ===============================================================
	private static final String ORG_TESTNG_ANNOTATIONS_BEFORE_CLASS = "org.testng.annotations.BeforeClass";
	private static final String ORG_JUNIT_JUPITER_API_BEFORE_ALL = "org.junit.jupiter.api.BeforeAll";
	// ===============================================================
	private static final String ORG_TESTNG_ANNOTATIONS_AFTER_CLASS = "org.testng.annotations.AfterClass";
	private static final String ORG_JUNIT_JUPITER_API_AFTER_ALL = "org.junit.jupiter.api.AfterAll";

	@Override
	public String getDisplayName() {
		return "Replace simple TestNG annotations without parameters";
	}

	@Override
	public String getDescription() {
		return String.format(
				"Annotations match TestNg -> JUnit: %n`@Test`: from %s to %s %n'@BeforeMethod': from %s to %s %n '@AfterMethod': from %s to %s %n '@BeforeTestClass': from %s to %s %n '@AfterTestClass': from %s to %s",
				ORG_TESTNG_ANNOTATIONS_TEST, ORG_JUNIT_JUPITER_API_TEST, ORG_TESTNG_ANNOTATIONS_BEFORE_METHOD,
				ORG_JUNIT_JUPITER_API_BEFORE_EACH, ORG_TESTNG_ANNOTATIONS_AFTER_METHOD,
				ORG_JUNIT_JUPITER_API_AFTER_EACH, ORG_TESTNG_ANNOTATIONS_BEFORE_CLASS, ORG_JUNIT_JUPITER_API_BEFORE_ALL,
				ORG_TESTNG_ANNOTATIONS_AFTER_CLASS, ORG_JUNIT_JUPITER_API_AFTER_ALL);
	}

	@Override
	protected TreeVisitor<?, ExecutionContext> getVisitor() {
		return new ReplaceSimpleAnnotationsVisitor();
	}

	public static class ReplaceSimpleAnnotationsVisitor extends JavaIsoVisitor<ExecutionContext> {
		@Override
		public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu, ExecutionContext executionContext) {

			// Test
			doAfterVisit(new ChangeType(ORG_TESTNG_ANNOTATIONS_TEST, ORG_JUNIT_JUPITER_API_TEST, false));
			doAfterVisit(new RemoveImport<>(ORG_TESTNG_ANNOTATIONS_TEST));
			doAfterVisit(new AddImport<>(ORG_JUNIT_JUPITER_API_TEST, "*", true));

			// Before test method
			doAfterVisit(
					new ChangeType(ORG_TESTNG_ANNOTATIONS_BEFORE_METHOD, ORG_JUNIT_JUPITER_API_BEFORE_EACH, false));
			doAfterVisit(new RemoveImport<>(ORG_TESTNG_ANNOTATIONS_BEFORE_METHOD));
			doAfterVisit(new AddImport<>(ORG_JUNIT_JUPITER_API_BEFORE_EACH, "*", true));

			// After test method
			doAfterVisit(new ChangeType(ORG_TESTNG_ANNOTATIONS_AFTER_METHOD, ORG_JUNIT_JUPITER_API_AFTER_EACH, false));
			doAfterVisit(new RemoveImport<>(ORG_TESTNG_ANNOTATIONS_AFTER_METHOD));
			doAfterVisit(new AddImport<>(ORG_JUNIT_JUPITER_API_AFTER_EACH, "*", true));

			// Before test class
			doAfterVisit(new ChangeType(ORG_TESTNG_ANNOTATIONS_BEFORE_CLASS, ORG_JUNIT_JUPITER_API_BEFORE_ALL, false));
			doAfterVisit(new RemoveImport<>(ORG_TESTNG_ANNOTATIONS_BEFORE_CLASS));
			doAfterVisit(new AddImport<>(ORG_JUNIT_JUPITER_API_BEFORE_ALL, "*", true));

			// After test class
			doAfterVisit(new ChangeType(ORG_TESTNG_ANNOTATIONS_AFTER_CLASS, ORG_JUNIT_JUPITER_API_AFTER_ALL, false));
			doAfterVisit(new RemoveImport<>(ORG_TESTNG_ANNOTATIONS_AFTER_CLASS));
			doAfterVisit(new AddImport<>(ORG_JUNIT_JUPITER_API_AFTER_ALL, "*", true));

			return super.visitCompilationUnit(cu, executionContext);
		}
	}
}
