package org.example.bundles;

import org.example.recipes.*;
import org.openrewrite.*;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;

public class UnitTestsBundleRecipe extends Recipe {

	@Override
	public String getDisplayName() {
		return "TO-DO, TO-DOOO...";
	}

	@Override
	public String getDescription() {
		return "TO-DO, TO-DOOO...";
	}

	@Override
	protected TreeVisitor<?, ExecutionContext> getVisitor() {
		return new UnitTestsVisitors();
	}

	public static class UnitTestsVisitors extends JavaIsoVisitor<ExecutionContext> {

		@Override
		public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu, ExecutionContext executionContext) {

			String unitTestSuffix = "Test";
			String fileName = cu.getSourcePath().getFileName().toString();

			if (fileName.endsWith(unitTestSuffix)) {
				doAfterVisit(new ReplaceSimpleTestNgAnnotations());
				doAfterVisit(new ReplaceTestNgAssert());
				doAfterVisit(new AddQuarkusTestAnnotation());
			}

			return super.visitCompilationUnit(cu, executionContext);
		}
	}
}
