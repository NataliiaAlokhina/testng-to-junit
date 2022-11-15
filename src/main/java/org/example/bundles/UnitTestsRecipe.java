package org.example.bundles;

import org.example.recipes.AddQuarkusTestAnnotation;
import org.example.recipes.ReplaceSimpleTestNgAnnotations;
import org.example.recipes.ReplaceTestNgAssert;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;


public class UnitTestsRecipe extends Recipe {

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
