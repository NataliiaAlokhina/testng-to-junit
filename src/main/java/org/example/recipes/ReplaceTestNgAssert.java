package org.example.recipes;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.AddImport;
import org.openrewrite.java.ChangeType;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.RemoveImport;
import org.openrewrite.java.tree.J;

public class ReplaceTestNgAssert extends Recipe {

    private static final String ORG_TESTNG_ASSERT ="org.testng.Assert";
    private static final String ORG_JUNIT_JUPITER_API_ASSERTIONS ="org.junit.jupiter.api.Assertions";

    @Override
    public String getDisplayName() {
        return "Replace TestNg assertions with JUnit assertions";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new AssertionsVisitor();
    }

    public static class AssertionsVisitor extends JavaIsoVisitor<ExecutionContext>{

        public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu, ExecutionContext executionContext) {

            doAfterVisit(new ChangeType(ORG_TESTNG_ASSERT, ORG_JUNIT_JUPITER_API_ASSERTIONS, false));
            doAfterVisit(new RemoveImport<>(ORG_TESTNG_ASSERT));
            doAfterVisit(new AddImport<>(ORG_JUNIT_JUPITER_API_ASSERTIONS, "*", true));

            return super.visitCompilationUnit(cu, executionContext);
        }
    }
}
