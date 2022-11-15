package org.example.recipes;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;

public class ThrowableRecipe extends Recipe {
    @Override
    public String getDisplayName() {
        return "Rewrite try catch exception";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new TryCatchVisitor();
    }

    public static class TryCatchVisitor extends JavaIsoVisitor<ExecutionContext>{

        String codeSnippet = "var throwable = catchThrowable(()->{#{}});";

        private JavaTemplate catchThrowable = JavaTemplate.builder(this::getCursor,codeSnippet)
                .javaParser(()->JavaParser.fromJavaVersion().classpath("assertj-core").build())
                .staticImports("org.assertj.core.api.Assertions.*").build();

        @Override
        public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext executionContext) {

            // get all annotations on test method
            var annotationsList = method.getAllAnnotations();

            // check if it contains `@Test(expectedExceptions=)`
            var expectExceptionAnnotation = annotationsList.stream()
                    .filter(a->a.getSimpleName().equals("Test"))
                    .findAny()
                    .filter(e->e.getSideEffects().toString().contains("expectedExceptions"))
                            .orElse(null);

            // if yes, replace try block with catchThrowable
            if(expectExceptionAnnotation!=null){
                System.out.println("Nothing to do here yet");
            }

            return super.visitMethodDeclaration(method, executionContext);
        }

        @Override
        public J.Try visitTry(J.Try _try, ExecutionContext executionContext) {

            var r = _try.getBody().getStatements();


            return super.visitTry(_try, executionContext);
        }

        @Override
        public J.Try.Catch visitCatch(J.Try.Catch _catch, ExecutionContext executionContext) {
            _catch.getParameter();
            return super.visitCatch(_catch, executionContext);
        }


    }
}
