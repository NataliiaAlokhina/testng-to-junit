package org.example.bundles;

import org.example.recipes.ReplaceSimpleTestNgAnnotations;
import org.example.recipes.ReplaceTestNgAssert;
import org.openrewrite.*;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;

public class ReplaceTestNGBundle extends Recipe {

	@Override
	public String getDisplayName() {
		return "Tests bundle";
	}

	@Override
	public String getDescription() {
		return "Tests bundle";
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new UnitTestsVisitors();
	}

	public static class UnitTestsVisitors extends JavaIsoVisitor<ExecutionContext> {
		@Override
		public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext ctx) {

			if (classDecl.getName().getSimpleName().endsWith("Test") || classDecl.getName().getSimpleName()
					.endsWith("EmMig") || classDecl.getName().getSimpleName().endsWith("IT") || classDecl.getName()
					.getSimpleName().endsWith("E2EComp")) {
				doAfterVisit(new ReplaceSimpleTestNgAnnotations().getVisitor());
				doAfterVisit(new ReplaceTestNgAssert().getVisitor());
			}

			return classDecl;
		}
	}
}
