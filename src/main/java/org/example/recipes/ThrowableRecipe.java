package org.example.recipes;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;

import java.util.ArrayList;

public class ThrowableRecipe extends Recipe {
    @Override
    public String getDisplayName() {
        return "Rewrite try catch exception";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new ExceptionTestVisitor();
    }

    public static class TryCatchVisitor extends JavaIsoVisitor<ExecutionContext> {
        private final String replaceTry = "var throwable = catchThrowable(()->{#{}});";
        private final JavaTemplate catchThrowable =
                JavaTemplate.builder(this::getCursor, replaceTry)
                        .javaParser(() -> JavaParser.fromJavaVersion()
                                .classpath("assertj-core").build())
                        .staticImports("org.assertj.core.api.Assertions.*")
                        .build();

        private final String replaceCatch = "assertThat(throwable).isInstanceOf(#{}.class)";
        private final JavaTemplate assertThrowable = JavaTemplate.builder(this::getCursor, replaceCatch)
                .javaParser(() -> JavaParser.fromJavaVersion()
                        .classpath("assertj-core").build())
                .staticImports("org.assertj.core.api.Assertions.*")
                .build();

        @Override
        public J.Block visitBlock(J.Block block, ExecutionContext executionContext) {
            var b = super.visitBlock(block, executionContext);


            return b;
        }

        @Override
        public J.Try visitTry(J.Try _try, ExecutionContext executionContext) {

            var tryStatements = _try.getBody().getStatements();
            var expectedTryReplacement = _try.withTemplate(catchThrowable, _try.getCoordinates().replace(), tryStatements);


            var catches = _try.getCatches();
            var expectedCatchReplacements = new ArrayList<>();
            for (J.Try.Catch aCatch : catches) {

            }


            return super.visitTry(_try, executionContext);
        }

        @Override
        public J.Try.Catch visitCatch(J.Try.Catch _catch, ExecutionContext executionContext) {
            _catch.getParameter();
            return super.visitCatch(_catch, executionContext);
        }


    }

    public static class ExceptionTestVisitor extends JavaIsoVisitor<ExecutionContext> {
        @Override
        public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext executionContext) {

            // get all annotations on test method
            var annotationsList = method.getAllAnnotations();

            // check if it contains `@Test(expectedExceptions=)`
            var expectExceptionAnnotation = annotationsList.stream().map(J.Annotation::toString)
                    .filter(e -> e.contains("@Test(expectedExceptions =")).findAny().orElse(null);

            // if yes, replace try block with catchThrowable
            if (expectExceptionAnnotation != null) {
                doAfterVisit(new TryCatchVisitor());
            }
            return super.visitMethodDeclaration(method, executionContext);
        }
    }
}
