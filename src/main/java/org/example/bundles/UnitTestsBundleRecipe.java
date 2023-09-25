package org.example.bundles;

import org.example.recipes.ReplaceSimpleTestNgAnnotations;
import org.example.recipes.ReplaceTestNgAssert;
import org.openrewrite.*;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;

public class UnitTestsBundleRecipe extends Recipe {

	@Override
	public String getDisplayName() {
		return "Unit Tests bundle";
	}

	@Override
	public String getDescription() {
		return "Unit Tests bundle";
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new UnitTestsVisitors();
	}

	public static class UnitTestsVisitors extends JavaIsoVisitor<ExecutionContext> {
		@Override
		public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext ctx) {

			if (classDecl.getName().getSimpleName().endsWith("Test")) {
				doAfterVisit(new ReplaceSimpleTestNgAnnotations().getVisitor());
				doAfterVisit(new ReplaceTestNgAssert().getVisitor());
			}

			return classDecl;
		}
	}
}
