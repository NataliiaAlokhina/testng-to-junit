package org.example.recipes;

import java.util.Comparator;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.*;
import org.openrewrite.java.*;
import org.openrewrite.java.tree.J;

public class AddQuarkusTestAnnotation extends Recipe {

	@Override
	public String getDisplayName() {
		return "Add `@QuarkusTest` on test class";
	}

	@Override
	public String getDescription() {
		return "If class does not have `@QuarkusTest` annotation, this recipe will add annotation and import for it";
	}

	@Override
	protected TreeVisitor<?, ExecutionContext> getVisitor() {
		return new QuarkusTestAnnotationVisitor();
	}

	public static class QuarkusTestAnnotationVisitor extends JavaIsoVisitor<ExecutionContext> {

		private static final String IMPORT_COMPONENT = "io.quarkus.test.junit.QuarkusTest";

		private final JavaTemplate componentAnnotationTemplate = JavaTemplate.builder(this::getCursor, "@QuarkusTest")
				.javaParser(() -> JavaParser.fromJavaVersion().classpath("quarkus-junit5").build())
				.imports(IMPORT_COMPONENT).build();

		@Override
		public J.ClassDeclaration visitClassDeclaration(J.@NotNull ClassDeclaration classDecl,
				@NotNull ExecutionContext executionContext) {

			// get class declaration ACT
			J.ClassDeclaration cd = super.visitClassDeclaration(classDecl, executionContext);

			// Check if it does not contain @QuarkusTest annotation
			if (cd.getAllAnnotations().stream().noneMatch(new AnnotationMatcher("@QuarkusTest")::matches)) {

				// add import for @QuarkusTest annotation
				maybeAddImport(IMPORT_COMPONENT);

				// and add annotation to class
				cd = cd.withTemplate(componentAnnotationTemplate, cd.getCoordinates().addAnnotation(
						Comparator.comparing(J.Annotation::getSimpleName))); //Annotation is added but not the import

			}
			return cd;
		}
	}
}
