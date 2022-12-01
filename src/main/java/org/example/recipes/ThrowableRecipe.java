package org.example.recipes;

import org.openrewrite.*;
import org.openrewrite.java.*;
import org.openrewrite.java.tree.J;

public class ThrowableRecipe extends Recipe {
	@Override
	public String getDisplayName() {
		return "Rewrite test expected exceptions";
	}

	@Override
	protected TreeVisitor<?, ExecutionContext> getVisitor() {
		return new ExceptionTestVisitor();
	}

	public static class TryCatchVisitor extends JavaIsoVisitor<ExecutionContext> {
		private final String replaceTry = "var throwable = catchThrowable(()->{#{}});";
		private final JavaTemplate catchThrowable = JavaTemplate.builder(this::getCursor, replaceTry)
				.javaParser(() -> JavaParser.fromJavaVersion().classpath("assertj-core").build())
				.staticImports("org.assertj.core.api.Assertions.*").build();

		private final String replaceCatch = "assertThat(throwable).isInstanceOf(#{}.class)";
		private final JavaTemplate assertThrowable = JavaTemplate.builder(this::getCursor, replaceCatch)
				.javaParser(() -> JavaParser.fromJavaVersion().classpath("assertj-core").build())
				.staticImports("org.assertj.core.api.Assertions.*").build();

		// todo: remove try-catch code and place there catchThrowable and assertThrowable
	}

	public static class ExceptionTestVisitor extends JavaIsoVisitor<ExecutionContext> {
		@Override
		public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method,
				ExecutionContext executionContext) {

			// get all annotations on test method
			var annotationsList = method.getAllAnnotations();

			// check if it contains `@Test(expectedExceptions=)`
			var expectExceptionAnnotation = annotationsList.stream().map(J.Annotation::toString)
					.filter(e -> e.contains("@Test(expectedExceptions =")).findAny().orElse(null);

			// if yes, replace try block with catchThrowable
			if (expectExceptionAnnotation != null) {
				doAfterVisit(new TryCatchVisitor());
				doAfterVisit(new RemoveAnnotationVisitor(
						new AnnotationMatcher("@org.testng.annotations.Test(expectedExceptions=*)")));
				// todo: then add JUnit annotation on method
			}

			return super.visitMethodDeclaration(method, executionContext);
		}
	}
}
