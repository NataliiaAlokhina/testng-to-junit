package org.example.recipes;

import lombok.EqualsAndHashCode;
import lombok.Value;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.*;
import org.openrewrite.java.*;
import org.openrewrite.java.tree.J;

import java.time.Duration;
import java.util.Comparator;

@Value
@EqualsAndHashCode(callSuper = false)
public class AddQuarkusTestAnnotation extends Recipe {

	@Override
	public String getDisplayName() {
		return "Add `@QuarkusTest` annotation to test classes";
	}

	@Override
	public String getDescription() {
		return "Add `@QuarkusTest` annotation to test classes";
	}

	@Override
	public Duration getEstimatedEffortPerOccurrence() {
		return Duration.ofMinutes(5);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new QuarkusTestAnnotationVisitor();
	}

	public static class QuarkusTestAnnotationVisitor extends JavaIsoVisitor<ExecutionContext> {

		private static final String IMPORT_COMPONENT = "io.quarkus.test.junit.QuarkusTest";

		@Override
		public J.ClassDeclaration visitClassDeclaration(J.@NotNull ClassDeclaration classDecl,
				@NotNull ExecutionContext ctx) {

			// get class declaration ACT
			J.ClassDeclaration cd = super.visitClassDeclaration(classDecl, ctx);

			// Check if it does not contain @QuarkusTest annotation
			if (cd.getAllAnnotations().isEmpty()) {

				// add import for @QuarkusTest annotation
				maybeAddImport(IMPORT_COMPONENT);

				// and add annotation to class
				cd = JavaTemplate.builder("@QuarkusTest")
						.javaParser(JavaParser.fromJavaVersion().classpath("quarkus-junit5")).imports(IMPORT_COMPONENT)
						.build().apply(getCursor(),
								cd.getCoordinates().addAnnotation(Comparator.comparing(J.Annotation::getSimpleName)));
			}
			return cd;
		}
	}
}